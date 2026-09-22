package com.example.final_for_ind.screens.dice_board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val WinBackground = Color(0xFF0F172A)
val WinAccent = Color(0xFF10B981)
val LoseBackground = Color(0xFF1F2937)
val LoseAccent = Color(0xFFF43F5E)
val RewardGold = Color(0xFFFFD700)

@Composable
fun WinnerScreen(
    didWin: Boolean,
    payout: Int = 0,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (didWin) WinBackground else LoseBackground)
    ) {
        if (didWin) {
            DualPartyPopperAnimation()
        } else {
            CryingEmojiRain()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (didWin) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
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

            if (didWin) {
                Spacer(modifier = Modifier.height(35.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.background(
                        brush = Brush.horizontalGradient(
                            listOf(RewardGold, Color(0xFFFFA500))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                ) {
                    Text(
                        text = "+$payout COINS",
                        color = Color(0xFF3A2500),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 40.dp,
                            vertical = 18.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            if (didWin) {
                                listOf(WinAccent, Color(0xFF059669))
                            } else {
                                listOf(LoseAccent, Color(0xFFE11D48))
                            }
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

// ==========================================
// --- HIGH AIR DRAG POPPER ANIMATION ---
// ==========================================

private class BurstParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val oscillationFrequency: Float,
    var phase: Float
)

@Composable
fun DualPartyPopperAnimation() {
    val confettiColors = listOf(
        Color(0xFFFFD700), Color(0xFF10B981), Color(0xFF3B82F6),
        Color(0xFFEC4899), Color(0xFFF59E0B), Color(0xFFA855F7), Color(0xFFEF4444)
    )

    val particles = remember { mutableStateListOf<BurstParticle>() }
    var frameTrigger by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTime ->
                frameTrigger = frameTime
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        @Suppress("UNUSED_VARIABLE")
        val unused = frameTrigger

        val canvasW = size.width
        val canvasH = size.height

        val leftX = 110f
        val rightX = canvasW - 110f
        val startY = 110f

        if (particles.isEmpty() && canvasW > 0) {
            repeat(80) {
                particles.add(createBurstParticle(isLeft = true, startX = leftX, startY = startY, colors = confettiColors))
            }
            repeat(80) {
                particles.add(createBurstParticle(isLeft = false, startX = rightX, startY = startY, colors = confettiColors))
            }
        }

        drawPopperCone(originX = 50f, originY = 50f, isLeft = true)
        drawPopperCone(originX = canvasW - 50f, originY = 50f, isLeft = false)

        // Light gravity and strong air resistance
        val gravity = 0.18f
        val drag = 0.88f // High resistance: quickly slows horizontal explosion to a float

        particles.forEach { p ->
            // Apply heavy air drag to slow down explosion speeds
            p.vx *= drag
            p.vy = (p.vy * drag) + gravity

            // Flutter oscillation
            p.phase += p.oscillationFrequency
            val wobbleX = (sin(p.phase.toDouble()) * 1.5f).toFloat()

            p.x += p.vx + wobbleX
            p.y += p.vy
            p.rotation += p.rotationSpeed

            rotate(p.rotation, Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x - p.width / 2f, p.y - p.height / 2f),
                    size = Size(p.width, p.height)
                )
            }
        }
    }
}

private fun createBurstParticle(isLeft: Boolean, startX: Float, startY: Float, colors: List<Color>): BurstParticle {
    val angleDegrees = if (isLeft) Random.nextFloat() * 50f + 15f else Random.nextFloat() * 50f + 115f
    val rads = Math.toRadians(angleDegrees.toDouble())

    // High explosive start speed (drag will quickly decelerate it)
    val explosionForce = Random.nextFloat() * 25f + 25f

    val isRibbon = Random.nextBoolean()
    val width = if (isRibbon) Random.nextFloat() * 5f + 5f else Random.nextFloat() * 8f + 8f
    val height = if (isRibbon) Random.nextFloat() * 14f + 14f else Random.nextFloat() * 8f + 8f

    return BurstParticle(
        x = startX,
        y = startY,
        vx = (cos(rads) * explosionForce).toFloat(),
        vy = (sin(rads) * explosionForce).toFloat(),
        color = colors.random(),
        width = width,
        height = height,
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = Random.nextFloat() * 12f - 6f,
        oscillationFrequency = Random.nextFloat() * 0.08f + 0.04f,
        phase = Random.nextFloat() * 6.28f
    )
}

private fun DrawScope.drawPopperCone(originX: Float, originY: Float, isLeft: Boolean) {
    val coneWidth = 55f
    val coneLength = 75f
    val angle = if (isLeft) 45f else -45f

    rotate(angle, pivot = Offset(originX, originY)) {
        val path = Path().apply {
            moveTo(originX, originY)
            lineTo(originX - coneWidth / 2f, originY + coneLength)
            lineTo(originX + coneWidth / 2f, originY + coneLength)
            close()
        }

        drawPath(path = path, color = Color(0xFFD97706))

        drawCircle(
            color = Color(0xFFFBBF24),
            radius = coneWidth / 2f,
            center = Offset(originX, originY + coneLength)
        )
    }
}

// ==========================================
// --- CRYING EMOJI RAIN ---
// ==========================================

private class RainParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    val alpha: Float
)

@Composable
fun CryingEmojiRain() {
    val textMeasurer = rememberTextMeasurer()
    val particles = remember { mutableStateListOf<RainParticle>() }
    var frameTrigger by remember { mutableLongStateOf(0L) }

    val textLayoutResult = remember(textMeasurer) {
        textMeasurer.measure(
            text = "😭",
            style = TextStyle(fontSize = 44.sp)
        )
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTime ->
                frameTrigger = frameTime
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        @Suppress("UNUSED_VARIABLE")
        val unused = frameTrigger

        val canvasW = size.width
        val canvasH = size.height

        if (particles.isEmpty() && canvasW > 0) {
            repeat(18) {
                particles.add(
                    RainParticle(
                        x = Random.nextFloat() * (canvasW - 80f),
                        y = Random.nextFloat() * -canvasH,
                        speed = Random.nextFloat() * 5f + 3f,
                        alpha = Random.nextFloat() * 0.4f + 0.5f
                    )
                )
            }
        }

        particles.forEachIndexed { index, p ->
            p.y += p.speed

            if (p.y > canvasH + 60f) {
                particles[index] = RainParticle(
                    x = Random.nextFloat() * (canvasW - 80f),
                    y = -80f,
                    speed = Random.nextFloat() * 5f + 3f,
                    alpha = Random.nextFloat() * 0.4f + 0.5f
                )
            }

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(p.x, p.y),
                alpha = p.alpha
            )
        }
    }
}