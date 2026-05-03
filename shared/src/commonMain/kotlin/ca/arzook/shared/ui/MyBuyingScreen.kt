package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.TradeItem

private val BrownFont = Color(0xFF8B4513)
private val GreenFont = Color(0xFF4CAF50)
private val Cream80 = Color(0xFFF5F0E8)

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
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("My Buying") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Completed") })
        }

        when (tabIndex) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = onAddBuying,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
                    ) { Text("Add new buying") }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
                if (drafts.isEmpty()) {
                    EmptyListMessage("No buying drafts.")
                } else {
                    drafts.forEach { BuyingDraftRow(
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
                    ) }
                }
            }
            1 -> {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
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

@Composable
private fun BuyingDraftRow(draft: TradeItem, token: String = "", homeViewModel: HomeViewModel? = null, key: Int = 0, onDeleted: () -> Unit = {}) {
    var expanded by remember(key) { mutableStateOf(false) }
    var advertised by remember { mutableStateOf(draft.advertised == true) }
    var smartMatching by remember { mutableStateOf(draft.smartMatchingEnabled == true) }
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
            .padding(vertical = 8.dp, horizontal = 32.dp)
            .background(Cream80)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("IRR ${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))}",modifier = Modifier.weight(1f),)
            Column(modifier = Modifier.weight(1f),horizontalAlignment = Alignment.CenterHorizontally) {
                Text(status, color = BrownFont, fontWeight = FontWeight.Bold)
                HorizontalDivider(thickness = 2.dp, color = BrownFont)
                if ((draft.locked == true || draft.eTransferForwarded == null) && draft.lockExpiresIn != null && status != "e-Transfer Deposit") {
                    Text(
                        text = "${draft.lockExpiresIn}s",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Text("${formatCad(draft.amount ?: 0.0)} ${draft.currency}",modifier = Modifier.weight(1f),textAlign = TextAlign.End,
                maxLines = 1)
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = expanded) {
            val isEditable = draft.eTransferForwardedDate == null
            var editAmount by remember { mutableStateOf((draft.amount ?: 0.0).toString()) }
            var editRate by remember { mutableStateOf((draft.askingRate ?: 0.0).toString()) }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Code: ${draft.code ?: ""}", color = GreenFont, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (isEditable) {
                    OutlinedTextField(value = editAmount.toDoubleOrNull()?.let { formatCad(it) } ?: editAmount, onValueChange = { editAmount = it },
                        label = { Text("Amount (${draft.currency})") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = editRate.toDoubleOrNull()?.let { formatIrr(it) } ?: editRate, onValueChange = { editRate = it },
                        label = { Text("Asking Rate (IRR)") }, modifier = Modifier.fillMaxWidth())
                } else {
                    InfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    InfoRow("Asking Rate", "IRR ${formatIrr(draft.askingRate ?: 0.0)}")
                }
                InfoRow("Total", "IRR ${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))}")
                InfoRow("Exchange Rate", "IRR ${formatIrr(draft.exchangeRate ?: 0.0)}")
                InfoRow("Service Rate", "IRR ${formatIrr((draft.askingRate ?: 0.0) - (draft.exchangeRate ?: 0.0))} per ${draft.currency}")
                InfoRow("1 ${draft.currency}", "IRR ${formatIrr(draft.exchangeRate ?: 0.0)}")
                if (!draft.arzookBankInfoName.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("YOU SEND", color = BrownFont, fontWeight = FontWeight.Bold)
                    Text("IRR ${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))}")
                    Text("Recipient: ${draft.arzookBankInfoName}")
                    Text("Sheba: ${draft.arzookBankInfoSheba}")
                }
                if (!draft.payeeName.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("YOU GET", color = GreenFont, fontWeight = FontWeight.Bold)
                    Text("${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    Text("Recipient: ${draft.payeeName}")
                    Text("e-Transfer to: ${draft.payeeEmail}")
                    if (!draft.eTransferPassword.isNullOrEmpty())
                        Text("Password: ${draft.eTransferPassword}")
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Switch(checked = advertised, onCheckedChange = { advertised = it }, modifier = Modifier.scale(0.5f).size(20.dp))
                            Text(" Post")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Switch(checked = smartMatching, onCheckedChange = { smartMatching = it }, modifier = Modifier.scale(0.5f).size(20.dp))
                            Text(" Smart Matching")
                        }
                    }
                    Button(
                        onClick = {
                            if (token.isNotEmpty()) homeViewModel?.deleteBuyingDraft(token, draft.id ?: "", onSuccess = onDeleted)
                        },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = BrownFont)
                    ) { Text("Delete", color = Color.White) }
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
private fun CompletedBuyingRow(trade: TradeItem, token: String) {
    var expanded by remember { mutableStateOf(false) }
    val repo = remember { ca.arzook.shared.repository.ArzookRepositoryImpl("https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    var printing by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 4.dp)
            .padding(vertical = 8.dp, horizontal = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Cream80)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$${trade.amount} ${trade.currency}")
            Text(trade.createdTime?.take(10) ?: "")
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                InfoRow("Buying Code", trade.buyingCode ?: "")
                InfoRow("Asking Rate", "IRR ${formatIrr(trade.askingRate ?: 0.0)}")
                InfoRow("Exchange Rate", "IRR ${formatIrr(trade.exchangeRate ?: 0.0)}")
                InfoRow("Service Charge", formatIrr((trade.askingRate ?: 0.0) - (trade.exchangeRate ?: 0.0)))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(
                        onClick = {
                            if (!printing) {
                                printing = true
                                scope.launch {
                                    when (val r = repo.printBuyingTrade(token, trade.id ?: "")) {
                                        is ca.arzook.shared.Result.Success -> openPdf(r.data, "buying_${trade.buyingCode}.pdf")
                                        is ca.arzook.shared.Result.Error -> println("[Print] error: ${r.message}")
                                    }
                                    printing = false
                                }
                            }
                        }
                    ) {
                        if (printing)
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else
                            Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Print, contentDescription = "Print")
                    }
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = Color.DarkGray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyListMessage(msg: String) {
    Row(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1e3957))
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) { Text(msg, color = Color.White) }
}
