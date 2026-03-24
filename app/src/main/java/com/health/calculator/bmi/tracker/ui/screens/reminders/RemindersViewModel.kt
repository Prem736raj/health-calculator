package com.health.calculator.bmi.tracker.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.models.QuietHours
import com.health.calculator.bmi.tracker.data.models.Reminder
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import com.health.calculator.bmi.tracker.data.repository.ReminderRepository
import com.health.calculator.bmi.tracker.notifications.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val activeCount: Int = 0,
    val quietHours: QuietHours = QuietHours(),
    val showAddEditDialog: Boolean = false,
    val editingReminder: Reminder? = null,
    val isCreatingNew: Boolean = false,
    val selectedCategory: ReminderCategory = ReminderCategory.CUSTOM,
    val editTitle: String = "",
    val editMessage: String = "",
    val editTimes: List<String> = listOf("09:00"),
    val editDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val editSoundName: String = "Default",
    val editSoundUri: String? = null,
    val editVibration: Boolean = true,
    val editHighPriority: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val reminderToDelete: Reminder? = null,
    val showQuietHoursSettings: Boolean = false,
    val showTimePicker: Boolean = false,
    val timePickerIndex: Int = -1, // -1=adding new, 0+=editing index
    val showCategoryPicker: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val showPermissionRationale: Boolean = false,
    val notificationSentCount: Int = 0,
    val notificationRemainingCount: Int = 8,
    val notificationTapRate: Float = 0f,
    val isLoading: Boolean = true
)

class RemindersViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val context: android.content.Context
) : ViewModel() {

    private val notificationStats = com.health.calculator.bmi.tracker.notifications.NotificationStatistics(context)
    private val rateLimiter = com.health.calculator.bmi.tracker.notifications.NotificationRateLimiter(context)

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            reminderRepository.getAllReminders().collect { reminders ->
                _uiState.update { it.copy(reminders = reminders, isLoading = false) }
            }
        }
        viewModelScope.launch {
            reminderRepository.getActiveCount().collect { count ->
                _uiState.update { it.copy(activeCount = count) }
            }
        }
        viewModelScope.launch {
            reminderRepository.getQuietHoursFlow().collect { quiet ->
                _uiState.update { it.copy(quietHours = quiet) }
            }
        }
        loadNotificationStats()
    }

    fun loadNotificationStats() {
        val stats = notificationStats.getStats()
        val remaining = rateLimiter.getRemainingNotificationsToday()
        _uiState.update { 
            it.copy(
                notificationSentCount = stats.totalSent,
                notificationRemainingCount = remaining,
                notificationTapRate = stats.tapRate
            )
        }
    }

    fun setPermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(hasNotificationPermission = granted) }
    }

    fun showPermissionRationale() {
        _uiState.update { it.copy(showPermissionRationale = true) }
    }

    fun dismissPermissionRationale() {
        _uiState.update { it.copy(showPermissionRationale = false) }
    }

    // Add/Edit Reminder
    fun showAddDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                isCreatingNew = true,
                editingReminder = null,
                showCategoryPicker = true
            )
        }
    }

    fun selectCategoryAndContinue(category: ReminderCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                editTitle = category.defaultTitle,
                editMessage = category.defaultMessage,
                editTimes = category.suggestedTimes.take(if (category == ReminderCategory.WATER_INTAKE) 7 else 2),
                editDays = listOf(1, 2, 3, 4, 5, 6, 7),
                editVibration = true,
                editHighPriority = false,
                editSoundName = "Default",
                editSoundUri = null,
                showCategoryPicker = false,
                showAddEditDialog = true
            )
        }
    }

    fun showEditDialog(reminder: Reminder) {
        val category = ReminderCategory.fromName(reminder.category)
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                isCreatingNew = false,
                editingReminder = reminder,
                selectedCategory = category,
                editTitle = reminder.title,
                editMessage = reminder.message,
                editTimes = reminder.getTimesList(),
                editDays = reminder.getDaysList(),
                editSoundName = reminder.soundName,
                editSoundUri = reminder.soundUri,
                editVibration = reminder.vibrationEnabled,
                editHighPriority = reminder.isHighPriority,
                showCategoryPicker = false
            )
        }
    }

    fun dismissAddEditDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                showCategoryPicker = false,
                editingReminder = null
            )
        }
    }

    fun dismissCategoryPicker() {
        _uiState.update {
            it.copy(
                showCategoryPicker = false,
                showAddEditDialog = false
            )
        }
    }

    fun updateEditTitle(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    fun updateEditMessage(message: String) {
        _uiState.update { it.copy(editMessage = message) }
    }

    fun updateEditDays(days: List<Int>) {
        _uiState.update { it.copy(editDays = days) }
    }

    fun updateEditVibration(enabled: Boolean) {
        _uiState.update { it.copy(editVibration = enabled) }
    }

    fun updateEditHighPriority(high: Boolean) {
        _uiState.update { it.copy(editHighPriority = high) }
    }

    // Time management
    fun showTimePickerForIndex(index: Int) {
        _uiState.update { it.copy(showTimePicker = true, timePickerIndex = index) }
    }

    fun showTimePickerForNew() {
        _uiState.update { it.copy(showTimePicker = true, timePickerIndex = -1) }
    }

    fun dismissTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun setTime(hour: Int, minute: Int) {
        val timeStr = String.format("%02d:%02d", hour, minute)
        val state = _uiState.value
        val times = state.editTimes.toMutableList()

        if (state.timePickerIndex == -1) {
            times.add(timeStr)
        } else if (state.timePickerIndex < times.size) {
            times[state.timePickerIndex] = timeStr
        }

        _uiState.update {
            it.copy(editTimes = times.distinct().sorted(), showTimePicker = false)
        }
    }

    fun removeTime(index: Int) {
        val times = _uiState.value.editTimes.toMutableList()
        if (times.size > 1 && index < times.size) {
            times.removeAt(index)
            _uiState.update { it.copy(editTimes = times) }
        }
    }

    fun saveReminder() {
        val state = _uiState.value
        if (state.editTitle.isBlank() || state.editTimes.isEmpty() || state.editDays.isEmpty()) return

        viewModelScope.launch {
            val reminder = Reminder(
                id = state.editingReminder?.id ?: java.util.UUID.randomUUID().toString(),
                category = state.selectedCategory.name,
                title = state.editTitle.trim(),
                message = state.editMessage.trim(),
                isEnabled = true,
                times = state.editTimes.joinToString(","),
                daysOfWeek = state.editDays.joinToString(","),
                soundUri = state.editSoundUri,
                soundName = state.editSoundName,
                vibrationEnabled = state.editVibration,
                isHighPriority = state.editHighPriority,
                navigateRoute = state.selectedCategory.defaultRoute,
                createdAt = state.editingReminder?.createdAt ?: System.currentTimeMillis()
            )

            reminderRepository.saveReminder(reminder)
            reminderScheduler.scheduleReminder(reminder)

            _uiState.update {
                it.copy(showAddEditDialog = false, editingReminder = null)
            }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val newEnabled = !reminder.isEnabled
            reminderRepository.toggleReminder(reminder.id, newEnabled)

            val updated = reminder.copy(isEnabled = newEnabled)
            if (newEnabled) {
                reminderScheduler.scheduleReminder(updated)
            } else {
                reminderScheduler.cancelReminder(updated)
            }
        }
    }

    // Delete
    fun confirmDelete(reminder: Reminder) {
        _uiState.update { it.copy(showDeleteConfirm = true, reminderToDelete = reminder) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false, reminderToDelete = null) }
    }

    fun deleteReminder() {
        val reminder = _uiState.value.reminderToDelete ?: return
        viewModelScope.launch {
            reminderScheduler.cancelReminder(reminder)
            reminderRepository.deleteReminder(reminder.id)
            _uiState.update { it.copy(showDeleteConfirm = false, reminderToDelete = null) }
        }
    }

    // Quiet Hours
    fun showQuietHoursSettings() {
        _uiState.update { it.copy(showQuietHoursSettings = true) }
    }

    fun dismissQuietHoursSettings() {
        _uiState.update { it.copy(showQuietHoursSettings = false) }
    }

    fun updateQuietHours(quietHours: QuietHours) {
        viewModelScope.launch {
            reminderRepository.saveQuietHours(quietHours)
        }
    }

    fun toggleQuietHours(enabled: Boolean) {
        val current = _uiState.value.quietHours
        updateQuietHours(current.copy(isEnabled = enabled))
    }

    fun setQuietStart(hour: Int, minute: Int) {
        val current = _uiState.value.quietHours
        updateQuietHours(current.copy(startHour = hour, startMinute = minute))
    }

    fun setQuietEnd(hour: Int, minute: Int) {
        val current = _uiState.value.quietHours
        updateQuietHours(current.copy(endHour = hour, endMinute = minute))
    }

    fun toggleEmergencyOverride(enabled: Boolean) {
        val current = _uiState.value.quietHours
        updateQuietHours(current.copy(allowEmergencyOverride = enabled))
    }
}
