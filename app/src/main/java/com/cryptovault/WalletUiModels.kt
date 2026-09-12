package com.cryptovault

data class PortfolioAsset(
    val symbol: String,
    val name: String,
    val amount: String,
    val usdValue: String
)

data class HistoryItem(
    val hash: String,
    val title: String,
    val subtitle: String
)

data class NftPreview(
    val collection: String,
    val tokenId: String,
    val imageUrl: String?
)
