package com.example

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthType
import com.example.data.UserSessionManager
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreatorMonetizationScreen
import com.example.ui.screens.DailyQuotaScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.vpn.VpnController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userSessionManager = UserSessionManager.getInstance(this)
        val vpnController = VpnController.getInstance(this)

        setContent {
            MyApplicationTheme {
                VpnAppRoot(
                    userSessionManager = userSessionManager,
                    vpnController = vpnController
                )
            }
        }
    }
}

@Composable
fun VpnAppRoot(
    userSessionManager: UserSessionManager,
    vpnController: VpnController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val session by userSessionManager.sessionState.collectAsState()
    val creatorInfo by userSessionManager.creatorInfo.collectAsState()
    val vpnState by vpnController.vpnState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Servers, 2: 50GB Quota, 3: OPay Hub, 4: Settings

    // VPN Permission Launcher
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnController.connect {}
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("VPN connection permission is required to secure traffic.")
            }
        }
    }

    // Listen for VPN events
    LaunchedEffect(Unit) {
        vpnController.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // If user is not authenticated yet, show Auth Screen
    if (session.authType == AuthType.NONE) {
        AuthScreen(
            onSignInEmail = { email, name ->
                userSessionManager.signInWithEmail(email, name)
                Toast.makeText(context, "Welcome! 100 GB daily quota activated for $email", Toast.LENGTH_LONG).show()
            },
            onSignInGuest = {
                userSessionManager.signInAsGuest()
                Toast.makeText(context, "Welcome Guest! 50 GB daily quota activated", Toast.LENGTH_LONG).show()
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = CyberSurfaceDark,
                    contentColor = CyberCyan,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_navigation_bar")
                ) {
                    // Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "VPN Hub",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("VPN Hub", fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    )

                    // Servers
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Servers",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Servers", fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    )

                    // Daily 50GB
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "50GB Daily",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("50GB Daily", fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberGreen,
                            selectedTextColor = CyberGreen,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = CyberGreen.copy(alpha = 0.15f)
                        )
                    )

                    // OPay Creator Hub
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "OPay Hub",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("OPay Hub", fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberAmber,
                            selectedTextColor = CyberAmber,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = CyberAmber.copy(alpha = 0.15f)
                        )
                    )

                    // Settings
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Settings", fontSize = 10.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        session = session,
                        vpnState = vpnState,
                        onToggleVpn = {
                            vpnController.toggleVpn {
                                val prepareIntent = VpnService.prepare(context)
                                if (prepareIntent != null) {
                                    vpnPrepareLauncher.launch(prepareIntent)
                                }
                            }
                        },
                        onOpenServers = { selectedTab = 1 },
                        onClaimDailyReward = {
                            userSessionManager.claimDaily50GbBonus()
                            scope.launch {
                                snackbarHostState.showSnackbar("Bonus +50 GB claimed! Added to your balance.")
                            }
                        },
                        onOpenMonetization = { selectedTab = 3 }
                    )
                    1 -> ServerListScreen(
                        currentServer = vpnState.activeServer,
                        onSelectServer = { server ->
                            vpnController.selectServer(server)
                            selectedTab = 0
                            scope.launch {
                                snackbarHostState.showSnackbar("Selected ${server.name} (${server.flagEmoji})")
                            }
                        }
                    )
                    2 -> DailyQuotaScreen(
                        session = session,
                        onClaim50Gb = {
                            userSessionManager.claimDaily50GbBonus()
                            scope.launch {
                                snackbarHostState.showSnackbar("+50 GB Daily Bandwidth refilled!")
                            }
                        },
                        onSwitchToEmail = {
                            userSessionManager.signOut()
                        },
                        onShareApp = {
                            userSessionManager.addBonusQuota(10.0)
                            scope.launch {
                                snackbarHostState.showSnackbar("Referral link shared! +10 GB added!")
                            }
                        }
                    )
                    3 -> CreatorMonetizationScreen(
                        creatorInfo = creatorInfo,
                        onRecordSupportPayment = { amount, ref ->
                            userSessionManager.recordOpayTransferSupport(amount, ref)
                            scope.launch {
                                snackbarHostState.showSnackbar("OPay payment confirmed for Sabiu Abdullahi Muhammad! VIP 500GB unlocked.")
                            }
                        }
                    )
                    4 -> SettingsScreen(
                        session = session,
                        vpnState = vpnState,
                        onSignOut = {
                            if (vpnState.isConnected) {
                                vpnController.disconnect()
                            }
                            userSessionManager.signOut()
                        },
                        onSetKillSwitch = { vpnController.setKillSwitch(it) },
                        onSetDnsProtection = { vpnController.setDnsProtection(it) },
                        onSetProtocol = { vpnController.setProtocol(it) }
                    )
                }
            }
        }
    }
}
