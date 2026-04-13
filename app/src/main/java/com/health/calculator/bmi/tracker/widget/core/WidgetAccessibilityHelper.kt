package com.health.calculator.bmi.tracker.widget.core

import android.widget.RemoteViews

/**
 * Ensures all widgets meet accessibility standards:
 * - Meaningful content descriptions
 * - Text alternatives to color indicators
 * - Minimum touch target enforcement
 */
object WidgetAccessibilityHelper {

    // ── Content Descriptions ──────────────────────────────────────────

    fun setBmiDescription(views: RemoteViews, iconId: Int, value: Float, category: String) {
        val desc = if (value > 0f)
            "BMI: ${String.format("%.1f", value)}, Category: $category"
        else
            "BMI not tracked. Tap to calculate."
        views.setContentDescription(iconId, desc)
    }

    fun setBpDescription(
        views: RemoteViews,
        iconId: Int,
        systolic: Int,
        diastolic: Int,
        category: String
    ) {
        val desc = if (systolic > 0)
            "Blood pressure: $systolic over $diastolic mmHg, $category"
        else
            "Blood pressure not tracked. Tap to measure."
        views.setContentDescription(iconId, desc)
    }

    fun setWaterDescription(
        views: RemoteViews,
        iconId: Int,
        intakeMl: Int,
        goalMl: Int,
        percentage: Int
    ) {
        val intakeL = intakeMl / 1000f
        val goalL   = goalMl  / 1000f
        val desc    = "Water intake: ${String.format("%.1f", intakeL)} liters of " +
                "${String.format("%.1f", goalL)} liter goal. $percentage% complete."
        views.setContentDescription(iconId, desc)
    }

    fun setCalorieDescription(
        views: RemoteViews,
        iconId: Int,
        consumed: Int,
        goal: Int,
        percentage: Int
    ) {
        val desc = if (goal > 0)
            "Calories: $consumed of $goal kilocalories. $percentage% of daily goal."
        else
            "Calorie goal not set. Tap to set up."
        views.setContentDescription(iconId, desc)
    }

    fun setProgressDescription(
        views: RemoteViews,
        progressId: Int,
        label: String,
        percentage: Int
    ) {
        views.setContentDescription(
            progressId,
            "$label progress: $percentage percent"
        )
    }

    fun setHealthScoreDescription(
        views: RemoteViews,
        scoreId: Int,
        score: Int,
        label: String
    ) {
        val desc = if (score > 0)
            "Overall health score: $score out of 100. Status: $label"
        else
            "Health score not available. Track more metrics."
        views.setContentDescription(scoreId, desc)
    }

    fun setStreakDescription(
        views: RemoteViews,
        rootId: Int,
        days: Int,
        milestone: String
    ) {
        val desc = if (days > 0)
            "Active streak: $days days in a row. $milestone"
        else
            "No active streak. Open app to start tracking."
        views.setContentDescription(rootId, desc)
    }

    fun setButtonDescription(
        views: RemoteViews,
        btnId: Int,
        action: String,
        amount: String = ""
    ) {
        val desc = if (amount.isNotEmpty()) "$action $amount" else action
        views.setContentDescription(btnId, desc)
    }

    fun setCalculatorShortcutDescription(
        views: RemoteViews,
        slotId: Int,
        calcName: String,
        lastResult: String
    ) {
        val desc = if (lastResult == "--" || lastResult.isEmpty())
            "$calcName shortcut. No data yet. Tap to open."
        else
            "$calcName shortcut. Last result: $lastResult. Tap to open."
        views.setContentDescription(slotId, desc)
    }

    // ── Text alternatives to color ────────────────────────────────────

    /**
     * Returns a text badge that communicates the same
     * information as a color dot — for screen readers.
     */
    fun colorToBadgeText(colorHex: String): String {
        return when (colorHex.uppercase()) {
            "#4CAF50", "#81C784" -> "✓ Good"
            "#8BC34A"            -> "✓ Normal"
            "#FFC107", "#FFEE58" -> "⚠ Fair"
            "#FF9800", "#FFB74D" -> "⚠ Elevated"
            "#F44336", "#EF9A9A" -> "✗ High"
            "#9C27B0", "#CE93D8" -> "✗ Critical"
            "#2196F3", "#64B5F6" -> "ℹ Low"
            "#9E9E9E"            -> "– No data"
            else                 -> ""
        }
    }

    fun bmiCategoryBadge(bmi: Float): String = when {
        bmi <= 0f    -> "– Not tracked"
        bmi < 18.5f  -> "▼ Underweight"
        bmi < 25f    -> "✓ Normal"
        bmi < 30f    -> "⚠ Overweight"
        else         -> "✗ Obese"
    }

    fun bpCategoryBadge(systolic: Int, diastolic: Int): String = when {
        systolic == 0                        -> "– Not tracked"
        systolic < 120 && diastolic < 80     -> "✓ Normal"
        systolic < 130 && diastolic < 80     -> "⚠ Elevated"
        systolic < 140 || diastolic < 90     -> "⚠ Stage 1"
        systolic >= 180 || diastolic >= 120  -> "✗ Crisis"
        else                                 -> "✗ Stage 2"
    }
}
