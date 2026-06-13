package com.example.final_for_ind.screens.deposit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
 currentBalance: Int = 1250,
 onBack: () -> Unit = {},
 onDeposit: (amount: Int) -> Unit = {}
) {
 var amount by remember { mutableStateOf("") }
 val gradient = Brush.verticalGradient(
  colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
 )

 val quickAmounts = listOf(100, 500, 1000, 5000)

 Scaffold(
  topBar = {
   TopAppBar(
    title = { Text("Deposit Coins", fontWeight = FontWeight.Bold) },
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
  ) {
   Spacer(Modifier.height(20.dp))

   // Current Balance Card
   Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
     containerColor = Color(0xFF9B59B6).copy(alpha = 0.2f)
    )
   ) {
    Row(
     modifier = Modifier
      .fillMaxWidth()
      .padding(20.dp),
     horizontalArrangement = Arrangement.Center,
     verticalAlignment = Alignment.CenterVertically
    ) {
     Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
     Spacer(Modifier.width(12.dp))
     Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text("Current Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
      Text("$currentBalance Coins", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
     }
    }
   }

   Spacer(Modifier.height(32.dp))

   Text("Enter Amount", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

   Spacer(Modifier.height(12.dp))

   // Amount Input
   OutlinedTextField(
    value = amount,
    onValueChange = { amount = it.filter { char -> char.isDigit() } },
    modifier = Modifier.fillMaxWidth(),
    placeholder = { Text("0", color = Color.White.copy(alpha = 0.5f)) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    colors = OutlinedTextFieldDefaults.colors(
     focusedTextColor = Color.White,
     unfocusedTextColor = Color.White,
     focusedBorderColor = Color(0xFFFFD700),
     unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
     cursorColor = Color(0xFFFFD700)
    ),
    shape = RoundedCornerShape(12.dp),
    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
   )

   Spacer(Modifier.height(16.dp))

   // Quick Amount Buttons
   Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
   ) {
    quickAmounts.forEach { quickAmount ->
     OutlinedButton(
      onClick = { amount = quickAmount.toString() },
      modifier = Modifier.weight(1f),
      colors = ButtonDefaults.outlinedButtonColors(
       contentColor = Color.White
      ),
      border = ButtonDefaults.outlinedButtonBorder.copy(
       width = 1.dp,
       brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f)))
      ),
      shape = RoundedCornerShape(8.dp)
     ) {
      Text("$quickAmount")
     }
    }
   }

   Spacer(Modifier.height(32.dp))

   // Payment Methods Card - JazzCash, EasyPaisa, Binance
   Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
     containerColor = Color.White.copy(alpha = 0.1f)
    )
   ) {
    Column(modifier = Modifier.padding(16.dp)) {
     Text("Payment Methods", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
     Spacer(Modifier.height(12.dp))

     Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.CreditCard, null, tint = Color.White.copy(alpha = 0.8f))
      Spacer(Modifier.width(12.dp))
      Text("JazzCash", color = Color.White, fontSize = 15.sp)
     }

     Spacer(Modifier.height(12.dp))

     Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.CreditCard, null, tint = Color.White.copy(alpha = 0.8f))
      Spacer(Modifier.width(12.dp))
      Text("EasyPaisa", color = Color.White, fontSize = 15.sp)
     }

     Spacer(Modifier.height(12.dp))

     Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Star, null, tint = Color(0xFFF0B90B), modifier = Modifier.size(24.dp)) // Binance yellow
      Spacer(Modifier.width(12.dp))
      Text("Binance", color = Color.White, fontSize = 15.sp)
     }
    }
   }

   Spacer(Modifier.weight(1f))

   // Deposit Button
   Button(
    onClick = {
     amount.toIntOrNull()?.let { depositAmount ->
      if (depositAmount > 0) {
       onDeposit(depositAmount)
       onBack()
      }
     }
    },
    modifier = Modifier
     .fillMaxWidth()
     .height(56.dp),
    enabled = amount.isNotEmpty() && amount.toIntOrNull() ?: 0 > 0,
    colors = ButtonDefaults.buttonColors(
     containerColor = Color(0xFFFFD700),
     disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
    ),
    shape = RoundedCornerShape(12.dp)
   ) {
    Text(
     "Deposit Now",
     color = Color.Black,
     fontWeight = FontWeight.Bold,
     fontSize = 18.sp
    )
   }

   Spacer(Modifier.height(16.dp))
  }
 }
}

@Preview(showBackground = true)
@Composable
fun DepositPreview() {
 DepositScreen()
}