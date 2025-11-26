package com.example.smoothtransfer.network.netty


import android.util.Log
import com.example.smoothtransfer.network.encoder.PacketDecoder
import com.example.smoothtransfer.network.encoder.PacketEncoder
import com.example.smoothtransfer.network.protocol.Packet
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.GenericFutureListener
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class NettyTcpClient(
    private val onConnectionStateChange: (Boolean) -> Unit,
    private val onPacketReceived: (Packet) -> Unit
) {
    companion object {
        private const val PREFIX = "SmartSwitch"
        private const val TAG = "NettyTcpClient"
        private const val LOG_TAG = "$PREFIX $TAG"
        private const val CONNECT_TIMEOUT_MILLIS = 15000
        private const val DEFAULT_PORT = 8888
    }

    private var group: EventLoopGroup? = null
    private var channel: Channel? = null
    private val writabilityLock = ReentrantLock()
    private val writabilityCondition = writabilityLock.newCondition()

    fun connect(host: String, port: Int = DEFAULT_PORT) {
        // Đảm bảo dọn dẹp tài nguyên cũ trước khi tạo kết nối mới
        disconnect()
        group = NioEventLoopGroup()
        Log.d(LOG_TAG, "Smart Switch: Attempting to connect to $host:$port")

        val bootstrap = Bootstrap()
        bootstrap.group(group)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val pipeline = ch.pipeline()
                    pipeline.addLast(PacketDecoder())
                    pipeline.addLast(PacketEncoder())
                    pipeline.addLast(ClientHandler())
                }
            })
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS) // 10 seconds timeout

        Log.d(LOG_TAG, "Smart Switch: Attempting to connect to $host:$port")

        // Connect asynchronously and handle result in callback
        val future = bootstrap.connect(host, port)

        future.addListener(GenericFutureListener { futureResult: ChannelFuture ->
            if (futureResult.isSuccess) {
                channel = futureResult.channel()
                Log.d(LOG_TAG, "Smart Switch: Successfully connected to server: $host:$port")
                // channelActive will be called and set _isConnected to true
            } else {
                val cause = futureResult.cause()
                Log.e(LOG_TAG, "Smart Switch: Failed to connect to $host:$port", cause)
                onConnectionStateChange(false)
                disconnect()
            }
        })
    }

    fun disconnect() {
        channel?.close()
        group?.shutdownGracefully()
        channel = null
        group = null
    }

    fun sendPacket(packet: Packet) {
        if (channel == null) {
            Log.e(LOG_TAG, "Smart Switch: channel is null, cannot send packet")
            return
        }

        val ch = channel ?: return

        // Wait for channel to become writable if needed
        writabilityLock.withLock {
            // Wait while channel is not writable
            while (!ch.isWritable) {
                Log.d(LOG_TAG, "Smart Switch: Channel is not writable, waiting...")
                try {
                    // Wait for channelWritabilityChanged to signal
                    writabilityCondition.await()
                } catch (e: InterruptedException) {
                    Log.e(LOG_TAG, "Smart Switch: Interrupted while waiting for writability", e)
                    Thread.currentThread().interrupt()
                    return
                }
            }
        }

        // Channel is now writable, send packet (non-blocking, no .sync())
        ch.writeAndFlush(packet).addListener(GenericFutureListener { future ->
            if (!future.isSuccess) {
                Log.e(LOG_TAG, "Smart Switch: Failed to send packet: ${future.cause()}")
            }
        })
    }

    private inner class ClientHandler : ChannelInboundHandlerAdapter() {

        override fun channelActive(ctx: ChannelHandlerContext) {
            Log.d(LOG_TAG, "Smart Switch: Channel active, connected to server: ${ctx.channel().remoteAddress()}")
            onConnectionStateChange(true)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                onPacketReceived(msg)
            } else {
                Log.w(LOG_TAG, "Smart Switch: Received unknown message type: ${msg.javaClass.name}")
            }
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            Log.d(LOG_TAG, "Smart Switch: Disconnected from server")

            // Signal condition when channel is closed to wake up any waiting threads
            writabilityLock.withLock {
                writabilityCondition.signalAll()
            }
            onConnectionStateChange(false)
        }

        override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
            val isWritable = ctx.channel().isWritable

            if (isWritable) {
                // Channel is now writable, signal waiting threads
                writabilityLock.withLock {
                    writabilityCondition.signalAll()
                    Log.d(LOG_TAG, "Smart Switch: Channel is now writable, signaling waiting threads")
                }
            } else {
                Log.d(LOG_TAG, "Smart Switch: Channel is not writable (send buffer full)")
            }

            // Important: Call super to propagate the event
            ctx.fireChannelWritabilityChanged()
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            Log.e(LOG_TAG, "Smart Switch: Exception caught", cause)
            ctx.close()
        }
    }
}


