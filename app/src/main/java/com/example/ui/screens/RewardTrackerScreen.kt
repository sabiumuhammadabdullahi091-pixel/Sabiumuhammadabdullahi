package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdRewardRecord
import com.example.ads.GoogleAdsManager
import com.example.ads.RewardTracker
import com.example.data.WithdrawalTransaction
import com.example.ui.theme.CyberAmber
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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RewardTrackerScreen(
    rewardTracker: RewardTracker,
    adsManager: GoogleAdsManager,
    onNavigateToOpayHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Reactive State from RewardTracker
    val adsWatchedCount by rewardTracker.adsWatchedCount.collectAsState()
    val totalEarningsNgn by rewardTracker.totalEarningsNgn.collectAsState()
    val withdrawableBalanceNgn by rewardTracker.withdrawableBalanceNgn.collectAsState()
    val rewardsHistory by rewardTracker.rewardsHistory.collectAsState()
    val transferHistory by rewardTracker.transferHistory.collectAsState()
    val lastEventMessage by rewardTracker.lastEventMessage.collectAsState()

    // Ads SDK State
    val isRewardedReady by adsManager.isRewardedAdLoaded.collectAsState()
    val isInterstitialReady by adsManager.isInterstitialAdLoaded.collectAsState()
    val isLoadingAd by adsManager.isLoadingAd.collectAsState()
    val adStatusMessage by adsManager.adStatusMessage.collectAsState()

    // Local UI State
    var selectedHistoryTab by remember { mutableIntStateOf(0) } // 0: Rewards History, 1: Transfer History
    var isPlayingSimulationAd by remember { mutableStateOf(false) }
    var adCountdown by remember { mutableIntStateOf(10) }
    var simulationAdCompleted by remember { mutableStateOf(false) }
    var actionToast by remember { mutableStateOf<String?>(null) }
    var latestReceipt by remember { mutableStateOf<WithdrawalTransaction?>(null) }

    // Quick Cashout state
    var selectedCashoutAmount by remember { mutableDoubleStateOf(1000.0) }

    val adCampaigns = listOf(
        AdCampaignItem("OPay Super Saver 15% APY", "Save & Earn Daily Interest with Instant Transfers", Color(0xFF00B074)),
        AdCampaignItem("Google Cloud for Creators", "Accelerate your media workflow and global reach", Color(0xFF4285F4)),
        AdCampaignItem("Telegram Premium Stars Hub", "Monetize your subscriber community worldwide", Color(0xFF2AABEE)),
        AdCampaignItem("Meta Digital Creator Bonus", "Earn stars on reels and monetize audience engagement", Color(0xFF0668E1))
    )
    var currentCampaignIndex by remember { mutableIntStateOf(0) }
    val currentCampaign = adCampaigns[currentCampaignIndex % adCampaigns.size]

    // Simulation Countdown timer
    LaunchedEffect(isPlayingSimulationAd) {
        if (isPlayingSimulationAd) {
            adCountdown = 10
            simulationAdCompleted = false
            while (adCountdown > 0) {
                delay(1000L)
                adCountdown -= 1
            }
            simulationAdCompleted = true
            isPlayingSimulationAd = false
        }
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label: $text", Toast.LENGTH_SHORT).show()
    }

    fun shareTransferReceipt(tx: WithdrawalTransaction) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "💰 OPAY CREATOR EARNINGS TRANSFER RECEIPT\n" +
                        "Amount: ₦${String.format(Locale.US, "%,.2f", tx.amountNgn)}\n" +
                        "Beneficiary: Sabiu Abdullahi Muhammad\n" +
                        "OPay Account: 9169878194\n" +
                        "Status: ${tx.status}\n" +
                        "Reference: ${tx.reference}\n" +
                        "Date: ${tx.formattedDate}\n" +
                        "Calculated from AdMob Rewarded Video views (₦500/ad)"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share OPay Payout Receipt"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header
        Text(
            text = "ADMOB REWARD TRACKER",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = "Formula: Total Earnings = (adsWatched × ₦500) Naira",
            fontSize = 12.sp,
            color = CyberGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Hero Total Earnings Display Card: (adsWatched * 500) Naira
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CyberGreen.copy(alpha = 0.25f),
                            CyberSurfaceDark
                        )
                    )
                )
                .border(1.5.dp, CyberGreen, RoundedCornerShape(22.dp))
                .padding(18.dp)
                .testTag("reward_tracker_total_earnings_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TOTAL CALCULATED EARNINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "adsWatched (${adsWatchedCount}) × ₦500.00",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$adsWatchedCount Ads Watched",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Total Earnings Number (adsWatched * 500) Naira
                Text(
                    text = String.format(Locale.US, "₦%,.2f", totalEarningsNgn),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Formula Breakdown Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceVariant.copy(alpha = 0.8f))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formula: ($adsWatchedCount × 500) NGN = ₦${String.format(Locale.US, "%,.0f", totalEarningsNgn)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberAmber
                        )
                        Text(
                            text = "₦500 / View",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Balance and Beneficiary Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Withdrawable to OPay: ₦${String.format(Locale.US, "%,.2f", withdrawableBalanceNgn)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                        Text(
                            text = "Beneficiary: Sabiu Abdullahi Muhammad (9169878194)",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Button(
                        onClick = onNavigateToOpayHub,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B074),
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("transfer_earnings_to_opay_btn")
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Cashout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AdMob Live SDK Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRewardedReady || isInterstitialReady) CyberGreen else CyberAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoadingAd) "AdMob Loading..." else adStatusMessage,
                        fontSize = 11.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                if (isLoadingAd) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyberCyan)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Ad",
                        tint = CyberCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                adsManager.loadRewardedAd()
                                adsManager.loadInterstitialAd()
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Interactive Ad Player & Reward Trigger Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = currentCampaign.themeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ADMOB REWARDED VIDEO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentCampaign.themeColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+₦500.00 / VIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ad Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    currentCampaign.themeColor.copy(alpha = 0.35f),
                                    CyberSurfaceVariant
                                )
                            )
                        )
                        .border(1.dp, currentCampaign.themeColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlayingSimulationAd) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(currentCampaign.themeColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$adCountdown s",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentCampaign.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Playing AdMob sponsor video... please wait",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (10 - adCountdown) / 10f },
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyberGreen,
                                trackColor = CyberSurfaceVariant
                            )
                        }
                    } else if (simulationAdCompleted) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ad Completed Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                            Text(
                                text = "Tap claim below to credit ₦500.00 to Sabiu Abdullahi Muhammad",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                if (activity != null && isRewardedReady) {
                                    adsManager.showRewardedAdWithTracker(
                                        activity = activity,
                                        rewardTracker = rewardTracker,
                                        onAdClosedOrFailed = { isPlayingSimulationAd = true }
                                    )
                                } else {
                                    isPlayingSimulationAd = true
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Ad",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentCampaign.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentCampaign.subtitle,
                                fontSize = 10.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watch / Claim Button
                if (simulationAdCompleted) {
                    Button(
                        onClick = {
                            rewardTracker.onUserEarnedReward(adType = "AdMob Rewarded Video")
                            simulationAdCompleted = false
                            currentCampaignIndex++
                            actionToast = "🎉 ₦500.00 added! Total Earnings now: ₦${String.format(Locale.US, "%,.0f", rewardTracker.totalEarningsNgn.value)}"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF003919)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("claim_admob_reward_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CLAIM ₦500.00 REWARD NOW",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (activity != null && isRewardedReady) {
                                adsManager.showRewardedAdWithTracker(
                                    activity = activity,
                                    rewardTracker = rewardTracker,
                                    onAdClosedOrFailed = { isPlayingSimulationAd = true }
                                )
                            } else {
                                isPlayingSimulationAd = true
                            }
                        },
                        enabled = !isPlayingSimulationAd,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00272B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("watch_admob_rewarded_video_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingSimulationAd) "WATCHING AD ($adCountdown s)..." else "WATCH ADMOB REWARDED VIDEO (₦500)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interstitial Ad Trigger
                OutlinedButton(
                    onClick = {
                        if (activity != null && isInterstitialReady) {
                            adsManager.showInterstitialAd(activity) {
                                rewardTracker.onUserEarnedReward(adType = "Google Interstitial Sponsor")
                                actionToast = "🎉 Interstitial Ad credited (+₦500.00)!"
                            }
                        } else {
                            rewardTracker.onUserEarnedReward(adType = "Google Interstitial Sponsor")
                            actionToast = "🎉 Sponsor Ad credited (+₦500.00)!"
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAmber),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("watch_interstitial_ad_btn")
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PLAY INTERSTITIAL SPONSOR AD (+₦500)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (actionToast != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actionToast!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Dual History Section: Tab Switcher (Rewards History vs Transfer History)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Tab Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceVariant)
                        .padding(4.dp)
                ) {
                    // Tab 0: Rewards History
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedHistoryTab == 0) CyberGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedHistoryTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (selectedHistoryTab == 0) CyberGreen else TextMutedDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rewards History (${rewardsHistory.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedHistoryTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedHistoryTab == 0) CyberGreen else TextSecondaryDark
                            )
                        }
                    }

                    // Tab 1: Transfer History
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedHistoryTab == 1) Color(0xFF00B074).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedHistoryTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (selectedHistoryTab == 1) Color(0xFF00B074) else TextMutedDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Transfer History (${transferHistory.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedHistoryTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedHistoryTab == 1) Color(0xFF00B074) else TextSecondaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedHistoryTab == 0) {
                    // --- REWARDS HISTORY LIST ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADMOB REWARD LEDGER (₦500 / AD)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Total Earned: ₦${String.format(Locale.US, "%,.0f", totalEarningsNgn)}",
                            fontSize = 10.sp,
                            color = CyberAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (rewardsHistory.isEmpty()) {
                        Text(
                            text = "No rewards logged yet. Watch your first AdMob video above!",
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        rewardsHistory.take(15).forEach { record ->
                            RewardHistoryItemCard(record = record)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                } else {
                    // --- TRANSFER HISTORY LIST ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPAY TRANSFERS (9169878194)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00B074),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${transferHistory.size} Transfers",
                            fontSize = 10.sp,
                            color = TextMutedDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (transferHistory.isEmpty()) {
                        Text(
                            text = "No transfers recorded yet. Earn rewards and cash out to OPay!",
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        transferHistory.take(15).forEach { tx ->
                            TransferHistoryItemCard(
                                tx = tx,
                                onCopyRef = { copyToClipboard(tx.reference, "Reference") },
                                onShare = { shareTransferReceipt(tx) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick OPay Transfer Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QUICK CASH OUT TO OPAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00B074),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Instant transfer to Sabiu Abdullahi Muhammad (9169878194)",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500.0, 1000.0, 2500.0, 5000.0).forEach { amount ->
                        val isSelected = selectedCashoutAmount == amount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00B074).copy(alpha = 0.25f) else CyberSurfaceVariant)
                                .border(1.dp, if (isSelected) Color(0xFF00B074) else CyberCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedCashoutAmount = amount }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₦${String.format(Locale.US, "%,.0f", amount)}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyberGreen else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val canCashout = withdrawableBalanceNgn >= selectedCashoutAmount && selectedCashoutAmount > 0

                Button(
                    onClick = {
                        val tx = rewardTracker.processTransferToOpay(selectedCashoutAmount)
                        if (tx != null) {
                            latestReceipt = tx
                            actionToast = "✅ ₦${String.format(Locale.US, "%,.0f", selectedCashoutAmount)} transferred to Sabiu Abdullahi Muhammad (9169878194)!"
                        } else {
                            actionToast = "⚠️ Insufficient balance. Watch more AdMob videos to earn ₦500 each!"
                        }
                    },
                    enabled = canCashout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B074),
                        contentColor = Color.White,
                        disabledContainerColor = CyberSurfaceVariant,
                        disabledContentColor = TextMutedDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("quick_opay_transfer_btn")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRANSFER ₦${String.format(Locale.US, "%,.0f", selectedCashoutAmount)} TO OPAY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RewardHistoryItemCard(record: AdRewardRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, CyberCardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyberGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = record.adType,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${record.reference} • ${record.formattedDate}",
                        fontSize = 10.sp,
                        color = TextMutedDark
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "+₦%,.2f", record.rewardAmountNgn),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberGreen
                )
                Text(
                    text = record.status,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberCyan
                )
            }
        }
    }
}

@Composable
fun TransferHistoryItemCard(
    tx: WithdrawalTransaction,
    onCopyRef: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, Color(0xFF00B074).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00B074).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF00B074),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OPay Transfer • ${tx.accountName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${tx.reference} • ${tx.formattedDate}",
                            fontSize = 10.sp,
                            color = TextMutedDark
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "-₦%,.2f", tx.amountNgn),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00B074)
                    )
                    Text(
                        text = tx.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Share Receipt",
                    fontSize = 10.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onShare() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy Ref",
                    fontSize = 10.sp,
                    color = Color(0xFF00B074),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onCopyRef() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

data class AdCampaignItem(
    val title: String,
    val subtitle: String,
    val themeColor: Color
)
