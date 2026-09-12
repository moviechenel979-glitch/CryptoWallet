package com.cryptovault
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricGate(private val activity: FragmentActivity) {
    fun authenticate(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val manager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onFailure("Не настроена биометрия или PIN/пароль устройства.")
            return
        }
        val prompt = BiometricPrompt(activity,
            androidx.core.content.ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) = onSuccess()
                override fun onAuthenticationError(code: Int, msg: CharSequence) = onFailure(msg.toString())
            })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("CryptoVault")
            .setSubtitle("Подтвердите доступ к секрету кошелька")
            .setAllowedAuthenticators(authenticators)
            .build())
    }
}
