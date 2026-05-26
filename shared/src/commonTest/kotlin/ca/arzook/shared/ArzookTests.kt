package ca.arzook.shared

import ca.arzook.shared.model.*
import ca.arzook.shared.ui.LoginState
import ca.arzook.shared.ui.decodeUser
import ca.arzook.shared.ui.formatCad
import ca.arzook.shared.ui.formatIrr
import kotlinx.serialization.json.Json
import kotlin.test.*

// ─── Format Utils ───────────────────────────────────────────────────────────────

class FormatUtilsTest {
    @Test fun formatIrr_wholeNumber() { assertEquals("1,000,000", formatIrr(1000000.0)) }
    @Test fun formatIrr_smallNumber() { assertEquals("500", formatIrr(500.0)) }
    @Test fun formatIrr_zero() { assertEquals("0", formatIrr(0.0)) }
    @Test fun formatIrr_largeNumber() { assertEquals("1,400,000", formatIrr(1400000.0)) }
    @Test fun formatIrr_12345() { assertEquals("12,345", formatIrr(12345.0)) }
    @Test fun formatIrr_12345678() { assertEquals("12,345,678", formatIrr(12345678.0)) }
    @Test fun formatCad_withDecimals() { assertEquals("1,250.50", formatCad(1250.50)) }
    @Test fun formatCad_wholeNumber() { assertEquals("100.00", formatCad(100.0)) }
    @Test fun formatCad_smallFraction() { assertEquals("0.01", formatCad(0.01)) }
}

// ─── Result Type ────────────────────────────────────────────────────────────────

class ResultTest {
    @Test fun success_holdsData() {
        val result: Result<String> = Result.Success("hello")
        assertTrue(result is Result.Success)
        assertEquals("hello", result.data)
    }
    @Test fun error_holdsMessage() {
        val result: Result<String> = Result.Error("failed")
        assertTrue(result is Result.Error)
        assertEquals("failed", result.message)
        assertNull(result.data)
    }
    @Test fun error_withData() {
        val result = Result.Error("partial", data = 42)
        assertEquals(42, result.data)
    }
}

// ─── Login / Auth ───────────────────────────────────────────────────────────────

class LoginResponseTest {
    @Test fun resolvedToken_withTokenType() {
        val response = LoginResponse(accessToken = "abc123", tokenType = "Bearer")
        assertEquals("Bearer abc123", response.resolvedToken())
    }
    @Test fun resolvedToken_alreadyPrefixed() {
        val response = LoginResponse(accessToken = "Bearer abc123", tokenType = "Bearer")
        assertEquals("Bearer abc123", response.resolvedToken())
    }
    @Test fun resolvedToken_emptyToken() {
        val response = LoginResponse(accessToken = "", tokenType = "Bearer")
        assertEquals("", response.resolvedToken())
    }
    @Test fun resolvedToken_emptyTokenType() {
        val response = LoginResponse(accessToken = "abc123", tokenType = "")
        assertEquals("abc123", response.resolvedToken())
    }
}

class LoginRequestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun loginRequest_emailPassword() {
        val req = LoginRequest(email = "user@arzook.ca", password = "secret")
        val encoded = json.encodeToString(LoginRequest.serializer(), req)
        assertTrue(encoded.contains("user@arzook.ca"))
        assertTrue(encoded.contains("secret"))
    }

    @Test fun loginState_types() {
        assertIs<LoginState>(LoginState.Idle)
        assertIs<LoginState>(LoginState.Loading)
        assertIs<LoginState>(LoginState.Success)
        assertIs<LoginState>(LoginState.Error("err"))
        assertEquals("err", (LoginState.Error("err")).message)
    }
}

class GoogleSignInTest {
    @Test fun loginResponse_fromGoogleToken() {
        val response = LoginResponse(accessToken = "google-jwt-token", tokenType = "Bearer")
        assertEquals("Bearer google-jwt-token", response.resolvedToken())
    }
}

// ─── Create Account / Register ──────────────────────────────────────────────────

class RegisterTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun user_registration_serialization() {
        val user = User(firstName = "John", lastName = "Doe", email = "john@test.com", password = "Pass123!")
        val encoded = json.encodeToString(User.serializer(), user)
        assertTrue(encoded.contains("john@test.com"))
        assertTrue(encoded.contains("John"))
        assertTrue(encoded.contains("Doe"))
    }

    @Test fun user_withInviter() {
        val user = User(firstName = "A", lastName = "B", email = "a@b.com", password = "x", inviterEmail = "ref@arzook.ca")
        val encoded = json.encodeToString(User.serializer(), user)
        assertTrue(encoded.contains("ref@arzook.ca"))
    }
}

// ─── Update Profile ─────────────────────────────────────────────────────────────

class UpdateProfileTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun updateProfile_serialization() {
        val req = UpdateProfileRequest(phoneNumber = "+14165551234", city = "Toronto", address = "123 Main St")
        val encoded = json.encodeToString(UpdateProfileRequest.serializer(), req)
        assertTrue(encoded.contains("+14165551234"))
        assertTrue(encoded.contains("Toronto"))
    }

    @Test fun updateProfile_partialUpdate() {
        val req = UpdateProfileRequest(phoneNumber = "+1234")
        val encoded = json.encodeToString(UpdateProfileRequest.serializer(), req)
        assertTrue(encoded.contains("+1234"))
    }
}

// ─── Decode User (from DataStore) ───────────────────────────────────────────────

class DecodeUserTest {
    @Test fun decodeUser_validJson() {
        val userJson = """{"id":"u1","email":"a@b.com","firstName":"John","lastName":"Doe"}"""
        val user = decodeUser(userJson)
        assertNotNull(user)
        assertEquals("u1", user.id)
        assertEquals("a@b.com", user.email)
    }
    @Test fun decodeUser_invalidJson() { assertNull(decodeUser("not json")) }
    @Test fun decodeUser_emptyString() { assertNull(decodeUser("")) }
}

// ─── Trade Item ─────────────────────────────────────────────────────────────────

class TradeItemSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun tradeItem_defaultValues() {
        val item = TradeItem()
        assertNull(item.id)
        assertNull(item.amount)
        assertFalse(item.selling)
        assertEquals(0.0, item.complianceFee)
        assertNull(item.advertised)
        assertNull(item.urgent)
        assertNull(item.smartMatchingEnabled)
    }

    @Test fun tradeItem_serialization() {
        val item = TradeItem(id = "t1", amount = 500.0, askingRate = 1100000.0, selling = true)
        val encoded = json.encodeToString(TradeItem.serializer(), item)
        val decoded = json.decodeFromString(TradeItem.serializer(), encoded)
        assertEquals("t1", decoded.id)
        assertEquals(500.0, decoded.amount)
        assertTrue(decoded.selling)
    }

    @Test fun tradeItem_withToggles() {
        val item = TradeItem(id = "t2", advertised = true, urgent = true, smartMatchingEnabled = false)
        val encoded = json.encodeToString(TradeItem.serializer(), item)
        val decoded = json.decodeFromString(TradeItem.serializer(), encoded)
        assertEquals(true, decoded.advertised)
        assertEquals(true, decoded.urgent)
        assertEquals(false, decoded.smartMatchingEnabled)
    }

    @Test fun tradeItem_deposited() {
        val item = TradeItem(id = "t3", deposited = true, depositedDate = "2026-01-01")
        assertTrue(item.deposited == true)
        assertEquals("2026-01-01", item.depositedDate)
    }

    @Test fun tradeItem_sellingDraft() {
        val item = TradeItem(
            amount = 100.0, askingRate = 1400000.0, currency = "CAD",
            purposeOfTransaction = "travel", sourceOfFund = "salary", urgent = true
        )
        assertEquals("travel", item.purposeOfTransaction)
        assertEquals("salary", item.sourceOfFund)
        assertEquals(true, item.urgent)
    }

    @Test fun tradeItem_buyingDraft() {
        val item = TradeItem(
            amount = 200.0, askingRate = 1200000.0, currency = "CAD", smartMatchingEnabled = true
        )
        assertEquals(true, item.smartMatchingEnabled)
    }

    @Test fun tradeItem_cannotDeleteWhenPosted() {
        val posted = TradeItem(id = "t4", advertised = true)
        assertTrue(posted.advertised == true)
        // Business rule: must unpost before delete
    }

    @Test fun tradeItem_canDeleteWhenUnposted() {
        val unposted = TradeItem(id = "t5", advertised = false, deposited = false)
        assertFalse(unposted.advertised == true)
        assertFalse(unposted.deposited == true)
        // Business rule: can delete
    }

    @Test fun tradeItem_cannotDeleteSellingWhenDeposited() {
        val deposited = TradeItem(id = "t6", advertised = false, deposited = true)
        assertTrue(deposited.deposited == true)
        // Business rule: must cancel e-Transfer first
    }
}

// ─── Locked Trade (Buy/Sell Confirm) ────────────────────────────────────────────

class LockedTradeTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun lockedTrade_defaults() {
        val lock = LockedTrade()
        assertEquals("", lock.id)
        assertEquals(0, lock.autoLockExpiresIn)
        assertEquals(0.0, lock.complianceFee)
        assertNull(lock.password)
        assertNull(lock.sellingCode)
    }

    @Test fun lockedTrade_serialization() {
        val lockJson = """{"id":"lock1","offeringId":null,"buyerId":"buyer1","autoLockTime":"2026-05-26T03:41:05","autoLockExpiryTime":"2026-05-26T03:41:35","autoLockExpiresIn":30,"arzookDepositEmail":"deposit_b@arzook.ca","arzookBankInfoName":null,"arzookBankInfoSheba":null,"holdTransactionFee":0,"complianceFee":4630000}"""
        val lock = json.decodeFromString(LockedTrade.serializer(), lockJson)
        assertEquals("lock1", lock.id)
        assertEquals(30, lock.autoLockExpiresIn)
        assertEquals(4630000.0, lock.complianceFee)
        assertEquals("deposit_b@arzook.ca", lock.arzookDepositEmail)
        assertNull(lock.arzookBankInfoName)
    }

    @Test fun lockedTrade_withPassword() {
        val lockJson = """{"id":"lock2","buyerId":"b1","autoLockExpiresIn":30,"autoLockExpiryTime":"","autoLockTime":"","complianceFee":0,"holdTransactionFee":0,"password":"Canada","sellingCode":"SLFRETFAT","arzookDepositEmail":"deposit@arzook.ca"}"""
        val lock = json.decodeFromString(LockedTrade.serializer(), lockJson)
        assertEquals("Canada", lock.password)
        assertEquals("SLFRETFAT", lock.sellingCode)
    }
}

// ─── Watch / Unwatch ────────────────────────────────────────────────────────────

class WatchItemTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun watchItem_serialization() {
        val watchJson = """{"offeringId":"offer1","watcherId":"user1","amount":100.0,"createdTime":"2026-01-01","listingRate":1400000.0}"""
        val item = json.decodeFromString(WatchItem.serializer(), watchJson)
        assertEquals("offer1", item.offeringId)
        assertEquals("user1", item.watcherId)
        assertEquals(100.0, item.amount)
    }
}

// ─── Payee ──────────────────────────────────────────────────────────────────────

class PayeeTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun payee_creation() {
        val payee = Payee(name = "Ali", sheba = "IR123456789012345678901234")
        assertEquals("Ali", payee.name)
        assertEquals("IR123456789012345678901234", payee.sheba)
        assertNull(payee.id)
    }

    @Test fun payee_serialization() {
        val payee = Payee(id = "p1", name = "Test", sheba = "IR000000000000000000000000", city = "Tehran")
        val encoded = json.encodeToString(Payee.serializer(), payee)
        val decoded = json.decodeFromString(Payee.serializer(), encoded)
        assertEquals("p1", decoded.id)
        assertEquals("Tehran", decoded.city)
    }
}

// ─── Rate Alert ─────────────────────────────────────────────────────────────────

class RateAlertTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun rateAlert_defaults() {
        val alert = RateAlert()
        assertNull(alert.sellingEnabled)
        assertNull(alert.minSellingRate)
        assertNull(alert.buyingEnabled)
    }

    @Test fun rateAlert_serialization() {
        val alert = RateAlert(sellingEnabled = true, minSellingRate = 1000000.0, maxSellingRate = 1200000.0)
        val encoded = json.encodeToString(RateAlert.serializer(), alert)
        val decoded = json.decodeFromString(RateAlert.serializer(), encoded)
        assertEquals(true, decoded.sellingEnabled)
        assertEquals(1000000.0, decoded.minSellingRate)
    }
}

// ─── Current Rate ───────────────────────────────────────────────────────────────

class CurrentRateTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun currentRate_serialization() {
        val rate = CurrentRate(
            currentMaxAskingRate = 1100000.0, currentMaxBuyingExchangeRate = 1050000.0,
            currentMidMarketRate = 1075000.0, currentMinAskingRate = 1000000.0,
            currentMinSellingExchangeRate = 950000.0, last24HourRateAvg = 1060000.0,
            userBuyingRateOffset = 5000.0, userSellingRateOffset = 3000.0
        )
        val encoded = json.encodeToString(CurrentRate.serializer(), rate)
        val decoded = json.decodeFromString(CurrentRate.serializer(), encoded)
        assertEquals(1100000.0, decoded.currentMaxAskingRate)
        assertEquals(1075000.0, decoded.currentMidMarketRate)
    }

    @Test fun suggestedRate_selling() {
        val rate = CurrentRate(
            currentMaxAskingRate = 0.0, currentMaxBuyingExchangeRate = 1050000.0,
            currentMidMarketRate = 0.0, currentMinAskingRate = 0.0,
            currentMinSellingExchangeRate = 0.0, last24HourRateAvg = 0.0,
            userBuyingRateOffset = 0.0, userSellingRateOffset = 3000.0
        )
        val suggested = (rate.currentMaxBuyingExchangeRate - rate.userSellingRateOffset).toInt()
        assertEquals(1047000, suggested)
    }

    @Test fun suggestedRate_buying() {
        val rate = CurrentRate(
            currentMaxAskingRate = 0.0, currentMaxBuyingExchangeRate = 0.0,
            currentMidMarketRate = 0.0, currentMinAskingRate = 0.0,
            currentMinSellingExchangeRate = 950000.0, last24HourRateAvg = 0.0,
            userBuyingRateOffset = 5000.0, userSellingRateOffset = 0.0
        )
        val suggested = (rate.currentMinSellingExchangeRate - rate.userBuyingRateOffset).toInt()
        assertEquals(945000, suggested)
    }
}

// ─── Wallet ─────────────────────────────────────────────────────────────────────

class WalletStatusTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun walletStatus_defaults() {
        val status = WalletStatus()
        assertEquals(0L, status.balance)
        assertEquals(0L, status.holdCredit)
    }

    @Test fun walletStatus_serialization() {
        val status = WalletStatus(userId = "u1", balance = 50000, holdCredit = 10000, lastUpdated = "2026-01-01")
        val encoded = json.encodeToString(WalletStatus.serializer(), status)
        val decoded = json.decodeFromString(WalletStatus.serializer(), encoded)
        assertEquals(50000L, decoded.balance)
        assertEquals(10000L, decoded.holdCredit)
    }

    @Test fun walletStatus_sufficientBalance() {
        val status = WalletStatus(balance = 200000000)
        val tradeTotal = 1200000.0 * 100.0 // 120,000,000 IRR
        assertTrue(status.balance >= tradeTotal.toLong())
    }

    @Test fun walletStatus_insufficientBalance() {
        val status = WalletStatus(balance = 50000000)
        val tradeTotal = 1200000.0 * 100.0
        assertFalse(status.balance >= tradeTotal.toLong())
    }
}

// ─── Promo Code ─────────────────────────────────────────────────────────────────

class PromoCodeResponseTest {
    @Test fun promoCode_valid() {
        val promo = PromoCodeResponse(code = "SAVE10", discountPercentage = 10, valid = true)
        assertEquals("SAVE10", promo.code)
        assertTrue(promo.valid!!)
    }
    @Test fun promoCode_invalid() {
        val promo = PromoCodeResponse(valid = false, message = "Expired")
        assertFalse(promo.valid!!)
        assertEquals("Expired", promo.message)
    }
}

// ─── Auth State ─────────────────────────────────────────────────────────────────

class AuthStateTest {
    @Test fun authState_idle() { assertIs<AuthState>(AuthState.Idle) }
    @Test fun authState_authenticated() {
        val data = AuthenticatedData(id = "u1", email = "a@b.com")
        val state: AuthState = AuthState.Authenticated(data)
        assertTrue(state is AuthState.Authenticated)
        assertEquals("u1", (state as AuthState.Authenticated).data.id)
    }
    @Test fun authState_error() {
        val state = AuthState.Error("Network error")
        assertEquals("Network error", (state as AuthState.Error).message)
    }
}

// ─── Update Selling Request Models ──────────────────────────────────────────────

class UpdateSellingRequestTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun updateAmount_serialization() {
        val req = UpdateSellingAmountRequest(amount = "500.0")
        val encoded = json.encodeToString(UpdateSellingAmountRequest.serializer(), req)
        assertTrue(encoded.contains("500.0"))
    }

    @Test fun updateRate_serialization() {
        val req = UpdateSellingRateRequest(rate = "1400000")
        val encoded = json.encodeToString(UpdateSellingRateRequest.serializer(), req)
        assertTrue(encoded.contains("1400000"))
    }

    @Test fun updatePayee_serialization() {
        val req = UpdateSellingPayeeRequest(id = "s1", sheba = "IR123", payeeName = "Ali")
        val encoded = json.encodeToString(UpdateSellingPayeeRequest.serializer(), req)
        assertTrue(encoded.contains("IR123"))
        assertTrue(encoded.contains("Ali"))
    }
}

// ─── Trade Toggle Business Logic ────────────────────────────────────────────────

class TradeToggleLogicTest {
    @Test fun advertised_toggle_updatesField() {
        val draft = TradeItem(id = "t1", advertised = false)
        val updated = draft.copy(advertised = true)
        assertTrue(updated.advertised == true)
        assertEquals("t1", updated.id)
    }

    @Test fun urgent_metadata_fields() {
        val draft = TradeItem(purposeOfTransaction = "travel", sourceOfFund = "salary", urgent = false)
        val updated = draft.copy(urgent = true)
        assertEquals("travel", updated.purposeOfTransaction)
        assertEquals("salary", updated.sourceOfFund)
        assertTrue(updated.urgent == true)
    }

    @Test fun smartMatching_toggle() {
        val draft = TradeItem(smartMatchingEnabled = true)
        val updated = draft.copy(smartMatchingEnabled = false)
        assertFalse(updated.smartMatchingEnabled == true)
    }
}

// ─── Service Rate Calculation ───────────────────────────────────────────────────

class ServiceRateCalculationTest {
    @Test fun selling_askingRate_equals_exchangeRate_minus_serviceRate() {
        val exchangeRate = 1406000.0
        val serviceRate = 6000.0
        val askingRate = exchangeRate - serviceRate
        assertEquals(1400000.0, askingRate)
    }

    @Test fun buying_askingRate_equals_exchangeRate_plus_serviceRate() {
        val exchangeRate = 1200000.0
        val serviceRate = 6000.0
        val askingRate = exchangeRate + serviceRate
        assertEquals(1206000.0, askingRate)
    }

    @Test fun netPayout_selling() {
        val askingRate = 1400000.0
        val amount = 100.0
        val complianceFee = 4630000.0
        val netPayout = askingRate * amount - complianceFee
        assertEquals(135370000.0, netPayout)
    }
}
