package com.cryptovault

data class SwapQuote(
    val provider: String,
    val from: String,
    val to: String,
    val amount: String,
    val estimatedOutput: String,
    val transactionData: String? = null
)

interface SwapService {
    suspend fun quote(from: String, to: String, amount: String, chainId: Long): SwapQuote
}

// The production implementation should be wired to a chosen quote provider.
// Never sign/broadcast a quote automatically: show spender, router, slippage,
// minimum received, fees and the final calldata to the user first.
class ConfigurableSwapService : SwapService {
    override suspend fun quote(from: String, to: String, amount: String, chainId: Long) =
        SwapQuote("configured-provider", from, to, amount, "0")
}
