package com.cryptovault

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class Eip1559PolicyTest {
    @Test fun effectivePriceIsCappedByMaxFee() {
        assertEquals(BigInteger("30"), Eip1559Policy.effectiveGasPrice(BigInteger("20"), BigInteger("10"), BigInteger("35")))
    }

    @Test fun rejectsMaxFeeBelowBaseFee() {
        assertFails { Eip1559Policy.validate(BigInteger("20"), BigInteger("2"), BigInteger("19")) }
    }
}
