// domain/usecases/WeeklyReportGenerator.kt
package com.health.calculator.bmi.tracker.domain.usecases

import com.health.calculator.bmi.tracker.data.local.dao.WeeklyReportDao
import com.health.calculator.bmi.tracker.data.models.*
import com.health.calculator.bmi.tracker.data.models.MilestoneType
import com.health.calculator.bmi.tracker.data.model.ParsedHistoryEntry
import com.health.calculator.bmi.tracker.data.repository.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class WeeklyReportGenerator(
    private val weeklyReportDao: WeeklyReportDao,
    private val historyRepository: HistoryRepository,
    private val weightRepository: WeightRepository,
    private val waterIntakeRepository: WaterIntakeRepository,
    private val foodLogRepository: FoodLogRepository,
    private val milestonesRepository: MilestonesRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun generateReport(
        weekStartDate: Long? = null
    ): WeeklyReportSummary {
        val (weekStart, weekEnd) = getWeekRange(weekStartDate)

        // Check if report already exists
        val existing = weeklyReportDao.getReportForWeek(weekStart)
        if (existing != null) {
            val prevWeekStart = weekStart - 7L * 24 * 60 * 60 * 1000
            val previousReport = weeklyReportDao.getReportForWeek(prevWeekStart)
            return buildSummary(existing, previousReport)
        }

        val prevWeekStart = weekStart - 7L * 24 * 60 * 60 * 1000
        val previousReport = weeklyReportDao.getReportForWeek(prevWeekStart)

        val weightData = getWeightData(weekStart, weekEnd)
        val bmiData = getBmiData(weekStart, weekEnd)
        val bpData = getBpData(weekStart, weekEnd, prevWeekStart, weekStart)
        val waterData = getWaterData(weekStart, weekEnd)
        val calorieData = getCalorieData(weekStart, weekEnd)
        val exerciseData = getExerciseData(weekStart, weekEnd)
        val healthScoreData = getHealthScoreData(weekStart, weekEnd)
        val achievementData = getAchievementData(weekStart, weekEnd)

        val overallGrade = calculateOverallGrade(
            waterDaysGoalMet = waterData.daysGoalMet,
            waterDaysTracked = waterData.daysTracked,
            calorieDaysOnTarget = calorieData.daysOnTarget,
            calorieDaysLogged = calorieData.daysLogged,
            bpReadings = bpData.readingCount,
            exerciseMinutes = exerciseData.first
        )

        val overallMessage = generateOverallMessage(overallGrade)
        val suggestions = generateSuggestions(
            waterData, bpData, calorieData, exerciseData, weightData
        )

        val report = WeeklyReport(
            weekStartDate = weekStart,
            weekEndDate = weekEnd,
            weightStart = weightData.first,
            weightEnd = weightData.last,
            weightChange = weightData.change,
            weightEntryCount = weightData.count,
            avgBmi = bmiData.first,
            bmiReadingCount = bmiData.second,
            bestBmiCategory = bmiData.third,
            avgSystolic = bpData.avgSystolic,
            avgDiastolic = bpData.avgDiastolic,
            bpReadingCount = bpData.readingCount,
            prevWeekAvgSystolic = bpData.prevAvg?.first,
            prevWeekAvgDiastolic = bpData.prevAvg?.second,
            waterDaysGoalMet = waterData.daysGoalMet,
            waterDaysTracked = waterData.daysTracked,
            avgWaterIntakeMl = waterData.avgIntake,
            waterGoalMl = waterData.goal,
            avgCaloriesConsumed = calorieData.avgConsumed,
            calorieTarget = calorieData.target,
            calorieDaysLogged = calorieData.daysLogged,
            calorieDaysOnTarget = calorieData.daysOnTarget,
            exerciseMinutes = exerciseData.first,
            exerciseSessions = exerciseData.second,
            healthScoreStart = healthScoreData.first,
            healthScoreEnd = healthScoreData.second,
            healthScoreChange = healthScoreData.third,
            milestonesEarned = achievementData.first,
            personalRecordsSet = achievementData.second,
            bestAchievement = achievementData.third,
            overallGrade = overallGrade,
            overallMessage = overallMessage,
            focusSuggestions = suggestions.joinToString("|") { "${it.icon}::${it.suggestion}::${it.reason}" }
        )

        val id = weeklyReportDao.insertReport(report)
        val saved = report.copy(id = id)

        return buildSummary(saved, previousReport)
    }

    private fun buildSummary(
        report: WeeklyReport,
        previousReport: WeeklyReport? = null
    ): WeeklyReportSummary {
        val metrics = buildMetricSummaries(report, previousReport)
        val highlights = buildHighlights(report)
        val goals = parseSuggestions(report.focusSuggestions)

        return WeeklyReportSummary(
            report = report,
            metricSummaries = metrics,
            highlights = highlights,
            nextWeekGoals = goals,
            previousWeekReport = previousReport
        )
    }

    private fun buildMetricSummaries(
        report: WeeklyReport,
        previous: WeeklyReport?
    ): List<MetricWeeklySummary> {
        val summaries = mutableListOf<MetricWeeklySummary>()

        // Weight
        if (report.weightEntryCount > 0) {
            val changeStr = report.weightChange?.let {
                val formatted = String.format("%.1f", kotlin.math.abs(it))
                if (it > 0) "+${formatted}kg" else "-${formatted}kg"
            } ?: "—"

            summaries.add(MetricWeeklySummary(
                metricName = "Weight",
                icon = "⚖️",
                currentValue = report.weightEnd?.let { String.format("%.1f kg", it) } ?: "—",
                previousValue = previous?.weightEnd?.let { String.format("%.1f kg", it) },
                trend = when {
                    report.weightChange == null -> MetricTrend.NO_DATA
                    kotlin.math.abs(report.weightChange) < 0.1 -> MetricTrend.STABLE
                    report.weightChange < 0 -> MetricTrend.IMPROVING
                    else -> MetricTrend.DECLINING
                },
                detail = "$changeStr this week • ${report.weightEntryCount} entries",
                isGood = report.weightChange?.let { it <= 0 } ?: true
            ))
        }

        // BMI
        if (report.bmiReadingCount > 0) {
            summaries.add(MetricWeeklySummary(
                metricName = "BMI",
                icon = "📊",
                currentValue = report.avgBmi?.let { String.format("%.1f", it) } ?: "—",
                previousValue = previous?.avgBmi?.let { String.format("%.1f", it) },
                trend = when {
                    previous?.avgBmi == null -> MetricTrend.NEW
                    report.avgBmi == null -> MetricTrend.NO_DATA
                    kotlin.math.abs(report.avgBmi - previous.avgBmi) < 0.3 -> MetricTrend.STABLE
                    report.avgBmi < previous.avgBmi -> MetricTrend.IMPROVING
                    else -> MetricTrend.DECLINING
                },
                detail = "${report.bmiReadingCount} readings • ${report.bestBmiCategory ?: "—"}",
                isGood = report.avgBmi?.let { it in 18.5..24.9 } ?: true
            ))
        }

        // Blood Pressure
        if (report.bpReadingCount > 0) {
            val bpStr = "${report.avgSystolic?.toInt() ?: 0}/${report.avgDiastolic?.toInt() ?: 0}"
            summaries.add(MetricWeeklySummary(
                metricName = "Blood Pressure",
                icon = "❤️",
                currentValue = "$bpStr mmHg",
                previousValue = previous?.let {
                    if (it.bpReadingCount > 0) "${it.avgSystolic?.toInt() ?: 0}/${it.avgDiastolic?.toInt() ?: 0} mmHg"
                    else null
                },
                trend = when {
                    previous?.avgSystolic == null -> MetricTrend.NEW
                    report.avgSystolic == null -> MetricTrend.NO_DATA
                    report.avgSystolic < previous.avgSystolic -> MetricTrend.IMPROVING
                    report.avgSystolic > previous.avgSystolic -> MetricTrend.DECLINING
                    else -> MetricTrend.STABLE
                },
                detail = "${report.bpReadingCount} readings this week",
                isGood = report.avgSystolic?.let { it < 130 } ?: true
            ))
        }

        // Water
        if (report.waterDaysTracked > 0) {
            summaries.add(MetricWeeklySummary(
                metricName = "Water Intake",
                icon = "💧",
                currentValue = "${report.waterDaysGoalMet}/7 days",
                previousValue = previous?.let { "${it.waterDaysGoalMet}/7 days" },
                trend = when {
                    previous == null -> MetricTrend.NEW
                    report.waterDaysGoalMet > previous.waterDaysGoalMet -> MetricTrend.IMPROVING
                    report.waterDaysGoalMet < previous.waterDaysGoalMet -> MetricTrend.DECLINING
                    else -> MetricTrend.STABLE
                },
                detail = "Avg ${report.avgWaterIntakeMl}ml/day • Goal: ${report.waterGoalMl}ml",
                isGood = report.waterDaysGoalMet >= 5
            ))
        }

        // Calories
        if (report.calorieDaysLogged > 0) {
            summaries.add(MetricWeeklySummary(
                metricName = "Calories",
                icon = "🍽️",
                currentValue = "${report.avgCaloriesConsumed} cal/day",
                previousValue = previous?.let {
                    if (it.calorieDaysLogged > 0) "${it.avgCaloriesConsumed} cal/day" else null
                },
                trend = when {
                    previous == null || previous.calorieDaysLogged == 0 -> MetricTrend.NEW
                    kotlin.math.abs(report.avgCaloriesConsumed - previous.avgCaloriesConsumed) < 100 -> MetricTrend.STABLE
                    report.avgCaloriesConsumed < report.calorieTarget && report.avgCaloriesConsumed > report.calorieTarget - 500 -> MetricTrend.IMPROVING
                    else -> MetricTrend.DECLINING
                },
                detail = "${report.calorieDaysOnTarget}/${report.calorieDaysLogged} days on target",
                isGood = report.calorieDaysOnTarget >= report.calorieDaysLogged / 2
            ))
        }

        // Exercise
        if (report.exerciseMinutes > 0 || report.exerciseSessions > 0) {
            summaries.add(MetricWeeklySummary(
                metricName = "Exercise",
                icon = "🏃",
                currentValue = "${report.exerciseMinutes} min",
                previousValue = previous?.let { "${it.exerciseMinutes} min" },
                trend = when {
                    previous == null -> MetricTrend.NEW
                    report.exerciseMinutes > previous.exerciseMinutes -> MetricTrend.IMPROVING
                    report.exerciseMinutes < previous.exerciseMinutes -> MetricTrend.DECLINING
                    else -> MetricTrend.STABLE
                },
                detail = "${report.exerciseSessions} sessions • WHO: 150 min/week",
                isGood = report.exerciseMinutes >= 150
            ))
        }

        return summaries
    }

    private fun buildHighlights(report: WeeklyReport): List<WeeklyHighlight> {
        val highlights = mutableListOf<WeeklyHighlight>()

        report.bestAchievement?.let {
            highlights.add(WeeklyHighlight(
                icon = "🏆", title = "Best Achievement", description = it, type = HighlightType.ACHIEVEMENT
            ))
        }

        if (report.personalRecordsSet > 0) {
            highlights.add(WeeklyHighlight(
                icon = "🥇", title = "Personal Records",
                description = "${report.personalRecordsSet} new record${if (report.personalRecordsSet > 1) "s" else ""} set!",
                type = HighlightType.RECORD
            ))
        }

        if (report.milestonesEarned > 0) {
            highlights.add(WeeklyHighlight(
                icon = "🏅", title = "Milestones Earned",
                description = "${report.milestonesEarned} new milestone${if (report.milestonesEarned > 1) "s" else ""} unlocked!",
                type = HighlightType.MILESTONE
            ))
        }

        if (report.waterDaysGoalMet == 7) {
            highlights.add(WeeklyHighlight(
                icon = "💧", title = "Perfect Hydration Week",
                description = "You met your water goal every single day!",
                type = HighlightType.STREAK
            ))
        }

        if (report.healthScoreChange > 0) {
            highlights.add(WeeklyHighlight(
                icon = "📈", title = "Health Score Improved",
                description = "Up ${report.healthScoreChange} points from last week!",
                type = HighlightType.IMPROVEMENT
            ))
        }

        return highlights
    }

    private fun parseSuggestions(encoded: String): List<NextWeekGoal> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split("|").mapIndexedNotNull { index, part ->
            val split = part.split("::")
            if (split.size >= 3) {
                NextWeekGoal(
                    icon = split[0],
                    suggestion = split[1],
                    reason = split[2],
                    priority = index + 1
                )
            } else null
        }
    }

    private fun generateSuggestions(
        waterData: WaterWeekData,
        bpData: BpWeekData,
        calorieData: CalorieWeekData,
        exerciseData: Pair<Int, Int>,
        weightData: WeightWeekData
    ): List<NextWeekGoal> {
        val suggestions = mutableListOf<NextWeekGoal>()

        if (waterData.daysGoalMet < 5) {
            suggestions.add(NextWeekGoal(
                icon = "💧",
                suggestion = "Aim to meet your water goal at least 5 days",
                reason = "You met it ${waterData.daysGoalMet}/7 days this week",
                priority = 1
            ))
        }

        if (bpData.readingCount < 3) {
            suggestions.add(NextWeekGoal(
                icon = "❤️",
                suggestion = "Try to measure your BP at least 3 times",
                reason = "Regular monitoring helps track heart health",
                priority = 2
            ))
        }

        if (calorieData.daysLogged < 5) {
            suggestions.add(NextWeekGoal(
                icon = "🍽️",
                suggestion = "Log your meals more consistently",
                reason = "You logged ${calorieData.daysLogged}/7 days this week",
                priority = 3
            ))
        }

        if (exerciseData.first < 150) {
            val remaining = 150 - exerciseData.first
            suggestions.add(NextWeekGoal(
                icon = "🏃",
                suggestion = "Add ${remaining} more minutes of exercise",
                reason = "WHO recommends 150 min/week of moderate activity",
                priority = 4
            ))
        }

        if (weightData.count == 0) {
            suggestions.add(NextWeekGoal(
                icon = "⚖️",
                suggestion = "Log your weight at least once",
                reason = "Tracking helps you stay aware of progress",
                priority = 5
            ))
        }

        return suggestions.take(4)
    }

    private fun calculateOverallGrade(
        waterDaysGoalMet: Int,
        waterDaysTracked: Int,
        calorieDaysOnTarget: Int,
        calorieDaysLogged: Int,
        bpReadings: Int,
        exerciseMinutes: Int
    ): String {
        var score = 0
        var maxScore = 0

        // Water (0-25)
        if (waterDaysTracked > 0) {
            maxScore += 25
            score += (waterDaysGoalMet.toFloat() / 7 * 25).toInt()
        }

        // Calories (0-25)
        if (calorieDaysLogged > 0) {
            maxScore += 25
            score += (calorieDaysOnTarget.toFloat() / 7 * 25).toInt()
        }

        // BP Tracking (0-25)
        maxScore += 25
        score += when {
            bpReadings >= 4 -> 25
            bpReadings >= 2 -> 15
            bpReadings >= 1 -> 8
            else -> 0
        }

        // Exercise (0-25)
        maxScore += 25
        score += when {
            exerciseMinutes >= 150 -> 25
            exerciseMinutes >= 75 -> 15
            exerciseMinutes >= 30 -> 8
            else -> 0
        }

        val percentage = if (maxScore > 0) (score.toFloat() / maxScore * 100).toInt() else 50

        return when {
            percentage >= 90 -> "A"
            percentage >= 75 -> "B"
            percentage >= 60 -> "C"
            percentage >= 40 -> "D"
            else -> "F"
        }
    }

    private fun generateOverallMessage(grade: String): String {
        return when (grade) {
            "A" -> "Incredible week! \uD83C\uDF1F You crushed your health goals. Keep up this amazing momentum!"
            "B" -> "Great week! \uD83D\uDCAA You met most of your health goals. Solid progress!"
            "C" -> "Good effort! \uD83D\uDC4D You made progress in several areas. A few small improvements could make a big difference."
            "D" -> "Challenging week. \uD83E\uDD17 Don't worry — every week is a fresh start. Focus on one goal at a time."
            else -> "Tough week. \uD83D\uDC99 It's okay — health is a journey, not a sprint. Start fresh and focus on what matters most."
        }
    }

    // Data fetchers
    private suspend fun getWeightData(start: Long, end: Long): WeightWeekData {
        return try {
            val entries = weightRepository.getWeightsInRange(start, end).first()
            if (entries.isEmpty()) return WeightWeekData(null, null, null, 0)
            val first = entries.first().weightKg
            val last = entries.last().weightKg
            WeightWeekData(first, last, last - first, entries.size)
        } catch (e: Exception) {
            WeightWeekData(null, null, null, 0)
        }
    }

    private suspend fun getBmiData(start: Long, end: Long): Triple<Double?, Int, String?> {
        return try {
            val entries = historyRepository.getEntriesByTypeInRange("BMI", start, end)
            if (entries.isEmpty()) return Triple(null, 0, null)
            val avg = entries.mapNotNull { it.primaryValue }.average()
            val bestCategory = entries.minByOrNull {
                kotlin.math.abs(it.primaryValue - 21.7)
            }?.category
            Triple(avg, entries.size, bestCategory)
        } catch (e: Exception) {
            Triple(null, 0, null)
        }
    }

    private suspend fun getBpData(
        start: Long, end: Long, prevStart: Long, prevEnd: Long
    ): BpWeekData {
        return try {
            val entries = historyRepository.getEntriesByTypeInRange("BP", start, end)
            val prevEntries = historyRepository.getEntriesByTypeInRange("BP", prevStart, prevEnd)

            if (entries.isEmpty()) return BpWeekData(null, null, 0, null)

            val avgSys = entries.mapNotNull { it.primaryValue }.average()
            val avgDia = entries.mapNotNull { it.secondaryValue }.average()

            val prevAvg = if (prevEntries.isNotEmpty()) {
                Pair(
                    prevEntries.mapNotNull { it.primaryValue }.average(),
                    prevEntries.mapNotNull { it.secondaryValue }.average()
                )
            } else null

            BpWeekData(avgSys, avgDia, entries.size, prevAvg)
        } catch (e: Exception) {
            BpWeekData(null, null, 0, null)
        }
    }

    private suspend fun getWaterData(start: Long, end: Long): WaterWeekData {
        return try {
            val daysGoalMet = waterIntakeRepository.getDaysGoalMetInRange(start, end)
            val daysTracked = waterIntakeRepository.getDaysTrackedInRange(start, end)
            val avgIntake = waterIntakeRepository.getAverageIntakeInRange(start, end)
            val goal = waterIntakeRepository.getDailyGoal().first()
            WaterWeekData(daysGoalMet, daysTracked, avgIntake, goal)
        } catch (e: Exception) {
            WaterWeekData(0, 0, 0, 2500)
        }
    }

    private suspend fun getCalorieData(start: Long, end: Long): CalorieWeekData {
        return try {
            val avg = foodLogRepository.getAverageCaloriesInRange(start, end)
            val logged = foodLogRepository.getDaysLoggedInRange(start, end)
            val onTarget = foodLogRepository.getDaysOnTargetInRange(start, end)
            val goal = foodLogRepository.getDailyCalorieGoal().first()
            CalorieWeekData(avg, logged, onTarget, goal)
        } catch (e: Exception) {
            CalorieWeekData(0, 0, 0, 2000)
        }
    }

    private suspend fun getExerciseData(start: Long, end: Long): Pair<Int, Int> {
        return Pair(0, 0) // TODO: Get from exercise tracking
    }

    private suspend fun getHealthScoreData(start: Long, end: Long): Triple<Int, Int, Int> {
        return Triple(-1, -1, 0) // TODO: Get from health overview snapshots
    }

    private suspend fun getAchievementData(start: Long, end: Long): Triple<Int, Int, String?> {
        return try {
            val milestones = milestonesRepository.getAllMilestones().first()
                .filter { (it.achievedAt ?: 0) in start..end }
            val records = milestonesRepository.getAllRecords().first()
                .filter { it.achievedAt in start..end }
            val best = milestones.firstOrNull()?.let {
                val type = runCatching { MilestoneType.valueOf(it.milestoneType) }.getOrNull()
                "${type?.icon ?: "\uD83C\uDFC6"} ${type?.displayName ?: "Achievement"}"
            } ?: records.firstOrNull()?.let { "${it.displayValue} (New Record!)" }

            Triple(milestones.size, records.size, best)
        } catch (e: Exception) {
            Triple(0, 0, null)
        }
    }

    private fun getWeekRange(startDate: Long?): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        if (startDate != null) {
            cal.timeInMillis = startDate
        } else {
            // Get last Monday
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val weekStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val weekEnd = cal.timeInMillis

        return Pair(weekStart, weekEnd)
    }

    private data class WeightWeekData(val first: Double?, val last: Double?, val change: Double?, val count: Int)
    private data class BpWeekData(val avgSystolic: Double?, val avgDiastolic: Double?, val readingCount: Int, val prevAvg: Pair<Double, Double>?)
    private data class WaterWeekData(val daysGoalMet: Int, val daysTracked: Int, val avgIntake: Int, val goal: Int)
    private data class CalorieWeekData(val avgConsumed: Int, val daysLogged: Int, val daysOnTarget: Int, val target: Int)
}
