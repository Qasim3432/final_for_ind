package com.example.final_for_ind.screens.component

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    currentBalance: Int = 0, // 🟢 Defaulting dynamically from balance sync
    onBack: () -> Unit = {},
    onWithdrawSubmitted: (amount: Int, method: String, accountTitle: String, accountNumber: String) -> Unit = { _, _, _, _ -> }
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("JAZZCASH") }

    // Interactive User Details Fields
    var accountTitle by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // 👇 1 WALE KA THEME COPY KIYA
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000)) // Black to Dark Red
    )
    val gold = Color(0xFFFFD700)
    val redDark = Color(0xFF2B0000)
    val redBright = Color(0xFFD32F2F)

    val quickAmounts = listOf(100, 500, 1000, currentBalance)
    val enteredAmount = amount.toIntOrNull() ?: 0
    val isValidAmount = enteredAmount > 0 && enteredAmount <= currentBalance

    // Input validator rule checks
    val isFormValid = isValidAmount && accountTitle.isNotBlank() && accountNumber.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw Coins", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = gold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
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

            // Live Available Balance Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, gold)
            ) {
                Box(
                    modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A)))).padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = gold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Available Balance", color = gold, fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("$currentBalance", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("1. Enter Withdrawal Value", color = gold, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = amount.isNotEmpty() && !isValidAmount,
                trailingIcon = {
                    TextButton(onClick = { amount = currentBalance.toString() }) {
                        Text("MAX", color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = if (isValidAmount || amount.isEmpty()) gold else redBright,
                    unfocusedBorderColor = gold.copy(alpha = 0.3f),
                    cursorColor = gold,
                    errorBorderColor = redBright
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            if (amount.isNotEmpty() && !isValidAmount) {
                Text(
                    text = if (enteredAmount > currentBalance) "Amount exceeds available balance" else "Please enter a valid amount",
                    color = redBright, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                quickAmounts.forEach { quickAmount ->
                    FilterChip(
                        selected = amount == quickAmount.toString(),
                        onClick = { amount = quickAmount.toString() },
                        label = { Text(if (quickAmount == currentBalance) "All" else "$quickAmount", fontWeight = if (amount == quickAmount.toString()) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gold,
                            selectedLabelColor = Color(0xFF1A0000),
                            containerColor = redDark,
                            labelColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
            Text("2. Choose Receiving Method", color = gold, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))

            val methodsList = listOf("JAZZCASH" to "JazzCash", "EASYPAISA" to "EasyPaisa", "BINANCE" to "Binance")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                methodsList.forEach { (key, title) ->
                    val isSelected = selectedMethod == key
                    val bgColor by animateColorAsState(
                        if (isSelected) gold.copy(alpha = 0.15f) else redDark.copy(alpha = 0.6f),
                        label = "bg"
                    )
                    val borderWidth by animateDpAsState(if (isSelected) 2.5.dp else 2.dp, label = "border")

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedMethod = key
                            accountTitle = ""
                            accountNumber = ""
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(borderWidth, if (isSelected) gold else gold.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (key == "BINANCE") Icons.Default.Star else Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = if (key == "BINANCE") Color(0xFFF0B90B) else Color.White
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = gold)
                                }
                            }

                            // 🟢 DYNAMIC FIELDS INJECTION BLOCK
                            AnimatedVisibility(visible = isSelected) {
                                Column {
                                    Spacer(Modifier.height(16.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                    Spacer(Modifier.height(12.dp))

                                    // Account Title Field
                                    OutlinedTextField(
                                        value = accountTitle,
                                        onValueChange = { accountTitle = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(text = if (key == "BINANCE") "Binance Account Name" else "Account Title Name", color = Color.White.copy(alpha = 0.6f)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    // Account Identification Target Number Field
                                    OutlinedTextField(
                                        value = accountNumber,
                                        onValueChange = { accountNumber = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(text = if (key == "BINANCE") "Binance Pay ID / Email" else "Account Number / Phone", color = Color.White.copy(alpha = 0.6f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = if (key == "BINANCE") KeyboardType.Text else KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // Submit Processing Action Button
            Button(
                onClick = {
                    if (isFormValid) {
                        onWithdrawSubmitted(enteredAmount, selectedMethod, accountTitle, accountNumber)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, gold)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (isFormValid) Brush.horizontalGradient(listOf(Color(0xFF8B0000), redBright)) // RED
                        else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Submit Withdrawal Request", color = if (isFormValid) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.5.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}