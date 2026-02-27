package dev.p4oc.domain.repository

import dev.p4oc.domain.model.ConnectionState
import dev.p4oc.domain.model.FileContent
import dev.p4oc.domain.model.FileItem
import dev.p4oc.domain.model.ServerConfig
import dev.p4oc.domain.model.UserConfirmation
import kotlinx.coroutines.flow.Flow

interface OpenCodeRepository {
    val connectionState: Flow<ConnectionState>
    val messages: Flow<String>
    val confirmations: Flow<UserConfirmation>

    suspend fun connect(config: ServerConfig)
    suspend fun disconnect()
    suspend fun sendMessage(content: String)
    suspend fun approveToolCall(toolCallId: String)
    suspend fun denyToolCall(toolCallId: String)
    suspend fun approveConfirmation(confirmationId: String)
    suspend fun denyConfirmation(confirmationId: String)
    suspend fun interrupt()
    suspend fun continueSession()
    
    suspend fun listFiles(path: String): List<FileItem>
    suspend fun readFile(path: String): FileContent
    
    fun isConnected(): Boolean
}
