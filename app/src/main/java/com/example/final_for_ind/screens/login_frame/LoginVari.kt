package com.example.final_for_ind.screens.login_frame

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
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
    onResend: () -> Unit = {} // 👈 Naya: Resend ka callback
) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var timer by remember { mutableStateOf(60) } // 👈 Timer state

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    val isOtpComplete = otpValues.all { it.isNotEmpty() }

    // 👇 Ye block add karo: Timer countdown
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
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
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFFFFD700), modifier = Modifier.size(44.dp))
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Enter OTP", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Code sent to", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    Text(phoneNumber, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    Spacer(Modifier.height(40.dp))

                    // OTP 6 Boxes - same as yours
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        otpValues.forEachIndexed { index, value ->
                            val isActive = value.isNotEmpty()
                            val scale by animateFloatAsState(targetValue = if (isActive) 1.05f else 1f, animationSpec = tween(150), label = "otp_scale")

                            BasicTextField(
                                value = value,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                        otpValues = otpValues.toMutableList().also { it[index] = newValue }
                                        if (newValue.isNotEmpty() && index < 5) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                        if (newValue.isEmpty() && index > 0) {
                                            // auto backspace
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .scale(scale)
                                    .focusRequester(focusRequesters[index])
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Backspace && value.isEmpty() && index > 0) {
                                            focusRequesters[index - 1].requestFocus()
                                            otpValues = otpValues.toMutableList().also { it[index - 1] = "" }
                                            true
                                        } else false
                                    }
                                    .border(width = if (isActive) 2.5.dp else 1.5.dp, color = if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = if (isActive) 0.15f else 0.08f), RoundedCornerShape(16.dp)),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        if (value.isEmpty()) Text("-", color = Color.White.copy(alpha = 0.3f), fontSize = 32.sp, fontWeight = FontWeight.Light)
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // 👇 Timer / Resend logic update
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (timer > 0) {
                            Text(
                                text = "Resend in 0:${timer.toString().padStart(2, '0')}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        } else {
                            Text("Didn't get code? ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            TextButton(onClick = {
                                timer = 60 // reset timer
                                onResend() // Firebase ko dobara call
                            }) {
                                Text("Resend OTP", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    PremiumVerifyButton(text = "VERIFY CODE", enabled = isOtpComplete, onClick = { onVerify(otpValues.joinToString("")) })
                }
            }
        }
    }
}

@Composable
fun PremiumVerifyButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (enabled) 1f else 0.98f, animationSpec = tween(200), label = "verify_scale")
    Box(
        modifier = Modifier.fillMaxWidth().height(58.dp).scale(scale).clip(RoundedCornerShape(18.dp))
            .background(brush = if (enabled) Brush.horizontalGradient(listOf(Color(0xFF38ef7d), Color(0xFF11998e))) else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))), shape = RoundedCornerShape(18.dp))
            .border(2.dp, if (enabled) Color.White.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues()) {
            Text(text, color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginVariPreview() {
    LoginVari(phoneNumber = "+92 300 1234567", onVerify = {}, onResend = {})
}