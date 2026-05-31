package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.TradeItem
import kotlinx.coroutines.launch
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
    val publicCompliance = homeViewModel?.publicCompliance?.collectAsState()?.value

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
                        completedTrades.forEach { CompletedBuyingRow(it, token, publicCompliance) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BuyingDraftRow(
    draft: TradeItem,
    token: String = "",
    homeViewModel: HomeViewModel? = null,
    key: Int = 0,
    onDeleted: () -> Unit = {}
) {
    val publicCompliance = homeViewModel?.publicCompliance?.collectAsState()?.value
    var expanded by remember(key) { mutableStateOf(false) }
    var advertised by remember { mutableStateOf(draft.advertised == true) }
    var smartMatching by remember { mutableStateOf(draft.smartMatchingEnabled == true) }
    val status = when {
        draft.deposited != true -> "e-Wallet Balance"
        draft.sellingCode == null -> "Pending Seller"
        draft.eTransferForwardedDate == null -> "e-Transfer"
        else -> "e-Transfer Deposit"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(
                vertical = 8.dp, horizontal = 16.dp
            )
            .background(Cream80)
    ) {
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
                        var remaining by remember(draft.lockExpiresIn) { mutableIntStateOf(draft.lockExpiresIn) }
                        LaunchedEffect(draft.lockExpiresIn) {
                            while (remaining > 0) { kotlinx.coroutines.delay(1000); remaining-- }
                        }
                        val h = remaining / 3600
                        val m = (remaining % 3600) / 60
                        val s = remaining % 60
                        val timeText = if (h > 0) "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}" else "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
                        Text(
                            text = timeText,
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
//            }
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = expanded) {
            var editAmount by remember { mutableStateOf((draft.amount ?: 0.0)) }
            var editRate by remember { mutableStateOf((draft.askingRate ?: 0.0)) }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Code: ${draft.code ?: ""}", color = GreenSold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                if (draft.locked != true) {
                    EditableField(
                        label = "Amount (${draft.currency})",
                        value = formatCad(editAmount),
                        onApply = { newVal ->
                            val v = newVal.CleanNumber()
                            editAmount = v
                            if (token.isNotEmpty()) homeViewModel?.updateBuyingDraft(
                                token,
                                draft.id ?: "",
                                v,
                                editRate
                            )
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    EditableField(
                        label = "Asking Rate (IRR)",
                        value = formatIrr(editRate),
                        onApply = { newVal ->
                            val v = newVal.CleanNumber()
                            editRate = v
                            if (token.isNotEmpty()) homeViewModel?.updateBuyingDraft(
                                token,
                                draft.id ?: "",
                                editAmount,
                                v
                            )
                        }
                    )
                } else {
                    InfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    InfoRow("Asking Rate", "${formatIrr(draft.askingRate ?: 0.0)} IRR")
                }
                InfoRow(
                    "Total",
                    "${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR"
                )
                InfoRow("Exchange Rate", "${formatIrr(draft.exchangeRate ?: 0.0)} IRR")
                if (!draft.arzookBankInfoName.isNullOrEmpty() || !draft.payeeName.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    val clipboardManager = LocalClipboardManager.current
                    var showSend by remember { mutableStateOf(true) }
                    var showGet by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "YOU SEND",
                            color = if (showSend) Color.White else Brown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showSend) Brown else Color.Transparent)
                                .clickable { showGet = false; showSend = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        Text(
                            "YOU GET",
                            color = if (showGet) Color.White else GreenSold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showGet) GreenSold else Color.Transparent)
                                .clickable { showSend = false; showGet = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    AnimatedVisibility(visible = showSend) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                InfoRow("Amount", "${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0)+draft.complianceFee)} IRR")

                            if (!draft.arzookBankInfoName.isNullOrEmpty()) InfoRow("Recipient", draft.arzookBankInfoName)
                            if (!draft.arzookBankInfoSheba.isNullOrEmpty()) InfoRow("Sheba", draft.arzookBankInfoSheba)
                            InfoRow("Deposit Id", draft.customerDepositId!!.toString())
                        }
                    }
                    AnimatedVisibility(visible = showGet) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                InfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                            if (!draft.payeeEmail.isNullOrEmpty())
                                InfoRow("e-Transfer to", draft.payeeEmail)

                            if (!draft.eTransferPassword.isNullOrEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                                InfoRow("Password", draft.eTransferPassword)
                                if (draft.eTransferPassword != "N/A") IconButton(onClick = { clipboardManager.setText(AnnotatedString(draft.eTransferPassword)) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
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
                if (draft.complianceFee > 0.0) {
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
                        InfoRow("Compliance fee (CAD \$${publicCompliance?.complianceFeeCAD ?: ""})", "${formatIrr(draft.complianceFee)} IRR")
                        IconButton(onClick = { showComplianceInfo = true }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null, modifier = Modifier.size(14.dp))
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
internal fun CompletedBuyingRow(trade: TradeItem, token: String, publicCompliance: ca.arzook.shared.model.PublicCompliance? = null) {
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
                    InfoRow("Compliance Fee (CAD \$${publicCompliance?.complianceFeeCAD ?: ""})", "${formatIrr(trade.complianceFee)} IRR")
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
fun InfoRow(label: String, value: String, fontSize: TextUnit = 12.sp) {
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
