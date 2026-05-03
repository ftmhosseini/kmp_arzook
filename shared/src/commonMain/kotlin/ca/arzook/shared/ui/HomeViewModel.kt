package ca.arzook.shared.ui

import ca.arzook.shared.Result
import ca.arzook.shared.model.*
import ca.arzook.shared.repository.ArzookRepositoryImpl
import ca.arzook.shared.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel {
    private val repo = ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca")
    private val wsManager = WebSocketManager(baseUrl = "wss://api.arzook.ca")
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _aveRates = MutableStateFlow<List<AveRates>>(emptyList())
    val aveRates: StateFlow<List<AveRates>> = _aveRates.asStateFlow()

    private val _cadTrades = MutableStateFlow<List<TradeItem>>(emptyList())
    val cadTrades: StateFlow<List<TradeItem>> = _cadTrades.asStateFlow()

    private val _usdTrades = MutableStateFlow<List<TradeItem>>(emptyList())
    val usdTrades: StateFlow<List<TradeItem>> = _usdTrades.asStateFlow()

    private val _watchList = MutableStateFlow<List<WatchItem>>(emptyList())
    val watchList: StateFlow<List<WatchItem>> = _watchList.asStateFlow()

    private val _deposits = MutableStateFlow<List<DigitalWalletItem>>(emptyList())
    val deposits: StateFlow<List<DigitalWalletItem>> = _deposits.asStateFlow()

    private val _walletStatus = MutableStateFlow<WalletStatus?>(null)
    val walletStatus: StateFlow<WalletStatus?> = _walletStatus.asStateFlow()

    private val _currentRate = MutableStateFlow<CurrentRate?>(null)
    val currentRate: StateFlow<CurrentRate?> = _currentRate.asStateFlow()

    private val _buyingServiceRate = MutableStateFlow<Double?>(null)
    val buyingServiceRate: StateFlow<Double?> = _buyingServiceRate.asStateFlow()

    private val _sellingServiceRate = MutableStateFlow<Double?>(null)
    val sellingServiceRate: StateFlow<Double?> = _sellingServiceRate.asStateFlow()

    private val _buyingDrafts = MutableStateFlow<List<TradeItem>>(emptyList())
    val buyingDrafts: StateFlow<List<TradeItem>> = _buyingDrafts.asStateFlow()

    private val _sellingDrafts = MutableStateFlow<List<TradeItem>>(emptyList())
    val sellingDrafts: StateFlow<List<TradeItem>> = _sellingDrafts.asStateFlow()

    private val _buyingTrades = MutableStateFlow<List<TradeItem>>(emptyList())
    val buyingTrades: StateFlow<List<TradeItem>> = _buyingTrades.asStateFlow()

    private val _sellingTrades = MutableStateFlow<List<TradeItem>>(emptyList())
    val sellingTrades: StateFlow<List<TradeItem>> = _sellingTrades.asStateFlow()

    private val _payees = MutableStateFlow<List<Payee>>(emptyList())
    val payees: StateFlow<List<Payee>> = _payees.asStateFlow()

    private val _lockedTrades = MutableStateFlow<Map<String, LockedTrade>>(emptyMap())
    val lockedTrades: StateFlow<Map<String, LockedTrade>> = _lockedTrades.asStateFlow()

    // tradeId -> service rate
    private val _serviceRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val serviceRates: StateFlow<Map<String, Double>> = _serviceRates.asStateFlow()

    init {
        scope.launch {
            when (val r = repo.getDailyStats()) {
                is Result.Success -> _aveRates.value = r.data
                is Result.Error -> println("[HomeVM] getDailyStats error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getTradesList()) {
                is Result.Success -> { _cadTrades.value = r.data; println("[HomeVM] cadTrades count=${r.data.size} selling=${r.data.count{it.selling}} buying=${r.data.count{!it.selling}}") }
                is Result.Error -> println("[HomeVM] getTradesList error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getUSDTradesList()) {
                is Result.Success -> { _usdTrades.value = r.data; println("[HomeVM] usdTrades count=${r.data.size}") }
                is Result.Error -> println("[HomeVM] getUSDTradesList error: ${r.message}")
            }
        }
    }

    fun loadWatchList(token: String) {
        scope.launch {
            when (val r = repo.getWatchList(token)) {
                is Result.Success -> {
                    println("[HomeVM] watchList loaded: ${r.data.size} items, ids=${r.data.map { it.offeringId }}")
                    _watchList.value = r.data
                }
                is Result.Error -> println("[HomeVM] watchList error: ${r.message}")
            }
        }
    }

    fun loadUserData(token: String) {
        scope.launch {
            when (val r = repo.getWalletStatus(token)) {
                is Result.Success -> { println("[HomeVM] walletStatus: balance=${r.data.balance} holdCredit=${r.data.holdCredit}"); _walletStatus.value = r.data }
                is Result.Error -> println("[HomeVM] getWalletStatus error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getCurrentRate(token)) {
                is Result.Success -> { _currentRate.value = r.data; println("[HomeVM] currentRate: buyingOffset=${r.data.userBuyingRateOffset} sellingOffset=${r.data.userSellingRateOffset}") }
                is Result.Error -> println("[HomeVM] getCurrentRate error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getServiceRateForBuying(token, 100.0)) {
                is Result.Success -> { _buyingServiceRate.value = r.data; println("[HomeVM] buyingServiceRate=${r.data}") }
                is Result.Error -> println("[HomeVM] buyingServiceRate error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getServiceRateForSelling(token, 100.0)) {
                is Result.Success -> { _sellingServiceRate.value = r.data; println("[HomeVM] sellingServiceRate=${r.data}") }
                is Result.Error -> println("[HomeVM] sellingServiceRate error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getDepositList(token)) {
                is Result.Success -> _deposits.value = r.data
                is Result.Error -> println("[HomeVM] getDepositList error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getBuyingDrafts(token)) {
                is Result.Success -> { 
                    _buyingDrafts.value = r.data
                    println("[HomeVM] ===== BUYING DRAFTS RESPONSE =====")
                    println("[HomeVM] buyingDrafts count=${r.data.size}")
                    r.data.forEach { draft ->
                        println("[HomeVM] BuyingDraft: id=${draft.id}, code=${draft.code}, amount=${draft.amount}, locked=${draft.locked}")
                    }
                    println("[HomeVM] ===================================")
                }
                is Result.Error -> println("[HomeVM] getBuyingDrafts error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getSellingDrafts(token)) {
                is Result.Success -> { 
                    _sellingDrafts.value = r.data
                    println("[HomeVM] ===== SELLING DRAFTS RESPONSE =====")
                    println("[HomeVM] sellingDrafts count=${r.data.size}")
                    r.data.forEach { draft ->
                        println("[HomeVM] SellingDraft: id=${draft.id}, code=${draft.code}, amount=${draft.amount}, locked=${draft.locked}, isLocked=${draft.isLocked}, lockExpiresIn=${draft.lockExpiresIn}")
                    }
                    println("[HomeVM] ====================================")
                }
                is Result.Error -> println("[HomeVM] getSellingDrafts error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getBuyingTrades(token)) {
                is Result.Success -> { _buyingTrades.value = r.data; println("[HomeVM] buyingTrades count=${r.data.size}") }
                is Result.Error -> println("[HomeVM] getBuyingTrades error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getSellingTrades(token)) {
                is Result.Success -> { _sellingTrades.value = r.data; println("[HomeVM] sellingTrades count=${r.data.size}") }
                is Result.Error -> println("[HomeVM] getSellingTrades error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getPayees(token)) {
                is Result.Success -> { _payees.value = r.data; println("[HomeVM] payees count=${r.data.size}") }
                is Result.Error -> println("[HomeVM] getPayees error: ${r.message}")
            }
        }
    }

    fun getTradeById(id: String?): TradeItem? {
        if (id == null) return null
        return _cadTrades.value.find { it.id == id } 
            ?: _usdTrades.value.find { it.id == id }
    }


    fun watchTrade(token: String, id: String) {
        scope.launch {
            when (val r = repo.watchTrade(token, id)) {
                is Result.Success -> loadWatchList(token)
                is Result.Error -> println("[HomeVM] watchTrade error: ${r.message}")
            }
        }
    }

    fun unwatchTrade(token: String, id: String) {
        scope.launch {
            when (val r = repo.unwatchTrade(token, id)) {
                is Result.Success -> loadWatchList(token)
                is Result.Error -> println("[HomeVM] unwatchTrade error: ${r.message}")
            }
        }
    }

    fun deleteBuyingDraft(token: String, id: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            when (val r = repo.deleteBuyingDraft(token, id)) {
                is Result.Success -> { loadUserData(token); onSuccess() }
                is Result.Error -> println("[HomeVM] deleteBuyingDraft error: ${r.message}")
            }
        }
    }

    fun addPayee(token: String, payee: Payee, onSuccess: (Payee) -> Unit = {}, onError: (String) -> Unit = {}) {
        scope.launch {
            when (val r = repo.addPayee(token, payee)) {
                is Result.Success -> { _payees.value = _payees.value + r.data; onSuccess(r.data) }
                is Result.Error -> onError(r.message ?: "Failed to add payee")
            }
        }
    }

    fun deleteSellingDraft(token: String, id: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            when (val r = repo.deleteSellingDraft(token, id)) {
                is Result.Success -> { loadUserData(token); onSuccess() }
                is Result.Error -> println("[HomeVM] deleteSellingDraft error: ${r.message}")
            }
        }
    }

    fun updateSellingDraft(token: String, id: String, amount: Double, rate: Double, sheba: String, payeeName: String) {
        scope.launch {
            repo.updateSellingAmount(token, id, amount)
            repo.updateSellingRate(token, id, rate)
            if (sheba.isNotEmpty() || payeeName.isNotEmpty())
                repo.updateSellingPayee(token, id, sheba, payeeName)
            loadUserData(token)
        }
    }

    fun lockTrade(token: String, id: String, onSuccess: (LockedTrade) -> Unit) {
        scope.launch {
            when (val r = repo.lockTrade(token, id)) {
                is Result.Success -> {
                    println("[HomeVM] lockTrade SUCCESS: expiresIn=${r.data.autoLockExpiresIn}s")
                    _lockedTrades.value = _lockedTrades.value + (id to r.data)
                    onSuccess(r.data)
                }
                is Result.Error -> {
                    println("[HomeVM] lockTrade ERROR: ${r.message}")
                    _tradeError.value = r.message ?: "Failed to lock trade."
                }
            }
        }
    }

    private val _tradeError = MutableStateFlow<String?>(null)
    val tradeError: StateFlow<String?> = _tradeError.asStateFlow()

    fun clearTradeError() { _tradeError.value = null }

    fun confirmTrade(token: String, id: String, isSelling: Boolean, amount: Double, onSuccess: () -> Unit) {
        scope.launch {
            // Step 1: fetch service rate
            val rateResult = if (isSelling) repo.getServiceRateForSelling(token, amount)
                             else repo.getServiceRateForBuying(token, amount)
            when (rateResult) {
                is Result.Success -> println("[HomeVM] confirmTrade serviceRate=${rateResult.data}")
                is Result.Error -> println("[HomeVM] confirmTrade serviceRate error: ${rateResult.message}")
            }

            // Step 2: confirm trade
            println("{confirmTrade] id= $id")
            when (val r = repo.createBuyingSellingDraft(token, id)) {
                is Result.Success -> {
                    println("[HomeVM] confirmTrade SUCCESS")
                    // Step 3: refresh the relevant drafts list
                    if (isSelling) {
                        when (val s = repo.getSellingDrafts(token)) {
                            is Result.Success -> _sellingDrafts.value = s.data
                            is Result.Error -> println("[HomeVM] refresh sellingDrafts error: ${s.message}")
                        }
                    } else {
                        when (val b = repo.getBuyingDrafts(token)) {
                            is Result.Success -> _buyingDrafts.value = b.data
                            is Result.Error -> println("[HomeVM] refresh buyingDrafts error: ${b.message}")
                        }
                    }
                    onSuccess()
                }
                is Result.Error -> {
                    println("[HomeVM] confirmTrade ERROR: ${r.message} — unlocking")
                    _tradeError.value = r.message ?: "Failed to confirm trade. Please try again."
                    repo.unlockTrade(token, id)
                }
            }
        }
    }

    fun unlockTrade(token: String, id: String) {
        scope.launch { repo.unlockTrade(token, id) }
    }

    fun buySellTrade(token: String, id: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            val lockResult = repo.lockTrade(token, id)
            if (lockResult is Result.Error) {
                _tradeError.value = lockResult.message ?: "Failed to lock trade."
                return@launch
            }
            when (val r = repo.createBuyingSellingDraft(token, id)) {
                is Result.Success -> { loadUserData(token); kotlinx.coroutines.delay(500); onSuccess() }
                is Result.Error -> { _tradeError.value = r.message ?: "Failed to process trade." }
            }
        }
    }

    fun loadServiceRate(token: String, tradeId: String, amount: Double, isSelling: Boolean) {
        scope.launch {
            // selling trade → use selling-taker-service-rate; buying trade → buying-taker-service-rate
            val result = if (isSelling) repo.getServiceRateForSelling(token, amount)
                         else repo.getServiceRateForBuying(token, amount)
            when (result) {
                is Result.Success -> _serviceRates.value = _serviceRates.value + (tradeId to result.data)
                is Result.Error -> println("[HomeVM] serviceRate error: ${result.message}")
            }
        }
    }

    // WebSocket functions
    fun connectWebSocket(token: String) {
        scope.launch {
            wsManager.connect(token)
        }
        
        // Listen for WebSocket messages
        scope.launch {
            wsManager.messages.collect { message ->
                handleWebSocketMessage(message, token)
            }
        }
        
        // Listen for connection state
        scope.launch {
            wsManager.connectionState.collect { state ->
                println("[HomeVM] WebSocket state: $state")
            }
        }
    }
    
    private fun handleWebSocketMessage(message: ca.arzook.shared.websocket.WebSocketMessage, token: String) {
        println("[HomeVM] Handling WebSocket message: type=${message.type}")
        when (message.type) {
            "TRADE_UPDATE" -> {
                // Refresh trades when any trade is created/updated/deleted
                refreshTrades()
            }
            "DRAFT_CREATED", "DRAFT_UPDATED", "DRAFT_DELETED" -> {
                // Refresh user data (drafts, trades, etc.)
                loadUserData(token)
            }
            "TRADE_MATCHED" -> {
                // Refresh everything
                loadUserData(token)
                refreshTrades()
            }
            "NOTIFICATION" -> {
                // Handle notification (you can emit this to UI)
                println("[HomeVM] Notification: ${message.title} - ${message.message}")
            }
        }
    }
    
    fun refreshTrades() {
        scope.launch {
            when (val r = repo.getTradesList()) {
                is Result.Success -> {
                    _cadTrades.value = r.data
                    println("[HomeVM] Refreshed cadTrades count=${r.data.size}")
                }
                is Result.Error -> println("[HomeVM] refresh cadTrades error: ${r.message}")
            }
        }
        scope.launch {
            when (val r = repo.getUSDTradesList()) {
                is Result.Success -> {
                    _usdTrades.value = r.data
                    println("[HomeVM] Refreshed usdTrades count=${r.data.size}")
                }
                is Result.Error -> println("[HomeVM] refresh usdTrades error: ${r.message}")
            }
        }
    }

    fun refreshCadTrades() {
        scope.launch {
            when (val r = repo.getTradesList()) {
                is Result.Success -> _cadTrades.value = r.data
                is Result.Error -> println("[HomeVM] refresh cadTrades error: ${r.message}")
            }
        }
    }

    fun refreshUsdTrades() {
        scope.launch {
            when (val r = repo.getUSDTradesList()) {
                is Result.Success -> _usdTrades.value = r.data
                is Result.Error -> println("[HomeVM] refresh usdTrades error: ${r.message}")
            }
        }
    }
    
    fun disconnectWebSocket() {
        scope.launch {
            wsManager.disconnect()
        }
    }
}
