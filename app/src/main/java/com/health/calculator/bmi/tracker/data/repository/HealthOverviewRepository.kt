package com.health.calculator.bmi.tracker.data.repository

import javax.inject.Inject

import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HealthOverviewRepository @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val waterGamificationRepository: WaterGamificationRepository,
    private val foodLogRepository: FoodLogRepository
) {

    fun getHealthOverview(): Flow<HealthOverview> {
        return combine(
            historyRepository.getLatestByType("BMI"),
            historyRepository.getLatestByType("BP"),
            historyRepository.getLatestByType("WHR"),
            historyRepository.getLatestByType("BMR"),
            historyRepository.getLatestByType("METABOLIC_SYNDROME"),
            waterGamificationRepository.observeStreakData(),
            foodLogRepository.getWeeklyAdherence()
        ) { args ->
            val bmi = args[0] as HistoryEntry?
            val bp = args[1] as HistoryEntry?
            val whr = args[2] as HistoryEntry?
            val bmr = args[3] as HistoryEntry?
            val metabolic = args[4] as HistoryEntry?
            val waterStreakData = args[5] as? WaterStreakData // Use safe cast just in case
            val calorieAdherence = args[6] as Float

            val waterStreak = waterStreakData?.currentStreak ?: 0
            val wellnessScore = calculateWellnessScore(bmi, bp, whr, waterStreak, calorieAdherence)

            HealthOverview(
                latestBmi = bmi?.toHealthMetricSummary("BMI", "bmi_calculator"),
                latestBp = bp?.toHealthMetricSummary("Blood Pressure", "bp_calculator"),
                latestWhr = whr?.toHealthMetricSummary("Waist-to-Hip", "whr_calculator"),
                latestBmr = bmr?.toHealthMetricSummary("BMR", "bmr_calculator"),
                waterStreak = waterStreak,
                calorieAdherence = calorieAdherence,
                metabolicSyndromeStatus = metabolic?.toHealthMetricSummary("Metabolic Risk", "metabolic_syndrome"),
                healthScore = wellnessScore
            )
        }
    }

    private fun HistoryEntry.toHealthMetricSummary(label: String, route: String): HealthMetricSummary {
        return HealthMetricSummary(
            label = label,
            value = resultValue + (resultLabel?.let { " $it" } ?: ""),
            category = category ?: "Unknown",
            categoryColor = mapToHealthCategoryColor(category),
            lastUpdated = timestamp,
            navigateRoute = route
        )
    }

    private fun mapToHealthCategoryColor(category: String?): HealthCategoryColor {
        if (category == null) return HealthCategoryColor.NEUTRAL
        val low = category.lowercase()
        return when {
            low.contains("normal") || low.contains("optimal") || low.contains("healthy") || low.contains("below action") ->
                HealthCategoryColor.EXCELLENT
            low.contains("overweight") || low.contains("high normal") || low.contains("moderate") || low.contains("above action") ->
                HealthCategoryColor.MODERATE
            low.contains("obese") || low.contains("hypertension") || low.contains("high risk") || low.contains("stage 1") || low.contains("several indicators") ->
                HealthCategoryColor.WARNING
            low.contains("emergency") || low.contains("crisis") || low.contains("stage 2") || low.contains("markedly") ->
                HealthCategoryColor.DANGER
            else -> HealthCategoryColor.NEUTRAL
        }
    }

    /**
     * Build the legacy integer field from data availability and logging only.
     * Measurement values are deliberately never graded as good or bad.
     */
    private fun calculateWellnessScore(
        bmi: HistoryEntry?,
        bp: HistoryEntry?,
        whr: HistoryEntry?,
        waterStreak: Int,
        calorieAdherence: Float
    ): Int {
        val components = listOf(
            bmi != null,
            bp != null,
            whr != null,
            waterStreak > 0,
            calorieAdherence > 0f
        )
        val available = components.count { it }
        return if (available == 0) -1 else (available * 100) / components.size
    }
}
