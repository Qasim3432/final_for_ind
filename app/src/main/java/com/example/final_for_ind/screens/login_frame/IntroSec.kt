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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay


@Composable
fun IntroSec(
    // ✅ FIX #1: Uri add kiya — profile image ab submit hogi
    onSubmit: (String, String, Uri?) -> Unit,
    // ✅ FIX #3: Loading state
    isLoading: Boolean = false
) {
    // ✅ FIX #6: rememberSaveable — rotation pe data preserve
    var nickname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var profileImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // ✅ FIX #9: Auto-focus nickname on load
    val focusManager = LocalFocusManager.current
    val nicknameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(300)   // smooth entry
        nicknameFocusRequester.requestFocus()
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // 👇 SAME GRADIENT AS FIRST_SCREEN
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    // ✅ FIX #4 + #5: Trim + nickname length validation
    val nicknameTrimmed = nickname.trim()
    val emailTrimmed = email.trim()

    val isNicknameValid = nicknameTrimmed.length in 3..20
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()

    val isFormValid = isNicknameValid && isEmailValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                // ✅ FIX #2: Scroll + IME padding — keyboard open pe button visible
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 👇 SAME LOGO STYLE AS FIRST_SCREEN
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.5.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎮", fontSize = 56.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Create Profile",
                color = Color(0xFFFFD700),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                "Setup your gaming identity",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            // 👇 SAME CARD AS FIRST_SCREEN
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        // PROFILE IMAGE UPLOAD
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .border(2.5.dp, Color(0xFFFFD700), CircleShape)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        "Upload",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD700), CircleShape)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    null,
                                    tint = Color(0xFF1A0000),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = if (profileImageUri == null)
                                "Tap to upload photo"
                            else
                                "Tap to change",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )

                        // ==========================================
                        // INPUT FIELDS
                        // ==========================================

                        // ✅ FIX #7: IME Next on nickname + auto-focus
                        GlassTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = "Username",
                            icon = Icons.Default.Tag,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onImeAction = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            modifier = Modifier.focusRequester(nicknameFocusRequester)
                        )

                        // ✅ FIX #5: Nickname validation hint
                        if (nickname.isNotBlank() && !isNicknameValid) {
                            Text(
                                "Username 3-20 characters ka hona chahiye",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        // ✅ FIX #7: IME Done on email — submit on enter
                        GlassTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                if (isFormValid && !isLoading) {
                                    focusManager.clearFocus()
                                    onSubmit(
                                        nicknameTrimmed,
                                        emailTrimmed,
                                        profileImageUri
                                    )
                                }
                            }
                        )

                        // ✅ FIX #4: Email validation hint
                        if (email.isNotBlank() && !isEmailValid) {
                            Text(
                                "Valid email address daalein",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // 👇 SAME BUTTON STYLE with loading
                        LoginPremiumButton(
                            text = if (isLoading) "PLEASE WAIT..." else "ENTER GAME",
                            gradient = Brush.horizontalGradient(
                                listOf(Color(0xFF8B0000), Color(0xFFD32F2F))
                            ),
                            enabled = isFormValid && !isLoading,
                            isLoading = isLoading,
                            onClick = {
                                focusManager.clearFocus()
                                // ✅ FIX #1: Uri pass ho rahi hai
                                onSubmit(
                                    nicknameTrimmed,
                                    emailTrimmed,
                                    profileImageUri
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "2-4 Players • Quick Matches • Real Rewards",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ==========================================================
// GLASS TEXT FIELD (with IME support)
// ==========================================================

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    // ✅ FIX #7: IME params
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(label, color = Color.White.copy(alpha = 0.6f))
        },
        leadingIcon = {
            Icon(
                icon,
                null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.5f),
            unfocusedContainerColor = Color(0xFF1A0000).copy(alpha = 0.3f),
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
            focusedLabelColor = Color(0xFFFFD700),
            cursorColor = Color(0xFFFFD700)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}


// ==========================================================
// PREMIUM BUTTON (with loading)
// ==========================================================

@Composable
fun LoginPremiumButton(
    text: String,
    gradient: Brush,
    enabled: Boolean = true,
    // ✅ FIX #3: Loading state
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) gradient
                else Brush.horizontalGradient(
                    listOf(Color.Gray, Color.DarkGray)
                ),
                RoundedCornerShape(18.dp)
            )
            .border(
                2.5.dp,
                if (enabled) Color(0xFFFFD700) else Color.Gray,
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        } else {
            Text(
                text = text,
                color = if (enabled)
                    Color.White
                else
                    Color.White.copy(alpha = 0.5f),
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IntroSecPreview() {
    IntroSec(
        onSubmit = { _, _, _ -> },
        isLoading = false
    )
}