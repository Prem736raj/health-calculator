package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HealthOverviewRepository(
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
            val healthScore = calculateHealthScore(bmi, bp, whr, waterStreak, calorieAdherence)

            HealthOverview(
                latestBmi = bmi?.toHealthMetricSummary("BMI", "bmi_calculator"),
                latestBp = bp?.toHealthMetricSummary("Blood Pressure", "bp_calculator"),
                latestWhr = whr?.toHealthMetricSummary("Waist-to-Hip", "whr_calculator"),
                latestBmr = bmr?.toHealthMetricSummary("BMR", "bmr_calculator"),
                waterStreak = waterStreak,
                calorieAdherence = calorieAdherence,
                metabolicSyndromeStatus = metabolic?.toHealthMetricSummary("Metabolic Risk", "metabolic_syndrome"),
                healthScore = healthScore
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
            low.contains("normal") || low.contains("optimal") || low.contains("healthy") || low.contains("low risk") -> 
                HealthCategoryColor.EXCELLENT
            low.contains("overweight") || low.contains("high normal") || low.contains("moderate") -> 
                HealthCategoryColor.MODERATE
            low.contains("obese") || low.contains("hypertension") || low.contains("high risk") || low.contains("stage 1") -> 
                HealthCategoryColor.WARNING
            low.contains("emergency") || low.contains("crisis") || low.contains("stage 2") -> 
                HealthCategoryColor.DANGER
            else -> HealthCategoryColor.NEUTRAL
        }
    }

    private fun calculateHealthScore(
        bmi: HistoryEntry?,
        bp: HistoryEntry?,
        whr: HistoryEntry?,
        waterStreak: Int,
        calorieAdherence: Float
    ): Int {
        var score = 0
        var maxScore = 0

        // BMI Score (0-20)
        bmi?.let {
            maxScore += 20
            score += when {
                it.category?.contains("Normal", ignoreCase = true) == true -> 20
                it.category?.contains("Overweight", ignoreCase = true) == true -> 12
                else -> 5
            }
        }

        // BP Score (0-20)
        bp?.let {
            maxScore += 20
            score += when {
                it.category?.contains("Optimal", ignoreCase = true) == true -> 20
                it.category?.contains("Normal", ignoreCase = true) == true -> 16
                else -> 8
            }
        }

        // WHR Score (0-15)
        whr?.let {
            maxScore += 15
            score += if (it.category?.contains("Low Risk", ignoreCase = true) == true) 15 else 5
        }

        // Water Streak (0-15)
        maxScore += 15
        score += when {
            waterStreak >= 7 -> 15
            waterStreak >= 3 -> 10
            waterStreak >= 1 -> 5
            else -> 0
        }

        // Calorie Adherence (0-15)
        maxScore += 15
        score += (calorieAdherence * 15).toInt().coerceIn(0, 15)

        return if (maxScore > 0) (score * 100) / maxScore else -1
    }
}
