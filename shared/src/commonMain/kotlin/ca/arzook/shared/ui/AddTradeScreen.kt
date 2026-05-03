package ca.arzook.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.Result
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.launch

private val FormGreen = Color(0xFF4CAF50)

@Composable
fun AddSellingBuyingScreen(
    token: String,
    isSelling: Boolean = true,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    homeViewModel: HomeViewModel? = null
) {
    var amount by remember { mutableStateOf("") }
    var askingRate by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("CAD") }
    var purposeOfTransaction by remember { mutableStateOf("") }
    var sourceOfFund by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    var smartMatching by remember { mutableStateOf(true) }
    var total by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var promoStatus by remember { mutableStateOf("") }
    var promoValid by remember { mutableStateOf(false) }
    var makerServiceRate by remember { mutableStateOf<Double?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var createdDraft by remember { mutableStateOf<TradeItem?>(null) }
    var amountTouched by remember { mutableStateOf(false) }
    var rateTouched by remember { mutableStateOf(false) }
    var purposeTouched by remember { mutableStateOf(false) }
    var sourceTouched by remember { mutableStateOf(false) }

    val repo = remember { ArzookRepositoryImpl("https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    val currentRate = homeViewModel?.currentRate?.collectAsState()?.value

    val suggestedBase = currentRate?.let {
        if (isSelling) (it.currentMaxBuyingExchangeRate - it.userSellingRateOffset).toInt()
        else (it.currentMinSellingExchangeRate - it.userBuyingRateOffset).toInt()
    }

    val amountVal = amount.toDoubleOrNull()
    val rateVal = askingRate.toDoubleOrNull()

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Cream40)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            if (isSelling) "Add New Selling" else "Add New Buying",
            fontWeight = FontWeight.Bold, fontSize = 20.sp
        )
        HorizontalDivider()

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CAD", "USD").forEach { c ->
                    FilterChip(
                        selected = currency == c,
                        onClick = { currency = c },
                        label = { Text(c) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = amount.toDoubleOrNull()?.let { formatCad(it) } ?: amount,
            onValueChange = { amount = it.replace(",", ""); amountTouched = true },
            label = { Text("Amount ($currency)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountTouched && (amountVal == null || amountVal < 100),
            supportingText = if (amountTouched && (amountVal == null || amountVal < 100))
                { { Text("The minimum required amount is \$100.", color = MaterialTheme.colorScheme.error) } } else null,
            modifier = Modifier.fillMaxWidth()
        )

        val belowAverage = rateTouched && rateVal != null && currentRate != null && rateVal < currentRate.currentMaxAskingRate
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = askingRate.toDoubleOrNull()?.let { formatIrr(it) } ?: askingRate,
                onValueChange = { askingRate = it.replace(",", ""); rateTouched = true; total = "" },
                label = { Text("Asking Rate (IRR/$currency)") },
                isError = rateTouched && (rateVal == null || rateVal < 900000 || rateVal > 1400000),
                supportingText = when {
                    rateTouched && (rateVal == null || rateVal < 900000) -> { { Text("Must be more than 900,000 IRR.", color = MaterialTheme.colorScheme.error) } }
                    rateTouched && rateVal != null && rateVal > 1400000 -> { { Text("Must be less than 1,400,000 IRR.", color = MaterialTheme.colorScheme.error) } }
                    belowAverage -> { { Text("Below today's average (${formatIrr(currentRate!!.currentMaxAskingRate)} IRR)", color = Color(0xFFFF9800)) } }
                    else -> null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            if (suggestedBase != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { askingRate = suggestedBase.toString(); rateTouched = true; total = "" },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text(formatIrr(suggestedBase.toDouble()), fontSize = 10.sp) }
                    Text("Suggested", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        val displayTotal = if (amountVal != null && rateVal != null && total.isEmpty()) formatIrr(amountVal * rateVal) else total
        OutlinedTextField(
            value = displayTotal.toDoubleOrNull()?.let { formatIrr(it) } ?: displayTotal,
            onValueChange = { v ->
                total = v.replace(",", "")
                val t = total.toDoubleOrNull()
                if (t != null && amountVal != null && amountVal > 0) {
                    askingRate = (t / amountVal).toInt().toString()
                    rateTouched = true
                }
            },
            label = { Text("Total (IRR)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = purposeOfTransaction,
            onValueChange = { purposeOfTransaction = it; purposeTouched = true },
            label = { Text("Purpose of Transaction") },
            isError = purposeTouched && purposeOfTransaction.length < 5,
            supportingText = if (purposeTouched && purposeOfTransaction.length < 5)
                { { Text("At least 5 characters required.", color = MaterialTheme.colorScheme.error) } } else null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sourceOfFund,
            onValueChange = { sourceOfFund = it; sourceTouched = true },
            label = { Text("Source of Fund") },
            isError = sourceTouched && sourceOfFund.length < 5,
            supportingText = if (sourceTouched && sourceOfFund.length < 5)
                { { Text("At least 5 characters required.", color = MaterialTheme.colorScheme.error) } } else null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = promoCode,
            onValueChange = { v ->
                promoCode = v; promoStatus = ""; promoValid = false
                if (v.isNotBlank()) scope.launch {
                    when (val r = repo.validatePromoCode(token, v.trim())) {
                        is Result.Success -> {
                            val response = r.data
                            val startDate = response.startDate
                            if (!startDate.isNullOrEmpty() && startDate > getCurrentDateString()) {
                                promoValid = false; promoStatus = "The promotion will start on $startDate"; return@launch
                            }
                            promoValid = response.valid == true
                            promoStatus = when {
                                !promoValid && response.message != null -> response.message
                                promoValid -> "✓ Valid: ${response.discountPercentage?.let { "$it% discount" } ?: "Applied"}"
                                else -> "Invalid promo code"
                            }
                            if (promoValid && isSelling && amountVal != null) {
                                when (val sr = repo.getSellingMakerServiceRate(token, amountVal, v.trim())) {
                                    is Result.Success -> makerServiceRate = sr.data
                                    is Result.Error -> println("[AddTrade] makerServiceRate error: ${sr.message}")
                                }
                            }
                        }
                        is Result.Error -> { promoValid = false; promoStatus = "Invalid promo code" }
                    }
                }
            },
            label = { Text("Promo Code (optional)") },
            supportingText = if (promoStatus.isNotEmpty())
                { { Text(promoStatus, color = if (promoValid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error) } } else null,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) Text(errorMsg, color = MaterialTheme.colorScheme.error)

        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isSelling) {

                    Switch(checked = urgent, onCheckedChange = { urgent = it }, modifier = Modifier.scale(0.5f))
                    Text("Urgent")

            } else {
                    Switch(checked = smartMatching, onCheckedChange = { smartMatching = it }, modifier = Modifier.scale(0.5f))
                    Text("Smart Matching")
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Button(
                onClick = {
                    if (amountVal == null || rateVal == null) { errorMsg = "Enter valid amount and rate."; return@Button }
                    if (amountVal < 100) { errorMsg = "The minimum required amount is \$100."; return@Button }
                    if (rateVal < 900000) { errorMsg = "Must be more than 900,000 IRR."; return@Button }
                    if (rateVal > 1400000) { errorMsg = "Must be less than 1,400,000 IRR."; return@Button }
                    if (purposeOfTransaction.length < 5) { purposeTouched = true; errorMsg = "Purpose must be at least 5 characters."; return@Button }
                    if (sourceOfFund.length < 5) { sourceTouched = true; errorMsg = "Source of fund must be at least 5 characters."; return@Button }
                    if (currentRate == null) { errorMsg = "Rate data not loaded yet."; return@Button }
                    loading = true
                    scope.launch {
                        val svcRateResult = if (isSelling) repo.getServiceRateForSelling(token, amountVal)
                                            else repo.getServiceRateForBuying(token, amountVal)
                        val actualSvcRate = when (svcRateResult) {
                            is Result.Success -> svcRateResult.data
                            is Result.Error -> { errorMsg = "Failed to fetch service rate: ${svcRateResult.message}"; loading = false; return@launch }
                        }
                        val result = if (isSelling) {
                            repo.createSellingDraft(token, TradeItem(
                                amount = amountVal, askingRate = rateVal,
                                exchangeRate = rateVal + actualSvcRate, currency = currency,
                                purposeOfTransaction = purposeOfTransaction, sourceOfFund = sourceOfFund,
                                urgent = urgent
                            ))
                        } else {
                            repo.createBuyingDraft(token, TradeItem(
                                amount = amountVal, askingRate = rateVal,
                                exchangeRate = rateVal - actualSvcRate, currency = currency,
                                purposeOfTransaction = purposeOfTransaction, sourceOfFund = sourceOfFund,
                                smartMatchingEnabled = smartMatching
                            ))
                        }
                        if (isSelling) {
                            when (result) {
                                is Result.Success<*> -> { createdDraft = result.data as? TradeItem; showSuccessDialog = true }
                                is Result.Error<*> -> { errorMsg = result.message; loading = false }
                            }
                        } else {
                            when (result) {
                                is Result.Success<*> -> onSuccess()
                                is Result.Error<*> -> { errorMsg = result.message; loading = false }
                            }
                        }
                    }
                },
                enabled = !loading, shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = FormGreen),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Creating..." else "Create") }
                Button(
                    onClick = onBack, shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            }
        }

        if (urgent || !smartMatching) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1e3957)).padding(2.dp)) {
                Text(
                    if (urgent) "When you flag a Selling as urgent, only buyers with sufficient funds in their e-Wallet will be able to lock the Selling."
                    else "When Smart Matching is deactivated, the Buying will NOT be automatically matched with any subsequent Selling.",
                    color = Color.White, fontSize = 12.sp
                )
            }
        }
    }

    if (showSuccessDialog && createdDraft != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onSuccess() },
            title = { Text("Selling Created Successfully") },
            text = {
                Column {
                    Text("Please send ${formatCad(createdDraft!!.amount ?: 0.0)} ${createdDraft!!.currency} to:")
                    Spacer(Modifier.height(8.dp))
                    if (!createdDraft!!.arzookDepositEmail.isNullOrEmpty())
                        Text("Email: ${createdDraft!!.arzookDepositEmail}", fontWeight = FontWeight.Bold)
                    if (!createdDraft!!.eTransferPassword.isNullOrEmpty())
                        Text("Password: ${createdDraft!!.eTransferPassword}", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onSuccess() }) { Text("OK") }
            }
        )
    }
}
