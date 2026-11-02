package com.example.final_for_ind.screens.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.json.JSONObject

import com.example.final_for_ind.utils.formatDollar
import kotlin.math.abs


data class GameHistoryEntry(
    val gameId: String,
    val betAmount: Int,
    val result: String,
    val coinsChange: Int,
    val gameMode: String,
    val opponentCount: Int,
    val playedAt: String,
)


// ==========================================================
// PREMIUM COLOR PALETTE (Prefixed to avoid conflicts)
// ==========================================================

private val HistoryBgDark = Color(0xFF050505)
private val HistoryCardBgBase = Color(0xFF0F1410) // Very dark green tint
private val HistoryCardSurrenderBg = Color(0xFF1A0D12) // Very dark red tint

private val HistoryGoldPure = Color(0xFFFFD700)
private val HistoryGoldRich = Color(0xFFD4AF37)

private val HistoryWinAccent = Color(0xFF00E676) // Neon Green
private val HistoryLossAccent = Color(0xFFFF3366) // Neon Red/Pink
private val HistoryOrangeAccent = Color(0xFFFFA500)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    historyJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit
) {

    val history = remember(historyJson) {
        val list = mutableListOf<GameHistoryEntry>()
        val arr = historyJson?.optJSONArray("history")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    GameHistoryEntry(
                        gameId = o.optString("game_id", ""),
                        betAmount = o.optInt("bet_amount", 0),
                        result = o.optString("result", ""),
                        coinsChange = o.optInt("coins_change", 0),
                        gameMode = o.optString("game_mode", "2P"),
                        opponentCount = o.optInt("opponent_count", 1),
                        playedAt = o.optString("played_at", ""),
                    )
                )
            }
        }
        list
    }

    val stats = historyJson?.optJSONObject("stats")
    val totalGames = stats?.optInt("total_games", 0) ?: 0
    val totalWins = stats?.optInt("total_wins", 0) ?: 0
    val totalLosses = stats?.optInt("total_losses", 0) ?: 0
    val winRate = stats?.optDouble("win_rate", 0.0) ?: 0.0

    // Background with diagonal stripes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoryBgDark)
            .drawBehind {
                val stripeColor = Color.White.copy(alpha = 0.02f)
                val stripeWidth = 50.dp.toPx()
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

            // ==========================================
            // CUSTOM HEADER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HistoryGoldPure)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = HistoryGoldPure,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Game History",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = HistoryGoldPure
                )

                Spacer(modifier = Modifier.weight(1f))

                // Right side racing stripes decoration
                Canvas(modifier = Modifier.width(40.dp).height(20.dp)) {
                    drawLine(
                        color = HistoryGoldPure,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 6.dp.toPx()
                    )
                    drawLine(
                        color = HistoryWinAccent,
                        start = Offset(15.dp.toPx(), size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }

            // ==========================================
            // STATS CARD
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1114)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HistoryGoldRich)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Games",
                        value = "$totalGames",
                        icon = Icons.Default.SportsEsports,
                        color = Color.White
                    )
                    DividerVertical()
                    StatItem(
                        label = "Wins",
                        value = "$totalWins",
                        icon = Icons.Default.EmojiEvents,
                        color = HistoryWinAccent
                    )
                    DividerVertical()
                    StatItem(
                        label = "Losses",
                        value = "$totalLosses",
                        icon = Icons.Default.HeartBroken,
                        color = HistoryLossAccent
                    )
                    DividerVertical()
                    StatItem(
                        label = "Win %",
                        value = "$winRate%",
                        icon = Icons.Default.Percent,
                        color = HistoryGoldPure
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HistoryGoldPure)
                }
            } else if (history.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history) { entry ->
                        HistoryCard(entry = entry)
                    }
                    item { Spacer(Modifier.height(30.dp)) }
                }
            }
        }
    }
}


@Composable
fun DividerVertical() {
    Box(
        Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}


@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun HistoryCard(entry: GameHistoryEntry) {

    val (icon, resultColor, resultText) = when (entry.result) {
        "WON" -> Triple(Icons.Default.EmojiEvents, HistoryWinAccent, "WON")
        "LOST" -> Triple(Icons.Default.SentimentDissatisfied, HistoryLossAccent, "LOST")
        "SURRENDERED" -> Triple(Icons.Default.Flag, HistoryLossAccent, "SURRENDERED")
        "CANCELLED" -> Triple(Icons.Default.Cancel, HistoryOrangeAccent, "CANCELLED")
        else -> Triple(Icons.Default.History, Color.White, entry.result.ifBlank { "UNKNOWN" })
    }

    val cardBg = when (entry.result) {
        "WON" -> HistoryCardBgBase
        "LOST", "SURRENDERED" -> HistoryCardSurrenderBg
        else -> HistoryCardBgBase
    }

    val coinsColor = if (entry.coinsChange > 0) HistoryWinAccent else if (entry.coinsChange < 0) HistoryLossAccent else Color.White
    val absAmount = abs(entry.coinsChange)
    val coinsText = when {
        entry.coinsChange > 0 -> "+${formatDollar(absAmount)}"
        entry.coinsChange < 0 -> "-${formatDollar(absAmount)}"
        else -> formatDollar(0)
    }

    val opponentText = buildString {
        append(entry.opponentCount)
        append(" opponent")
        if (entry.opponentCount > 1) append("s")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 1.5.dp,
                color = resultColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Large Left Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(2.dp, resultColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = resultColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                // Row 1: Result Text + Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resultText,
                        color = resultColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HistoryGoldPure)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.gameMode,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Row 2: Bet amount & Opponent
                Text(
                    text = "Bet: ${formatDollar(entry.betAmount)}  •  $opponentText",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Row 3: Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF00B0FF), // Blue calendar icon
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.playedAt,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Side: Coins + Arrow
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = coinsText,
                    color = coinsColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF00B0FF), // Blue arrow
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = HistoryGoldPure.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "No games yet",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Play your first match to see history here.",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}