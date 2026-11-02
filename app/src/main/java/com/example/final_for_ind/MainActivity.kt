package com.example.final_for_ind

import android.content.Context
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.final_for_ind.screens.component.PendingReward
import com.example.final_for_ind.screens.component.ReferralDashboardScreen
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.login_frame.auth.AuthScreenRouter
import com.example.final_for_ind.screens.profile.ProfileScreen
import com.example.final_for_ind.screens.referral.PrivateRoomHostScreen
import com.example.final_for_ind.screens.start.PinEntryDialog
import com.example.final_for_ind.screens.start.PinResetScreen
import com.example.final_for_ind.screens.start.PinSetupScreen
import com.example.final_for_ind.screens.start.SettingScreen
import com.example.final_for_ind.screens.start.SplashScreen // ✅ SplashScreen Import kiya
import com.example.final_for_ind.utils.SoundManager
import com.example.final_for_ind.utils.formatDollar

import kotlinx.coroutines.delay
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

        val sessionManager = GameSessionManager(applicationContext)

        setContent {
            MaterialTheme {
                // ✅ Splash Screen State
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    // ✅ Show Splash Screen
                    SplashScreen(
                        onTimeout = { showSplash = false }
                    )
                } else {
                    // ✅ Show Main App
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(sessionManager = sessionManager)
                    }
                }
            }
        }
    }
}


@Composable
fun AppNavigation(sessionManager: GameSessionManager) {

    val navController = rememberNavController()
    val composeScope = rememberCoroutineScope()

    // ==========================================================
    // LOGIN GATE STATE
    // ==========================================================
    var showLoginDialog by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }
    var showLoginScreen by remember { mutableStateOf(false) }

    fun triggerLoginDialog(message: String) {
        loginMessage = message
        showLoginDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {

            // ==================================================
            // HOME
            // ==================================================
            composable("home") {
                HomeScreen(
                    sessionManager = sessionManager,
                    onRequireLogin = { msg -> triggerLoginDialog(msg) },
                    onNavigateToDeposit = { navController.navigate("wallet") },
                    onNavigateToWallet = { navController.navigate("wallet") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToGift = { navController.navigate("gift") },
                    onNavigateToFriends = { navController.navigate("friends_room") },
                    onProfileClick = { navController.navigate("profile") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            // ==================================================
            // GIFT
            // ==================================================
            composable("gift") {
                GiftScreen(
                    selectedNavIndex = 3,
                    onNavItemClick = { index ->
                        when (index) {
                            0 -> navController.navigate("home") { popUpTo("gift") { inclusive = true } }
                            1 -> navController.navigate("wallet") { popUpTo("gift") { inclusive = true } }
                            2 -> navController.navigate("profile") { popUpTo("gift") { inclusive = true } }
                        }
                    },
                    onRequireLogin = { msg -> triggerLoginDialog(msg) }
                )
            }

            // ==================================================
            // WALLET
            // ==================================================
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
                        when (index) {
                            0 -> navController.navigate("home") { popUpTo("wallet") { inclusive = true } }
                            2 -> navController.navigate("profile") { popUpTo("wallet") { inclusive = true } }
                            3 -> navController.navigate("gift") { popUpTo("wallet") { inclusive = true } }
                        }
                    },
                    onDeposit = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("deposit")
                        } else {
                            triggerLoginDialog("Deposit karne ke liye login karein")
                        }
                    },
                    onWithdraw = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("withdraw")
                        } else {
                            triggerLoginDialog("Withdraw karne ke liye login karein")
                        }
                    },
                    onBalanceChanged = {
                        composeScope.launch {
                            try {
                                val token = sessionManager.getOrCreateUserToken()
                                val balanceResult = sessionManager.fetchUserBalanceFromServer(token)
                                walletBalance = balanceResult.coins
                                escrowBalance = balanceResult.lockedCoins
                                transactionsList = sessionManager.fetchTransactionHistory(token)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }

            // ==================================================
            // PROFILE
            // ==================================================
            composable("profile") {
                ProfileScreen(
                    selectedNavIndex = 2,
                    onNavItemClick = { index ->
                        when (index) {
                            0 -> navController.navigate("home") { popUpTo("profile") { inclusive = true } }
                            1 -> navController.navigate("wallet") { popUpTo("profile") { inclusive = true } }
                            3 -> navController.navigate("gift") { popUpTo("profile") { inclusive = true } }
                        }
                    },
                    sessionManager = sessionManager,
                    onBack = { navController.popBackStack() },
                    onLeaderboard = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("leaderboard")
                        } else {
                            triggerLoginDialog("Leaderboard dekhne ke liye login karein")
                        }
                    },
                    onGameHistory = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("game_history")
                        } else {
                            triggerLoginDialog("Game history dekhne ke liye login karein")
                        }
                    },
                    onReferralDashboard = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("referral_dashboard")
                        } else {
                            triggerLoginDialog("Referral dekhne ke liye login karein")
                        }
                    },
                    onLogout = {
                        composeScope.launch {
                            sessionManager.logoutUser()
                            sessionManager.clearEmailAuth()
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // ==================================================
            // SETTINGS
            // ==================================================
            composable("settings") {
                SettingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToInvite = {
                        if (sessionManager.isEmailLoggedIn()) {
                            navController.navigate("referral_dashboard")
                        } else {
                            triggerLoginDialog("Referral dekhne ke liye login karein")
                        }
                    },
                    onLogout = {
                        composeScope.launch {
                            sessionManager.logoutUser()
                            sessionManager.clearEmailAuth()
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // ==================================================
            // FRIENDS ROOM
            // ==================================================
            composable("friends_room") {
                PrivateRoomHostScreen(
                    sessionManager = sessionManager,
                    onBack = { navController.popBackStack() },
                    onGameStart = { _, _ -> },
                    onCopyLink = { },
                    onRequireLogin = { msg -> triggerLoginDialog(msg) }
                )
            }

            // ==================================================
            // LEADERBOARD
            // ==================================================
            composable("leaderboard") {

                var leaderboardJson by remember { mutableStateOf<org.json.JSONObject?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var currentPeriod by remember { mutableStateOf("alltime") }

                var pendingRewards by remember {
                    mutableStateOf<List<PendingReward>>(emptyList())
                }
                var isClaimingReward by remember { mutableStateOf(false) }

                suspend fun loadLeaderboard() {
                    isLoading = true
                    try {
                        leaderboardJson = sessionManager.fetchLeaderboard(
                            period = currentPeriod,
                            limit = 100
                        )

                        val rewardsJson = sessionManager.fetchPendingRewards()
                        if (rewardsJson != null && rewardsJson.optString("status") == "success") {
                            val arr = rewardsJson.optJSONArray("claims")
                            val list = mutableListOf<PendingReward>()
                            if (arr != null) {
                                for (i in 0 until arr.length()) {
                                    val o = arr.optJSONObject(i) ?: continue
                                    list.add(
                                        PendingReward(
                                            id = o.optInt("id", 0),
                                            amount = o.optInt("amount", 0),
                                            rank = o.optInt("rank", 0),
                                            periodType = o.optString("period_type", ""),
                                            periodKey = o.optString("period_key", ""),
                                        )
                                    )
                                }
                            }
                            pendingRewards = list
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }

                LaunchedEffect(currentPeriod) {
                    loadLeaderboard()
                }

                LeaderboardScreen(
                    leaderboardJson = leaderboardJson,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() },
                    onRefresh = { newPeriod -> currentPeriod = newPeriod },
                    myDeviceToken = sessionManager.getOrCreateUserToken(),
                    pendingRewards = pendingRewards,
                    isClaimingReward = isClaimingReward,
                    onCollectReward = { claimId ->
                        if (!isClaimingReward) {
                            isClaimingReward = true
                            composeScope.launch {
                                val result = sessionManager.claimLeaderboardReward(claimId)
                                isClaimingReward = false

                                if (result != null && result.optString("status") == "success") {
                                    val amt = result.optInt("amount", 0)
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        "🎉 ${formatDollar(amt)} claimed!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    loadLeaderboard()
                                } else {
                                    val msg = result?.optString("message") ?: "Claim failed"
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        msg,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }

            // ==================================================
            // GAME HISTORY
            // ==================================================
            composable("game_history") {
                var historyJson by remember { mutableStateOf<org.json.JSONObject?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    try {
                        val token = sessionManager.getOrCreateUserToken()
                        historyJson = sessionManager.fetchGameHistory(token)
                    } catch (e: Exception) { e.printStackTrace() }
                    finally { isLoading = false }
                }

                GameHistoryScreen(
                    historyJson = historyJson,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() }
                )
            }

            // ==================================================
            // REFERRAL DASHBOARD
            // ==================================================
            composable("referral_dashboard") {
                var dashboardJson by remember { mutableStateOf<org.json.JSONObject?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    try {
                        val token = sessionManager.getOrCreateUserToken()
                        var json = sessionManager.fetchReferralDashboard(token)
                        val codeFromDashboard = json?.optString("referral_code", "") ?: ""
                        if (json == null || codeFromDashboard.isBlank()) {
                            val backupCode = sessionManager.fetchUserReferralCode(token)
                            if (json == null) {
                                json = org.json.JSONObject().apply {
                                    put("status", "success")
                                    put("referral_code", backupCode)
                                    put("total_referred", 0)
                                    put("total_commission_earned", 0)
                                    put("referred_users", org.json.JSONArray())
                                }
                            } else {
                                json.put("referral_code", backupCode)
                            }
                        }
                        dashboardJson = json
                    } catch (e: Exception) { e.printStackTrace() }
                    finally { isLoading = false }
                }

                ReferralDashboardScreen(
                    dashboardJson = dashboardJson,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() }
                )
            }

            // ==================================================
            // PIN SCREENS
            // ==================================================
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

            // ==================================================
            // DEPOSIT
            // ==================================================
            composable("deposit") {
                DepositScreen(
                    sessionManager = sessionManager,
                    onBack = { navController.popBackStack() },
                    onDepositSubmitted = { localAmount, currency, method, senderName ->
                        composeScope.launch {
                            sessionManager.submitDepositNotification(
                                localAmount = localAmount,
                                currency = currency,
                                method = method,
                                senderName = senderName
                            )
                        }
                        navController.popBackStack()
                    }
                )
            }

            // ==================================================
            // WITHDRAW
            // ==================================================
            composable("withdraw") {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

                var balanceAmount by remember { mutableStateOf(0) }
                var pendingWithdraw by remember { mutableStateOf<PendingWithdraw?>(null) }
                var showPinDialog by remember { mutableStateOf(false) }
                var pinError by remember { mutableStateOf("") }
                var isPinLoading by remember { mutableStateOf(false) }
                var hasPin by remember { mutableStateOf<Boolean?>(null) }

                var cooldownEndTime by remember { mutableStateOf(prefs.getLong("withdraw_cooldown_end", 0L)) }
                var cooldownSeconds by remember {
                    mutableStateOf(
                        if (cooldownEndTime > System.currentTimeMillis())
                            ((cooldownEndTime - System.currentTimeMillis()) / 1000).toInt()
                        else 0
                    )
                }

                LaunchedEffect(Unit) {
                    try {
                        val token = sessionManager.getOrCreateUserToken()
                        val balanceResult = sessionManager.fetchUserBalanceFromServer(token)
                        balanceAmount = balanceResult.coins
                        val pinStatus = sessionManager.getPinStatus()
                        hasPin = pinStatus?.optBoolean("has_withdrawal_pin", false) ?: false
                    } catch (e: Exception) {
                        e.printStackTrace()
                        hasPin = false
                    }
                }

                LaunchedEffect(cooldownSeconds) {
                    if (cooldownSeconds > 0) {
                        delay(1000L)
                        cooldownSeconds--
                    } else if (cooldownEndTime > 0) {
                        cooldownEndTime = 0L
                        prefs.edit().remove("withdraw_cooldown_end").apply()
                    }
                }

                WithdrawScreen(
                    currentBalance = balanceAmount,
                    sessionManager = sessionManager,
                    isSubmitting = isPinLoading,
                    cooldownSeconds = cooldownSeconds,
                    onBack = { navController.popBackStack() },
                    onWithdrawSubmitted = { amount, method, title, number ->
                        pendingWithdraw = PendingWithdraw(amount, method, title, number)
                        pinError = ""

                        if (hasPin == null) {
                            composeScope.launch {
                                val pinStatus = sessionManager.getPinStatus()
                                val pinExists = pinStatus?.optBoolean("has_withdrawal_pin", false) ?: false
                                hasPin = pinExists
                                if (!pinExists) navController.navigate("pin_setup")
                                else showPinDialog = true
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
                        cooldownSeconds = cooldownSeconds,
                        onDismiss = {
                            if (!isPinLoading && cooldownSeconds == 0) {
                                showPinDialog = false
                                pinError = ""
                            }
                        },
                        onForgotPin = {
                            if (cooldownSeconds == 0) {
                                showPinDialog = false
                                pinError = ""
                                navController.navigate("pin_reset")
                            }
                        },
                        onSubmit = { enteredPin ->
                            val data = pendingWithdraw ?: return@PinEntryDialog
                            pinError = ""
                            isPinLoading = true
                            composeScope.launch {
                                val result = sessionManager.submitWithdrawalWithPin(
                                    amount = data.amount,
                                    method = data.method,
                                    title = data.title,
                                    number = data.number,
                                    pin = enteredPin
                                )
                                isPinLoading = false
                                if (result == null) {
                                    pinError = "Network error. Please check your connection."
                                    return@launch
                                }

                                val status = result.optString("status", "")
                                val errorMsg = result.optString("message", result.optString("error", ""))
                                val errorCode = result.optString("code", "")

                                if (status == "success") {
                                    showPinDialog = false
                                    pendingWithdraw = null
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        "Withdrawal submitted!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()

                                } else if (errorCode == "RATE_LIMITED") {
                                    val retryAfter = result.optInt("retry_after", 21600)
                                    cooldownSeconds = retryAfter
                                    cooldownEndTime = System.currentTimeMillis() + (retryAfter * 1000L)
                                    prefs.edit().putLong("withdraw_cooldown_end", cooldownEndTime).apply()
                                    pinError = ""

                                } else if (errorCode == "PIN_LOCKED") {
                                    pinError = "Too many failed attempts. Your account is locked for 12 hours. Please try again later."

                                } else if (errorCode == "PIN_INCORRECT") {
                                    val attemptsLeft = result.optInt("attempts_left", 0)
                                    pinError = "Incorrect PIN. You have $attemptsLeft attempts left."

                                } else if (errorCode == "PIN_NOT_SET") {
                                    showPinDialog = false
                                    navController.navigate("pin_setup")

                                } else if (errorCode == "LOCATION_REQUIRED") {
                                    showPinDialog = false
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        "Location permission required.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()

                                } else {
                                    pinError = if (errorMsg.isNotEmpty()) errorMsg else "Unable to process request. Please try again."
                                }
                            }
                        }
                    )
                }
            }
        }

        // =====================================================
        // LOGIN DIALOG
        // =====================================================
        if (showLoginDialog) {
            AlertDialog(
                onDismissRequest = { showLoginDialog = false },
                containerColor = Color(0xFF1A0000),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 24.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Login Required",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Text(
                        loginMessage,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLoginDialog = false
                            showLoginScreen = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Login", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoginDialog = false }) {
                        Text("Later", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // =====================================================
        // LOGIN SCREEN
        // =====================================================
        if (showLoginScreen) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0A0A0A)
            ) {
                AuthScreenRouter(
                    sessionManager = sessionManager,
                    forceLogin = true,
                    onAuthComplete = { showLoginScreen = false },
                    onCancel = { showLoginScreen = false }
                )
            }
        }
    }
}