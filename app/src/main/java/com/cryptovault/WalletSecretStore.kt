package com.cryptovault

import android.content.Context

/**
 * Encrypted mnemonic storage. Prefs file is not backed up (allowBackup=false in manifest).
 * Always pair loadMnemonic() with BiometricGate in UI before use.
 */
class WalletSecretStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val vault = SecureVault()

    fun saveMnemonic(mnemonic: String) {
        val cleaned = mnemonic.trim().replace(Regex("\\s+"), " ")
        val words = cleaned.split(" ")
        require(words.size in 12..24) { "Seed-фраза должна содержать 12–24 слова" }
        require(words.all { it.matches(Regex("[a-z]+")) }) { "Некорректные слова seed-фразы" }
        prefs.edit().putString(KEY, vault.encrypt(cleaned)).apply()
    }

    fun hasWallet(): Boolean = prefs.contains(KEY)

    fun loadMnemonic(): String {
        val enc = prefs.getString(KEY, null) ?: error("Кошелёк не найден")
        val mnemonic = vault.decrypt(enc)
        SecretLeakGuard.requireSafeLog("wallet_loaded")
        return mnemonic
    }

    fun deleteWallet() {
        prefs.edit().remove(KEY).apply()
    }

    private companion object {
        const val PREFS = "wallet_secret"
        const val KEY = "encrypted_mnemonic"
    }
}
