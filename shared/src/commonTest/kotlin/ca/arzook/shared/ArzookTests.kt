package ca.arzook.shared

import ca.arzook.shared.model.*
import ca.arzook.shared.ui.LoginState
import ca.arzook.shared.ui.decodeUser
import ca.arzook.shared.ui.formatCad
import ca.arzook.shared.ui.formatIrr
import kotlinx.serialization.json.Json
import kotlin.test.*

class FormatUtilsTest {

    @Test
    fun formatIrr_wholeNumber() {
        assertEquals("1,000,000", formatIrr(1000000.0))
    }

    @Test
    fun formatIrr_smallNumber() {
        assertEquals("500", formatIrr(500.0))
    }

    @Test
    fun formatIrr_zero() {
        assertEquals("0", formatIrr(0.0))
    }

    @Test
    fun formatIrr_largeNumber() {
        assertEquals("1,400,000", formatIrr(1400000.0))
    }

    @Test
    fun formatCad_withDecimals() {
        assertEquals("1,250.50", formatCad(1250.50))
    }

    @Test
    fun formatCad_wholeNumber() {
        assertEquals("100.00", formatCad(100.0))
    }

    @Test
    fun formatCad_smallFraction() {
        assertEquals("0.01", formatCad(0.01))
    }
}

class ResultTest {

    @Test
    fun success_holdsData() {
        val result: Result<String> = Result.Success("hello")
        assertTrue(result is Result.Success)
        assertEquals("hello", result.data)
    }

    @Test
    fun error_holdsMessage() {
        val result: Result<String> = Result.Error("failed")
        assertTrue(result is Result.Error)
        assertEquals("failed", result.message)
        assertNull(result.data)
    }

    @Test
    fun error_withData() {
        val result: Result<Int> = Result.Error("partial", data = 42)
        assertTrue(result is Result.Error)
        assertEquals(42, result.data)
    }
}

class LoginResponseTest {

    @Test
    fun resolvedToken_withTokenType() {
        val response = LoginResponse(accessToken = "abc123", tokenType = "Bearer")
        assertEquals("Bearer abc123", response.resolvedToken())
    }

    @Test
    fun resolvedToken_alreadyPrefixed() {
        val response = LoginResponse(accessToken = "Bearer abc123", tokenType = "Bearer")
        assertEquals("Bearer abc123", response.resolvedToken())
    }

    @Test
    fun resolvedToken_emptyToken() {
        val response = LoginResponse(accessToken = "", tokenType = "Bearer")
        assertEquals("", response.resolvedToken())
    }

    @Test
    fun resolvedToken_emptyTokenType() {
        val response = LoginResponse(accessToken = "abc123", tokenType = "")
        assertEquals("abc123", response.resolvedToken())
    }
}

class TradeItemSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun tradeItem_defaultValues() {
        val item = TradeItem()
        assertNull(item.id)
        assertNull(item.amount)
        assertFalse(item.selling)
        assertEquals(0.0, item.complianceFee)
    }

    @Test
    fun tradeItem_serialization() {
        val item = TradeItem(id = "t1", amount = 500.0, askingRate = 1100000.0, selling = true)
        val encoded = json.encodeToString(TradeItem.serializer(), item)
        val decoded = json.decodeFromString(TradeItem.serializer(), encoded)
        assertEquals("t1", decoded.id)
        assertEquals(500.0, decoded.amount)
        assertEquals(1100000.0, decoded.askingRate)
        assertTrue(decoded.selling)
    }
}

class PayeeTest {

    @Test
    fun payee_creation() {
        val payee = Payee(name = "Ali", sheba = "IR123456789012345678901234")
        assertEquals("Ali", payee.name)
        assertEquals("IR123456789012345678901234", payee.sheba)
        assertNull(payee.id)
    }

    @Test
    fun payee_serialization() {
        val json = Json { ignoreUnknownKeys = true }
        val payee = Payee(id = "p1", name = "Test", sheba = "IR000000000000000000000000", city = "Tehran")
        val encoded = json.encodeToString(Payee.serializer(), payee)
        val decoded = json.decodeFromString(Payee.serializer(), encoded)
        assertEquals("p1", decoded.id)
        assertEquals("Tehran", decoded.city)
    }
}

class AuthModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun loginRequest_serialization() {
        val req = LoginRequest(email = "test@test.com", password = "pass123")
        val encoded = json.encodeToString(LoginRequest.serializer(), req)
        assertTrue(encoded.contains("test@test.com"))
        assertTrue(encoded.contains("pass123"))
    }

    @Test
    fun loginState_types() {
        assertIs<LoginState>(LoginState.Idle)
        assertIs<LoginState>(LoginState.Loading)
        assertIs<LoginState>(LoginState.Success)
        assertIs<LoginState>(LoginState.Error("err"))
        assertEquals("err", (LoginState.Error("err")).message)
    }

    @Test
    fun decodeUser_validJson() {
        val userJson = """{"id":"u1","email":"a@b.com","firstName":"John","lastName":"Doe"}"""
        val user = decodeUser(userJson)
        assertNotNull(user)
        assertEquals("u1", user.id)
        assertEquals("a@b.com", user.email)
        assertEquals("John", user.firstName)
    }

    @Test
    fun decodeUser_invalidJson() {
        val user = decodeUser("not json")
        assertNull(user)
    }

    @Test
    fun decodeUser_emptyString() {
        val user = decodeUser("")
        assertNull(user)
    }
}

class RateAlertTest {

    @Test
    fun rateAlert_defaults() {
        val alert = RateAlert()
        assertNull(alert.sellingEnabled)
        assertNull(alert.minSellingRate)
        assertNull(alert.buyingEnabled)
    }

    @Test
    fun rateAlert_serialization() {
        val json = Json { ignoreUnknownKeys = true }
        val alert = RateAlert(sellingEnabled = true, minSellingRate = 1000000.0, maxSellingRate = 1200000.0)
        val encoded = json.encodeToString(RateAlert.serializer(), alert)
        val decoded = json.decodeFromString(RateAlert.serializer(), encoded)
        assertEquals(true, decoded.sellingEnabled)
        assertEquals(1000000.0, decoded.minSellingRate)
        assertEquals(1200000.0, decoded.maxSellingRate)
    }
}

class CurrentRateTest {

    @Test
    fun currentRate_serialization() {
        val json = Json { ignoreUnknownKeys = true }
        val rate = CurrentRate(
            currentMaxAskingRate = 1100000.0,
            currentMaxBuyingExchangeRate = 1050000.0,
            currentMidMarketRate = 1075000.0,
            currentMinAskingRate = 1000000.0,
            currentMinSellingExchangeRate = 950000.0,
            last24HourRateAvg = 1060000.0,
            userBuyingRateOffset = 5000.0,
            userSellingRateOffset = 3000.0
        )
        val encoded = json.encodeToString(CurrentRate.serializer(), rate)
        val decoded = json.decodeFromString(CurrentRate.serializer(), encoded)
        assertEquals(1100000.0, decoded.currentMaxAskingRate)
        assertEquals(1075000.0, decoded.currentMidMarketRate)
    }
}

class WalletStatusTest {

    @Test
    fun walletStatus_defaults() {
        val status = WalletStatus()
        assertEquals("", status.userId)
        assertEquals(0L, status.balance)
        assertEquals(0L, status.holdCredit)
    }

    @Test
    fun walletStatus_serialization() {
        val json = Json { ignoreUnknownKeys = true }
        val status = WalletStatus(userId = "u1", balance = 50000, holdCredit = 10000, lastUpdated = "2026-01-01")
        val encoded = json.encodeToString(WalletStatus.serializer(), status)
        val decoded = json.decodeFromString(WalletStatus.serializer(), encoded)
        assertEquals(50000L, decoded.balance)
        assertEquals(10000L, decoded.holdCredit)
    }
}

class PromoCodeResponseTest {

    @Test
    fun promoCode_valid() {
        val promo = PromoCodeResponse(code = "SAVE10", discountPercentage = 10, valid = true)
        assertEquals("SAVE10", promo.code)
        assertEquals(10, promo.discountPercentage)
        assertTrue(promo.valid!!)
    }

    @Test
    fun promoCode_invalid() {
        val promo = PromoCodeResponse(valid = false, message = "Expired")
        assertFalse(promo.valid!!)
        assertEquals("Expired", promo.message)
    }
}

class AuthStateTest {

    @Test
    fun authState_idle() {
        val state: AuthState = AuthState.Idle
        assertTrue(state is AuthState.Idle)
    }

    @Test
    fun authState_authenticated() {
        val data = AuthenticatedData(id = "u1", email = "a@b.com")
        val state: AuthState = AuthState.Authenticated(data)
        assertTrue(state is AuthState.Authenticated)
        assertEquals("u1", (state as AuthState.Authenticated).data.id)
    }

    @Test
    fun authState_error() {
        val state: AuthState = AuthState.Error("Network error")
        assertTrue(state is AuthState.Error)
        assertEquals("Network error", (state as AuthState.Error).message)
    }
}

class FormatIrrExtraTest {
    @Test fun formatIrr_12345() { assertEquals("12,345", formatIrr(12345.0)) }
    @Test fun formatIrr_12345678() { assertEquals("12,345,678", formatIrr(12345678.0)) }
}
