package ca.arzook.shared.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun isBiometricAvailable(): Boolean {
    val manager = BiometricManager.from(androidAppContext)
    return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
}

actual suspend fun authenticateWithBiometrics(reason: String): BiometricResult {
    if (!isBiometricAvailable()) return BiometricResult.NotAvailable

    val activity = currentActivity as? FragmentActivity ?: return BiometricResult.NotAvailable

    return suspendCancellableCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(BiometricResult.Success)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (cont.isActive) {
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON)
                        cont.resume(BiometricResult.Cancelled)
                    else cont.resume(BiometricResult.Failed)
                }
            }
            override fun onAuthenticationFailed() { /* partial failure, prompt stays open */ }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle(reason)
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }
}

// Set from MainActivity
var currentActivity: android.app.Activity? = null
