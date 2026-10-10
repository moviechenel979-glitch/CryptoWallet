package com.cryptovault

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger

class RpcClient(private val http: OkHttpClient = OkHttpClient()) {
    fun call(rpcUrl: String, method: String, params: JSONArray = JSONArray()): String {
        val body = JSONObject()
            .put("jsonrpc", "2.0").put("id", 1)
            .put("method", method).put("params", params).toString()
        val request = Request.Builder().url(rpcUrl)
            .post(okhttp3.RequestBody.create("application/json".toMediaTypeCompat(), body)).build()
        http.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "RPC HTTP ${response.code}" }
            val text = response.body?.string() ?: error("Пустой RPC ответ")
            val obj = JSONObject(text)
            if (obj.has("error")) error(obj.getJSONObject("error").toString())
            return obj.get("result").toString()
        }
    }

    fun balance(network: Network, address: String): BigInteger =
        hexBigInteger(call(network.rpcUrl, "eth_getBalance", JSONArray().put(address).put("latest")))

    fun nonce(network: Network, address: String): Long =
        call(network.rpcUrl, "eth_getTransactionCount", JSONArray().put(address).put("pending"))
            .removePrefix("0x").ifBlank { "0" }.toLong(16)

    fun gasPrice(network: Network): BigInteger =
        hexBigInteger(call(network.rpcUrl, "eth_gasPrice"))

    fun estimateGas(network: Network, from: String, to: String, valueWei: BigInteger, dataHex: String = "0x"): BigInteger =
        hexBigInteger(call(network.rpcUrl, "eth_estimateGas",
            JSONArray().put(JSONObject()
                .put("from", from).put("to", to)
                .put("value", "0x${valueWei.toString(16)}").put("data", dataHex))))

    fun ethCall(network: Network, from: String?, to: String, dataHex: String): String {
        val tx = JSONObject().put("to", to).put("data", dataHex)
        if (!from.isNullOrBlank()) tx.put("from", from)
        return call(network.rpcUrl, "eth_call", JSONArray().put(tx).put("latest"))
    }

    fun sendRaw(network: Network, signedHex: String): String =
        call(network.rpcUrl, "eth_sendRawTransaction", JSONArray().put(signedHex))

    fun transactionReceipt(network: Network, txHash: String): TransactionReceipt? {
        val result = call(network.rpcUrl, "eth_getTransactionReceipt", JSONArray().put(txHash))
        if (result == "null") return null
        val o = JSONObject(result)
        val status = o.optString("status", "0x0").removePrefix("0x").toLongOrNull(16) ?: 0L
        return TransactionReceipt(
            o.optString("transactionHash", txHash),
            o.optString("blockNumber", "").ifBlank { null },
            status == 1L
        )
    }

    private fun hexBigInteger(value: String): BigInteger {
        val clean = value.removePrefix("0x")
        return if (clean.isBlank()) BigInteger.ZERO else BigInteger(clean, 16)
    }
}

data class TransactionReceipt(val hash: String, val blockNumber: String?, val success: Boolean)

private fun String.toMediaTypeCompat(): okhttp3.MediaType =
    okhttp3.MediaType.parse(this) ?: error("Invalid media type: $this")
