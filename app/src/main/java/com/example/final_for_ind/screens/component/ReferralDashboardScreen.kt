package com.example.final_for_ind.screens.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import org.json.JSONObject

import com.example.final_for_ind.network.GameSessionManager


data class ReferredUser(
    val name: String,
    val profilePic: String?,
    val joinedAt: String,
    val commissionEarned: Int,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralDashboardScreen(
    dashboardJson: JSONObject?,
    isLoading: Boolean,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val gold = Color(0xFFFFD700)
    val darkRed = Color(0xFF2B0000)

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )

    var copied by remember { mutableStateOf(false) }

    // ==========================================================
    // SHARE TEXT TEMPLATE (fetched from server config)
    // ==========================================================

    var shareTextTemplate by remember {
        mutableStateOf(
            "Join me on Rocks Games! 🎮\n\n" +
                    "Use my referral code: {code}\n" +
                    "We both get 50 coins! 🎁"
        )
    }

    LaunchedEffect(Unit) {
        try {
            val sessionManager = GameSessionManager(context)
            val config = sessionManager.fetchAppConfig()

            config?.optString("referral_share_text")
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    shareTextTemplate = it
                }
        } catch (e: Exception) {
            // Silent fail — default stays
        }
    }

    val referralCode = remember(dashboardJson) {
        dashboardJson?.optString("referral_code", "------") ?: "------"
    }

    val totalReferred = remember(dashboardJson) {
        dashboardJson?.optInt("total_referred", 0) ?: 0
    }

    val totalCommission = remember(dashboardJson) {
        dashboardJson?.optInt("total_commission_earned", 0) ?: 0
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
                        profilePic = o.optString("profile_pic", "")
                            .takeIf { it.isNotBlank() && it != "null" },
                        joinedAt = o.optString("joined_at", ""),
                        commissionEarned = o.optInt("commission_earned", 0),
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Referral Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {

            if (isLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = gold)
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ----------------------------------------------
                    // Referral Code Card
                    // ----------------------------------------------

                    item {

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.5.dp,
                                gold
                            )
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF4A0000),
                                                Color(0xFF1A0000)
                                            )
                                        )
                                    )
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(
                                    "🎁 YOUR REFERRAL CODE",
                                    color = gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    referralCode,
                                    color = Color.White,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 8.sp
                                )

                                Spacer(Modifier.height(20.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (copied) Color(0xFF2E7D32)
                                                else gold
                                            )
                                            .clickable {

                                                val clipboard = context.getSystemService(
                                                    Context.CLIPBOARD_SERVICE
                                                ) as ClipboardManager

                                                clipboard.setPrimaryClip(
                                                    ClipData.newPlainText(
                                                        "Referral Code",
                                                        referralCode
                                                    )
                                                )

                                                copied = true

                                                Toast.makeText(
                                                    context,
                                                    "Code copied!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = Color(0xFF1A0000),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(
                                                if (copied) "COPIED!" else "COPY CODE",
                                                color = Color(0xFF1A0000),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFF8B0000),
                                                        Color(0xFFD32F2F)
                                                    )
                                                )
                                            )
                                            .clickable {

                                                // ✅ Dynamic share text from server config
                                                val shareText = shareTextTemplate
                                                    .replace("{code}", referralCode)

                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }

                                                context.startActivity(
                                                    Intent.createChooser(
                                                        intent,
                                                        "Share Referral Code"
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(
                                                "SHARE",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ----------------------------------------------
                    // Stats Row
                    // ----------------------------------------------

                    item {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            StatCard(
                                icon = "👥",
                                label = "Referred",
                                value = "$totalReferred",
                                cardColor = Color(0xFF1A4A6B),
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                icon = "💰",
                                label = "Earned",
                                value = "$totalCommission",
                                cardColor = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ----------------------------------------------
                    // Section header
                    // ----------------------------------------------

                    item {

                        Text(
                            "Referred Users",
                            color = gold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // ----------------------------------------------
                    // Users list / Empty
                    // ----------------------------------------------

                    if (referredUsers.isEmpty()) {

                        item {
                            EmptyReferralState(gold)
                        }

                    } else {

                        items(referredUsers) { user ->
                            ReferredUserCard(
                                user = user,
                                gold = gold,
                                darkRed = darkRed
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}


// ==========================================================
// STAT CARD
// ==========================================================

@Composable
fun StatCard(
    icon: String,
    label: String,
    value: String,
    cardColor: Color,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.30f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            cardColor.copy(alpha = 0.8f)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                icon,
                fontSize = 32.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                value,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            Text(
                label,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}


// ==========================================================
// REFERRED USER CARD
// ==========================================================

@Composable
fun ReferredUserCard(
    user: ReferredUser,
    gold: Color,
    darkRed: Color
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0000)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            gold.copy(alpha = 0.3f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(darkRed)
                    .border(1.5.dp, gold.copy(alpha = 0.5f), CircleShape),
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

                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = gold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    user.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    "Joined ${user.joinedAt}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            if (user.commissionEarned > 0) {

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        "+${user.commissionEarned}",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        "coins",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}


// ==========================================================
// EMPTY STATE
// ==========================================================

@Composable
fun EmptyReferralState(gold: Color) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("🎁", fontSize = 60.sp)

        Spacer(Modifier.height(16.dp))

        Text(
            "No referrals yet",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Share your code with friends to earn 50 coins!",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}