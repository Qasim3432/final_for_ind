package com.example.final_for_ind.screens.start


import android.widget.Toast

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
fun PinSetupScreen(
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

    var pin by remember {
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

    val isFormValid =
        pin.length == 4
                && confirmPin.length == 4
                && pin == confirmPin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column(Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Text(
                        "Set Withdrawal PIN",
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

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                            2.5.dp,
                            Color(0xFFFFD700),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Create your secure PIN",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "You will need this PIN every time you withdraw coins.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                PinField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter { c ->
                            c.isDigit()
                        }.take(4)
                    },
                    label = "Enter 4-digit PIN"
                )

                Spacer(Modifier.height(16.dp))

                PinField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it.filter { c ->
                            c.isDigit()
                        }.take(4)
                    },
                    label = "Confirm PIN"
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

                Spacer(Modifier.height(32.dp))

                PinActionButton(
                    text = if (isLoading) "Setting..." else "SET PIN",
                    enabled = isFormValid && !isLoading,
                    onClick = {

                        errorMessage = ""

                        scope.launch {

                            isLoading = true

                            val result =
                                sessionManager.setWithdrawalPin(
                                    pin,
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
                                    "PIN set successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onSuccess()

                            } else {

                                errorMessage =
                                    result.optString(
                                        "message",
                                        "Failed to set PIN."
                                    )
                            }
                        }
                    }
                )
            }
        }
    }
}


// ==========================================================
// PIN FIELD
// ==========================================================

@Composable
fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                label,
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword
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
        shape = RoundedCornerShape(16.dp)
    )
}


// ==========================================================
// ACTION BUTTON
// ==========================================================

@Composable
fun PinActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.97f,
        animationSpec = tween(120)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (enabled)
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF8B0000),
                            Color(0xFFD32F2F)
                        )
                    )
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.Gray.copy(alpha = 0.3f),
                            Color.Gray.copy(alpha = 0.3f)
                        )
                    ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                2.5.dp,
                if (enabled)
                    Color(0xFFFFD700)
                else
                    Color.Gray,
                RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = if (enabled)
                Color.White
            else
                Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            letterSpacing = 1.sp
        )
    }
}