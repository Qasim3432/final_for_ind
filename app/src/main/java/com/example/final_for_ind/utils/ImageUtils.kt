package com.example.final_for_ind.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> input.copyTo(out) }
        input.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}