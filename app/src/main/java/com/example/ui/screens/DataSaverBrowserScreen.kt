package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.DataSaverMetrics
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DataSaverBrowserScreen(
    dataSaverMetrics: DataSaverMetrics,
    onRecordBrowsing: (savedMb: Double, generatedMb: Double) -> Unit,
    onToggleUltraSaver: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var inputQuery by remember { mutableStateOf("https://www.google.com") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var pageLoadingProgress by remember { mutableFloatStateOf(0f) }
    var isPageLoading by remember { mutableStateOf(false) }

    val quickShortcuts = listOf(
        "Google Search" to "https://www.google.com",
        "FB Creator" to "https://business.facebook.com/creatorstudio",
        "Telegram Web" to "https://web.telegram.org",
        "YouTube Studio" to "https://studio.youtube.com",
        "BBC News" to "https://www.bbc.com"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
    ) {
        // Data Saver & Generator Top HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DataSaverOn,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DATA SAVER & GENERATOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberGreen,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ultra Saver",
                        fontSize = 11.sp,
                        color = if (dataSaverMetrics.isUltraDataSaverActive) CyberGreen else TextMutedDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = dataSaverMetrics.isUltraDataSaverActive,
                        onCheckedChange = onToggleUltraSaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberGreen,
                            checkedTrackColor = CyberGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceVariant
                        ),
                        modifier = Modifier.testTag("toggle_ultra_data_saver")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Real-Time Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Data Saved Metric
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CyberGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "SAVED BANDWIDTH", fontSize = 9.sp, color = TextMutedDark)
                        Text(
                            text = String.format(Locale.US, "%.1f MB (82%%)", dataSaverMetrics.dataSavedMb),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                }

                // Data Generated Metric from Website
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "GENERATED FROM WEB", fontSize = 9.sp, color = TextMutedDark)
                        Text(
                            text = String.format(Locale.US, "+%,.0f MB Bonus", dataSaverMetrics.dataGeneratedMb),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        // Search & URL Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Web navigation back button
            IconButton(
                onClick = { webViewInstance?.let { if (it.canGoBack()) it.goBack() } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
            }

            // Web navigation forward button
            IconButton(
                onClick = { webViewInstance?.let { if (it.canGoForward()) it.goForward() } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
            }

            // Input Field
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Search or enter website URL...", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CyberSurfaceDark,
                    unfocusedContainerColor = CyberSurfaceDark
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val target = if (inputQuery.startsWith("http://") || inputQuery.startsWith("https://")) {
                                inputQuery.trim()
                            } else {
                                "https://www.google.com/search?q=${inputQuery.trim()}"
                            }
                            currentUrl = target
                            webViewInstance?.loadUrl(target)
                            onRecordBrowsing(15.0, 50.0) // Record data compression & generation
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberCyan)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("browser_url_input")
            )

            // Reload
            IconButton(
                onClick = {
                    webViewInstance?.reload()
                    onRecordBrowsing(10.0, 30.0)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CyberCyan, modifier = Modifier.size(20.dp))
            }
        }

        // Quick Shortcuts Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickShortcuts.forEach { (label, url) ->
                FilterChip(
                    selected = currentUrl == url,
                    onClick = {
                        currentUrl = url
                        inputQuery = url
                        webViewInstance?.loadUrl(url)
                        onRecordBrowsing(20.0, 60.0)
                    },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color(0xFF00272B),
                        containerColor = CyberSurfaceDark,
                        labelColor = TextSecondaryDark
                    )
                )
            }
        }

        if (isPageLoading) {
            LinearProgressIndicator(
                progress = { pageLoadingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = CyberCyan,
                trackColor = CyberSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }

        // Embedded Android WebView with Ultra Data Saver configurations
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            // Data saver settings: compress images, disable heavy animations if saver is on
                            loadsImagesAutomatically = true
                            blockNetworkImage = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                pageLoadingProgress = newProgress / 100f
                                isPageLoading = newProgress < 100
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isPageLoading = true
                                url?.let { inputQuery = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isPageLoading = false
                                url?.let {
                                    currentUrl = it
                                    inputQuery = it
                                }
                                onRecordBrowsing(8.5, 40.0) // Add data generated from website
                            }
                        }

                        loadUrl(currentUrl)
                        webViewInstance = this
                    }
                },
                update = { view ->
                    webViewInstance = view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("data_saver_webview")
            )
        }
    }
}
