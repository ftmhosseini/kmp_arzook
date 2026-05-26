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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.Result
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.launch
import kotlin.math.round

@Composable
fun AddSellingBuyingScreen(
    token: String,
    isSelling: Boolean = true,
    fromCurrency: String = "CAD",
    toCurrency: String = "IRR",
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    homeViewModel: HomeViewModel? = null
) {
    // Derive available trading currencies from settings.
    // If one of the settings currencies is IRR, the other is the only valid trading currency.
    val availableCurrencies = remember(fromCurrency, toCurrency) {
        when {
            fromCurrency == "IRR" -> listOf(toCurrency)
            toCurrency == "IRR" -> listOf(fromCurrency)
            else -> listOf(fromCurrency, toCurrency)
        }
    }

    var amount by remember { mutableStateOf("") }
    var askingRate by remember { mutableStateOf("") }
    var currency by remember(availableCurrencies) { mutableStateOf(availableCurrencies.first()) }
    var purposeOfTransaction by remember { mutableStateOf("") }
    var sourceOfFund by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    var smartMatching by remember { mutableStateOf(true) }
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
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Cream40)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            if (isSelling) "Add New Selling" else "Add New Buying",
            fontWeight = FontWeight.Bold, fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        HorizontalDivider()

        if (availableCurrencies.size > 1) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableCurrencies.forEach { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text(c, fontSize = 10.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.replace(",", ""); amountTouched = true },
            label = { Text("Amount ($currency)", fontSize = 10.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountTouched && (amountVal == null || amountVal < 100),
            supportingText = if (amountTouched && (amountVal == null || amountVal < 100)) {
                {
                    Text(
                        "The minimum required amount is \$100.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        val belowAverage =
            rateTouched && rateVal != null && currentRate != null && rateVal < currentRate.currentMaxAskingRate
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = askingRate,
                onValueChange = {
                    askingRate = it.filter { c -> c.isDigit() }; rateTouched = true
                },
                visualTransformation = ThousandSeparatorTransformation,
                label = { Text("Asking Rate (IRR/$currency)", fontSize = 10.sp) },
                isError = rateTouched && (rateVal == null || rateVal < 900000 || rateVal > 1400000),
                supportingText = when {
                    rateTouched && (rateVal == null || rateVal < 900000) -> {
                        {
                            Text(
                                "Must be more than 900,000 IRR.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 0.dp).offset(y=(-4).dp)
                            )
                        }
                    }

                    rateTouched && rateVal != null && rateVal > 1400000 -> {
                        {
                            Text(
                                "Must be less than 1,400,000 IRR.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 0.dp).offset(y=(-4).dp)
                            )
                        }
                    }

                    belowAverage -> {
                        {
                            Text(
                                "Below today's average: ${formatIrr(currentRate!!.currentMaxAskingRate)} IRR",
                                color = Orange,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 0.dp).offset(y=(-4).dp, x=(-16).dp)
                            )
                        }
                    }

                    else -> null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            if (suggestedBase != null && currency == "CAD") {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-4).dp)) {
                    ArzookButton(
                        onClick = {
                            askingRate = suggestedBase.toString(); rateTouched = true
                        },
                        modifier = Modifier.offset(2.dp),
                        containerColor = ChosenMenu,
                        contentColor = Color.White
                    ) { Text(formatIrr(suggestedBase.toDouble()), fontSize = 10.sp) }
                    Text("Suggested", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        val computedTotal = if (amountVal != null && rateVal != null) (amountVal * rateVal).toLong() else null
        var fetchedSvcRate by remember { mutableStateOf<Double?>(null) }

        LaunchedEffect(amountVal, isSelling) {
            if (amountVal != null && amountVal >= 100) {
                val result = if (isSelling) repo.getServiceRateForSelling(token, amountVal)
                else repo.getServiceRateForBuying(token, amountVal)
                fetchedSvcRate = (result as? Result.Success)?.data
            } else fetchedSvcRate = null
        }
        if (computedTotal != null && fetchedSvcRate != null && rateVal != null)
        {
            println("[selling]: $fetchedSvcRate")
            println("[selling]: $rateVal")
            println("[selling]: ${(rateVal?.minus((fetchedSvcRate!! / 3)))?.toLong()}")

            println("[selling]: ${4.times((rateVal?.minus((fetchedSvcRate!! / 2))!!))}")
        }
        val computedComplianceFee = if (computedTotal != null && fetchedSvcRate != null && rateVal != null)
            if (isSelling) ((4 * (rateVal + (fetchedSvcRate!! / 2))).toLong()+5000)/10000*10000 else  ((4 * (rateVal - (fetchedSvcRate!! / 2))).toLong()+5000)/10000*10000  else null
        val computedNetPayout = if (computedTotal != null && computedComplianceFee != null)
            computedTotal - computedComplianceFee else null

        if (computedTotal != null) {
            OutlinedTextField(
                value = formatIrr(computedTotal.toDouble()),
                onValueChange = {},
                readOnly = true,
                label = { Text("Total (IRR)", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth()
            )
            if (computedComplianceFee != null) {
                OutlinedTextField(
                    value = formatIrr(computedComplianceFee.toDouble()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Compliance Fee (IRR)", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (computedNetPayout != null) {
                OutlinedTextField(
                    value = formatIrr(computedNetPayout.toDouble()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Net Payout (IRR, after Compliance Fee)", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = purposeOfTransaction,
            onValueChange = { purposeOfTransaction = it; purposeTouched = true },
            label = { Text("Purpose of Transaction", fontSize = 10.sp) },
            isError = purposeTouched && purposeOfTransaction.length < 5,
            supportingText = if (purposeTouched && purposeOfTransaction.length < 5) {
                { Text("At least 5 characters required.", color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sourceOfFund,
            onValueChange = { sourceOfFund = it; sourceTouched = true },
            label = { Text("Source of Fund", fontSize = 10.sp) },
            isError = sourceTouched && sourceOfFund.length < 5,
            supportingText = if (sourceTouched && sourceOfFund.length < 5) {
                { Text("At least 5 characters required.", color = MaterialTheme.colorScheme.error) }
            } else null,
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
                            val endDate = response.endDate
                            if (!startDate.isNullOrEmpty() && startDate > getCurrentDateString()) {
                                promoValid = false; promoStatus =
                                    "The promotion will start on $startDate"; return@launch
                            }
                            if (!endDate.isNullOrEmpty() && endDate < getCurrentDateString()) {
                                promoValid = false; promoStatus =
                                    "Promo code expired on $endDate"; return@launch
                            }
                            promoValid = response.valid ?: (response.code != null)
                            promoStatus = when {
                                !promoValid && response.message != null -> response.message
                                promoValid -> "✓ Valid: ${response.discountPercentage?.let { "$it% discount" } ?: "Applied"}"
                                else -> "Invalid promo code"
                            }
                            if (promoValid && isSelling && amountVal != null) {
                                when (val sr =
                                    repo.getSellingMakerServiceRate(token, amountVal, v.trim())) {
                                    is Result.Success -> makerServiceRate = sr.data
                                    is Result.Error -> println("[AddTrade] makerServiceRate error: ${sr.message}")
                                }
                            }
                        }

                        is Result.Error -> {
                            promoValid = false; promoStatus = "Invalid promo code"
                        }
                    }
                }
            },
            label = { Text("Promo Code (optional)", fontSize = 10.sp) },
            supportingText = if (promoStatus.isNotEmpty()) {
                {
                    Text(
                        promoStatus,
                        color = if (promoValid) GreenSuccess else MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) Text(errorMsg, color = MaterialTheme.colorScheme.error)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (isSelling) {

                    Switch(
                        checked = urgent,
                        onCheckedChange = { urgent = it },
                        modifier = Modifier.scale(0.5f),
                        colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                    )
                    Text("Urgent")

                } else {
                    Switch(
                        checked = smartMatching,
                        onCheckedChange = { smartMatching = it },
                        modifier = Modifier.scale(0.5f),
                        colors = SwitchDefaults.colors(checkedTrackColor = ChosenMenu)
                    )
                    Text("Smart Matching")
                }
            }
            Column(
                modifier = Modifier.weight(1f),//.padding(vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy((-4).dp)
            ) {
                ArzookButton(
                    onClick = {
                        if (amountVal == null || rateVal == null) {
                            errorMsg = "Enter valid amount and rate."; return@ArzookButton
                        }
                        if (amountVal < 100) {
                            errorMsg = "The minimum required amount is \$100."; return@ArzookButton
                        }
                        if (rateVal < 900000) {
                            errorMsg = "Must be more than 900,000 IRR."; return@ArzookButton
                        }
                        if (rateVal > 1400000) {
                            errorMsg = "Must be less than 1,400,000 IRR."; return@ArzookButton
                        }
                        if (purposeOfTransaction.length < 5) {
                            purposeTouched = true; errorMsg =
                                "Purpose must be at least 5 characters."; return@ArzookButton
                        }
                        if (sourceOfFund.length < 5) {
                            sourceTouched = true; errorMsg =
                                "Source of fund must be at least 5 characters."; return@ArzookButton
                        }
                        if (currentRate == null) {
                            errorMsg = "Rate data not loaded yet."; return@ArzookButton
                        }
                        loading = true
                        scope.launch {
                            val svcRateResult =
                                if (isSelling) repo.getServiceRateForSelling(token, amountVal)
                                else repo.getServiceRateForBuying(token, amountVal)
                            val actualSvcRate = when (svcRateResult) {
                                is Result.Success -> svcRateResult.data
                                is Result.Error -> {
                                    errorMsg =
                                        "Failed to fetch service rate: ${svcRateResult.message}"; loading =
                                        false; return@launch
                                }
                            }
                            val total = amountVal * rateVal
                            val result = if (isSelling) {
                                repo.createSellingDraft(
                                    token, TradeItem(
                                        amount = amountVal,
                                        askingRate = rateVal,
                                        total = total,
                                        serviceRate = actualSvcRate,
                                        exchangeRate = rateVal + actualSvcRate,
                                        currency = currency,
                                        purposeOfTransaction = purposeOfTransaction,
                                        sourceOfFund = sourceOfFund,
                                        urgent = urgent
                                    )
                                )
                            } else {
                                repo.createBuyingDraft(
                                    token, TradeItem(
                                        amount = amountVal,
                                        askingRate = rateVal,
                                        total = total,
                                        serviceRate = actualSvcRate,
                                        exchangeRate = rateVal - actualSvcRate,
                                        currency = currency,
                                        purposeOfTransaction = purposeOfTransaction,
                                        sourceOfFund = sourceOfFund,
                                        smartMatchingEnabled = smartMatching
                                    )
                                )
                            }
                            if (isSelling) {
                                when (result) {
                                    is Result.Success<*> -> {
                                        createdDraft =
                                            result.data as? TradeItem; showSuccessDialog = true
                                    }

                                    is Result.Error<*> -> {
                                        errorMsg = result.message; loading = false
                                    }
                                }
                            } else {
                                when (result) {
                                    is Result.Success<*> -> onSuccess()
                                    is Result.Error<*> -> {
                                        errorMsg = result.message; loading = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    containerColor = GreenSold,
                    contentColor = Color.White
                ) { Text(if (loading) "Creating..." else "Create") }
                ArzookButton(
                    onClick = onBack,
                    containerColor = Brown,
                    contentColor = Color.White
                ) { Text("Cancel") }
            }
        }

        if (urgent || !smartMatching) {
            Row(modifier = Modifier.fillMaxWidth().background(EmptyList).padding(2.dp)) {
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
                        Text(
                            "Email: ${createdDraft!!.arzookDepositEmail}",
                            fontWeight = FontWeight.Bold
                        )
                    if (!createdDraft!!.eTransferPassword.isNullOrEmpty())
                        Text(
                            "Password: ${createdDraft!!.eTransferPassword}",
                            fontWeight = FontWeight.Bold
                        )
                    Text(
                        "Message: ${createdDraft!!.code}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onSuccess() }) { Text("OK") }
            }
        )
    }
}
