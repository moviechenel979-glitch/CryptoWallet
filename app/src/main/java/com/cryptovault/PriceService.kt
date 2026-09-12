package com.cryptovault

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class PriceService(private val http: OkHttpClient = OkHttpClient()) {
    // Provider-agnostic adapter. Keep provider URL/key configurable.
    fun getUsdPrices(url: String, symbols: List<String>): Map<String, Double> {
        val request = Request.Builder().url(url).build()
        http.newCall(request).execute().use { r ->
            check(r.isSuccessful) { "Price HTTP ${r.code}" }
            val obj = JSONObject(r.body?.string() ?: "{}")
            return symbols.associateWith { obj.optJSONObject(it)?.optDouble("usd", 0.0) ?: 0.0 }
        }
    }
}
