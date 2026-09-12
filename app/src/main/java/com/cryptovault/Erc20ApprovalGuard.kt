package com.cryptovault

import java.math.BigInteger

object Erc20ApprovalGuard {
    private const val APPROVE_SELECTOR = "095ea7b3"
    private const val MAX_UINT256 = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"

    data class Approval(val spender: String, val amount: BigInteger, val unlimited: Boolean)

    fun decode(data: String): Approval? {
        val clean = data.removePrefix("0x").lowercase()
        if (clean.length != 8 + 64 + 64 || clean.take(8) != APPROVE_SELECTOR) return null
        val spender = "0x" + clean.substring(8 + 24, 8 + 64)
        val amount = BigInteger(clean.substring(72, 136), 16)
        return Approval(spender, amount, amount == BigInteger(MAX_UINT256, 16))
    }

    fun requireSafe(data: String, allowUnlimited: Boolean = false) {
        val approval = decode(data) ?: return
        require(allowUnlimited || !approval.unlimited) {
            "Неограниченное ERC-20 разрешение заблокировано политикой безопасности"
        }
    }
}
