package com.loveever.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LovePink500 = Color(0xFFF43F5E)
val LovePink600 = Color(0xFFE11D48)
val LovePink100 = Color(0xFFFFE3E8)
val WarmAmber500 = Color(0xFFF59E0B)

private val LightColorScheme = lightColorScheme(
    primary = LovePink500,
    onPrimary = Color.White,
    primaryContainer = LovePink100,
    secondary = WarmAmber500,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onSurface = Color(0xFF1E293B)
)

private val DarkColorScheme = darkColorScheme(
    primary = LovePink500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF881337),
    secondary = WarmAmber500,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC)
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
