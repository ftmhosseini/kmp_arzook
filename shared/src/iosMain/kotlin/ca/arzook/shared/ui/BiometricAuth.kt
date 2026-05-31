package ca.arzook.shared.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Foundation.NSError
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual fun isBiometricAvailable(): Boolean {
    val context = LAContext()
    return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun authenticateWithBiometrics(reason: String): BiometricResult {
    if (!isBiometricAvailable()) return BiometricResult.NotAvailable

    return suspendCancellableCoroutine { cont ->
        val context = LAContext()
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = reason
        ) { success, error ->
            if (cont.isActive) {
                when {
                    success -> cont.resume(BiometricResult.Success)
                    error != null && (error.code == -2L || error.code == -4L) -> cont.resume(BiometricResult.Cancelled) // userCancel or systemCancel
                    else -> cont.resume(BiometricResult.Failed)
                }
            }
        }
    }
}
