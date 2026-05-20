package ca.arzook.shared.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import ca.arzook.shared.model.RateAlert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RateAlertScreen(onBack: () -> Unit, token: String = "", homeViewModel: HomeViewModel? = null) {
    val rateAlert by homeViewModel?.rateAlert?.collectAsState() ?: remember { mutableStateOf(null) }

    var sellingAlertEnabled by remember(rateAlert) { mutableStateOf(rateAlert?.sellingEnabled == true) }
    var buyingAlertEnabled by remember(rateAlert) { mutableStateOf(rateAlert?.buyingEnabled == true) }
    var minSellingRate by remember(rateAlert) { mutableStateOf(rateAlert?.minSellingRate?.toInt()?.toString() ?: "") }
    var maxSellingRate by remember(rateAlert) { mutableStateOf(rateAlert?.maxSellingRate?.toInt()?.toString() ?: "") }
    var minSellingAmount by remember(rateAlert) { mutableStateOf(rateAlert?.minSellingAmount?.toInt()?.toString() ?: "") }
    var maxSellingAmount by remember(rateAlert) { mutableStateOf(rateAlert?.maxSellingAmount?.toInt()?.toString() ?: "") }
    var minBuyingRate by remember(rateAlert) { mutableStateOf(rateAlert?.minBuyingRate?.toInt()?.toString() ?: "") }
    var maxBuyingRate by remember(rateAlert) { mutableStateOf(rateAlert?.maxBuyingRate?.toInt()?.toString() ?: "") }
    var minBuyingAmount by remember(rateAlert) { mutableStateOf(rateAlert?.minBuyingAmount?.toInt()?.toString() ?: "") }
    var maxBuyingAmount by remember(rateAlert) { mutableStateOf(rateAlert?.maxBuyingAmount?.toInt()?.toString() ?: "") }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

// Define your colors based on the press state
    val buttonColor = if (isPressed) ChosenMenu else Yellow40
    val textColor = if (isPressed) Color.White else Color.Black
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Rate & Amount Alerts", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Brown)
        Spacer(Modifier.height(24.dp))
        homeViewModel?.let { Chart(homeViewModel = it) }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        // Selling Alerts
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(modifier = Modifier.scale(.5f),
                checked = sellingAlertEnabled,
                onCheckedChange = { sellingAlertEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Yellow40,      // The background when ON
                )
            )
            Text("Selling Alerts", fontWeight = FontWeight.SemiBold)
        }
        if (sellingAlertEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minSellingRate, onValueChange = { minSellingRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Min Selling Rate", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxSellingRate, onValueChange = { maxSellingRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Selling Rate", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minSellingAmount, onValueChange = { minSellingAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Min Selling Amount", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxSellingAmount, onValueChange = { maxSellingAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Selling Amount", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
        }

        HorizontalDivider()

        // Buying Alerts
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = buyingAlertEnabled, onCheckedChange = { buyingAlertEnabled = it },
                modifier = Modifier.scale(.5f),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Yellow40,      // The background when ON
                ))
            Text("Buying Alerts", fontWeight = FontWeight.SemiBold)
        }
        if (buyingAlertEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minBuyingRate, onValueChange = { minBuyingRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Min Buying Rate", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxBuyingRate, onValueChange = { maxBuyingRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Buying Rate", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minBuyingAmount, onValueChange = { minBuyingAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Min Buying Amount", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxBuyingAmount, onValueChange = { maxBuyingAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Buying Amount", fontSize = 10.sp) },
                    visualTransformation = ThousandSeparatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            ArzookButton(onClick = {
                    homeViewModel?.saveRateAlerts(
                        token,
                        RateAlert(
                            sellingEnabled = if (sellingAlertEnabled) true else null,
                            minSellingRate = minSellingRate.toDoubleOrNull(),
                            maxSellingRate = maxSellingRate.toDoubleOrNull(),
                            minSellingAmount = minSellingAmount.toDoubleOrNull(),
                            maxSellingAmount = maxSellingAmount.toDoubleOrNull(),
                            buyingEnabled = if (buyingAlertEnabled) true else null,
                            minBuyingRate = minBuyingRate.toDoubleOrNull(),
                            maxBuyingRate = maxBuyingRate.toDoubleOrNull(),
                            minBuyingAmount = minBuyingAmount.toDoubleOrNull(),
                            maxBuyingAmount = maxBuyingAmount.toDoubleOrNull()
                        ),
                        onSuccess = { onBack() }
                    ) ?: onBack()
                },
                containerColor = buttonColor,
                contentColor = textColor,
                modifier = Modifier.weight(1f)) {
                Text("Save Alert Config")
            }
        }
    }
}
