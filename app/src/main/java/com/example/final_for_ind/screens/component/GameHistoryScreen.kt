package com.example.final_for_ind.screens.component


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.json.JSONObject


data class GameHistoryEntry(
    val gameId: String,
    val betAmount: Int,
    val result: String,
    val coinsChange: Int,
    val gameMode: String,
    val opponentCount: Int,
    val playedAt: String,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    historyJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )

    val gold = Color(0xFFFFD700)

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

    val stats =
        historyJson?.optJSONObject("stats")

    val totalGames =
        stats?.optInt("total_games", 0) ?: 0

    val totalWins =
        stats?.optInt("total_wins", 0) ?: 0

    val totalLosses =
        stats?.optInt("total_losses", 0) ?: 0

    val winRate =
        stats?.optDouble("win_rate", 0.0) ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Game History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.5.dp,
                        gold
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1A0000),
                                        Color(0xFF0A0A0A)
                                    )
                                )
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        StatItem(
                            label = "Games",
                            value = "$totalGames",
                            color = Color.White
                        )

                        StatItem(
                            label = "Wins",
                            value = "$totalWins",
                            color = Color(0xFF4CAF50)
                        )

                        StatItem(
                            label = "Losses",
                            value = "$totalLosses",
                            color = Color(0xFFE53935)
                        )

                        StatItem(
                            label = "Win %",
                            value = "$winRate%",
                            color = gold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isLoading) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = gold)
                    }

                } else if (history.isEmpty()) {

                    EmptyState()

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(history) { entry ->
                            HistoryCard(entry = entry, gold = gold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun HistoryCard(entry: GameHistoryEntry, gold: Color) {

    val (icon, resultColor, resultText) = when (entry.result) {

        "WON" -> Triple(
            Icons.Default.EmojiEvents,
            Color(0xFF4CAF50),
            "WON"
        )

        "LOST" -> Triple(
            Icons.Default.SentimentDissatisfied,
            Color(0xFFE53935),
            "LOST"
        )

        "CANCELLED" -> Triple(
            Icons.Default.Cancel,
            Color(0xFFFFA500),
            "CANCELLED"
        )

        else -> Triple(
            Icons.Default.History,
            Color.White,
            entry.result
        )
    }

    val coinsColor =
        if (entry.coinsChange > 0) Color(0xFF4CAF50)
        else if (entry.coinsChange < 0) Color(0xFFE53935)
        else Color.White.copy(alpha = 0.6f)

    val coinsText =
        if (entry.coinsChange > 0) "+${entry.coinsChange}"
        else "${entry.coinsChange}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0000)),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            resultColor.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(resultColor.copy(alpha = 0.15f))
                    .border(1.5.dp, resultColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = resultColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = resultText,
                        color = resultColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(gold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.gameMode,
                            color = gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Bet: ${entry.betAmount}  •  " +
                            "${entry.opponentCount} opponent" +
                            if (entry.opponentCount > 1) "s" else "",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = entry.playedAt,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            Text(
                text = coinsText,
                color = coinsColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
            tint = Color(0xFFFFD700).copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "No games yet",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Play your first match to see history here.",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}