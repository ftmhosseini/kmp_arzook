package ca.arzook.shared.ui

enum class BiometricResult {
    Success, Failed, NotAvailable, Cancelled
}

expect suspend fun authenticateWithBiometrics(reason: String): BiometricResult

expect fun isBiometricAvailable(): Boolean
