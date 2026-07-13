package com.ebchat.ui.theme

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

// ===== PINK & GLASS THEME COLORS =====
val PinkPrimary = Color(0xFFFF69B4)
val PinkPrimaryDark = Color(0xFFFF1493)
val PinkLight = Color(0xFFFFB6C1)
val PinkAccent = Color(0xFFFFC0CB)

// Glass effect colors
val GlassBackground = Color(0xFFFCE4EC)
val GlassSurface = Color(0xFFFFEBEE)
val GlassOverlay = Color(0x80FFFFFF)
val GlassBorder = Color(0x40FFFFFF)

// Status colors
val OnlineGreen = Color(0xFF4CAF50)
val TypingBlue = Color(0xFF2196F3)
val SentGray = Color(0xFF9E9E9E)
val DeliveredBlue = Color(0xFF64B5F6)
val ReadBlue = Color(0xFF1976D2)

// Dark theme colors
val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF16213E)
val DarkGlass = Color(0x80000000)

// Theme variations
val BluePrimary = Color(0xFF2196F3)
val PurplePrimary = Color(0xFF9C27B0)
val GreenPrimary = Color(0xFF4CAF50)
val OrangePrimary = Color(0xFFFF9800)
val RedPrimary = Color(0xFFE91E63)
val TealPrimary = Color(0xFF009688)

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkLight,
    onPrimaryContainer = PinkPrimaryDark,
    secondary = PinkAccent,
    onSecondary = Color.White,
    secondaryContainer = PinkLight.copy(alpha = 0.5f),
    onSecondaryContainer = PinkPrimaryDark,
    tertiary = Color(0xFFFFA07A),
    onTertiary = Color.White,
    background = GlassBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = GlassSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFFFE4E1),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color.White,
    outline = PinkPrimary.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PinkLight,
    onPrimary = PinkPrimaryDark,
    primaryContainer = PinkPrimaryDark,
    onPrimaryContainer = PinkLight,
    secondary = PinkAccent,
    onSecondary = PinkPrimaryDark,
    secondaryContainer = PinkPrimaryDark.copy(alpha = 0.5f),
    onSecondaryContainer = PinkLight,
    tertiary = Color(0xFFFFA07A),
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFE6E1E5),
    surface = DarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2A2A4A),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    outline = PinkLight.copy(alpha = 0.5f)
)

@Composable
fun EBChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun getThemeColors(themeName: String): ThemeColors {
    return when (themeName) {
        "blue" -> ThemeColors(
            primary = BluePrimary,
            primaryDark = Color(0xFF1565C0),
            light = Color(0xFFBBDEFB),
            accent = Color(0xFF90CAF9),
            background = Color(0xFFE3F2FD),
            surface = Color(0xFFBBDEFB)
        )
        "purple" -> ThemeColors(
            primary = PurplePrimary,
            primaryDark = Color(0xFF6A1B9A),
            light = Color(0xFFE1BEE7),
            accent = Color(0xFFCE93D8),
            background = Color(0xFFF3E5F5),
            surface = Color(0xFFE1BEE7)
        )
        "green" -> ThemeColors(
            primary = GreenPrimary,
            primaryDark = Color(0xFF388E3C),
            light = Color(0xFFC8E6C9),
            accent = Color(0xFFA5D6A7),
            background = Color(0xFFE8F5E9),
            surface = Color(0xFFC8E6C9)
        )
        "dark" -> ThemeColors(
            primary = PinkLight,
            primaryDark = PinkPrimary,
            light = PinkAccent,
            accent = PinkLight,
            background = DarkBackground,
            surface = DarkSurface
        )
        else -> ThemeColors( // pink_glass default
            primary = PinkPrimary,
            primaryDark = PinkPrimaryDark,
            light = PinkLight,
            accent = PinkAccent,
            background = GlassBackground,
            surface = GlassSurface
        )
    }
}

data class ThemeColors(
    val primary: Color,
    val primaryDark: Color,
    val light: Color,
    val accent: Color,
    val background: Color,
    val surface: Color
)
