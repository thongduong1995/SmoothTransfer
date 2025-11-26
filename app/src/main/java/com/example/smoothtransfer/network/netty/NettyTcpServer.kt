package com.example.smoothtransfer.network.netty

import android.R.attr.port
import android.util.Log
import com.example.smoothtransfer.network.encoder.PacketDecoder
import com.example.smoothtransfer.network.encoder.PacketEncoder
import com.example.smoothtransfer.network.protocol.Commands
import com.example.smoothtransfer.network.protocol.Packet
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.jvm.java
import kotlin.jvm.javaClass

class NettyTcpServer(
    private val onConnectionStateChange: (Boolean) -> Unit,
    private val onPacketReceived: (Packet) -> Unit // <<< Di chuyển vào constructor
) {
    companion object {

        private const val PREFIX = "SmartSwitch"

        private const val TAG = "NettyTcpServer"

        private const val LOG_TAG = "$PREFIX $TAG"
        private const val DEFAULT_PORT = 8888
    }

    private val port: Int = 8888
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var serverChannel: Channel? = null
    private var clientChannel: Channel? = null
    private val _isConnected = MutableStateFlow(false)

    fun start(port: Int = DEFAULT_PORT) {
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()
        
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(PacketDecoder())
                        pipeline.addLast(PacketEncoder())
                        pipeline.addLast(ServerHandler())
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            
            val future = bootstrap.bind(port).sync()
            serverChannel = future.channel()
            
            // Server is now listening
            Log.d(LOG_TAG, "Smart Switch: TCP Server started on port $port")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Smart Switch: Failed to start TCP server on port $port", e)
            stop()
        }
    }

    fun stop() {
        clientChannel?.close()
        serverChannel?.close()
        workerGroup?.shutdownGracefully()
        bossGroup?.shutdownGracefully()
        
        _isConnected.value = false
        clientChannel = null
        serverChannel = null
        bossGroup = null
        workerGroup = null
    }

    fun sendPacket(packet: Packet) {
        val cmdName = Commands.getCommandName(packet.cmd)
        Log.d(LOG_TAG, "Smart Switch: sendPacket: cmd=$cmdName (${packet.cmd}), payloadSize=${packet.payload.size}")
        if (clientChannel == null) {
            Log.e(LOG_TAG, "Smart Switch: clientChannel is null, cannot send packet")
        } else {
            clientChannel?.writeAndFlush(packet)?.addListener { future ->
                if (!future.isSuccess) {
                    Log.e(LOG_TAG, "Smart Switch: Failed to send packet: ${future.cause()}")
                } else {
                    //Log.d(LOG_TAG, "Smart Switch: Packet sent successfully")
                }
            }
        }
    }

    private inner class ServerHandler : ChannelInboundHandlerAdapter() {
        override fun channelActive(ctx: ChannelHandlerContext) {
            clientChannel = ctx.channel()
            _isConnected.value = true
            Log.d(LOG_TAG, "Smart Switch: Client connected: ${ctx.channel().remoteAddress()}")
            onConnectionStateChange(true)
        }
        

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                //Log.d(LOG_TAG, "Smart Switch: Received packet: cmd=${msg.cmd}, payloadSize=${msg.payload.size}")
                onPacketReceived(msg)
            } else {
                Log.w(LOG_TAG, "Smart Switch: Received unknown message type: ${msg.javaClass.name}")
            }
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            _isConnected.value = false
            clientChannel = null
            Log.d(LOG_TAG, "Smart Switch: Client disconnected")
            onConnectionStateChange(false)
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            Log.e(LOG_TAG, "Smart Switch: Exception caught", cause)
            ctx.close()
        }
    }
}


