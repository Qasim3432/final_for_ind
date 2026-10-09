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


// ==========================================================
// PREMIUM COLOR PALETTE
// ==========================================================

private val BgScreenTop = Color(0xFF0A0505)
private val BgScreenBottom = Color(0xFF150808)
private val CardDeep = Color(0xFF0F0707)
private val CardSecondary = Color(0xFF1A1A1D)
private val GoldPure = Color(0xFFFFD700)
private val GoldRich = Color(0xFFD4AF37)
private val GoldDim = Color(0xFF8B6914)
private val EmeraldSolid = Color(0xFF1B5E20)
private val EmeraldBright = Color(0xFF43A047)

// ==========================================================
// RANK CARD COLORS
// ==========================================================

// Rank 1 — Blue
private val Rank1CardBg = Color(0xFF0D2B5C)
private val Rank1Accent = Color(0xFF42A5F5)

// Rank 2 — Red
private val Rank2CardBg = Color(0xFF4A0A0A)
private val Rank2Accent = Color(0xFFEF5350)

// Rank 3 — Yellow
private val Rank3CardBg = Color(0xFF4A3A00)
private val Rank3Accent = Color(0xFFFFCA28)

// Rank 4+ — Green
private val Rank4CardBg = Color(0xFF0F3D18)
private val Rank4Accent = Color(0xFF66BB6A)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: (String) -> Unit = {}
) {

    val screenGradient = Brush.verticalGradient(
        colors = listOf(BgScreenTop, BgScreenBottom)
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
                        profilePic = o.optString("profile_pic", "")
                            .takeIf { it.isNotBlank() && it != "null" },
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
                            tint = GoldPure,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Leaderboard",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = GoldPure
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPure
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
                .background(screenGradient)
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
                        onClick = {
                            selectedPeriod = "weekly"
                            onRefresh("weekly")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PeriodTab(
                        label = "Monthly",
                        isSelected = selectedPeriod == "monthly",
                        onClick = {
                            selectedPeriod = "monthly"
                            onRefresh("monthly")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PeriodTab(
                        label = "All Time",
                        isSelected = selectedPeriod == "alltime",
                        onClick = {
                            selectedPeriod = "alltime"
                            onRefresh("alltime")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (isLoading) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldPure)
                    }

                } else if (topPlayers.isEmpty()) {

                    EmptyLeaderboard(GoldPure)

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
                            LeaderboardRow(entry = entry)
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

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldSolid
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        GoldRich
                    )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldPure)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "#$myRank",
                                color = Color(0xFF1A0A0A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                "YOUR RANK",
                                color = GoldPure,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                "$myWins wins • $myGames games • $myWinRate%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "🎯",
                            fontSize = 28.sp
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) GoldPure else CardDeep
            )
            .border(
                1.5.dp,
                if (isSelected) GoldPure else GoldDim.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) Color(0xFF1A0A0A) else Color.White,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}


// ==========================================================
// LEADERBOARD ROW
// ==========================================================

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {

    // ==========================================
    // Rank-based colors
    // 1 → Blue, 2 → Red, 3 → Yellow, 4+ → Green
    // ==========================================

    val (cardBg, accentColor) = when (entry.rank) {
        1 -> Rank1CardBg to Rank1Accent
        2 -> Rank2CardBg to Rank2Accent
        3 -> Rank3CardBg to Rank3Accent
        else -> Rank4CardBg to Rank4Accent
    }

    val medalEmoji = when (entry.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            accentColor
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Rank badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f))
                    .border(
                        1.5.dp,
                        accentColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (entry.rank <= 3) {
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

            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1D))
                    .border(
                        1.5.dp,
                        accentColor,
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
                        tint = GoldPure,
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

                Spacer(Modifier.height(3.dp))

                Text(
                    "${entry.wins}W • ${entry.losses}L • ${entry.winRate}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${entry.wins}",
                    color = GoldPure,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "wins",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
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