package com.cryptovault

object WalletSecurityPolicy {
    fun requireMainnetConfirmation(explicitConfirmation: Boolean) {
        check(explicitConfirmation) { "Операция mainnet требует явного подтверждения пользователя" }
    }

    fun requireHttps(url: String) {
        require(url.startsWith("https://")) { "RPC должен использовать HTTPS" }
    }
}
