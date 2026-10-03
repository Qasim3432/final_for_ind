package com.example.final_for_ind

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import org.json.JSONObject

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
    // TURN COUNTDOWN STATE
    // =========================================================

    private var turnCountdownJob: Job? = null

    private var currentTurnDeadline: Double = 0.0

    private var currentTurnColor: String = ""

    // =========================================================
    // PROFILE VIEWS FOR TURN HIGHLIGHT
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

        // =====================================================
        // FIND VIEWS
        // =====================================================

        val ludoBoardView =
            findViewById<LudoBoardView>(
                R.id.ludoBoardView
            )

        val imgDice =
            findViewById<ImageView>(
                R.id.imgDice
            )

        val tvStatus =
            findViewById<TextView>(
                R.id.tvStatus
            )

        tvStatusRef = tvStatus

        val btnTestFinishBlue =
            findViewById<Button>(
                R.id.btnTestFinishBlue
            )

        btnTestFinishBlue.visibility =
            View.GONE

        profileBlue =
            findViewById<View>(
                R.id.profileBlue
            )

        profileRed =
            findViewById<View>(
                R.id.profileRed
            )

        profileGreen =
            findViewById<View>(
                R.id.profileGreen
            )

        profileYellow =
            findViewById<View>(
                R.id.profileYellow
            )

        val txtNameBlue =
            findViewById<TextView>(
                R.id.txtNameBlue
            )

        val txtNameRed =
            findViewById<TextView>(
                R.id.txtNameRed
            )

        val txtNameGreen =
            findViewById<TextView>(
                R.id.txtNameGreen
            )

        val txtNameYellow =
            findViewById<TextView>(
                R.id.txtNameYellow
            )

        sessionManager =
            GameSessionManager(this)

        isTwoPlayerMode =
            intent.getBooleanExtra(
                "GAME_MODE_2P",
                true
            )

        ludoBoardView.isTwoPlayerMode =
            isTwoPlayerMode

        if (isTwoPlayerMode) {

            profileBlue.visibility =
                View.VISIBLE

            profileGreen.visibility =
                View.VISIBLE

            profileRed.visibility =
                View.GONE

            profileYellow.visibility =
                View.GONE

        } else {

            profileBlue.visibility =
                View.VISIBLE

            profileRed.visibility =
                View.VISIBLE

            profileGreen.visibility =
                View.VISIBLE

            profileYellow.visibility =
                View.VISIBLE

        }

        tvStatus.text =
            "Connecting to matchmaking..."

        // =====================================================
        // GET DEVICE TOKEN + JOIN MATCH
        // =====================================================

        lifecycleScope.launch {

            val deviceToken =
                sessionManager.getOrCreateUserToken()

            var gameId =
                intent.getStringExtra(
                    "GAME_ID"
                ) ?: ""

            if (gameId.isBlank()) {

                gameId =
                    sessionManager.registerAndJoinMatch(
                        isTwoPlayerMode
                    )

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
                "Matchmaking Success: " +
                        "Game=$gameId " +
                        "Token=$deviceToken"
            )

            gameSocket =
                GameSocket(
                    gameId,
                    deviceToken,
                    onConnectionChange = { connected ->

                        runOnUiThread {

                            if (
                                gameFinished ||
                                surrenderedByUser
                            ) {
                                return@runOnUiThread
                            }

                            if (!connected) {

                                tvStatus.text =
                                    "Reconnecting..."

                            } else {

                                tvStatus.text =
                                    "Connected"

                            }
                        }
                    }
                ) { rootJson ->

                    runOnUiThread {

                        Log.d(
                            "LUDO_WEBSOCKET",
                            "Incoming payload: $rootJson"
                        )

                        try {

                            if (
                                rootJson.optString(
                                    "status"
                                ) != "success"
                            ) {
                                return@runOnUiThread
                            }

                            val gameState =
                                rootJson.getJSONObject(
                                    "game_state"
                                )

                            isAnimating = false

                            updatePlayersFromServer(
                                gameState,
                                txtNameBlue,
                                txtNameRed,
                                txtNameGreen,
                                txtNameYellow
                            )

                            val gameStatus =
                                gameState.optString(
                                    "game_status",
                                    ""
                                )

                            currentLobbyStatus = gameStatus

                            if (gameStatus == "LOBBY") {

                                startMatchmakingTimer()

                                // Cancel turn countdown while in lobby
                                turnCountdownJob?.cancel()

                            } else if (
                                gameStatus == "ACTIVE" &&
                                matchmakingTimerJob != null
                            ) {

                                matchmakingTimerJob?.cancel()

                                matchmakingTimerJob = null

                                Log.d(
                                    "MATCHMAKING",
                                    "Game started — timer cancelled"
                                )
                            }

                            if (
                                gameStatus == "COMPLETED"
                                || gameStatus == "CANCELLED"
                            ) {

                                gameFinished = true

                                turnCountdownJob?.cancel()

                                if (surrenderedByUser) {

                                    return@runOnUiThread
                                }

                                val winnerToken =
                                    gameState.optString(
                                        "winner_device_token",
                                        ""
                                    )

                                val myToken =
                                    sessionManager
                                        .getOrCreateUserToken()

                                val didWin =
                                    winnerToken == myToken

                                val payout =
                                    gameState.optInt(
                                        "winner_payout",
                                        0
                                    )

                                showWinnerScreen(
                                    didWin = didWin,
                                    payout = payout
                                )

                                return@runOnUiThread

                            }

                            parseAndSyncFullGameState(
                                gameState,
                                ludoBoardView,
                                imgDice,
                                tvStatus
                            )

                        } catch (e: Exception) {

                            Log.e(
                                "LUDO_WEBSOCKET",
                                "Parsing crash",
                                e
                            )

                        }

                    }

                }

            gameSocket?.connect()

        }

        // =====================================================
        // DICE DRAWABLE HELPER
        // =====================================================

        fun getDiceDrawableId(
            value: Int
        ): Int {

            return when (value) {

                1 -> R.drawable.dice_1

                2 -> R.drawable.dice_2

                3 -> R.drawable.dice_3

                4 -> R.drawable.dice_4

                5 -> R.drawable.dice_5

                else -> R.drawable.dice_6

            }

        }

        // =====================================================
        // DICE CLICK
        // =====================================================

        imgDice.setOnClickListener {

            if (
                isAnimating ||
                clientHasRolledLock ||
                gameFinished ||
                surrenderedByUser ||
                currentLobbyStatus == "LOBBY"
            ) {
                return@setOnClickListener
            }

            SoundManager.playDiceRoll()

            isAnimating = true

            tvStatus.text =
                "Rolling..."

            lifecycleScope.launch {

                for (i in 1..5) {

                    imgDice.setImageResource(
                        getDiceDrawableId(
                            Random.nextInt(1, 7)
                        )
                    )

                    imgDice.rotation =
                        i * 60f

                    delay(30)

                }

                gameSocket?.rollDice()

                delay(1500)

                if (isAnimating) {

                    isAnimating = false

                    tvStatus.text =
                        "Connection slow. Try again."

                }

            }

        }

        // =====================================================
        // TOKEN CLICK
        // =====================================================

        ludoBoardView.onTokenClickListener =
            { clickedToken ->

                if (
                    !isAnimating &&
                    clientHasRolledLock &&
                    !gameFinished &&
                    !surrenderedByUser &&
                    currentLobbyStatus != "LOBBY"
                ) {

                    SoundManager.playTokenMove()

                    gameSocket?.moveToken(
                        clickedToken.id,
                        clickedToken.color.name
                    )

                }

            }

    }

    // =========================================================
    // MATCHMAKING TIMER
    // =========================================================

    private fun startMatchmakingTimer() {

        if (matchmakingTimerJob != null) {
            return
        }

        if (cancelDialogShown) {
            return
        }

        matchmakingTimerJob = lifecycleScope.launch {

            Log.d(
                "MATCHMAKING",
                "Timer started — waiting for opponent"
            )

            var elapsedSeconds = 0

            while (
                currentLobbyStatus == "LOBBY" &&
                !gameFinished &&
                !surrenderedByUser
            ) {

                delay(1000L)

                elapsedSeconds++

                val remainingForDialog =
                    60 - elapsedSeconds

                if (
                    remainingForDialog > 0 &&
                    tvStatusRef != null &&
                    currentLobbyStatus == "LOBBY"
                ) {

                    tvStatusRef?.text =
                        "Finding opponent... " +
                                "${remainingForDialog}s"
                }

                if (
                    elapsedSeconds == 60 &&
                    !cancelDialogShown &&
                    currentLobbyStatus == "LOBBY"
                ) {

                    cancelDialogShown = true

                    matchmakingTimerJob = null

                    showCancelDialog()

                    return@launch
                }

                if (elapsedSeconds >= 300) {

                    Log.d(
                        "MATCHMAKING",
                        "Auto-cancel after 5 minutes"
                    )

                    matchmakingTimerJob = null

                    autoCancelMatch()

                    return@launch
                }

            }

            matchmakingTimerJob = null

        }

    }

    // =========================================================
    // TURN COUNTDOWN
    // =========================================================

    private fun startTurnCountdown() {

        turnCountdownJob?.cancel()

        turnCountdownJob = lifecycleScope.launch {

            while (isActive) {

                val now =
                    System.currentTimeMillis() / 1000.0

                val remaining =
                    currentTurnDeadline - now

                if (remaining <= 0) {

                    runOnUiThread {

                        if (
                            currentLobbyStatus == "ACTIVE" &&
                            !gameFinished &&
                            !surrenderedByUser
                        ) {

                            tvStatusRef?.text =
                                "⏰ Time out!"
                        }
                    }

                    break
                }

                val secs = remaining.toInt()

                val isMyTurn =
                    !myColor.isNullOrBlank() &&
                            currentTurnColor.equals(
                                myColor,
                                ignoreCase = true
                            )

                runOnUiThread {

                    if (
                        currentLobbyStatus == "ACTIVE" &&
                        !gameFinished &&
                        !surrenderedByUser
                    ) {

                        tvStatusRef?.text =
                            if (isMyTurn) {

                                "⏳ Your Turn! ${secs}s"

                            } else {

                                "⏳ " +
                                        "${currentTurnColor}'s " +
                                        "Turn - ${secs}s"
                            }
                    }
                }

                delay(1000L)
            }
        }
    }

    // =========================================================
    // SHOW CANCEL DIALOG
    // =========================================================

    private fun showCancelDialog() {

        if (isFinishing || isDestroyed) {
            return
        }

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

                Log.d(
                    "MATCHMAKING",
                    "User chose to keep waiting"
                )

                startMatchmakingTimer()

            }
            .setNegativeButton("Cancel & Refund") { dialog, _ ->

                dialog.dismiss()

                Log.d(
                    "MATCHMAKING",
                    "User cancelled — refunding"
                )

                performCancel()

            }
            .show()

    }

    // =========================================================
    // AUTO CANCEL
    // =========================================================

    private fun autoCancelMatch() {

        Toast.makeText(
            this,
            "No opponent found. Cancelling...",
            Toast.LENGTH_LONG
        ).show()

        performCancel()

    }

    // =========================================================
    // PERFORM CANCEL
    // =========================================================

    private fun performCancel() {

        if (surrenderedByUser) {
            return
        }

        surrenderedByUser = true

        turnCountdownJob?.cancel()

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
    // HIGHLIGHT CURRENT TURN
    // =========================================================

    private fun highlightCurrentTurn(
        currentTurnColor: String
    ) {

        val map =
            mapOf(
                "BLUE" to profileBlue,
                "RED" to profileRed,
                "GREEN" to profileGreen,
                "YELLOW" to profileYellow
            )

        for (
        (color, view) in map
        ) {

            if (
                view.visibility != View.VISIBLE
            ) {
                continue
            }

            val isActive =
                color.equals(
                    currentTurnColor,
                    ignoreCase = true
                )

            if (isActive) {

                view.animate()
                    .scaleX(1.12f)
                    .scaleY(1.12f)
                    .setDuration(300)
                    .start()

                val drawable =
                    GradientDrawable()

                drawable.shape =
                    GradientDrawable.RECTANGLE

                drawable.cornerRadius = 24f

                drawable.setColor(
                    Color.parseColor("#0A3D3D")
                )

                drawable.setStroke(
                    6,
                    Color.parseColor("#FFD700")
                )

                view.background =
                    drawable

            } else {

                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()

                val drawable =
                    GradientDrawable()

                drawable.shape =
                    GradientDrawable.RECTANGLE

                drawable.cornerRadius = 24f

                drawable.setColor(
                    Color.parseColor("#0A3D3D")
                )

                drawable.setStroke(
                    2,
                    Color.parseColor("#555555")
                )

                drawable.alpha = 180

                view.background =
                    drawable

            }

        }

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

        val assignments =
            gameState.optJSONObject(
                "player_assignments"
            )

        if (assignments == null) {
            return
        }

        val playerNames =
            gameState.optJSONObject(
                "player_names"
            )

        val myToken =
            sessionManager.getOrCreateUserToken()

        myColor = null

        val assignmentKeys =
            assignments.keys()

        while (assignmentKeys.hasNext()) {

            val token =
                assignmentKeys.next()

            val color =
                assignments.optString(
                    token,
                    ""
                )

            if (token == myToken) {

                myColor =
                    color

                myPlayerName =
                    playerNames?.optString(
                        token
                    )?.takeIf {
                        it.isNotBlank()
                    } ?: deviceNameFromToken(
                        token
                    )

                break

            }

        }

        txtNameBlue.text = "Blue"

        txtNameRed.text = "Red"

        txtNameGreen.text = "Green"

        txtNameYellow.text = "Yellow"

        val keys =
            assignments.keys()

        while (keys.hasNext()) {

            val token =
                keys.next()

            val color =
                assignments.optString(
                    token,
                    ""
                )

            val playerName =
                playerNames?.optString(
                    token
                )?.takeIf {
                    it.isNotBlank()
                } ?: deviceNameFromToken(
                    token
                )

            when (color) {

                "BLUE" -> {
                    txtNameBlue.text = playerName
                }

                "RED" -> {
                    txtNameRed.text = playerName
                }

                "GREEN" -> {
                    txtNameGreen.text = playerName
                }

                "YELLOW" -> {
                    txtNameYellow.text = playerName
                }

            }

        }

    }

    // =========================================================
    // GET DEVICE NAME
    // =========================================================

    private fun deviceNameFromToken(
        token: String
    ): String {

        if (token.isBlank()) {
            return "Player"
        }

        val parts =
            token.split("_")

        if (parts.isEmpty()) {
            return "Player"
        }

        if (
            token.startsWith(
                "sdk_gphone",
                ignoreCase = true
            )
        ) {
            return "Android Emulator"
        }

        if (
            token.startsWith(
                "Infinix",
                ignoreCase = true
            )
        ) {

            return parts
                .take(2)
                .joinToString(" ")

        }

        return parts
            .take(2)
            .joinToString(" ")
            .ifBlank {
                "Player"
            }

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

        currentDiceValue =
            gameState.optInt(
                "current_dice_value",
                1
            )

        clientHasRolledLock =
            gameState.optBoolean(
                "has_rolled",
                false
            )

        val turnOrder =
            gameState.optJSONArray(
                "player_turn_order"
            )

        val turnIndex =
            gameState.optInt(
                "turn_index",
                0
            )

        var currentTurnColorLocal =
            ""

        if (
            turnOrder != null &&
            turnOrder.length() > 0 &&
            turnIndex >= 0 &&
            turnIndex < turnOrder.length()
        ) {

            currentTurnColorLocal =
                turnOrder.optString(
                    turnIndex,
                    ""
                )

        }

        currentTurnColor = currentTurnColorLocal

        // Don't overwrite status while in LOBBY
        if (currentLobbyStatus != "LOBBY") {

            val friendlyStatus =
                createFriendlyStatus(
                    gameState,
                    currentTurnColorLocal
                )

            tvStatus.text =
                friendlyStatus
        }

        highlightCurrentTurn(
            currentTurnColorLocal
        )

        val diceDrawables =
            listOf(
                R.drawable.dice_1,
                R.drawable.dice_2,
                R.drawable.dice_3,
                R.drawable.dice_4,
                R.drawable.dice_5,
                R.drawable.dice_6
            )

        imgDice.setImageResource(
            diceDrawables[
                (currentDiceValue - 1)
                    .coerceIn(0, 5)
            ]
        )

        imgDice.rotation =
            0f

        val tokensArray =
            gameState.optJSONArray(
                "tokens"
            )

        if (tokensArray != null) {

            boardView.updateBoardStateFromServer(
                tokensArray
            )

        }

        // =====================================================
        // TURN COUNTDOWN
        // =====================================================

        val deadline =
            gameState.optDouble(
                "turn_deadline",
                0.0
            )

        if (
            deadline > 0 &&
            currentLobbyStatus == "ACTIVE"
        ) {

            currentTurnDeadline = deadline

            startTurnCountdown()

        } else {

            turnCountdownJob?.cancel()

        }

    }

    // =========================================================
    // FRIENDLY STATUS
    // =========================================================

    private fun createFriendlyStatus(
        gameState: JSONObject,
        currentTurnColor: String
    ): String {

        val playerAssignments =
            gameState.optJSONObject(
                "player_assignments"
            )

        val playerCount =
            playerAssignments?.length()
                ?: 0

        if (playerCount < 2) {
            return "Waiting for opponent..."
        }

        if (
            gameState.optString(
                "game_status"
            ) == "COMPLETED"
        ) {
            return "Game completed"
        }

        if (
            !myColor.isNullOrBlank() &&
            currentTurnColor.equals(
                myColor,
                ignoreCase = true
            )
        ) {

            return if (clientHasRolledLock) {
                "Your turn • Select a token"
            } else {
                "Your Turn! Tap the dice."
            }

        }

        val playerNames =
            gameState.optJSONObject(
                "player_names"
            )

        val opponentName =
            getPlayerNameForColor(
                playerAssignments,
                playerNames,
                currentTurnColor
            )

        return if (opponentName.isNotBlank()) {
            "$opponentName's Turn"
        } else {
            "$currentTurnColor's Turn"
        }

    }

    // =========================================================
    // FIND PLAYER NAME BY COLOR
    // =========================================================

    private fun getPlayerNameForColor(
        assignments: JSONObject?,
        playerNames: JSONObject?,
        color: String
    ): String {

        if (assignments == null) {
            return ""
        }

        val keys =
            assignments.keys()

        while (keys.hasNext()) {

            val token =
                keys.next()

            val assignedColor =
                assignments.optString(
                    token,
                    ""
                )

            if (
                assignedColor.equals(
                    color,
                    ignoreCase = true
                )
            ) {

                return playerNames?.optString(
                    token
                )?.takeIf {
                    it.isNotBlank()
                } ?: deviceNameFromToken(
                    token
                )

            }

        }

        return ""

    }

    // =========================================================
    // WINNER SCREEN
    // =========================================================

    private fun showWinnerScreen(
        didWin: Boolean,
        payout: Int
    ) {

        if (didWin) {
            SoundManager.playWin()
        } else {
            SoundManager.playLose()
        }

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

    // =========================================================
    // SURRENDER SCREEN
    // =========================================================

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

        turnCountdownJob?.cancel()

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

        turnCountdownJob?.cancel()

        turnCountdownJob = null

        gameSocket?.disconnect()

    }

}