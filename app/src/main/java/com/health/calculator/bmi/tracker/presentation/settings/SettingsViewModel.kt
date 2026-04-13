package com.health.calculator.bmi.tracker.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.export.DataExportManager
import com.health.calculator.bmi.tracker.data.export.ExportConfig
import com.health.calculator.bmi.tracker.data.export.ExportFormat
import com.health.calculator.bmi.tracker.data.export.ExportScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.SettingsData
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the Settings screen.
 * Mirrors SettingsData plus UI-specific states like dialogs.
 */
data class SettingsUiState(
    // ── Settings Values ───────────────────────────────────────────────
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val remindersEnabled: Boolean = false,
    val waterReminderEnabled: Boolean = false,
    val weightReminderEnabled: Boolean = false,

    // ── UI State ──────────────────────────────────────────────────────
    val isLoading: Boolean = true,
    val showClearHistoryDialog: Boolean = false,
    val showClearAllDataDialog: Boolean = false,
    val showExportSuccessMessage: Boolean = false,
    val exportStatusMessage: String? = null,
    val showClearSuccessMessage: Boolean = false,
    val showUnitSystemPicker: Boolean = false,
    val showThemePicker: Boolean = false
)

/**
 * ViewModel for the Settings screen.
 * Manages settings state, persistence, and data management actions.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val appDatabase = AppDatabase.getDatabase(appContext)

    private val settingsRepository = SettingsRepository(
        SettingsDataStore(appContext)
    )

    private val profileRepository = ProfileRepository(
        ProfileDataStore(appContext)
    )
    private val historyRepository = HistoryRepository(appDatabase.historyDao())
    private val exportManager = DataExportManager.getInstance(appContext)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    // ─── Load Settings ────────────────────────────────────────────────────

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        unitSystem = settings.unitSystem,
                        themeMode = settings.themeMode,
                        remindersEnabled = settings.remindersEnabled,
                        waterReminderEnabled = settings.waterReminderEnabled,
                        weightReminderEnabled = settings.weightReminderEnabled,
                        isLoading = false
                    )
                }
            }
        }
    }

    // ─── Theme Mode ───────────────────────────────────────────────────────

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
            _uiState.update {
                it.copy(themeMode = mode, showThemePicker = false)
            }
        }
    }

    fun showThemePicker() {
        _uiState.update { it.copy(showThemePicker = true) }
    }

    fun hideThemePicker() {
        _uiState.update { it.copy(showThemePicker = false) }
    }

    // ─── Unit System ──────────────────────────────────────────────────────

    fun updateUnitSystem(system: UnitSystem) {
        viewModelScope.launch {
            settingsRepository.updateUnitSystem(system)
            _uiState.update {
                it.copy(unitSystem = system, showUnitSystemPicker = false)
            }
        }
    }

    fun showUnitSystemPicker() {
        _uiState.update { it.copy(showUnitSystemPicker = true) }
    }

    fun hideUnitSystemPicker() {
        _uiState.update { it.copy(showUnitSystemPicker = false) }
    }

    // ─── Notifications ────────────────────────────────────────────────────

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            // If master toggle is turned off, disable all sub-toggles
            if (!enabled) {
                settingsRepository.updateReminderSetting(
                    remindersEnabled = false,
                    waterReminder = false,
                    weightReminder = false
                )
            } else {
                settingsRepository.updateReminderSetting(remindersEnabled = true)
            }
        }
    }

    fun toggleWaterReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReminderSetting(waterReminder = enabled)
        }
    }

    fun toggleWeightReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReminderSetting(weightReminder = enabled)
        }
    }

    // ─── Data Management ──────────────────────────────────────────────────

    fun showClearHistoryDialog() {
        _uiState.update { it.copy(showClearHistoryDialog = true) }
    }

    fun hideClearHistoryDialog() {
        _uiState.update { it.copy(showClearHistoryDialog = false) }
    }

    fun confirmClearHistory() {
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    historyRepository.clearAllHistory()
                }
            }

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        showClearHistoryDialog = false,
                        showClearSuccessMessage = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        showClearHistoryDialog = false,
                        exportStatusMessage = "Failed to clear history: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun showClearAllDataDialog() {
        _uiState.update { it.copy(showClearAllDataDialog = true) }
    }

    fun hideClearAllDataDialog() {
        _uiState.update { it.copy(showClearAllDataDialog = false) }
    }

    fun confirmClearAllData() {
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    appDatabase.clearAllTables()
                }
                profileRepository.clearProfile()
                settingsRepository.clearSettings()
            }

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        showClearAllDataDialog = false,
                        showClearSuccessMessage = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        showClearAllDataDialog = false,
                        exportStatusMessage = "Failed to clear all data: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) {
                historyRepository.getAllEntries().first().map { it.toDisplayEntry() }
            }
            if (entries.isEmpty()) {
                _uiState.update { it.copy(exportStatusMessage = "No data available to export") }
                return@launch
            }

            exportManager.exportData(
                entries = entries,
                config = ExportConfig(
                    format = ExportFormat.JSON,
                    scope = ExportScope.ALL
                )
            )
            val progress = exportManager.exportProgress.value
            if (progress.isComplete && progress.resultUri != null) {
                exportManager.shareFile(progress.resultUri, ExportFormat.JSON)
                _uiState.update { it.copy(showExportSuccessMessage = true) }
            } else {
                _uiState.update {
                    it.copy(
                        exportStatusMessage = progress.error ?: "Export failed. Please try again."
                    )
                }
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update {
            it.copy(
                showExportSuccessMessage = false,
                showClearSuccessMessage = false
            )
        }
    }

    fun dismissExportStatusMessage() {
        _uiState.update { it.copy(exportStatusMessage = null) }
    }
}
