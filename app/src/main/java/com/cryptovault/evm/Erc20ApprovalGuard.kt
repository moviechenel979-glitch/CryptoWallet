package com.cryptovault.evm

import java.math.BigInteger

data class ApprovalReview(
    val token: String,
    val spender: String,
    val amount: BigInteger,
    val unlimited: Boolean,
    val warning: String
)

object Erc20ApprovalGuard {
    private val MAX_UINT = BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16)

    fun review(token: String, spender: String, amount: BigInteger): ApprovalReview {
        require(token.matches(Regex("^0x[0-9a-fA-F]{40}$")))
        require(spender.matches(Regex("^0x[0-9a-fA-F]{40}$")))
        require(amount >= BigInteger.ZERO)
        val unlimited = amount == MAX_UINT
        return ApprovalReview(
            token, spender, amount, unlimited,
            if (unlimited) "Неограниченный approve: dApp получит право тратить любые токены этого типа." 
            else "Проверьте токен и адрес spender перед подписью."
        )
    }
}
