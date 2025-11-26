package com.example.smoothtransfer.network.wifi.netty


import android.util.Log
import com.example.smartswitchpc.network.encoder.PacketDecoder
import com.example.smoothtransfer.network.wifi.encoder.PacketEncoder
import com.example.smoothtransfer.network.wifi.protocol.Packet
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.GenericFutureListener
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * TcpClient - TCP client for sender device
 *
 * This class implements a TCP client using Netty framework.
 * Used by sender device to connect to receiver's TCP server.
 *
 * Architecture:
 * - EventLoopGroup: Handles I/O operations
 * - Channel: Connection to server
 * - Pipeline: PacketDecoder → PacketEncoder → ClientHandler
 *
 * Features:
 * - Connects to server at specified host:port
 * - Handles connection asynchronously
 * - Decodes/encodes packets automatically
 * - Provides connection state via StateFlow
 * - Handles packet reception and transmission
 *
 * Connection Flow:
 * 1. Call connect() with server IP and port
 * 2. Netty attempts connection asynchronously
 * 3. On success: channelActive() called, isConnected = true
 * 4. On failure: connection failed, cleanup resources
 *
 * Usage:
 * Used by ConnectionViewModel in sender mode.
 * Connects when sender scans QR code and gets receiver IP.
 *
 * Note: Default port is 8888 (matches receiver server port)
 */
class NettyTcpClient {
    companion object {
        /**
         * PREFIX: Log tag prefix for Smart Switch
         */
        private const val PREFIX = "Smart_Switch"

        /**
         * TAG: Log tag for this class
         */
        private const val TAG = "TcpClient"

        /**
         * LOG_TAG: Combined log tag (PREFIX + TAG)
         */
        private const val LOG_TAG = "$PREFIX $TAG"
    }

    /**
     * group: EventLoopGroup for handling I/O operations
     *
     * Manages threads for network I/O.
     * Created on connect, shutdown on disconnect.
     */
    private var group: EventLoopGroup? = null

    /**
     * channel: Connection channel to server
     *
     * Represents the TCP connection to receiver server.
     * Used to send packets and receive responses.
     * Set when connection succeeds, cleared on disconnect.
     */
    private var channel: Channel? = null

    /**
     * _isConnected: StateFlow for connection status
     *
     * true: Connected to server
     * false: Not connected
     *
     * Observed by UI to show connection status
     */
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /**
     * _receivedPacket: StateFlow for received packets
     *
     * Contains the most recently received packet from server.
     * Used for debugging and packet handling.
     */
    private val _receivedPacket = MutableStateFlow<Packet?>(null)
    val receivedPacket: StateFlow<Packet?> = _receivedPacket.asStateFlow()

    /**
     * writabilityLock: Lock for synchronizing writability state
     *
     * Used to wait for channel to become writable when send buffer is full.
     */
    private val writabilityLock = ReentrantLock()

    /**
     * writabilityCondition: Condition to wait for channel to become writable
     *
     * Signaled when channelWritabilityChanged() detects channel is writable.
     */
    private val writabilityCondition = writabilityLock.newCondition()

    /**
     * packetHandler: Callback for handling received packets
     *
     * Called when a packet is received from server.
     * Set by connect() method, used by ClientHandler.
     */
    private var packetHandler: ((Packet) -> Unit)? = null

    /**
     * connect - Connect to TCP server at specified host and port
     *
     * This method establishes a TCP connection to the receiver's server.
     * Connection is asynchronous and non-blocking.
     *
     * Process:
     * 1. Create EventLoopGroup for I/O operations
     * 2. Configure Bootstrap with channel type and handlers
     * 3. Set up pipeline: PacketDecoder → PacketEncoder → ClientHandler
     * 4. Attempt connection asynchronously
     * 5. Handle connection result (success/failure)
     *
     * Pipeline Setup:
     * - PacketDecoder: Decodes incoming ByteBuf to Packet objects
     * - PacketEncoder: Encodes outgoing Packet objects to ByteBuf
     * - ClientHandler: Handles connection events and packet reception
     *
     * Channel Options:
     * - SO_KEEPALIVE: Keep connection alive (true)
     * - CONNECT_TIMEOUT_MILLIS: Connection timeout (10 seconds)
     *
     * @param host Server IP address (e.g., "192.168.1.100")
     * @param port Server port number (default: 8888)
     * @param handler Callback function to handle received packets
     *                Called when a packet is received from server
     */
    fun connect(host: String, port: Int = 8888, handler: (Packet) -> Unit) {
        packetHandler = handler

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
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10 seconds timeout

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
                _isConnected.value = false
                disconnect()
            }
        })
    }

    /**
     * disconnect - Disconnect from server and cleanup resources
     *
     * This method gracefully closes the connection:
     * 1. Closes channel (sends FIN packet to server)
     * 2. Shuts down EventLoopGroup (waits for pending operations)
     * 3. Resets connection state
     * 4. Clears all references
     *
     * Note: shutdownGracefully() waits for pending operations to complete
     * before shutting down, ensuring no data loss.
     */
    fun disconnect() {
        channel?.close()
        group?.shutdownGracefully()

        _isConnected.value = false
        channel = null
        group = null
    }

    /**
     * sendPacket - Send packet to server with backpressure handling
     *
     * This method sends a Packet to the connected server.
     * Uses lock/condition to wait when channel is not writable.
     *
     * Process:
     * 1. Check if connected (channel != null)
     * 2. If channel is not writable, wait on condition
     * 3. When writable, send packet (non-blocking, no .sync())
     * 4. Handle send result (success/failure)
     *
     * Backpressure:
     * - Waits when channel.isWritable = false
     * - Resumes when channelWritabilityChanged() signals condition
     * - Prevents OutOfMemory by limiting queued packets
     *
     * Performance:
     * - Uses async writeAndFlush() for non-blocking operation
     * - No .sync() for better performance
     * - Reduces logging overhead (only logs errors)
     *
     * Error Handling:
     * - Returns early if channel is null
     * - Logs error if send fails
     * - Does not throw exceptions (graceful failure)
     *
     * @param packet Packet object to send to server
     */
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

    /**
     * ClientHandler - Netty channel handler for client-side operations
     *
     * This inner class handles all channel events for the client:
     * - channelActive: Called when connection is established
     * - channelRead: Called when packet is received
     * - channelInactive: Called when connection is closed
     * - channelWritabilityChanged: Called when channel writability changes
     * - exceptionCaught: Called when error occurs
     *
     * Lifecycle:
     * 1. Connection established → channelActive() → update isConnected
     * 2. Packet received → channelRead() → decode packet, call handler
     * 3. Connection closed → channelInactive() → update isConnected, signal condition
     * 4. Channel writability changes → channelWritabilityChanged() → signal condition
     * 5. Error occurs → exceptionCaught() → log error, close channel
     */
    private inner class ClientHandler : ChannelInboundHandlerAdapter() {

        /**
         * channelActive - Called when connection to server is established
         *
         * This method is called when the TCP connection to server is successfully established.
         * Updates connection state to indicate client is connected.
         *
         * @param ctx ChannelHandlerContext for the connection
         */
        override fun channelActive(ctx: ChannelHandlerContext) {
            _isConnected.value = true
            Log.d(LOG_TAG, "Smart Switch: Channel active, connected to server: ${ctx.channel().remoteAddress()}")
        }

        /**
         * channelRead - Called when data is received from server
         *
         * This method is called when a packet is received (after decoding).
         * Updates receivedPacket state and calls packet handler callback.
         *
         * @param ctx ChannelHandlerContext for the connection
         * @param msg Received message (should be Packet after decoding)
         */
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                //Log.d(LOG_TAG, "Smart Switch: Received packet: cmd=${msg.cmd}, payloadSize=${msg.payload.size}")
                _receivedPacket.value = msg
                packetHandler?.invoke(msg)
            } else {
                Log.w(LOG_TAG, "Smart Switch: Received unknown message type: ${msg.javaClass.name}")
            }
        }

        /**
         * channelInactive - Called when connection to server is closed
         *
         * This method is called when the connection to server is closed.
         * Updates connection state to indicate client is disconnected.
         * Signals condition to wake up any waiting threads.
         *
         * @param ctx ChannelHandlerContext for the connection
         */
        override fun channelInactive(ctx: ChannelHandlerContext) {
            _isConnected.value = false
            Log.d(LOG_TAG, "Smart Switch: Disconnected from server")

            // Signal condition when channel is closed to wake up any waiting threads
            writabilityLock.withLock {
                writabilityCondition.signalAll()
            }
        }

        /**
         * channelWritabilityChanged - Called when channel writability changes
         *
         * This method is called when the channel's writability state changes.
         * Signals condition to wake up threads waiting to send packets.
         *
         * @param ctx ChannelHandlerContext for the connection
         */
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

        /**
         * exceptionCaught - Called when exception occurs in pipeline
         *
         * This method handles exceptions that occur during packet processing.
         * Logs the error and closes the channel to prevent further issues.
         *
         * @param ctx ChannelHandlerContext for the connection
         * @param cause Throwable exception that occurred
         */
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            Log.e(LOG_TAG, "Smart Switch: Exception caught", cause)
            ctx.close()
        }
    }
}


