package com.example.smoothtransfer.network.wifi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonBuilder

/**
 * Data class representing the information encoded in QR code for Wi-Fi Aware connection
 */
@Serializable
data class WifiAwareQrData(
    val serviceName: String,
    val peerId: String,
    val connectionMetadata: String = ""
) {
    /**
     * Convert to JSON string for QR code encoding
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parse JSON string from scanned QR code
         */
        fun fromJson(jsonString: String): WifiAwareQrData? {
            return try {
                json.decodeFromString<WifiAwareQrData>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
}

