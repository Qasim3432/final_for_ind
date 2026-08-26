package com.example.final_for_ind.screens.component

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PaymentMethod(val name: String, val country: String, val icon: String = "💳")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    currentBalance: Int = 1250,
    onBack: () -> Unit = {},
    onWithdraw: (amount: Int) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 👇 BLACK RED GRADIENT
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val quickAmounts = listOf(100, 500, 1000, currentBalance)
    val enteredAmount = amount.toIntOrNull() ?: 0
    val isValidAmount = enteredAmount > 0 && enteredAmount <= currentBalance
    val canWithdraw = selectedMethod != null && isValidAmount

    val paymentMethods = listOf(
        PaymentMethod("PhonePe", "India", "📱"),
        PaymentMethod("Paytm", "India", "💙"),
        PaymentMethod("Razorpay", "India", "⚡"),
        PaymentMethod("PayU", "India", "💰"),
        PaymentMethod("JazzCash", "Pakistan", "🟢"),
        PaymentMethod("EasyPaisa", "Pakistan", "🟡"),
        PaymentMethod("NayaPay", "Pakistan", "🔵"),
        PaymentMethod("SadaPay", "Pakistan", "🟠"),
        PaymentMethod("Binance", "Crypto", "₿")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Withdraw Coins", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFD700)) // 👈 GOLDEN
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFFFFD700)) // 👈 GOLDEN
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(bgGradient).padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(24.dp))

            // BALANCE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A)))).padding(24.dp) // 👈 BLACK RED
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wallet, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp)) // 👈 GOLDEN
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Available Balance", color = Color(0xFFFFD700), fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN
                            Spacer(Modifier.height(4.dp))
                            Text("$currentBalance", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Enter Amount", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // 👈 GOLDEN
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue -> amount = newValue.filter { it.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = amount.isNotEmpty() && !isValidAmount,
                trailingIcon = {
                    TextButton(onClick = { amount = currentBalance.toString() }) {
                        Text("MAX", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp) // 👈 GOLDEN
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (isValidAmount || amount.isEmpty()) Color(0xFFFFD700) else Color(0xFFD32F2F), // 👈 GOLDEN/RED
                    unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                    cursorColor = Color(0xFFFFD700),
                    errorBorderColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            if (amount.isNotEmpty() && !isValidAmount) {
                Text("Amount exceeds balance", color = Color(0xFFD32F2F), fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 6.dp)) // 👈 RED
            }

            Spacer(Modifier.height(20.dp))

            // QUICK AMOUNTS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                quickAmounts.forEach { quickAmount ->
                    FilterChip(
                        selected = amount == quickAmount.toString(),
                        onClick = { amount = quickAmount.toString() },
                        label = {
                            Text(if (quickAmount == currentBalance) "All" else "$quickAmount", fontWeight = if (amount == quickAmount.toString()) FontWeight.Bold else FontWeight.Medium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700), // 👈 GOLDEN
                            selectedLabelColor = Color(0xFF1A0000), // 👈 DARK RED
                            containerColor = Color(0xFF2B0000), // 👈 DARK RED
                            labelColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color(0xFFFFD700)), // 👈 GOLDEN
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Text("Select Withdrawal Method", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // 👈 GOLDEN
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedMethod?.name == method.name
                    val bgColor by animateColorAsState(
                        if (isSelected) Color(0xFFFFD700).copy(alpha = 0.15f) else Color(0xFF2B0000).copy(alpha = 0.6f), // 👈 GOLDEN/RED
                        label = "bg"
                    )
                    val borderWidth by animateDpAsState(if (isSelected) 2.5.dp else 2.dp, label = "border")

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedMethod = method },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(borderWidth, if (isSelected) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f)) // 👈 GOLDEN
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(method.icon, fontSize = 28.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.name, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                Text(method.country, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp)) // 👈 GOLDEN
                            } else {
                                RadioButton(selected = false, onClick = null, colors = RadioButtonDefaults.colors(unselectedColor = Color.White.copy(alpha = 0.3f)))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // WITHDRAW BUTTON
            Button(
                onClick = {
                    if (!canWithdraw) {
                        Toast.makeText(context, if (selectedMethod == null) "Select payment method" else "Enter valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onWithdraw(enteredAmount)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = canWithdraw,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (canWithdraw)
                            Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))) // 👈 RED
                        else
                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Withdraw Now", color = if (canWithdraw) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WithdrawPreview() {
    WithdrawScreen()
}