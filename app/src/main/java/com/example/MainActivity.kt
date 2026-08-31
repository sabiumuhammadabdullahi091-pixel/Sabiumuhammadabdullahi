package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.GoogleAdsManager
import com.example.ads.RewardTracker
import com.example.data.CreatorManager
import com.example.ui.screens.CreatorProfileScreen
import com.example.ui.screens.DataSaverBrowserScreen
import com.example.ui.screens.OpayPayoutScreen
import com.example.ui.screens.RewardTrackerScreen
import com.example.ui.screens.StatsStarsHubScreen
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val creatorManager = CreatorManager.getInstance(this)
        val adsManager = GoogleAdsManager.getInstance(this)
        val rewardTracker = RewardTracker.getInstance(this)

        setContent {
            MyApplicationTheme {
                DigitalCreatorAppRoot(
                    creatorManager = creatorManager,
                    adsManager = adsManager,
                    rewardTracker = rewardTracker
                )
            }
        }
    }
}

@Composable
fun DigitalCreatorAppRoot(
    creatorManager: CreatorManager,
    adsManager: GoogleAdsManager,
    rewardTracker: RewardTracker
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val stats by creatorManager.stats.collectAsState()
    val wallet by creatorManager.wallet.collectAsState()
    val dataSaver by creatorManager.dataSaver.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Stars & Stats, 1: ₦500 Reward Tracker, 2: Data Saver, 3: OPay Hub, 4: Profile

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurfaceDark,
                contentColor = CyberCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("creator_navigation_bar")
            ) {
                // 1. Stars & Stats
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars & Stats",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Stars & Stats",
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark,
                        indicatorColor = CyberAmber.copy(alpha = 0.15f)
                    )
                )

                // 2. AdMob Reward Tracker (₦500)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reward Tracker",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Reward Tracker",
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberGreen,
                        selectedTextColor = CyberGreen,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark,
                        indicatorColor = CyberGreen.copy(alpha = 0.15f)
                    )
                )

                // 3. Data Saver Browser
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DataSaverOn,
                            contentDescription = "Data Saver",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Data Saver",
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f)
                    )
                )

                // 4. OPay Payout Hub
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "OPay Payout",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "OPay Hub",
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00B074),
                        selectedTextColor = Color(0xFF00B074),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark,
                        indicatorColor = Color(0xFF00B074).copy(alpha = 0.15f)
                    )
                )

                // 5. Creator Profile
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPurple,
                        selectedTextColor = CyberPurple,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark,
                        indicatorColor = CyberPurple.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberDarkBg)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> StatsStarsHubScreen(
                    stats = stats,
                    wallet = wallet,
                    onGenerateStars = { platform ->
                        creatorManager.generatePlatformStars(platform, 500)
                        scope.launch {
                            snackbarHostState.showSnackbar("+500 ${platform.displayName} added to your creator dashboard!")
                        }
                    },
                    onGenerateAllStarsSurge = {
                        creatorManager.generateAllStarsBoost()
                        scope.launch {
                            snackbarHostState.showSnackbar("⚡ Multi-Platform Star Surge activated! +3,000 Stars Added!")
                        }
                    },
                    onUpdateFollowers = { newCount ->
                        creatorManager.updateFollowers(newCount)
                    },
                    onNavigateToAdReward = { selectedTab = 1 },
                    onNavigateToOpay = { selectedTab = 3 }
                )
                1 -> RewardTrackerScreen(
                    rewardTracker = rewardTracker,
                    adsManager = adsManager,
                    onNavigateToOpayHub = { selectedTab = 3 }
                )
                2 -> DataSaverBrowserScreen(
                    dataSaverMetrics = dataSaver,
                    onRecordBrowsing = { saved, generated ->
                        creatorManager.recordBrowsingActivity(saved, generated)
                    },
                    onToggleUltraSaver = { enabled ->
                        creatorManager.toggleUltraDataSaver(enabled)
                        scope.launch {
                            snackbarHostState.showSnackbar(if (enabled) "Ultra Data Saver ACTIVE (82% data saved)" else "Standard Data Mode")
                        }
                    }
                )
                3 -> OpayPayoutScreen(
                    wallet = wallet,
                    onWithdrawAmount = { amount ->
                        val tx = creatorManager.withdrawToOpay(amount)
                        if (tx != null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("₦${String.format(Locale.US, "%,.0f", amount)} successfully sent to Sabiu Abdullahi Muhammad (OPay: 9169878194)!")
                            }
                        }
                        tx
                    }
                )
                4 -> CreatorProfileScreen(
                    stats = stats,
                    wallet = wallet,
                    dataSaver = dataSaver,
                    creatorManager = creatorManager,
                    onToggleUltraSaver = { enabled ->
                        creatorManager.toggleUltraDataSaver(enabled)
                    }
                )
            }
        }
    }
}
