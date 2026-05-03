package ca.arzook.shared.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object Login : Screen()
    data object SignUp : Screen()
    data object Buying : Screen()
    data object Selling : Screen()
    data object EWallet : Screen()
    data object MyBuying : Screen()
    data object MySelling : Screen()
    data object AddBuying : Screen()
    data object AddSelling : Screen()
    data object Profile : Screen()
    data object RateAlert : Screen()
    data class Content(val title: String) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArzookApp(authViewModel: AuthViewModel) {
    val homeViewModel = remember { HomeViewModel() }
    val token by authViewModel.token.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val showChrome = screen !is Screen.Splash

    val menuItems = listOf("How It Works", "About Us", "FAQ", "Contact Us", "Privacy Policy", "Terms and Conditions")
    var menuExpanded by remember { mutableStateOf(false) }

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
        if (!t.isNullOrEmpty()) {
            when (screen) {
                is Screen.Profile -> authViewModel.loadUserDetails(t)
                is Screen.MyBuying, is Screen.MySelling -> homeViewModel.loadUserData(t)
                else -> {}
            }
        }
    }

    ArzookTheme {
        Scaffold(
            floatingActionButton = {
                if (showChrome) {
                    FabUI(
                        token = token,
                        onLoginClick = { screen = Screen.Login },
                        onLogout = { authViewModel.logout() },
                        onMyBuying = { screen = Screen.MyBuying },
                        onMySelling = { screen = Screen.MySelling },
                        onProfile = { screen = Screen.Profile },
                        onWallet = { screen = Screen.EWallet },
                        onRateAlert = { screen = Screen.RateAlert }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            topBar = {
                if (showChrome) {
                    val pageTitle = when (val s = screen) {
                        is Screen.Buying -> "Buying"
                        is Screen.Selling -> "Selling"
                        is Screen.Content -> s.title
                        is Screen.Login -> "Sign In"
                        is Screen.SignUp -> "Sign Up"
                        else -> "Arzook"
                    }
                    TopAppBar(
                        navigationIcon = {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                                }
                                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                    menuItems.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = { menuExpanded = false; screen = Screen.Content(item) }
                                        )
                                    }
                                }
                            }
                        },
                        title = { Text(pageTitle, style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            TopBarLogo { screen = Screen.Home }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream40)
                    )
                }
            },
            bottomBar = {
                if (showChrome) {
                    NavigationBar(containerColor = Cream40) {
                        NavigationBarItem(
                            selected = screen is Screen.Buying,
                            onClick = { screen = Screen.Buying },
                            icon = { Text("💰") },
                            label = { Text("Buying") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Yellow40, selectedIconColor = Color.Black, unselectedIconColor = Color.DarkGray)
                        )
                        NavigationBarItem(
                            selected = screen is Screen.Selling,
                            onClick = { screen = Screen.Selling },
                            icon = { Text("💱") },
                            label = { Text("Selling") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Yellow40, selectedIconColor = Color.Black, unselectedIconColor = Color.DarkGray)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val s = screen) {
                    is Screen.Splash -> SplashScreen { screen = Screen.Home }
                    is Screen.Home -> HomeScreen(
                        homeViewModel = homeViewModel
                    )
                    is Screen.Login -> LoginScreen(viewModel = authViewModel, onLoginSuccess = { screen = Screen.Home }, onSignUp = { screen = Screen.SignUp })
                    is Screen.SignUp -> SignUpScreen(viewModel = authViewModel, onSuccess = { screen = Screen.Home }, onSignIn = { screen = Screen.Login })
                    is Screen.Buying -> TradesScreen(
                        title = "Buying Trades",
                        isSelling = false,
                        token = token,
                        onLoginRequired = { screen = Screen.Login },
                        onBuy = { screen = Screen.MyBuying },
                        onSell = { screen = Screen.MySelling },
                        homeViewModel = homeViewModel,
                        user = authViewModel.userDetails.collectAsState().value
                    )
                    is Screen.Selling -> TradesScreen(
                        title = "Selling Trades",
                        isSelling = true,
                        token = token,
                        onLoginRequired = { screen = Screen.Login },
                        onBuy = { screen = Screen.MyBuying },
                        onSell = { screen = Screen.MySelling },
                        homeViewModel = homeViewModel,
                        user = authViewModel.userDetails.collectAsState().value
                    )
                    is Screen.Content -> ContentScreen(
                        title = s.title,
                        onBack = { screen = Screen.Home }
                    )
                    is Screen.EWallet -> EWalletScreen(
                        deposits = homeViewModel.deposits.collectAsState().value
                    )
                    is Screen.MyBuying -> MyBuyingScreen(
                        drafts = homeViewModel.buyingDrafts.collectAsState().value,
                        completedTrades = homeViewModel.buyingTrades.collectAsState().value,
                        onAddBuying = { screen = Screen.AddBuying },
                        token = token ?: "",
                        homeViewModel = homeViewModel
                    )
                    is Screen.MySelling -> MySellingScreen(
                        drafts = homeViewModel.sellingDrafts.collectAsState().value,
                        completedTrades = homeViewModel.sellingTrades.collectAsState().value,
                        payees = homeViewModel.payees.collectAsState().value,
                        onAddSelling = { screen = Screen.AddSelling },
                        token = token ?: "",
                        homeViewModel = homeViewModel
                    )
                    is Screen.AddBuying -> AddSellingBuyingScreen(
                        token = token ?: "",
                        isSelling = false,
                        onBack = { screen = Screen.MyBuying },
                        onSuccess = { homeViewModel.loadUserData(token ?: ""); screen = Screen.MyBuying },
                        homeViewModel = homeViewModel
                    )
                    is Screen.AddSelling -> AddSellingBuyingScreen(
                        token = token ?: "",
                        isSelling = true,
                        onBack = { screen = Screen.MySelling },
                        onSuccess = { homeViewModel.loadUserData(token ?: ""); screen = Screen.MySelling },
                        homeViewModel = homeViewModel
                    )
                    is Screen.Profile -> ProfileScreen(
                        user = authViewModel.userDetails.collectAsState().value,
                        isLoggedIn = !token.isNullOrEmpty(),
                        authViewModel = authViewModel
                    )
                    is Screen.RateAlert -> RateAlertScreen(onBack = { screen = Screen.Home })
                }
            }
        }
    }
}
