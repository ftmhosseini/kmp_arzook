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

    override suspend fun getDailyStats() = Result.Success(emptyList<AveRates>())
    override suspend fun getTradesList() = tradesResult
    override suspend fun getUSDTradesList() = tradesResult
    override suspend fun login(request: LoginRequest) = loginResult
    override suspend fun googleSignIn(idToken: String) = loginResult
    override suspend fun register(user: User) = Result.Success(user)
    override suspend fun getUserDetails(token: String) = userDetailsResult
    override suspend fun updateProfile(token: String, request: UpdateProfileRequest) = userDetailsResult
    override suspend fun getRateAlerts(token: String) = Result.Success(RateAlert())
    override suspend fun saveRateAlerts(token: String, alert: RateAlert) = Result.Success(alert)
    override suspend fun getCurrentRate(token: String) = currentRateResult
    override suspend fun getWalletStatus(token: String) = walletResult
    override suspend fun getWatchList(token: String) = Result.Success(emptyList<WatchItem>())
    override suspend fun watchTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun unwatchTrade(token: String, id: String) = Result.Success(Unit)
    override suspend fun lockTrade(token: String, id: String) = Result.Success(LockedTrade(id = "lock1"))
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
    override suspend fun createSellingDraft(token: String, request: TradeItem) = Result.Success(request)
    override suspend fun createSellingDraftWithItem(token: String, draft: TradeItem) = Result.Success(Unit)
    override suspend fun printBuyingTrade(token: String, id: String) = Result.Success(ByteArray(0))
    override suspend fun printSellingTrade(token: String, id: String) = Result.Success(ByteArray(0))
    override suspend fun uploadPhotoId(token: String, bytes: ByteArray, fileName: String) = Result.Success(Unit)
    override suspend fun uploadUtilityBill(token: String, bytes: ByteArray, fileName: String) = Result.Success(Unit)
    override suspend fun validatePromoCode(token: String, promoCode: String) = Result.Success(PromoCodeResponse(valid = true))
    override suspend fun getAdminBuyingDraftById(token: String, id: String) = Result.Success(TradeItem())
    override suspend fun adminForwardETransfers(token: String, buyingId: String) = Result.Success(Unit)
    override suspend fun adminDeactivateBuying(token: String, id: String) = Result.Success(Unit)
    override suspend fun adminTransferToWallet(token: String, id: String) = Result.Success(Unit)
    override suspend fun adminComplete(token: String, id: String) = Result.Success(Unit)
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

class RepositoryTest {

    private val repo = FakeArzookRepository()

    @Test
    fun login_success() = runTest {
        val result = repo.login(LoginRequest("test@test.com", "pass"))
        assertTrue(result is Result.Success)
        assertEquals("token123", result.data.accessToken)
        assertEquals("Bearer token123", result.data.resolvedToken())
    }

    @Test
    fun login_failure() = runTest {
        repo.loginResult = Result.Error("Invalid credentials")
        val result = repo.login(LoginRequest("bad@test.com", "wrong"))
        assertTrue(result is Result.Error)
        assertEquals("Invalid credentials", result.message)
    }

    @Test
    fun getUserDetails_success() = runTest {
        val result = repo.getUserDetails("Bearer token")
        assertTrue(result is Result.Success)
        assertEquals("u1", result.data.id)
        assertEquals("test@test.com", result.data.email)
    }

    @Test
    fun getTradesList_empty() = runTest {
        val result = repo.getTradesList()
        assertTrue(result is Result.Success)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun getTradesList_withItems() = runTest {
        repo.tradesResult = Result.Success(listOf(
            TradeItem(id = "t1", amount = 500.0, selling = true),
            TradeItem(id = "t2", amount = 1000.0, selling = false)
        ))
        val result = repo.getTradesList()
        assertTrue(result is Result.Success)
        assertEquals(2, result.data.size)
        assertTrue(result.data[0].selling)
        assertFalse(result.data[1].selling)
    }

    @Test
    fun getCurrentRate() = runTest {
        val result = repo.getCurrentRate("token")
        assertTrue(result is Result.Success)
        assertEquals(1075000.0, result.data.currentMidMarketRate)
    }

    @Test
    fun getWalletStatus() = runTest {
        val result = repo.getWalletStatus("token")
        assertTrue(result is Result.Success)
        assertEquals(50000L, result.data.balance)
    }

    @Test
    fun lockTrade() = runTest {
        val result = repo.lockTrade("token", "trade1")
        assertTrue(result is Result.Success)
        assertEquals("lock1", result.data.id)
    }

    @Test
    fun getServiceRate() = runTest {
        val result = repo.getServiceRateForBuying("token", 500.0)
        assertTrue(result is Result.Success)
        assertEquals(15000.0, result.data)
    }

    @Test
    fun addPayee() = runTest {
        val payee = Payee(name = "Ali", sheba = "IR123")
        val result = repo.addPayee("token", payee)
        assertTrue(result is Result.Success)
        assertEquals("Ali", result.data.name)
    }

    @Test
    fun createSellingDraft() = runTest {
        val draft = TradeItem(amount = 1000.0, askingRate = 1100000.0, selling = true)
        val result = repo.createSellingDraft("token", draft)
        assertTrue(result is Result.Success)
        assertEquals(1000.0, result.data.amount)
    }

    @Test
    fun validatePromoCode() = runTest {
        val result = repo.validatePromoCode("token", "SAVE10")
        assertTrue(result is Result.Success)
        assertTrue(result.data.valid!!)
    }

    @Test
    fun register() = runTest {
        val user = User(firstName = "John", lastName = "Doe", email = "j@d.com", password = "pass123")
        val result = repo.register(user)
        assertTrue(result is Result.Success)
        assertEquals("John", result.data.firstName)
    }
}
