package com.example.smoothtransfer.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


enum class Role {
    SENDER,
    RECEIVER,
    NONE // Một trạng thái mặc định, an toàn khi chưa xác định vai trò
}

/**
 * Enum để định nghĩa các phương thức kết nối khác nhau.
 * Giúp code rõ ràng và dễ mở rộng hơn so với dùng Boolean.
 */
enum class ServiceType {
    WIFI_AWARE,
    WIFI_DIRECT,
    OTG, // Hoặc Cable
    NONE; // Trạng thái mặc định, an toàn
    fun isWifi(): Boolean {
        return this == WIFI_AWARE || this == WIFI_DIRECT
    }

    /**
     * Kiểm tra xem phương thức kết nối hiện tại có phải là qua cáp OTG hay không.
     * @return True nếu là OTG.
     */
    fun isOtg(): Boolean {
        return this == OTG
    }
}


/**
 * Singleton object - Nguồn chân lý duy nhất (Single Source of Truth)
 * cho tất cả các trạng thái chung của một phiên truyền dữ liệu.
 *
 * Bất kỳ lớp nào trong ứng dụng (ConnectionManager, MediaManager, ViewModel...)
 * cũng có thể truy cập `TransferSession.getRole()` để biết vai trò hiện tại
 * mà không cần phải truyền tham số đi khắp nơi.
 */
object TransferSession {
    private val _role = MutableStateFlow(Role.NONE)
    val roleFlow = _role.asStateFlow()

    private val _serviceType = MutableStateFlow(ServiceType.NONE)
    val serviceTypeFlow = _serviceType.asStateFlow()

    fun setRole(newRole: Role) {
        _role.value = newRole
    }
    fun getRole() : Role = _role.value

    fun isSender(): Boolean = _role.value == Role.SENDER

    fun setServiceType(newServiceType: ServiceType) {
        _serviceType.value = newServiceType
    }

    fun getServiceType(): ServiceType = _serviceType.value
}