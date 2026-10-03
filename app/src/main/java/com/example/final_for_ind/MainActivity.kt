package com.example.final_for_ind

import android.os.Bundle
import android.util.Log

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
import com.example.final_for_ind.screens.component.GameHistoryScreen
import com.example.final_for_ind.screens.component.GiftScreen
import com.example.final_for_ind.screens.component.LeaderboardScreen
import com.example.final_for_ind.screens.component.ReferralDashboardScreen
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.login_frame.First_Screen
import com.example.final_for_ind.screens.login_frame.auth.AuthScreenRouter
import com.example.final_for_ind.screens.profile.ProfileScreen
import com.example.final_for_ind.screens.referral.PrivateRoomHostScreen
import com.example.final_for_ind.screens.start.PinEntryDialog
import com.example.final_for_ind.screens.start.PinResetScreen
import com.example.final_for_ind.screens.start.PinSetupScreen
import com.example.final_for_ind.screens.start.SettingScreen
import com.example.final_for_ind.utils.SoundManager

import kotlinx.coroutines.launch


private data class PendingWithdraw(
    val amount: Int,
    val method: String,
    val title: String,
    val number: String
)


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        SoundManager.init(applicationContext)

        val sessionManager =
            GameSessionManager(applicationContext)

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

                                val success = sessionManager
                                    .submitDepositNotification(
                                        amount, method, senderName
                                    )

                                Log.d(
                                    "APP_NAV",
                                    "Deposit save: $success"
                                )
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

    val startRoute = remember {
        if (sessionManager.isEmailLoggedIn()) "home"
        else "first_screen_route"
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        composable("first_screen_route") {
            First_Screen(
                onPlayClick = {
                    if (sessionManager.isEmailLoggedIn()) {
                        navController.navigate("home") {
                            popUpTo("first_screen_route") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate("auth_flow") {
                            popUpTo("first_screen_route") {
                                inclusive = true
                            }
                        }
                    }
                },
                sessionManager = sessionManager
            )
        }

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
                onNavigateToFriends = {
                    navController.navigate("friends_room") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable("settings") {
            SettingScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // ✅ FIXED: Invite Friends now opens Referral Dashboard
                onNavigateToInvite = {
                    navController.navigate("referral_dashboard") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    composeScope.launch {
                        sessionManager.logoutUser()
                        sessionManager.clearEmailAuth()
                        navController.navigate(
                            "first_screen_route"
                        ) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("pin_setup") {
            PinSetupScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable("pin_reset") {
            PinResetScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable("game_history") {
            var historyJson by remember {
                mutableStateOf<org.json.JSONObject?>(null)
            }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager
                        .getOrCreateUserToken()
                    historyJson = sessionManager
                        .fetchGameHistory(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            GameHistoryScreen(
                historyJson = historyJson,
                isLoading = isLoading,
                onBack = { navController.popBackStack() }
            )
        }

        composable("leaderboard") {

            var leaderboardJson by remember {
                mutableStateOf<org.json.JSONObject?>(null)
            }
            var isLoading by remember { mutableStateOf(true) }
            var currentPeriod by remember {
                mutableStateOf("alltime")
            }

            LaunchedEffect(currentPeriod) {
                isLoading = true
                try {
                    leaderboardJson = sessionManager
                        .fetchLeaderboard(
                            period = currentPeriod,
                            limit = 50
                        )
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            LeaderboardScreen(
                leaderboardJson = leaderboardJson,
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onRefresh = { newPeriod ->
                    currentPeriod = newPeriod
                }
            )
        }

        composable("referral_dashboard") {

            var dashboardJson by remember {
                mutableStateOf<org.json.JSONObject?>(null)
            }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager
                        .getOrCreateUserToken()

                    var json = sessionManager
                        .fetchReferralDashboard(token)

                    val codeFromDashboard =
                        json?.optString(
                            "referral_code", ""
                        ) ?: ""

                    if (
                        json == null
                        || codeFromDashboard.isBlank()
                    ) {
                        val backupCode = sessionManager
                            .fetchUserReferralCode(token)

                        if (json == null) {
                            json = org.json.JSONObject().apply {
                                put("status", "success")
                                put("referral_code", backupCode)
                                put("total_referred", 0)
                                put(
                                    "total_commission_earned", 0
                                )
                                put(
                                    "referred_users",
                                    org.json.JSONArray()
                                )
                            }
                        } else {
                            json.put(
                                "referral_code",
                                backupCode
                            )
                        }
                    }

                    dashboardJson = json

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            ReferralDashboardScreen(
                dashboardJson = dashboardJson,
                isLoading = isLoading,
                onBack = { navController.popBackStack() }
            )
        }

        composable("friends_room") {
            PrivateRoomHostScreen(
                onBack = { navController.popBackStack() },
                onGameStart = { _, _ -> },
                onCopyLink = { }
            )
        }

        composable("wallet") {
            var walletBalance by remember {
                mutableStateOf(0)
            }
            var escrowBalance by remember {
                mutableStateOf(0)
            }
            var userReferralCode by remember {
                mutableStateOf("Loading...")
            }
            var transactionsList by remember {
                mutableStateOf<List<TransactionLog>>(
                    emptyList()
                )
            }

            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager
                        .getOrCreateUserToken()
                    val balanceResult = sessionManager
                        .fetchUserBalanceFromServer(token)
                    walletBalance = balanceResult.coins
                    escrowBalance = balanceResult.lockedCoins
                    transactionsList = sessionManager
                        .fetchTransactionHistory(token)
                    userReferralCode = sessionManager
                        .fetchUserReferralCode(token)
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
                    when (index) {
                        0 -> navController.navigate("home") {
                            popUpTo("wallet") {
                                inclusive = true
                            }
                        }
                        2 -> navController.navigate(
                            "profile"
                        ) {
                            popUpTo("wallet") {
                                inclusive = true
                            }
                        }
                        3 -> navController.navigate("gift") {
                            popUpTo("wallet") {
                                inclusive = true
                            }
                        }
                    }
                },
                onDeposit = {
                    navController.navigate("deposit")
                },
                onWithdraw = {
                    navController.navigate("withdraw")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                selectedNavIndex = 2,
                onNavItemClick = { index ->
                    when (index) {
                        0 -> navController.navigate("home") {
                            popUpTo("profile") {
                                inclusive = true
                            }
                        }
                        1 -> navController.navigate("wallet") {
                            popUpTo("profile") {
                                inclusive = true
                            }
                        }
                        3 -> navController.navigate("gift") {
                            popUpTo("profile") {
                                inclusive = true
                            }
                        }
                    }
                },
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() },

                onLeaderboard = {
                    navController.navigate("leaderboard") {
                        popUpTo("profile") { saveState = true }
                        launchSingleTop = true
                    }
                },

                onGameHistory = {
                    navController.navigate("game_history") {
                        popUpTo("profile") { saveState = true }
                        launchSingleTop = true
                    }
                },

                onReferralDashboard = {
                    navController.navigate(
                        "referral_dashboard"
                    ) {
                        popUpTo("profile") { saveState = true }
                        launchSingleTop = true
                    }
                },

                onLogout = {
                    composeScope.launch {
                        sessionManager.logoutUser()
                        sessionManager.clearEmailAuth()
                        navController.navigate(
                            "first_screen_route"
                        ) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("gift") {
            GiftScreen(
                selectedNavIndex = 3,
                onNavItemClick = { index ->
                    when (index) {
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
                }
            )
        }

        composable("deposit") {
            DepositScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() },
                onDepositSubmitted = {
                        amount, method, senderName ->
                    onPerformSubmit(amount, method, senderName)
                    navController.popBackStack()
                }
            )
        }

        composable("withdraw") {

            var balanceAmount by remember {
                mutableStateOf(0)
            }
            var pendingWithdraw by remember {
                mutableStateOf<PendingWithdraw?>(null)
            }
            var showPinDialog by remember {
                mutableStateOf(false)
            }
            var pinError by remember {
                mutableStateOf("")
            }
            var isPinLoading by remember {
                mutableStateOf(false)
            }
            var hasPin by remember {
                mutableStateOf<Boolean?>(null)
            }

            LaunchedEffect(Unit) {
                try {
                    val token = sessionManager
                        .getOrCreateUserToken()
                    val balanceResult = sessionManager
                        .fetchUserBalanceFromServer(token)
                    balanceAmount = balanceResult.coins
                    val pinStatus = sessionManager
                        .getPinStatus()
                    hasPin = pinStatus?.optBoolean(
                        "has_withdrawal_pin", false
                    ) ?: false
                } catch (e: Exception) {
                    e.printStackTrace()
                    hasPin = false
                }
            }

            WithdrawScreen(
                currentBalance = balanceAmount,
                onBack = { navController.popBackStack() },
                onWithdrawSubmitted = {
                        amount, method, title, number ->

                    pendingWithdraw = PendingWithdraw(
                        amount = amount,
                        method = method,
                        title = title,
                        number = number
                    )

                    pinError = ""

                    if (hasPin == null) {
                        composeScope.launch {
                            val pinStatus = sessionManager
                                .getPinStatus()
                            val pinExists = pinStatus
                                ?.optBoolean(
                                    "has_withdrawal_pin",
                                    false
                                ) ?: false
                            hasPin = pinExists
                            if (!pinExists) {
                                navController.navigate(
                                    "pin_setup"
                                )
                            } else {
                                showPinDialog = true
                            }
                        }
                        return@WithdrawScreen
                    }

                    if (hasPin == false) {
                        navController.navigate("pin_setup")
                        return@WithdrawScreen
                    }

                    showPinDialog = true
                }
            )

            if (showPinDialog && pendingWithdraw != null) {
                PinEntryDialog(
                    isLoading = isPinLoading,
                    errorMessage = pinError,
                    onDismiss = {
                        if (!isPinLoading) {
                            showPinDialog = false
                            pinError = ""
                        }
                    },
                    onForgotPin = {
                        showPinDialog = false
                        pinError = ""
                        navController.navigate("pin_reset")
                    },
                    onSubmit = { enteredPin ->

                        val data = pendingWithdraw
                            ?: return@PinEntryDialog
                        pinError = ""
                        isPinLoading = true

                        composeScope.launch {
                            val result = sessionManager
                                .submitWithdrawalWithPin(
                                    amount = data.amount,
                                    method = data.method,
                                    title = data.title,
                                    number = data.number,
                                    pin = enteredPin
                                )

                            isPinLoading = false

                            if (result == null) {
                                pinError = (
                                        "Network error. Try again."
                                        )
                                return@launch
                            }

                            val status = result.optString(
                                "status", ""
                            )
                            val errorMsg = result.optString(
                                "error", ""
                            )
                            val errorCode = result.optString(
                                "code", ""
                            )

                            if (status == "success") {
                                showPinDialog = false
                                pendingWithdraw = null
                                android.widget.Toast
                                    .makeText(
                                        navController.context,
                                        "Withdrawal submitted!",
                                        android.widget.Toast
                                            .LENGTH_SHORT
                                    ).show()
                                navController.popBackStack()
                            } else if (
                                errorCode == "PIN_INCORRECT"
                            ) {
                                pinError = (
                                        "Incorrect PIN. Try again."
                                        )
                            } else if (
                                errorCode == "PIN_NOT_SET"
                            ) {
                                showPinDialog = false
                                navController.navigate(
                                    "pin_setup"
                                )
                            } else {
                                pinError = if (
                                    errorMsg.isNotEmpty()
                                ) errorMsg
                                else "Failed to submit withdrawal."
                            }
                        }
                    }
                )
            }
        }
    }
}