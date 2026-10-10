package com.cryptovault

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class Eip1559PolicyTest {
    @Test
    fun effectivePriceIsCappedByMaxFee() {
        assertEquals(
            BigInteger("30"),
            Eip1559Policy.effectiveGasPrice(BigInteger("20"), BigInteger("10"), BigInteger("35"))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMaxFeeBelowBaseFee() {
        Eip1559Policy.validate(BigInteger("20"), BigInteger("2"), BigInteger("19"))
    }
}
