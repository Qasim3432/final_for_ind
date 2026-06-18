package com.example.final_for_ind.screens.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = "wallet", onNavigate = onNavItemClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF00D4FF),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "My Wallet",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(Modifier.height(28.dp))


                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00D4FF).copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Total Balance",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "$balance",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Coins",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))

                    SectionTitle("Quick Actions")

                    Spacer(Modifier.height(16.dp))

                    ModernGradientButton(
                        text = "Add Coins by USDT",
                        icon = Icons.Default.CurrencyExchange,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF26A17B), Color(0xFF50AF95))),
                        onClick = onAddCoinsByUSDT
                    )

                    Spacer(Modifier.height(14.dp))

                    ModernGradientButton(
                        text = "Deposit Coins",
                        icon = Icons.Default.Download,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFFFA500), Color(0xFFFFD700))),
                        onClick = onDeposit
                    )

                    Spacer(Modifier.height(14.dp))

                    ModernGradientButton(
                        text = "Withdraw Coins",
                        icon = Icons.Default.Upload,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFC0392B), Color(0xFFE74C3C))),
                        onClick = onWithdraw
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun ModernGradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(balance = 1250)
}