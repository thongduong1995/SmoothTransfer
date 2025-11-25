package com.example.smoothtransfer.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible = _isBottomBarVisible.asStateFlow()

    fun setBottomBarVisibility(isVisible: Boolean) {
        // Chỉ cập nhật nếu giá trị mới khác giá trị cũ để tránh recomposition không cần thiết
        if (_isBottomBarVisible.value != isVisible) {
            _isBottomBarVisible.update { isVisible }
        }
    }
}