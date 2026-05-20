package ca.arzook.shared.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter

data class MiniFabItem(val icon: ImageVector, val title: String)

@Composable
fun FabUI(
    token: String?,
    pictureUrl: String? = null,
    expanded: MutableState<Boolean>,
    onExpandedChange: (Boolean) -> Unit = {},
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    onMyBuying: () -> Unit,
    onMySelling: () -> Unit,
    onProfile: () -> Unit,
    onWallet: () -> Unit = {},
    onRateAlert: () -> Unit = {},
    isAdmin: Boolean = false,
    onAdminWallet: () -> Unit = {},
    onConfirmDeposit: () -> Unit = {},
) {
    LaunchedEffect(expanded.value) { onExpandedChange(expanded.value) }
    var fabItems:List<MiniFabItem>? = null
    fabItems = if (token.isNullOrEmpty()) null else buildList {
        if (isAdmin) {
            add(MiniFabItem(Icons.Filled.AccountBalanceWallet, "Admin Wallet"))
            add(MiniFabItem(Icons.Filled.ManageAccounts, "Confirm Deposit"))
        }
        add(MiniFabItem(Icons.Filled.AccountBalanceWallet, "My Wallet"))
        add(MiniFabItem(Icons.Filled.ShoppingCart, "My Buying"))
        add(MiniFabItem(Icons.Filled.Sell, "My Selling"))
        add(MiniFabItem(Icons.Filled.RateReview, "Rate Alert"))
        add(MiniFabItem(Icons.Filled.ManageAccounts, "Profile"))
        add(MiniFabItem(Icons.Filled.Logout, "Sign out"))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = expanded.value && fabItems != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }) + expandVertically(),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }) + shrinkVertically()
        ) {
            LazyColumn(Modifier.offset(y = 40.dp)) {
                fabItems?.let { list ->
                    items(list.size) { i ->
                        FloatingActionButton(
                            onClick = {
                                expanded.value = false
                                when (list[i].title) {
                                    "Sign out" -> onLogout()
                                    "Profile" -> onProfile()
                                    "My Wallet" -> onWallet()
                                    "My Buying" -> onMyBuying()
                                    "My Selling" -> onMySelling()
                                    "Rate Alert" -> onRateAlert()
                                    "Admin Wallet" -> onAdminWallet()
                                    "Confirm Deposit" -> onConfirmDeposit()
                                }
                            },
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).wrapContentWidth(),
                            containerColor = Orange
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("  ${list[i].title}", modifier = Modifier.padding(vertical = 16.dp))
                                Icon(list[i].icon, contentDescription = "", modifier = Modifier.padding(6.dp).size(45.dp))
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }

        val transition = updateTransition(targetState = expanded, label = "fab")
        val rotation by transition.animateFloat(label = "rotation") { if (it.value) 360f else 0f }

        FloatingActionButton(
            shape = CircleShape,
            onClick = {
                if (token.isNullOrEmpty()) onLoginClick()
                else expanded.value = !expanded.value
            },
            containerColor = Yellow40,
            modifier = Modifier.offset(y = 40.dp)
        ) {
            if (!pictureUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(pictureUrl),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).rotate(rotation).clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = if (token.isNullOrEmpty()) Icons.Filled.Lock else Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation).clip(CircleShape)
                )
            }
        }
    }
}
