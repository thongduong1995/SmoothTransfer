package com.example.smoothtransfer.network.strategies

import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.network.wifi.WifiQrData
import kotlinx.coroutines.flow.SharedFlow

/** * Các sự kiện mà một chiến lược có thể phát ra để báo cáo cho ConnectionManager.
 */
sealed interface StrategyEvent {
    data class IpAddressAvailable(val ipAddress: String) : StrategyEvent
    data class TransportConnectionChanged(val isConnected: Boolean) : StrategyEvent
    data class PacketReceived(val packet: Packet) : StrategyEvent
    data class Error(val message: String) : StrategyEvent
}

/**
 * Giao diện chung cho tất cả các "chiến lược" kết nối.
 * Mỗi công nghệ (WifiAware, WifiDirect, OTG) sẽ triển khai interface này.
 */
interface IDiscoveryStrategy {
    /**
     * Một Flow để phát ra các sự kiện mạng quan trọng.
     * ConnectionManager sẽ lắng nghe Flow này.
     */
    val events: SharedFlow<StrategyEvent>

    /**
     * Bắt đầu chiến lược với vai trò được chỉ định.
     */
    fun startConnect()

    fun startPreSender()


    /**
     * Dừng và giải phóng tất cả tài nguyên của chiến lược này.
     */
    fun stop()

    /**
     * Gửi một gói tin đi. Việc gửi như thế nào (qua Netty, BluetoothSocket...)
     * sẽ do chính Strategy quyết định.
     */
    fun sendPacket(packet: Packet)
    fun startConnectAsSender(peerIp: String)
    fun getQrCodeData(): WifiQrData
}