package com.health.calculator.bmi.tracker.presentation.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectManager
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectPermissionPolicy
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.export.DataExportManager
import com.health.calculator.bmi.tracker.data.export.ExportConfig
import com.health.calculator.bmi.tracker.data.export.ExportFormat
import com.health.calculator.bmi.tracker.data.export.ExportScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.management.FullAppDataResetter
import com.health.calculator.bmi.tracker.data.model.SettingsData
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.SettingsRepository
import com.health.calculator.bmi.tracker.data.repository.StepHistoryRepository
import com.health.calculator.bmi.tracker.data.model.StepHistoryEntry
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalytics
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler
import com.health.calculator.bmi.tracker.notifications.WeightReminderManager
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
    val productAnalyticsEnabled: Boolean = false,

    // ── UI State ──────────────────────────────────────────────────────
    val isLoading: Boolean = true,
    val showClearHistoryDialog: Boolean = false,
    val showClearAllDataDialog: Boolean = false,
    val showExportSuccessMessage: Boolean = false,
    val exportStatusMessage: String? = null,
    val showClearSuccessMessage: Boolean = false,
    val showUnitSystemPicker: Boolean = false,
    val showThemePicker: Boolean = false,

    // ── Health Connect State ──────────────────────────────────────────
    val isHealthConnectSupported: Boolean = false,
    val isHealthConnectConnected: Boolean = false,
    val isHealthConnectWeightConnected: Boolean = false,
    val healthConnectSteps: Long? = null,
    val healthConnectWeightKg: Double? = null,
    val healthConnectStepHistory: List<StepHistoryEntry> = emptyList(),
    val healthConnectSyncStatus: String? = null
)

/**
 * ViewModel for the Settings screen.
 * Manages settings state, persistence, and data management actions.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    val healthConnectManager: HealthConnectManager,
    private val settingsDataStore: SettingsDataStore,
    private val productAnalytics: ProductAnalytics,
    private val weightReminderManager: WeightReminderManager,
    private val stepHistoryRepository: StepHistoryRepository
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val waterReminderPreferences = WaterReminderPreferences(appContext)
    private val waterReminderScheduler = WaterReminderScheduler(appContext)
    private val appDatabase = AppDatabase.getDatabase(appContext)

    private val settingsRepository = SettingsRepository(
        settingsDataStore
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
        loadProductAnalyticsConsent()
        checkHealthConnectStatus()
        observeHealthConnectStepHistory()
    }

    private fun observeHealthConnectStepHistory() {
        viewModelScope.launch {
            stepHistoryRepository.recentEntries.collect { entries ->
                _uiState.update { it.copy(healthConnectStepHistory = entries) }
            }
        }
    }

    private fun loadProductAnalyticsConsent() {
        viewModelScope.launch {
            settingsDataStore.productAnalyticsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(productAnalyticsEnabled = enabled) }
            }
        }
    }

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val supported = healthConnectManager.isSupported.value
            val connected = if (supported) healthConnectManager.hasAllPermissions() else false
            val weightConnected = if (supported) {
                healthConnectManager.hasAllPermissions(HealthConnectPermissionPolicy.weightRead)
            } else false
            val previous = _uiState.value
            _uiState.update {
                it.copy(
                    isHealthConnectSupported = supported,
                    isHealthConnectConnected = connected,
                    isHealthConnectWeightConnected = weightConnected
                )
            }
            if (!previous.isHealthConnectConnected && connected) {
                productAnalytics.track(
                    ProductAnalyticsEvent.HEALTH_CONNECT_CONNECTED,
                    mapOf("permission_type" to "steps")
                )
            }
            if (!previous.isHealthConnectWeightConnected && weightConnected) {
                productAnalytics.track(
                    ProductAnalyticsEvent.HEALTH_CONNECT_CONNECTED,
                    mapOf("permission_type" to "weight")
                )
            }
        }
    }

    fun syncHealthConnectData() {
        viewModelScope.launch {
            _uiState.update { it.copy(healthConnectSyncStatus = "Syncing...") }
            try {
                val steps = if (healthConnectManager.hasAllPermissions()) {
                    val imported = healthConnectManager.readStepsHistory(days = 30)
                    val zone = java.time.ZoneId.systemDefault()
                    stepHistoryRepository.saveAll(imported.map { day ->
                        StepHistoryEntry(
                            dayStartMillis = day.date.atStartOfDay(zone).toInstant().toEpochMilli(),
                            steps = day.steps
                        )
                    })
                    stepHistoryRepository.prune(
                        before = java.time.LocalDate.now(zone).minusDays(34),
                        zone = zone
                    )
                    imported.firstOrNull { it.date == java.time.LocalDate.now(zone) }?.steps
                } else null
                val weight = if (healthConnectManager.hasAllPermissions(HealthConnectPermissionPolicy.weightRead)) {
                    healthConnectManager.readLatestWeight()
                } else null
                val parts = buildList {
                    steps?.let { add("$it steps today") }
                    weight?.let { add("latest weight ${"%.1f".format(it.kilograms)} kg") }
                }
                _uiState.update {
                    it.copy(
                        healthConnectSteps = steps,
                        healthConnectWeightKg = weight?.kilograms,
                        healthConnectSyncStatus = if (parts.isEmpty()) {
                            "No Health Connect permissions granted"
                        } else {
                            "Synced: ${parts.joinToString(" · ")}"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(healthConnectSyncStatus = "Health Connect is unavailable right now") }
            }
        }
    }

    fun dismissHealthConnectSyncStatus() {
        _uiState.update { it.copy(healthConnectSyncStatus = null) }
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
            // Turning off the master switch stops delivery while preserving
            // each category's preference for the next explicit re-enable.
            if (!enabled) {
                settingsRepository.updateReminderSetting(
                    remindersEnabled = false
                )
                waterReminderPreferences.save(waterReminderPreferences.load().copy(isEnabled = false))
                waterReminderScheduler.cancel()
                weightReminderManager.pauseReminder()
            } else {
                settingsRepository.updateReminderSetting(remindersEnabled = true)
                // Re-enable only the child reminders the user previously
                // selected. The master switch never silently opts someone in
                // to a new notification category.
                val current = settingsRepository.settingsFlow.first()
                if (current.waterReminderEnabled) {
                    val waterSettings = waterReminderPreferences.load().copy(isEnabled = true)
                    waterReminderPreferences.save(waterSettings)
                    waterReminderScheduler.schedule(waterSettings)
                }
                if (current.weightReminderEnabled) {
                    weightReminderManager.restoreIfEnabled()
                }
                productAnalytics.track(
                    ProductAnalyticsEvent.REMINDER_ENABLED,
                    mapOf("reminder_type" to "all")
                )
            }
        }
    }

    fun toggleWaterReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReminderSetting(waterReminder = enabled)
            val waterSettings = waterReminderPreferences.load().copy(isEnabled = enabled)
            waterReminderPreferences.save(waterSettings)
            if (enabled && settingsRepository.settingsFlow.first().remindersEnabled) {
                waterReminderScheduler.schedule(waterSettings)
            } else {
                waterReminderScheduler.cancel()
            }
            if (enabled) {
                productAnalytics.track(
                    ProductAnalyticsEvent.REMINDER_ENABLED,
                    mapOf("reminder_type" to "water")
                )
            }
        }
    }

    fun toggleWeightReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReminderSetting(weightReminder = enabled)
            if (enabled && settingsRepository.settingsFlow.first().remindersEnabled) {
                weightReminderManager.restoreIfEnabled()
                // A fresh install has no saved weekly time yet. The manager's
                // default Monday 09:00 schedule is intentionally conservative.
                if (!com.health.calculator.bmi.tracker.data.preferences.WeightReminderPreferences(appContext)
                        .load().enabled
                ) {
                    weightReminderManager.scheduleWeeklyReminder(
                        java.util.Calendar.MONDAY,
                        9,
                        0
                    )
                }
            } else {
                weightReminderManager.cancelReminder()
            }
            if (enabled) {
                productAnalytics.track(
                    ProductAnalyticsEvent.REMINDER_ENABLED,
                    mapOf("reminder_type" to "weight")
                )
            }
        }
    }

    fun toggleProductAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setProductAnalyticsEnabled(enabled)
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
        _uiState.update { it.copy(showClearAllDataDialog = false) }
        val requested = FullAppDataResetter.request(appContext)
        if (!requested) {
            _uiState.update {
                it.copy(exportStatusMessage = "Android could not start the full app-data reset. Please try again.")
            }
        }
        // On success Android clears the complete private app data set and normally
        // terminates this process, so no partial success state is shown here.
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

