package com.health.calculator.bmi.tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_milestones")
data class HealthMilestone(
    @PrimaryKey
    val milestoneType: String, // MilestoneType.name
    val achievedAt: Long = System.currentTimeMillis(),
    val details: String? = null,
    val isCelebrated: Boolean = false
)

enum class MilestoneType(
    val displayName: String,
    val icon: String,
    val description: String,
    val category: MilestoneCategory
) {
    FIRST_CALCULATION(
        displayName = "First Calculation",
        icon = "🎯",
        description = "Completed your first health calculation",
        category = MilestoneCategory.GETTING_STARTED
    ),
    PROFILE_COMPLETE(
        displayName = "Profile Complete",
        icon = "✅",
        description = "Filled in all profile information",
        category = MilestoneCategory.GETTING_STARTED
    ),
    FIRST_GOAL_SET(
        displayName = "Goal Setter",
        icon = "🎯",
        description = "Set your first health goal",
        category = MilestoneCategory.GETTING_STARTED
    ),
    ONE_WEEK_TRACKING(
        displayName = "One Week Strong",
        icon = "📅",
        description = "Tracked your health for 7 consecutive days",
        category = MilestoneCategory.CONSISTENCY
    ),
    TWO_WEEKS_TRACKING(
        displayName = "Two Week Warrior",
        icon = "💪",
        description = "Tracked your health for 14 consecutive days",
        category = MilestoneCategory.CONSISTENCY
    ),
    ONE_MONTH_ACTIVE(
        displayName = "Monthly Champion",
        icon = "🗓️",
        description = "Active for 30 days",
        category = MilestoneCategory.CONSISTENCY
    ),
    THREE_MONTHS_ACTIVE(
        displayName = "Quarter Master",
        icon = "🏅",
        description = "Active for 90 days — true commitment!",
        category = MilestoneCategory.CONSISTENCY
    ),
    SIX_MONTHS_ACTIVE(
        displayName = "Half Year Hero",
        icon = "⭐",
        description = "Active for 180 days",
        category = MilestoneCategory.CONSISTENCY
    ),
    ONE_YEAR_ACTIVE(
        displayName = "Year of Health",
        icon = "👑",
        description = "An entire year of health tracking!",
        category = MilestoneCategory.CONSISTENCY
    ),
    FIRST_GOAL_ACHIEVED(
        displayName = "Goal Achieved!",
        icon = "🏆",
        description = "Reached your first health goal",
        category = MilestoneCategory.ACHIEVEMENTS
    ),
    HEALTH_SCORE_60(
        displayName = "Good Health",
        icon = "💚",
        description = "Health score reached 60+",
        category = MilestoneCategory.ACHIEVEMENTS
    ),
    HEALTH_SCORE_80(
        displayName = "Excellent Health",
        icon = "🌟",
        description = "Health score reached 80+",
        category = MilestoneCategory.ACHIEVEMENTS
    ),
    HEALTH_SCORE_95(
        displayName = "Peak Health",
        icon = "💎",
        description = "Health score reached 95+",
        category = MilestoneCategory.ACHIEVEMENTS
    ),
    ALL_CALCULATORS_USED(
        displayName = "Explorer",
        icon = "🧭",
        description = "Tried all 10 health calculators",
        category = MilestoneCategory.EXPLORATION
    ),
    FIFTY_CALCULATIONS(
        displayName = "50 Calculations",
        icon = "📊",
        description = "Performed 50 health calculations",
        category = MilestoneCategory.EXPLORATION
    ),
    HUNDRED_CALCULATIONS(
        displayName = "Century Club",
        icon = "💯",
        description = "Performed 100 health calculations",
        category = MilestoneCategory.EXPLORATION
    ),
    FIVE_HUNDRED_CALCULATIONS(
        displayName = "Data Master",
        icon = "🔬",
        description = "Performed 500 health calculations",
        category = MilestoneCategory.EXPLORATION
    ),
    BMI_NORMAL(
        displayName = "Healthy BMI",
        icon = "✨",
        description = "Achieved a BMI in the normal range",
        category = MilestoneCategory.HEALTH_WINS
    ),
    BP_OPTIMAL(
        displayName = "Optimal BP",
        icon = "❤️",
        description = "Recorded an optimal blood pressure reading",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WATER_7_DAY_STREAK(
        displayName = "Hydration Week",
        icon = "💧",
        description = "Met water goal 7 days in a row",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WATER_30_DAY_STREAK(
        displayName = "Hydration Month",
        icon = "🌊",
        description = "Met water goal 30 days in a row!",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WEIGHT_GOAL_25_PERCENT(
        displayName = "Quarter Way There",
        icon = "🚀",
        description = "25% progress toward weight goal",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WEIGHT_GOAL_50_PERCENT(
        displayName = "Halfway Mark",
        icon = "🎊",
        description = "50% progress toward weight goal!",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WEIGHT_GOAL_75_PERCENT(
        displayName = "Almost There!",
        icon = "🔥",
        description = "75% progress toward weight goal!",
        category = MilestoneCategory.HEALTH_WINS
    ),
    WEIGHT_GOAL_REACHED(
        displayName = "Weight Goal Reached!",
        icon = "🏆",
        description = "You reached your weight goal!",
        category = MilestoneCategory.HEALTH_WINS
    ),
    SHARED_FIRST_RESULT(
        displayName = "Sharing is Caring",
        icon = "📤",
        description = "Shared a health result for the first time",
        category = MilestoneCategory.SOCIAL
    );
}

enum class MilestoneCategory(val displayName: String, val icon: String) {
    GETTING_STARTED("Getting Started", "🚀"),
    CONSISTENCY("Consistency", "📅"),
    ACHIEVEMENTS("Achievements", "🏆"),
    EXPLORATION("Exploration", "🧭"),
    HEALTH_WINS("Health Wins", "💚"),
    SOCIAL("Social", "👥")
}
