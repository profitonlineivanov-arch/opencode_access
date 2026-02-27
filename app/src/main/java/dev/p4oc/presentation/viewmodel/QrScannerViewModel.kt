package dev.p4oc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.p4oc.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun saveUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.saveUrlOnly(url)
        }
    }
}
