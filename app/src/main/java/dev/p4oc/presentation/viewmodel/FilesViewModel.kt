package dev.p4oc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.p4oc.domain.model.FileContent
import dev.p4oc.domain.model.FileItem
import dev.p4oc.domain.repository.OpenCodeRepository
import dev.p4oc.domain.repository.ProjectInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val openCodeRepository: OpenCodeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val pathStack = mutableListOf<String>()

    val connectionState = openCodeRepository.connectionState

    init {
        viewModelScope.launch {
            openCodeRepository.connectionState.collect { state ->
                if (state.isConnected && _uiState.value.projects.isEmpty()) {
                    loadProjects()
                }
            }
        }
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val projects = openCodeRepository.getProjects()
                _uiState.value = _uiState.value.copy(
                    projects = projects,
                    isLoading = false,
                    showProjects = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectProject(project: ProjectInfo) {
        _uiState.value = _uiState.value.copy(showProjects = false)
        navigateToFolder(project.directory)
    }

    fun loadRoot() {
        pathStack.clear()
        if (openCodeRepository.isConnected()) {
            loadProjects()
        }
    }

    fun navigateToFolder(path: String) {
        pathStack.clear()
        loadFiles(path)
    }

    fun navigateBack(): Boolean {
        if (_uiState.value.showProjects) return false
        if (pathStack.isEmpty()) {
            loadProjects()
            return true
        }
        pathStack.removeLastOrNull()
        val parentPath = pathStack.lastOrNull() ?: "/"
        loadFiles(parentPath)
        return true
    }

    fun navigateUp(): Boolean {
        if (_uiState.value.showProjects) return false
        if (pathStack.isEmpty()) {
            loadProjects()
            return true
        }
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
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, showProjects = false)
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
            pathStack.add(_uiState.value.currentPath)
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

    fun showProjects() {
        loadProjects()
    }
}

data class FilesUiState(
    val files: List<FileItem> = emptyList(),
    val projects: List<ProjectInfo> = emptyList(),
    val currentPath: String = "/",
    val isLoading: Boolean = false,
    val isLoadingFile: Boolean = false,
    val selectedFile: FileContent? = null,
    val error: String? = null,
    val showProjects: Boolean = false
)
