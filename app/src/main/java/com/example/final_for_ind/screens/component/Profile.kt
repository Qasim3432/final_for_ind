package com.example.final_for_ind.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String = "", // 👈 default khali rakha
    coins: Int = 0, // 👈 1250 -> 0
    onBack: () -> Unit = {},
    onBetHistory: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // 👇 GUEST CHECK
    val displayName = if (name.isBlank()) "Guest" else name

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFD700))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFFFFD700))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(bgGradient).padding(paddingValues).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // PROFILE PIC
            Box(
                modifier = Modifier.size(120.dp).shadow(16.dp, CircleShape).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))))
                    .border(3.dp, Color(0xFFFFD700), CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(model = profileImageUri, contentDescription = "Profile Picture", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFFFD700), modifier = Modifier.size(56.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Tap to change photo", color = Color(0xFFFFD700).copy(alpha = 0.7f), fontSize = 12.sp, letterSpacing = 0.5.sp)

            Spacer(Modifier.height(20.dp))

            Text(text = displayName, color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp) // 👈 displayName use kiya

            Spacer(Modifier.height(28.dp))

            // COINS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))).padding(22.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Coins", color = Color(0xFFFFD700), fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("$coins", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Account Settings", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, modifier = Modifier.fillMaxWidth().padding(start = 4.dp))

            Spacer(Modifier.height(14.dp))

            PremiumProfileMenuItem(icon = Icons.Default.History, title = "Bet History", subtitle = "View your game history", onClick = onBetHistory)

            Spacer(Modifier.weight(1f))

            // LOGOUT BUTTON
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(22.dp), tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("Logout", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White, letterSpacing = 0.5.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PremiumProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(Color(0xFF2B0000).copy(alpha = 0.6f), label = "bg")

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFFFD700).copy(alpha = 0.5f), modifier = Modifier.size(18.dp).rotate(180f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    ProfileScreen(name = "Guest", coins = 0) // 👈 Preview me Guest
}