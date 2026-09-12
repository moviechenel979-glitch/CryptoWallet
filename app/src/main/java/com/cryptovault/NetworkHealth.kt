package com.cryptovault

import java.math.BigInteger

data class NetworkHealth(
    val network: Network,
    val chainId: BigInteger,
    val healthy: Boolean,
    val message: String
)

class NetworkHealthChecker(private val rpc: RpcClient = RpcClient()) {
    fun check(network: Network): NetworkHealth {
        return try {
            // eth_chainId is the canonical identity check for EVM RPC.
            val raw = rpc.call(network.rpcUrl, "eth_chainId")
            val id = BigInteger(raw.removePrefix("0x"), 16)
            RpcSafety.requireExpectedChain(network, id)
            NetworkHealth(network, id, true, "RPC chainId подтверждён")
        } catch (e: Throwable) {
            NetworkHealth(network, BigInteger.ZERO, false, e.message ?: "RPC error")
        }
    }
}
