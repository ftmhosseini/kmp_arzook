package ca.arzook.shared.repository

import ca.arzook.shared.Result
import ca.arzook.shared.model.*

interface ArzookRepository {
    // Public (no auth)
    suspend fun getDailyStats(): Result<List<AveRates>>
    suspend fun getTradesList(): Result<List<TradeItem>>
    suspend fun getUSDTradesList(): Result<List<TradeItem>>

    // Auth
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun googleSignIn(idToken: String): Result<LoginResponse>
    suspend fun register(user: User): Result<User>
    suspend fun getUserDetails(token: String): Result<AuthenticatedData>
    suspend fun updateProfile(token: String, request: UpdateProfileRequest): Result<AuthenticatedData>
    suspend fun getRateAlerts(token: String): Result<RateAlert>
    suspend fun saveRateAlerts(token: String, alert: RateAlert): Result<RateAlert>

    // Trade
    suspend fun getCurrentRate(token: String): Result<CurrentRate>
    suspend fun getWalletStatus(token: String): Result<WalletStatus>
    suspend fun getWatchList(token: String): Result<List<WatchItem>>
    suspend fun watchTrade(token: String, id: String): Result<Unit>
    suspend fun unwatchTrade(token: String, id: String): Result<Unit>
    suspend fun lockTrade(token: String, id: String): Result<LockedTrade>
    suspend fun unlockTrade(token: String, id: String): Result<Unit>
    suspend fun buyTrade(token: String, id: String): Result<Unit>
    suspend fun sellTrade(token: String, id: String): Result<Unit>

    // Wallet
    suspend fun getDepositList(token: String): Result<List<DigitalWalletItem>>

    // Trades
    suspend fun getBuyingTrades(token: String): Result<List<TradeItem>>
    suspend fun getSellingTrades(token: String): Result<List<TradeItem>>
    suspend fun getBuyingDrafts(token: String): Result<List<TradeItem>>
    suspend fun getSellingDrafts(token: String): Result<List<TradeItem>>
    suspend fun getPayees(token: String): Result<List<Payee>>
    suspend fun addPayee(token: String, payee: Payee): Result<Payee>
    suspend fun getServiceRateForBuying(token: String, amount: Double): Result<Double>
    suspend fun getServiceRateForSelling(token: String, amount: Double): Result<Double>
    suspend fun getSellingMakerServiceRate(token: String, amount: Double, promoCode: String): Result<Double>
    suspend fun createBuyingDraft(token: String, request: ca.arzook.shared.model.TradeItem): Result<Unit>
    suspend fun createBuyingDraftWithItem(token: String, draft: ca.arzook.shared.model.TradeItem): Result<Unit>
    suspend fun updateBuyingAmount(token: String, id: String, amount: Double): Result<Unit>
    suspend fun updateBuyingRate(token: String, id: String, rate: Double): Result<Unit>
    suspend fun deleteBuyingDraft(token: String, id: String): Result<Unit>
    suspend fun createBuyingSellingDraft(token: String, id: String): Result<Unit>
    suspend fun deleteSellingDraft(token: String, id: String): Result<Unit>
    suspend fun updateSellingAmount(token: String, id: String, amount: Double): Result<Unit>
    suspend fun updateSellingRate(token: String, id: String, rate: Double): Result<Unit>
    suspend fun updateSellingAdvertised(token: String, draft: TradeItem): Result<Unit>
//    suspend fun updateSellingAdvertised(token: String, id: String, advertised: Boolean): Result<Unit>
    suspend fun updateSellingUrgent(token: String, id: String, purposeOfTransaction: String, sourceOfFund: String, urgent: Boolean): Result<Unit>
    suspend fun updateBuyingAdvertised(token: String, draft: TradeItem): Result<Unit>
//    suspend fun updateBuyingAdvertised(token: String, id: String, advertised: Boolean): Result<Unit>
    suspend fun updateBuyingSmartMatching(token: String, id: String, purposeOfTransaction: String, sourceOfFund: String, smartMatchingEnabled: Boolean): Result<Unit>
    suspend fun updateSellingPayee(token: String, id: String, sheba: String, payeeName: String): Result<Unit>
    suspend fun createSellingDraft(token: String, request: ca.arzook.shared.model.TradeItem): Result<TradeItem>
    suspend fun createSellingDraftWithItem(token: String, draft: ca.arzook.shared.model.TradeItem): Result<Unit>
    suspend fun printBuyingTrade(token: String, id: String): Result<ByteArray>
    suspend fun printSellingTrade(token: String, id: String): Result<ByteArray>
    suspend fun uploadPhotoId(token: String, bytes: ByteArray, fileName: String): Result<Unit>
    suspend fun uploadUtilityBill(token: String, bytes: ByteArray, fileName: String): Result<Unit>
    suspend fun validatePromoCode(token: String, promoCode: String): Result<PromoCodeResponse>

    // Admin
    suspend fun getAdminWalletItemTypes(token: String): Result<List<DigitalWalletItemType>>
    suspend fun getAdminWalletItems(
        token: String,
        fromDate: String? = null,
        toDate: String? = null,
        customer: String? = null,
        type: String? = null,
        bank: String? = null
    ): Result<List<DigitalWalletItem>>
    suspend fun getAdminBuyingDrafts(token: String, deposited: Boolean = true): Result<List<TradeItem>>
    suspend fun getAdminSellingDrafts(token: String, deposited: Boolean = true): Result<List<TradeItem>>
    suspend fun getAdminBuyingDraftById(token: String, id: String): Result<TradeItem>
    suspend fun getAdminSellingDraftById(token: String, id: String): Result<TradeItem>
    suspend fun getAdminUser(token: String, userId: String): Result<AuthenticatedData>

    // Admin actions
    suspend fun adminMarkDeposited(token: String, sellingId: String): Result<Unit>
    suspend fun adminMarkExchangeDeposited(token: String, sellingId: String): Result<Unit>
    suspend fun adminTransferToWallet(token: String, sellingId: String): Result<Unit>
    suspend fun adminComplete(token: String, sellingId: String): Result<Unit>
    suspend fun adminUploadReceipt(token: String, userId: String, bytes: ByteArray, fileName: String): Result<Unit>
    suspend fun adminGetUserWallet(token: String, userId: String): Result<WalletStatus>
    suspend fun adminForwardETransfers(token: String, buyingId: String): Result<Unit>
    suspend fun adminDeactivateBuying(token: String, buyingId: String): Result<Unit>
    suspend fun getAdminBuyingTrades(token: String): Result<List<TradeItem>>
    suspend fun getAdminSellingTrades(token: String): Result<List<TradeItem>>
}
