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
│   ├── commonTest/         ← Unit tests (ArzookTests.kt, RepositoryTest.kt)
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
| Feature | Endpoint | Notes |
|---------|----------|-------|
| Login (email/password) | `POST api/auth/login` | Returns JWT token |
| Google Sign-In | `POST api/auth/social-login` | Send Google ID token |
| Biometric Login | — | Fingerprint/Face ID, uses stored token locally |
| Register | `POST api/auth/register` | firstName, lastName, email, password, inviterEmail |
| Logout | — | Clears token from DataStore locally |

### Profile Management
| Feature | Endpoint | Notes |
|---------|----------|-------|
| View profile | `GET api/profile/me` | Returns AuthenticatedData |
| Update profile | `PUT api/profile` | phone, birthday, address, city, postalCode, occupation |
| Upload Photo ID | `POST api/profile/photo-id` | Multipart file upload |
| Upload Utility Bill | `POST api/profile/utility-bill` | Multipart file upload |

### Trading (Browse & Execute)
| Feature | Endpoint | Notes |
|---------|----------|-------|
| Browse CAD trades | `GET api/main?min=0&max=2&displayDepositedOnly=false` | Public, no auth |
| Browse USD trades | `GET api/main?...&selectedCurrencyCode=USD` | Public, no auth |
| Watch trade | `POST api/trading/watch/{id}` | Adds to watch list |
| Unwatch trade | `POST api/trading/unwatch/{id}` | Removes from watch list |
| Lock trade | `POST api/trading/lock` | Body: raw trade ID string. Returns LockedTrade with 30s countdown |
| Unlock trade | `POST api/trading/unlock` | Body: raw trade ID string. Called on cancel/timeout |
| Confirm buy | `POST api/trading/buy` | After lock, within countdown |
| Confirm sell | `POST api/trading/sell` | After lock, within countdown |
| Service rate (buying) | `POST api/trading/buying-taker-service-rate` | Body: raw Double amount |
| Service rate (selling) | `POST api/trading/selling-taker-service-rate` | Body: raw Double amount |

#### Lock Flow
1. User taps Buy/Sell → app calls `POST api/trading/lock` with trade ID
2. Server returns `LockedTrade` with `autoLockExpiresIn: 30` (seconds)
3. App shows confirm dialog with countdown
4. If user confirms → call buy/sell endpoint
5. If user cancels → call unlock endpoint
6. If countdown expires → auto-unlock
7. If another user already locked (HTTP 409) → show "This trade is currently locked by another user"

### My Trades (Selling)
| Feature | Endpoint | Request Body |
|---------|----------|-------------|
| Create selling | `POST api/sellings` | Full TradeItem: amount, askingRate, currency, purposeOfTransaction, sourceOfFund, urgent |
| Edit amount | `PUT api/sellings/update-amount/{id}` | `{"amount": "500.0"}` |
| Edit rate | `PUT api/sellings/update-asking-rate/{id}` | `{"rate": "1400000"}` |
| Edit payee | `PUT api/sellings/update-payee-bank-info` | `{"id": "...", "sheba": "...", "payeeName": "..."}` |
| Toggle Post | `PUT api/sellings/advertised` | **Full TradeItem body** with `advertised` field toggled |
| Toggle Urgent | `PUT api/sellings/metadata/{id}` | `{"purposeOfTransaction":"travel","sourceOfFund":"salary","urgent":true}` |
| Delete | `DELETE api/sellings/{id}` | Only if unposted AND not deposited |

### My Trades (Buying)
| Feature | Endpoint | Request Body |
|---------|----------|-------------|
| Create buying | `POST api/buyings` | Full TradeItem: amount, askingRate, currency, purposeOfTransaction, sourceOfFund, smartMatchingEnabled |
| Edit amount | `PUT api/buyings/update-amount/{id}` | `{"amount": "200.0"}` |
| Edit rate | `PUT api/buyings/update-asking-rate/{id}` | `{"rate": "1200000"}` |
| Toggle Post | `PUT api/buyings/advertised` | **Full TradeItem body** with `advertised` field toggled |
| Toggle Smart Matching | `PUT api/buyings/metadata/{id}` | `{"purposeOfTransaction":"travel","sourceOfFund":"salary","smartMatchingEnabled":false}` |
| Delete | `DELETE api/buyings/{id}` | Only if unposted |

### Toggle API Details

**Selling Post** (`PUT api/sellings/advertised`):
- Sends the **entire TradeItem** as JSON body with `advertised` set to new value
- Serialized with `explicitNulls = true` (null fields included)
- Sent as `TextContent(jsonString, ContentType.Application.Json)` to bypass Ktor's content negotiation

**Selling Urgent** (`PUT api/sellings/metadata/{id}`):
- Sends raw JSON: `{"purposeOfTransaction":"...","sourceOfFund":"...","urgent":true/false}`
- Sent as `TextContent(jsonString, ContentType.Application.Json)`

**Buying Post** (`PUT api/buyings/advertised`):
- Same pattern as selling post — full TradeItem body with `explicitNulls = true`

**Buying Smart Matching** (`PUT api/buyings/metadata/{id}`):
- Sends raw JSON: `{"purposeOfTransaction":"...","sourceOfFund":"...","smartMatchingEnabled":true/false}`

### Delete Rules
| Condition | Behavior |
|-----------|----------|
| `advertised == true` | Alert: "Please make it unposted first, then delete the trade." |
| `deposited == true` (selling only) | Alert: "Please cancel the e-Transfer from your banking account." |
| `advertised == false && deposited == false` | Confirm dialog → delete |

### Toggle Confirmation Alerts
| Toggle | Direction | Alert Message |
|--------|-----------|---------------|
| Post (selling) | OFF | "Are you sure you want to make your selling invisible to other customers? By turning off the 'Post' slider, others will no longer see your selling." |
| Post (buying) | OFF | "Are you sure you want to make your buying invisible to other customers? By turning off the 'Post' slider, others will no longer see your buying." |
| Urgent (selling) | ON | "Are you sure you want to make your selling URGENT? Only buyers with sufficient funds in their e-Wallet can lock urgent Sellings." |
| Smart Matching (buying) | OFF | "Are you sure you want to disable Smart Matching? Smart Matching is a great feature of buy all competitive Sellings automatically." |

Alert buttons: **Yes** (green background) / **No** (brown background)

### E-Wallet
| Feature | Endpoint |
|---------|----------|
| Balance | `GET api/digital-wallet` |
| Deposit history | `GET api/digital-wallet/items` |

### Rate Alerts
| Feature | Endpoint |
|---------|----------|
| Get alerts | `GET api/rate-alerts` |
| Save/Update alerts | `POST api/rate-alerts` |

### Payees
| Feature | Endpoint |
|---------|----------|
| Get payees | `GET api/payees` |
| Add payee | `POST api/payees` |

---

## Screen Map

| Screen | Class | Auth Required |
|--------|-------|--------------|
| Splash | `SplashScreen` | No |
| Home (banner, chart, videos) | `HomeScreen` | No |
| Buying trades | `TradesScreen(isSelling=false)` | No (watch/buy requires auth) |
| Selling trades | `TradesScreen(isSelling=true)` | No (watch/sell requires auth) |
| Offering (currency pair) | `TradesScreen` with currency pair | No (actions require auth) |
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
| `biometricEnabled` | `StateFlow<Boolean>` | Whether biometric login is enabled |

Key functions: `login()`, `logout()`, `register()`, `googleSignIn()`, `receiveToken()`, `updateProfile()`, `loadUserDetails()`, `biometricLogin()`, `setBiometricEnabled()`

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
| `tradeError` | `StateFlow<String?>` | Error message for snackbar |

Key functions:
- `watchTrade()`, `unwatchTrade()`
- `lockTrade()`, `unlockTrade()`, `confirmTrade()`
- `updateSellingDraft()`, `updateBuyingDraft()`
- `updateSellingAdvertised(token, draft)` — sends full TradeItem
- `updateSellingUrgent(token, id, purposeOfTransaction, sourceOfFund, urgent)`
- `updateBuyingAdvertised(token, draft)` — sends full TradeItem
- `updateBuyingSmartMatching(token, id, purposeOfTransaction, sourceOfFund, enabled)`
- `deleteBuyingDraft()`, `deleteSellingDraft()`
- `loadServiceRate()`, `loadUserData()`
- `connectWebSocket()`, `disconnectWebSocket()`
- `refreshTrades()`, `refreshCadTrades()`, `refreshUsdTrades()`

---

## Confirm Dialog Info

### Buying (locking a selling trade, `trade.selling == true`)
```
YOU SEND: {exchangeRate × amount} IRR
YOU GET: ${amount} CAD/USD
Arzook Recipient: {lock.arzookBankInfoName}
Sheba: {lock.arzookBankInfoSheba}
e-Transfer to: {lock.arzookDepositEmail}
Deposit Id: {trade.customerDepositId}
e-Transfer password: {lock.password} or "N/A"
─────────────────────────────
1 CAD = {exchangeRate} IRR
Service Rate: {serviceRate} IRR per CAD
Compliance Fee: {lock.complianceFee} IRR
```

### Selling (locking a buying trade, `trade.selling == false`)
```
YOU SEND: ${amount} CAD/USD
e-Transfer to: {user.email}
e-Transfer password: N/A
YOU GET: {askingRate × amount} IRR
Your Sheba: {trade.sheba} or "TBD"
─────────────────────────────
1 CAD = {exchangeRate} IRR
Service Rate: {serviceRate} IRR per CAD
Total Amount: {exchangeRate × amount} IRR
Compliance Fee: {lock.complianceFee} IRR
Net Payout: {askingRate × amount - complianceFee} IRR
```

### Rate Calculation
- **Selling askingRate** = `exchangeRate - serviceRate`
- **Buying askingRate** = `exchangeRate + serviceRate`
- Compliance fee comes from the **lock response**, not the trade
- Suggested rate (AddTradeScreen, CAD only):
  - Selling: `currentMaxBuyingExchangeRate - userSellingRateOffset`
  - Buying: `currentMinSellingExchangeRate - userBuyingRateOffset`

---

## My Selling Screen Details

### Expanded Draft View
- Code, Amount (editable), Asking Rate (editable), Total, Exchange Rate
- Payee selector (shows `draft.payeeName` if already set, or "Select payee")
- **YOU SEND / YOU GET** tabs (clickable, highlighted with background when selected):
  - YOU SEND: Amount, Arzook Deposit Email, e-Transfer password, e-Transfer message (code)
  - YOU GET: IRR total, Sheba, Recipient
  - Each field has a **copy icon** (clipboard)
- Service Rate, Compliance Fee (with info icon)
- Post toggle, Urgent toggle
- Update button, Delete button

### Carousel (Deposited Trades)
- Auto-scrolls every 3 seconds
- **Pauses** when Buy/Sell dialog is open
- Resumes after confirm/cancel/timeout
- Shows same confirm dialog as regular trade cards

---

## Trade Card Color Logic

| Color | Condition |
|-------|-----------|
| Dark Green | `status != null` (completed) |
| Green | `deposited == true` (ready to trade) |
| Cream | `deposited == null` (open listing) |

---

## Error Handling

| HTTP Code | Context | User Message |
|-----------|---------|-------------|
| 409 | Lock trade | "This trade is currently locked by another user. Please try again shortly." |
| Other errors | Any API call | Raw error shown in snackbar via `tradeError` StateFlow |

---

## Serialization Notes

- Ktor client configured with `explicitNulls = false` globally (nulls omitted from JSON)
- **Exception**: `updateSellingAdvertised` and `updateBuyingAdvertised` use `explicitNulls = true` because the API expects the full body with null fields
- `Map<String, Any>` does NOT serialize with kotlinx.serialization — always use `TextContent(rawJsonString, ContentType.Application.Json)` for raw JSON bodies
- Lock/Unlock endpoints use `ContentType.Text.Plain` with raw string ID body
- Service rate endpoint uses raw `Double` body

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
| Lock trade returns error | Use `POST api/trading/lock` with raw string ID body (Text.Plain) |
| Unlock not working | Must use `ContentType.Text.Plain` + raw ID (not JSON map) |
| Service rate not showing | Body must be raw `Double`, not `{"amount": ...}` |
| Profile loading forever | Endpoint is `api/digital-wallet`, not `api/wallet/status` |
| City/address crash on deserialization | Use `JsonElement?` for fields that can be string or `{}` |
| Toggle not smooth | Use `derivedStateOf` for filtered lists; use `Modifier.height()` not `Modifier.scale()` on Switch |
| Serialization of Map<String,Any> fails | Use `TextContent(jsonString, ContentType.Application.Json)` for raw JSON |
| Toggle API not working | Must send raw JSON via TextContent, not `setBody(mapOf(...))` |
| 409 on lock | Another user locked the trade; show friendly message |

---

## Running Tests

```bash
./gradlew :shared:testDebugUnitTest
```

### Test Files

**`ArzookTests.kt`** — Unit tests for models and business logic:
- Format utilities (formatIrr, formatCad)
- Result type (Success, Error)
- Login/Register serialization
- Google Sign-In
- Biometric login (preference toggle, state management)
- Update profile
- Decode user from DataStore
- TradeItem serialization + toggle fields + delete rules
- LockedTrade serialization (password, sellingCode, complianceFee)
- WatchItem serialization
- Payee serialization
- Rate alerts
- Current rate + suggested rate calculation
- Wallet status + balance checks
- Promo codes
- Auth state
- Update request models
- Toggle business logic (copy with toggled fields)
- Service rate calculations (selling: exchange - service, buying: exchange + service)
- Net payout calculation

**`RepositoryTest.kt`** — Integration tests with FakeArzookRepository:
- Login (success, failure)
- Google Sign-In (success, failure)
- Biometric login (enable, disable, login with stored token)
- Register (with/without inviter)
- Get/Update profile
- Upload documents (photo ID, utility bill)
- Rate alerts (get, set, update, remove)
- Create selling/buying drafts
- Update amount/rate/payee
- Add payee
- Toggle post (selling/buying, on/off)
- Toggle urgent (on/off)
- Toggle smart matching (on/off)
- Delete trades + business rules
- Watch/Unwatch
- Lock (success, 409 conflict)
- Unlock
- Confirm buy/sell
- Service rates (buying, selling, with promo)
- Wallet status + deposits
- Current rate
- Completed trades
- Print trade receipts

---

## Environment

Base URL is hardcoded in `ArzookRepositoryImpl`:
```
https://api.arzook.ca
wss://api.arzook.ca  (WebSocket)
```

No `.env` file needed.
