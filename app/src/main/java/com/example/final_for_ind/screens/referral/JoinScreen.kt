package com.example.final_for_ind.screens.referral

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinReferralScreen(
    bonusCoins: Int = 50,
    onReferralApplied: (code: String) -> Unit = {},
    onSkip: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    var codeValues by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var error by remember { mutableStateOf(false) }

    val code = codeValues.joinToString("")
    val isComplete = code.length == 6

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome! 🎉",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter referral code & get $bonusCoins bonus coins", // 👈 English
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                codeValues.forEachIndexed { index, value ->
                    val isActive = value.isNotEmpty()
                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.05f else 1f,
                        animationSpec = tween(150),
                        label = "scale"
                    )

                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isLetterOrDigit() }) {
                                codeValues = codeValues.toMutableList().also {
                                    it[index] = newValue.uppercase()
                                }
                                error = false
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .scale(scale)
                            .focusRequester(focusRequesters[index])
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Backspace && value.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                    codeValues = codeValues.toMutableList().also {
                                        it[index - 1] = ""
                                    }
                                    true
                                } else false
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Color.White.copy(alpha = if (isActive) 0.15f else 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = if (error) 2.5.dp else if (isActive) 2.5.dp else 1.5.dp,
                                color = if (error) Color.Red else if (isActive) Color(0xFFFFD700) else Color.White.copy(
                                    alpha = 0.2f
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        "-",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            if (error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚠️ Code must be 6 characters", // 👈 English
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = if (isComplete)
                            Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700)))
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.3f),
                                    Color.Gray.copy(alpha = 0.3f)
                                )
                            ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        2.dp,
                        if (isComplete) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Button(
                    onClick = {
                        if (isComplete) {
                            onReferralApplied(code)
                        } else {
                            error = true
                        }
                    },
                    enabled = isComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        text = "Apply Code",
                        color = if (isComplete) Color(0xFF1A252F) else Color.White.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip it, I'll do it later",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚡ Note: Code can only be used once", // 👈 English
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun JoinReferralScreenPreview() {
    JoinReferralScreen()
}