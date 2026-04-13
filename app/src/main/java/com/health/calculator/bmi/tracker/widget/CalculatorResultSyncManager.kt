package com.health.calculator.bmi.tracker.widget

import android.content.Context

/**
 * Call from your calculator ViewModels after
 * every calculation to keep widget results fresh.
 */
object CalculatorResultSyncManager {

    fun onBmiCalculated(context: Context, bmi: Float, category: String) {
        val result   = String.format("%.1f", bmi)
        val subtitle = category
        SingleCalculatorWidget.saveResultAndRefresh(context, CalculatorType.BMI, result, subtitle)
        WidgetPreferencesManager(context).trackUsage(CalculatorType.BMI)
        HealthWidgetSyncManager.onBmiUpdated(context, bmi, category)
    }

    fun onBpCalculated(context: Context, systolic: Int, diastolic: Int, category: String) {
        val result   = "$systolic/$diastolic"
        val subtitle = "mmHg · $category"
        SingleCalculatorWidget.saveResultAndRefresh(context, CalculatorType.BLOOD_PRESSURE, result, subtitle)
        WidgetPreferencesManager(context).trackUsage(CalculatorType.BLOOD_PRESSURE)
        HealthWidgetSyncManager.onBloodPressureUpdated(context, systolic, diastolic, category)
    }

    fun onWaterLogged(context: Context, intakeMl: Int, goalMl: Int, glassesCount: Int) {
        val pct      = if (goalMl > 0) ((intakeMl.toFloat() / goalMl) * 100).toInt() else 0
        val result   = "$pct% today"
        val subtitle = "${formatMl(intakeMl)} of ${formatMl(goalMl)}"
        SingleCalculatorWidget.saveResultAndRefresh(context, CalculatorType.WATER, result, subtitle)
        WidgetPreferencesManager(context).trackUsage(CalculatorType.WATER)
        HealthWidgetSyncManager.onWaterUpdated(context, intakeMl, glassesCount, goalMl)
    }

    fun onCaloriesLogged(context: Context, consumed: Int, goal: Int) {
        val pct      = if (goal > 0) ((consumed.toFloat() / goal) * 100).toInt() else 0
        val result   = "$pct% of goal"
        val subtitle = "$consumed / $goal kcal"
        SingleCalculatorWidget.saveResultAndRefresh(context, CalculatorType.CALORIES, result, subtitle)
        WidgetPreferencesManager(context).trackUsage(CalculatorType.CALORIES)
        HealthWidgetSyncManager.onCaloriesUpdated(context, consumed, goal)
    }

    fun onHeartRateCalculated(context: Context, maxHr: Int, zone: String) {
        val result   = "$maxHr bpm max"
        val subtitle = "Zone: $zone"
        SingleCalculatorWidget.saveResultAndRefresh(context, CalculatorType.HEART_RATE, result, subtitle)
        WidgetPreferencesManager(context).trackUsage(CalculatorType.HEART_RATE)
    }

    private fun formatMl(ml: Int): String {
        return if (ml >= 1000) String.format("%.1fL", ml / 1000f) else "${ml}ml"
    }
}
