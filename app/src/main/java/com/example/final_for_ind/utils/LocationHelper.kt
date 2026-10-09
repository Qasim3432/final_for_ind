package com.example.final_for_ind.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


// ==========================================================
// CHECK IF LOCATION SERVICES ARE ENABLED GLOBALLY
// ==========================================================

fun isLocationEnabled(context: Context): Boolean {

    val locationManager = context
        .getSystemService(Context.LOCATION_SERVICE)
            as LocationManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        locationManager.isLocationEnabled

    } else {

        locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
}


// ==========================================================
// GET CURRENT LOCATION
//
// Only uses "last known" if it's fresh (< 30 seconds).
// Otherwise requests a fresh GPS fix.
//
// This is important for security — we don't want to
// accept a stale cached location.
// ==========================================================

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(
    context: Context
): Pair<Double, Double>? = withContext(Dispatchers.IO) {

    try {

        val locationManager = context
            .getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        val allProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        // ==================================================
        // Step 1: Try last known — only accept if < 30s old
        // ==================================================

        var bestLastKnown: Location? = null

        for (provider in allProviders) {

            try {

                if (!locationManager.isProviderEnabled(provider)) {
                    continue
                }

                val loc = locationManager
                    .getLastKnownLocation(provider)

                if (loc != null) {

                    if (
                        bestLastKnown == null ||
                        loc.time > bestLastKnown.time
                    ) {
                        bestLastKnown = loc
                    }
                }

            } catch (_: Exception) { }
        }

        if (bestLastKnown != null) {

            val ageMs = System.currentTimeMillis() - bestLastKnown.time
            val ageSec = ageMs / 1000

            if (ageMs < 30_000) {

                // Very fresh — use it
                Log.d(
                    "LOCATION",
                    "Using fresh cached (${ageSec}s old): " +
                            "${bestLastKnown.latitude}, " +
                            "${bestLastKnown.longitude}"
                )

                return@withContext Pair(
                    bestLastKnown.latitude,
                    bestLastKnown.longitude
                )

            } else {

                // Too old — will get fresh fix instead
                Log.d(
                    "LOCATION",
                    "Cached location too old (${ageSec}s) — " +
                            "requesting fresh GPS fix"
                )
            }
        }

        // ==================================================
        // Step 2: Request fresh GPS fix (with timeout)
        // ==================================================

        val activeProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        ).filter {
            try {
                locationManager.isProviderEnabled(it)
            } catch (_: Exception) {
                false
            }
        }

        if (activeProviders.isEmpty()) {

            Log.e(
                "LOCATION",
                "No location providers enabled"
            )

            // Fallback: return old cached location anyway
            if (bestLastKnown != null) {
                Log.w(
                    "LOCATION",
                    "Fallback to old cached (no providers)"
                )
                return@withContext Pair(
                    bestLastKnown.latitude,
                    bestLastKnown.longitude
                )
            }

            return@withContext null
        }

        var result: Pair<Double, Double>? = null

        val listener = object : android.location.LocationListener {

            override fun onLocationChanged(location: Location) {

                Log.d(
                    "LOCATION",
                    "Fresh fix: " +
                            "${location.latitude}, " +
                            "${location.longitude} " +
                            "(accuracy=${location.accuracy}m)"
                )

                result = Pair(
                    location.latitude,
                    location.longitude
                )
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}

            @Deprecated("Deprecated")
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: android.os.Bundle?
            ) { }
        }

        for (provider in activeProviders) {

            try {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.e(
                    "LOCATION",
                    "Register failed $provider",
                    e
                )
            }
        }

        val startTime = System.currentTimeMillis()
        val timeoutMs = 20000L

        while (
            result == null &&
            System.currentTimeMillis() - startTime < timeoutMs
        ) {
            delay(300)
        }

        try {
            locationManager.removeUpdates(listener)
        } catch (_: Exception) { }

        if (result != null) {
            return@withContext result
        }

        // ==================================================
        // Step 3: Timeout — fallback to old cached
        // ==================================================

        if (bestLastKnown != null) {

            Log.w(
                "LOCATION",
                "Timeout — using old cached"
            )

            return@withContext Pair(
                bestLastKnown.latitude,
                bestLastKnown.longitude
            )
        }

        Log.e(
            "LOCATION",
            "Timeout — no location available"
        )

        null

    } catch (e: Exception) {

        Log.e("LOCATION", "Fetch failed", e)
        null
    }
}