package com.loveever.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loveever.app.ui.theme.DesignTokens

/** 纪念日图标字段 → 矢量图标映射 */
fun anniversaryIcon(icon: String): ImageVector = when (icon) {
    "star" -> Icons.Filled.Star
    "cake" -> Icons.Filled.Cake
    "gift" -> Icons.Filled.CardGiftcard
    "music" -> Icons.Filled.MusicNote
    "plane" -> Icons.Filled.Flight
    "camera" -> Icons.Filled.PhotoCamera
    else -> Icons.Filled.Favorite
}

/** 圆形图标徽章：统一的装饰容器 */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/** 统一卡片容器 */
@Composable
fun LoveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(DesignTokens.radiusLg)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.cardElevation)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

/** 区块标题：圆形图标 + 标题 */
@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, size = 36.dp)
        Spacer(modifier = Modifier.width(DesignTokens.spaceMd))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 空状态占位 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.spaceSection),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
    ) {
        IconBadge(
            icon = icon,
            size = 64.dp,
            container = MaterialTheme.colorScheme.surfaceVariant,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** 骨架屏脉冲块 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DesignTokens.radiusMd)
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}

/** 列表骨架卡片 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    LoveCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(DesignTokens.spaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            )
            Spacer(modifier = Modifier.width(DesignTokens.spaceMd))
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceSm)) {
                SkeletonBox(modifier = Modifier.width(140.dp).height(14.dp))
                SkeletonBox(modifier = Modifier.width(90.dp).height(11.dp))
            }
        }
    }
}
