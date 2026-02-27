package dev.p4oc.domain.model

data class Session(
    val id: String,
    val projectPath: String,
    val createdAt: Long,
    val isActive: Boolean = true
)

data class UserConfirmation(
    val id: String,
    val message: String,
    val toolCallId: String
)

data class ConnectionState(
    val isConnected: Boolean,
    val isConnecting: Boolean,
    val error: String? = null
)
