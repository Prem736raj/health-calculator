package com.health.calculator.bmi.tracker.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val overallStats: OverallStatistics = OverallStatistics(),
    val healthTrends: List<HealthTrendSummary> = emptyList(),
    val heatmapMonths: List<HeatmapMonth> = emptyList(),
    val isLoading: Boolean = true,
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTHLY
)

enum class ReportPeriod {
    WEEKLY, MONTHLY, QUARTERLY, YEARLY
}

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val historyRepository = HistoryRepository(database.historyDao())
    private val statisticsRepository = StatisticsRepository.getInstance(historyRepository)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        combine(
            statisticsRepository.getOverallStatistics(),
            statisticsRepository.getHeatmapData(),
            getTrendsForAllTypes()
        ) { overall, heatmap, trends ->
            _uiState.update { 
                it.copy(
                    overallStats = overall,
                    heatmapMonths = heatmap,
                    healthTrends = trends,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun getTrendsForAllTypes(): Flow<List<HealthTrendSummary>> {
        val trendFlows = CalculatorType.entries.map { statisticsRepository.getTrendSummary(it) }
        return combine(trendFlows) { trends ->
            trends.filterNotNull()
        }
    }

    fun onPeriodSelected(period: ReportPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }
}
