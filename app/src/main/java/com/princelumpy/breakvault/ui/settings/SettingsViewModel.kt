package com.princelumpy.breakvault.ui.settings

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.repository.SettingsRepository
import com.princelumpy.breakvault.data.service.export.model.AppDataExport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

// Sealed class to represent specific, type-safe UI events
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun exportData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val appData = settingsRepository.getAppDataForExport()
                val jsonString = json.encodeToString(AppDataExport.serializer(), appData)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Log.i("SettingsViewModel", "Data exported to $uri")
                _uiEvent.emit(
                    UiEvent.ShowSnackbar(
                        context.getString(
                            R.string.settings_export_success_snackbar,
                            uri.toString()
                        )
                    )
                )
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error exporting data", e)
                val errorMessage = context.getString(
                    R.string.settings_action_failed_snackbar,
                    context.getString(R.string.settings_export_action_label),
                    e.message ?: "Unknown error"
                )
                _uiEvent.emit(UiEvent.ShowSnackbar(errorMessage))
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use {
                    BufferedReader(InputStreamReader(it)).readText()
                }

                if (jsonString != null) {
                    val appData = json.decodeFromString<AppDataExport>(jsonString)
                    settingsRepository.importAppData(appData)
                    Log.i("SettingsViewModel", "Data imported from $uri")
                    _uiEvent.emit(UiEvent.ShowSnackbar(context.getString(R.string.settings_import_success_snackbar)))
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar(context.getString(R.string.settings_import_error_reading_file_snackbar)))
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error importing data", e)
                val errorMessage = context.getString(
                    R.string.settings_action_failed_snackbar,
                    context.getString(R.string.settings_import_action_label),
                    e.message ?: "Unknown error"
                )
                _uiEvent.emit(UiEvent.ShowSnackbar(errorMessage))
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                settingsRepository.resetDatabase()
                _uiEvent.emit(UiEvent.ShowSnackbar(context.getString(R.string.settings_database_reset_success_snackbar)))
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error resetting database", e)
                _uiEvent.emit(UiEvent.ShowSnackbar("Database reset failed."))
            }
        }
    }
}
