package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreatorPlatform
import com.example.data.CreatorStats
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
import java.util.Locale

@Composable
fun StatsStarsHubScreen(
    stats: CreatorStats,
    wallet: CreatorWallet,
    onGenerateStars: (CreatorPlatform) -> Unit,
    onGenerateAllStarsSurge: () -> Unit,
    onUpdateFollowers: (Int) -> Unit,
    onNavigateToAdReward: () -> Unit,
    onNavigateToOpay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFollowerAdjuster by remember { mutableStateOf(false) }
    var followerSliderValue by remember { mutableStateOf(stats.totalFollowers.toFloat()) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    val totalStars = stats.facebookStars + stats.telegramStars + stats.googleStars
    val estimatedStarsValueNgn = totalStars * 1.85 // Conversion to Naira value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Creator Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DIGITAL CREATOR HUB",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Creator",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Immediate Stars & Stats Access",
                    fontSize = 11.sp,
                    color = CyberGreen
                )
            }

            // OPay Quick Link
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF00B074).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF00B074), RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToOpay)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("hub_opay_shortcut")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFF00B074),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "₦%,.0f", wallet.balanceNgn),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B074)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1,000 Followers Milestone Card (Immediate Stats Unlock)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (stats.isStatsUnlocked) CyberCyan.copy(alpha = 0.25f) else CyberAmber.copy(alpha = 0.25f),
                            CyberSurfaceDark
                        )
                    )
                )
                .border(
                    1.5.dp,
                    if (stats.isStatsUnlocked) CyberCyan else CyberAmber,
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
                .testTag("milestone_card")
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
                                .background(if (stats.isStatsUnlocked) CyberGreen.copy(alpha = 0.2f) else CyberAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (stats.isStatsUnlocked) Icons.Default.CheckCircle else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (stats.isStatsUnlocked) CyberGreen else CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (stats.isStatsUnlocked) "1,000+ FOLLOWERS PASSED" else "ROAD TO 1,000 FOLLOWERS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (stats.isStatsUnlocked) CyberGreen else CyberAmber,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (stats.isStatsUnlocked) "Instant stats & star monetization unlocked without waiting!" else "Reach 1,000 followers to get instant stats",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .clickable { showFollowerAdjuster = !showFollowerAdjuster }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${stats.totalFollowers} followers",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val progress = (stats.totalFollowers / 1000f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberGreen,
                    trackColor = CyberSurfaceVariant
                )

                AnimatedVisibility(visible = showFollowerAdjuster) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Adjust Digital Creator Follower Count:",
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                        Slider(
                            value = followerSliderValue,
                            onValueChange = {
                                followerSliderValue = it
                                onUpdateFollowers(it.toInt())
                            },
                            valueRange = 500f..5000f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = CyberSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("500 (Stage 1)", fontSize = 10.sp, color = TextMutedDark)
                            Text("1,000 (Instant Stats Active)", fontSize = 10.sp, color = CyberGreen, fontWeight = FontWeight.Bold)
                            Text("5,000 (Elite)", fontSize = 10.sp, color = TextMutedDark)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Stars & Live Monetization Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL CREATOR STARS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EST: ₦${String.format(Locale.US, "%,.0f", estimatedStarsValueNgn)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = String.format(Locale.US, "%,d STARS", totalStars),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Live cross-platform star yield generated across Google, Telegram, and Facebook",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Instant 3x Multi-Platform Star Surge Generator Button
                Button(
                    onClick = {
                        onGenerateAllStarsSurge()
                        actionToast = "⚡ Multi-Platform Star Surge Activated! +3,000 Stars Added!"
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberAmber,
                        contentColor = Color(0xFF241500)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("boost_all_stars_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GENERATE 3X STARS SURGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Platform-Specific Star Breakdown (Google, Telegram, Facebook)
        Text(
            text = "PLATFORM STAR GENERATORS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Facebook Stars
        PlatformStarItem(
            platformName = "Facebook Stars",
            badgeText = "FB Creator",
            starsCount = stats.facebookStars,
            followers = stats.facebookFollowers,
            iconTint = Color(0xFF1877F2),
            onGenerateClick = {
                onGenerateStars(CreatorPlatform.FACEBOOK)
                actionToast = "+500 Facebook Stars Generated!"
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Telegram Stars
        PlatformStarItem(
            platformName = "Telegram Stars",
            badgeText = "TG Channel",
            starsCount = stats.telegramStars,
            followers = stats.telegramFollowers,
            iconTint = Color(0xFF2AABEE),
            onGenerateClick = {
                onGenerateStars(CreatorPlatform.TELEGRAM)
                actionToast = "+500 Telegram Stars Generated!"
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Google Stars & Ad Impressions
        PlatformStarItem(
            platformName = "Google Creator Stars",
            badgeText = "Google AdSense",
            starsCount = stats.googleStars,
            followers = stats.googleFollowers,
            iconTint = Color(0xFFEA4335),
            onGenerateClick = {
                onGenerateStars(CreatorPlatform.GOOGLE)
                actionToast = "+500 Google Creator Stars Generated!"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Performance Stats (Impressions, Engagement, RPM)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "INSTANT CREATOR STATS & METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = String.format(Locale.US, "%,d", stats.totalImpressions),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(text = "Total Impressions", fontSize = 11.sp, color = TextMutedDark)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stats.engagementRate}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(text = "Engagement Rate", fontSize = 11.sp, color = TextMutedDark)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₦${stats.rpmNgn.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                        Text(text = "Estimated RPM", fontSize = 11.sp, color = TextMutedDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Earn 500 Naira per Ad Watch Quick Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToAdReward),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Watch Ads & Earn ₦500.00",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Instant ₦500 direct to your OPay wallet per ad",
                            fontSize = 11.sp,
                            color = CyberGreen
                        )
                    }
                }

                Button(
                    onClick = onNavigateToAdReward,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("hub_watch_ad_btn")
                ) {
                    Text(text = "EARN ₦500", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (actionToast != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = actionToast!!,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlatformStarItem(
    platformName: String,
    badgeText: String,
    starsCount: Long,
    followers: Int,
    iconTint: Color,
    onGenerateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = platformName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(iconTint.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = badgeText, fontSize = 9.sp, color = iconTint, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "${String.format(Locale.US, "%,d", starsCount)} Stars • ${String.format(Locale.US, "%,d", followers)} Followers",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Button(
                onClick = onGenerateClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconTint.copy(alpha = 0.2f),
                    contentColor = iconTint
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.6f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("gen_${platformName.take(4)}_btn")
            ) {
                Text(text = "+500 Stars", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
