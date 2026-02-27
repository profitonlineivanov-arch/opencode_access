package dev.p4oc.domain.model

sealed class Message {
    abstract val id: String
    abstract val timestamp: Long

    data class UserMessage(
        override val id: String,
        override val timestamp: Long,
        val content: String
    ) : Message()

    data class AssistantMessage(
        override val id: String,
        override val timestamp: Long,
        val content: String,
        val isStreaming: Boolean = false
    ) : Message()

    data class SystemMessage(
        override val id: String,
        override val timestamp: Long,
        val content: String
    ) : Message()

    data class ToolCallMessage(
        override val id: String,
        override val timestamp: Long,
        val toolName: String,
        val args: Map<String, String>,
        val status: ToolCallStatus = ToolCallStatus.PENDING
    ) : Message()

    data class DiffMessage(
        override val id: String,
        override val timestamp: Long,
        val filePath: String,
        val additions: Int,
        val deletions: Int,
        val diff: String
    ) : Message()
}

enum class ToolCallStatus {
    PENDING,
    APPROVED,
    DENIED,
    EXECUTING,
    COMPLETED,
    ERROR
}
