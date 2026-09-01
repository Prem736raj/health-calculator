package com.health.calculator.bmi.tracker.presentation.weight

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.WeightRepository
import com.health.calculator.bmi.tracker.data.repository.WeightTimeFilter
import com.health.calculator.bmi.tracker.notifications.WeightReminderManager
import com.health.calculator.bmi.tracker.domain.tracking.TrackingQualityPolicy
import com.health.calculator.bmi.tracker.domain.tracking.TrackingComparison
import com.health.calculator.bmi.tracker.domain.tracking.buildTrackingComparison
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeightTrackingUiState(
    val weights: List<WeightEntry> = emptyList(),
    val statistics: WeightStatistics = WeightStatistics(),
    val weeklyComparison: TrackingComparison? = null,
    val monthlyComparison: TrackingComparison? = null,
    val goalProgress: WeightGoalProgress? = null,
    val useMetric: Boolean = true,
    val timeFilter: WeightTimeFilter = WeightTimeFilter.THIRTY_DAYS,
    val isLogDialogOpen: Boolean = false,
    val editingEntry: WeightEntry? = null,
    val weightInput: String = "",
    val noteInput: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class WeightTrackingViewModel @Inject constructor(
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

        // Keep period comparisons based on the same local entries used by the graph.
        weightRepository.getAllWeights()
            .onEach { entries ->
                val now = System.currentTimeMillis()
                val week = 7L * 24 * 60 * 60 * 1000
                val month = 30L * 24 * 60 * 60 * 1000
                val weekly = buildTrackingComparison(
                    currentValues = entries.filter { it.dateMillis >= now - week }.map { it.weightKg },
                    previousValues = entries.filter { it.dateMillis in (now - 2 * week) until (now - week) }.map { it.weightKg }
                )
                val monthly = buildTrackingComparison(
                    currentValues = entries.filter { it.dateMillis >= now - month }.map { it.weightKg },
                    previousValues = entries.filter { it.dateMillis in (now - 2 * month) until (now - month) }.map { it.weightKg }
                )
                _uiState.update { it.copy(weeklyComparison = weekly, monthlyComparison = monthly) }
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
        _uiState.update {
            it.copy(
                isLogDialogOpen = true,
                editingEntry = null,
                weightInput = "",
                noteInput = "",
                dateMillis = System.currentTimeMillis()
            )
        }
    }

    fun onEditEntry(entry: WeightEntry) {
        _uiState.update {
            it.copy(
                isLogDialogOpen = true,
                editingEntry = entry,
                weightInput = String.format(
                    java.util.Locale.getDefault(),
                    "%.1f",
                    if (it.useMetric) entry.weightKg else entry.weightLbs
                ),
                noteInput = entry.note.orEmpty(),
                dateMillis = entry.dateMillis
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
        _uiState.update { it.copy(isLogDialogOpen = false, editingEntry = null) }
    }

    fun onSaveWeight() {
        val state = _uiState.value
        val weight = state.weightInput.toDoubleOrNull()
        val weightKg = weight?.let { if (state.useMetric) it else it / 2.20462 }
        val validationError = when {
            weightKg == null -> "Enter a valid weight"
            else -> TrackingQualityPolicy.validateWeightKg(weightKg)
                ?: TrackingQualityPolicy.validateNote(state.noteInput)
                ?: TrackingQualityPolicy.validateTimestamp(state.dateMillis)
        }
        if (validationError != null) {
            _uiState.update { it.copy(snackbarMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val note = state.noteInput.trim().ifBlank { null }
            val editingEntry = state.editingEntry
            try {
                if (editingEntry == null) {
                    weightRepository.logWeight(
                        weightKg = weightKg!!,
                        dateMillis = state.dateMillis,
                        note = note
                    )
                } else {
                    weightRepository.updateWeight(
                        editingEntry.copy(
                            weightKg = weightKg!!,
                            dateMillis = state.dateMillis,
                            note = note
                        )
                    )
                }
            } catch (_: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, snackbarMessage = "Weight entry could not be saved") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isLogDialogOpen = false,
                    editingEntry = null,
                    snackbarMessage = if (editingEntry == null) "Weight logged successfully" else "Weight entry updated"
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
