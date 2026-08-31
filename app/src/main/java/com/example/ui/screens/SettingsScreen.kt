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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthType
import com.example.data.UserSession
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.vpn.VpnConnectionState

@Composable
fun SettingsScreen(
    session: UserSession,
    vpnState: VpnConnectionState,
    onSignOut: () -> Unit,
    onSetKillSwitch: (Boolean) -> Unit,
    onSetDnsProtection: (Boolean) -> Unit,
    onSetProtocol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var autoConnectWifi by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SETTINGS & PROFILE",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Configure security protocols, account, and tunnel settings",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Account Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (session.authType == AuthType.EMAIL) CyberGreen.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (session.authType == AuthType.EMAIL) CyberGreen else CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.userName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (session.authType == AuthType.EMAIL) session.email else "Guest Session (50 GB Daily)",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = CyberRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch Account / Sign Out", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SECURITY & TUNNELING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Kill Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Kill Switch", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Block internet traffic if VPN drops unexpectedly", fontSize = 11.sp, color = TextMutedDark)
                    }
                    Switch(
                        checked = vpnState.isKillSwitchEnabled,
                        onCheckedChange = onSetKillSwitch,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberGreen,
                            checkedTrackColor = CyberGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DNS Leak Shield
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DNS Leak Protection", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Force encrypted 1.1.1.1 Cloudflare DNS resolver", fontSize = 11.sp, color = TextMutedDark)
                    }
                    Switch(
                        checked = vpnState.isDnsProtectionEnabled,
                        onCheckedChange = onSetDnsProtection,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Connect Wi-Fi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto-Protect on Wi-Fi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Automatically secure tunnel on public Wi-Fi networks", fontSize = 11.sp, color = TextMutedDark)
                    }
                    Switch(
                        checked = autoConnectWifi,
                        onCheckedChange = { autoConnectWifi = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protocol Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VPN PROTOCOL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                listOf("WireGuard", "OpenVPN Stealth (UDP)", "IKEv2 Fast").forEach { protocol ->
                    val isSelected = vpnState.protocol == protocol
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberSurfaceVariant else CyberSurfaceDark)
                            .border(1.dp, if (isSelected) CyberCyan else CyberCardBorder, RoundedCornerShape(10.dp))
                            .clickable { onSetProtocol(protocol) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = protocol,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberCyan else Color.White
                            )
                            if (isSelected) {
                                Text(text = "Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "50GB Daily VPN v1.0.0 • Developed for Sabiu Abdullahi Muhammad",
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }
    }
}
