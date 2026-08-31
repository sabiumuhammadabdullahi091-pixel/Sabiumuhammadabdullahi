package com.example.vpn

import com.example.data.VpnServer

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class VpnConnectionState(
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val activeServer: VpnServer? = null,
    val connectedDurationSeconds: Long = 0L,
    val downloadSpeedKbps: Double = 0.0,
    val uploadSpeedKbps: Double = 0.0,
    val totalBytesDownloaded: Long = 0L,
    val totalBytesUploaded: Long = 0L,
    val pingMs: Int = 0,
    val protocol: String = "WireGuard",
    val isKillSwitchEnabled: Boolean = true,
    val isDnsProtectionEnabled: Boolean = true,
    val ipAddressAssigned: String = "10.8.0.2"
) {
    val isConnected: Boolean
        get() = status == VpnStatus.CONNECTED

    val isConnecting: Boolean
        get() = status == VpnStatus.CONNECTING
}
