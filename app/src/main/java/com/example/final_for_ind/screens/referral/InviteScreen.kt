package com.example.final_for_ind.screens.referral

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    userReferralCode: String = "AB7X9K",
    invitedCount: Int = 3,
    targetCount: Int = 5,
    rewardPerInvite: Int = 100,
    bonusReward: Int = 50,
    onShare: (code: String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // 👇 BLACK RED GRADIENT
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val progress = invitedCount.toFloat() / targetCount.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))

    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(gradient)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier = Modifier
                        .size(40.dp).clip(CircleShape)
                        .background(Color(0xFF2B0000), CircleShape) // 👈 DARK RED
                        .border(2.dp, Color(0xFFFFD700), CircleShape) // 👈 GOLDEN
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFFFD700)) // 👈 GOLDEN
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)), CircleShape)
                        .border(2.5.dp, Color(0xFFFFD700), CircleShape) // 👈 GOLDEN BORDER
                )
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(56.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Invite & Earn Coins 🎁", color = Color(0xFFFFD700), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, letterSpacing = 0.5.sp) // 👈 GOLDEN

            Spacer(modifier = Modifier.height(8.dp))

            Text("Get $rewardPerInvite coins per friend + bonus rewards", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN BORDER
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(28.dp))
                        .padding(28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Referral Code", color = Color(0xFFFFD700), fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF2B0000), RoundedCornerShape(18.dp)) // 👈 DARK RED
                                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp)) // 👈 GOLDEN
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userReferralCode, color = Color(0xFFFFD700), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                            // COPY BUTTON
                            Box(
                                modifier = Modifier
                                    .weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                                    .background(if (copied) Color(0xFFD32F2F) else Color(0xFF2B0000), RoundedCornerShape(14.dp)) // 👈 RED when copied
                                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp)) // 👈 GOLDEN
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(userReferralCode))
                                        copied = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (copied) Icons.Default.Done else Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AnimatedContent(targetState = copied, transitionSpec = { fadeIn() togetherWith fadeOut() }) { isCopied ->
                                        Text(if (isCopied) "Copied!" else "Copy", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }

                            // SHARE BUTTON
                            Box(
                                modifier = Modifier
                                    .weight(1.5f).height(52.dp).clip(RoundedCornerShape(14.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF25D366), Color(0xFF20BA5A))), RoundedCornerShape(14.dp)) // WhatsApp Green
                                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp)) // 👈 GOLDEN
                                    .clickable { onShare(userReferralCode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share on WhatsApp", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Color(0xFFFFD700)) // 👈 GOLDEN BORDER
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp) // 👈 GOLDEN
                            Text("$invitedCount/$targetCount Friends", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2B0000)) // 👈 DARK RED BG
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), RoundedCornerShape(6.dp)) // 👈 GOLDEN PROGRESS
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f)) // 👈 GOLDEN BG
                                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)) // 👈 GOLDEN
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Invite ${targetCount - invitedCount} more friends for $bonusReward bonus coins! 🔥",
                                color = Color(0xFFFFD700), // 👈 GOLDEN
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(onClick = onBack) {
                Text("Skip", color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun InviteScreenPreview() {
    InviteScreen()
}