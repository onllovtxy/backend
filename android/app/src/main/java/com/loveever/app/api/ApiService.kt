package com.loveever.app.api

import com.loveever.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    companion object {
        // 生产环境后端地址（备案接入完成前使用 http + 20119 端口）
        const val BASE_URL = "http://8.134.149.235:20119/"
    }

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/v1/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @POST("api/v1/auth/pair")
    suspend fun pairCouple(
        @Header("Authorization") token: String,
        @Body request: PairRequest
    ): Response<PairResponse>

    @GET("api/v1/anniversaries")
    suspend fun getAnniversaries(@Header("Authorization") token: String): Response<DataResponse<List<Anniversary>>>

    @POST("api/v1/anniversaries")
    suspend fun createAnniversary(
        @Header("Authorization") token: String,
        @Body request: CreateAnniversaryReq
    ): Response<DataResponse<Anniversary>>

    @PUT("api/v1/anniversaries/{id}/pin")
    suspend fun togglePin(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<DataResponse<Anniversary>>

    @DELETE("api/v1/anniversaries/{id}")
    suspend fun deleteAnniversary(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<DataResponse<Any>>

    @PUT("api/v1/couple/pair-date")
    suspend fun updatePairDate(
        @Header("Authorization") token: String,
        @Body request: UpdatePairDateReq
    ): Response<DataResponse<Any>>

    @GET("api/v1/memories")
    suspend fun getMemories(@Header("Authorization") token: String): Response<DataResponse<List<Memory>>>

    @POST("api/v1/memories")
    suspend fun createMemory(
        @Header("Authorization") token: String,
        @Body request: CreateMemoryReq
    ): Response<DataResponse<Memory>>

    @DELETE("api/v1/memories/{id}")
    suspend fun deleteMemory(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<DataResponse<Any>>
}
