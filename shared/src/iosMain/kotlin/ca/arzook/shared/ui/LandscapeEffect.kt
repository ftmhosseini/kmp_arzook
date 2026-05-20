package ca.arzook.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

// Callback to be set from Swift side
var onRequestLandscape: (() -> Unit)? = null
var onRequestPortrait: (() -> Unit)? = null

@Composable
actual fun LandscapeEffect() {
    DisposableEffect(Unit) {
        isLandscapeRequested = true
        onRequestLandscape?.invoke()
        onDispose {
            isLandscapeRequested = false
            onRequestPortrait?.invoke()
        }
    }
}