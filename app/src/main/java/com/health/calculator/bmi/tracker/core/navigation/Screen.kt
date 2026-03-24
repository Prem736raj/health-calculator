// app/src/main/java/com/health/calculator/bmi/tracker/core/navigation/Screen.kt

package com.health.calculator.bmi.tracker.core.navigation

/**
 * Sealed class representing all screens/destinations in the app.
 * Routes are organized hierarchically for clarity.
 */
sealed class Screen(val route: String) {

    // ── Splash & Onboarding ──────────────────────────────────────────
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")

    // ── Main Bottom Navigation Screens ───────────────────────────────
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")

    // ── Calculator Screens ───────────────────────────────────────────
    data object BmiCalculator : Screen("calculator/bmi")
    data object BmrCalculator : Screen("calculator/bmr")
    data object BloodPressureCalculator : Screen("calculator/blood_pressure")
    data object WaistToHipCalculator : Screen("calculator/waist_hip")
    data object WaterIntakeCalculator : Screen("calculator/water_intake")
    data object MetabolicSyndromeCalculator : Screen("calculator/metabolic_syndrome")
    data object BsaCalculator : Screen("calculator/bsa")
    data object IdealWeightCalculator : Screen("ideal_body_weight")
    data object DailyCalorieCalculator : Screen("calculator/daily_calorie")
    data object HeartRateZoneCalculator : Screen("calculator/heart_rate_zone")
    data object FoodLog : Screen("food_log")
    data object CalorieHistory : Screen("calorie_history")

    // ── Result / Detail Screens ──────────────────────────────────────
    data object CalculationDetail : Screen("calculation/{calculationId}") {
        fun createRoute(calculationId: String): String = "calculation/$calculationId"
        fun createWhrResultRoute(waistCm: Float, hipCm: Float, gender: String, age: Int): String {
            return "calculation/whr_result?waist=$waistCm&hip=$hipCm&gender=$gender&age=$age"
        }
    }

    // ── Feature Screens ──────────────────────────────────────────────
    data object WaterTracker : Screen("water_tracker")
    data object BloodPressureLog : Screen("blood_pressure_log")
    data object BloodPressureTrends : Screen("blood_pressure_trends")
    data object BloodPressureReminders : Screen("blood_pressure_reminders")
    data object BloodPressureExport : Screen("blood_pressure_export")
    data object BloodPressureEducation : Screen("blood_pressure_education")
    data object HealthArticles : Screen("health_articles")
    data object Achievements : Screen("achievements")
    data object ExportData : Screen("export_data")
    data object Backup : Screen("backup")
    data object DataManagement : Screen("data_management")
    data object HealthConnections : Screen("health_connections")
    data object WeightTracking : Screen("weight_tracking")
    data object Reminders : Screen("reminders")
    data object WeeklyReport : Screen("weekly_report")

    companion object {
        /**
         * All bottom navigation routes for easy checking
         */
        val bottomNavRoutes = listOf(
            Home.route,
            History.route,
            Profile.route,
            Settings.route
        )

        /**
         * Check if a route is a bottom navigation destination
         */
        fun isBottomNavRoute(route: String?): Boolean {
            return route in bottomNavRoutes
        }
    }
}
