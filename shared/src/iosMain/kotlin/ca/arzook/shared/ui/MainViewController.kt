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
    loadToken = { NSUserDefaults.standardUserDefaults.stringForKey("auth_token") }
)

fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider {
        ArzookApp(authViewModel = sharedAuthViewModel)
    }
}
