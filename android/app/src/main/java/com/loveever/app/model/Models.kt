package com.loveever.app.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long = 0,
    val username: String = "",
    @SerializedName("display_name") val displayName: String = "",
    @SerializedName("avatar_url") val avatarUrl: String = "",
    @SerializedName("couple_id") val coupleId: Long? = null,
    val role: String = ""
)

data class Couple(
    val id: Long = 0,
    @SerializedName("invite_code") val inviteCode: String = "",
    @SerializedName("pair_date") val pairDate: String = "2023-05-20",
    @SerializedName("created_at") val createdAt: String = ""
)

data class Anniversary(
    val id: Long = 0,
    @SerializedName("couple_id") val coupleId: Long = 0,
    val title: String = "",
    @SerializedName("target_date") val targetDate: String = "",
    @SerializedName("is_pinned") val isPinned: Boolean = false,
    val icon: String = "heart"
)

data class Memory(
    val id: Long = 0,
    @SerializedName("couple_id") val coupleId: Long = 0,
    val title: String = "",
    val content: String = "",
    @SerializedName("memory_date") val memoryDate: String = "",
    @SerializedName("image_url") val imageUrl: String = ""
)

data class Message(
    val id: Long = 0,
    @SerializedName("couple_id") val coupleId: Long = 0,
    @SerializedName("sender_id") val senderId: Long = 0,
    val type: String = "text", // text | image | voice
    val content: String = "",
    val duration: Int = 0,
    @SerializedName("created_at") val createdAt: String = ""
)

data class SendMessageReq(
    val type: String,
    val content: String,
    val duration: Int = 0
)

data class UploadResponse(
    val url: String? = null,
    val error: String? = null
)

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("pair_date") val pairDate: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class PairRequest(
    @SerializedName("invite_code") val inviteCode: String
)

data class AuthResponse(
    val token: String? = null,
    val user: User? = null,
    val couple: Couple? = null,
    @SerializedName("invite_code") val inviteCode: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class PairResponse(
    val message: String? = null,
    val couple: Couple? = null,
    val token: String? = null,
    val error: String? = null
)

data class ProfileResponse(
    val user: User? = null,
    val couple: Couple? = null,
    @SerializedName("partner_name") val partnerName: String? = null,
    @SerializedName("partner_avatar") val partnerAvatar: String? = null,
    val error: String? = null
)

data class DataResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

data class CreateAnniversaryReq(
    val title: String,
    @SerializedName("target_date") val targetDate: String,
    @SerializedName("is_pinned") val isPinned: Boolean = false,
    val icon: String = "heart"
)

data class CreateMemoryReq(
    val title: String,
    val content: String,
    @SerializedName("memory_date") val memoryDate: String,
    @SerializedName("image_url") val imageUrl: String = ""
)

data class UpdatePairDateReq(
    @SerializedName("pair_date") val pairDate: String
)
