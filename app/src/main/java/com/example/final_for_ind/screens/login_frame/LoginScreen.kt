package com.example.final_for_ind.screens.login_frame

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String, name: String) -> Unit = { _, _, _ -> }, // 👈 name add kiya
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val canLogin = email.isNotEmpty() && password.isNotEmpty()

    // 👇 email se name nikalne ka simple logic
    val displayName = if (email.isNotEmpty()) {
        email.substringBefore("@").replaceFirstChar { it.uppercase() }
    } else "User"

    Box(
        modifier = Modifier.fillMaxSize().background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.5.dp, Color(0xFFFFD700))
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))),
                    shape = RoundedCornerShape(28.dp)
                ).padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // TITLE
                    Text(
                        "Welcome Back",
                        color = Color(0xFFFFD700),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Login to continue",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        letterSpacing = 0.3.sp
                    )

                    Spacer(Modifier.height(40.dp))

                    // EMAIL FIELD
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Phone Num", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Numbers, null, tint = Color(0xFFFFD700))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                            cursorColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    // PASSWORD FIELD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Password", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFFFFD700))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    "Toggle password",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                            cursorColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(36.dp))

                    // LOGIN BUTTON
                    Button(
                        onClick = {
                            if (!canLogin) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onLogin(email, password, displayName) // 👈 name bhej diya
                        },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        enabled = canLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(),
                        border = BorderStroke(2.5.dp, Color(0xFFFFD700))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                if (canLogin)
                                    Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F)))
                                else
                                    Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Login",
                                color = if (canLogin) Color.White else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // SIGN UP ROW
                    Row {
                        Text("Don't have an account? ", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        TextButton(onClick = onSignUpClick) {
                            Text(
                                "Sign Up",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    LoginScreen()
}