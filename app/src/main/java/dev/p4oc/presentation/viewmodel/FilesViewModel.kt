package dev.p4oc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.p4oc.domain.model.ConnectionState
import dev.p4oc.domain.model.FileContent
import dev.p4oc.domain.model.FileItem
import dev.p4oc.domain.repository.OpenCodeRepository
import dev.p4oc.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val openCodeRepository: OpenCodeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val pathStack = mutableListOf<String>()

    val connectionState = openCodeRepository.connectionState

    init {
        viewModelScope.launch {
            openCodeRepository.connectionState.collect { state ->
                if (state.isConnected && _uiState.value.currentPath == "/") {
                    loadRoot()
                }
            }
        }
    }

    fun loadRoot() {
        pathStack.clear()
        if (openCodeRepository.isConnected()) {
            loadFiles("/")
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Not connected to server. Please connect first.",
                files = emptyList(),
                currentPath = "/"
            )
        }
    }

    fun navigateToFolder(path: String) {
        pathStack.add(path)
        loadFiles(path)
    }

    fun navigateBack(): Boolean {
        if (pathStack.isEmpty()) return false
        pathStack.removeLastOrNull()
        val parentPath = pathStack.lastOrNull() ?: "/"
        loadFiles(parentPath)
        return true
    }

    fun navigateUp(): Boolean {
        return navigateBack()
    }

    private fun loadFiles(path: String) {
        viewModelScope.launch {
            if (!openCodeRepository.isConnected()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not connected to server. Please connect first.",
                    files = emptyList()
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val files = openCodeRepository.listFiles(path)
                _uiState.value = _uiState.value.copy(
                    files = files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })),
                    currentPath = path,
                    isLoading = false,
                    selectedFile = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectFile(file: FileItem) {
        if (file.isDirectory) {
            navigateToFolder(file.path)
        } else {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoadingFile = true)
                try {
                    val content = openCodeRepository.readFile(file.path)
                    _uiState.value = _uiState.value.copy(
                        selectedFile = content,
                        isLoadingFile = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingFile = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun closeFileViewer() {
        _uiState.value = _uiState.value.copy(selectedFile = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FilesUiState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "/",
    val isLoading: Boolean = false,
    val isLoadingFile: Boolean = false,
    val selectedFile: FileContent? = null,
    val error: String? = null
)
