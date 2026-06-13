package com.example.final_for_ind.screens.component

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first // <-- YE ADD KARO
import kotlinx.coroutines.launch
import kotlin.random.Random

val Context.spinDataStore by preferencesDataStore(name = "spin_prefs")
private val LAST_SPIN_KEY = longPreferencesKey("last_spin_time")

data class SpinGift(
    val name: String,
    val emoji: String,
    val coinValue: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftSpinScreen(
    currentCoins: Int = 1250,
    paidSpinCost: Int = 30,
    onBack: () -> Unit = {},
    onRewardClaimed: (coins: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
    )

    val gifts = listOf(
        SpinGift("10 Coins", "🪙", 10, Color(0xFFFF5252)),
        SpinGift("20 Coins", "💰", 20, Color(0xFFFFD700)),
        SpinGift("Rose", "🌹", 0, Color(0xFFFF1744)),
        SpinGift("30 Coins", "💎", 30, Color(0xFF4481EB)),
        SpinGift("Try Again", "😅", 0, Color(0xFF9E9E9E)),
        SpinGift("40 Coins", "🪙", 40, Color(0xFF4CAF50)),
        SpinGift("Crown", "👑", 0, Color(0xFFFFA500)),
        SpinGift("50 Coins", "💰", 50, Color(0xFF9C27B0))
    )

    var coins by remember { mutableStateOf(currentCoins) }
    var isSpinning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var wonGift by remember { mutableStateOf<SpinGift?>(null) }
    var spinData by remember { mutableStateOf(SpinData(true, 0L)) }

    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        spinData = getSpinData(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lucky Spin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9B59B6).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("$coins Coins", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (spinData.canUseFreeSpin)
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else
                        Color(0xFFFFA500).copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (spinData.canUseFreeSpin) {
                        Text("FREE SPIN AVAILABLE!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        val hoursLeft = spinData.timeLeft / (60 * 60 * 1000)
                        val minutesLeft = (spinData.timeLeft % (60 * 60 * 1000)) / (60 * 1000)
                        Text("Next FREE spin in: ${hoursLeft}h ${minutesLeft}m", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Or pay $paidSpinCost coins to spin now", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(40.dp).align(Alignment.TopCenter)) {
                    val path = Path().apply {
                        moveTo(size.width / 2, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, Color(0xFFFFD700))
                }

                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(animatedRotation.value)
                ) {
                    val sweepAngle = 360f / gifts.size
                    gifts.forEachIndexed { index, gift ->
                        val startAngle = index * sweepAngle - 90f
                        drawArc(
                            color = gift.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            style = Stroke(width = 3.dp.toPx()),
                            size = Size(size.width, size.height)
                        )
                    }
                }

                Canvas(modifier = Modifier.size(80.dp)) {
                    drawCircle(Color(0xFF2C3E50))
                    drawCircle(Color(0xFFFFD700), style = Stroke(width = 4.dp.toPx()))
                }

                Text("SPIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    val cost = if (spinData.canUseFreeSpin) 0 else paidSpinCost
                    if (coins >= cost &&!isSpinning) { // <-- brackets fix
                        isSpinning = true

                        if (cost > 0) coins -= cost

                        val targetIndex = Random.nextInt(gifts.size)
                        val sweepAngle = 360f / gifts.size
                        val targetRotation = 360f * 5 + (360f - targetIndex * sweepAngle - sweepAngle / 2)

                        scope.launch {
                            animatedRotation.animateTo(
                                targetValue = targetRotation,
                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                            )
                            wonGift = gifts[targetIndex]
                            showResult = true
                            isSpinning = false

                            if (spinData.canUseFreeSpin) {
                                markFreeSpinUsed(context)
                            }
                            spinData = getSpinData(context)

                            if (wonGift!!.coinValue > 0) {
                                coins += wonGift!!.coinValue
                                onRewardClaimed(wonGift!!.coinValue)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = coins >= (if (spinData.canUseFreeSpin) 0 else paidSpinCost) &&!isSpinning, // <-- brackets fix
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (spinData.canUseFreeSpin) Color(0xFF4CAF50) else Color(0xFFFFD700),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isSpinning) "Spinning..."
                    else if (spinData.canUseFreeSpin) "FREE SPIN"
                    else "SPIN - $paidSpinCost Coins",
                    color = if (spinData.canUseFreeSpin) Color.White else Color(0xFF1A252F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            if (coins < (if (spinData.canUseFreeSpin) 0 else paidSpinCost)) { // <-- brackets fix
                Spacer(Modifier.height(8.dp))
                Text("Not enough coins!", color = Color.Red, fontSize = 12.sp)
            }
        }
    }

    if (showResult && wonGift!= null) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            containerColor = Color(0xFF2C3E50),
            title = {
                Text(
                    if (wonGift!!.coinValue > 0) "Congratulations! 🎉" else "Better Luck Next Time!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = wonGift!!.emoji, fontSize = 64.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(text = wonGift!!.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (wonGift!!.coinValue > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text("+${wonGift!!.coinValue} Coins", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showResult = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("OK", color = Color(0xFF1A252F), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private suspend fun getSpinData(context: Context): SpinData {
    val prefs = context.spinDataStore.data.first()
    val lastSpin = prefs[LAST_SPIN_KEY]?: 0L
    val now = System.currentTimeMillis()

    val canUseFreeSpin = (now - lastSpin) > 24 * 60 * 60 * 1000
    val timeLeft = if (canUseFreeSpin) 0L else (24 * 60 * 60 * 1000) - (now - lastSpin)

    return SpinData(canUseFreeSpin, timeLeft)
}

private suspend fun markFreeSpinUsed(context: Context) {
    context.spinDataStore.edit { prefs ->
        prefs[LAST_SPIN_KEY] = System.currentTimeMillis()
    }
}

data class SpinData(
    val canUseFreeSpin: Boolean,
    val timeLeft: Long
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GiftSpinPreview() {
    GiftSpinScreen()
}