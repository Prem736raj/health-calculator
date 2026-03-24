// ui/screens/reports/WeeklyReportViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.dao.WeeklyReportDao
import com.health.calculator.bmi.tracker.data.models.*
import com.health.calculator.bmi.tracker.domain.usecases.WeeklyReportGenerator
import com.health.calculator.bmi.tracker.notifications.WeeklyReportScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class WeeklyReportUiState(
    val currentReport: WeeklyReportSummary? = null,
    val previousReports: List<WeeklyReport> = emptyList(),
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val reportScheduleEnabled: Boolean = false,
    val scheduledDay: Int = Calendar.SUNDAY,
    val scheduledHour: Int = 19,
    val scheduledMinute: Int = 0,
    val showScheduleSettings: Boolean = false,
    val showShareDialog: Boolean = false,
    val shareIncludeWeight: Boolean = true,
    val shareIncludeBmi: Boolean = true,
    val shareIncludeBp: Boolean = true,
    val shareIncludeWater: Boolean = true,
    val shareIncludeCalories: Boolean = true,
    val shareIncludeExercise: Boolean = true,
    val shareIncludeScore: Boolean = true,
    val selectedReportId: Long? = null,
    val error: String? = null
)

class WeeklyReportViewModel(
    private val reportGenerator: WeeklyReportGenerator,
    private val weeklyReportDao: WeeklyReportDao,
    private val reportScheduler: WeeklyReportScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReportUiState())
    val uiState: StateFlow<WeeklyReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update {
            it.copy(
                reportScheduleEnabled = reportScheduler.isEnabled(),
                scheduledDay = reportScheduler.getScheduledDay(),
                scheduledHour = reportScheduler.getScheduledHour(),
                scheduledMinute = reportScheduler.getScheduledMinute()
            )
        }

        viewModelScope.launch {
            weeklyReportDao.getAllReports().collect { reports ->
                _uiState.update { it.copy(previousReports = reports) }
            }
        }

        generateCurrentWeekReport()
    }

    fun generateCurrentWeekReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                val summary = reportGenerator.generateReport()
                weeklyReportDao.markAsRead(summary.report.id)
                _uiState.update {
                    it.copy(
                        currentReport = summary,
                        isLoading = false,
                        isGenerating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGenerating = false,
                        error = "Failed to generate report: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadReport(reportId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val report = weeklyReportDao.getReportById(reportId)
                if (report != null) {
                    weeklyReportDao.markAsRead(reportId)
                    val summary = reportGenerator.generateReport(report.weekStartDate)
                    _uiState.update {
                        it.copy(
                            currentReport = summary,
                            selectedReportId = reportId,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Schedule settings
    fun showScheduleSettings() {
        _uiState.update { it.copy(showScheduleSettings = true) }
    }

    fun dismissScheduleSettings() {
        _uiState.update { it.copy(showScheduleSettings = false) }
    }

    fun toggleSchedule(enabled: Boolean) {
        if (enabled) {
            val state = _uiState.value
            reportScheduler.scheduleWeeklyReport(state.scheduledDay, state.scheduledHour, state.scheduledMinute)
        } else {
            reportScheduler.cancelWeeklyReport()
        }
        _uiState.update { it.copy(reportScheduleEnabled = enabled) }
    }

    fun setScheduleDay(day: Int) {
        _uiState.update { it.copy(scheduledDay = day) }
        if (_uiState.value.reportScheduleEnabled) {
            reportScheduler.scheduleWeeklyReport(day, _uiState.value.scheduledHour, _uiState.value.scheduledMinute)
        }
    }

    fun setScheduleTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(scheduledHour = hour, scheduledMinute = minute) }
        if (_uiState.value.reportScheduleEnabled) {
            reportScheduler.scheduleWeeklyReport(_uiState.value.scheduledDay, hour, minute)
        }
    }

    // Share
    fun showShareDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    fun dismissShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun toggleShareSection(section: String, included: Boolean) {
        _uiState.update {
            when (section) {
                "weight" -> it.copy(shareIncludeWeight = included)
                "bmi" -> it.copy(shareIncludeBmi = included)
                "bp" -> it.copy(shareIncludeBp = included)
                "water" -> it.copy(shareIncludeWater = included)
                "calories" -> it.copy(shareIncludeCalories = included)
                "exercise" -> it.copy(shareIncludeExercise = included)
                "score" -> it.copy(shareIncludeScore = included)
                else -> it
            }
        }
    }

    fun shareReport(context: Context) {
        val state = _uiState.value
        val report = state.currentReport?.report ?: return

        val text = buildShareText(report, state)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My Weekly Health Report")
        }
        context.startActivity(Intent.createChooser(intent, "Share Weekly Report"))
        _uiState.update { it.copy(showShareDialog = false) }
    }

    private fun buildShareText(report: WeeklyReport, state: WeeklyReportUiState): String {
        val lines = mutableListOf<String>()
        lines.add("\uD83D\uDCCA My Weekly Health Report")
        lines.add("━━━━━━━━━━━━━━━━━━━━")
        lines.add("Grade: ${report.overallGrade}")
        lines.add("")

        if (state.shareIncludeScore && report.healthScoreEnd >= 0) {
            lines.add("\uD83C\uDFC6 Health Score: ${report.healthScoreEnd}/100 (${if (report.healthScoreChange >= 0) "+" else ""}${report.healthScoreChange})")
        }
        if (state.shareIncludeWeight && report.weightEntryCount > 0) {
            val change = report.weightChange?.let { String.format("%.1f", it) } ?: "0"
            lines.add("⚖️ Weight: ${report.weightEnd?.let { String.format("%.1f kg", it) } ?: "—"} (${change}kg)")
        }
        if (state.shareIncludeBmi && report.bmiReadingCount > 0) {
            lines.add("\uD83D\uDCCA BMI: ${report.avgBmi?.let { String.format("%.1f", it) } ?: "—"}")
        }
        if (state.shareIncludeBp && report.bpReadingCount > 0) {
            lines.add("❤️ BP: ${report.avgSystolic?.toInt() ?: 0}/${report.avgDiastolic?.toInt() ?: 0} mmHg")
        }
        if (state.shareIncludeWater) {
            lines.add("\uD83D\uDCA7 Water: ${report.waterDaysGoalMet}/7 days goal met")
        }
        if (state.shareIncludeCalories && report.calorieDaysLogged > 0) {
            lines.add("\uD83C\uDF7D️ Avg Calories: ${report.avgCaloriesConsumed}/day")
        }
        if (state.shareIncludeExercise && report.exerciseMinutes > 0) {
            lines.add("\uD83C\uDFC3 Exercise: ${report.exerciseMinutes} min")
        }

        lines.add("")
        lines.add("Generated by Health Calculator: BMI Tracker")

        return lines.joinToString("\n")
    }
}
