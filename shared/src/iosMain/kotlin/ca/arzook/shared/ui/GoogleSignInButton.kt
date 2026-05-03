package ca.arzook.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.BuildConfig

// Set by Swift before the UI is shown
var onGoogleSignInRequested: (() -> Unit)? = null

@Composable
actual fun GoogleSignInButton(onIdToken: (String) -> Unit) {
    OutlinedButton(
        onClick = { onGoogleSignInRequested?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text("Sign in with Google", color = Color.DarkGray)
    }
}
