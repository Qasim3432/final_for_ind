package com.example.final_for_ind.screens.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.network.TransactionLog

// Theme from screenshot
private val BgDark = Color(0xFF140806)
private val BgDark2 = Color(0xFF1F0F0A)
private val Gold = Color(0xFFFFD700)
private val GoldLight = Color(0xFFFFE55C)
private val CardBg = Color(0xFF1E1210)
private val RedStart = Color(0xFF8B1A1A)
private val RedEnd = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    balance: Int = 0,
    lockedCoins: Int = 0,
    selectedNavIndex: Int = 1,
    referralCode: String = "Loading...",
    historyLogs: List<TransactionLog> = emptyList(),
    onNavItemClick: (Int) -> Unit = {},
    onDeposit: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    var showHistorySection by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val gradient = Brush.verticalGradient(
        colors = listOf(BgDark2, BgDark)
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedNavIndex,
                onItemClick = onNavItemClick
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- MAIN WALLET CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .border(1.dp, Gold.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "My Wallet",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // --- TWO-COLUMN BALANCE PANEL - Red gradient like screenshot ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.horizontalGradient(listOf(RedStart, RedEnd)))
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Text("Available", color = Color.White.copy(0.9f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = "$$balance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.horizontalGradient(listOf(RedStart, RedEnd)))
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("In Match", color = Color.White.copy(0.9f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(text = "$$lockedCoins", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    // --- REFERRAL CODE ROW ---
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable {
                                if (referralCode != "Loading..." && referralCode != "ERROR") {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Ludo Referral Code", referralCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Referral Code Copied: $referralCode", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("My Referral Code", color = Color.LightGray, fontSize = 13.sp)
                            }
                            Text(
                                text = referralCode,
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Gold.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Text(
                        text = "Quick Actions",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        textAlign = TextAlign.Start
                    )

                    GradientButton(
                        text = "Deposit Money",
                        icon = Icons.Default.Download,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
                        onClick = onDeposit
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = "Withdraw Funds",
                        icon = Icons.Default.Upload,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF5D2A1A), Color(0xFF3E1F14))),
                        onClick = onWithdraw,
                        borderColor = Gold.copy(alpha = 0.3f)
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = if (showHistorySection) "Hide Transaction History" else "View Statement History",
                        icon = Icons.Default.History,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1))),
                        onClick = { showHistorySection = !showHistorySection }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showHistorySection,
                modifier = Modifier.fillMaxWidth(0.94f)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recent Transactions Ledger",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (historyLogs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recorded transactions found.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(historyLogs) { log ->
                                    TransactionItemRow(log)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(log: TransactionLog) {
    val isDeposit = log.type == "DEPOSIT"
    val statusColor = when (log.status) {
        "APPROVED" -> Color(0xFF2ECC71)
        "REJECTED" -> Color(0xFFE74C3C)
        else -> Color(0xFFF1C40F)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isDeposit) "Deposit Request" else "Withdrawal Request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = log.dateString,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isDeposit) "+" else "-"}${log.amount}",
                    color = if (isDeposit) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = log.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    borderColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp)) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}