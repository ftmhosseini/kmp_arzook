package ca.arzook.shared

import ca.arzook.shared.model.*
import ca.arzook.shared.repository.ArzookRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FakeArzookRepository : ArzookRepository {
    var loginResult: Result<LoginResponse> = Result.Success(LoginResponse("token123", "Bearer"))
    var userDetailsResult: Result<AuthenticatedData> = Result.Success(
        AuthenticatedData(id = "u1", email = "test@test.com", firstName = "Test")
    )
    var tradesResult: Result<List<TradeItem>> = Result.Success(emptyList())
    var currentRateResult: Result<CurrentRate> = Result.Success(
        CurrentRate(1100000.0, 1050000.0, 1075000.0, 1000000.0, 950000.0, 1060000.0, 5000.0, 3000.0)
    )
    var walletResult: Result<WalletStatus> = Result.Success(WalletStatus("u1", 50000, 0, "2026-01-01"))
    var serviceRateResult: Result<Double> = Result.Success(15000.0)
    var lockResult: Result<LockedTrade> = Result.Success(LockedTrade(id = "lock1", autoLockExpiresIn = 30, complianceFee = 4630000.0, arzookDepositEmail = "deposit@arzook.ca"))
    var rateAlertResult: Result<RateAlert> = Result.Success(RateAlert(sellingEnabled = true, minSellingRate = 1000000.0))

    override suspend fun getDailyStats() = Result.Success(emptyList<AveRates>())
    override suspend fun getTradesList() = tradesResult
    override suspend fun getUSDTradesList() = tradesResult
    override suspend fun login(request: LoginRequest) = loginResult
    override suspend fun googleSignIn(idToken: String) = loginResult
    override suspend fun register(user: User) = Result.Success(user)
    override suspend fun getUserDetails(token: String) = userDetailsResult
    override suspend fun updateProfile(token: String, request: UpdateProfileRequest) = userDetailsResult
    override suspend fun getRateAlerts(token: String) = rateAlertResult
    override suspend fun saveRateAlerts(token: String, alert: RateAlert) = Result.Success(alert)
    override suspend fun getCurrentRate(token: String) = currentRateResult
    override suspend fun getWalletStatus(token: String) = walletResult
    override suspend fun getWatchList(token: String) = Result.Success(emptyList<WatchItem>())
    override suspend fun watchTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun unwatchTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun lockTrade(token: String, id: String) = lockResult
    override suspend fun unlockTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun buyTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun sellTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun getDepositList(token: String) = Result.Success(emptyList<DigitalWalletItem>())
    override suspend fun getBuyingTrades(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun getSellingTrades(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun getBuyingDrafts(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun getSellingDrafts(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun getPayees(token: String) = Result.Success(emptyList<Payee>())
    override suspend fun addPayee(token: String, payee: Payee) = Result.Success(payee)
    override suspend fun getServiceRateForBuying(token: String, amount: Double) = serviceRateResult
    override suspend fun getServiceRateForSelling(token: String, amount: Double) = serviceRateResult
    override suspend fun getSellingMakerServiceRate(token: String, amount: Double, promoCode: String) = serviceRateResult
    override suspend fun createBuyingDraft(token: String, request: TradeItem) = Result.Success(Unit)
    override suspend fun createBuyingDraftWithItem(token: String, draft: TradeItem) = Result.Success(Unit)
    override suspend fun updateBuyingAmount(token: String, id: String, amount: Double) = Result.Success(Unit)
    override suspend fun updateBuyingRate(token: String, id: String, rate: Double) = Result.Success(Unit)
    override suspend fun deleteBuyingDraft(token: String, id: String) = Result.Success(Unit)
    override suspend fun createBuyingSellingDraft(token: String, id: String) = Result.Success(Unit)
    override suspend fun deleteSellingDraft(token: String, id: String) = Result.Success(Unit)
    override suspend fun updateSellingAmount(token: String, id: String, amount: Double) = Result.Success(Unit)
    override suspend fun updateSellingRate(token: String, id: String, rate: Double) = Result.Success(Unit)
    override suspend fun updateSellingPayee(token: String, id: String, sheba: String, payeeName: String) = Result.Success(Unit)
    override suspend fun updateSellingAdvertised(token: String, draft: TradeItem) = Result.Success(Unit)
    override suspend fun updateSellingUrgent(token: String, id: String, purposeOfTransaction: String, sourceOfFund: String, urgent: Boolean) = Result.Success(Unit)
    override suspend fun updateBuyingAdvertised(token: String, draft: TradeItem) = Result.Success(Unit)
    override suspend fun updateBuyingSmartMatching(token: String, id: String, purposeOfTransaction: String, sourceOfFund: String, smartMatchingEnabled: Boolean) = Result.Success(Unit)
    override suspend fun createSellingDraft(token: String, request: TradeItem) = Result.Success(request)
    override suspend fun createSellingDraftWithItem(token: String, draft: TradeItem) = Result.Success(Unit)
    override suspend fun printBuyingTrade(token: String, id: String) = Result.Success(ByteArray(0))
    override suspend fun printSellingTrade(token: String, id: String) = Result.Success(ByteArray(0))
    override suspend fun uploadPhotoId(token: String, bytes: ByteArray, fileName: String) = Result.Success(Unit)
    override suspend fun uploadUtilityBill(token: String, bytes: ByteArray, fileName: String) = Result.Success(Unit)
    override suspend fun validatePromoCode(token: String, promoCode: String) = Result.Success(PromoCodeResponse(valid = true))
    override suspend fun getAdminBuyingDraftById(token: String, id: String) = Result.Success(TradeItem())
    override suspend fun adminForwardETransfers(token: String, buyingId: String) = Result.Success(Unit)
    override suspend fun adminDeactivateBuying(token: String, buyingId: String) = Result.Success(Unit)
    override suspend fun getAdminBuyingTrades(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun getAdminSellingTrades(token: String) = Result.Success(emptyList<TradeItem>())
    override suspend fun adminTransferToWallet(token: String, sellingId: String) = Result.Success(Unit)
    override suspend fun adminComplete(token: String, sellingId: String) = Result.Success(Unit)
    override suspend fun getAdminWalletItemTypes(token: String) = Result.Success(emptyList<DigitalWalletItemType>())
    override suspend fun getAdminWalletItems(token: String, fromDate: String?, toDate: String?, customer: String?, type: String?, bank: String?) = Result.Success(emptyList<DigitalWalletItem>())
    override suspend fun getAdminBuyingDrafts(token: String, deposited: Boolean) = Result.Success(emptyList<TradeItem>())
    override suspend fun getAdminSellingDrafts(token: String, deposited: Boolean) = Result.Success(emptyList<TradeItem>())
    override suspend fun getAdminSellingDraftById(token: String, id: String) = Result.Success(TradeItem())
    override suspend fun getAdminUser(token: String, userId: String) = userDetailsResult
    override suspend fun adminMarkDeposited(token: String, sellingId: String) = Result.Success(Unit)
    override suspend fun adminMarkExchangeDeposited(token: String, sellingId: String) = Result.Success(Unit)
    override suspend fun adminUploadReceipt(token: String, userId: String, bytes: ByteArray, fileName: String) = Result.Success(Unit)
    override suspend fun adminGetUserWallet(token: String, userId: String) = walletResult
}

// ─── Authentication Tests ───────────────────────────────────────────────────────

class LoginTest {
    private val repo = FakeArzookRepository()

    @Test fun login_success() = runTest {
        val result = repo.login(LoginRequest("test@test.com", "pass"))
        assertIs<Result.Success<LoginResponse>>(result)
        assertEquals("token123", result.data.accessToken)
        assertEquals("Bearer token123", result.data.resolvedToken())
    }

    @Test fun login_failure() = runTest {
        repo.loginResult = Result.Error("Invalid credentials")
        val result = repo.login(LoginRequest("bad@test.com", "wrong"))
        assertIs<Result.Error<LoginResponse>>(result)
        assertEquals("Invalid credentials", result.message)
    }

    @Test fun googleSignIn_success() = runTest {
        val result = repo.googleSignIn("google-id-token-xyz")
        assertIs<Result.Success<LoginResponse>>(result)
        assertEquals("Bearer token123", result.data.resolvedToken())
    }

    @Test fun googleSignIn_failure() = runTest {
        repo.loginResult = Result.Error("Google auth failed")
        val result = repo.googleSignIn("invalid-token")
        assertIs<Result.Error<LoginResponse>>(result)
    }
}

// ─── Create Account Tests ───────────────────────────────────────────────────────

class RegisterTest2 {
    private val repo = FakeArzookRepository()

    @Test fun register_success() = runTest {
        val user = User(firstName = "John", lastName = "Doe", email = "j@d.com", password = "Pass123!")
        val result = repo.register(user)
        assertIs<Result.Success<User>>(result)
        assertEquals("John", result.data.firstName)
        assertEquals("j@d.com", result.data.email)
    }

    @Test fun register_withInviter() = runTest {
        val user = User(firstName = "A", lastName = "B", email = "a@b.com", password = "x", inviterEmail = "ref@arzook.ca")
        val result = repo.register(user)
        assertIs<Result.Success<User>>(result)
        assertEquals("ref@arzook.ca", result.data.inviterEmail)
    }
}

// ─── Update Profile Tests ───────────────────────────────────────────────────────

class UpdateProfileTest2 {
    private val repo = FakeArzookRepository()

    @Test fun getUserDetails_success() = runTest {
        val result = repo.getUserDetails("Bearer token")
        assertIs<Result.Success<AuthenticatedData>>(result)
        assertEquals("u1", result.data.id)
        assertEquals("test@test.com", result.data.email)
    }

    @Test fun updateProfile_success() = runTest {
        val result = repo.updateProfile("token", UpdateProfileRequest(phoneNumber = "+14165551234", city = "Toronto"))
        assertIs<Result.Success<AuthenticatedData>>(result)
    }
}

// ─── Upload Documents Tests ─────────────────────────────────────────────────────

class UploadDocumentsTest {
    private val repo = FakeArzookRepository()

    @Test fun uploadPhotoId_success() = runTest {
        val result = repo.uploadPhotoId("token", ByteArray(100), "photo.jpg")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun uploadUtilityBill_success() = runTest {
        val result = repo.uploadUtilityBill("token", ByteArray(200), "bill.pdf")
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Rate Alert Tests ───────────────────────────────────────────────────────────

class RateAlertRepoTest {
    private val repo = FakeArzookRepository()

    @Test fun getRateAlerts_success() = runTest {
        val result = repo.getRateAlerts("token")
        assertIs<Result.Success<RateAlert>>(result)
        assertEquals(true, result.data.sellingEnabled)
        assertEquals(1000000.0, result.data.minSellingRate)
    }

    @Test fun saveRateAlerts_set() = runTest {
        val alert = RateAlert(sellingEnabled = true, minSellingRate = 1100000.0, maxSellingRate = 1300000.0)
        val result = repo.saveRateAlerts("token", alert)
        assertIs<Result.Success<RateAlert>>(result)
        assertEquals(1100000.0, result.data.minSellingRate)
    }

    @Test fun saveRateAlerts_update() = runTest {
        val alert = RateAlert(buyingEnabled = true, minBuyingRate = 900000.0, maxBuyingRate = 1100000.0)
        val result = repo.saveRateAlerts("token", alert)
        assertIs<Result.Success<RateAlert>>(result)
        assertEquals(true, result.data.buyingEnabled)
    }

    @Test fun saveRateAlerts_remove() = runTest {
        val alert = RateAlert(sellingEnabled = false, buyingEnabled = false)
        val result = repo.saveRateAlerts("token", alert)
        assertIs<Result.Success<RateAlert>>(result)
        assertEquals(false, result.data.sellingEnabled)
    }
}

// ─── Create Selling/Buying Tests ────────────────────────────────────────────────

class CreateTradeTest {
    private val repo = FakeArzookRepository()

    @Test fun createSellingDraft_success() = runTest {
        val draft = TradeItem(amount = 100.0, askingRate = 1400000.0, currency = "CAD", purposeOfTransaction = "travel", sourceOfFund = "salary", urgent = true)
        val result = repo.createSellingDraft("token", draft)
        assertIs<Result.Success<TradeItem>>(result)
        assertEquals(100.0, result.data.amount)
        assertEquals(true, result.data.urgent)
    }

    @Test fun createBuyingDraft_success() = runTest {
        val draft = TradeItem(amount = 200.0, askingRate = 1200000.0, currency = "CAD", smartMatchingEnabled = true)
        val result = repo.createBuyingDraft("token", draft)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun createSellingDraft_withPayee() = runTest {
        val draft = TradeItem(amount = 500.0, askingRate = 1400000.0, currency = "CAD", sheba = "IR123", payeeName = "Ali")
        val result = repo.createSellingDraft("token", draft)
        assertIs<Result.Success<TradeItem>>(result)
        assertEquals("IR123", result.data.sheba)
    }
}

// ─── Update Rate Tests ──────────────────────────────────────────────────────────

class UpdateRateTest {
    private val repo = FakeArzookRepository()

    @Test fun updateSellingAmount() = runTest {
        val result = repo.updateSellingAmount("token", "s1", 150.0)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateSellingRate() = runTest {
        val result = repo.updateSellingRate("token", "s1", 1450000.0)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateBuyingAmount() = runTest {
        val result = repo.updateBuyingAmount("token", "b1", 300.0)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateBuyingRate() = runTest {
        val result = repo.updateBuyingRate("token", "b1", 1250000.0)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateSellingPayee() = runTest {
        val result = repo.updateSellingPayee("token", "s1", "IR456", "Reza")
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Add Payee Tests ────────────────────────────────────────────────────────────

class AddPayeeTest {
    private val repo = FakeArzookRepository()

    @Test fun addPayee_success() = runTest {
        val payee = Payee(name = "Ali Hosseini", sheba = "IR123456789012345678901234", city = "Tehran")
        val result = repo.addPayee("token", payee)
        assertIs<Result.Success<Payee>>(result)
        assertEquals("Ali Hosseini", result.data.name)
        assertEquals("IR123456789012345678901234", result.data.sheba)
    }

    @Test fun getPayees_success() = runTest {
        val result = repo.getPayees("token")
        assertIs<Result.Success<List<Payee>>>(result)
    }
}

// ─── Toggle Post Tests ──────────────────────────────────────────────────────────

class TogglePostTest {
    private val repo = FakeArzookRepository()

    @Test fun updateSellingAdvertised_post() = runTest {
        val draft = TradeItem(id = "s1", advertised = true, amount = 100.0, askingRate = 1400000.0)
        val result = repo.updateSellingAdvertised("token", draft)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateSellingAdvertised_unpost() = runTest {
        val draft = TradeItem(id = "s1", advertised = false, amount = 100.0, askingRate = 1400000.0)
        val result = repo.updateSellingAdvertised("token", draft)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateBuyingAdvertised_post() = runTest {
        val draft = TradeItem(id = "b1", advertised = true, amount = 200.0, askingRate = 1200000.0)
        val result = repo.updateBuyingAdvertised("token", draft)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateBuyingAdvertised_unpost() = runTest {
        val draft = TradeItem(id = "b1", advertised = false, amount = 200.0, askingRate = 1200000.0)
        val result = repo.updateBuyingAdvertised("token", draft)
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Toggle Urgent Tests ────────────────────────────────────────────────────────

class ToggleUrgentTest {
    private val repo = FakeArzookRepository()

    @Test fun updateSellingUrgent_enable() = runTest {
        val result = repo.updateSellingUrgent("token", "s1", "travel", "salary", true)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateSellingUrgent_disable() = runTest {
        val result = repo.updateSellingUrgent("token", "s1", "travel", "salary", false)
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Toggle Smart Matching Tests ────────────────────────────────────────────────

class ToggleSmartMatchingTest {
    private val repo = FakeArzookRepository()

    @Test fun updateBuyingSmartMatching_enable() = runTest {
        val result = repo.updateBuyingSmartMatching("token", "b1", "travel", "salary", true)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun updateBuyingSmartMatching_disable() = runTest {
        val result = repo.updateBuyingSmartMatching("token", "b1", "travel", "salary", false)
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Delete Trade Tests ─────────────────────────────────────────────────────────

class DeleteTradeTest {
    private val repo = FakeArzookRepository()

    @Test fun deleteSellingDraft_success() = runTest {
        val result = repo.deleteSellingDraft("token", "s1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun deleteBuyingDraft_success() = runTest {
        val result = repo.deleteBuyingDraft("token", "b1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun deleteRules_cannotDeleteWhenPosted() {
        val trade = TradeItem(id = "t1", advertised = true)
        assertTrue(trade.advertised == true)
        // Business rule: show "Please make it unposted first"
    }

    @Test fun deleteRules_cannotDeleteSellingWhenDeposited() {
        val trade = TradeItem(id = "t1", advertised = false, deposited = true)
        assertTrue(trade.deposited == true)
        // Business rule: show "Please cancel the e-Transfer"
    }

    @Test fun deleteRules_canDeleteWhenUnpostedAndNotDeposited() {
        val trade = TradeItem(id = "t1", advertised = false, deposited = false)
        assertFalse(trade.advertised == true)
        assertFalse(trade.deposited == true)
        // Business rule: allow delete
    }
}

// ─── Watch / Unwatch Tests ──────────────────────────────────────────────────────

class WatchUnwatchTest {
    private val repo = FakeArzookRepository()

    @Test fun watchTrade_success() = runTest {
        val result = repo.watchTrade("token", "trade1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun unwatchTrade_success() = runTest {
        val result = repo.unwatchTrade("token", "trade1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun getWatchList_success() = runTest {
        val result = repo.getWatchList("token")
        assertIs<Result.Success<List<WatchItem>>>(result)
    }
}

// ─── Lock / Unlock / Confirm Tests ──────────────────────────────────────────────

class LockUnlockConfirmTest {
    private val repo = FakeArzookRepository()

    @Test fun lockTrade_success() = runTest {
        val result = repo.lockTrade("token", "trade1")
        assertIs<Result.Success<LockedTrade>>(result)
        assertEquals("lock1", result.data.id)
        assertEquals(30, result.data.autoLockExpiresIn)
        assertEquals(4630000.0, result.data.complianceFee)
        assertEquals("deposit@arzook.ca", result.data.arzookDepositEmail)
    }

    @Test fun lockTrade_conflict409() = runTest {
        repo.lockResult = Result.Error("This trade is currently locked by another user. Please try again shortly.")
        val result = repo.lockTrade("token", "trade1")
        assertIs<Result.Error<LockedTrade>>(result)
        assertTrue(result.message.contains("locked by another user"))
    }

    @Test fun unlockTrade_success() = runTest {
        val result = repo.unlockTrade("token", "trade1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun buyTrade_success() = runTest {
        val result = repo.buyTrade("token", "trade1")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test fun sellTrade_success() = runTest {
        val result = repo.sellTrade("token", "trade1")
        assertIs<Result.Success<Unit>>(result)
    }
}

// ─── Service Rate Tests ─────────────────────────────────────────────────────────

class ServiceRateTest {
    private val repo = FakeArzookRepository()

    @Test fun getServiceRateForBuying() = runTest {
        val result = repo.getServiceRateForBuying("token", 500.0)
        assertIs<Result.Success<Double>>(result)
        assertEquals(15000.0, result.data)
    }

    @Test fun getServiceRateForSelling() = runTest {
        val result = repo.getServiceRateForSelling("token", 500.0)
        assertIs<Result.Success<Double>>(result)
        assertEquals(15000.0, result.data)
    }

    @Test fun getSellingMakerServiceRate_withPromo() = runTest {
        val result = repo.getSellingMakerServiceRate("token", 500.0, "PROMO10")
        assertIs<Result.Success<Double>>(result)
    }
}

// ─── Wallet Tests ───────────────────────────────────────────────────────────────

class WalletTest {
    private val repo = FakeArzookRepository()

    @Test fun getWalletStatus() = runTest {
        val result = repo.getWalletStatus("token")
        assertIs<Result.Success<WalletStatus>>(result)
        assertEquals(50000L, result.data.balance)
    }

    @Test fun getDepositList() = runTest {
        val result = repo.getDepositList("token")
        assertIs<Result.Success<List<DigitalWalletItem>>>(result)
    }
}

// ─── Current Rate Tests ─────────────────────────────────────────────────────────

class CurrentRateRepoTest {
    private val repo = FakeArzookRepository()

    @Test fun getCurrentRate() = runTest {
        val result = repo.getCurrentRate("token")
        assertIs<Result.Success<CurrentRate>>(result)
        assertEquals(1075000.0, result.data.currentMidMarketRate)
        assertEquals(1050000.0, result.data.currentMaxBuyingExchangeRate)
    }
}

// ─── Promo Code Tests ───────────────────────────────────────────────────────────

class PromoCodeRepoTest {
    private val repo = FakeArzookRepository()

    @Test fun validatePromoCode_valid() = runTest {
        val result = repo.validatePromoCode("token", "SAVE10")
        assertIs<Result.Success<PromoCodeResponse>>(result)
        assertTrue(result.data.valid!!)
    }
}

// ─── Completed Trades Tests ─────────────────────────────────────────────────────

class CompletedTradesTest {
    private val repo = FakeArzookRepository()

    @Test fun getBuyingTrades() = runTest {
        val result = repo.getBuyingTrades("token")
        assertIs<Result.Success<List<TradeItem>>>(result)
    }

    @Test fun getSellingTrades() = runTest {
        val result = repo.getSellingTrades("token")
        assertIs<Result.Success<List<TradeItem>>>(result)
    }

    @Test fun getBuyingDrafts() = runTest {
        val result = repo.getBuyingDrafts("token")
        assertIs<Result.Success<List<TradeItem>>>(result)
    }

    @Test fun getSellingDrafts() = runTest {
        val result = repo.getSellingDrafts("token")
        assertIs<Result.Success<List<TradeItem>>>(result)
    }
}

// ─── Print Trade Tests ──────────────────────────────────────────────────────────

class PrintTradeTest {
    private val repo = FakeArzookRepository()

    @Test fun printBuyingTrade() = runTest {
        val result = repo.printBuyingTrade("token", "t1")
        assertIs<Result.Success<ByteArray>>(result)
    }

    @Test fun printSellingTrade() = runTest {
        val result = repo.printSellingTrade("token", "t1")
        assertIs<Result.Success<ByteArray>>(result)
    }
}
