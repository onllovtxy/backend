package com.loveever.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.loveever.app.ui.components.EmptyState
import com.loveever.app.ui.components.LoveCard
import com.loveever.app.ui.components.SectionHeader
import com.loveever.app.ui.components.SkeletonCard
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel

@Composable
fun MemoriesScreen(viewModel: LoveViewModel) {
    val memories by viewModel.memories.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2023-08-15") }
    var imageUrlInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = RoundedCornerShape(DesignTokens.radiusLg),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = DesignTokens.fabElevation)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记录新回忆")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.background(DesignTokens.backgroundBrush)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = DesignTokens.spaceLg),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
            ) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeader(
                        icon = Icons.Filled.PhotoCamera,
                        title = "时光墙",
                        modifier = Modifier.padding(vertical = DesignTokens.spaceLg)
                    )
                }

                when {
                    refreshing && memories.isEmpty() -> {
                        items(4) {
                            SkeletonCard()
                        }
                    }
                    memories.isEmpty() -> {
                        item(span = { GridItemSpan(2) }) {
                            EmptyState(
                                icon = Icons.Filled.PhotoCamera,
                                title = "还没有回忆",
                                subtitle = "点右下角的 + 记录你们的甜蜜瞬间"
                            )
                        }
                    }
                    else -> {
                        items(memories, key = { it.id }) { mem ->
                            MemoryCard(
                                title = mem.title,
                                content = mem.content,
                                date = mem.memoryDate,
                                imageUrl = mem.imageUrl,
                                onDelete = { viewModel.deleteMemory(mem.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MemoryDialog(
            title = titleInput,
            onTitleChange = { titleInput = it },
            content = contentInput,
            onContentChange = { contentInput = it },
            date = dateInput,
            onDateChange = { dateInput = it },
            imageUrl = imageUrlInput,
            onImageUrlChange = { imageUrlInput = it },
            onConfirm = {
                if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                    viewModel.addMemory(titleInput, contentInput, dateInput, imageUrlInput)
                    titleInput = ""
                    contentInput = ""
                    showDialog = false
                }
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun MemoryCard(
    title: String,
    content: String,
    date: String,
    imageUrl: String,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    LoveCard {
        Column {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = DesignTokens.radiusLg, topEnd = DesignTokens.radiusLg)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(DesignTokens.spaceMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除回忆",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = content,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除回忆") },
            text = { Text("确定要删除「$title」吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MemoryDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录新回忆", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("回忆主题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("甜蜜日记内容") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = onDateChange,
                    label = { Text("发生日期 (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = onImageUrlChange,
                    label = { Text("照片 URL (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(DesignTokens.radiusMd)
            ) {
                Text("保存回忆")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
