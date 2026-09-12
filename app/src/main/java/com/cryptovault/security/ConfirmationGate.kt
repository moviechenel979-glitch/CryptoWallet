package com.cryptovault.security

/**
 * Deliberate security boundary: no signing/broadcast operation may call through
 * without a fresh explicit confirmation from the user-facing review screen.
 */
class ConfirmationGate {
    fun requireExplicitConfirmation(confirmed: Boolean) {
        require(confirmed) { "Explicit user confirmation is required" }
    }
}
