package com.example.final_for_ind.screens.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Preview
@Composable
fun DashboardScreen() {
    var balance by remember { mutableStateOf(2900) }
    var amountInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C3E50))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF34495E))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF9B59B6),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Wallet",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Balance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color(0xFFF1C40F),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Balance: ",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$$balance",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Add Coins Section
                    SectionTitle("Add Balance (Test)")

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = {
                            // Only allow digits and limit length to prevent overflow
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                amountInput = it
                            }
                        },
                        label = { Text("Enter amount", color = Color.Gray) },
                        placeholder = { Text("0", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    GradientButton(
                        text = "Add Coins",
                        icon = Icons.Default.Games,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF2980B9), Color(0xFF3498DB))),
                        onClick = {
                            val amount = amountInput.toIntOrNull()
                            if (amount != null && amount > 0) {
                                balance += amount
                                amountInput = ""
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a valid positive number")
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    GradientButton(
                        text = "Deposit",
                        icon = Icons.Default.Download,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFF39C12), Color(0xFFF1C40F))),
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Deposit feature coming soon!")
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    GradientButton(
                        text = "Withdraw",
                        icon = Icons.Default.Upload,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFC0392B), Color(0xFFE74C3C))),
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Withdraw feature coming soon!")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}