package com.example.final_for_ind.screens.login_frame

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.network.GameSessionManager
import kotlinx.coroutines.launch

@Composable
fun First_Screen(
    onPlayClick: () -> Unit,
    sessionManager: GameSessionManager,
    showLogoutMessage: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isEnteringCodeScreen by remember { mutableStateOf(false) }
    var enteredReferralCode by remember { mutableStateOf("") }
    var isVerifyingNetworkCall by remember { mutableStateOf(false) }

    LaunchedEffect(showLogoutMessage) {
        if (showLogoutMessage) {
            scope.launch { snackbarHostState.showSnackbar("Logged out successfully") }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A0A0A), Color(0xFF1A1508), Color(0xFF0A0A0A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0A0A).copy(alpha = 0.96f))
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                        .padding(3.dp)
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑", fontSize = 76.sp, textAlign = TextAlign.Center)
                            Text(
                                text = if (isEnteringCodeScreen) "Enter Code" else "Game Hub",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isEnteringCodeScreen) "Claim your welcome bonus balance" else "Play • Earn • Compete",
                                fontSize = 19.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFFFE8A0),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (!isEnteringCodeScreen) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LuxuryButton(
                                    text = "Play Now",
                                    icon = Icons.Default.PlayArrow,
                                    gradient = Brush.horizontalGradient(listOf(Color(0xFF9E1B1B), Color(0xFFD4A017), Color(0xFFFFD700))),
                                    textColor = Color(0xFFFFE8A0),
                                    onClick = onPlayClick
                                )
                                LuxuryButton(
                                    text = "Join with Code",
                                    icon = Icons.Default.Key,
                                    gradient = Brush.verticalGradient(listOf(Color(0xFFFFE8A0), Color(0xFFD4A017), Color(0xFFFFE8A0))),
                                    textColor = Color.Black,
                                    onClick = { isEnteringCodeScreen = true }
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = enteredReferralCode,
                                    onValueChange = { enteredReferralCode = it.uppercase().trim() },
                                    label = { Text("Referral Promo Code", color = Color(0xFFFFE8A0).copy(alpha = 0.7f)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFD700),
                                        unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.4f),
                                        focusedLabelColor = Color(0xFFFFD700),
                                        cursorColor = Color(0xFFFFD700),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isVerifyingNetworkCall) {
                                    CircularProgressIndicator(color = Color(0xFFFFD700))
                                } else {
                                    LuxuryButton(
                                        text = "Verify & Claim",
                                        icon = Icons.Default.CheckCircle,
                                        gradient = Brush.verticalGradient(listOf(Color(0xFFFFE8A0), Color(0xFFD4A017))),
                                        textColor = Color.Black,
                                        onClick = {
                                            if (enteredReferralCode.isEmpty()) {
                                                Toast.makeText(context, "Please enter a valid code", Toast.LENGTH_SHORT).show()
                                                return@LuxuryButton
                                            }
                                            isVerifyingNetworkCall = true
                                            scope.launch {
                                                try {
                                                    val token = sessionManager.getOrCreateUserToken()
                                                    val success = sessionManager.verifyAndApplyReferral(token, enteredReferralCode)
                                                    isVerifyingNetworkCall = false
                                                    if (success) {
                                                        Toast.makeText(context, "Code Verified! Welcome Bonus Credited.", Toast.LENGTH_LONG).show()
                                                        isEnteringCodeScreen = false
                                                    } else {
                                                        Toast.makeText(context, "Invalid or Already Used Referral Code!", Toast.LENGTH_LONG).show()
                                                    }
                                                } catch (e: Exception) {
                                                    isVerifyingNetworkCall = false
                                                    Toast.makeText(context, "Server Error connection lost!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                    TextButton(onClick = { isEnteringCodeScreen = false }) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFFFE8A0), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Back to Main Menu", color = Color(0xFFFFE8A0))
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "2-4 Players • Quick Matches • Real Rewards",
                            fontSize = 14.sp,
                            color = Color(0xFFFFE8A0).copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text("❖", color = Color(0xFFFFD700), modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
                    Text("❖", color = Color(0xFFFFD700), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
                    Text("❖", color = Color(0xFFFFD700), modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    Text("❖", color = Color(0xFFFFD700), modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun LuxuryButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    textColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, tween(100), label = "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(gradient)
            .border(2.dp, Color(0xFFFFE8A0), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, color = textColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}