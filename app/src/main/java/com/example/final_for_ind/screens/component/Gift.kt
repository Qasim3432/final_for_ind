package com.example.final_for_ind.screens.component

import android.content.Context
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.example.final_for_ind.network.GameSessionManager

data class SpinGift(val name: String, val emoji: String, val coinValue: Int, val color: Color)
data class SpinData(val canUseFreeSpin: Boolean, val timeLeft: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftScreen(
    selectedNavIndex: Int = 3,
    onNavItemClick: (Int) -> Unit = {},
    currentCoins: Int = 0,
    paidSpinCost: Int = 40,
    onBack: () -> Unit = {},
    onRewardClaimed: (coins: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val sessionManager = remember { GameSessionManager(context) }

    val gifts = listOf(
        SpinGift("50 Coins", "🪙", 50, Color(0xFFD4AF37)),
        SpinGift("10 Coins", "🪙", 10, Color(0xFF8B0000)),
        SpinGift("20 Coins", "💰", 20, Color(0xFFD4AF37)),
        SpinGift("Rose", "🌹", 0, Color(0xFF8B0000)),
        SpinGift("30 Diamond", "💎", 30, Color(0xFFD4AF37)),
        SpinGift("Try Again", "✨", 0, Color(0xFF8B0000)),
        SpinGift("40 Coins", "🪙", 40, Color(0xFFD4AF37)),
        SpinGift("Crown", "👑", 0, Color(0xFF8B0000))
    )

    var coins by remember { mutableStateOf(currentCoins) }
    var isSpinning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var wonGift by remember { mutableStateOf<SpinGift?>(null) }
    var spinData by remember { mutableStateOf(SpinData(true, 0L)) }
    var serverPaidCost by remember { mutableStateOf(paidSpinCost) }
    var giftEnabled by remember { mutableStateOf(true) }

    val animatedRotation = remember { Animatable(0f) }
    val sparkleAlpha by rememberInfiniteTransition(label = "sparkle").animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "sparkle"
    )

    suspend fun loadStatusFromServer() {
        val status = sessionManager.getSpinStatus()
        if (status!= null && status.optString("status") == "success") {
            spinData = SpinData(
                canUseFreeSpin = status.optBoolean("can_use_free_spin", true),
                timeLeft = status.optLong("time_left", 0L)
            )
            coins = status.optInt("coins", coins)
            serverPaidCost = status.optInt("paid_spin_cost", serverPaidCost)
        }
    }

    // Pehli baar backend se config + status load
    LaunchedEffect(Unit) {
        val config = sessionManager.fetchGiftConfig()
        giftEnabled = config.enabled
        serverPaidCost = config.paidCost
        if (giftEnabled) {
            loadStatusFromServer()
        }
    }

    // Timer - sirf jab free nahi hai
    LaunchedEffect(spinData.canUseFreeSpin) {
        if (!spinData.canUseFreeSpin && giftEnabled) {
            while (true) {
                delay(1000L)
                // local countdown
                val newTimeLeft = spinData.timeLeft - 1
                if (newTimeLeft <= 0) {
                    loadStatusFromServer()
                    break
                } else {
                    spinData = spinData.copy(timeLeft = newTimeLeft)
                }
            }
        }
    }

    fun doSpin() {
        val cost = if (spinData.canUseFreeSpin) 0 else serverPaidCost
        if (coins < cost || isSpinning) return
        isSpinning = true
        scope.launch {
            // Backend se spin karo - won_index server dega
            val result = sessionManager.submitSpin()
            if (result!= null && result.optString("status") == "success") {
                val targetIndex = result.optInt("won_index", Random.nextInt(gifts.size))
                val newCoins = result.optInt("coins", coins)
                val sweepAngle = 360f / gifts.size
                val targetRotation = 360f * 6 + (360f - targetIndex * sweepAngle - sweepAngle / 2)
                animatedRotation.animateTo(targetValue = targetRotation, animationSpec = tween(4000, easing = FastOutSlowInEasing))
                wonGift = gifts[targetIndex]
                coins = newCoins
                showResult = true
                isSpinning = false
                loadStatusFromServer()
                if (wonGift!!.coinValue > 0) { onRewardClaimed(wonGift!!.coinValue) }
            } else {
                isSpinning = false
            }
        }
    }

    // Agar admin ne gift off kiya ho
    if (!giftEnabled) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Gift system is currently disabled", color = Color.White, fontSize = 18.sp)
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = com.example.final_for_ind.R.drawable.h),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Canvas(Modifier.fillMaxSize()) {
            repeat(50) {
                drawCircle(
                    Color(0xFFFFD700).copy(alpha = sparkleAlpha * Random.nextFloat()),
                    radius = Random.nextFloat() * 3f,
                    center = androidx.compose.ui.geometry.Offset(Random.nextFloat() * size.width, Random.nextFloat() * size.height)
                )
            }
        }

        Scaffold(
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedNavIndex,
                    onItemClick = onNavItemClick
                )
            },
            topBar = {
                Box(
                    Modifier
                        .fillMaxWidth().padding(16.dp).height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(brush = Brush.horizontalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF000))), alpha = 0.8f)
                        .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(25.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFFD4AF37), modifier = Modifier.size(28.dp))
                    }
                    Text("Lucky Spin", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFD4AF37), letterSpacing = 1.sp)
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(paddingValues).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier.fillMaxWidth().height(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF4A0000), Color(0xFF2B0000))), alpha = 0.9f)
                        .border(3.dp, Color(0xFFD4AF37), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(36.dp).align(Alignment.CenterStart).padding(start = 20.dp))
                    Canvas(Modifier.size(40.dp).align(Alignment.CenterEnd).padding(end = 20.dp)) {
                        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37))), radius = size.width / 2)
                    }
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Balance", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("$coins Coins", color = Color(0xFFFFD700), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Box(
                    Modifier.offset(y = (-15).dp).height(45.dp).padding(horizontal = 30.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(brush = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), alpha = 0.9f)
                        .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(25.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(targetState = spinData.canUseFreeSpin, label = "status") { canFree ->
                        if (canFree) Text("FREE SPIN READY", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        else {
                            val h = spinData.timeLeft / 3600
                            val m = (spinData.timeLeft % 3600) / 60
                            val s = spinData.timeLeft % 60
                            Text("Next FREE in: ${h}h ${m}m ${s}s", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(modifier = Modifier.size(340.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(340.dp).blur(25.dp)) {
                        drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)), radius = size.width / 2)
                    }
                    Canvas(modifier = Modifier.size(50.dp).align(Alignment.TopCenter).offset(y = (-10).dp)) {
                        val path = Path().apply { moveTo(size.width / 2, size.height); lineTo(0f, 0f); lineTo(size.width, 0f); close() }
                        drawPath(path, Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37))))
                    }
                    Canvas(modifier = Modifier.size(320.dp).rotate(animatedRotation.value)) {
                        val sweepAngle = 360f / gifts.size
                        val radius = size.width / 2
                        val nativeCanvas = drawContext.canvas.nativeCanvas
                        gifts.forEachIndexed { index, gift ->
                            val startAngle = index * sweepAngle - 90f
                            val middleAngle = startAngle + sweepAngle / 2
                            drawArc(brush = Brush.sweepGradient(listOf(gift.color, gift.color.copy(alpha = 0.8f))), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, size = Size(size.width, size.height))
                            drawArc(color = Color(0xFFD4AF37), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true, style = Stroke(width = 4.dp.toPx()), size = Size(size.width, size.height))
                            val textRadius = radius * 0.65f
                            val angleRad = Math.toRadians(middleAngle.toDouble())
                            val x = (radius + textRadius * cos(angleRad)).toFloat()
                            val y = (radius + textRadius * sin(angleRad)).toFloat()
                            val label = if (gift.coinValue > 0) "${gift.coinValue}" else gift.name
                            val emojiPaint = AndroidPaint().apply { textSize = 48f; textAlign = AndroidPaint.Align.CENTER; isAntiAlias = true }
                            val textPaint = AndroidPaint().apply { textSize = 22f; textAlign = AndroidPaint.Align.CENTER; color = android.graphics.Color.WHITE; isFakeBoldText = true; isAntiAlias = true }
                            nativeCanvas.save(); nativeCanvas.rotate(middleAngle + 90, x, y)
                            nativeCanvas.drawText(gift.emoji, x, y - 15f, emojiPaint)
                            nativeCanvas.drawText(label, x, y + 20f, textPaint)
                            nativeCanvas.restore()
                        }
                    }
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37))))
                            .border(6.dp, Color(0xFF8B0000), CircleShape).shadow(12.dp, CircleShape)
                            .clickable(enabled =!isSpinning) { doSpin() },
                        contentAlignment = Alignment.Center
                    ) { Text("SPIN", color = Color(0xFF8B0000), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 1.5.sp) }
                }

                Spacer(Modifier.height(30.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(brush = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), alpha = 0.95f)
                        .border(3.dp, Color(0xFFD4AF37), RoundedCornerShape(30.dp))
                        .clickable(enabled =!isSpinning) { doSpin() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isSpinning) "Spinning..." else if (spinData.canUseFreeSpin) "FREE SPIN" else "SPIN - $serverPaidCost Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        if (showResult && wonGift!= null) {
            AlertDialog(
                onDismissRequest = { showResult = false },
                containerColor = Color(0xFF2B0000).copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp),
                title = { Text(if (wonGift!!.coinValue > 0) "🎉 JACKPOT!" else "😅 Try Again!", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontSize = 26.sp, modifier = Modifier.fillMaxWidth()) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(wonGift!!.emoji, fontSize = 80.sp); Spacer(Modifier.height(16.dp))
                        Text(wonGift!!.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        if (wonGift!!.coinValue > 0) {
                            Spacer(Modifier.height(12.dp))
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))) {
                                Text("+${wonGift!!.coinValue} Coins", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37)))).clickable { showResult = false }, contentAlignment = Alignment.Center) {
                        Text("Collect Reward", color = Color(0xFF8B0000), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GiftSpinPreview() { GiftScreen() }