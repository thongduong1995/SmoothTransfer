package com.example.smoothtransfer.transfer

import com.example.smoothtransfer.network.protocol.DeviceInfo

/**
 * Một interface chung cho tất cả các sự kiện có thể xảy ra trong quá trình truyền dữ liệu.
 * Các Manager con sẽ phát ra các sự kiện kế thừa từ interface này.
 */
sealed interface TransferEvent {
    // Các sự kiện từ ConnectionManager
    data class OnConnected(val deviceInfo: DeviceInfo) : TransferEvent
    data class onSearchContent(val deviceInfo: DeviceInfo) : TransferEvent
    data class OnConnectionLost(val reason: String = "Connection Lost") : TransferEvent
    data class OnConnecting(val message: String) : TransferEvent

    // Các sự kiện từ MediaManager (ví dụ trong tương lai)
    // data class OnMediaScanComplete(val images: List<Uri>) : TransferEvent
    // data class OnApkScanComplete(val apks: List<AppInfo>) : TransferEvent

    // Các sự kiện về tiến trình truyền file (ví dụ trong tương lai)
    // data class OnFileProgress(val fileName: String, val progress: Float) : TransferEvent
}