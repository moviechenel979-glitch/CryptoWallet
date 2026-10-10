package com.cryptovault

import java.math.BigInteger

/** Final transaction invariants used immediately before any local signature. */
object TransactionPolicyV3 {
    fun requireEvm(prepared: PreparedEvmTransfer) {
        AddressValidation.requireEvm(prepared.from)
        AddressValidation.requireEvm(prepared.to)
        require(prepared.amountWei >= BigInteger.ZERO) { "Отрицательная сумма" }
        require(prepared.gasLimit > BigInteger.ZERO) { "Некорректный gas limit" }
        require(prepared.gasPriceWei > BigInteger.ZERO) { "Некорректная gas price" }
        require(prepared.network.chainId > 0) { "Некорректный chain id" }
        if (prepared.tokenContract != null) {
            AddressValidation.requireEvm(prepared.tokenContract)
            val recipient = prepared.recipient
                ?: error("ERC-20 transfer must include recipient")
            AddressValidation.requireEvm(recipient)
        }
    }

    fun requireBitcoin(plan: BitcoinSendPlan) {
        require(plan.network == BitcoinNetwork.MAINNET || plan.network == BitcoinNetwork.TESTNET)
        require(plan.amountSats > 0)
        require(plan.feeSats > 0)
        require(plan.feeRateSatVb > 0)
        require(plan.inputs.isNotEmpty())
        require(plan.changeSats >= 0)
        require(plan.inputs.sumOf { it.valueSats } >= plan.amountSats + plan.feeSats)
    }
}
