package com.example.final_for_ind.network

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.final_for_ind.AppConfig

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONObject

import java.util.UUID


data class TransactionLog(
    val type: String,
    val amount: Int,
    val status: String,
    val dateString: String
)


data class ServerBalanceResult(
    val coins: Int,
    val lockedCoins: Int
)


data class GiftConfig(
    val enabled: Boolean,
    val paidCost: Int
)


data class SyncResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val profilePicUrl: String? = null
)


class GameSessionManager(
    private val context: Context
) {

    private val sharedPreferences =
        context.getSharedPreferences(
            "ludo_session_prefs",
            Context.MODE_PRIVATE
        )

    private val client =
        OkHttpClient()

    private val jsonMediaType =
        "application/json; charset=utf-8"
            .toMediaType()

    private val baseUrl =
        AppConfig.BASE_URL

    // ✅ Store last auth error so UI can display it
    var lastAuthError: String? = null


    // =========================================================
    // AUTH HEADER HELPER
    // =========================================================

    private fun Request.Builder.withAuth(): Request.Builder {

        val token =
            getEmailAuthToken()

        if (!token.isNullOrBlank()) {

            this.addHeader(
                "X-Auth-Token",
                token
            )
        }

        return this
    }


    // =========================================================
    // DEVICE TOKEN
    // =========================================================

    fun getOrCreateUserToken(): String {

        val existingToken =
            sharedPreferences.getString(
                "user_device_token",
                null
            )

        if (existingToken != null) {
            return existingToken
        }

        val modelName =
            Build.MODEL.replace(
                "\\s+".toRegex(),
                "_"
            )

        val shortId =
            UUID.randomUUID()
                .toString()
                .substring(0, 5)

        val generatedToken =
            "${modelName}_$shortId"

        sharedPreferences.edit()
            .putString(
                "user_device_token",
                generatedToken
            )
            .apply()

        return generatedToken
    }


    // =========================================================
    // DEVICE FINGERPRINT
    // =========================================================

    fun getDeviceFingerprint(): String {

        val androidId = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        } catch (e: Exception) {
            Log.e("FINGERPRINT", "Failed to read Android ID", e)
            "unknown"
        }

        val manufacturer = Build.MANUFACTURER
            .replace("\\s+".toRegex(), "_")

        val model = Build.MODEL
            .replace("\\s+".toRegex(), "_")

        return "${androidId}_${manufacturer}_${model}"
    }


    // =========================================================
    // EMAIL AUTH TOKEN
    // =========================================================

    fun saveEmailAuthToken(
        token: String
    ) {

        sharedPreferences.edit()
            .putString(
                "email_auth_token",
                token
            )
            .apply()
    }

    fun getEmailAuthToken(): String? {

        return sharedPreferences.getString(
            "email_auth_token",
            null
        )
    }

    fun getSavedUsername(): String {

        return sharedPreferences.getString(
            "email_user_name",
            "Guest"
        ) ?: "Guest"
    }

    fun getSavedUserId(): Int {

        return sharedPreferences.getInt(
            "email_user_id",
            0
        )
    }

    fun saveEmailUser(
        email: String,
        username: String
    ) {

        sharedPreferences.edit()
            .putString(
                "email_user_email",
                email
            )
            .putString(
                "email_user_name",
                username
            )
            .apply()
    }

    fun isEmailLoggedIn(): Boolean {

        val token =
            getEmailAuthToken()

        return !token.isNullOrBlank()
    }

    fun clearEmailAuth() {

        sharedPreferences.edit()
            .remove("email_auth_token")
            .remove("email_user_email")
            .remove("email_user_name")
            .remove("email_user_country")
            .apply()
    }


    // =========================================================
    // ✅ NEW: COUNTRY (region lock — set once at signup)
    // =========================================================

    fun saveCountry(country: String) {

        val normalized = country.trim().uppercase()

        if (normalized == "PK" || normalized == "IN") {
            sharedPreferences.edit()
                .putString(
                    "email_user_country",
                    normalized
                )
                .apply()
        }
    }

    fun getSavedCountry(): String {

        return sharedPreferences.getString(
            "email_user_country",
            "PK"
        ) ?: "PK"
    }


    // =========================================================
    // CHECK USERNAME AVAILABILITY  (public)
    // Returns: true = available, false = taken, null = error
    // =========================================================

    suspend fun checkUsernameAvailability(
        username: String
    ): Boolean? = withContext(Dispatchers.IO) {

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "username",
                username
            )

        } catch (e: Exception) {

            Log.e(
                "USERNAME_CHECK",
                "Failed to build payload",
                e
            )

            return@withContext null
        }

        Log.d(
            "USERNAME_CHECK",
            "Checking username=$username"
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/auth/check-username/"
                )
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "USERNAME_CHECK",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    val json =
                        JSONObject(body)

                    if (
                        json.optString("status")
                        != "success"
                    ) {
                        return@withContext null
                    }

                    return@withContext json.optBoolean(
                        "available",
                        false
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "USERNAME_CHECK",
                "Network error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // ✅ SEND OTP  (public) — country param added
    // =========================================================

    suspend fun sendOtpEmail(
        email: String,
        username: String,
        country: String
    ): Boolean = withContext(Dispatchers.IO) {

        lastAuthError = null
        val fingerprint = getDeviceFingerprint()
        val jsonBody = JSONObject()

        try {
            jsonBody.put("email", email)
            jsonBody.put("username", username)
            jsonBody.put("device_fingerprint", fingerprint)
            jsonBody.put("country", country.trim().uppercase())
        } catch (e: Exception) {
            Log.e("OTP", "Failed to build payload", e)
            return@withContext false
        }

        Log.d(
            "OTP",
            "Requesting OTP for $email | user=$username | country=$country | fp=${fingerprint.take(12)}..."
        )

        val request =
            Request.Builder()
                .url("${baseUrl}api/auth/send-otp/")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

        try {
            client.newCall(request).execute().use { response ->

                val responseBody = response.body?.string() ?: ""
                Log.d("OTP", "HTTP ${response.code}: $responseBody")

                if (!response.isSuccessful) {
                    try {
                        val errJson = JSONObject(responseBody)
                        if (errJson.optString("code") == "USERNAME_TAKEN") {
                            lastAuthError = errJson.optString("message")
                        }
                    } catch (_: Exception) { }

                    return@withContext false
                }

                val json = JSONObject(responseBody)
                return@withContext (json.optString("status") == "success")
            }
        } catch (e: Exception) {
            Log.e("OTP", "Send OTP failed", e)
            false
        }
    }


    // =========================================================
    // ✅ VERIFY EMAIL LOGIN  (public) — country param added
    // =========================================================

    suspend fun verifyEmailWithBackend(
        email: String,
        username: String,
        code: String,
        country: String
    ): Boolean = withContext(Dispatchers.IO) {

        lastAuthError = null
        val deviceId = getOrCreateUserToken()
        val fingerprint = getDeviceFingerprint()
        val jsonBody = JSONObject()

        try {

            jsonBody.put("email", email)
            jsonBody.put("username", username)
            jsonBody.put("device_id", deviceId)
            jsonBody.put("device_fingerprint", fingerprint)
            jsonBody.put("code", code)
            jsonBody.put("country", country.trim().uppercase())

        } catch (e: Exception) {

            Log.e(
                "EMAIL_AUTH",
                "Failed to build payload",
                e
            )

            return@withContext false
        }

        Log.d(
            "EMAIL_AUTH",
            "Verifying email=$email device=$deviceId | country=$country | fp=${fingerprint.take(12)}..."
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/auth/verify-email/"
                )
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "EMAIL_AUTH",
                        "HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {

                        try {
                            val errJson = JSONObject(responseBody)
                            val errCode = errJson.optString("code", "")
                            val errMsg = errJson.optString("message", "Verification failed")

                            if (errCode == "DEVICE_MISMATCH") {
                                lastAuthError = "This account is already registered on another device."
                                Log.e("EMAIL_AUTH", "🚫 DEVICE_MISMATCH")
                            } else if (errCode == "USERNAME_TAKEN") {
                                lastAuthError = errMsg
                                Log.e("EMAIL_AUTH", "🚫 USERNAME_TAKEN: $errMsg")
                            } else if (errCode == "INVALID_COUNTRY") {
                                lastAuthError = errMsg
                                Log.e("EMAIL_AUTH", "🚫 INVALID_COUNTRY: $errMsg")
                            }
                        } catch (_: Exception) { }

                        return@withContext false
                    }

                    val json =
                        JSONObject(responseBody)

                    if (
                        json.optString("status")
                        != "success"
                    ) {

                        Log.e(
                            "EMAIL_AUTH",
                            "Failed: ${json.optString("message")}"
                        )

                        return@withContext false
                    }

                    val userObj =
                        json.optJSONObject("user")

                    val authToken =
                        userObj?.optString(
                            "auth_token",
                            ""
                        )

                    if (authToken.isNullOrBlank()) {
                        return@withContext false
                    }

                    saveEmailAuthToken(authToken)

                    saveEmailUser(
                        email,
                        username
                    )

                    // ✅ Save server-authoritative country
                    val returnedCountry = json.optString("country", country)
                    if (returnedCountry == "PK" || returnedCountry == "IN") {
                        saveCountry(returnedCountry)
                    }

                    val userId =
                        userObj?.optInt(
                            "user_id",
                            0
                        ) ?: 0

                    sharedPreferences.edit()
                        .putInt(
                            "email_user_id",
                            userId
                        )
                        .apply()

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


    // =========================================================
    // LOGOUT USER  (auth)
    // =========================================================

    suspend fun logoutUser(): Boolean = withContext(Dispatchers.IO) {

        val deviceId =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_id",
                deviceId
            )

        } catch (e: Exception) {

            Log.e(
                "LOGOUT",
                "Failed to build payload",
                e
            )

            return@withContext false
        }

        Log.d(
            "LOGOUT",
            "Requesting logout for device=$deviceId"
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/auth/logout/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "LOGOUT",
                        "HTTP ${response.code}: $body"
                    )

                    response.isSuccessful
                }

        } catch (e: Exception) {

            Log.e(
                "LOGOUT",
                "Network error",
                e
            )

            false
        }
    }


    // =========================================================
    // MATCHMAKING  (auth)
    // =========================================================

    suspend fun registerAndJoinMatch(
        isTwoPlayer: Boolean
    ): String = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "player_token",
                userToken
            )

            jsonBody.put(
                "player_name",
                getSavedUsername()
            )

            jsonBody.put(
                "is_two_player_mode",
                isTwoPlayer
            )

        } catch (e: Exception) {

            Log.e(
                "MATCHMAKING",
                "Failed to build payload",
                e
            )

            return@withContext ""
        }

        Log.d(
            "MATCHMAKING",
            "Joining matchmaking: " +
                    "token=$userToken twoPlayer=$isTwoPlayer"
        )

        val request =
            Request.Builder()
                .url("${baseUrl}initialize-game/")
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "MATCHMAKING",
                        "HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {

                        Log.e(
                            "MATCHMAKING",
                            "Server rejected: $responseBody"
                        )

                        return@withContext ""
                    }

                    val json =
                        JSONObject(responseBody)

                    val status =
                        json.optString("status", "")

                    if (status == "error") {

                        Log.e(
                            "MATCHMAKING",
                            "Error: ${
                                json.optString(
                                    "message",
                                    "Unknown"
                                )
                            }"
                        )

                        return@withContext ""
                    }

                    val gameId =
                        json.optString("game_id", "")

                    if (gameId.isBlank()) {

                        Log.e(
                            "MATCHMAKING",
                            "Server did not return game_id"
                        )

                        return@withContext ""
                    }

                    Log.d(
                        "MATCHMAKING",
                        "Joined game=$gameId"
                    )

                    return@withContext gameId
                }

        } catch (e: Exception) {

            Log.e(
                "MATCHMAKING",
                "Network error",
                e
            )

            return@withContext ""
        }
    }


    // =========================================================
    // PLAY WITH FRIENDS — CREATE ROOM  (auth)
    // =========================================================

    suspend fun createFriendRoom(
        betAmount: Int,
        isTwoPlayer: Boolean
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "player_token",
                userToken
            )

            jsonBody.put(
                "player_name",
                getSavedUsername()
            )

            jsonBody.put(
                "bet_amount",
                betAmount
            )

            jsonBody.put(
                "is_two_player_mode",
                isTwoPlayer
            )

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Failed to build payload",
                e
            )

            return@withContext JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Failed to build request"
                )
            }
        }

        Log.d(
            "FRIENDS",
            "Creating room: token=$userToken " +
                    "bet=$betAmount 2P=$isTwoPlayer"
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/game/create-room/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "FRIENDS",
                        "Create room HTTP ${response.code}: $body"
                    )

                    val parsed =
                        try {

                            JSONObject(body)

                        } catch (e: Exception) {

                            null
                        }

                    if (parsed != null) {
                        return@withContext parsed
                    }

                    return@withContext JSONObject().apply {

                        put(
                            "status",
                            "error"
                        )

                        put(
                            "message",
                            "Server error ${response.code}"
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Create room failed",
                e
            )

            JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Network error: ${e.message}"
                )
            }
        }
    }


    // =========================================================
    // PLAY WITH FRIENDS — JOIN ROOM  (auth)
    // =========================================================

    suspend fun joinFriendRoom(
        roomCode: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "room_code",
                roomCode.uppercase()
            )

            jsonBody.put(
                "player_token",
                userToken
            )

            jsonBody.put(
                "player_name",
                getSavedUsername()
            )

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Failed to build payload",
                e
            )

            return@withContext JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Failed to build request"
                )
            }
        }

        Log.d(
            "FRIENDS",
            "Joining room $roomCode with token=$userToken"
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/game/join-room/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "FRIENDS",
                        "Join room HTTP ${response.code}: $body"
                    )

                    val parsed =
                        try {

                            JSONObject(body)

                        } catch (e: Exception) {

                            null
                        }

                    if (parsed != null) {
                        return@withContext parsed
                    }

                    return@withContext JSONObject().apply {

                        put(
                            "status",
                            "error"
                        )

                        put(
                            "message",
                            "Server error ${response.code}"
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Join room failed",
                e
            )

            JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Network error: ${e.message}"
                )
            }
        }
    }


    // =========================================================
    // PLAY WITH FRIENDS — CANCEL ROOM  (auth)
    // =========================================================

    suspend fun cancelFriendRoom(
        gameId: String
    ): Boolean = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "player_token",
                userToken
            )

            jsonBody.put(
                "game_id",
                gameId
            )

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Failed to build payload",
                e
            )

            return@withContext false
        }

        Log.d(
            "FRIENDS",
            "Cancelling room gameId=$gameId"
        )

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/game/cancel-room/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "FRIENDS",
                        "Cancel HTTP ${response.code}: $body"
                    )

                    response.isSuccessful
                }

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Cancel failed",
                e
            )

            false
        }
    }


    // =========================================================
    // PLAY WITH FRIENDS — GET ROOM STATUS  (auth)
    // =========================================================

    suspend fun getRoomStatus(
        gameId: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val url =
            "${baseUrl}api/game/room-status/$gameId/" +
                    "?player_token=$userToken"

        Log.d(
            "FRIENDS",
            "GET room-status: $url"
        )

        val request =
            Request.Builder()
                .url(url)
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "FRIENDS",
                        "room-status HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e(
                "FRIENDS",
                "Get room status failed",
                e
            )

            null
        }
    }


    // =========================================================
    // BALANCE  (auth)
    //
    // ✅ FIX: .withAuth() add kiya
    // Pehle ye endpoint public GET tha — koi bhi kisi ka bhi
    // balance fetch kar sakta tha agar device_token pata ho.
    // Ab backend @require_device_owner + middleware check karta hai.
    // =========================================================

    suspend fun fetchUserBalanceFromServer(
        deviceToken: String
    ): ServerBalanceResult = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/deposit/balance/$deviceToken/"
                )
                .withAuth()   // ✅ NEW
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {

                        return@withContext ServerBalanceResult(
                            0,
                            0
                        )
                    }

                    val data =
                        JSONObject(
                            response.body?.string()
                                ?: ""
                        )

                    return@withContext ServerBalanceResult(
                        coins = data.optInt(
                            "coins",
                            0
                        ),

                        lockedCoins = data.optInt(
                            "locked_coins",
                            0
                        )
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "Balance fetch failure",
                e
            )

            ServerBalanceResult(0, 0)
        }
    }


    // =========================================================
    // ✅ ADMIN PAYMENT DETAILS — region-filtered via device_token
    // =========================================================

    suspend fun fetchAdminPaymentDetails():
            Map<String, Pair<String, String>> =
        withContext(Dispatchers.IO) {

            val resultMap =
                mutableMapOf<String, Pair<String, String>>()

            val userToken =
                getOrCreateUserToken()

            val request =
                Request.Builder()
                    .url(
                        "${baseUrl}api/deposit/methods/" +
                                "?device_token=$userToken"
                    )
                    .withAuth()
                    .get()
                    .build()

            try {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        if (!response.isSuccessful) {
                            return@withContext resultMap
                        }

                        val root =
                            JSONObject(
                                response.body?.string()
                                    ?: ""
                            )

                        val methodsJson =
                            root.getJSONObject("methods")

                        val keys =
                            methodsJson.keys()

                        while (keys.hasNext()) {

                            val key =
                                keys.next()

                            val details =
                                methodsJson
                                    .getJSONObject(key)

                            resultMap[key] =
                                Pair(
                                    details.getString(
                                        "name"
                                    ),
                                    details.getString(
                                        "number"
                                    )
                                )
                        }
                    }

            } catch (e: Exception) {

                Log.e(
                    "SESSION_MGR",
                    "Payment details fetch error",
                    e
                )
            }

            return@withContext resultMap
        }


    // =========================================================
    // DEPOSIT NOTIFICATION  (auth)
    // =========================================================

    suspend fun submitDepositNotification(
        localAmount: Float,
        currency: String,
        method: String,
        senderName: String
    ): Boolean = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "local_amount",
                localAmount
            )

            jsonBody.put(
                "currency",
                currency
            )

            jsonBody.put(
                "payment_method",
                method
            )

            jsonBody.put(
                "sender_name",
                senderName
            )

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "Failed to build deposit payload",
                e
            )

            return@withContext false
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/deposit/submit/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    return@withContext response.isSuccessful
                }

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "Submission error",
                e
            )

            false
        }
    }


    // =========================================================
    // TRANSACTION HISTORY  (auth)
    // =========================================================

    suspend fun fetchTransactionHistory(
        deviceToken: String
    ): List<TransactionLog> = withContext(Dispatchers.IO) {

        val historyList =
            mutableListOf<TransactionLog>()

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/deposit/history/$deviceToken/"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {
                        return@withContext historyList
                    }

                    val root =
                        JSONObject(
                            response.body?.string()
                                ?: ""
                        )

                    val transactions =
                        root.getJSONArray("transactions")

                    for (
                    i in 0
                            until transactions.length()
                    ) {

                        val obj =
                            transactions
                                .getJSONObject(i)

                        historyList.add(
                            TransactionLog(
                                type = obj.getString(
                                    "type"
                                ),

                                amount = obj.getInt(
                                    "amount"
                                ),

                                status = obj.getString(
                                    "status"
                                ),

                                dateString = obj.getString(
                                    "date"
                                )
                            )
                        )
                    }
                }

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "History fetch error",
                e
            )
        }

        return@withContext historyList
    }


    // =========================================================
    // WITHDRAWAL NOTIFICATION (legacy)  (auth)
    // =========================================================

    suspend fun submitWithdrawalNotification(
        amount: Int,
        method: String,
        title: String,
        number: String
    ): Boolean = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "amount",
                amount
            )

            jsonBody.put(
                "method",
                method
            )

            jsonBody.put(
                "account_title",
                title
            )

            jsonBody.put(
                "account_number",
                number
            )

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "Failed to build withdrawal payload",
                e
            )

            return@withContext false
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/withdraw/submit/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    return@withContext response.isSuccessful
                }

        } catch (e: Exception) {

            false
        }
    }


    // =========================================================
    // JOIN WAGER MATCH  (auth)
    // =========================================================

    suspend fun joinWagerMatch(
        gameId: String,
        betAmount: Int
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "game_id",
                gameId
            )

            jsonBody.put(
                "bet_amount",
                betAmount
            )

        } catch (e: Exception) {

            Log.e(
                "WAGER",
                "Failed to build payload",
                e
            )

            return@withContext JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Failed to build request"
                )
            }
        }

        Log.d(
            "WAGER",
            "Sending wager: device=$userToken " +
                    "game=$gameId bet=$betAmount"
        )

        val request =
            Request.Builder()
                .url("${baseUrl}api/wager/join/")
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "WAGER",
                        "HTTP ${response.code}: $responseBody"
                    )

                    val jsonResponse =
                        try {

                            JSONObject(responseBody)

                        } catch (e: Exception) {

                            JSONObject().apply {

                                put(
                                    "status",
                                    "error"
                                )

                                put(
                                    "message",
                                    "Server returned invalid JSON: " +
                                            responseBody
                                )
                            }
                        }

                    if (!response.isSuccessful) {

                        Log.e(
                            "WAGER",
                            "Rejected. HTTP=${response.code} " +
                                    "Response=$jsonResponse"
                        )

                        return@withContext jsonResponse
                    }

                    if (jsonResponse.optString("status") == "error" || !jsonResponse.optBoolean("success", true)) {

                        Log.e(
                            "WAGER",
                            "Failed: ${
                                jsonResponse.optString(
                                    "message",
                                    "Unknown"
                                )
                            }"
                        )

                        return@withContext jsonResponse
                    }

                    Log.d(
                        "WAGER",
                        "Accepted: $jsonResponse"
                    )

                    return@withContext jsonResponse
                }

        } catch (e: Exception) {

            Log.e(
                "WAGER",
                "Network error",
                e
            )

            return@withContext JSONObject().apply {

                put(
                    "status",
                    "error"
                )

                put(
                    "message",
                    "Network error: ${e.message}"
                )
            }
        }
    }


    // =========================================================
    // REFERRAL CODE  (public GET)
    // =========================================================

    suspend fun fetchUserReferralCode(
        deviceToken: String
    ): String = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/referral/$deviceToken/"
                )
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {
                        return@withContext "ERROR"
                    }

                    val data =
                        JSONObject(
                            response.body?.string()
                                ?: ""
                        )

                    return@withContext data.optString(
                        "referral_code",
                        "NONE"
                    )
                }

        } catch (e: Exception) {

            "ERROR"
        }
    }


    // =========================================================
    // REFERRAL DASHBOARD  (auth)
    // =========================================================

    suspend fun fetchReferralDashboard(
        deviceToken: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/referral-dashboard/" +
                            "$deviceToken/"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "REFERRAL",
                        "Dashboard HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e("REFERRAL", "Fetch failed", e)
            null
        }
    }


    // =========================================================
    // APPLY REFERRAL  (auth)
    // =========================================================

    suspend fun verifyAndApplyReferral(
        deviceToken: String,
        code: String
    ): Boolean = withContext(Dispatchers.IO) {

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                deviceToken
            )

            jsonBody.put(
                "referral_code",
                code
            )

        } catch (e: Exception) {

            Log.e(
                "SESSION_MGR",
                "Failed to build referral payload",
                e
            )

            return@withContext false
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/verify-referral/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    if (!response.isSuccessful) {
                        return@withContext false
                    }

                    val data =
                        JSONObject(
                            response.body?.string()
                                ?: ""
                        )

                    return@withContext (
                            data.optString("status")
                                    == "success"
                            )
                }

        } catch (e: Exception) {

            false
        }
    }


    // =========================================================
    // SYNC USER PROFILE  (auth)
    // =========================================================

    suspend fun syncUserProfile(
        deviceToken: String,
        nickname: String,
        email: String,
        profilePicFile: java.io.File? = null
    ): SyncResult = withContext(Dispatchers.IO) {

        val requestBodyBuilder =
            okhttp3.MultipartBody.Builder()
                .setType(
                    okhttp3.MultipartBody.FORM
                )
                .addFormDataPart(
                    "device_token",
                    deviceToken
                )
                .addFormDataPart(
                    "nickname",
                    nickname
                )
                .addFormDataPart(
                    "email",
                    email
                )

        if (
            profilePicFile != null
            && profilePicFile.exists()
        ) {

            requestBodyBuilder.addFormDataPart(
                "profile_pic",
                profilePicFile.name,
                profilePicFile.asRequestBody(
                    "image/*".toMediaType()
                )
            )
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/update-profile/"
                )
                .withAuth()
                .post(requestBodyBuilder.build())
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PROFILE",
                        "HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {

                        val errorMsg = try {
                            JSONObject(responseBody)
                                .optString("error", "")
                                .ifBlank {
                                    "Update failed (${response.code})"
                                }
                        } catch (_: Exception) {
                            "Update failed (${response.code})"
                        }

                        return@withContext SyncResult(
                            success = false,
                            errorMessage = errorMsg
                        )
                    }

                    val json =
                        JSONObject(responseBody)

                    val picUrl =
                        json.optString(
                            "profile_pic_url",
                            ""
                        ).takeIf { it.isNotBlank() }

                    val editor = sharedPreferences.edit()

                    if (picUrl != null) {
                        editor.putString(
                            "profile_pic_url",
                            picUrl
                        )
                    }

                    editor.putString(
                        "email_user_name",
                        nickname
                    )
                    editor.putString(
                        "email_user_email",
                        email
                    )
                    editor.apply()

                    val success = (
                            json.optString("status") == "success"
                            )

                    return@withContext SyncResult(
                        success = success,
                        errorMessage = if (!success)
                            json.optString("error", "Update failed")
                        else null,
                        profilePicUrl = picUrl
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PROFILE",
                "Network error during profile sync",
                e
            )

            return@withContext SyncResult(
                success = false,
                errorMessage = "Network error. Please check your connection."
            )
        }
    }


    // =========================================================
    // GIFT CONFIG  (public)
    // =========================================================

    suspend fun fetchGiftConfig():
            GiftConfig = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url("${baseUrl}api/gift/config/")
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "GIFT",
                        "Config HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {

                        return@withContext GiftConfig(
                            false,
                            40
                        )
                    }

                    val json =
                        JSONObject(responseBody)

                    return@withContext GiftConfig(
                        enabled = (
                                json.optString(
                                    "gift_enabled",
                                    "0"
                                ) == "1"
                                ),

                        paidCost = json.optInt(
                            "paid_spin_cost",
                            40
                        )
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


    // =========================================================
    // SPIN STATUS  (auth)
    // =========================================================

    suspend fun getSpinStatus():
            JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

        } catch (e: Exception) {

            Log.e(
                "GIFT",
                "Failed to build status payload",
                e
            )

            return@withContext null
        }

        Log.d(
            "GIFT",
            "Getting spin status for token=$userToken"
        )

        val request =
            Request.Builder()
                .url("${baseUrl}api/gift/status/")
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "GIFT",
                        "Status HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
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


    // =========================================================
    // APP CONFIG  (public)
    // =========================================================

    suspend fun fetchAppConfig():
            JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url("${baseUrl}api/config/app/")
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "APP_CONFIG",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e(
                "APP_CONFIG",
                "Fetch failed",
                e
            )

            null
        }
    }


    // =========================================================
    // SUBMIT SPIN  (auth)
    // =========================================================

    suspend fun submitSpin():
            JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

        } catch (e: Exception) {

            Log.e(
                "GIFT",
                "Failed to build spin payload",
                e
            )

            return@withContext null
        }

        Log.d(
            "GIFT",
            "Submitting spin for token=$userToken"
        )

        val request =
            Request.Builder()
                .url("${baseUrl}api/gift/spin/")
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "GIFT",
                        "Spin HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
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


    // =========================================================
    // WITHDRAWAL PIN — STATUS  (auth)
    // =========================================================

    suspend fun getPinStatus():
            JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Failed to build status payload",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/pin/status/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PIN",
                        "Status HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Status network error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // WITHDRAWAL PIN — SET  (auth)
    // =========================================================

    suspend fun setWithdrawalPin(
        pin: String,
        confirmPin: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "pin",
                pin
            )

            jsonBody.put(
                "confirm_pin",
                confirmPin
            )

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Failed to build set payload",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/pin/set/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PIN",
                        "Set HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Set network error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // WITHDRAWAL PIN — CHANGE  (auth)
    // =========================================================

    suspend fun changeWithdrawalPin(
        oldPin: String,
        newPin: String,
        confirmPin: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "old_pin",
                oldPin
            )

            jsonBody.put(
                "new_pin",
                newPin
            )

            jsonBody.put(
                "confirm_pin",
                confirmPin
            )

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Failed to build change payload",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/pin/change/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PIN",
                        "Change HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Change network error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // WITHDRAWAL PIN — RESET STEP 1  (auth)
    // =========================================================

    suspend fun requestPinReset():
            JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Failed to build reset request",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/pin/reset-request/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PIN",
                        "Reset request HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Reset request error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // WITHDRAWAL PIN — RESET STEP 2  (auth)
    // =========================================================

    suspend fun verifyPinReset(
        otpCode: String,
        newPin: String,
        confirmPin: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "otp_code",
                otpCode
            )

            jsonBody.put(
                "new_pin",
                newPin
            )

            jsonBody.put(
                "confirm_pin",
                confirmPin
            )

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Failed to build reset verify",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/pin/reset-verify/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "PIN",
                        "Reset verify HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "PIN",
                "Reset verify error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // WITHDRAWAL SUBMIT WITH PIN  (auth)
    //
    // ✅ FIX: client_request_id — idempotency key add kiya
    //
    // Agar user "Submit" button ko double-tap kare, ya network
    // retry ho jaye, to backend same UUID detect karke same
    // response return karta hai — coins dobara deduct nahi hote.
    // =========================================================

    suspend fun submitWithdrawalWithPin(
        amount: Int,
        method: String,
        title: String,
        number: String,
        pin: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        // ✅ NEW: Idempotency key
        val clientRequestId =
            UUID.randomUUID().toString()

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                userToken
            )

            jsonBody.put(
                "amount",
                amount
            )

            jsonBody.put(
                "method",
                method
            )

            jsonBody.put(
                "account_title",
                title
            )

            jsonBody.put(
                "account_number",
                number
            )

            jsonBody.put(
                "pin",
                pin
            )

            // ✅ NEW
            jsonBody.put(
                "client_request_id",
                clientRequestId
            )

        } catch (e: Exception) {

            Log.e(
                "WITHDRAW",
                "Failed to build payload",
                e
            )

            return@withContext null
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/withdraw/submit/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val responseBody =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "WITHDRAW",
                        "HTTP ${response.code}: $responseBody"
                    )

                    return@withContext JSONObject(
                        responseBody
                    )
                }

        } catch (e: Exception) {

            Log.e(
                "WITHDRAW",
                "Network error",
                e
            )

            return@withContext null
        }
    }


    // =========================================================
    // GAME HISTORY  (auth)
    // =========================================================

    suspend fun fetchGameHistory(
        deviceToken: String,
        limit: Int = 50
    ): JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/game-history/" +
                            "$deviceToken/?limit=$limit"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "GAME_HISTORY",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e(
                "GAME_HISTORY",
                "Fetch failed",
                e
            )

            null
        }
    }


    // =========================================================
    // LEADERBOARD  (public GET)
    // =========================================================

    suspend fun fetchLeaderboard(
        period: String = "alltime",
        limit: Int = 50
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken =
            getOrCreateUserToken()

        val url =
            "${baseUrl}api/leaderboard/" +
                    "?period=$period" +
                    "&limit=$limit" +
                    "&device_token=$userToken"

        val request =
            Request.Builder()
                .url(url)
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "LEADERBOARD",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e(
                "LEADERBOARD",
                "Fetch failed",
                e
            )

            null
        }
    }


    // =========================================================
    // LEADERBOARD — PENDING REWARDS  (auth)
    // =========================================================

    suspend fun fetchPendingRewards(): JSONObject? =
        withContext(Dispatchers.IO) {

            val userToken = getOrCreateUserToken()

            val request =
                Request.Builder()
                    .url(
                        "${baseUrl}api/leaderboard/pending-rewards/" +
                                "?device_token=$userToken"
                    )
                    .withAuth()
                    .get()
                    .build()

            try {
                client.newCall(request)
                    .execute()
                    .use { response ->

                        val body = response.body?.string() ?: ""

                        Log.d(
                            "PENDING_REWARDS",
                            "HTTP ${response.code}: $body"
                        )

                        if (!response.isSuccessful) {
                            return@withContext null
                        }

                        return@withContext JSONObject(body)
                    }

            } catch (e: Exception) {
                Log.e("PENDING_REWARDS", "Fetch failed", e)
                null
            }
        }


    // =========================================================
    // LEADERBOARD — CLAIM REWARD  (auth)
    // =========================================================

    suspend fun claimLeaderboardReward(
        claimId: Int
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken = getOrCreateUserToken()

        val jsonBody = JSONObject()

        try {
            jsonBody.put("device_token", userToken)
            jsonBody.put("claim_id", claimId)
        } catch (e: Exception) {
            Log.e("CLAIM_REWARD", "Build failed", e)
            return@withContext null
        }

        val request =
            Request.Builder()
                .url("${baseUrl}api/leaderboard/claim-reward/")
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(jsonMediaType)
                )
                .build()

        try {
            client.newCall(request)
                .execute()
                .use { response ->

                    val body = response.body?.string() ?: ""

                    Log.d(
                        "CLAIM_REWARD",
                        "HTTP ${response.code}: $body"
                    )

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {
            Log.e("CLAIM_REWARD", "Network error", e)
            null
        }
    }


    // =========================================================
    // DAILY BONUS — STATUS  (auth)
    // =========================================================

    suspend fun fetchDailyBonusStatus(
        deviceToken: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/daily-bonus/status/" +
                            "$deviceToken/"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "DAILY_BONUS",
                        "Status HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e("DAILY_BONUS", "Status failed", e)
            null
        }
    }


    // =========================================================
    // DAILY BONUS — CLAIM  (auth)
    // =========================================================

    suspend fun claimDailyBonus(): JSONObject? =
        withContext(Dispatchers.IO) {

            val userToken =
                getOrCreateUserToken()

            val jsonBody =
                JSONObject()

            try {

                jsonBody.put(
                    "device_token",
                    userToken
                )

            } catch (e: Exception) {

                Log.e("DAILY_BONUS", "Build failed", e)
                return@withContext null
            }

            val request =
                Request.Builder()
                    .url(
                        "${baseUrl}api/user/daily-bonus/claim/"
                    )
                    .withAuth()
                    .post(
                        jsonBody.toString()
                            .toRequestBody(jsonMediaType)
                    )
                    .build()

            try {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val body =
                            response.body?.string()
                                ?: ""

                        Log.d(
                            "DAILY_BONUS",
                            "Claim HTTP ${response.code}: $body"
                        )

                        return@withContext JSONObject(body)
                    }

            } catch (e: Exception) {

                Log.e("DAILY_BONUS", "Claim failed", e)
                null
            }
        }


    // =========================================================
    // SPIN HISTORY  (auth)
    // =========================================================

    suspend fun fetchSpinHistory(
        deviceToken: String,
        limit: Int = 20
    ): JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/spin-history/" +
                            "$deviceToken/?limit=$limit"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "SPIN_HISTORY",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e("SPIN_HISTORY", "Fetch failed", e)
            null
        }
    }


    // =========================================================
    // SEND USER LOCATION  (auth)
    // =========================================================

    suspend fun sendUserLocation(
        deviceToken: String,
        latitude: Double,
        longitude: Double
    ): Boolean = withContext(Dispatchers.IO) {

        val jsonBody =
            JSONObject()

        try {

            jsonBody.put(
                "device_token",
                deviceToken
            )

            jsonBody.put(
                "latitude",
                latitude
            )

            jsonBody.put(
                "longitude",
                longitude
            )

        } catch (e: Exception) {

            Log.e(
                "LOCATION",
                "Failed to build payload",
                e
            )

            return@withContext false
        }

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/user/location/"
                )
                .withAuth()
                .post(
                    jsonBody.toString()
                        .toRequestBody(
                            jsonMediaType
                        )
                )
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "LOCATION",
                        "HTTP ${response.code}: $body"
                    )

                    response.isSuccessful
                }

        } catch (e: Exception) {

            Log.e(
                "LOCATION",
                "Send failed",
                e
            )

            false
        }
    }


    // =========================================================
    // FETCH WITHDRAWAL STATUS  (auth)
    // =========================================================

    suspend fun fetchWithdrawalStatus(
        deviceToken: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        val request =
            Request.Builder()
                .url(
                    "${baseUrl}api/withdraw/status/$deviceToken/"
                )
                .withAuth()
                .get()
                .build()

        try {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body =
                        response.body?.string()
                            ?: ""

                    Log.d(
                        "WITHDRAW_STATUS",
                        "HTTP ${response.code}: $body"
                    )

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    return@withContext JSONObject(body)
                }

        } catch (e: Exception) {

            Log.e(
                "WITHDRAW_STATUS",
                "Fetch failed",
                e
            )

            null
        }
    }


    // =========================================================
    // ✅ NEW: AGORA VOICE TOKEN
    // =========================================================

    suspend fun fetchAgoraToken(
        channelName: String,
        uid: Int
    ): JSONObject? = withContext(Dispatchers.IO) {

        val userToken = getOrCreateUserToken()
        val jsonBody = JSONObject()

        try {
            jsonBody.put("device_token", userToken)
            jsonBody.put("channel_name", channelName)
            jsonBody.put("uid", uid)
        } catch (e: Exception) {
            Log.e("AGORA", "Failed to build payload", e)
            return@withContext null
        }

        val request =
            Request.Builder()
                .url("${baseUrl}api/agora/token/")
                .withAuth()
                .post(
                    jsonBody.toString().toRequestBody(jsonMediaType)
                )
                .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("AGORA", "Token HTTP ${response.code}: $responseBody")

                if (!response.isSuccessful) {
                    return@withContext null
                }

                return@withContext JSONObject(responseBody)
            }
        } catch (e: Exception) {
            Log.e("AGORA", "Network error during token fetch", e)
            return@withContext null
        }
    }
}