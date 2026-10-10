package com.cryptovault

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

/* ── Premium dark palette ─────────────────────────────────────────── */
private val Bg = Color(0xFF080B12)
private val Surface1 = Color(0xFF0F131C)
private val Surface2 = Color(0xFF161B27)
private val Surface3 = Color(0xFF1C2333)
private val Stroke = Color(0xFF2A3347)
private val TextPrimary = Color(0xFFF7F8FC)
private val TextSecondary = Color(0xFF9AA3B5)
private val TextTertiary = Color(0xFF6B7385)
private val Accent = Color(0xFF5B8CFF)
private val AccentSoft = Color(0xFF5B8CFF).copy(alpha = 0.14f)
private val Success = Color(0xFF3DDC97)
private val Warning = Color(0xFFFFC857)
private val Danger = Color(0xFFFF5C6C)
private val Btc = Color(0xFFF7931A)
private val Eth = Color(0xFF8A9CFF)
private val Bnb = Color(0xFFF0B90B)

private val Scheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Success,
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    outline = Stroke
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            MaterialTheme(colorScheme = Scheme) {
                Surface(Modifier.fillMaxSize(), color = Bg) {
                    CryptoVaultApp(this)
                }
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.AccountBalanceWallet),
    Send("Send", Icons.AutoMirrored.Filled.Send),
    Receive("Receive", Icons.Default.SouthWest),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
private fun CryptoVaultApp(activity: FragmentActivity) {
    val secretStore = remember { WalletSecretStore(activity) }
    val metadata = remember { WalletMetadataStore(activity) }
    val biometric = remember { BiometricGate(activity) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val txService = remember { RealTransactionService() }
    val tokenService = remember { TokenService() }

    var tab by remember { mutableStateOf(Tab.Home) }
    var sendAsset by remember { mutableStateOf("ETH") }
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
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            notice = null
        }
    }

    fun createWallet() {
        busy = true
        runCatching { WalletCoreService.create() }
            .onSuccess { (seed, a) ->
                secretStore.saveMnemonic(seed)
                metadata.save(a)
                addresses = a
                mnemonic = seed
                showSeed = true
                notice = "Wallet created. Back up your seed phrase."
            }
            .onFailure { notice = it.message ?: "Create failed" }
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
            notice = "Wallet restored"
        }.onFailure { notice = it.message ?: "Invalid seed phrase" }
        busy = false
    }

    Scaffold(
        containerColor = Bg,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Surface3,
                    contentColor = TextPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface1,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(BorderStroke(1.dp, Stroke.copy(alpha = 0.6f)))
            ) {
                Tab.entries.forEach { item ->
                    val selected = tab == item
                    val tint by animateColorAsState(
                        if (selected) Accent else TextTertiary,
                        label = "nav"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = item.label, tint = tint) },
                        label = {
                            Text(
                                item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = tint
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentSoft
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            item { TopBar() }

            when (tab) {
                Tab.Home -> {
                    if (addresses == null) {
                        item { OnboardingCard() }
                        item {
                            CtaButton("Create new wallet", enabled = !busy, onClick = ::createWallet)
                        }
                        item { DividerLabel("Already have a wallet?") }
                        item {
                            VaultField(importSeed, { importSeed = it }, "Seed phrase", singleLine = false)
                        }
                        item {
                            GhostButton(
                                "Restore wallet",
                                enabled = importSeed.isNotBlank() && !busy,
                                onClick = ::restoreWallet
                            )
                        }
                    } else {
                        item { PortfolioHeader() }
                        item { DividerLabel("Assets") }
                        item {
                            AssetTile("BTC", "Bitcoin", addresses!!.btc, Btc) {
                                sendAsset = "BTC"; tab = Tab.Send
                            }
                        }
                        item {
                            AssetTile("ETH", "Ethereum", addresses!!.eth, Eth) {
                                sendAsset = "ETH"; evmNetwork = Network.ETHEREUM; tab = Tab.Send
                            }
                        }
                        item {
                            AssetTile("BNB", "BNB Smart Chain", addresses!!.bnb, Bnb) {
                                sendAsset = "BNB"; evmNetwork = Network.BNB; tab = Tab.Send
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) {
                                    CtaButton("Send") { tab = Tab.Send }
                                }
                                Box(Modifier.weight(1f)) {
                                    GhostButton("Receive") { tab = Tab.Receive }
                                }
                            }
                        }
                        if (showSeed && mnemonic.isNotBlank()) {
                            item { AlertBanner("Write down this seed. Anyone with it controls your funds.") }
                            item { SeedPanel(mnemonic) }
                        }
                    }
                }

                Tab.Receive -> {
                    item { DividerLabel("Receive addresses") }
                    addresses?.let { a ->
                        item { AddressPanel("Bitcoin", a.btc, Btc) }
                        item { AddressPanel("Ethereum", a.eth, Eth) }
                        item { AddressPanel("BNB Smart Chain", a.bnb, Bnb) }
                        item {
                            Text(
                                "Only send matching assets to each address. Wrong network can mean permanent loss.",
                                color = TextTertiary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    } ?: item { AlertBanner("Create or restore a wallet first.") }
                }

                Tab.Send -> {
                    item { DividerLabel("Send") }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BTC", "ETH", "BNB").forEach { asset ->
                                val on = sendAsset == asset
                                FilterChip(
                                    selected = on,
                                    onClick = {
                                        sendAsset = asset
                                        btcReview = null
                                        evmReview = null
                                        when (asset) {
                                            "ETH" -> evmNetwork = Network.ETHEREUM
                                            "BNB" -> evmNetwork = Network.BNB
                                        }
                                    },
                                    label = { Text(asset, fontWeight = FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentSoft,
                                        selectedLabelColor = Accent,
                                        containerColor = Surface2,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = on,
                                        borderColor = Stroke,
                                        selectedBorderColor = Accent.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }

                    if (sendAsset == "BTC") {
                        item { VaultField(btcRecipient, { btcRecipient = it }, "Recipient address") }
                        item { VaultField(btcAmount, { btcAmount = it }, "Amount (BTC)") }
                        item {
                            CtaButton(
                                if (busy) "Preparing…" else "Review transaction",
                                enabled = addresses != null && btcRecipient.isNotBlank() &&
                                    btcAmount.toDoubleOrNull() != null && !busy
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
                                            notice = "Review carefully before confirming"
                                            busy = false
                                        }
                                    }.onFailure { e ->
                                        activity.runOnUiThread {
                                            notice = e.message ?: "Prepare failed"
                                            busy = false
                                        }
                                    }
                                }
                            }
                        }
                        btcReview?.let { plan ->
                            item {
                                ReviewPanel(
                                    listOf(
                                        "To" to shorten(plan.to),
                                        "Amount" to "$btcAmount BTC",
                                        "Network fee" to "${plan.feeSats} sats",
                                        "Change" to "${plan.changeSats} sats",
                                        "Inputs" to "${plan.inputs.size}"
                                    )
                                )
                            }
                            item {
                                DangerButton("Confirm & broadcast") {
                                    biometric.authenticate(
                                        onSuccess = {
                                            executor.execute {
                                                runCatching {
                                                    val signed = BitcoinSigner.sign(plan, secretStore.loadMnemonic())
                                                    BitcoinNetworkClient().broadcast(BitcoinNetwork.MAINNET, signed.rawHex)
                                                    signed.txId
                                                }.onSuccess { txid ->
                                                    activity.runOnUiThread {
                                                        notice = "Sent · ${shorten(txid)}"
                                                        btcReview = null
                                                    }
                                                }.onFailure { e ->
                                                    activity.runOnUiThread {
                                                        notice = e.message ?: "Broadcast failed"
                                                    }
                                                }
                                            }
                                        },
                                        onFailure = { notice = it }
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(Network.SEPOLIA, Network.ETHEREUM, Network.BNB).forEach { n ->
                                    val label = when (n) {
                                        Network.SEPOLIA -> "Sepolia"
                                        Network.ETHEREUM -> "Ethereum"
                                        Network.BNB -> "BNB"
                                    }
                                    val on = evmNetwork == n
                                    FilterChip(
                                        selected = on,
                                        onClick = {
                                            evmNetwork = n
                                            evmReview = null
                                            tokenDecimals = null
                                        },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentSoft,
                                            selectedLabelColor = Accent,
                                            containerColor = Surface2,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = on,
                                            borderColor = Stroke,
                                            selectedBorderColor = Accent.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Native", "Token").forEach { mode ->
                                    val on = sendMode == mode
                                    FilterChip(
                                        selected = on,
                                        onClick = { sendMode = mode; evmReview = null },
                                        label = { Text(if (mode == "Native") "Native" else "ERC-20") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentSoft,
                                            selectedLabelColor = Accent,
                                            containerColor = Surface2,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = on,
                                            borderColor = Stroke,
                                            selectedBorderColor = Accent.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                        }
                        if (sendMode == "Token") {
                            item { VaultField(tokenContract, { tokenContract = it; tokenDecimals = null }, "Token contract") }
                            item {
                                GhostButton(
                                    "Detect decimals",
                                    enabled = tokenContract.length == 42
                                ) {
                                    executor.execute {
                                        runCatching { tokenService.decimals(evmNetwork, tokenContract) }
                                            .onSuccess { d ->
                                                activity.runOnUiThread {
                                                    tokenDecimals = d
                                                    notice = "Decimals: $d"
                                                }
                                            }
                                            .onFailure { e ->
                                                activity.runOnUiThread {
                                                    notice = e.message ?: "Contract read failed"
                                                }
                                            }
                                    }
                                }
                            }
                            tokenDecimals?.let {
                                item {
                                    Text("Decimals · $it", color = Success, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        item { VaultField(evmRecipient, { evmRecipient = it }, "Recipient") }
                        item {
                            VaultField(
                                evmAmount,
                                { evmAmount = it },
                                if (sendMode == "Token") "Token amount" else "Amount"
                            )
                        }
                        if (evmNetwork != Network.SEPOLIA) {
                            item {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Danger.copy(alpha = 0.1f))
                                        .border(1.dp, Danger.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = evmMainnetAck,
                                        onCheckedChange = { evmMainnetAck = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Danger,
                                            uncheckedColor = TextTertiary
                                        )
                                    )
                                    Text(
                                        "Mainnet · real funds · irreversible",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        item {
                            CtaButton(
                                if (busy) "Preparing…" else "Review transaction",
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
                                            val d = tokenDecimals ?: error("Detect decimals first")
                                            val amountBase = evmAmount.toBigDecimal()
                                                .multiply(java.math.BigDecimal.TEN.pow(d)).toBigInteger()
                                            txService.prepareErc20(
                                                evmNetwork, a.eth, tokenContract, evmRecipient, amountBase
                                            )
                                        }.also { TransactionPolicyV3.requireEvm(it) }
                                    }.onSuccess { p ->
                                        activity.runOnUiThread {
                                            evmReview = p
                                            notice = "Review carefully before confirming"
                                            busy = false
                                        }
                                    }.onFailure { e ->
                                        activity.runOnUiThread {
                                            notice = e.message ?: "Prepare failed"
                                            busy = false
                                        }
                                    }
                                }
                            }
                        }
                        evmReview?.let { p ->
                            item {
                                val lines = buildList {
                                    add("Network" to "${p.network.name} · ${p.network.chainId}")
                                    if (p.tokenContract != null) {
                                        add("Contract" to shorten(p.tokenContract))
                                        add("Recipient" to shorten(p.recipient ?: "—"))
                                        add("Amount" to "${p.tokenAmount}")
                                    } else {
                                        add("Recipient" to shorten(p.to))
                                        add("Amount" to "$evmAmount")
                                    }
                                    add("Nonce / gas" to "${p.nonce} / ${p.gasLimit}")
                                    add("Est. fee" to "${p.feeNative()}")
                                }
                                ReviewPanel(lines)
                            }
                            item {
                                DangerButton("Confirm & broadcast") {
                                    biometric.authenticate(
                                        onSuccess = {
                                            executor.execute {
                                                runCatching {
                                                    txService.signAndBroadcast(
                                                        p, secretStore.loadMnemonic(), explicitUserConfirmation = true
                                                    )
                                                }.onSuccess { hash ->
                                                    activity.runOnUiThread {
                                                        notice = "Sent · ${shorten(hash)}"
                                                        evmReview = null
                                                        evmMainnetAck = false
                                                    }
                                                }.onFailure { e ->
                                                    activity.runOnUiThread {
                                                        notice = e.message ?: "Sign failed"
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
                }

                Tab.Settings -> {
                    item { DividerLabel("Security") }
                    item {
                        SettingsRow(Icons.Default.Fingerprint, "Reveal seed phrase", "Requires biometrics") {
                            if (!secretStore.hasWallet()) {
                                notice = "No wallet found"
                            } else {
                                biometric.authenticate(
                                    onSuccess = {
                                        showSeed = true
                                        mnemonic = secretStore.loadMnemonic()
                                        tab = Tab.Home
                                    },
                                    onFailure = { notice = it }
                                )
                            }
                        }
                    }
                    item {
                        SettingsRow(Icons.Default.Shield, "Protection", "Keystore · FLAG_SECURE · no cloud backup") {}
                    }
                    item {
                        SettingsRow(Icons.Default.Info, "About", "CryptoVault 1.0.0 · self-custody") {}
                    }
                    item {
                        Text(
                            RealFundsNotice.TEXT,
                            color = TextTertiary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun shorten(value: String): String {
    if (value.length <= 16) return value
    return value.take(8) + "…" + value.takeLast(6)
}

@Composable
private fun TopBar() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(Accent, Color(0xFF7B6CFF)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("CryptoVault", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextPrimary)
            Text("Self-custody wallet", color = TextTertiary, fontSize = 12.sp)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Success.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("Local", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun OnboardingCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF152038), Color(0xFF0C1220)))
            )
            .border(1.dp, Stroke.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text("Your keys,\nstay on device", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = TextPrimary, lineHeight = 32.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Create a wallet or restore from a seed phrase. Signing happens locally — seed never leaves the phone.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun PortfolioHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF142038), Color(0xFF0B101C)))
            )
            .border(1.dp, Stroke.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Text("PORTFOLIO", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Ready", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Success))
            Spacer(Modifier.width(8.dp))
            Text("Encrypted with Android Keystore", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DividerLabel(text: String) {
    Text(
        text.uppercase(),
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun AssetTile(symbol: String, name: String, address: String, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface2)
            .border(1.dp, Stroke.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol.take(1), color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(symbol, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(name, color = TextTertiary, fontSize = 12.sp)
            Text(
                shorten(address),
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AddressPanel(title: String, address: String, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface2)
            .border(1.dp, Stroke.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(8.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        SelectionContainer {
            Text(
                address,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ReviewPanel(lines: List<Pair<String, String>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface2)
            .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Transaction review", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        lines.forEach { (k, v) ->
            Row(Modifier.fillMaxWidth()) {
                Text(k, color = TextTertiary, fontSize = 13.sp, modifier = Modifier.width(96.dp))
                Text(
                    v,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
            }
            if (k != lines.last().first) {
                HorizontalDivider(color = Stroke.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun AlertBanner(text: String) {
    Text(
        text,
        color = Warning,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Warning.copy(alpha = 0.1f))
            .border(1.dp, Warning.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    )
}

@Composable
private fun SeedPanel(mnemonic: String) {
    SelectionContainer {
        Text(
            buildAnnotatedString {
                mnemonic.split(" ").forEachIndexed { i, w ->
                    withStyle(SpanStyle(color = TextTertiary, fontSize = 11.sp)) {
                        append("${i + 1}. ")
                    }
                    withStyle(SpanStyle(color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)) {
                        append(w)
                    }
                    if (i < mnemonic.split(" ").lastIndex) append("  ")
                }
            },
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0A0E16))
                .border(1.dp, Danger.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        )
    }
}

@Composable
private fun VaultField(
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
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = Stroke,
            focusedLabelColor = Accent,
            unfocusedLabelColor = TextTertiary,
            cursorColor = Accent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = Surface2,
            unfocusedContainerColor = Surface2
        )
    )
}

@Composable
private fun CtaButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = Color.White,
            disabledContainerColor = Accent.copy(alpha = 0.25f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun GhostButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Stroke),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary,
            disabledContentColor = TextTertiary
        )
    ) {
        Text(text, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(1.dp, Stroke.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AccentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = TextTertiary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
