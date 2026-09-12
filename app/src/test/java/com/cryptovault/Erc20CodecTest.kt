package com.cryptovault

import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class Erc20CodecTest {
    @Test fun balanceCallHasSelectorAndWord() {
        val call = Erc20Codec.balanceOfCall("0x0000000000000000000000000000000000000001")
        assertTrue(call.startsWith("0x70a08231"))
        assertTrue(call.length == 74)
    }

    @Test fun transferCallContainsSelector() {
        val call = Erc20Codec.transferCall(
            "0x0000000000000000000000000000000000000001",
            BigInteger.ONE
        )
        assertTrue(call.startsWith("0xa9059cbb"))
        assertTrue(call.length == 138)
    }
}
