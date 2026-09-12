package com.cryptovault.security

object ReleaseHardening {
    const val REQUIRE_BIOMETRIC_BEFORE_SECRET_DECRYPT = true
    const val DISABLE_BACKUP_OF_SECRET_STORE = true
    const val DISABLE_CLEAR_TEXT_NETWORK = true
    const val BLOCK_SCREENSHOTS_ON_SECRET_SCREENS = true
    const val NEVER_LOG_SEED_OR_PRIVATE_KEY = true
}
