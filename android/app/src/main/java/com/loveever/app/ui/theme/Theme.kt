package com.loveever.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WarmCoral = Color(0xFFE2673F)
val WarmPeach = Color(0xFFF5A25D)
val WarmHoney = Color(0xFFF2A65A)

private val LightColorScheme = lightColorScheme(
    primary = WarmCoral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2D4),
    onPrimaryContainer = Color(0xFF4A1806),
    secondary = WarmHoney,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8CF),
    onSecondaryContainer = Color(0xFF452A00),
    tertiary = Color(0xFFB98A6E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E5D9),
    onTertiaryContainer = Color(0xFF3F2413),
    background = Color(0xFFFFF9F3),
    onBackground = Color(0xFF3A2A20),
    surface = Color.White,
    onSurface = Color(0xFF3A2A20),
    surfaceVariant = Color(0xFFFCF0E5),
    onSurfaceVariant = Color(0xFF7A6A5C),
    outline = Color(0xFFE9DCCD),
    outlineVariant = Color(0xFFF2E8DB),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB59B),
    onPrimary = Color(0xFF5A1F0A),
    primaryContainer = Color(0xFF7A3519),
    onPrimaryContainer = Color(0xFFFFDBCA),
    secondary = Color(0xFFFFC98A),
    onSecondary = Color(0xFF452A00),
    secondaryContainer = Color(0xFF66400F),
    onSecondaryContainer = Color(0xFFFFE0B9),
    tertiary = Color(0xFFE4C2A9),
    onTertiary = Color(0xFF3F2413),
    tertiaryContainer = Color(0xFF5E3F2B),
    onTertiaryContainer = Color(0xFFF6E5D9),
    background = Color(0xFF1B130E),
    onBackground = Color(0xFFF6E7DC),
    surface = Color(0xFF241A13),
    onSurface = Color(0xFFF6E7DC),
    surfaceVariant = Color(0xFF3A2B20),
    onSurfaceVariant = Color(0xFFDCC8B8),
    outline = Color(0xFF6B5647),
    outlineVariant = Color(0xFF4A3B2E),
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
