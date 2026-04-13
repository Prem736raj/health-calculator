package com.health.calculator.bmi.tracker.widget

import android.graphics.Color

enum class WidgetTheme(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    TRANSPARENT("Transparent"),
    SYSTEM("Follow System");

    companion object {
        fun fromName(name: String) = values().find { it.name == name } ?: SYSTEM
        fun spinnerLabels() = values().map { it.label }.toTypedArray()
    }
}

object WidgetThemeHelper {

    data class ThemeColors(
        val backgroundColor: Int,
        val cardBackground: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accentColor: Int,
        val borderColor: Int
    )

    fun getColors(theme: WidgetTheme, opacity: Int = 100): ThemeColors {
        val alpha = ((opacity / 100f) * 255).toInt().coerceIn(0, 255)

        return when (theme) {
            WidgetTheme.LIGHT -> ThemeColors(
                backgroundColor = withAlpha(Color.parseColor("#FAFEFF"), alpha),
                cardBackground  = withAlpha(Color.parseColor("#FFFFFF"), alpha),
                textPrimary     = Color.parseColor("#1A237E"),
                textSecondary   = Color.parseColor("#607D8B"),
                accentColor     = Color.parseColor("#2196F3"),
                borderColor     = Color.parseColor("#E3EDF7")
            )
            WidgetTheme.DARK -> ThemeColors(
                backgroundColor = withAlpha(Color.parseColor("#1A2332"), alpha),
                cardBackground  = withAlpha(Color.parseColor("#1E2D40"), alpha),
                textPrimary     = Color.parseColor("#E3F2FD"),
                textSecondary   = Color.parseColor("#90A4AE"),
                accentColor     = Color.parseColor("#64B5F6"),
                borderColor     = Color.parseColor("#263547")
            )
            WidgetTheme.TRANSPARENT -> ThemeColors(
                backgroundColor = withAlpha(Color.parseColor("#1A2332"), (alpha * 0.3f).toInt()),
                cardBackground  = withAlpha(Color.parseColor("#FFFFFF"), (alpha * 0.15f).toInt()),
                textPrimary     = Color.parseColor("#FFFFFF"),
                textSecondary   = Color.parseColor("#B0BEC5"),
                accentColor     = Color.parseColor("#64B5F6"),
                borderColor     = withAlpha(Color.parseColor("#FFFFFF"), 40)
            )
            WidgetTheme.SYSTEM -> ThemeColors(
                // Default to light; actual system detection happens via context
                backgroundColor = withAlpha(Color.parseColor("#FAFEFF"), alpha),
                cardBackground  = withAlpha(Color.parseColor("#FFFFFF"), alpha),
                textPrimary     = Color.parseColor("#1A237E"),
                textSecondary   = Color.parseColor("#607D8B"),
                accentColor     = Color.parseColor("#2196F3"),
                borderColor     = Color.parseColor("#E3EDF7")
            )
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun applyThemeToBackground(
        views: android.widget.RemoteViews,
        rootId: Int,
        theme: ThemeColors
    ) {
        views.setInt(rootId, "setBackgroundColor", theme.backgroundColor)
    }
}
