package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.BloodPressureRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class BpTimeRange(val displayName: String, val days: Long?) {
    WEEK("7 Days", 7),
    MONTH("30 Days", 30),
    THREE_MONTHS("90 Days", 90),
    YEAR("1 Year", 365),
    ALL("All Time", null)
}

enum class BpTrendDirection(val label: String, val emoji: String) {
    IMPROVING("Improving", "📉"),
    WORSENING("Worsening", "📈"),
    STEADY("Steady", "➡️"),
    INSUFFICIENT("Not enough data", "📊")
}

data class BpDataPoint(
    val entity: BloodPressureEntity,
    val dateTime: LocalDateTime,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val category: BpCategory
)

data class BpDayData(
    val date: LocalDate,
    val readings: List<BloodPressureEntity>,
    val avgSystolic: Int?,
    val avgDiastolic: Int?,
    val worstCategory: BpCategory?
)

data class BpCategoryDistribution(
    val category: BpCategory,
    val count: Int,
    val percentage: Float
)

data class BpStatistics(
    val totalReadings: Int = 0,
    val avgSystolic: Double = 0.0,
    val avgDiastolic: Double = 0.0,
    val avgPulse: Double? = null,
    val highestSystolic: Int? = null,
    val highestDiastolic: Int? = null,
    val lowestSystolic: Int? = null,
    val lowestDiastolic: Int? = null,
    val highestReading: BloodPressureEntity? = null,
    val lowestReading: BloodPressureEntity? = null,
    val morningAvgSystolic: Double? = null,
    val morningAvgDiastolic: Double? = null,
    val eveningAvgSystolic: Double? = null,
    val eveningAvgDiastolic: Double? = null,
    val categoryDistribution: List<BpCategoryDistribution> = emptyList(),
    val trendDirection: BpTrendDirection = BpTrendDirection.INSUFFICIENT,
    val trendSystolicChange: Double = 0.0,
    val trendDiastolicChange: Double = 0.0
)
data class BpTrendUiState(
    val isLoading: Boolean = true,
    val dataPoints: List<BpDataPoint> = emptyList(),
    val filteredDataPoints: List<BpDataPoint> = emptyList(),
    val calendarDayData: Map<LocalDate, BpDayData> = emptyMap(),
    val statistics: BpStatistics = BpStatistics(),
    val selectedTimeRange: BpTimeRange = BpTimeRange.WEEK,
    val calendarMonth: YearMonth = YearMonth.now(),
    val showDayDetail: Boolean = false,
    val selectedDayReadings: BpDayData? = null,
    val showDataPointDetail: Boolean = false,
    val selectedDataPoint: BpDataPoint? = null,
    val selectedTabIndex: Int = 0, // 0=Graph, 1=Calendar, 2=Stats
    val showPulsePressure: Boolean = false,
    val showMAP: Boolean = false,
    val showPulse: Boolean = false
)

class BpTrendViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BloodPressureRepository(database.bloodPressureDao())

    private val _uiState = MutableStateFlow(BpTrendUiState())
    val uiState: StateFlow<BpTrendUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            repository.allMainReadings.collect { entities ->
                val dataPoints = entities.map { entity ->
                    val dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(entity.measurementTimestamp),
                        ZoneId.systemDefault()
                    )
                    val category = try {
                        BpCategory.valueOf(entity.category)
                    } catch (e: Exception) {
                        BpCategory.OPTIMAL
                    }
                    BpDataPoint(
                        entity = entity,
                        dateTime = dateTime,
                        systolic = entity.systolic,
                        diastolic = entity.diastolic,
                        pulse = entity.pulse,
                        category = category
                    )
                }

                _uiState.update { state ->
                    val filtered = filterDataPoints(dataPoints, state.selectedTimeRange)
                    val stats = calculateStatistics(filtered, dataPoints)
                    val calendarData = buildCalendarData(dataPoints, state.calendarMonth)

                    state.copy(
                        isLoading = false,
                        dataPoints = dataPoints,
                        filteredDataPoints = filtered,
                        statistics = stats,
                        calendarDayData = calendarData
                    )
                }
            }
        }
    }

    fun onTimeRangeSelected(range: BpTimeRange) {
        _uiState.update { state ->
            val filtered = filterDataPoints(state.dataPoints, range)
            val stats = calculateStatistics(filtered, state.dataPoints)
            state.copy(
                selectedTimeRange = range,
                filteredDataPoints = filtered,
                statistics = stats
            )
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onCalendarMonthChange(yearMonth: YearMonth) {
        _uiState.update { state ->
            val calendarData = buildCalendarData(state.dataPoints, yearMonth)
            state.copy(
                calendarMonth = yearMonth,
                calendarDayData = calendarData
            )
        }
    }

    fun onDayClicked(date: LocalDate) {
        val dayData = _uiState.value.calendarDayData[date]
        if (dayData != null && dayData.readings.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    selectedDayReadings = dayData,
                    showDayDetail = true
                )
            }
        }
    }

    fun onDismissDayDetail() {
        _uiState.update { it.copy(showDayDetail = false, selectedDayReadings = null) }
    }

    fun onDataPointClicked(dataPoint: BpDataPoint) {
        _uiState.update {
            it.copy(selectedDataPoint = dataPoint, showDataPointDetail = true)
        }
    }

    fun onDismissDataPointDetail() {
        _uiState.update { it.copy(showDataPointDetail = false, selectedDataPoint = null) }
    }

    fun onTogglePulsePressure() {
        _uiState.update { it.copy(showPulsePressure = !it.showPulsePressure) }
    }

    fun onToggleMAP() {
        _uiState.update { it.copy(showMAP = !it.showMAP) }
    }

    fun onTogglePulse() {
        _uiState.update { it.copy(showPulse = !it.showPulse) }
    }

    private fun filterDataPoints(
        allPoints: List<BpDataPoint>,
        range: BpTimeRange
    ): List<BpDataPoint> {
        val cutoff = range.days?.let {
            LocalDateTime.now().minusDays(it)
        }
        return if (cutoff != null) {
            allPoints.filter { it.dateTime.isAfter(cutoff) }
        } else {
            allPoints
        }.sortedBy { it.dateTime }
    }

    private fun calculateStatistics(
        filtered: List<BpDataPoint>,
        allPoints: List<BpDataPoint>
    ): BpStatistics {
        if (filtered.isEmpty()) return BpStatistics()

        val avgSys = filtered.map { it.systolic }.average()
        val avgDia = filtered.map { it.diastolic }.average()
        val pulses = filtered.mapNotNull { it.pulse }
        val avgPulse = if (pulses.isNotEmpty()) pulses.average() else null

        val highestSys = filtered.maxByOrNull { it.systolic }
        val lowestSys = filtered.minByOrNull { it.systolic }

        // Morning vs Evening averages
        val morningReadings = filtered.filter {
            it.entity.timeOfDay == BpTimeOfDay.MORNING.name ||
                    (it.entity.timeOfDay == null && it.dateTime.hour in 5..11)
        }
        val eveningReadings = filtered.filter {
            it.entity.timeOfDay == BpTimeOfDay.EVENING.name ||
                    it.entity.timeOfDay == BpTimeOfDay.NIGHT.name ||
                    (it.entity.timeOfDay == null && it.dateTime.hour in 17..23)
        }

        // Category distribution
        val categoryCounts = filtered.groupBy { it.category }
            .map { (cat, points) ->
                BpCategoryDistribution(
                    category = cat,
                    count = points.size,
                    percentage = points.size.toFloat() / filtered.size.toFloat()
                )
            }
            .sortedBy { it.category.sortOrder }

        // Trend calculation: last 7 vs previous 7
        val trend = calculateTrend(allPoints)

        return BpStatistics(
            totalReadings = filtered.size,
            avgSystolic = Math.round(avgSys * 10.0) / 10.0,
            avgDiastolic = Math.round(avgDia * 10.0) / 10.0,
            avgPulse = avgPulse?.let { Math.round(it * 10.0) / 10.0 },
            highestSystolic = filtered.maxOfOrNull { it.systolic },
            highestDiastolic = filtered.maxOfOrNull { it.diastolic },
            lowestSystolic = filtered.minOfOrNull { it.systolic },
            lowestDiastolic = filtered.minOfOrNull { it.diastolic },
            highestReading = highestSys?.entity,
            lowestReading = lowestSys?.entity,
            morningAvgSystolic = if (morningReadings.isNotEmpty())
                Math.round(morningReadings.map { it.systolic }.average() * 10.0) / 10.0 else null,
            morningAvgDiastolic = if (morningReadings.isNotEmpty())
                Math.round(morningReadings.map { it.diastolic }.average() * 10.0) / 10.0 else null,
            eveningAvgSystolic = if (eveningReadings.isNotEmpty())
                Math.round(eveningReadings.map { it.systolic }.average() * 10.0) / 10.0 else null,
            eveningAvgDiastolic = if (eveningReadings.isNotEmpty())
                Math.round(eveningReadings.map { it.diastolic }.average() * 10.0) / 10.0 else null,
            categoryDistribution = categoryCounts,
            trendDirection = trend.first,
            trendSystolicChange = trend.second,
            trendDiastolicChange = trend.third
        )
    }

    private fun calculateTrend(
        allPoints: List<BpDataPoint>
    ): Triple<BpTrendDirection, Double, Double> {
        val sorted = allPoints.sortedByDescending { it.dateTime }
        if (sorted.size < 4) return Triple(BpTrendDirection.INSUFFICIENT, 0.0, 0.0)

        val recentCount = minOf(7, sorted.size / 2)
        val recent = sorted.take(recentCount)
        val previous = sorted.drop(recentCount).take(recentCount)

        if (previous.isEmpty()) return Triple(BpTrendDirection.INSUFFICIENT, 0.0, 0.0)

        val recentAvgSys = recent.map { it.systolic }.average()
        val previousAvgSys = previous.map { it.systolic }.average()
        val sysChange = recentAvgSys - previousAvgSys

        val recentAvgDia = recent.map { it.diastolic }.average()
        val previousAvgDia = previous.map { it.diastolic }.average()
        val diaChange = recentAvgDia - previousAvgDia

        val direction = when {
            sysChange < -3 || diaChange < -2 -> BpTrendDirection.IMPROVING
            sysChange > 3 || diaChange > 2 -> BpTrendDirection.WORSENING
            else -> BpTrendDirection.STEADY
        }

        return Triple(
            direction,
            Math.round(sysChange * 10.0) / 10.0,
            Math.round(diaChange * 10.0) / 10.0
        )
    }

    private fun buildCalendarData(
        allPoints: List<BpDataPoint>,
        yearMonth: YearMonth
    ): Map<LocalDate, BpDayData> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val pointsInMonth = allPoints.filter { dp ->
            val date = dp.dateTime.toLocalDate()
            !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val grouped = pointsInMonth.groupBy { it.dateTime.toLocalDate() }

        return grouped.mapValues { (date, points) ->
            val avgSys = points.map { it.systolic }.average().toInt()
            val avgDia = points.map { it.diastolic }.average().toInt()
            val worstCategory = points.maxByOrNull { it.category.sortOrder }?.category

            BpDayData(
                date = date,
                readings = points.map { it.entity },
                avgSystolic = avgSys,
                avgDiastolic = avgDia,
                worstCategory = worstCategory
            )
        }
    }
}
