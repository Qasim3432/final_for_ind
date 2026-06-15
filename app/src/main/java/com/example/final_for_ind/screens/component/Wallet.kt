package com.example.final_for_ind.screens.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CurrencyExchange // USDT icon
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Upload
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
    onAddCoinsByUSDT: () -> Unit = {}, // naam change
    onDeposit: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "wallet",
                onNavigate = onNavItemClick
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF34495E))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF9B59B6),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Wallet",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFFF1C40F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Balance: $$balance",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    GradientButton(
                        text = "Add Coins by USDT", // text change
                        icon = Icons.Default.CurrencyExchange, // USDT icon
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF26A17B), Color(0xFF50AF95))), // USDT green gradient
                        onClick = onAddCoinsByUSDT
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = "Deposit",
                        icon = Icons.Default.Download,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFF39C12), Color(0xFFF1C40F))),
                        onClick = onDeposit
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = "Withdraw",
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
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(balance = 1250)
}