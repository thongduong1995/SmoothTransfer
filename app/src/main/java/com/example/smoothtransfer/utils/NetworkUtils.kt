package com.example.smoothtransfer.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.net.wifi.WifiInfo
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.LinkProperties
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.all
import kotlin.text.contains
import kotlin.text.format
import kotlin.text.split
import kotlin.text.toInt
import kotlin.text.toIntOrNull

/**
 * NetworkUtils - Utility functions for network operations
 * 
 * This object provides functions to get device IP address from various sources.
 * Used by receiver device to get its IP address for QR code generation.
 * 
 * IP Address Detection Methods (in order of priority):
 * 1. WiFiManager.getLocalIpAddress() - Primary method for WiFi connections
 * 2. ConnectivityManager (Android 10+) - For AP mode and modern Android
 * 3. NetworkInterface enumeration - Fallback method
 * 4. wlan0 interface - Specific check for WiFi AP mode
 * 
 * Supported Scenarios:
 * - WiFi connected device
 * - WiFi AP mode (hotspot)
 * - Various Android versions (API 21+)
 */
object NetworkUtils {
    
    /**
     * getDeviceIpAddress - Get device's IP address from network
     * 
     * This function tries multiple methods to get the device's local IP address.
     * It attempts methods in order until a valid IP is found.
     * 
     * IP Address Requirements:
     * - Must be valid IPv4 address
     * - Must be local network IP (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
     * - Must not be loopback (127.0.0.1)
     * - Must not be "0.0.0.0"
     * 
     * @param context Android Context for accessing system services
     * @return IP address as string (e.g., "192.168.1.100") or null if not found
     * 
     * Usage:
     * Used by ReceiverScreen to get IP address for QR code generation
     */
    fun getDeviceIpAddress(context: Context): String? {
        try {
            // Method 1: Try to get IP from WiFiManager using getLocalIpAddress (primary method)
            val ip = getLocalIpAddress(context)
            if (ip != null && ip != "0.0.0.0" && isValidLocalIp(ip)) {
                return ip
            }
            
            // Method 2: Try to get IP from WiFi AP mode (for Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val connectivityIp = getIpFromConnectivityManager(connectivityManager)
                if (connectivityIp != null) {
                    return connectivityIp
                }
            }
            
            // Method 3: Get IP from network interfaces (fallback)
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                // Skip loopback and inactive interfaces
                if (intf.isLoopback || !intf.isUp) {
                    continue
                }
                
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (addr.isLoopbackAddress) {
                        continue
                    }
                    
                    val hostAddress = addr.hostAddress
                    if (hostAddress != null && !hostAddress.contains(":")) {
                        // IPv4 address - check if it's a valid local IP
                        if (isValidLocalIp(hostAddress)) {
                            return hostAddress
                        }
                    }
                }
            }
            
            // Method 4: Try to get from wlan0 interface (common for WiFi AP)
            try {
                val wlanInterface = NetworkInterface.getByName("wlan0")
                if (wlanInterface != null) {
                    val addrs = Collections.list(wlanInterface.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val hostAddress = addr.hostAddress
                            if (hostAddress != null && !hostAddress.contains(":") && isValidLocalIp(hostAddress)) {
                                return hostAddress
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // wlan0 might not exist, continue
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }

    /**
     * getLocalIpAddress - Get IP address from WiFiManager (Method 1)
     * 
     * This is the primary method for getting IP address when device is connected to WiFi.
     * Uses WifiManager to get connection info and extract IP address.
     * 
     * Process:
     * 1. Get WifiManager from system service
     * 2. Get connection info (WifiInfo)
     * 3. Extract IP address as integer
     * 4. Convert integer to InetAddress
     * 5. Format as string (e.g., "192.168.1.100")
     * 
     * @param context Android Context for accessing WifiManager
     * @return IP address string or null if not available
     * 
     * Note: Returns null if IP is 0 (not connected) or on error
     */
    private fun getLocalIpAddress(context: Context): String? {
        return try {
            val wifi = context.getSystemService(WIFI_SERVICE) as? WifiManager
            if (wifi == null) return null
            
            val wifiInfo = wifi.connectionInfo ?: return null
            val ipInt = wifiInfo.ipAddress
            
            if (ipInt == 0) {
                null // Return null instead of "0.0.0.0"
            } else {
                InetAddress.getByAddress(
                    byteArrayOf(
                        (ipInt and 0xFF).toByte(),
                        (ipInt shr 8 and 0xFF).toByte(),
                        (ipInt shr 16 and 0xFF).toByte(),
                        (ipInt shr 24 and 0xFF).toByte()
                    )
                ).hostAddress
            }
        } catch (e: Exception) {
            null // Return null on error instead of "0.0.0.0"
        }
    }
    
    /**
     * getIpFromConnectivityManager - Get IP address from ConnectivityManager (Method 2)
     * 
     * This method works for Android 10+ (API 29+) and is useful for:
     * - WiFi AP mode (hotspot)
     * - Modern Android versions
     * - Devices where WiFiManager method doesn't work
     * 
     * Process:
     * 1. Get active network from ConnectivityManager
     * 2. Get LinkProperties for active network
     * 3. Extract LinkAddresses (IP addresses)
     * 4. Filter for IPv4 addresses
     * 5. Validate as local IP
     * 
     * @param connectivityManager ConnectivityManager instance (can be null)
     * @return IP address string or null if not found
     * 
     * @RequiresApi(Build.VERSION_CODES.Q) Android 10+ (API 29+)
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getIpFromConnectivityManager(connectivityManager: ConnectivityManager?): String? {
        if (connectivityManager == null) return null
        
        try {
            val activeNetwork = connectivityManager.activeNetwork ?: return null
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return null
            
            val linkAddresses = linkProperties.linkAddresses
            for (linkAddress in linkAddresses) {
                val address = linkAddress.address
                if (address != null && !address.isLoopbackAddress) {
                    val hostAddress = address.hostAddress
                    if (hostAddress != null && !hostAddress.contains(":") && isValidLocalIp(hostAddress)) {
                        return hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }
    
    /**
     * isValidIpAddress - Validate IP address format
     * 
     * Checks if string is a valid IPv4 address format.
     * Validates: X.X.X.X where each X is 0-255
     * 
     * @param ip IP address string to validate
     * @return true if format is valid, false otherwise
     */
    private fun isValidIpAddress(ip: String): Boolean {
        return try {
            val parts = ip.split(".")
            if (parts.size != 4) return false
            parts.all { part ->
                val num = part.toInt()
                num in 0..255
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * isValidLocalIp - Check if IP is a valid local network IP
     * 
     * Validates that IP address is:
     * 1. Valid IPv4 format
     * 2. In local network range (not public internet IP)
     * 
     * Supported Local IP Ranges:
     * - 192.168.x.x (most common for home networks)
     * - 10.x.x.x (common for corporate networks)
     * - 172.16-31.x.x (less common, but valid)
     * 
     * Excluded:
     * - Loopback (127.0.0.1)
     * - Public IPs
     * - Multicast IPs
     * 
     * @param ip IP address string to validate
     * @return true if IP is valid local network IP, false otherwise
     */
    private fun isValidLocalIp(ip: String): Boolean {
        if (!isValidIpAddress(ip)) return false
        
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        val first = parts[0].toIntOrNull() ?: return false
        val second = parts[1].toIntOrNull() ?: return false
        
        // Check for common local IP ranges:
        // 192.168.x.x, 10.x.x.x, 172.16-31.x.x
        return when {
            first == 192 && second == 168 -> true
            first == 10 -> true
            first == 172 && second in 16..31 -> true
            else -> false
        }
    }
    
    /**
     * Format IP address from integer format
     */
    private fun formatIpAddress(ip: Int): String {
        return String.format(
            Locale.getDefault(),
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }
}


