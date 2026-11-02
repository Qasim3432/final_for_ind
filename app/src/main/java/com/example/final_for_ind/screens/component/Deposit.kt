package com.example.final_for_ind.screens.deposit

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
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
import com.example.final_for_ind.utils.formatDollar
import com.example.final_for_ind.utils.formatLocalCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


// ==========================================================
// COPY TO CLIPBOARD HELPER
// ==========================================================

fun copyToClipboard(
    context: Context,
    label: String,
    value: String
): Boolean {

    return try {

        val clipboard = context
            .getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager

        val clip = ClipData.newPlainText(label, value)

        clipboard.setPrimaryClip(clip)

        Log.d("CLIPBOARD", "Copied $label: $value")

        true

    } catch (e: Exception) {

        Log.e("CLIPBOARD", "Copy failed", e)
        false
    }
}


// ==========================================================
// PLAY STORE HELPER
// ==========================================================

fun openPlayStore(
    context: Context,
    packageName: String
) {
    try {

        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        )

        marketIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        context.startActivity(marketIntent)

        Log.d(
            "PAYMENT_APP",
            "Opened Play Store: $packageName"
        )

    } catch (e: ActivityNotFoundException) {

        try {

            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://play.google.com/store/apps/details?id=$packageName"
                )
            )

            webIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(webIntent)

        } catch (e2: Exception) {

            Toast.makeText(
                context,
                "Could not open Play Store.",
                Toast.LENGTH_SHORT
            ).show()
        }

    } catch (e: Exception) {

        Log.e("PAYMENT_APP", "Play Store failed", e)
    }
}


// ==========================================================
// PAYMENT APP LAUNCHER (FIXED LOGIC)
// ==========================================================

fun openPaymentApp(
    context: Context,
    provider: String,
    accountNumber: String? = null
) {

    // 1. Copy Account Number First
    if (!accountNumber.isNullOrBlank()) {

        copyToClipboard(
            context = context,
            label = "$provider Account Number",
            value = accountNumber
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                context,
                "📋 Account number copied!",
                Toast.LENGTH_SHORT
            ).show()
        }

    } else {

        Toast.makeText(
            context,
            "Admin has not set details for $provider yet.",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    // 2. Razorpay UPI Intent
    if (provider == "RAZORPAY") {

        try {

            val upiUri = Uri.parse(
                "upi://pay?pa=$accountNumber&pn=Admin&cu=INR"
            )

            val upiIntent = Intent(
                Intent.ACTION_VIEW,
                upiUri
            )

            upiIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            val chooser = Intent.createChooser(
                upiIntent,
                "Pay with UPI"
            )

            chooser.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(chooser)

            return

        } catch (e: Exception) {

            // Agar UPI app nahi mila, toh Play Store pe bhej dein
            openPlayStore(
                context,
                "com.razorpay.app"
            )

            return
        }
    }

    // 3. Package Mapping
    val packageName: String? = when (provider) {
        "JAZZCASH" -> "com.techlogix.mobilinkcustomer"
        "EASYPAISA" -> "pk.com.telenor.phoenix"
        "BINANCE" -> "com.binance.dev"
        "PHONEPE" -> "com.phonepe.app"
        else -> null
    }

    if (packageName == null) {

        Toast.makeText(
            context,
            "📋 Account copied! Open your app to pay.",
            Toast.LENGTH_LONG
        ).show()

        return
    }

    Log.d(
        "PAYMENT_APP",
        "Trying to open: $packageName"
    )

    // 4. Check if Installed
    try {

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(launchIntent)

            Log.d(
                "PAYMENT_APP",
                "Launched App: $packageName"
            )

        } else {

            // App install nahi hai -> Play Store open karo
            openPlayStore(
                context,
                packageName
            )
        }

    } catch (e: Exception) {

        Log.e("PAYMENT_APP", "Failed to open app", e)

        // Fallback to Play Store
        openPlayStore(
            context,
            packageName
        )
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
fun DepositScreen(
    sessionManager: GameSessionManager,
    onBack: () -> Unit = {},
    onDepositSubmitted: (
        localAmount: Float,
        currency: String,
        method: String,
        senderName: String
    ) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentBalance by remember { mutableStateOf(0) }
    var adminPaymentDetails by remember {
        mutableStateOf<Map<String, Pair<String, String>>>(
            emptyMap()
        )
    }
    var isLoading by remember { mutableStateOf(true) }

    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember {
        mutableStateOf("")
    }
    var senderName by remember { mutableStateOf("") }

    // Exchange rates
    var usdToInr by remember { mutableStateOf(94.95) }
    var usdToPkr by remember { mutableStateOf(276.53) }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0000)
        )
    )
    val gold = Color(0xFFFFD700)
    val redDark = Color(0xFF2B0000)
    val redBright = Color(0xFFD32F2F)

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                val deviceToken = sessionManager
                    .getOrCreateUserToken()

                val balanceResult = sessionManager
                    .fetchUserBalanceFromServer(deviceToken)

                currentBalance = balanceResult.coins

                adminPaymentDetails = sessionManager
                    .fetchAdminPaymentDetails()

                sessionManager.fetchAppConfig()?.let { cfg ->
                    usdToInr = cfg.optDouble("usd_to_inr_rate", 94.95)
                    usdToPkr = cfg.optDouble("usd_to_pkr_rate", 276.53)
                }

                if (selectedMethod.isBlank() && adminPaymentDetails.isNotEmpty()) {
                    selectedMethod = orderedMethodKeys(adminPaymentDetails.keys).firstOrNull() ?: ""
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val quickAmountCents = listOf(250, 500, 1000, 2000)

    val availableKeys = adminPaymentDetails.keys
    val orderedKeys = orderedMethodKeys(availableKeys)
    val methodsList = orderedKeys.map {
        it to (METHOD_DISPLAY_NAMES[it] ?: it)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Deposit Funds",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = gold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgGradient),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = gold)
            }
        } else {
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
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1A0000),
                                        Color(0xFF0A0A0A)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
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
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.width(14.dp))
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
                                    fontSize = 30.sp,
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

                Text(
                    "1. Enter Amount ($)",
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
                        Text(
                            "$",
                            color = gold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = gold,
                        unfocusedBorderColor = gold.copy(alpha = 0.3f),
                        cursorColor = gold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography
                        .bodyLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                )

                Spacer(Modifier.height(20.dp))

                // ✅ FIX: Added horizontalScroll and removed weight
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
                                    fontWeight = if (
                                        amount == String.format(Locale.US, "%.2f", quickCents / 100.0)
                                    )
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
                    "2. Select Provider & View Details",
                    color = gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Tap on a provider — account number will be copied automatically",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(14.dp))

                if (methodsList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFA500).copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFFFA500))
                    ) {
                        Text(
                            "No payment methods available for your region yet. Contact support.",
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
                                if (isSelected)
                                    gold.copy(alpha = 0.15f)
                                else
                                    redDark.copy(alpha = 0.6f),
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

                                        coroutineScope.launch {

                                            delay(150)

                                            val accountNumber =
                                                adminPaymentDetails[key]?.second

                                            openPaymentApp(
                                                context = context,
                                                provider = key,
                                                accountNumber = accountNumber
                                            )
                                        }
                                    },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = bgColor
                                ),
                                border = BorderStroke(
                                    borderWidth,
                                    if (isSelected)
                                        gold
                                    else
                                        gold.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = methodIcon(key),
                                                contentDescription = null,
                                                tint = methodIconTint(key)
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    title,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = if (isSelected)
                                                        FontWeight.Bold
                                                    else
                                                        FontWeight.Medium
                                                )
                                                Text(
                                                    text = "Tap to copy & open app",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = gold
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        adminPaymentDetails[key]?.let {
                                                (accName, accNumber) ->

                                            Spacer(Modifier.height(14.dp))

                                            HorizontalDivider(
                                                color = Color.White.copy(alpha = 0.15f)
                                            )

                                            Spacer(Modifier.height(10.dp))

                                            Text(
                                                text = "Send Money To:",
                                                color = gold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(
                                                text = "Account Title: $accName",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Account Number: $accNumber",
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                TextButton(
                                                    onClick = {

                                                        val copied = copyToClipboard(
                                                            context = context,
                                                            label = "$key Account Number",
                                                            value = accNumber
                                                        )

                                                        if (copied) {
                                                            if (Build.VERSION.SDK_INT <
                                                                Build.VERSION_CODES.TIRAMISU
                                                            ) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "📋 Account number copied!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(
                                                        horizontal = 8.dp,
                                                        vertical = 0.dp
                                                    )
                                                ) {
                                                    Text(
                                                        "📋 Copy",
                                                        color = gold,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } ?: run {
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Details pending admin configuration setup.",
                                                color = Color.LightGray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "3. Transaction Sender Details",
                    color = gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Enter Sender Name / Trans ID",
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = gold,
                        unfocusedBorderColor = gold.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(36.dp))

                val enteredDollars = amount.toFloatOrNull() ?: 0f
                val selectedCurrency = currencyForMethod(selectedMethod)

                val localAmount = when (selectedCurrency) {
                    "PKR" -> enteredDollars * usdToPkr.toFloat()
                    "INR" -> enteredDollars * usdToInr.toFloat()
                    else -> enteredDollars
                }

                if (
                    enteredDollars > 0f &&
                    selectedMethod.isNotBlank() &&
                    selectedCurrency != "USD" &&
                    selectedCurrency != "USDT"
                ) {
                    Text(
                        text = "You will send: ${formatLocalCurrency(
                            (enteredDollars * 100).toInt(),
                            selectedCurrency,
                            if (selectedCurrency == "INR") usdToInr else usdToPkr
                        )}",
                        color = Color(0xFF2ECC71),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                } else if (enteredDollars > 0f && selectedMethod == "USDT") {
                    Text(
                        text = "You will send: ${formatLocalCurrency(
                            (enteredDollars * 100).toInt(),
                            "USDT",
                            1.0
                        )}",
                        color = Color(0xFF2ECC71),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                val isValid = enteredDollars > 0f
                        && senderName.isNotBlank()
                        && selectedMethod.isNotBlank()

                Button(
                    onClick = {
                        if (isValid) {
                            onDepositSubmitted(
                                localAmount,
                                selectedCurrency,
                                selectedMethod,
                                senderName
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(),
                    border = BorderStroke(2.5.dp, gold)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isValid)
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF8B0000),
                                            redBright
                                        )
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Gray,
                                            Color.Gray
                                        )
                                    )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Submit Deposit Notification",
                            color = if (isValid)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.5f),
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
}