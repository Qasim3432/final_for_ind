package com.example.final_for_ind.screens.login_frame.auth

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.final_for_ind.network.GameApi
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.screens.login_frame.LoginVari
import com.example.final_for_ind.screens.profile.uriToFile
import kotlinx.coroutines.launch


sealed interface AuthStep {
    object InputProfile : AuthStep
    data class VerifyCode(val email: String) : AuthStep
    object Loading : AuthStep
    object AuthSuccess : AuthStep
}


@Composable
fun AuthScreenRouter(
    sessionManager: GameSessionManager,
    onAuthComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf<AuthStep>(AuthStep.InputProfile) }
    var savedNickname by remember { mutableStateOf("") }
    var savedEmail by remember { mutableStateOf("") }

    // ✅ NEW: Profile image Uri ko hold karo — verify ke baad upload karenge
    var savedProfileUri by remember { mutableStateOf<Uri?>(null) }

    // ✅ NEW: Loading state for IntroSec button
    var isSendingCode by remember { mutableStateOf(false) }

    BackHandler(enabled = currentStep is AuthStep.Loading) { }

    LaunchedEffect(Unit) {
        if (sessionManager.isEmailLoggedIn()) {
            currentStep = AuthStep.AuthSuccess
        }
    }

    // ==========================================================
    // SEND OTP
    //
    // Server generates the code, we only send the email.
    // ==========================================================

    fun sendCode(email: String) {
        isSendingCode = true
        currentStep = AuthStep.Loading

        scope.launch {

            Log.d("OTP", "Requesting OTP for $email")

            val sent = GameApi.sendOtpEmail(email)

            if (sent) {

                Toast.makeText(
                    context,
                    "Code bhej diya $email pe",
                    Toast.LENGTH_LONG
                ).show()

                currentStep = AuthStep.VerifyCode(email)

            } else {

                Toast.makeText(
                    context,
                    "Code bhejne me error, net check karo",
                    Toast.LENGTH_LONG
                ).show()

                currentStep = AuthStep.InputProfile

            }

            isSendingCode = false
        }
    }

    // ==========================================================
    // UPLOAD PROFILE IMAGE (after successful auth)
    // ==========================================================

    suspend fun uploadProfileIfNeeded() {
        val uri = savedProfileUri ?: return

        try {
            val token = sessionManager.getOrCreateUserToken()
            val picFile = uriToFile(context, uri)

            if (picFile != null) {
                val ok = sessionManager.syncUserProfile(
                    deviceToken = token,           // ✅ correct
                    nickname = savedNickname,      // ✅ correct
                    email = savedEmail,            // ✅ correct
                    profilePicFile = picFile       // ✅ correct
                )

                Log.d("ProfileUpload", "Image upload result: $ok")
            }
        } catch (e: Exception) {
            Log.e("ProfileUpload", "Upload failed: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val step = currentStep) {

            is AuthStep.InputProfile -> {

                // ✅ FIX: 3-param onSubmit + isLoading
                IntroSec(
                    isLoading = isSendingCode,
                    onSubmit = { nickname, email, imageUri ->

                        savedNickname = nickname.trim()
                        savedEmail = email.trim()
                        savedProfileUri = imageUri

                        if (savedNickname.isEmpty() || savedEmail.isEmpty()) {

                            Toast.makeText(
                                context,
                                "Nickname aur email dono likho",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            sendCode(savedEmail)
                        }
                    }
                )
            }

            is AuthStep.VerifyCode -> {

                LoginVari(
                    phoneNumber = step.email,
                    onVerify = { enteredCode ->

                        val typedCode = enteredCode.trim()

                        if (typedCode.isEmpty()) {

                            Toast.makeText(
                                context,
                                "Code likho",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@LoginVari
                        }

                        currentStep = AuthStep.Loading

                        scope.launch {

                            // ==========================================
                            // VERIFY ON SERVER
                            // ==========================================

                            val ok = sessionManager.verifyEmailWithBackend(
                                savedEmail,
                                savedNickname,
                                typedCode
                            )

                            if (ok) {

                                // ✅ NEW: Auth ke baad profile image upload
                                uploadProfileIfNeeded()

                                Toast.makeText(
                                    context,
                                    "Welcome $savedNickname",
                                    Toast.LENGTH_SHORT
                                ).show()

                                currentStep = AuthStep.AuthSuccess

                            } else {

                                Toast.makeText(
                                    context,
                                    "Ghalat code ya email kisi aur device par hai",
                                    Toast.LENGTH_LONG
                                ).show()

                                currentStep = AuthStep.InputProfile

                            }
                        }
                    }
                )
            }

            is AuthStep.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            }

            is AuthStep.AuthSuccess -> {
                LaunchedEffect(Unit) { onAuthComplete() }
            }
        }
    }
}