package com.health.calculator.bmi.tracker.widget

import android.content.Context
import android.content.SharedPreferences

class WidgetPreferencesManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "widget_preferences"

        // Quick Calc Widget keys
        private fun slotKey(widgetId: Int, slot: Int) = "quick_${widgetId}_slot_$slot"
        private fun themeKey(widgetId: Int)            = "widget_${widgetId}_theme"
        private fun opacityKey(widgetId: Int)          = "widget_${widgetId}_opacity"

        // Single Calc Widget keys
        private fun singleCalcKey(widgetId: Int) = "single_${widgetId}_calculator"

        // Last results keys (shared across widget types)
        private fun resultKey(calcType: String) = "result_${calcType}"
        private fun resultSubKey(calcType: String) = "result_sub_${calcType}"

        // Usage tracking (for auto-detect most-used)
        private fun usageKey(calcType: String) = "usage_count_${calcType}"

        // Health Score metrics
        private const val KEY_HEALTH_SCORE = "health_score_value"
        private const val KEY_HEALTH_BMI = "health_bmi_value"
        private const val KEY_HEALTH_BP_SYS = "health_bp_sys"
        private const val KEY_HEALTH_BP_DIA = "health_bp_dia"
        private const val KEY_HEALTH_WATER_PCT = "health_water_pct"
        private const val KEY_HEALTH_CALORIES_PCT = "health_calories_pct"
    }

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Quick Calc Widget Prefs ───────────────────────────────────────

    fun saveQuickCalcConfig(
        widgetId: Int,
        slot1: CalculatorType,
        slot2: CalculatorType,
        slot3: CalculatorType,
        slot4: CalculatorType,
        theme: WidgetTheme,
        opacity: Int
    ) {
        prefs.edit().apply {
            putString(slotKey(widgetId, 1), slot1.name)
            putString(slotKey(widgetId, 2), slot2.name)
            putString(slotKey(widgetId, 3), slot3.name)
            putString(slotKey(widgetId, 4), slot4.name)
            putString(themeKey(widgetId), theme.name)
            putInt(opacityKey(widgetId), opacity)
            apply()
        }
    }

    fun getQuickCalcSlot(widgetId: Int, slot: Int): CalculatorType {
        val defaults = listOf(
            CalculatorType.BMI,
            CalculatorType.BLOOD_PRESSURE,
            CalculatorType.WATER,
            CalculatorType.CALORIES
        )
        val saved = prefs.getString(slotKey(widgetId, slot), null)
        return if (saved != null) CalculatorType.fromName(saved)
        else defaults.getOrElse(slot - 1) { CalculatorType.BMI }
    }

    fun getWidgetTheme(widgetId: Int): WidgetTheme {
        val saved = prefs.getString(themeKey(widgetId), WidgetTheme.SYSTEM.name)
        return WidgetTheme.fromName(saved ?: WidgetTheme.SYSTEM.name)
    }

    fun getWidgetOpacity(widgetId: Int): Int {
        return prefs.getInt(opacityKey(widgetId), 100)
    }

    // ── Single Calc Widget Prefs ──────────────────────────────────────

    fun saveSingleCalcConfig(
        widgetId: Int,
        calcType: CalculatorType,
        theme: WidgetTheme,
        opacity: Int
    ) {
        prefs.edit().apply {
            putString(singleCalcKey(widgetId), calcType.name)
            putString(themeKey(widgetId), theme.name)
            putInt(opacityKey(widgetId), opacity)
            apply()
        }
    }

    fun getSingleCalcType(widgetId: Int): CalculatorType {
        val saved = prefs.getString(singleCalcKey(widgetId), CalculatorType.BMI.name)
        return CalculatorType.fromName(saved ?: CalculatorType.BMI.name)
    }

    // ── Last Results ──────────────────────────────────────────────────

    fun saveLastResult(calcType: CalculatorType, result: String, subtitle: String = "") {
        prefs.edit().apply {
            putString(resultKey(calcType.name), result)
            putString(resultSubKey(calcType.name), subtitle)
            apply()
        }
    }

    fun getLastResult(calcType: CalculatorType): String {
        return prefs.getString(resultKey(calcType.name), "--") ?: "--"
    }

    fun getLastResultSubtitle(calcType: CalculatorType): String {
        return prefs.getString(resultSubKey(calcType.name), "Tap to calculate") ?: "Tap to calculate"
    }

    // ── Usage Tracking ────────────────────────────────────────────────

    fun trackUsage(calcType: CalculatorType) {
        val current = prefs.getInt(usageKey(calcType.name), 0)
        prefs.edit().putInt(usageKey(calcType.name), current + 1).apply()
    }

    fun getMostUsedCalculators(count: Int = 4): List<CalculatorType> {
        return CalculatorType.values()
            .sortedByDescending { prefs.getInt(usageKey(it.name), 0) }
            .take(count)
            .let { sorted ->
                // Fill with defaults if not enough tracked
                val defaults = listOf(
                    CalculatorType.BMI,
                    CalculatorType.BLOOD_PRESSURE,
                    CalculatorType.WATER,
                    CalculatorType.CALORIES
                )
                if (sorted.all { prefs.getInt(usageKey(it.name), 0) == 0 }) defaults
                else sorted
            }
    }

    // ── Clean up when widget is removed ──────────────────────────────

    fun clearWidgetPrefs(widgetId: Int) {
        prefs.edit().apply {
            remove(slotKey(widgetId, 1))
            remove(slotKey(widgetId, 2))
            remove(slotKey(widgetId, 3))
            remove(slotKey(widgetId, 4))
            remove(themeKey(widgetId))
            remove(opacityKey(widgetId))
            remove(singleCalcKey(widgetId))
            apply()
        }
    }

    // ── Health Score Storage ──────────────────────────────────────────

    fun saveHealthStats(
        score: Int,
        bmi: Float? = null,
        sys: Int? = null,
        dia: Int? = null,
        waterPct: Int? = null,
        calPct: Int? = null
    ) {
        prefs.edit().apply {
            putInt(KEY_HEALTH_SCORE, score)
            bmi?.let { putFloat(KEY_HEALTH_BMI, it) }
            sys?.let { putInt(KEY_HEALTH_BP_SYS, it) }
            dia?.let { putInt(KEY_HEALTH_BP_DIA, it) }
            waterPct?.let { putInt(KEY_HEALTH_WATER_PCT, it) }
            calPct?.let { putInt(KEY_HEALTH_CALORIES_PCT, it) }
            apply()
        }
    }

    fun getHealthScore(): Int = prefs.getInt(KEY_HEALTH_SCORE, 0)
    fun getHealthBmi(): Float = prefs.getFloat(KEY_HEALTH_BMI, 0f)
    fun getHealthBp(): Pair<Int, Int> = Pair(
        prefs.getInt(KEY_HEALTH_BP_SYS, 0),
        prefs.getInt(KEY_HEALTH_BP_DIA, 0)
    )
    fun getHealthWaterPct(): Int = prefs.getInt(KEY_HEALTH_WATER_PCT, 0)
    fun getHealthCalPct(): Int = prefs.getInt(KEY_HEALTH_CALORIES_PCT, 0)
}
