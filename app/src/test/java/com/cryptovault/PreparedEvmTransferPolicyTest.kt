package com.cryptovault

import org.junit.Test
import java.math.BigInteger

class PreparedEvmTransferPolicyTest {
    private fun sampleNative() = PreparedEvmTransfer(
        network = Network.SEPOLIA,
        from = "0x0000000000000000000000000000000000000001",
        to = "0x0000000000000000000000000000000000000002",
        amountWei = BigInteger.TEN,
        nonce = 1,
        gasPriceWei = BigInteger("1000000000"),
        gasLimit = BigInteger.valueOf(21_000),
        estimatedFeeWei = BigInteger("21000000000000")
    )

    private fun sampleErc20() = PreparedEvmTransfer(
        network = Network.SEPOLIA,
        from = "0x0000000000000000000000000000000000000001",
        to = "0x00000000000000000000000000000000000000aa",
        amountWei = BigInteger.ZERO,
        nonce = 2,
        gasPriceWei = BigInteger("1000000000"),
        gasLimit = BigInteger.valueOf(60_000),
        estimatedFeeWei = BigInteger("60000000000000"),
        tokenContract = "0x00000000000000000000000000000000000000aa",
        tokenAmount = BigInteger("1000"),
        recipient = "0x0000000000000000000000000000000000000002"
    )

    @Test
    fun nativePolicyPasses() {
        TransactionPolicyV3.requireEvm(sampleNative())
    }

    @Test
    fun erc20PolicyPassesWithRecipient() {
        TransactionPolicyV3.requireEvm(sampleErc20())
    }

    @Test(expected = IllegalStateException::class)
    fun erc20WithoutRecipientFails() {
        TransactionPolicyV3.requireEvm(sampleErc20().copy(recipient = null))
    }
}
