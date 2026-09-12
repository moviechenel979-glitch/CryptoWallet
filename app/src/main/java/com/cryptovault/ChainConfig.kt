package com.cryptovault

enum class Network(val chainId: Long, val rpcUrl: String) {
    ETHEREUM(1, "https://ethereum-rpc.publicnode.com"),
    BNB(56, "https://bsc-rpc.publicnode.com"),
    SEPOLIA(11155111, "https://ethereum-sepolia-rpc.publicnode.com")
}

data class Asset(
    val symbol: String,
    val name: String,
    val decimals: Int,
    val contract: String? = null,
    val network: Network = Network.ETHEREUM
)

object AssetRegistry {
    // Contract addresses are intentionally configurable. Do not assume a token
    // symbol is sufficient to identify an asset.
    val native = listOf(
        Asset("ETH", "Ethereum", 18),
        Asset("BNB", "BNB Smart Chain", 18, network = Network.BNB)
    )

    fun erc20(symbol: String, name: String, decimals: Int, contract: String, network: Network) =
        Asset(symbol, name, decimals, contract, network)
}
