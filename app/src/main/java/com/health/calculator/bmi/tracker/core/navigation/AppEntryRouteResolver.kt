package com.health.calculator.bmi.tracker.core.navigation

/**
 * Converts external app-entry hints from notifications/widgets into an
 * allow-listed in-app navigation route. Unknown input is deliberately ignored.
 */
object AppEntryRouteResolver {

    private const val WELLNESS_WELCOME_ROUTE = "welcome_back"
    private const val APP_LINK_PREFIX = "healthapp://navigate/"

    private val aliases: Map<String, String> = mapOf(
        "dashboard" to Screen.Home.route,
        "home" to Screen.Home.route,
        "track" to Screen.Track.route,
        "calculators" to Screen.Calculators.route,
        "insights" to Screen.Insights.route,
        "history" to Screen.History.route,
        "profile" to Screen.Profile.route,
        "settings" to Screen.Settings.route,
        "bmi_calculator" to Screen.BmiCalculator.route,
        "bmr_calculator" to Screen.BmrCalculator.route,
        "blood_pressure" to Screen.BloodPressureCalculator.route,
        "blood_pressure_checker" to Screen.BloodPressureCalculator.route,
        "blood_pressure_log" to Screen.BloodPressureLog.route,
        "blood_pressure_trends" to Screen.BloodPressureTrends.route,
        "water_intake" to Screen.WaterTracker.route,
        "water_tracking" to Screen.WaterTracker.route,
        "water_tracker" to Screen.WaterTracker.route,
        "calorie_calculator" to Screen.DailyCalorieCalculator.route,
        "heart_rate_calculator" to Screen.HeartRateZoneCalculator.route,
        "weight_tracking" to Screen.WeightTracking.route,
        "health_connections" to Screen.HealthConnections.route,
        "reminders" to Screen.Reminders.route,
        "weekly_report" to Screen.WeeklyReport.route,
        "ai_coach" to Screen.AiCoach.route,
        WELLNESS_WELCOME_ROUTE to WELLNESS_WELCOME_ROUTE
    )

    private val allowedRoutes: Set<String> = aliases.values.toSet() + setOf(
        Screen.FoodLog.route,
        Screen.Achievements.route,
        Screen.ExportData.route,
        Screen.DataManagement.route
    )

    fun resolve(
        dataUri: String? = null,
        navigateTo: String? = null,
        navigateRoute: String? = null,
        showWelcomeBack: Boolean = false
    ): String? {
        if (showWelcomeBack) return WELLNESS_WELCOME_ROUTE

        val deepLinkTarget = dataUri
            ?.takeIf { it.startsWith(APP_LINK_PREFIX, ignoreCase = true) }
            ?.substring(APP_LINK_PREFIX.length)
            ?.substringBefore('?')
            ?.substringBefore('#')

        return listOf(deepLinkTarget, navigateRoute, navigateTo)
            .firstNotNullOfOrNull(::resolveTarget)
    }

    fun resolveTarget(rawTarget: String?): String? {
        val normalized = rawTarget
            ?.trim()
            ?.trim('/')
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        aliases[normalized]?.let { return it }
        return normalized.takeIf { it in allowedRoutes }
    }
}

