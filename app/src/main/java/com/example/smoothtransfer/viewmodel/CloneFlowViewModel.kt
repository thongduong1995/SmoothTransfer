package com.example.smoothtransfer.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.smoothtransfer.viewmodel.base.BaseFlowViewModel
import com.example.smoothtransfer.ui.navigation.DeepLinkHandler
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CloneFlowViewModel(/* private val transferCoordinator: TransferCoordinator */) :
    BaseFlowViewModel<PhoneClone.State, PhoneClone.Event>(PhoneClone.State.SelectRole),
    PhoneClone.PhoneCloneActions {
    private var isSender: Boolean = true
    init {
        // ViewModel sẽ tự động lắng nghe các sự kiện từ DeepLinkHandler
        // ngay khi nó được tạo ra.
        DeepLinkHandler.eventFlow
            .onEach { event ->
                // Khi nhận được event, gọi hàm onEvent của chính ViewModel này
                onEvent(event)
            }
            .launchIn(viewModelScope) // Tự động hủy khi ViewModel bị hủy
    }

    override suspend fun handleEvent(event: PhoneClone.Event) {

        when (event) {
            is PhoneClone.Event.RoleSelected -> {
                isSender = event.isSender
                updateState(PhoneClone.State.SelectTransferMethod)
            }

            is PhoneClone.Event.UsbAttached -> {
                // Cập nhật State để hiển thị màn hình Cable Connection
                _state.value = PhoneClone.State.Connecting("Connecting via Cable...")
            }

            is PhoneClone.Event.MethodSelected -> handleSelectMethodEvents(event)

            is PhoneClone.Event.QrCodeScanned -> {
                _state.value = PhoneClone.State.Connecting()
                // transferCoordinator.connectToDevice(event.qrData)
                // Giả lập kết nối thành công sau 2 giây
                delay(2000)
                onEvent(PhoneClone.Event.ConnectionEstablished)
            }

            is PhoneClone.Event.ConnectionEstablished -> {
                _state.value = PhoneClone.State.Connected
                // transferCoordinator.sendContentList()
            }

            is PhoneClone.Event.BackPressed -> navigateBack()
            // --- Các sự kiện khác sẽ được thêm vào đây ---
            // is CloneFlowContract.Event.ContentListReady -> { ... }
            // is CloneFlowContract.Event.RequestStartTransfer -> { ... }
            // ...
            else -> {
                // Xử lý các trường hợp khác
            }
        }

    }

    private fun handleSelectMethodEvents(event: PhoneClone.Event.MethodSelected) {
        if (event.isWifi) {
            _state.value = PhoneClone.State.ShowQrCodeToScan
            // transferCoordinator.startWifiScan()
        } else { // Cable
            _state.value = PhoneClone.State.Connecting("Plug in USB cable...")
            // transferCoordinator.startCableConnection()
        }
    }
}