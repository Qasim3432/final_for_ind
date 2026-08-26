package com.example.final_for_ind.screens.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    balance: Int = 0,
    onNavItemClick: (String) -> Unit = {},
    onAddCoinsByUSDT: () -> Unit = {},
    onDeposit: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    // 👇 BLACK RED GRADIENT
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = "wallet", onNavigate = onNavItemClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().background(bgGradient).padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f).padding(20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(28.dp)).padding(28.dp) // 👈 BLACK RED
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wallet, // 👈 Wallet icon better for wallet screen
                                contentDescription = null,
                                tint = Color(0xFFFFD700), // 👈 GOLDEN
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("My Wallet", color = Color(0xFFFFD700), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp) // 👈 GOLDEN
                        }

                        Spacer(Modifier.height(28.dp))

                        // BALANCE CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(2.dp, Color(0xFFFFD700)) // 👈 GOLDEN
                        ) {
                            Box(
                                modifier = Modifier.background(brush = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), shape = RoundedCornerShape(20.dp)).padding(horizontal = 32.dp, vertical = 20.dp) // 👈 RED
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp)) // 👈 GOLDEN
                                        Spacer(Modifier.width(10.dp))
                                        Text("Total Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, letterSpacing = 1.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("$balance", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(36.dp))

                        SectionTitle("Quick Actions")

                        Spacer(Modifier.height(16.dp))

                        PremiumGradientButton(
                            text = "Add Coins by USDT",
                            icon = Icons.Default.CurrencyExchange,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF26A17B), Color(0xFF50AF95))), // USDT Green same
                            onClick = onAddCoinsByUSDT
                        )

                        Spacer(Modifier.height(14.dp))

                        PremiumGradientButton(
                            text = "Deposit Coins",
                            icon = Icons.Default.Download,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), // 👈 GOLDEN
                            onClick = onDeposit
                        )

                        Spacer(Modifier.height(14.dp))

                        PremiumGradientButton(
                            text = "Withdraw Coins",
                            icon = Icons.Default.Upload,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), // 👈 RED
                            onClick = onWithdraw
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFFFFD700), // 👈 GOLDEN
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun PremiumGradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        border = BorderStroke(2.dp, Color(0xFFFFD700)) // 👈 GOLDEN BORDER
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(gradient, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = 0.3.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(balance = 1250)
}