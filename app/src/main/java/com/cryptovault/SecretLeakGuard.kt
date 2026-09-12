package com.cryptovault

object SecretLeakGuard {
    private val forbidden = listOf(
        "mnemonic", "privatekey", "private_key", "seedphrase", "seed phrase",
        "secret", "xprv", "xpriv"
    )

    fun requireSafeLog(text: String) {
        val lower = text.lowercase()
        check(forbidden.none { lower.contains(it) }) {
            "Попытка записать потенциальный секрет в лог заблокирована"
        }
    }
}
