package com.cryptovault

data class ReviewRow(val title: String, val value: String)

object TransactionReviewFormatter {
    fun rows(
        network: String,
        recipient: String,
        amount: String,
        fee: String,
        data: String?
    ): List<ReviewRow> = listOf(
        ReviewRow("Сеть", network),
        ReviewRow("Получатель", recipient),
        ReviewRow("Сумма", amount),
        ReviewRow("Комиссия", fee),
        ReviewRow("Calldata", data ?: "Нет")
    )
}
