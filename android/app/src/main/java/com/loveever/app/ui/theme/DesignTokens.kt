package com.loveever.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 全局设计令牌：间距(4dp 节奏)、圆角、阴影与品牌渐变。
 * 所有界面统一引用，禁止在各 Screen 中散落裸色值。
 */
object DesignTokens {
    // 间距
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 20.dp
    val spaceXxl = 24.dp
    val spaceSection = 32.dp

    // 圆角
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 20.dp
    val radiusXl = 28.dp
    val radiusFull = 50.dp

    // 阴影层级
    val cardElevation = 1.dp
    val heroElevation = 6.dp
    val fabElevation = 4.dp

    // 品牌渐变：玫瑰粉 → 珊瑚粉 → 暖杏
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE94D6C),
            Color(0xFFFB7A96),
            Color(0xFFF5A25D)
        )
    )

    // 页面背景渐变（随明暗模式切换）
    val backgroundBrush: Brush
        @Composable get() = if (isSystemInDarkTheme()) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF1F1418), Color(0xFF171014))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF3F5), Color(0xFFFFFBF9))
            )
        }
}
