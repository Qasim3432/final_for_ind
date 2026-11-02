package com.example.final_for_ind.screens.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.final_for_ind.AppConfig
import com.example.final_for_ind.MainActivityL
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.utils.formatDollar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


// ==========================================================
// LOBBY PLAYER DATA CLASS
// ==========================================================

data class LobbyPlayer(
    val token: String,
    val name: String,
    val profilePic: String? = null,   // ✅ NEW
    val color: String? = null,        // ✅ NEW
    val isCreator: Boolean = false
)


// ==========================================================
// PRIVATE ROOM VIEW MODEL
// ==========================================================

class PrivateRoomViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)

    var createdRoomId by mutableStateOf("")
    var roomCode by mutableStateOf("")
    var error by mutableStateOf("")
    var shouldStartGame by mutableStateOf(false)
    var players by mutableStateOf<List<LobbyPlayer>>(emptyList())
    var maxPlayers by mutableStateOf(2)
    var betAmount by mutableStateOf(0)
    var navigationHandled by mutableStateOf(false)

    private var pollingJob: Job? = null


    // --------------------------------------------------
    // CREATE ROOM
    // --------------------------------------------------

    fun createRoom(
        context: Context,
        bet: Int,
        maxPlayers: Int
    ) {
        viewModelScope.launch {

            isLoading = true
            error = ""
            shouldStartGame = false
            navigationHandled = false

            this@PrivateRoomViewModel.maxPlayers = maxPlayers
            this@PrivateRoomViewModel.betAmount = bet

            try {

                val manager = GameSessionManager(context)

                val response = manager.createFriendRoom(
                    bet,
                    maxPlayers == 2
                )

                if (
                    response == null
                    || response.optString("status") != "success"
                ) {
                    error = response?.optString("message")
                        ?: "Room create failed. Server check karo."
                } else {
                    createdRoomId = response.optString("game_id", "")
                    roomCode = response.optString("room_code", "")
                    startPolling(context)
                }

            } catch (e: Exception) {
                error = e.message ?: "Failed"
            }

            isLoading = false
        }
    }


    // --------------------------------------------------
    // JOIN ROOM
    // --------------------------------------------------

    fun joinRoom(context: Context, code: String) {
        viewModelScope.launch {

            isLoading = true
            error = ""
            shouldStartGame = false
            navigationHandled = false

            try {

                val manager = GameSessionManager(context)
                val response = manager.joinFriendRoom(code)

                if (
                    response == null
                    || response.optString("status") != "success"
                ) {
                    error = response?.optString("message")
                        ?: "Room not found or Full"
                } else {
                    createdRoomId = response.optString("game_id", "")
                    roomCode = response.optString("room_code", code)
                    betAmount = response.optInt("bet_amount", 0)

                    maxPlayers = if (
                        response.optBoolean("is_two_player_mode", true)
                    ) 2 else 4

                    shouldStartGame = true
                }

            } catch (e: Exception) {
                error = e.message ?: "Failed"
            }

            isLoading = false
        }
    }


    // ✅ FIX #3 — Cancel room inside ViewModel scope (no GlobalScope)
    fun cancelRoomAsync(context: Context) {

        val gameId = createdRoomId

        if (gameId.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val manager = GameSessionManager(context)
                    manager.cancelFriendRoom(gameId)
                } catch (_: Exception) {
                    // Silent fail — cleanup will happen server-side
                }
            }
        }

        stopPolling()
        createdRoomId = ""
        roomCode = ""
        players = emptyList()
    }


    // --------------------------------------------------
    // POLLING
    // --------------------------------------------------

    private fun startPolling(context: Context) {

        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {

            val manager = GameSessionManager(context)

            val startTime = System.currentTimeMillis()
            val timeoutMs = 5 * 60 * 1000L

            while (
                createdRoomId.isNotBlank()
                && !shouldStartGame
            ) {

                if (
                    System.currentTimeMillis() - startTime
                    > timeoutMs
                ) {
                    error = "Room timed out. Please try again."
                    stopPolling()
                    return@launch
                }

                delay(2000)

                try {

                    val status = manager.getRoomStatus(createdRoomId)

                    if (
                        status != null
                        && status.optString("status") == "success"
                    ) {

                        val gameStatus = status.optString("game_status", "")
                        val playerCount = status.optInt("player_count", 0)

                        players = parsePlayers(status)

                        if (
                            gameStatus == "ACTIVE"
                            || playerCount >= maxPlayers
                        ) {
                            shouldStartGame = true
                        }
                    }

                } catch (_: Exception) {
                    // Silent fail — keep polling
                }
            }
        }
    }


    // --------------------------------------------------
    // PARSE PLAYERS
    // --------------------------------------------------

    private fun parsePlayers(status: JSONObject): List<LobbyPlayer> {

        val result = mutableListOf<LobbyPlayer>()

        val playersArray = status.optJSONArray("players")
            ?: return result

        for (i in 0 until playersArray.length()) {

            val obj = playersArray.optJSONObject(i) ?: continue

            val token = obj.optString("device_token", "")
            val name = obj.optString("name", "Player")
            val color = obj.optString("color", "").takeIf { it.isNotBlank() }

            // ✅ NEW — Parse profile_pic
            var picUrl = obj.optString("profile_pic", "")
                .takeIf { it.isNotBlank() && it != "null" }

            // Prepend base URL if relative
            if (!picUrl.isNullOrBlank() && !picUrl.startsWith("http")) {
                picUrl = AppConfig.BASE_URL.trimEnd('/') + picUrl
            }

            result.add(
                LobbyPlayer(
                    token = token,
                    name = name,
                    profilePic = picUrl,
                    color = color,
                    isCreator = (i == 0)
                )
            )
        }

        return result
    }


    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}


// ==========================================================
// HOST SCREEN
// ==========================================================

@Composable
fun PrivateRoomHostScreen(
    sessionManager: GameSessionManager,
    viewModel: PrivateRoomViewModel = viewModel(),
    onBack: () -> Unit = {},
    onGameStart: (gameId: String, playerToken: String) -> Unit = { _, _ -> },
    onCopyLink: (String) -> Unit = {},
    onRequireLogin: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var showJoinScreen by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("Guest") }

    LaunchedEffect(Unit) {
        try {
            val prefs = context.getSharedPreferences(
                "ludo_session_prefs",
                Context.MODE_PRIVATE
            )
            val savedName = prefs.getString("email_user_name", "Guest") ?: "Guest"

            username = if (savedName.isNotBlank()) savedName else "Guest"
        } catch (_: Exception) {
            username = "Guest"
        }
    }

    // ✅ FIX #4 — Login gate helper
    fun requireLogin(message: String, action: () -> Unit) {
        if (sessionManager.isEmailLoggedIn()) {
            action()
        } else {
            if (onRequireLogin != null) {
                onRequireLogin(message)
            }
        }
    }

    // ✅ FIX #3 — Navigation with flag
    LaunchedEffect(viewModel.shouldStartGame) {

        if (
            viewModel.shouldStartGame
            && viewModel.createdRoomId.isNotBlank()
            && !viewModel.navigationHandled
        ) {
            viewModel.navigationHandled = true

            val intent = Intent(context, MainActivityL::class.java).apply {
                putExtra("GAME_MODE_2P", viewModel.maxPlayers == 2)
                putExtra("GAME_ID", viewModel.createdRoomId)
                putExtra("BET_AMOUNT", viewModel.betAmount)
            }

            context.startActivity(intent)

            viewModel.stopPolling()
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    if (showJoinScreen) {
        JoinRoomScreen(
            isLoading = viewModel.isLoading,
            error = viewModel.error,
            onBack = { showJoinScreen = false },
            onJoin = { code ->
                requireLogin("Room join karne ke liye login karein") {
                    viewModel.joinRoom(context, code)
                }
            }
        )
    } else {
        CreateRoomScreen(
            friendName = username,
            createdRoomId = viewModel.createdRoomId,
            roomCode = viewModel.roomCode,
            players = viewModel.players,
            maxPlayers = viewModel.maxPlayers,
            isLoading = viewModel.isLoading,
            error = viewModel.error,
            onBack = {
                viewModel.stopPolling()
                onBack()
            },
            onCreateLink = { bet, playersCount ->
                requireLogin("Room banane ke liye login karein") {
                    viewModel.createRoom(context, bet, playersCount)
                }
            },
            onShareLink = {
                val link = "Join my Ludo room!\nCode: ${viewModel.roomCode}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share Room")
                )
            },
            onCopyCode = {
                val clipboard = context.getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "Room Code",
                        viewModel.roomCode
                    )
                )

                onCopyLink(viewModel.roomCode)
            },
            // ✅ FIX #3 — uses ViewModel scope
            onCancelRoom = {
                viewModel.cancelRoomAsync(context)
            },
            onJoinRoomClick = {
                requireLogin("Room join karne ke liye login karein") {
                    showJoinScreen = true
                }
            }
        )
    }
}


// ==========================================================
// CREATE ROOM SCREEN
// ==========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    friendName: String = "Guest",
    createdRoomId: String = "",
    roomCode: String = "",
    players: List<LobbyPlayer> = emptyList(),
    maxPlayers: Int = 2,
    isLoading: Boolean = false,
    error: String = "",
    onBack: () -> Unit = {},
    onCreateLink: (Int, Int) -> Unit = { _, _ -> },
    onShareLink: () -> Unit = {},
    onCopyCode: () -> Unit = {},
    onCancelRoom: () -> Unit = {},
    onJoinRoomClick: () -> Unit = {}
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    var selectedBet by remember { mutableStateOf(1000) }
    var roomCreated by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    var showPlayerPopup by remember { mutableStateOf(false) }
    var selectedPlayers by remember { mutableStateOf(2) }

    val betOptions = listOf(500, 1000, 2000, 5000, 10000)

    LaunchedEffect(roomCode) {
        if (roomCode.isNotEmpty()) roomCreated = true
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    BackHandler {
        if (roomCreated && createdRoomId.isNotBlank()) {
            onCancelRoom()
        }
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Private Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFFFD700)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B0000))
                            .border(2.dp, Color(0xFFFFD700), CircleShape)
                            .clickable {
                                if (roomCreated && createdRoomId.isNotBlank()) {
                                    onCancelRoom()
                                }
                                onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    ),
                                    CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B0000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Playing with",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )

                        Text(
                            friendName,
                            color = Color(0xFFFFD700),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Min 2 • Max 4 Players",
                                color = Color(0xFF1A0000),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(
                visible = !roomCreated,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        "Choose Bet Amount",
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        betOptions.forEach { bet ->
                            RoomBetChip(
                                bet = bet,
                                isSelected = selectedBet == bet,
                                onClick = { selectedBet = bet }
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    RoomPremiumButton(
                        text = if (isLoading) "Creating..."
                        else "Create Room - ${formatDollar(selectedBet)}",
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFF8B0000), Color(0xFFD32F2F))
                        ),
                        onClick = { showPlayerPopup = true }
                    )

                    Spacer(Modifier.height(16.dp))

                    RoomPremiumButton(
                        text = "Join Room",
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        ),
                        textColor = Color(0xFF1A0000),
                        onClick = onJoinRoomClick
                    )

                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = roomCreated,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B0000), Color(0xFFD32F2F))
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                                .padding(28.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "🎉 Room Created!",
                                    color = Color(0xFFFFD700),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Bet: ${formatDollar(selectedBet)}  |  Players: $maxPlayers",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "Room Code",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )

                                Text(
                                    roomCode,
                                    color = Color(0xFFFFD700),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 6.sp
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFFFD700),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Waiting for players... ${players.size}/$maxPlayers",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }

                                if (players.isNotEmpty()) {
                                    Spacer(Modifier.height(16.dp))

                                    // ✅ NEW — Player cards with profile pics
                                    players.forEach { player ->
                                        LobbyPlayerCard(player = player)
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.dp, Color(0xFFFFD700))
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    "Share Code",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        roomCode,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (copied) Color(0xFFD32F2F)
                                                else Color(0xFFFFD700)
                                            )
                                            .clickable {
                                                onCopyCode()
                                                copied = true
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Icon(
                                            if (copied) Icons.Default.Done
                                            else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF1A0000),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (copied) {
                                    Text(
                                        "Copied!",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    RoomPremiumButton(
                        text = "Share Room Code",
                        icon = Icons.Default.Share,
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        ),
                        textColor = Color(0xFF1A0000),
                        onClick = onShareLink
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Game ${players.size}/$maxPlayers — waiting...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    RoomPremiumButton(
                        text = "Cancel Room",
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFF2B0000), Color(0xFF4A0000))
                        ),
                        textColor = Color(0xFFFFD700),
                        onClick = {
                            onCancelRoom()
                            onBack()
                        }
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }

    if (showPlayerPopup) {
        AlertDialog(
            onDismissRequest = { showPlayerPopup = false },
            containerColor = Color(0xFF1A0000),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Select Players",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedPlayers == 2)
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2B0000), Color(0xFF2B0000))
                                    )
                            )
                            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
                            .clickable { selectedPlayers = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "2",
                                color = if (selectedPlayers == 2) Color(0xFF1A0000) else Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Players",
                                color = if (selectedPlayers == 2) Color(0xFF1A0000) else Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedPlayers == 4)
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2B0000), Color(0xFF2B0000))
                                    )
                            )
                            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
                            .clickable { selectedPlayers = 4 },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "4",
                                color = if (selectedPlayers == 4) Color(0xFF1A0000) else Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Players",
                                color = if (selectedPlayers == 4) Color(0xFF1A0000) else Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPlayerPopup = false
                    onCreateLink(selectedBet, selectedPlayers)
                }) {
                    Text(
                        "Create Now",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlayerPopup = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}


// ==========================================================
// ✅ NEW — LOBBY PLAYER CARD (with profile pic)
// ==========================================================

@Composable
fun LobbyPlayerCard(player: LobbyPlayer) {

    val avatarBg = lobbyColorFor(player.color)
    val avatarBorder = lobbyColorFor(player.color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ✅ Circular profile pic with fallback initial
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(avatarBg)
                .border(2.dp, avatarBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!player.profilePic.isNullOrBlank()) {
                AsyncImage(
                    model = player.profilePic,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = player.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (player.color != null) {
                Text(
                    text = player.color.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = avatarBorder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (player.isCreator) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFD700))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "HOST",
                    color = Color(0xFF1A0000),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853))
            )
        }
    }
}


// ✅ Helper — color mapping for lobby avatars
private fun lobbyColorFor(color: String?): Color = when (color?.uppercase()) {
    "BLUE"   -> Color(0xFF1976D2)
    "RED"    -> Color(0xFFD32F2F)
    "GREEN"  -> Color(0xFF388E3C)
    "YELLOW" -> Color(0xFFFBC02D)
    else     -> Color(0xFFFFD700)
}


// ==========================================================
// JOIN ROOM SCREEN
// ==========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomScreen(
    isLoading: Boolean = false,
    error: String = "",
    onBack: () -> Unit = {},
    onJoin: (String) -> Unit = {}
) {
    var roomCode by remember { mutableStateOf("") }
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Join Private Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFFFD700)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B0000))
                            .border(2.dp, Color(0xFFFFD700), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        )
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF2B0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Enter Room Code",
                color = Color(0xFFFFD700),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Type the 6-character code shared by your friend",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = roomCode,
                onValueChange = {
                    roomCode = it.uppercase()
                        .filter { c -> c.isLetterOrDigit() }
                        .take(6)
                },
                label = {
                    Text("Room Code", color = Color.White.copy(alpha = 0.6f))
                },
                placeholder = {
                    Text("K9X3MP", color = Color.White.copy(alpha = 0.3f))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2B0000),
                    unfocusedContainerColor = Color(0xFF2B0000).copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                    cursorColor = Color(0xFFFFD700)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (error.isNotEmpty()) {
                Text(
                    error,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            RoomPremiumButton(
                text = if (isLoading) "Joining..." else "Join Room Now",
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                ),
                textColor = Color(0xFF1A0000),
                onClick = {
                    if (roomCode.length == 6) onJoin(roomCode)
                }
            )
        }
    }
}


// ==========================================================
// REUSABLE COMPONENTS
// ==========================================================

@Composable
fun RoomBetChip(
    bet: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "betChipScale"
    )

    Box(
        modifier = modifier
            .width(96.dp)
            .height(70.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected)
                    Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                else
                    Brush.horizontalGradient(listOf(Color(0xFF2B0000), Color(0xFF2B0000))),
                RoundedCornerShape(18.dp)
            )
            .border(
                if (isSelected) 2.5.dp else 2.dp,
                Color(0xFFFFD700),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatDollar(bet),
                color = if (isSelected) Color(0xFF1A0000) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "entry",
                color = if (isSelected) Color(0xFF1A0000).copy(alpha = 0.7f)
                else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}


@Composable
fun RoomPremiumButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    gradient: Brush,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivateRoomPreview() {
    // Preview requires sessionManager
}