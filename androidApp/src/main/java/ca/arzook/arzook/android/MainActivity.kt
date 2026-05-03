package ca.arzook.arzook.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.arzook.shared.ui.ArzookApp
import ca.arzook.shared.ui.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "auth")
private val TOKEN_KEY = stringPreferencesKey("token")
private val USER_KEY = stringPreferencesKey("user")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        ca.arzook.shared.ui.androidAppContext = ctx

        setContent {
            val authViewModel = remember {
                AuthViewModel(
                    saveToken = { token ->
                        CoroutineScope(Dispatchers.IO).launch {
                            ctx.dataStore.edit {
                                if (token == null) it.remove(TOKEN_KEY) else it[TOKEN_KEY] = token
                            }
                        }
                    },
                    loadToken = { ctx.dataStore.data.first()[TOKEN_KEY] },
                    saveUser = { user ->
                        CoroutineScope(Dispatchers.IO).launch {
                            ctx.dataStore.edit {
                                if (user == null) it.remove(USER_KEY) else it[USER_KEY] = user
                            }
                        }
                    },
                    loadUser = { ctx.dataStore.data.first()[USER_KEY] },
                    initialUser = null
                )
            }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ArzookApp(authViewModel = authViewModel)
                }
            }
        }
    }
}
