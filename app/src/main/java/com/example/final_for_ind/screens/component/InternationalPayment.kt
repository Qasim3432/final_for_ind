package com.example.final_for_ind.screens.component

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CurrencyExchange
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

data class InternationalPaymentMethod(
    val name: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternationalPayment(
    currentBalance: Int = 0,
    onBack: () -> Unit = {},
    onProceed: (amount: Int, method: String) -> Unit = { _, _ -> }
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<InternationalPaymentMethod?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
    )

    val usdtMethods = listOf(
        InternationalPaymentMethod("Binance", "Send USDT via Binance Pay"),
        InternationalPaymentMethod("Bybit", "Send USDT via Bybit Pay")
    )

    val usdtAmount = amount.toDoubleOrNull() ?: 0.0
    val pkrRate = 280
    val inrRate = 83

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Coins by USDT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .background(gradient)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(20.dp))

            // USDT Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF26A17B).copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CurrencyExchange,
                            null,
                            tint = Color(0xFF26A17B),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("USDT Rate", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("1 USDT = $pkrRate PKR", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("1 USDT = $inrRate INR", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Enter USDT Amount", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.CurrencyExchange, null, tint = Color(0xFF26A17B))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF26A17B),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF26A17B)
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            if (usdtAmount > 0) {
                Column(modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                    Text(
                        text = "≈ ${(usdtAmount * pkrRate).toInt()} PKR",
                        color = Color(0xFF26A17B),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "≈ ${(usdtAmount * inrRate).toInt()} INR",
                        color = Color(0xFF26A17B),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Payment Methods Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Payment Method", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    usdtMethods.forEach { method ->
                        val isSelected = selectedMethod?.name == method.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF26A17B) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    color = if (isSelected) Color(0xFF26A17B).copy(alpha = 0.1f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMethod = method },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF26A17B),
                                    unselectedColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Default.CurrencyExchange, null, tint = Color(0xFF26A17B))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(method.name, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Text(method.description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Proceed Button
            Button(
                onClick = {
                    val usdtAmountInt = amount.toDoubleOrNull()
                    when {
                        selectedMethod == null -> {
                            Toast.makeText(context, "Please select payment method", Toast.LENGTH_SHORT).show()
                        }
                        usdtAmountInt == null || usdtAmountInt <= 0 -> {
                            Toast.makeText(context, "Please enter valid USDT amount", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            onProceed(usdtAmountInt.toInt(), selectedMethod!!.name)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26A17B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Proceed to Payment",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "Note: Only send USDT on TRC20 network to avoid loss of funds",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InternationalPaymentPreview() {
    InternationalPayment()
}