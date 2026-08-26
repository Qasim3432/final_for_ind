package com.example.final_for_ind.screens.home_lobby

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.screens.component.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    coins: Int = 0,
    userId: String = "101221460378",
    isGuest: Boolean = true, // 👈 Guest check
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGameModeClick: (String, Int) -> Unit,
    onDepositClick: () -> Unit = {}, // 👈 Deposit ke liye
    onNavItemClick: (String) -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var showBotDialog by remember { mutableStateOf(false) }
    var showTwoPlayerDialog by remember { mutableStateOf(false) }
    var showFourPlayerDialog by remember { mutableStateOf(false) }
    var showLoginPrompt by remember { mutableStateOf(false) } // 👈 Login popup
    val context = LocalContext.current

    val blackGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val blackTopBarGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
    )

    val goldGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
    )

    // 👇 LOGIN PROMPT POPUP
    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            containerColor = Color(0xFF1A0000),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Login Required", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            },
            text = {
                Text(
                    "You need to login or signup to access this feature",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLoginPrompt = false; onLoginClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLoginPrompt = false; onSignUpClick() },
                    border = BorderStroke(2.dp, Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Up", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    // 👇 GUEST CHECK: Wallet, Deposit, Gift, Profile
                    if (isGuest && (route == "wallet" || route == "gift" || route == "profile" || route == "deposit")) {
                        showLoginPrompt = true
                    } else {
                        onNavItemClick(route)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(blackGradient)
                .padding(innerPadding)
        ) {

            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blackTopBarGradient)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A0000))
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2B0000))
                            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                            .clickable {
                                if (isGuest) showLoginPrompt = true else onProfileClick() // 👈 GUEST CHECK
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "Profile", Modifier.size(28.dp), tint = Color(0xFFFFD700))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(goldGradient, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("💰", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("$coins", color = Color(0xFF1A0000), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Box(modifier = Modifier.size(18.dp).background(Color.Black, RoundedCornerShape(6.dp)).clickable {
                                if (isGuest) showLoginPrompt = true else onDepositClick() // 👈 DEPOSIT CHECK
                            }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, "", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID  $userId", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ContentCopy, "Copy ID", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp).clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("User ID", userId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ID Copied!", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(color = Color(0xFF1A0000), shape = RoundedCornerShape(10.dp)).border(2.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp)).clickable { onSettingsClick() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                }
            }

            // Game Mode Cards
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Select Game Mode", color = Color(0xFFFFD700), fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 30.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GameModeCard(
                            title = "Quick Match",
                            subtitle = "Play against another player",
                            icon = Icons.Default.Person,
                            gradient = Brush.linearGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))
                        ) { showTwoPlayerDialog = true }

                        GameModeCard(
                            title = "4 Players",
                            subtitle = "Play with other three players",
                            icon = Icons.Default.Group,
                            gradient = Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935)))
                        ) { showFourPlayerDialog = true }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GameModeCard(
                            title = "Play with Friends",
                            subtitle = "Invite your friends to play",
                            icon = Icons.Default.People,
                            gradient = Brush.linearGradient(listOf(Color(0xFFFFA000), Color(0xFFFFD700)))
                        ) { onFriendsClick() }

                        GameModeCard(
                            title = "Computer",
                            subtitle = "Play against AI",
                            icon = Icons.Default.SmartToy,
                            gradient = Brush.linearGradient(listOf(Color(0xFF8B0000), Color(0xFFC62828)))
                        ) { showBotDialog = true }
                    }
                }
            }

            // Dialogs
            if (showBotDialog) {
                AlertDialog(
                    onDismissRequest = { showBotDialog = false },
                    containerColor = Color(0xFF1A0000),
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Confirm Match", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure?\nMatch fee: 25 coins", color = Color.White.copy(alpha = 0.8f)) },
                    confirmButton = {
                        TextButton(onClick = { showBotDialog = false; onGameModeClick("Bot", 25) }) {
                            Text("Yes", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBotDialog = false }) {
                            Text("No", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                )
            }
            if (showTwoPlayerDialog) { TwoPlayerDialog(gameMode = "2P", walletBalance = coins, onDismiss = { showTwoPlayerDialog = false }, onStartGame = { mode, bet -> showTwoPlayerDialog = false; onGameModeClick(mode, bet) }) }
            if (showFourPlayerDialog) { FourPlayerDialog(walletBalance = coins, onDismiss = { showFourPlayerDialog = false }, onPlayBet = { bet -> showFourPlayerDialog = false; onGameModeClick("4P", bet) }) }
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = tween(100))

    Card(
        modifier = Modifier
            .size(150.dp)
            .scale(scale)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(width = 2.5.dp, color = Color(0xFFFFD700))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient, shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(icon, title, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(6.dp))
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(coins = 0, userId = "101221460378", isGuest = true, onProfileClick = {}, onSettingsClick = {}, onGameModeClick = { _, _ -> }, onNavItemClick = {}, onFriendsClick = {})
}