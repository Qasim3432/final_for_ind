package com.example.final_for_ind.screens.profile

import android.content.Context
import android.net.Uri
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.TransactionLog
import com.example.final_for_ind.screens.component.BottomNavBar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> input.copyTo(out) }
        input.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
    onTransactionHistory: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedPicUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("Guest") }
    var userEmail by remember { mutableStateOf("") }
    var coins by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // ✅ NEW: Transaction popup state
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionsList by remember { mutableStateOf<List<TransactionLog>>(emptyList()) }
    var isLoadingTxns by remember { mutableStateOf(false) }

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
                    val success = sessionManager.syncUserProfile(token, userName, userEmail, picFile)
                    if (success) {
                        val prefs = context.getSharedPreferences("ludo_session_prefs", Context.MODE_PRIVATE)
                        savedPicUrl = prefs.getString("profile_pic_url", null)
                        profileImageUri = null
                        snackbarHostState.showSnackbar("Profile picture updated")
                    } else {
                        profileImageUri = null
                        snackbarHostState.showSnackbar("Upload failed, try again")
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
                val prefs = context.getSharedPreferences("ludo_session_prefs", Context.MODE_PRIVATE)
                savedPicUrl = prefs.getString("profile_pic_url", null)
                userName = prefs.getString("email_user_name", "Guest") ?: "Guest"
                userEmail = prefs.getString("email_user_email", "") ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val bgGradient = Brush.radialGradient(listOf(Color(0xFF2B0000), Color(0xFF0A0A0A)), radius = 1200f)

    Box(Modifier.fillMaxSize().background(bgGradient)) {
        CanvasBg()
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { BottomNavBar(selectedIndex = selectedNavIndex, onItemClick = onNavItemClick) },
            topBar = {
                Box(Modifier.fillMaxWidth().padding(top = 24.dp).height(40.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF000)))).border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("PREMIUM PRO", color = Color(0xFFD4AF37), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
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
                Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(130.dp).blur(20.dp).background(Color(0xFFFFD700).copy(alpha = 0.3f), CircleShape))
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))).border(3.dp, Color(0xFFD4AF37), CircleShape).clickable(enabled = !isUploading) { imagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                        if (profileImageUri != null || isUploading) {
                            if (profileImageUri != null) {
                                AsyncImage(model = profileImageUri, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            if (isUploading) {
                                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFFD4AF37), modifier = Modifier.size(32.dp))
                                }
                            }
                        } else if (savedPicUrl != null) {
                            AsyncImage(model = savedPicUrl, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color(0xFFFFD700), modifier = Modifier.size(60.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
                    }
                }
                if (userEmail.isNotBlank()) {
                    Text(userEmail, color = Color(0xFFD4AF37).copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0A0A0A).copy(alpha = 0.8f)).border(2.5.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 42.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Total Coins", color = Color(0xFFD4AF37), fontSize = 14.sp)
                            Text("$coins", color = Color(0xFFFFD700), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))

                RoyalMenuItem(
                    icon = "🏅",
                    title = "Leaderboard",
                    subtitle = "Top players ranking",
                    onClick = onLeaderboard
                )
                Spacer(Modifier.height(12.dp))

                RoyalMenuItem(icon = "🏆", title = "Game History", onClick = onGameHistory)
                Spacer(Modifier.height(12.dp))

                RoyalMenuItem(
                    icon = "👥",
                    title = "Referral Dashboard",
                    subtitle = "Invite friends & earn",
                    onClick = onReferralDashboard
                )
                Spacer(Modifier.height(12.dp))

                // ✅ FIXED: Transaction History opens popup
                RoyalMenuItem(
                    icon = "🔄",
                    title = "Transaction History",
                    subtitle = "View deposits & withdrawals",
                    onClick = {
                        onTransactionHistory()
                        showTransactionDialog = true
                        if (transactionsList.isEmpty()) {
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
                    }
                )
                Spacer(Modifier.height(12.dp))

                RoyalMenuItem(icon = "⏻", title = "Logout", onClick = { onLogout() }, isLogout = true)

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
                showEditDialog = false
                scope.launch {
                    try {
                        val token = sessionManager.getOrCreateUserToken()
                        val success = sessionManager.syncUserProfile(token, newName, newEmail, null)
                        if (success) {
                            userName = newName
                            userEmail = newEmail
                            snackbarHostState.showSnackbar("Profile updated")
                        } else {
                            snackbarHostState.showSnackbar("Update failed")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    }
                }
            }
        )
    }

    // ✅ NEW: Transaction history popup
    if (showTransactionDialog) {
        TransactionHistoryPopup(
            transactions = transactionsList,
            isLoading = isLoadingTxns,
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
    onDismiss: () -> Unit
) {
    val gold = Color(0xFFD4AF37)
    val redDark = Color(0xFF2B0000)
    val redBright = Color(0xFFD32F2F)

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
                    "Recent Transactions Ledger",
                    color = gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
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
                                ProfileTransactionRow(
                                    log = log,
                                    gold = gold,
                                    redBright = redBright,
                                    redDark = redDark
                                )
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
                Text(
                    "Close",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun ProfileTransactionRow(
    log: TransactionLog,
    gold: Color,
    redBright: Color,
    redDark: Color
) {
    val isDeposit = log.type == "DEPOSIT"
    val statusColor = when (log.status) {
        "APPROVED" -> Color(0xFF2ECC71)
        "REJECTED" -> Color(0xFFE74C3C)
        else -> Color(0xFFF1C40F)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                gold.copy(alpha = 0.25f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDeposit) "Deposit Request" else "Withdrawal Request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = log.dateString,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isDeposit) "+" else "-"}${log.amount}",
                    color = if (isDeposit) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = log.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ==========================================================
// EDIT PROFILE DIALOG
// ==========================================================

@Composable
fun EditProfileDialog(currentName: String, currentEmail: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit){
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Edit Profile", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name", color = Color(0xFFD4AF37)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD4AF37), unfocusedBorderColor = Color(0xFFD4AF37).copy(alpha = 0.5f), focusedLabelColor = Color(0xFFD4AF37), unfocusedLabelColor = Color(0xFFD4AF37).copy(alpha = 0.7f), cursorColor = Color(0xFFD4AF37), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", color = Color(0xFFD4AF37)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD4AF37), unfocusedBorderColor = Color(0xFFD4AF37).copy(alpha = 0.5f), focusedLabelColor = Color(0xFFD4AF37), unfocusedLabelColor = Color(0xFFD4AF37).copy(alpha = 0.7f), cursorColor = Color(0xFFD4AF37), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
        },
        confirmButton = { Button(onClick = { onSave(name, email) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))) { Text("Save", color = Color.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFD4AF37)) } }
    )
}

@Composable
fun RoyalMenuItem(icon: String, title: String, subtitle: String? = null, onClick: () -> Unit, isLogout: Boolean = false) {
    Box(modifier = Modifier.fillMaxWidth().height(if (subtitle == null) 64.dp else 80.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0A0A0A).copy(alpha = 0.7f)).border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(18.dp)).clickable { onClick() }.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) Text(subtitle, color = Color(0xFFD4AF37).copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Icon(Icons.Default.ArrowForwardIos, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CanvasBg() {
    Canvas(Modifier.fillMaxSize()) {
        repeat(60) { drawCircle(Color(0xFFFFD700).copy(alpha = Random.nextFloat() * 0.5f), radius = Random.nextFloat() * 2.5f, center = androidx.compose.ui.geometry.Offset(Random.nextFloat() * size.width, Random.nextFloat() * size.height)) }
    }
}