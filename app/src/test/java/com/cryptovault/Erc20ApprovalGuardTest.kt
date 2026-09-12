package com.cryptovault

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Erc20ApprovalGuardTest {
    @Test fun decodesUnlimitedApproval() {
        val data = "0x095ea7b3" + "0000000000000000000000001111111111111111111111111111111111111111" + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        val approval = Erc20ApprovalGuard.decode(data)
        assertNotNull(approval)
        assertEquals("0x1111111111111111111111111111111111111111", approval.spender)
        assertTrue(approval.unlimited)
    }
}
