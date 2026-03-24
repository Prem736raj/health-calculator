package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

data class SmartRecommendation(
    val id: String,
    val emoji: String,
    val title: String,
    val message: String,
    val actionLabel: String,
    val actionRoute: String,
    val priority: RecommendationPriority,
    val type: RecommendationType,
    val color: Color,
    val dismissedUntil: Long? = null
)

enum class RecommendationPriority(val weight: Int) {
    CRITICAL(100),    // Health concerns, emergency
    HIGH(80),         // Important actions needed
    MEDIUM(50),       // Helpful reminders
    LOW(20),          // Nice to have
    CELEBRATION(10)   // Positive reinforcement
}

enum class RecommendationType {
    BMI_CHECK,
    BP_CHECK,
    WATER_REMINDER,
    CALORIE_REMINDER,
    WEIGHT_TREND,
    GOAL_PROGRESS,
    WHR_CHECK,
    HR_CHECK,
    ALL_GOOD,
    STREAK,
    NEW_CALCULATOR,
    PROFILE_INCOMPLETE
}

data class UserHealthContext(
    // BMI data
    val lastBMIValue: Float? = null,
    val lastBMITimestamp: Long? = null,
    val bmiTrend: List<Float> = emptyList(), // last 5 readings
    
    // BP data
    val lastBPSystolic: Int? = null,
    val lastBPDiastolic: Int? = null,
    val lastBPTimestamp: Long? = null,
    val bpReadingsCount: Int = 0,
    
    // Water
    val waterLoggedToday: Boolean = false,
    val waterProgress: Float = 0f, // 0-1
    val waterStreak: Int = 0,
    
    // Calories
    val caloriesLoggedToday: Boolean = false,
    val calorieProgress: Float = 0f,
    
    // Weight/Goals
    val currentWeight: Float? = null,
    val goalWeight: Float? = null,
    val weightTrend: List<Float> = emptyList(), // last 5 readings
    
    // WHR
    val lastWHRValue: Float? = null,
    val lastWHRTimestamp: Long? = null,
    
    // Heart Rate
    val lastRestingHR: Int? = null,
    val lastHRTimestamp: Long? = null,
    
    // Profile
    val profileComplete: Boolean = false,
    val userName: String? = null,
    
    // Usage
    val calculatorsUsed: Set<String> = emptySet(),
    val appOpenedDaysStreak: Int = 0,
    
    // Dismissed recommendations
    val dismissedRecommendations: Map<String, Long> = emptyMap() // id -> dismissedUntil timestamp
)

object SmartRecommendationEngine {

    private const val DAYS_STALE_BMI = 30
    private const val DAYS_STALE_BP = 7
    private const val DAYS_STALE_WHR = 30
    private const val DAYS_STALE_HR = 14
    private const val DISMISS_DURATION_DAYS = 7L

    /**
     * Generate prioritized recommendations based on user context
     */
    fun generateRecommendations(context: UserHealthContext): List<SmartRecommendation> {
        val now = System.currentTimeMillis()
        val recommendations = mutableListOf<SmartRecommendation>()

        // Filter out dismissed recommendations
        fun isNotDismissed(id: String): Boolean {
            val dismissedUntil = context.dismissedRecommendations[id]
            return dismissedUntil == null || dismissedUntil < now
        }

        // 1. Profile incomplete
        if (!context.profileComplete && isNotDismissed("profile_incomplete")) {
            recommendations.add(
                SmartRecommendation(
                    id = "profile_incomplete",
                    emoji = "👤",
                    title = "Complete Your Profile",
                    message = "Add your details to get personalized health insights and auto-fill calculators.",
                    actionLabel = "Set Up Profile",
                    actionRoute = "profile",
                    priority = RecommendationPriority.HIGH,
                    type = RecommendationType.PROFILE_INCOMPLETE,
                    color = Color(0xFF9C27B0)
                )
            )
        }

        // 2. BMI never calculated
        if (context.lastBMITimestamp == null && isNotDismissed("bmi_never")) {
            recommendations.add(
                SmartRecommendation(
                    id = "bmi_never",
                    emoji = "📊",
                    title = "Calculate Your BMI",
                    message = "Know your Body Mass Index — it's a key health indicator. Takes just 30 seconds!",
                    actionLabel = "Check BMI",
                    actionRoute = "bmi_calculator",
                    priority = RecommendationPriority.HIGH,
                    type = RecommendationType.BMI_CHECK,
                    color = Color(0xFF2196F3)
                )
            )
        }

        // 3. BMI stale (not calculated in X days)
        context.lastBMITimestamp?.let { timestamp ->
            val daysSince = daysBetween(timestamp, now)
            if (daysSince >= DAYS_STALE_BMI && isNotDismissed("bmi_stale")) {
                recommendations.add(
                    SmartRecommendation(
                        id = "bmi_stale",
                        emoji = "📊",
                        title = "Time for a BMI Check",
                        message = "It's been $daysSince days since your last BMI reading. Let's see how you're doing!",
                        actionLabel = "Check BMI",
                        actionRoute = "bmi_calculator",
                        priority = RecommendationPriority.MEDIUM,
                        type = RecommendationType.BMI_CHECK,
                        color = Color(0xFF2196F3)
                    )
                )
            }
        }

        // 4. BP never tracked
        if (context.bpReadingsCount == 0 && isNotDismissed("bp_never")) {
            recommendations.add(
                SmartRecommendation(
                    id = "bp_never",
                    emoji = "💓",
                    title = "Start Tracking Blood Pressure",
                    message = "Regular BP monitoring is crucial for heart health. Log your first reading!",
                    actionLabel = "Log BP",
                    actionRoute = "blood_pressure_checker",
                    priority = RecommendationPriority.HIGH,
                    type = RecommendationType.BP_CHECK,
                    color = Color(0xFFE53935)
                )
            )
        }

        // 5. BP stale
        context.lastBPTimestamp?.let { timestamp ->
            val daysSince = daysBetween(timestamp, now)
            if (daysSince >= DAYS_STALE_BP && isNotDismissed("bp_stale")) {
                recommendations.add(
                    SmartRecommendation(
                        id = "bp_stale",
                        emoji = "💓",
                        title = "Log Your Blood Pressure",
                        message = "It's been $daysSince days since your last reading. Regular tracking helps spot trends!",
                        actionLabel = "Log BP",
                        actionRoute = "blood_pressure_checker",
                        priority = RecommendationPriority.MEDIUM,
                        type = RecommendationType.BP_CHECK,
                        color = Color(0xFFE53935)
                    )
                )
            }
        }

        // 6. Water not logged today
        if (!context.waterLoggedToday && isNotDismissed("water_today")) {
            val message = if (context.waterStreak > 0) {
                "Keep your ${context.waterStreak}-day streak going! Log your water intake."
            } else {
                "Stay hydrated! Don't forget to log your water intake today."
            }
            recommendations.add(
                SmartRecommendation(
                    id = "water_today",
                    emoji = "💧",
                    title = "Log Your Water Intake",
                    message = message,
                    actionLabel = "Log Water",
                    actionRoute = "water_intake_calculator",
                    priority = if (context.waterStreak > 3) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM,
                    type = RecommendationType.WATER_REMINDER,
                    color = Color(0xFF03A9F4)
                )
            )
        }

        // 7. Water goal almost reached
        if (context.waterProgress in 0.7f..0.95f && isNotDismissed("water_almost")) {
            val remaining = ((1f - context.waterProgress) * 100).toInt()
            recommendations.add(
                SmartRecommendation(
                    id = "water_almost",
                    emoji = "🎯",
                    title = "Almost There!",
                    message = "You're $remaining% away from your water goal. Just a bit more to go!",
                    actionLabel = "Log More",
                    actionRoute = "water_intake_calculator",
                    priority = RecommendationPriority.LOW,
                    type = RecommendationType.GOAL_PROGRESS,
                    color = Color(0xFF4CAF50)
                )
            )
        }

        // 8. Calories not logged today
        if (!context.caloriesLoggedToday && context.calorieProgress < 0.1f && isNotDismissed("calories_today")) {
            recommendations.add(
                SmartRecommendation(
                    id = "calories_today",
                    emoji = "🔥",
                    title = "Track Your Meals",
                    message = "Log what you eat to stay on track with your nutrition goals.",
                    actionLabel = "Log Food",
                    actionRoute = "calorie_calculator",
                    priority = RecommendationPriority.MEDIUM,
                    type = RecommendationType.CALORIE_REMINDER,
                    color = Color(0xFFFF9800)
                )
            )
        }

        // 9. Weight trending up (if goal is weight loss)
        if (context.weightTrend.size >= 3 && context.goalWeight != null) {
            val isGainingWeight = context.weightTrend.takeLast(3).zipWithNext().all { (a, b) -> b > a }
            val currentWeight = context.currentWeight ?: context.weightTrend.lastOrNull()
            
            if (isGainingWeight && currentWeight != null && currentWeight > context.goalWeight && isNotDismissed("weight_trending_up")) {
                recommendations.add(
                    SmartRecommendation(
                        id = "weight_trending_up",
                        emoji = "📈",
                        title = "Weight Trending Up",
                        message = "Your weight has increased recently. Would you like to review your calorie plan?",
                        actionLabel = "Review Plan",
                        actionRoute = "calorie_calculator",
                        priority = RecommendationPriority.HIGH,
                        type = RecommendationType.WEIGHT_TREND,
                        color = Color(0xFFFF9800)
                    )
                )
            }
        }

        // 10. Goal close to achievement
        if (context.currentWeight != null && context.goalWeight != null) {
            val diff = kotlin.math.abs(context.currentWeight - context.goalWeight)
            if (diff <= 2f && diff > 0 && isNotDismissed("goal_close")) {
                recommendations.add(
                    SmartRecommendation(
                        id = "goal_close",
                        emoji = "🏆",
                        title = "Almost at Your Goal!",
                        message = "You're only ${"%.1f".format(diff)} kg away from your goal weight. Keep going!",
                        actionLabel = "View Progress",
                        actionRoute = "bmi_calculator",
                        priority = RecommendationPriority.CELEBRATION,
                        type = RecommendationType.GOAL_PROGRESS,
                        color = Color(0xFF4CAF50)
                    )
                )
            }
        }

        // 11. Goal achieved!
        if (context.currentWeight != null && context.goalWeight != null) {
            val diff = kotlin.math.abs(context.currentWeight - context.goalWeight)
            if (diff <= 0.5f && isNotDismissed("goal_achieved")) {
                recommendations.add(
                    SmartRecommendation(
                        id = "goal_achieved",
                        emoji = "🎉",
                        title = "Congratulations!",
                        message = "You've reached your goal weight! Amazing work! Set a new goal to keep progressing.",
                        actionLabel = "Update Goal",
                        actionRoute = "profile",
                        priority = RecommendationPriority.CELEBRATION,
                        type = RecommendationType.GOAL_PROGRESS,
                        color = Color(0xFF4CAF50)
                    )
                )
            }
        }

        // 12. WHR never calculated
        if (context.lastWHRTimestamp == null && context.calculatorsUsed.size >= 2 && isNotDismissed("whr_never")) {
            recommendations.add(
                SmartRecommendation(
                    id = "whr_never",
                    emoji = "📏",
                    title = "Try Waist-Hip Ratio",
                    message = "WHR is a better predictor of health risks than BMI alone. Give it a try!",
                    actionLabel = "Calculate WHR",
                    actionRoute = "whr_calculator",
                    priority = RecommendationPriority.LOW,
                    type = RecommendationType.WHR_CHECK,
                    color = Color(0xFF9C27B0)
                )
            )
        }

        // 13. Heart rate zones not calculated
        if (context.lastHRTimestamp == null && context.calculatorsUsed.size >= 3 && isNotDismissed("hr_never")) {
            recommendations.add(
                SmartRecommendation(
                    id = "hr_never",
                    emoji = "❤️",
                    title = "Discover Your Heart Rate Zones",
                    message = "Optimize your workouts by training in the right heart rate zones.",
                    actionLabel = "Calculate Zones",
                    actionRoute = "heart_rate_zone_calculator",
                    priority = RecommendationPriority.LOW,
                    type = RecommendationType.HR_CHECK,
                    color = Color(0xFFE91E63)
                )
            )
        }

        // 14. Streak celebration
        if (context.waterStreak >= 7 && isNotDismissed("water_streak_${context.waterStreak / 7}")) {
            recommendations.add(
                SmartRecommendation(
                    id = "water_streak_${context.waterStreak / 7}",
                    emoji = "🔥",
                    title = "${context.waterStreak}-Day Hydration Streak!",
                    message = "Amazing consistency! You've met your water goal for ${context.waterStreak} days in a row!",
                    actionLabel = "Keep Going",
                    actionRoute = "water_intake_calculator",
                    priority = RecommendationPriority.CELEBRATION,
                    type = RecommendationType.STREAK,
                    color = Color(0xFFFF5722)
                )
            )
        }

        // 15. All metrics good
        val allGood = context.lastBMIValue != null && 
                      context.lastBMIValue in 18.5f..24.9f &&
                      context.lastBPSystolic != null && context.lastBPSystolic < 130 &&
                      context.lastBPDiastolic != null && context.lastBPDiastolic < 85 &&
                      context.waterProgress >= 0.8f &&
                      context.calorieProgress in 0.85f..1.15f

        if (allGood && isNotDismissed("all_good")) {
            recommendations.add(
                SmartRecommendation(
                    id = "all_good",
                    emoji = "⭐",
                    title = "Great Job!",
                    message = "All your health metrics are looking excellent! Keep up the fantastic work!",
                    actionLabel = "View Summary",
                    actionRoute = "home",
                    priority = RecommendationPriority.CELEBRATION,
                    type = RecommendationType.ALL_GOOD,
                    color = Color(0xFF4CAF50)
                )
            )
        }

        // Sort by priority (highest first), then filter duplicates
        return recommendations
            .distinctBy { it.id }
            .sortedByDescending { it.priority.weight }
    }

    /**
     * Calculate days between two timestamps
     */
    private fun daysBetween(from: Long, to: Long): Int {
        return try {
            ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(from),
                Instant.ofEpochMilli(to)
            ).toInt()
        } catch (_: Exception) { 0 }
    }

    /**
     * Calculate dismiss until timestamp (7 days from now)
     */
    fun calculateDismissUntil(): Long {
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DISMISS_DURATION_DAYS)
    }
}
