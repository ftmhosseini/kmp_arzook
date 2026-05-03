package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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

private val greenSold = Color(0xFF4CAF50)
private val brown = Color(0xFF8B4513)
private val green = Color(0xFF1B5E20)
private val blue20 = Color(0xFF1e3957)//Color(0xFF1e3957)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
    title: String,
    isSelling: Boolean,
    token: String?,
    onLoginRequired: () -> Unit,
    onSell: () -> Unit = {},
    onBuy: () -> Unit = {},
    homeViewModel: HomeViewModel,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
) {
    val cadTrades by homeViewModel.cadTrades.collectAsState()
    val usdTrades by homeViewModel.usdTrades.collectAsState()
    val watchList by homeViewModel.watchList.collectAsState()
    val buyingDrafts by homeViewModel.buyingDrafts.collectAsState()
    val sellingDrafts by homeViewModel.sellingDrafts.collectAsState()
    val lockedTrades by homeViewModel.lockedTrades.collectAsState()
    val serviceRates by homeViewModel.serviceRates.collectAsState()
    val walletStatus by homeViewModel.walletStatus.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    val rawList = if (tabIndex == 0) cadTrades else usdTrades
    val tradeList = rawList.filter { it.selling == isSelling && (!depositedOnly || it.deposited == true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
    Column(
            modifier = Modifier.fillMaxSize().background(Cream40).padding(padding)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                listOf("CAD", "USD").forEachIndexed { i, t ->
                    Tab(text = { Text(t) }, selected = tabIndex == i, onClick = { tabIndex = i })
                }
            }

            Row(modifier = Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = depositedOnly, onCheckedChange = { depositedOnly = it })
                Text(" Deposited Only", fontSize = 14.sp)
            }

            if (tradeList.isEmpty()) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)).background(blue20).padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) { Text("List is empty.", color = Color.White) }
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    tradeList.forEach { trade ->
                        TradeCard(
                            trade = trade,
                            watchList = watchList,
                            token = token,
                            onLoginRequired = onLoginRequired,
                            onWatch = { homeViewModel.watchTrade(token!!, trade.id ?: "") },
                            onUnwatch = { homeViewModel.unwatchTrade(token!!, trade.id ?: "") },
                            onLock = { onSuccess ->
                                if (trade.id != null) homeViewModel.lockTrade(token!!, trade.id, onSuccess)
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
                                        (if (trade.selling) onBuy else onSell)()
                                    }
                                )
                            },
                            onUnlockExpired = {
                                if (trade.id != null) homeViewModel.unlockTrade(token!!, trade.id)
                            },
                            onLoadServiceRate = { if (!token.isNullOrEmpty()) homeViewModel.loadServiceRate(token, trade.id ?: "", trade.amount ?: 0.0, trade.selling) },
                            user = user,
                            buyingDraft = buyingDrafts.find { it.id == trade.id },
                            sellingDraft = sellingDrafts.find { it.id == trade.id },
//                            lockedTrade = lockedTrades[trade.id],
                            serviceRate = serviceRates[trade.id],
                            walletBalance = walletStatus?.balance,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = scope
                        )
                        Spacer(Modifier.height(8.dp))
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
    lockedTrade: LockedTrade? = null,
    serviceRate: Double? = null,
    walletBalance: Int? = null,
    snackbarHostState: SnackbarHostState? = null,
    coroutineScope: CoroutineScope? = null,
) {
    val repo = remember { ArzookRepositoryImpl("https://api.arzook.ca") }
    val isWatched = watchList.any { it.offeringId == trade.id }
    val isSoldOut = trade.status != null
    val isDeposited = trade.deposited == true
    val cardColor = when {
        isSoldOut -> green
        isDeposited -> greenSold
        else -> Cream40
    }
    val textColor = if (isSoldOut || isDeposited) Color.White else Color.Unspecified
    val subTextColor = if (isSoldOut || isDeposited) Color.White.copy(alpha = 0.8f) else Color.DarkGray

    var expanded by remember { mutableStateOf(false) }
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
                    Text("⏱ You have $countdown seconds to confirm.", fontWeight = FontWeight.Bold,
                        color = if (countdown <= 10) Color.Red else Color.Unspecified)
                    Spacer(Modifier.height(8.dp))
                    if (lock.arzookDepositEmail != null)
                        Text("Deposit e-Transfer to: ${lock.arzookDepositEmail}")
                    if (lock.arzookBankInfoName != null)
                        Text("Bank: ${lock.arzookBankInfoName}")
                    if (lock.arzookBankInfoSheba != null)
                        Text("Sheba: ${lock.arzookBankInfoSheba}")
                    if (lock.holdTransactionFee > 0)
                        Text("Transaction fee: ${formatIrr(lock.holdTransactionFee.toDouble())} IRR")
                }
            },
            confirmButton = {
                print("[onConfirm] id = ${trade.id}")
                Button(onClick = {
                    pendingLock = null
                    onConfirm {}
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (!token.isNullOrEmpty() && trade.id != null) onUnlockExpired()
                    pendingLock = null
                }) { Text("Cancel") }
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
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.clickable { expanded = !expanded }.padding(12.dp)) {
            // Collapsed header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        if (trade.urgent == true && isDeposited && !isSoldOut)
                            Text(" 🔥", fontSize = 16.sp)
                    }
                    Text("${formatIrr(trade.exchangeRate ?: 0.0)} IRR", fontSize = 13.sp, color = subTextColor)
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Total", fontSize = 12.sp, color = textColor)
                    Text("${formatIrr((trade.amount ?: 0.0) * (trade.exchangeRate ?: 0.0))} IRR", fontWeight = FontWeight.SemiBold, color = subTextColor, fontSize = 14.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor
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
                            color = Color(0xFF76FF03)
                        )
                        Spacer(Modifier.height(8.dp))
//                        TradeInfoRows(trade = trade, textColor = textColor, subTextColor = subTextColor)
                    } else if (isDeposited) {
                        // Ready to buy/sell — BuySellSheet style inline
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        TradeInfoRows(trade = trade, textColor = textColor, subTextColor = subTextColor, user = user, buyingDraft = buyingDraft, sellingDraft = sellingDraft, lockedTrade = lockedTrade)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("1 ${trade.currency} = ${formatIrr(trade.exchangeRate ?: 0.0)} IRR", color = subTextColor, fontSize = 12.sp)
                                if (serviceRate != null)
                                    Text("Service Rate: ${formatIrr(serviceRate)} IRR per ${trade.currency}", color = subTextColor, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    if (token.isNullOrEmpty()) onLoginRequired()
                                    else if (trade.urgent == true && walletBalance != null && walletBalance < (trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0)) showBalanceAlert = true
                                    else onLock { lock -> pendingLock = lock }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp)
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
                                .background(Color(0xFF1e3957))
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("1 ${trade.currency} = ${formatIrr(trade.exchangeRate ?: 0.0)} IRR", color = subTextColor, fontSize = 12.sp)
                                if (serviceRate != null)
                                    Text("Service Rate: ${formatIrr(serviceRate)} IRR per ${trade.currency}", color = subTextColor, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    if (token.isNullOrEmpty()) onLoginRequired()
                                    else if (isWatched) onUnwatch()
                                    else onWatch()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWatched) Color(0xFFEF6C00) else Color(0xFFFF9800)
                                ),
                                shape = RoundedCornerShape(8.dp)
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
    textColor: Color,
    subTextColor: Color,
    user: ca.arzook.shared.model.AuthenticatedData? = null,
    buyingDraft: TradeItem? = null,
    sellingDraft: TradeItem? = null,
    lockedTrade: LockedTrade? = null,
) {
    if (trade.selling) {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = brown)
        Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR", color = subTextColor)
        Text("Arzook Recipient: ${buyingDraft?.arzookBankInfoName}", color = subTextColor)
        Text("Sheba: ${buyingDraft?.arzookBankInfoSheba}", color = subTextColor)
        if (user != null) Text("Deposit Id (شناسه واریز): ${user.customerDepositId}", color = subTextColor)
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = green)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}", color = subTextColor)
        if (user != null) Text("Recipient: ${user.lastName}, ${user.firstName}", color = subTextColor)
        if (user != null) Text("e-Transfer to:: ${user.email}", color = subTextColor)
        if (user != null) Text("e-Transfer password: ${sellingDraft?.eTransferPassword ?: "N/A"}", color = subTextColor)
    } else {
        Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = brown)
        Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}", color = subTextColor)
        Text("e-Transfer details: 'TBD'", color = subTextColor)
        Spacer(Modifier.height(12.dp))
        Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = green)
        Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR", color = subTextColor)
        Text("Your Sheba: 'TBD'", color = subTextColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchSheet(
    trade: TradeItem,
) {
            if (trade.selling) {
                Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = brown)
                Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR")
                Spacer(Modifier.height(12.dp))
                Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = green)
                Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
            } else {
                Text("YOU SEND", fontWeight = FontWeight.ExtraBold, color = brown)
                Text("${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
                Spacer(Modifier.height(12.dp))
                Text("YOU GET", fontWeight = FontWeight.ExtraBold, color = green)
                Text("${formatIrr((trade.exchangeRate ?: 0.0) * (trade.amount ?: 0.0))} IRR")
            }
}
