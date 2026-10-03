package com.example.final_for_ind.screens.start

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.final_for_ind.network.GameSessionManager

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinResetScreen(
    sessionManager: GameSessionManager,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )

    var step by remember {
        mutableStateOf(1)
    }

    var maskedEmail by remember {
        mutableStateOf("")
    }

    var otpCode by remember {
        mutableStateOf("")
    }

    var newPin by remember {
        mutableStateOf("")
    }

    var confirmPin by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column(Modifier.fillMaxSize()) {

            TopAppBar(
                title = {

                    Text(
                        "Reset PIN",
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFF2B0000),
                                CircleShape
                            )
                            .border(
                                2.dp,
                                Color(0xFFFFD700),
                                CircleShape
                            )
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Color(0xFFFFD700),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (step == 1) {

                    Text(
                        text = "Verify Your Email",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "We will send a 6-digit code to your registered email.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage.isNotEmpty()) {

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    PinActionButton(
                        text = if (isLoading) "Sending..." else "SEND OTP",
                        enabled = !isLoading,
                        onClick = {

                            errorMessage = ""

                            scope.launch {

                                isLoading = true

                                val result =
                                    sessionManager.requestPinReset()

                                isLoading = false

                                if (result == null) {

                                    errorMessage =
                                        "Network error. Try again."

                                    return@launch
                                }

                                val status =
                                    result.optString(
                                        "status",
                                        ""
                                    )

                                if (status == "success") {

                                    maskedEmail =
                                        result.optString(
                                            "masked_email",
                                            ""
                                        )

                                    Toast.makeText(
                                        context,
                                        "OTP sent!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    step = 2

                                } else {

                                    errorMessage =
                                        result.optString(
                                            "message",
                                            "Failed to send OTP."
                                        )
                                }
                            }
                        }
                    )
                }

                else {

                    Text(
                        text = "Enter Reset Code",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Code sent to $maskedEmail",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {

                            otpCode = it.filter { c ->
                                c.isDigit()
                            }.take(6)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {

                            Text(
                                "6-digit code",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2B0000),
                            unfocusedContainerColor = Color(0xFF2B0000).copy(alpha = 0.7f),
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                            cursorColor = Color(0xFFFFD700),
                            focusedLabelColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    PinField(
                        value = newPin,
                        onValueChange = {

                            newPin = it.filter { c ->
                                c.isDigit()
                            }.take(4)
                        },
                        label = "New 4-digit PIN"
                    )

                    Spacer(Modifier.height(16.dp))

                    PinField(
                        value = confirmPin,
                        onValueChange = {

                            confirmPin = it.filter { c ->
                                c.isDigit()
                            }.take(4)
                        },
                        label = "Confirm new PIN"
                    )

                    if (errorMessage.isNotEmpty()) {

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    val isValid =
                        otpCode.length == 6
                                && newPin.length == 4
                                && confirmPin.length == 4
                                && newPin == confirmPin

                    PinActionButton(
                        text = if (isLoading) "Verifying..." else "RESET PIN",
                        enabled = isValid && !isLoading,
                        onClick = {

                            errorMessage = ""

                            scope.launch {

                                isLoading = true

                                val result =
                                    sessionManager.verifyPinReset(
                                        otpCode,
                                        newPin,
                                        confirmPin
                                    )

                                isLoading = false

                                if (result == null) {

                                    errorMessage =
                                        "Network error. Try again."

                                    return@launch
                                }

                                val status =
                                    result.optString(
                                        "status",
                                        ""
                                    )

                                if (status == "success") {

                                    Toast.makeText(
                                        context,
                                        "PIN reset successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onSuccess()

                                } else {

                                    errorMessage =
                                        result.optString(
                                            "message",
                                            "Failed to reset PIN."
                                        )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}