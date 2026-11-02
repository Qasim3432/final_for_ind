package com.example.final_for_ind.screens.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ==========================================================
// CHAT MESSAGE DATA CLASS
// ==========================================================

data class ChatMessage(
    val senderToken: String,
    val senderName: String,
    val senderColor: String,
    val text: String,
    val timestamp: Long,
    val isMe: Boolean,
)


// ==========================================================
// QUICK EMOJI SET
// ==========================================================

private val QUICK_EMOJIS = listOf(
    "❤️", "😂", "👍", "😮", "😢", "🔥", "💪", "👏",
    "🎯", "😎", "🙏", "💯", "🏆", "⚡", "😡", "🥳"
)


// ==========================================================
// ✅ SPEECH BUBBLE — Shows over profile (8 Ball Pool style)
// ==========================================================

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {

    val borderColor = when (message.senderColor.uppercase()) {
        "BLUE"   -> Color(0xFF42A5F5)
        "RED"    -> Color(0xFFEF5350)
        "GREEN"  -> Color(0xFF66BB6A)
        "YELLOW" -> Color(0xFFFFCA28)
        else     -> Color(0xFFFFD700)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .widthIn(max = 240.dp)
    ) {

        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2B0000), Color(0xFF1A0000))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            // Sender name
            Text(
                text = message.senderName,
                color = borderColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = message.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ==========================================================
// ✅ CHAT BUBBLES OVERLAY — Positions bubbles exactly next to profiles
// ==========================================================

@Composable
fun ChatBubblesOverlay(
    bubbles: Map<String, ChatMessage>,   // color → message
    isTwoPlayerMode: Boolean,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier.fillMaxSize()) {

        // ---- BLUE — Top Start (Left profile, bubble on RIGHT) ----
        bubbles["BLUE"]?.let { msg ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(200)) + slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(250)
                ),
                exit = fadeOut(tween(300)) + slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    // 80dp pushes bubble to the right of blue profile
                    .padding(start = 80.dp, top = 90.dp)
            ) {
                ChatBubble(message = msg)
            }
        }

        // ---- RED — Top End (Right profile, bubble on LEFT) ----
        if (!isTwoPlayerMode) {
            bubbles["RED"]?.let { msg ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200)) + slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(250)
                    ),
                    exit = fadeOut(tween(300)) + slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        // 80dp pushes bubble to the left of red profile
                        .padding(end = 80.dp, top = 90.dp)
                ) {
                    ChatBubble(message = msg)
                }
            }
        }

        // ---- GREEN — Bottom End (Right profile, bubble on LEFT) ----
        bubbles["GREEN"]?.let { msg ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(200)) + slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(250)
                ),
                exit = fadeOut(tween(300)) + slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // 80dp pushes left, 130dp pushes up from bottom profile
                    .padding(end = 80.dp, bottom = 130.dp)
            ) {
                ChatBubble(message = msg)
            }
        }

        // ---- YELLOW — Bottom Start (Left profile, bubble on RIGHT) ----
        if (!isTwoPlayerMode) {
            bubbles["YELLOW"]?.let { msg ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200)) + slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(250)
                    ),
                    exit = fadeOut(tween(300)) + slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // 80dp pushes right, 130dp pushes up
                        .padding(start = 80.dp, bottom = 130.dp)
                ) {
                    ChatBubble(message = msg)
                }
            }
        }
    }
}


// ==========================================================
// ✅ CHAT BUTTON — Small icon to open quick sender
// ==========================================================

@Composable
fun ChatButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                )
            )
            .border(2.dp, Color(0xFF1A0000), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = "Chat",
            tint = Color(0xFF1A0000),
            modifier = Modifier.size(24.dp)
        )

        // Small unread dot
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}


// ==========================================================
// ✅ CHAT QUICK SENDER — Compact popup for sending
// ==========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatQuickSender(
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }

    // Auto-focus input when opened
    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    // Auto-hide keyboard when dismissed
    DisposableEffect(Unit) {
        onDispose { keyboard?.hide() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
    ) {

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A0000)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                // ---- Header ----
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("💬", fontSize = 16.sp)

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "Quick Message",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "Tap to send",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ---- Emoji Row ----
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    items(QUICK_EMOJIS.size) { idx ->

                        val emoji = QUICK_EMOJIS[idx]

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2B0000))
                                .border(
                                    1.dp,
                                    Color(0xFFFFD700).copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSendMessage(emoji)
                                    keyboard?.hide()
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ---- Text Input Row ----
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            if (it.length <= 100) inputText = it
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                "Type a message...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                            focusedContainerColor = Color(0xFF2B0000),
                            unfocusedContainerColor = Color(0xFF2B0000).copy(alpha = 0.6f),
                            cursorColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                    keyboard?.hide()
                                    onDismiss()
                                }
                            }
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    if (inputText.isNotBlank()) {
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    } else {
                                        listOf(Color(0xFF444444), Color(0xFF333333))
                                    }
                                )
                            )
                            .clickable(enabled = inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                                keyboard?.hide()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank())
                                Color(0xFF1A0000)
                            else
                                Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}