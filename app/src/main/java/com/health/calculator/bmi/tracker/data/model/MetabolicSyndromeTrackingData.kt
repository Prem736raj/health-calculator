package com.health.calculator.bmi.tracker.data.model

data class MetabolicSyndromeRecord(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val dateTime: String = "",
    val criteriaMet: Int = 0,
    val isSyndromePresent: Boolean = false,
    val riskLevel: String = "",

    // Individual criterion values
    val waistCm: Float = 0f,
    val waistMet: Boolean = false,
    val waistOnMed: Boolean = false,

    val systolic: Float = 0f,
    val diastolic: Float = 0f,
    val bpMet: Boolean = false,
    val bpOnMed: Boolean = false,

    val glucoseMgDl: Float = 0f,
    val glucoseMet: Boolean = false,
    val glucoseOnMed: Boolean = false,

    val triglyceridesMgDl: Float = 0f,
    val triglyceridesMet: Boolean = false,
    val triglyceridesOnMed: Boolean = false,

    val hdlMgDl: Float = 0f,
    val hdlMet: Boolean = false,
    val hdlOnMed: Boolean = false,

    val isMale: Boolean = true
)

data class CriterionTrend(
    val name: String,
    val icon: String,
    val currentValue: String,
    val previousValue: String?,
    val currentlyMet: Boolean,
    val previouslyMet: Boolean?,
    val trend: MetabolicTrendDirection,
    val changeDescription: String
)

enum class MetabolicTrendDirection(val label: String, val icon: String) {
    IMPROVED("Improved", "↗️"),
    WORSENED("Worsened", "↘️"),
    UNCHANGED("Unchanged", "➡️"),
    NEW("First Reading", "🆕")
}

data class AssessmentComparison(
    val currentDate: String,
    val previousDate: String?,
    val currentCriteriaMet: Int,
    val previousCriteriaMet: Int?,
    val criterionTrends: List<CriterionTrend>,
    val improvedCount: Int,
    val worsenedCount: Int,
    val unchangedCount: Int,
    val overallTrend: MetabolicTrendDirection,
    val newlyNormalCriteria: List<String>,
    val newlyAbnormalCriteria: List<String>
)
