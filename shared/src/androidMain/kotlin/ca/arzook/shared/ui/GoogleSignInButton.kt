package ca.arzook.shared.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import ca.arzook.shared.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
actual fun GoogleSignInButton(onIdToken: (String) -> Unit) {
    val context = LocalContext.current
    val activityContext = context as? android.app.Activity ?: run {
        var ctx: android.content.Context = context
        while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) {
            ctx = ctx.baseContext
        }
        ctx
    }
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                try {
                    val credentialManager = CredentialManager.create(activityContext)
                    val signInOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_CLIENT_ID_WEB).build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()
                    val result = credentialManager.getCredential(activityContext, request)
                    val idToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                    Log.d("GoogleSignIn", "idToken empty=${idToken.isNullOrEmpty()} token=$idToken")
                    if (idToken.isNullOrEmpty()) {
                        Log.e("GoogleSignIn", "credential data=${result.credential.data}")
                        return@launch
                    }
                    onIdToken(idToken)
                } catch (e: GetCredentialException) {
                    Log.e("GoogleSignIn", "type=${e.type} msg=${e.message}", e)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Unexpected: ${e.message}", e)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text("Sign in with Google", color = Color.DarkGray)
    }
}
