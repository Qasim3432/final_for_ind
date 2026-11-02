package com.example.final_for_ind.screens.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONObject


data class LeaderboardEntry(
    val rank: Int,
    val deviceToken: String,
    val displayName: String,
    val profilePic: String?,
    val wins: Int,
    val games: Int,
    val losses: Int,
    val earnings: Int,
    val winRate: Double,
)


data class PendingReward(
    val id: Int,
    val amount: Int,
    val rank: Int,
    val periodType: String,
    val periodKey: String,
)


// ==========================================================
// PREMIUM COLOR PALETTE (Matched from Image)
// ==========================================================

private val BgDark = Color(0xFF050505)
private val CardBgBase = Color(0xFF111111)
private val GoldPure = Color(0xFFFFD700)
private val GoldRich = Color(0xFFD4AF37)

// Rank 1 — Blue
private val Rank1CardBg = Color(0xFF0A1C3A)
private val Rank1Accent = Color(0xFF00A3FF)

// Rank 2 — Red/Maroon
private val Rank2CardBg = Color(0xFF3A0A0A)
private val Rank2Accent = Color(0xFFFF3366)

// Rank 3 — Gold/Brown
private val Rank3CardBg = Color(0xFF3A2A0A)
private val Rank3Accent = Color(0xFFFFB300)

// Rank 4+ — Green
private val Rank4CardBg = Color(0xFF0A2A12)
private val Rank4Accent = Color(0xFF00E676)

// Rewards banner
private val RewardBg = Color(0xFF3A1A00)
private val RewardBg2 = Color(0xFF1F0E00)
private val RewardAccent = Color(0xFFFFA000)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: (String) -> Unit = {},
    myDeviceToken: String = "",
    pendingRewards: List<PendingReward> = emptyList(),
    isClaimingReward: Boolean = false,
    onCollectReward: (Int) -> Unit = {}
) {

    var selectedPeriod by remember { mutableStateOf("weekly") }

    // Countdown Logic
    val periodEndsAtMs: Long =
        remember(leaderboardJson, selectedPeriod) {
            val iso = leaderboardJson?.optString("period_ends_at", "")
            if (iso.isNullOrBlank() || iso == "null") 0L else parseIsoToMillis(iso)
        }

    var countdownText by remember { mutableStateOf("") }

    LaunchedEffect(selectedPeriod, periodEndsAtMs) {
        if (selectedPeriod == "alltime" || periodEndsAtMs <= 0L) {
            countdownText = ""
            return@LaunchedEffect
        }
        while (true) {
            val diff = periodEndsAtMs - System.currentTimeMillis()
            countdownText = if (diff <= 0L) "Ending..." else formatCountdown(diff)
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Parse Data
    val topPlayers = remember(leaderboardJson) {
        val list = mutableListOf<LeaderboardEntry>()
        val arr = leaderboardJson?.optJSONArray("top_players")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    LeaderboardEntry(
                        rank = o.optInt("rank", 0),
                        deviceToken = o.optString("device_token", ""),
                        displayName = o.optString("display_name", "Player"),
                        profilePic = o.optString("profile_pic", "").takeIf { it.isNotBlank() && it != "null" },
                        wins = o.optInt("wins", 0),
                        games = o.optInt("games", 0),
                        losses = o.optInt("losses", 0),
                        earnings = o.optInt("earnings", 0),
                        winRate = o.optDouble("win_rate", 0.0),
                    )
                )
            }
        }
        list
    }

    val myRankJson = leaderboardJson?.optJSONObject("my_rank")
    val myRank = myRankJson?.optInt("rank", 0) ?: 0
    val myWins = myRankJson?.optInt("wins", 0) ?: 0
    val myLosses = myRankJson?.optInt("losses", 0) ?: 0
    val myEarnings = myRankJson?.optInt("earnings", 0) ?: 0
    val myWinRate = myRankJson?.optDouble("win_rate", 0.0) ?: 0.0

    val totalPending = pendingRewards.sumOf { it.amount }

    // Background with diagonal stripes and crown watermark
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .drawBehind {
                // Draw diagonal racing stripes
                val stripeColor = Color.White.copy(alpha = 0.03f)
                val stripeWidth = 40.dp.toPx()
                for (i in 0..20) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x = -size.height + (i * stripeWidth * 2), y = 0f),
                        end = Offset(x = 0f + (i * stripeWidth * 2), y = size.height),
                        strokeWidth = stripeWidth
                    )
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPure)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldPure,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Leaderboard",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PeriodTab(
                    label = "Weekly",
                    icon = Icons.Default.DateRange,
                    isSelected = selectedPeriod == "weekly",
                    onClick = {
                        selectedPeriod = "weekly"
                        onRefresh("weekly")
                    },
                    modifier = Modifier.weight(1f)
                )
                PeriodTab(
                    label = "Monthly",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedPeriod == "monthly",
                    onClick = {
                        selectedPeriod = "monthly"
                        onRefresh("monthly")
                    },
                    modifier = Modifier.weight(1f)
                )
                PeriodTab(
                    label = "All Time",
                    icon = Icons.Default.AccessTime,
                    isSelected = selectedPeriod == "alltime",
                    onClick = {
                        selectedPeriod = "alltime"
                        onRefresh("alltime")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Countdown Chip
            if (countdownText.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black)
                            .border(1.dp, GoldRich.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = GoldPure, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when (selectedPeriod) {
                                "weekly" -> "Week ends in "
                                "monthly" -> "Month ends in "
                                else -> ""
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = countdownText,
                            color = GoldPure,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pending Rewards (Hidden if empty)
            if (pendingRewards.isNotEmpty()) {
                PendingRewardsBanner(
                    rewards = pendingRewards,
                    totalPending = totalPending,
                    isClaiming = isClaimingReward,
                    onCollect = onCollectReward
                )
                Spacer(Modifier.height(16.dp))
            }

            // List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPure)
                }
            } else if (topPlayers.isEmpty()) {
                EmptyLeaderboard(GoldPure)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topPlayers) { entry ->
                        LeaderboardRow(
                            entry = entry,
                            isMe = entry.deviceToken == myDeviceToken
                        )
                    }
                    item { Spacer(Modifier.height(100.dp)) } // Space for bottom banner
                }
            }
        }

        // Bottom My Rank Banner
        if (myRank > 0 && !isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                MyRankBanner(
                    myRank = myRank,
                    myWins = myWins,
                    myLosses = myLosses,
                    myWinRate = myWinRate,
                    myEarnings = myEarnings
                )
            }
        }
    }
}


// ==========================================================
// CUSTOM UI COMPONENTS
// ==========================================================

@Composable
fun PeriodTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) GoldPure else Color.Transparent
    val borderColor = if (isSelected) GoldPure else Color.White.copy(alpha = 0.2f)
    val contentColor = if (isSelected) Color.Black else Color.White

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}


@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    isMe: Boolean = false
) {
    val (cardBg, accentColor) = when (entry.rank) {
        1 -> Rank1CardBg to Rank1Accent
        2 -> Rank2CardBg to Rank2Accent
        3 -> Rank3CardBg to Rank3Accent
        else -> Rank4CardBg to Rank4Accent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isMe) 2.dp else 1.5.dp,
                color = if (isMe) GoldPure else accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                // Draw faint diagonal stripes on card
                val stripeColor = Color.White.copy(alpha = 0.03f)
                val stripeWidth = 25.dp.toPx()
                for (i in 0..10) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x = size.width - (i * stripeWidth * 2), y = 0f),
                        end = Offset(x = size.width, y = size.height),
                        strokeWidth = stripeWidth
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Graphics (Crowns for top 3, Hexagon for others)
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (entry.rank <= 3) {
                    val crownColor = when(entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFE0E0E0) // Silver
                        else -> Color(0xFFCD7F32) // Bronze
                    }
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium, // Close to crown/shield
                        contentDescription = null,
                        tint = crownColor,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${entry.rank}",
                        color = if (entry.rank == 2) Color.Black else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    // Hexagon shape for rank 4+
                    Canvas(modifier = Modifier.size(40.dp)) {
                        val path = Path().apply {
                            val w = size.width
                            val h = size.height
                            moveTo(w / 2, 0f)
                            lineTo(w, h / 4)
                            lineTo(w, 3 * h / 4)
                            lineTo(w / 2, h)
                            lineTo(0f, 3 * h / 4)
                            lineTo(0f, h / 4)
                            close()
                        }
                        drawPath(path = path, color = accentColor.copy(alpha = 0.2f))
                        drawPath(path = path, color = accentColor, style = Stroke(width = 2.dp.toPx()))
                    }
                    Text(
                        text = "#${entry.rank}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CardBgBase)
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!entry.profilePic.isNullOrBlank()) {
                    AsyncImage(
                        model = entry.profilePic,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = accentColor)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isMe) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPure)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("YOU", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${entry.wins}W • ${entry.losses}L • ${entry.winRate}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "🪙 ${entry.earnings} coins",
                    color = Color(0xFF4CAF50), // Green coin text
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(8.dp))

            // Wins Big Number
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.wins}",
                    color = accentColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "wins",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun MyRankBanner(
    myRank: Int,
    myWins: Int,
    myLosses: Int,
    myWinRate: Double,
    myEarnings: Int
) {
    val purpleBg = Color(0xFF1A0B2E)
    val purpleAccent = Color(0xFF9D4EDD)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(purpleBg)
            .border(2.dp, purpleAccent, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GoldPure)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$myRank",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YOUR RANK",
                    color = GoldPure,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$myWins W • $myLosses L • $myWinRate%",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Earnings: $myEarnings coins",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Target Icon
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = Color(0xFFFF3366),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}


// ==========================================================
// REMAINING HELPERS (Rewards, Empty State, Timer)
// ==========================================================

@Composable
private fun PendingRewardsBanner(
    rewards: List<PendingReward>,
    totalPending: Int,
    isClaiming: Boolean,
    onCollect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(listOf(RewardBg, RewardBg2))
            )
            .border(2.dp, RewardAccent, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(RewardAccent.copy(alpha = 0.2f))
                    .border(1.5.dp, RewardAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = RewardAccent)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Unclaimed Rewards", color = GoldPure, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text("$totalPending coins pending", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Text("🪙", fontSize = 26.sp)
        }

        Spacer(Modifier.height(14.dp))

        rewards.forEach { reward ->
            val medalEmoji = when (reward.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "🏅"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(medalEmoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rank #${reward.rank} — ${reward.periodType}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text("+${reward.amount}", color = GoldPure, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isClaiming) Color.Gray else GoldPure)
                        .clickable(enabled = !isClaiming) { onCollect(reward.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(if (isClaiming) "..." else "Collect", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun EmptyLeaderboard(gold: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = gold.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(20.dp))
        Text("No rankings yet", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private fun parseIsoToMillis(iso: String): Long {
    return try {
        java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            java.time.LocalDateTime.parse(iso.substringBefore("+").substringBefore("Z"))
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) { 0L }
    }
}

private fun formatCountdown(diffMs: Long): String {
    val totalSec = diffMs / 1000L
    val days = totalSec / 86400L
    val hours = (totalSec % 86400L) / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L
    return when {
        days > 0L -> "${days}d ${hours}h ${minutes}m"
        hours > 0L -> "${hours}h ${minutes}m ${seconds}s"
        else -> "${minutes}m ${seconds}s"
    }
}