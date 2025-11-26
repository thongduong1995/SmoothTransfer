package com.example.smoothtransfer.viewmodel

import android.Manifest
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.viewModelScope
import com.example.smoothtransfer.data.local.MainDataModel
import com.example.smoothtransfer.network.netty.NettyTcpClient
import com.example.smoothtransfer.network.netty.NettyTcpServer
import com.example.smoothtransfer.network.protocol.Commands
import com.example.smoothtransfer.network.protocol.DeviceInfo
import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.network.wifi.WifiAwareService
import com.example.smoothtransfer.ui.navigation.DeepLinkHandler
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.utils.NetworkUtils
import com.example.smoothtransfer.utils.PermissionHelper
import com.example.smoothtransfer.viewmodel.base.BaseFlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CloneFlowViewModel(
    /* private val transferCoordinator: TransferCoordinator */
    val context: Application
) : BaseFlowViewModel<PhoneClone.State, PhoneClone.Event>(context, PhoneClone.State.SelectRole),
    PhoneClone.PhoneCloneActions {

    companion object {
        private const val TAG = "SmartSwitch CloneFlowViewModel"
    }

    private var isSender: Boolean = false
    private var wifiAwareService: WifiAwareService? = null

    private val nettyServer by lazy {
        NettyTcpServer { isConnected ->
            // Callback từ Server: Khi có client kết nối hoặc ngắt kết nối
            handleTcpConnectionResult(isConnected, isServer = true)
        }
    }

    private val nettyClient by lazy {
        NettyTcpClient { isConnected ->
            // Callback từ Client: Khi kết nối thành công hoặc thất bại
            handleTcpConnectionResult(isConnected, isServer = false)
        }
    }

    init {
        // ViewModel sẽ tự động lắng nghe các sự kiện từ DeepLinkHandler
        // ngay khi nó được tạo ra.
        wifiAwareService = WifiAwareService(context)

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
                updateState(PhoneClone.State.Connecting())
                //_state.value = PhoneClone.State.Connecting()
                wifiAwareService?.setRole(WifiAwareService.Role.SENDER)
                wifiAwareService?.enable()
                // transferCoordinator.connectToDevice(event.qrData)
                // Giả lập kết nối thành công sau 2 giây
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
                handleSelectMethodEventsFromReceiver(event)
            }
        } else { // Cable
            //_state.value = PhoneClone.State.Connecting("Plug in USB cable...")
            // transferCoordinator.startCableConnection()
        }
    }

    private fun handleReceivedPacket(packet: Packet) {
        val cmdName = Commands.getCommandName(packet.cmd)
        Log.d(TAG, "Smart Switch: handleReceivedPacket: cmd=$cmdName (${packet.cmd}), payloadSize=${packet.payload.size}")

        when (packet.cmd) {
            Commands.CMD_DEVICE_INFO  -> {
                Log.d(TAG, "Smart Switch: Received CMD_DEVICE_INFO")
                handleDeviceInfo(packet.payload)
            }
        }
    }

    private fun handleDeviceInfo(payload: ByteArray) {
        val deviceInfo = DeviceInfo.fromBytes(payload)
        if (deviceInfo != null) {
            Log.d(TAG, "Smart Switch: Received device info: deviceName=${deviceInfo.deviceName}, ipAddress=${deviceInfo.ipAddress}")

            if (isSender) {
                Log.d(TAG, "Smart Switch: Receiver received device info but hasn't sent yet, sending now")
                updateState(PhoneClone.State.Connected(deviceInfo))
            } else {
                Log.d(TAG, "handleDeviceInfo: send back my device info")
                sendDeviceInfo()
                updateState(PhoneClone.State.SearchingContent(deviceInfo))
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handleSelectMethodEventsFromSender(event: PhoneClone.Event.MethodSelected) {
        updateState(PhoneClone.State.ShowCameraToScanQr)
        listenForPeerIpAndConnect()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun handleSelectMethodEventsFromReceiver(event: PhoneClone.Event.MethodSelected) {
        Log.d(TAG, "handleSelectMethodEvents() - Receiver selected Wifi Aware, starting publisher")
        updateState(PhoneClone.State.DisplayQrCode(isSender))
        wifiAwareService?.setRole(WifiAwareService.Role.RECEIVER)
        wifiAwareService?.enable()
        Log.d(TAG, "handleSelectMethodEvents() - start server")
        listenForPeerIpAndConnect()
    }


    private fun sendPacket(packet: Packet) {
        if (isSender) {
            nettyClient.sendPacket(packet)
        } else {
            nettyServer.sendPacket(packet)
        }
    }


    private fun handleTcpConnectionResult(connected: Boolean, isServer: Boolean) {
        Log.d(
            TAG,
            "handleTcpConnectionResult. isSender: $isSender, connected: $connected, isServer: $isServer"
        )
        if (connected) {
            if (isSender) {
                sendDeviceInfo()
            }
        } else {

        }
    }


    private fun sendDeviceInfo() {
        try {
            val deviceName = getDeviceName()
            val ipAddress = NetworkUtils.getDeviceIpAddress(context) ?: "Unknown"

            val deviceInfo = DeviceInfo(
                deviceName = deviceName,
                ipAddress = ipAddress,
                additionalData = mapOf(
                    "model" to (Build.MODEL ?: "Unknown"),
                    "manufacturer" to (Build.MANUFACTURER ?: "Unknown"),
                    "androidVersion" to Build.VERSION.RELEASE
                )
            )

            val payload = deviceInfo.toBytes()
            val packet = Packet(
                cmd = Commands.CMD_DEVICE_INFO,
                isPath = false,
                totalDataLength = payload.size.toLong(),
                curPos = 0,
                payload = payload
            )

            sendPacket(packet)

            Log.i(TAG, "Smart Switch: sent CMD_DEVICE_INFO: $deviceInfo")
        } catch (e: Exception) {
            Log.e(TAG, "Smart Switch: Error sending device info: ${e.message}", e)
        }
    }

    private fun getDeviceName(): String {
        return try {
            // Try to get device name from Settings (Android 7.0+)
            val deviceName = android.provider.Settings.Global.getString(
                getApplication<Application>().contentResolver,
                android.provider.Settings.Global.DEVICE_NAME
            )
            if (!deviceName.isNullOrBlank()) {
                return deviceName
            }
            // Fallback to Build.MODEL
            Build.MODEL ?: "Android Device"
        } catch (e: Exception) {
            Log.e(TAG, "Smart Switch: Error getting device name: ${e.message}", e)
            Build.MODEL ?: "Android Device"
        }
    }

    private fun listenForPeerIpAndConnect() {
        viewModelScope.launch {
            // Lấy địa chỉ IP đầu tiên không phải null từ MainDataModel
            val peerIp = MainDataModel.peerIP.filterNotNull().first()
            if (isSender) {
                connectToServer(peerIp)
            } else {
                nettyServer.start { packet ->
                    handleReceivedPacket(packet)
                }
            }
            // Khi có IP, chuyển sang màn hình Connecting và bắt đầu kết nối TCP
            updateState(PhoneClone.State.Connecting("Connecting to server..."))
            Log.d(TAG, "Got peer IP: $peerIp.")
        }
    }

    private fun connectToServer(peerIp: String) {
        Log.d(TAG, "Got peer IP: $peerIp. Starting Netty client...")
        nettyClient.disconnect()
        nettyClient.connect(peerIp, 8888) { packet ->
            handleReceivedPacket(packet)
        }
    }
}