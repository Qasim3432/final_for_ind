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
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Warning
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
    val description: String,
    val icon: String = "💚"
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

    // 👇 BLACK RED GRADIENT
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val usdtMethods = listOf(
        InternationalPaymentMethod("Binance", "Send USDT via Binance Pay", "🟢"),
        InternationalPaymentMethod("Bybit", "Send USDT via Bybit Pay", "⚡")
    )

    val usdtAmount = amount.toDoubleOrNull()?: 0.0
    val pkrRate = 280
    val inrRate = 83
    val canProceed = selectedMethod!= null && usdtAmount > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Coins by USDT", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFD700)) // 👈 GOLDEN
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

            // RATE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A)))).padding(24.dp) // 👈 BLACK RED
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyExchange, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp)) // 👈 GOLDEN
                            Spacer(Modifier.width(12.dp))
                            Text("Live USDT Rate", color = Color(0xFFFFD700), fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("1 USDT", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        Text("= $pkrRate PKR", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(4.dp))
                        Text("= $inrRate INR", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Enter USDT Amount", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // 👈 GOLDEN
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    val parts = filtered.split('.')
                    amount = if (parts.size > 2) parts[0] + "." + parts[1] else filtered
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.CurrencyExchange, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp)) // 👈 GOLDEN
                },
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

            if (usdtAmount > 0) {
                Spacer(Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8B0000).copy(alpha = 0.2f)), // 👈 DARK RED
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)) // 👈 GOLDEN
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("You will receive ≈", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row {
                            Text("${(usdtAmount * pkrRate).toInt()} PKR", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN
                            Spacer(Modifier.width(16.dp))
                            Text("${(usdtAmount * inrRate).toInt()} INR", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold) // 👈 GOLDEN
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            Text("Select Payment Method", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) // 👈 GOLDEN
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                usdtMethods.forEach { method ->
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
                            Text(method.icon, fontSize = 32.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.name, color = Color.White, fontSize = 17.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                Text(method.description, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700), unselectedColor = Color.White.copy(alpha = 0.3f)) // 👈 GOLDEN
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // PROCEED BUTTON
            Button(
                onClick = {
                    val usdtAmountDouble = amount.toDoubleOrNull()
                    when {
                        selectedMethod == null -> Toast.makeText(context, "Select payment method", Toast.LENGTH_SHORT).show()
                        usdtAmountDouble == null || usdtAmountDouble <= 0 -> Toast.makeText(context, "Enter valid USDT amount", Toast.LENGTH_SHORT).show()
                        else -> onProceed(usdtAmountDouble.toInt(), selectedMethod!!.name)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = canProceed,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (canProceed)
                            Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))) // 👈 RED
                        else
                            Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Proceed to Payment",
                        color = if (canProceed) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // WARNING CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.08f)),
                border = BorderStroke(2.dp, Color(0xFFFFD700)) // 👈 GOLDEN
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp)) // 👈 GOLDEN
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Only send USDT on TRC20 network. BEP20/ERC20 will cause loss of funds.",
                        color = Color(0xFFFFD700), // 👈 GOLDEN
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InternationalPaymentPreview() {
    InternationalPayment()
}