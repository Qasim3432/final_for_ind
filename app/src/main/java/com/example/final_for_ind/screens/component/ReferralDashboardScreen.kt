package com.example.final_for_ind.screens.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import org.json.JSONObject

import com.example.final_for_ind.network.GameSessionManager
import com.example.final_for_ind.utils.formatDollar


data class ReferredUser(
    val name: String,
    val profilePic: String?,
    val joinedAt: String,
    val commissionEarned: Int,
    val rewardPaid: Boolean,
    val rewardAmount: Int,
)


// ==========================================================
// PREMIUM COLOR PALETTE (Cyberpunk / Neon)
// ==========================================================

private val BgDark = Color(0xFF050A15) // Deep dark blue
private val CardBgDark = Color(0xFF081020)

private val NeonBlue = Color(0xFF00E5FF)
private val NeonPurple = Color(0xFF9D4EDD)
private val NeonRed = Color(0xFFFF3D71)
private val YellowGold = Color(0xFFFFD700)
private val GreenNeon = Color(0xFF00E676)

private val TextGray = Color(0xFFA0AABF)


// ==========================================================
// ADAPTIVE FONT SIZE FOR LONG NUMBERS (counts, not currency)
// ==========================================================

private fun adaptiveNumberFontSize(value: Int): TextUnit {
    val digits = value.toString().length
    return when {
        digits <= 4 -> 26.sp
        digits == 5 -> 22.sp
        digits == 6 -> 20.sp
        digits == 7 -> 18.sp
        digits == 8 -> 16.sp
        digits == 9 -> 14.sp
        else -> 12.sp
    }
}

private fun adaptiveCurrencyFontSize(value: Int): TextUnit {
    val length = formatDollar(value).length
    return when {
        length <= 5 -> 26.sp
        length == 6 -> 22.sp
        length == 7 -> 20.sp
        length == 8 -> 18.sp
        length == 9 -> 16.sp
        length == 10 -> 14.sp
        else -> 12.sp
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralDashboardScreen(
    dashboardJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val sessionManager = remember { GameSessionManager(context) }

    var copied by remember { mutableStateOf(false) }

    var shareTextTemplate by remember {
        mutableStateOf(
            "Join me on Rocks Games! 🎮\n\n" +
                    "Use my referral code: {code}\n" +
                    "Sign up and we both earn rewards! 🎁"
        )
    }

    LaunchedEffect(Unit) {
        try {
            val config = sessionManager.fetchAppConfig()
            config?.optString("referral_share_text")
                ?.takeIf { it.isNotBlank() }
                ?.let { shareTextTemplate = it }
        } catch (_: Exception) {}
    }

    val referralCode = remember(dashboardJson) {
        dashboardJson?.optString("referral_code", "") ?: ""
    }

    val hasValidCode = referralCode.isNotBlank()
            && referralCode != "------"
            && referralCode != "NONE"
            && referralCode != "ERROR"

    val totalReferred = remember(dashboardJson) {
        dashboardJson?.optInt("total_referred", 0) ?: 0
    }

    val totalCommission = remember(dashboardJson) {
        dashboardJson?.optInt("total_commission_earned", 0) ?: 0
    }

    val pendingRewards = remember(dashboardJson) {
        dashboardJson?.optInt("pending_rewards", 0) ?: 0
    }

    val referredUsers = remember(dashboardJson) {
        val list = mutableListOf<ReferredUser>()
        val arr = dashboardJson?.optJSONArray("referred_users")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    ReferredUser(
                        name = o.optString("name", "Player"),
                        profilePic = o.optString("profile_pic", "").takeIf { it.isNotBlank() && it != "null" },
                        joinedAt = o.optString("joined_at", ""),
                        commissionEarned = o.optInt("commission_earned", 0),
                        rewardPaid = o.optBoolean("reward_paid", false),
                        rewardAmount = o.optInt("reward_amount", 0),
                    )
                )
            }
        }
        list
    }

    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(2000L)
            copied = false
        }
    }

    // Background with diagonal tech stripes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .drawBehind {
                // Diagonal light beams
                val sweepColor = Color(0xFF0F2040).copy(alpha = 0.6f)
                val path1 = Path().apply {
                    moveTo(size.width * 0.6f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.4f)
                    close()
                }
                drawPath(path1, sweepColor)

                val path2 = Path().apply {
                    moveTo(size.width * 0.8f, size.height)
                    lineTo(size.width, size.height)
                    lineTo(size.width, size.height * 0.7f)
                    close()
                }
                drawPath(path2, sweepColor)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp) // Safe area
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = YellowGold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Referral ",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dashboard",
                    color = YellowGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("👑", fontSize = 20.sp)
            }

            // Header Underline
            Box(
                modifier = Modifier
                    .padding(start = 60.dp)
                    .width(160.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(YellowGold, Color.Transparent))
                    )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ==========================================
                    // REFERRAL CODE CARD (Neon Gradient Border)
                    // ==========================================
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(16.dp))
                                // Gradient Border trick
                                .background(Brush.linearGradient(listOf(NeonBlue, NeonPurple, NeonBlue)))
                                .padding(2.dp) // Border width
                                .clip(CutCornerShape(14.dp))
                                .background(CardBgDark)
                        ) {
                            // Inner decorative diagonal lines
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val lineColor = Color.White.copy(alpha = 0.05f)
                                drawLine(lineColor, start = Offset(0f, size.height), end = Offset(size.width * 0.4f, size.height), strokeWidth = 6.dp.toPx())
                                drawLine(lineColor, start = Offset(size.width * 0.8f, 0f), end = Offset(size.width, 0f), strokeWidth = 6.dp.toPx())
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title with side lines
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.width(20.dp).height(1.dp).background(YellowGold))
                                    Spacer(Modifier.width(8.dp))
                                    Text("🎁", fontSize = 18.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "YOUR REFERRAL CODE",
                                        color = YellowGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.width(20.dp).height(1.dp).background(YellowGold))
                                }

                                Spacer(Modifier.height(16.dp))

                                // Code Box (Dark with Neon Blue Border)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CutCornerShape(12.dp))
                                        .border(1.5.dp, NeonBlue.copy(alpha = 0.6f), CutCornerShape(12.dp))
                                        .background(Color(0xFF030812))
                                        .clickable(enabled = hasValidCode) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", referralCode))
                                            copied = true
                                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (hasValidCode) referralCode else "------",
                                        color = if (hasValidCode) Color.White else Color.White.copy(alpha = 0.2f),
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 8.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (hasValidCode) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Tap code to copy",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                // Buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Copy Button (Cyan)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .clip(CutCornerShape(12.dp))
                                            .background(if (copied) GreenNeon else NeonBlue)
                                            .clickable(enabled = hasValidCode) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", referralCode))
                                                copied = true
                                                Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (copied) "COPIED!" else "COPY CODE",
                                                color = Color.Black,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    // Share Button (Red/Pink Gradient)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .clip(CutCornerShape(12.dp))
                                            .background(Brush.horizontalGradient(listOf(NeonRed, Color(0xFFFF8A65))))
                                            .clickable(enabled = hasValidCode) {
                                                val shareText = shareTextTemplate.replace("{code}", referralCode)
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Share Referral Code"))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "SHARE",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STATS ROW
                    // ==========================================
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Referred Card (Blue)
                            StatCard(
                                icon = "👥",
                                label = "Referred",
                                value = totalReferred,
                                accentColor = NeonBlue,
                                modifier = Modifier.weight(1f),
                                isCurrency = false
                            )

                            // Earned Card (Yellow/Green)
                            StatCard(
                                icon = "💰",
                                label = "Earned",
                                value = totalCommission,
                                accentColor = YellowGold,
                                valueColor = GreenNeon, // The text inside is green
                                modifier = Modifier.weight(1f),
                                isCurrency = true
                            )
                        }
                    }

                    // ==========================================
                    // PENDING REWARDS BANNER
                    // ==========================================
                    if (pendingRewards > 0) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1500)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, YellowGold)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = YellowGold)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("$pendingRewards rewards pending", color = YellowGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Unpaid until first deposit", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION HEADER
                    // ==========================================
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(Modifier.width(4.dp).height(18.dp).background(YellowGold))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Referred Users",
                                color = YellowGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(Modifier.weight(1f))

                            // Decorative slashes
                            Canvas(modifier = Modifier.width(40.dp).height(10.dp)) {
                                val slashColor = Color(0xFF3A1A5C)
                                drawLine(slashColor, start = Offset(size.width*0.2f, size.height), end = Offset(size.width, 0f), strokeWidth = 3.dp.toPx())
                                drawLine(slashColor, start = Offset(size.width*0.5f, size.height), end = Offset(size.width, 0f), strokeWidth = 3.dp.toPx())
                                drawLine(slashColor, start = Offset(size.width*0.8f, size.height), end = Offset(size.width, 0f), strokeWidth = 3.dp.toPx())
                            }
                        }
                    }

                    // ==========================================
                    // USERS LIST / EMPTY STATE
                    // ==========================================
                    if (referredUsers.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Glowing Gift Box
                                Box(contentAlignment = Alignment.Center) {
                                    // Glow
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(
                                                Brush.radialGradient(listOf(NeonPurple.copy(alpha=0.3f), Color.Transparent))
                                            )
                                    )
                                    Text("🎁", fontSize = 70.sp)
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "No referrals yet",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Share your code with friends to start earning!",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(referredUsers) { user ->
                            ReferredUserCard(user = user)
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}


// ==========================================================
// STAT CARD (Cyberpunk Style)
// ==========================================================

@Composable
fun StatCard(
    icon: String,
    label: String,
    value: Int,
    accentColor: Color,
    valueColor: Color = accentColor, // Added specific color for Earned text
    modifier: Modifier = Modifier,
    isCurrency: Boolean = false
) {
    val displayText = if (isCurrency) formatDollar(value) else "$value"
    val fontSize = if (isCurrency) adaptiveCurrencyFontSize(value) else adaptiveNumberFontSize(value)

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(CutCornerShape(16.dp))
            .border(1.5.dp, accentColor, CutCornerShape(16.dp))
            .background(CardBgDark),
        contentAlignment = Alignment.Center
    ) {
        // Faint background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.1f), Color.Transparent),
                        radius = 300f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 36.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                displayText,
                color = valueColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ==========================================================
// REFERRED USER CARD
// ==========================================================

@Composable
fun ReferredUserCard(user: ReferredUser) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBgDark)
            .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A0A2A))
                    .border(1.5.dp, NeonPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user.profilePic.isNullOrBlank()) {
                    AsyncImage(
                        model = user.profilePic,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Joined ${user.joinedAt}",
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    // Status Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user.rewardPaid) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (user.rewardPaid) GreenNeon else YellowGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (user.rewardPaid) "Paid" else "Pending",
                            color = if (user.rewardPaid) GreenNeon else YellowGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (user.commissionEarned > 0) {
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "+${formatDollar(user.commissionEarned)}",
                        color = GreenNeon,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("earned", color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}