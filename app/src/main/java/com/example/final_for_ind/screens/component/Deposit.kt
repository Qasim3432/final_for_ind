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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.network.GameSessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    sessionManager: GameSessionManager,
    onBack: () -> Unit = {},
    onDepositSubmitted: (amount: Int, method: String, senderName: String) -> Unit = { _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentBalance by remember { mutableStateOf(0) }
    var adminPaymentDetails by remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("JAZZCASH") }
    var senderName by remember { mutableStateOf("") }

    // 👇 1 WALE KA THEME COPY KIYA
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000)) // Black to Dark Red
    )
    val gold = Color(0xFFFFD700)
    val redDark = Color(0xFF2B0000)
    val redBright = Color(0xFFD32F2F)

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                val deviceToken = sessionManager.getOrCreateUserToken()
                val balanceResult = sessionManager.fetchUserBalanceFromServer(deviceToken)
                currentBalance = balanceResult.coins
                adminPaymentDetails = sessionManager.fetchAdminPaymentDetails()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val quickAmounts = listOf(100, 500, 1000, 5000)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deposit Coins", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = gold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(bgGradient), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = gold)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().background(bgGradient).padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(24.dp))

                // BALANCE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.5.dp, gold)
                ) {
                    Box(
                        modifier = Modifier.background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(24.dp)).padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = gold, modifier = Modifier.size(30.dp))
                            Spacer(Modifier.width(14.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Available Balance", color = gold, fontSize = 13.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("$currentBalance", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Coins", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text("1. Enter Amount", color = gold, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() }.take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.3f),
                        cursorColor = gold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(Modifier.height(20.dp))

                // QUICK AMOUNTS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickAmounts.forEach { quickAmount ->
                        FilterChip(
                            selected = amount == quickAmount.toString(),
                            onClick = { amount = quickAmount.toString() },
                            label = { Text("$quickAmount", fontWeight = if (amount == quickAmount.toString()) FontWeight.Bold else FontWeight.Medium) },
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

                Text("2. Select Provider & View Details", color = gold, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
                            modifier = Modifier.fillMaxWidth().clickable { selectedMethod = key },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(borderWidth, if (isSelected) gold else gold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = if (key == "BINANCE") Icons.Default.Star else Icons.Default.CreditCard, contentDescription = null, tint = if (key == "BINANCE") Color(0xFFF0B90B) else Color.White)
                                        Spacer(Modifier.width(16.dp))
                                        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = gold)
                                    }
                                }

                                if (isSelected) {
                                    adminPaymentDetails[key]?.let { (accName, accNumber) ->
                                        Spacer(Modifier.height(14.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                        Spacer(Modifier.height(10.dp))
                                        Text(text = "Send Money To:", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Account Title: $accName", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                                        Text(text = "Account Number: $accNumber", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                                    }?: run {
                                        Spacer(Modifier.height(8.dp))
                                        Text("Details pending admin configuration setup.", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("3. Transaction Sender Details", color = gold, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter Sender Name / Trans ID", color = Color.White.copy(alpha = 0.4f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(36.dp))

                // DEPOSIT BUTTON
                val isValid = amount.isNotEmpty() && (amount.toIntOrNull()?: 0) > 0 && senderName.isNotBlank()
                Button(
                    onClick = {
                        amount.toIntOrNull()?.let { depositAmount ->
                            Log.d("DEPOSIT", "Deposit $depositAmount via $selectedMethod")
                            onDepositSubmitted(depositAmount, selectedMethod, senderName)
                            onBack()
                        }?: Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(),
                    border = BorderStroke(2.5.dp, gold)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            if (isValid) Brush.horizontalGradient(listOf(Color(0xFF8B0000), redBright)) // RED BUTTON
                            else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Submit Deposit Notification", color = if (isValid) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.5.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}