package com.example.final_for_ind.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GameApi {
    private const val BASE_URL = "http://192.168.18.48:8090/"
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private suspend fun postSecureNetworkCall(endpoint: String, jsonBodyStr: String = ""): JSONObject? = withContext(Dispatchers.IO) {
        val requestBody = jsonBodyStr.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL$endpoint/")
            .post(requestBody)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                JSONObject(response.body?.string() ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sendOtpEmail(email: String, code: String): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("code", code)
        }
        val result = postSecureNetworkCall("api/auth/send-otp", body.toString())
        result != null
    }

    suspend fun initializeMatchOnServer(isTwoPlayer: Boolean): JSONObject? {
        val body = JSONObject().put("is_two_player_mode", isTwoPlayer)
        return postSecureNetworkCall("initialize-game", body.toString())
    }

    suspend fun rollDice(): JSONObject? {
        return postSecureNetworkCall("roll-dice")
    }

    suspend fun moveTokenOnServer(tokenId: Int, colorName: String): JSONObject? {
        val body = JSONObject().apply {
            put("token_id", tokenId)
            put("color", colorName)
        }
        return postSecureNetworkCall("move-token", body.toString())
    }
}