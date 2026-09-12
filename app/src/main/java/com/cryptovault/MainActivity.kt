package com.cryptovault

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent { MaterialTheme { CryptoVaultApp(this) } }
    }
}

@Composable
private fun CryptoVaultApp(activity: FragmentActivity) {
    val secretStore = remember { WalletSecretStore(activity) }
    val metadata = remember { WalletMetadataStore(activity) }
    val biometric = remember { BiometricGate(activity) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var tab by remember { mutableStateOf("Portfolio") }
    var addresses by remember { mutableStateOf(metadata.load()) }
    var mnemonic by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var showSeed by remember { mutableStateOf(false) }
    var importSeed by remember { mutableStateOf("") }
    var btcRecipient by remember { mutableStateOf("") }
    var btcAmount by remember { mutableStateOf("") }
    var btcReview by remember { mutableStateOf<BitcoinSendPlan?>(null) }
    var evmNetwork by remember { mutableStateOf(Network.ETHEREUM) }
    var evmRecipient by remember { mutableStateOf("") }
    var evmAmount by remember { mutableStateOf("") }
    var evmReview by remember { mutableStateOf<PreparedEvmTransfer?>(null) }
    var evmMainnetAck by remember { mutableStateOf(false) }
    var sendMode by remember { mutableStateOf("Native") }
    var tokenContract by remember { mutableStateOf("") }
    var tokenDecimals by remember { mutableStateOf<Int?>(null) }
    val txService = remember { RealTransactionService() }
    val tokenService = remember { TokenService() }

    fun createWallet() {
        runCatching { WalletCoreService.create() }.onSuccess { (seed, a) ->
            secretStore.saveMnemonic(seed); metadata.save(a); addresses = a; mnemonic = seed
            notice = "Кошелёк создан и зашифрован локально. Seed не отправляется в сеть."
        }.onFailure { notice = it.message ?: "Ошибка создания" }
    }
    fun restoreWallet() {
        runCatching { WalletCoreService.restore(importSeed).also { a -> secretStore.saveMnemonic(importSeed); metadata.save(a); addresses = a; mnemonic = importSeed } }
            .onSuccess { importSeed = ""; notice = "Кошелёк восстановлен локально." }
            .onFailure { notice = it.message ?: "Неверная seed-фраза" }
    }

    Scaffold(bottomBar = {
        NavigationBar { listOf("Portfolio","Send","ETH/BNB","Receive","Settings").forEach { item ->
            NavigationBarItem(selected=tab==item,onClick={tab=item},icon={},label={Text(item)})
        }}
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Text("CryptoVault", style=MaterialTheme.typography.headlineMedium) }
            item { Text("Self-custody • локальная подпись • mainnet/testnet") }
            when (tab) {
                "Portfolio" -> {
                    item { Text("Портфель", style=MaterialTheme.typography.titleLarge) }
                    if (addresses == null) {
                        item { Button(onClick=::createWallet) { Text("Создать кошелёк") } }
                        item { OutlinedTextField(importSeed,{importSeed=it},label={Text("Seed-фраза для восстановления")},modifier=Modifier.fillMaxWidth()) }
                        item { Button(enabled=importSeed.isNotBlank(),onClick=::restoreWallet) { Text("Восстановить") } }
                    } else {
                        item { Text("Кошелёк сохранён. Секрет зашифрован Android Keystore.") }
                        item { AssetCard("BTC", addresses!!.btc) }
                        item { AssetCard("ETH", addresses!!.eth) }
                        item { AssetCard("BNB", addresses!!.bnb) }
                        item { Button(onClick={tab="Send"}) { Text("Отправить BTC") } }
                        item { Button(onClick={tab="ETH/BNB"}) { Text("Отправить ETH/BNB") } }
                    }
                }
                "Receive" -> {
                    item { Text("Получить", style=MaterialTheme.typography.titleLarge) }
                    addresses?.let { a ->
                        item { Text("BTC\n${a.btc}") }
                        item { Text("ETH (Ethereum)\n${a.eth}") }
                        item { Text("BNB (BNB Smart Chain, тот же адрес формата 0x)\n${a.bnb}") }
                    } ?: item { Text("Сначала создайте или восстановите кошелёк.") }
                }
                "Send" -> {
                    item { Text("Bitcoin mainnet — отправка", style=MaterialTheme.typography.titleLarge) }
                    item { OutlinedTextField(btcRecipient,{btcRecipient=it},label={Text("Адрес получателя")},modifier=Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(btcAmount,{btcAmount=it},label={Text("BTC")},modifier=Modifier.fillMaxWidth()) }
                    item { Button(enabled=addresses!=null && btcRecipient.isNotBlank() && btcAmount.toDoubleOrNull()!=null,onClick={
                        val a=addresses!!; val amount=(btcAmount.toBigDecimal()*java.math.BigDecimal("100000000")).toLong()
                        executor.execute {
                            runCatching {
                                val net=BitcoinNetwork.MAINNET; val client=BitcoinNetworkClient(); val fees=client.feeRates(net); val utxos=client.utxos(net,a.btc)
                                val feeRate=fees.halfHour; val roughFee=(68L*utxos.size+31L)*feeRate
                                val rough=BitcoinTransactionService().prepare(net,a.btc,btcRecipient,amount,roughFee,feeRate,utxos)
                                val exact=BitcoinSigner.exactPlan(rough, secretStore.loadMnemonic())
                                rough.copy(feeSats=exact.feeSats, changeSats=exact.changeSats)
                            }.onSuccess { plan -> activity.runOnUiThread { btcReview=plan; notice="Проверьте адрес, сумму, UTXO и комиссию перед подписью." } }
                             .onFailure { e -> activity.runOnUiThread { notice=e.message ?: "Не удалось подготовить BTC-транзакцию" } }
                        }
                    }) { Text("Подготовить транзакцию") } }
                    btcReview?.let { plan ->
                        item { Text("Проверка: ${plan.amountSats} sat + ~${plan.feeSats} sat fee; change ${plan.changeSats} sat; UTXO ${plan.inputs.size}") }
                        item { Button(onClick={
                            TransactionPolicyV3.requireBitcoin(plan)
                            biometric.authenticate(onSuccess={
                                executor.execute {
                                    runCatching { BitcoinSigner.sign(plan, secretStore.loadMnemonic()) }.onSuccess { signed ->
                                        runCatching { BitcoinNetworkClient().broadcast(plan.network, signed.rawHex) }
                                        .onSuccess { txid -> activity.runOnUiThread { notice="BTC отправлен. TXID: $txid"; btcReview=null } }
                                        .onFailure { e -> activity.runOnUiThread { notice=e.message ?: "Ошибка отправки BTC" } }
                                    }.onFailure { e -> activity.runOnUiThread { notice=e.message ?: "Ошибка подписи/отправки" } }
                                }
                            },onFailure={notice=it})
                        }) { Text("ПОДТВЕРДИТЬ, ПОДПИСАТЬ И ОТПРАВИТЬ") } }
                    }
                }
                "ETH/BNB" -> {
                    item { Text("EVM mainnet — отправка", style=MaterialTheme.typography.titleLarge) }
                    item { Text(RealFundsNotice.TEXT) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(Network.ETHEREUM, Network.BNB, Network.SEPOLIA).forEach { n ->
                                FilterChip(selected = evmNetwork == n, onClick = { evmNetwork = n; evmReview = null; tokenDecimals = null }, label = { Text(n.name) })
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Native", "Token").forEach { m ->
                                FilterChip(selected = sendMode == m, onClick = { sendMode = m; evmReview = null; tokenDecimals = null }, label = { Text(if (m=="Native") "ETH/BNB" else "ERC-20 токен") })
                            }
                        }
                    }
                    if (sendMode == "Token") {
                        item { OutlinedTextField(tokenContract,{tokenContract=it; tokenDecimals=null},label={Text("Адрес контракта токена (0x...)")},modifier=Modifier.fillMaxWidth()) }
                        item { Button(enabled = tokenContract.matches(Regex("^0x[0-9a-fA-F]{40}$")), onClick = {
                            executor.execute {
                                runCatching { tokenService.decimals(Asset("TOKEN","",0, tokenContract, evmNetwork)) }
                                    .onSuccess { d -> activity.runOnUiThread { tokenDecimals = d; notice = "Контракт отвечает. Decimals: $d" } }
                                    .onFailure { e -> activity.runOnUiThread { notice = e.message ?: "Не удалось прочитать decimals() у контракта" } }
                            }
                        }) { Text("Проверить контракт (decimals)") } }
                        tokenDecimals?.let { d -> item { Text("Decimals контракта: $d") } }
                    }
                    item { OutlinedTextField(evmRecipient,{evmRecipient=it},label={Text("Адрес получателя (0x...)")},modifier=Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(evmAmount,{evmAmount=it},label={Text(if (sendMode=="Token") "Сумма токена" else if(evmNetwork==Network.BNB) "BNB" else "ETH")},modifier=Modifier.fillMaxWidth()) }
                    if (evmNetwork != Network.SEPOLIA) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = evmMainnetAck, onCheckedChange = { evmMainnetAck = it })
                                Text("Понимаю: это mainnet, операция необратима и переводит реальные средства")
                            }
                        }
                    }
                    item { Button(enabled = addresses!=null && evmRecipient.isNotBlank() && evmAmount.toDoubleOrNull()!=null &&
                            (evmNetwork==Network.SEPOLIA || evmMainnetAck) &&
                            (sendMode=="Native" || tokenDecimals!=null), onClick={
                        val a=addresses!!
                        executor.execute {
                            runCatching {
                                MainnetGuard.requireExplicitAcknowledgement(evmNetwork, evmNetwork==Network.SEPOLIA || evmMainnetAck)
                                if (sendMode == "Native") {
                                    val amountWei = evmAmount.toBigDecimal().multiply(java.math.BigDecimal.TEN.pow(18)).toBigInteger()
                                    txService.prepareNative(evmNetwork, a.eth, evmRecipient, amountWei)
                                } else {
                                    val d = tokenDecimals ?: error("Сначала проверьте контракт")
                                    val amountBase = evmAmount.toBigDecimal().multiply(java.math.BigDecimal.TEN.pow(d)).toBigInteger()
                                    txService.prepareErc20(evmNetwork, a.eth, tokenContract, evmRecipient, amountBase)
                                }.also { TransactionPolicyV3.requireEvm(it) }
                            }.onSuccess { p -> activity.runOnUiThread { evmReview=p; notice="Проверьте адрес, сумму, газ и сеть перед подписью." } }
                             .onFailure { e -> activity.runOnUiThread { notice=e.message ?: "Не удалось подготовить транзакцию" } }
                        }
                    }) { Text("Подготовить транзакцию") } }
                    evmReview?.let { p ->
                        item { Text("Сеть: ${p.network.name} (chainId ${p.network.chainId})") }
                        if (p.tokenContract != null) {
                            item { Text("Токен-контракт: ${p.tokenContract}") }
                            item { Text("Сумма (base units): ${p.tokenAmount}") }
                        } else {
                            item { Text("Сумма: $evmAmount ${if(evmNetwork==Network.BNB) "BNB" else "ETH"}") }
                        }
                        item { Text("nonce ${p.nonce}, gasLimit ${p.gasLimit}, gasPrice ${p.gasPriceWei} wei") }
                        item { Text("Оценка комиссии сети: ${p.feeNative()} ${if(evmNetwork==Network.BNB) "BNB" else "ETH"}") }
                        item { Button(onClick={
                            biometric.authenticate(onSuccess={
                                executor.execute {
                                    runCatching { txService.signAndBroadcast(p, secretStore.loadMnemonic(), explicitUserConfirmation = true) }
                                        .onSuccess { hash -> activity.runOnUiThread { notice="Транзакция отправлена. Hash: $hash"; evmReview=null; evmMainnetAck=false } }
                                        .onFailure { e -> activity.runOnUiThread { notice=e.message ?: "Ошибка подписи/отправки" } }
                                }
                            },onFailure={notice=it})
                        }) { Text("ПОДТВЕРДИТЬ, ПОДПИСАТЬ И ОТПРАВИТЬ") } }
                    }
                }
                "Settings" -> {
                    item { Text("Безопасность", style=MaterialTheme.typography.titleLarge) }
                    item { Text("FLAG_SECURE • Android Keystore • BiometricPrompt • секреты не отправляются RPC/indexer") }
                    item { Button(enabled=secretStore.hasWallet(),onClick={
                        biometric.authenticate(onSuccess={ showSeed=true; mnemonic=secretStore.loadMnemonic() },onFailure={notice=it})
                    }) { Text("Показать seed после биометрии") } }
                    if(showSeed && mnemonic.isNotBlank()) item { Text(mnemonic) }
                    item { Text(RealFundsNotice.TEXT) }
                    if (notice.isNotBlank()) item { Text(notice) }
                }
            }
        }
    }
}

@Composable private fun AssetCard(symbol:String,address:String) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(symbol, style=MaterialTheme.typography.titleMedium); Text(address) } }
}
