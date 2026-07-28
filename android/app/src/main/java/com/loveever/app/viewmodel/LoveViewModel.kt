package com.loveever.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loveever.app.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LoveViewModel : ViewModel() {
    private val _couple = MutableStateFlow(
        Couple(id = 1, inviteCode = "LOVE-520", pairDate = "2023-05-20")
    )
    val couple: StateFlow<Couple> = _couple.asStateFlow()

    private val _user = MutableStateFlow(
        User(displayName = "宝贝", avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150")
    )
    val user: StateFlow<User> = _user.asStateFlow()

    private val _partnerName = MutableStateFlow("亲爱的")
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    private val _anniversaries = MutableStateFlow<List<Anniversary>>(
        listOf(
            Anniversary(1, 1, "相爱在一起", "2023-05-20", isPinned = true, icon = "heart"),
            Anniversary(2, 1, "相识 1000 天纪念", "2026-02-14", isPinned = true, icon = "star"),
            Anniversary(3, 1, "宝贝的生日", "2026-10-24", isPinned = false, icon = "cake")
        )
    )
    val anniversaries: StateFlow<List<Anniversary>> = _anniversaries.asStateFlow()

    private val _memories = MutableStateFlow<List<Memory>>(
        listOf(
            Memory(
                1, 1, "第一次看海 🌊",
                "青岛的海风真的很温柔，踩在沙滩上夕阳把我们的影子拉得很长很长。",
                "2023-08-15",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"
            )
        )
    )
    val memories: StateFlow<List<Memory>> = _memories.asStateFlow()

    fun updatePairDate(newDate: String) {
        _couple.value = _couple.value.copy(pairDate = newDate)
    }

    fun addAnniversary(title: String, date: String, isPinned: Boolean) {
        val newAnniv = Anniversary(
            id = System.currentTimeMillis(),
            coupleId = _couple.value.id,
            title = title,
            targetDate = date,
            isPinned = isPinned,
            icon = "heart"
        )
        _anniversaries.value = listOf(newAnniv) + _anniversaries.value
    }

    fun togglePin(id: Long) {
        _anniversaries.value = _anniversaries.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun deleteAnniversary(id: Long) {
        _anniversaries.value = _anniversaries.value.filter { it.id != id }
    }

    fun addMemory(title: String, content: String, date: String, imageUrl: String) {
        val newMemory = Memory(
            id = System.currentTimeMillis(),
            coupleId = _couple.value.id,
            title = title,
            content = content,
            memoryDate = date,
            imageUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=600" else imageUrl
        )
        _memories.value = listOf(newMemory) + _memories.value
    }

    fun deleteMemory(id: Long) {
        _memories.value = _memories.value.filter { it.id != id }
    }

    fun calculateDaysCount(): Long {
        return try {
            val start = LocalDate.parse(_couple.value.pairDate)
            val now = LocalDate.now()
            ChronoUnit.DAYS.between(start, now).coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }
}
