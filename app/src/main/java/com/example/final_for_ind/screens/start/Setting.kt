package com.example.final_for_ind.screens.start

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.final_for_ind.network.GameSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInvite: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // 👇 BLACK RED GRADIENT
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    // ==========================================================
    // SHARED PREFERENCES
    // ==========================================================

    val prefs = remember {
        context.getSharedPreferences(
            "ludo_session_prefs",
            Context.MODE_PRIVATE
        )
    }

    // ==========================================================
    // DIALOG STATES
    // ==========================================================

    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ==========================================================
    // SOUND EFFECTS — PERSISTED
    //
    // Read initial value from SharedPreferences.
    // Save on every change.
    // ==========================================================

    var soundEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(
                "sound_enabled",
                true
            )
        )
    }

    // ==========================================================
    // SERVER CONFIG — support contacts, terms, version
    //
    // Fetched once on first composition.
    // Defaults used until server responds.
    // ==========================================================

    var supportWhatsapp by remember {
        mutableStateOf("+923001234567")
    }

    var supportTelegram by remember {
        mutableStateOf("yourusername")
    }

    var termsText by remember {
        mutableStateOf(
            "1. App ka misuse mat karo\n" +
                    "2. Apna account kisi ko share mat karo\n" +
                    "3. Hamara data copy karna mana hai\n" +
                    "4. Support se tameez se baat karo\n" +
                    "5. Rules torney pe account block ho sakta hai"
        )
    }

    LaunchedEffect(Unit) {

        try {

            val sessionManager =
                GameSessionManager(context)

            val config =
                sessionManager.fetchAppConfig()

            if (config != null) {

                supportWhatsapp =
                    config.optString(
                        "support_whatsapp",
                        supportWhatsapp
                    )

                supportTelegram =
                    config.optString(
                        "support_telegram",
                        supportTelegram
                    )

                termsText =
                    config.optString(
                        "terms_text",
                        termsText
                    )
            }

        } catch (e: Exception) {

            // Silent fail — defaults already set
        }
    }

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
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFF2B0000),
                                CircleShape
                            )
                            .border(
                                2.dp,
                                Color(0xFFFFD700),
                                CircleShape
                            )
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        20.dp,
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(
                    2.5.dp,
                    Color(0xFFFFD700)
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1A0000),
                                    Color(0xFF0A0A0A)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(vertical = 8.dp)
                ) {

                    PremiumSettingItem(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = "Edit your info",
                        onClick = onNavigateToProfile
                    )

                    Divider(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Sound Effects",
                        subtitle = if (soundEnabled) "Enabled" else "Disabled",
                        showSwitch = true,
                        switchState = soundEnabled,
                        onSwitchChange = { newValue ->

                            soundEnabled = newValue

                            // ------------------------------------
                            // PERSIST TO SHARED PREFERENCES
                            // ------------------------------------

                            prefs.edit()
                                .putBoolean(
                                    "sound_enabled",
                                    newValue
                                )
                                .apply()
                        }
                    )

                    Divider(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.GroupAdd,
                        title = "Invite Friends",
                        subtitle = "Earn rewards",
                        onClick = onNavigateToInvite
                    )

                    Divider(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.HeadsetMic,
                        title = "Customer Support",
                        subtitle = "24/7 help",
                        onClick = { showSupportDialog = true }
                    )

                    Divider(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    PremiumSettingItem(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions",
                        subtitle = "Read policies",
                        onClick = { showTermsDialog = true }
                    )

                    Divider(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        thickness = 1.dp,
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
                confirmColor = Color(0xFFD32F2F),
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

                    openWhatsApp(
                        context,
                        supportWhatsapp
                    )

                    showSupportDialog = false
                },
                onDismiss = {

                    openTelegram(
                        context,
                        supportTelegram
                    )

                    showSupportDialog = false
                }
            )
        }

        if (showTermsDialog) {

            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color(0xFF1A0000),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        "Terms & Conditions",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        termsText,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTermsDialog = false }
                    ) {
                        Text(
                            "OK",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
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
        animationSpec = tween(100)
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
                    if (isDestructive)
                        Color(0xFFD32F2F).copy(alpha = 0.2f)
                    else
                        Color(0xFFFFD700).copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive)
                    Color(0xFFD32F2F)
                else
                    Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isDestructive)
                    Color(0xFFD32F2F)
                else
                    Color.White,
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
                    checkedTrackColor = Color(0xFFD32F2F),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFFD700).copy(alpha = 0.5f),
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
    confirmColor: Color = Color(0xFFD32F2F),
    dismissColor: Color = Color.White.copy(alpha = 0.7f),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0000),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                title,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                message,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = confirmColor,
                    fontWeight = FontWeight.Bold
                )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen(
        onBack = {},
        onNavigateToProfile = {},
        onNavigateToInvite = {},
        onLogout = {}
    )
}