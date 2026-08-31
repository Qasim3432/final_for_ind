package com.example.final_for_ind.screens.component

import android.content.Context
import android.graphics.Paint as AndroidPaint
import android.graphics.Rect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
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
    paidSpinCost: Int = 40,
    onBack: () -> Unit = {},
    onRewardClaimed: (coins: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val gifts = listOf(
        SpinGift("10 Coins", "🪙", 10, Color(0xFFD32F2F)),
        SpinGift("20 Coins", "💰", 20, Color(0xFFFFD700)),
        SpinGift("Rose", "🌹", 0, Color(0xFF8B0000)),
        SpinGift("30 Coins", "💎", 30, Color(0xFFD32F2F)),
        SpinGift("Try Again", "😅", 0, Color(0xFF555)),
        SpinGift("40 Coins", "🪙", 40, Color(0xFF8B0000)),
        SpinGift("Crown", "👑", 0, Color(0xFFFFD700)),
        SpinGift("50 Coins", "💰", 50, Color(0xFFD32F2F))
    )

    var coins by remember { mutableStateOf(currentCoins) }
    var isSpinning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var wonGift by remember { mutableStateOf<SpinGift?>(null) }
    var spinData by remember { mutableStateOf(SpinData(true, 0L)) }

    val animatedRotation = remember { Animatable(0f) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSpinning) 0.7f else 0.3f,
        animationSpec = tween(500),
        label = "glow"
    )

    // 👇 TIMER KO HAR SECOND UPDATE KARNE KE LIYE
    LaunchedEffect(spinData.canUseFreeSpin) {
        if (!spinData.canUseFreeSpin) {
            while (true) {
                delay(1000L) // har 1 sec baad
                spinData = getSpinData(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        spinData = getSpinData(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Lucky Spin", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 0.5.sp, color = Color(0xFFFFD700))
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF2B0000)).border(2.dp, Color(0xFFFFD700), CircleShape)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFFFFD700))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(bgGradient).verticalScroll(scrollState).padding(paddingValues).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))).padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Balance", color = Color(0xFFFFD700), fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Text("$coins Coins", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier.background(
                        if (spinData.canUseFreeSpin)
                            Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFF2B0000), Color(0xFF1A0000)))
                    ).padding(18.dp)
                ) {
                    AnimatedContent(targetState = spinData.canUseFreeSpin, label = "status") { canFree ->
                        if (canFree) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉 FREE SPIN READY!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp)
                                Text("Tap SPIN to try your luck", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        } else {
                            // 👇 YAHAN SECONDS ADD KIYE
                            val hoursLeft = spinData.timeLeft / (60 * 60 * 1000)
                            val minutesLeft = (spinData.timeLeft % (60 * 60 * 1000)) / (60 * 1000)
                            val secondsLeft = (spinData.timeLeft % (60 * 1000)) / 1000
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏰ Next FREE in: ${hoursLeft}h ${minutesLeft}m ${secondsLeft}s", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Or pay $paidSpinCost coins", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(44.dp))

            // WHEEL WITH BIGGER BOX
            Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {

                Canvas(modifier = Modifier.size(320.dp).blur(20.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color.Transparent)),
                        radius = size.width / 2
                    )
                }

                // POINTER
                Canvas(modifier = Modifier.size(60.dp).align(Alignment.TopCenter).offset(y = (-15).dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2, size.height)
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    drawPath(path, Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700))))
                    drawPath(path, Color.White, style = Stroke(width = 2.5.dp.toPx()))
                }

                // WHEEL
                Canvas(modifier = Modifier.size(300.dp).rotate(animatedRotation.value).shadow(24.dp, CircleShape)) {
                    val sweepAngle = 360f / gifts.size
                    val radius = size.width / 2
                    val nativeCanvas = drawContext.canvas.nativeCanvas
                    val bounds = Rect()

                    gifts.forEachIndexed { index, gift ->
                        val startAngle = index * sweepAngle - 90f
                        val middleAngle = startAngle + sweepAngle / 2

                        // Slice
                        drawArc(
                            brush = Brush.sweepGradient(colorStops = arrayOf(0f to gift.color, 0.5f to gift.color.copy(alpha = 0.9f), 1f to gift.color)),
                            startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, size = Size(size.width, size.height)
                        )

                        // Border
                        drawArc(color = Color(0xFFFFD700).copy(alpha = 0.4f), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, style = Stroke(width = 3.dp.toPx()), size = Size(size.width, size.height))

                        // 👇 BIGGER BOX + BIG EMOJI + TEXT
                        val textRadius = radius * 0.60f
                        val angleRad = Math.toRadians(middleAngle.toDouble())
                        val x = (radius + textRadius * cos(angleRad)).toFloat()
                        val y = (radius + textRadius * sin(angleRad)).toFloat()

                        val label = if(gift.coinValue > 0) "${gift.coinValue}" else gift.name

                        // Paints
                        val emojiPaint = AndroidPaint().apply {
                            textSize = 58f
                            textAlign = AndroidPaint.Align.CENTER
                            isAntiAlias = true
                        }
                        val textPaint = AndroidPaint().apply {
                            textSize = 26f
                            textAlign = AndroidPaint.Align.CENTER
                            color = android.graphics.Color.WHITE
                            isFakeBoldText = true
                            isAntiAlias = true
                        }

                        val boxWidth = 120f
                        val boxHeight = emojiPaint.textSize + textPaint.textSize + 28f

                        nativeCanvas.save()
                        nativeCanvas.rotate(middleAngle + 90, x, y)

                        // 1. Box Background
                        val boxPaint = AndroidPaint().apply {
                            color = android.graphics.Color.argb(220, 0, 0, 0)
                            style = AndroidPaint.Style.FILL
                        }
                        nativeCanvas.drawRoundRect(
                            x - boxWidth/2, y - boxHeight/2 - 6,
                            x + boxWidth/2, y + boxHeight/2 + 6,
                            20f, 20f, boxPaint
                        )

                        // Box Border Golden
                        boxPaint.style = AndroidPaint.Style.STROKE
                        boxPaint.strokeWidth = 3.5f
                        boxPaint.color = android.graphics.Color.parseColor("#FFD700")
                        nativeCanvas.drawRoundRect(
                            x - boxWidth/2, y - boxHeight/2 - 6,
                            x + boxWidth/2, y + boxHeight/2 + 6,
                            20f, 20f, boxPaint
                        )

                        // 2. Emoji
                        nativeCanvas.drawText(gift.emoji, x, y - 10f, emojiPaint)

                        // 3. Text
                        nativeCanvas.drawText(label, x, y + 30f, textPaint)

                        nativeCanvas.restore()
                    }
                }

                // CENTER BUTTON
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))))
                        .border(5.dp, Color(0xFFFFD700), CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SPIN", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(44.dp))

            val buttonGradient = if (spinData.canUseFreeSpin) {
                Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700)))
            }
            val canSpin = coins >= (if (spinData.canUseFreeSpin) 0 else paidSpinCost) &&!isSpinning

            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(20.dp)).background(buttonGradient)
                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                    .clickable(enabled = canSpin) {
                        val cost = if (spinData.canUseFreeSpin) 0 else paidSpinCost
                        if (coins >= cost &&!isSpinning) {
                            isSpinning = true
                            if (cost > 0) coins -= cost
                            val targetIndex = Random.nextInt(gifts.size)
                            val sweepAngle = 360f / gifts.size
                            val targetRotation = 360f * 6 + (360f - targetIndex * sweepAngle - sweepAngle / 2)
                            scope.launch {
                                animatedRotation.animateTo(targetValue = targetRotation, animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing))
                                wonGift = gifts[targetIndex]
                                showResult = true
                                isSpinning = false
                                if (spinData.canUseFreeSpin) markFreeSpinUsed(context)
                                spinData = getSpinData(context)
                                if (wonGift!!.coinValue > 0) {
                                    coins += wonGift!!.coinValue
                                    onRewardClaimed(wonGift!!.coinValue)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isSpinning) "Spinning..."
                    else if (spinData.canUseFreeSpin) "🎁 FREE SPIN"
                    else "🎰 SPIN - $paidSpinCost Coins",
                    color = if (spinData.canUseFreeSpin) Color.White else Color(0xFF1A0000),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    letterSpacing = 0.5.sp
                )
            }

            if (coins < (if (spinData.canUseFreeSpin) 0 else paidSpinCost)) {
                Spacer(Modifier.height(12.dp))
                Text("Not enough coins!", color = Color(0xFFD32F2F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showResult && wonGift!= null) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            containerColor = Color(0xFF1A0000),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(if (wonGift!!.coinValue > 0) "🎉 JACKPOT!" else "😅 Try Again!", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontSize = 26.sp, letterSpacing = 0.5.sp, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                    Text(text = wonGift!!.emoji, fontSize = 80.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(text = wonGift!!.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (wonGift!!.coinValue > 0) {
                        Spacer(Modifier.height(12.dp))
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))) {
                            Text("+${wonGift!!.coinValue} Coins", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700))))
                        .clickable { showResult = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Collect Reward", color = Color(0xFF1A0000), fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 0.5.sp)
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

data class SpinData(val canUseFreeSpin: Boolean, val timeLeft: Long)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GiftSpinPreview() {
    GiftSpinScreen()
}