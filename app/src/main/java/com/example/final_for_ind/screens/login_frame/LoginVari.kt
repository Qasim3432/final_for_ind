package com.example.final_for_ind.screens.login_frame

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun LoginVari(
    phoneNumber: String,
    onVerify: (otp: String) -> Unit,
    onResend: () -> Unit = {},
    isVerifying: Boolean = false
) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var timer by remember { mutableStateOf(60) }

    // ✅ FIX #1: trigger-based timer so it restarts after resend
    var resendTrigger by remember { mutableStateOf(0) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val isOtpComplete = otpValues.all { it.isNotEmpty() }

    // ✅ Timer with restart support
    LaunchedEffect(resendTrigger) {
        focusRequesters[0].requestFocus()
        timer = 60
        while (timer > 0) {
            delay(1000L)
            timer--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.5.dp, Color(0xFFFFD700))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF1A0000),
                                Color(0xFF0A0A0A)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Shield icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
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
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Enter OTP",
                        color = Color(0xFFFFD700),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Code sent to",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    Text(
                        phoneNumber,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(Modifier.height(40.dp))

                    // ==========================================
                    // OTP BOXES
                    // ==========================================

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        otpValues.forEachIndexed { index, value ->

                            val isActive = value.isNotEmpty()

                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1.05f else 1f,
                                animationSpec = tween(150),
                                label = "scale"
                            )

                            BasicTextField(
                                value = value,
                                onValueChange = { newValue ->

                                    // ✅ FIX #2 + #5: paste support + backspace handling

                                    val digitsOnly = newValue.filter { it.isDigit() }

                                    when {

                                        // Empty input → clear this cell, or step back
                                        newValue.isEmpty() -> {

                                            if (value.isNotEmpty()) {
                                                // Cleared this cell
                                                otpValues = otpValues.toMutableList().also {
                                                    it[index] = ""
                                                }
                                            } else if (index > 0) {
                                                // Backspace on empty → step back
                                                focusRequesters[index - 1].requestFocus()
                                                otpValues = otpValues.toMutableList().also {
                                                    it[index - 1] = ""
                                                }
                                            }
                                        }

                                        // Pasted multiple digits → distribute
                                        digitsOnly.length > 1 -> {

                                            val updated = otpValues.toMutableList()

                                            digitsOnly.take(6).forEachIndexed { i, d ->
                                                updated[i] = d.toString()
                                            }

                                            otpValues = updated

                                            val lastIndex = minOf(digitsOnly.length - 1, 5)
                                            focusRequesters[lastIndex].requestFocus()
                                        }

                                        // Normal single digit
                                        digitsOnly.length == 1 -> {

                                            otpValues = otpValues.toMutableList().also {
                                                it[index] = digitsOnly
                                            }

                                            if (index < 5) {
                                                focusRequesters[index + 1].requestFocus()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .scale(scale)
                                    .focusRequester(focusRequesters[index])
                                    .border(
                                        width = if (isActive) 2.5.dp else 2.dp,
                                        color = if (isActive)
                                            Color(0xFFFFD700)
                                        else
                                            Color(0xFFFFD700).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(
                                        Color(0xFF2B0000).copy(
                                            alpha = if (isActive) 0.8f else 0.6f
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                                    // ✅ FIX #7: accessibility
                                    .semantics {
                                        contentDescription = "OTP digit ${index + 1}"
                                    },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (value.isEmpty()) {
                                            Text(
                                                "-",
                                                color = Color(0xFFFFD700).copy(alpha = 0.4f),
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Light
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ==========================================
                    // TIMER / RESEND
                    // ==========================================

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        if (timer > 0) {

                            // ✅ FIX #3: proper minutes:seconds format
                            val minutes = timer / 60
                            val seconds = timer % 60

                            Text(
                                text = "Resend in $minutes:${
                                    seconds.toString().padStart(2, '0')
                                }",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        } else {

                            Text(
                                "Didn't get code? ",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )

                            TextButton(onClick = {
                                // ✅ FIX #1: trigger restarts LaunchedEffect
                                resendTrigger++
                                onResend()
                            }) {
                                Text(
                                    "Resend OTP",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ✅ FIX #4: loading state support
                    PremiumVerifyButton(
                        text = "VERIFY CODE",
                        enabled = isOtpComplete,
                        isLoading = isVerifying,
                        onClick = {
                            onVerify(otpValues.joinToString(""))
                        }
                    )
                }
            }
        }
    }
}


// ==========================================================
// PREMIUM VERIFY BUTTON
// ==========================================================

@Composable
fun PremiumVerifyButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.98f,
        animationSpec = tween(200),
        label = "btnScale"
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
                Color(0xFFFFD700),
                RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {

        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues()
        ) {

            if (isLoading) {

                // ✅ FIX #4: spinner while verifying
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )

            } else {

                Text(
                    text,
                    color = if (enabled)
                        Color.White
                    else
                        Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginVariPreview() {
    LoginVari(
        phoneNumber = "+92 300 1234567",
        onVerify = {},
        onResend = {},
        isVerifying = false
    )
}