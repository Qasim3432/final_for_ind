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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.final_for_ind.MainActivityL
import com.example.final_for_ind.R
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.screens.component.BottomNavBar
import com.example.final_for_ind.ui.theme.Orange
import com.example.final_for_ind.ui.theme.Purple40
import kotlinx.coroutines.launch

@Composable
fun GameModeCard(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    subtitle: String = "",
    dotColor: Color = Color.Red
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = 0.8f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF8B1A00), Color(0xFFE67E00), Color(0xFFFFA500))
                    )
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(text = "🎲", fontSize = 40.sp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = "• $subtitle",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
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
    onNavigateToGift: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dynamicLiveCoins by remember { mutableStateOf(0) }
    var savedPicUrl by remember { mutableStateOf<String?>(null) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var chosenModeLabel by remember { mutableStateOf("2 PLAYERS") }
    var isTwoPlayerModeSelected by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (sessionManager != null) {
            try {
                val token = sessionManager.getOrCreateUserToken()
                val balanceResult = sessionManager.fetchUserBalanceFromServer(token)
                dynamicLiveCoins = balanceResult.coins
                val prefs = context.getSharedPreferences("ludo_session_prefs", Context.MODE_PRIVATE)
                savedPicUrl = prefs.getString("profile_pic_url", null)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = 0,
                onItemClick = { index ->
                    when (index) {
                        0 -> {}
                        1 -> onNavigateToWallet()
                        2 -> onNavigateToProfile()
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp)
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
                                    .clickable { onProfileClick() },
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
                                    Icon(Icons.Default.Person, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("\$", color = Color(0xFFFFD700), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                    Text("$dynamicLiveCoins", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFD700))
                                            .clickable { onNavigateToDeposit() },
                                        contentAlignment = Alignment.Center
                                    ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
                                }
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, null, tint = Color(0xFFFFD700), modifier = Modifier.size(30.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Select Game Mode",
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameModeCard("Quick Match", Color(0xFF4CAF50), {
                        chosenModeLabel = "2 PLAYERS"
                        isTwoPlayerModeSelected = true
                        showSetupDialog = true
                    }, subtitle = "Play against another\nplayer", dotColor = Color(0xFFFF4444))

                    GameModeCard("4 Players", Color(0xFF2196F3), {
                        chosenModeLabel = "4 PLAYERS"
                        isTwoPlayerModeSelected = false
                        showSetupDialog = true
                    }, subtitle = "Play with other three\nplayers", dotColor = Color(0xFFFFC107))

                    GameModeCard("Play with Friends", Orange, {
                        chosenModeLabel = "FRIENDS MATCH"
                        isTwoPlayerModeSelected = true
                        showSetupDialog = true
                    }, subtitle = "Invite your friends to", dotColor = Color(0xFFFF4444))

                    GameModeCard("Computer", Purple40, {
                        chosenModeLabel = "COMPUTER"
                        isTwoPlayerModeSelected = true
                        showSetupDialog = true
                    }, subtitle = "Play against AI", dotColor = Color(0xFFFFC107))
                }
            }

            if (showSetupDialog) {
                GameSetupDialog(
                    headerTitle = chosenModeLabel,
                    onDismiss = { showSetupDialog = false },
                    onPlay = { confirmedBetAmount ->
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
                                        val currentToken = sessionManager.getOrCreateUserToken()
                                        val balanceResult = sessionManager.fetchUserBalanceFromServer(currentToken)
                                        dynamicLiveCoins = balanceResult.coins
                                        val intent = Intent(context, MainActivityL::class.java).apply {
                                            putExtra("GAME_MODE_2P", isTwoPlayerModeSelected)
                                            putExtra("GAME_ID", allocatedGameId)
                                            putExtra("BET_AMOUNT", confirmedBetAmount)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val msg = matchResultJson?.optString("message") ?: "Insufficient Coins or Ledger Error!"
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