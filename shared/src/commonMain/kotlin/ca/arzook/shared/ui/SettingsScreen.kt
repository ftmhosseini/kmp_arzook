package ca.arzook.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import arzook.shared.generated.resources.Res
import arzook.shared.generated.resources.arzook_logo
import org.jetbrains.compose.resources.painterResource

val ALL_CURRENCIES = listOf("CAD", "IRR")

private val MENU_ITEMS = listOf(
//    "Learning Videos",
    "How It Works",
    "About Us",
    "FAQ",
    "Contact Us",
    "Privacy Policy",
    "Terms and Conditions"
)

@Composable
fun SettingsScreen(
    fromCurrency: String = ALL_CURRENCIES[0],
    toCurrency: String? = null,
    onSave: (from: String, to: String) -> Unit = { _, _ -> },
) {
    var pendingFrom by remember(fromCurrency) { mutableStateOf(fromCurrency) }
    var pendingTo by remember(toCurrency) { mutableStateOf<String?>(toCurrency) }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val toCurrencies =
        if (pendingFrom == "IRR") ALL_CURRENCIES.filter { it != "IRR" } else listOf("IRR")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Currency", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { fromExpanded = true }, modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(pendingFrom, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    ArzookDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }) {
                        ALL_CURRENCIES.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    if (currency != pendingFrom) {
                                        pendingFrom = currency
                                        pendingTo = null
                                    }
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }
                Text("To")
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { toExpanded = true }, modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(pendingTo ?: "Select", modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                    ArzookDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                        toCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    pendingTo = currency
                                    toExpanded = false
                                    onSave(pendingFrom, currency)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MenuScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.arzook_logo),
                contentDescription = "Arzook",
                modifier = Modifier.height(40.dp)
            )
        }
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(MENU_ITEMS) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(item) }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)
            }
            HorizontalDivider()
        }
    }
    }
}
