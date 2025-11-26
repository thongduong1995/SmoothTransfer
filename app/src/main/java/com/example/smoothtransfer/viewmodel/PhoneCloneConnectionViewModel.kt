package com.example.smoothtransfer.viewmodel

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.example.smoothtransfer.network.ConnectionManager
import com.example.smoothtransfer.network.ConnectionManagerListener
import com.example.smoothtransfer.ui.navigation.DeepLinkHandler
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.utils.PermissionHelper
import com.example.smoothtransfer.viewmodel.base.BaseFlowViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PhoneCloneConnectionViewModel(
    /* private val transferCoordinator: TransferCoordinator */
    val context: Application
) : BaseFlowViewModel<PhoneClone.State, PhoneClone.Event>(context, PhoneClone.State.SelectRole),
    PhoneClone.PhoneCloneActions, ConnectionManagerListener {

    companion object {
        private const val TAG = "SmartSwitch CloneFlowViewModel"
    }

    private var mediaViewModel: MediaViewModel? = null

    private val connectionManager = ConnectionManager(context, this)
    private var isSender: Boolean = false

    init {
        DeepLinkHandler.eventFlow
            .onEach { event ->
                // Khi nhận được event, gọi hàm onEvent của chính ViewModel này
                onEvent(event)
            }
            .launchIn(viewModelScope) // Tự động hủy khi ViewModel bị hủy
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override suspend fun handleEvent(event: PhoneClone.Event) {
        Log.d(TAG, "handleEvent. event: $event")
        when (event) {
            is PhoneClone.Event.RoleSelected -> handleRoleSelected(event)

            is PhoneClone.Event.UsbAttached -> {
                // Cập nhật State để hiển thị màn hình Cable Connection
                //_state.value = PhoneClone.State.Connecting("Connecting via Cable...")
            }

            is PhoneClone.Event.MethodSelected -> handleSelectMethodEvents(event)

            is PhoneClone.Event.QrCodeScanned -> {
                updateUiState(PhoneClone.State.Connecting())
                connectionManager.startPreSenderConnect()
            }



            is PhoneClone.Event.BackPressed -> navigateBack()
            // --- Các sự kiện khác sẽ được thêm vào đây ---
            // is CloneFlowContract.Event.ContentListReady -> { ... }
            // is CloneFlowContract.Event.RequestStartTransfer -> { ... }
            // ...
            is PhoneClone.Event.PermissionsGranted -> {
                updateUiState(PhoneClone.State.SelectTransferMethod(event.isSender))
            }

            else -> {
                // Xử lý các trường hợp khác
            }
        }

    }

    private fun handleRoleSelected(event: PhoneClone.Event.RoleSelected) {
        isSender = event.isSender
        connectionManager.isSender = isSender
        if (PermissionHelper.hasAllPermissions(context)) {
            updateUiState(PhoneClone.State.SelectTransferMethod(isSender))
        } else {
            updateUiState(PhoneClone.State.RequestPermissions(isSender))
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handleSelectMethodEvents(event: PhoneClone.Event.MethodSelected) {
        if (event.isWifi) {
            if (isSender) {
                updateUiState(PhoneClone.State.ShowCameraToScanQr)
            } else {
                updateUiState(PhoneClone.State.DisplayQrCode(isSender))
            }
            connectionManager.startConnect(isWifi = true, isSender)
        } else { // Cable
        }
    }

    fun setMediaViewModel(model: MediaViewModel) {
        mediaViewModel = model
    }

    override fun onStateUpdate(newState: PhoneClone.State) {
        updateUiState(newState)
    }
}