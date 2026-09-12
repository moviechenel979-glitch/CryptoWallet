package com.cryptovault

import org.junit.Assert.assertEquals
import org.junit.Test

class BitcoinTransactionServiceTest {
    @Test fun selectsEnoughUtxosAndCalculatesChange() {
        val service = BitcoinTransactionService()
        val plan = service.prepare(
            BitcoinNetwork.MAINNET,
            "bc1qfrom",
            "bc1qto",
            100_000L,
            5_000L,
            listOf(
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
            BitcoinNetwork.MAINNET, "from", "to", 100_000L, 5_000L,
            listOf(BitcoinUtxo("a", 0, 10_000L, "0014aa"))
        )
    }
}
