// ui/screens/bloodpressure/BpReminderViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.preferences.BpReminderPreferences
import com.health.calculator.bmi.tracker.notification.BpNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class BpReminderUiState(
    val morningEnabled: Boolean = false,
    val morningHour: Int = 7,
    val morningMinute: Int = 0,
    val eveningEnabled: Boolean = false,
    val eveningHour: Int = 19,
    val eveningMinute: Int = 0,
    val reminderMessage: String = "Time to check your blood pressure! 🩺",
    val doctorReminderSet: Boolean = false,
    val doctorReminderDateFormatted: String = "",
    val doctorNote: String = "",
    val showMorningTimePicker: Boolean = false,
    val showEveningTimePicker: Boolean = false,
    val showDoctorDatePicker: Boolean = false
)

class BpReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val bpPreferences = BpReminderPreferences(application)
    private val notificationHelper = BpNotificationHelper(application)

    private val _uiState = MutableStateFlow(BpReminderUiState())
    val uiState: StateFlow<BpReminderUiState> = _uiState.asStateFlow()

    init {
        notificationHelper.createNotificationChannel()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            bpPreferences.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        morningEnabled = settings.morningReminderEnabled,
                        morningHour = settings.morningReminderHour,
                        morningMinute = settings.morningReminderMinute,
                        eveningEnabled = settings.eveningReminderEnabled,
                        eveningHour = settings.eveningReminderHour,
                        eveningMinute = settings.eveningReminderMinute,
                        reminderMessage = settings.customReminderMessage,
                        doctorReminderSet = settings.doctorReminderEnabled && settings.doctorReminderTimestamp > System.currentTimeMillis(),
                        doctorReminderDateFormatted = if (settings.doctorReminderTimestamp > 0) {
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(settings.doctorReminderTimestamp),
                                ZoneId.systemDefault()
                            ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        } else "",
                        doctorNote = settings.doctorReminderNote
                    )
                }
            }
        }
    }

    fun onMorningToggle(enabled: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            bpPreferences.updateMorningReminder(enabled, state.morningHour, state.morningMinute)
            if (enabled) {
                notificationHelper.scheduleMorningReminder(state.morningHour, state.morningMinute, state.reminderMessage)
            } else {
                notificationHelper.cancelMorningReminder()
            }
        }
    }

    fun onEveningToggle(enabled: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            bpPreferences.updateEveningReminder(enabled, state.eveningHour, state.eveningMinute)
            if (enabled) {
                notificationHelper.scheduleEveningReminder(state.eveningHour, state.eveningMinute, state.reminderMessage)
            } else {
                notificationHelper.cancelEveningReminder()
            }
        }
    }

    fun onMorningTimeSet(hour: Int, minute: Int) {
        viewModelScope.launch {
            bpPreferences.updateMorningReminder(true, hour, minute)
            if (_uiState.value.morningEnabled) {
                notificationHelper.scheduleMorningReminder(hour, minute, _uiState.value.reminderMessage)
            }
        }
        _uiState.update { it.copy(morningHour = hour, morningMinute = minute, showMorningTimePicker = false) }
    }

    fun onEveningTimeSet(hour: Int, minute: Int) {
        viewModelScope.launch {
            bpPreferences.updateEveningReminder(true, hour, minute)
            if (_uiState.value.eveningEnabled) {
                notificationHelper.scheduleEveningReminder(hour, minute, _uiState.value.reminderMessage)
            }
        }
        _uiState.update { it.copy(eveningHour = hour, eveningMinute = minute, showEveningTimePicker = false) }
    }

    fun onReminderMessageChange(message: String) {
        _uiState.update { it.copy(reminderMessage = message) }
        viewModelScope.launch { bpPreferences.updateReminderMessage(message) }
    }

    fun onShowMorningTimePicker(show: Boolean) { _uiState.update { it.copy(showMorningTimePicker = show) } }
    fun onShowEveningTimePicker(show: Boolean) { _uiState.update { it.copy(showEveningTimePicker = show) } }
    fun onShowDoctorDatePicker(show: Boolean) { _uiState.update { it.copy(showDoctorDatePicker = show) } }

    fun onDoctorDateSet(timestamp: Long) {
        viewModelScope.launch {
            bpPreferences.updateDoctorReminder(true, timestamp, _uiState.value.doctorNote)
            notificationHelper.scheduleDoctorReminder(timestamp, _uiState.value.doctorNote)
        }
        _uiState.update { it.copy(showDoctorDatePicker = false) }
    }

    fun onDoctorNoteChange(note: String) {
        _uiState.update { it.copy(doctorNote = note) }
        viewModelScope.launch {
            val settings = bpPreferences.settingsFlow.first()
            bpPreferences.updateDoctorReminder(settings.doctorReminderEnabled, settings.doctorReminderTimestamp, note)
        }
    }

    fun onCancelDoctorReminder() {
        viewModelScope.launch {
            bpPreferences.updateDoctorReminder(false, 0L, "")
            notificationHelper.cancelDoctorReminder()
        }
    }
}
