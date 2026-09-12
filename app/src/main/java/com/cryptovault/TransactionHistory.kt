package com.cryptovault

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class TxRecord(
    val hash: String,
    val from: String,
    val to: String,
    val value: String,
    val timestamp: Long? = null
)

class EthereumHistoryService(private val http: OkHttpClient = OkHttpClient()) {
    // Adapter point for Etherscan-compatible indexers. API keys stay local
    // and should never be embedded in the APK.
    fun fetch(apiUrl: String, address: String, apiKey: String): List<TxRecord> {
        val url = "$apiUrl?module=account&action=txlist&address=$address&sort=desc&apikey=$apiKey"
        val request = Request.Builder().url(url).build()
        http.newCall(request).execute().use { r ->
            check(r.isSuccessful) { "History HTTP ${r.code}" }
            val result = JSONObject(r.body?.string() ?: "{}").optJSONArray("result") ?: return emptyList()
            return List(result.length()) { i ->
                val x = result.getJSONObject(i)
                TxRecord(
                    x.optString("hash"),
                    x.optString("from"),
                    x.optString("to"),
                    x.optString("value"),
                    x.optString("timeStamp").toLongOrNull()
                )
            }
        }
    }
}
