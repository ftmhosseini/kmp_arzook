package ca.arzook.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arzook.shared.generated.resources.Res
import arzook.shared.generated.resources.ic_nav_logo
import arzook.shared.generated.resources.ic_nav_logo_white
import org.jetbrains.compose.resources.painterResource

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object Login : Screen()
    data object SignUp : Screen()
    data object Offering : Screen()
    data object Ordering : Screen()
    data object Buying : Screen()
    data object Selling : Screen()
    data object EWallet : Screen()
    data object MyBuying : Screen()
    data object MySelling : Screen()
    data object AddBuying : Screen()
    data object AddSelling : Screen()
    data object Profile : Screen()
    data object RateAlert : Screen()
    data object AdminWallet : Screen()
    data object ConfirmDeposit : Screen()
    data object AdminCompletedTrades : Screen()
    data object Account : Screen()
    data object Settings : Screen()
    data object SettingsPage : Screen()
    data object Menu : Screen()
    data class Content(val title: String) : Screen()
    data object LearningVideos : Screen()
}

@Composable
fun ArzookApp(
    authViewModel: AuthViewModel,
    initialFromCurrency: String = "CAD",
    initialToCurrency: String = "IRR",
    onCurrencySettingChanged: (from: String, to: String) -> Unit = { _, _ -> }
) {
    val isFabMenuOpen by remember { mutableStateOf(false) }
    val fabExpanded = remember { mutableStateOf(false) }
    val homeViewModel = remember { HomeViewModel() }
    val token by authViewModel.token.collectAsState()
    val buyingDrafts by homeViewModel.buyingDrafts.collectAsState()
    val buyingTrades by homeViewModel.buyingTrades.collectAsState()

    // Currency state: drives Offering screen behaviour
    var fromCurrency by remember { mutableStateOf(initialFromCurrency) }
    var toCurrency by remember { mutableStateOf(initialToCurrency) }

    // Back stack — Home is the root, Splash is the entry point
    val backStack = remember { mutableStateListOf<Screen>(Screen.Splash) }
    val screen by remember { derivedStateOf { backStack.last() } }

    fun navigate(s: Screen) {
        fabExpanded.value = false
        backStack.add(s)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeLast()
    }

    val showChrome = screen !is Screen.Splash


    LaunchedEffect(token) {
        val t = token
        if (!t.isNullOrEmpty()) {
            homeViewModel.loadWatchList(t)
            homeViewModel.loadUserData(t)
            authViewModel.loadUserDetails(t)
        }
    }

    // Re-load profile when navigating to Profile screen (handles Google login case where token was already set)
    LaunchedEffect(screen) {
        val t = token
        when (screen) {
            is Screen.Offering -> homeViewModel.refreshTrades()
            is Screen.Ordering -> if (!t.isNullOrEmpty()) homeViewModel.loadUserData(t)
            is Screen.Profile -> if (!t.isNullOrEmpty()) authViewModel.loadUserDetails(t)
            is Screen.MyBuying, is Screen.MySelling -> if (!t.isNullOrEmpty()) homeViewModel.loadUserData(
                t
            )

            else -> {}
        }
    }

    ArzookTheme {
        Scaffold(
            containerColor = Cream40,
            bottomBar = {
                if (showChrome) {
                    val logo = when {
                        isFabMenuOpen -> Res.drawable.ic_nav_logo   // disabled state
                        screen is Screen.Menu || screen is Screen.Content -> Res.drawable.ic_nav_logo_white // selected state
                        else -> Res.drawable.ic_nav_logo           // normal state
                    }
                    val alpha = if (isFabMenuOpen) 0.4f else 1f
                    NavigationBar(containerColor = Color.White.copy(alpha = alpha)) {
                        NavigationBarItem(
                            selected = screen is Screen.Account || screen is Screen.Profile || screen is Screen.RateAlert || screen is Screen.EWallet || screen is Screen.AdminWallet || screen is Screen.ConfirmDeposit
                                    || screen is Screen.Login || screen is Screen.SignUp,
                            onClick = {
                                if (!isFabMenuOpen) {
                                    backStack.clear(); backStack.add(Screen.Account)
                                }
                            },
                            enabled = !isFabMenuOpen,
                            icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                            label = { Text("Account") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ChosenMenu,
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.DarkGray
                            )
                        )
                        NavigationBarItem(
                            selected = screen is Screen.Offering,
                            onClick = {
                                if (!isFabMenuOpen) {
                                    backStack.clear(); backStack.add(Screen.Offering)
                                }
                            },
                            enabled = !isFabMenuOpen,
                            icon = {
                                Icon(
                                    Icons.Default.CurrencyExchange,
                                    contentDescription = null
                                )
                            },
                            label = { Text("Market") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ChosenMenu,
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.DarkGray
                            )
                        )
                        NavigationBarItem(
                            selected = screen is Screen.Ordering,
                            onClick = {
                                if (!isFabMenuOpen) {
                                    backStack.clear(); backStack.add(Screen.Ordering)
                                }
                            },
                            enabled = !isFabMenuOpen,
                            icon = {
                                Icon(Icons.Default.Receipt, contentDescription = null)

                            },
                            label = { Text("My Trades") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ChosenMenu,
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.DarkGray
                            )
                        )
                        NavigationBarItem(
                            selected = screen is Screen.Menu || screen is Screen.Content,
                            onClick = {
                                if (!isFabMenuOpen) {
                                    backStack.clear(); backStack.add(Screen.Menu)
                                }
                            },
                            enabled = !isFabMenuOpen,
                            icon = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        // Define the exact width and custom height you want for your indicator
                                        .then(
                                            if (screen is Screen.Menu || screen is Screen.Content) {
                                                Modifier
                                                    .size(width = 30.dp, height = 26.dp) // Default Material 3 height is usually ~32dp
                                                    .background(color = ChosenMenu, shape = RoundedCornerShape(12.dp))
                                            } else Modifier.size(26.dp)
                                        )
                                ) {
                                    Image(
                                        painter = painterResource(logo),
                                        contentDescription = "Arzook",
                                    )
                                }
                            },
                            label = { Text("Arzook",
                                modifier = Modifier.offset(y = (-2).dp)
                            ) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ChosenMenu,
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.DarkGray
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .background(Cream40)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { if (fabExpanded.value) fabExpanded.value = false }
            ) {
                when (val s = screen) {
                    is Screen.Splash -> SplashScreen { backStack.clear(); backStack.add(if (token.isNullOrEmpty()) Screen.Login else Screen.Offering) }
                    is Screen.Home -> HomeScreen(homeViewModel = homeViewModel)
                    is Screen.Login -> LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { backStack.clear(); backStack.add(Screen.Offering) },
                        onSignUp = { navigate(Screen.SignUp) })

                    is Screen.SignUp -> SignUpScreen(
                        viewModel = authViewModel,
                        onSuccess = { backStack.clear(); backStack.add(Screen.Home) },
                        onSignIn = { goBack() })

                    is Screen.Offering -> TradesScreen(
//                        title = "Offering",
                        initialFrom = fromCurrency,
                        initialTo = toCurrency,
                        onCurrencyChange = { f, t -> fromCurrency = f; toCurrency = t; onCurrencySettingChanged(f, t) },
                        token = token,
                        onLoginRequired = { navigate(Screen.Login) },
//                        onBuy = { navigate(Screen.MyBuying) },
//                        onSell = { navigate(Screen.MySelling) },
                        onBuy = { navigate(Screen.Ordering) },
                        onSell = { navigate(Screen.Ordering) },
                        homeViewModel = homeViewModel,
                        user = authViewModel.userDetails.collectAsState().value
                    )

                    is Screen.Buying -> TradesScreen(
//                        title = "Buying Trades",
                        isSelling = false,
                        token = token,
                        onLoginRequired = { navigate(Screen.Login) },
                        onBuy = { navigate(Screen.MyBuying) },
                        onSell = { navigate(Screen.MySelling) },
                        homeViewModel = homeViewModel,
                        user = authViewModel.userDetails.collectAsState().value
                    )

                    is Screen.Selling -> TradesScreen(
                        isSelling = true,
                        token = token,
                        onLoginRequired = { navigate(Screen.Login) },
                        onBuy = { navigate(Screen.MyBuying) },
                        onSell = { navigate(Screen.MySelling) },
                        homeViewModel = homeViewModel,
                        user = authViewModel.userDetails.collectAsState().value
                    )

                    is Screen.Ordering -> if (token.isNullOrEmpty()) navigate(Screen.Login) else OrderingScreen(
                        token = token ?: "",
                        homeViewModel = homeViewModel,
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        onSaveSettings = { f, t -> fromCurrency = f; toCurrency = t; onCurrencySettingChanged(f, t) },
                        onAddBuying = { navigate(Screen.AddBuying) },
                        onAddSelling = { navigate(Screen.AddSelling) }
                    )

                    is Screen.Content -> if (s.title == "Learning Videos") {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 4.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { goBack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "Back")
                                }
                                Text("Learning Videos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                            HorizontalDivider()
                            HomeScreen(homeViewModel = homeViewModel)
                        }
                    } else ContentScreen(
                        title = s.title,
                        onBack = { goBack() })

                    is Screen.EWallet -> Column(modifier = Modifier.fillMaxSize()) {
                        EWalletScreen(deposits = homeViewModel.deposits.collectAsState().value, onBack = { goBack() })
                    }
                    is Screen.MyBuying -> MyBuyingScreen(
                        drafts = buyingDrafts, completedTrades = buyingTrades,
                        onAddBuying = { navigate(Screen.AddBuying) },
                        token = token ?: "", homeViewModel = homeViewModel
                    )

                    is Screen.MySelling -> MySellingScreen(
                        drafts = homeViewModel.sellingDrafts.collectAsState().value,
                        completedTrades = homeViewModel.sellingTrades.collectAsState().value,
                        payees = homeViewModel.payees.collectAsState().value,
                        onAddSelling = { navigate(Screen.AddSelling) },
                        token = token ?: "", homeViewModel = homeViewModel
                    )

                    is Screen.AddBuying -> AddSellingBuyingScreen(
                        token = token ?: "", isSelling = false,
                        fromCurrency = fromCurrency, toCurrency = toCurrency,
                        onBack = { goBack() },
                        onSuccess = { homeViewModel.loadUserData(token ?: ""); goBack() },
                        homeViewModel = homeViewModel
                    )

                    is Screen.AddSelling -> AddSellingBuyingScreen(
                        token = token ?: "", isSelling = true,
                        fromCurrency = fromCurrency, toCurrency = toCurrency,
                        onBack = { goBack() },
                        onSuccess = { homeViewModel.loadUserData(token ?: ""); goBack() },
                        homeViewModel = homeViewModel
                    )

                    is Screen.Profile -> if (token.isNullOrEmpty()) navigate(Screen.Login) else Column(modifier = Modifier.fillMaxSize()) {
                        ProfileScreen(
                            user = authViewModel.userDetails.collectAsState().value,
                            isLoggedIn = !token.isNullOrEmpty(),
                            authViewModel = authViewModel,
                            onBack = { goBack() },
                            token = token ?: ""
                        )
                    }

                    is Screen.RateAlert -> if (token.isNullOrEmpty()) navigate(Screen.Login) else Column(modifier = Modifier.fillMaxSize()) {
                        RateAlertScreen(
                            onBack = { goBack() },
                            token = token ?: "",
                            homeViewModel = homeViewModel
                        )
                    }

                    is Screen.AdminWallet -> AdminWalletScreen(token = token ?: "")
                    is Screen.ConfirmDeposit -> ConfirmDepositScreen(token = token ?: "")
                    is Screen.AdminCompletedTrades -> AdminCompletedTradesScreen(token = token ?: "", onBack = { goBack() })
                    is Screen.Account -> AccountScreen(
                        user = authViewModel.userDetails.collectAsState().value,
                        isLoggedIn = !token.isNullOrEmpty(),
                        isAdmin = homeViewModel.isAdmin.collectAsState().value,
                        authViewModel = authViewModel,
//                        onBack = { goBack() },
                        onLogin = { navigate(Screen.Login) },
                        onLogout = { authViewModel.logout() },
                        onProfile = { navigate(Screen.Profile) },
                        onWallet = { navigate(Screen.EWallet) },
//                        onMyBuying = { navigate(Screen.MyBuying) },
//                        onMySelling = { navigate(Screen.MySelling) },
                        onRateAlert = { navigate(Screen.RateAlert) },
                        onAdminWallet = { navigate(Screen.AdminWallet) },
                        onConfirmDeposit = { navigate(Screen.ConfirmDeposit) },
                        onAdminCompletedTrades = { navigate(Screen.AdminCompletedTrades) },
//                        onOrdering = { navigate(Screen.Ordering) },
//                        fromCurrency = fromCurrency,
//                        toCurrency = toCurrency,
//                        onSaveSettings = { f, t -> fromCurrency = f; toCurrency = t }
                    )

                    is Screen.SettingsPage -> SettingsScreen(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        onSave = { f, t -> fromCurrency = f; toCurrency = t })

                    is Screen.Settings -> SettingsScreen(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        onSave = { f, t -> fromCurrency = f; toCurrency = t })

                    is Screen.Menu -> MenuScreen(onNavigate = { navigate(Screen.Content(it)) })
                    is Screen.LearningVideos -> HomeScreen(homeViewModel = homeViewModel)
                }
            }
        }
    }
}
