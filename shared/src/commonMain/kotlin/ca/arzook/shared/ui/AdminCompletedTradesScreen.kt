package ca.arzook.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.Result
import ca.arzook.shared.model.TradeItem
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCompletedTradesScreen(token: String, onBack: () -> Unit) {
    val repo = remember { ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca") }
    val scope = rememberCoroutineScope()
    var tabIndex by remember { mutableIntStateOf(0) }
    var buyingTrades by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var sellingTrades by remember { mutableStateOf<List<TradeItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(token) {
        loading = true
        scope.launch {
            when (val r = repo.getAdminBuyingTrades(token)) {
                is Result.Success -> buyingTrades = r.data
                else -> {}
            }
            when (val r = repo.getAdminSellingTrades(token)) {
                is Result.Success -> sellingTrades = r.data
                else -> {}
            }
            loading = false
        }
    }

    var showSearch by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fromDate = millisToDateString(it) }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showToPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { toDate = millisToDateString(it) }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Completed Trades", style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()

        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Buying") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Selling") })
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentList = if (tabIndex == 0) buyingTrades else sellingTrades

            Column(
                modifier = Modifier.fillMaxSize().background(Cream40).verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                                modifier = Modifier.weight(1f).clickable { showFromPicker = true },
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showFromPicker = true }) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Pick date")
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = toDate,
                                onValueChange = { toDate = it },
                                label = { Text("To Date", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f).clickable { showToPicker = true },
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showToPicker = true }) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Pick date")
                                    }
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                val filtered = remember(currentList, fromDate, toDate) {
                    currentList.filter { item ->
                        val itemDate = (item.exchangeDepositedTime ?: "").take(10)
                        val matchesFrom = fromDate.isBlank() || itemDate >= fromDate
                        val matchesTo = toDate.isBlank() || itemDate <= toDate
                        matchesFrom && matchesTo
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)

                if (filtered.isEmpty()) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)).background(EmptyList).padding(20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) { Text("No completed trades.", color = Color.White) }
                } else {
                    filtered.forEach { item ->
                        if (tabIndex == 1) {
                            CompletedSellingRow(trade = item, token = token)
                        } else {
                            CompletedBuyingRow(trade = item, token = token)
                        }
                    }
                }
            }
        }
    }
}
