package com.example.final_for_ind.screens.start



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinEntryDialog(
    title: String = "Enter Withdrawal PIN",
    message: String = "Enter your 4-digit PIN to continue.",
    isLoading: Boolean = false,
    errorMessage: String = "",
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    onForgotPin: () -> Unit = {}
) {

    var pin by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {

            if (!isLoading) {

                onDismiss()
            }
        },
        containerColor = Color(0xFF1A0000),
        shape = RoundedCornerShape(24.dp),
        title = {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Color(0xFFFFD700),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = {

                        pin = it.filter { c ->
                            c.isDigit()
                        }.take(4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {

                        Text(
                            "PIN",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2B0000),
                        unfocusedContainerColor = Color(0xFF2B0000).copy(alpha = 0.7f),
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                        cursorColor = Color(0xFFFFD700),
                        focusedLabelColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage.isNotEmpty()) {

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !isLoading,
                            onClick = onForgotPin
                        )
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "Forgot PIN?",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {

            TextButton(
                enabled = pin.length == 4 && !isLoading,
                onClick = { onSubmit(pin) }
            ) {

                Text(
                    text = if (isLoading) "Verifying..." else "CONFIRM",
                    color = if (pin.length == 4 && !isLoading)
                        Color(0xFFFFD700)
                    else
                        Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {

            TextButton(
                enabled = !isLoading,
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    )
}