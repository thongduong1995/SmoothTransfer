package com.example.smoothtransfer.viewmodel

import android.Manifest
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.example.smoothtransfer.data.local.MainDataModel
import com.example.smoothtransfer.network.wifi.WifiAwareManager
import com.example.smoothtransfer.ui.navigation.DeepLinkHandler
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.utils.PermissionHelper
import com.example.smoothtransfer.viewmodel.base.BaseFlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CloneFlowViewModel(
    /* private val transferCoordinator: TransferCoordinator */
    val context: Application
) : BaseFlowViewModel<PhoneClone.State, PhoneClone.Event>(context, PhoneClone.State.SelectRole),
    PhoneClone.PhoneCloneActions {

    companion object {
        private const val TAG = "SmartSwitch CloneFlowViewModel"
    }
    private var isSender: Boolean = true

    private var wifiAwareManager: WifiAwareManager? = null

    init {
        // ViewModel sẽ tự động lắng nghe các sự kiện từ DeepLinkHandler
        // ngay khi nó được tạo ra.
        wifiAwareManager = WifiAwareManager(context)

        DeepLinkHandler.eventFlow
            .onEach { event ->
                // Khi nhận được event, gọi hàm onEvent của chính ViewModel này
                onEvent(event)
            }
            .launchIn(viewModelScope) // Tự động hủy khi ViewModel bị hủy
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override suspend fun handleEvent(event: PhoneClone.Event) {

        when (event) {
            is PhoneClone.Event.RoleSelected -> {
                isSender = event.isSender
                if (PermissionHelper.hasAllPermissions(context)) {
                    updateState(PhoneClone.State.SelectTransferMethod(isSender))
                } else {
                    updateState(PhoneClone.State.RequestPermissions(isSender))
                }
            }

            is PhoneClone.Event.UsbAttached -> {
                // Cập nhật State để hiển thị màn hình Cable Connection
                _state.value = PhoneClone.State.Connecting("Connecting via Cable...")
            }

            is PhoneClone.Event.MethodSelected -> handleSelectMethodEvents(event)

            is PhoneClone.Event.QrCodeScanned -> {
                _state.value = PhoneClone.State.Connecting()
                wifiAwareManager?.setRole(WifiAwareManager.Role.SENDER)
                wifiAwareManager?.enable()
                // transferCoordinator.connectToDevice(event.qrData)
                // Giả lập kết nối thành công sau 2 giây
                delay(15000)
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
            is PhoneClone.Event.PermissionsGranted -> {
                updateState(PhoneClone.State.SelectTransferMethod(event.isSender))
            }
            else -> {
                // Xử lý các trường hợp khác
            }
        }

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handleSelectMethodEvents(event: PhoneClone.Event.MethodSelected) {
        if (event.isWifi) {
            if (isSender) {
                handleSelectMethodEventsFromSender(event)
            } else {
                Log.d(TAG, "handleSelectMethodEvents() - Receiver selected Wifi Aware, starting publisher")
                updateState(PhoneClone.State.DisplayQrCode(isSender))
                wifiAwareManager?.setRole(WifiAwareManager.Role.RECEIVER)
                wifiAwareManager?.enable()
            }

        } else { // Cable
            _state.value = PhoneClone.State.Connecting("Plug in USB cable...")
            // transferCoordinator.startCableConnection()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handleSelectMethodEventsFromSender(event: PhoneClone.Event.MethodSelected) {
        updateState(PhoneClone.State.ShowCameraToScanQr)
       /* wifiAwareManager?.setRole(WifiAwareManager.Role.SENDER)
        wifiAwareManager?.enable()*/
        //_state.value = PhoneClone.State.Connecting("Connecting")
        // transferCoordinator.startWifiScan()
    }
}