package com.loveever.app.api

import com.loveever.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("profile")
    suspend fun getProfile(): Response<ApiResponse<Any>>

    @GET("anniversaries")
    suspend fun getAnniversaries(): Response<ApiResponse<List<Anniversary>>>

    @POST("anniversaries")
    suspend fun createAnniversary(@Body req: CreateAnniversaryReq): Response<ApiResponse<Anniversary>>

    @PUT("anniversaries/{id}/pin")
    suspend fun togglePinAnniversary(@Path("id") id: Long): Response<ApiResponse<Anniversary>>

    @DELETE("anniversaries/{id}")
    suspend fun deleteAnniversary(@Path("id") id: Long): Response<ApiResponse<Any>>

    @PUT("couple/pair-date")
    suspend fun updatePairDate(@Body req: UpdatePairDateReq): Response<ApiResponse<Any>>

    @GET("memories")
    suspend fun getMemories(): Response<ApiResponse<List<Memory>>>

    @POST("memories")
    suspend fun createMemory(@Body req: CreateMemoryReq): Response<ApiResponse<Memory>>

    @DELETE("memories/{id}")
    suspend fun deleteMemory(@Path("id") id: Long): Response<ApiResponse<Any>>
}
