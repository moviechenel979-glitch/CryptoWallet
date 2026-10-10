package com.cryptovault

import org.junit.Assert.assertEquals
import org.junit.Test

class BitcoinTransactionServiceTest {
    @Test
    fun selectsEnoughUtxosAndCalculatesChange() {
        val service = BitcoinTransactionService()
        val plan = service.prepare(
            network = BitcoinNetwork.MAINNET,
            from = "bc1qfrom",
            to = "bc1qto",
            amountSats = 100_000L,
            feeSats = 5_000L,
            feeRateSatVb = 1L,
            utxos = listOf(
                BitcoinUtxo("a", 0, 50_000L, "0014aa"),
                BitcoinUtxo("b", 1, 70_000L, "0014bb")
            )
        )
        assertEquals(15_000L, plan.changeSats)
        assertEquals(2, plan.inputs.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun insufficientFundsRejected() {
        BitcoinTransactionService().prepare(
            network = BitcoinNetwork.MAINNET,
            from = "from",
            to = "to",
            amountSats = 100_000L,
            feeSats = 5_000L,
            feeRateSatVb = 1L,
            utxos = listOf(BitcoinUtxo("a", 0, 10_000L, "0014aa"))
        )
    }
}
