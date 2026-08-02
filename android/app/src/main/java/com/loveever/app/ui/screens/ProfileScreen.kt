package com.loveever.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveever.app.api.ApiService
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "👤 个人与双人设置",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "我的邀请码 (发给恋人绑定)", fontSize = 13.sp, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = couple?.inviteCode ?: "—",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF43F5E)
                        )
                        IconButton(onClick = {
                            couple?.inviteCode?.let { code ->
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                        as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite_code", code))
                                android.widget.Toast.makeText(context, "邀请码已复制", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "复制邀请码", tint = Color(0xFFF43F5E))
                        }
                    }
                }
                Text(
                    text = if (partnerName.isBlank()) "还单身等恋人加入～" else "已与「$partnerName」绑定 💕",
                    fontSize = 13.sp,
                    color = if (partnerName.isBlank()) Color.Gray else Color(0xFFF43F5E)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "输入对方的邀请码完成绑定", fontSize = 13.sp, color = Color.Gray)
                OutlinedTextField(
                    value = inviteCodeInput,
                    onValueChange = { inviteCodeInput = it },
                    label = { Text("邀请码 (如 LOVE-520)") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (loading) "绑定中…" else "绑定恋人")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "相爱日期", fontSize = 13.sp, color = Color.Gray)
                OutlinedTextField(
                    value = pairDateInput,
                    onValueChange = { pairDateInput = it },
                    label = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.updatePairDate(pairDateInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("更新相爱日期")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "后端服务地址 (Go Backend)", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = ApiService.BASE_URL,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (user != null) "🟢 已连接「${user!!.username}」" else "🟢 高性能 Go + Gin 后端",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981)
                )
            }
        }

        OutlinedButton(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("退出登录", fontWeight = FontWeight.Bold)
        }
    }
}
