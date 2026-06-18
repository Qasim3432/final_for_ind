package com.example.final_for_ind.screens.start

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    // Main animations
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }
    val glowAlpha = remember { Animatable(0f) }

    // Loading dots animation
    val infiniteTransition = rememberInfiniteTransition()
    val dot1 = infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot2 = infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot3 = infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {

        launch {
            alpha.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
            scale.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            glowAlpha.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
        }
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo with glow effect
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow background
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(1.3f)
                        .alpha(glowAlpha.value * 0.4f)
                        .blur(30.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // Emoji Logo
                Text(
                    text = "🎮",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .alpha(alpha.value)
                        .scale(scale.value)
                )
            }

            Spacer(Modifier.height(32.dp))

            // App Name with gradient
            Box {

                Text(
                    text = "Game Hub",
                    color = Color(0xFFFFD700).copy(alpha = 0.5f),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .blur(8.dp)
                        .alpha(glowAlpha.value * 0.6f)
                )


                Text(
                    text = "Game Hub",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .alpha(alpha.value)
                        .scale(scale.value)
                )
            }

            Spacer(Modifier.height(8.dp))


            Text(
                text = "Play • Compete • Win",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(Modifier.height(56.dp))


            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LoadingDot(scale = dot1.value)
                LoadingDot(scale = dot2.value)
                LoadingDot(scale = dot3.value)
            }
        }
    }
}

@Composable
fun LoadingDot(scale: Float) {
    Box(
        Modifier
            .size(10.dp)
            .scale(scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFB700))
                ),
                shape = CircleShape
            )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}