package com.example.smoothtransfer.network

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.smoothtransfer.network.protocol.Commands
import com.example.smoothtransfer.network.protocol.DeviceInfo
import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.network.strategies.IDiscoveryStrategy
import com.example.smoothtransfer.network.strategies.StrategyEvent
import com.example.smoothtransfer.network.wifi.WifiAwareService
import com.example.smoothtransfer.transfer.ServiceType
import com.example.smoothtransfer.transfer.TransferEvent
import com.example.smoothtransfer.transfer.TransferSession
import com.example.smoothtransfer.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ConnectionManager(
    private val context: Application
) {
    companion object {
        private const val TAG = "SmartSwitch ConnectionManager"
    }

    private val _events = MutableSharedFlow<TransferEvent>()
    val events: SharedFlow<TransferEvent> = _events.asSharedFlow()

    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private var transferService: IDiscoveryStrategy? = null

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
        transferService?.sendPacket(packet)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun startConnect() {
        initConnectionService()
        transferService?.startConnect()
    }

    private fun initConnectionService() {
        Log.d(TAG, "initConnectionService")

        val role = TransferSession.getRole()
        val serviceType = TransferSession.getServiceType()
        Log.d(TAG, "Starting with role: $role, serviceType: $serviceType")

        transferService?.stop()
        transferService = when (serviceType) {
            ServiceType.WIFI_AWARE -> WifiAwareService(context)
            ServiceType.WIFI_DIRECT -> null
            ServiceType.OTG -> null
            ServiceType.NONE -> null
        }

        // 2. Lắng nghe "kênh phát sóng" của phương thức đó
        listenToMethodEvents()
    }

    private fun listenToMethodEvents() {
        scope.launch {
            transferService?.events?.collect { event ->
                // Khi nhận được "tin tức" từ WifiAwareService...
                Log.d(TAG, "Received MethodEvent: ${event::class.simpleName}")

                // ...ConnectionManager sẽ xử lý và "dịch" nó thành TransferEvent
                // để báo cáo lên cấp cao hơn (ManagerHost).
                when (event) {
                    is StrategyEvent.IpAddressAvailable -> {
                        // Đây chính là nơi bạn muốn emit OnConnecting
                        _events.emit(TransferEvent.OnConnecting("Peer found. Establishing TCP..."))
                    }
                    is StrategyEvent.TransportConnectionChanged -> {
                        if (event.isConnected) {
                            acquireWakeLock()
                            startHeartbeat()
                            if (TransferSession.isSender()) sendDeviceInfo()
                        } else {
                            releaseWakeLock()
                            stopHeartbeat()
                            _events.emit(TransferEvent.OnConnectionLost("Transport disconnected"))
                        }
                    }
                    is StrategyEvent.PacketReceived -> handleReceivedPacket(event.packet)
                    is StrategyEvent.Error -> _events.emit(TransferEvent.OnConnectionLost(event.message))
                }
            }
        }
    }


    fun stop() {
        Log.d(TAG, "Stopping ConnectionManager.")
        transferService?.stop()
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
       transferService?.startPreSender()
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

    fun prepareQrCodeData() {
        scope.launch {
            val qrData = transferService?.getQrCodeData()
            if (qrData != null) {
                _events.emit(TransferEvent.OnQrCodeReady(qrData))
            } else {
                Log.e(TAG, "prepareQrCodeData: currentMethod is null or does not support QR code.")
                _events.emit(TransferEvent.OnError("This connection method does"))
            }
        }
    }


}