package com.example.final_for_ind

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import coil.load

import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.GameSocket
import com.example.final_for_ind.network.VoiceCallManager
import com.example.final_for_ind.network.VoiceState
import com.example.final_for_ind.screens.component.ChatBubblesOverlay
import com.example.final_for_ind.screens.component.ChatButton
import com.example.final_for_ind.screens.component.ChatMessage
import com.example.final_for_ind.screens.component.ChatQuickSender
import com.example.final_for_ind.screens.component.VersusPlayer
import com.example.final_for_ind.screens.component.VersusScreen
import com.example.final_for_ind.screens.component.VoiceCallButton
import com.example.final_for_ind.screens.component.VoiceControlsPanel
import com.example.final_for_ind.screens.component.WinnerScreen
import com.example.final_for_ind.screens.dice_board.LudoBoardView
import com.example.final_for_ind.utils.SoundManager

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.json.JSONObject

import kotlin.math.sin
import kotlin.random.Random


class MainActivityL : AppCompatActivity() {

    // =========================================================
    // GAME
    // =========================================================

    private var gameSocket: GameSocket? = null
    private lateinit var sessionManager: GameSessionManager
    private var currentDiceValue = 1
    private var isAnimating = false
    private var clientHasRolledLock = false

    // =========================================================
    // ✅ F4 — Voice call
    // =========================================================

    private var voiceCallManager: VoiceCallManager? = null
    private var voiceState by mutableStateOf<VoiceState>(VoiceState.Idle)
    private var voicePermissionRequested = false

    // ✅ NEW: Incoming Call State
    private var showIncomingCallDialog by mutableStateOf(false)
    private var incomingCallName by mutableStateOf("")
    private var incomingCallJob: Job? = null

    // =========================================================
    // FINISH FLAGS
    // =========================================================

    private var gameFinished = false
    private var surrenderedByUser = false

    // ✅ NEW: Board Game Over Overlay State
    private var showBoardGameOverMessage by mutableStateOf(false)
    private var boardGameOverDidWin by mutableStateOf(false)

    // ✅ NEW: Home Token Tracker
    private var myHomeTokenCount = 0

    // ✅ NEW: Turn Timer State for UI
    private var showTurnTimer by mutableStateOf(false)
    private var turnTimerSeconds by mutableIntStateOf(15)

    // =========================================================
    // MATCHMAKING TIMEOUT STATE
    // =========================================================

    private var matchmakingTimerJob: Job? = null
    private var cancelDialogShown = false
    private var currentLobbyStatus = ""
    private var tvStatusRef: TextView? = null

    // =========================================================
    // TURN SKIP TIMER
    // =========================================================

    private var turnTimerJob: Job? = null
    private var currentTurnColor: String = ""
    private val TURN_TIMEOUT_SECONDS = 15L

    // =========================================================
    // Track last server status
    // =========================================================

    private var lastServerStatusText: String = ""

    // =========================================================
    // TURN HIGHLIGHT ANIMATION
    // =========================================================

    private var turnPulseJob: Job? = null
    private var currentHighlightedColor: String = ""

    // =========================================================
    // PROFILE VIEWS
    // =========================================================

    private lateinit var profileBlue: View
    private lateinit var profileRed: View
    private lateinit var profileGreen: View
    private lateinit var profileYellow: View

    private lateinit var imgProfileBlue: ImageView
    private lateinit var imgProfileRed: ImageView
    private lateinit var imgProfileGreen: ImageView
    private lateinit var imgProfileYellow: ImageView

    private lateinit var txtSurrenderedBlue: TextView
    private lateinit var txtSurrenderedRed: TextView
    private lateinit var txtSurrenderedGreen: TextView
    private lateinit var txtSurrenderedYellow: TextView

    // =========================================================
    // PLAYER INFORMATION
    // =========================================================

    private var myColor: String? = null
    private var myPlayerName: String = "You"
    private var isTwoPlayerMode: Boolean = true

    // =========================================================
    // VERSUS & WINNER SCREEN STATES
    // =========================================================

    private var showVersus by mutableStateOf(false)
    private var isWaitingForOpponent by mutableStateOf(true)
    private var versusPlayersList by mutableStateOf<List<VersusPlayer>>(emptyList())
    private var currentBet by mutableIntStateOf(0)

    private var versusCompleted by mutableStateOf(false)

    // ✅ F3 — Chat state
    private var activeBubbles by mutableStateOf<Map<String, ChatMessage>>(emptyMap())
    private var showChatSender by mutableStateOf(false)

    // F2 — Rematch notification state
    private var showRematchRequest by mutableStateOf(false)
    private var rematchRequesterName by mutableStateOf("")

    private var showWinner by mutableStateOf(false)
    private var winnerDidWin by mutableStateOf(false)
    private var winnerPayout by mutableIntStateOf(0)

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        SoundManager.init(applicationContext)

        setContentView(R.layout.activity_main_l)

        val ludoBoardView = findViewById<LudoBoardView>(R.id.ludoBoardView)
        val imgDice = findViewById<ImageView>(R.id.imgDice)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvStatusRef = tvStatus

        val btnTestFinishBlue = findViewById<Button>(R.id.btnTestFinishBlue)
        btnTestFinishBlue.visibility = View.GONE

        profileBlue = findViewById(R.id.profileBlue)
        profileRed = findViewById(R.id.profileRed)
        profileGreen = findViewById(R.id.profileGreen)
        profileYellow = findViewById(R.id.profileYellow)

        imgProfileBlue = findViewById(R.id.imgProfileBlue)
        imgProfileRed = findViewById(R.id.imgProfileRed)
        imgProfileGreen = findViewById(R.id.imgProfileGreen)
        imgProfileYellow = findViewById(R.id.imgProfileYellow)

        setCircularClip(imgProfileBlue)
        setCircularClip(imgProfileRed)
        setCircularClip(imgProfileGreen)
        setCircularClip(imgProfileYellow)

        txtSurrenderedBlue = findViewById(R.id.txtSurrenderedBlue)
        txtSurrenderedRed = findViewById(R.id.txtSurrenderedRed)
        txtSurrenderedGreen = findViewById(R.id.txtSurrenderedGreen)
        txtSurrenderedYellow = findViewById(R.id.txtSurrenderedYellow)

        val txtNameBlue = findViewById<TextView>(R.id.txtNameBlue)
        val txtNameRed = findViewById<TextView>(R.id.txtNameRed)
        val txtNameGreen = findViewById<TextView>(R.id.txtNameGreen)
        val txtNameYellow = findViewById<TextView>(R.id.txtNameYellow)

        sessionManager = GameSessionManager(this)

        isTwoPlayerMode = intent.getBooleanExtra("GAME_MODE_2P", true)
        currentBet = intent.getIntExtra("BET_AMOUNT", 0)

        ludoBoardView.isTwoPlayerMode = isTwoPlayerMode

        if (isTwoPlayerMode) {
            profileBlue.visibility = View.VISIBLE
            profileGreen.visibility = View.VISIBLE
            profileRed.visibility = View.GONE
            profileYellow.visibility = View.GONE
        } else {
            profileBlue.visibility = View.VISIBLE
            profileRed.visibility = View.VISIBLE
            profileGreen.visibility = View.VISIBLE
            profileYellow.visibility = View.VISIBLE
        }

        // =====================================================
        // BOARD CALLBACKS
        // =====================================================

        ludoBoardView.onTokenHop = { SoundManager.playTokenHop() }
        ludoBoardView.onTokenKicked = { SoundManager.playTokenKick() }
        ludoBoardView.onAnimationComplete = {
            runOnUiThread { isAnimating = false }
        }

        tvStatus.text = "Connecting to matchmaking..."
        imgDice.alpha = 0.55f

        // =====================================================
        // ✅ F4 — Voice call manager init
        // =====================================================

        voiceCallManager = VoiceCallManager(
            context = applicationContext,
        ) { newState ->
            runOnUiThread {
                voiceState = newState
                Log.d("VOICE_UI", "State changed: $newState")
            }
        }

        // =====================================================
        // SETUP COMPOSE OVERLAY
        // =====================================================

        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        val xmlBoardLayout = rootLayout.getChildAt(0)
        xmlBoardLayout.visibility = View.INVISIBLE

        val composeView = ComposeView(this).apply {
            setContent {
                MaterialTheme {
                    Box(modifier = Modifier.fillMaxSize()) {

                        if (showVersus) {
                            VersusScreen(
                                players = versusPlayersList,
                                betAmount = currentBet,
                                isWaitingForOpponent = isWaitingForOpponent,
                                onCountdownEnd = {
                                    showVersus = false
                                    versusCompleted = true
                                    xmlBoardLayout.visibility = View.VISIBLE
                                }
                            )
                        } else if (showWinner) {
                            WinnerScreen(
                                didWin = winnerDidWin,
                                payout = winnerPayout,
                                showRematchRequest = showRematchRequest,
                                rematchRequesterName = rematchRequesterName,
                                onContinue = { finish() },
                                onRematch = { resetAndStartNewGame() }
                            )
                        }

                        // ✅ F3 — Chat bubbles over profiles
                        if (!showVersus && !showWinner &&
                            (currentLobbyStatus == "ACTIVE" || currentLobbyStatus == "LOBBY")
                        ) {

                            ChatBubblesOverlay(
                                bubbles = activeBubbles,
                                isTwoPlayerMode = isTwoPlayerMode
                            )

                            // ✅ F4 & F3 — Buttons shifted to Bottom (Left & Right of Dice)
                            // Voice Button (Left side of Dice)
                            VoiceCallButton(
                                voiceState = voiceState,
                                onClick = {
                                    SoundManager.playButtonClick()
                                    onVoiceCallButtonPressed()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 35.dp)
                                    .offset(x = (-135).dp)
                                    .graphicsLayer {
                                        scaleX = 1.3f
                                        scaleY = 1.3f
                                    }
                            )

                            // Chat Button (Right side of Dice)
                            ChatButton(
                                hasUnread = false,
                                onClick = {
                                    SoundManager.playButtonClick()
                                    showChatSender = true
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 35.dp)
                                    .offset(x = 135.dp)
                                    .graphicsLayer {
                                        scaleX = 1.3f
                                        scaleY = 1.3f
                                    }
                            )

                            // ✅ F4 — Voice controls panel (bottom center, above dice)
                            VoiceControlsPanel(
                                voiceState = voiceState,
                                onToggleMute = {
                                    SoundManager.playButtonClick()
                                    voiceCallManager?.toggleMute()
                                    val current = voiceState
                                    if (current is VoiceState.Connected) {
                                        voiceState = current.copy(isMuted = !current.isMuted)
                                    }
                                },
                                onToggleSpeaker = {
                                    SoundManager.playButtonClick()
                                    voiceCallManager?.toggleSpeaker()
                                    val current = voiceState
                                    if (current is VoiceState.Connected) {
                                        voiceState = current.copy(isSpeakerOn = !current.isSpeakerOn)
                                    }
                                },
                                onEndCall = {
                                    SoundManager.playButtonClick()
                                    voiceCallManager?.leaveChannel()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 130.dp)
                            )

                            // ✅ NEW: Turn Timer View (Above Dice)
                            if (showTurnTimer) {
                                TurnTimerView(
                                    secondsLeft = turnTimerSeconds,
                                    totalSeconds = TURN_TIMEOUT_SECONDS.toInt(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 130.dp) // Timer dice ke upar
                                )
                            }
                        }

                        // ✅ NEW: Board Game Over Message Overlay (Pulse Animation)
                        if (showBoardGameOverMessage) {
                            // Continuous pulse animation for the text
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ComposeColor.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (boardGameOverDidWin) "YOU WIN!" else "YOU LOSE",
                                    color = if (boardGameOverDidWin) ComposeColor(0xFFFFD700) else ComposeColor(0xFFF44336),
                                    fontSize = 60.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 4.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .scale(pulseScale)
                                        .graphicsLayer {
                                            shadowElevation = 15f
                                        }
                                )
                            }
                        }

                        // ✅ NEW: Incoming Voice Call Dialog
                        if (showIncomingCallDialog) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ComposeColor.Black.copy(alpha = 0.4f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        SoundManager.playButtonClick()
                                        showIncomingCallDialog = false
                                        incomingCallJob?.cancel()
                                    },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(top = 60.dp, start = 16.dp, end = 16.dp)
                                        .fillMaxWidth(0.9f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = ComposeColor(0xFF1A0000)),
                                    border = BorderStroke(2.dp, ComposeColor(0xFFFFD700)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(ComposeColor(0xFF43A047)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Mic",
                                                tint = ComposeColor.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = incomingCallName,
                                                color = ComposeColor.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Wants to voice chat...",
                                                color = ComposeColor.Gray,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                SoundManager.playButtonClick()
                                                showIncomingCallDialog = false
                                                incomingCallJob?.cancel()
                                                startVoiceCall()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFF43A047)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Join", color = ComposeColor.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ✅ F3 — Quick sender popup
                        if (showChatSender) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(ComposeColor.Black.copy(alpha = 0.3f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        SoundManager.playButtonClick()
                                        showChatSender = false
                                    }
                            ) {
                                ChatQuickSender(
                                    onSendMessage = { text ->
                                        SoundManager.playButtonClick()
                                        gameSocket?.sendChatMessage(text)
                                    },
                                    onDismiss = {
                                        SoundManager.playButtonClick()
                                        showChatSender = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 100.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        rootLayout.addView(
            composeView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // =====================================================
        // INITIAL JOIN MATCH / CONNECT SOCKET
        // =====================================================

        lifecycleScope.launch {
            val deviceToken = sessionManager.getOrCreateUserToken()
            var gameId = intent.getStringExtra("GAME_ID") ?: ""

            if (gameId.isBlank()) {
                gameId = sessionManager.registerAndJoinMatch(isTwoPlayerMode)

                if (gameId.isBlank()) {
                    Toast.makeText(
                        this@MainActivityL,
                        "Could not join a game. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@launch
                }

                val wagerResult = sessionManager.joinWagerMatch(
                    gameId = gameId,
                    betAmount = currentBet
                )

                val wagerOk = wagerResult != null &&
                        wagerResult.optString("status") == "success"

                if (!wagerOk) {
                    val msg = wagerResult?.optString("message")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Failed to lock entry fee. Please try again."

                    Log.e("LUDO_WAGER", "Fallback wager failed: $msg")
                    Toast.makeText(this@MainActivityL, msg, Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
            }

            Log.d("LUDO_SETUP", "Matchmaking Success: Game=$gameId Token=$deviceToken")
            setupSocketConnection(gameId, deviceToken)
        }

        // =====================================================
        // DICE CLICK
        // =====================================================

        imgDice.setOnClickListener {
            if (isAnimating || clientHasRolledLock || gameFinished || surrenderedByUser ||
                currentLobbyStatus == "LOBBY" || showVersus || showWinner || showBoardGameOverMessage
            ) {
                return@setOnClickListener
            }

            if (myColor.isNullOrBlank() || !currentTurnColor.equals(myColor, ignoreCase = true)) {
                Log.d("LUDO_DICE", "Blocked — not your turn")
                return@setOnClickListener
            }

            stopTurnTimer()
            SoundManager.playDiceRoll()

            isAnimating = true
            tvStatus.text = "Rolling..."

            playDiceRollAnimation(imgDice) {
                gameSocket?.rollDice()
                isAnimating = false
            }
        }

        // =====================================================
        // TOKEN CLICK
        // =====================================================

        ludoBoardView.onTokenClickListener = { clickedToken ->
            if (!isAnimating && clientHasRolledLock && !gameFinished &&
                !surrenderedByUser && currentLobbyStatus != "LOBBY" &&
                !showVersus && !showWinner && !showBoardGameOverMessage
            ) {
                stopTurnTimer()
                SoundManager.playTokenMove()
                gameSocket?.moveToken(clickedToken.id, clickedToken.color.name)
            }
        }
    }

    // =========================================================
    // ✅ F4 — VOICE CALL BUTTON HANDLER
    // =========================================================

    private fun onVoiceCallButtonPressed() {
        val current = voiceState
        if (current is VoiceState.Connected || current is VoiceState.Joining) {
            Log.d("VOICE", "Already in call or joining")
            return
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!voicePermissionRequested) {
                voicePermissionRequested = true
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    ),
                    REQ_VOICE_PERMISSION
                )
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission required for voice call",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }
        startVoiceCall()
    }

    private fun startVoiceCall() {
        val gameId = intent.getStringExtra("GAME_ID") ?: ""
        if (gameId.isBlank()) {
            Toast.makeText(this, "Game ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        val appId = BuildConfig.AGORA_APP_ID
        if (appId.isBlank() || appId == "YOUR_APP_ID") {
            Toast.makeText(this, "Agora App ID not configured", Toast.LENGTH_LONG).show()
            return
        }

        // ✅ NEW: Opponent ko invite bhejein
        gameSocket?.sendVoiceCallInvite()

        lifecycleScope.launch {
            try {
                voiceCallManager?.initialize(appId)
                voiceCallManager?.joinChannel(sessionManager = sessionManager, gameId = gameId)
            } catch (e: Exception) {
                Log.e("VOICE", "startVoiceCall failed", e)
                Toast.makeText(
                    this@MainActivityL,
                    "Voice call failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================
    // ✅ F4 — Permission result handler
    // =========================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_VOICE_PERMISSION) {
            voicePermissionRequested = false
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceCall()
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    // =========================================================
    // ✅ SOCKET SETUP
    // =========================================================

    private fun setupSocketConnection(gameId: String, deviceToken: String) {
        val ludoBoardView = findViewById<LudoBoardView>(R.id.ludoBoardView)
        val imgDice = findViewById<ImageView>(R.id.imgDice)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val txtNameBlue = findViewById<TextView>(R.id.txtNameBlue)
        val txtNameRed = findViewById<TextView>(R.id.txtNameRed)
        val txtNameGreen = findViewById<TextView>(R.id.txtNameGreen)
        val txtNameYellow = findViewById<TextView>(R.id.txtNameYellow)

        gameSocket = GameSocket(
            gameId,
            deviceToken,
            onConnectionChange = { connected ->
                runOnUiThread {
                    if (gameFinished || surrenderedByUser) return@runOnUiThread
                    if (connected) tvStatus.text = "Connected"
                }
            }
        ) { rootJson ->
            runOnUiThread {
                Log.d("LUDO_WEBSOCKET", "Incoming payload: $rootJson")
                try {
                    val msgType = rootJson.optString("type", "")

                    if (msgType == "chat_message") {
                        val messageObj = rootJson.optJSONObject("message")
                        if (messageObj != null) {
                            val myToken = sessionManager.getOrCreateUserToken()
                            val senderToken = messageObj.optString("sender", "")
                            val senderColor = messageObj.optString("sender_color", "").uppercase()

                            val newMsg = ChatMessage(
                                senderToken = senderToken,
                                senderName = messageObj.optString("sender_name", "Player"),
                                senderColor = senderColor,
                                text = messageObj.optString("text", ""),
                                timestamp = (messageObj.optDouble("ts", 0.0) * 1000).toLong(),
                                isMe = senderToken == myToken
                            )

                            if (senderColor.isNotBlank()) {
                                activeBubbles = activeBubbles + (senderColor to newMsg)
                                val capturedTs = newMsg.timestamp
                                val capturedColor = senderColor

                                // ✅ Chat Receive Sound
                                SoundManager.playButtonClick()

                                lifecycleScope.launch {
                                    delay(4000L)
                                    if (activeBubbles[capturedColor]?.timestamp == capturedTs) {
                                        activeBubbles = activeBubbles - capturedColor
                                    }
                                }
                            }
                        }
                        return@runOnUiThread
                    }

                    if (msgType == "rematch_request") {
                        val fromName = rootJson.optString("from_name", "Opponent")
                        val fromPlayer = rootJson.optString("from_player", "")
                        val myToken = sessionManager.getOrCreateUserToken()

                        if (fromPlayer != myToken && showWinner) {
                            rematchRequesterName = fromName
                            showRematchRequest = true

                            // ✅ Rematch Request Sound
                            SoundManager.playSpinTick()

                            Log.d("REMATCH", "Received rematch request from $fromName")

                            lifecycleScope.launch {
                                delay(45_000L)
                                if (showWinner) showRematchRequest = false
                            }
                        }
                        return@runOnUiThread
                    }

                    // ✅ NEW: Voice Call Invite Receive karna
                    if (msgType == "voice_call_invite") {
                        val fromName = rootJson.optString("from_name", "Opponent")
                        val fromPlayer = rootJson.optString("from_player", "")
                        val myToken = sessionManager.getOrCreateUserToken()

                        if (fromPlayer != myToken && voiceState !is VoiceState.Connected && voiceState !is VoiceState.Joining) {
                            incomingCallName = fromName
                            showIncomingCallDialog = true

                            // ✅ Incoming Call Sound
                            SoundManager.playCoinReward()

                            // 15 seconds baad dialog automatically hata dein
                            incomingCallJob?.cancel()
                            incomingCallJob = lifecycleScope.launch {
                                delay(15000L)
                                if (showIncomingCallDialog) {
                                    showIncomingCallDialog = false
                                }
                            }
                            Log.d("VOICE", "Received voice invite from $fromName")
                        }
                        return@runOnUiThread
                    }

                    if (rootJson.optString("status") != "success") {
                        return@runOnUiThread
                    }

                    val gameState = rootJson.getJSONObject("game_state")

                    updatePlayersFromServer(gameState, txtNameBlue, txtNameRed, txtNameGreen, txtNameYellow)
                    loadPlayerProfilePics(gameState)

                    val gameStatus = gameState.optString("game_status", "")
                    currentLobbyStatus = gameStatus
                    updateSurrenderLabelsFromState(gameState)

                    if (gameStatus == "LOBBY") {
                        parseVersusData(gameState)
                        if (!versusCompleted && !showVersus) {
                            isWaitingForOpponent = true
                            showVersus = true
                        }
                        startMatchmakingTimer()
                    } else if (gameStatus == "ACTIVE") {
                        if (matchmakingTimerJob != null) {
                            matchmakingTimerJob?.cancel()
                            matchmakingTimerJob = null
                        }
                        parseVersusData(gameState)
                        if (!versusCompleted && !showVersus) {
                            isWaitingForOpponent = false
                            showVersus = true
                        } else if (showVersus && isWaitingForOpponent) {
                            isWaitingForOpponent = false
                        }
                    }

                    if (gameStatus == "COMPLETED" || gameStatus == "CANCELLED" || gameStatus == "PAYOUT_ERROR") {
                        // ✅ FIX: GameFinished ko yahan true NAHI karenge, taake board state update ho jaye
                        turnPulseJob?.cancel()
                        stopTurnTimer()
                        gameSocket?.stopReconnect()

                        // ✅ F4 — Leave voice channel when game ends
                        voiceCallManager?.leaveChannel()
                        showIncomingCallDialog = false

                        if (surrenderedByUser) return@runOnUiThread

                        val winnerToken = gameState.optString("winner_device_token", "")
                        val myToken = sessionManager.getOrCreateUserToken()
                        val didWin = winnerToken == myToken
                        val payout = gameState.optInt("winner_payout", 0)

                        // ✅ FIX: Pehle board update karein taake aakhri token move ho jaye
                        parseAndSyncFullGameState(gameState, ludoBoardView, imgDice, tvStatus)

                        // ✅ Game Finished ko true yahan karein taake user click na kar sake
                        gameFinished = true

                        // ✅ FIX: Token move hone ke liye 1.5 seconds ka delay dein
                        lifecycleScope.launch {
                            delay(1500L) // Board animation finish hone dein

                            // ✅ NEW: Ab board par message show karein
                            boardGameOverDidWin = didWin
                            showBoardGameOverMessage = true

                            if (didWin) SoundManager.playWin() else SoundManager.playLose()

                            delay(2500L) // Message 2.5 seconds tak dikhe

                            showBoardGameOverMessage = false
                            showWinnerScreen(didWin = didWin, payout = payout)
                        }
                        return@runOnUiThread
                    }

                    parseAndSyncFullGameState(gameState, ludoBoardView, imgDice, tvStatus)

                } catch (e: Exception) {
                    Log.e("LUDO_WEBSOCKET", "Parsing crash", e)
                }
            }
        }
        gameSocket?.connect()
    }

    // =========================================================
    // Profile pics helpers
    // =========================================================

    private fun setCircularClip(imageView: ImageView) {
        imageView.clipToOutline = true
        imageView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }
    }

    private fun loadPlayerProfilePics(gameState: JSONObject) {
        val assignments = gameState.optJSONObject("player_assignments") ?: return
        val profiles = gameState.optJSONObject("player_profiles")

        val colorToToken = mutableMapOf<String, String>()
        val keys = assignments.keys()
        while (keys.hasNext()) {
            val token = keys.next()
            val color = assignments.optString(token, "").uppercase()
            if (color.isNotBlank()) colorToToken[color] = token
        }

        loadPicForColor(colorToToken["BLUE"], profiles, imgProfileBlue, R.drawable.blue_token)
        loadPicForColor(colorToToken["RED"], profiles, imgProfileRed, R.drawable.red_token)
        loadPicForColor(colorToToken["GREEN"], profiles, imgProfileGreen, R.drawable.green_token)
        loadPicForColor(colorToToken["YELLOW"], profiles, imgProfileYellow, R.drawable.token_yellow)
    }

    private fun loadPicForColor(
        token: String?, profiles: JSONObject?, imageView: ImageView, fallbackRes: Int
    ) {
        if (token == null) {
            imageView.load(fallbackRes)
            return
        }

        var picUrl = profiles?.optString(token, null)
        if (picUrl.isNullOrBlank() || picUrl == "null") {
            imageView.load(fallbackRes)
            return
        }

        if (!picUrl.startsWith("http")) {
            picUrl = AppConfig.BASE_URL.trimEnd('/') + picUrl
        }

        imageView.load(picUrl) {
            crossfade(true)
            placeholder(fallbackRes)
            error(fallbackRes)
        }
    }

    // =========================================================
    // REMATCH LOGIC (F2)
    // =========================================================

    private fun resetAndStartNewGame() {
        gameFinished = false
        surrenderedByUser = false
        showBoardGameOverMessage = false // ✅ Reset
        boardGameOverDidWin = false // ✅ Reset
        myHomeTokenCount = 0 // ✅ Reset
        showTurnTimer = false // ✅ Reset Timer

        matchmakingTimerJob?.cancel()
        matchmakingTimerJob = null
        cancelDialogShown = false
        currentLobbyStatus = ""
        lastServerStatusText = ""
        turnPulseJob?.cancel()
        turnPulseJob = null
        stopTurnTimer()
        currentTurnColor = ""
        currentHighlightedColor = ""
        myColor = null
        showWinner = false
        versusCompleted = false
        showIncomingCallDialog = false

        voiceCallManager?.leaveChannel()

        try {
            gameSocket?.sendRematchRequest()
        } catch (e: Exception) {
            Log.e("REMATCH", "Failed to send rematch_request", e)
        }

        showRematchRequest = false
        rematchRequesterName = ""
        activeBubbles = emptyMap()
        showChatSender = false
        applySurrenderLabels(emptySet())

        showVersus = true
        isWaitingForOpponent = true

        val myToken = sessionManager.getOrCreateUserToken()
        val myName = sessionManager.getSavedUsername()
        val prefs = getSharedPreferences("ludo_session_prefs", MODE_PRIVATE)
        val myPic = prefs.getString("profile_pic_url", null)

        versusPlayersList = listOf(
            VersusPlayer(name = "$myName (You)", picUrl = myPic, isMe = true, color = "BLUE")
        )

        resetProfileView(profileBlue)
        resetProfileView(profileRed)
        resetProfileView(profileGreen)
        resetProfileView(profileYellow)

        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        val xmlBoardLayout = rootLayout.getChildAt(0)
        xmlBoardLayout.visibility = View.INVISIBLE

        tvStatusRef?.text = "Finding new opponent..."
        findViewById<ImageView>(R.id.imgDice).alpha = 0.55f

        lifecycleScope.launch {
            delay(200)
            gameSocket?.disconnect()
            gameSocket = null
            delay(100)

            val deviceToken = sessionManager.getOrCreateUserToken()
            val gameId = sessionManager.registerAndJoinMatch(isTwoPlayerMode)

            if (gameId.isBlank()) {
                Toast.makeText(this@MainActivityL, "Could not find match. Try again.", Toast.LENGTH_SHORT).show()
                showVersus = false
                finish()
                return@launch
            }

            val matchResultJson = sessionManager.joinWagerMatch(gameId = gameId, betAmount = currentBet)
            val isSuccess = matchResultJson != null && matchResultJson.optString("status") == "success"

            if (!isSuccess) {
                val msg = matchResultJson?.optString("message") ?: "Failed to lock bet."
                Toast.makeText(this@MainActivityL, msg, Toast.LENGTH_SHORT).show()
                showVersus = false
                finish()
                return@launch
            }
            setupSocketConnection(gameId, deviceToken)
        }
    }

    // =========================================================
    // Surrender label helpers (F1)
    // =========================================================

    private fun updateSurrenderLabelsFromState(gameState: JSONObject) {
        val surrenderedColorsArr = gameState.optJSONArray("surrendered_colors")
        val surrenderedSet = mutableSetOf<String>()

        if (surrenderedColorsArr != null) {
            for (i in 0 until surrenderedColorsArr.length()) {
                val color = surrenderedColorsArr.optString(i, "").uppercase()
                if (color.isNotBlank()) surrenderedSet.add(color)
            }
        }
        applySurrenderLabels(surrenderedSet)
    }

    private fun applySurrenderLabels(surrenderedColors: Set<String>) {
        txtSurrenderedBlue.visibility = if ("BLUE" in surrenderedColors) View.VISIBLE else View.GONE
        txtSurrenderedRed.visibility = if ("RED" in surrenderedColors) View.VISIBLE else View.GONE
        txtSurrenderedGreen.visibility = if ("GREEN" in surrenderedColors) View.VISIBLE else View.GONE
        txtSurrenderedYellow.visibility = if ("YELLOW" in surrenderedColors) View.VISIBLE else View.GONE
    }

    // =========================================================
    // PARSE DATA FOR VERSUS SCREEN
    // =========================================================

    private fun parseVersusData(gameState: JSONObject) {
        val assignments = gameState.optJSONObject("player_assignments") ?: return
        val names = gameState.optJSONObject("player_names")
        val profiles = gameState.optJSONObject("player_profiles")
        val myToken = sessionManager.getOrCreateUserToken()

        val list = mutableListOf<VersusPlayer>()
        val keys = assignments.keys()

        while (keys.hasNext()) {
            val token = keys.next()
            val color = assignments.optString(token)
            val name = names?.optString(token) ?: "Player"

            var picUrl = profiles?.optString(token)
            if (!picUrl.isNullOrBlank() && !picUrl.startsWith("http")) {
                picUrl = AppConfig.BASE_URL.trimEnd('/') + picUrl
            }

            list.add(
                VersusPlayer(
                    name = if (token == myToken) "$name (You)" else name,
                    picUrl = picUrl,
                    isMe = token == myToken,
                    color = color
                )
            )
        }

        if (list.isNotEmpty()) versusPlayersList = list
    }

    // =========================================================
    // TURN SKIP TIMER (✅ UPDATED: Text remove kar diya)
    // =========================================================

    private fun startTurnTimer(turnColor: String, tvStatus: TextView) {
        if (myColor.isNullOrBlank()) return
        if (!turnColor.equals(myColor, ignoreCase = true)) {
            stopTurnTimer()
            return
        }

        turnTimerJob?.cancel()
        turnTimerJob = lifecycleScope.launch {
            var secondsLeft = TURN_TIMEOUT_SECONDS.toInt()

            // Timer UI ko show karein
            showTurnTimer = true
            turnTimerSeconds = secondsLeft

            while (secondsLeft > 0 && !gameFinished && !surrenderedByUser) {

                // ✅ FIX: tvStatus mein timer nahi dikhayenge, sirf turn ka msg rahega
                if (clientHasRolledLock) {
                    tvStatus.text = "Your turn • Select a token"
                } else {
                    tvStatus.text = "Your Turn! Tap the dice."
                }

                // Timer UI update karein
                turnTimerSeconds = secondsLeft

                // Tick sound (sirf last 5 seconds mein taake annoying na ho)
                if (secondsLeft <= 5) {
                    SoundManager.playButtonClick()
                }

                delay(1000L)
                secondsLeft--
            }

            // Time khatam hone par timer hide karein
            showTurnTimer = false

            if (gameFinished || surrenderedByUser) return@launch
            tvStatus.text = "⏱ Waiting for server..."
        }
    }

    private fun stopTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = null
        showTurnTimer = false // Timer UI hide karein
    }

    // =========================================================
    // DICE ROLL 3D ANIMATION
    // =========================================================

    private fun playDiceRollAnimation(imgDice: ImageView, onComplete: () -> Unit) {
        lifecycleScope.launch {
            val durationMs = 1200L
            val startTime = System.currentTimeMillis()
            var lastFaceChange = 0L

            while (System.currentTimeMillis() - startTime < durationMs) {
                val elapsed = System.currentTimeMillis() - startTime
                val fraction = elapsed.toFloat() / durationMs

                imgDice.rotation = fraction * 720f
                val scale = 1f + 0.25f * sin((fraction * Math.PI).toFloat())
                imgDice.scaleX = scale
                imgDice.scaleY = scale

                if (elapsed - lastFaceChange > 80) {
                    lastFaceChange = elapsed
                    imgDice.setImageResource(getDiceDrawableId(Random.nextInt(1, 7)))
                }
                delay(16L)
            }

            imgDice.animate()
                .rotation(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250L)
                .setInterpolator(OvershootInterpolator(2f))
                .withEndAction { onComplete() }
                .start()
        }
    }

    private fun getDiceDrawableId(value: Int): Int {
        return when (value) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }
    }

    // =========================================================
    // MATCHMAKING TIMER
    // =========================================================

    private fun startMatchmakingTimer() {
        if (matchmakingTimerJob != null) return
        if (cancelDialogShown) return

        matchmakingTimerJob = lifecycleScope.launch {
            var elapsedSeconds = 0
            while (currentLobbyStatus == "LOBBY" && !gameFinished && !surrenderedByUser) {
                delay(1000L)
                elapsedSeconds++

                if (elapsedSeconds == 60 && !cancelDialogShown && currentLobbyStatus == "LOBBY") {
                    cancelDialogShown = true
                    matchmakingTimerJob = null
                    showCancelDialog()
                    return@launch
                }

                if (elapsedSeconds >= 300) {
                    matchmakingTimerJob = null
                    autoCancelMatch()
                    return@launch
                }
            }
            matchmakingTimerJob = null
        }
    }

    private fun showCancelDialog() {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("⏱️ No Opponent Found")
            .setMessage("It's been 60 seconds and no opponent has joined. Do you want to keep waiting or cancel and get a refund?")
            .setCancelable(false)
            .setPositiveButton("Keep Waiting") { dialog, _ ->
                SoundManager.playButtonClick()
                dialog.dismiss()
                cancelDialogShown = false
                startMatchmakingTimer()
            }
            .setNegativeButton("Cancel & Refund") { dialog, _ ->
                SoundManager.playButtonClick()
                dialog.dismiss()
                performCancel()
            }
            .show()
    }

    private fun autoCancelMatch() {
        Toast.makeText(this, "No opponent found. Cancelling...", Toast.LENGTH_LONG).show()
        performCancel()
    }

    private fun performCancel() {
        if (surrenderedByUser) return
        surrenderedByUser = true
        turnPulseJob?.cancel()
        stopTurnTimer()
        voiceCallManager?.leaveChannel()
        showIncomingCallDialog = false
        showBoardGameOverMessage = false // ✅ Reset

        val socket = gameSocket
        if (socket != null) {
            socket.surrender()
            socket.stopReconnect()
        }

        Toast.makeText(this, "Match cancelled. Coins refunded.", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(700)
            finish()
        }
    }

    // =========================================================
    // TURN HIGHLIGHT (GLOW PULSE)
    // =========================================================

    private fun highlightCurrentTurn(currentTurnColor: String) {
        if (currentTurnColor.equals(currentHighlightedColor, true)) return
        currentHighlightedColor = currentTurnColor

        resetProfileView(profileBlue)
        resetProfileView(profileRed)
        resetProfileView(profileGreen)
        resetProfileView(profileYellow)

        val activeView = when (currentTurnColor.uppercase()) {
            "BLUE" -> profileBlue
            "RED" -> profileRed
            "GREEN" -> profileGreen
            "YELLOW" -> profileYellow
            else -> null
        } ?: return

        if (activeView.visibility != View.VISIBLE) return
        startTurnPulse(activeView)
    }

    private fun startTurnPulse(view: View) {
        turnPulseJob?.cancel()
        turnPulseJob = lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            while (!gameFinished && !surrenderedByUser) {
                val elapsed = System.currentTimeMillis() - startTime
                val phase = (elapsed % 1500L) / 1500f
                val pulse = if (phase < 0.5f) phase * 2f else (1f - phase) * 2f
                val scale = 1.05f + 0.08f * pulse
                val strokeWidthDp = 4 + (4 * pulse).toInt()

                runOnUiThread {
                    view.scaleX = scale
                    view.scaleY = scale
                    val drawable = GradientDrawable()
                    drawable.shape = GradientDrawable.RECTANGLE
                    drawable.cornerRadius = 24f
                    drawable.setColor(Color.parseColor("#0A3D3D"))
                    drawable.setStroke(strokeWidthDp, Color.parseColor("#FFD700"))
                    view.background = drawable
                }
                delay(30L)
            }
        }
    }

    private fun resetProfileView(view: View) {
        view.scaleX = 1f
        view.scaleY = 1f
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 24f
        drawable.setColor(Color.parseColor("#0A3D3D"))
        drawable.setStroke(2, Color.parseColor("#555555"))
        drawable.alpha = 180
        view.background = drawable
    }

    // =========================================================
    // UPDATE PLAYERS
    // =========================================================

    private fun updatePlayersFromServer(
        gameState: JSONObject,
        txtNameBlue: TextView,
        txtNameRed: TextView,
        txtNameGreen: TextView,
        txtNameYellow: TextView
    ) {
        val assignments = gameState.optJSONObject("player_assignments") ?: return
        val playerNames = gameState.optJSONObject("player_names")
        val myToken = sessionManager.getOrCreateUserToken()

        myColor = null
        val assignmentKeys = assignments.keys()
        while (assignmentKeys.hasNext()) {
            val token = assignmentKeys.next()
            val color = assignments.optString(token, "")
            if (token == myToken) {
                myColor = color
                myPlayerName = playerNames?.optString(token)?.takeIf { it.isNotBlank() } ?: deviceNameFromToken(token)
                break
            }
        }

        txtNameBlue.text = "Blue"
        txtNameRed.text = "Red"
        txtNameGreen.text = "Green"
        txtNameYellow.text = "Yellow"

        val keys = assignments.keys()
        while (keys.hasNext()) {
            val token = keys.next()
            val color = assignments.optString(token, "")
            val playerName = playerNames?.optString(token)?.takeIf { it.isNotBlank() } ?: deviceNameFromToken(token)

            when (color) {
                "BLUE" -> txtNameBlue.text = playerName
                "RED" -> txtNameRed.text = playerName
                "GREEN" -> txtNameGreen.text = playerName
                "YELLOW" -> txtNameYellow.text = playerName
            }
        }
    }

    private fun deviceNameFromToken(token: String): String {
        if (token.isBlank()) return "Player"
        val parts = token.split("_")
        if (parts.isEmpty()) return "Player"
        if (token.startsWith("sdk_gphone", ignoreCase = true)) return "Android Emulator"
        return parts.take(2).joinToString(" ").ifBlank { "Player" }
    }

    // =========================================================
    // PARSE GAME STATE
    // =========================================================

    private fun parseAndSyncFullGameState(
        gameState: JSONObject,
        boardView: LudoBoardView,
        imgDice: ImageView,
        tvStatus: TextView
    ) {
        currentDiceValue = gameState.optInt("current_dice_value", 1)
        clientHasRolledLock = gameState.optBoolean("has_rolled", false)

        val turnOrder = gameState.optJSONArray("player_turn_order")
        val turnIndex = gameState.optInt("turn_index", 0)

        var currentTurnColorLocal = ""
        if (turnOrder != null && turnOrder.length() > 0 && turnIndex >= 0 && turnIndex < turnOrder.length()) {
            currentTurnColorLocal = turnOrder.optString(turnIndex, "")
        }

        val turnChanged = currentTurnColorLocal != currentTurnColor
        currentTurnColor = currentTurnColorLocal

        if (currentLobbyStatus != "LOBBY") {
            val serverStatusText = gameState.optString("status_text", "")
            val isTimeoutMessage = serverStatusText.contains("Time out", ignoreCase = true) || serverStatusText.contains("timed out", ignoreCase = true)

            if (isTimeoutMessage && serverStatusText != lastServerStatusText) {
                val isMyTurnNow = !myColor.isNullOrBlank() && currentTurnColorLocal.equals(myColor, ignoreCase = true)
                runOnUiThread {
                    if (isMyTurnNow) {
                        Toast.makeText(this@MainActivityL, "⏰ Opponent's time was up! Your turn now.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivityL, "⏰ A player's turn timed out!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            lastServerStatusText = serverStatusText
            tvStatus.text = createFriendlyStatus(gameState, currentTurnColorLocal)
        }

        highlightCurrentTurn(currentTurnColorLocal)

        val diceDrawables = listOf(R.drawable.dice_1, R.drawable.dice_2, R.drawable.dice_3, R.drawable.dice_4, R.drawable.dice_5, R.drawable.dice_6)
        imgDice.setImageResource(diceDrawables[(currentDiceValue - 1).coerceIn(0, 5)])

        val isMyTurnNow = !myColor.isNullOrBlank() && currentTurnColorLocal.equals(myColor, ignoreCase = true)
        imgDice.alpha = if (isMyTurnNow) 1f else 0.55f

        val validKeys = mutableSetOf<String>()
        if (clientHasRolledLock && !myColor.isNullOrBlank() && currentTurnColorLocal.equals(myColor, ignoreCase = true)) {
            val tokensArray = gameState.optJSONArray("tokens")
            if (tokensArray != null) {
                val roll = currentDiceValue
                for (i in 0 until tokensArray.length()) {
                    val t = tokensArray.getJSONObject(i)
                    val id = t.getInt("id")
                    val colorStr = t.getString("color")
                    val pos = t.getInt("position")

                    if (!colorStr.equals(myColor, ignoreCase = true)) continue

                    var isValid = false
                    if (pos == -1 && roll == 6) isValid = true
                    else if (pos >= 0 && pos + roll <= 56) isValid = true

                    if (isValid) validKeys.add("${colorStr}_${id}")
                }
            }
        }

        boardView.validTokenKeys = validKeys

        if (currentLobbyStatus == "ACTIVE" && !gameFinished) {
            val isMyTurn = !myColor.isNullOrBlank() && currentTurnColorLocal.equals(myColor, ignoreCase = true)
            if (isMyTurn && turnChanged) startTurnTimer(currentTurnColorLocal, tvStatus)
            else if (!isMyTurn) stopTurnTimer()
        }

        // ✅ NEW: Check if my token reached home
        if (myColor != null) {
            var currentHomeCount = 0
            val tokensArray = gameState.optJSONArray("tokens")
            if (tokensArray != null) {
                for (i in 0 until tokensArray.length()) {
                    val t = tokensArray.getJSONObject(i)
                    if (t.getString("color").equals(myColor, ignoreCase = true) && t.getInt("position") == 56) {
                        currentHomeCount++
                    }
                }
            }

            // Agar naya token home pohancha hai
            if (currentHomeCount > myHomeTokenCount) {
                myHomeTokenCount = currentHomeCount

                // Agar game khatam nahi hua (yani 1, 2 ya 3 token home pohanche hain)
                if (myHomeTokenCount < 4) {
                    // ✅ NEW: Phone Vibrate karein
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(150)
                    }

                    // ✅ NEW: Board ko halka sa pop (bara-chota) animation dein
                    boardView.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).withEndAction {
                        boardView.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }.start()

                    SoundManager.playTokenKick()
                }
            }
        }

        val tokensArray = gameState.optJSONArray("tokens")
        if (tokensArray != null) boardView.updateBoardStateFromServer(tokensArray)
    }

    // =========================================================
    // FRIENDLY STATUS
    // =========================================================

    private fun createFriendlyStatus(gameState: JSONObject, currentTurnColor: String): String {
        val playerAssignments = gameState.optJSONObject("player_assignments")
        val playerCount = playerAssignments?.length() ?: 0

        if (playerCount < 2) return "Waiting for opponent..."
        if (gameState.optString("game_status") == "COMPLETED") return "Game completed"

        if (!myColor.isNullOrBlank() && currentTurnColor.equals(myColor, ignoreCase = true)) {
            return if (clientHasRolledLock) "Your turn • Select a token" else "Your Turn! Tap the dice."
        }

        val playerNames = gameState.optJSONObject("player_names")
        val opponentName = getPlayerNameForColor(playerAssignments, playerNames, currentTurnColor)

        return if (opponentName.isNotBlank()) "$opponentName's Turn" else "$currentTurnColor's Turn"
    }

    private fun getPlayerNameForColor(assignments: JSONObject?, playerNames: JSONObject?, color: String): String {
        if (assignments == null) return ""
        val keys = assignments.keys()
        while (keys.hasNext()) {
            val token = keys.next()
            val assignedColor = assignments.optString(token, "")
            if (assignedColor.equals(color, ignoreCase = true)) {
                return playerNames?.optString(token)?.takeIf { it.isNotBlank() } ?: deviceNameFromToken(token)
            }
        }
        return ""
    }

    // =========================================================
    // WINNER SCREEN
    // =========================================================

    private fun showWinnerScreen(didWin: Boolean, payout: Int) {
        winnerDidWin = didWin
        winnerPayout = payout
        showWinner = true
    }

    private fun showSurrenderScreen() {
        SoundManager.playLose()
        winnerDidWin = false
        winnerPayout = 0
        showWinner = true
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (surrenderedByUser) {
            super.onBackPressed()
            return
        }

        val socket = gameSocket
        if (socket == null || gameFinished) {
            super.onBackPressed()
            return
        }

        if (currentLobbyStatus == "LOBBY") {
            performCancel()
            return
        }

        surrenderedByUser = true
        turnPulseJob?.cancel()
        stopTurnTimer()

        voiceCallManager?.leaveChannel()
        showIncomingCallDialog = false
        showBoardGameOverMessage = false

        socket.surrender()
        socket.stopReconnect()
        showSurrenderScreen()
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {
        super.onDestroy()

        matchmakingTimerJob?.cancel()
        matchmakingTimerJob = null
        turnPulseJob?.cancel()
        turnPulseJob = null
        turnTimerJob?.cancel()
        turnTimerJob = null
        gameSocket?.disconnect()

        // ✅ F4 — Release voice engine
        voiceCallManager?.release()
        voiceCallManager = null
    }

    // =========================================================
    // COMPANION OBJECT
    // =========================================================

    companion object {
        private const val REQ_VOICE_PERMISSION = 7001
    }
}

// =========================================================
// ✅ NEW: Turn Timer View Composable
// =========================================================
@Composable
fun TurnTimerView(
    secondsLeft: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = secondsLeft.toFloat() / totalSeconds.toFloat()

    // Colors based on time
    val timerColor = when {
        secondsLeft > 10 -> ComposeColor(0xFF4CAF50) // Green
        secondsLeft > 5 -> ComposeColor(0xFFFFC107)  // Amber/Yellow
        else -> ComposeColor(0xFFF44336)             // Red
    }

    // Pulse animation only when time is low (<= 5s)
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (secondsLeft <= 5) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(65.dp)
            .scale(if (secondsLeft <= 5) pulseScale else 1f)
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = ComposeColor.Black.copy(alpha = 0.5f),
            strokeWidth = 6.dp
        )

        // Progress circle
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = timerColor,
            strokeWidth = 6.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Text in the center
        Text(
            text = secondsLeft.toString(),
            color = ComposeColor.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}