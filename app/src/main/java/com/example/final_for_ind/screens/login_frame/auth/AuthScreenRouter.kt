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
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.screens.login_frame.LoginVari
import com.example.final_for_ind.screens.start.InviteCodePopup
import com.example.final_for_ind.screens.start.TermsScreen
import com.example.final_for_ind.utils.uriToFile
import kotlinx.coroutines.launch


sealed interface AuthStep {
    object InputProfile : AuthStep
    data class VerifyCode(val email: String) : AuthStep
    object InviteCodeStep : AuthStep
    object TermsStep : AuthStep
    object Loading : AuthStep
    object AuthSuccess : AuthStep
}


@Composable
fun AuthScreenRouter(
    sessionManager: GameSessionManager,
    onAuthComplete: () -> Unit,
    onCancel: (() -> Unit)? = null,
    forceLogin: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStep by remember {
        mutableStateOf<AuthStep>(
            if (forceLogin) AuthStep.InputProfile else AuthStep.Loading
        )
    }

    var savedNickname by remember { mutableStateOf("") }
    var savedEmail by remember { mutableStateOf("") }
    var savedProfileUri by remember { mutableStateOf<Uri?>(null) }

    // ✅ NEW: region lock — set at signup, used for OTP send + verify
    var savedCountry by remember { mutableStateOf("PK") }

    var isSendingCode by remember { mutableStateOf(false) }

    // Back button — cancel case
    BackHandler(enabled = forceLogin) {
        if (onCancel != null) onCancel()
    }

    LaunchedEffect(Unit) {
        if (!forceLogin) {
            kotlinx.coroutines.delay(200L)
            currentStep = AuthStep.AuthSuccess
            Log.d("AUTH_ROUTER", "Auto-skip to Home")
        }
    }

    // ✅ NEW: country passed to backend
    fun sendCode(email: String, username: String, country: String) {
        isSendingCode = true
        currentStep = AuthStep.Loading
        scope.launch {
            val sent = sessionManager.sendOtpEmail(email, username, country)
            if (sent) {
                Toast.makeText(
                    context,
                    "Code bhej diya $email pe",
                    Toast.LENGTH_LONG
                ).show()
                currentStep = AuthStep.VerifyCode(email)
            } else {
                val errMsg = sessionManager.lastAuthError
                if (!errMsg.isNullOrBlank()) {
                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        context,
                        "Code bhejne me error, net check karo",
                        Toast.LENGTH_LONG
                    ).show()
                }
                sessionManager.lastAuthError = null
                currentStep = AuthStep.InputProfile
            }
            isSendingCode = false
        }
    }

    suspend fun uploadProfileIfNeeded() {
        val uri = savedProfileUri ?: return
        try {
            val token = sessionManager.getOrCreateUserToken()
            val picFile = uriToFile(context, uri)

            if (picFile != null) {
                val result = sessionManager.syncUserProfile(
                    deviceToken = token,
                    nickname = savedNickname,
                    email = savedEmail,
                    profilePicFile = picFile
                )
                Log.d(
                    "ProfileUpload",
                    "Upload result: ${result.success} | ${result.errorMessage}"
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileUpload", "Upload failed: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val step = currentStep) {

            // ==================================================
            // STEP 1 — INTRO (nickname + email + photo + country)
            // ==================================================
            is AuthStep.InputProfile -> {
                IntroSec(
                    sessionManager = sessionManager,
                    isLoading = isSendingCode,
                    // ✅ 4 params now — country included
                    onSubmit = { nickname, email, imageUri, country ->
                        savedNickname = nickname.trim()
                        savedEmail = email.trim()
                        savedProfileUri = imageUri
                        savedCountry = country
                        if (savedNickname.isEmpty() || savedEmail.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Nickname aur email dono likho",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            sendCode(savedEmail, savedNickname, savedCountry)
                        }
                    }
                )
            }

            // ==================================================
            // STEP 2 — OTP VERIFY
            // ==================================================
            is AuthStep.VerifyCode -> {
                LoginVari(
                    phoneNumber = step.email,
                    onVerify = { enteredCode ->
                        val typedCode = enteredCode.trim()
                        if (typedCode.isEmpty()) {
                            Toast.makeText(context, "Code likho", Toast.LENGTH_SHORT).show()
                            return@LoginVari
                        }
                        currentStep = AuthStep.Loading
                        scope.launch {
                            val ok = sessionManager.verifyEmailWithBackend(
                                savedEmail,
                                savedNickname,
                                typedCode,
                                savedCountry   // ✅ NEW
                            )
                            if (ok) {
                                uploadProfileIfNeeded()
                                currentStep = AuthStep.InviteCodeStep
                            } else {
                                val errMsg = sessionManager.lastAuthError
                                if (!errMsg.isNullOrBlank()) {
                                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Ghalat code ya account kisi aur device pe hai",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                sessionManager.lastAuthError = null
                                currentStep = AuthStep.InputProfile
                            }
                        }
                    }
                )
            }

            // ==================================================
            // STEP 3 — INVITE CODE
            // ==================================================
            is AuthStep.InviteCodeStep -> {
                InviteCodePopup(
                    sessionManager = sessionManager,
                    onSkip = {
                        currentStep = AuthStep.TermsStep
                    },
                    onCodeApplied = {
                        currentStep = AuthStep.TermsStep
                    }
                )
            }

            // ==================================================
            // STEP 4 — TERMS
            // ==================================================
            is AuthStep.TermsStep -> {
                TermsScreen(
                    onAccept = {
                        currentStep = AuthStep.AuthSuccess
                    }
                )
            }

            // ==================================================
            // LOADING
            // ==================================================
            is AuthStep.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            }

            // ==================================================
            // COMPLETE
            // ==================================================
            is AuthStep.AuthSuccess -> {
                LaunchedEffect(Unit) { onAuthComplete() }
            }
        }
    }
}