package com.cryptovault

import android.app.Application
import android.os.StrictMode
import com.cryptovault.security.AppIntegrity
import com.cryptovault.security.ReleaseHardening

class CryptoVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
        }
        AppIntegrity.noteEnvironment(this)
        // Document compile-time hardening flags (no-op runtime markers)
        check(ReleaseHardening.NEVER_LOG_SEED_OR_PRIVATE_KEY)
        check(ReleaseHardening.DISABLE_CLEAR_TEXT_NETWORK)
    }
}
