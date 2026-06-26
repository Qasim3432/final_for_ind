package com.example.final_for_ind.screens.login_frame

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun IntroSec(
    onSubmit: (String, String, Uri?) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+92") }
    var phone by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    val isFormValid = nickname.isNotBlank() && phone.length >= 10 && countryCode.startsWith("+")

    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f).shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Profile",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "Setup your gaming identity",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    // Profile Image
                    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                        if (profileImageUri != null) {
                            Box(modifier = Modifier.size(120.dp).clip(CircleShape).border(3.dp, Color(0xFFFFD700), CircleShape))
                        }
                        Box(
                            modifier = Modifier.size(110.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, "Upload Photo", Modifier.size(40.dp), tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape)
                                .background(Color(0xFFFFD700), CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF1A252F), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (profileImageUri == null) "Tap to upload photo" else "Tap to change",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(32.dp))

                    GlassTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = "Username",
                        icon = Icons.Default.Tag,
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(Modifier.height(20.dp))

                    // Phone with Country Code - Keyboard Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = countryCode,
                            onValueChange = {
                                if (it.length <= 4 && it.startsWith("+")) countryCode = it
                            },
                            modifier = Modifier.width(95.dp),
                            label = { Text("Code", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = Color(0xFFFFD700),
                                cursorColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )

                        Spacer(Modifier.width(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 11) phone = it
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Phone Number", color = Color.White.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = Color(0xFFFFD700),
                                cursorColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            placeholder = { Text("3012345678", color = Color.White.copy(alpha = 0.4f)) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Example: +92 or +91",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    PremiumSubmitButton(
                        text = "SEND OTP",
                        enabled = isFormValid,
                        onClick = {
                            val fullPhone = countryCode + phone
                            onSubmit(nickname, fullPhone, profileImageUri)
                        }
                    )
                }
            }
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
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            focusedLabelColor = Color(0xFFFFD700),
            cursorColor = Color(0xFFFFD700)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun PremiumSubmitButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (enabled)
                    Brush.horizontalGradient(listOf(Color(0xFF38ef7d), Color(0xFF11998e)))
                else
                    Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))),
                shape = RoundedCornerShape(18.dp)
            )
            .border(2.dp, if (enabled) Color.White.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IntroSecPreview() {
    IntroSec(onSubmit = { name, phone, uri ->
        // Preview ke liye kuch nahi karna
    })
}