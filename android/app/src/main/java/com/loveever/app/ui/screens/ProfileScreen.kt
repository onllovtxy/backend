package com.loveever.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.loveever.app.api.ApiService
import com.loveever.app.ui.components.IconBadge
import com.loveever.app.ui.components.LoveCard
import com.loveever.app.ui.components.SectionHeader
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel

@Composable
fun ProfileScreen(viewModel: LoveViewModel) {
    val couple by viewModel.couple.collectAsState()
    val user by viewModel.user.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    var inviteCodeInput by remember { mutableStateOf("") }
    var pairDateInput by remember { mutableStateOf("") }
    var pairDateInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(couple?.pairDate) {
        if (!pairDateInitialized && couple?.pairDate != null) {
            pairDateInput = couple!!.pairDate
            pairDateInitialized = true
        }
    }

    Box(modifier = Modifier.background(DesignTokens.backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DesignTokens.spaceLg)
                .padding(bottom = DesignTokens.spaceXxl),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceLg)
        ) {
            SectionHeader(
                icon = Icons.Filled.Person,
                title = "个人与双人设置",
                modifier = Modifier.padding(vertical = DesignTokens.spaceLg)
            )

            // 我的信息
            LoveCard {
                Row(
                    modifier = Modifier.padding(DesignTokens.spaceLg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user?.avatarUrl,
                        contentDescription = "我的头像",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.spaceLg))
                    Column {
                        Text(
                            text = user?.displayName.orEmpty().ifBlank { "未命名" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@${user?.username.orEmpty()}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // 邀请码
            LoveCard {
                Column(
                    modifier = Modifier.padding(DesignTokens.spaceXl),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(icon = Icons.Filled.Favorite, size = 32.dp)
                        Spacer(modifier = Modifier.width(DesignTokens.spaceSm))
                        Text(
                            text = "我的邀请码",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(DesignTokens.radiusMd),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = DesignTokens.spaceLg, vertical = DesignTokens.spaceMd),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = couple?.inviteCode ?: "—",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(onClick = {
                                couple?.inviteCode?.let { code ->
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                            as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite_code", code))
                                    android.widget.Toast.makeText(context, "邀请码已复制", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "复制邀请码",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(
                        text = if (partnerName.isBlank()) {
                            "把邀请码发给恋人，完成绑定后即可实时同步"
                        } else {
                            "已与「$partnerName」绑定，双端实时同步中"
                        },
                        fontSize = 13.sp,
                        color = if (partnerName.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // 绑定恋人
            LoveCard {
                Column(
                    modifier = Modifier.padding(DesignTokens.spaceXl),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(icon = Icons.Filled.SyncAlt, size = 32.dp)
                        Spacer(modifier = Modifier.width(DesignTokens.spaceSm))
                        Text(
                            text = "绑定恋人",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it },
                        label = { Text("对方的邀请码 (如 LOVE-520)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (inviteCodeInput.isNotBlank()) {
                                viewModel.pair(inviteCodeInput)
                            }
                        },
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(DesignTokens.radiusMd),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (loading) "绑定中…" else "绑定恋人")
                    }
                }
            }

            // 相爱日期
            LoveCard {
                Column(
                    modifier = Modifier.padding(DesignTokens.spaceXl),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(icon = Icons.Filled.WbSunny, size = 32.dp)
                        Spacer(modifier = Modifier.width(DesignTokens.spaceSm))
                        Text(
                            text = "相爱日期",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedTextField(
                        value = pairDateInput,
                        onValueChange = { pairDateInput = it },
                        label = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.updatePairDate(pairDateInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(DesignTokens.radiusMd),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("更新相爱日期")
                    }
                }
            }

            // 后端服务状态
            LoveCard {
                Row(
                    modifier = Modifier.padding(DesignTokens.spaceXl),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(
                        icon = Icons.Filled.Storage,
                        size = 40.dp,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.spaceLg))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "后端服务",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ApiService.BASE_URL,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = DesignTokens.spaceSm)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " 在线 · 高性能 Go + Gin 后端",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // 退出登录（危险操作，与普通操作视觉隔离）
            OutlinedButton(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = Color.Transparent
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(DesignTokens.radiusMd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.spaceSm))
                Text("退出登录", fontWeight = FontWeight.Bold)
            }
        }
    }
}
