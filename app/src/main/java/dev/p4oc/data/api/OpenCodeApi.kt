package dev.p4oc.data.api

import dev.p4oc.data.model.*
import retrofit2.http.*

interface OpenCodeApi {

    @POST("api/chat")
    suspend fun sendChat(@Body request: ChatRequest): ApiResponse<Unit>

    @POST("api/toolcall")
    suspend fun respondToToolCall(@Body request: ToolCallRequest): ApiResponse<Unit>

    @POST("api/confirmation")
    suspend fun respondToConfirmation(@Body request: ConfirmationRequest): ApiResponse<Unit>

    @POST("api/session/interrupt")
    suspend fun interruptSession(): ApiResponse<Unit>

    @POST("api/session/continue")
    suspend fun continueSession(): ApiResponse<Unit>

    @GET("api/files")
    suspend fun listFiles(@Query("path") path: String): ApiResponse<List<FileEntryDto>>

    @GET("api/file/content")
    suspend fun getFileContent(@Query("path") path: String): ApiResponse<FileContentDto>

    @GET("api/session")
    suspend fun getCurrentSession(): ApiResponse<SessionDto?>

    @GET("api/confirmations")
    suspend fun getPendingConfirmations(): ApiResponse<List<UserConfirmationDto>>
}
