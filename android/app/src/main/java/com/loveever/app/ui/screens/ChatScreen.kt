package com.loveever.app.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.loveever.app.api.ApiService
import com.loveever.app.data.TokenHolder
import com.loveever.app.model.Message
import com.loveever.app.ui.components.AuthImage
import com.loveever.app.ui.theme.DesignTokens
import com.loveever.app.viewmodel.LoveViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: LoveViewModel, onBack: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val user by viewModel.user.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val myUserId = user?.id ?: 0

    var input by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val recorder = remember { ChatRecorder(context) }
    val player = remember { ChatPlayer(context) }
    var playingId by remember { mutableStateOf<Long?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            recorder.release()
            player.release()
        }
    }

    // 新消息自动滚到底部（reverseLayout 时滚到 index 0）
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // 录音权限
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (recorder.start()) isRecording = true
        } else {
            Toast.makeText(context, "需要录音权限才能发送语音", Toast.LENGTH_SHORT).show()
        }
    }

    // 图片选择
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.sendImage(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = partnerName.ifBlank { "聊天" },
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "你们俩的专属空间",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                input = input,
                onInputChange = { input = it },
                isRecording = isRecording,
                onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendText(input)
                        input = ""
                    }
                },
                onPickImage = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onToggleRecord = {
                    if (isRecording) {
                        val duration = recorder.stop()
                        isRecording = false
                        recorder.file?.let { viewModel.sendVoice(it, duration) }
                    } else {
                        val granted = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            if (recorder.start()) isRecording = true
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignTokens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spaceSm),
            contentPadding = PaddingValues(vertical = DesignTokens.spaceMd)
        ) {
            if (messages.size >= 50) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { viewModel.loadOlderMessages() }) {
                            Text("查看更早的消息")
                        }
                    }
                }
            }

            items(messages.reversed(), key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    isMine = msg.senderId == myUserId,
                    isPlaying = playingId == msg.id,
                    onPlayToggle = {
                        if (playingId == msg.id) {
                            player.stop()
                            playingId = null
                        } else {
                            player.play(
                                url = ApiService.BASE_URL.trimEnd('/') + msg.content,
                                token = TokenHolder.token,
                                onFinish = { playingId = null }
                            )
                            playingId = msg.id
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    isRecording: Boolean,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    onToggleRecord: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(vertical = DesignTokens.spaceSm)) {
            if (isRecording) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.spaceLg)
                        .padding(bottom = DesignTokens.spaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.spaceSm))
                    Text(
                        text = "正在录音，点击麦克风停止并发送",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPickImage) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "发送图片",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = { Text("说点什么…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(DesignTokens.radiusXl),
                    maxLines = 4
                )
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) onSend() else onToggleRecord()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    if (input.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "发送",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "语音",
                            tint = if (isRecording) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = DesignTokens.radiusLg,
                    topEnd = DesignTokens.radiusLg,
                    bottomStart = if (isMine) DesignTokens.radiusLg else DesignTokens.radiusSm,
                    bottomEnd = if (isMine) DesignTokens.radiusSm else DesignTokens.radiusLg
                ),
                color = if (isMine) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isMine) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            ) {
                when (message.type) {
                    "image" -> {
                        AuthImage(
                            url = message.content,
                            contentDescription = "图片消息",
                            modifier = Modifier
                                .width(220.dp)
                                .height(220.dp)
                                .clip(RoundedCornerShape(DesignTokens.radiusLg))
                        )
                    }
                    "voice" -> {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.spaceMd,
                                vertical = DesignTokens.spaceSm
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onPlayToggle) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "暂停" else "播放语音",
                                    tint = if (isMine) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "${message.duration} 秒",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.spaceLg,
                                vertical = DesignTokens.spaceMd
                            )
                        )
                    }
                }
            }
            Text(
                text = timeOf(message.createdAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(
                    horizontal = DesignTokens.spaceSm,
                    vertical = 2.dp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun timeOf(iso: String): String {
    return if (iso.length >= 16) iso.substring(11, 16) else iso
}

/** 录音器封装：m4a/AAC */
private class ChatRecorder(private val context: Context) {
    var file: File? = null
        private set
    private var recorder: MediaRecorder? = null
    private var startTime = 0L

    fun start(): Boolean {
        return try {
            val dir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val f = File(dir, "voice_${System.currentTimeMillis()}.m4a")
            val r = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(f.absolutePath)
                prepare()
                start()
            }
            recorder = r
            file = f
            startTime = SystemClock.elapsedRealtime()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stop(): Int {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            // 录音时间过短时会抛异常，忽略
        } finally {
            recorder = null
        }
        val duration = ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
        return duration.coerceAtLeast(1)
    }

    fun release() {
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
    }
}

/** 语音播放封装：携带鉴权头 */
private class ChatPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var onFinish: (() -> Unit)? = null

    fun play(url: String, token: String?, onFinish: () -> Unit) {
        stop()
        this.onFinish = onFinish
        val p = MediaPlayer()
        try {
            if (token != null) {
                val headers = HashMap<String, String>().apply {
                    put("Authorization", "Bearer $token")
                }
                p.setDataSource(context, Uri.parse(url), headers, emptyList())
            } else {
                p.setDataSource(context, Uri.parse(url))
            }
            p.setOnPreparedListener { it.start() }
            p.setOnCompletionListener { mp ->
                mp.release()
                if (player === mp) player = null
                this.onFinish?.invoke()
            }
            p.setOnErrorListener { mp, _, _ ->
                mp.release()
                if (player === mp) player = null
                this.onFinish?.invoke()
                true
            }
            p.prepareAsync()
            player = p
        } catch (e: Exception) {
            p.release()
            onFinish()
        }
    }

    fun stop() {
        val p = player
        player = null
        onFinish = null
        try {
            p?.stop()
        } catch (_: Exception) {
        }
        try {
            p?.release()
        } catch (_: Exception) {
        }
    }

    fun release() {
        stop()
    }
}
