package com.example.final_for_ind.screens.deposit

import android.util.Log
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

data class PaymentMethod(
    val name: String,
    val country: String,
    val icon: String = "💳"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    currentBalance: Int = 1250,
    onBack: () -> Unit = {},
    onDeposit: (amount: Int) -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 👇 BLACK RED GRADIENT
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val quickAmounts = listOf(100, 500, 1000, 5000)
    val enteredAmount = amount.toIntOrNull() ?: 0
    val canDeposit = selectedMethod != null && enteredAmount > 0

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
                    Text("Deposit Coins", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFD700)) // 👈 GOLDEN
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
                    modifier = Modifier.background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(24.dp)).padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Wallet, null, tint = Color(0xFFFFD700), modifier = Modifier.size(30.dp)) // 👈 GOLDEN
                        Spacer(Modifier.width(14.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Balance", color = Color(0xFFFFD700), fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN
                            Spacer(Modifier.height(4.dp))
                            Text("$currentBalance", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFFD700), // 👈 GOLDEN
                    unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                    cursorColor = Color(0xFFFFD700)
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(20.dp))

            // QUICK AMOUNTS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                quickAmounts.forEach { quickAmount ->
                    FilterChip(
                        selected = amount == quickAmount.toString(),
                        onClick = { amount = quickAmount.toString() },
                        label = {
                            Text("$quickAmount", fontWeight = if (amount == quickAmount.toString()) FontWeight.Bold else FontWeight.Medium)
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

            Text("Select Payment Method", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // 👈 GOLDEN
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedMethod?.name == method.name
                    val bgColor by animateColorAsState(
                        if (isSelected) Color(0xFFFFD700).copy(alpha = 0.15f) else Color(0xFF2B0000).copy(alpha = 0.6f), // 👈 GOLDEN/RED
                        label = "bg"
                    )
                    val borderWidth by animateDpAsState(if (isSelected) 2.5.dp else 2.dp, label = "border")

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedMethod = method },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(borderWidth, if (isSelected) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f)) // 👈 GOLDEN
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(method.icon, fontSize = 30.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.name, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                Text(method.country, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFFD700), // 👈 GOLDEN
                                    unselectedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // DEPOSIT BUTTON
            Button(
                onClick = {
                    val depositAmount = amount.toIntOrNull()
                    when {
                        selectedMethod == null -> Toast.makeText(context, "Select payment method", Toast.LENGTH_SHORT).show()
                        depositAmount == null || depositAmount <= 0 -> Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                        else -> {
                            Log.d("DEPOSIT", "Deposit $depositAmount via ${selectedMethod!!.name}")
                            onDeposit(depositAmount)
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = canDeposit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (canDeposit)
                            Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))) // 👈 RED
                        else
                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Deposit Now",
                        color = if (canDeposit) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DepositPreview() {
    DepositScreen()
}