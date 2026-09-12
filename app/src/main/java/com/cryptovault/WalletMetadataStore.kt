package com.cryptovault

import android.content.Context

class WalletMetadataStore(context: Context) {
    private val prefs = context.getSharedPreferences("wallet_metadata", Context.MODE_PRIVATE)
    fun save(a: WalletAddresses) = prefs.edit().putString("btc", a.btc).putString("eth", a.eth).putString("bnb", a.bnb).apply()
    fun load(): WalletAddresses? {
        val btc = prefs.getString("btc", null) ?: return null
        return WalletAddresses(btc, prefs.getString("eth", "") ?: "", prefs.getString("bnb", "") ?: "")
    }
    fun clear() = prefs.edit().clear().apply()
}
