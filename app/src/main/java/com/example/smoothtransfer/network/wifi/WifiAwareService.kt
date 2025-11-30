package com.example.smoothtransfer.network.wifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkSpecifier
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.IdentityChangedListener
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareNetworkInfo
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.smoothtransfer.data.local.MainDataModel
import com.example.smoothtransfer.network.netty.NettyTransport
import com.example.smoothtransfer.network.protocol.Packet
import com.example.smoothtransfer.network.strategies.IDiscoveryStrategy
import com.example.smoothtransfer.network.strategies.StrategyEvent
import com.example.smoothtransfer.transfer.TransferSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class WifiAwareService(
    private val context: Context
): IDiscoveryStrategy {
    companion object {
        private const val TAG = "SmartSwitch WifiAwareService"
        private const val SERVICE_NAME = "SmoothTransfer_Service"
        private const val PSK_PASSPHRASE = "SmoothTransfer2024"
        private const val TCP_PORT = 8888
    }

    enum class Role {
        SENDER,
        RECEIVER
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _events = MutableSharedFlow<StrategyEvent>()
    override val events: SharedFlow<StrategyEvent> = _events.asSharedFlow()


    var currentRole: Role = Role.SENDER

    private val wifiAwareManager: WifiAwareManager? =
        context.getSystemService(Context.WIFI_AWARE_SERVICE) as? WifiAwareManager

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val nettyTransport = NettyTransport(
        onConnectionStateChange = { isConnected -> scope.launch { _events.emit(StrategyEvent.TransportConnectionChanged(isConnected)) } },
        onPacketReceived = { packet -> scope.launch { _events.emit(StrategyEvent.PacketReceived(packet)) } }
    )

    private var awareSession: WifiAwareSession? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null

    private var networkSpecifier: NetworkSpecifier? = null

    private var peerHandle: PeerHandle? = null

    private var myMacAddress = ""

    private var myIP: String? = null
    private var peerIP: String? = null

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Attaching : ConnectionState()
        object Publishing : ConnectionState()
        object Subscribing : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val network: Network, val socket: Socket?) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startConnect() {
        val isWifi = TransferSession.getServiceType().isWifi()
        val isSender = TransferSession.isSender()
        Log.d(TAG,"startConnect:  serviceType: ${TransferSession.getServiceType()}, isSender: $isSender")
        if (isWifi) {
            if (isSender) {
                listenForPeerIpAndConnect()
            } else {
                setRole(Role.RECEIVER)
                enable()
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
                Log.d(TAG, "connectToServer Got peer IP: $peerIp. Starting Netty client...")
                startConnectAsSender(peerIp)
            } else {
                nettyTransport.startServer()
            }
            // Khi có IP, chuyển sang màn hình Connecting và bắt đầu kết nối TCP
            //_events.emit(TransferEvent.OnConnecting("Connecting to server..."))
            _events.emit(StrategyEvent.IpAddressAvailable(peerIp))

        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startPreSender() {
        setRole(Role.SENDER)
        enable()
    }

    override fun stop() {
        nettyTransport.stopServer()
        nettyTransport.disconnectClient()
    }

    override fun sendPacket(packet: Packet) {
        nettyTransport.sendPacket(packet)
    }

    /**
     * Check if Wi-Fi Aware is available
     */
    fun isAvailable(): Boolean {
        return wifiAwareManager?.isAvailable == true
    }

    override fun startConnectAsSender(peerIp: String) {
        nettyTransport.disconnectClient()
        nettyTransport.connectToServer(peerIp, TCP_PORT)
    }

    override fun getQrCodeData(): WifiQrData {
        return WifiQrData("SmartSwitch", "11111", "")
    }

    fun setRole(role: Role) {
        this.currentRole = role
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    fun enable() {
        Log.d(TAG, "enable")
        attachSession()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun attachSession() {
        Log.d(TAG, "attachSession")
        if (wifiAwareManager == null || !wifiAwareManager!!.isAvailable) {
            Log.d(TAG, "Wifi Aware is Unavailable in attach")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiAwareManager.attach(object : AttachCallback() {
            @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
            override fun onAttached(session: WifiAwareSession) {
                super.onAttached(session)
                Log.d(TAG, "onAttached")
                closeSession()
                awareSession = session
                when (currentRole) {
                    Role.SENDER -> subscribe()
                    Role.RECEIVER -> publish()
                }
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                Log.d(TAG, "onAttachFailed")
            }
        }, object : IdentityChangedListener() {
            override fun onIdentityChanged(mac: ByteArray) {
                super.onIdentityChanged(mac)
                myMacAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5])

            }
        }, null)
    }



    private fun closeSession() {
        publishSession?.close()
        publishSession = null

        subscribeSession?.close()
        subscribeSession = null

        awareSession?.close()
        awareSession = null
    }

    @RequiresPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
    private fun publish() {
        Log.d(TAG, "Starting publish...")
        val config = PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .setServiceSpecificInfo("SmartSwitch".toByteArray())
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        awareSession?.publish(config, object : DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                super.onPublishStarted(session)
                Log.d(TAG, "onPublishStarted")
                publishSession = session
                if(publishSession == null || peerHandle == null) return
                //connect(true)
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray,
                matchFilter: List<ByteArray>) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                Log.d(TAG, "onServiceDiscovered")
                Log.d(TAG, "onServiceDiscovered() - peerHandle found")
                val discoveredPeerId = if (serviceSpecificInfo.isNotEmpty()) {
                    String(serviceSpecificInfo, Charsets.UTF_8)
                } else {
                    ""
                }
                Log.d(TAG, "Discovered peerId: '$discoveredPeerId'")
                this@WifiAwareService.peerHandle = peerHandle
                //connect()
                if (subscribeSession != null && peerHandle != null) {
                    //connect(true)
                }
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray) {
                super.onMessageReceived(peerHandle, message)
                Log.d(TAG, "onMessageReceived")

                if(publishSession == null || peerHandle == null) return


                this@WifiAwareService.peerHandle = peerHandle
                val ss = ServerSocket(0)
                val port = ss.localPort
                networkSpecifier = WifiAwareNetworkSpecifier.Builder(publishSession!!, peerHandle!!)
                    .setPskPassphrase(PSK_PASSPHRASE)
                    .setPort(port)
                    .setTransportProtocol(6) // TCP = 6
                    .build()
                try {
                    requestNetwork()
                    connect(true)
                } catch (e: Exception) {
                    Log.d(TAG, "exception $e")
                }

                /*val packet = D2dPacket.parseFromLite(message) ?: return
                if (packet.cmd == Command.CMD_AWARE_CONNECTION_INFO) {
                    val msg = try {
                        ByteUtil.getString(packet.getLiteData())
                    } catch (e: Exception) {
                        Log.v(TAG, "exception $e")
                        return
                    }

                    Log.d(TAG, "received message(%s)", msg)

                    if (msg == myMacAddress || msg.contains(myMacAddress)) {

                    }
                }*/
            }
        }, null)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun subscribe() {
        Log.d(TAG, "Starting subscribe...")
        val config = SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .setServiceSpecificInfo("SmartSwitch".toByteArray()) //
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        awareSession?.subscribe(config, object : DiscoverySessionCallback() {
            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray,
                matchFilter: List<ByteArray>) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                Log.d(TAG, "onServiceDiscovered")
                Log.d(TAG, "onServiceDiscovered() - peerHandle found")
                val discoveredPeerId = if (serviceSpecificInfo.isNotEmpty()) {
                    String(serviceSpecificInfo, Charsets.UTF_8)
                } else {
                    ""
                }
                Log.d(TAG, "Discovered peerId: '$discoveredPeerId'")
                this@WifiAwareService.peerHandle = peerHandle
                //connect()
                if (subscribeSession != null && peerHandle != null) {

                    connect(false)
                }
            }

            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                super.onSubscribeStarted(session)
                Log.d(TAG, "onSubscribeStarted")
                subscribeSession = session
                if (subscribeSession != null && peerHandle != null) {
                    connect(false)
                }
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                super.onMessageReceived(peerHandle, message)
                Log.d(TAG, "onMessageReceived")


                if(true) {
                    val port = "1111"
                    Log.d(TAG, "received message(${port})")
                    if (subscribeSession == null || this@WifiAwareService.peerHandle == null) return
                    networkSpecifier = WifiAwareNetworkSpecifier.Builder(subscribeSession!!, this@WifiAwareService.peerHandle!!)
                        .setPskPassphrase(PSK_PASSPHRASE)
                        .build()
                    requestNetwork()
                }
            }


        }, null)
    }

    private fun requestNetwork() {
        Log.d(TAG, "Starting requestNetwork...")

        if (networkSpecifier == null) {
            Log.d(TAG, "No NetworkSpecifier Created ")
            return
        }

        Log.d(TAG, "networkspecifier: ${networkSpecifier.toString()}")
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        connectivityManager.requestNetwork(networkRequest, object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "onAvailable: $network")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Log.d(TAG, "onLosing")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
            }

            override fun onUnavailable() {
                super.onUnavailable()
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo?
                val peerIpv6 = peerAwareInfo?.peerIpv6Addr
                val peerPort = peerAwareInfo?.port
                Log.d(TAG, "onCapabilitiesChanged ip(${peerIpv6?.hostAddress}), port(${peerPort})")
                peerIP = peerIpv6?.hostAddress
                peerIP?.let {
                    MainDataModel.setPeerIP(peerIP)
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)

                try {
                    val awareNi = NetworkInterface.getByName(linkProperties.interfaceName)

                    val addresses = awareNi.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if(addr !is Inet6Address) continue
                        Log.d(TAG, "netinterface ipv6 address: $addr")
                        if (addr.isLinkLocalAddress) {
                            myIP = addr.getHostAddress()
                            Log.d(TAG, "netinterface my address, myIp: $myIP")

                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "exception $e")
                }
            }
        })
    }

    private fun connect(isPubliser: Boolean) {
        Log.d(TAG, "connect")
        sendMessage(isPubliser)
    }

    private fun sendMessage(isPubliser: Boolean) {
        val data = "ThongDuong".toByteArray()
        val CMD_AWARE_CONNECTION_INFO = 111
        if (isPubliser) {
            publishSession?.sendMessage(peerHandle!!, CMD_AWARE_CONNECTION_INFO, data)
        } else {
            subscribeSession?.sendMessage(peerHandle!!, CMD_AWARE_CONNECTION_INFO, data)
        }

    }

    fun serverStart() {
        nettyTransport.startServer()
    }
}
