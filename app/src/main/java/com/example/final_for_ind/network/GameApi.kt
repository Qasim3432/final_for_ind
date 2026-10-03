package com.example.final_for_ind.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object GameApi {

    private const val BASE_URL =
        "http://192.168.18.55:8090/"

    private val client =
        OkHttpClient()

    private val JSON_MEDIA_TYPE =
        "application/json; charset=utf-8".toMediaType()


    // =========================================================
    // BASE POST HELPER
    // =========================================================

    private suspend fun postSecureNetworkCall(
        endpoint: String,
        jsonBodyStr: String = ""
    ): JSONObject? = withContext(Dispatchers.IO) {

        val requestBody =
            jsonBodyStr.toRequestBody(
                JSON_MEDIA_TYPE
            )

        val request =
            Request.Builder()
                .url("$BASE_URL$endpoint/")
                .post(requestBody)
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {

                        return@withContext null
                    }

                    return@withContext JSONObject(
                        response.body?.string()
                            ?: ""
                    )
                }

        } catch (e: Exception) {

            e.printStackTrace()

            return@withContext null
        }
    }


    // =========================================================
    // SEND OTP
    //
    // Server generates the code now.
    // Client only sends the email.
    // =========================================================

    suspend fun sendOtpEmail(
        email: String
    ): Boolean = withContext(Dispatchers.IO) {

        val body =
            JSONObject()

        try {

            body.put(
                "email",
                email
            )

        } catch (e: Exception) {

            e.printStackTrace()

            return@withContext false
        }

        val result =
            postSecureNetworkCall(
                "api/auth/send-otp",
                body.toString()
            )

        return@withContext (
                result != null
                        && result.optString("status") == "success"
                )
    }


    // =========================================================
    // VERIFY EMAIL LOGIN
    //
    // Sends the code the user typed in.
    // =========================================================

    suspend fun verifyEmailLogin(
        email: String,
        username: String,
        deviceId: String,
        code: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val body =
            JSONObject()

        try {

            body.put(
                "email",
                email
            )

            body.put(
                "username",
                username
            )

            body.put(
                "device_id",
                deviceId
            )

            body.put(
                "code",
                code
            )

        } catch (e: Exception) {

            e.printStackTrace()

            return@withContext null
        }

        return@withContext postSecureNetworkCall(
            "api/auth/verify-email",
            body.toString()
        )
    }


    // =========================================================
    // INITIALIZE MATCHMAKING
    //
    // Sends player_token + player_name
    // as required by backend.
    // =========================================================

    suspend fun initializeMatchOnServer(
        playerToken: String,
        playerName: String,
        isTwoPlayer: Boolean
    ): JSONObject? = withContext(Dispatchers.IO) {

        val body =
            JSONObject()

        try {

            body.put(
                "player_token",
                playerToken
            )

            body.put(
                "player_name",
                playerName
            )

            body.put(
                "is_two_player_mode",
                isTwoPlayer
            )

        } catch (e: Exception) {

            e.printStackTrace()

            return@withContext null
        }

        return@withContext postSecureNetworkCall(
            "initialize-game",
            body.toString()
        )
    }


    // =========================================================
    // NOTE:
    //
    // rollDice() and moveTokenOnServer() have been REMOVED.
    //
    // These actions are now handled exclusively through the
    // WebSocket connection in GameSocket.kt.
    //
    // The REST endpoints "roll-dice" and "move-token" never
    // existed on the backend and were dead code.
    // =========================================================
}