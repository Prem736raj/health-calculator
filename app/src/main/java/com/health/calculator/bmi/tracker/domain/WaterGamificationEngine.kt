package com.health.calculator.bmi.tracker.domain

import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import java.text.SimpleDateFormat
import java.util.*

class WaterGamificationEngine(
    private val gamificationRepo: WaterGamificationRepository,
    private val waterRepo: WaterIntakeRepository
) {
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun checkAndAwardBadges(
        currentMl: Int,
        goalMl: Int,
        todayLogCount: Int,
        streakData: WaterStreakData,
        firstLogHour: Int?
    ): List<BadgeType> {
        val newBadges = mutableListOf<BadgeType>()

        // First Drop
        if (todayLogCount >= 1) {
            if (gamificationRepo.earnBadge(BadgeType.FIRST_DROP.name)) {
                newBadges.add(BadgeType.FIRST_DROP)
            }
        }

        // Glass Half Full
        if (goalMl > 0 && currentMl >= goalMl / 2) {
            if (gamificationRepo.earnBadge(BadgeType.GLASS_HALF_FULL.name)) {
                newBadges.add(BadgeType.GLASS_HALF_FULL)
            }
        }

        // Daily Champion
        if (currentMl >= goalMl) {
            if (gamificationRepo.earnBadge(BadgeType.DAILY_CHAMPION.name)) {
                newBadges.add(BadgeType.DAILY_CHAMPION)
            }
        }

        // Overachiever
        if (goalMl > 0 && currentMl >= (goalMl * 1.5f).toInt()) {
            if (gamificationRepo.earnBadge(BadgeType.OVERACHIEVER.name)) {
                newBadges.add(BadgeType.OVERACHIEVER)
            }
        }

        // Early Bird
        if (firstLogHour != null && firstLogHour < 8) {
            if (gamificationRepo.earnBadge(BadgeType.EARLY_BIRD.name)) {
                newBadges.add(BadgeType.EARLY_BIRD)
            }
        }

        // Night Owl
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 21 && todayLogCount > 0) {
            if (gamificationRepo.earnBadge(BadgeType.NIGHT_OWL.name)) {
                newBadges.add(BadgeType.NIGHT_OWL)
            }
        }

        // Streak-based badges
        val streak = streakData.currentStreak
        val streakBadges = mapOf(
            3 to BadgeType.THREE_DAY_STREAK,
            7 to BadgeType.SEVEN_DAY_STREAK,
            14 to BadgeType.FOURTEEN_DAY_STREAK,
            30 to BadgeType.MONTHLY_MASTER,
            60 to BadgeType.SIXTY_DAY_STREAK,
            90 to BadgeType.NINETY_DAY_STREAK,
            365 to BadgeType.DIAMOND_DRINKER
        )
        streakBadges.forEach { (requiredStreak, badge) ->
            if (streak >= requiredStreak) {
                if (gamificationRepo.earnBadge(badge.name)) {
                    newBadges.add(badge)
                }
            }
        }

        // Hydration Hero (100 total days)
        if (streakData.totalDaysTracked >= 100) {
            if (gamificationRepo.earnBadge(BadgeType.HYDRATION_HERO.name)) {
                newBadges.add(BadgeType.HYDRATION_HERO)
            }
        }

        // Consistency King (7 consecutive A grades)
        if (streakData.consecutiveAGrades >= 7) {
            if (gamificationRepo.earnBadge(BadgeType.CONSISTENCY_KING.name)) {
                newBadges.add(BadgeType.CONSISTENCY_KING)
            }
        }

        return newBadges
    }

    suspend fun updateStreak(currentMl: Int, goalMl: Int, score: HydrationScore): WaterStreakData {
        val existing = gamificationRepo.getStreakData()
        val todayKey = dateKeyFormat.format(Date())
        val metGoalToday = currentMl >= goalMl

        val newData = if (metGoalToday && existing.lastGoalMetDate != todayKey) {
            // Check if yesterday was the last date
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = dateKeyFormat.format(cal.time)

            val isConsecutive = existing.lastGoalMetDate == yesterdayKey ||
                    existing.currentStreak == 0

            val newStreak = if (isConsecutive) existing.currentStreak + 1 else 1
            val newAGrades = if (score.grade == "A") existing.consecutiveAGrades + 1 else 0

            existing.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(existing.longestStreak, newStreak),
                lastGoalMetDate = todayKey,
                totalDaysGoalMet = existing.totalDaysGoalMet + 1,
                consecutiveAGrades = newAGrades
            )
        } else if (!metGoalToday && existing.lastGoalMetDate == todayKey) {
            existing // Already recorded for today
        } else {
            // Check if streak should break
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = dateKeyFormat.format(cal.time)

            if (existing.lastGoalMetDate != todayKey && existing.lastGoalMetDate != yesterdayKey
                && existing.currentStreak > 0) {
                existing.copy(
                    currentStreak = 0,
                    consecutiveAGrades = 0
                )
            } else {
                existing
            }
        }

        gamificationRepo.updateStreakData(newData)
        return newData
    }

    fun calculateHydrationScore(
        currentMl: Int,
        goalMl: Int,
        logs: List<com.health.calculator.bmi.tracker.data.model.WaterIntakeLog>
    ): HydrationScore {
        if (logs.isEmpty()) {
            return HydrationScore(
                totalScore = 0, grade = "F", gradeEmoji = "😟",
                goalPoints = 0, consistencyPoints = 0,
                earlyStartPoints = 0, trackingPoints = 0,
                breakdown = createBreakdown(0, 0, 0, 0)
            )
        }

        // 1. Goal completion (50 points)
        val goalPoints = if (goalMl > 0) {
            val pct = (currentMl.toFloat() / goalMl).coerceAtMost(1f)
            (pct * 50).toInt()
        } else 0

        // 2. Consistency throughout day (25 points)
        val consistencyPoints = calculateConsistencyScore(logs)

        // 3. Early start (15 points)
        val earlyStartPoints = calculateEarlyStartScore(logs)

        // 4. Tracking activity (10 points)
        val trackingPoints = when {
            logs.size >= 6 -> 10
            logs.size >= 4 -> 8
            logs.size >= 2 -> 5
            logs.isNotEmpty() -> 3
            else -> 0
        }

        val totalScore = (goalPoints + consistencyPoints + earlyStartPoints + trackingPoints)
            .coerceIn(0, 100)

        val (grade, emoji) = when {
            totalScore >= 90 -> "A" to "🌟"
            totalScore >= 80 -> "B+" to "😊"
            totalScore >= 70 -> "B" to "👍"
            totalScore >= 60 -> "C+" to "😐"
            totalScore >= 50 -> "C" to "🤔"
            totalScore >= 40 -> "D" to "😕"
            else -> "F" to "😟"
        }

        return HydrationScore(
            totalScore = totalScore,
            grade = grade,
            gradeEmoji = emoji,
            goalPoints = goalPoints,
            consistencyPoints = consistencyPoints,
            earlyStartPoints = earlyStartPoints,
            trackingPoints = trackingPoints,
            breakdown = createBreakdown(goalPoints, consistencyPoints, earlyStartPoints, trackingPoints)
        )
    }

    private fun calculateConsistencyScore(logs: List<com.health.calculator.bmi.tracker.data.model.WaterIntakeLog>): Int {
        if (logs.size < 2) return 5

        val cal = Calendar.getInstance()
        val hourBuckets = mutableSetOf<Int>()

        logs.forEach { log ->
            cal.timeInMillis = log.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourBuckets.add(hour / 3) // 3-hour buckets (0-2, 3-5, 6-8, 9-11, 12-14, 15-17, 18-20, 21-23)
        }

        // More spread across time buckets = better consistency
        return when {
            hourBuckets.size >= 5 -> 25
            hourBuckets.size >= 4 -> 20
            hourBuckets.size >= 3 -> 15
            hourBuckets.size >= 2 -> 10
            else -> 5
        }
    }

    private fun calculateEarlyStartScore(logs: List<com.health.calculator.bmi.tracker.data.model.WaterIntakeLog>): Int {
        if (logs.isEmpty()) return 0

        val cal = Calendar.getInstance()
        cal.timeInMillis = logs.minOf { it.timestamp }
        val firstHour = cal.get(Calendar.HOUR_OF_DAY)

        return when {
            firstHour < 7 -> 15
            firstHour < 8 -> 13
            firstHour < 9 -> 10
            firstHour < 10 -> 7
            firstHour < 12 -> 4
            else -> 0
        }
    }

    private fun createBreakdown(goal: Int, consistency: Int, early: Int, tracking: Int): List<ScoreBreakdownItem> {
        return listOf(
            ScoreBreakdownItem("Met daily goal", goal, 50, goal >= 45),
            ScoreBreakdownItem("Consistent timing", consistency, 25, consistency >= 20),
            ScoreBreakdownItem("Started early", early, 15, early >= 10),
            ScoreBreakdownItem("Active tracking", tracking, 10, tracking >= 8)
        )
    }
}
