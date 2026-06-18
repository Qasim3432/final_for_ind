package com.example.final_for_ind.screens.referral

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    friendName: String = "Ali Khan",
    onBack: () -> Unit = {},
    onCreateLink: () -> Unit = {},
    onShareLink: () -> Unit = {},
    onCopyLink: () -> Unit = {}
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    var selectedBet by remember { mutableStateOf(1000) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customBet by remember { mutableStateOf("") }
    var roomCreated by remember { mutableStateOf(false) }
    var roomLink by remember { mutableStateOf("yourapp://room/ABC123") }
    var copied by remember { mutableStateOf(false) }

    val betOptions = listOf(400, 1000, 2000, 5000, -1)

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Private Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Avatar with gradient ring
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFB700))
                                    ),
                                    CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C3E50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Playing with",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            friendName,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Min 2 • Max 4 Players",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(
                visible = !roomCreated,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Choose Bet Amount",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        betOptions.forEach { bet ->
                            BetChip(
                                bet = bet,
                                isSelected = selectedBet == bet,
                                modifier = Modifier.weight(1f), // <- weight yahan diya
                                onClick = {
                                    if (bet == -1) showCustomDialog = true
                                    else selectedBet = bet
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    PremiumButton(
                        text = "Create Room - $selectedBet Coins",
                        gradient = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF38ef7d),
                                Color(0xFF11998e)
                            )
                        ),
                        onClick = {
                            roomCreated = true
                            onCreateLink()
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = roomCreated,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.25f),
                                            Color(0xFF2ECC71).copy(alpha = 0.25f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    1.5.dp,
                                    Color(0xFF2ECC71).copy(alpha = 0.5f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(28.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "🎉 Room Created!",
                                    color = Color(0xFF2ECC71),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Bet: $selectedBet coins",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Waiting for players to join...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))


                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    "Room Link",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        roomLink,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (copied) Color(0xFF38ef7d) else Color(
                                                    0xFFFFD700
                                                ), CircleShape
                                            )
                                            .clickable {
                                                onCopyLink()
                                                copied = true
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Icon(
                                            if (copied) Icons.Default.Done else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF1A252F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (copied) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Copied!",
                                        color = Color(0xFF38ef7d),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    PremiumButton(
                        text = "Share & Reserve Room",
                        icon = Icons.Default.Share,
                        gradient = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00c9ff),
                                Color(0xFF92fe9d)
                            )
                        ),
                        textColor = Color(0xFF1A252F),
                        onClick = onShareLink
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "2 to 4 players can join this room",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }


    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = Color(0xFF2C3E50),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Custom Bet Amount",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = customBet,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 6) customBet = it
                    },
                    label = { Text("Enter coins", color = Color.White.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = Color(0xFFFFD700),
                        cursorColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customBet.isNotEmpty() && customBet.toInt() > 0) {
                        selectedBet = customBet.toInt()
                        showCustomDialog = false
                        customBet = ""
                    }
                }) {
                    Text("Set Bet", color = Color(0xFF38ef7d), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomDialog = false
                    customBet = ""
                }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
fun BetChip(
    bet: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "bet_scale"
    )

    Box(
        modifier = modifier
            .height(70.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isSelected)
                    Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB700)))
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                if (isSelected) 2.5.dp else 1.5.dp,
                if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (bet == -1) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Custom",
                    tint = if (isSelected) Color(0xFF1A252F) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Custom",
                    color = if (isSelected) Color(0xFF1A252F) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    "$bet",
                    color = if (isSelected) Color(0xFF1A252F) else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "coins",
                    color = if (isSelected) Color(0xFF1A252F).copy(alpha = 0.7f) else Color.White.copy(
                        alpha = 0.6f
                    ),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    gradient: Brush,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .border(2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateRoomPreview() {
    CreateRoomScreen()
}