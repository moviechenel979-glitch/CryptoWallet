package com.cryptovault

object AddressPoisoningGuard {
    fun suspicious(previous: List<String>, recipient: String): Boolean {
        val r = recipient.lowercase()
        if (r.length < 10) return false
        return previous.any { p ->
            val a = p.lowercase()
            a != r && a.take(6) == r.take(6) && a.takeLast(4) == r.takeLast(4)
        }
    }
}
