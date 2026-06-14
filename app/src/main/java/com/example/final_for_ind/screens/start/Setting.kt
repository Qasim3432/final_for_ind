package com.example.final_for_ind.screens.start

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
    )
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) } // naya
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(Modifier.weight(1f))
                Text("Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(60.dp))
            }

            Spacer(Modifier.height(32.dp))

            SettingItem("Profile") { }
            SettingItem("Notifications") { showNotificationDialog = true }
            SettingItem("Customer Support") { showSupportDialog = true }
            SettingItem("Terms & Conditions") { showTermsDialog = true }
            SettingItem("Logout") { showLogoutDialog = true } // popup open hoga
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = Color(0xFF2C3E50),
                title = { Text("Logout", color = Color.White) },
                text = {
                    Text(
                        "Kya tum sach mein logout karna chahte ho?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        // yahan actual logout logic likhna
                        // jaise: clear token, navigate to LoginScreen
                    }) {
                        Text("Yes", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Baqi dialogs same rahenge...
        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                containerColor = Color(0xFF2C3E50),
                title = { Text("Notifications", color = Color.White) },
                text = {
                    Text(
                        "Current status: ${if(notificationsEnabled) "ON" else "OFF"}",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        notificationsEnabled = true
                        showNotificationDialog = false
                    }) {
                        Text("Turn ON", color = Color(0xFF25D366))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        notificationsEnabled = false
                        showNotificationDialog = false
                    }) {
                        Text("Turn OFF", color = Color.Red)
                    }
                }
            )
        }

        if (showSupportDialog) {
            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                containerColor = Color(0xFF2C3E50),
                title = { Text("Contact Support", color = Color.White) },
                text = { Text("Choose where to contact us:", color = Color.White.copy(alpha = 0.8f)) },
                confirmButton = {
                    TextButton(onClick = {
                        openWhatsApp(context, "+923001234567")
                        showSupportDialog = false
                    }) {
                        Text("WhatsApp", color = Color(0xFF25D366))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        openTelegram(context, "yourusername")
                        showSupportDialog = false
                    }) {
                        Text("Telegram", color = Color(0xFF0088cc))
                    }
                }
            )
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color(0xFF2C3E50),
                title = { Text("Terms & Conditions", color = Color.White) },
                text = {
                    Text(
                        "1. App ka misuse mat karo\n2. Apna account kisi ko share mat karo\n" +
                                "3. Hamara data copy karna mana hai\n4. Support se tameez se baat karo\n" +
                                "5. Rules torney pe account block ho sakta hai",
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text("OK", color = Color.White)
                    }
                }
            )
        }
    }
}
@Composable
fun SettingItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(">", color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp)
        }
    }
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
    SettingScreen(onBack = {})
}