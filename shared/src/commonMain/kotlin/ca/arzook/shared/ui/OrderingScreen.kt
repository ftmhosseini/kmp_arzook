package ca.arzook.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderingScreen(
    token: String,
    homeViewModel: HomeViewModel,
    fromCurrency: String = ALL_CURRENCIES[0],
    toCurrency: String = ALL_CURRENCIES[1],
    onSaveSettings: (from: String, to: String) -> Unit = { _, _ -> },
    onAddBuying: () -> Unit = {},
    onAddSelling: () -> Unit = {},
) {
    val buyingDrafts by homeViewModel.buyingDrafts.collectAsState()
    val buyingTrades by homeViewModel.buyingTrades.collectAsState()
    val sellingDrafts by homeViewModel.sellingDrafts.collectAsState()
    val sellingTrades by homeViewModel.sellingTrades.collectAsState()
    val payees by homeViewModel.payees.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deleteKey by remember { mutableIntStateOf(0) }

    val activeMerged = remember(buyingDrafts, sellingDrafts, deleteKey) {
        (buyingDrafts + sellingDrafts).sortedByDescending { it.createdTime ?: "" }
    }
    val completedMerged = remember(buyingTrades, sellingTrades) {
        (buyingTrades + sellingTrades).sortedByDescending { it.exchangeDepositedTime ?: "" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Active") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Completed") })
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream40)
                .verticalScroll(scrollState)
        ) {
            when (tabIndex) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    ) {
                        SettingsScreen(
                            fromCurrency = fromCurrency,
                            toCurrency = toCurrency,
                            onSave = { f, t -> onSaveSettings(f, t) },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ArzookButton(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onClick = if (fromCurrency == "IRR") onAddBuying else onAddSelling
                            ) { Text(if (fromCurrency == "IRR") "Add New Buying" else "Add New Selling") }
                        }
                    }


                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    if (activeMerged.isEmpty()) {
                        EmptyOrderMessage("No active orders.")
                    } else {
                        activeMerged.forEach { item ->
                            if (item.code?.startsWith("S") == true) {
                                SellingDraftRow(
                                    draft = item,
                                    payees = payees,
                                    token = token,
                                    homeViewModel = homeViewModel,
                                    key = deleteKey,
                                    onDeleted = {
                                        deleteKey++
                                        scope.launch { snackbarHostState.showSnackbar("Selling deleted successfully") }
                                    }
                                )
                            } else {
                                BuyingDraftRow(
                                    draft = item,
                                    token = token,
                                    homeViewModel = homeViewModel,
                                    key = deleteKey,
                                    onDeleted = {
                                        deleteKey++
                                        scope.launch { snackbarHostState.showSnackbar("Buying deleted successfully") }
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    var showSearch by remember { mutableStateOf(false) }
                    var fromDate by remember { mutableStateOf("") }
                    var toDate by remember { mutableStateOf("") }
                    var filterType by remember { mutableStateOf("All") }
                    var showFromPicker by remember { mutableStateOf(false) }
                    var showToPicker by remember { mutableStateOf(false) }

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

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }

                    if (showSearch) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = fromDate,
                                    onValueChange = { fromDate = it },
                                    label = { Text("From Date", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                        .clickable { showFromPicker = true },
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
                                    onValueChange = { toDate = it },
                                    label = { Text("To Date", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                        .clickable { showToPicker = true },
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
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                ArzookButton(
                                    onClick = { filterType = if (filterType == "Selling") "All" else "Selling" },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    containerColor = if (filterType == "Selling") Brown else Yellow40,
                                    contentColor = if (filterType == "Selling") Color.White else Color.Black
                                ) { Text("Selling") }
                                ArzookButton(
                                    onClick = { filterType = if (filterType == "Buying") "All" else "Buying" },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    containerColor = if (filterType == "Buying") Brown else Yellow40,
                                    contentColor = if (filterType == "Buying") Color.White else Color.Black
                                ) { Text("Buying") }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    val filtered = remember(completedMerged, fromDate, toDate, filterType) {
                        completedMerged.filter { item ->
                            val matchesType = when (filterType) {
                                "Selling" -> item.sellingCode != null
                                "Buying" -> item.sellingCode == null
                                else -> true
                            }
                            val itemDate = (item.exchangeDepositedTime ?: "").take(10)
                            val matchesFrom = fromDate.isBlank() || itemDate >= fromDate
                            val matchesTo = toDate.isBlank() || itemDate <= toDate
                            matchesType && matchesFrom && matchesTo
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    if (filtered.isEmpty()) {
                        EmptyOrderMessage("No completed orders.")
                    } else {
                        filtered.forEach { item ->
                            if (item.sellingCode != null) {
                                CompletedSellingRow(trade = item, token = token)
                            } else {
                                CompletedBuyingRow(trade = item, token = token)
                            }
                        }
                    }
                }
            }
        }
//        }
    }
}

@Composable
private fun EmptyOrderMessage(msg: String) {
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

internal fun millisToDateString(millis: Long): String {
    // DatePicker returns millis at UTC midnight, convert to YYYY-MM-DD
    val days = millis / 86_400_000L
    // Calculate date from epoch days (1970-01-01)
    var y = 1970
    var remaining = days.toInt()
    while (true) {
        val daysInYear = if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 366 else 365
        if (remaining < daysInYear) break
        remaining -= daysInYear
        y++
    }
    val leap = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)
    val monthDays = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var m = 0
    while (remaining >= monthDays[m]) {
        remaining -= monthDays[m]
        m++
    }
    return "${y}-${(m + 1).toString().padStart(2, '0')}-${
        (remaining + 1).toString().padStart(2, '0')
    }"
}