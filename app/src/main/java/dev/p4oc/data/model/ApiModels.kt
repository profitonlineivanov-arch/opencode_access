package dev.p4oc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class ChatRequest(
    val message: String,
    val continueSession: Boolean = false
)

@Serializable
data class ToolCallRequest(
    val toolCallId: String,
    val approved: Boolean
)

@Serializable
data class ConfirmationRequest(
    val confirmationId: String,
    val approved: Boolean
)

@Serializable
data class FileEntryDto(
    val name: String,
    val path: String,
    @SerialName("is_directory")
    val isDirectory: Boolean,
    val size: Long = 0,
    val extension: String = ""
)

@Serializable
data class FileContentDto(
    val path: String,
    val content: String,
    @SerialName("line_count")
    val lineCount: Int,
    val language: String = ""
)

@Serializable
data class SessionDto(
    val id: String,
    @SerialName("project_path")
    val projectPath: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class UserConfirmationDto(
    val id: String,
    val message: String,
    @SerialName("tool_call_id")
    val toolCallId: String
)

@Serializable
data class StreamEvent(
    val type: String,
    val data: String
)
