package com.cryptovault
object MainnetGuard {
    fun requireExplicitAcknowledgement(network: Network, acknowledged: Boolean) {
        if (network == Network.ETHEREUM || network == Network.BNB)
            check(acknowledged) { "Mainnet требует отдельного подтверждения риска." }
    }
}
