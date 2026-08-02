package com.loveever.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LoveRose = Color(0xFFE94D6C)
val LoveRoseSoft = Color(0xFFFF8FA3)
val LoveAmber = Color(0xFFF5A25D)
val LovePeach = Color(0xFFFF9A8B)
val LoveBlush = Color(0xFFFFF0F3)

private val LightColorScheme = lightColorScheme(
    primary = LoveRose,
    onPrimary = Color.White,
    primaryContainer = LoveBlush,
    onPrimaryContainer = Color(0xFF4A1220),
    secondary = LoveAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8D6),
    onSecondaryContainer = Color(0xFF4A2410),
    tertiary = LovePeach,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0DB),
    onTertiaryContainer = Color(0xFF4A150F),
    background = Color(0xFFFFF7F8),
    onBackground = Color(0xFF2A1B1F),
    surface = Color.White,
    onSurface = Color(0xFF2A1B1F),
    surfaceVariant = Color(0xFFFFEFF2),
    onSurfaceVariant = Color(0xFF6E5A60),
    outline = Color(0xFFE8D4D9),
    outlineVariant = Color(0xFFF3E4E7),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = LoveRoseSoft,
    onPrimary = Color(0xFF4A1220),
    primaryContainer = Color(0xFF6E2434),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Color(0xFFFFB77C),
    onSecondary = Color(0xFF3D2108),
    secondaryContainer = Color(0xFF5A3412),
    onSecondaryContainer = Color(0xFFFFDCC0),
    tertiary = Color(0xFFFFB4A8),
    onTertiary = Color(0xFF46150F),
    tertiaryContainer = Color(0xFF65251C),
    onTertiaryContainer = Color(0xFFFFDBD3),
    background = Color(0xFF171014),
    onBackground = Color(0xFFF7E9EC),
    surface = Color(0xFF1F161A),
    onSurface = Color(0xFFF7E9EC),
    surfaceVariant = Color(0xFF2E2025),
    onSurfaceVariant = Color(0xFFD9C2C8),
    outline = Color(0xFF4A3A3F),
    outlineVariant = Color(0xFF3A2B30),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun LoveEverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
