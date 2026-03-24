package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BadgeType(
    val displayName: String,
    val description: String,
    val requirement: String,
    val icon: String,
    val tier: BadgeTier
) {
    FIRST_DROP(
        "First Drop", "Log your first water entry",
        "Log 1 water entry", "💧", BadgeTier.BRONZE
    ),
    GLASS_HALF_FULL(
        "Glass Half Full", "Reach 50% of your daily goal",
        "Reach 50% of daily goal", "🥛", BadgeTier.BRONZE
    ),
    DAILY_CHAMPION(
        "Daily Champion", "Meet your daily water goal for the first time",
        "Complete 100% of daily goal", "🏅", BadgeTier.SILVER
    ),
    THREE_DAY_STREAK(
        "Hydration Habit", "3 consecutive days at goal",
        "3-day streak", "🌱", BadgeTier.BRONZE
    ),
    SEVEN_DAY_STREAK(
        "7-Day Streak", "A full week of hydration",
        "7-day streak", "🔥", BadgeTier.SILVER
    ),
    FOURTEEN_DAY_STREAK(
        "Two Week Warrior", "14 consecutive days at goal",
        "14-day streak", "⚡", BadgeTier.SILVER
    ),
    MONTHLY_MASTER(
        "Monthly Master", "30 days of consistent hydration",
        "30-day streak", "👑", BadgeTier.GOLD
    ),
    SIXTY_DAY_STREAK(
        "Hydration Expert", "60 consecutive days at goal",
        "60-day streak", "💎", BadgeTier.GOLD
    ),
    NINETY_DAY_STREAK(
        "Quarterly Champion", "90 days of perfect hydration",
        "90-day streak", "🏆", BadgeTier.PLATINUM
    ),
    HYDRATION_HERO(
        "Hydration Hero", "Track water intake for 100 total days",
        "Log water on 100 different days", "🦸", BadgeTier.GOLD
    ),
    DIAMOND_DRINKER(
        "Diamond Drinker", "365 consecutive days at goal",
        "365-day streak", "💠", BadgeTier.DIAMOND
    ),
    EARLY_BIRD(
        "Early Bird", "Log water before 8 AM",
        "Log water before 8 AM", "🌅", BadgeTier.BRONZE
    ),
    NIGHT_OWL(
        "Night Hydrator", "Log water after 9 PM",
        "Log water after 9 PM", "🌙", BadgeTier.BRONZE
    ),
    OVERACHIEVER(
        "Overachiever", "Exceed daily goal by 50%",
        "Reach 150% of daily goal", "🚀", BadgeTier.SILVER
    ),
    CONSISTENCY_KING(
        "Consistency King", "Score A grade 7 days in a row",
        "Get A grade for 7 consecutive days", "🎯", BadgeTier.GOLD
    )
}

enum class BadgeTier(val label: String, val accentColor: Long) {
    BRONZE("Bronze", 0xFFCD7F32),
    SILVER("Silver", 0xFFC0C0C0),
    GOLD("Gold", 0xFFFFD700),
    PLATINUM("Platinum", 0xFFE5E4E2),
    DIAMOND("Diamond", 0xFFB9F2FF)
}

@Entity(tableName = "water_badges")
data class EarnedBadge(
    @PrimaryKey val badgeType: String,
    val earnedTimestamp: Long = System.currentTimeMillis(),
    val seen: Boolean = false
)

@Entity(tableName = "water_streak_data")
data class WaterStreakData(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastGoalMetDate: String = "",
    val totalDaysTracked: Int = 0,
    val totalDaysGoalMet: Int = 0,
    val consecutiveAGrades: Int = 0
)

data class HydrationScore(
    val totalScore: Int,
    val grade: String,
    val gradeEmoji: String,
    val goalPoints: Int,
    val consistencyPoints: Int,
    val earlyStartPoints: Int,
    val trackingPoints: Int,
    val breakdown: List<ScoreBreakdownItem>
)

data class ScoreBreakdownItem(
    val label: String,
    val points: Int,
    val maxPoints: Int,
    val earned: Boolean
)
