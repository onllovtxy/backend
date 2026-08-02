package com.loveever.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel

@Composable
fun AuthScreen(viewModel: LoveViewModel) {
    var isRegister by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var pairDate by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DesignTokens.spaceXxl)
                .padding(vertical = DesignTokens.spaceSection),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "恋念",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
            Text(
                text = "恋念 LoveEver",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "记录我们的每一个重要时刻",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = DesignTokens.spaceXs)
            )

            Spacer(modifier = Modifier.height(DesignTokens.spaceSection))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.radiusXl),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.heroElevation)
            ) {
                Column(modifier = Modifier.padding(DesignTokens.spaceXxl)) {
                    TabRow(
                        selectedTabIndex = if (isRegister) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = !isRegister,
                            onClick = { isRegister = false },
                            text = { Text("登录", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = isRegister,
                            onClick = { isRegister = true },
                            text = { Text("注册", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.spaceXl))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isRegister) {
                        Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("昵称（恋人眼中你的名字）") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
                        OutlinedTextField(
                            value = pairDate,
                            onValueChange = { pairDate = it },
                            label = { Text("相爱日期 (YYYY-MM-DD，可选)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    error?.let {
                        Spacer(modifier = Modifier.height(DesignTokens.spaceMd))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.spaceXl))
                    Button(
                        onClick = {
                            if (isRegister) {
                                viewModel.register(username, password, displayName, pairDate)
                            } else {
                                viewModel.login(username, password)
                            }
                        },
                        enabled = !loading && username.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(DesignTokens.radiusLg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = when {
                                loading -> "请稍候…"
                                isRegister -> "注册并创建情侣空间"
                                else -> "登录"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.spaceLg))
            Text(
                text = "注册后即可获得专属邀请码，与恋人绑定实时同步",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
