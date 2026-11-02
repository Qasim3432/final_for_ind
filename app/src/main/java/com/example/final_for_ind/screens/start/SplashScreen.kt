package com.example.final_for_ind.screens.start

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // ✅ 1. DARK BLUE PREMIUM GRADIENT (Matching the Logo)
    val gradient = Brush.radialGradient(
        colors = listOf(Color(0xFF0F1A2C), Color(0xFF050A15)),
        radius = 1500f
    )

    // Animations
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }
    val textAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    // Loading dots animation
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    // Glow pulse animation
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by pulseTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowScale"
    )

    LaunchedEffect(Unit) {
        // Logo pop-in
        launch { logoAlpha.animateTo(1f, tween(800)) }
        logoScale.animateTo(1.1f, tween(800, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))

        // Glow and Text fade-in
        launch { glowAlpha.animateTo(1f, tween(1200)) }
        launch { textAlpha.animateTo(1f, tween(1000, delayMillis = 400)) }

        delay(2200) // Total hold time
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ==========================================
            // LOGO & GLOW
            // ==========================================
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Golden Glow Behind Logo
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha.value * 0.6f)
                        .blur(40.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFD700), Color.Transparent)),
                            CircleShape
                        )
                )

                // Main Logo Image
                Image(
                    painter = painterResource(id = R.drawable.rock),
                    contentDescription = "Rocks Game Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .alpha(logoAlpha.value)
                        .scale(logoScale.value)
                        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                )
            }

            Spacer(Modifier.height(36.dp))

            // ==========================================
            // APP NAME (Golden Gradient)
            // ==========================================
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "ROCKS GAME",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .alpha(textAlpha.value)
                        .blur(10.dp) // Shadow effect
                )

                Text(
                    text = "ROCKS GAME",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))
                        )
                    ),
                    modifier = Modifier.alpha(textAlpha.value)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "2X LUDO",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(60.dp))

            // ==========================================
            // LOADING DOTS
            // ==========================================
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                LoadingDot(scale = dot1)
                LoadingDot(scale = dot2)
                LoadingDot(scale = dot3)
            }
        }
    }
}

@Composable
fun LoadingDot(scale: Float) {
    Box(
        Modifier
            .size(12.dp)
            .scale(scale)
            .background(
                brush = Brush.radialGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                shape = CircleShape
            )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}