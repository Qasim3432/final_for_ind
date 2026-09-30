package com.example.final_for_ind.network

import android.os.Handler
import android.os.Looper
import android.util.Log

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

import org.json.JSONObject

import java.util.concurrent.TimeUnit


class GameSocket(
    private val gameId: String,
    private val playerToken: String,
    private val onConnectionChange: ((Boolean) -> Unit)? = null,
    private val onMessage: (JSONObject) -> Unit
) {

    private val client =
        OkHttpClient.Builder()
            .pingInterval(
                20,
                TimeUnit.SECONDS
            )
            .build()

    private var socket: WebSocket? = null

    private var isConnected = false

    // ---------------------------------------------
    // RECONNECT STATE
    // ---------------------------------------------

    private var manuallyClosed = false

    private var reconnectAttempts = 0

    private val maxReconnectAttempts = 5

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val reconnectRunnable =
        Runnable {

            if (!manuallyClosed) {

                connect()
            }
        }


    // =========================================================
    // CONNECT
    // =========================================================

    fun connect() {

        if (manuallyClosed) {

            return
        }

        Log.d(
            "GAME_SOCKET",
            "Connecting WebSocket. " +
                    "Game=$gameId " +
                    "Token=$playerToken"
        )

        val url =
            "ws://192.168.18.48:8090/ws/ludo/$gameId/?player_token=$playerToken"

        Log.d(
            "GAME_SOCKET",
            "WebSocket URL=$url"
        )

        val request =
            Request.Builder()
                .url(url)
                .build()


        socket =
            client.newWebSocket(
                request,
                object : WebSocketListener() {

                    override fun onOpen(
                        webSocket: WebSocket,
                        response: Response
                    ) {

                        isConnected = true

                        reconnectAttempts = 0

                        Log.d(
                            "GAME_SOCKET",
                            "WebSocket CONNECTED. " +
                                    "Game=$gameId " +
                                    "Token=$playerToken"
                        )

                        onConnectionChange?.invoke(true)
                    }


                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String
                    ) {

                        Log.d(
                            "GAME_SOCKET",
                            "MESSAGE: $text"
                        )

                        try {

                            val rootJson =
                                JSONObject(text)

                            onMessage(rootJson)

                        } catch (e: Exception) {

                            Log.e(
                                "GAME_SOCKET",
                                "Invalid JSON received: $text",
                                e
                            )
                        }
                    }


                    override fun onClosing(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {

                        Log.d(
                            "GAME_SOCKET",
                            "WebSocket closing. " +
                                    "code=$code reason=$reason"
                        )

                        isConnected = false

                        webSocket.close(
                            code,
                            reason
                        )
                    }


                    override fun onClosed(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {

                        isConnected = false

                        Log.d(
                            "GAME_SOCKET",
                            "WebSocket CLOSED. " +
                                    "code=$code reason=$reason"
                        )

                        socket = null

                        onConnectionChange?.invoke(false)

                        scheduleReconnect()
                    }


                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {

                        isConnected = false

                        Log.e(
                            "GAME_SOCKET",
                            "WebSocket FAILURE. " +
                                    "Game=$gameId " +
                                    "Token=$playerToken " +
                                    "HTTP=${response?.code} " +
                                    "Message=${t.message}",
                            t
                        )

                        socket = null

                        onConnectionChange?.invoke(false)

                        scheduleReconnect()
                    }
                }
            )
    }


    // =========================================================
    // RECONNECT WITH BACKOFF
    // =========================================================

    private fun scheduleReconnect() {

        if (manuallyClosed) {

            return
        }

        if (reconnectAttempts >= maxReconnectAttempts) {

            Log.w(
                "GAME_SOCKET",
                "Max reconnect attempts reached " +
                        "for Game=$gameId"
            )

            return
        }

        reconnectAttempts++

        val delayMs =
            (
                    1000L
                            * (1L shl (reconnectAttempts - 1))
                    ).coerceAtMost(16000L)

        Log.d(
            "GAME_SOCKET",
            "Scheduling reconnect in " +
                    "${delayMs}ms " +
                    "(attempt $reconnectAttempts/" +
                    "$maxReconnectAttempts)"
        )

        mainHandler.postDelayed(
            reconnectRunnable,
            delayMs
        )
    }


    // =========================================================
    // ROLL DICE
    // =========================================================

    fun rollDice() {

        val payload =
            JSONObject().apply {

                put(
                    "action",
                    "roll_dice"
                )
            }

        Log.d(
            "GAME_SOCKET",
            "Sending roll_dice: $payload"
        )

        socket?.send(
            payload.toString()
        )
    }


    // =========================================================
    // MOVE TOKEN
    // =========================================================

    fun moveToken(
        tokenId: Int,
        color: String
    ) {

        val payload =
            JSONObject().apply {

                put(
                    "action",
                    "move_token"
                )

                put(
                    "token_id",
                    tokenId
                )

                put(
                    "color",
                    color
                )
            }

        Log.d(
            "GAME_SOCKET",
            "Sending move_token: $payload"
        )

        socket?.send(
            payload.toString()
        )
    }


    // =========================================================
    // SURRENDER
    // =========================================================

    fun surrender() {

        val payload =
            JSONObject().apply {

                put(
                    "action",
                    "surrender"
                )
            }

        Log.d(
            "GAME_SOCKET",
            "Sending surrender: $payload"
        )

        socket?.send(
            payload.toString()
        )
    }


    // =========================================================
    // STOP RECONNECT
    //
    // Call this before disconnect when the user explicitly
    // quits (back button, surrender). Prevents auto-reconnect
    // from kicking in during the shutdown sequence.
    // =========================================================

    fun stopReconnect() {

        manuallyClosed = true

        mainHandler.removeCallbacks(
            reconnectRunnable
        )
    }


    // =========================================================
    // DISCONNECT
    // =========================================================

    fun disconnect() {

        manuallyClosed = true

        mainHandler.removeCallbacks(
            reconnectRunnable
        )

        Log.d(
            "GAME_SOCKET",
            "Disconnecting WebSocket. Game=$gameId"
        )

        isConnected = false

        socket?.close(
            1000,
            "Client closed"
        )

        socket = null
    }


    // =========================================================
    // CONNECTION STATE
    // =========================================================

    fun isConnected(): Boolean {

        return isConnected
    }
}