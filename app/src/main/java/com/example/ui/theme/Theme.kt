package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF002022),
    primaryContainer = Color(0xFF004F56),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0040A1),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = Color(0xFF6CFFA0),
    background = CyberDarkBg,
    onBackground = TextPrimaryDark,
    surface = CyberSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberCardBorder,
    error = CyberRed
)

private val LightColorScheme = lightColorScheme(
    primary = CyberBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE2FF),
    onPrimaryContainer = Color(0xFF001946),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA6EEF7),
    onSecondaryContainer = Color(0xFF001F23),
    tertiary = Color(0xFF00897B),
    onTertiary = Color.White,
    background = CyberLightBg,
    onBackground = TextPrimaryLight,
    surface = CyberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CyberLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = CyberRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek high-tech cyber dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
