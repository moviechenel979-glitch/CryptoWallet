package com.cryptovault

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

private val VaultBg = Color(0xFF0B0F1A)
private val VaultSurface = Color(0xFF141A2A)
private val VaultCard = Color(0xFF1A2236)
private val VaultAccent = Color(0xFF6C8CFF)
private val VaultAccent2 = Color(0xFF00D4AA)
private val VaultDanger = Color(0xFFFF6B6B)
private val VaultMuted = Color(0xFF8B95A8)
private val VaultText = Color(0xFFF2F5FF)

private val VaultColors = darkColorScheme(
    primary = VaultAccent,
    onPrimary = Color.White,
    secondary = VaultAccent2,
    onSecondary = Color.Black,
    background = VaultBg,
    onBackground = VaultText,
    surface = VaultSurface,
    onSurface = VaultText,
    surfaceVariant = VaultCard,
    onSurfaceVariant = VaultMuted,
    error = VaultDanger,
    outline = Color(0xFF2A3548)
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            MaterialTheme(
                colorScheme = VaultColors,
                typography = Typography(
                    headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = VaultBg) {
                    CryptoVaultApp(this)
                }
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Portfolio("Портфель", Icons.Default.AccountBalanceWallet),
    Send("BTC", Icons.Default.Toll),
    Evm("EVM", Icons.AutoMirrored.Filled.Send),
    Receive("Получить", Icons.Default.QrCode2),
    Settings("Ещё", Icons.Default.Settings)
}

@Composable
private fun CryptoVaultApp(activity: FragmentActivity) {
    val secretStore = remember { WalletSecretStore(activity) }
    val metadata = remember { WalletMetadataStore(activity) }
    val biometric = remember { BiometricGate(activity) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var tab by remember { mutableStateOf(Tab.Portfolio) }
    var addresses by remember { mutableStateOf(metadata.load()) }
    var mnemonic by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    var showSeed by remember { mutableStateOf(false) }
    var importSeed by remember { mutableStateOf("") }
    var btcRecipient by remember { mutableStateOf("") }
    var btcAmount by remember { mutableStateOf("") }
    var btcReview by remember { mutableStateOf<BitcoinSendPlan?>(null) }
    var evmNetwork by remember { mutableStateOf(Network.SEPOLIA) }
    var evmRecipient by remember { mutableStateOf("") }
    var evmAmount by remember { mutableStateOf("") }
    var evmReview by remember { mutableStateOf<PreparedEvmTransfer?>(null) }
    var evmMainnetAck by remember { mutableStateOf(false) }
    var sendMode by remember { mutableStateOf("Native") }
    var tokenContract by remember { mutableStateOf("") }
    var tokenDecimals by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    val txService = remember { RealTransactionService() }
    val tokenService = remember { TokenService() }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            notice = null
        }
    }

    fun createWallet() {
        busy = true
        runCatching { WalletCoreService.create() }.onSuccess { (seed, a) ->
            secretStore.saveMnemonic(seed)
            metadata.save(a)
            addresses = a
            mnemonic = seed
            showSeed = true
            notice = "Кошелёк создан. Сохраните seed в безопасном месте."
        }.onFailure { notice = it.message ?: "Ошибка создания" }
        busy = false
    }

    fun restoreWallet() {
        busy = true
        runCatching {
            WalletCoreService.restore(importSeed).also { a ->
                secretStore.saveMnemonic(importSeed)
                metadata.save(a)
                addresses = a
            }
        }.onSuccess {
            importSeed = ""
            notice = "Кошелёк восстановлен"
        }.onFailure { notice = it.message ?: "Неверная seed-фраза" }
        busy = false
    }

    Scaffold(
        containerColor = VaultBg,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = VaultCard,
                    contentColor = VaultText,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = VaultSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, Color(0xFF1E2A3D))
            ) {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VaultAccent,
                            selectedTextColor = VaultAccent,
                            unselectedIconColor = VaultMuted,
                            unselectedTextColor = VaultMuted,
                            indicatorColor = VaultAccent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { pad ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(VaultAccent, VaultAccent2))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("CryptoVault", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = VaultText)
                        Text("Self-custody · локальная подпись", color = VaultMuted, fontSize = 13.sp)
                    }
                }
            }

            when (tab) {
                Tab.Portfolio -> {
                    if (addresses == null) {
                        item {
                            HeroCard(
                                title = "Добро пожаловать",
                                subtitle = "Создайте новый кошелёк или восстановите из seed-фразы. Ключи шифруются на устройстве."
                            )
                        }
                        item {
                            PrimaryButton("Создать кошелёк", enabled = !busy, onClick = ::createWallet)
                        }
                        item {
                            SectionLabel("Или восстановить")
                        }
                        item {
                            VaultTextField(
                                value = importSeed,
                                onValueChange = { importSeed = it },
                                label = "Seed-фраза (12–24 слова)",
                                singleLine = false
                            )
                        }
                        item {
                            SecondaryButton(
                                "Восстановить",
                                enabled = importSeed.isNotBlank() && !busy,
                                onClick = ::restoreWallet
                            )
                        }
                    } else {
                        item {
                            BalanceHero()
                        }
                        item { SectionLabel("Активы") }
                        item { AssetRow("BTC", "Bitcoin", addresses!!.btc, Color(0xFFF7931A)) }
                        item { AssetRow("ETH", "Ethereum", addresses!!.eth, Color(0xFF627EEA)) }
                        item { AssetRow("BNB", "BNB Chain", addresses!!.bnb, Color(0xFFF3BA2F)) }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) {
                                    PrimaryButton("Отправить BTC", onClick = { tab = Tab.Send })
                                }
                                Box(Modifier.weight(1f)) {
                                    SecondaryButton("Отправить EVM", onClick = { tab = Tab.Evm })
                                }
                            }
                        }
                        if (showSeed && mnemonic.isNotBlank()) {
                            item {
                                WarningCard("Сохраните seed. Он больше не будет показан без биометрии.")
                            }
                            item {
                                SeedBox(mnemonic)
                            }
                        }
                    }
                }

                Tab.Receive -> {
                    item { SectionLabel("Ваши адреса") }
                    addresses?.let { a ->
                        item { AddressCard("Bitcoin", a.btc, Color(0xFFF7931A)) }
                        item { AddressCard("Ethereum", a.eth, Color(0xFF627EEA)) }
                        item { AddressCard("BNB Smart Chain", a.bnb, Color(0xFFF3BA2F)) }
                        item {
                            Text(
                                "Отправляйте только совместимые активы на соответствующий адрес.",
                                color = VaultMuted,
                                fontSize = 13.sp
                            )
                        }
                    } ?: item {
                        WarningCard("Сначала создайте или восстановите кошелёк.")
                    }
                }

                Tab.Send -> {
                    item { SectionLabel("Bitcoin · Mainnet") }
                    item {
                        VaultTextField(btcRecipient, { btcRecipient = it }, "Адрес получателя")
                    }
                    item {
                        VaultTextField(btcAmount, { btcAmount = it }, "Сумма (BTC)")
                    }
                    item {
                        PrimaryButton(
                            "Подготовить",
                            enabled = addresses != null && btcRecipient.isNotBlank() && btcAmount.toDoubleOrNull() != null && !busy
                        ) {
                            val a = addresses!!
                            val amount = (btcAmount.toBigDecimal() * java.math.BigDecimal("100000000")).toLong()
                            busy = true
                            executor.execute {
                                runCatching {
                                    val client = BitcoinNetworkClient()
                                    val utxos = client.utxos(BitcoinNetwork.MAINNET, a.btc)
                                    val fees = client.feeRates(BitcoinNetwork.MAINNET)
                                    val feeRate = fees.halfHour
                                    val roughFee = 250L * feeRate
                                    val rough = BitcoinTransactionService().prepare(
                                        BitcoinNetwork.MAINNET, a.btc, btcRecipient, amount, roughFee, feeRate, utxos
                                    )
                                    val exact = BitcoinSigner.exactPlan(rough, secretStore.loadMnemonic())
                                    BitcoinTransactionService().prepare(
                                        BitcoinNetwork.MAINNET, a.btc, btcRecipient, amount, exact.feeSats, feeRate, utxos
                                    )
                                }.onSuccess { plan ->
                                    activity.runOnUiThread {
                                        btcReview = plan
                                        notice = "Проверьте адрес и комиссию"
                                        busy = false
                                    }
                                }.onFailure { e ->
                                    activity.runOnUiThread {
                                        notice = e.message ?: "Ошибка подготовки"
                                        busy = false
                                    }
                                }
                            }
                        }
                    }
                    btcReview?.let { plan ->
                        item {
                            ReviewCard(
                                lines = listOf(
                                    "Кому" to plan.to,
                                    "Сумма" to "$btcAmount BTC",
                                    "Комиссия" to "${plan.feeSats} sats",
                                    "Сдача" to "${plan.changeSats} sats",
                                    "Входов" to "${plan.inputs.size}"
                                )
                            )
                        }
                        item {
                            DangerButton("Подтвердить и отправить") {
                                biometric.authenticate(
                                    onSuccess = {
                                        executor.execute {
                                            runCatching {
                                                val signed = BitcoinSigner.sign(plan, secretStore.loadMnemonic())
                                                BitcoinNetworkClient().broadcast(BitcoinNetwork.MAINNET, signed.rawHex)
                                                signed.txId
                                            }.onSuccess { txid ->
                                                activity.runOnUiThread {
                                                    notice = "Отправлено · $txid"
                                                    btcReview = null
                                                }
                                            }.onFailure { e ->
                                                activity.runOnUiThread {
                                                    notice = e.message ?: "Ошибка отправки"
                                                }
                                            }
                                        }
                                    },
                                    onFailure = { notice = it }
                                )
                            }
                        }
                    }
                }

                Tab.Evm -> {
                    item { SectionLabel("Ethereum / BNB / Sepolia") }
                    item {
                        NetworkChips(evmNetwork) {
                            evmNetwork = it
                            evmReview = null
                            tokenDecimals = null
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = sendMode == "Native",
                                onClick = { sendMode = "Native"; evmReview = null },
                                label = { Text("Native") },
                                colors = chipColors()
                            )
                            FilterChip(
                                selected = sendMode == "Token",
                                onClick = { sendMode = "Token"; evmReview = null },
                                label = { Text("ERC-20") },
                                colors = chipColors()
                            )
                        }
                    }
                    if (sendMode == "Token") {
                        item {
                            VaultTextField(tokenContract, { tokenContract = it; tokenDecimals = null }, "Контракт токена (0x…)")
                        }
                        item {
                            SecondaryButton("Проверить контракт", enabled = tokenContract.startsWith("0x") && tokenContract.length == 42) {
                                executor.execute {
                                    runCatching {
                                        tokenService.decimals(evmNetwork, tokenContract)
                                    }.onSuccess { d ->
                                        activity.runOnUiThread {
                                            tokenDecimals = d
                                            notice = "Decimals: $d"
                                        }
                                    }.onFailure { e ->
                                        activity.runOnUiThread {
                                            notice = e.message ?: "Не удалось прочитать контракт"
                                        }
                                    }
                                }
                            }
                        }
                        tokenDecimals?.let {
                            item { Text("Decimals: $it", color = VaultAccent2, fontSize = 13.sp) }
                        }
                    }
                    item { VaultTextField(evmRecipient, { evmRecipient = it }, "Получатель (0x…)") }
                    item {
                        VaultTextField(
                            evmAmount,
                            { evmAmount = it },
                            if (sendMode == "Token") "Сумма токена" else if (evmNetwork == Network.BNB) "BNB" else "ETH"
                        )
                    }
                    if (evmNetwork != Network.SEPOLIA) {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VaultDanger.copy(alpha = 0.12f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = evmMainnetAck,
                                    onCheckedChange = { evmMainnetAck = it },
                                    colors = CheckboxDefaults.colors(checkedColor = VaultDanger)
                                )
                                Text(
                                    "Mainnet: реальные средства, операция необратима",
                                    color = VaultText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    item {
                        PrimaryButton(
                            "Подготовить транзакцию",
                            enabled = addresses != null &&
                                evmRecipient.isNotBlank() &&
                                evmAmount.toDoubleOrNull() != null &&
                                (evmNetwork == Network.SEPOLIA || evmMainnetAck) &&
                                (sendMode == "Native" || tokenDecimals != null) &&
                                !busy
                        ) {
                            val a = addresses!!
                            busy = true
                            executor.execute {
                                runCatching {
                                    MainnetGuard.requireExplicitAcknowledgement(
                                        evmNetwork, evmNetwork == Network.SEPOLIA || evmMainnetAck
                                    )
                                    if (sendMode == "Native") {
                                        val amountWei = evmAmount.toBigDecimal()
                                            .multiply(java.math.BigDecimal.TEN.pow(18)).toBigInteger()
                                        txService.prepareNative(evmNetwork, a.eth, evmRecipient, amountWei)
                                    } else {
                                        val d = tokenDecimals ?: error("Сначала проверьте контракт")
                                        val amountBase = evmAmount.toBigDecimal()
                                            .multiply(java.math.BigDecimal.TEN.pow(d)).toBigInteger()
                                        txService.prepareErc20(
                                            evmNetwork, a.eth, tokenContract, evmRecipient, amountBase
                                        )
                                    }.also { TransactionPolicyV3.requireEvm(it) }
                                }.onSuccess { p ->
                                    activity.runOnUiThread {
                                        evmReview = p
                                        notice = "Проверьте детали перед подписью"
                                        busy = false
                                    }
                                }.onFailure { e ->
                                    activity.runOnUiThread {
                                        notice = e.message ?: "Ошибка подготовки"
                                        busy = false
                                    }
                                }
                            }
                        }
                    }
                    evmReview?.let { p ->
                        item {
                            val lines = buildList {
                                add("Сеть" to "${p.network.name} · chain ${p.network.chainId}")
                                if (p.tokenContract != null) {
                                    add("Контракт" to p.tokenContract)
                                    add("Получатель" to (p.recipient ?: "—"))
                                    add("Сумма (base)" to "${p.tokenAmount}")
                                } else {
                                    add("Получатель" to p.to)
                                    add("Сумма" to "$evmAmount ${if (evmNetwork == Network.BNB) "BNB" else "ETH"}")
                                }
                                add("Nonce / Gas" to "${p.nonce} / ${p.gasLimit}")
                                add("Комиссия ~" to "${p.feeNative()} ${if (evmNetwork == Network.BNB) "BNB" else "ETH"}")
                            }
                            ReviewCard(lines)
                        }
                        item {
                            DangerButton("Подтвердить · подписать · отправить") {
                                biometric.authenticate(
                                    onSuccess = {
                                        executor.execute {
                                            runCatching {
                                                txService.signAndBroadcast(
                                                    p, secretStore.loadMnemonic(), explicitUserConfirmation = true
                                                )
                                            }.onSuccess { hash ->
                                                activity.runOnUiThread {
                                                    notice = "Отправлено · $hash"
                                                    evmReview = null
                                                    evmMainnetAck = false
                                                }
                                            }.onFailure { e ->
                                                activity.runOnUiThread {
                                                    notice = e.message ?: "Ошибка подписи"
                                                }
                                            }
                                        }
                                    },
                                    onFailure = { notice = it }
                                )
                            }
                        }
                    }
                }

                Tab.Settings -> {
                    item { SectionLabel("Безопасность") }
                    item {
                        SettingsTile(
                            Icons.Default.Fingerprint,
                            "Показать seed",
                            "Только после биометрии / PIN"
                        ) {
                            if (!secretStore.hasWallet()) {
                                notice = "Кошелёк не найден"
                            } else {
                                biometric.authenticate(
                                    onSuccess = {
                                        showSeed = true
                                        mnemonic = secretStore.loadMnemonic()
                                        tab = Tab.Portfolio
                                    },
                                    onFailure = { notice = it }
                                )
                            }
                        }
                    }
                    item {
                        SettingsTile(
                            Icons.Default.Lock,
                            "Защита",
                            "Keystore · FLAG_SECURE · без backup"
                        ) {}
                    }
                    item {
                        SettingsTile(
                            Icons.Default.Info,
                            "Версия",
                            "CryptoVault 1.0.0 · self-custody"
                        ) {}
                    }
                    item {
                        Text(
                            RealFundsNotice.TEXT,
                            color = VaultMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = VaultAccent.copy(alpha = 0.2f),
    selectedLabelColor = VaultAccent,
    containerColor = VaultCard,
    labelColor = VaultMuted
)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = VaultMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun HeroCard(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1B2550), Color(0xFF12203A))))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = VaultText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(subtitle, color = VaultMuted, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun BalanceHero() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A2A4A), Color(0xFF0F1C33))))
            .padding(22.dp)
    ) {
        Column {
            Text("Кошелёк готов", color = VaultMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("Self-custody", color = VaultText, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(Modifier.height(8.dp))
            Text("Ключи зашифрованы на устройстве", color = VaultAccent2, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AssetRow(symbol: String, name: String, address: String, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol.take(1), color = accent, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(symbol, color = VaultText, fontWeight = FontWeight.SemiBold)
            Text(name, color = VaultMuted, fontSize = 12.sp)
            Text(
                address,
                color = VaultMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun AddressCard(title: String, address: String, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCard)
            .padding(16.dp)
    ) {
        Text(title, color = accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        SelectionContainer {
            Text(address, color = VaultText, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ReviewCard(lines: List<Pair<String, String>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCard)
            .border(1.dp, VaultAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Проверка перед отправкой", color = VaultAccent, fontWeight = FontWeight.SemiBold)
        lines.forEach { (k, v) ->
            Row(Modifier.fillMaxWidth()) {
                Text(k, color = VaultMuted, fontSize = 13.sp, modifier = Modifier.width(100.dp))
                Text(
                    v,
                    color = VaultText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WarningCard(text: String) {
    Text(
        text,
        color = Color(0xFFFFD166),
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFD166).copy(alpha = 0.12f))
            .padding(14.dp)
    )
}

@Composable
private fun SeedBox(mnemonic: String) {
    SelectionContainer {
        Text(
            mnemonic,
            color = VaultText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1525))
                .border(1.dp, VaultDanger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        )
    }
}

@Composable
private fun VaultTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VaultAccent,
            unfocusedBorderColor = Color(0xFF2A3548),
            focusedLabelColor = VaultAccent,
            unfocusedLabelColor = VaultMuted,
            cursorColor = VaultAccent,
            focusedTextColor = VaultText,
            unfocusedTextColor = VaultText,
            focusedContainerColor = VaultCard,
            unfocusedContainerColor = VaultCard
        )
    )
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VaultAccent,
            contentColor = Color.White,
            disabledContainerColor = VaultAccent.copy(alpha = 0.3f)
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = VaultText),
        border = BorderStroke(1.dp, Color(0xFF2A3548))
    ) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VaultDanger, contentColor = Color.White)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NetworkChips(selected: Network, onSelect: (Network) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Network.SEPOLIA, Network.ETHEREUM, Network.BNB).forEach { n ->
            val label = when (n) {
                Network.SEPOLIA -> "Sepolia"
                Network.ETHEREUM -> "Ethereum"
                Network.BNB -> "BNB"
            }
            FilterChip(
                selected = selected == n,
                onClick = { onSelect(n) },
                label = { Text(label) },
                colors = chipColors()
            )
        }
    }
}

@Composable
private fun SettingsTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VaultCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = VaultAccent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = VaultText, fontWeight = FontWeight.Medium)
            Text(subtitle, color = VaultMuted, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = VaultMuted)
    }
}
