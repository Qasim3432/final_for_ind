package com.example.final_for_ind.screens.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.final_for_ind.utils.SoundManager
import com.example.final_for_ind.utils.formatDollar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class VersusPlayer(
    val name: String,
    val picUrl: String?,
    val isMe: Boolean,
    val color: String
)


@Composable
fun VersusScreen(
    players: List<VersusPlayer>,
    betAmount: Int,
    isWaitingForOpponent: Boolean,
    onCountdownEnd: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(3) }
    var showContent by remember { mutableStateOf(false) }
    var showCoinAnimation by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val gold = Color(0xFFFFD700)
    val deepBg = Color(0xFF0A0000)

    LaunchedEffect(Unit) {
        showContent = true
    }

    // ✅ Logic: Jab match mil jaye toh pehle coin animation chalayein, phir countdown start karein
    LaunchedEffect(isWaitingForOpponent) {
        if (!isWaitingForOpponent) {

            delay(400L) // Thoda wait taake UI update ho jaye
            showCoinAnimation = true

            // ✅ NEW: Yahan 'collect' wali sound bajegi
            SoundManager.playCollectSound()

            delay(1500L) // Coin animation ka time (1.5 seconds)
            showCoinAnimation = false

            // Ab countdown start karein
            while (countdown > 1) {
                delay(1000L)
                countdown--
                // ✅ Countdown Tick Sound
                SoundManager.playButtonClick()
            }
            delay(1000L)
            onCountdownEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF4A0000), deepBg),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isWaitingForOpponent) "WAITING FOR OPPONENT" else "MATCH FOUND",
                color = gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            if (players.size <= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (players.isNotEmpty()) {
                        ProductionPlayerCard(
                            player = players[0],
                            betAmount = betAmount,
                            modifier = Modifier
                                .weight(1f)
                                .scale(scaleAnim)
                        )
                    } else {
                        WaitingPlayerCard(modifier = Modifier.weight(1f).scale(scaleAnim))
                    }

                    Text(
                        text = "VS",
                        color = gold,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .scale(scaleAnim)
                    )

                    if (isWaitingForOpponent) {
                        WaitingPlayerCard(modifier = Modifier.weight(1f).scale(scaleAnim))
                    } else if (players.size > 1) {
                        ProductionPlayerCard(
                            player = players[1],
                            betAmount = betAmount,
                            modifier = Modifier
                                .weight(1f)
                                .scale(scaleAnim)
                        )
                    } else {
                        WaitingPlayerCard(modifier = Modifier.weight(1f).scale(scaleAnim))
                    }
                }
            } else {
                // 4 Player Mode Logic
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (players.isNotEmpty()) ProductionPlayerCard(player = players[0], betAmount = betAmount, modifier = Modifier.weight(1f).scale(scaleAnim)) else WaitingPlayerCard(modifier = Modifier.weight(1f))
                        if (players.size > 1) ProductionPlayerCard(player = players[1], betAmount = betAmount, modifier = Modifier.weight(1f).scale(scaleAnim)) else WaitingPlayerCard(modifier = Modifier.weight(1f))
                    }

                    Text(
                        text = "VS",
                        color = gold,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally).scale(scaleAnim)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (players.size > 2) ProductionPlayerCard(player = players[2], betAmount = betAmount, modifier = Modifier.weight(1f).scale(scaleAnim)) else WaitingPlayerCard(modifier = Modifier.weight(1f))
                        if (players.size > 3) ProductionPlayerCard(player = players[3], betAmount = betAmount, modifier = Modifier.weight(1f).scale(scaleAnim)) else WaitingPlayerCard(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(60.dp))

            if (!isWaitingForOpponent && !showCoinAnimation) {
                CircularProgressIndicator(
                    progress = { countdown / 3f },
                    modifier = Modifier.size(60.dp),
                    color = gold,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Starting in $countdown...",
                    color = gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            } else if (isWaitingForOpponent) {
                Text(
                    text = "Please wait, player is joining...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ✅ COIN FLY ANIMATION OVERLAY
        if (showCoinAnimation) {
            CoinFlyAnimation()
        }
    }
}


// ==========================================================
// ✅ COIN ANIMATION COMPOSABLE
// ==========================================================
@Composable
fun CoinFlyAnimation() {
    val offsetLeft = remember { Animatable(-120f) }
    val offsetRight = remember { Animatable(120f) }
    val alphaAnim = remember { Animatable(1f) }
    val scaleAnim = remember { Animatable(1.5f) }

    LaunchedEffect(Unit) {
        val spec = tween<Float>(1000, easing = LinearOutSlowInEasing)

        // Left and Right coins move towards center
        launch { offsetLeft.animateTo(0f, spec) }
        launch { offsetRight.animateTo(0f, spec) }
        launch { scaleAnim.animateTo(0.2f, spec) }

        // Fade out at the end
        alphaAnim.animateTo(0f, tween(300, delayMillis = 700))
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Left Coin (Player 1)
        Text(
            text = "🪙",
            fontSize = 40.sp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetLeft.value
                    this.alpha = alphaAnim.value
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
        )

        // Right Coin (Player 2)
        Text(
            text = "🪙",
            fontSize = 40.sp,
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetRight.value
                    this.alpha = alphaAnim.value
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
        )
    }
}


@Composable
fun ProductionPlayerCard(
    player: VersusPlayer,
    betAmount: Int,
    modifier: Modifier = Modifier
) {
    val gold = Color(0xFFFFD700)
    val borderColor = if (player.isMe) Color(0xFF4CAF50) else Color(0xFFF44336)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2B0000), Color.Black)
                        )
                    )
                    .border(3.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!player.picUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = player.picUrl,
                        contentDescription = player.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = player.name.take(1).uppercase(),
                        color = gold,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = 2.dp, y = 8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = gold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = player.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // ✅ Amount below player name
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatDollar(betAmount),
                color = gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun WaitingPlayerCard(modifier: Modifier = Modifier) {
    val gold = Color(0xFFFFD700)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .border(3.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = gold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Joining...",
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}


// ==========================================================
// WINNER SCREEN (with rematch banner — F2)
// ==========================================================

val WinBackground = Color(0xFF0F172A)
val WinAccent = Color(0xFF10B981)
val LoseBackground = Color(0xFF1F2937)
val LoseAccent = Color(0xFFF43F5E)
val RewardGold = Color(0xFFFFD700)


@Composable
fun WinnerScreen(
    didWin: Boolean,
    payout: Int = 0,
    showRematchRequest: Boolean = false,
    rematchRequesterName: String = "",
    onContinue: () -> Unit,
    onRematch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (didWin) WinBackground else LoseBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (didWin) RewardGold else LoseAccent,
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (didWin) "VICTORY!" else "GAME OVER",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (didWin) "You are the champion! Keep it up." else "Better luck next time!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            if (showRematchRequest && rematchRequesterName.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RewardGold.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    border = BorderStroke(2.dp, RewardGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "$rematchRequesterName wants a rematch!",
                                color = RewardGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Tap REMATCH below to accept",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (didWin) {
                Spacer(modifier = Modifier.height(35.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.background(
                        brush = Brush.horizontalGradient(listOf(RewardGold, Color(0xFFFFA500))),
                        shape = RoundedCornerShape(24.dp)
                    )
                ) {
                    Text(
                        text = "+${formatDollar(payout)}",
                        color = Color(0xFF3A2500),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    SoundManager.playButtonClick()
                    onRematch()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(RewardGold, Color(0xFFFFA500))),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text = "🔄 REMATCH",
                    fontSize = 20.sp,
                    color = Color(0xFF3A2500),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    SoundManager.playButtonClick()
                    onContinue()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            if (didWin) listOf(WinAccent, Color(0xFF059669)) else listOf(LoseAccent, Color(0xFFE11D48))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text = "CONTINUE",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VersusScreenPreview() {
    MaterialTheme {
        VersusScreen(
            players = listOf(
                VersusPlayer("You", null, true, "BLUE")
            ),
            betAmount = 250,
            isWaitingForOpponent = true,
            onCountdownEnd = {}
        )
    }
}