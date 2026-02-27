package dev.p4oc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.p4oc.domain.model.ServerConfig
import dev.p4oc.domain.repository.SettingsRepository
import dev.p4oc.domain.repository.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.serverConfig,
                settingsRepository.theme,
                settingsRepository.connectionTimeout
            ) { config, theme, timeout ->
                SettingsUiState(
                    serverConfig = config,
                    theme = theme,
                    connectionTimeout = timeout,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateHost(host: String) {
        _uiState.value = _uiState.value.copy(
            serverConfig = _uiState.value.serverConfig?.copy(host = host)
                ?: ServerConfig(host = host, port = 4096)
        )
    }

    fun updatePort(port: String) {
        val portInt = port.toIntOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            serverConfig = _uiState.value.serverConfig?.copy(port = portInt)
                ?: ServerConfig(host = "", port = portInt)
        )
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            serverConfig = _uiState.value.serverConfig?.copy(username = username)
                ?: ServerConfig(host = "", port = 4096, username = username)
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            serverConfig = _uiState.value.serverConfig?.copy(password = password)
                ?: ServerConfig(host = "", port = 4096, password = password)
        )
    }

    fun updateFullUrl(url: String) {
        val currentConfig = _uiState.value.serverConfig
        _uiState.value = _uiState.value.copy(
            serverConfig = if (currentConfig != null) {
                currentConfig.copy(fullUrl = url, useUrl = true)
            } else {
                ServerConfig(host = "", port = 4096, fullUrl = url, useUrl = true)
            }
        )
    }

    fun updateUseUrl(useUrl: Boolean) {
        val currentConfig = _uiState.value.serverConfig
        _uiState.value = _uiState.value.copy(
            serverConfig = if (currentConfig != null) {
                currentConfig.copy(useUrl = useUrl)
            } else {
                ServerConfig(host = "", port = 4096, useUrl = useUrl)
            }
        )
    }

    fun saveServerConfig() {
        viewModelScope.launch {
            val currentConfig = _uiState.value.serverConfig
            val configToSave = currentConfig ?: ServerConfig(
                host = "",
                port = 4096,
                fullUrl = "",
                useUrl = false
            )
            
            // Always save, ignore isConfigured check
            settingsRepository.saveServerConfig(configToSave)
            
            // Show saved feedback
            _uiState.value = _uiState.value.copy(isSaved = true)
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(isSaved = false)
        }
    }

    fun clearServerConfig() {
        viewModelScope.launch {
            settingsRepository.clearServerConfig()
        }
    }

    fun updateTheme(theme: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun updateConnectionTimeout(timeout: Int) {
        viewModelScope.launch {
            settingsRepository.saveConnectionTimeout(timeout)
        }
    }
}

data class SettingsUiState(
    val serverConfig: ServerConfig? = null,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val connectionTimeout: Int = 30,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)
