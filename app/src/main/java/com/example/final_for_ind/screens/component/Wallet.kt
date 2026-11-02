package com.example.final_for_ind.screens.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.network.TransactionLog
import com.example.final_for_ind.utils.formatDollar
import kotlin.math.abs

// ==========================================================
// THEME
// ==========================================================

private val BgDark = Color(0xFF140806)
private val BgDark2 = Color(0xFF1F0F0A)
private val Gold = Color(0xFFFFD700)
private val GoldLight = Color(0xFFFFE55C)
private val CardBg = Color(0xFF1E1210)
private val RedStart = Color(0xFF8B1A1A)
private val RedEnd = Color(0xFFE53935)

// ==========================================================
// ADAPTIVE FONT SIZE
// ==========================================================

fun adaptiveFontSize(value: Int): TextUnit {
    val digits = value.toString().length
    return when {
        digits <= 4 -> 22.sp
        digits == 5 -> 20.sp
        digits == 6 -> 18.sp
        digits == 7 -> 16.sp
        digits == 8 -> 14.sp
        digits == 9 -> 13.sp
        digits == 10 -> 12.sp
        else -> 11.sp
    }
}

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
    onWithdraw: () -> Unit = {},
    onBalanceChanged: () -> Unit = {}
) {
    var showHistorySection by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val sessionManager = remember { GameSessionManager(context) }

    val gradient = Brush.verticalGradient(
        colors = listOf(BgDark2, BgDark)
    )

    val scrollState = rememberScrollState()

    var depositBonusAmount by remember { mutableStateOf(10) }

    LaunchedEffect(Unit) {
        try {
            val config = sessionManager.fetchAppConfig()
            depositBonusAmount = config?.optInt("deposit_bonus_coins", 10) ?: 10
        } catch (_: Exception) { }
    }

    val hasValidCode = referralCode.isNotBlank()
            && referralCode != "Loading..."
            && referralCode != "ERROR"
            && referralCode != "NONE"
            && referralCode != "------"

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
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // MAIN WALLET CARD
            // ==========================================
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

                    // ==========================================
                    // BALANCE PANEL
                    // ==========================================
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
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "Available",
                                            color = Color.White.copy(0.9f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = formatDollar(balance), // ✅ Format to $
                                        color = Color.White,
                                        fontSize = adaptiveFontSize(balance),
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
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
                                        Icon(
                                            Icons.Default.Lock,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "In Match",
                                            color = Color.White.copy(0.9f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = formatDollar(lockedCoins), // ✅ Format to $
                                        color = Color.White,
                                        fontSize = adaptiveFontSize(lockedCoins),
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // REFERRAL CODE ROW
                    // ==========================================
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(enabled = hasValidCode) {
                                val clipboard = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                                ) as ClipboardManager

                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "Referral Code",
                                        referralCode
                                    )
                                )

                                Toast.makeText(
                                    context,
                                    "Referral Code Copied: $referralCode",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (hasValidCode)
                                        Color.LightGray
                                    else
                                        Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "My Referral Code",
                                    color = if (hasValidCode)
                                        Color.LightGray
                                    else
                                        Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = if (hasValidCode) referralCode else "------",
                                color = if (hasValidCode)
                                    Gold
                                else
                                    Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // ==========================================
                    // ✅ DEPOSIT BONUS INFO CARD
                    // ==========================================
                    Spacer(Modifier.height(16.dp))

                    DepositBonusInfoCard(bonusAmount = depositBonusAmount)

                    HorizontalDivider(
                        color = Gold.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Text(
                        text = "Quick Actions",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Start
                    )

                    GradientButton(
                        text = "Deposit Money",
                        icon = Icons.Default.Download,
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                        ),
                        onClick = onDeposit
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = "Withdraw Funds",
                        icon = Icons.Default.Upload,
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFF5D2A1A), Color(0xFF3E1F14))
                        ),
                        onClick = onWithdraw,
                        borderColor = Gold.copy(alpha = 0.3f)
                    )

                    Spacer(Modifier.height(12.dp))

                    GradientButton(
                        text = if (showHistorySection)
                            "Hide Transaction History"
                        else
                            "View Statement History",
                        icon = Icons.Default.History,
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                        ),
                        onClick = { showHistorySection = !showHistorySection }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ==========================================
            // TRANSACTION HISTORY
            // ==========================================
            if (showHistorySection) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
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
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                historyLogs.forEach { log ->
                                    TransactionItemRow(log)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}


// ==========================================================
// ✅ DEPOSIT BONUS INFO CARD
// ==========================================================

@Composable
fun DepositBonusInfoCard(bonusAmount: Int) {

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = Gold.copy(alpha = 0.6f),
                shape = RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C1A00)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("💎", fontSize = 28.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "DEPOSIT BONUS",
                        color = Gold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Deposit earns you bonus funds",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        Gold.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🎁", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Get ",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "+${formatDollar(bonusAmount)}", // ✅ Format bonus to $
                        color = Gold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        "  on deposit",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Auto-credited when your deposit is approved",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )
        }
    }
}


// ==========================================================
// TRANSACTION ROW
// ==========================================================

@Composable
fun TransactionItemRow(log: TransactionLog) {

    val (title, isCredit, icon) = when (log.type) {

        "DEPOSIT"                   -> Triple("Deposit",            true,  "📥")
        "WITHDRAWAL"                -> Triple("Withdrawal",         false, "📤")
        "WITHDRAWAL_REFUND"         -> Triple("Withdrawal Refund",  true,  "↩️")
        "DEPOSIT_BONUS"             -> Triple("Deposit Bonus",      true,  "💎")
        "DAILY_BONUS"               -> Triple("Daily Bonus",        true,  "🎁")
        "SPIN_COST"                 -> Triple("Spin Cost",          false, "🎰")
        "SPIN_REWARD"               -> Triple("Spin Reward",        true,  "🎉")
        "LEADERBOARD_REWARD"        -> Triple("Leaderboard Reward", true,  "🏆")
        "REFERRAL_BONUS"            -> Triple("Referral Bonus",     true,  "👥")
        "REFERRAL_INVITE_REWARD"    -> Triple("Invite Reward",      true,  "👥")
        "REFERRAL_COMMISSION"       -> Triple("Referral Commission",true,  "💸")
        "WAGER_ESCROW"              -> Triple("Match Entry",        false, "⚔️")
        "WAGER_PAYOUT"              -> Triple("Match Win",          true,  "🏅")
        "WAGER_REFUND"              -> Triple("Match Refund",       true,  "↩️")

        else -> Triple(
            log.type.replace("_", " "),
            log.amount > 0,
            "💰"
        )
    }

    val amountColor = if (isCredit) Color(0xFF2ECC71) else Color(0xFFE74C3C)

    // ✅ Format to Dollar
    val absAmount = abs(log.amount)
    val amountText = if (log.amount >= 0) "+${formatDollar(absAmount)}" else "-${formatDollar(absAmount)}"

    val statusColor = when (log.status.uppercase()) {
        "APPROVED", "SUCCESS" -> Color(0xFF2ECC71)
        "REJECTED", "FAILED"  -> Color(0xFFE74C3C)
        else                  -> Color(0xFFF1C40F)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = log.dateString,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountText,
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = log.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}


// ==========================================================
// GRADIENT BUTTON
// ==========================================================

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
            .then(
                if (borderColor != null)
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp))
                else
                    Modifier
            ),
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