package com.cryptovault

import org.json.JSONArray

class TransactionSimulation(private val rpc: RpcClient = RpcClient()) {
    fun ethCall(network: Network, from: String, to: String, data: String): String {
        require(AddressValidation.evm(from))
        require(AddressValidation.evm(to))
        val tx = org.json.JSONObject()
            .put("from", from)
            .put("to", to)
            .put("data", data)
        return rpc.call(network.rpcUrl, "eth_call", JSONArray().put(tx).put("latest"))
    }

    fun estimateGas(network: Network, from: String, to: String, data: String): Long {
        require(AddressValidation.evm(from))
        require(AddressValidation.evm(to))
        val tx = org.json.JSONObject()
            .put("from", from)
            .put("to", to)
            .put("data", data)
        return rpc.call(network.rpcUrl, "eth_estimateGas", JSONArray().put(tx))
            .removePrefix("0x").toLong(16)
    }
}
