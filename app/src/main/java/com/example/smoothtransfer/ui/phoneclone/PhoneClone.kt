package com.example.smoothtransfer.ui.phoneclone

import com.example.smoothtransfer.network.protocol.DeviceInfo
import com.example.smoothtransfer.transfer.ServiceType

// Dùng "Contract" để nhóm State, Event, và có thể cả Effect
object PhoneClone {

    // 1. Enum để định nghĩa các hướng di chuyển
    enum class NavDirection {
        FORWARD, // Tiến tới
        BACKWARD, // Lùi lại
        NONE // Không có animation (cho lần tải đầu tiên)
    }

    /**
     * Đại diện cho tất cả các màn hình/trạng thái trong luồng Phone Clone.
     * Mỗi object/class ở đây tương ứng với một màn hình UI.
     */
    sealed class State() {
        var direction: NavDirection = NavDirection.FORWARD
        // Màn hình đầu tiên chọn Send/Receive (đã có ở PhoneCloneHomeScreen)
        // Đây là các màn hình BÊN TRONG luồng
        object SelectRole : State()
        data class RequestPermissions(val isSender: Boolean) : State()

        data class SelectTransferMethod(val isSender: Boolean) : State()
        object ShowCameraToScanQr : State() // Sender thấy camera để quét
        data class DisplayQrCode(val isSender: Boolean) : State()     // Receiver hiển thị QR code
        data class Connecting(val message: String = "Connecting...") : State()
        data class Connected(val deviceInfo: DeviceInfo) : State()

        data class SearchingContent(val deviceInfo: DeviceInfo) : State()
        object WaitingForContentList : State() // Receiver đợi Sender gửi danh sách
        data class ContentListSelection(val items: List<Any>) : State() // Receiver chọn data
        data class Transferring(val progress: Float, val statusText: String) : State()
        object Restoring : State()
        object Completed : State()
        data class Error(val reason: String) : State()
    }

    /**
     * Đại diện cho tất cả hành động người dùng hoặc sự kiện hệ thống
     * mà ViewModel cần xử lý.
     */
    sealed class Event {
        // User chọn phương thức
        data class UsbAttached(val isSender: Boolean) : Event()
        data class RoleSelected(val isSender: Boolean) : Event()

        data class MethodSelected(val serviceType: ServiceType) : Event()

        // Sự kiện kết nối
        data class QrCodeScanned(val qrData: String) : Event()
        data class ConnectionEstablished(val deviceInfo: DeviceInfo) : Event()
        object ConnectionFailed : Event()

        // Sự kiện gửi/nhận content list
        data class ContentListReady(val items: List<Any>) : Event() // Sender gửi
        object RequestStartTransfer : Event() // Receiver bấm nút bắt đầu

        // Sự kiện transfer
        data class OnProgressUpdate(val progress: Float, val statusText: String) : Event()
        object OnTransferFinished : Event()

        // Sự kiện Restore
        object OnRestoreFinished : Event()

        // Kết thúc luồng
        object FinishFlow : Event()

        object BackPressed : Event()

        data class PermissionsGranted(val isSender: Boolean) : Event()
    }

    interface PhoneCloneActions {
        fun onEvent(event: Event)
    }
}
