package com.loveever.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.loveever.app.ui.components.EmptyState
import com.loveever.app.ui.components.IconBadge
import com.loveever.app.ui.components.LoveCard
import com.loveever.app.ui.components.SectionHeader
import com.loveever.app.ui.components.SkeletonCard
import com.loveever.app.ui.components.anniversaryIcon
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(viewModel: LoveViewModel) {
    val couple by viewModel.couple.collectAsState()
    val user by viewModel.user.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val partnerAvatar by viewModel.partnerAvatar.collectAsState()
    val anniversaries by viewModel.anniversaries.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val daysCount = viewModel.calculateDaysCount()

    val pinnedList = anniversaries.filter { it.isPinned }

    Box(modifier = Modifier.background(DesignTokens.backgroundBrush)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceXl)
        ) {
            // 1. 相爱天数 Hero 大卡片
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
                HeroCard(
                    daysCount = daysCount,
                    pairDate = couple?.pairDate,
                    myName = user?.displayName.orEmpty().ifBlank { "我" },
                    myAvatar = user?.avatarUrl,
                    partnerName = partnerName.ifBlank { "恋人" },
                    partnerAvatar = partnerAvatar
                )
            }

            // 2. 重要置顶纪念日
            item {
                SectionHeader(
                    icon = Icons.Filled.Favorite,
                    title = "重要纪念日",
                    modifier = Modifier.padding(top = DesignTokens.spaceSm)
                )
            }

            when {
                refreshing && pinnedList.isEmpty() -> {
                    items(2) {
                        SkeletonCard()
                    }
                }
                pinnedList.isEmpty() -> {
                    item {
                        EmptyState(
                            icon = Icons.Filled.SentimentSatisfied,
                            title = "还没有置顶纪念日",
                            subtitle = "去「清单」里添加一个，并点亮星标就会出现在这里"
                        )
                    }
                }
                else -> {
                    items(pinnedList, key = { it.id }) { item ->
                        PinnedAnniversaryCard(
                            title = item.title,
                            date = item.targetDate,
                            icon = item.icon
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    daysCount: Long,
    pairDate: String?,
    myName: String,
    myAvatar: String?,
    partnerName: String,
    partnerAvatar: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(DesignTokens.radiusXl),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.heroElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignTokens.heroGradient)
                .padding(DesignTokens.spaceXxl)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 双人头像与跳动心形
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.spaceXl)
                ) {
                    AvatarWithName(name = myName, avatar = myAvatar)
                    HeartPulse()
                    AvatarWithName(name = partnerName, avatar = partnerAvatar)
                }

                // 相爱天数
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "我们在一起已经",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$daysCount",
                            color = Color.White,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " 天",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                    }
                }

                // 相爱开始时间标签
                Surface(
                    shape = RoundedCornerShape(DesignTokens.radiusFull),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "相爱于 ${pairDate ?: "—"}",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = DesignTokens.spaceLg, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarWithName(name: String, avatar: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = avatar?.takeIf { it.isNotBlank() },
            contentDescription = name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = DesignTokens.spaceXs)
        )
    }
}

@Composable
private fun HeartPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(heartScale)
            .background(Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun PinnedAnniversaryCard(title: String, date: String, icon: String) {
    LoveCard {
        Row(
            modifier = Modifier.padding(DesignTokens.spaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = anniversaryIcon(icon))
            Spacer(modifier = Modifier.width(DesignTokens.spaceMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = countdownText(date),
                    color = if (daysUntil(date) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "查看详情",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun daysUntil(dateStr: String): Long {
    return try {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(dateStr))
    } catch (e: Exception) {
        0
    }
}

private fun countdownText(dateStr: String): String {
    val diff = daysUntil(dateStr)
    return when {
        diff > 0 -> "还有 $diff 天"
        diff == 0L -> "就是今天"
        else -> "已过 ${-diff} 天"
    }
}
