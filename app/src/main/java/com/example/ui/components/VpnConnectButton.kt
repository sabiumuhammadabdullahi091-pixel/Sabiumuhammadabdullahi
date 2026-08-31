package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberGreenGlow
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.vpn.VpnStatus

@Composable
fun VpnConnectButton(
    status: VpnStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val activeColor = when (status) {
        VpnStatus.CONNECTED -> CyberGreen
        VpnStatus.CONNECTING -> CyberAmber
        VpnStatus.DISCONNECTING -> CyberRed
        VpnStatus.DISCONNECTED -> CyberCyan
    }

    val glowColor = when (status) {
        VpnStatus.CONNECTED -> CyberGreenGlow
        VpnStatus.CONNECTING -> CyberAmber
        VpnStatus.DISCONNECTING -> CyberRed
        VpnStatus.DISCONNECTED -> CyberBlue
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .testTag("vpn_connect_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Pulsing Wave
        if (status == VpnStatus.CONNECTED || status == VpnStatus.CONNECTING) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
            ) {
                drawCircle(
                    color = activeColor.copy(alpha = pulseAlpha),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = activeColor.copy(alpha = pulseAlpha * 0.5f),
                    radius = size.minDimension / 2f
                )
            }
        }

        // Rotating Cyber Ring
        Canvas(
            modifier = Modifier
                .size(190.dp)
                .rotate(if (status == VpnStatus.CONNECTING) rotationAngle * 3 else rotationAngle)
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        activeColor,
                        glowColor.copy(alpha = 0.2f),
                        Color.Transparent,
                        activeColor.copy(alpha = 0.8f)
                    )
                ),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Inner Power Button Container
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = if (status == VpnStatus.CONNECTED) 20.dp else 10.dp,
                    shape = CircleShape,
                    ambientColor = activeColor,
                    spotColor = activeColor
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyberSurfaceDark,
                            CyberDarkBg
                        )
                    )
                )
                .border(2.dp, activeColor.copy(alpha = 0.8f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = activeColor),
                    onClick = onClick
                )
                .testTag("vpn_power_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (status) {
                    VpnStatus.CONNECTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = CyberAmber,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CONNECTING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber,
                            letterSpacing = 1.5.sp
                        )
                    }
                    VpnStatus.DISCONNECTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = CyberRed,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STOPPING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRed,
                            letterSpacing = 1.5.sp
                        )
                    }
                    VpnStatus.CONNECTED -> {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Connected",
                            tint = CyberGreen,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DISCONNECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGreen,
                            letterSpacing = 1.2.sp
                        )
                    }
                    VpnStatus.DISCONNECTED -> {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Connect VPN",
                            tint = CyberCyan,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TAP TO CONNECT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
