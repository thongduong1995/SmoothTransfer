package com.example.smoothtransfer.viewmodel.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Một ViewModel cơ sở trừu tượng cho tất cả các luồng (flow) trong ứng dụng.
 * Nó tự động quản lý State, Event, và một ngăn xếp lịch sử (history stack) cho nút Back.
 *
 * @param S Kiểu dữ liệu cho State (ví dụ: PhoneClone.State)
 * @param E Kiểu dữ liệu cho Event (ví dụ: PhoneClone.Event)
 */
abstract class BaseFlowViewModel<S : Any, E : Any>(
    application: Application,
    initialState: S
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "SmartSwitch BaseFlowViewModel"
    }
    // --- State Management ---
    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    // Dùng ArrayDeque làm một ngăn xếp (Stack) hiệu quả hơn cho lịch sử.
    private val stateHistory = ArrayDeque<S>()

    /**
     * Hàm này được lớp con triển khai để xử lý các sự kiện.
     */
    protected abstract suspend fun handleEvent(event: E)

    /**
     * Hàm công khai để UI gọi, nó sẽ bọc lời gọi đến handleEvent trong một coroutine.
     */
    open fun onEvent(event: E) {
        viewModelScope.launch {
            handleEvent(event)
        }
    }

    /**
     * Hàm duy nhất để thay đổi state. Nó tự động ghi lại lịch sử.
     * Lớp con sẽ gọi hàm này.
     */
    protected fun updateUiState(newState: S) {
        // Chỉ thêm vào lịch sử nếu state mới khác state cũ
        if (_state.value::class != newState::class) {
            Log.d(TAG, "updateUiState: $newState")
            stateHistory.addLast(_state.value)
            _state.value = newState
        }
    }

    /**
     * Hàm duy nhất để xử lý logic quay lại.
     * Lớp con sẽ gọi hàm này khi nhận được sự kiện BackPressed.
     */
    protected fun navigateBack() {
        if (stateHistory.isNotEmpty()) {
            _state.value = stateHistory.removeLast()
        }
    }
}