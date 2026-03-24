package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.WhrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WhrProgressState(
    val entries: List<WhrHistoryEntry> = emptyList(),
    val filteredEntries: List<WhrHistoryEntry> = emptyList(),
    val selectedTimeRange: WhrTimeRange = WhrTimeRange.NINETY_DAYS,
    val stats: WhrProgressStats? = null,
    val comparison: WhrComparison? = null,
    val goal: WhrGoal? = null,
    val showGoalDialog: Boolean = false,
    val goalInput: String = "",
    val goalError: String? = null,
    val selectedGraphLine: WhrGraphLine = WhrGraphLine.WHR,
    val showAllLines: Boolean = false
)

enum class WhrGraphLine(val label: String) {
    WHR("WHR"),
    WAIST("Waist"),
    HIP("Hip")
}

class WhrProgressViewModel(
    private val repository: WhrRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WhrProgressState())
    val state: StateFlow<WhrProgressState> = _state.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        val allEntries = repository.entries.value.sortedBy { it.timestamp }
        val timeRange = _state.value.selectedTimeRange
        val filtered = repository.getEntriesInRange(timeRange).sortedBy { it.timestamp }
        val stats = repository.getProgressStats()
        val comparison = repository.getComparison()
        val goal = repository.goal.value

        _state.update {
            it.copy(
                entries = allEntries,
                filteredEntries = filtered,
                stats = stats,
                comparison = comparison,
                goal = goal
            )
        }
    }

    fun selectTimeRange(range: WhrTimeRange) {
        _state.update { it.copy(selectedTimeRange = range) }
        refreshData()
    }

    fun selectGraphLine(line: WhrGraphLine) {
        _state.update { it.copy(selectedGraphLine = line) }
    }

    fun toggleAllLines() {
        _state.update { it.copy(showAllLines = !it.showAllLines) }
    }

    fun showGoalDialog() {
        _state.update {
            it.copy(
                showGoalDialog = true,
                goalInput = it.goal?.targetWaistCm?.let { v -> String.format("%.1f", v) } ?: "",
                goalError = null
            )
        }
    }

    fun dismissGoalDialog() {
        _state.update { it.copy(showGoalDialog = false) }
    }

    fun updateGoalInput(input: String) {
        _state.update { it.copy(goalInput = input, goalError = null) }
    }

    fun saveGoal() {
        val value = _state.value.goalInput.toFloatOrNull()
        if (value == null || value < 40f || value > 200f) {
            _state.update { it.copy(goalError = "Enter a valid waist measurement (40-200 cm)") }
            return
        }

        repository.setGoal(WhrGoal(targetWaistCm = value))
        _state.update { it.copy(showGoalDialog = false) }
        refreshData()
    }

    fun clearGoal() {
        repository.setGoal(null)
        _state.update { it.copy(showGoalDialog = false) }
        refreshData()
    }

    fun deleteEntry(id: String) {
        repository.deleteEntry(id)
        refreshData()
    }

    fun getMotivationalMessage(): String {
        val stats = _state.value.stats ?: return ""
        val goal = _state.value.goal

        return when {
            goal != null -> {
                val remaining = stats.currentWaist - goal.targetWaistCm
                when {
                    remaining <= 0 -> "🎉 Amazing! You've reached your waist goal!"
                    remaining < 2f -> "🔥 Almost there! Just ${String.format("%.1f", remaining)} cm to go!"
                    stats.waistTrend == WhrTrendDirection.IMPROVING -> "📈 Great progress! Keep going, you're heading in the right direction!"
                    else -> "💪 Stay consistent! Every measurement brings you closer to your goal."
                }
            }
            stats.whrTrend == WhrTrendDirection.IMPROVING -> "👏 Your WHR is trending in the right direction!"
            stats.whrTrend == WhrTrendDirection.STEADY -> "📊 Your WHR is steady. Consistency is key!"
            else -> "📝 Keep tracking to monitor your progress over time."
        }
    }
}

class WhrProgressViewModelFactory(
    private val repository: WhrRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WhrProgressViewModel(repository) as T
    }
}
