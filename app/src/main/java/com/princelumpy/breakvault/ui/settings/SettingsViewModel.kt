package com.princelumpy.breakvault.ui.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.repository.SettingsRepository
import com.princelumpy.breakvault.data.service.export.model.AppDataExport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

// The final, combined state for the UI.
data class SettingsUiState(
    val showResetConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() { // No longer an AndroidViewModel

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val appData = settingsRepository.getAppDataForExport()
                val jsonString = json.encodeToString(AppDataExport.serializer(), appData)

                settingsRepository.writeDataToUri(uri, jsonString)

                Log.i("SettingsViewModel", "Data exported to $uri")
                _uiState.update { it.copy(snackbarMessage = "Export successful!") }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error exporting data", e)
                _uiState.update { it.copy(snackbarMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = settingsRepository.readDataFromUri(uri)

                if (jsonString != null) {
                    val appData = json.decodeFromString<AppDataExport>(jsonString)
                    settingsRepository.importAppData(appData)
                    Log.i("SettingsViewModel", "Data imported from $uri")
                    _uiState.update { it.copy(snackbarMessage = "Import successful!") }
                } else {
                    _uiState.update { it.copy(snackbarMessage = "Failed to read import file.") }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error importing data", e)
                _uiState.update { it.copy(snackbarMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun onResetDatabaseClicked() {
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    fun onResetDatabaseConfirm() {
        viewModelScope.launch {
            try {
                settingsRepository.resetDatabase()
                _uiState.update {
                    it.copy(
                        showResetConfirmDialog = false,
                        snackbarMessage = "Database reset successfully."
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error resetting database", e)
                _uiState.update {
                    it.copy(
                        showResetConfirmDialog = false,
                        snackbarMessage = "Database reset failed."
                    )
                }
            }
        }
    }

    fun onResetDatabaseDismiss() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
