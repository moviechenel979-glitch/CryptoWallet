package com.cryptovault

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealTransactionPolicyTest {
    @Test fun validEvmAddressAccepted() {
        AddressValidation.requireEvm("0x0000000000000000000000000000000000000001")
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedAddressRejected() {
        AddressValidation.requireEvm("0x1234")
    }

    @Test fun erc20TransferSelectorIsCorrect() {
        val data = Erc20Codec.transferCall(
            "0x0000000000000000000000000000000000000001", BigInteger.TEN)
        assertTrue(data.startsWith("0xa9059cbb"))
        assertEquals(138, data.length)
    }
}
