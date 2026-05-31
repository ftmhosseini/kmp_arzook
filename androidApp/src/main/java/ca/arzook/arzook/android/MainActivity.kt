package ca.arzook.arzook.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.arzook.shared.ui.ArzookApp
import ca.arzook.shared.ui.AuthViewModel
import ca.arzook.shared.ui.Screen
import ca.arzook.shared.ui.currentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "auth")
private val TOKEN_KEY = stringPreferencesKey("token")
private val USER_KEY = stringPreferencesKey("user")
private val BIOMETRIC_KEY = booleanPreferencesKey("biometric_enabled")

private val FROM_CURRENCY_KEY = stringPreferencesKey("from_currency")
private val TO_CURRENCY_KEY = stringPreferencesKey("to_currency")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        ca.arzook.shared.ui.androidAppContext = ctx
        currentActivity = this

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
                    saveBiometricEnabled = { enabled ->
                        CoroutineScope(Dispatchers.IO).launch {
                            ctx.dataStore.edit { it[BIOMETRIC_KEY] = enabled }
                        }
                    },
                    loadBiometricEnabled = { ctx.dataStore.data.first()[BIOMETRIC_KEY] ?: false },
                    initialUser = null
                )
            }
            var initialFrom by remember { mutableStateOf("CAD") }
            var initialTo by remember { mutableStateOf("IRR") }
            var settingsLoaded by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                val prefs = ctx.dataStore.data.first()
                prefs[FROM_CURRENCY_KEY]?.let { initialFrom = it }
                prefs[TO_CURRENCY_KEY]?.let { initialTo = it }
                settingsLoaded = true
            }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (settingsLoaded) {
                        ArzookApp(
                            authViewModel = authViewModel,
                            initialFromCurrency = initialFrom,
                            initialToCurrency = initialTo,
                            onCurrencySettingChanged = { from, to ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    ctx.dataStore.edit {
                                        it[FROM_CURRENCY_KEY] = from
                                        it[TO_CURRENCY_KEY] = to
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
