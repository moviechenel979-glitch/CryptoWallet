package com.cryptovault.dapp

data class DappRequestReview(
    val origin: String,
    val method: String,
    val chainId: Long,
    val account: String,
    val humanSummary: String,
    val requiresExplicitConfirmation: Boolean = true
)

object DappRequestPolicy {
    private val allowed = setOf(
        "eth_sendTransaction",
        "personal_sign",
        "eth_signTypedData_v4"
    )

    fun review(origin: String, method: String, chainId: Long, account: String): DappRequestReview {
        require(origin.startsWith("https://")) { "Only HTTPS dApps are allowed" }
        require(method in allowed) { "Unsupported signing method" }
        require(chainId > 0)
        require(account.matches(Regex("^0x[0-9a-fA-F]{40}$")))
        val text = when (method) {
            "eth_sendTransaction" -> "dApp просит отправить транзакцию"
            "personal_sign" -> "dApp просит подписать произвольное сообщение"
            else -> "dApp просит подписать typed-data"
        }
        return DappRequestReview(origin, method, chainId, account, text)
    }
}
