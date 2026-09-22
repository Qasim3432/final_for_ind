package com.example.final_for_ind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.TransactionLog
import com.example.final_for_ind.screens.component.DashboardScreen
import com.example.final_for_ind.screens.component.GiftScreen
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.login_frame.First_Screen
import com.example.final_for_ind.screens.login_frame.auth.AuthScreenRouter
import com.example.final_for_ind.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = GameSessionManager(applicationContext)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        sessionManager = sessionManager,
                        onPerformSubmit = { amount, method, senderName ->
                            lifecycleScope.launch {
                                val success = sessionManager.submitDepositNotification(amount, method, senderName)
                                android.util.Log.d("APP_NAV", "Was database save successful? $success")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    sessionManager: GameSessionManager,
    onPerformSubmit: (Int, String, String) -> Unit
) {
    val navController = rememberNavController()
    val composeScope = rememberCoroutineScope()

    // Agar pehle se login hai to seedha home, warna first screen
    val startRoute = remember {
        if (sessionManager.isEmailLoggedIn()) "home" else "first_screen_route"
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        // Route 1: Welcome / Entry Option Screen
        composable("first_screen_route") {
            First_Screen(
                onPlayClick = {
                    // Pehle auth check karo
                    if (sessionManager.isEmailLoggedIn()) {
                        navController.navigate("home") {
                            popUpTo("first_screen_route") { inclusive = true }
                        }
                    } else {
                        navController.navigate("auth_flow") {
                            popUpTo("first_screen_route") { inclusive = true }
                        }
                    }
                },
                sessionManager = sessionManager
            )
        }

        // Route: Email Auth
        composable("auth_flow") {
            AuthScreenRouter(
                sessionManager = sessionManager,
                onAuthComplete = {
                    navController.navigate("home") {
                        popUpTo("auth_flow") { inclusive = true }
                    }
                }
            )
        }

        // Route 2: Home Screen Lobby Layout
        composable("home") {
            HomeScreen(
                sessionManager = sessionManager,
                onNavigateToDeposit = {
                    navController.navigate("wallet") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToWallet = {
                    navController.navigate("wallet") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToGift = {
                    navController.navigate("gift") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onProfileClick = { navController.navigate("profile") },
                onSettingsClick = { /* settings */ }
            )
        }

        // Route 3: Real-time Wallet Dashboard Layout
        composable("wallet") {
            var walletBalance by remember { mutableStateOf(0) }
            var escrowBalance by remember { mutableStateOf(0) }
            var userReferralCode by remember { mutableStateOf("Loading...") }
            var transactionsList by remember { mutableStateOf<List<TransactionLog>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager.getOrCreateUserToken()
                    val balanceResult = sessionManager.fetchUserBalanceFromServer(token)

                    walletBalance = balanceResult.coins
                    escrowBalance = balanceResult.lockedCoins
                    transactionsList = sessionManager.fetchTransactionHistory(token)
                    userReferralCode = sessionManager.fetchUserReferralCode(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                    userReferralCode = "ERROR"
                }
            }

            DashboardScreen(
                balance = walletBalance,
                lockedCoins = escrowBalance,
                referralCode = userReferralCode,
                historyLogs = transactionsList,
                selectedNavIndex = 1,
                onNavItemClick = { index ->
                    when(index){
                        0 -> navController.navigate("home") {
                            popUpTo("wallet") { inclusive = true }
                        }
                        2 -> navController.navigate("profile") {
                            popUpTo("wallet") { inclusive = true }
                        }
                        3 -> navController.navigate("gift") {
                            popUpTo("wallet") { inclusive = true }
                        }
                    }
                },
                onDeposit = { navController.navigate("deposit") },
                onWithdraw = { navController.navigate("withdraw") }
            )
        }

        // Route 4: Profile Screen
        composable("profile") {
            ProfileScreen(
                selectedNavIndex = 2,
                onNavItemClick = { index ->
                    when(index){
                        0 -> navController.navigate("home") {
                            popUpTo("profile") { inclusive = true }
                        }
                        1 -> navController.navigate("wallet") {
                            popUpTo("profile") { inclusive = true }
                        }
                        3 -> navController.navigate("gift") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                },
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        // Route 5: Gift Screen
        composable("gift") {
            GiftScreen(
                selectedNavIndex = 3,
                onNavItemClick = { index ->
                    when(index){
                        0 -> navController.navigate("home") {
                            popUpTo("gift") { inclusive = true }
                        }
                        1 -> navController.navigate("wallet") {
                            popUpTo("gift") { inclusive = true }
                        }
                        2 -> navController.navigate("profile") {
                            popUpTo("gift") { inclusive = true }
                        }
                    }
                },
            )
        }

        // Route 6: Deposit Form Action Trigger Panel
        composable("deposit") {
            DepositScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() },
                onDepositSubmitted = { amount, method, senderName ->
                    onPerformSubmit(amount, method, senderName)
                    navController.popBackStack()
                }
            )
        }

        // Route 7: Withdrawal Request Form Processing Panel
        composable("withdraw") {
            var balanceAmount by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager.getOrCreateUserToken()
                    val balanceResult = sessionManager.fetchUserBalanceFromServer(token)
                    balanceAmount = balanceResult.coins
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            WithdrawScreen(
                currentBalance = balanceAmount,
                onBack = { navController.popBackStack() },
                onWithdrawSubmitted = { amount, method, title, number ->
                    composeScope.launch {
                        val success = sessionManager.submitWithdrawalNotification(amount, method, title, number)
                        android.util.Log.d("APP_NAV", "Was withdrawal submit successful? $success")

                        launch(kotlinx.coroutines.Dispatchers.Main) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}