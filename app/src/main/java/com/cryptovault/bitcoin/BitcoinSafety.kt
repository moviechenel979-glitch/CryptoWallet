package com.cryptovault.bitcoin

data class BitcoinSafetyReview(
    val inputCount: Int,
    val outputCount: Int,
    val feeSats: Long,
    val feeRateSatVb: Long
) {
    init {
        require(inputCount > 0)
        require(outputCount in 1..10)
        require(feeSats >= 0)
        require(feeRateSatVb in 1..1000)
    }
}
