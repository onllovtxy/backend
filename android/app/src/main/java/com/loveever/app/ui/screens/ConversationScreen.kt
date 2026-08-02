package com.loveever.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.loveever.app.model.Message
import com.loveever.app.ui.components.EmptyState
import com.loveever.app.ui.components.LoveCard
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel

@Composable
fun ConversationScreen(viewModel: LoveViewModel, onOpenChat: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val partnerAvatar by viewModel.partnerAvatar.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val lastMsg = messages.lastOrNull()

    Box(modifier = Modifier.background(DesignTokens.backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.spaceLg)
        ) {
            Text(
                text = "消息",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = DesignTokens.spaceLg)
            )

            if (lastMsg != null) {
                LoveCard(onClick = onOpenChat) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.spaceLg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 恋人头像
                        AsyncImage(
                            model = partnerAvatar.ifBlank { null },
                            contentDescription = partnerName,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.spaceMd))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partnerName.ifBlank { "恋人" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = messagePreview(lastMsg),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = timeLabel(lastMsg.createdAt),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 5.dp)
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Filled.ChatBubbleOutline,
                    title = "还没有消息",
                    subtitle = "给恋人发第一条悄悄话吧，支持文字、图片和语音"
                )
            }
        }
    }
}

private fun messagePreview(msg: Message): String = when (msg.type) {
    "image" -> "[图片]"
    "voice" -> "[语音] ${msg.duration}″"
    else -> msg.content
}

private fun timeLabel(iso: String): String {
    return if (iso.length >= 16) iso.substring(11, 16) else ""
}
