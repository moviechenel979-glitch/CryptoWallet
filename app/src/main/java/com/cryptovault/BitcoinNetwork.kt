package com.cryptovault

enum class BitcoinNetwork { MAINNET, TESTNET }

data class BitcoinTransactionReview(
    val network: BitcoinNetwork,
    val recipient: String,
    val amountSats: Long,
    val feeSats: Long,
    val simulationPassed: Boolean
)

object BitcoinPolicy {
    fun validate(r: BitcoinTransactionReview) {
        require(r.recipient.isNotBlank())
        require(r.amountSats > 0)
        require(r.feeSats >= 0)
        require(r.simulationPassed)
    }
}
