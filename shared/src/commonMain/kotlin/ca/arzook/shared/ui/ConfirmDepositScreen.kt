@file:OptIn(ExperimentalMaterial3Api::class)

package ca.arzook.shared.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.Result
import ca.arzook.shared.model.AuthenticatedData
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.model.WalletStatus
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.launch

private val repo = ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca")

@Composable
fun ConfirmDepositScreen(token: String) {
    println("[confirmDeposit]: $token")
    var drafts by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        println("[DEBUG] Fetching drafts with token: ${token.take(10)}...")
        when (val r = repo.getAdminSellingDrafts(token, deposited = true)) {
            is Result.Success -> {
                drafts = r.data
                println("[DEBUG] Success! Received ${drafts.size} drafts.")
                drafts.forEachIndexed { index, item ->
                    println("[DEBUG] Draft #$index: ID=${item.id}, Code=${item.code}, Status=${item.status} seller id=${item.sellerId}, buyer id=${item.buyingId}")
                }
            }

            is Result.Error -> {
                println("[DEBUG] API Error: ${r.message}")
            }
        }
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(Cream40)) {
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow40)
            }
        } else if (drafts.isEmpty()) {
            Box(
                modifier = Modifier.padding(24.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)).background(Blue40).padding(20.dp),
                contentAlignment = Alignment.Center
            ) { Text("No deposits found.", color = Color.White) }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(drafts) { draft ->
                    SellingBuyingDraftCard(draft, token)
                }
            }
        }
    }
}

@Composable
private fun SellingBuyingDraftCard(draft: TradeItem, token: String) {
    var expanded by remember { mutableStateOf<String?>(null) }
    var sellerUser by remember { mutableStateOf<AuthenticatedData?>(null) }
    var buyerUser by remember { mutableStateOf<AuthenticatedData?>(null) }
    var localDraft by remember { mutableStateOf<TradeItem?>(null) } // FIXED: Changed to mutable state
    var detailLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Cream80)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            println("[Draft] draft:$draft")
            Text(
                text = draft.code ?: "-",
                fontWeight = FontWeight.Bold,
                color = Blue40,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable {
                        if (expanded == "seller") {
                            expanded = null
                        } else {
                            expanded = "seller"
                            sellerUser = null
                            localDraft = null
                            detailLoading = true
                            scope.launch {
                                try {
                                    // Load seller user data
                                    if (draft.sellerId != null) {
                                        when (val userResult = repo.getAdminUser(token, draft.sellerId)) {
                                            is Result.Success -> sellerUser = userResult.data
                                            is Result.Error -> println("[ConfirmDeposit] seller error: ${userResult.message}")
                                        }
                                    }

                                    // Load draft data
                                    if (draft.id != null) {
                                        when (val draftResult = repo.getAdminSellingDraftById(token, draft.id)) {
                                            is Result.Success -> localDraft = draftResult.data
                                            is Result.Error -> println("[ConfirmDeposit] draft error: ${draftResult.message}")
                                        }
                                    }

                                } catch (e: Exception) {
                                    println("[ConfirmDeposit] exception: ${e.message}")
                                } finally {
                                    detailLoading = false
                                }
                            }
                        }
                    }
            )
            Text(
                text = "${draft.amount} ${draft.currency ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            Text(
                text = draft.buyingCode ?: "-",
                fontWeight = FontWeight.Bold,
                color = Blue40,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable {
                        if (expanded == "buyer") {
                            expanded = null
                        } else {
                            expanded = "buyer"
                            buyerUser = null
                            localDraft = null
                            detailLoading = true
                            scope.launch {
                                try {
                                    if (draft.buyingId != null) {
                                        when (val draftResult = repo.getAdminBuyingDraftById(token, draft.buyingId)) {
                                            is Result.Success -> {
                                                localDraft = draftResult.data
                                                val d = draftResult.data
                                                println("[buyer] draft sellerId=${d.sellerId} buyerId=${d.buyerId} buyingId=${d.buyingId} sellingId=${d.sellingId}")
                                                val userId = d.sellerId ?: d.buyerId
                                                if (userId != null) {
                                                    when (val userResult = repo.getAdminUser(token, userId)) {
                                                        is Result.Success -> { buyerUser = userResult.data; println("[buyer] user: $buyerUser") }
                                                        is Result.Error -> println("[ConfirmDeposit] buyer user error: ${userResult.message}")
                                                    }
                                                } else println("[buyer] no user ID found in buying draft")
                                            }
                                            is Result.Error -> println("[ConfirmDeposit] buying draft error: ${draftResult.message}")
                                        }
                                    }
                                    println("[buyer] order is  $localDraft")
                                } catch (e: Exception) {
                                    println("[ConfirmDeposit] exception: ${e.message}")
                                } finally {
                                    detailLoading = false
                                }
                            }
                        }
                    }
            )
        }
        if (detailLoading) {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Yellow40)
            }
        } else if (expanded == "seller" && sellerUser != null) {
            val draftData = localDraft ?: draft
            UserInfoSection(
                title = "Seller",
                user = sellerUser!!,
                draftData = draftData,
                isSeller = true,
                token = token
            )
        } else if (expanded == "buyer" && buyerUser != null) {
            val draftData = localDraft ?: draft
            UserInfoSection(
                title = "Buyer",
                user = buyerUser!!,
                draftData = draftData,
                isSeller = false
            )
        }

    }
}

@Composable
fun UserInfoSection(
    title: String,
    user: AuthenticatedData,
    draftData: TradeItem,
    isSeller: Boolean = true,
    token: String = ""
) {
    val expiryDate = draftData.photoIdExpiryDate ?: user.photoIdExpiryDate
    val expiryColor = expiryDate?.let { if (isDateExpired(it)) Color.Red else GreenDark } ?: Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CreamDark)
            .padding(10.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))

        if (isSeller) {

            var selectedBank by remember { mutableStateOf("") }
            var bankExpanded by remember { mutableStateOf(false) }
//            InfoRow("Seller Id", user.id)
            InfoRow("Seller Name", "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim())
            InfoRow("Seller Email", user.email)
            InfoRow("Seller Phone", user.phoneNumber ?: "-")
            Spacer(Modifier.height(6.dp))
            InfoRow("Created at", draftData.createdTime ?: "-")
            InfoRow("Amount", draftData.amount?.let { formatCad(it) } ?: "-")
            InfoRow("Asking Rate (IRR)", draftData.askingRate?.let { formatIrr(it) } ?: "-")
            InfoRow("Exchange Rate (IRR)", draftData.exchangeRate?.let { formatIrr(it) } ?: "-")
            InfoRow("Currency", draftData.currency ?: "-")
            if(draftData.discount?.length != 0) InfoRow("Promo Code", draftData.discount ?: "-")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Total",
//                    modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.bodySmall, color = Color.Gray
                )
                Text((draftData.amount?.times(draftData.askingRate!!))?.let { formatIrr(it) } ?: "-", //modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = if (draftData.totalCopied == true) Icons.Filled.CheckCircle else Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = if (draftData.totalCopied == true) GreenDark else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            InfoRow("Payee Name", draftData.payeeName ?: "-")
            PayeeEditForm(draftData = draftData, token = token)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Sheba")
                Text(draftData.sheba ?: "-")
                Icon(
                    imageVector = if (draftData.shebaCopied == true) Icons.Filled.CheckCircle else Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = if (draftData.shebaCopied == true) GreenDark else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Photo id expires on")
                Text(expiryDate ?: "-", color = expiryColor)
            }

            Spacer(Modifier.height(8.dp))

            // Bank dropdown
            ExposedDropdownMenuBox(expanded = bankExpanded, onExpandedChange = { bankExpanded = it }) {
                OutlinedTextField(
                    value = selectedBank,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bank", fontSize = 10.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                ExposedDropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
                    banks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank) },
                            onClick = { selectedBank = bank; bankExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            AdminActionsSection(draftData = draftData, token = token, userId = user.id)

        } else {
            var wallet by remember { mutableStateOf<WalletStatus?>(null) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(user.id) {
                when (val r = repo.adminGetUserWallet(token, user.id)) {
                    is Result.Success -> wallet = r.data
                    is Result.Error -> println("[buyer wallet] ${r.message}")
                }
            }

            // Buying Details
            InfoRow("Code", draftData.code ?: "-")
            InfoRow("Buyer Name", "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim())
            InfoRow("Buyer Email", user.email)
            InfoRow("Buyer Phone", user.phoneNumber ?: "-")
            InfoRow("Deposit Id", user.customerDepositId?.toString() ?: "-")
            InfoRow("Created at", draftData.createdTime ?: "-")
            InfoRow("Amount", draftData.amount?.let { formatCad(it) } ?: "-")
            InfoRow("Asking Rate (IRR)", draftData.askingRate?.let { formatIrr(it) } ?: "-")
            InfoRow("Exchange Rate (IRR)", draftData.exchangeRate?.let { formatIrr(it) } ?: "-")
            InfoRow("Hold Transaction Fee", draftData.holdTransactionFee?.toString() ?: "0")
            InfoRow("Currency", draftData.currency ?: "-")
            InfoRow("Promo Code", draftData.discount ?: "-")
            InfoRow("Total", draftData.amount?.let { amt -> draftData.askingRate?.let { rate -> formatIrr(amt * rate) } } ?: "-")

            wallet?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Balance")
                    Text("${formatIrr(it.balance.toDouble())} IRR", color = expiryColor)
                }
                InfoRow("Hold Credit", "${formatIrr(it.holdCredit.toDouble())} IRR")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Photo id expires on")
                Text(expiryDate ?: "-", color = expiryColor)
            }

            Spacer(Modifier.height(8.dp))

            // Buyer Admin Actions
            BuyerAdminActionsSection(draftData = draftData, token = token)
        }
    }
}

//fun Row(horizontalArrangement: Alignment.Horizontal, verticalAlignment: Alignment.Vertical, content: RowScope.() -> Unit) {
//
//}

private val banks = listOf("Melli", "Mellat")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayeeEditForm(draftData: TradeItem, token: String) {
    var firstName by remember { mutableStateOf(draftData.payeeFirstName ?: draftData.payeeName ?: "") }
    var lastName by remember { mutableStateOf(draftData.payeeLastName ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (draftData.payeeName == null) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Payee First Name", fontSize = 10.sp) },
                placeholder = { Text(draftData.payeeName ?: "") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Payee Last Name", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** Returns true if the date string (e.g. "Sep 30, 2029") is in the past. */
private fun isDateExpired(dateStr: String): Boolean {
    return try {
        val months = mapOf(
            "Jan" to "01", "Feb" to "02", "Mar" to "03", "Apr" to "04",
            "May" to "05", "Jun" to "06", "Jul" to "07", "Aug" to "08",
            "Sep" to "09", "Oct" to "10", "Nov" to "11", "Dec" to "12"
        )
        // Expected format: "Sep 30, 2029"
        val parts = dateStr.trim().split(" ")
        val month = months[parts[0]] ?: return false
        val day = parts[1].trimEnd(',').padStart(2, '0')
        val year = parts[2]
        val expiryYmd = "$year-$month-$day"   // "2029-09-30"
        val today = getCurrentDateString()     // "2026-05-11"
        expiryYmd < today
    } catch (_: Exception) { false }
}

@Composable
private fun BuyerAdminActionsSection(draftData: TradeItem, token: String) {
    val scope = rememberCoroutineScope()
    var deposited by remember { mutableStateOf(draftData.deposited == true) }
    var eTransferForwarded by remember { mutableStateOf(draftData.eTransferForwarded == true) }
    var eTransferForwardedDate by remember { mutableStateOf(draftData.eTransferForwardedDate) }
    var exchangeDeposited by remember { mutableStateOf(draftData.buyingDraftExchangeDeposited == true) }
    var actionResult by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun launch(action: suspend () -> Result<Unit>, label: String, onSuccess: () -> Unit = {}) {
        loading = true; actionResult = null
        scope.launch {
            val r = action()
            if (r is Result.Success) { actionResult = "$label ✓"; onSuccess() }
            else actionResult = "$label failed"
            loading = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
//        Text("Admin Actions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

        draftData.depositedDate?.let { InfoRow("Deposited on", it) }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            deposited = !deposited
        }) {
            androidx.compose.material3.Checkbox(checked = deposited, onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                checkedColor = GreenSold
            )
            )
            Text("Deposited", style = MaterialTheme.typography.bodySmall)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ArzookButton(
                onClick = {
                    draftData.buyingId?.let { buyingId ->
                        loading = true; actionResult = null
                        scope.launch {
                            val r = repo.adminForwardETransfers(token, buyingId)
                            if (r is Result.Success) {
                                actionResult = "Forward e-Transfer ✓"
                                // Re-fetch to get the real server timestamp
                                when (val updated = repo.getAdminBuyingDraftById(token, buyingId)) {
                                    is Result.Success -> {
                                        eTransferForwarded = true
                                        eTransferForwardedDate = updated.data.eTransferForwardedDate
                                    }
                                    else -> eTransferForwarded = true
                                }
                            } else actionResult = "Forward e-Transfer failed"
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                containerColor = GreenSold,
                contentColor = Color.White,
                modifier = Modifier
            ) {
                Text("Forward e-Transfer", style = MaterialTheme.typography.labelSmall)
            }
            if (eTransferForwarded) {
                Text(
                    "e-Transfer email forwarded on: ${eTransferForwardedDate ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
//                    color = Color(0xFF2E7D32)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            exchangeDeposited = !exchangeDeposited
        }) {
            androidx.compose.material3.Checkbox(checked = exchangeDeposited, onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenSold
                ))
            Text(" Exchange Deposited", style = MaterialTheme.typography.bodySmall)
        }

        ArzookButton(
            onClick = {
                draftData.buyingId?.let {
                    launch({ repo.adminDeactivateBuying(token, it) }, "Deactivate")
                }
            },
            enabled = !loading,
            modifier = Modifier
        ) {
            Text("Deactivate", style = MaterialTheme.typography.labelSmall, color = Color.Black)
        }

        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Yellow40)
        actionResult?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = if (it.endsWith("✓")) GreenDark else Color.Red)
        }
    }
}

@Composable
private fun AdminActionsSection(draftData: TradeItem, token: String, userId: String) {
    val scope = rememberCoroutineScope()
    var deposited by remember { mutableStateOf(draftData.deposited == true) }
    var exchangeDeposited by remember { mutableStateOf(draftData.exchangeDeposited == true) }
    var actionResult by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val filePicker = rememberFilePicker { bytes, name ->
        loading = true; actionResult = null
        scope.launch {
            val r = repo.adminUploadReceipt(token, userId, bytes, name)
            actionResult = if (r is Result.Success) "Receipt uploaded" else "Upload failed"
            loading = false
        }
    }

    fun launch(action: suspend () -> Result<Unit>, label: String) {
        loading = true; actionResult = null
        scope.launch {
            actionResult = if (action() is Result.Success) "$label ✓" else "$label failed"
            loading = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Admin Actions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

        // Checkboxes
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            if (!deposited && draftData.id != null) launch({ repo.adminMarkDeposited(token, draftData.id) }, "Deposited")
            deposited = !deposited
        }) {
            androidx.compose.material3.Checkbox(checked = deposited, onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenSold
                ))
            Text("Deposited", style = MaterialTheme.typography.bodySmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            if (!exchangeDeposited && draftData.id != null) launch({ repo.adminMarkExchangeDeposited(token, draftData.id) }, "Exchange Deposited")
            exchangeDeposited = !exchangeDeposited
        }) {
            androidx.compose.material3.Checkbox(checked = exchangeDeposited, onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenSold
                ))
            Text("Exchange Deposited", style = MaterialTheme.typography.bodySmall)
        }

        // Action buttons + upload icon
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ArzookButton(onClick = { draftData.id?.let { launch({ repo.adminTransferToWallet(token, it) }, "Transfer") } }, enabled = !loading,
                modifier = Modifier) {
                Text("Transfer to Wallet", style = MaterialTheme.typography.labelSmall)
            }
            ArzookButton(onClick = { draftData.id?.let { launch({ repo.adminComplete(token, it) }, "Complete") } }, enabled = !loading,
                modifier = Modifier) {
                Text("Complete", style = MaterialTheme.typography.labelSmall)
            }
            Icon(
                imageVector = Icons.Filled.Upload,
                contentDescription = "Upload Receipt",
                tint = Yellow40,
                modifier = Modifier.size(28.dp).clickable { filePicker.launch() }
            )
        }

        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Yellow40)
        actionResult?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = if (it.endsWith("✓") || it.contains("uploaded")) GreenDark else Color.Red) }
    }
}
