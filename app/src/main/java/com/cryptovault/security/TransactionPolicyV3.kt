package com.cryptovault.security

data class TransactionPolicyV3(
    val maxNativeValueWei: java.math.BigInteger,
    val maxGasLimit: Long = 1_000_000L,
    val maxFeePerGasWei: java.math.BigInteger,
    val maxPriorityFeePerGasWei: java.math.BigInteger
) {
    fun validateNative(value: java.math.BigInteger, gasLimit: Long, maxFee: java.math.BigInteger, priority: java.math.BigInteger) {
        require(value.signum() >= 0) { "Negative value" }
        require(value <= maxNativeValueWei) { "Native value exceeds policy" }
        require(gasLimit in 21_000L..maxGasLimit) { "Gas limit outside policy" }
        require(maxFee <= maxFeePerGasWei) { "Max fee exceeds policy" }
        require(priority <= maxPriorityFeePerGasWei) { "Priority fee exceeds policy" }
        require(priority <= maxFee) { "Priority fee exceeds max fee" }
    }
}
