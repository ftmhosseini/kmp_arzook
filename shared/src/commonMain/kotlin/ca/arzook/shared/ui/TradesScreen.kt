package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.LockedTrade
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.model.WatchItem
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun TradesScreen(
    isSelling: Boolean? = null,
    initialFrom: String = "CAD",
    initialTo: String = "IRR",
    onCurrencyChange: (from: String, to: String) -> Unit = { _, _ -> },
    token: String?,
    onLoginRequired: () -> Unit,
    onSell: () -> Unit = {},
    onBuy: () -> Unit = {},
    homeViewModel: HomeViewModel,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
) {
    var fromCurrency by remember { mutableStateOf(initialFrom) }
    var toCurrency by remember { mutableStateOf(initialTo) }
    LaunchedEffect(initialFrom, initialTo) { fromCurrency = initialFrom; toCurrency = initialTo }
    val cadTrades by homeViewModel.cadTrades.collectAsState()
    val usdTrades by homeViewModel.usdTrades.collectAsState()
    val watchList by homeViewModel.watchList.collectAsState()
    val buyingDrafts by homeViewModel.buyingDrafts.collectAsState()
    val sellingDrafts by homeViewModel.sellingDrafts.collectAsState()
    val serviceRates by homeViewModel.serviceRates.collectAsState()
    val walletStatus by homeViewModel.walletStatus.collectAsState()
    val publicCompliance by homeViewModel.publicCompliance.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(token) {
        if (!token.isNullOrEmpty()) {
            println("[TradesScreen] loading watchList with token prefix=${token.take(20)}")
            homeViewModel.loadWatchList(token)
        }
    }

    val tradeError by homeViewModel.tradeError.collectAsState()
    LaunchedEffect(tradeError) {
        tradeError?.let {
            snackbarHostState.showSnackbar(it)
            homeViewModel.clearTradeError()
        }
    }

    var tabIndex by remember { mutableIntStateOf(0) }
    var depositedOnly by remember { mutableStateOf(false) }


    val scrollState = rememberScrollState()

    val tradeList: List<TradeItem> by remember(
        cadTrades,
        usdTrades,
        isSelling,
        tabIndex,
        fromCurrency,
        toCurrency,
        depositedOnly
    ) {
        derivedStateOf {
            val pair = setOf(fromCurrency, toCurrency)
            when {
                isSelling != null -> {
                    val rawList = if (tabIndex == 0) cadTrades else usdTrades
                    rawList.filter { it.selling == isSelling && (!depositedOnly || (it.deposited == true && it.status == null)) }
                }

                pair == setOf("IRR", "CAD") -> {
                    val sellerHasCAD = toCurrency == "IRR"
                    cadTrades.filter { it.selling == !sellerHasCAD && (!depositedOnly || (it.deposited == true && it.status == null)) }
                }

                pair == setOf("IRR", "USD") -> {
                    val sellerHasUSD = toCurrency == "IRR"
                    usdTrades.filter { it.selling == !sellerHasUSD && (!depositedOnly || (it.deposited == true && it.status == null)) }
                }

                else -> emptyList()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Cream40)
        ) {
            if (isSelling != null) {
                TabRow(selectedTabIndex = tabIndex, containerColor = Cream40) {
                    listOf("CAD", "USD").forEachIndexed { i, t ->
                        Tab(
                            text = { Text(t) },
                            selected = tabIndex == i,
                            onClick = { tabIndex = i })
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SettingsScreen(
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    onSave = { f, t ->
                        fromCurrency = f
                        toCurrency = t
                        onCurrencyChange(f, t)
                    })
                Row(
                    modifier = Modifier.padding(start = 16.dp).height(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = depositedOnly,
                        onCheckedChange = { depositedOnly = it },
                        modifier = Modifier.scale(.5f),
                        colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                    )
                    Text("Deposited Only", fontSize = 14.sp)
                }
            }

            // Deposited-only carousel
            val depositedTrades = tradeList.filter { it.deposited == true && it.status == null }
            DepositedCarousel(
                trades = depositedTrades,
                token = token,
                user = user,
                serviceRates = serviceRates,
                onLoginRequired = onLoginRequired,
                onLock = { trade, onSuccess ->
                    if (trade.id != null) homeViewModel.lockTrade(token!!, trade.id, onSuccess)
                },
                onConfirm = { trade, onSuccess ->
                    if (token.isNullOrEmpty()) onLoginRequired()
                    else if (trade.id != null) homeViewModel.confirmTrade(
                        token, trade.id,
                        isSelling = trade.selling,
                        amount = trade.amount ?: 0.0,
                        onSuccess = {
                            onSuccess()
                            if (trade.currency == "USD") homeViewModel.refreshUsdTrades()
                            else homeViewModel.refreshCadTrades()
                            if (isSelling == true) onSell() else onBuy()
                        }
                    )
                },
                onUnlockExpired = { trade ->
                    if (!token.isNullOrEmpty() && trade.id != null) homeViewModel.unlockTrade(
                        token,
                        trade.id
                    )
                },
                onLoadServiceRate = { trade ->
                    if (!token.isNullOrEmpty()) homeViewModel.loadServiceRate(
                        token,
                        trade.id ?: "",
                        trade.amount ?: 0.0,
                        trade.selling
                    )
                },
                publicCompliance = publicCompliance,
            )

            if (tradeList.isEmpty()) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)).background(EmptyList).padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) { Text("List is empty.", color = Color.White) }
            } else {

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    tradeList.forEach { trade ->
                        var expandedCardId by remember { mutableStateOf<String?>(null) }
//                        var isExpanded = expandedCardId == trade.id
                        TradeCard(
                            trade = trade,
                            expanded = expandedCardId == trade.id,
                            onExpanded = {
                                expandedCardId = if (expandedCardId == trade.id) null
                                else trade.id
                            },
                            watchList = watchList,
                            token = token,
                            onLoginRequired = onLoginRequired,
                            onWatch = { homeViewModel.watchTrade(token!!, trade.id ?: "") },
                            onUnwatch = { homeViewModel.unwatchTrade(token!!, trade.id ?: "") },
                            onLock = { onSuccess ->
                                if (trade.id != null) homeViewModel.lockTrade(
                                    token!!,
                                    trade.id,
                                    onSuccess
                                )
                            },
                            onConfirm = { onSuccess ->
                                if (trade.id != null) homeViewModel.confirmTrade(
                                    token!!,
                                    trade.id,
                                    isSelling = trade.selling,
                                    amount = trade.amount ?: 0.0,
                                    onSuccess = {
                                        onSuccess()
                                        if (trade.currency == "USD") homeViewModel.refreshUsdTrades()
                                        else homeViewModel.refreshCadTrades()
                                        onSell()
//                                        (if (trade.selling) onBuy else onSell)()
                                    }
                                )
                            },
                            onUnlockExpired = {
                                if (trade.id != null) homeViewModel.unlockTrade(token!!, trade.id)
                            },
                            onLoadServiceRate = {
                                if (!token.isNullOrEmpty()) homeViewModel.loadServiceRate(
                                    token,
                                    trade.id ?: "",
                                    trade.amount ?: 0.0,
                                    trade.selling
                                )
                            },
                            user = user,
                            buyingDraft = buyingDrafts.find { it.id == trade.id },
                            sellingDraft = sellingDrafts.find { it.id == trade.id },
                            walletBalance = walletStatus?.balance,
                            serviceRate = serviceRates[trade.id],
                            publicCompliance = publicCompliance,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositedCarousel(
    trades: List<TradeItem>,
    token: String?,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
    serviceRates: Map<String, Double> = emptyMap(),
    onLoginRequired: () -> Unit = {},
    onLock: (TradeItem, onSuccess: (LockedTrade) -> Unit) -> Unit = { _, _ -> },
    onConfirm: (TradeItem, onSuccess: () -> Unit) -> Unit,
    onUnlockExpired: (TradeItem) -> Unit = {},
    onLoadServiceRate: (TradeItem) -> Unit = {},
    publicCompliance: ca.arzook.shared.model.PublicCompliance? = null,
) {
    if (trades.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { trades.size })
    var dialogOpen by remember { mutableStateOf(false) }

    // Auto-scroll every 3 seconds, paused when dialog is open
    LaunchedEffect(pagerState, trades.size, dialogOpen) {
        while (true) {
            delay(3000L)
            if (!dialogOpen) {
                val next = (pagerState.currentPage + 1) % trades.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().height(190.dp).padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 48.dp),
        pageSpacing = 12.dp
    ) { page ->
        val pageOffset =
            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        val scale = 1f - (pageOffset * 0.15f).coerceAtMost(0.15f)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            colors = CardDefaults.cardColors(containerColor = Cream40),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            val trade = trades[page]
            var pendingLock by remember { mutableStateOf<LockedTrade?>(null) }
            var countdown by remember { mutableIntStateOf(0) }
            val serviceRate = serviceRates[trade.id]

            LaunchedEffect(pendingLock) {
                if (pendingLock != null) {
                    while (countdown > 0) {
                        delay(1000); countdown--
                    }
                    if (pendingLock != null) {
                        onUnlockExpired(trade); pendingLock = null; dialogOpen = false
                    }
                }
            }

            pendingLock?.let { lock ->
                val askingRate = if (trade.selling)
                    (trade.exchangeRate ?: 0.0) - (trade.serviceRate ?: 0.0)
                else
                    (trade.exchangeRate ?: 0.0) + (trade.serviceRate ?: 0.0)
                AlertDialog(
                    onDismissRequest = {
                        onUnlockExpired(trade); pendingLock = null; dialogOpen = false
                    },
                    title = { Text("Confirm ${if (trade.selling) "Buy" else "Sell"}") },
                    text = {
                        Column {
                            Text(
                                "⏱ You have $countdown seconds to confirm.",
                                fontWeight = FontWeight.Bold,
                                color = if (countdown <= 10) Color.Red else Color.Unspecified
                            )
                            Spacer(Modifier.height(8.dp))
                            if (trade.selling) {
                                Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                                Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0) + lock.complianceFee)} IRR")
                                Spacer(Modifier.height(4.dp))
                                Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                                Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency ?: "CAD"}")
                                if (lock.arzookBankInfoName != null) InfoRow(
                                    "Arzook Recipient",
                                    lock.arzookBankInfoName
                                )
                                if (lock.arzookBankInfoSheba != null) InfoRow(
                                    "Sheba",
                                    lock.arzookBankInfoSheba
                                )
                                if (lock.arzookDepositEmail != null) InfoRow(
                                    "e-Transfer to",
                                    lock.arzookDepositEmail
                                )
                                InfoRow("e-Transfer password", lock.password ?: "N/A")
                            } else {
                                Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                                Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency ?: "CAD"}")
                                lock.arzookDepositEmail?.let {
                                    InfoRow(
                                        "e-Transfer to",
                                        it
                                    )
                                }
                                InfoRow("e-Transfer password", lock.password ?: "N/A")
                                Spacer(Modifier.height(4.dp))
                                Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                                Text("${formatIrr(askingRate * (trade.amount ?: 0.0) - lock.complianceFee)} IRR")
                                InfoRow("Your Sheba", trade.sheba ?: "TBD")
                            }
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider()
                            InfoRow(
                                "1 ${trade.currency ?: "CAD"}",
                                "${formatIrr(trade.exchangeRate ?: 0.0)} IRR",
                                fontSize = 12.sp
                            )
                            if (serviceRate != null) InfoRow(
                                "Service Rate",
                                "${formatIrr(serviceRate)} IRR per ${trade.currency ?: "CAD"}",
                                fontSize = 12.sp
                            )
                            if (lock.complianceFee > 0.0) {
                                InfoRow(
                                    "Compliance Fee (\$${publicCompliance?.complianceFeeCAD ?: ""})",
                                    "${formatIrr(lock.complianceFee)} IRR",
                                    fontSize = 12.sp
                                )
                                Text(
                                    "The Compliance Fee applies to all transactions as part of our regulatory compliance measures",
                                    fontSize = 10.sp
                                )
                            }
                            if (lock.holdTransactionFee > 0) InfoRow(
                                "Transaction fee",
                                "${formatIrr(lock.holdTransactionFee.toDouble())} IRR",
                                fontSize = 12.sp
                            )
                        }
                    },
                    confirmButton = {
                        ArzookButton(
                            onClick = {
                                pendingLock = null; dialogOpen = false; onConfirm(
                                trade
                            ) {}
                            },
                            containerColor = GreenSold,
                            contentColor = Color.White,
                            modifier = Modifier
                        ) { Text("Confirm") }
                    },
                    dismissButton = {
                        ArzookButton(
                            onClick = {
                                onUnlockExpired(trade); pendingLock = null; dialogOpen = false
                            },
                            containerColor = Brown,
                            contentColor = Color.White,
                            modifier = Modifier
                        ) { Text("Cancel") }
                    }
                )
            }

            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                InfoRow(
                    "Amount", " ${formatIrr(trade.amount ?: 0.0)} ${trade.currency ?: ""}",
                )
                InfoRow(
                    "Rate",
                    "${formatIrr(trade.exchangeRate ?: 0.0)} IRR",
                    fontSize = 12.sp
                )
                InfoRow(
                    "Total",
                    " ${trade.total ?: "${formatIrr((trade.amount ?: 0.0) * ((trade.exchangeRate ?: 0.0) - (trade.serviceRate ?: 0.0)))} IRR"}",
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                ArzookButton(
                    onClick = {
                        if (token.isNullOrEmpty()) onLoginRequired()
                        else {
                            onLoadServiceRate(trade)
                            onLock(trade) { lock ->
                                pendingLock = lock; countdown = lock.autoLockExpiresIn; dialogOpen =
                                true
                            }
                        }
                    },
                    containerColor = Green,
                    contentColor = Color.White
                ) {
                    Text(
                        if (!trade.selling) "Sell" else if (trade.urgent == true)
                            "Buy 🔥" else "Buy", color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
//                }
            }
        }
    }
}

@Composable
private fun TradeCard(
    trade: TradeItem,
    expanded: Boolean,
    onExpanded: () -> Unit,
    watchList: List<WatchItem>,
    token: String?,
    onLoginRequired: () -> Unit,
    onWatch: () -> Unit,
    onUnwatch: () -> Unit,
    onLock: (onSuccess: (LockedTrade) -> Unit) -> Unit = {},
    onConfirm: (onSuccess: () -> Unit) -> Unit = {},
    onUnlockExpired: () -> Unit = {},
    onLoadServiceRate: () -> Unit = {},
    user: ca.arzook.shared.model.AuthenticatedData? = null,
    buyingDraft: TradeItem? = null,
    sellingDraft: TradeItem? = null,
    walletBalance: Long? = null,
    serviceRate: Double? = null,
    publicCompliance: ca.arzook.shared.model.PublicCompliance? = null,
) {
    val isWatched = watchList.any { it.offeringId == trade.id }
    val isSoldOut = trade.status != null
    val isDeposited = trade.deposited == true
    val textColor = Color.Black
    val subTextColor = Color.Black
    var showBalanceAlert by remember { mutableStateOf(false) }
    var pendingLock by remember { mutableStateOf<LockedTrade?>(null) }
    var countdown by remember { mutableStateOf(0) }
    val askingRate = if (trade.selling) ((trade.exchangeRate ?: 0.0) + (serviceRate
        ?: 0.0)) else ((trade.exchangeRate ?: 0.0) - (serviceRate ?: 0.0))
    println("[rate]: $askingRate")


    // Countdown timer when lock dialog is showing
    LaunchedEffect(pendingLock) {
        val lock = pendingLock ?: return@LaunchedEffect
        print("[onLock] id = ${lock.id}")
        countdown = lock.autoLockExpiresIn
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        // Expired — unlock and dismiss
        if (pendingLock != null && !token.isNullOrEmpty() && trade.id != null) {
            print("[onDismiss] id = ${trade.id}")
            onUnlockExpired()
        }
        pendingLock = null
    }

    pendingLock?.let { lock ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                print("[onDismiss] id = ${trade.id}")
                if (!token.isNullOrEmpty() && trade.id != null) onUnlockExpired()
                pendingLock = null
            },
            title = { Text("Confirm ${if (trade.selling) "Buy" else "Sell"}") },
            text = {
                Column {
                    Text(
                        "⏱ You have $countdown seconds to confirm.", fontWeight = FontWeight.Bold,
                        color = if (countdown <= 10) Color.Red else Color.Unspecified
                    )
                    Spacer(Modifier.height(8.dp))
                    if (trade.selling) {
                        // Buying flow - user sends IRR, gets CAD
                        Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                        Text("${formatIrr(askingRate * (trade.amount ?: 0.0) + lock.complianceFee)} IRR")
                        if (lock.arzookBankInfoName != null)
                            InfoRow("Arzook Recipient", lock.arzookBankInfoName)
                        if (lock.arzookBankInfoSheba != null) {
                            InfoRow("Sheba", lock.arzookBankInfoSheba)
                            InfoRow("Deposit Id (شناسه واریز)", user?.customerDepositId.toString())
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                        Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency ?: "CAD"}")
                        InfoRow(
                            "e-Transfer to",
                            user?.email.toString()
                        )
                        InfoRow("e-Transfer password", lock.password ?: "N/A")
                    } else {
                        // Selling flow - user sends CAD, gets IRR
                        Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                        Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency ?: "CAD"}")
                        lock.arzookDepositEmail?.let {
                            InfoRow(
                                "e-Transfer to",
                                it
                            )
                        }
                        InfoRow("e-Transfer password", lock.password ?: "N/A")

                        Spacer(Modifier.height(4.dp))
                        Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                        Text("${formatIrr(askingRate * (trade.amount ?: 0.0) - lock.complianceFee)} IRR")
                        InfoRow("Your Sheba", trade.sheba ?: "TBD")
                    }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    InfoRow(
                        "1 ${trade.currency ?: "CAD"}",
                        "${formatIrr(trade.exchangeRate ?: 0.0)} IRR"
                    )
                    if (serviceRate != null)
                        InfoRow(
                            "Service Rate",
                            "${formatIrr(serviceRate)} IRR per ${trade.currency ?: "CAD"}"
                        )
                    if (!trade.selling) {
                        InfoRow(
                            "Total Amount",
                            "${formatIrr((askingRate) * (trade.amount ?: 0.0))} IRR"
                        )
                    }
                    if (lock.complianceFee > 0.0) {
                        InfoRow(
                            "Compliance Fee (\$${publicCompliance?.complianceFeeCAD ?: ""})",
                            "${formatIrr(lock.complianceFee)} IRR"
                        )
                        if (!trade.selling) {
                            val netPayout = askingRate * (trade.amount ?: 0.0) - lock.complianceFee
                            InfoRow("Net Payout", "${formatIrr(netPayout)} IRR")
                        } else {
                            val netPayout = askingRate * (trade.amount ?: 0.0) + lock.complianceFee
                            InfoRow("Net Payout", "${formatIrr(netPayout)} IRR")
                        }
                        Text(
                            "The Compliance Fee applies to all transactions as part of our regulatory compliance measures",
                            fontSize = 10.sp
                        )
                    }
                    if (lock.holdTransactionFee > 0)
                        InfoRow(
                            "Transaction fee",
                            "${formatIrr(lock.holdTransactionFee.toDouble())} IRR"
                        )
                }
            },
            confirmButton = {
                print("[onConfirm] id = ${trade.id}")
                ArzookButton(
                    modifier = Modifier,
                    onClick = {
                        pendingLock = null
                        onConfirm {}
                    },
                    contentColor = Color.White,
                    containerColor = GreenSold
                ) { Text("Confirm") }
            },
            dismissButton = {
                ArzookButton(
                    modifier = Modifier,
                    onClick = {
                        if (!token.isNullOrEmpty() && trade.id != null) onUnlockExpired()
                        pendingLock = null
                    },
                    contentColor = Color.White,
                    containerColor = Brown
                ) { Text("Cancel") }
            }
        )
    }

    if (showBalanceAlert) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBalanceAlert = false },
            title = { Text("Insufficient Wallet Balance") },
            text = {
                Text(
                    "Required: ${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR\n" +
                            "Available: ${formatIrr(walletBalance?.toDouble() ?: 0.0)} IRR\n\n" +
                            "Please top up your wallet before proceeding."
                )
            },
            confirmButton = {
                ArzookButton(
                    onClick = { showBalanceAlert = false },
                    containerColor = GreenSold
                ) { Text("OK") }
            }
        )
    }

    LaunchedEffect(expanded) {
        if (expanded && !token.isNullOrEmpty()) onLoadServiceRate()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.clickable {
            onExpanded()
        }
            .background(Cream40)
            .padding(12.dp)) {
            // Collapsed header
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${formatCad(trade.amount ?: 0.0)} ${trade.currency}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                            if (trade.urgent == true && isDeposited && !isSoldOut)
                                Text(" 🔥", fontSize = 16.sp)
                        }
                        Text(
                            "${formatIrr(trade.exchangeRate ?: 0.0)} IRR",
                            fontSize = 13.sp,
                            color = subTextColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Total", fontSize = 12.sp, color = textColor)
                        Text(
                            "${formatIrr((trade.amount ?: 0.0) * (askingRate))} IRR",
                            fontWeight = FontWeight.SemiBold,
                            color = subTextColor,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textColor
                    )
                }
                // The Banner positioned at the top left
                StatusBanner(
                    statusText = (if (trade.status != null) trade.status.title else if (trade.deposited == true) if (trade.selling) "Buy" else "Sell" else "Watch").toString(),
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 14.dp, y = (-14).dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    val calculatedComplianceFee = if (trade.complianceFee > 0.0) trade.complianceFee
                    else if (publicCompliance != null) {
                        val raw = publicCompliance.complianceFeeCAD * (trade.exchangeRate ?: 0.0)
                        val rounding = publicCompliance.complianceFeeRounding
                        if (rounding > 0) (kotlin.math.round(raw / rounding) * rounding) else raw
                    } else 0.0
                    if (isSoldOut) {
                        // Sold/Bought — bold status label, no button
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = trade.status!!.title.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = GreenSold
                        )
                        Spacer(Modifier.height(8.dp))
//                        TradeInfoRows(trade = trade, textColor = textColor, subTextColor = subTextColor)
                    } else if (isDeposited) {
                        // Ready to buy/sell — BuySellSheet style inline
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        TradeInfoRows(
                            trade = trade,
                            subTextColor = subTextColor,
                            user = user,
                            buyingDraft = buyingDraft,
                            sellingDraft = sellingDraft,
                            askingRate = askingRate,
                            calculatedComplianceFee = calculatedComplianceFee
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (calculatedComplianceFee > 0.0) {
                                    var showComplianceInfo by remember { mutableStateOf(false) }
                                    if (showComplianceInfo) {
                                        AlertDialog(
                                            onDismissRequest = { showComplianceInfo = false },
                                            text = { Text("The Compliance Fee applies to all transactions as part of our regulatory compliance measures") },
                                            confirmButton = {
                                                ArzookButton(onClick = {
                                                    showComplianceInfo = false
                                                }, containerColor = GreenSold) { Text("OK") }
                                            }
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 0.dp)
                                    ) {
                                        InfoRow(
                                            "Compliance fee (\$${publicCompliance?.complianceFeeCAD ?: ""})",
                                            "IRR ${formatIrr(calculatedComplianceFee)}",
                                            fontSize = 12.sp
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Help,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                                .clickable { showComplianceInfo = true }
                                        )
                                    }
                                }
                                InfoRow(
                                    "Service Rate",
                                    "${serviceRate?.let { formatIrr(it) }} IRR per ${trade.currency}",
                                    fontSize = 12.sp
                                )
                                InfoRow(
                                    "1 ${trade.currency}",
                                    "${formatIrr(trade.exchangeRate ?: 0.0)} IRR",
                                    fontSize = 12.sp
                                )
//                                val cardComplianceFee = if (trade.complianceFee > 0.0) trade.complianceFee
//                                    else if (publicCompliance != null && askingRate > 0.0) {
//                                        val raw = publicCompliance.complianceFeeCAD * askingRate
//                                        val rounding = publicCompliance.complianceFeeRounding
//                                        if (rounding > 0) (kotlin.math.round(raw / rounding) * rounding) else raw
//                                    } else 0.0
//                                if (cardComplianceFee > 0.0) {
//                                    InfoRow(
//                                        "Compliance Fee (\$${publicCompliance?.complianceFeeCAD ?: ""})",
//                                        "${formatIrr(cardComplianceFee)} IRR",
//                                        fontSize = 12.sp
//                                    )
//                                }
                            }
                            ArzookButton(
                                onClick = {
                                    if (token.isNullOrEmpty()) onLoginRequired()
                                    else if (trade.urgent == true && walletBalance != null && walletBalance < (trade.exchangeRate
                                            ?: 0.0) * (trade.amount ?: 0.0)
                                    ) showBalanceAlert = true
                                    else onLock { lock -> pendingLock = lock }
                                },
                                containerColor = GreenSold,
                                contentColor = Color.White,
                                modifier = Modifier
                            ) {
                                Text(
                                    if (trade.selling) "Buy" else "Sell",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Watch — inline info + watch/unwatch button
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(EmptyList)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (trade.selling)
                                    "By watching, let the Seller know of your interest anonymously, and get notified when the Selling e-Transfer received."
                                else
                                    "By watching, let the Buyer know of your interest anonymously, and get notified when the Buying is deposited.",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        WatchSheet(trade = trade, askingRate, calculatedComplianceFee)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (calculatedComplianceFee > 0.0) {
                                    var showComplianceInfo by remember { mutableStateOf(false) }
                                    if (showComplianceInfo) {
                                        AlertDialog(
                                            onDismissRequest = { showComplianceInfo = false },
                                            text = { Text("The Compliance Fee applies to all transactions as part of our regulatory compliance measures") },
                                            confirmButton = {
                                                ArzookButton(onClick = {
                                                    showComplianceInfo = false
                                                }, containerColor = GreenSold) { Text("OK") }
                                            }
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 0.dp)
                                    ) {
                                        InfoRow(
                                            "Compliance fee (\$${publicCompliance?.complianceFeeCAD ?: ""})",
                                            "IRR ${formatIrr(calculatedComplianceFee)}",
                                            fontSize = 12.sp
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Help,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                                .clickable { showComplianceInfo = true }
                                        )
                                    }
                                }
                                InfoRow(
                                    "Service Rate",
                                    "${serviceRate?.let { formatIrr(it) }} IRR per ${trade.currency}",
                                    fontSize = 12.sp
                                )
                                InfoRow(
                                    "1 ${trade.currency}",
                                    "${formatIrr(trade.exchangeRate ?: 0.0)} IRR",
                                    fontSize = 12.sp
                                )
                            }
                            ArzookButton(
                                onClick = {
                                    if (token.isNullOrEmpty()) onLoginRequired()
                                    else if (isWatched) onUnwatch()
                                    else onWatch()
                                },
                                modifier = Modifier
                            ) {
                                Text(
                                    if (isWatched) "Unwatch" else "Watch",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeInfoRows(
    trade: TradeItem,
    subTextColor: Color,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
    buyingDraft: TradeItem? = null,
    sellingDraft: TradeItem? = null,
    askingRate: Double,
    calculatedComplianceFee: Double
) {
    if (trade.selling) {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text(
            "${formatIrr((trade.amount ?: 0.0) * askingRate + calculatedComplianceFee)} IRR",
            color = subTextColor
        )
        Text("Arzook Recipient: ${buyingDraft?.arzookBankInfoName}", color = subTextColor)
        Text("Sheba: ${buyingDraft?.arzookBankInfoSheba}", color = subTextColor)
        if (user != null) Text(
            "Deposit Id (شناسه واریز): ${user.customerDepositId}",
            color = subTextColor
        )
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}", color = subTextColor)
        if (user != null) Text(
            "Recipient: ${user.lastName}, ${user.firstName}",
            color = subTextColor
        )
        if (user != null) Text("e-Transfer to:: ${user.email}", color = subTextColor)
        if (user != null) Text(
            "e-Transfer password: ${sellingDraft?.eTransferPassword ?: "N/A"}",
            color = subTextColor
        )
    } else {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}", color = subTextColor)
        Text("e-Transfer details: 'TBD'", color = subTextColor)
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text(
            "${formatIrr((trade.amount ?: 0.0) * askingRate - calculatedComplianceFee)} IRR",
            color = subTextColor
        )
        Text("Your Sheba: 'TBD'", color = subTextColor)
    }
}

@Composable
private fun WatchSheet(
    trade: TradeItem,
    askingRate: Double,
    calculatedComplianceFee: Double
) {
    if (trade.selling) {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text("${formatIrr((askingRate) * (trade.amount ?: 0.0) + calculatedComplianceFee)} IRR")
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
    } else {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text("${formatIrr((askingRate) * (trade.amount ?: 0.0) - calculatedComplianceFee)} IRR")
    }
}

@Composable
fun StatusBanner(statusText: String, modifier: Modifier = Modifier) {
    val backgroundColor = when (statusText) {
        "Watch" -> Yellow40
        "Sell", "Buy" -> GreenSold
        else -> ChosenMenu
    }//if (inStock) Color(0xFF4CAF50) else Color.Red // Green if in stock, Red if out
//    val statusText = if (inStock) "IN STOCK" else "OUT OF STOCK"

    Surface(
        color = backgroundColor,
        contentColor = if (statusText == "Watch") Color.Black else Color.White,
        shape = RoundedCornerShape(bottomStart = 8.dp), // Optional: rounds only one corner
        modifier = modifier
    ) {
        Text(
            text = statusText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)//, vertical = 4.dp)
        )
    }
}
