package com.example.smoothtransfer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
    /**
     * Get all required permissions for Wi-Fi Aware transfer
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        // Camera permission for QR scanning
        permissions.add(Manifest.permission.CAMERA)

        // Location permission for Wi-Fi Aware
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Nearby devices permission for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        return permissions.toTypedArray()
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(context: Context): Array<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}