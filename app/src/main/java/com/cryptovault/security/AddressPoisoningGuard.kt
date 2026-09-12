package com.cryptovault.security

object AddressPoisoningGuard {
    fun suspicious(address: String, knownAddress: String?): Boolean {
        if (knownAddress.isNullOrBlank()) return false
        val a = normalize(address)
        val b = normalize(knownAddress)
        if (a == b) return false
        if (a.length < 10 || b.length < 10) return false
        return a.take(6) == b.take(6) || a.takeLast(6) == b.takeLast(6)
    }
    private fun normalize(s: String) = s.removePrefix("0x").lowercase()
}
