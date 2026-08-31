package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthType
import com.example.data.UserSession
import com.example.data.VpnServer
import com.example.ui.components.DataUsageCard
import com.example.ui.components.VpnConnectButton
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.vpn.VpnConnectionState
import com.example.vpn.VpnStatus
import java.util.Locale

@Composable
fun HomeScreen(
    session: UserSession,
    vpnState: VpnConnectionState,
    onToggleVpn: () -> Unit,
    onOpenServers: () -> Unit,
    onClaimDailyReward: () -> Unit,
    onOpenMonetization: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: User badge & Quick claim status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hello, ${session.userName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (session.authType == AuthType.EMAIL) "100GB Daily Account" else "50GB Daily Guest",
                        fontSize = 11.sp,
                        color = if (session.authType == AuthType.EMAIL) CyberGreen else CyberCyan
                    )
                }
            }

            // OPay Creator shortcut
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberAmber.copy(alpha = 0.15f))
                    .border(1.dp, CyberAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenMonetization)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("creator_opay_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OPay Support",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Data Balance Card
        DataUsageCard(
            session = session,
            onClaimDailyReward = onClaimDailyReward
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Cyber VPN Power Button
        VpnConnectButton(
            status = vpnState.status,
            onClick = onToggleVpn
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Status & Live Duration
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val statusColor = when (vpnState.status) {
                VpnStatus.CONNECTED -> CyberGreen
                VpnStatus.CONNECTING -> CyberAmber
                VpnStatus.DISCONNECTING -> CyberRed
                VpnStatus.DISCONNECTED -> TextSecondaryDark
            }

            val statusText = when (vpnState.status) {
                VpnStatus.CONNECTED -> "CONNECTED • 256-BIT ENCRYPTED"
                VpnStatus.CONNECTING -> "AUTHENTICATING & SECURING TUNNEL..."
                VpnStatus.DISCONNECTING -> "DISCONNECTING TUNNEL..."
                VpnStatus.DISCONNECTED -> "NOT PROTECTED • READY TO CONNECT"
            }

            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                letterSpacing = 1.sp
            )

            if (vpnState.isConnected) {
                Spacer(modifier = Modifier.height(4.dp))
                val hours = vpnState.connectedDurationSeconds / 3600
                val minutes = (vpnState.connectedDurationSeconds % 3600) / 60
                val seconds = vpnState.connectedDurationSeconds % 60
                val durationFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Session Duration: $durationFormatted",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Server Selector Card
        val server = vpnState.activeServer
        if (server != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenServers)
                    .padding(14.dp)
                    .testTag("home_server_selector_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = server.flagEmoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = server.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Protocol: ${vpnState.protocol} • Ping: ${vpnState.pingMs} ms",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Change server",
                            tint = CyberCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Speed Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Download Speed
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Download",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DOWNLOAD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (vpnState.isConnected) {
                                String.format(Locale.US, "%.1f MB/s", vpnState.downloadSpeedKbps / 1024)
                            } else {
                                "0.0 MB/s"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vpnState.isConnected) CyberCyan else TextSecondaryDark
                        )
                    }
                }
            }

            // Upload Speed
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upload",
                            tint = CyberPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "UPLOAD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (vpnState.isConnected) {
                                String.format(Locale.US, "%.1f MB/s", vpnState.uploadSpeedKbps / 1024)
                            } else {
                                "0.0 MB/s"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vpnState.isConnected) CyberPurple else TextSecondaryDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Features Quick Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kill Switch Active",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DNS Leak Shield",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = CyberAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "IP: ${if (vpnState.isConnected) vpnState.ipAddressAssigned else "Hidden"}",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }
    }
}
