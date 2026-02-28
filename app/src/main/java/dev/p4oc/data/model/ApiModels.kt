package dev.p4oc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val healthy: Boolean,
    val version: String
)

@Serializable
data class CreateSessionRequest(
    val title: String? = null
)

@Serializable
data class SessionResponse(
    val id: String,
    @SerialName("project_path")
    val projectPath: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("is_active")
    val isActive: Boolean = true,
    val title: String = ""
)

@Serializable
data class SessionsListResponse(
    val info: List<SessionDto>
)

@Serializable
data class ChatRequest(
    val message: String,
    val parts: List<Part>? = null,
    val agent: String? = null,
    val model: String? = null
)

@Serializable
data class Part(
    val type: String,
    val text: String? = null
)

@Serializable
data class MessageResponse(
    val info: MessageDto,
    val parts: List<MessagePart>
)

@Serializable
data class MessageDto(
    val id: String,
    val role: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: Long
)

@Serializable
data class MessagePart(
    val type: String,
    val text: String? = null,
    @SerialName("tool_name")
    val toolName: String? = null,
    val args: Map<String, String>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null
)

@Serializable
data class PermissionRequest(
    val response: String,
    val remember: Boolean = false
)

@Serializable
data class FilesResponse(
    val info: List<FileEntryDto>
)

@Serializable
data class FileContentResponse(
    val info: FileContentDto
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
    val isActive: Boolean = true,
    val title: String = ""
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
