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
import kotlinx.coroutines.launch


// ==========================================================
// ADAPTIVE FONT SIZE FOR LONG NUMBERS
// ==========================================================

fun adaptiveFontSize(value: Int): TextUnit {

    val digits = value.toString().length

    return when {
        digits <= 4 -> 24.sp      // 9999
        digits == 5 -> 22.sp      // 99999
        digits == 6 -> 20.sp      // 999999
        digits == 7 -> 18.sp      // 9999999
        digits == 8 -> 16.sp      // 99999999
        digits == 9 -> 14.sp      // 999999999
        digits == 10 -> 13.sp     // 9999999999
        else -> 12.sp             // 10000000000+
    }
}


// ==========================================================
// GAME MODE CARD
// ==========================================================

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
            .height(92.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Color(0xFFFFD700)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.4f),
                            CircleShape
                        )
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "🎲",
                    fontSize = 36.sp
                )

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (subtitle.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "• $subtitle",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
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
    onNavigateToFriends: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var dynamicLiveCoins by remember { mutableStateOf(0) }
    var savedPicUrl by remember { mutableStateOf<String?>(null) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var chosenModeLabel by remember { mutableStateOf("2 PLAYERS") }
    var isTwoPlayerModeSelected by remember { mutableStateOf(true) }
    var isLoadingBalance by remember { mutableStateOf(true) }

    // ==========================================================
    // LOAD BALANCE
    // ==========================================================

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

            val prefs = context.getSharedPreferences(
                "ludo_session_prefs",
                Context.MODE_PRIVATE
            )
            savedPicUrl = prefs.getString("profile_pic_url", null)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingBalance = false
        }
    }

    LaunchedEffect(Unit) {
        loadBalance()
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

                // =====================================================
                // TOP BAR
                // =====================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(
                            2.dp,
                            Color(0xFFFFD700),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // Profile avatar
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        Color(0xFFFFD700),
                                        CircleShape
                                    )
                                    .background(
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                    .clickable { onProfileClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (savedPicUrl != null) {
                                    AsyncImage(
                                        model = savedPicUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            // Balance + buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // $ symbol — fixed size
                                Text(
                                    "$",
                                    color = Color(0xFFFFD700),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.width(2.dp))

                                // Balance amount — adaptive font
                                Text(
                                    text = if (isLoadingBalance) "..."
                                    else "$dynamicLiveCoins",
                                    color = Color.White,
                                    fontSize = if (isLoadingBalance) 24.sp
                                    else adaptiveFontSize(dynamicLiveCoins),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(
                                        min = 30.dp,
                                        max = 110.dp
                                    )
                                )

                                Spacer(Modifier.width(8.dp))

                                // Refresh button
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Color.White.copy(alpha = 0.15f)
                                        )
                                        .clickable {
                                            if (!isLoadingBalance) {
                                                coroutineScope.launch {
                                                    loadBalance()
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(Modifier.width(6.dp))

                                // Deposit button
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFD700))
                                        .clickable { onNavigateToDeposit() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "+",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // =====================================================
                // TITLE
                // =====================================================
                Text(
                    text = "Select Game Mode",
                    color = Color(0xFFFFD700),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(14.dp))

                // =====================================================
                // GAME MODE CARDS
                // =====================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    GameModeCard(
                        title = "Quick Match",
                        backgroundColor = Color(0xFF1B5E20),
                        onClick = {
                            SoundManager.playButtonClick()
                            chosenModeLabel = "2 PLAYERS"
                            isTwoPlayerModeSelected = true
                            showSetupDialog = true
                        },
                        subtitle = "Play against another player",
                        dotColor = Color(0xFF00E676)
                    )

                    GameModeCard(
                        title = "4 Players",
                        backgroundColor = Color(0xFF0D47A1),
                        onClick = {
                            SoundManager.playButtonClick()
                            chosenModeLabel = "4 PLAYERS"
                            isTwoPlayerModeSelected = false
                            showSetupDialog = true
                        },
                        subtitle = "Play with other three players",
                        dotColor = Color(0xFF40C4FF)
                    )

                    GameModeCard(
                        title = "Play with Friends",
                        backgroundColor = Color(0xFFBF360C),
                        onClick = {
                            SoundManager.playButtonClick()
                            onNavigateToFriends()
                        },
                        subtitle = "Invite your friends to play",
                        dotColor = Color(0xFFFFAB40)
                    )

                    GameModeCard(
                        title = "Computer",
                        backgroundColor = Color(0xFF4A148C),
                        onClick = {
                            SoundManager.playButtonClick()
                            Toast.makeText(
                                context,
                                "Coming Soon!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        subtitle = "Play against AI bot",
                        dotColor = Color(0xFFE040FB)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // =====================================================
            // GAME SETUP DIALOG
            // =====================================================
            if (showSetupDialog) {
                GameSetupDialog(
                    headerTitle = chosenModeLabel,
                    onDismiss = { showSetupDialog = false },
                    onPlay = { confirmedBetAmount ->
                        showSetupDialog = false
                        coroutineScope.launch {
                            if (sessionManager != null) {

                                val allocatedGameId =
                                    sessionManager.registerAndJoinMatch(
                                        isTwoPlayerModeSelected
                                    )

                                if (!allocatedGameId.isNullOrBlank()) {

                                    val matchResultJson =
                                        sessionManager.joinWagerMatch(
                                            gameId = allocatedGameId,
                                            betAmount = confirmedBetAmount
                                        )

                                    val isSuccess =
                                        matchResultJson != null &&
                                                matchResultJson.optString(
                                                    "status"
                                                ) == "success"

                                    if (isSuccess) {

                                        loadBalance()

                                        val intent = Intent(
                                            context,
                                            MainActivityL::class.java
                                        ).apply {
                                            putExtra(
                                                "GAME_MODE_2P",
                                                isTwoPlayerModeSelected
                                            )
                                            putExtra(
                                                "GAME_ID",
                                                allocatedGameId
                                            )
                                            putExtra(
                                                "BET_AMOUNT",
                                                confirmedBetAmount
                                            )
                                        }
                                        context.startActivity(intent)

                                    } else {

                                        val msg = matchResultJson
                                            ?.optString("message")
                                            ?: "Insufficient Coins or Ledger Error!"

                                        Toast.makeText(
                                            context,
                                            msg,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Room full hai, dobara try karo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}