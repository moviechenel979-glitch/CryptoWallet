package com.cryptovault
import kotlinx.coroutines.delay
class ReceiptPoller(private val service: RealTransactionService) {
    suspend fun waitForReceipt(network: Network, txHash: String, attempts: Int = 40): TransactionReceipt? {
        repeat(attempts) {
            service.receipt(network, txHash)?.let { return it }
            delay(3000)
        }
        return null
    }
}
