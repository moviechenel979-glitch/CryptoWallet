package com.cryptovault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Erc20ApprovalGuardTest {
    @Test
    fun decodesUnlimitedApproval() {
        val data = "0x095ea7b3" +
            "0000000000000000000000001111111111111111111111111111111111111111" +
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        val approval = Erc20ApprovalGuard.decode(data)
        assertNotNull(approval)
        assertEquals("0x1111111111111111111111111111111111111111", approval!!.spender)
        assertTrue(approval.unlimited)
    }
}
