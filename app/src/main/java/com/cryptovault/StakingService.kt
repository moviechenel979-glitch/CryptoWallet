package com.cryptovault

data class StakingPosition(
    val protocol: String,
    val chainId: Long,
    val asset: String,
    val amount: String,
    val rewards: String
)

interface StakingService {
    suspend fun positions(address: String, chainId: Long): List<StakingPosition>
    suspend fun buildStakeTransaction(address: String, amount: String, chainId: Long): String
}

class StakingAdapter : StakingService {
    override suspend fun positions(address: String, chainId: Long) = emptyList<StakingPosition>()
    override suspend fun buildStakeTransaction(address: String, amount: String, chainId: Long) =
        error("Staking provider is not configured")
}
