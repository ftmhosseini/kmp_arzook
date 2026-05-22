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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.model.LockedTrade
import kotlinx.coroutines.CoroutineScope
import ca.arzook.shared.model.WatchItem
import ca.arzook.shared.repository.ArzookRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
//    title: String,
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
//    val lockedTrades by homeViewModel.lockedTrades.collectAsState()
    val serviceRates by homeViewModel.serviceRates.collectAsState()
    val walletStatus by homeViewModel.walletStatus.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()

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
    var settingEnabaled by remember { mutableStateOf(true) }


    val scrollState = rememberScrollState()

    val pair = setOf(fromCurrency, toCurrency)
    val tradeList: List<TradeItem> = when {
        // Buying/Selling screens: isSelling provided, use CAD tab (tabIndex) filter
        isSelling != null -> {
            val rawList = if (tabIndex == 0) cadTrades else usdTrades
            rawList.filter { it.selling == isSelling && (!depositedOnly || (it.deposited == true && it.status == null)) }
        }
        // Offering screen: currency-pair driven
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
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
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsScreen(
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    onSave = { f, t ->
                        fromCurrency = f;
                        toCurrency = t;
                        onCurrencyChange(f, t)
                    })
                Row(
                    modifier = Modifier.padding(start = 16.dp).height(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = depositedOnly,
                        onCheckedChange = { depositedOnly = it },
                        modifier = Modifier.scale(0.5f),
                        colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                    )
                    Text("Deposited Only", fontSize = 14.sp)
                }
            }

            // Deposited-only carousel
            val depositedTrades = tradeList.filter { it.deposited == true && it.status == null }
            DepositedCarousel(
                trades = depositedTrades,
//                onLock = { trade ->
//                    if (token.isNullOrEmpty()) onLoginRequired()
//                    else if (trade.id != null) homeViewModel.lockTrade(token, trade.id) {if (isSelling == true) onSell() else onBuy()}
//                },
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
                }
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
                        var isExpanded = expandedCardId == trade.id
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
//    onAction: () -> Unit,
//    onLock: (TradeItem) -> Unit,
    onConfirm: (TradeItem, onSuccess: () -> Unit) -> Unit
) {
    if (trades.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { trades.size })

    // Auto-scroll every 3 seconds (full cycle ~1 min for 20 items)
    LaunchedEffect(pagerState, trades.size) {
        while (true) {
            delay(3000L)
            val next = (pagerState.currentPage + 1) % trades.size
            pagerState.animateScrollToPage(next)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().height(160.dp).padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 48.dp),
        pageSpacing = 12.dp
    ) { page ->
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
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
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Amount: ${formatIrr(trade.amount ?: 0.0)} ${trade.currency ?: ""}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Rate: ${trade.askingRate ?: trade.exchangeRate ?: "-"}", fontSize = 12.sp)
                Text("Total: ${trade.total ?: "${formatIrr((trade.amount ?: 0.0) * (trade.exchangeRate ?: 0.0))} IRR"}", fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { },//onConfirm(trade) {} 
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text(if (!trade.selling) "Sell" else if (trade.urgent == true)
                            "Buy 🔥" else "Buy", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
//    lockedTrade: LockedTrade? = null,
    serviceRate: Double? = null,
//    snackbarHostState: SnackbarHostState? = null,
//    coroutineScope: CoroutineScope? = null,
) {
//    val repo = remember { ArzookRepositoryImpl("https://api.arzook.ca") }
    val isWatched = watchList.any { it.offeringId == trade.id }
    val isSoldOut = trade.status != null
    val isDeposited = trade.deposited == true
//    val cardColor = when {
//        isSoldOut -> Color.White//green
//        isDeposited -> Color.White//greenSold
//        else -> Color.White//Cream40
//    }
    val textColor = Color.Black//if (isSoldOut || isDeposited) Color.White else Color.Unspecified
    val subTextColor =
        Color.Black//if (isSoldOut || isDeposited) Color.White.copy(alpha = 0.8f) else Color.DarkGray

//    var expanded by remember { mutableStateOf(false) }
    var showBalanceAlert by remember { mutableStateOf(false) }
    var pendingLock by remember { mutableStateOf<LockedTrade?>(null) }
    var countdown by remember { mutableStateOf(0) }

    // Countdown timer when lock dialog is showing
    LaunchedEffect(pendingLock) {
        val lock = pendingLock ?: return@LaunchedEffect
        print("[onLock] id = ${lock.id}")
        countdown = lock.autoLockExpiresIn
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000)
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
                    if (lock.arzookDepositEmail != null)
                        Text("Deposit e-Transfer to: ${lock.arzookDepositEmail}")
                    if (lock.arzookBankInfoName != null)
                        Text("Bank: ${lock.arzookBankInfoName}")
                    if (lock.arzookBankInfoSheba != null)
                        Text("Sheba: ${lock.arzookBankInfoSheba}")
                    if (lock.holdTransactionFee > 0)
                        Text("Transaction fee: ${formatIrr(lock.holdTransactionFee.toDouble())} IRR")
                    if (lock.complianceFee > 0.0) {
                        Text("Compliance Fee ($5): ${formatIrr(lock.complianceFee)}")
                        Text(
                            "The Compliance Fee applies to all transactions as part of our regulatory compliance measures",
                            fontSize = 10.sp
                        )
                    }


                }
            },
            confirmButton = {
                print("[onConfirm] id = ${trade.id}")
                Button(
                    onClick = {
                        pendingLock = null
                        onConfirm {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSold
                    )
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!token.isNullOrEmpty() && trade.id != null) onUnlockExpired()
                        pendingLock = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    )
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
                TextButton(onClick = { showBalanceAlert = false }) { Text("OK") }
            }
        )
    }

    LaunchedEffect(expanded) {
        if (expanded && !token.isNullOrEmpty()) onLoadServiceRate()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.clickable {
//            expanded = !expanded;
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
                            "${formatIrr((trade.amount ?: 0.0) * (trade.exchangeRate ?: 0.0))} IRR",
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
//                            textColor = textColor,
//                            lockedTrade = lockedTrade
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
//                                Text("1 ${trade.currency} = ${formatIrr(trade.exchangeRate ?: 0.0)} IRR", color = subTextColor, fontSize = 12.sp)
//                                if (serviceRate != null)
//                                    Text("Service Rate: ${formatIrr(serviceRate)} IRR per ${trade.currency}", color = subTextColor, fontSize = 12.sp)
                                InfoRow(
                                    "Service Rate",
                                    "IRR ${serviceRate?.let { formatIrr(it) }} per ${trade.currency}",
                                    fontSize = 12.sp
                                )
                                InfoRow(
                                    "1 ${trade.currency}",
                                    "IRR ${formatIrr(trade.exchangeRate ?: 0.0)}",
                                    fontSize = 12.sp
                                )
                                if (trade.complianceFee > 0.0) {
                                    var showComplianceInfo by remember { mutableStateOf(false) }
                                    if (showComplianceInfo) {
                                        AlertDialog(
                                            onDismissRequest = { showComplianceInfo = false },
                                            text = { Text("The Compliance Fee applies to all transactions as part of our regulatory compliance measures") },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    showComplianceInfo = false
                                                }) { Text("OK") }
                                            }
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        InfoRow(
                                            "Compliance fee ($5)",
                                            "IRR ${formatIrr(trade.complianceFee)}"
                                        )
                                        IconButton(onClick = { showComplianceInfo = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Help,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
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
                        WatchSheet(trade = trade)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
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
                                if (trade.complianceFee != null && trade.complianceFee.toDouble() > 0.0) {
                                    var showComplianceInfo by remember { mutableStateOf(false) }
                                    if (showComplianceInfo) {
                                        AlertDialog(
                                            onDismissRequest = { showComplianceInfo = false },
                                            text = { Text("The Compliance Fee applies to all transactions as part of our regulatory compliance measures") },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    showComplianceInfo = false
                                                }) { Text("OK") }
                                            }
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        InfoRow(
                                            "Compliance fee ($5)",
                                            "${formatIrr(trade.complianceFee)} IRR"
                                        )
                                        IconButton(onClick = { showComplianceInfo = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Help,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
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
//    textColor: Color,
    subTextColor: Color,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
    buyingDraft: TradeItem? = null,
    sellingDraft: TradeItem? = null,
//    lockedTrade: LockedTrade? = null,
) {
    if (trade.selling) {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text(
            "${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR",
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
            "${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR",
            color = subTextColor
        )
        Text("Your Sheba: 'TBD'", color = subTextColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchSheet(
    trade: TradeItem,
) {
    if (trade.selling) {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR")
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
    } else {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = Brown)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = Green)
        Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR")
    }
}

@Composable
fun StatusBanner(statusText: String, modifier: Modifier = Modifier) {
    val backgroundColor =
        ChosenMenu//if (inStock) Color(0xFF4CAF50) else Color.Red // Green if in stock, Red if out
//    val statusText = if (inStock) "IN STOCK" else "OUT OF STOCK"

    Surface(
        color = backgroundColor,
        contentColor = Color.White,
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
