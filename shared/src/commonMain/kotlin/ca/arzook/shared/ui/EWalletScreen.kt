package ca.arzook.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.arzook.shared.model.DigitalWalletItem

private val BrownFont = Color(0xFF8B4513)
private val GreenFont = Color(0xFF4CAF50)
private val Cream80 = Color(0xFFF5F0E8)
private val Blue20 = Color(0xFF1e3957)

@Composable
fun EWalletScreen(
    deposits: List<DigitalWalletItem>,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .background(Cream40)
            .verticalScroll(scrollState)
    ) {
        // Balance + deposit button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Cream80)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Current Balance: ", fontWeight = FontWeight.Bold)
                Text(
                    text = formatIrr(deposits.firstOrNull()?.balance?.toDouble() ?: 0.0),
                    color = BrownFont
                )
                Text(" IRR")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Yellow40, contentColor = Color.Black)
            ) {
                Text("New e-Wallet Deposit")
            }
        }

        if (deposits.isEmpty()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue20)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Your e-Wallet is empty.", color = Color.White)
            }
        } else {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
            deposits.forEach { deposit ->
                DepositRow(deposit)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
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
        Column(Modifier.weight(0.4f)) {
            Text(deposit.code ?: "", fontWeight = FontWeight.Bold)
            Text(deposit.date, style = MaterialTheme.typography.bodySmall)
            Text(deposit.description ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "${if (deposit.code?.startsWith('B') == true) "-" else ""}${formatIrr(deposit.amount)}",
            color = if (deposit.code?.startsWith('D') == true) GreenFont else BrownFont,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.3f)
        )
        Column(Modifier.weight(0.3f)) {
            Text(formatIrr(deposit.balance))
            Text("balance", style = MaterialTheme.typography.bodySmall)
        }
    }
}


