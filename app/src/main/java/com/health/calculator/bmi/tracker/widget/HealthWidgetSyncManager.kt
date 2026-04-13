package com.health.calculator.bmi.tracker.widget

import android.content.Context
import com.health.calculator.bmi.tracker.widget.core.WidgetDataChangeReceiver

/**
 * Manages the aggregation of health metrics and computes
 * the overall "Health Score" for the summary widgets.
 */
object HealthWidgetSyncManager {

    fun onBmiUpdated(context: Context, bmi: Float, category: String) {
        val prefs = WidgetPreferencesManager(context)
        val score = calculateNewScore(context, bmi = bmi)
        prefs.saveHealthStats(score = score, bmi = bmi)
        notifyWidgets(context)
    }

    fun onBloodPressureUpdated(context: Context, sys: Int, dia: Int, category: String) {
        val prefs = WidgetPreferencesManager(context)
        val score = calculateNewScore(context, sys = sys, dia = dia)
        prefs.saveHealthStats(score = score, sys = sys, dia = dia)
        notifyWidgets(context)
    }

    fun onWaterUpdated(context: Context, intakeMl: Int, glasses: Int, goalMl: Int) {
        val prefs = WidgetPreferencesManager(context)
        val pct = if (goalMl > 0) ((intakeMl.toFloat() / goalMl) * 100).toInt() else 0
        val score = calculateNewScore(context, waterPct = pct)
        prefs.saveHealthStats(score = score, waterPct = pct)
        notifyWidgets(context)
    }

    fun onCaloriesUpdated(context: Context, consumed: Int, goal: Int) {
        val prefs = WidgetPreferencesManager(context)
        val pct = if (goal > 0) ((consumed.toFloat() / goal) * 100).toInt() else 0
        val score = calculateNewScore(context, calPct = pct)
        prefs.saveHealthStats(score = score, calPct = pct)
        notifyWidgets(context)
    }

    /**
     * Computes a weighted health score (0-100) based on latest known metrics.
     * Weights: BMI (30%), BP (30%), Water (20%), Calories (20%).
     */
    private fun calculateNewScore(
        context: Context,
        bmi: Float? = null,
        sys: Int? = null,
        dia: Int? = null,
        waterPct: Int? = null,
        calPct: Int? = null
    ): Int {
        val prefs = WidgetPreferencesManager(context)
        
        // Use provided values or fall back to stored ones
        val currentBmi = bmi ?: prefs.getHealthBmi()
        val (currentSys, currentDia) = if (sys != null && dia != null) sys to dia else prefs.getHealthBp()
        val currentWater = waterPct ?: prefs.getHealthWaterPct()
        val currentCal = calPct ?: prefs.getHealthCalPct()

        var totalWeight = 0f
        var weightedScore = 0f

        // BMI Score (Normal range 18.5 - 25.0 is 100 pts)
        if (currentBmi > 0) {
            val bmiScore = when {
                currentBmi in 18.5..25.0 -> 100
                currentBmi in 25.0..30.0 -> 70
                currentBmi in 17.0..18.5 -> 70
                else -> 40
            }
            weightedScore += bmiScore * 0.3f
            totalWeight += 0.3f
        }

        // BP Score (Normal < 120/80 is 100 pts)
        if (currentSys > 0 && currentDia > 0) {
            val bpScore = when {
                currentSys < 120 && currentDia < 80 -> 100
                currentSys < 130 && currentDia < 85 -> 80
                currentSys < 140 && currentDia < 90 -> 60
                else -> 40
            }
            weightedScore += bpScore * 0.3f
            totalWeight += 0.3f
        }

        // Water Score (Target 100%)
        if (currentWater > 0) {
            val waterScore = currentWater.coerceAtMost(100)
            weightedScore += waterScore * 0.2f
            totalWeight += 0.2f
        }

        // Calorie Score (Target 100%)
        if (currentCal > 0) {
            val calScore = currentCal.coerceAtMost(100)
            weightedScore += calScore * 0.2f
            totalWeight += 0.2f
        }

        return if (totalWeight > 0) (weightedScore / totalWeight).toInt() else 0
    }

    private fun notifyWidgets(context: Context) {
        WidgetDataNotifier.notifyAll(context)
    }
}
