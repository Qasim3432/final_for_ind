package com.example.final_for_ind

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
import kotlinx.coroutines.delay
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

        val btnTestFinishBlue =
            findViewById<Button>(
                R.id.btnTestFinishBlue
            )

        // =====================================================
        // PROFILE VIEWS
        // =====================================================

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

        // =====================================================
        // SESSION
        // =====================================================

        sessionManager =
            GameSessionManager(this)

        // =====================================================
        // GAME MODE
        // =====================================================

        isTwoPlayerMode =
            intent.getBooleanExtra(
                "GAME_MODE_2P",
                true
            )

        ludoBoardView.isTwoPlayerMode =
            isTwoPlayerMode

        // =====================================================
        // IMPORTANT
        //
        // 2 PLAYER:
        //
        // BLUE + GREEN ONLY
        //
        // 4 PLAYER:
        //
        // BLUE + RED + GREEN + YELLOW
        // =====================================================

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

            // =================================================
            // CONNECT WEBSOCKET
            // =================================================

            gameSocket =
                GameSocket(
                    gameId,
                    deviceToken
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

                            // =================================================
                            // UPDATE PLAYER NAMES / COLORS
                            // =================================================

                            updatePlayersFromServer(
                                gameState,
                                txtNameBlue,
                                txtNameRed,
                                txtNameGreen,
                                txtNameYellow
                            )

                            // =================================================
                            // GAME STATUS
                            // =================================================

                            val gameStatus =
                                gameState.optString(
                                    "game_status",
                                    ""
                                )

                            // =================================================
                            // GAME COMPLETED
                            // =================================================

                            if (
                                gameStatus == "COMPLETED"

                            ) {

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

                                Log.d(
                                    "LUDO_RESULT",
                                    "Game completed. " +
                                            "Winner=$winnerToken " +
                                            "Me=$myToken " +
                                            "DidWin=$didWin " +
                                            "Payout=$payout"
                                )

                                showWinnerScreen(
                                    didWin = didWin,
                                    payout = payout
                                )

                                return@runOnUiThread

                            }

                            // =================================================
                            // NORMAL GAME STATE
                            // =================================================

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
        // DICE DRAWABLE
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
        // TEST BLUE FINISH
        // =====================================================

        btnTestFinishBlue.setOnClickListener {

            Log.d(
                "LUDO_TEST",
                "TEST FINISH BLUE pressed"
            )

            Toast.makeText(
                this,
                "Finishing BLUE for test...",
                Toast.LENGTH_SHORT
            ).show()

            gameSocket?.testFinishBlue()

        }

        // =====================================================
        // DICE CLICK
        // =====================================================

        imgDice.setOnClickListener {

            Log.d(
                "LUDO_UI",
                "Dice clicked. " +
                        "Animating=$isAnimating " +
                        "Rolled=$clientHasRolledLock " +
                        "MyColor=$myColor"
            )

            if (
                isAnimating ||
                clientHasRolledLock
            ) {
                return@setOnClickListener
            }

            isAnimating = true

            tvStatus.text =
                "Rolling..."

            lifecycleScope.launch {

                // =============================================
                // DICE ANIMATION
                // =============================================

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

                // =============================================
                // SEND ROLL
                // =============================================

                Log.d(
                    "LUDO_UI",
                    "Sending roll_dice"
                )

                gameSocket?.rollDice()

                // =============================================
                // SAFETY TIMEOUT
                // =============================================

                delay(1500)

                if (isAnimating) {

                    Log.w(
                        "LUDO_UI",
                        "Server response timeout"
                    )

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

                Log.d(
                    "LUDO_UI",
                    "Token clicked: " +
                            "${clickedToken.color.name} " +
                            "ID=${clickedToken.id} " +
                            "MyColor=$myColor " +
                            "HasRolled=$clientHasRolledLock"
                )

                if (
                    !isAnimating &&
                    clientHasRolledLock
                ) {

                    gameSocket?.moveToken(
                        clickedToken.id,
                        clickedToken.color.name
                    )

                }

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

            Log.w(
                "LUDO_PLAYERS",
                "player_assignments missing"
            )

            return

        }

        val playerNames =
            gameState.optJSONObject(
                "player_names"
            )

        val myToken =
            sessionManager.getOrCreateUserToken()

        // =====================================================
        // FIND MY COLOR
        // =====================================================

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

                Log.d(
                    "LUDO_PLAYERS",
                    "I am $color " +
                            "($myPlayerName)"
                )

                break

            }

        }

        // =====================================================
        // DEFAULT NAMES
        // =====================================================

        txtNameBlue.text =
            "Blue"

        txtNameRed.text =
            "Red"

        txtNameGreen.text =
            "Green"

        txtNameYellow.text =
            "Yellow"

        // =====================================================
        // ASSIGN REAL PLAYER NAMES FROM SERVER
        // =====================================================

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

                    txtNameBlue.text =
                        playerName

                }

                "RED" -> {

                    txtNameRed.text =
                        playerName

                }

                "GREEN" -> {

                    txtNameGreen.text =
                        playerName

                }

                "YELLOW" -> {

                    txtNameYellow.text =
                        playerName

                }

            }

        }

        Log.d(
            "LUDO_PLAYERS",
            "Assignments=$assignments Names=$playerNames"
        )

    }

    // =========================================================
    // GET DEVICE NAME (FALLBACK ONLY)
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

        // =====================================================
        // SERVER TURN
        // =====================================================

        val turnOrder =
            gameState.optJSONArray(
                "player_turn_order"
            )

        val turnIndex =
            gameState.optInt(
                "turn_index",
                0
            )

        var currentTurnColor =
            ""

        if (
            turnOrder != null &&
            turnOrder.length() > 0 &&
            turnIndex >= 0 &&
            turnIndex < turnOrder.length()
        ) {

            currentTurnColor =
                turnOrder.optString(
                    turnIndex,
                    ""
                )

        }

        // =====================================================
        // SHOW HUMAN-FRIENDLY STATUS
        // =====================================================

        val friendlyStatus =
            createFriendlyStatus(
                gameState,
                currentTurnColor
            )

        tvStatus.text =
            friendlyStatus

        // =====================================================
        // HIGHLIGHT TURN PROFILE
        // =====================================================

        highlightCurrentTurn(
            currentTurnColor
        )

        // =====================================================
        // DICE
        // =====================================================

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

        // =====================================================
        // BOARD
        // =====================================================

        val tokensArray =
            gameState.optJSONArray(
                "tokens"
            )

        if (tokensArray != null) {

            boardView.updateBoardStateFromServer(
                tokensArray
            )

        }

        Log.d(
            "LUDO_STATE",
            "Sync complete. " +
                    "MyColor=$myColor " +
                    "Turn=$currentTurnColor " +
                    "HasRolled=$clientHasRolledLock"
        )

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

        if (
            playerCount < 2
        ) {

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

        return if (
            opponentName.isNotBlank()
        ) {

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

        setContent {

            MaterialTheme {

                WinnerScreen(

                    didWin = didWin,

                    payout = payout,

                    onContinue = {

                        finish()

                    }

                )

            }

        }

    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        super.onDestroy()

        gameSocket?.disconnect()

    }

}