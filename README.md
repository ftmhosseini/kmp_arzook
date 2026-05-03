# Arzook KMP

Kotlin Multiplatform mobile app (Android & iOS) for the [Arzook](https://arzook.ca) peer-to-peer CAD/USD ↔ IRR currency exchange platform. Shared business logic, networking, and UI between Android and iOS using Kotlin Multiplatform + Compose Multiplatform.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2.20 |
| Shared UI | Compose Multiplatform 1.6.11 |
| Android UI | Jetpack Compose 1.8.2 |
| Networking | Ktor 2.3.12 |
| Serialization | kotlinx.serialization 1.6.3 |
| Async | kotlinx.coroutines 1.8.1 |
| Navigation | Custom sealed class `Screen` (no Jetpack Nav) |
| Token storage | DataStore Preferences 1.2.1 |
| Video | android-youtube-player 12.1.1 |
| Auth | Google Sign-In 21.5.1 |
| Build | AGP 8.8.1 · Gradle 8.10.2 |
| Min SDK | 24 (Android 7.0) |
| Compile SDK | 35 |

---

## Prerequisites

### Both platforms
- Android Studio Meerkat (supports AGP 8.8.1)
- JDK 17+ (`java -version` to check)

### iOS only (macOS required)
- Xcode 15+
- macOS 13+

---

## First-time Setup

```bash
git clone <repo-url>
cd KMP
```

Ensure `local.properties` exists with your SDK path:
```
sdk.dir=/Users/<you>/Library/Android/sdk
```

---

## Running Android

```bash
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Or open in Android Studio → select `androidApp` → press ▶

---

## Running iOS

```bash
./gradlew :shared:assembleXCFramework
open iosApp/iosApp.xcodeproj
```

Link `shared/build/XCFrameworks/release/shared.xcframework` in Xcode under **General → Frameworks** (Do Not Embed), then press ⌘R.

> Re-run `assembleXCFramework` every time you change shared Kotlin code.

---

## Project Structure

```
KMP/
├── shared/src/
│   ├── commonMain/kotlin/ca/arzook/shared/
│   │   ├── model/          ← Data classes (Auth, Trade, Visitor…)
│   │   ├── repository/     ← ArzookRepository interface + Impl
│   │   ├── network/        ← HttpClient (expect/actual)
│   │   ├── websocket/      ← WebSocketManager
│   │   └── ui/             ← All screens + ViewModels (shared)
│   ├── androidMain/        ← Android-specific: Ktor engine, FilePicker, YoutubePlayer
│   └── iosMain/            ← iOS-specific: Ktor Darwin, FilePicker, YoutubePlayer
│
├── androidApp/             ← Android entry point
│   └── MainActivity.kt     ← DataStore setup, AuthViewModel wiring
│
└── iosApp/                 ← iOS entry point (SwiftUI)
    └── ContentView.swift
```

---

## Architecture

```
MainActivity (Android) / iOSApp.swift (iOS)
        ↓
    ArzookApp          ← Root composable, owns screen state (sealed class Screen)
        ↓
  AuthViewModel        ← Login, logout, token, user profile (DataStore)
  HomeViewModel        ← Trades, watch list, drafts, wallet, WebSocket
        ↓
  ArzookRepositoryImpl ← All API calls via Ktor
        ↓
  api.arzook.ca        ← REST + WebSocket backend
```

**No Dependency Injection framework is used.** ViewModels are instantiated manually with `remember { HomeViewModel() }` and dependencies are passed via constructor.

---

## Screen Map

| Screen | Class | Auth Required |
|--------|-------|--------------|
| Splash | `SplashScreen` | No |
| Home (banner, chart, videos) | `HomeScreen` | No |
| Buying trades | `TradesScreen(isSelling=false)` | No (watch/buy requires auth) |
| Selling trades | `TradesScreen(isSelling=true)` | No (watch/sell requires auth) |
| Login | `LoginScreen` | — |
| Sign Up | `SignUpScreen` | — |
| My Buying | `MyBuyingScreen` | Yes |
| My Selling | `MySellingScreen` | Yes |
| Add Buying/Selling | `AddSellingBuyingScreen` | Yes |
| Profile | `ProfileScreen` | Yes |
| E-Wallet | `EWalletScreen` | Yes |
| Rate Alert | `RateAlertScreen` | Yes |
| Content (FAQ, About…) | `ContentScreen` | No |

Navigation is managed by a `var screen by remember { mutableStateOf<Screen>(Screen.Splash) }` in `ArzookApp.kt`. No Jetpack Navigation library.

---

## ViewModels

### AuthViewModel (`shared/ui/AuthViewModel.kt`)
Manages authentication state. Constructed in `MainActivity` with DataStore lambdas injected via constructor (manual DI).

| State | Type | Description |
|-------|------|-------------|
| `token` | `StateFlow<String?>` | JWT token, null = logged out |
| `loginState` | `StateFlow<LoginState>` | Idle / Loading / Success / Error |
| `userDetails` | `StateFlow<AuthenticatedData?>` | Cached user profile |

Key functions: `login()`, `logout()`, `register()`, `googleSignIn()`, `receiveToken()`, `updateProfile()`, `loadUserDetails()`

### HomeViewModel (`shared/ui/HomeViewModel.kt`)
Manages all app data. Created once in `ArzookApp` with `remember { HomeViewModel() }`.

| State | Type | Description |
|-------|------|-------------|
| `cadTrades` | `StateFlow<List<TradeItem>>` | CAD trade listings |
| `usdTrades` | `StateFlow<List<TradeItem>>` | USD trade listings |
| `watchList` | `StateFlow<List<WatchItem>>` | User's watched trades |
| `buyingDrafts` | `StateFlow<List<TradeItem>>` | User's buying drafts |
| `sellingDrafts` | `StateFlow<List<TradeItem>>` | User's selling drafts |
| `walletStatus` | `StateFlow<WalletStatus?>` | Balance + hold credit |
| `currentRate` | `StateFlow<CurrentRate?>` | Exchange rate + offsets |
| `lockedTrades` | `StateFlow<Map<String,LockedTrade>>` | Locked trade bank info |

---

## Key API Endpoints

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `api/main?min=0&max=2&displayDepositedOnly=false` | GET | No | CAD trades |
| `api/main?...&selectedCurrencyCode=USD` | GET | No | USD trades |
| `api/main/daily-stats` | GET | No | Chart data |
| `api/main/rate-stats?selectedCurrencyCode=undefined` | GET | Yes | Current rate |
| `api/auth/login` | POST | No | Login |
| `api/auth/register` | POST | No | Register |
| `api/auth/social-login` | POST | No | Google sign-in |
| `api/profile/me` | GET | Yes | User profile |
| `api/digital-wallet` | GET | Yes | Wallet balance |
| `api/digital-wallet/items` | GET | Yes | Deposit history |
| `api/trading/watch` | GET | Yes | Watch list |
| `api/trading/watch/{id}` | POST | Yes | Watch trade |
| `api/trading/unwatch/{id}` | POST | Yes | Unwatch trade |
| `api/trading/lock` | POST | Yes | Lock + get bank info |
| `api/trading/buying-taker-service-rate` | POST | Yes | Service rate (buying) |
| `api/trading/selling-taker-service-rate` | POST | Yes | Service rate (selling) |
| `api/buyings/buying-drafts` | GET | Yes | My buying drafts |
| `api/buyings` | POST | Yes | Create buying draft |
| `api/sellings` | GET | Yes | My selling drafts |
| `api/sellings` | POST | Yes | Create selling draft |
| `api/trades/buying-trades` | GET | Yes | Completed buying |
| `api/trades/selling-trades` | GET | Yes | Completed selling |
| `api/payees` | GET | Yes | User payees |

---

## Adding a New Screen

1. Add to `Screen` sealed class in `ArzookApp.kt`:
```kotlin
data object Notifications : Screen()
```

2. Add a `when` branch in `ArzookApp.kt`:
```kotlin
is Screen.Notifications -> NotificationsScreen(onBack = { screen = Screen.Home })
```

3. Create `NotificationsScreen.kt` in `shared/ui/`.

---

## Adding a New API Endpoint

1. `ArzookRepository.kt` — add to interface:
```kotlin
suspend fun getNotifications(token: String): Result<List<Notification>>
```

2. `model/` — add data class:
```kotlin
@Serializable
data class Notification(val id: String, val message: String)
```

3. `ArzookRepositoryImpl.kt` — implement:
```kotlin
override suspend fun getNotifications(token: String): Result<List<Notification>> = safeCall {
    client.get("$baseUrl/api/notifications") {
        header(HttpHeaders.Authorization, token.bearer())
    }.body()
}
```

4. `HomeViewModel.kt` — add StateFlow + call:
```kotlin
private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

// in loadUserData():
scope.launch {
    when (val r = repo.getNotifications(token)) {
        is Result.Success -> _notifications.value = r.data
        is Result.Error -> println("[HomeVM] notifications error: ${r.message}")
    }
}
```

---

## Trade Card Color Logic

| Color | Condition |
|-------|-----------|
| Dark Green | `status != null` (completed) |
| Green | `deposited == true` (ready to trade) |
| Cream | `deposited == null` (open listing) |

---

## Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| `Permission denied` on `gradlew` | `chmod +x gradlew` |
| Gradle sync fails | Check `local.properties` has correct `sdk.dir` |
| `Minimum supported Gradle version` error | Update `gradle-wrapper.properties` distributionUrl |
| AGP incompatible with Android Studio | Match AGP version to your Android Studio version |
| Stale build cache / `LoginState.class` not found | `./gradlew clean` |
| Corrupted Kotlin metadata | `rm -rf .kotlin/metadata` then Invalidate Caches |
| Old Gradle daemon still running | `./gradlew --stop` + delete `.gradle/configuration-cache` |
| Watch list always empty | `WatchItem.amount` must be `Double`, not `Int` |
| `safeCall` silently fails for Unit returns | Add explicit `Unit` at end of block |
| Lock trade returns error | Use `POST api/trading/lock` with `{"id":"..."}` body |
| Service rate not showing | Body must be raw `Double`, not `{"amount": ...}` |
| Profile loading forever | Endpoint is `api/digital-wallet`, not `api/wallet/status` |
| City/address crash on deserialization | Use `JsonElement?` for fields that can be string or `{}` |

---

## Environment

Base URL is hardcoded in `ArzookRepositoryImpl`:
```
https://api.arzook.ca
wss://api.arzook.ca  (WebSocket)
```

No `.env` file needed.
