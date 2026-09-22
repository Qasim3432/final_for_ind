package com.example.final_for_ind.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class GameSocket(
    private val gameId: String,
    private val playerToken: String,
    private val onMessage: (JSONObject) -> Unit
) {

    private val client = OkHttpClient()

    private var socket: WebSocket? = null

    private var isConnected = false


    // =========================================================
    // CONNECT
    // =========================================================

    fun connect() {

        Log.d(
            "GAME_SOCKET",
            "Connecting WebSocket. Game=$gameId Token=$playerToken"
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

                        Log.d(
                            "GAME_SOCKET",
                            "WebSocket CONNECTED. " +
                                    "Game=$gameId Token=$playerToken"
                        )
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

                            // IMPORTANT:
                            // Use the callback supplied by MainActivity.
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
                    }
                }
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
    // TEST FINISH BLUE
    // =========================================================

    fun testFinishBlue() {

        val payload =
            JSONObject().apply {

                put(
                    "action",
                    "test_finish_blue"
                )
            }

        Log.d(
            "GAME_SOCKET",
            "Sending test_finish_blue: $payload"
        )

        socket?.send(
            payload.toString()
        )
    }


    // =========================================================
    // DISCONNECT
    // =========================================================

    fun disconnect() {

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