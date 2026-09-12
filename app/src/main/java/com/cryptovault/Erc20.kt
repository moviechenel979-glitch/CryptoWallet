package com.cryptovault

import java.math.BigInteger

data class Erc20Token(
    val symbol: String,
    val contract: String,
    val decimals: Int,
    val chainId: Long
)

object Erc20Codec {
    fun balanceOfCall(address: String): String {
        val clean = address.removePrefix("0x").lowercase().padStart(64, '0')
        return "0x70a08231$clean"
    }

    fun transferCall(to: String, amount: BigInteger): String {
        val addr = to.removePrefix("0x").lowercase().padStart(64, '0')
        val value = amount.toString(16).padStart(64, '0')
        return "0xa9059cbb$addr$value"
    }
}
