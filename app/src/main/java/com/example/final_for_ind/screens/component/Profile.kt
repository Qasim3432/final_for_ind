package com.example.final_for_ind.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.TransactionLog
import com.example.final_for_ind.screens.component.BottomNavBar
import com.example.final_for_ind.utils.uriToFile
import com.example.final_for_ind.utils.formatDollar
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.abs

// ==========================================================
// ADAPTIVE FONT SIZE FOR DOLLAR BALANCE
// ==========================================================

private fun adaptiveCoinsFontSize(value: Int): TextUnit {
    val length = formatDollar(value).length
    return when {
        length <= 6 -> 36.sp
        length == 7 -> 32.sp
        length == 8 -> 28.sp
        length == 9 -> 24.sp
        length == 10 -> 22.sp
        length == 11 -> 20.sp
        length == 12 -> 18.sp
        else -> 16.sp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    selectedNavIndex: Int = 2,
    onNavItemClick: (Int) -> Unit = {},
    sessionManager: GameSessionManager,
    onBack: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    onGameHistory: () -> Unit = {},
    onReferralDashboard: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedPicUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("Guest") }
    var userEmail by remember { mutableStateOf("") }
    var coins by remember { mutableStateOf(0) }

    var userCountry by remember { mutableStateOf("PK") }

    var showEditDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionsList by remember { mutableStateOf<List<TransactionLog>>(emptyList()) }
    var isLoadingTxns by remember { mutableStateOf(false) }

    suspend fun refreshCoins() {
        try {
            val token = sessionManager.getOrCreateUserToken()
            val balance = sessionManager.fetchUserBalanceFromServer(token)
            coins = balance.coins
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            isUploading = true
            scope.launch {
                try {
                    val token = sessionManager.getOrCreateUserToken()
                    val picFile = uriToFile(context, uri)

                    val result = sessionManager.syncUserProfile(
                        deviceToken = token,
                        nickname = userName,
                        email = userEmail,
                        profilePicFile = picFile
                    )

                    if (result.success) {
                        val prefs = context.getSharedPreferences(
                            "ludo_session_prefs",
                            Context.MODE_PRIVATE
                        )
                        savedPicUrl = prefs.getString("profile_pic_url", null)
                        profileImageUri = null
                        snackbarHostState.showSnackbar("Profile picture updated")
                    } else {
                        profileImageUri = null
                        snackbarHostState.showSnackbar(
                            result.errorMessage ?: "Upload failed"
                        )
                    }
                } catch (e: Exception) {
                    profileImageUri = null
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                } finally {
                    isUploading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val token = sessionManager.getOrCreateUserToken()
                val balance = sessionManager.fetchUserBalanceFromServer(token)
                coins = balance.coins
                val prefs = context.getSharedPreferences(
                    "ludo_session_prefs",
                    Context.MODE_PRIVATE
                )
                savedPicUrl = prefs.getString("profile_pic_url", null)
                userName = prefs.getString("email_user_name", "Guest") ?: "Guest"
                userEmail = prefs.getString("email_user_email", "") ?: ""

                userCountry = sessionManager.getSavedCountry()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadTransactions() {
        isLoadingTxns = true
        scope.launch {
            try {
                val token = sessionManager.getOrCreateUserToken()
                transactionsList = sessionManager.fetchTransactionHistory(token)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingTxns = false
            }
        }
    }

    val bgGradient = Brush.radialGradient(
        listOf(Color(0xFF2B0000), Color(0xFF0A0A0A)),
        radius = 1200f
    )

    Box(Modifier.fillMaxSize().background(bgGradient)) {

        CanvasBg()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedNavIndex,
                    onItemClick = onNavItemClick
                )
            },
            topBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1A1A1A), Color(0xFF000))
                                )
                            )
                            .border(
                                2.dp,
                                Color(0xFFD4AF37),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                userName.uppercase(),
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(130.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF8B0000), Color(0xFFD32F2F))
                                )
                            )
                            .border(3.dp, Color(0xFFD4AF37), CircleShape)
                            .clickable(enabled = !isUploading) {
                                imagePicker.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (profileImageUri != null || isUploading) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (isUploading) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFD4AF37),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else if (savedPicUrl != null) {
                            AsyncImage(
                                model = savedPicUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        userName,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (userEmail.isNotBlank()) {
                    Text(
                        userEmail,
                        color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            Color(0xFFD4AF37).copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (userCountry == "IN") "🇮🇳" else "🇵🇰",
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (userCountry == "IN") "India" else "Pakistan",
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // BALANCE CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0A0A0A).copy(alpha = 0.8f))
                        .border(
                            2.5.dp,
                            Color(0xFFD4AF37),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 42.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Total Balance",
                                color = Color(0xFFD4AF37),
                                fontSize = 14.sp
                            )
                            Text(
                                formatDollar(coins),
                                color = Color(0xFFFFD700),
                                fontSize = adaptiveCoinsFontSize(coins),
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Leaderboard (Purple Theme)
                RoyalMenuItem(
                    icon = "🏅",
                    title = "Leaderboard",
                    subtitle = "Top players ranking",
                    onClick = onLeaderboard,
                    bgColor = Color(0xFF1A0B2E),
                    borderColor = Color(0xFF9D4EDD)
                )

                Spacer(Modifier.height(12.dp))

                // Game History (Neon Green Theme)
                RoyalMenuItem(
                    icon = "🏆",
                    title = "Game History",
                    subtitle = "Check your past matches",
                    onClick = onGameHistory,
                    bgColor = Color(0xFF0F1410),
                    borderColor = Color(0xFF00E676)
                )

                Spacer(Modifier.height(12.dp))

                // Referral Dashboard (Neon Blue Theme)
                RoyalMenuItem(
                    icon = "👥",
                    title = "Referral Dashboard",
                    subtitle = "Invite friends & earn",
                    onClick = onReferralDashboard,
                    bgColor = Color(0xFF081020),
                    borderColor = Color(0xFF00E5FF)
                )

                Spacer(Modifier.height(12.dp))

                // Transaction History (Default Gold/Black)
                RoyalMenuItem(
                    icon = "🔄",
                    title = "Transaction History",
                    subtitle = "View deposits & withdrawals",
                    onClick = {
                        showTransactionDialog = true
                        loadTransactions()
                    }
                )

                Spacer(Modifier.height(12.dp))

                // ✅ Logout (Red Border/Line)
                RoyalMenuItem(
                    icon = "⏻",
                    title = "Logout",
                    subtitle = "Sign out from your account",
                    onClick = { onLogout() },
                    borderColor = Color(0xFFE53935) // Red outline
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = userName,
            currentEmail = userEmail,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newEmail ->
                scope.launch {
                    try {
                        val token = sessionManager.getOrCreateUserToken()

                        val result = sessionManager.syncUserProfile(
                            deviceToken = token,
                            nickname = newName,
                            email = newEmail,
                            profilePicFile = null
                        )

                        if (result.success) {
                            userName = newName
                            userEmail = newEmail
                            showEditDialog = false
                            refreshCoins()
                            snackbarHostState.showSnackbar("Profile updated")
                        } else {
                            snackbarHostState.showSnackbar(
                                result.errorMessage ?: "Update failed"
                            )
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    }
                }
            }
        )
    }

    if (showTransactionDialog) {
        TransactionHistoryPopup(
            transactions = transactionsList,
            isLoading = isLoadingTxns,
            onRefresh = { loadTransactions() },
            onDismiss = { showTransactionDialog = false }
        )
    }
}


// ==========================================================
// TRANSACTION HISTORY POPUP
// ==========================================================

@Composable
fun TransactionHistoryPopup(
    transactions: List<TransactionLog>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val gold = Color(0xFFD4AF37)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0000),
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Recent Transactions",
                    color = gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { if (!isLoading) onRefresh() },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isLoading) gold.copy(alpha = 0.4f) else gold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 420.dp)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = gold)
                        }
                    }
                    transactions.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recorded transactions found.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            transactions.forEach { log ->
                                ProfileTransactionRow(log = log, gold = gold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}


@Composable
fun ProfileTransactionRow(
    log: TransactionLog,
    gold: Color
) {

    val (title, isCredit, icon) = when (log.type) {

        "DEPOSIT"                   -> Triple("Deposit",            true,  "📥")
        "WITHDRAWAL"                -> Triple("Withdrawal",         false, "📤")
        "DAILY_BONUS"               -> Triple("Daily Bonus",        true,  "🎁")
        "SPIN_COST"                 -> Triple("Spin Cost",          false, "🎰")
        "SPIN_REWARD"               -> Triple("Spin Reward",        true,  "🎉")
        "LEADERBOARD_REWARD"        -> Triple("Leaderboard Reward", true,  "🏆")
        "REFERRAL_BONUS"            -> Triple("Referral Bonus",     true,  "👥")
        "REFERRAL_INVITE_REWARD"    -> Triple("Invite Reward",      true,  "👥")
        "REFERRAL_COMMISSION"       -> Triple("Referral Commission",true,  "💸")
        "WAGER_ESCROW"              -> Triple("Match Entry",        false, "⚔️")
        "WAGER_PAYOUT"              -> Triple("Match Win",          true,  "🏅")
        "WAGER_REFUND"              -> Triple("Match Refund",       true,  "↩️")

        else -> Triple(
            log.type.replace("_", " "),
            log.amount > 0,
            "💰"
        )
    }

    val amountColor = if (isCredit) Color(0xFF2ECC71) else Color(0xFFE74C3C)

    val absAmount = abs(log.amount)
    val amountText = if (log.amount >= 0) "+${formatDollar(absAmount)}" else "-${formatDollar(absAmount)}"

    val statusColor = when (log.status.uppercase()) {
        "APPROVED", "SUCCESS" -> Color(0xFF2ECC71)
        "REJECTED", "FAILED"  -> Color(0xFFE74C3C)
        else                  -> Color(0xFFF1C40F)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, gold.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = log.dateString,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountText,
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = log.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}


// ==========================================================
// EDIT PROFILE DIALOG
// ==========================================================

@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val trimmedName = name.trim()
    val isNameValid = trimmedName.length in 2..20
    val isEmailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val canSave = isNameValid && isEmailValid && !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Edit Profile",
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    label = { Text("Name", color = Color(0xFFD4AF37)) },
                    singleLine = true,
                    enabled = !isSaving,
                    isError = name.isNotBlank() && !isNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        unfocusedBorderColor = Color(0xFFD4AF37).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedLabelColor = Color(0xFFD4AF37).copy(alpha = 0.7f),
                        cursorColor = Color(0xFFD4AF37),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorBorderColor = Color(0xFFD32F2F)
                    )
                )

                if (name.isNotBlank() && !isNameValid) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Name must be 2-20 characters",
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Email", color = Color(0xFFD4AF37)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !isSaving,
                    isError = email.isNotBlank() && !isEmailValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        unfocusedBorderColor = Color(0xFFD4AF37).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedLabelColor = Color(0xFFD4AF37).copy(alpha = 0.7f),
                        cursorColor = Color(0xFFD4AF37),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorBorderColor = Color(0xFFD32F2F)
                    )
                )

                if (email.isNotBlank() && !isEmailValid) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Please enter a valid email address",
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        isSaving = true
                        onSave(trimmedName, email.trim())
                    }
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    disabledContainerColor = Color(0xFF8B6914).copy(alpha = 0.5f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isSaving) "Saving..." else "Save",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = Color(0xFFD4AF37))
            }
        }
    )
}


// ==========================================================
// MENU ITEM (Supports Solid Colors & Gradients)
// ==========================================================

@Composable
fun RoyalMenuItem(
    icon: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    bgColor: Color = Color(0xFF0A0A0A).copy(alpha = 0.7f),
    bgBrush: Brush? = null, // Added support for gradient
    borderColor: Color = Color(0xFFD4AF37),
    iconColor: Color = Color.White,
    textColor: Color = Color.White,
    subtitleColor: Color = Color(0xFFD4AF37).copy(alpha = 0.7f)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle == null) 64.dp else 80.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                // Apply gradient if provided, otherwise use solid color
                if (bgBrush != null) {
                    Modifier.background(bgBrush)
                } else {
                    Modifier.background(bgColor)
                }
            )
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 28.sp, color = iconColor)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = subtitleColor,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Default.ArrowForwardIos,
                null,
                tint = borderColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// ==========================================================
// BACKGROUND SPARKLES
// ==========================================================

@Composable
fun CanvasBg() {

    val sparklePositions = remember {
        List(60) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        sparklePositions.forEach { (fx, fy, sizeFactor) ->
            drawCircle(
                Color(0xFFFFD700).copy(alpha = sizeFactor * 0.5f),
                radius = sizeFactor * 2.5f,
                center = Offset(fx * size.width, fy * size.height)
            )
        }
    }
}