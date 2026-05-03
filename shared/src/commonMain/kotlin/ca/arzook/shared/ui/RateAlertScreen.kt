package ca.arzook.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RateAlertScreen(onBack: () -> Unit) {
    var sellingAlertEnabled by remember { mutableStateOf(false) }
    var buyingAlertEnabled by remember { mutableStateOf(false) }
    var minSellingRate by remember { mutableStateOf("") }
    var maxSellingRate by remember { mutableStateOf("") }
    var minSellingAmount by remember { mutableStateOf("") }
    var maxSellingAmount by remember { mutableStateOf("") }
    var minBuyingRate by remember { mutableStateOf("") }
    var maxBuyingRate by remember { mutableStateOf("") }
    var minBuyingAmount by remember { mutableStateOf("") }
    var maxBuyingAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Rate & Amount Alerts", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF8B4513))
        HorizontalDivider()

        // Selling Alerts
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = sellingAlertEnabled, onCheckedChange = { sellingAlertEnabled = it })
            Spacer(Modifier.width(8.dp))
            Text("Selling Alerts", fontWeight = FontWeight.SemiBold)
        }
        if (sellingAlertEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minSellingRate, onValueChange = { minSellingRate = it },
                    label = { Text("Min Selling Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxSellingRate, onValueChange = { maxSellingRate = it },
                    label = { Text("Max Selling Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minSellingAmount, onValueChange = { minSellingAmount = it },
                    label = { Text("Min Selling Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxSellingAmount, onValueChange = { maxSellingAmount = it },
                    label = { Text("Max Selling Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
            }
        }

        HorizontalDivider()

        // Buying Alerts
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = buyingAlertEnabled, onCheckedChange = { buyingAlertEnabled = it })
            Spacer(Modifier.width(8.dp))
            Text("Buying Alerts", fontWeight = FontWeight.SemiBold)
        }
        if (buyingAlertEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minBuyingRate, onValueChange = { minBuyingRate = it },
                    label = { Text("Min Buying Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxBuyingRate, onValueChange = { maxBuyingRate = it },
                    label = { Text("Max Buying Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = minBuyingAmount, onValueChange = { minBuyingAmount = it },
                    label = { Text("Min Buying Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
                OutlinedTextField(value = maxBuyingAmount, onValueChange = { maxBuyingAmount = it },
                    label = { Text("Max Buying Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = { onBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.weight(1f)) {
                Text("Save Alert Config", color = Color.Black)
            }
        }
    }
}
