package com.example.smoothtransfer.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

/**
 * Một đối tượng Singleton để lưu trữ trạng thái và dữ liệu chung của toàn bộ ứng dụng.
 * Dữ liệu trong đây sẽ tồn tại cho đến khi tiến trình (process) của ứng dụng bị hủy.
 */
object MainDataModel {

    // --- Thông tin kết nối ---
    // Sử dụng MutableStateFlow để các thành phần UI hoặc ViewModel có thể "lắng nghe"
    // sự thay đổi của các địa chỉ IP này nếu cần.

    private val _myIP = MutableStateFlow<String?>(null)
    val myIP = _myIP.asStateFlow()

    private val _peerIP = MutableStateFlow<String?>(null)
    val peerIP = _peerIP.asStateFlow()

    /**
     * Cập nhật địa chỉ IP của thiết bị hiện tại.
     * @param ip Địa chỉ IP cục bộ trong mạng Wi-Fi Aware.
     */
    fun setMyIP(ip: String?) {
        _myIP.value = ip
    }

    /**
     * Cập nhật địa chỉ IP của thiết bị đối phương (peer).
     * @param ip Địa chỉ IP của thiết bị đã kết nối.
     */
    fun setPeerIP(ip: String?) {
        _peerIP.value = ip
    }

    /**
     * Xóa tất cả thông tin kết nối, thường được gọi khi ngắt kết nối.
     */
    fun clearConnectionInfo() {
        _myIP.value = null
        _peerIP.value = null
    }
}