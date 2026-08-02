package com.loveever.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.loveever.app.api.ApiClient
import com.loveever.app.api.ApiService
import com.loveever.app.data.TokenHolder
import com.loveever.app.data.TokenStore
import com.loveever.app.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response as RetrofitResponse
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    data class LoggedIn(val token: String) : AuthState()
}

class LoveViewModel(application: Application) : AndroidViewModel(application) {
    private val api: ApiService = ApiClient.api
    private val tokenStore = TokenStore(application)
    private val gson = Gson()

    private val _auth = MutableStateFlow<AuthState>(AuthState.Loading)
    val auth: StateFlow<AuthState> = _auth.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _couple = MutableStateFlow<Couple?>(null)
    val couple: StateFlow<Couple?> = _couple.asStateFlow()

    private val _partnerName = MutableStateFlow("")
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    private val _partnerAvatar = MutableStateFlow("")
    val partnerAvatar: StateFlow<String> = _partnerAvatar.asStateFlow()

    private val _anniversaries = MutableStateFlow<List<Anniversary>>(emptyList())
    val anniversaries: StateFlow<List<Anniversary>> = _anniversaries.asStateFlow()

    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories: StateFlow<List<Memory>> = _memories.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _info = MutableStateFlow<String?>(null)
    val info: StateFlow<String?> = _info.asStateFlow()

    private var ws: WebSocket? = null
    private var reconnectJob: Job? = null
    private val wsClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    init {
        viewModelScope.launch {
            val token = tokenStore.get()
            if (token.isNullOrBlank()) {
                _auth.value = AuthState.LoggedOut
            } else {
                TokenHolder.token = token
                _auth.value = AuthState.LoggedIn(token)
                refreshAll()
                connectWebSocket(token)
            }
        }
    }

    fun login(username: String, password: String) {
        runTask {
            val resp = api.login(LoginRequest(username, password))
            val body = resp.body()
            if (resp.isSuccessful && body?.token != null) {
                tokenStore.save(body.token!!)
                applyLoggedIn(body.token!!)
            } else {
                _error.value = errorMessage(resp, body?.error ?: "登录失败，请检查用户名或密码")
            }
        }
    }

    fun register(username: String, password: String, displayName: String, pairDate: String?) {
        runTask {
            val req = RegisterRequest(
                username = username,
                password = password,
                displayName = displayName,
                pairDate = pairDate?.takeIf { it.isNotBlank() }
            )
            val resp = api.register(req)
            val body = resp.body()
            if (resp.isSuccessful && body?.token != null) {
                tokenStore.save(body.token!!)
                applyLoggedIn(body.token!!)
                _info.value = "注册成功，请与恋人分享你的邀请码"
            } else {
                _error.value = errorMessage(resp, body?.error ?: "注册失败")
            }
        }
    }

    fun pair(inviteCode: String) {
        val token = currentToken() ?: return
        runTask {
            val resp = api.pairCouple(token, PairRequest(inviteCode.trim()))
            val body = resp.body()
            if (resp.isSuccessful && body?.token != null) {
                tokenStore.save(body.token!!)
                applyLoggedIn(body.token!!)
                _info.value = body.message ?: "配对成功"
            } else {
                _error.value = errorMessage(resp, body?.error ?: "配对失败，请检查邀请码")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            reconnectJob?.cancel()
            ws?.close(1000, "logout")
            ws = null
            tokenStore.clear()
            TokenHolder.token = null
            _auth.value = AuthState.LoggedOut
            _user.value = null
            _couple.value = null
            _partnerName.value = ""
            _partnerAvatar.value = ""
            _anniversaries.value = emptyList()
            _memories.value = emptyList()
            _messages.value = emptyList()
        }
    }

    fun refreshAll() {
        val token = currentToken() ?: return
        viewModelScope.launch {
            _refreshing.value = true
            try {
                runCatching { api.getProfile(token) }.getOrNull()?.let { resp ->
                    if (resp.isSuccessful) resp.body()?.let { b ->
                        _user.value = b.user
                        _couple.value = b.couple
                        _partnerName.value = b.partnerName.orEmpty()
                        _partnerAvatar.value = b.partnerAvatar.orEmpty()
                    }
                }
                runCatching { api.getAnniversaries(token) }.getOrNull()?.let { resp ->
                    if (resp.isSuccessful) resp.body()?.data?.let { _anniversaries.value = it }
                }
                runCatching { api.getMemories(token) }.getOrNull()?.let { resp ->
                    if (resp.isSuccessful) resp.body()?.data?.let { _memories.value = it }
                }
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun addAnniversary(title: String, date: String, isPinned: Boolean, icon: String = "heart") {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.createAnniversary(token, CreateAnniversaryReq(title, date, isPinned, icon))
            if (resp.isSuccessful) {
                resp.body()?.data?.let { anniv ->
                    _anniversaries.value = listOf(anniv) + _anniversaries.value.filter { it.id != anniv.id }
                }
            } else {
                _error.value = errorMessage(resp, "添加纪念日失败")
            }
        }
    }

    fun togglePin(id: Long) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.togglePin(token, id)
            if (resp.isSuccessful) {
                resp.body()?.data?.let { updated ->
                    _anniversaries.value = _anniversaries.value.map { if (it.id == updated.id) updated else it }
                }
            } else {
                _error.value = errorMessage(resp, "置顶操作失败")
            }
        }
    }

    fun deleteAnniversary(id: Long) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.deleteAnniversary(token, id)
            if (resp.isSuccessful) {
                _anniversaries.value = _anniversaries.value.filter { it.id != id }
            } else {
                _error.value = errorMessage(resp, "删除纪念日失败")
            }
        }
    }

    fun updatePairDate(date: String) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.updatePairDate(token, UpdatePairDateReq(date))
            if (resp.isSuccessful) {
                _couple.value = _couple.value?.copy(pairDate = date)
                _info.value = "相爱日期已更新"
            } else {
                _error.value = errorMessage(resp, "更新失败")
            }
        }
    }

    fun addMemory(title: String, content: String, date: String, imageUrl: String) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.createMemory(token, CreateMemoryReq(title, content, date, imageUrl))
            if (resp.isSuccessful) {
                resp.body()?.data?.let { mem ->
                    _memories.value = listOf(mem) + _memories.value.filter { it.id != mem.id }
                }
            } else {
                _error.value = errorMessage(resp, "添加回忆失败")
            }
        }
    }

    fun deleteMemory(id: Long) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            val resp = api.deleteMemory(token, id)
            if (resp.isSuccessful) {
                _memories.value = _memories.value.filter { it.id != id }
            } else {
                _error.value = errorMessage(resp, "删除回忆失败")
            }
        }
    }

    fun calculateDaysCount(): Long {
        val pairDate = _couple.value?.pairDate ?: return 0
        return try {
            val start = LocalDate.parse(pairDate)
            val now = LocalDate.now()
            ChronoUnit.DAYS.between(start, now).coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    // ========== 聊天 ==========

    fun loadMessages() {
        val token = currentToken() ?: return
        viewModelScope.launch {
            runCatching { api.getMessages(token) }.getOrNull()?.let { resp ->
                if (resp.isSuccessful) {
                    resp.body()?.data?.let { _messages.value = it }
                }
            }
        }
    }

    fun loadOlderMessages() {
        val token = currentToken() ?: return
        val oldest = _messages.value.firstOrNull()?.id ?: return
        viewModelScope.launch {
            runCatching { api.getMessages(token, beforeId = oldest) }.getOrNull()?.let { resp ->
                if (resp.isSuccessful) {
                    resp.body()?.data?.let { older ->
                        if (older.isNotEmpty()) {
                            _messages.value = (older + _messages.value).distinctBy { it.id }
                        }
                    }
                }
            }
        }
    }

    fun sendText(text: String) {
        val token = currentToken() ?: return
        val content = text.trim()
        if (content.isEmpty()) return
        viewModelScope.launch {
            val resp = api.sendMessage(token, SendMessageReq(type = "text", content = content))
            if (resp.isSuccessful) {
                resp.body()?.data?.let { appendMessage(it) }
            } else {
                _error.value = errorMessage(resp, "发送失败")
            }
        }
    }

    fun sendImage(uri: Uri) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            try {
                val bytes = compressImage(uri)
                uploadAndSend(token, bytes, "image.jpg", "image", 0)
            } catch (e: Exception) {
                _error.value = "发送图片失败"
            }
        }
    }

    fun sendVoice(file: File, duration: Int) {
        val token = currentToken() ?: return
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                uploadAndSend(token, bytes, "voice.m4a", "voice", duration.coerceAtLeast(1))
            } catch (e: Exception) {
                _error.value = "发送语音失败"
            }
        }
    }

    private suspend fun uploadAndSend(token: String, bytes: ByteArray, filename: String, type: String, duration: Int) {
        val body = bytes.toRequestBody("application/octet-stream".toMediaType())
        val part = MultipartBody.Part.createFormData("file", filename, body)
        val resp = api.upload(token, part)
        val url = resp.body()?.url
        if (!resp.isSuccessful || url.isNullOrBlank()) {
            _error.value = "文件上传失败"
            return
        }
        val sendResp = api.sendMessage(token, SendMessageReq(type = type, content = url, duration = duration))
        if (sendResp.isSuccessful) {
            sendResp.body()?.data?.let { appendMessage(it) }
        } else {
            _error.value = errorMessage(sendResp, "发送失败")
        }
    }

    private fun appendMessage(msg: Message) {
        _messages.value = (_messages.value + msg).distinctBy { it.id }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val resolver = getApplication<Application>().contentResolver
        val input = resolver.openInputStream(uri) ?: error("cannot read image")
        val bitmap = BitmapFactory.decodeStream(input)
        input.close()

        val maxDim = 1280f
        val scale = minOf(1f, maxDim / maxOf(bitmap.width.toFloat(), bitmap.height.toFloat()))
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearInfo() {
        _info.value = null
    }

    private fun currentToken(): String? = (_auth.value as? AuthState.LoggedIn)?.token

    private fun applyLoggedIn(token: String) {
        TokenHolder.token = token
        _auth.value = AuthState.LoggedIn(token)
        refreshAll()
        connectWebSocket(token)
    }

    private fun runTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                block()
            } catch (e: HttpException) {
                _error.value = e.response()?.errorBody()?.string() ?: "网络请求失败"
            } catch (e: Exception) {
                _error.value = e.message ?: "网络请求失败"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun errorMessage(resp: RetrofitResponse<*>, fallback: String): String {
        return try {
            val body = resp.errorBody()?.string()
            if (body.isNullOrBlank()) fallback
            else JSONObject(body).optString("error").ifBlank { fallback }
        } catch (e: Exception) {
            fallback
        }
    }

    private fun wsUrl(): String {
        val base = ApiService.BASE_URL
        val rest = base.removePrefix("https://").removePrefix("http://")
        val scheme = if (base.startsWith("https://")) "wss://" else "ws://"
        return scheme + rest + "api/v1/ws"
    }

    private fun connectWebSocket(token: String) {
        // 防止旧 token（绑定/重新登录前）的幽灵连接继续重连
        if (currentToken() != token) return
        reconnectJob?.cancel()
        ws?.close(1000, "reconnect")
        val request = Request.Builder()
            .url(wsUrl())
            .header("Authorization", "Bearer $token")
            .build()
        ws = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 连接就绪后拉一次消息，弥补错过实时推送的情况
                loadMessages()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWsMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scheduleReconnect(token)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scheduleReconnect(token)
            }
        })
    }

    private fun scheduleReconnect(token: String) {
        if (_auth.value !is AuthState.LoggedIn) return
        if (currentToken() != token) return
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            delay(5000)
            connectWebSocket(token)
        }
    }

    private fun handleWsMessage(text: String) {
        try {
            val json = JSONObject(text)
            val event = json.optString("event")
            val payload = json.optJSONObject("payload") ?: return
            when (event) {
                "anniversary_added", "anniversary_updated" -> {
                    val anniv = gson.fromJson(payload.toString(), Anniversary::class.java)
                    _anniversaries.value = listOf(anniv) + _anniversaries.value.filter { it.id != anniv.id }
                }
                "anniversary_deleted" -> {
                    val id = payload.optLong("id")
                    _anniversaries.value = _anniversaries.value.filter { it.id != id }
                }
                "memory_added" -> {
                    val mem = gson.fromJson(payload.toString(), Memory::class.java)
                    _memories.value = listOf(mem) + _memories.value.filter { it.id != mem.id }
                }
                "memory_deleted" -> {
                    val id = payload.optLong("id")
                    _memories.value = _memories.value.filter { it.id != id }
                }
                "pair_date_updated" -> {
                    val date = payload.optString("pair_date")
                    if (date.isNotBlank()) {
                        _couple.value = _couple.value?.copy(pairDate = date)
                    }
                }
                "message_new" -> {
                    val msg = gson.fromJson(payload.toString(), Message::class.java)
                    appendMessage(msg)
                }
            }
        } catch (e: Exception) {
            // 忽略无法解析的 WS 消息
        }
    }
}
