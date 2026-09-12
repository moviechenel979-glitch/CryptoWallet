package com.cryptovault
import java.math.BigInteger
object RpcSafety {
    fun requireExpectedChain(network: Network, actualChainId: BigInteger) {
        check(actualChainId == BigInteger.valueOf(network.chainId)) {
            "RPC chainId не совпадает с выбранной сетью"
        }
    }
}
