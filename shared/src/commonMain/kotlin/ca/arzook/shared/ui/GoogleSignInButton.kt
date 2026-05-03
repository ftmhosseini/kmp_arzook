package ca.arzook.shared.ui

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleSignInButton(onIdToken: (String) -> Unit)
