package com.health.calculator.bmi.tracker.presentation.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.WeightRepository
import com.health.calculator.bmi.tracker.data.repository.WeightTimeFilter
import com.health.calculator.bmi.tracker.notifications.WeightReminderManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeightTrackingUiState(
    val weights: List<WeightEntry> = emptyList(),
    val statistics: WeightStatistics = WeightStatistics(),
    val goalProgress: WeightGoalProgress? = null,
    val useMetric: Boolean = true,
    val timeFilter: WeightTimeFilter = WeightTimeFilter.THIRTY_DAYS,
    val isLogDialogOpen: Boolean = false,
    val weightInput: String = "",
    val noteInput: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

class WeightTrackingViewModel(
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository,
    private val reminderManager: WeightReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightTrackingUiState())
    val uiState: StateFlow<WeightTrackingUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        // Observe profile units
        profileRepository.getProfile()
            .onEach { profile ->
                _uiState.update { it.copy(useMetric = profile.useMetricSystem) }
            }
            .launchIn(viewModelScope)

        // Observe filtered weights
        _uiState.map { it.timeFilter }
            .distinctUntilChanged()
            .flatMapLatest { filter ->
                weightRepository.getFilteredWeights(filter)
            }
            .onEach { weights ->
                _uiState.update { it.copy(weights = weights) }
            }
            .launchIn(viewModelScope)

        // Observe statistics
        weightRepository.getWeightStatistics()
            .onEach { stats ->
                _uiState.update { it.copy(statistics = stats) }
            }
            .launchIn(viewModelScope)

        // Observe goal progress
        profileRepository.getProfile()
            .map { it.goalWeightKg }
            .distinctUntilChanged()
            .flatMapLatest { goal ->
                if (goal != null && goal > 0f) {
                    weightRepository.getGoalProgress(goal.toDouble())
                } else {
                    flowOf(null)
                }
            }
            .onEach { progress ->
                _uiState.update { it.copy(goalProgress = progress) }
            }
            .launchIn(viewModelScope)
    }

    fun onTimeFilterChange(filter: WeightTimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
    }

    fun onLogWeightClick() {
        val currentWeight = _uiState.value.statistics.currentWeight
        _uiState.update {
            it.copy(
                isLogDialogOpen = true,
                weightInput = currentWeight?.let { w -> String.format("%.1f", if (it.useMetric) w else w * 2.20462) } ?: "",
                noteInput = "",
                dateMillis = System.currentTimeMillis()
            )
        }
    }

    fun onWeightInputChange(input: String) {
        _uiState.update { it.copy(weightInput = input) }
    }

    fun onNoteInputChange(input: String) {
        _uiState.update { it.copy(noteInput = input) }
    }

    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun onDismissLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = false) }
    }

    fun onSaveWeight() {
        val weight = _uiState.value.weightInput.toDoubleOrNull() ?: return
        val weightKg = if (_uiState.value.useMetric) weight else weight / 2.20462

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            weightRepository.logWeight(
                weightKg = weightKg,
                dateMillis = _uiState.value.dateMillis,
                note = _uiState.value.noteInput.ifBlank { null }
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isLogDialogOpen = false,
                    snackbarMessage = "Weight logged successfully"
                )
            }
        }
    }

    fun onDeleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightRepository.deleteEntry(entry)
            _uiState.update { it.copy(snackbarMessage = "Entry deleted") }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun logWeightFromCalculator(weightKg: Double, source: WeightSource) {
        viewModelScope.launch {
            weightRepository.logWeight(weightKg = weightKg, source = source)
            _uiState.update { it.copy(snackbarMessage = "Weight updated from ${source.displayName}") }
        }
    }

    class Factory(
        private val weightRepository: WeightRepository,
        private val profileRepository: ProfileRepository,
        private val reminderManager: WeightReminderManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeightTrackingViewModel(weightRepository, profileRepository, reminderManager) as T
        }
    }
}
