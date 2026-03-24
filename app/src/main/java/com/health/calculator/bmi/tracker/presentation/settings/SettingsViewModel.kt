package com.health.calculator.bmi.tracker.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.model.SettingsData
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val showClearSuccessMessage: Boolean = false,
    val showUnitSystemPicker: Boolean = false,
    val showThemePicker: Boolean = false
)

/**
 * ViewModel for the Settings screen.
 * Manages settings state, persistence, and data management actions.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(
        SettingsDataStore(application.applicationContext)
    )

    private val profileRepository = ProfileRepository(
        ProfileDataStore(application.applicationContext)
    )

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
            // TODO: Clear Room database history when implemented
            _uiState.update {
                it.copy(
                    showClearHistoryDialog = false,
                    showClearSuccessMessage = true
                )
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
            // Clear profile data
            profileRepository.clearProfile()
            // Clear settings (resets to defaults)
            settingsRepository.clearSettings()
            // TODO: Clear Room database when implemented

            _uiState.update {
                it.copy(
                    showClearAllDataDialog = false,
                    showClearSuccessMessage = true
                )
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            // TODO: Implement actual export when data layer is complete
            _uiState.update { it.copy(showExportSuccessMessage = true) }
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
}
