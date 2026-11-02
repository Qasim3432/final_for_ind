package com.example.final_for_ind.screens.component

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.example.final_for_ind.utils.getCurrentLocation
import com.example.final_for_ind.utils.isLocationEnabled
import com.example.final_for_ind.utils.formatDollar
import com.example.final_for_ind.utils.formatLocalCurrency

import kotlinx.coroutines.launch
import java.util.Locale


// ✅ Helper function to format seconds into Hours, Minutes, Seconds
private fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60

    return when {
        h > 0 -> "${h}h ${m}m ${s}s"
        m > 0 -> "${m}m ${s}s"
        else -> "${s}s"
    }
}


// ==========================================================
// DISPLAY NAMES + ORDER HELPERS
// ==========================================================

private val METHOD_DISPLAY_NAMES = mapOf(
    "JAZZCASH" to "JazzCash",
    "EASYPAISA" to "EasyPaisa",
    "RAZORPAY" to "Razorpay",
    "PHONEPE" to "PhonePe",
    "BINANCE" to "Binance",
    "USDT" to "USDT (Crypto)"
)

private val PREFERRED_ORDER = listOf(
    "JAZZCASH", "EASYPAISA",
    "RAZORPAY", "PHONEPE",
    "BINANCE", "USDT"
)

private fun orderedMethodKeys(available: Set<String>): List<String> {
    val preferred = PREFERRED_ORDER.filter { it in available }
    val extra = available.filter { it !in PREFERRED_ORDER }
    return preferred + extra
}

private fun currencyForMethod(method: String): String = when (method) {
    "JAZZCASH", "EASYPAISA" -> "PKR"
    "RAZORPAY", "PHONEPE" -> "INR"
    "USDT" -> "USDT"
    else -> "USD"
}

private fun methodIcon(method: String) = when (method) {
    "BINANCE" -> Icons.Default.Star
    "USDT" -> Icons.Default.AccountBalanceWallet
    else -> Icons.Default.CreditCard
}

private fun methodIconTint(method: String): Color = when (method) {
    "BINANCE" -> Color(0xFFF0B90B)
    "USDT" -> Color(0xFF26A17B)
    else -> Color.White
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    currentBalance: Int = 0,
    sessionManager: GameSessionManager? = null,
    isSubmitting: Boolean = false,
    cooldownSeconds: Int = 0,
    onBack: () -> Unit = {},
    onWithdrawSubmitted: (
        amount: Int,
        method: String,
        accountTitle: String,
        accountNumber: String
    ) -> Unit = { _, _, _, _ -> }
) {

    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }

    var accountTitle by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Payment methods (region-aware from server)
    var adminPaymentDetails by remember {
        mutableStateOf<Map<String, Pair<String, String>>>(emptyMap())
    }
    var isLoadingMethods by remember { mutableStateOf(true) }

    // ✅ FIX: Separate rates for INR and PKR
    var inrRate by remember { mutableStateOf(84.0) }
    var pkrRate by remember { mutableStateOf(280.0) }

    // ==========================================================
    // LOCATION STATE
    // ==========================================================

    var locationRequired by remember { mutableStateOf(false) }
    var hasLocation by remember { mutableStateOf(true) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isLoadingStatus by remember { mutableStateOf(true) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                scope.launch {
                    if (!isLocationEnabled(context)) {
                        Toast.makeText(context, "Please turn ON your phone's location (GPS)", Toast.LENGTH_LONG).show()
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                        isFetchingLocation = false
                        return@launch
                    }

                    isFetchingLocation = true

                    try {
                        val coords = getCurrentLocation(context)
                        if (coords != null && sessionManager != null) {
                            val token = sessionManager.getOrCreateUserToken()
                            sessionManager.sendUserLocation(
                                deviceToken = token,
                                latitude = coords.first,
                                longitude = coords.second
                            )

                            val status = sessionManager.fetchWithdrawalStatus(token)
                            hasLocation = status?.optBoolean("has_location", false) ?: false
                            showLocationDialog = !hasLocation
                        } else {
                            Toast.makeText(context, "Could not get GPS location. Please turn ON location and try again.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isFetchingLocation = false
                    }
                }
            } else {
                Toast.makeText(context, "Location permission denied. You cannot withdraw without it.", Toast.LENGTH_LONG).show()
            }
        }

    // Load withdrawal status + payment methods on open
    LaunchedEffect(Unit) {
        if (sessionManager != null) {
            try {
                val token = sessionManager.getOrCreateUserToken()
                val status = sessionManager.fetchWithdrawalStatus(token)

                if (status != null) {
                    locationRequired = status.optBoolean("requires_location", false)
                    hasLocation = status.optBoolean("has_location", true)
                    if (locationRequired && !hasLocation) {
                        showLocationDialog = true
                    }
                }

                // ✅ Fetch payment methods (region-filtered by backend)
                adminPaymentDetails = sessionManager.fetchAdminPaymentDetails()

                // ✅ FIX: Fetch both rates from AppConfig
                val config = sessionManager.fetchAppConfig()
                if (config != null) {
                    inrRate = config.optDouble("usd_to_inr_rate", 84.0)
                    pkrRate = config.optDouble("usd_to_pkr_rate", 280.0)
                }

                // Auto-select first available method
                if (selectedMethod.isBlank() && adminPaymentDetails.isNotEmpty()) {
                    selectedMethod = orderedMethodKeys(adminPaymentDetails.keys).firstOrNull() ?: ""
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingStatus = false
                isLoadingMethods = false
            }
        } else {
            isLoadingStatus = false
            isLoadingMethods = false
        }
    }

    // ==========================================================
    // THEME
    // ==========================================================

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )

    val gold = Color(0xFFFFD700)
    val redDark = Color(0xFF2B0000)
    val redBright = Color(0xFFD32F2F)

    // ✅ Quick Amounts in Cents
    val quickAmountCents = listOf(250, 500, 1000, 2000)

    // Parse input as Dollars, convert to Cents for API
    val enteredDollars = amount.toFloatOrNull() ?: 0f
    val enteredCents = (enteredDollars * 100).toInt()

    val isValidAmount = enteredCents > 0 && enteredCents <= currentBalance

    val isFormValid = isValidAmount
            && accountTitle.isNotBlank()
            && accountNumber.isNotBlank()
            && selectedMethod.isNotBlank()

    val balanceLoaded = currentBalance > 0

    val isBlockedByLocation = locationRequired && !hasLocation

    // ✅ Effective currency based on selected method (overrides locale)
    val effectiveCurrency = if (selectedMethod.isNotBlank())
        currencyForMethod(selectedMethod)
    else
        "USD"

    // ✅ FIX: Calculate exchange rate dynamically based on selected method
    val exchangeRate = when (effectiveCurrency) {
        "INR" -> inrRate
        "PKR" -> pkrRate
        else -> 1.0
    }

    // Available methods for UI
    val availableKeys = adminPaymentDetails.keys
    val orderedKeys = orderedMethodKeys(availableKeys)
    val methodsList = orderedKeys.map { it to (METHOD_DISPLAY_NAMES[it] ?: it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Withdraw Funds",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(2.5.dp, gold)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = gold,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Available Balance",
                                color = gold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = formatDollar(currentBalance),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                "USD",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (isBlockedByLocation) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLocationDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 24.sp)

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                "Location Required",
                                color = Color(0xFFFF6B6B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Please grant location permission to continue withdrawing.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            if (!balanceLoaded) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFA500).copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFFA500))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp)

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                "Loading balance...",
                                color = Color(0xFFFFA500),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Please wait or restart the app if stuck.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            Text(
                "1. Enter Withdrawal Value ($)",
                color = gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() || char == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        amount = filtered
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "0.00",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 28.sp
                    )
                },
                leadingIcon = {
                    Text("$", color = gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                isError = amount.isNotEmpty() && !isValidAmount,
                trailingIcon = {
                    TextButton(
                        onClick = {
                            amount = String.format(Locale.US, "%.2f", currentBalance / 100.0)
                        }
                    ) {
                        Text(
                            "MAX",
                            color = gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (isValidAmount || amount.isEmpty())
                        gold
                    else
                        redBright,
                    unfocusedBorderColor = gold.copy(alpha = 0.3f),
                    cursorColor = gold,
                    errorBorderColor = redBright
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // ✅ Show Local Payout (uses selected method currency and correct rate)
            if (isValidAmount && effectiveCurrency != "USD") {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "You will receive: ${formatLocalCurrency(
                        enteredCents,
                        effectiveCurrency,
                        exchangeRate
                    )}",
                    color = Color(0xFF2ECC71),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (amount.isNotEmpty() && !isValidAmount) {
                Text(
                    text = if (enteredCents > currentBalance)
                        "Amount exceeds available balance"
                    else
                        "Please enter a valid amount",
                    color = redBright,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ✅ Quick Amount Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickAmountCents.forEach { quickCents ->

                    FilterChip(
                        selected = amount == String.format(Locale.US, "%.2f", quickCents / 100.0),
                        onClick = {
                            amount = String.format(Locale.US, "%.2f", quickCents / 100.0)
                        },
                        label = {
                            Text(
                                formatDollar(quickCents),
                                fontWeight = if (amount == String.format(Locale.US, "%.2f", quickCents / 100.0))
                                    FontWeight.Bold
                                else
                                    FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gold,
                            selectedLabelColor = Color(0xFF1A0000),
                            containerColor = redDark,
                            labelColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(min = 75.dp)
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                "2. Choose Receiving Method",
                color = gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(14.dp))

            if (isLoadingMethods) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = gold)
                }
            } else if (methodsList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFA500).copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFFA500))
                ) {
                    Text(
                        "No withdrawal methods available for your region yet.",
                        color = Color(0xFFFFA500),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    methodsList.forEach { (key, title) ->

                        val isSelected = selectedMethod == key

                        val bgColor by animateColorAsState(
                            if (isSelected) gold.copy(alpha = 0.15f) else redDark.copy(alpha = 0.6f),
                            label = "bg"
                        )

                        val borderWidth by animateDpAsState(
                            if (isSelected) 2.5.dp else 2.dp,
                            label = "border"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = key
                                    accountTitle = ""
                                    accountNumber = ""
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(
                                borderWidth,
                                if (isSelected) gold else gold.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = methodIcon(key),
                                            contentDescription = null,
                                            tint = methodIconTint(key)
                                        )

                                        Spacer(Modifier.width(16.dp))

                                        Text(
                                            title,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = gold)
                                    }
                                }

                                AnimatedVisibility(visible = isSelected) {

                                    Column {
                                        Spacer(Modifier.height(16.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                        Spacer(Modifier.height(12.dp))

                                        if (key == "USDT") {
                                            OutlinedTextField(
                                                value = accountTitle,
                                                onValueChange = { accountTitle = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = { Text("Account Holder Name", color = Color.White.copy(alpha = 0.6f)) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = accountNumber,
                                                onValueChange = { accountNumber = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = { Text("USDT Wallet Address (TRC20/BEP20)", color = Color.White.copy(alpha = 0.6f)) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold, unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            val titleLabel = when (key) {
                                                "BINANCE" -> "Binance Account Name"
                                                "RAZORPAY" -> "Razorpay Account Holder Name"
                                                "PHONEPE" -> "PhonePe Account Holder Name"
                                                else -> "Account Title Name"
                                            }

                                            val numberLabel = when (key) {
                                                "BINANCE" -> "Binance Pay ID / Email"
                                                "RAZORPAY" -> "Razorpay UPI / Email"
                                                "PHONEPE" -> "PhonePe Mobile Number"
                                                else -> "Account Number / Phone"
                                            }

                                            val numberKeyboard = when (key) {
                                                "BINANCE", "RAZORPAY" -> KeyboardType.Text
                                                else -> KeyboardType.Phone
                                            }

                                            OutlinedTextField(
                                                value = accountTitle,
                                                onValueChange = { accountTitle = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = {
                                                    Text(
                                                        text = titleLabel,
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    )
                                                },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold,
                                                    unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = accountNumber,
                                                onValueChange = { accountNumber = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = {
                                                    Text(
                                                        text = numberLabel,
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    )
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = numberKeyboard
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold,
                                                    unfocusedBorderColor = gold.copy(alpha = 0.2f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            val canSubmit = isFormValid
                    && balanceLoaded
                    && !isBlockedByLocation
                    && !isLoadingStatus
                    && !isLoadingMethods
                    && !isFetchingLocation
                    && !isSubmitting
                    && cooldownSeconds == 0

            Button(
                onClick = {
                    if (canSubmit) {
                        onWithdrawSubmitted(
                            enteredCents,
                            selectedMethod,
                            accountTitle,
                            accountNumber
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(),
                border = BorderStroke(2.5.dp, if (cooldownSeconds > 0) Color.Gray else gold)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canSubmit)
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B0000), redBright)
                                )
                            else
                                Brush.horizontalGradient(
                                    listOf(Color.Gray, Color.Gray)
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Submitting...",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else if (cooldownSeconds > 0) {
                        Text(
                            "⏳ Wait ${formatTime(cooldownSeconds)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            when {
                                isBlockedByLocation -> "📍 Grant Location First"
                                isFetchingLocation -> "Getting location..."
                                isLoadingStatus || isLoadingMethods -> "Loading..."
                                else -> "Submit Withdrawal Request"
                            },
                            color = if (canSubmit) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showLocationDialog) {

        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1A0000),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 26.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Location Required",
                        color = gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        "For security reasons, you must enable location permission to continue withdrawing.\n\nPlease grant location access to proceed.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = gold.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "💡 Tip: Turn ON your phone's GPS from the notification panel before granting.",
                            color = gold,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    enabled = !isFetchingLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = gold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (isFetchingLocation) "Getting location..." else "Grant Location",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false
                        onBack()
                    },
                    enabled = !isFetchingLocation
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}