package com.example.smoothtransfer.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.apply
import kotlin.ranges.until
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.example.smoothtransfer.network.wifi.WifiAwareQrData

object QrCodeHelper {
    /**
     * Generate QR code bitmap from data string
     * @param data The string data to encode in QR code
     * @param size The size of the QR code bitmap (width and height in pixels)
     * @return Bitmap of the QR code
     */
    fun generateQrCodeBitmap(data: String, size: Int = 512): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

        val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return bitmap
    }

    /**
     * Generate QR code bitmap from WifiAwareQrData
     */
    fun generateQrCodeBitmap(qrData: WifiAwareQrData, size: Int = 512): Bitmap {
        return generateQrCodeBitmap(qrData.toJson(), size)
    }

    /**
     * Parse QR code data string to WifiAwareQrData
     */
    fun parseQrCodeData(qrData: String): WifiAwareQrData? {
        return WifiAwareQrData.fromJson(qrData)
    }
}

