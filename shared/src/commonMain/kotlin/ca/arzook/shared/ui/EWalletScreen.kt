package ca.arzook.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.DigitalWalletItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWalletScreen(
    deposits: List<DigitalWalletItem>,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showSearch by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "Back")
        }
        Text("e-Wallet", style = MaterialTheme.typography.titleMedium)
    }
    HorizontalDivider()
    if (showFromPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fromDate = millisToDateString(millis)
                    }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showToPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        toDate = millisToDateString(millis)
                    }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    val filteredDeposits = remember(deposits, fromDate, toDate) {
        deposits.filter { deposit ->
            (fromDate.isBlank() || deposit.date >= fromDate) &&
                    (toDate.isBlank() || deposit.date <= toDate)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Cream40)
            .verticalScroll(scrollState),
    ) {
        // Balance + deposit button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Cream80),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoRow("Current Balance", "${formatIrr(deposits.firstOrNull()?.balance ?: 0.0)} IRR")
                Icon(Icons.Default.Search, contentDescription = null,
                    modifier = Modifier.clickable { showSearch = !showSearch })
            }

            if (showSearch) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = {},
                        label = { Text("From Date", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).clickable { showFromPicker = true },
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showFromPicker = true }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pick date"
                                )
                            }
                        }
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = {},
                        label = { Text("To Date", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).clickable { showToPicker = true },
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showToPicker = true }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pick date"
                                )
                            }
                        }
                    )
                }
            }
//            Spacer(Modifier.height(12.dp))
//            Button(
//                onClick = {},
//                shape = RectangleShape,
//                colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
//            ) {
//                Text("New e-Wallet Deposit")
//            }
        }

        if (filteredDeposits.isEmpty()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmptyList)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Your e-Wallet is empty.", color = Color.White)
            }
        } else {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
            filteredDeposits.forEach { deposit ->
                DepositRow(deposit)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun DepositRow(deposit: DigitalWalletItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Cream80)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(deposit.code ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(deposit.date, style = MaterialTheme.typography.bodySmall)
            Text(deposit.description ?: "", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${if (deposit.code?.startsWith('B') == true) "-" else ""}${formatIrr(deposit.amount)}",
                color = if (deposit.code?.startsWith('B') == true) Brown else GreenSold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Column(Modifier.weight(1f)) {
            Text(formatIrr(deposit.balance), fontSize = 14.sp)
            Text("balance", style = MaterialTheme.typography.bodySmall)
        }
    }
}


