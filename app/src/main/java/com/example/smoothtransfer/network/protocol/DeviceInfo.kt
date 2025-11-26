package com.example.smoothtransfer.network.protocol

import com.google.gson.Gson
import android.util.Log

/**
 * DeviceInfo - Device information data class
 * 
 * This class represents device information exchanged between sender and receiver.
 * Contains device name, IP address, and other device metadata.
 * 
 * Serialization:
 * - Uses Gson for JSON encoding/decoding
 * - Standard data class with Gson serialization
 * 
 * Usage:
 * - Sender sends DeviceInfo after connection is established
 * - Receiver responds with its own DeviceInfo
 * - Both devices store received DeviceInfo for display
 * 
 * @param deviceName Device name (e.g., "Galaxy S25", "Pixel 8")
 * @param ipAddress Device IP address (e.g., "192.168.1.100")
 * @param additionalData Additional device data (for future use)
 */
data class DeviceInfo(
    val deviceName: String,
    val ipAddress: String,
    val additionalData: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * PREFIX: Log tag prefix for Smart Switch
         */
        private const val PREFIX = "Smart_Switch"
        
        /**
         * TAG: Log tag for this class
         */
        private const val TAG = "DeviceInfo"
        
        /**
         * LOG_TAG: Combined log tag (PREFIX + TAG)
         */
        private const val LOG_TAG = "$PREFIX $TAG"
        
        /**
         * GSON: Gson instance for JSON serialization
         */
        private val gson = Gson()
        
        /**
         * fromJson - Deserialize DeviceInfo from JSON string
         * 
         * @param jsonString JSON string to deserialize
         * @return DeviceInfo instance or null on error
         */
        fun fromJson(jsonString: String): DeviceInfo? {
            return try {
                gson.fromJson(jsonString, DeviceInfo::class.java)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Smart Switch: Error deserializing DeviceInfo: ${e.message}", e)
                null
            }
        }
        
        /**
         * fromBytes - Deserialize DeviceInfo from byte array
         * 
         * @param bytes Byte array containing JSON string
         * @return DeviceInfo instance or null on error
         */
        fun fromBytes(bytes: ByteArray): DeviceInfo? {
            return try {
                val jsonString = String(bytes, Charsets.UTF_8)
                fromJson(jsonString)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Smart Switch: Error deserializing DeviceInfo from bytes: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * toJson - Serialize DeviceInfo to JSON string
     * 
     * @return JSON string representation
     */
    fun toJson(): String {
        return try {
            gson.toJson(this)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Smart Switch: Error serializing DeviceInfo: ${e.message}", e)
            "{}"
        }
    }
    
    /**
     * toBytes - Serialize DeviceInfo to byte array
     * 
     * @return Byte array containing JSON string
     */
    fun toBytes(): ByteArray {
        return toJson().toByteArray(Charsets.UTF_8)
    }
}

