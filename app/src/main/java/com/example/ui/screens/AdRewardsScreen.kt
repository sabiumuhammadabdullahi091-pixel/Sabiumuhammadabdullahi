package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ads.GoogleAdsManager
import com.example.data.CreatorWallet
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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun AdRewardsScreen(
    wallet: CreatorWallet,
    adsManager: GoogleAdsManager,
    onWatchAdReward: () -> Unit,
    onNavigateToOpay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isRewardedReady by adsManager.isRewardedAdLoaded.collectAsState()
    val isInterstitialReady by adsManager.isInterstitialAdLoaded.collectAsState()
    val isLoadingAd by adsManager.isLoadingAd.collectAsState()
    val adStatusMessage by adsManager.adStatusMessage.collectAsState()

    var isPlayingSimulationAd by remember { mutableStateOf(false) }
    var adCountdown by remember { mutableIntStateOf(10) }
    var simulationAdCompleted by remember { mutableStateOf(false) }
    var successToast by remember { mutableStateOf<String?>(null) }
    var currentAdIndex by remember { mutableIntStateOf(0) }

    val adCampaigns = listOf(
        AdCampaign("OPay Super Saver 15% APY", "Save & Earn Daily Interest with Instant Transfers", Color(0xFF00B074)),
        AdCampaign("Google Cloud for Creators", "Accelerate your media workflow and global reach", Color(0xFF4285F4)),
        AdCampaign("Telegram Premium Stars Hub", "Monetize your subscriber community worldwide", Color(0xFF2AABEE)),
        AdCampaign("Meta Digital Creator Bonus", "Earn stars on reels and monetize audience engagement", Color(0xFF0668E1))
    )

    val currentCampaign = adCampaigns[currentAdIndex % adCampaigns.size]

    // Simulation Countdown timer if playing internal video preview
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Title
        Text(
            text = "GOOGLE MOBILE ADS REWARDS",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = "Watch Google Rewarded & Interstitial Ads to earn ₦500.00 each",
            fontSize = 12.sp,
            color = CyberGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Balance & Direct OPay Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF00B074).copy(alpha = 0.25f),
                            CyberSurfaceDark
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF00B074), RoundedCornerShape(20.dp))
                .padding(18.dp)
                .testTag("ad_reward_wallet_card")
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
                                .background(Color(0xFF00B074).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF00B074),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OPAY WALLET BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B074),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${wallet.opayAccountName} (9169878194)",
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
                            text = "${wallet.adsWatchedCount} Ads Watched",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = String.format(Locale.US, "₦%,.2f", wallet.balanceNgn),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Reward: ₦500 per ad watch",
                            fontSize = 11.sp,
                            color = CyberGreen
                        )
                    }

                    Button(
                        onClick = onNavigateToOpay,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B074),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("withdraw_opay_now_btn")
                    ) {
                        Text(text = "Cashout to OPay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Mobile Ads SDK Live Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.6f)),
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
                        text = adStatusMessage,
                        fontSize = 11.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                if (isLoadingAd) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = CyberCyan
                    )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Sponsor Video Ad Player Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ad Header
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
                            text = "GOOGLE REWARDED VIDEO",
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
                            text = "+₦500.00 REWARD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Ad View Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                                    .size(54.dp)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCampaign.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Playing sponsored video... Please do not close",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (10 - adCountdown) / 10f },
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyberGreen,
                                trackColor = CyberSurfaceVariant
                            )
                        }
                    } else if (simulationAdCompleted) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyberGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ad Completed Successfully!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                            Text(
                                text = "Tap the button below to claim your ₦500 cash reward",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .clickable {
                                        if (activity != null && isRewardedReady) {
                                            adsManager.showRewardedAd(
                                                activity = activity,
                                                onRewardEarned = { _, _ ->
                                                    onWatchAdReward()
                                                    currentAdIndex++
                                                    successToast = "🎉 ₦500.00 credited to Sabiu Abdullahi Muhammad's OPay wallet!"
                                                },
                                                onAdClosedOrFailed = {
                                                    isPlayingSimulationAd = true
                                                }
                                            )
                                        } else {
                                            isPlayingSimulationAd = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Ad",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCampaign.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentCampaign.subtitle,
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Button (Rewarded Video)
                if (simulationAdCompleted) {
                    Button(
                        onClick = {
                            onWatchAdReward()
                            simulationAdCompleted = false
                            currentAdIndex++
                            successToast = "🎉 ₦500.00 credited to Sabiu Abdullahi Muhammad's OPay wallet!"
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberGreen,
                            contentColor = Color(0xFF003919)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_500_naira_reward_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CLAIM ₦500.00 REWARD NOW",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (activity != null && isRewardedReady) {
                                adsManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = { _, _ ->
                                        onWatchAdReward()
                                        currentAdIndex++
                                        successToast = "🎉 ₦500.00 credited to Sabiu Abdullahi Muhammad's OPay wallet!"
                                    },
                                    onAdClosedOrFailed = {
                                        isPlayingSimulationAd = true
                                    }
                                )
                            } else {
                                isPlayingSimulationAd = true
                            }
                        },
                        enabled = !isPlayingSimulationAd,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF00272B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_watch_ad_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingSimulationAd) "WATCHING AD ($adCountdown s)..." else "WATCH GOOGLE REWARDED AD (₦500)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interstitial Ad Trigger Button
                OutlinedButton(
                    onClick = {
                        if (activity != null && isInterstitialReady) {
                            adsManager.showInterstitialAd(activity) {
                                onWatchAdReward()
                                successToast = "🎉 Interstitial Ad completed! ₦500.00 rewarded."
                            }
                        } else {
                            onWatchAdReward()
                            successToast = "🎉 Interstitial Ad view recorded! ₦500.00 rewarded."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyberAmber
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("play_interstitial_ad_button")
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY INTERSTITIAL SPONSOR AD (+₦500)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (successToast != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = successToast!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Ad Earning Limits & Multipliers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REWARD POTENTIAL & DAILY STREAK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "1 Ad Watch", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(text = "₦500.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "10 Ads Daily", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(text = "₦5,000.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "20 Ads Daily", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(text = "₦10,000.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    }
                }
            }
        }
    }
}

data class AdCampaign(
    val title: String,
    val subtitle: String,
    val themeColor: Color
)

