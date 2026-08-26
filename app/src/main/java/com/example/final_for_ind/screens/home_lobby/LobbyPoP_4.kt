package com.example.final_for_ind.screens.home_lobby

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourPlayerDialog(
    walletBalance: Int = 0,
    onDismiss: () -> Unit = {},
    onPlayBet: (betAmount: Int) -> Unit = { _ -> }
) {
    var betAmount by remember { mutableStateOf(1000) }
    val bets = listOf(200, 500, 1000, 2000, 5000, 10000)
    val currentBetIndex = bets.indexOf(betAmount).coerceAtLeast(0)
    val hasEnoughBalance = betAmount <= walletBalance

    // 👇 BLACK OVERLAY
    val overlayGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A).copy(alpha = 0.95f), Color(0xFF1A0000).copy(alpha = 0.95f))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(overlayGradient),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.width(340.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(32.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN BORDER
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(28.dp))
                        .padding(vertical = 32.dp, horizontal = 28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(8.dp))

                        // 👇 GOLDEN BALANCE BADGE
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Balance: $walletBalance coins",
                                color = Color(0xFF1A0000),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        Text("CHOOSE BET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), letterSpacing = 2.sp)
                        Spacer(Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            // 👇 NAAM CHANGE KIYA TA KE CLASH NA HO
                            FourCircleButton(
                                icon = Icons.Default.Remove,
                                enabled = currentBetIndex > 0,
                                onClick = {
                                    val newIndex = (currentBetIndex - 1).coerceAtLeast(0)
                                    betAmount = bets[newIndex]
                                }
                            )

                            Spacer(Modifier.width(16.dp))

                            // 👇 BET BOX - RED GRADIENT
                            Box(
                                modifier = Modifier
                                    .width(150.dp).height(56.dp).clip(RoundedCornerShape(28.dp))
                                    .background(brush = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), shape = RoundedCornerShape(28.dp))
                                    .border(2.5.dp, if (hasEnoughBalance) Color(0xFFFFD700) else Color.Red, RoundedCornerShape(28.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$betAmount", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.size(22.dp).clip(CircleShape).background(color = Color(0xFFFFD700)))
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            FourCircleButton(
                                icon = Icons.Default.Add,
                                enabled = currentBetIndex < bets.size - 1,
                                onClick = {
                                    val newIndex = (currentBetIndex + 1).coerceAtMost(bets.size - 1)
                                    betAmount = bets[newIndex]
                                }
                            )
                        }

                        AnimatedVisibility(visible =!hasEnoughBalance) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Insufficient balance for this bet", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        Spacer(Modifier.height(36.dp))

                        // 👇 PLAY BUTTON - RED
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f).height(56.dp).clip(RoundedCornerShape(28.dp))
                                .background(
                                    brush = if (hasEnoughBalance) Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))
                                    else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f))),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
                                .clickable(enabled = hasEnoughBalance) { onPlayBet(betAmount) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (hasEnoughBalance) "PLAY" else "INSUFFICIENT BALANCE", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 2.sp, color = Color.White)
                        }
                    }
                }
            }

            // 👇 4 PLAYERS BADGE - GOLDEN
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter).offset(y = (-18).dp).clip(RoundedCornerShape(14.dp))
                    .background(brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), shape = RoundedCornerShape(14.dp))
                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            ) {
                Text("4 PLAYERS", color = Color(0xFF1A0000), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 1.5.sp)
            }

            // 👇 CLOSE BUTTON - RED
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp).size(32.dp).clip(CircleShape)
                    .background(brush = Brush.linearGradient(listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))), shape = CircleShape)
                    .border(2.5.dp, Color(0xFFFFD700), CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// 👇 NAAM UNIQUE RAKHA TA KE ERROR NA AYE
@Composable
fun FourCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, animationSpec = tween(100))

    Box(
        modifier = Modifier
            .size(44.dp).scale(scale).clip(CircleShape)
            .background(
                brush = if (enabled) Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                else Brush.linearGradient(listOf(Color.Gray, Color.Gray)),
                shape = CircleShape
            )
            .border(2.5.dp, Color(0xFFFFD700), CircleShape)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color(0xFF1A0000), modifier = Modifier.size(22.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun FourPlayerDialogPreview() {
    FourPlayerDialog(walletBalance = 2450)
}