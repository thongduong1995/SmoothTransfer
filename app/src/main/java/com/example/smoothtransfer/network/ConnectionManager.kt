package com.example.smoothtransfer.network

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.smoothtransfer.data.local.MainDataModel
import com.example.smoothtransfer.network.netty.NettyTcpClient
import com.example.smoothtransfer.network.netty.NettyTcpServer
import com.example.smoothtransfer.network.protocol.Commands
import com.example.smoothtransfer.network.protocol.DeviceInfo
import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.network.wifi.WifiAwareService
import com.example.smoothtransfer.transfer.TransferEvent
import com.example.smoothtransfer.transfer.TransferSession
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConnectionManager(
    private val context: Application
) {
    companion object {
        private const val TAG = "SmartSwitch ConnectionManager"
        private const val TCP_PORT = 8888
    }

    // Thay vì ConnectionEvent, giờ chúng ta dùng TransferEvent
    private val _events = MutableSharedFlow<TransferEvent>()
    val events: SharedFlow<TransferEvent> = _events.asSharedFlow()

    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val wifiAwareService: WifiAwareService = WifiAwareService(context)

    private val nettyServer by lazy {
        NettyTcpServer(
            onConnectionStateChange = { isConnected ->
                handleTcpConnectionResult(isConnected, isServer = true)
            },
            onPacketReceived = { packet ->
                handleReceivedPacket(packet)
            }
        )
    }

    private val nettyClient by lazy {
        NettyTcpClient (
            onConnectionStateChange = { isConnected ->
                handleTcpConnectionResult(isConnected, isServer = false)
            },
            onPacketReceived = { packet ->
                handleReceivedPacket(packet)
            }
        )
    }

    private fun handleTcpConnectionResult(connected: Boolean, isServer: Boolean) {
        val isSender = TransferSession.isSender()
        Log.d(TAG, "handleTcpConnectionResult. isSender: $isSender, connected: $connected, isServer: $isServer")
        if (connected) {
            acquireWakeLock()
            if (TransferSession.isSender()) {
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

    private fun sendPacket(packet: Packet) {
        val isSender = TransferSession.isSender()
        if (isSender) {
            nettyClient.sendPacket(packet)
        } else {
            nettyServer.sendPacket(packet)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startConnect(isWifi: Boolean, isSender: Boolean) {
        Log.d(TAG,"startConnect:  isWifi: $isWifi, isSender: $isSender")
        if (isWifi) {
            if (isSender) {
                listenForPeerIpAndConnect()
            } else {
                wifiAwareService.setRole(WifiAwareService.Role.RECEIVER)
                wifiAwareService.enable()
                listenForPeerIpAndConnect()
            }
        }
    }

    private fun listenForPeerIpAndConnect() {
        scope.launch {
            // Lấy địa chỉ IP đầu tiên không phải null từ MainDataModel
            val peerIp = MainDataModel.peerIP.filterNotNull().first()
            Log.d(TAG, "Got peer IP: $peerIp.")
            val isSender = TransferSession.isSender()
            if (isSender) {
                connectToServer(peerIp)
            } else {
                nettyServer.start ()
            }
            // Khi có IP, chuyển sang màn hình Connecting và bắt đầu kết nối TCP
            _events.emit(TransferEvent.OnConnecting("Connecting to server..."))


        }
    }

    private fun connectToServer(peerIp: String) {
        Log.d(TAG, "connectToServer Got peer IP: $peerIp. Starting Netty client...")
        nettyClient.disconnect()
        nettyClient.connect(peerIp, TCP_PORT)
    }

    fun stop() {
        Log.d(TAG, "Stopping ConnectionManager.")
        nettyClient.disconnect()
        nettyServer.stop()
    }

    private fun getDeviceName(): String {
        return try {
            // Try to get device name from Settings (Android 7.0+)
            val deviceName = android.provider.Settings.Global.getString(
                context.contentResolver,
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

    private fun handleReceivedPacket(packet: Packet) {
        val cmdName = Commands.getCommandName(packet.cmd)
        Log.d(TAG, "Smart Switch: handleReceivedPacket: cmd=$cmdName (${packet.cmd}), payloadSize=${packet.payload.size}")

        when (packet.cmd) {
            Commands.CMD_DEVICE_INFO  -> {
                Log.d(TAG, "Smart Switch: Received CMD_DEVICE_INFO")
                handleDeviceInfo(packet.payload)
            }

            Commands.CMD_CONTENT_LIST -> {
                Log.d(TAG, "Smart Switch: Received CMD_CONTENT_LIST")
            }
        }
    }

    private fun handleDeviceInfo(payload: ByteArray) {
        val deviceInfo = DeviceInfo.fromBytes(payload)
        if (deviceInfo != null) {
            Log.d(TAG, "Smart Switch: Received device info: deviceName=${deviceInfo.deviceName}, ipAddress=${deviceInfo.ipAddress}")

            val isSender = TransferSession.isSender()
            if (isSender) {
                Log.d(TAG, "Smart Switch: Receiver received device info but hasn't sent yet, sending now")
                scope.launch {
                    _events.emit(TransferEvent.OnConnected(deviceInfo))
                }
            } else {
                Log.d(TAG, "handleDeviceInfo: send back my device info")
                scope.launch {
                    _events.emit(TransferEvent.onSearchContent(deviceInfo))
                }
                sendDeviceInfo()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startPreSenderConnect() {
        wifiAwareService.setRole(WifiAwareService.Role.SENDER)
        wifiAwareService.enable()
    }

    private fun acquireWakeLock() {
        // Kiểm tra xem wakeLock đã được giữ chưa để tránh gọi lại không cần thiết
        if (wakeLock?.isHeld == true) {
            Log.d(TAG, "WakeLock is already held.")
            return
        }

        // Lấy PowerManager từ context của Application
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        // Tạo một PARTIAL_WAKE_LOCK mới.
        // PARTIAL_WAKE_LOCK: Giữ cho CPU hoạt động, nhưng cho phép màn hình và bàn phím tắt.
        // Đây là loại WakeLock phù hợp nhất để duy trì kết nối mạng mà không quá tốn pin.
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SmoothTransfer::ConnectionWakelockTag" // Một tên tag để debug
        )

        // Đặt thời gian timeout cho WakeLock để nó tự động được giải phóng
        // sau một khoảng thời gian dài (ví dụ: 1 tiếng) nếu có lỗi xảy ra
        // và chúng ta quên gọi releaseWakeLock().
        wakeLock?.acquire(1 * 60 * 60 * 1000L /* 1 hour timeout */)

        Log.i(TAG, "WakeLock acquired.")
    }

    /**
     * Giải phóng WakeLock, cho phép CPU có thể đi vào trạng thái ngủ để tiết kiệm pin.
     */
    private fun releaseWakeLock() {
        // Chỉ giải phóng nếu wakeLock đang thực sự được giữ
        if (wakeLock?.isHeld == true) {
            try {
                wakeLock?.release()
                Log.i(TAG, "WakeLock released.")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing WakeLock", e)
            } finally {
                wakeLock = null
            }
        }
    }

    // Biến để quản lý tác vụ gửi heartbeat
    private var heartbeatJob: Job? = null
    /**
     * Bắt đầu một coroutine định kỳ gửi gói tin heartbeat.
     */
    private fun startHeartbeat() {
        // Hủy job cũ nếu có để tránh chạy nhiều job cùng lúc
        stopHeartbeat()
        Log.d(TAG, "Starting heartbeat job.")
        heartbeatJob = scope.launch {
            while (true) {
                // Đợi 30 giây
                delay(30_000)

                // Tạo và gửi một gói tin heartbeat rỗng
                val heartbeatPacket = Packet(
                    cmd = Commands.CMD_HEARTBEAT, // Cần định nghĩa command này
                    isPath = false,
                    totalDataLength = 0,
                    curPos = 0,
                    payload = ByteArray(0)
                )
                sendPacket(heartbeatPacket)
                Log.v(TAG, "Heartbeat sent.")
            }
        }
    }

    /**
     * Dừng coroutine gửi heartbeat.
     */
    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        Log.d(TAG, "Heartbeat job stopped.")
    }
}