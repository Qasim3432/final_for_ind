package com.example.final_for_ind.screens.home_lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun GameSetupDialog(
    headerTitle: String = "2 PLAYERS",
    onDismiss: () -> Unit = {},
    onPlay: (Int) -> Unit = {}
) {
    var betAmount by remember { mutableStateOf(1000) }
    val bets = listOf(500, 1000, 2000, 5000, 10000)
    val currentBetIndex = bets.indexOf(betAmount).coerceAtLeast(0)

    // THEME COLORS
    val bgOverlay = Color.Black.copy(alpha = 0.8f)
    val cardBg = Color(0xFF1C1C1C)
    val gold = Color(0xFFFFD700)
    val red = Color(0xFFD32F2F)
    val darkCard = Color(0xFF2A2A2A)
    val textGray = Color(0xFFB0B0B0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgOverlay),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.width(320.dp).clickable(enabled = false) {}) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)) // 🟡 Golden border
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "CHOOSE MATCH WAGER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textGray,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Bet selector Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // - Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(darkCard)
                                .border(1.dp, gold, CircleShape) // 🟡 Golden border
                                .clickable {
                                    val newIndex = (currentBetIndex - 1).coerceAtLeast(0)
                                    betAmount = bets[newIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, null, tint = gold, modifier = Modifier.size(20.dp))
                        }

                        Spacer(Modifier.width(12.dp))

                        // Amount Box
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(44.dp)
                                .background(darkCard, RoundedCornerShape(22.dp))
                                .border(1.dp, gold, RoundedCornerShape(22.dp)), // 🟡 Golden border
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$betAmount",
                                    color = gold, // 🟡 Golden text
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(gold)) // 🟡 Golden coin
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // + Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(darkCard)
                                .border(1.dp, gold, CircleShape) // 🟡 Golden border
                                .clickable {
                                    val newIndex = (currentBetIndex + 1).coerceAtMost(bets.size - 1)
                                    betAmount = bets[newIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = gold, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Play Button
                    Button(
                        onClick = { onPlay(betAmount) },
                        modifier = Modifier.fillMaxWidth(0.9f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold, // 🟡 Golden Button
                            contentColor = Color.Black
                        )
                    ) {
                        Text("PLAY MATCH", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // Top Header Ribbon Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(red) // 🔴 Red Header
                    .border(1.dp, gold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }

            // Close button icon
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(red) // 🔴 Red Close
                    .border(1.dp, gold, CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}