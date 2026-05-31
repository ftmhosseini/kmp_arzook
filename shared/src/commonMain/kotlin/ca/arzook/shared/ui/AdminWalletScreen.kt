package ca.arzook.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import ca.arzook.shared.model.DigitalWalletItem
import ca.arzook.shared.model.DigitalWalletItemType
import ca.arzook.shared.repository.ArzookRepositoryImpl
import kotlinx.coroutines.launch

private val repo = ArzookRepositoryImpl(baseUrl = "https://api.arzook.ca")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWalletScreen(token: String) {
    val scope = rememberCoroutineScope()

    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<DigitalWalletItemType?>(null) }
    var bank by remember { mutableStateOf("") }

    var itemTypes by remember { mutableStateOf<List<DigitalWalletItemType>>(emptyList()) }
    var walletItems by remember { mutableStateOf<List<DigitalWalletItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf(false) }
    var addNew by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // New item form fields
    var newAmount by remember { mutableStateOf("") }
    var newCustomer by remember { mutableStateOf("") }
    var newTrackingNumber by remember { mutableStateOf("") }
    var newSelectedType by remember { mutableStateOf<DigitalWalletItemType?>(null) }
    var newTypeDropdownExpanded by remember { mutableStateOf(false) }
    var newBank by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        when (val r = repo.getAdminWalletItemTypes(token)) {
            is Result.Success -> itemTypes = r.data
            is Result.Error -> println("[AdminWallet] types error: ${r.message}")
        }
        when (val r = repo.getAdminWalletItems(token)) {
            is Result.Success -> walletItems = r.data
            is Result.Error -> println("[AdminWallet] items error: ${r.message}")
        }
    }

    fun search() {
        scope.launch {
            loading = true
            when (val r = repo.getAdminWalletItems(
                token = token,
                fromDate = fromDate.ifBlank { null },
                toDate = toDate.ifBlank { null },
                customer = customer.ifBlank { null },
                type = selectedType?.code,
                bank = bank.ifBlank { null }
            )) {
                is Result.Success -> walletItems = r.data
                is Result.Error -> println("[AdminWallet] search error: ${r.message}")
            }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Cream40)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArzookButton(onClick = { search = !search; addNew = false }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Search")
            }
            ArzookButton(onClick = { addNew = !addNew; search = false }, modifier = Modifier.weight(1f), containerColor = Blue40, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("New Item")
            }
        }
        if(search){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Cream80)
                    .padding(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fromDate, onValueChange = { fromDate = it }, label = { Text("From Date", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = toDate, onValueChange = { toDate = it }, label = { Text("To Date", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Customer", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = typeDropdownExpanded, onExpandedChange = { typeDropdownExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                        DropdownMenuItem(text = { Text("All") }, onClick = { selectedType = null; typeDropdownExpanded = false })
                        itemTypes.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name ?: t.code ?: "") }, onClick = { selectedType = t; typeDropdownExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Bank", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                ArzookButton(onClick = { search() }) {
                    Text("Apply")
                }
            }
        }
        if(addNew){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Cream80)
                    .padding(12.dp)
            ) {
                OutlinedTextField(value = newAmount, onValueChange = { newAmount = it }, label = { Text("Amount", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newCustomer, onValueChange = { newCustomer = it }, label = { Text("Customer", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = newTypeDropdownExpanded, onExpandedChange = { newTypeDropdownExpanded = it }) {
                    OutlinedTextField(
                        value = newSelectedType?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = newTypeDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = newTypeDropdownExpanded, onDismissRequest = { newTypeDropdownExpanded = false }) {
                        itemTypes.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name ?: t.code ?: "") }, onClick = { newSelectedType = t; newTypeDropdownExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newBank, onValueChange = { newBank = it }, label = { Text("Bank", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newTrackingNumber, onValueChange = { newTrackingNumber = it }, label = { Text("Tracking Number", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                ArzookButton(onClick = { /* TODO: submit new item */ }, containerColor = Blue40, contentColor = Color.White) {
                    Text("Submit")
                }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow40)
            }
        } else if (walletItems.isEmpty()) {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Blue40).padding(20.dp), contentAlignment = Alignment.Center) {
                Text("No items found.", color = Color.White)
            }
        } else {
            LazyColumn {
                items(walletItems) { item ->
                    AdminWalletItemRow(item)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun AdminWalletItemRow(item: DigitalWalletItem) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.background(Cream40).clickable { expanded = !expanded }.padding(12.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                            Text(item.userFullName ?: "", fontWeight = FontWeight.Bold)
                            Text(formatIrr(item.amount), fontWeight = FontWeight.Bold)
                            Text(formatIrr(item.balance), fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = expanded) {
                    Column(Modifier.weight(0.4f)) {
                        Text("Deposit id: ${item.customerDepositId.toString()}", fontWeight = FontWeight.Bold)
                        Text("code: ${item.code}", style = MaterialTheme.typography.bodySmall)
                        Text("amount: ${item.amount}", style = MaterialTheme.typography.bodySmall)
                        Text("date: ${item.date}", style = MaterialTheme.typography.bodySmall)
                        Text("description: ${item.description}", style = MaterialTheme.typography.bodySmall)
                        Text("balance: ${item.balance}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
