package com.cryptovault.dapp

object DappOriginGuard {
    private val blockedHosts = setOf(
        "localhost", "127.0.0.1", "0.0.0.0"
    )

    fun validateOrigin(origin: String): String {
        val uri = java.net.URI(origin)
        require(uri.scheme.equals("https", ignoreCase = true)) { "dApp must use HTTPS" }
        val host = uri.host?.lowercase() ?: error("Missing dApp host")
        require(host !in blockedHosts) { "Blocked dApp host" }
        require(!host.contains("xn--")) { "Internationalized/punycode origin requires extra review" }
        return host
    }
}
