package com.cryptovault

import java.time.Instant

data class SecurityEvent(
    val time: String,
    val type: String
)

/**
 * Does not store seed, private keys, raw transaction secrets or addresses.
 * Intended for local diagnostic/security events only.
 */
class AuditLog {
    private val events = mutableListOf<SecurityEvent>()

    fun add(type: String) {
        events += SecurityEvent(Instant.now().toString(), type)
    }

    fun snapshot(): List<SecurityEvent> = events.toList()
}
