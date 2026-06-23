package com.example.final_for_ind.screens.home_lobby

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.screens.component.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    coins: Int = 1250, // 👈 yehi wallet balance hai
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGameModeClick: (String, Int) -> Unit, // 👈 gameMode, betAmount
    onCoinsClick: () -> Unit = {},
    onNavItemClick: (String) -> Unit = {},
    onFriendsClick: () -> Unit = {}
) {
    var showBotDialog by remember { mutableStateOf(false) }
    var showTwoPlayerDialog by remember { mutableStateOf(false) } // 👈 2P dialog state
    var showFourPlayerDialog by remember { mutableStateOf(false) } // 👈 4P dialog state

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        bottomBar = { BottomNavBar(currentRoute = "home", onNavigate = onNavItemClick) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
        ) {

            // Top Bar - same as before
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape)
                            .background(color = Color.White.copy(alpha = 0.1f), shape = CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "Profile", Modifier.size(28.dp), tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Card(
                        modifier = Modifier.height(38.dp).clickable { onCoinsClick() },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700))),
                                shape = RoundedCornerShape(20.dp)
                            ).padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "Coins", tint = Color(0xFF8B5A00), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$coins", color = Color(0xFF8B5A00), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                        .background(color = Color.White.copy(alpha = 0.1f), shape = CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }

            // Game Mode Cards
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select Game Mode",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        GameModeCard(
                            "2 Players",
                            Icons.Default.Person,
                            Brush.linearGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
                        ) { showTwoPlayerDialog = true } // 👈 Dialog khole ga

                        GameModeCard(
                            "4 Players",
                            Icons.Default.Group,
                            Brush.linearGradient(listOf(Color(0xFF00c9ff), Color(0xFF92fe9d)))
                        ) { showFourPlayerDialog = true } // 👈 Dialog khole ga
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        GameModeCard(
                            "Friends",
                            Icons.Default.People,
                            Brush.linearGradient(listOf(Color(0xFFf7971e), Color(0xFFffd200)))
                        ) { onFriendsClick() }
                        GameModeCard(
                            "Computer",
                            Icons.Default.SmartToy,
                            Brush.linearGradient(listOf(Color(0xFF8e2de2), Color(0xFF4a00e0)))
                        ) { showBotDialog = true }
                    }
                }
            }

            // Bot Dialog - same
            if (showBotDialog) {
                AlertDialog(
                    onDismissRequest = { showBotDialog = false },
                    containerColor = Color(0xFF2C3E50),
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Confirm Match", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    text = { Text("Are you sure?\nMatch fee: 25 coins", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp) },
                    confirmButton = {
                        TextButton(onClick = {
                            showBotDialog = false
                            onGameModeClick("Bot", 25)
                        }) { Text("Yes", color = Color(0xFF38ef7d), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBotDialog = false }) {
                            Text("No", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                        }
                    }
                )
            }

            // 👇 2 Player Dialog
            if (showTwoPlayerDialog) {
                TwoPlayerDialog(
                    gameMode = "2P",
                    walletBalance = coins, // 👈 HomeScreen ka coins pass
                    onDismiss = { showTwoPlayerDialog = false },
                    onStartGame = { mode, bet ->
                        showTwoPlayerDialog = false
                        onGameModeClick(mode, bet) // 👈 Game start
                    }
                )
            }

            // 👇 4 Player Dialog
            if (showFourPlayerDialog) {
                FourPlayerDialog(
                    walletBalance = coins, // 👈 HomeScreen ka coins pass
                    onDismiss = { showFourPlayerDialog = false },
                    onPlayBet = { bet ->
                        showFourPlayerDialog = false
                        onGameModeClick("4P", bet) // 👈 Game start
                    }
                )
            }
        }
    }
}

@Composable
fun GameModeCard(text: String, icon: ImageVector, gradient: Brush, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Card(
        modifier = Modifier.size(150.dp).scale(scale)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(brush = gradient, shape = RoundedCornerShape(24.dp))
                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, text, tint = Color.White, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        coins = 3500,
        onProfileClick = {},
        onSettingsClick = {},
        onGameModeClick = { mode, bet -> },
        onNavItemClick = {},
        onFriendsClick = {}
    )
}