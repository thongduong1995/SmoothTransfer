package com.example.smoothtransfer.network.netty

import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.transfer.TransferSession


class NettyTransport(
    // Các callback để báo cáo lại cho Manager tổng
    private val onConnectionStateChange: (Boolean) -> Unit,
    private val onPacketReceived: (Packet) -> Unit
) {
    private val nettyServer = NettyTcpServer(onConnectionStateChange, onPacketReceived)
    private val nettyClient = NettyTcpClient(onConnectionStateChange, onPacketReceived)
    fun startServer() = nettyServer.start()
    fun stopServer() = nettyServer.stop()
    fun connectToServer(ip: String, port: Int) = nettyClient.connect(ip, port)
    fun disconnectClient() = nettyClient.disconnect()
    fun sendPacket(packet: Packet) {
        if (TransferSession.isSender()) nettyClient.sendPacket(packet) else nettyServer.sendPacket(packet)
    }
}