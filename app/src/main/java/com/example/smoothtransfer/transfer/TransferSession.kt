package com.example.smoothtransfer.transfer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


enum class Role {
    SENDER,
    RECEIVER,
    NONE // Một trạng thái mặc định, an toàn khi chưa xác định vai trò
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

    fun setRole(newRole: Role) {
        _role.value = newRole
    }

    fun getRole() : Role = _role.value

    fun isSender(): Boolean = _role.value == Role.SENDER
}