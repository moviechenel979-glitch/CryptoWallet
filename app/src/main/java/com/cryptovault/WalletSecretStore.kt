package com.cryptovault

import android.content.Context

class WalletSecretStore(context: Context) {
    private val prefs = context.getSharedPreferences("wallet_secret", Context.MODE_PRIVATE)
    private val vault = SecureVault(context)

    fun saveMnemonic(mnemonic: String) {
        require(mnemonic.trim().split(Regex("\\s+")).size >= 12)
        prefs.edit().putString(KEY, vault.encrypt(mnemonic.trim())).apply()
    }

    fun hasWallet(): Boolean = prefs.contains(KEY)

    fun loadMnemonic(): String = prefs.getString(KEY, null)?.let(vault::decrypt)
        ?: error("Кошелёк не найден")

    fun deleteWallet() { prefs.edit().remove(KEY).apply() }

    private companion object { const val KEY = "encrypted_mnemonic" }
}
