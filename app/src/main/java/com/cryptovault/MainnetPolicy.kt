package com.cryptovault

/**
 * Mainnet support is explicit but never automatic.
 * A wallet must require a final user confirmation immediately before signing.
 */
data class MainnetReview(
    val chain: Network,
    val recipient: String,
    val amount: String,
    val fee: String,
    val data: String?,
    val simulationPassed: Boolean
)

object MainnetPolicy {
    fun validate(review: MainnetReview) {
        require(review.chain == Network.ETHEREUM || review.chain == Network.BNB)
        require(review.recipient.matches(Regex("^0x[a-fA-F0-9]{40}$")))
        require(review.amount.isNotBlank())
        require(review.fee.isNotBlank())
        require(review.simulationPassed) { "Транзакция mainnet должна пройти simulation/review" }
    }
}
