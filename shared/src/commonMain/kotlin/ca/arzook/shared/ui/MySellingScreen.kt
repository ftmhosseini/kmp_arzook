package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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

private val SellingBrownFont = Color(0xFF8B4513)
private val SellingGreenFont = Color(0xFF4CAF50)
private val SellingCream80 = Color(0xFFF5F0E8)

@OptIn(ExperimentalMaterial3Api::class)
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
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("My Selling") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Completed") })
        }

        when (tabIndex) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = onAddSelling,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
                    ) { Text("Add new selling") }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
                if (drafts.isEmpty()) {
                    SellingEmptyMessage("No selling drafts.")
                } else {
                    drafts.forEach { SellingDraftRow(
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
                    ) }
                }
            }
            1 -> {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
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

@Composable
private fun SellingDraftRow(draft: TradeItem, payees: List<Payee>, token: String = "", homeViewModel: HomeViewModel? = null, key: Int = 0, onDeleted: () -> Unit = {}) {
    var expanded by remember(key) { mutableStateOf(false) }
    var selectedPayee by remember { mutableStateOf<Payee?>(null) }
    var payeeDropdownExpanded by remember { mutableStateOf(false) }
    var advertised by remember { mutableStateOf(draft.advertised == true) }
    var urgent by remember { mutableStateOf(draft.urgent == true) }

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
            .padding(vertical = 8.dp, horizontal = 32.dp)
//            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SellingCream80)
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
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = status,
                    color = SellingBrownFont,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                HorizontalDivider(
                    thickness = 2.dp,
                    color = SellingBrownFont
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
                text = "IRR ${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))}",
                modifier = Modifier.weight(1f),
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
            var editAmount by remember { mutableStateOf((draft.amount ?: 0.0).toString()) }
            var editRate by remember { mutableStateOf((draft.askingRate ?: 0.0).toString()) }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Code: ${draft.code ?: ""}", color = SellingGreenFont, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))


                var showAddPayeeSheet by remember { mutableStateOf(false) }
                if (showAddPayeeSheet) AddPayeeSheet(token = token, homeViewModel = homeViewModel, onDismiss = { showAddPayeeSheet = false })



                Spacer(Modifier.height(8.dp))

                if (draft.isLocked == null || draft.eTransferForwarded == null){
                    EditableField(
                        label = "Amount (${draft.currency})",
                        value = editAmount,
                        onApply = { newVal ->
                            editAmount = newVal
                            val v = newVal.toDoubleOrNull() ?: return@EditableField
                            if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(token, draft.id ?: "", v, editRate.toDoubleOrNull() ?: (draft.askingRate ?: 0.0), selectedPayee?.sheba ?: draft.sheba ?: "", selectedPayee?.name ?: draft.payeeName ?: "")
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    EditableField(
                        label = "Asking Rate (IRR)",
                        value = editRate,
                        onApply = { newVal ->
                            editRate = newVal
                            val v = newVal.toDoubleOrNull() ?: return@EditableField
                            if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(token, draft.id ?: "", editAmount.toDoubleOrNull() ?: (draft.amount ?: 0.0), v, selectedPayee?.sheba ?: draft.sheba ?: "", selectedPayee?.name ?: draft.payeeName ?: "")
                        }
                    )
                } else {
                    SellingInfoRow("Amount", "${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                    SellingInfoRow("Asking Rate", "IRR ${formatIrr(draft.askingRate ?: 0.0)}")
                }
                // Payee selector
                Text("Payee", color = SellingBrownFont, fontWeight = FontWeight.Bold)
                Row (horizontalArrangement = Arrangement.SpaceBetween){
                    OutlinedButton(onClick = { payeeDropdownExpanded = true }) {
                        Text(selectedPayee?.name ?: "Select payee")
                    }
                    DropdownMenu(
                        expanded = payeeDropdownExpanded,
                        onDismissRequest = { payeeDropdownExpanded = false }
                    ) {
                        payees.forEach { payee ->
                            DropdownMenuItem(
                                text = { Text(payee.name) },
                                onClick = { selectedPayee = payee; payeeDropdownExpanded = false }
                            )
                        }
                    }
                    OutlinedButton(onClick = { showAddPayeeSheet = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("+ Add New Payee")
                    }
                }
                selectedPayee?.let { Text("Sheba: ${it.sheba}") }


                Spacer(Modifier.height(8.dp))
                Text("YOU SEND", color = SellingBrownFont, fontWeight = FontWeight.Bold)
                Text("${formatCad(draft.amount ?: 0.0)} ${draft.currency}")
                if (!draft.arzookDepositEmail.isNullOrEmpty()) Text("Arzook Deposit Email: ${draft.arzookDepositEmail}")
                if (!draft.eTransferPassword.isNullOrEmpty()) Text("e-Transfer password: ${draft.eTransferPassword}")
                Text("YOU GET", color = SellingGreenFont, fontWeight = FontWeight.Bold)
                Text("IRR ${formatIrr((draft.askingRate ?: 0.0) * (draft.amount ?: 0.0))}")
                if (!draft.sheba.isNullOrEmpty()) Text("Your Sheba: ${draft.sheba}")
                if (!draft.payeeName.isNullOrEmpty()) Text("Recipient: ${draft.payeeName}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//                SellingInfoRow("Total", "IRR ${formatIrr(draft.askingRate * draft.amount)}")
                SellingInfoRow("Exchange Rate", "IRR ${formatIrr(draft.exchangeRate ?: 0.0)}")
                SellingInfoRow("Service Rate", "IRR ${formatIrr((draft.exchangeRate ?: 0.0) - (draft.askingRate ?: 0.0))} per ${draft.currency}")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Switch(checked = advertised, onCheckedChange = { advertised = it }, modifier = Modifier.scale(0.5f).size(20.dp))
                            Text(" Post")
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Switch(checked = urgent, onCheckedChange = { urgent = it }, modifier = Modifier.scale(0.5f).size(20.dp))
                            Text(" Urgent")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                if (token.isNotEmpty()) homeViewModel?.updateSellingDraft(
                                    token, draft.id ?: "",
                                    editAmount.toDoubleOrNull() ?: (draft.amount ?: 0.0),
                                    editRate.toDoubleOrNull() ?: (draft.askingRate ?: 0.0),
                                    selectedPayee?.sheba ?: draft.sheba ?: "",
                                    selectedPayee?.name ?: draft.payeeName ?: ""
                                )
                            },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) { Text("Update", color = Color.White) }
                        if (draft.isLocked == null || draft.eTransferForwarded == false){
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Delete Selling") },
                                    text = { Text("Are you sure you want to delete this selling draft? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showDeleteConfirm = false
                                            if (token.isNotEmpty()) homeViewModel?.deleteSellingDraft(token, draft.id ?: "", onSuccess = onDeleted)
                                        }) { Text("Delete", color = Color.Red) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                                    }
                                )
                            }
                            Button(
                                onClick = { showDeleteConfirm = true },
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SellingBrownFont)
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
private fun CompletedSellingRow(trade: TradeItem, token: String) {
    var expanded by remember { mutableStateOf(false) }
    val repo = remember { ca.arzook.shared.repository.ArzookRepositoryImpl("https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    var printing by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 32.dp)
            .background(SellingCream80)
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
                SellingInfoRow("Selling Code", trade.sellingCode ?: "")
                SellingInfoRow("Asking Rate", "IRR ${formatIrr(trade.askingRate ?: 0.0)}")
                SellingInfoRow("Exchange Rate", "IRR ${formatIrr(trade.exchangeRate ?: 0.0)}")
                SellingInfoRow("Service Charge", formatIrr((trade.exchangeRate ?: 0.0) - (trade.askingRate ?: 0.0)))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(
                        onClick = {
                            if (!printing) {
                                printing = true
                                scope.launch {
                                    when (val r = repo.printSellingTrade(token, trade.id ?: "")) {
                                        is ca.arzook.shared.Result.Success -> openPdf(r.data, "selling_${trade.sellingCode}.pdf")
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
                            Icon(imageVector = Icons.Filled.Print, contentDescription = "Print")
                    }
                }
                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Composable
private fun SellingInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = Color.DarkGray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SellingEmptyMessage(msg: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPayeeSheet(token: String = "", homeViewModel: HomeViewModel? = null, onDismiss: () -> Unit) {
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
                color = SellingGreenFont,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Name") },
                isError = submitted && !nameValid,
                supportingText = if (submitted && !nameValid) { { Text("Name is required.", color = MaterialTheme.colorScheme.error) } } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = shebaDigits,
                onValueChange = { v ->
                    shebaDigits = v.filter { it.isDigit() }.take(24)
                },
                label = { Text("Sheba (IR + 24 digits)") },
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
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )
//            val postalValid = postalCode.length == 10
//
//            OutlinedTextField(
//                value = postalCode,
//                onValueChange = { v ->
//                    postalCode = v.filter { it.isDigit() }.take(24)
//                },
//                label = { Text("IR Postal code (10 digits)") },
//                placeholder = { Text("10 digit number") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                isError = submitted && !postalValid,
//                supportingText = if (submitted && !postalValid) {
//                    {
//                        Text(
//                            if (postalCode.isEmpty())
//                                "IR Postal code is required."
//                            else
//                                "IR Postal Code (کد پستی) is a 10 digit number. ${postalCode.length}/10).",
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                } else null,
//                modifier = Modifier.fillMaxWidth()
//            )

            OutlinedTextField(
                value = postalCode,
                onValueChange = { v -> postalCode = v.filter { it.isDigit() }.take(10) },
                label = { Text("IR Postal Code (کد پستی)") },
                placeholder = { Text("10 digit number") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                isError = submitted && !postalCodeValid,
                supportingText = if (submitted && !postalCodeValid) { { Text("IR Postal Code (کد پستی) is a 10 digit number.", color = MaterialTheme.colorScheme.error) } } else null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nationalId,
                onValueChange = { v -> nationalId = v.filter { it.isDigit() }.take(10) },
                label = { Text("IR National ID (کد ملی)") },
                placeholder = { Text("10 digit number") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                isError = submitted && !nationalIdValid,
                supportingText = if (submitted && !nationalIdValid) { { Text("IR National ID (کد ملی) is a 10 digit number.", color = MaterialTheme.colorScheme.error) } } else null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
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
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
            ) { Text("Add") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EditableField(label: String, value: String, onApply: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { if (editing) text = it },
        label = { Text(label) },
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
