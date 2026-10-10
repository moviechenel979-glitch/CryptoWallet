package com.cryptovault.security

import android.content.Context
import android.os.Build
import android.os.Debug

/**
 * Lightweight environment signals for production builds.
 * Not a substitute for Play Integrity / SafetyNet.
 */
object AppIntegrity {
    data class Report(
        val debuggerAttached: Boolean,
        val suspiciousBuildTags: Boolean,
        val emulatorHints: Boolean
    )

    fun evaluate(): Report {
        val tags = (Build.TAGS ?: "").lowercase()
        val fingerprint = (Build.FINGERPRINT ?: "").lowercase()
        val product = (Build.PRODUCT ?: "").lowercase()
        val model = (Build.MODEL ?: "").lowercase()
        return Report(
            debuggerAttached = Debug.isDebuggerConnected(),
            suspiciousBuildTags = tags.contains("test-keys"),
            emulatorHints = listOf("generic", "emulator", "sdk_gphone", "goldfish", "ranchu")
                .any { fingerprint.contains(it) || product.contains(it) || model.contains(it) }
        )
    }

    fun noteEnvironment(context: Context) {
        // Intentional no network call — local only
        evaluate()
        context.applicationContext
    }

    fun shouldWarnBeforeMainnet(): Boolean {
        val r = evaluate()
        return r.debuggerAttached || r.suspiciousBuildTags
    }
}
