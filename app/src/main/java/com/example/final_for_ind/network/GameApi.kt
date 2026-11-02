package com.example.final_for_ind.network

import com.example.final_for_ind.AppConfig


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object GameApi {

    // ✅ BuildConfig se — no hardcoded IP
    private val BASE_URL =
        AppConfig.BASE_URL

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
    // ⚠️ DEPRECATED — AuthScreenRouter ab
    //    sessionManager.sendOtpEmail() use karta hai
    //    (device_fingerprint ke saath).
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
    // ⚠️ DEPRECATED — use sessionManager.verifyEmailWithBackend
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
    // ⚠️ DEPRECATED — use sessionManager.registerAndJoinMatch
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
}