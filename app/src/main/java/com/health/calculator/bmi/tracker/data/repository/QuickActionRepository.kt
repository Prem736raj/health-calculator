package com.health.calculator.bmi.tracker.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.health.calculator.bmi.tracker.data.local.SearchPreferences
import com.health.calculator.bmi.tracker.data.model.QuickAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuickActionRepository(
    private val searchPreferences: SearchPreferences
) {

    private val allQuickActions = listOf(
        QuickAction("quick_water", "Log Water", Icons.Default.WaterDrop, "water_calculator", emoji = "💧"),
        QuickAction("quick_bp", "Check BP", Icons.Default.Favorite, "blood_pressure_calculator", emoji = "❤️"),
        QuickAction("quick_calorie", "Log Food", Icons.Default.Restaurant, "calorie_calculator", emoji = "🍽️"),
        QuickAction("quick_bmi", "Check BMI", Icons.Default.Scale, "bmi_calculator", emoji = "⚖️"),
        QuickAction("quick_bmr", "Check BMR", Icons.Default.LocalFireDepartment, "bmr_calculator", emoji = "🔥"),
        QuickAction("quick_whr", "Check WHR", Icons.Default.Straighten, "whr_calculator", emoji = "📏"),
        QuickAction("quick_hr", "HR Zones", Icons.Default.MonitorHeart, "heart_rate_calculator", emoji = "💓")
    )

    private val featureToQuickAction = mapOf(
        "water_log" to "quick_water",
        "bp_check" to "quick_bp",
        "calorie_log" to "quick_calorie",
        "bmi_calc" to "quick_bmi",
        "bmr_calc" to "quick_bmr",
        "whr_calc" to "quick_whr",
        "hr_calc" to "quick_hr"
    )

    fun getQuickActions(): Flow<List<QuickAction>> {
        return searchPreferences.getUsageCounts().map { usageCounts ->
            val actionsWithUsage = allQuickActions.map { action ->
                val featureKey = featureToQuickAction.entries
                    .find { it.value == action.id }?.key ?: ""
                val count = usageCounts[featureKey] ?: 0
                action.copy(usageCount = count)
            }

            val used = actionsWithUsage
                .filter { it.usageCount > 0 }
                .sortedByDescending { it.usageCount }
                .take(3)

            if (used.size >= 3) {
                used
            } else {
                val defaults = listOf("quick_water", "quick_bmi", "quick_bp")
                val remaining = defaults
                    .filter { defaultId -> used.none { it.id == defaultId } }
                    .mapNotNull { defaultId -> allQuickActions.find { it.id == defaultId } }

                (used + remaining).take(3)
            }
        }
    }

    suspend fun trackUsage(featureKey: String) {
        searchPreferences.incrementUsage(featureKey)
    }
}
