package com.cryptovault

import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import wallet.core.jni.BitcoinScript
import wallet.core.jni.CoinType

/** Read-only Bitcoin provider + raw transaction broadcast. It never receives private keys. */
class BitcoinNetworkClient {
    data class FeeRates(val fastest: Long, val halfHour: Long, val hour: Long)

    fun utxos(network: BitcoinNetwork, address: String): List<BitcoinUtxo> {
        val arr = JSONArray(URL("${baseUrl(network)}/address/${URLEncoder.encode(address, "UTF-8")}/utxo").readText())
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    BitcoinUtxo(
                        o.getString("txid"),
                        o.getInt("vout"),
                        o.getLong("value"),
                        BitcoinScriptHelper.scriptForAddress(address)
                    )
                )
            }
        }
    }

    fun feeRates(network: BitcoinNetwork): FeeRates {
        val o = JSONObject(URL("${baseUrl(network)}/v1/fees/recommended").readText())
        return FeeRates(
            o.optLong("fastestFee", 10).coerceAtLeast(1),
            o.optLong("halfHourFee", 5).coerceAtLeast(1),
            o.optLong("hourFee", 2).coerceAtLeast(1)
        )
    }

    fun broadcast(network: BitcoinNetwork, rawTxHex: String): String {
        require(rawTxHex.matches(Regex("[0-9a-fA-F]+"))) { "Некорректный raw transaction" }
        val c = URL("${baseUrl(network)}/tx").openConnection() as java.net.HttpURLConnection
        c.requestMethod = "POST"
        c.doOutput = true
        c.setRequestProperty("Content-Type", "text/plain")
        c.outputStream.use { it.write(rawTxHex.toByteArray()) }
        val body = (if (c.responseCode in 200..299) c.inputStream else c.errorStream)
            .bufferedReader().use { it.readText() }.trim()
        if (c.responseCode !in 200..299) error("Bitcoin broadcast отклонён: $body")
        return body
    }

    private fun baseUrl(network: BitcoinNetwork) = when (network) {
        BitcoinNetwork.MAINNET -> "https://mempool.space/api"
        BitcoinNetwork.TESTNET -> "https://mempool.space/testnet/api"
    }
}

object BitcoinScriptHelper {
    fun scriptForAddress(address: String): String {
        val script = BitcoinScript.lockScriptForAddress(address, CoinType.BITCOIN)
        return script.data().joinToString("") { b -> "%02x".format(b) }
    }
}
