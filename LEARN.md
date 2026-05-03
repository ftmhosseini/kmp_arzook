# Learning Mobile Development Through the Arzook Project

This guide teaches everything a mobile developer needs to know — from Kotlin basics to architecture patterns like ViewModel, Repository, and Dependency Injection — using real code from this project.

---

## Table of Contents

1. [What is a Program?](#1-what-is-a-program)
2. [Kotlin Basics](#2-kotlin-basics)
3. [Object-Oriented Programming](#3-object-oriented-programming)
4. [Kotlin Advanced Features](#4-kotlin-advanced-features)
5. [Coroutines & Async Programming](#5-coroutines--async-programming)
6. [Jetpack Compose — Building UI](#6-jetpack-compose--building-ui)
7. [ViewModel — Managing State](#7-viewmodel--managing-state)
8. [Repository Pattern](#8-repository-pattern)
9. [Dependency Injection](#9-dependency-injection)
10. [Navigation](#10-navigation)
11. [Data Persistence — DataStore](#11-data-persistence--datastore)
12. [Networking with Ktor](#12-networking-with-ktor)
13. [Kotlin Multiplatform (KMP)](#13-kotlin-multiplatform-kmp)
14. [Android App Lifecycle](#14-android-app-lifecycle)
15. [Real Bugs Fixed in This Project](#15-real-bugs-fixed-in-this-project)
16. [Glossary](#16-glossary)

---

## 1. What is a Program?

A program is a set of instructions for a computer. A mobile app is a program that runs on a phone. We write those instructions in a **programming language** — this project uses **Kotlin**.

---

## 2. Kotlin Basics

### Variables

```kotlin
val name = "Arzook"   // val = immutable (cannot reassign)
var score = 0         // var = mutable (can reassign)
score = 10
```

### Types

```kotlin
val text: String = "hello"
val number: Int = 42
val price: Double = 16.5
val isLoggedIn: Boolean = true
```

### Nullable Types

In Kotlin, a variable **cannot** be null by default. Add `?` to allow null:

```kotlin
val city: String? = null   // allowed
val name: String = null    // COMPILE ERROR — not allowed
```

Use `?.` to safely access nullable values, and `?:` for a fallback:

```kotlin
val display = user.city?.uppercase() ?: "Unknown"
```

In this project, many server fields are nullable because the API sometimes omits them:
```kotlin
// From Trade.kt
val urgent: Boolean? = null
val purposeOfTransaction: String? = null
```

### Functions

```kotlin
fun greet(name: String): String {
    return "Hello, $name!"
}

// Single-expression shorthand
fun greet(name: String) = "Hello, $name!"
```

### String Templates

```kotlin
val amount = 500.0
println("Trade amount: $amount CAD")
println("Token: ${token.take(10)}...")
```

### If / Else

```kotlin
if (isLoggedIn) {
    showDashboard()
} else {
    showLoginScreen()
}

// As an expression
val label = if (trade.selling) "Selling" else "Buying"
```

### When (like switch)

```kotlin
val color = when {
    trade.status != null -> DarkGreen
    trade.deposited == true -> Green
    else -> Cream
}
```

Used in this project to decide trade card background colors.

### Lists and Loops

```kotlin
val trades = listOf(trade1, trade2, trade3)

for (trade in trades) {
    println(trade.amount)
}

// Functional style
trades.forEach { println(it.amount) }
trades.filter { it.selling }.map { it.amount }
```

---

## 3. Object-Oriented Programming

### Classes

A class is a blueprint. An object is an instance of that blueprint.

```kotlin
class TradeCard(val amount: Double, val selling: Boolean) {
    fun label() = if (selling) "Selling" else "Buying"
}

val card = TradeCard(500.0, true)
println(card.label())  // "Selling"
```

### Data Classes

Used purely to hold data. Kotlin auto-generates `equals`, `hashCode`, `toString`, and `copy`.

```kotlin
data class TradeItem(
    val id: String,
    val amount: Double,
    val selling: Boolean,
    val deposited: Boolean? = null,
    val urgent: Boolean? = null
)
```

Every model in `shared/model/` is a data class.

### Interfaces

An interface defines a contract — what functions must exist, without implementing them.

```kotlin
interface ArzookRepository {
    suspend fun getTradesList(): Result<List<TradeItem>>
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}
```

`ArzookRepositoryImpl` implements this interface. This separation makes it easy to swap implementations (e.g. for testing).

### Sealed Classes

A sealed class is a restricted class hierarchy — all subclasses are known at compile time.

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String) : Result<T>()
}
```

Every API call in this project returns `Result<T>`:

```kotlin
when (val r = repo.getTradesList()) {
    is Result.Success -> _cadTrades.value = r.data
    is Result.Error -> println("Error: ${r.message}")
}
```

Also used for screen navigation and login state:

```kotlin
sealed class Screen {
    data object Home : Screen()
    data object Login : Screen()
    data class Content(val title: String) : Screen()
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
```

### Companion Objects

Like `static` in Java/Swift — belongs to the class, not an instance.

```kotlin
class Config {
    companion object {
        const val BASE_URL = "https://api.arzook.ca"
    }
}
```

---

## 4. Kotlin Advanced Features

### Extension Functions

Add functions to existing classes without modifying them.

```kotlin
fun String.bearer() = "Bearer $this"

// Used in ArzookRepositoryImpl:
header(HttpHeaders.Authorization, token.bearer())
```

### Lambda Functions

A function passed as a value.

```kotlin
val double: (Int) -> Int = { x -> x * 2 }
println(double(5))  // 10
```

Used everywhere in Compose for click handlers:
```kotlin
Button(onClick = { screen = Screen.Login }) { Text("Sign In") }
```

### Higher-Order Functions

Functions that take other functions as parameters.

```kotlin
fun runIfLoggedIn(token: String?, action: () -> Unit) {
    if (token != null) action() else showLogin()
}
```

### Generics

Write code that works with any type.

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String) : Result<T>()
}

// Works for any type:
Result.Success(listOf(trade1, trade2))   // Result<List<TradeItem>>
Result.Success("token123")               // Result<String>
```

### `inline` + `reified`

Used in `safeCall` to preserve the generic type at runtime:

```kotlin
private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> =
    try { Result.Success(block()) } catch (e: Exception) { Result.Error(e.message ?: "Unknown error") }
```

`reified` lets Kotlin know the actual type `T` at runtime, needed for Ktor's `.body<T>()` deserialization.


---

## 5. Coroutines & Async Programming

### The Problem

Network calls take time. If you run them on the main thread, the UI freezes. Coroutines solve this.

### What is a Coroutine?

A coroutine is a lightweight thread that can be paused and resumed. You write async code that looks synchronous.

```kotlin
// Without coroutines — blocks the UI thread
val trades = repo.getTradesList()  // freezes app while waiting

// With coroutines — runs in background, UI stays responsive
scope.launch {
    val trades = repo.getTradesList()  // suspends here, doesn't block
    _cadTrades.value = trades
}
```

### `suspend` Functions

A `suspend` function can pause execution without blocking the thread. It can only be called from a coroutine or another suspend function.

```kotlin
suspend fun getTradesList(): Result<List<TradeItem>> {
    return safeCall { client.get("$baseUrl/api/main").body() }
}
```

### CoroutineScope

Every coroutine needs a scope — it defines the lifetime of the coroutine.

```kotlin
// In HomeViewModel — coroutines live as long as the ViewModel
private val scope = CoroutineScope(Dispatchers.Main)

scope.launch {
    // runs in background, result delivered on Main thread
}
```

### Dispatchers

| Dispatcher | Use |
|-----------|-----|
| `Dispatchers.Main` | UI updates |
| `Dispatchers.IO` | Network, disk |
| `Dispatchers.Default` | CPU-heavy work |

```kotlin
// DataStore writes use IO dispatcher
CoroutineScope(Dispatchers.IO).launch {
    ctx.dataStore.edit { it[TOKEN_KEY] = token }
}
```

### StateFlow

A `StateFlow` holds a value and emits updates to all collectors. It's the bridge between ViewModel and UI.

```kotlin
// In ViewModel — mutable internally
private val _cadTrades = MutableStateFlow<List<TradeItem>>(emptyList())

// Exposed to UI — read-only
val cadTrades: StateFlow<List<TradeItem>> = _cadTrades.asStateFlow()

// Update it
_cadTrades.value = newTrades
```

In the screen, collect it as Compose state:
```kotlin
val trades by homeViewModel.cadTrades.collectAsState()
// "trades" recomposes the UI automatically when data changes
```

### LaunchedEffect

Run a coroutine when a composable enters composition, or when a key changes.

```kotlin
// Runs once when the screen appears
LaunchedEffect(Unit) {
    homeViewModel.loadData()
}

// Runs every time token changes
LaunchedEffect(token) {
    if (!token.isNullOrEmpty()) {
        homeViewModel.loadUserData(token)
    }
}
```

Used in `ArzookApp.kt` to load user data when the token changes (login/logout).

### `runBlocking`

Blocks the current thread until the coroutine completes. Only use at app startup, never in UI.

```kotlin
// In MainActivity — read cached user before first composition
val cachedUser = runBlocking {
    ctx.dataStore.data.first()[USER_KEY]
}
```

---

## 6. Jetpack Compose — Building UI

### What is Compose?

Compose is a declarative UI toolkit. Instead of XML layouts, you write Kotlin functions that describe what the UI should look like.

```kotlin
@Composable
fun TradeCard(trade: TradeItem) {
    Card {
        Text("Amount: ${trade.amount}")
        Text(if (trade.selling) "Selling" else "Buying")
    }
}
```

### `@Composable` Annotation

Marks a function as a UI component. Compose re-runs ("recomposes") it whenever its inputs change.

### State in Compose

```kotlin
// Local state — lives inside the composable
var expanded by remember { mutableStateOf(false) }

// State from ViewModel — survives recomposition
val trades by homeViewModel.cadTrades.collectAsState()
```

`remember` keeps the value across recompositions. Without it, the value resets every time.

### Common Composables Used in This Project

```kotlin
Column { }          // vertical layout
Row { }             // horizontal layout
Box { }             // stack/overlay layout
Text("Hello")       // text
Button(onClick={}) { Text("Click") }
Card { }            // elevated card
LazyColumn { items(list) { item -> ... } }  // scrollable list
HorizontalPager(state = pagerState) { page -> ... }  // swipeable pages
Spacer(Modifier.height(16.dp))
```

### Modifiers

Modifiers describe how a composable looks and behaves:

```kotlin
Text(
    "Arzook",
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .background(Color.Blue)
        .clip(RoundedCornerShape(8.dp))
)
```

### Scaffold

The standard screen layout with top bar, bottom bar, and FAB:

```kotlin
Scaffold(
    topBar = { TopAppBar(title = { Text("Arzook") }) },
    bottomBar = { NavigationBar { ... } },
    floatingActionButton = { FabUI(...) }
) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
        // screen content
    }
}
```

This is exactly how `ArzookApp.kt` is structured.

### Dialog

```kotlin
if (showFullscreen) {
    Dialog(
        onDismissRequest = { showFullscreen = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            YoutubePlayer(videoId = id)
            TextButton(onClick = { showFullscreen = false }) { Text("✕ Close") }
        }
    }
}
```

Used in `HomeScreen.kt` for fullscreen video.

### HorizontalPager (Swipeable Pages)

```kotlin
val pagerState = rememberPagerState(pageCount = { videoIds.size })

HorizontalPager(state = pagerState) { page ->
    YoutubePlayer(videoId = videoIds[page])
}

PageIndicator(pageCount = videoIds.size, currentPage = pagerState.currentPage)
```

Used for both the banner carousel and the video pager in `HomeScreen.kt`.


---

## 7. ViewModel — Managing State

### What is a ViewModel?

A ViewModel holds and manages UI-related data. It survives configuration changes (like screen rotation) and keeps data separate from the UI layer.

```
Screen (Composable)  ←→  ViewModel  ←→  Repository  ←→  Network
```

The screen never fetches data directly. It only reads from the ViewModel and calls its functions.

### This Project's ViewModels

**AuthViewModel** — owns login/logout/token/user profile:

```kotlin
class AuthViewModel(
    private val saveToken: (String?) -> Unit,   // injected
    private val loadToken: suspend () -> String?,
    private val saveUser: (String?) -> Unit,
    private val loadUser: suspend () -> String?,
    initialUser: AuthenticatedData? = null
) {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun login(email: String, password: String) {
        scope.launch {
            _loginState.value = LoginState.Loading
            when (val result = repo.login(LoginRequest(email, password, recaptcha))) {
                is Result.Success -> {
                    saveToken(result.data.resolvedToken())
                    _token.value = result.data.resolvedToken()
                    _loginState.value = LoginState.Success
                }
                is Result.Error -> _loginState.value = LoginState.Error(result.message)
            }
        }
    }

    fun logout() {
        saveToken(null)
        _token.value = null
        _loginState.value = LoginState.Idle
    }
}
```

**HomeViewModel** — owns all app data (trades, wallet, watch list, drafts):

```kotlin
class HomeViewModel {
    private val repo = ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca")
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _cadTrades = MutableStateFlow<List<TradeItem>>(emptyList())
    val cadTrades: StateFlow<List<TradeItem>> = _cadTrades.asStateFlow()

    init {
        scope.launch {
            when (val r = repo.getTradesList()) {
                is Result.Success -> _cadTrades.value = r.data
                is Result.Error -> println("[HomeVM] error: ${r.message}")
            }
        }
    }
}
```

### How the Screen Uses the ViewModel

```kotlin
@Composable
fun TradesScreen(homeViewModel: HomeViewModel) {
    // Collect state — recomposes when data changes
    val trades by homeViewModel.cadTrades.collectAsState()
    val watchList by homeViewModel.watchList.collectAsState()

    LazyColumn {
        items(trades) { trade ->
            TradeCard(
                trade = trade,
                isWatched = watchList.any { it.offeringId == trade.id },
                onWatch = { homeViewModel.watchTrade(token, trade.id) }
            )
        }
    }
}
```

### ViewModel Lifecycle

In this project, `HomeViewModel` is created once in `ArzookApp` using `remember`:

```kotlin
val homeViewModel = remember { HomeViewModel() }
```

`remember` keeps the same instance across recompositions. It lives as long as `ArzookApp` is in composition.

---

## 8. Repository Pattern

### Why Use a Repository?

Without a repository, screens would call the network directly. This creates problems:
- Hard to test (can't swap real network for fake data)
- Logic scattered across screens
- Changing the API URL requires editing every screen

The repository centralizes all data access behind an interface.

### The Pattern

```
Screen → ViewModel → Repository Interface → Repository Implementation → Network
```

### Interface (the contract)

```kotlin
// ArzookRepository.kt
interface ArzookRepository {
    suspend fun getTradesList(): Result<List<TradeItem>>
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun watchTrade(token: String, id: String): Result<Unit>
}
```

### Implementation (the real work)

```kotlin
// ArzookRepositoryImpl.kt
class ArzookRepositoryImpl(
    private val baseUrl: String,
    private val client: HttpClient = createHttpClient()
) : ArzookRepository {

    private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> =
        try { Result.Success(block()) } catch (e: Exception) { Result.Error(e.message ?: "Unknown error") }

    override suspend fun getTradesList(): Result<List<TradeItem>> = safeCall {
        client.get("$baseUrl/api/main?min=0&max=2&displayDepositedOnly=false").body()
    }

    override suspend fun watchTrade(token: String, id: String): Result<Unit> = safeCall {
        client.post("$baseUrl/api/trading/watch/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        Unit  // explicit — safeCall needs a return value
    }
}
```

### `safeCall` — Centralized Error Handling

Instead of wrapping every call in try/catch, `safeCall` does it once:

```kotlin
private suspend inline fun <reified T> safeCall(block: () -> T): Result<T> =
    try { Result.Success(block()) } catch (e: Exception) { Result.Error(e.message ?: "Unknown error") }
```

Every API call returns `Result.Success(data)` or `Result.Error(message)`. The ViewModel handles both cases.

---

## 9. Dependency Injection

### What is Dependency Injection?

Dependency Injection (DI) means giving an object its dependencies from outside, rather than having it create them itself.

**Without DI (hard to test, tightly coupled):**
```kotlin
class HomeViewModel {
    // Creates its own dependency — can't swap for testing
    private val repo = ArzookRepositoryImpl("https://api.arzook.ca")
}
```

**With DI (flexible, testable):**
```kotlin
class HomeViewModel(private val repo: ArzookRepository) {
    // Dependency is injected — can pass a fake repo in tests
}

// Production
val vm = HomeViewModel(repo = ArzookRepositoryImpl("https://api.arzook.ca"))

// Testing
val vm = HomeViewModel(repo = FakeRepository())
```

### Manual DI (What This Project Uses)

This project does **not** use a DI framework (like Hilt or Koin). Dependencies are wired manually in `MainActivity` and `ArzookApp`.

**AuthViewModel** receives its DataStore functions via constructor:

```kotlin
// MainActivity.kt — wiring dependencies manually
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
        saveUser = { user -> /* DataStore write */ },
        loadUser = { ctx.dataStore.data.first()[USER_KEY] },
        initialUser = cachedUser
    )
}
```

This is called **constructor injection** — dependencies are passed through the constructor.

### DI Frameworks (for reference)

In larger Android projects, DI frameworks automate this wiring:

**Hilt (Google, most common):**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ArzookRepository
) : ViewModel()

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides fun provideRepo(): ArzookRepository = ArzookRepositoryImpl("https://api.arzook.ca")
}
```

**Koin (KMP-friendly, simpler):**
```kotlin
val appModule = module {
    single<ArzookRepository> { ArzookRepositoryImpl("https://api.arzook.ca") }
    viewModel { HomeViewModel(get()) }
}
```

This project avoids frameworks to keep the KMP shared module free of Android-specific DI annotations.

### Why DI Matters

| Without DI | With DI |
|-----------|---------|
| Hard to test | Easy to inject fake dependencies |
| Tightly coupled | Loosely coupled |
| Hard to change implementations | Swap implementations without touching callers |
| Singletons everywhere | Controlled lifetimes |


---

## 10. Navigation

### This Project's Approach

Navigation is handled with a single `var screen` state variable in `ArzookApp.kt` — no Jetpack Navigation library.

```kotlin
var screen by remember { mutableStateOf<Screen>(Screen.Splash) }

// Navigate by setting the state
Button(onClick = { screen = Screen.Login }) { Text("Sign In") }

// Render the right screen
when (val s = screen) {
    is Screen.Splash  -> SplashScreen { screen = Screen.Home }
    is Screen.Home    -> HomeScreen(homeViewModel)
    is Screen.Login   -> LoginScreen(onLoginSuccess = { screen = Screen.Home })
    is Screen.Content -> ContentScreen(title = s.title, onBack = { screen = Screen.Home })
    // ...
}
```

### Sealed Class Navigation

`Screen` is a sealed class so the compiler enforces that every screen is handled:

```kotlin
sealed class Screen {
    data object Home : Screen()
    data object Login : Screen()
    data class Content(val title: String) : Screen()  // carries data
}
```

`data class Content(val title: String)` shows how to pass parameters during navigation.

### Jetpack Navigation (for reference)

Larger apps use Jetpack Navigation with a `NavController`:

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("login") { LoginScreen(navController) }
    composable("content/{title}") { backStackEntry ->
        ContentScreen(title = backStackEntry.arguments?.getString("title") ?: "")
    }
}

// Navigate
navController.navigate("content/FAQ")
```

---

## 11. Data Persistence — DataStore

### What is DataStore?

DataStore is Android's modern replacement for SharedPreferences. It stores key-value pairs asynchronously using coroutines.

### Setup (from MainActivity.kt)

```kotlin
private val Context.dataStore by preferencesDataStore(name = "auth")
private val TOKEN_KEY = stringPreferencesKey("token")
private val USER_KEY = stringPreferencesKey("user")
```

### Writing

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    ctx.dataStore.edit { preferences ->
        preferences[TOKEN_KEY] = token
    }
}
```

### Reading

```kotlin
// Async (in coroutine)
val token = ctx.dataStore.data.first()[TOKEN_KEY]

// Sync at startup (blocks thread — only use in onCreate)
val cachedUser = runBlocking {
    ctx.dataStore.data.first()[USER_KEY]
}
```

### Why Cache the User?

Without caching, the profile screen shows a loading spinner every time the app opens while it waits for the network. By reading the cached user synchronously in `onCreate` and passing it as `initialUser` to `AuthViewModel`, the profile appears instantly.

```kotlin
// MainActivity.kt
val cachedUser = runBlocking { ctx.dataStore.data.first()[USER_KEY]?.let { decodeUser(it) } }

val authViewModel = remember {
    AuthViewModel(initialUser = cachedUser, ...)
}
```

---

## 12. Networking with Ktor

### What is Ktor?

Ktor is a Kotlin-native HTTP client that works on both Android and iOS (KMP-compatible). It uses coroutines natively.

### HTTP Client Setup

```kotlin
// shared/network/HttpClient.kt
expect fun createHttpClient(): HttpClient

// androidMain
actual fun createHttpClient() = HttpClient(Android) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(Logging) { level = LogLevel.BODY }
}

// iosMain
actual fun createHttpClient() = HttpClient(Darwin) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}
```

`ignoreUnknownKeys = true` prevents crashes when the server adds new fields the app doesn't know about.

### Making Requests

```kotlin
// GET
client.get("$baseUrl/api/main/daily-stats").body<List<AveRates>>()

// GET with auth header
client.get("$baseUrl/api/profile/me") {
    header(HttpHeaders.Authorization, "Bearer $token")
}.body<AuthenticatedData>()

// POST with JSON body
client.post("$baseUrl/api/auth/login") {
    contentType(ContentType.Application.Json)
    setBody(mapOf("email" to email, "password" to password))
}.body<LoginResponse>()

// POST with raw Double body (service rate endpoint)
client.post("$baseUrl/api/trading/buying-taker-service-rate") {
    header(HttpHeaders.Authorization, "Bearer $token")
    contentType(ContentType.Application.Json)
    setBody(amount)  // raw Double, NOT {"amount": 100.0}
}.body<Double>()
```

### JSON Serialization

Models are annotated with `@Serializable`:

```kotlin
@Serializable
data class TradeItem(
    val id: String,
    val amount: Double,
    val selling: Boolean,
    val deposited: Boolean? = null,
    @SerialName("listingRate") val rate: Double = 0.0
)
```

`@SerialName` maps a JSON key to a different Kotlin property name.

### WebSocket

```kotlin
// WebSocketManager.kt
client.webSocket("$baseUrl/ws?token=$token") {
    for (frame in incoming) {
        if (frame is Frame.Text) {
            val message = Json.decodeFromString<WebSocketMessage>(frame.readText())
            _messages.emit(message)
        }
    }
}
```

---

## 13. Kotlin Multiplatform (KMP)

### How It Works

```
shared/src/
  commonMain/   ← Runs on ALL platforms (Android + iOS)
  androidMain/  ← Android-only implementations
  iosMain/      ← iOS-only implementations
```

### expect / actual

`expect` declares a function that must be implemented per platform. `actual` provides the implementation.

```kotlin
// commonMain — the promise
expect fun createHttpClient(): HttpClient
expect fun openPdf(bytes: ByteArray, fileName: String)

// androidMain — Android implementation
actual fun createHttpClient() = HttpClient(Android) { ... }
actual fun openPdf(bytes: ByteArray, fileName: String) {
    // Android: write to file, open with FileProvider
}

// iosMain — iOS implementation
actual fun createHttpClient() = HttpClient(Darwin) { ... }
actual fun openPdf(bytes: ByteArray, fileName: String) {
    // iOS: write to temp dir, open with UIDocumentInteractionController
}
```

### Compose Multiplatform

Screens written in `commonMain` using Compose render natively on both platforms:

```kotlin
// commonMain — works on Android AND iOS
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    Column {
        BannerView(...)
        Chart(...)
        YoutubePlayer(...)  // expect/actual — different per platform
    }
}
```

### XCFramework for iOS

The shared Kotlin code is compiled into an XCFramework that Xcode links:

```bash
./gradlew :shared:assembleXCFramework
# Output: shared/build/XCFrameworks/release/shared.xcframework
```

---

## 14. Android App Lifecycle

### Activity

`MainActivity` is the entry point of the Android app. It extends `ComponentActivity`.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Compose UI starts here
            ArzookApp(authViewModel = authViewModel)
        }
    }
}
```

### Lifecycle States

```
onCreate → onStart → onResume → [app running]
                              ↓
                          onPause → onStop → onDestroy
```

- `onCreate` — app starts, set up UI
- `onResume` — app is in foreground, user can interact
- `onPause` / `onStop` — app goes to background
- `onDestroy` — app is killed

### Configuration Changes

When the user rotates the screen, Android destroys and recreates the Activity. Data in local variables is lost. `remember` in Compose and `ViewModel` survive this.

### Permissions

Declared in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Without `INTERNET`, all network calls fail silently.

### `applicationContext` vs `this`

```kotlin
// applicationContext — lives as long as the app, safe to store
ca.arzook.shared.ui.androidAppContext = applicationContext

// this (Activity context) — tied to Activity lifecycle, don't store
```

---

## 15. Real Bugs Fixed in This Project

### Bug 1: Wrong API Endpoints
```
❌ api/trade/buying-drafts
✅ api/buyings/buying-drafts

❌ api/trade/current-rate
✅ api/main/rate-stats?selectedCurrencyCode=undefined
```
**Lesson:** Always verify endpoints against a working version of the app.

### Bug 2: Non-Nullable Fields Crashing on Null Server Data
```kotlin
// Crashes when server sends null
val city: String

// Safe
val city: JsonElement? = null
val cityStr get() = try { city?.jsonPrimitive?.content } catch (_: Exception) { null }
```
**Lesson:** Make server-sourced fields nullable. Use `JsonElement?` for fields that can be a string or an object `{}`.

### Bug 3: `safeCall` Silently Failing for Unit Returns
```kotlin
// Silently fails — HttpResponse is not Unit
override suspend fun watchTrade(...): Result<Unit> = safeCall {
    client.post(...)
}

// Fixed — explicit Unit
override suspend fun watchTrade(...): Result<Unit> = safeCall {
    client.post(...)
    Unit
}
```
**Lesson:** Always add explicit `Unit` in `safeCall` blocks that don't call `.body()`.

### Bug 4: `WatchItem` Always Empty (Int vs Double)
```kotlin
// Fails when server sends 16.5
data class WatchItem(val amount: Int, val listingRate: Int)

// Fixed
data class WatchItem(val amount: Double = 0.0, val listingRate: Double = 0.0)
```
**Lesson:** Use `Double` for numeric fields unless you're certain they're always integers.

### Bug 5: Service Rate Wrong Request Body
```kotlin
// Wrong — server expects raw Double
setBody(mapOf("amount" to amount))

// Correct
setBody(amount)
```

### Bug 6: Stale Build Cache
The Kotlin incremental compiler looked for `LoginState.class` in the build cache but it didn't exist.
**Fix:** `./gradlew clean`

### Bug 7: Old Gradle Daemon Ignoring Wrapper Changes
After updating `gradle-wrapper.properties`, Android Studio kept using the old daemon.
**Fix:** `./gradlew --stop` + delete `.gradle/configuration-cache`

### Bug 8: Corrupted Kotlin Metadata
```
Accessing invalid virtual file: jar:///...kotlinTransformedMetadataLibraries/...klib!/
```
**Fix:** `rm -rf .kotlin/metadata` then Invalidate Caches in Android Studio.

### Bug 9: Profile Showing Loading Spinner
`GET api/wallet/status` returned 404. Correct endpoint: `GET api/digital-wallet`.

### Bug 10: Lock Trade Wrong Endpoint
`POST api/trading/{id}` just locks. `POST api/trading/lock` with `{"id":"..."}` body returns bank info.

---

## 16. Glossary

| Term | Meaning |
|------|---------|
| `val` / `var` | Immutable / mutable variable |
| `suspend` | Function that can pause without blocking the thread |
| `StateFlow` | Live data container that notifies collectors on change |
| `collectAsState()` | Converts StateFlow to Compose State |
| `remember` | Keeps a value alive across recompositions |
| `LaunchedEffect` | Runs a coroutine when a composable enters composition |
| `@Composable` | Marks a function as a UI component |
| `sealed class` | A class with a fixed set of subclasses |
| `data class` | A class whose purpose is to hold data |
| `expect` / `actual` | KMP mechanism for platform-specific implementations |
| ViewModel | Holds and manages UI data, survives configuration changes |
| Repository | Layer between ViewModel and data sources (network, DB) |
| Dependency Injection | Providing dependencies from outside rather than creating them internally |
| Coroutine | Lightweight thread that can be paused and resumed |
| `CoroutineScope` | Defines the lifetime of coroutines |
| DataStore | Key-value storage for Android, async, replaces SharedPreferences |
| Ktor | KMP HTTP client |
| `@Serializable` | Marks a class for JSON serialization/deserialization |
| `safeCall` | Wrapper that catches exceptions and returns `Result.Error` |
| `Result<T>` | Sealed class representing Success or Error from an API call |
| KMP | Kotlin Multiplatform — share code between Android and iOS |
| XCFramework | Compiled Kotlin framework linked into the iOS Xcode project |
| Scaffold | Compose layout with top bar, bottom bar, FAB slots |
| `HorizontalPager` | Swipeable pages composable |
| Logcat | Android log viewer — shows `println` and error output |
| AGP | Android Gradle Plugin — builds the Android app |
| Gradle | Build tool that compiles, links, and packages the app |

---

## Where to Learn More

| Resource | Topic |
|----------|-------|
| https://kotlinlang.org/docs/getting-started.html | Kotlin basics |
| https://play.kotlinlang.org/koans | Interactive Kotlin exercises |
| https://developer.android.com/courses/android-basics-compose/course | Android + Compose from scratch |
| https://developer.android.com/topic/libraries/architecture/viewmodel | ViewModel docs |
| https://developer.android.com/topic/architecture | Android architecture guide |
| https://ktor.io/docs/client-create-multiplatform-application.html | Ktor KMP client |
| https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-getting-started.html | KMP getting started |
| https://www.youtube.com/@PhilippLackner | Kotlin, Compose, architecture (YouTube) |
| https://www.youtube.com/@stevdza-san | KMP tutorials (YouTube) |

> **Best way to learn:** Open any file in this project in Android Studio, read it line by line, and search anything unfamiliar. You have a real production app — that's the best classroom.
