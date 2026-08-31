package com.example.vpn

import android.content.Context
import android.net.VpnService
import com.example.data.ServerRepository
import com.example.data.UserSessionManager
import com.example.data.VpnServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnController private constructor(private val context: Context) {

    private val userSessionManager = UserSessionManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _vpnState = MutableStateFlow(
        VpnConnectionState(
            status = VpnStatus.DISCONNECTED,
            activeServer = ServerRepository.defaultServers.first(),
            pingMs = ServerRepository.defaultServers.first().pingMs
        )
    )
    val vpnState: StateFlow<VpnConnectionState> = _vpnState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun selectServer(server: VpnServer) {
        val current = _vpnState.value
        _vpnState.value = current.copy(
            activeServer = server,
            pingMs = server.pingMs
        )

        // If currently connected, reconnect to the newly selected server
        if (current.isConnected) {
            reconnectToServer(server)
        }
    }

    fun toggleVpn(onPrepareVpnRequired: () -> Unit) {
        val current = _vpnState.value
        if (current.isConnected || current.isConnecting) {
            disconnect()
        } else {
            connect(onPrepareVpnRequired)
        }
    }

    fun connect(onPrepareVpnRequired: () -> Unit) {
        // Check if user has quota remaining
        val session = userSessionManager.sessionState.value
        if (session.remainingQuotaGb <= 0.0) {
            scope.launch {
                _events.emit("Daily data limit reached! Claim your daily 50GB reward to continue.")
            }
            return
        }

        // Check VPN preparation permission
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            onPrepareVpnRequired()
            return
        }

        val server = _vpnState.value.activeServer ?: ServerRepository.defaultServers.first()
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.CONNECTING
        )

        scope.launch {
            // Simulated handshake
            delay(1200)
            MaxVpnService.start(context, server.id)
        }
    }

    fun disconnect() {
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.DISCONNECTING
        )
        MaxVpnService.stop(context)
    }

    private fun reconnectToServer(server: VpnServer) {
        _vpnState.value = _vpnState.value.copy(status = VpnStatus.CONNECTING)
        MaxVpnService.stop(context)
        scope.launch {
            delay(800)
            MaxVpnService.start(context, server.id)
        }
    }

    fun onVpnConnected(server: VpnServer) {
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.CONNECTED,
            activeServer = server,
            pingMs = server.pingMs,
            ipAddressAssigned = server.ipAddress
        )
    }

    fun onVpnDisconnected() {
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.DISCONNECTED,
            connectedDurationSeconds = 0L,
            downloadSpeedKbps = 0.0,
            uploadSpeedKbps = 0.0,
            totalBytesDownloaded = 0L,
            totalBytesUploaded = 0L
        )
    }

    fun onVpnError(errorMessage: String) {
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.DISCONNECTED
        )
        scope.launch {
            _events.emit(errorMessage)
        }
    }

    fun onQuotaExceeded() {
        scope.launch {
            _events.emit("Your Daily GB quota is exhausted! Tap 'Claim Daily 50GB' to refill.")
        }
    }

    fun updateTrafficStats(
        durationSeconds: Long,
        downloadSpeedKbps: Double,
        uploadSpeedKbps: Double,
        totalDownloaded: Long,
        totalUploaded: Long,
        ping: Int
    ) {
        _vpnState.value = _vpnState.value.copy(
            connectedDurationSeconds = durationSeconds,
            downloadSpeedKbps = downloadSpeedKbps,
            uploadSpeedKbps = uploadSpeedKbps,
            totalBytesDownloaded = totalDownloaded,
            totalBytesUploaded = totalUploaded,
            pingMs = ping
        )
    }

    fun setKillSwitch(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(isKillSwitchEnabled = enabled)
    }

    fun setDnsProtection(enabled: Boolean) {
        _vpnState.value = _vpnState.value.copy(isDnsProtectionEnabled = enabled)
    }

    fun setProtocol(protocol: String) {
        _vpnState.value = _vpnState.value.copy(protocol = protocol)
    }

    companion object {
        @Volatile
        private var instance: VpnController? = null

        fun getInstance(context: Context): VpnController {
            return instance ?: synchronized(this) {
                instance ?: VpnController(context.applicationContext).also { instance = it }
            }
        }
    }
}
