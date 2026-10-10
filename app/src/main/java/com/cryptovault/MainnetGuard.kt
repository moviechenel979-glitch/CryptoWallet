package com.cryptovault

import com.cryptovault.security.AppIntegrity
import com.cryptovault.security.ReleaseHardening

object MainnetGuard {
    fun requireExplicitAcknowledgement(network: Network, acknowledged: Boolean) {
        if (network == Network.ETHEREUM || network == Network.BNB) {
            check(acknowledged) { "Mainnet requires explicit risk acknowledgement." }
            if (ReleaseHardening.REQUIRE_MAINNET_ACK && AppIntegrity.shouldWarnBeforeMainnet()) {
                check(acknowledged) {
                    "Debugger/test-keys detected. Mainnet blocked without explicit acknowledgement."
                }
            }
        }
    }
}
