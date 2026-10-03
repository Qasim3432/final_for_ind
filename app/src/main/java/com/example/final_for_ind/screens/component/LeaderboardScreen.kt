package com.example.final_for_ind.screens.component


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: (String) -> Unit = {}
) {

    val gold = Color(0xFFFFD700)
    val darkRed = Color(0xFF2B0000)

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )

    var selectedPeriod by remember {
        mutableStateOf("alltime")
    }

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
    val myGames = myRankJson?.optInt("games", 0) ?: 0
    val myWinRate = myRankJson?.optDouble("win_rate", 0.0) ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Leaderboard",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = gold
                        )
                    }
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

                // ==========================================
                // Period Tabs
                // ==========================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    PeriodTab(
                        label = "Weekly",
                        isSelected = selectedPeriod == "weekly",
                        gold = gold,
                        onClick = {
                            selectedPeriod = "weekly"
                            onRefresh("weekly")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PeriodTab(
                        label = "Monthly",
                        isSelected = selectedPeriod == "monthly",
                        gold = gold,
                        onClick = {
                            selectedPeriod = "monthly"
                            onRefresh("monthly")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PeriodTab(
                        label = "All Time",
                        isSelected = selectedPeriod == "alltime",
                        gold = gold,
                        onClick = {
                            selectedPeriod = "alltime"
                            onRefresh("alltime")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ==========================================
                // Loading / Empty / List
                // ==========================================

                if (isLoading) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = gold)
                    }

                } else if (topPlayers.isEmpty()) {

                    EmptyLeaderboard(gold)

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(topPlayers) { entry ->
                            LeaderboardRow(
                                entry = entry,
                                gold = gold,
                                darkRed = darkRed
                            )
                        }

                        item {
                            Spacer(Modifier.height(90.dp))
                        }
                    }
                }
            }

            // ==========================================
            // My Rank — floating bottom card
            // ==========================================

            if (myRank > 0 && !isLoading) {

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                            )
                        )
                        .border(2.dp, gold, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(gold)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "#$myRank",
                                color = darkRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                "Your Rank",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                "$myWins wins • $myGames games • $myWinRate%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "🎯",
                            fontSize = 26.sp
                        )
                    }
                }
            }
        }
    }
}


// ==========================================================
// PERIOD TAB
// ==========================================================

@Composable
fun PeriodTab(
    label: String,
    isSelected: Boolean,
    gold: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected)
                    Brush.horizontalGradient(
                        listOf(gold, Color(0xFFD4AF37))
                    )
                else
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A0000), Color(0xFF1A0000))
                    )
            )
            .border(
                1.5.dp,
                if (isSelected) gold else gold.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) Color(0xFF1A0000) else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}


// ==========================================================
// LEADERBOARD ROW
// ==========================================================

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    gold: Color,
    darkRed: Color
) {

    val isTop3 = entry.rank <= 3

    val (medalColor, medalEmoji) = when (entry.rank) {
        1 -> Color(0xFFFFD700) to "🥇"
        2 -> Color(0xFFC0C0C0) to "🥈"
        3 -> Color(0xFFCD7F32) to "🥉"
        else -> Color.Transparent to ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTop3)
                medalColor.copy(alpha = 0.10f)
            else
                Color(0xFF1A0000)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isTop3) medalColor else gold.copy(alpha = 0.25f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTop3)
                            medalColor.copy(alpha = 0.25f)
                        else
                            Color(0xFF2B0000)
                    )
                    .border(
                        1.5.dp,
                        if (isTop3) medalColor else gold.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isTop3) {
                    Text(medalEmoji, fontSize = 22.sp)
                } else {
                    Text(
                        "#${entry.rank}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B0000))
                    .border(
                        1.5.dp,
                        if (isTop3) medalColor else gold.copy(alpha = 0.5f),
                        CircleShape
                    ),
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = gold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    entry.displayName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    "${entry.wins}W • ${entry.losses}L • ${entry.winRate}%",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${entry.wins}",
                    color = gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "wins",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


// ==========================================================
// EMPTY STATE
// ==========================================================

@Composable
fun EmptyLeaderboard(gold: Color) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = gold.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "No rankings yet",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Play matches to appear on the leaderboard.",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}