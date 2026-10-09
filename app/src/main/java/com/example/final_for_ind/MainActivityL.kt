package com.example.final_for_ind

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope

import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.GameSocket
import com.example.final_for_ind.screens.dice_board.LudoBoardView
import com.example.final_for_ind.screens.dice_board.WinnerScreen
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
    // FINISH FLAGS
    // =========================================================

    private var gameFinished = false

    private var surrenderedByUser = false

    // =========================================================
    // MATCHMAKING TIMEOUT STATE
    // =========================================================

    private var matchmakingTimerJob: Job? = null

    private var cancelDialogShown = false

    private var currentLobbyStatus = ""

    private var tvStatusRef: TextView? = null

    // =========================================================
    // TURN SKIP TIMER (AFK PREVENTION)
    // =========================================================

    private var turnTimerJob: Job? = null

    private var currentTurnColor: String = ""

    // 40 seconds — timeout pe turn skip ho jayega
    private val TURN_TIMEOUT_SECONDS = 40L

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

    // =========================================================
    // PLAYER INFORMATION
    // =========================================================

    private var myColor: String? = null

    private var myPlayerName: String = "You"

    private var isTwoPlayerMode: Boolean = true

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        SoundManager.init(applicationContext)

        setContentView(R.layout.activity_main_l)

        val ludoBoardView =
            findViewById<LudoBoardView>(R.id.ludoBoardView)

        val imgDice =
            findViewById<ImageView>(R.id.imgDice)

        val tvStatus =
            findViewById<TextView>(R.id.tvStatus)

        tvStatusRef = tvStatus

        val btnTestFinishBlue =
            findViewById<Button>(R.id.btnTestFinishBlue)

        btnTestFinishBlue.visibility = View.GONE

        profileBlue = findViewById(R.id.profileBlue)
        profileRed = findViewById(R.id.profileRed)
        profileGreen = findViewById(R.id.profileGreen)
        profileYellow = findViewById(R.id.profileYellow)

        val txtNameBlue = findViewById<TextView>(R.id.txtNameBlue)
        val txtNameRed = findViewById<TextView>(R.id.txtNameRed)
        val txtNameGreen = findViewById<TextView>(R.id.txtNameGreen)
        val txtNameYellow = findViewById<TextView>(R.id.txtNameYellow)

        sessionManager = GameSessionManager(this)

        isTwoPlayerMode = intent.getBooleanExtra("GAME_MODE_2P", true)

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

        ludoBoardView.onTokenHop = {
            SoundManager.playTokenHop()
        }

        ludoBoardView.onTokenKicked = {
            SoundManager.playTokenKick()
        }

        ludoBoardView.onAnimationComplete = {
            runOnUiThread {
                isAnimating = false
            }
        }

        tvStatus.text = "Connecting to matchmaking..."

        // =====================================================
        // JOIN MATCH / CONNECT SOCKET
        // =====================================================

        lifecycleScope.launch {

            val deviceToken = sessionManager.getOrCreateUserToken()

            var gameId = intent.getStringExtra("GAME_ID") ?: ""

            if (gameId.isBlank()) {
                gameId = sessionManager.registerAndJoinMatch(isTwoPlayerMode)
            }

            if (gameId.isBlank()) {
                Toast.makeText(
                    this@MainActivityL,
                    "Could not join a game. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            Log.d(
                "LUDO_SETUP",
                "Matchmaking Success: Game=$gameId Token=$deviceToken"
            )

            gameSocket = GameSocket(
                gameId,
                deviceToken,
                onConnectionChange = { connected ->
                    runOnUiThread {
                        if (gameFinished || surrenderedByUser) {
                            return@runOnUiThread
                        }
                        tvStatus.text = if (!connected) "Reconnecting..." else "Connected"
                    }
                }
            ) { rootJson ->

                runOnUiThread {

                    Log.d("LUDO_WEBSOCKET", "Incoming payload: $rootJson")

                    try {

                        if (rootJson.optString("status") != "success") {
                            return@runOnUiThread
                        }

                        val gameState = rootJson.getJSONObject("game_state")

                        updatePlayersFromServer(
                            gameState,
                            txtNameBlue,
                            txtNameRed,
                            txtNameGreen,
                            txtNameYellow
                        )

                        val gameStatus = gameState.optString("game_status", "")

                        currentLobbyStatus = gameStatus

                        if (gameStatus == "LOBBY") {
                            startMatchmakingTimer()
                        } else if (gameStatus == "ACTIVE" &&
                            matchmakingTimerJob != null
                        ) {
                            matchmakingTimerJob?.cancel()
                            matchmakingTimerJob = null
                        }

                        if (gameStatus == "COMPLETED" ||
                            gameStatus == "CANCELLED"
                        ) {
                            gameFinished = true
                            turnPulseJob?.cancel()
                            stopTurnTimer()

                            if (surrenderedByUser) return@runOnUiThread

                            val winnerToken = gameState.optString("winner_device_token", "")
                            val myToken = sessionManager.getOrCreateUserToken()
                            val didWin = winnerToken == myToken
                            val payout = gameState.optInt("winner_payout", 0)

                            showWinnerScreen(didWin = didWin, payout = payout)
                            return@runOnUiThread
                        }

                        parseAndSyncFullGameState(
                            gameState,
                            ludoBoardView,
                            imgDice,
                            tvStatus
                        )

                    } catch (e: Exception) {
                        Log.e("LUDO_WEBSOCKET", "Parsing crash", e)
                    }
                }
            }

            gameSocket?.connect()
        }

        // =====================================================
        // DICE CLICK
        // =====================================================

        imgDice.setOnClickListener {

            if (isAnimating ||
                clientHasRolledLock ||
                gameFinished ||
                surrenderedByUser ||
                currentLobbyStatus == "LOBBY"
            ) {
                return@setOnClickListener
            }

            // Player acted — cancel turn timer
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

            if (!isAnimating &&
                clientHasRolledLock &&
                !gameFinished &&
                !surrenderedByUser &&
                currentLobbyStatus != "LOBBY"
            ) {
                // Player acted — cancel turn timer
                stopTurnTimer()

                SoundManager.playTokenMove()
                gameSocket?.moveToken(
                    clickedToken.id,
                    clickedToken.color.name
                )
            }
        }
    }

    // =========================================================
    // TURN SKIP TIMER
    //
    // - Sirf apni turn pe 40s countdown
    // - Timeout pe: skip_turn message server ko bhejo
    // - Player tap kare (dice ya token) → timer cancel
    // =========================================================

    private fun startTurnTimer(
        turnColor: String,
        tvStatus: TextView
    ) {

        if (myColor.isNullOrBlank()) return

        // Sirf apni turn pe timer chalao
        if (!turnColor.equals(myColor, ignoreCase = true)) {
            stopTurnTimer()
            return
        }

        // Pehle wala timer cancel
        turnTimerJob?.cancel()

        turnTimerJob = lifecycleScope.launch {

            var secondsLeft = TURN_TIMEOUT_SECONDS

            while (secondsLeft > 0 && !gameFinished && !surrenderedByUser) {

                tvStatus.text = "⏱ Your turn • ${secondsLeft}s"

                delay(1000L)
                secondsLeft--
            }

            if (gameFinished || surrenderedByUser) return@launch

            // TIMEOUT — skip turn
            Log.d("AFK_TIMER", "Turn timeout — sending skip_turn")

            tvStatus.text = "⏱ Time up! Skipping turn..."

            gameSocket?.skipTurn()
        }
    }

    private fun stopTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = null
    }

    // =========================================================
    // DICE ROLL 3D ANIMATION
    // =========================================================

    private fun playDiceRollAnimation(
        imgDice: ImageView,
        onComplete: () -> Unit
    ) {

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
                    imgDice.setImageResource(
                        getDiceDrawableId(Random.nextInt(1, 7))
                    )
                }

                delay(16L)
            }

            imgDice.animate()
                .rotation(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250L)
                .setInterpolator(OvershootInterpolator(2f))
                .withEndAction {
                    onComplete()
                }
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

            while (currentLobbyStatus == "LOBBY" &&
                !gameFinished &&
                !surrenderedByUser
            ) {

                delay(1000L)
                elapsedSeconds++

                val remainingForDialog = 60 - elapsedSeconds

                if (remainingForDialog > 0 &&
                    tvStatusRef != null &&
                    currentLobbyStatus == "LOBBY"
                ) {
                    tvStatusRef?.text =
                        "Finding opponent... ${remainingForDialog}s"
                }

                if (elapsedSeconds == 60 &&
                    !cancelDialogShown &&
                    currentLobbyStatus == "LOBBY"
                ) {
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
            .setMessage(
                "It's been 60 seconds and no opponent " +
                        "has joined. Do you want to keep waiting " +
                        "or cancel and get a refund?"
            )
            .setCancelable(false)
            .setPositiveButton("Keep Waiting") { dialog, _ ->
                dialog.dismiss()
                cancelDialogShown = false
                startMatchmakingTimer()
            }
            .setNegativeButton("Cancel & Refund") { dialog, _ ->
                dialog.dismiss()
                performCancel()
            }
            .show()
    }

    private fun autoCancelMatch() {
        Toast.makeText(
            this,
            "No opponent found. Cancelling...",
            Toast.LENGTH_LONG
        ).show()
        performCancel()
    }

    private fun performCancel() {

        if (surrenderedByUser) return

        surrenderedByUser = true
        turnPulseJob?.cancel()
        stopTurnTimer()

        val socket = gameSocket

        if (socket != null) {
            socket.surrender()
            socket.stopReconnect()
        }

        Toast.makeText(
            this,
            "Match cancelled. Coins refunded.",
            Toast.LENGTH_SHORT
        ).show()

        lifecycleScope.launch {
            delay(700)
            finish()
        }
    }

    // =========================================================
    // TURN HIGHLIGHT (GLOW PULSE)
    // =========================================================

    private fun highlightCurrentTurn(currentTurnColor: String) {

        if (currentTurnColor.equals(currentHighlightedColor, true)) {
            return
        }

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

                val pulse = if (phase < 0.5f) {
                    phase * 2f
                } else {
                    (1f - phase) * 2f
                }

                val scale = 1.05f + 0.08f * pulse
                val strokeWidthDp = 4 + (4 * pulse).toInt()

                runOnUiThread {

                    view.scaleX = scale
                    view.scaleY = scale

                    val drawable = GradientDrawable()
                    drawable.shape = GradientDrawable.RECTANGLE
                    drawable.cornerRadius = 24f
                    drawable.setColor(Color.parseColor("#0A3D3D"))
                    drawable.setStroke(
                        strokeWidthDp,
                        Color.parseColor("#FFD700")
                    )
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

        val assignments = gameState.optJSONObject("player_assignments")
            ?: return

        val playerNames = gameState.optJSONObject("player_names")
        val myToken = sessionManager.getOrCreateUserToken()

        myColor = null

        val assignmentKeys = assignments.keys()
        while (assignmentKeys.hasNext()) {
            val token = assignmentKeys.next()
            val color = assignments.optString(token, "")
            if (token == myToken) {
                myColor = color
                myPlayerName = playerNames?.optString(token)
                    ?.takeIf { it.isNotBlank() }
                    ?: deviceNameFromToken(token)
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
            val playerName = playerNames?.optString(token)
                ?.takeIf { it.isNotBlank() }
                ?: deviceNameFromToken(token)

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

        if (token.startsWith("sdk_gphone", ignoreCase = true)) {
            return "Android Emulator"
        }

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

        if (turnOrder != null &&
            turnOrder.length() > 0 &&
            turnIndex >= 0 &&
            turnIndex < turnOrder.length()
        ) {
            currentTurnColorLocal = turnOrder.optString(turnIndex, "")
        }

        // Detect turn change (used to restart timer)
        val turnChanged = currentTurnColorLocal != currentTurnColor
        currentTurnColor = currentTurnColorLocal

        if (currentLobbyStatus != "LOBBY") {
            val friendlyStatus = createFriendlyStatus(gameState, currentTurnColorLocal)
            tvStatus.text = friendlyStatus
        }

        highlightCurrentTurn(currentTurnColorLocal)

        val diceDrawables = listOf(
            R.drawable.dice_1, R.drawable.dice_2,
            R.drawable.dice_3, R.drawable.dice_4,
            R.drawable.dice_5, R.drawable.dice_6
        )

        imgDice.setImageResource(
            diceDrawables[(currentDiceValue - 1).coerceIn(0, 5)]
        )

        // =====================================================
        // COMPUTE VALID TOKENS
        // =====================================================

        val validKeys = mutableSetOf<String>()

        if (
            clientHasRolledLock &&
            !myColor.isNullOrBlank() &&
            currentTurnColorLocal.equals(myColor, ignoreCase = true)
        ) {
            val tokensArray = gameState.optJSONArray("tokens")

            if (tokensArray != null) {

                val roll = currentDiceValue

                for (i in 0 until tokensArray.length()) {

                    val t = tokensArray.getJSONObject(i)
                    val id = t.getInt("id")
                    val colorStr = t.getString("color")
                    val pos = t.getInt("position")

                    if (!colorStr.equals(myColor, ignoreCase = true)) {
                        continue
                    }

                    var isValid = false

                    if (pos == -1 && roll == 6) {
                        isValid = true
                    } else if (pos >= 0 && pos + roll <= 56) {
                        isValid = true
                    }

                    if (isValid) {
                        validKeys.add("${colorStr}_${id}")
                    }
                }
            }
        }

        boardView.validTokenKeys = validKeys

        // =====================================================
        // TURN SKIP TIMER LOGIC
        //
        // - Nayi turn meri → fresh 40s timer
        // - Turn kisi aur ka → mera timer band
        // =====================================================

        if (currentLobbyStatus == "ACTIVE" && !gameFinished) {

            val isMyTurn = !myColor.isNullOrBlank() &&
                    currentTurnColorLocal.equals(myColor, ignoreCase = true)

            if (isMyTurn && turnChanged) {
                // Nayi turn — fresh timer
                startTurnTimer(currentTurnColorLocal, tvStatus)
            } else if (!isMyTurn) {
                // Turn kisi aur ka hai — apna timer band karo
                stopTurnTimer()
            }
        }

        // =====================================================
        // UPDATE BOARD
        // =====================================================

        val tokensArray = gameState.optJSONArray("tokens")

        if (tokensArray != null) {
            boardView.updateBoardStateFromServer(tokensArray)
        }
    }

    // =========================================================
    // FRIENDLY STATUS
    // =========================================================

    private fun createFriendlyStatus(
        gameState: JSONObject,
        currentTurnColor: String
    ): String {

        val playerAssignments = gameState.optJSONObject("player_assignments")
        val playerCount = playerAssignments?.length() ?: 0

        if (playerCount < 2) return "Waiting for opponent..."

        if (gameState.optString("game_status") == "COMPLETED") {
            return "Game completed"
        }

        if (!myColor.isNullOrBlank() &&
            currentTurnColor.equals(myColor, ignoreCase = true)
        ) {
            return if (clientHasRolledLock) {
                "Your turn • Select a token"
            } else {
                "Your Turn! Tap the dice."
            }
        }

        val playerNames = gameState.optJSONObject("player_names")
        val opponentName = getPlayerNameForColor(
            playerAssignments, playerNames, currentTurnColor
        )

        return if (opponentName.isNotBlank()) {
            "$opponentName's Turn"
        } else {
            "$currentTurnColor's Turn"
        }
    }

    private fun getPlayerNameForColor(
        assignments: JSONObject?,
        playerNames: JSONObject?,
        color: String
    ): String {

        if (assignments == null) return ""

        val keys = assignments.keys()
        while (keys.hasNext()) {
            val token = keys.next()
            val assignedColor = assignments.optString(token, "")
            if (assignedColor.equals(color, ignoreCase = true)) {
                return playerNames?.optString(token)
                    ?.takeIf { it.isNotBlank() }
                    ?: deviceNameFromToken(token)
            }
        }

        return ""
    }

    // =========================================================
    // WINNER SCREEN
    // =========================================================

    private fun showWinnerScreen(didWin: Boolean, payout: Int) {

        if (didWin) SoundManager.playWin()
        else SoundManager.playLose()

        setContent {
            MaterialTheme {
                WinnerScreen(
                    didWin = didWin,
                    payout = payout,
                    onContinue = { finish() }
                )
            }
        }
    }

    private fun showSurrenderScreen() {

        SoundManager.playLose()

        setContent {
            MaterialTheme {
                WinnerScreen(
                    didWin = false,
                    payout = 0,
                    onContinue = { finish() }
                )
            }
        }
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
    }
}