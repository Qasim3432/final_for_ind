package com.example.final_for_ind.screens.login_frame

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun First_Screen(
    onPlayClick: () -> Unit,
    onJoinCodeClick: () -> Unit, //
    showLogoutMessage: Boolean = false
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showLogoutMessage) {
        if (showLogoutMessage) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Logged out successfully",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF1A0000),
                        contentColor = Color(0xFFFFD700),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(gradient).padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.88f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LOGO
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)))
                        .border(2.5.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎮", fontSize = 56.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(text = "Game Hub", color = Color(0xFFFFD700), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)

                Text(
                    text = "Play • Earn • Compete",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(56.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().shadow(24.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.5.dp, Color(0xFFFFD700))
                ) {
                    Box(
                        modifier = Modifier.background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(28.dp)).padding(32.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {


                            LoginPremiumButton(
                                text = "Play",
                                icon = Icons.Default.PlayArrow,
                                gradient = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))),
                                onClick = onPlayClick
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "2-4 Players • Quick Matches • Real Rewards",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LoginPremiumButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(100))

    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).scale(scale).clip(RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = text, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun First_ScreenPreview() {
    First_Screen(onPlayClick = {}, onJoinCodeClick = {})
}