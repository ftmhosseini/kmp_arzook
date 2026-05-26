package ca.arzook.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter

@Composable
fun AccountScreen(
    user: ca.arzook.shared.model.AuthenticatedData?,
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onWallet: () -> Unit,
    onMyBuying: () -> Unit,
    onMySelling: () -> Unit,
    onRateAlert: () -> Unit,
    onAdminWallet: () -> Unit,
    onConfirmDeposit: () -> Unit,
    onAdminCompletedTrades: () -> Unit = {},
    onOrdering: () -> Unit = {},
    fromCurrency: String = ALL_CURRENCIES[0],
    toCurrency: String = ALL_CURRENCIES[1],
    onSaveSettings: (from: String, to: String) -> Unit = { _, _ -> },
) {
//    var settingsExpanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // User info header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!user?.pictureUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(user!!.pictureUrl),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isLoggedIn) "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim().ifEmpty { "User" }
                               else "Not signed in",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isLoggedIn && !user?.email.isNullOrBlank()) {
                        Text(user!!.email!!, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            HorizontalDivider()
        }

        if (!isLoggedIn) {
            item {
                AccountMenuItem(Icons.Filled.Login, "Sign In", onClick = onLogin)
                HorizontalDivider()
            }
        } else {
            // Admin items
//            if (isAdmin) {
//                item {
//                    AccountMenuItem(Icons.Filled.AccountBalanceWallet, "Admin Wallet", onClick = onAdminWallet)
//                    HorizontalDivider()
//                    AccountMenuItem(Icons.Filled.ManageAccounts, "Confirm Deposit", onClick = onConfirmDeposit)
//                    HorizontalDivider()
//                    AccountMenuItem(Icons.Filled.Receipt, "Completed Trades", onClick = onAdminCompletedTrades)
//                    HorizontalDivider()
//                }
//            }

            item {
                AccountMenuItem(Icons.Filled.AccountBalanceWallet, "My Wallet", onClick = onWallet)
                HorizontalDivider()
//                AccountMenuItem(Icons.Filled.Receipt, "My Trades", onClick = onOrdering)
//                HorizontalDivider()
//                AccountMenuItem(Icons.Filled.ShoppingCart, "My Buying", onClick = onMyBuying)
//                HorizontalDivider()
//                AccountMenuItem(Icons.Filled.Sell, "My Selling", onClick = onMySelling)
//                HorizontalDivider()
                AccountMenuItem(Icons.Filled.RateReview, "Rate Alert", onClick = onRateAlert)
                HorizontalDivider()
                AccountMenuItem(Icons.Filled.ManageAccounts, "Profile", onClick = onProfile)
//                HorizontalDivider()
//                AccountMenuItem(
//                    icon = if (settingsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.Settings,
//                    title = "Settings",
//                    onClick = { settingsExpanded = !settingsExpanded }
//                )
//                if (settingsExpanded) {
//                    SettingsScreen(
//                        fromCurrency = fromCurrency,
//                        toCurrency = toCurrency,
//                        onSave = onSaveSettings
//                    )
//                }
                HorizontalDivider()
                AccountMenuItem(Icons.Filled.Logout, "Sign Out", tint = Brown, onClick = onLogout)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (tint == Color.Unspecified) LocalContentColor.current else tint)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = tint.takeIf { it != Color.Unspecified } ?: Color.Unspecified)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
    }
}
