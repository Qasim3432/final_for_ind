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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
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
                    Text(
                        "Withdraw Coins",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(24.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Wallet, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Available Balance", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("$currentBalance", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Enter Amount", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    amount = newValue.filter { it.isDigit() }.take(6)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = amount.isNotEmpty() && !isValidAmount,
                trailingIcon = {
                    TextButton(onClick = { amount = currentBalance.toString() }) {
                        Text("MAX", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (isValidAmount || amount.isEmpty()) Color(0xFF00D4FF) else Color(0xFFFF5252),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    cursorColor = Color(0xFF00D4FF),
                    errorBorderColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            if (amount.isNotEmpty() && !isValidAmount) {
                Text(
                    "Amount exceeds balance",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickAmounts.forEach { quickAmount ->
                    FilterChip(
                        selected = amount == quickAmount.toString(),
                        onClick = { amount = quickAmount.toString() },
                        label = {
                            Text(
                                if (quickAmount == currentBalance) "All" else "$quickAmount",
                                fontWeight = if (amount == quickAmount.toString()) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00D4FF),
                            selectedLabelColor = Color.Black,
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Text("Select Withdrawal Method", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))


            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentMethods.forEach { method ->
                    val isSelected = selectedMethod?.name == method.name
                    val bgColor by animateColorAsState(
                        if (isSelected) Color(0xFF00D4FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        label = "bg"
                    )
                    val borderWidth by animateDpAsState(
                        if (isSelected) 2.dp else 1.dp,
                        label = "border"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(borderWidth, if (isSelected) Color(0xFF00D4FF) else Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method.icon, fontSize = 28.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.name, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                Text(method.country, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00D4FF), modifier = Modifier.size(24.dp))
                            } else {
                                RadioButton(
                                    selected = false,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(unselectedColor = Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))


            Button(
                onClick = {
                    if (!canWithdraw) {
                        Toast.makeText(
                            context,
                            if (selectedMethod == null) "Select payment method" else "Enter valid amount",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    onWithdraw(enteredAmount)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = canWithdraw,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canWithdraw)
                                Brush.horizontalGradient(listOf(Color(0xFF00D4FF), Color(0xFF0099CC)))
                            else
                                Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Withdraw Now",
                        color = if (canWithdraw) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WithdrawPreview() {
    WithdrawScreen()
}