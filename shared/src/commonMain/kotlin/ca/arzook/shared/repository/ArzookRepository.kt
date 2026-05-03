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
    suspend fun updateSellingPayee(token: String, id: String, sheba: String, payeeName: String): Result<Unit>
    suspend fun createSellingDraft(token: String, request: ca.arzook.shared.model.TradeItem): Result<TradeItem>
    suspend fun createSellingDraftWithItem(token: String, draft: ca.arzook.shared.model.TradeItem): Result<Unit>
    suspend fun printBuyingTrade(token: String, id: String): Result<ByteArray>
    suspend fun printSellingTrade(token: String, id: String): Result<ByteArray>
    suspend fun validatePromoCode(token: String, promoCode: String): Result<PromoCodeResponse>
}
