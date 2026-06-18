package com.example.final_for_ind.screens.home_lobby

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupDialog(onDismiss: () -> Unit = {}, onPlay: (String, Int) -> Unit = { _, _ -> }) {
    var selectedVariation by remember { mutableStateOf("CLASSIC") }
    var betAmount by remember { mutableStateOf(1000) }

    val bets = listOf(200, 500, 1000, 2000, 5000, 10000)
    val currentBetIndex = bets.indexOf(betAmount).coerceAtLeast(0)
    val variations = listOf("CLASSIC", "AK47", "JOKER", "MUFLIS")

    val overlayGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027).copy(alpha = 0.9f), Color(0xFF203A43).copy(alpha = 0.9f))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayGradient),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.width(340.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(32.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                        .padding(vertical = 32.dp, horizontal = 28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(8.dp))

                        Text(
                            "SELECT VARIATION", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            variations.forEach { variation ->
                                VariationChip(
                                    text = variation,
                                    isSelected = selectedVariation == variation,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedVariation = variation }
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            "CHOOSE BET", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            PremiumCircleButton(
                                icon = Icons.Default.Remove,
                                enabled = currentBetIndex > 0,
                                onClick = {
                                    val newIndex = (currentBetIndex - 1).coerceAtLeast(0)
                                    betAmount = bets[newIndex]
                                }
                            )

                            Spacer(Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF1A252F),
                                                Color(0xFF2C3E50)
                                            )
                                        ),
                                        shape = RoundedCornerShape(28.dp)
                                    )
                                    .border(
                                        2.dp,
                                        Color(0xFFFFD700).copy(alpha = 0.5f),
                                        RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "$betAmount", color = Color(0xFFFFD700), fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            PremiumCircleButton(
                                icon = Icons.Default.Add,
                                enabled = currentBetIndex < bets.size - 1,
                                onClick = {
                                    val newIndex = (currentBetIndex + 1).coerceAtMost(bets.size - 1)
                                    betAmount = bets[newIndex]
                                }
                            )
                        }

                        Spacer(Modifier.height(36.dp))

                        val playInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF38ef7d),
                                            Color(0xFF11998e)
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .border(
                                    2.dp,
                                    Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(28.dp)
                                )
                                .clickable(
                                    interactionSource = playInteraction,
                                    indication = null
                                ) { onPlay(selectedVariation, betAmount) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PLAY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-18).dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF7B25),
                                Color(0xFFFFA500)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            ) {
                Text(
                    "4 PLAYERS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            }

            val closeInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color = Color(0xFFFF5252), shape = CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(
                        interactionSource = closeInteraction,
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun VariationChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "chip_scale"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (isSelected)
                    Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700)))
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                if (isSelected) 2.5.dp else 1.5.dp,
                if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) Color(0xFF1A252F) else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PremiumCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = if (enabled)
                    Brush.radialGradient(listOf(Color(0xFF9B59B6), Color(0xFF8E44AD)))
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.Gray.copy(alpha = 0.3f),
                            Color.Gray.copy(alpha = 0.3f)
                        )
                    ),
                shape = CircleShape
            )
            .border(2.dp, Color.White.copy(alpha = if (enabled) 0.3f else 0.1f), CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GameSetupDialogPreview() {
    GameSetupDialog()
}