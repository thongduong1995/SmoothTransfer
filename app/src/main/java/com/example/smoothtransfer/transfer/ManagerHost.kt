package com.example.smoothtransfer.transfer

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.smoothtransfer.network.ConnectionManager
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class ManagerHost(private val application: Application) {
    companion object {
        private const val TAG = " SmartSwitch TransferCoordinator"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    //State giao tiếp với ViewModel
    private val _uiState = MutableStateFlow<PhoneClone.State>(PhoneClone.State.SelectRole)
    val uiState: StateFlow<PhoneClone.State> = _uiState

    // --- 2. Khai báo các Manager "vai trò" ---
    // ManagerHost giờ chỉ biết đến các manager cấp cao.
    private val connectionManager = ConnectionManager(application)
    // private val mediaManager = MediaManager(application) // Sẽ thêm sau
    // private val apkManager = ApkManager(application)   // Sẽ thêm sau

    init {
        val allEventsFlow = flowOf(
            connectionManager.events
            // , mediaManager.events // Sẽ thêm sau
            // , apkManager.events   // Sẽ thêm sau
        ).flattenMerge()

        // Chỉ cần một hàm lắng nghe duy nhất
        listenToAllTransferEvents(allEventsFlow)
    }

    private fun listenToAllTransferEvents(eventsFlow: Flow<TransferEvent>) {
        eventsFlow
            .onEach { event ->
                Log.d(TAG, "Processing event: ${event::class.simpleName}")

                // Dùng `when` để xử lý tất cả các loại sự kiện
                // và cập nhật _uiState tương ứng.
                val newState = when (event) {
                    is TransferEvent.OnConnecting -> PhoneClone.State.Connecting(event.message)
                    is TransferEvent.OnConnected -> {
                        // Khi kết nối thành công, có thể ra lệnh cho MediaManager quét file
                        // mediaManager.scanImages()
                        PhoneClone.State.Connected(event.deviceInfo)
                    }
                    is TransferEvent.OnConnectionLost -> PhoneClone.State.Error(event.reason)
                    is TransferEvent.onSearchContent -> PhoneClone.State.SearchingContent(event.deviceInfo)
                }
                _uiState.value = newState
            }
            .launchIn(scope)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startConnectionProcess(isWifi: Boolean, isSender: Boolean) {
        connectionManager.startPreSenderConnect()
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startConnect(isWifi: Boolean, sender: Boolean) {
        connectionManager.startConnect(isWifi, sender)
    }


}