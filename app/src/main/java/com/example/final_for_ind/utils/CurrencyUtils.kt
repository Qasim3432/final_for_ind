package com.example.final_for_ind.utils


import java.util.Locale
import kotlin.math.roundToInt

// ✅ Cents to USD String (e.g., 250 -> $2.50)
fun formatDollar(cents: Int): String {
    val dollars = cents / 100.0
    return String.format(Locale.US, "$%.2f", dollars)
}

// ✅ Cents to Local Currency (INR/PKR)
fun formatLocalCurrency(cents: Int, currencyCode: String, rate: Double): String {
    if (currencyCode == "USD" || rate <= 0.0) {
        return formatDollar(cents)
    }

    val usdValue = cents / 100.0
    val localValue = usdValue * rate

    return when (currencyCode) {
        "INR" -> String.format(Locale.US, "₹%.2f", localValue)
        "PKR" -> String.format(Locale.US, "₨%.2f", localValue)
        "USDT" -> String.format(Locale.US, "%.2f USDT", localValue)
        else -> String.format(Locale.US, "%.2f %s", localValue, currencyCode)
    }
}

// ✅ Get Local Currency based on Phone Locale
fun getDeviceCurrency(): String {
    val country = Locale.getDefault().country
    return when (country) {
        "IN" -> "INR"
        "PK" -> "PKR"
        else -> "USD"
    }
}