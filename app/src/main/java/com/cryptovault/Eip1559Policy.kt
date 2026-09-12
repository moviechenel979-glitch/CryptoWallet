package com.cryptovault

import java.math.BigInteger

object Eip1559Policy {
    fun validate(baseFeeWei: BigInteger, maxPriorityFeeWei: BigInteger, maxFeeWei: BigInteger) {
        require(baseFeeWei >= BigInteger.ZERO) { "Некорректная base fee" }
        require(maxPriorityFeeWei >= BigInteger.ZERO) { "Некорректный priority fee" }
        require(maxFeeWei >= baseFeeWei) { "Max fee ниже base fee" }
        require(maxFeeWei >= maxPriorityFeeWei) { "Max fee ниже priority fee" }
        require(maxFeeWei <= BigInteger("1000000000000")) { "Слишком высокий max fee" }
        require(maxPriorityFeeWei <= BigInteger("100000000000")) { "Слишком высокий priority fee" }
    }

    fun effectiveGasPrice(baseFeeWei: BigInteger, maxPriorityFeeWei: BigInteger, maxFeeWei: BigInteger): BigInteger {
        validate(baseFeeWei, maxPriorityFeeWei, maxFeeWei)
        return minOf(maxFeeWei, baseFeeWei + maxPriorityFeeWei)
    }
}
