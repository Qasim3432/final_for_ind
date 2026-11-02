package com.example.final_for_ind.screens.home_lobby

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.final_for_ind.MainActivityL
import com.example.final_for_ind.R
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.screens.component.BottomNavBar
import com.example.final_for_ind.utils.SoundManager
import com.example.final_for_ind.utils.formatDollar
import kotlinx.coroutines.launch
import org.json.JSONObject

fun adaptiveFontSize(value: Int): TextUnit {
    val digits = value.toString().length
    return when {
        digits <= 4 -> 24.sp
        digits == 5 -> 22.sp
        digits == 6 -> 20.sp
        digits == 7 -> 18.sp
        digits == 8 -> 16.sp
        digits == 9 -> 14.sp
        digits == 10 -> 13.sp
        else -> 12.sp
    }
}

@Composable
fun GameModeCard(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    subtitle: String = "",
    dotColor: Color = Color.Red
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(92.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(dotColor)
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(text = "🎲", fontSize = 36.sp)
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title, color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "• $subtitle", color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDeposit: () -> Unit,
    sessionManager: GameSessionManager? = null,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGift: () -> Unit,
    onNavigateToFriends: () -> Unit = {},
    onRequireLogin: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dynamicLiveCoins by remember { mutableStateOf(0) }
    var savedPicUrl by remember { mutableStateOf<String?>(null) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var chosenModeLabel by remember { mutableStateOf("2 PLAYERS") }
    var isTwoPlayerModeSelected by remember { mutableStateOf(true) }
    var isLoadingBalance by remember { mutableStateOf(true) }

    val gate: (String, () -> Unit) -> Unit = { msg, act ->
        if (sessionManager?.isEmailLoggedIn() == true) {
            act()
        } else {
            onRequireLogin?.invoke(msg)
        }
    }

    suspend fun loadBalance() {
        if (sessionManager == null) {
            isLoadingBalance = false
            return
        }
        try {
            isLoadingBalance = true
            val token = sessionManager.getOrCreateUserToken()
            val balanceResult = sessionManager.fetchUserBalanceFromServer(token)
            dynamicLiveCoins = balanceResult.coins
            val prefs = context.getSharedPreferences("ludo_session_prefs", Context.MODE_PRIVATE)
            savedPicUrl = prefs.getString("profile_pic_url", null)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingBalance = false
        }
    }

    LaunchedEffect(Unit) { loadBalance() }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = 0,
                onItemClick = { index ->
                    when (index) {
                        0 -> {}
                        1 -> onNavigateToWallet()
                        2 -> gate("Profile dekhne ke liye login karein") { onNavigateToProfile() }
                        3 -> onNavigateToGift()
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.p),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFFFFD700), CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .clickable {
                                        gate("Profile dekhne ke liye login karein") { onProfileClick() }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (savedPicUrl != null) {
                                    AsyncImage(
                                        model = savedPicUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person, null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isLoadingBalance) "$0.00" else formatDollar(dynamicLiveCoins),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(min = 30.dp, max = 110.dp)
                                )
                                Spacer(Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable {
                                            if (!isLoadingBalance) coroutineScope.launch { loadBalance() }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Refresh, "Refresh",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFD700))
                                        .clickable {
                                            gate("Deposit karne ke liye login karein") {
                                                onNavigateToDeposit()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                gate("Settings dekhne ke liye login karein") { onSettingsClick() }
                            }
                        ) {
                            Icon(
                                Icons.Default.Settings, null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Select Game Mode",
                    color = Color(0xFFFFD700),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    GameModeCard(
                        title = "Quick Match",
                        backgroundColor = Color(0xFF1B5E20),
                        onClick = {
                            gate("Quick Match khelne ke liye login karein") {
                                SoundManager.playButtonClick()
                                chosenModeLabel = "2 PLAYERS"
                                isTwoPlayerModeSelected = true
                                showSetupDialog = true
                            }
                        },
                        subtitle = "Play against another player",
                        dotColor = Color(0xFF00E676)
                    )

                    GameModeCard(
                        title = "4 Players",
                        backgroundColor = Color(0xFF0D47A1),
                        onClick = {
                            gate("4 Player match khelne ke liye login karein") {
                                SoundManager.playButtonClick()
                                chosenModeLabel = "4 PLAYERS"
                                isTwoPlayerModeSelected = false
                                showSetupDialog = true
                            }
                        },
                        subtitle = "Play with other three players",
                        dotColor = Color(0xFF40C4FF)
                    )

                    GameModeCard(
                        title = "Play with Friends",
                        backgroundColor = Color(0xFFBF360C),
                        onClick = {
                            gate("Friends ke saath khelne ke liye login karein") {
                                SoundManager.playButtonClick()
                                onNavigateToFriends()
                            }
                        },
                        subtitle = "Invite your friends to play",
                        dotColor = Color(0xFFFFAB40)
                    )

                    GameModeCard(
                        title = "Computer",
                        backgroundColor = Color(0xFF4A148C),
                        onClick = {
                            SoundManager.playButtonClick()
                            Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
                        },
                        subtitle = "Play against AI bot",
                        dotColor = Color(0xFFE040FB)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            if (showSetupDialog) {
                GameSetupDialog(
                    headerTitle = chosenModeLabel,
                    onDismiss = { showSetupDialog = false },
                    onPlay = { confirmedBetAmount: Int -> // 🟢 Yahan : Int add kar diya hai
                        showSetupDialog = false
                        coroutineScope.launch {
                            if (sessionManager != null) {
                                val allocatedGameId = sessionManager.registerAndJoinMatch(isTwoPlayerModeSelected)
                                if (!allocatedGameId.isNullOrBlank()) {
                                    val matchResultJson = sessionManager.joinWagerMatch(
                                        gameId = allocatedGameId,
                                        betAmount = confirmedBetAmount
                                    )
                                    val isSuccess = matchResultJson != null &&
                                            matchResultJson.optString("status") == "success"
                                    if (isSuccess) {
                                        loadBalance()
                                        val intent = Intent(context, MainActivityL::class.java).apply {
                                            putExtra("GAME_MODE_2P", isTwoPlayerModeSelected)
                                            putExtra("GAME_ID", allocatedGameId)
                                            putExtra("BET_AMOUNT", confirmedBetAmount)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val msg = matchResultJson?.optString("message")
                                            ?: "Insufficient Balance or Ledger Error!"
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Room full hai, dobara try karo", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GameSetupDialog(
    headerTitle: String,
    onDismiss: () -> Unit,
    onPlay: (Int) -> Unit
) {
    val betOptions = listOf(250, 500, 1000, 2000, 5000, 10000)
    var selectedBet by remember { mutableStateOf(250) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0000),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Select Entry Fee",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in betOptions.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BetOptionCard(
                            amount = betOptions[i],
                            isSelected = selectedBet == betOptions[i],
                            onClick = { selectedBet = betOptions[i] },
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < betOptions.size) {
                            BetOptionCard(
                                amount = betOptions[i + 1],
                                isSelected = selectedBet == betOptions[i + 1],
                                onClick = { selectedBet = betOptions[i + 1] },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPlay(selectedBet) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PLAY NOW", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun BetOptionCard(
    amount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFFFFD700) else Color(0xFF2B0000)
            )
            .border(
                2.dp,
                if (isSelected) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatDollar(amount),
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}