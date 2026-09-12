package com.cryptovault.evm

import java.math.BigInteger

data class Eip1559Transaction(
    val chainId: Long,
    val nonce: Long,
    val maxPriorityFeePerGas: BigInteger,
    val maxFeePerGas: BigInteger,
    val gasLimit: Long,
    val to: String,
    val value: BigInteger,
    val dataHex: String = "0x"
) {
    init {
        require(chainId > 0)
        require(nonce >= 0)
        require(maxPriorityFeePerGas >= BigInteger.ZERO)
        require(maxFeePerGas >= maxPriorityFeePerGas)
        require(gasLimit > 0)
        require(to.matches(Regex("^0x[0-9a-fA-F]{40}$")))
        require(value >= BigInteger.ZERO)
    }
}
