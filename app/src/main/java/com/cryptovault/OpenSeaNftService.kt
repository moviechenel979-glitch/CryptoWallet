package com.cryptovault

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class NftItem(
    val chain: String,
    val contract: String,
    val tokenId: String,
    val name: String,
    val imageUrl: String?
)

class OpenSeaNftService(
    private val apiKey: String,
    private val http: OkHttpClient = OkHttpClient()
) {
    fun getNft(chain: String, contract: String, tokenId: String): NftItem {
        val url = "https://api.opensea.io/api/v2/chain/$chain/contract/$contract/nfts/$tokenId"
        val request = Request.Builder().url(url).header("x-api-key", apiKey).build()
        http.newCall(request).execute().use { r ->
            check(r.isSuccessful) { "NFT HTTP ${r.code}" }
            val n = JSONObject(r.body?.string() ?: "{}").optJSONObject("nft") ?: JSONObject()
            return NftItem(
                chain, contract, tokenId,
                n.optString("name", "NFT"),
                n.optString("image_url").ifBlank { null }
            )
        }
    }
}
