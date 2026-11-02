package com.example.final_for_ind.screens.start

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.network.GameSessionManager
import kotlinx.coroutines.launch


@Composable
fun InviteCodePopup(
    sessionManager: GameSessionManager,
    onSkip: () -> Unit,
    onCodeApplied: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var codeText by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.5.dp, Color(0xFFFFD700))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(2.5.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "Have an Invite Code?",
                        color = Color(0xFFFFD700),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Referral code daal kar welcome bonus claim karein",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Code Input
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = {
                            codeText = it.uppercase()
                                .filter { c -> c.isLetterOrDigit() }
                                .take(6)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "ABC123",
                                color = Color.White.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        singleLine = true,
                        enabled = !isVerifying,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.5f),
                            unfocusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.3f),
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.4f),
                            cursorColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // APPLY BUTTON
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFD700),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        InviteActionButton(
                            text = "Apply Code",
                            gradient = Brush.horizontalGradient(
                                listOf(Color(0xFF8B0000), Color(0xFFD32F2F))
                            ),
                            textColor = Color.White,
                            enabled = codeText.length >= 4,
                            onClick = {
                                if (codeText.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Code likho",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@InviteActionButton
                                }

                                isVerifying = true
                                scope.launch {
                                    try {
                                        val token = sessionManager.getOrCreateUserToken()
                                        val success = sessionManager.verifyAndApplyReferral(
                                            token, codeText
                                        )
                                        isVerifying = false

                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                "🎉 Code applied! Bonus credited.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onCodeApplied()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Invalid ya already used code",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        isVerifying = false
                                        Toast.makeText(
                                            context,
                                            "Server error. Try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // SKIP BUTTON
                    TextButton(
                        onClick = onSkip,
                        enabled = !isVerifying
                    ) {
                        Text(
                            "Skip for now",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun InviteActionButton(
    text: String,
    gradient: Brush,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "invite_btn"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) gradient
                else Brush.horizontalGradient(
                    listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))
                )
            )
            .border(
                2.dp,
                if (enabled) Color(0xFFFFD700) else Color.Gray,
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isPressed,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
    }
}