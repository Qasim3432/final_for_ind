package com.example.final_for_ind.screens.login_frame

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.VpnKey
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
    onJoinCodeClick: () -> Unit,
    showLogoutMessage: Boolean = false
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
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
                        containerColor = Color(0xFF2C3E50),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 56.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Game Hub",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Play • Earn • Compete",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(56.dp))


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(28.dp)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(28.dp)
                            )
                            .padding(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            PremiumButton(
                                text = "Play Now",
                                icon = Icons.Default.PlayArrow,
                                gradient = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF38ef7d),
                                        Color(0xFF11998e)
                                    )
                                ),
                                onClick = onPlayClick
                            )


                            PremiumButton(
                                text = "Join with Code",
                                icon = Icons.Default.VpnKey,
                                gradient = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFB700)
                                    )
                                ),
                                onClick = onJoinCodeClick
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "2-4 Players • Quick Matches • Real Rewards",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun First_ScreenPreview() {
    First_Screen(
        onPlayClick = {},
        onJoinCodeClick = {}
    )
}