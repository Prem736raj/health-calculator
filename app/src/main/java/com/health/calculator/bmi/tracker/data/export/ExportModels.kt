package com.health.calculator.bmi.tracker.data.export

import com.health.calculator.bmi.tracker.data.model.CalculatorType

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    PDF("PDF Report", "pdf", "application/pdf"),
    CSV("CSV Spreadsheet", "csv", "text/csv"),
    JSON("JSON Backup", "json", "application/json")
}

enum class ExportScope(val label: String) {
    ALL("All Data"),
    FILTERED("Filtered Data"),
    CALCULATOR("Single Calculator")
}

data class ExportConfig(
    val format: ExportFormat = ExportFormat.PDF,
    val scope: ExportScope = ExportScope.ALL,
    val calculatorType: CalculatorType? = null,
    val includeCharts: Boolean = true,
    val includeProfile: Boolean = true,
    val includeSummaryStats: Boolean = true,
    val dateRangeLabel: String? = null
)

data class ExportProgress(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val isComplete: Boolean = false,
    val resultUri: android.net.Uri? = null,
    val error: String? = null
)

data class ExportSchedule(
    val enabled: Boolean = false,
    val frequency: ExportFrequency = ExportFrequency.MONTHLY,
    val format: ExportFormat = ExportFormat.PDF,
    val emailAddress: String? = null,
    val lastExportTime: Long = 0L
)

enum class ExportFrequency(val label: String, val days: Int) {
    WEEKLY("Weekly", 7),
    BIWEEKLY("Every 2 Weeks", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Every 3 Months", 90)
}

data class HealthReportCard(
    val userName: String?,
    val bmi: String?,
    val bmiCategory: String?,
    val bp: String?,
    val bpCategory: String?,
    val weight: String?,
    val waterProgress: String?,
    val calorieProgress: String?,
    val healthScore: Int?,
    val generatedDate: String
)
