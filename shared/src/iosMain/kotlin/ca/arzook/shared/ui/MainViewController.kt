package ca.arzook.shared.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUserDefaults

// Orientation lock: true = landscape allowed
var isLandscapeRequested = false

// Shared instance so Swift can call googleSignIn after OAuth redirect
val sharedAuthViewModel = AuthViewModel(
    saveToken = { token ->
        if (token == null) NSUserDefaults.standardUserDefaults.removeObjectForKey("auth_token")
        else NSUserDefaults.standardUserDefaults.setObject(token, "auth_token")
    },
    loadToken = { NSUserDefaults.standardUserDefaults.stringForKey("auth_token") },
    saveBiometricEnabled = { enabled ->
        NSUserDefaults.standardUserDefaults.setBool(enabled, "biometric_enabled")
    },
    loadBiometricEnabled = { NSUserDefaults.standardUserDefaults.boolForKey("biometric_enabled") }
)

fun MainViewController() = ComposeUIViewController {
    val defaults = NSUserDefaults.standardUserDefaults
    val savedFrom = defaults.stringForKey("from_currency") ?: "CAD"
    val savedTo = defaults.stringForKey("to_currency") ?: "IRR"
    CompositionLocalProvider {
        ArzookApp(
            authViewModel = sharedAuthViewModel,
            initialFromCurrency = savedFrom,
            initialToCurrency = savedTo,
            onCurrencySettingChanged = { from, to ->
                defaults.setObject(from, "from_currency")
                defaults.setObject(to, "to_currency")
            }
        )
    }
}
