package com.loveever.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveever.app.ui.components.EmptyState
import com.loveever.app.ui.components.IconBadge
import com.loveever.app.ui.components.LoveCard
import com.loveever.app.ui.components.SectionHeader
import com.loveever.app.ui.components.SkeletonCard
import com.loveever.app.ui.components.anniversaryIcon
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel

private val iconChoices = listOf("heart", "star", "cake", "gift", "music")

@Composable
fun AnniversariesScreen(viewModel: LoveViewModel) {
    val anniversaries by viewModel.anniversaries.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2026-05-20") }
    var isPinnedInput by remember { mutableStateOf(false) }
    var iconInput by remember { mutableStateOf("heart") }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(DesignTokens.radiusLg),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = DesignTokens.fabElevation)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加纪念日")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.background(DesignTokens.backgroundBrush)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = DesignTokens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)
            ) {
                item {
                    SectionHeader(
                        icon = Icons.Filled.CalendarMonth,
                        title = "纪念日清单",
                        modifier = Modifier.padding(vertical = DesignTokens.spaceLg)
                    )
                }

                when {
                    refreshing && anniversaries.isEmpty() -> {
                        items(3) {
                            SkeletonCard()
                        }
                    }
                    anniversaries.isEmpty() -> {
                        item {
                            EmptyState(
                                icon = Icons.Filled.CalendarMonth,
                                title = "还没有纪念日",
                                subtitle = "点右下角的 + 记录第一个值得庆祝的日子"
                            )
                        }
                    }
                    else -> {
                        items(anniversaries, key = { it.id }) { anniv ->
                            AnniversaryRow(
                                title = anniv.title,
                                date = anniv.targetDate,
                                icon = anniv.icon,
                                isPinned = anniv.isPinned,
                                onTogglePin = { viewModel.togglePin(anniv.id) },
                                onDelete = { viewModel.deleteAnniversary(anniv.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AnniversaryDialog(
            title = titleInput,
            onTitleChange = { titleInput = it },
            date = dateInput,
            onDateChange = { dateInput = it },
            icon = iconInput,
            onIconChange = { iconInput = it },
            isPinned = isPinnedInput,
            onPinnedChange = { isPinnedInput = it },
            onConfirm = {
                if (titleInput.isNotBlank()) {
                    viewModel.addAnniversary(titleInput, dateInput, isPinnedInput, iconInput)
                    titleInput = ""
                    showDialog = false
                }
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun AnniversaryRow(
    title: String,
    date: String,
    icon: String,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

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
                    fontSize = 16.sp,
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
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (isPinned) "取消置顶" else "置顶到首页",
                    tint = if (isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除纪念日") },
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
private fun AnniversaryDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    icon: String,
    onIconChange: (String) -> Unit,
    isPinned: Boolean,
    onPinnedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建纪念日", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceMd)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("纪念日名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = onDateChange,
                    label = { Text("日期 (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "图标",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.spaceSm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconChoices.forEach { choice ->
                        val selected = choice == icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickableNoRipple { onIconChange(choice) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = anniversaryIcon(choice),
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "置顶到首页",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = onPinnedChange
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(DesignTokens.radiusMd)
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this
        .clip(CircleShape)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
