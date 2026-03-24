package com.health.calculator.bmi.tracker.ui.screens.profile.milestones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.models.*
import com.health.calculator.bmi.tracker.data.repository.HealthOverviewRepository
import com.health.calculator.bmi.tracker.data.repository.MilestonesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MilestonesUiState(
    val personalRecords: List<PersonalRecord> = emptyList(),
    val earnedMilestones: List<HealthMilestone> = emptyList(),
    val unearnedMilestoneTypes: List<MilestoneType> = emptyList(),
    val journeySummary: HealthJourneySummary = HealthJourneySummary(),
    val selectedCategory: MilestoneCategory? = null, // null = all
    val showNewRecordCelebration: Boolean = false,
    val newRecordType: PersonalRecordType? = null,
    val newRecordValue: String = "",
    val previousRecordValue: String? = null,
    val showNewMilestoneCelebration: Boolean = false,
    val newMilestones: List<MilestoneType> = emptyList(),
    val currentMilestoneCelebrationIndex: Int = 0,
    val isLoading: Boolean = true
)

class MilestonesViewModel(
    private val milestonesRepository: MilestonesRepository,
    private val healthOverviewRepository: HealthOverviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MilestonesUiState())
    val uiState: StateFlow<MilestonesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Load personal records
        viewModelScope.launch {
            milestonesRepository.getAllRecords().collect { records ->
                _uiState.update { it.copy(personalRecords = records) }
            }
        }

        // Load milestones
        viewModelScope.launch {
            milestonesRepository.getAllMilestones().collect { milestones ->
                val earnedTypes = milestones.map { it.milestoneType }.toSet()
                val unearned = MilestoneType.values().filter { it.name !in earnedTypes }

                _uiState.update {
                    it.copy(
                        earnedMilestones = milestones,
                        unearnedMilestoneTypes = unearned,
                        isLoading = false
                    )
                }
            }
        }

        // Load uncelebrated milestones
        viewModelScope.launch {
            milestonesRepository.getUncelebratedMilestones().collect { uncelebrated ->
                if (uncelebrated.isNotEmpty()) {
                    val types = uncelebrated.mapNotNull { m ->
                        try { MilestoneType.valueOf(m.milestoneType) } catch (e: Exception) { null }
                    }
                    if (types.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                showNewMilestoneCelebration = true,
                                newMilestones = types,
                                currentMilestoneCelebrationIndex = 0
                            )
                        }
                    }
                }
            }
        }

        // Load journey summary
        viewModelScope.launch {
            healthOverviewRepository.getHealthOverview().collect { overview ->
                val summary = milestonesRepository.getJourneySummary(
                    healthScore = overview.healthScore,
                    firstHealthScore = null,
                    trackingStreak = 0,
                    longestStreak = 0,
                    goalsSet = 0,
                    goalsAchieved = 0,
                    mostUsedCalculator = null
                )
                _uiState.update { it.copy(journeySummary = summary) }
            }
        }
    }

    fun selectCategory(category: MilestoneCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun getFilteredEarnedMilestones(): List<HealthMilestone> {
        val state = _uiState.value
        val category = state.selectedCategory ?: return state.earnedMilestones
        return state.earnedMilestones.filter { m ->
            try { MilestoneType.valueOf(m.milestoneType).category == category } catch (e: Exception) { false }
        }
    }

    fun getFilteredUnearnedMilestones(): List<MilestoneType> {
        val state = _uiState.value
        val category = state.selectedCategory ?: return state.unearnedMilestoneTypes
        return state.unearnedMilestoneTypes.filter { it.category == category }
    }

    // Called from calculators when a new value might be a personal record
    fun checkPersonalRecord(type: PersonalRecordType, value: Double, displayValue: String) {
        viewModelScope.launch {
            val isNew = milestonesRepository.checkAndUpdateRecord(type, value, displayValue)
            if (isNew) {
                val existing = milestonesRepository.getAllRecords().first()
                    .find { it.recordType == type.name }

                _uiState.update {
                    it.copy(
                        showNewRecordCelebration = true,
                        newRecordType = type,
                        newRecordValue = displayValue,
                        previousRecordValue = existing?.previousDisplayValue
                    )
                }
            }
        }
    }

    fun dismissRecordCelebration() {
        _uiState.update {
            it.copy(showNewRecordCelebration = false, newRecordType = null)
        }
    }

    fun dismissMilestoneCelebration() {
        val state = _uiState.value
        val currentIdx = state.currentMilestoneCelebrationIndex
        val milestones = state.newMilestones

        // Mark current as celebrated
        if (currentIdx < milestones.size) {
            viewModelScope.launch {
                milestonesRepository.markMilestoneCelebrated(milestones[currentIdx].name)
            }
        }

        // Show next or dismiss all
        if (currentIdx + 1 < milestones.size) {
            _uiState.update {
                it.copy(currentMilestoneCelebrationIndex = currentIdx + 1)
            }
        } else {
            _uiState.update {
                it.copy(
                    showNewMilestoneCelebration = false,
                    newMilestones = emptyList(),
                    currentMilestoneCelebrationIndex = 0
                )
            }
        }
    }

    fun dismissAllCelebrations() {
        viewModelScope.launch {
            _uiState.value.newMilestones.forEach { type ->
                milestonesRepository.markMilestoneCelebrated(type.name)
            }
        }
        _uiState.update {
            it.copy(
                showNewMilestoneCelebration = false,
                newMilestones = emptyList(),
                showNewRecordCelebration = false
            )
        }
    }
}
