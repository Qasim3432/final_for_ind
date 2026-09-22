package com.example.final_for_ind.screens.login_frame.auth

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
    var generatedCode by remember { mutableStateOf("") }

    BackHandler(enabled = currentStep is AuthStep.Loading) { }

    LaunchedEffect(Unit) {
        if (sessionManager.isEmailLoggedIn()) {
            currentStep = AuthStep.AuthSuccess
        }
    }

    fun sendCode(email: String) {
        currentStep = AuthStep.Loading
        scope.launch {
            generatedCode = (100000..999999).random().toString()
            Log.d("OTP", "Code $generatedCode for $email")
            val sent = GameApi.sendOtpEmail(email, generatedCode)
            if (sent) {
                Toast.makeText(context, "Code bhej diya $email pe", Toast.LENGTH_LONG).show()
                currentStep = AuthStep.VerifyCode(email)
            } else {
                Toast.makeText(context, "Code bhejne me error, net check karo", Toast.LENGTH_LONG).show()
                currentStep = AuthStep.InputProfile
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val step = currentStep) {
            is AuthStep.InputProfile -> {
                IntroSec(onSubmit = { nickname, email ->
                    savedNickname = nickname.trim()
                    savedEmail = email.trim()
                    if (savedNickname.isEmpty() || savedEmail.isEmpty()) {
                        Toast.makeText(context, "Nickname aur email dono likho", Toast.LENGTH_SHORT).show()
                    } else {
                        sendCode(savedEmail)
                    }
                })
            }
            is AuthStep.VerifyCode -> {
                LoginVari(
                    phoneNumber = step.email,
                    onVerify = { enteredCode ->
                        if (enteredCode.trim() == generatedCode) {
                            currentStep = AuthStep.Loading
                            scope.launch {
                                val ok = sessionManager.verifyEmailWithBackend(savedEmail, savedNickname)
                                if (ok) {
                                    Toast.makeText(context, "Welcome $savedNickname", Toast.LENGTH_SHORT).show()
                                    currentStep = AuthStep.AuthSuccess
                                } else {
                                    Toast.makeText(context, "Ye email kisi aur device par hai", Toast.LENGTH_LONG).show()
                                    currentStep = AuthStep.InputProfile
                                }
                            }
                        } else {
                            Toast.makeText(context, "Ghalat code", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            is AuthStep.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                }
            }
            is AuthStep.AuthSuccess -> {
                LaunchedEffect(Unit) { onAuthComplete() }
            }
        }
    }
}