package ca.arzook.shared.ui

import ca.arzook.shared.Result
import ca.arzook.shared.model.AuthenticatedData
import ca.arzook.shared.model.LoginRequest
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun decodeUser(json: String): AuthenticatedData? =
    try { Json.decodeFromString(AuthenticatedData.serializer(), json) } catch (_: Exception) { null }

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(
    private val saveToken: (String?) -> Unit,
    private val loadToken: suspend () -> String?,
    private val saveUser: (String?) -> Unit = {},
    private val loadUser: suspend () -> String? = { null },
    private val saveBiometricEnabled: (Boolean) -> Unit = {},
    private val loadBiometricEnabled: suspend () -> Boolean = { false },
    initialUser: AuthenticatedData? = null,
) {
    private val repo = ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca")
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _userDetails = MutableStateFlow<AuthenticatedData?>(initialUser)
    val userDetails: StateFlow<AuthenticatedData?> = _userDetails.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    init {
        scope.launch {
            _biometricEnabled.value = loadBiometricEnabled()
            val saved = loadToken()
            // Strip any "Bearer " prefix that may have been saved by older app versions
            val raw = if (saved != null && saved.startsWith("Bearer ", ignoreCase = true)) saved.substring(7) else saved
            println("[Auth] init: saved token=${if (raw != null) "exists (${raw.take(30)}...)" else "null"}")
            if (raw != saved && raw != null) {
                println("[Auth] init: stripping Bearer prefix from saved token, re-saving")
                saveToken(raw)
            }
            _token.value = raw
            if (_userDetails.value == null) {
                val cachedUser = loadUser()
                if (cachedUser != null) {
                    try { _userDetails.value = Json.decodeFromString(AuthenticatedData.serializer(), cachedUser) } catch (e: Exception) { println("[Auth] init: decode error: $e") }
                }
            }
            if (raw != null) {
                _loginState.value = LoginState.Success
                loadUserDetails(raw)
            }
        }
    }

    fun login(email: String, password: String) {
        scope.launch {
            _loginState.value = LoginState.Loading
            println("[Auth] login attempt: $email & password: $password")
            val recaptcha = try {
                kotlinx.coroutines.withTimeoutOrNull(5000) { getRecaptchaToken("login") } ?: ""
            } catch (_: Exception) { "" }
            println("[Auth] recaptcha attempt: $recaptcha")
            when (val result = repo.login(LoginRequest(email, password, recaptcha))) {
                is Result.Success -> {
                    println("[Auth] login success, token=${result},,,${result.data}")
                    val t = result.data.resolvedToken()
                    println("[Auth] login success, token=${t}")
                    if (t.isEmpty()) {
                        println("[Auth] warning: token is empty, raw=${result.data}")
                        _loginState.value = LoginState.Error("Login failed: no token received")
                        return@launch
                    }
                    val raw = if (t.startsWith("Bearer ", ignoreCase = true)) t.substring(7) else t
                    saveToken(raw)
                    _token.value = raw
                    _loginState.value = LoginState.Success
                    loadUserDetails(raw)
                }
                is Result.Error -> {
                    println("[Auth] login error: ${result.message}")
                    _loginState.value = LoginState.Error(result.message)
                }
            }
        }
    }

    // Called from Swift after social login completes natively
    fun receiveToken(token: String) {
        // Strip any existing "Bearer " prefix — bearer() in the repo will add it
        val raw = if (token.startsWith("Bearer ", ignoreCase = true)) token.substring(7) else token
        println("[Auth] receiveToken raw=$raw")
        saveToken(raw)
        _token.value = raw
        _loginState.value = LoginState.Success
        loadUserDetails(raw)
    }

    fun googleSignIn(idToken: String) {
        scope.launch {
            _loginState.value = LoginState.Loading
            when (val result = repo.googleSignIn(idToken)) {
                is Result.Success -> {
                    println("[login with google] $result")
                    val t = result.data.resolvedToken()
                    println("[Auth] login success, token=${t}")
                    val raw = if (t.startsWith("Bearer ", ignoreCase = true)) t.substring(7) else t
                    saveToken(raw)
                    _token.value = raw
                    _loginState.value = LoginState.Success
                    loadUserDetails(raw)
                }
                is Result.Error -> _loginState.value = LoginState.Error(result.message)
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        saveBiometricEnabled(enabled)
    }

    fun biometricLogin() {
        scope.launch {
            if (!_biometricEnabled.value) return@launch
            val savedToken = loadToken() ?: return@launch
            val result = authenticateWithBiometrics("Sign in to Arzook")
            if (result == BiometricResult.Success) {
                val raw = if (savedToken.startsWith("Bearer ", ignoreCase = true)) savedToken.substring(7) else savedToken
                _token.value = raw
                _loginState.value = LoginState.Success
                loadUserDetails(raw)
            }
        }
    }

    fun logout() {
        saveToken(null)
        saveUser(null)
        saveBiometricEnabled(false)
        _token.value = null
        _userDetails.value = null
        _biometricEnabled.value = false
        _loginState.value = LoginState.Idle
    }

    fun updateProfile(request: ca.arzook.shared.model.UpdateProfileRequest, onResult: (Boolean) -> Unit) {
        val t = _token.value ?: return
        scope.launch {
            when (val r = repo.updateProfile(t, request)) {
                is Result.Success -> {
                    _userDetails.value = r.data
                    saveUser(Json.encodeToString(ca.arzook.shared.model.AuthenticatedData.serializer(), r.data))
                    onResult(true)
                }
                is Result.Error -> { println("[Auth] updateProfile error: ${r.message}"); onResult(false) }
            }
        }
    }

    fun loadUserDetails(token: String) {
        scope.launch {
            println("[Auth] loadUserDetails calling api/profile/me, token prefix=${token.take(20)}")
            when (val r = repo.getUserDetails(token)) {
                is Result.Success -> {
                    println("[Auth] getUserDetails success: email=${r.data.email} firstName=${r.data.firstName}")
                    _userDetails.value = r.data
                    saveUser(Json.encodeToString(AuthenticatedData.serializer(), r.data))
                }
                is Result.Error -> println("[Auth] getUserDetails error: ${r.message}")
            }
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String, inviterEmail: String?) {
        scope.launch {
            _loginState.value = LoginState.Loading
            val recaptcha = getRecaptchaToken("register")
            when (val result = repo.register(ca.arzook.shared.model.User(firstName, lastName, email, password, inviterEmail, recaptcha))) {
                is Result.Success -> login(email, password)
                is Result.Error -> _loginState.value = LoginState.Error(result.message)
            }
        }
    }
}
