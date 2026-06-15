package com.example.final_for_ind.screens.home_lobby

import androidx.compose.foundation.background
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
fun GameSetupDialog(onDismiss: () -> Unit = {}, onPlay: (String, Int) -> Unit = { _, _ -> }) {
    var selectedVariation by remember { mutableStateOf("CLASSIC") }
    var betAmount by remember { mutableStateOf(1000) }

    val bets = listOf(200, 500, 1000, 2000, 5000, 10000)
    val currentBetIndex = bets.indexOf(betAmount).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.width(320.dp)) {
            // Main card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1E8))


            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "SELECT VARIATION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B6B6B),
                        letterSpacing = 1.sp
                    )



                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "CHOOSE BET",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B6B6B),
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Bet selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Minus button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9B59B6))
                                .clickable {
                                    val newIndex = (currentBetIndex - 1).coerceAtLeast(0)
                                    betAmount = bets[newIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        // Bet amount
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(44.dp)
                                .background(Color(0xFF5D4037), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$betAmount",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700))
                                )
                            }
                        }

                        // Plus button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9B59B6))
                                .clickable {
                                    val newIndex = (currentBetIndex + 1).coerceAtMost(bets.size - 1)
                                    betAmount = bets[newIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Play button
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2ECC71),
                            contentColor = Color.White
                        )
                    ) {
                        Text("PLAY", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }


            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF7B25))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "4 PLAYERS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }

            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}