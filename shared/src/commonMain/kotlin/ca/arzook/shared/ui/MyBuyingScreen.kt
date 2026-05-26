package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.TradeItem
import kotlin.math.abs

@Composable
fun MyBuyingScreen(
    drafts: List<TradeItem>,
    completedTrades: List<TradeItem>,
    onAddBuying: () -> Unit = {},
    token: String = "",
    homeViewModel: HomeViewModel? = null,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deleteKey by remember { mutableIntStateOf(0) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(Cream40)
                .verticalScroll(scrollState)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("My Buying") })
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Completed") })
            }

            when (tabIndex) {
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        ArzookButton(
                            onClick = onAddBuying,
                            modifier = Modifier
                        ) { Text("Add new buying") }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    if (drafts.isEmpty()) {
                        EmptyListMessage("No buying drafts.")
                    } else {
                        drafts.forEach {
                            BuyingDraftRow(
                                draft = it,
                                token = token,
                                homeViewModel = homeViewModel,
                                key = deleteKey,
                                onDeleted = {
                                    deleteKey++
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Buying deleted successfully")
                                    }
                                }
                            )
                        }
                    }
                }

                1 -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    if (completedTrades.isEmpty()) {
                        EmptyListMessage("No completed buying trades.")
                    } else {
                        completedTrades.forEach { CompletedBuyingRow(it, token) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuyingDraftRow(
    draft: TradeItem,
    token: String = "",
    homeViewModel: HomeViewModel? = null,
    key: Int = 0,
    onDeleted: () -> Unit = {}
) {
    var expanded by remember(key) { mutableStateOf(false) }
    var advertised by remember { mutableStateOf(draft.advertised == true) }
    var smartMatching by remember { mutableStateOf(draft.smartMatchingEnabled == true) }
    val scope = rememberCoroutineScope()
    val status = when {
        draft.deposited == null -> "e-Wallet Balance"
        draft.sellingCode == null -> "Pending Seller"
        draft.eTransferForwardedDate == null -> "e-Transfer"
        else -> "e-Transfer Deposit"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(
                vertical = 8.dp, horizontal = 16.dp
            )
            .background(Cream80)
    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { expanded = !expanded }
//                .padding(4.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR",
                    modifier = Modifier.weight(1.5f),
                    fontSize = 12.sp
                )
                Column(
                    modifier = Modifier.height(IntrinsicSize.Min).weight(1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(status, color = Brown, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    HorizontalDivider(thickness = 2.dp, color = Brown)
                    if (draft.locked == true && draft.lockExpiresIn != null) {
                        Text(
                            text = "${draft.lockExpiresIn}s",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    "${formatCad(draft.amount ?: 0.0)} ${draft.currency}",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
//            Icon(
//                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
//                contentDescription = null
//            )
//        }

        AnimatedVisibility(visible = expanded) {
            var editAmount by remember { mutableStateOf((draft.amount ?: 0.0)) }
            var editRate by remember { mutableStateOf((draft.askingRate ?: 0.0)) }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Code: ${draft.code ?: ""}", color = GreenSold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (draft.locked != true) {
                    EditableField(
                        label = "Amount (${draft.currency})",
                        value = formatCad(editAmount.toDouble()),
                        onApply = { newVal ->
                            val v = newVal.CleanNumber()
                            editAmount = v
                            if (token.isNotEmpty()) homeViewModel?.updateBuyingDraft(
                                token,
                                draft.id ?: "",
                                v,
                                editRate ?: (draft.askingRate ?: 0.0)
                            )
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    EditableField(
                        label = "Asking Rate (IRR)",
                        value = formatIrr(editRate.toDouble()),
                        onApply = { newVal ->
                            val v = newVal.CleanNumber()
                            editRate = v
                            if (token.isNotEmpty()) homeViewModel?.updateBuyingDraft(
                                token,
                                draft.id ?: "",
                                editAmount ?: (draft.amount ?: 0.0),
                                v
                            )
                        }
                    )
                } else {
                    InfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    InfoRow("Asking Rate", "${formatIrr(draft.askingRate ?: 0.0)} IRR")
                }
//                if (draft.locked == true) {
//                    InfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
//                    InfoRow("Asking Rate", "IRR ${formatIrr(draft.askingRate ?: 0.0)}")
//                } else {
//                    OutlinedTextField(value = editAmount.toDoubleOrNull()?.let { formatCad(it) } ?: editAmount, onValueChange = { editAmount = it },
//                        label = { Text("Amount (${draft.currency})") }, modifier = Modifier.fillMaxWidth())
//                    Spacer(Modifier.height(4.dp))
//                    OutlinedTextField(value = editRate.toDoubleOrNull()?.let { formatIrr(it) } ?: editRate, onValueChange = { editRate = it },
//                        label = { Text("Asking Rate (IRR)") }, modifier = Modifier.fillMaxWidth())
//                }
                InfoRow(
                    "Total",
                    "${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR"
                )
                InfoRow("Exchange Rate", "${formatIrr(draft.exchangeRate ?: 0.0)} IRR")
                if (!draft.arzookBankInfoName.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                    Text("${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR")
                    Text("Recipient: ${draft.arzookBankInfoName}")
                    Text("Sheba: ${draft.arzookBankInfoSheba}")
                }
                if (!draft.payeeName.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                    Text("${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    Text("Recipient: ${draft.payeeName}")
                    Text("e-Transfer to: ${draft.payeeEmail}")
                    if (!draft.eTransferPassword.isNullOrEmpty())
                        Text("Password: ${draft.eTransferPassword}")
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray
                )
                InfoRow(
                    "Service Rate",
                    "${formatIrr((draft.askingRate ?: 0.0) - (draft.exchangeRate ?: 0.0))} IRR per ${draft.currency}"
                )
                InfoRow("1 ${draft.currency}", "${formatIrr(draft.exchangeRate ?: 0.0)} IRR")
                if (draft.complianceFee != null && draft.complianceFee.toDouble() > 0.0) {
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
                        InfoRow("Compliance fee", "${formatIrr(draft.complianceFee)} IRR")
                        IconButton(onClick = { showComplianceInfo = true }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Help, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            var showPostAlert by remember { mutableStateOf(false) }
                            if (showPostAlert) {
                                AlertDialog(
                                    onDismissRequest = { showPostAlert = false },
                                    title = { Text("Are you sure you want to make your buying invisible to other customers?") },
                                    text = { Text("By turning off the \"Post\" slider, others will no longer see your buying.") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showPostAlert = false
                                                advertised = false
                                                if (token.isNotEmpty()) homeViewModel?.updateBuyingAdvertised(token, draft.copy(advertised = false))
                                            },
                                            colors = ButtonDefaults.textButtonColors(containerColor = GreenSold)
                                        ) { Text("Yes", color = Color.White) }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showPostAlert = false },
                                            colors = ButtonDefaults.textButtonColors(containerColor = Brown)
                                        ) { Text("No", color = Color.White) }
                                    }
                                )
                            }
                            Switch(
                                checked = advertised,
                                onCheckedChange = {
                                    if (!it) showPostAlert = true
                                    else { advertised = true; if (token.isNotEmpty()) homeViewModel?.updateBuyingAdvertised(token, draft.copy(advertised = true)) }
                                },
                                modifier = Modifier.scale(0.5f).size(20.dp),
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = ChosenMenu
                                )
                            )
                            Text(" Post")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            var showSmartMatchAlert by remember { mutableStateOf(false) }
                            if (showSmartMatchAlert) {
                                AlertDialog(
                                    onDismissRequest = { showSmartMatchAlert = false },
                                    title = { Text("Are you sure you want to disable Smart Matching?") },
                                    text = { Text("Smart Matching is a great feature of buy all competitive Sellings automatically.") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showSmartMatchAlert = false
                                                smartMatching = false
                                                if (token.isNotEmpty()) homeViewModel?.updateBuyingSmartMatching(token, draft.id ?: "", draft.purposeOfTransaction ?: "", draft.sourceOfFund ?: "", false)
                                            },
                                            colors = ButtonDefaults.textButtonColors(containerColor = GreenSold)
                                        ) { Text("Yes", color = Color.White) }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showSmartMatchAlert = false },
                                            colors = ButtonDefaults.textButtonColors(containerColor = Brown)
                                        ) { Text("No", color = Color.White) }
                                    }
                                )
                            }
                            Switch(
                                checked = smartMatching,
                                onCheckedChange = {
                                    if (!it) showSmartMatchAlert = true
                                    else { smartMatching = true; if (token.isNotEmpty()) homeViewModel?.updateBuyingSmartMatching(token, draft.id ?: "", draft.purposeOfTransaction ?: "", draft.sourceOfFund ?: "", true) }
                                },
                                modifier = Modifier.scale(0.5f).size(20.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                            )
                            Text(" Smart Matching")
                        }
                    }
                    var showDepositedAlert by remember { mutableStateOf(false) }
                    if (showDepositedAlert) {
                        AlertDialog(
                            onDismissRequest = { showDepositedAlert = false },
                            title = { Text("Cannot Delete") },
                            text = { Text("Please make it unposted first, then delete the trade.") },
                            confirmButton = {
                                TextButton(onClick = { showDepositedAlert = false }) { Text("OK") }
                            }
                        )
                    }
                    ArzookButton(
                        onClick = {
                            if (draft.advertised == true) showDepositedAlert = true
                            else if (token.isNotEmpty()) homeViewModel?.deleteBuyingDraft(
                                token,
                                draft.id ?: "",
                                onSuccess = onDeleted
                            )
                        },
                        containerColor = Brown,
                        contentColor = Color.White,
                        modifier = Modifier
                    ) { Text("Delete", color = Color.White) }
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
internal fun CompletedBuyingRow(trade: TradeItem, token: String) {
    var expanded by remember { mutableStateOf(false) }
    val repo =
        remember { ca.arzook.shared.repository.ArzookRepositoryImpl("https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    var printing by remember { mutableStateOf(false) }
    var showReceipt by remember { mutableStateOf(false) }
    var receiptText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 4.dp)
            .padding(vertical = 8.dp, horizontal = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Cream80)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
        ) {
            (trade.sellingCode ?: trade.buyingCode)?.let { Text(it) }
            Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency?:"CAD"}")
            Text(trade.exchangeDepositedTime?.take(10) ?: "")
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                InfoRow(
                    if (trade.buyingNumber != null) "Buying Number" else "Selling Number",
                    (trade.buyingNumber ?: trade.sellingNumber).toString()
                )
                InfoRow(
                    if (trade.buyingCode != null) "Buying Code" else "Selling Code",
                    trade.buyingCode ?: trade.sellingCode ?: ""
                )
                InfoRow("Asking Rate", "${formatIrr(trade.askingRate ?: 0.0)} IRR")
                InfoRow("Exchange Rate", "${formatIrr(trade.exchangeRate ?: 0.0)} IRR")
                InfoRow(
                    "Service Charge",
                    formatIrr(abs((trade.askingRate ?: 0.0) - (trade.exchangeRate ?: 0.0)))
                )
                if (trade.complianceFee > 0.0) {
                    InfoRow("Compliance Fee", "${formatIrr(trade.complianceFee)} IRR")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            if (!printing) {
                                printing = true
                                scope.launch {
                                    when (val r = repo.printBuyingTrade(token, trade.id ?: "")) {
                                        is ca.arzook.shared.Result.Success -> {
                                            val opened =
                                                openPdf(r.data, "buying_${trade.buyingCode}.pdf")
                                            if (!opened) {
                                                receiptText = r.data.decodeToString()
                                                showReceipt = true
                                            }
                                        }

                                        is ca.arzook.shared.Result.Error -> println("[Print] error: ${r.message}")
                                    }
                                    printing = false
                                }
                            }
                        }
                    ) {
                        if (printing)
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        else
                            Icon(imageVector = Icons.Filled.Print, contentDescription = "Print")
                    }
                }
                if (showReceipt) {
                    Text(
                        "Unable to open PDF. Please install a PDF reader (e.g. Google Drive, Adobe Acrobat) from the Play Store, then try again.",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, fontSize: TextUnit = 14.sp) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = Color.DarkGray, fontSize = fontSize)
        Text(value, fontWeight = FontWeight.Bold, fontSize = fontSize)
    }
}

@Composable
private fun EmptyListMessage(msg: String) {
    Row(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Blue40)
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) { Text(msg, color = Color.White) }
}
