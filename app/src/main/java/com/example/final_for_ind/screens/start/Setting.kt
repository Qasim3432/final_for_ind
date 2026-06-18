package com.example.final_for_ind.screens.start

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInvite: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
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
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(20.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(vertical = 8.dp)
                ) {
                    PremiumSettingItem(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = "Edit your info",
                        onClick = onNavigateToProfile
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                        showSwitch = true,
                        switchState = notificationsEnabled,
                        onSwitchChange = { notificationsEnabled = it }
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.GroupAdd,
                        title = "Invite Friends",
                        subtitle = "Earn rewards",
                        onClick = onNavigateToInvite
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.HeadsetMic,
                        title = "Customer Support",
                        subtitle = "24/7 help",
                        onClick = { showSupportDialog = true }
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions",
                        subtitle = "Read policies",
                        onClick = { showTermsDialog = true }
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Logout",
                        subtitle = "Sign out",
                        isDestructive = true,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }


        if (showLogoutDialog) {
            PremiumDialog(
                title = "Logout",
                message = "Kya tum sach mein logout karna chahte ho?",
                confirmText = "Yes, Logout",
                confirmColor = Color(0xFFFF5252),
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        if (showSupportDialog) {
            PremiumDialog(
                title = "Contact Support",
                message = "Kahan se contact karna chahte ho?",
                confirmText = "WhatsApp",
                dismissText = "Telegram",
                confirmColor = Color(0xFF25D366),
                dismissColor = Color(0xFF0088cc),
                onConfirm = {
                    openWhatsApp(context, "+923001234567")
                    showSupportDialog = false
                },
                onDismiss = {
                    openTelegram(context, "yourusername")
                    showSupportDialog = false
                }
            )
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color(0xFF2C3E50),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        "Terms & Conditions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "1. App ka misuse mat karo\n2. Apna account kisi ko share mat karo\n3. Hamara data copy karna mana hai\n4. Support se tameez se baat karo\n5. Rules torney pe account block ho sakta hai",
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text("OK", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun PremiumSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showSwitch: Boolean = false,
    switchState: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "item_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !showSwitch,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDestructive) Color.Red.copy(alpha = 0.2f)
                    else Color(0xFFFFD700).copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) Color.Red else Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))


        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isDestructive) Color.Red else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }


        if (showSwitch) {
            Switch(
                checked = switchState,
                onCheckedChange = onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF38ef7d),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PremiumDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String = "Cancel",
    confirmColor: Color = Color(0xFF38ef7d),
    dismissColor: Color = Color.White.copy(alpha = 0.7f),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C3E50),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Text(message, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = dismissColor)
            }
        }
    )
}

fun openWhatsApp(context: Context, number: String) {
    val url = "https://wa.me/${number.replace("+", "").replace(" ", "")}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun openTelegram(context: Context, username: String) {
    val url = "https://t.me/$username"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}