package com.example.final_for_ind.screens.component

import android.content.Context
import android.graphics.Paint as AndroidPaint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.example.final_for_ind.utils.SoundManager


data class SpinGift(val name: String, val emoji: String, val coinValue: Int, val color: Color)
data class SpinData(val canUseFreeSpin: Boolean, val timeLeft: Long)


// ==========================================================
// PREMIUM COLOR PALETTE
// ==========================================================

private val BgDeep = Color(0xFF0A0606)
private val BgRich = Color(0xFF150808)
private val GoldPure = Color(0xFFFFD700)
private val GoldRich = Color(0xFFD4AF37)
private val GoldDim = Color(0xFF8B6914)
private val CrimsonRich = Color(0xFF8B0000)
private val CrimsonBright = Color(0xFFD32F2F)
private val EmeraldDeep = Color(0xFF1B5E20)
private val EmeraldBright = Color(0xFF43A047)


// ==========================================================
// HAPTIC HELPER
// ==========================================================

fun vibrate(context: Context, duration: Long = 50) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    } catch (_: Exception) {
    }
}


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
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val wheelSize = minOf(screenWidth - 56.dp, 320.dp)

    // ==========================================================
    // GIFT POOL — premium palette
    // ==========================================================

    val gifts = listOf(
        SpinGift("50", "🪙", 50, Color(0xFFB8860B)),    // Goldenrod
        SpinGift("10", "🪙", 10, Color(0xFF8B0000)),    // Dark red
        SpinGift("20", "💰", 20, Color(0xFF6B4A0B)),    // Dark gold
        SpinGift("Rose", "🌹", 0, Color(0xFF6B1B1B)),   // Deep rose
        SpinGift("30", "💎", 30, Color(0xFF1E4D6B)),    // Steel blue
        SpinGift("Try", "✨", 0, Color(0xFF4A1B5C)),    // Deep purple
        SpinGift("40", "🪙", 40, Color(0xFF1B5E20)),    // Forest green
        SpinGift("Crown", "👑", 0, Color(0xFF6B3A1A))   // Burnt amber
    )

    // ==========================================================
    // SPARKLE POSITIONS
    // ==========================================================

    val sparklePositions = remember {
        List(40) {
            Triple(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat()
            )
        }
    }

    var coins by remember { mutableStateOf(currentCoins) }
    var isSpinning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var wonGift by remember { mutableStateOf<SpinGift?>(null) }
    var spinData by remember { mutableStateOf(SpinData(true, 0L)) }
    var serverPaidCost by remember { mutableStateOf(paidSpinCost) }
    var giftEnabled by remember { mutableStateOf(true) }

    val animatedCoins by animateIntAsState(
        targetValue = coins,
        animationSpec = tween(durationMillis = 800),
        label = "coins"
    )

    val animatedRotation = remember { Animatable(0f) }

    val sparkleAlpha by rememberInfiniteTransition(label = "sparkle").animateFloat(
        0.2f, 0.8f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "sparkle"
    )

    val idleSway by rememberInfiniteTransition(label = "idle").animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle"
    )

    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    suspend fun loadStatusFromServer() {
        val status = sessionManager.getSpinStatus()
        if (status != null && status.optString("status") == "success") {
            spinData = SpinData(
                canUseFreeSpin = status.optBoolean("can_use_free_spin", true),
                timeLeft = status.optLong("time_left", 0L)
            )
            coins = status.optInt("coins", coins)
            serverPaidCost = status.optInt("paid_spin_cost", serverPaidCost)
        }
    }

    LaunchedEffect(Unit) {
        val config = sessionManager.fetchGiftConfig()
        giftEnabled = config.enabled
        serverPaidCost = config.paidCost
        if (giftEnabled) {
            loadStatusFromServer()
        }
    }

    LaunchedEffect(spinData.canUseFreeSpin) {
        if (!spinData.canUseFreeSpin && giftEnabled) {
            while (true) {
                delay(1000L)
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

    val currentCost = if (spinData.canUseFreeSpin) 0 else serverPaidCost
    val canAfford = coins >= currentCost
    val canSpin = canAfford && !isSpinning

    fun doSpin() {
        if (!canSpin) {
            if (!canAfford) {
                Toast.makeText(
                    context,
                    "Not enough coins. Need $serverPaidCost",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        isSpinning = true
        vibrate(context, 60)

        scope.launch {
            val result = sessionManager.submitSpin()

            if (result != null && result.optString("status") == "success") {
                val targetIndex = result.optInt("won_index", Random.nextInt(gifts.size))
                val newCoins = result.optInt("coins", coins)
                val sweepAngle = 360f / gifts.size
                val targetRotation = 360f * 6 + (360f - targetIndex * sweepAngle - sweepAngle / 2)

                launch {
                    repeat(30) {
                        delay(120L)
                        SoundManager.playSpinTick()
                    }
                }

                animatedRotation.animateTo(
                    targetValue = targetRotation,
                    animationSpec = tween(4000, easing = FastOutSlowInEasing)
                )

                delay(200)

                val gift = gifts.getOrNull(targetIndex)
                wonGift = gift
                coins = newCoins
                showResult = true
                isSpinning = false

                vibrate(context, 120)
                loadStatusFromServer()

                if (gift != null && gift.coinValue > 0) {
                    SoundManager.playCoinReward()
                    onRewardClaimed(gift.coinValue)
                }
            } else {
                isSpinning = false
                val msg = result?.optString("error")
                    ?.takeIf { it.isNotBlank() }
                    ?: "Spin failed. Try again."
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================================
    // GIFT DISABLED
    // ==========================================================

    if (!giftEnabled) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(BgDeep, BgRich)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("🎁", fontSize = 80.sp)

                Spacer(Modifier.height(24.dp))

                Text(
                    "Gift System Offline",
                    color = GoldPure,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "We're preparing something special.\nCheck back soon!",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldDim, GoldRich)
                            )
                        )
                        .border(
                            2.dp,
                            GoldPure.copy(alpha = 0.6f),
                            RoundedCornerShape(26.dp)
                        )
                        .clickable { onBack() }
                        .padding(horizontal = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Go Back",
                        color = BgDeep,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
        return
    }

    // ==========================================================
    // MAIN LAYOUT
    // ==========================================================

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgDeep, BgRich, BgDeep)
                )
            )
    ) {

        // ----------------------------------------------
        // Subtle radial glow behind wheel
        // ----------------------------------------------

        Box(
            Modifier
                .align(Alignment.Center)
                .size(wheelSize + 200.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            GoldDim.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ----------------------------------------------
        // Sparkles
        // ----------------------------------------------

        Canvas(Modifier.fillMaxSize()) {
            sparklePositions.forEach { (fx, fy, sizeFactor) ->
                drawCircle(
                    GoldPure.copy(alpha = sparkleAlpha * sizeFactor * 0.5f),
                    radius = sizeFactor * 2.5f,
                    center = Offset(fx * size.width, fy * size.height)
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
            containerColor = Color.Transparent
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(16.dp))

                // ==============================================
                // TOP HEADER BAR
                // ==============================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Back button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                GoldRich.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = GoldPure,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Title with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Casino,
                            null,
                            tint = GoldPure,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Lucky Spin",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Placeholder for symmetry
                    Box(Modifier.size(44.dp))
                }

                Spacer(Modifier.height(20.dp))

                // ==============================================
                // BALANCE CARD — premium
                // ==============================================

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1F0A0A),
                                    Color(0xFF2B0000),
                                    Color(0xFF1F0A0A)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    GoldRich.copy(alpha = 0.8f),
                                    GoldPure.copy(alpha = 0.4f),
                                    GoldRich.copy(alpha = 0.8f)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = GoldRich,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "YOUR BALANCE",
                                    color = GoldRich,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${animatedCoins}",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = -0.5.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Coins",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Coin emblem
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            GoldPure,
                                            GoldRich
                                        )
                                    )
                                )
                                .border(
                                    2.dp,
                                    GoldDim,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪙", fontSize = 26.sp)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ==============================================
                // STATUS CHIP
                // ==============================================

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (spinData.canUseFreeSpin)
                                Brush.horizontalGradient(
                                    listOf(EmeraldDeep, EmeraldBright)
                                )
                            else
                                Brush.horizontalGradient(
                                    listOf(CrimsonRich, CrimsonBright)
                                )
                        )
                        .border(
                            1.5.dp,
                            GoldRich.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {

                    AnimatedContent(
                        targetState = spinData.canUseFreeSpin,
                        label = "status"
                    ) { canFree ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canFree) {
                                Icon(
                                    Icons.Default.Bolt,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "FREE SPIN READY",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                val h = spinData.timeLeft / 3600
                                val m = (spinData.timeLeft % 3600) / 60
                                val s = spinData.timeLeft % 60
                                Text(
                                    "⏱ ${h}h ${m}m ${s}s",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ==============================================
                // SPIN WHEEL
                // ==============================================

                Box(
                    modifier = Modifier.size(wheelSize),
                    contentAlignment = Alignment.Center
                ) {

                    // Outer glow ring
                    Box(
                        Modifier
                            .size(wheelSize + 30.dp)
                            .blur(30.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        GoldRich.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Pointer (top triangle)
                    Canvas(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-5).dp)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2, size.height)
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(
                            path,
                            Brush.verticalGradient(
                                listOf(GoldPure, GoldRich, GoldDim)
                            )
                        )
                        drawPath(
                            path,
                            Color(0xFF1A0A0A),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Wheel canvas
                    Canvas(
                        modifier = Modifier
                            .size(wheelSize - 30.dp)
                            .rotate(animatedRotation.value + idleSway)
                    ) {
                        val sweepAngle = 360f / gifts.size
                        val radius = size.width / 2
                        val nativeCanvas = drawContext.canvas.nativeCanvas

                        gifts.forEachIndexed { index, gift ->

                            val startAngle = index * sweepAngle - 90f
                            val middleAngle = startAngle + sweepAngle / 2

                            // Segment
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        gift.color,
                                        gift.color.copy(alpha = 0.85f)
                                    )
                                ),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )

                            // Thin gold border
                            drawArc(
                                color = GoldRich.copy(alpha = 0.7f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                style = Stroke(width = 1.5.dp.toPx()),
                                size = Size(size.width, size.height)
                            )

                            // Text
                            val textRadius = radius * 0.68f
                            val angleRad = Math.toRadians(middleAngle.toDouble())
                            val x = (radius + textRadius * cos(angleRad)).toFloat()
                            val y = (radius + textRadius * sin(angleRad)).toFloat()

                            val label = if (gift.coinValue > 0) "${gift.coinValue}" else gift.name

                            val normalizedAngle = ((middleAngle + 360) % 360)
                            val shouldFlip = normalizedAngle > 90 && normalizedAngle < 270

                            val emojiPaint = AndroidPaint().apply {
                                textSize = 42f
                                textAlign = AndroidPaint.Align.CENTER
                                isAntiAlias = true
                            }
                            val textPaint = AndroidPaint().apply {
                                textSize = 18f
                                textAlign = AndroidPaint.Align.CENTER
                                color = android.graphics.Color.WHITE
                                isFakeBoldText = true
                                isAntiAlias = true
                            }

                            nativeCanvas.save()
                            nativeCanvas.rotate(
                                middleAngle + if (shouldFlip) -90 else 90,
                                x,
                                y
                            )
                            nativeCanvas.drawText(gift.emoji, x, y - 12f, emojiPaint)
                            nativeCanvas.drawText(label, x, y + 20f, textPaint)
                            nativeCanvas.restore()
                        }
                    }

                    // Gold ring border around wheel
                    Box(
                        Modifier
                            .size(wheelSize - 30.dp)
                            .border(
                                4.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        GoldRich,
                                        GoldPure,
                                        GoldDim,
                                        GoldPure,
                                        GoldRich
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // ==========================================
                    // SPIN BUTTON (center)
                    // ==========================================

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(
                                if (spinData.canUseFreeSpin && !isSpinning)
                                    pulseScale
                                else
                                    1f
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    if (canSpin)
                                        listOf(
                                            GoldPure,
                                            GoldRich,
                                            GoldDim
                                        )
                                    else
                                        listOf(
                                            Color(0xFF444444),
                                            Color(0xFF222222)
                                        )
                                )
                            )
                            .border(
                                5.dp,
                                if (canSpin) CrimsonRich else Color(0xFF1A1A1A),
                                CircleShape
                            )
                            .shadow(20.dp, CircleShape)
                            .clickable(enabled = !isSpinning) { doSpin() },
                        contentAlignment = Alignment.Center
                    ) {

                        if (isSpinning) {

                            CircularProgressIndicator(
                                color = CrimsonRich,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(42.dp)
                            )

                        } else {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(
                                    "SPIN",
                                    color = if (canSpin)
                                        CrimsonRich
                                    else
                                        Color(0xFF999999),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    letterSpacing = 2.sp
                                )

                                if (spinData.canUseFreeSpin) {

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        "FREE",
                                        color = CrimsonRich,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.5.sp
                                    )

                                } else if (!canAfford) {

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        "NEED $serverPaidCost",
                                        color = Color(0xFFFF6B6B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ==============================================
                // ACTION BUTTON
                // ==============================================

                val buttonText = when {
                    isSpinning -> "SPINNING..."
                    spinData.canUseFreeSpin -> "SPIN NOW — FREE"
                    !canAfford -> "NEED $serverPaidCost COINS"
                    else -> "SPIN — $serverPaidCost COINS"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .scale(
                            if (spinData.canUseFreeSpin && !isSpinning)
                                pulseScale
                            else
                                1f
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.horizontalGradient(
                                when {
                                    spinData.canUseFreeSpin ->
                                        listOf(EmeraldDeep, EmeraldBright, EmeraldDeep)

                                    !canAfford ->
                                        listOf(
                                            Color(0xFF2A2A2A),
                                            Color(0xFF1A1A1A)
                                        )

                                    else ->
                                        listOf(CrimsonRich, CrimsonBright, CrimsonRich)
                                }
                            )
                        )
                        .border(
                            2.dp,
                            if (canSpin)
                                GoldRich
                            else
                                Color(0xFF333333),
                            RoundedCornerShape(32.dp)
                        )
                        .shadow(
                            if (canSpin) 12.dp else 0.dp,
                            RoundedCornerShape(32.dp)
                        )
                        .clickable(enabled = !isSpinning) { doSpin() },
                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        if (!isSpinning && spinData.canUseFreeSpin) {
                            Icon(
                                Icons.Default.Bolt,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }

                        Text(
                            buttonText,
                            color = when {
                                spinData.canUseFreeSpin -> Color.White
                                !canAfford -> Color(0xFF999999)
                                else -> GoldPure
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        // ==============================================
        // RESULT DIALOG — premium
        // ==============================================

        val gift = wonGift
        if (showResult && gift != null) {

            val isWin = gift.coinValue > 0

            AlertDialog(
                onDismissRequest = { },
                containerColor = Color(0xFF1A0A0A),
                shape = RoundedCornerShape(32.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isWin) "🎉 YOU WON!" else "😅 TRY AGAIN!",
                            color = GoldPure,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            GoldPure.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(gift.emoji, fontSize = 80.sp)
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            gift.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        if (isWin) {

                            Spacer(Modifier.height(16.dp))

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = GoldPure.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.5.dp, GoldRich)
                            ) {
                                Text(
                                    "+${gift.coinValue} Coins",
                                    color = GoldPure,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(
                                        horizontal = 28.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldRich, GoldPure, GoldRich)
                                )
                            )
                            .clickable { showResult = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isWin) "COLLECT REWARD" else "OK",
                            color = Color(0xFF1A0A0A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GiftSpinPreview() {
    GiftScreen()
}