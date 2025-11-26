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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.java
import kotlin.jvm.javaClass

/**
 * TcpServer - TCP server for receiver device
 * 
 * This class implements a TCP server using Netty framework.
 * Used by receiver device to listen for incoming connections from sender.
 * 
 * Architecture:
 * - Boss Group: Single thread for accepting connections
 * - Worker Group: Multiple threads for handling I/O operations
 * - Server Channel: Listens on specified port
 * - Client Channel: Active connection from sender
 * 
 * Features:
 * - Listens on configurable port (default: 8888)
 * - Handles incoming connections
 * - Decodes/encodes packets automatically
 * - Provides connection state via StateFlow
 * - Handles packet reception and transmission
 * 
 * Netty Pipeline:
 * - PacketDecoder: Decodes incoming ByteBuf to Packet
 * - PacketEncoder: Encodes outgoing Packet to ByteBuf
 * - ServerHandler: Handles connection events and packet reception
 * 
 * Usage:
 * Used by ConnectionViewModel in receiver mode.
 * Started when receiver screen displays QR code.
 * 
 * @param port Port number to listen on (default: 8888)
 */
class NettyTcpServer(private val onStateChange: (Boolean) -> Unit) {

    companion object {
        /**
         * PREFIX: Log tag prefix for Smart Switch
         */
        private const val PREFIX = "SmartSwitch"
        
        /**
         * TAG: Log tag for this class
         */
        private const val TAG = "TcpServer"
        
        /**
         * LOG_TAG: Combined log tag (PREFIX + TAG)
         */
        private const val LOG_TAG = "$PREFIX $TAG"
    }

    private val port: Int = 8888
    
    /**
     * bossGroup: EventLoopGroup for accepting connections
     * 
     * Single thread that accepts incoming connections.
     * Once connection is accepted, it's passed to workerGroup.
     */
    private var bossGroup: EventLoopGroup? = null
    
    /**
     * workerGroup: EventLoopGroup for handling I/O
     * 
     * Multiple threads that handle actual data transfer.
     * Processes packets from connected clients.
     */
    private var workerGroup: EventLoopGroup? = null
    
    /**
     * serverChannel: Server socket channel
     * 
     * Listens on specified port for incoming connections.
     * Created when server starts, closed when server stops.
     */
    private var serverChannel: Channel? = null
    
    /**
     * clientChannel: Active client connection channel
     * 
     * Represents the connected sender device.
     * Used to send packets to sender.
     * Set when client connects, cleared when client disconnects.
     */
    private var clientChannel: Channel? = null
    
    /**
     * _isConnected: StateFlow for connection status
     * 
     * true: Client is connected
     * false: No client connected
     * 
     * Observed by UI to show connection status
     */
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    /**
     * _receivedPacket: StateFlow for received packets
     * 
     * Contains the most recently received packet.
     * Used for debugging and packet handling.
     */
    private val _receivedPacket = MutableStateFlow<Packet?>(null)
    val receivedPacket: StateFlow<Packet?> = _receivedPacket.asStateFlow()
    
    /**
     * packetHandler: Callback for handling received packets
     * 
     * Called when a packet is received from client.
     * Set by start() method, used by ServerHandler.
     */
    private var packetHandler: ((Packet) -> Unit)? = null
    
    /**
     * start - Start the TCP server and begin listening for connections
     * 
     * This method initializes Netty server bootstrap and starts listening
     * on the specified port. Sets up pipeline for packet encoding/decoding.
     * 
     * Process:
     * 1. Create boss and worker event loop groups
     * 2. Configure ServerBootstrap with channel type and handlers
     * 3. Set up pipeline: PacketDecoder → PacketEncoder → ServerHandler
     * 4. Bind to port and start listening
     * 5. Handle errors and cleanup on failure
     * 
     * Pipeline Setup:
     * - PacketDecoder: Decodes incoming ByteBuf to Packet objects
     * - PacketEncoder: Encodes outgoing Packet objects to ByteBuf
     * - ServerHandler: Handles connection events and packet reception
     * 
     * Channel Options:
     * - SO_BACKLOG: Maximum pending connections (128)
     * - SO_KEEPALIVE: Keep connection alive (true)
     * 
     * @param handler Callback function to handle received packets
     *                Called when a packet is received from client
     */
    fun start(handler: (Packet) -> Unit) {
        packetHandler = handler
        
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
    
    /**
     * stop - Stop the TCP server and cleanup resources
     * 
     * This method gracefully shuts down the server:
     * 1. Closes client connection if active
     * 2. Closes server socket
     * 3. Shuts down worker group (waits for pending operations)
     * 4. Shuts down boss group (waits for pending operations)
     * 5. Resets connection state
     * 6. Clears all references
     * 
     * Note: shutdownGracefully() waits for pending operations to complete
     * before shutting down, ensuring no data loss.
     */
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
    
    /**
     * sendPacket - Send packet to connected client
     * 
     * This method sends a Packet to the connected sender device.
     * Packet is automatically encoded by PacketEncoder in pipeline.
     * 
     * Process:
     * 1. Check if client is connected (clientChannel != null)
     * 2. Write packet to channel (async, non-blocking)
     * 3. Flush to ensure packet is sent immediately
     * 4. Handle send result (success/failure)
     * 
     * Error Handling:
     * - Logs error if clientChannel is null
     * - Logs error if send fails
     * - Does not throw exceptions (graceful failure)
     * 
     * @param packet Packet object to send to client
     * 
     * Note: writeAndFlush() is async and non-blocking.
     * Packet is queued and sent asynchronously.
     */
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
    
    /**
     * ServerHandler - Netty channel handler for server-side operations
     * 
     * This inner class handles all channel events for the server:
     * - channelActive: Called when client connects
     * - channelRead: Called when packet is received
     * - channelInactive: Called when client disconnects
     * - exceptionCaught: Called when error occurs
     * 
     * Lifecycle:
     * 1. Client connects → channelActive() → set clientChannel, update isConnected
     * 2. Packet received → channelRead() → decode packet, call handler
     * 3. Client disconnects → channelInactive() → clear clientChannel, update isConnected
     * 4. Error occurs → exceptionCaught() → log error, close channel
     */
    private inner class ServerHandler : ChannelInboundHandlerAdapter() {
        
        /**
         * channelActive - Called when client connects to server
         * 
         * This method is called when a new client connection is established.
         * Sets up the client channel and updates connection state.
         * 
         * @param ctx ChannelHandlerContext for the connection
         */
        override fun channelActive(ctx: ChannelHandlerContext) {
            clientChannel = ctx.channel()
            _isConnected.value = true
            Log.d(LOG_TAG, "Smart Switch: Client connected: ${ctx.channel().remoteAddress()}")
            onStateChange(true)
        }
        
        /**
         * channelRead - Called when data is received from client
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
         * channelInactive - Called when client disconnects
         * 
         * This method is called when the client connection is closed.
         * Cleans up client channel and updates connection state.
         * 
         * @param ctx ChannelHandlerContext for the connection
         */
        override fun channelInactive(ctx: ChannelHandlerContext) {
            _isConnected.value = false
            clientChannel = null
            Log.d(LOG_TAG, "Smart Switch: Client disconnected")
            onStateChange(false)
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


