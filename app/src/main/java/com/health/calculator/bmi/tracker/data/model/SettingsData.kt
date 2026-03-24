package com.health.calculator.bmi.tracker.data.model

/**
 * Represents all user-configurable app settings.
 * Stored locally using DataStore Preferences.
 *
 * These settings control app appearance, behavior, notification preferences,
 * and unit display across all calculators.
 */
data class SettingsData(
    // ── General ───────────────────────────────────────────────────────
    /** Unit system preference for displaying measurements */
    val unitSystem: UnitSystem = UnitSystem.METRIC,

    /** App theme preference */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // ── Notifications ─────────────────────────────────────────────────
    /** Master toggle for all reminders */
    val remindersEnabled: Boolean = false,

    /** Daily water intake reminder */
    val waterReminderEnabled: Boolean = false,

    /** Weekly weight tracking reminder */
    val weightReminderEnabled: Boolean = false,

    // ── Metadata ──────────────────────────────────────────────────────
    /** Timestamp of last settings change */
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

/**
 * Unit system preference that affects how measurements are displayed
 * throughout all calculators and screens.
 */
enum class UnitSystem(
    val displayName: String,
    val description: String
) {
    METRIC(
        displayName = "Metric",
        description = "kg, cm, °C"
    ),
    IMPERIAL(
        displayName = "Imperial",
        description = "lbs, ft/in, °F"
    ),
    CUSTOM(
        displayName = "Custom",
        description = "Mix units per field"
    )
}

/**
 * Theme mode preference controlling the app's visual appearance.
 * SYSTEM follows the device's system-wide dark mode setting.
 */
enum class ThemeMode(
    val displayName: String,
    val emoji: String
) {
    LIGHT(
        displayName = "Light",
        emoji = "☀️"
    ),
    DARK(
        displayName = "Dark",
        emoji = "🌙"
    ),
    SYSTEM(
        displayName = "System",
        emoji = "📱"
    )
}
