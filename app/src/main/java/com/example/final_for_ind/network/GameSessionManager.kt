package com.example.final_for_ind.network

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

// Representation for localized transaction ledger history records
data class TransactionLog(
    val type: String,       // "DEPOSIT" or "WITHDRAWAL"
    val amount: Int,
    val status: String,     // "PENDING", "APPROVED", "REJECTED"
    val dateString: String  // Date format from your Django server
)

// 🟢 NEW: Data class to parse complete atomic balance responses
data class ServerBalanceResult(
    val coins: Int,
    val lockedCoins: Int
)

// 🟢 NEW: Gift config model
data class GiftConfig(
    val enabled: Boolean,
    val paidCost: Int
)

class GameSessionManager(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("ludo_session_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "http://192.168.18.48:8090/" // Replace with your host machine IP

    fun getOrCreateUserToken(): String {
        val existingToken = sharedPreferences.getString("user_device_token", null)
        if (existingToken != null) return existingToken

        val modelName = Build.MODEL.replace("\\s+".toRegex(), "_")
        val shortId = UUID.randomUUID().toString().substring(0, 5)
        val generatedToken = "${modelName}_$shortId"

        sharedPreferences.edit().putString("user_device_token", generatedToken).apply()
        return generatedToken
    }

    // --------------------------------------------------
    // EMAIL AUTH - Token mixture IP + device + email
    // --------------------------------------------------

    fun saveEmailAuthToken(token: String) {
        sharedPreferences.edit().putString("email_auth_token", token).apply()
    }

    fun getEmailAuthToken(): String? {
        return sharedPreferences.getString("email_auth_token", null)
    }

    fun getSavedUsername(): String {
        return sharedPreferences.getString("email_user_name", "Guest") ?: "Guest"
    }

    fun getSavedUserId(): Int {
        return sharedPreferences.getInt("email_user_id", 0)
    }

    fun saveEmailUser(email: String, username: String) {
        sharedPreferences.edit()
            .putString("email_user_email", email)
            .putString("email_user_name", username)
            .apply()
    }

    fun isEmailLoggedIn(): Boolean {
        val token = getEmailAuthToken()
        return !token.isNullOrBlank()
    }

    fun clearEmailAuth() {
        sharedPreferences.edit()
            .remove("email_auth_token")
            .remove("email_user_email")
            .remove("email_user_name")
            .apply()
    }

    suspend fun verifyEmailWithBackend(
        email: String,
        username: String
    ): Boolean = withContext(Dispatchers.IO) {

        val deviceId = getOrCreateUserToken()

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("username", username)
            put("device_id", deviceId)
        }

        Log.d(
            "EMAIL_AUTH",
            "Verifying email: $email device=$deviceId"
        )

        val request = Request.Builder()
            .url("${baseUrl}api/auth/verify-email/")
            .post(
                jsonBody
                    .toString()
                    .toRequestBody(jsonMediaType)
            )
            .build()

        try {

            client.newCall(request).execute().use { response ->

                val responseBody =
                    response.body?.string() ?: ""

                Log.d(
                    "EMAIL_AUTH",
                    "HTTP ${response.code}: $responseBody"
                )

                if (!response.isSuccessful) {
                    return@withContext false
                }

                val json = JSONObject(responseBody)

                if (
                    json.optString("status") != "success"
                ) {
                    Log.e(
                        "EMAIL_AUTH",
                        "Failed: ${json.optString("message")}"
                    )
                    return@withContext false
                }

                val userObj = json.optJSONObject("user")
                val authToken = userObj?.optString("auth_token", "")

                if (authToken.isNullOrBlank()) {
                    return@withContext false
                }

                // Save token in device - koi aur login nahi kar payega
                saveEmailAuthToken(authToken)
                saveEmailUser(email, username)
                val userId = userObj?.optInt("user_id", 0) ?: 0
                sharedPreferences.edit().putInt("email_user_id", userId).apply()

                Log.d(
                    "EMAIL_AUTH",
                    "Email verified and token saved"
                )

                return@withContext true
            }

        } catch (e: Exception) {

            Log.e(
                "EMAIL_AUTH",
                "Network error during email auth",
                e
            )

            return@withContext false
        }
    }

    suspend fun registerAndJoinMatch(
        isTwoPlayer: Boolean
    ): String = withContext(Dispatchers.IO) {

        val userToken = getOrCreateUserToken()

        val jsonBody = JSONObject().apply {
            put("player_token", userToken)
            put("player_name", getSavedUsername())
            put("is_two_player_mode", isTwoPlayer)
        }

        Log.d(
            "MATCHMAKING",
            "Joining matchmaking: token=$userToken twoPlayer=$isTwoPlayer"
        )

        val request = Request.Builder()
            .url("${baseUrl}initialize-game/")
            .post(
                jsonBody
                    .toString()
                    .toRequestBody(jsonMediaType)
            )
            .build()

        try {

            client.newCall(request).execute().use { response ->

                val responseBody =
                    response.body?.string() ?: ""

                Log.d(
                    "MATCHMAKING",
                    "HTTP ${response.code}: $responseBody"
                )

                if (!response.isSuccessful) {

                    Log.e(
                        "MATCHMAKING",
                        "Server rejected matchmaking: $responseBody"
                    )

                    return@withContext ""
                }

                val json = JSONObject(responseBody)

                val status =
                    json.optString("status", "")

                if (
                    status == "error"
                ) {

                    Log.e(
                        "MATCHMAKING",
                        "Matchmaking error: ${
                            json.optString(
                                "message",
                                "Unknown error"
                            )
                        }"
                    )

                    return@withContext ""
                }

                val gameId =
                    json.optString(
                        "game_id",
                        ""
                    )

                if (gameId.isBlank()) {

                    Log.e(
                        "MATCHMAKING",
                        "Server did not return game_id"
                    )

                    return@withContext ""
                }

                Log.d(
                    "MATCHMAKING",
                    "Successfully joined game=$gameId"
                )

                return@withContext gameId
            }

        } catch (e: Exception) {

            Log.e(
                "MATCHMAKING",
                "Network error during matchmaking",
                e
            )

            return@withContext ""
        }
    }

    suspend fun fetchUserBalanceFromServer(deviceToken: String): ServerBalanceResult =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${baseUrl}api/deposit/balance/$deviceToken/")
                .get()
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext ServerBalanceResult(0, 0)
                    val data = JSONObject(response.body?.string() ?: "")

                    return@withContext ServerBalanceResult(
                        coins = data.optInt("coins", 0),
                        lockedCoins = data.optInt("locked_coins", 0)
                    )
                }
            } catch (e: Exception) {
                Log.e("SESSION_MGR", "Balance metric synchronization footprint failure", e)
                ServerBalanceResult(0, 0)
            }
        }

    suspend fun fetchAdminPaymentDetails(): Map<String, Pair<String, String>> =
        withContext(Dispatchers.IO) {
            val resultMap = mutableMapOf<String, Pair<String, String>>()
            val request = Request.Builder()
                .url("${baseUrl}api/deposit/methods/")
                .get()
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext resultMap
                    val root = JSONObject(response.body?.string() ?: "")
                    val methodsJson = root.getJSONObject("methods")

                    val keys = methodsJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val details = methodsJson.getJSONObject(key)
                        resultMap[key] =
                            Pair(details.getString("name"), details.getString("number"))
                    }
                }
            } catch (e: Exception) {
                Log.e("SESSION_MGR", "Payment details fetch error", e)
            }
            return@withContext resultMap
        }

    suspend fun submitDepositNotification(
        amount: Int,
        method: String,
        senderName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val userToken = getOrCreateUserToken()
        val jsonBody = JSONObject().apply {
            put("device_token", userToken)
            put("amount", amount)
            put("payment_method", method)
            put("sender_name", senderName)
        }
        val request = Request.Builder()
            .url("${baseUrl}api/deposit/submit/")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SESSION_MGR", "Submission error", e)
            false
        }
    }

    suspend fun fetchTransactionHistory(deviceToken: String): List<TransactionLog> =
        withContext(Dispatchers.IO) {
            val historyList = mutableListOf<TransactionLog>()
            val request = Request.Builder()
                .url("${baseUrl}api/deposit/history/$deviceToken/")
                .get()
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext historyList
                    val root = JSONObject(response.body?.string() ?: "")
                    val transactions = root.getJSONArray("transactions")

                    for (i in 0 until transactions.length()) {
                        val obj = transactions.getJSONObject(i)
                        historyList.add(
                            TransactionLog(
                                type = obj.getString("type"),
                                amount = obj.getInt("amount"),
                                status = obj.getString("status"),
                                dateString = obj.getString("date")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SESSION_MGR", "Failed to fetch history ledger logs", e)
            }
            return@withContext historyList
        }

    suspend fun submitWithdrawalNotification(
        amount: Int,
        method: String,
        title: String,
        number: String
    ): Boolean = withContext(Dispatchers.IO) {
        val userToken = getOrCreateUserToken()
        val jsonBody = JSONObject().apply {
            put("device_token", userToken)
            put("amount", amount)
            put("method", method)
            put("account_title", title)
            put("account_number", number)
        }
        val request = Request.Builder()
            .url("${baseUrl}api/withdraw/submit/")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()
        try {
            client.newCall(request).execute()
                .use { response -> return@withContext response.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun joinWagerMatch(
        gameId: String,
        betAmount: Int
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken = getOrCreateUserToken()

        val jsonBody = JSONObject().apply {
            put("device_token", userToken)
            put("game_id", gameId)
            put("bet_amount", betAmount)
        }

        Log.d(
            "WAGER",
            "Sending wager request: " +
                    "device_token=$userToken " +
                    "game_id=$gameId " +
                    "bet_amount=$betAmount"
        )

        val request = Request.Builder()
            .url("${baseUrl}api/wager/join/")
            .post(
                jsonBody
                    .toString()
                    .toRequestBody(jsonMediaType)
            )
            .build()

        try {

            client.newCall(request).execute().use { response ->

                val responseBody =
                    response.body?.string() ?: ""

                Log.d(
                    "WAGER",
                    "HTTP ${response.code}: $responseBody"
                )

                val jsonResponse = try {
                    JSONObject(responseBody)
                } catch (e: Exception) {

                    JSONObject().apply {
                        put("status", "error")
                        put(
                            "message",
                            "Server returned invalid JSON: $responseBody"
                        )
                    }
                }

                if (!response.isSuccessful) {

                    Log.e(
                        "WAGER",
                        "Wager rejected. " +
                                "HTTP=${response.code} " +
                                "Response=$jsonResponse"
                    )

                    return@withContext jsonResponse
                }

                if (
                    jsonResponse.optString("status") == "error" ||
                    jsonResponse.optBoolean("success", true) == false
                ) {

                    Log.e(
                        "WAGER",
                        "Wager failed: " +
                                jsonResponse.optString(
                                    "message",
                                    "Unknown server error"
                                )
                    )

                    return@withContext jsonResponse
                }

                Log.d(
                    "WAGER",
                    "Wager successfully accepted: $jsonResponse"
                )

                return@withContext jsonResponse
            }

        } catch (e: Exception) {

            Log.e(
                "WAGER",
                "Network error while joining wager",
                e
            )

            return@withContext JSONObject().apply {
                put("status", "error")
                put(
                    "message",
                    "Network error: ${e.message}"
                )
            }
        }
    }

    suspend fun fetchUserReferralCode(deviceToken: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}api/user/referral/$deviceToken/")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "ERROR"
                val data = JSONObject(response.body?.string() ?: "")
                return@withContext data.optString("referral_code", "NONE")
            }
        } catch (e: Exception) {
            "ERROR"
        }
    }

    suspend fun verifyAndApplyReferral(deviceToken: String, code: String): Boolean =
        withContext(Dispatchers.IO) {
            val jsonPayload = JSONObject().apply {
                put("device_token", deviceToken)
                put("referral_code", code)
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("${baseUrl}api/user/verify-referral/")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val data = JSONObject(response.body?.string() ?: "")
                    return@withContext data.optString("status") == "success"
                }
            } catch (e: Exception) {
                false
            }
        }

    suspend fun syncUserProfile(
        deviceToken: String,
        nickname: String,
        email: String,
        profilePicFile: java.io.File? = null
    ): Boolean = withContext(Dispatchers.IO) {

        val requestBodyBuilder =
            okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("device_token", deviceToken)
                .addFormDataPart("nickname", nickname)
                .addFormDataPart("email", email)

        if (profilePicFile != null && profilePicFile.exists()) {
            requestBodyBuilder.addFormDataPart(
                "profile_pic",
                profilePicFile.name,
                profilePicFile.asRequestBody(
                    "image/*".toMediaType()
                )
            )
        }

        val request = Request.Builder()
            .url("${baseUrl}api/user/update-profile/")
            .post(requestBodyBuilder.build())
            .build()

        try {

            client.newCall(request).execute().use { response ->

                val responseBody =
                    response.body?.string() ?: ""

                Log.d(
                    "PROFILE",
                    "HTTP ${response.code}: $responseBody"
                )

                if (!response.isSuccessful) {
                    return@withContext false
                }

                val json = JSONObject(responseBody)

                val picUrl =
                    json.optString("profile_pic_url", "")

                if (picUrl.isNotBlank()) {
                    sharedPreferences.edit()
                        .putString("profile_pic_url", picUrl)
                        .apply()
                }

                // username / email local save taake screen change par gayab na ho
                sharedPreferences.edit()
                    .putString("email_user_name", nickname)
                    .putString("email_user_email", email)
                    .apply()

                return@withContext (
                        json.optString("status") == "success"
                        )
            }

        } catch (e: Exception) {

            Log.e(
                "PROFILE",
                "Network error during profile sync",
                e
            )

            return@withContext false
        }
    }

    // --------------------------------------------------
    // GIFT / SPIN - Attach
    // --------------------------------------------------

    suspend fun fetchGiftConfig(): GiftConfig =
        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url("${baseUrl}api/gift/config/")
                .get()
                .build()

            try {

                client.newCall(request).execute().use { response ->

                    val responseBody =
                        response.body?.string() ?: ""

                    Log.d(
                        "GIFT",
                        "Config HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {
                        return@withContext GiftConfig(false, 40)
                    }

                    val json = JSONObject(responseBody)

                    return@withContext GiftConfig(
                        enabled = json.optString("gift_enabled", "0") == "1",
                        paidCost = json.optInt("paid_spin_cost", 40)
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "GIFT",
                    "Config fetch failed",
                    e
                )

                GiftConfig(false, 40)
            }
        }

    suspend fun getSpinStatus(): JSONObject? =
        withContext(Dispatchers.IO) {

            val userToken = getOrCreateUserToken()

            val jsonBody = JSONObject().apply {
                put("device_token", userToken)
            }

            Log.d(
                "GIFT",
                "Getting spin status for token=$userToken"
            )

            val request = Request.Builder()
                .url("${baseUrl}api/gift/status/")
                .post(
                    jsonBody
                        .toString()
                        .toRequestBody(jsonMediaType)
                )
                .build()

            try {

                client.newCall(request).execute().use { response ->

                    val responseBody =
                        response.body?.string() ?: ""

                    Log.d(
                        "GIFT",
                        "Status HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(responseBody)
                }

            } catch (e: Exception) {

                Log.e(
                    "GIFT",
                    "Spin status network error",
                    e
                )

                return@withContext null
            }
        }

    suspend fun submitSpin(): JSONObject? =
        withContext(Dispatchers.IO) {

            val userToken = getOrCreateUserToken()

            val jsonBody = JSONObject().apply {
                put("device_token", userToken)
            }

            Log.d(
                "GIFT",
                "Submitting spin for token=$userToken"
            )

            val request = Request.Builder()
                .url("${baseUrl}api/gift/spin/")
                .post(
                    jsonBody
                        .toString()
                        .toRequestBody(jsonMediaType)
                )
                .build()

            try {

                client.newCall(request).execute().use { response ->

                    val responseBody =
                        response.body?.string() ?: ""

                    Log.d(
                        "GIFT",
                        "Spin HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(responseBody)
                }

            } catch (e: Exception) {

                Log.e(
                    "GIFT",
                    "Network error during spin",
                    e
                )

                return@withContext null
            }
        }

    suspend fun sendOtpEmail(email: String, code: String): Boolean = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("code", code)
        }
        val request = Request.Builder()
            .url("${baseUrl}api/auth/send-otp/")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                Log.d("OTP", "Send OTP HTTP ${response.code}")
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("OTP", "Send OTP failed", e)
            false
        }
    }
}