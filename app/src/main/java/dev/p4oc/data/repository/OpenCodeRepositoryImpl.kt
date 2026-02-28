package dev.p4oc.data.repository

import android.util.Base64
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.p4oc.data.api.OpenCodeApi
import dev.p4oc.data.api.SseClient
import dev.p4oc.data.model.*
import dev.p4oc.domain.model.*
import dev.p4oc.domain.repository.OpenCodeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCodeRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val sseClient: SseClient
) : OpenCodeRepository {

    private val _connectionState = MutableStateFlow(ConnectionState(false, false))
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(replay = 0)
    override val messages: Flow<String> = _messages.asSharedFlow()

    private val _confirmations = MutableSharedFlow<UserConfirmation>(replay = 0)
    override val confirmations: Flow<UserConfirmation> = _confirmations.asSharedFlow()

    private var currentConfig: ServerConfig? = null
    private var currentApi: OpenCodeApi? = null
    private var streamingJob: Job? = null
    private var currentSessionId: String? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun createApi(baseUrl: String): OpenCodeApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(OpenCodeApi::class.java)
    }

    override suspend fun connect(config: ServerConfig) {
        currentConfig = config
        
        try {
            _connectionState.value = ConnectionState(false, true)

            currentApi = createApi(config.baseUrl)

            // Check health first
            try {
                val health = currentApi?.healthCheck()
                if (health?.healthy != true) {
                    throw Exception("Server not healthy")
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState(false, false, "Cannot connect to server: ${e.message}")
                return
            }

            // Get or create session
            try {
                val sessions = currentApi?.listSessions()
                val activeSession = sessions?.info?.firstOrNull { it.isActive }
                currentSessionId = activeSession?.id ?: createNewSession()
            } catch (e: Exception) {
                currentSessionId = createNewSession()
            }

            // Start SSE stream
            streamingJob?.cancel()
            streamingJob = scope.launch {
                try {
                    sseClient.connect(
                        url = config.baseUrl,
                        username = config.username,
                        password = config.password,
                        onMessage = { data ->
                            scope.launch {
                                _messages.emit(data)
                                parseAndEmitConfirmations(data)
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                _connectionState.value = ConnectionState(
                                    isConnected = false,
                                    isConnecting = false,
                                    error = error.message
                                )
                            }
                        }
                    ).collect()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState(
                        isConnected = false,
                        isConnecting = false,
                        error = e.message
                    )
                }
            }

            _connectionState.value = ConnectionState(true, false)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState(false, false, e.message)
        }
    }

    private suspend fun createNewSession(): String {
        val response = currentApi?.createSession(CreateSessionRequest())
        return response?.id ?: throw Exception("Failed to create session")
    }

    private suspend fun parseAndEmitConfirmations(data: String) {
        try {
            if (data.contains("permission")) {
                val confirmation = UserConfirmation(
                    id = "pending",
                    message = "Permission request",
                    toolCallId = "pending"
                )
                _confirmations.emit(confirmation)
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
    }

    private fun parseSseEvent(data: String): StreamEvent? {
        return try {
            json.decodeFromString<StreamEvent>(data)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun disconnect() {
        streamingJob?.cancel()
        streamingJob = null
        sseClient.disconnect()
        currentConfig = null
        currentApi = null
        currentSessionId = null
        _connectionState.value = ConnectionState(false, false)
    }

    override suspend fun sendMessage(content: String) {
        currentConfig?.let { config ->
            val sessionId = currentSessionId ?: return
            try {
                val request = ChatRequest(
                    message = content,
                    parts = listOf(Part(type = "text", text = content))
                )
                currentApi?.sendMessage(sessionId, request)
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(
                    error = e.message
                )
            }
        }
    }

    override suspend fun approveToolCall(toolCallId: String) {
        currentSessionId?.let { sessionId ->
            try {
                currentApi?.respondToPermission(sessionId, toolCallId, PermissionRequest("allow"))
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(error = e.message)
            }
        }
    }

    override suspend fun denyToolCall(toolCallId: String) {
        currentSessionId?.let { sessionId ->
            try {
                currentApi?.respondToPermission(sessionId, toolCallId, PermissionRequest("deny"))
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(error = e.message)
            }
        }
    }

    override suspend fun approveConfirmation(confirmationId: String) {
        currentSessionId?.let { sessionId ->
            try {
                currentApi?.respondToPermission(sessionId, confirmationId, PermissionRequest("allow"))
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(error = e.message)
            }
        }
    }

    override suspend fun denyConfirmation(confirmationId: String) {
        currentSessionId?.let { sessionId ->
            try {
                currentApi?.respondToPermission(sessionId, confirmationId, PermissionRequest("deny"))
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(error = e.message)
            }
        }
    }

    override suspend fun interrupt() {
        currentSessionId?.let { sessionId ->
            try {
                currentApi?.abortSession(sessionId)
            } catch (e: Exception) {
                _connectionState.value = _connectionState.value.copy(error = e.message)
            }
        }
    }

    override suspend fun continueSession() {
        // Not needed with new API
    }

    override suspend fun listFiles(path: String): List<FileItem> {
        return try {
            val response = currentApi?.listFiles(path)
            response?.info?.map { dto ->
                FileItem(
                    name = dto.name,
                    path = dto.path,
                    isDirectory = dto.isDirectory,
                    size = dto.size,
                    extension = dto.extension
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun readFile(path: String): FileContent {
        val response = currentApi?.getFileContent(path)
        return response?.info?.let { dto ->
            FileContent(
                path = dto.path,
                content = dto.content,
                lineCount = dto.lineCount,
                language = dto.language
            )
        } ?: FileContent(path, "", 0, "")
    }

    override fun isConnected(): Boolean = sseClient.isConnected()
}
