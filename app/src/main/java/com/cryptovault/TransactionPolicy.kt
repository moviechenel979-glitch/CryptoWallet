package com.cryptovault

enum class BroadcastMode { TESTNET_ONLY, EXPLICIT_MAINNET }

data class TransactionReview(
    val network: Network,
    val recipient: String,
    val amount: String,
    val fee: String,
    val data: String? = null
)

object TransactionPolicy {
    fun requireUserReview(review: TransactionReview) {
        require(review.recipient.isNotBlank()) { "Не указан получатель" }
        require(review.amount.isNotBlank()) { "Не указана сумма" }
        require(review.fee.isNotBlank()) { "Не рассчитана комиссия" }
        if (review.network == Network.ETHEREUM || review.network == Network.BNB) {
            require(review.recipient.startsWith("0x") && review.recipient.length == 42) {
                "Некорректный EVM-адрес"
            }
        }
    }
}
