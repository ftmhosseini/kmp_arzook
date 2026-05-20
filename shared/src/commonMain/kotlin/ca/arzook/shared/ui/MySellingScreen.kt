package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.Payee
import ca.arzook.shared.model.TradeItem
import kotlinx.coroutines.launch

@Composable
fun MySellingScreen(
    drafts: List<TradeItem>,
    completedTrades: List<TradeItem>,
    payees: List<Payee>,
    onAddSelling: () -> Unit = {},
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
                    text = { Text("My Selling") })
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
                            onClick = onAddSelling,
                            modifier = Modifier
                        ) { Text("Add new selling") }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    if (drafts.isEmpty()) {
                        SellingEmptyMessage("No selling drafts.")
                    } else {
                        drafts.forEach {
                            SellingDraftRow(
                                draft = it,
                                payees = payees,
                                token = token,
                                homeViewModel = homeViewModel,
                                key = deleteKey,
                                onDeleted = {
                                    deleteKey++
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Selling deleted successfully")
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
                        SellingEmptyMessage("No completed selling trades.")
                    } else {
                        completedTrades.forEach { CompletedSellingRow(it, token) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SellingDraftRow(
    draft: TradeItem,
    payees: List<Payee>,
    token: String = "",
    homeViewModel: HomeViewModel? = null,
    key: Int = 0,
    onDeleted: () -> Unit = {}
) {
    var expanded by remember(key) { mutableStateOf(false) }
    var selectedPayee by remember { mutableStateOf<Payee?>(null) }
    var payeeDropdownExpanded by remember { mutableStateOf(false) }
    var advertised by remember { mutableStateOf(draft.advertised == true) }
    var urgent by remember { mutableStateOf(draft.urgent == true) }
    val scope = rememberCoroutineScope()

    val status = when {
        draft.deposited == null -> "e-Transfer"
        draft.buyingCode == null -> "Pending Buyer"
        draft.buyingDraftExchangeDeposited == null -> "Buyer Deposit"
        draft.eTransferForwarded == null -> "e-Transfer Forward"
        else -> "e-Transfer Deposit"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp, horizontal = 16.dp
            )
//            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Cream80)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${formatCad(draft.amount ?: 0.0)} ${draft.currency}",
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = status,
                    color = Brown,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                HorizontalDivider(
                    thickness = 2.dp,
                    color = Brown
                )
                if ((draft.locked == true || draft.eTransferForwarded == null) && draft.lockExpiresIn != null && status != "e-Transfer Deposit") {
                    Text(
                        text = "${draft.lockExpiresIn}s",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

            }
            Text(
                text = "${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR",
                fontSize = 12.sp,
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Icon(
                imageVector = if (expanded)
                    Icons.Filled.KeyboardArrowUp
                else
                    Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = expanded) {
            var editAmount by remember { mutableStateOf((draft.amount ?: 0.0)) }
            var editRate by remember { mutableStateOf((draft.askingRate ?: 0.0)) }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Code: ${draft.code ?: ""}", color = GreenSold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))


                var showAddPayeeSheet by remember { mutableStateOf(false) }
                if (showAddPayeeSheet) AddPayeeSheet(
                    token = token,
                    homeViewModel = homeViewModel,
                    onDismiss = { showAddPayeeSheet = false })



                Spacer(Modifier.height(8.dp))

                if (draft.isLocked == null || draft.eTransferForwarded == null) {
                    EditableField(
                        label = "Amount (${draft.currency})",
                        value = formatCad(editAmount.toDouble()),
                        onApply = { newVal ->
                            val v = newVal.CleanNumber()
                            editAmount = v
                            if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(
                                token,
                                draft.id ?: "",
                                v,
                                editRate ?: (draft.askingRate ?: 0.0),
                                selectedPayee?.sheba ?: draft.sheba ?: "",
                                selectedPayee?.name ?: draft.payeeName ?: ""
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
                            if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(
                                token,
                                draft.id ?: "",
                                editAmount ?: (draft.amount ?: 0.0),
                                v,
                                selectedPayee?.sheba ?: draft.sheba ?: "",
                                selectedPayee?.name ?: draft.payeeName ?: ""
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
                InfoRow("Exchange Rate", "${formatIrr(draft.exchangeRate ?: 0.0)} IRR ")
                // Payee selector
                Text("Payee", color = Brown, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { payeeDropdownExpanded = true }) {
                        Text(selectedPayee?.name ?: "Select payee")
                    }
                    ArzookDropdownMenu(
                        expanded = payeeDropdownExpanded,
                        onDismissRequest = { payeeDropdownExpanded = false }
                    ) {
                        payees.forEach { payee ->
                            DropdownMenuItem(
                                text = { Text(payee.name) },
                                onClick = { selectedPayee = payee; payeeDropdownExpanded = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "+ Add New Payee",
                                    color = Brown,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = { payeeDropdownExpanded = false; showAddPayeeSheet = true }
                        )
                    }
                }
                selectedPayee?.let { Text("Sheba: ${it.sheba}") }


                Spacer(Modifier.height(8.dp))
                Text("YOU SEND", color = Brown, fontWeight = FontWeight.Bold)
                Text("${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                if (!draft.arzookDepositEmail.isNullOrEmpty()) Text("Arzook Deposit Email: ${draft.arzookDepositEmail}")
                if (!draft.eTransferPassword.isNullOrEmpty()) Text("e-Transfer password: ${draft.eTransferPassword}")
                Text("YOU GET", color = GreenSold, fontWeight = FontWeight.Bold)
                Text("${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))} IRR")
                if (!draft.sheba.isNullOrEmpty()) Text("Your Sheba: ${draft.sheba}")
                if (!draft.payeeName.isNullOrEmpty()) Text("Recipient: ${draft.payeeName}")

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray
                )
                InfoRow(
                    "Service Rate",
                    "${formatIrr((draft.exchangeRate ?: 0.0) - (draft.askingRate ?: 0.0))} IRR per ${draft.currency}"
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
                        InfoRow("Compliance fee", "${formatIrr(draft.complianceFee)} IRR ")
                        IconButton(onClick = { showComplianceInfo = true }) {
                            Icon(imageVector = Icons.Default.Help, contentDescription = null)
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
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Switch(
                                checked = advertised,
                                onCheckedChange = { advertised = it },
                                modifier = Modifier.scale(0.5f).size(20.dp),
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = ChosenMenu,      // The background when ON
                                )
                            )
                            Text(" Post")
                        }
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Switch(
                                checked = urgent,
                                onCheckedChange = { urgent = it },
                                modifier = Modifier.scale(0.5f).size(20.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                            )
                            Text(" Urgent")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ArzookButton(
                            onClick = {
                                if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(
                                    token, draft.id ?: "",
                                    editAmount ?: (draft.amount ?: 0.0),
                                    editRate ?: (draft.askingRate ?: 0.0),
                                    selectedPayee?.sheba ?: draft.sheba ?: "",
                                    selectedPayee?.name ?: draft.payeeName ?: ""
                                )
                            },
                            containerColor = GreenSold,
                            contentColor = Color.White,
                            modifier = Modifier
                        ) { Text("Update", color = Color.White) }
                        if (draft.isLocked == null || draft.eTransferForwarded == false) {
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Delete Selling") },
                                    text = { Text("Are you sure you want to delete this selling draft? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showDeleteConfirm = false
                                            if (token.isNotEmpty()) homeViewModel?.deleteSellingDraft(
                                                token,
                                                draft.id ?: "",
                                                onSuccess = onDeleted
                                            )
                                        }) { Text("Delete", color = Color.Red) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showDeleteConfirm = false
                                        }) { Text("Cancel") }
                                    }
                                )
                            }
                            ArzookButton(
                                onClick = { showDeleteConfirm = true },
                                containerColor = Brown,
                                contentColor = Color.White,
                                modifier = Modifier
                            ) { Text("Delete", color = Color.White) }
                        }
                    }
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
internal fun CompletedSellingRow(trade: TradeItem, token: String) {
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
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 32.dp)
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
            Text("$${formatCad(trade.amount ?: 0.0)} ${trade.currency}")
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
                InfoRow("Exchange Rate", "${formatIrr(trade.exchangeRate ?: 0.0)} IRR ")
                InfoRow(
                    "Service Charge",
                    "${formatIrr((trade.exchangeRate ?: 0.0) - (trade.askingRate ?: 0.0))} IRR "
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
                                    when (val r = repo.printSellingTrade(token, trade.id ?: "")) {
                                        is ca.arzook.shared.Result.Success -> {
                                            val opened =
                                                openPdf(r.data, "selling_${trade.sellingCode}.pdf")
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
private fun SellingEmptyMessage(msg: String) {
    Row(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EmptyList)
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) { Text(msg, color = Color.White) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPayeeSheet(
    token: String = "",
    homeViewModel: HomeViewModel? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var shebaDigits by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val shebaValid = shebaDigits.length == 24 && shebaDigits.all { it.isDigit() }
    val nationalIdValid = nationalId.length == 10 && nationalId.all { it.isDigit() }
    val postalCodeValid = postalCode.length == 10 && postalCode.all { it.isDigit() }
    val nameValid = name.isNotBlank()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "For instant deposits, provide Bank Melli Sheba; otherwise, expect Paya transfer delays.",
                color = GreenSold,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name", fontSize = 10.sp) },
                isError = submitted && !nameValid,
                supportingText = if (submitted && !nameValid) {
                    { Text("Name is required.", color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = shebaDigits,
                onValueChange = { v ->
                    shebaDigits = v.filter { it.isDigit() }.take(24)
                },
                label = { Text("Sheba (IR + 24 digits)", fontSize = 10.sp) },
                prefix = { Text("IR") },
                placeholder = { Text("24 digit number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = submitted && !shebaValid,
                supportingText = if (submitted && !shebaValid) {
                    {
                        Text(
                            if (shebaDigits.isEmpty())
                                "Sheba number is required."
                            else
                                "Sheba must be exactly 24 digits (currently ${shebaDigits.length}/24).",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text("City", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = postalCode,
                onValueChange = { v -> postalCode = v.filter { it.isDigit() }.take(10) },
                label = { Text("IR Postal Code (کد پستی)", fontSize = 10.sp) },
                placeholder = { Text("10 digit number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = submitted && !postalCodeValid,
                supportingText = if (submitted && !postalCodeValid) {
                    {
                        Text(
                            "IR Postal Code (کد پستی) is a 10 digit number.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nationalId,
                onValueChange = { v -> nationalId = v.filter { it.isDigit() }.take(10) },
                label = { Text("IR National ID (کد ملی)", fontSize = 10.sp) },
                placeholder = { Text("10 digit number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = submitted && !nationalIdValid,
                supportingText = if (submitted && !nationalIdValid) {
                    {
                        Text(
                            "IR National ID (کد ملی) is a 10 digit number.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ArzookButton(
                onClick = {
                    submitted = true
                    if (nameValid && shebaValid && nationalIdValid && postalCodeValid) {
                        val payee = Payee(
                            id = null,
                            name = name,
                            sheba = shebaDigits,
                            city = city,
                            irPostalCode = postalCode,
                            irNationalId = nationalId
                        )
                        homeViewModel?.addPayee(token, payee, onSuccess = { onDismiss() })
                            ?: onDismiss()
                    }
                }
            ) { Text("Add") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun EditableField(label: String, value: String, onApply: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { if (editing) text = it },
        label = { Text(label, fontSize = 10.sp) },
        readOnly = !editing,
        trailingIcon = {
            if (editing) {
                IconButton(onClick = { onApply(text); editing = false }) {
                    Icon(Icons.Filled.Check, contentDescription = "Apply")
                }
            } else {
                IconButton(onClick = { editing = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

internal fun String.CleanNumber(): Double {
    return this.replace(",", "").toDoubleOrNull() ?: 0.0
}