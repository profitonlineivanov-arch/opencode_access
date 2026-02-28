package dev.p4oc.data.api

import dev.p4oc.data.model.*
import retrofit2.http.*

interface OpenCodeApi {

    @GET("global/health")
    suspend fun healthCheck(): HealthResponse

    @POST("session")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    @GET("session")
    suspend fun listSessions(): SessionsListResponse

    @GET("session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): SessionResponse

    @POST("session/{sessionId}/message")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: String,
        @Body request: ChatRequest
    ): MessageResponse

    @POST("session/{sessionId}/prompt_async")
    suspend fun sendMessageAsync(
        @Path("sessionId") sessionId: String,
        @Body request: ChatRequest
    )

    @POST("session/{sessionId}/abort")
    suspend fun abortSession(@Path("sessionId") sessionId: String)

    @POST("session/{sessionId}/permissions/{permissionId}")
    suspend fun respondToPermission(
        @Path("sessionId") sessionId: String,
        @Path("permissionId") permissionId: String,
        @Body request: PermissionRequest
    )

    @GET("file")
    suspend fun listFiles(@Query("path") path: String): List<FileEntryDto>

    @GET("file/content")
    suspend fun getFileContent(@Query("path") path: String): FileContentDto
}
