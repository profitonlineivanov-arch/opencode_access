package dev.p4oc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.p4oc.domain.model.*
import dev.p4oc.domain.repository.OpenCodeRepository
import dev.p4oc.domain.repository.SettingsRepository
import dev.p4oc.domain.repository.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openCodeRepository: OpenCodeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val connectionState = openCodeRepository.connectionState
    val messages = openCodeRepository.messages
    val confirmations = openCodeRepository.confirmations

    // Server config from settings
    val serverConfig: StateFlow<ServerConfig?> = settingsRepository.serverConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        observeMessages()
        observeConfirmations()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            messages.collect { event ->
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(
                    Message.AssistantMessage(
                        id = System.currentTimeMillis().toString(),
                        timestamp = System.currentTimeMillis(),
                        content = event,
                        isStreaming = true
                    )
                )
                _uiState.value = _uiState.value.copy(
                    messages = currentMessages,
                    isLoading = false
                )
            }
        }
    }

    private fun observeConfirmations() {
        viewModelScope.launch {
            confirmations.collect { confirmation ->
                _uiState.value = _uiState.value.copy(
                    pendingConfirmations = _uiState.value.pendingConfirmations + confirmation
                )
            }
        }
    }

    fun connect(config: ServerConfig) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            settingsRepository.saveServerConfig(config)
            openCodeRepository.connect(config)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            openCodeRepository.disconnect()
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(
                Message.UserMessage(
                    id = System.currentTimeMillis().toString(),
                    timestamp = System.currentTimeMillis(),
                    content = content
                )
            )
            _uiState.value = _uiState.value.copy(
                messages = currentMessages,
                inputText = "",
                isLoading = true
            )
            openCodeRepository.sendMessage(content)
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun approveToolCall(toolCallId: String) {
        viewModelScope.launch {
            openCodeRepository.approveToolCall(toolCallId)
            removePendingConfirmation(toolCallId)
        }
    }

    fun denyToolCall(toolCallId: String) {
        viewModelScope.launch {
            openCodeRepository.denyToolCall(toolCallId)
            removePendingConfirmation(toolCallId)
        }
    }

    fun approveConfirmation(confirmationId: String) {
        viewModelScope.launch {
            openCodeRepository.approveConfirmation(confirmationId)
            removePendingConfirmation(confirmationId)
        }
    }

    fun denyConfirmation(confirmationId: String) {
        viewModelScope.launch {
            openCodeRepository.denyConfirmation(confirmationId)
            removePendingConfirmation(confirmationId)
        }
    }

    private fun removePendingConfirmation(id: String) {
        _uiState.value = _uiState.value.copy(
            pendingConfirmations = _uiState.value.pendingConfirmations.filter { it.id != id }
        )
    }

    fun interrupt() {
        viewModelScope.launch {
            openCodeRepository.interrupt()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun continueSession() {
        viewModelScope.launch {
            openCodeRepository.continueSession()
        }
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isConnecting: Boolean = false,
    val pendingConfirmations: List<UserConfirmation> = emptyList(),
    val error: String? = null
)
