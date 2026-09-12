package com.cryptovault

data class DappRequest(
    val topic: String,
    val method: String,
    val chainId: String,
    val payloadJson: String
)

interface WalletConnectBridge {
    fun connect(uri: String)
    fun approve(request: DappRequest, signatureOrTransaction: String)
    fun reject(request: DappRequest, reason: String)
}

// Intentionally kept as an adapter: WalletConnect's Android Wallet SDK
// requires a projectId and explicit session/namespace handling.
class ConfigurableWalletConnectBridge : WalletConnectBridge {
    override fun connect(uri: String) { require(uri.startsWith("wc:")) }
    override fun approve(request: DappRequest, signatureOrTransaction: String) {}
    override fun reject(request: DappRequest, reason: String) {}
}
