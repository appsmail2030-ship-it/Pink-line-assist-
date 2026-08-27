package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PinkLineDarkColorScheme = darkColorScheme(
    primary = PinkPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PinkGlow,
    onPrimaryContainer = PinkAccent,
    secondary = CyanNeon,
    onSecondary = Color.Black,
    secondaryContainer = CyanGlow,
    onSecondaryContainer = CyanNeon,
    tertiary = AmberAlert,
    onTertiary = Color.Black,
    tertiaryContainer = AmberAlertGlow,
    onTertiaryContainer = AmberAlert,
    error = RedCritical,
    onError = Color.White,
    errorContainer = RedCriticalGlow,
    onErrorContainer = RedCritical,
    background = DarkBackground,
    onBackground = TextHighEmphasis,
    surface = DarkSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMediumEmphasis,
    outline = DarkSurfaceBorder
)

private val PinkLineLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8E4),
    onPrimaryContainer = PinkPrimaryDark,
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = Color(0xFFF57C00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color(0xFFF8F9FD),
    onBackground = Color(0xFF10141D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10141D),
    surfaceVariant = Color(0xFFF0F2F8),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic dark mode for Pink Line Assist
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PinkLineDarkColorScheme else PinkLineLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

