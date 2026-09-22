package com.example.final_for_ind.screens.login_frame

import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun IntroSec(
    onSubmit: (String, String) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // 👇 SAME GRADIENT AS FIRST_SCREEN
    val gradient = Brush.verticalGradient(colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000)))

    val isFormValid = nickname.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 👇 SAME LOGO STYLE AS FIRST_SCREEN
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)))
                    .border(2.5.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎮", fontSize = 56.sp)
            }

            Spacer(Modifier.height(24.dp))
            Text(text = "Create Profile", color = Color(0xFFFFD700), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text("Setup your gaming identity", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)

            Spacer(Modifier.height(40.dp))

            // 👇 SAME CARD AS FIRST_SCREEN
            Card(
                modifier = Modifier.fillMaxWidth().shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), RoundedCornerShape(28.dp))
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {

                        // PROFILE IMAGE UPLOAD
                        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier.size(110.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    .border(2.5.dp, Color(0xFFFFD700), CircleShape)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    AsyncImage(model = profileImageUri, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.CameraAlt, "Upload", modifier = Modifier.size(40.dp), tint = Color.White.copy(alpha = 0.7f))
                                }
                            }
                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).background(Color(0xFFFFD700), CircleShape).clickable { imagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF1A0000), modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(text = if (profileImageUri == null) "Tap to upload photo" else "Tap to change", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                        // INPUT FIELDS
                        GlassTextField(value = nickname, onValueChange = { nickname = it }, label = "Username", icon = Icons.Default.Tag, keyboardType = KeyboardType.Text)
                        GlassTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(Modifier.height(12.dp))

                        // 👇 SAME BUTTON STYLE
                        LoginPremiumButton(
                            text = "ENTER GAME",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))),
                            enabled = isFormValid,
                            onClick = { onSubmit(nickname, email) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("2-4 Players • Quick Matches • Real Rewards", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.5f),
            unfocusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.3f),
            focusedBorderColor = Color(0xFFFFD700), unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
            focusedLabelColor = Color(0xFFFFD700), cursorColor = Color(0xFFFFD700)
        ),
        shape = RoundedCornerShape(16.dp), singleLine = true
    )
}

// 👇 SAME BUTTON AS FIRST_SCREEN
@Composable
fun LoginPremiumButton(
    text: String,
    gradient: Brush,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(100))

    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).scale(scale).clip(RoundedCornerShape(18.dp))
            .background(if(enabled) gradient else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)), RoundedCornerShape(18.dp))
            .border(2.5.dp, if(enabled) Color(0xFFFFD700) else Color.Gray, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if(enabled) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IntroSecPreview() {
    IntroSec(onSubmit = { _, _ -> })
}