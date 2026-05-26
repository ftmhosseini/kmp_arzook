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

## Features & Functionality

### Authentication
| Feature | Description |
|---------|-------------|
| Login (email/password) | `POST api/auth/login` |
| Google Sign-In | `POST api/auth/social-login` with Google ID token |
| Register | `POST api/auth/register` with name, email, password |
| Logout | Clears token from DataStore |

### Profile Management
| Feature | Description |
|---------|-------------|
| View profile | `GET api/profile/me` |
| Update profile | `PUT api/profile` (phone, birthday, address, city, etc.) |
| Upload Photo ID | `POST api/profile/photo-id` (multipart) |
| Upload Utility Bill | `POST api/profile/utility-bill` (multipart) |

### Trading
| Feature | Description |
|---------|-------------|
| Browse trades | Public CAD/USD trade listings |
| Watch trade | `POST api/trading/watch/{id}` |
| Unwatch trade | `POST api/trading/unwatch/{id}` |
| Lock trade | `POST api/trading/lock` → returns LockedTrade with countdown |
| Confirm buy/sell | `POST api/trading/buy` or `api/trading/sell` |
| Service rate | `POST api/trading/buying-taker-service-rate` / `selling-taker-service-rate` |

### My Trades (Selling)
| Feature | Endpoint | Notes |
|---------|----------|-------|
| Create selling | `POST api/sellings` | With amount, rate, currency, urgent |
| Edit amount | `PUT api/sellings/update-amount/{id}` | |
| Edit rate | `PUT api/sellings/update-asking-rate/{id}` | |
| Edit payee | `PUT api/sellings/update-payee-bank-info` | |
| Toggle Post | `PUT api/sellings/advertised` | Full TradeItem body with `advertised` toggled |
| Toggle Urgent | `PUT api/sellings/metadata/{id}` | Body: `{purposeOfTransaction, sourceOfFund, urgent}` |
| Delete | `DELETE api/sellings/{id}` | Only if unposted AND not deposited |

### My Trades (Buying)
| Feature | Endpoint | Notes |
|---------|----------|-------|
| Create buying | `POST api/buyings` | With amount, rate, currency, smartMatching |
| Edit amount | `PUT api/buyings/update-amount/{id}` | |
| Edit rate | `PUT api/buyings/update-asking-rate/{id}` | |
| Toggle Post | `PUT api/buyings/advertised` | Full TradeItem body with `advertised` toggled |
| Toggle Smart Matching | `PUT api/buyings/metadata/{id}` | Body: `{purposeOfTransaction, sourceOfFund, smartMatchingEnabled}` |
| Delete | `DELETE api/buyings/{id}` | Only if unposted |

### Delete Rules
| Condition | Behavior |
|-----------|----------|
| `advertised == true` | Alert: "Please make it unposted first, then delete the trade." |
| `deposited == true` (selling only) | Alert: "Please cancel the e-Transfer from your banking account." |
| `advertised == false && deposited == false` | Confirm dialog → delete |

### Toggle Confirmation Alerts
| Toggle | Direction | Alert Message |
|--------|-----------|---------------|
| Post (selling) | OFF | "Are you sure you want to make your selling invisible to other customers?" |
| Post (buying) | OFF | "Are you sure you want to make your buying invisible to other customers?" |
| Urgent (selling) | ON | "Are you sure you want to make your selling URGENT? Only buyers with sufficient funds..." |
| Smart Matching (buying) | OFF | "Are you sure you want to disable Smart Matching? Smart Matching is a great feature..." |

### E-Wallet
| Feature | Endpoint |
|---------|----------|
| Balance | `GET api/digital-wallet` |
| Deposit history | `GET api/digital-wallet/items` |

### Rate Alerts
| Feature | Endpoint |
|---------|----------|
| Get alerts | `GET api/rate-alerts` |
| Save alerts | `POST api/rate-alerts` |

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
| `serviceRates` | `StateFlow<Map<String,Double>>` | Per-trade service rates |

Key functions:
- `watchTrade()`, `unwatchTrade()`
- `lockTrade()`, `unlockTrade()`, `confirmTrade()`
- `updateSellingDraft()`, `updateBuyingDraft()`
- `updateSellingAdvertised()`, `updateSellingUrgent()`
- `updateBuyingAdvertised()`, `updateBuyingSmartMatching()`
- `deleteBuyingDraft()`, `deleteSellingDraft()`
- `loadServiceRate()`, `loadUserData()`
- `connectWebSocket()`, `disconnectWebSocket()`

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
| `api/trading/watch/{id}` | POST | Yes | Watch trade |
| `api/trading/unwatch/{id}` | POST | Yes | Unwatch trade |
| `api/trading/lock` | POST | Yes | Lock + get bank info |
| `api/trading/buying-taker-service-rate` | POST | Yes | Service rate (buying) |
| `api/trading/selling-taker-service-rate` | POST | Yes | Service rate (selling) |
| `api/buyings/buying-drafts` | GET | Yes | My buying drafts |
| `api/buyings` | POST | Yes | Create buying draft |
| `api/buyings/update-amount/{id}` | PUT | Yes | Edit buying amount |
| `api/buyings/update-asking-rate/{id}` | PUT | Yes | Edit buying rate |
| `api/buyings/advertised` | PUT | Yes | Toggle buying post |
| `api/buyings/metadata/{id}` | PUT | Yes | Toggle smart matching |
| `api/buyings/{id}` | DELETE | Yes | Delete buying draft |
| `api/sellings` | GET | Yes | My selling drafts |
| `api/sellings` | POST | Yes | Create selling draft |
| `api/sellings/update-amount/{id}` | PUT | Yes | Edit selling amount |
| `api/sellings/update-asking-rate/{id}` | PUT | Yes | Edit selling rate |
| `api/sellings/update-payee-bank-info` | PUT | Yes | Edit selling payee |
| `api/sellings/advertised` | PUT | Yes | Toggle selling post |
| `api/sellings/metadata/{id}` | PUT | Yes | Toggle urgent |
| `api/sellings/{id}` | DELETE | Yes | Delete selling draft |
| `api/trades/buying-trades` | GET | Yes | Completed buying |
| `api/trades/selling-trades` | GET | Yes | Completed selling |
| `api/payees` | GET | Yes | User payees |

---

## Trade Card Color Logic

| Color | Condition |
|-------|-----------|
| Dark Green | `status != null` (completed) |
| Green | `deposited == true` (ready to trade) |
| Cream | `deposited == null` (open listing) |

---

## Confirm Dialog Info

### Buying (locking a selling trade)
- YOU SEND: IRR amount (`exchangeRate × amount`)
- YOU GET: CAD/USD amount
- Arzook Recipient, Sheba, e-Transfer to, Deposit Id, e-Transfer password
- Exchange rate, Service Rate, Compliance Fee

### Selling (locking a buying trade)
- YOU SEND: CAD/USD amount
- YOU GET: IRR amount (`askingRate × amount`)
- e-Transfer to (user's email), e-Transfer password: N/A
- Exchange rate, Service Rate, Total Amount, Compliance Fee, Net Payout

### Rate Calculation
- Selling: `askingRate = exchangeRate - serviceRate`
- Buying: `askingRate = exchangeRate + serviceRate`
- Compliance fee comes from the lock response, not the trade

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
| Lock trade returns error | Use `POST api/trading/lock` with raw string ID body |
| Service rate not showing | Body must be raw `Double`, not `{"amount": ...}` |
| Profile loading forever | Endpoint is `api/digital-wallet`, not `api/wallet/status` |
| City/address crash on deserialization | Use `JsonElement?` for fields that can be string or `{}` |
| Toggle not smooth | Use `derivedStateOf` for filtered lists; avoid `Modifier.scale()` on Switch |
| Serialization of Map<String,Any> fails | Use `TextContent(jsonString, ContentType.Application.Json)` for raw JSON |

---

## Running Tests

```bash
./gradlew :shared:allTests
```

Tests cover: formatting, serialization, auth models, trade items, locked trades, watch items, payees, rate alerts, wallet status, promo codes, toggle business logic, service rate calculations, and delete rules.

---

## Environment

Base URL is hardcoded in `ArzookRepositoryImpl`:
```
https://api.arzook.ca
wss://api.arzook.ca  (WebSocket)
```

No `.env` file needed.
