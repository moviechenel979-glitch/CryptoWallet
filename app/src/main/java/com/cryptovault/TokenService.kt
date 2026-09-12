package com.cryptovault

import org.json.JSONArray
import java.math.BigInteger

class TokenService(private val rpc: RpcClient = RpcClient()) {
    fun balance(token: Asset, owner: String): BigInteger {
        val call = Erc20Codec.balanceOfCall(owner)
        val result = rpc.call(token.network.rpcUrl, "eth_call",
            JSONArray().put(org.json.JSONObject().put("to", token.contract).put("data", call)).put("latest"))
        return BigInteger(result.removePrefix("0x").ifBlank { "0" }, 16)
    }

    fun decimals(token: Asset): Int {
        val result = rpc.call(token.network.rpcUrl, "eth_call",
            JSONArray().put(org.json.JSONObject().put("to", token.contract).put("data", "0x313ce567")).put("latest"))
        return BigInteger(result.removePrefix("0x"), 16).toInt()
    }
}
