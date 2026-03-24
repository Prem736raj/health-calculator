// notifications/NotificationContextDataProvider.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.repository.FoodLogRepository
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class NotificationContextDataProvider(
    private val context: Context,
    private val waterRepo: WaterIntakeRepository,
    private val foodRepo: FoodLogRepository,
    private val historyRepo: HistoryRepository,
    private val profileRepo: ProfileRepository
) {

    suspend fun provideData(): NotificationContextData {
        val calendar = Calendar.getInstance()
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = calendar.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis - 1

        val profile = profileRepo.getProfile().firstOrNull()
        val waterIntake = waterRepo.getTotalWaterForDay(startOfDay, endOfDay).firstOrNull() ?: 0
        val foodLog = foodRepo.todayLog.value
        
        // Calculate streaks
        val waterStreak = calculateWaterStreak()
        val bpStreak = historyRepo.getConsecutiveDaysWithEntryType(CalculatorType.BLOOD_PRESSURE.key)
        val calorieStreak = calculateCalorieLoggingStreak()
        
        // Get last readings
        val lastBpEntry = historyRepo.getLatestByTypeSync(CalculatorType.BLOOD_PRESSURE.key)
        val lastBpReading = lastBpEntry?.let { "${it.resultValue} ${it.resultLabel ?: ""}" }
        
        val lastWeightEntry = historyRepo.getLatestByTypeSync(CalculatorType.IBW.key) // Using IBW or regular weight if available
        val currentWeight = profile?.weightKg?.toDouble()
        
        // Exercise data (simulated or from history if tracked)
        val exerciseMinutesThisWeek = calculateWeeklyExerciseMinutes()

        // Health score (placeholder calculation)
        val healthScore = calculateHealthScore(waterIntake, foodLog.entries.isNotEmpty(), profile != null)

        val lastAppUse = context.getSharedPreferences("notification_rate_limiter", Context.MODE_PRIVATE)
            .getLong("last_app_use_time", 0)
        val daysSinceLastUse = if (lastAppUse > 0) {
            ((System.currentTimeMillis() - lastAppUse) / (1000 * 60 * 60 * 24)).toInt()
        } else 0

        return NotificationContextData(
            waterIntakeMl = waterIntake,
            waterGoalMl = 2500, // Default or from settings if added
            waterStreak = waterStreak,
            bpTrackingStreak = bpStreak,
            lastBpReading = lastBpReading,
            currentWeightKg = currentWeight,
            weightGoalKg = profile?.goalWeightKg?.toDouble(),
            weightTrackingWeeks = calculateWeightTrackingWeeks(),
            maxHeartRate = calculateMaxHeartRate(profile?.dateOfBirthMillis),
            exerciseMinutesThisWeek = exerciseMinutesThisWeek,
            caloriesConsumed = foodLog.entries.sumOf { it.calories }.toInt(),
            calorieGoal = foodLog.targetCalories.toInt(),
            calorieLoggingStreak = calorieStreak,
            healthScore = healthScore,
            daysSinceLastAppUse = daysSinceLastUse
        )
    }

    private suspend fun calculateWaterStreak(): Int {
        // Simple streak for water logs
        var streak = 0
        val cal = Calendar.getInstance()
        val oneDay = 24 * 60 * 60 * 1000L
        
        for (i in 0 until 30) {
            val dayStart = getDayStart(i)
            val dayEnd = getDayStart(i - 1) - 1
            val total = waterRepo.getTotalWaterBetweenSync(dayStart, dayEnd) ?: 0
            if (total > 0) {
                streak++
            } else if (i > 0) { // If missing yesterday/before, break (today can be 0 and streak continues)
                break
            }
        }
        return streak
    }

    private fun calculateCalorieLoggingStreak(): Int {
        val history = foodRepo.getHistoricalLogs()
        if (history.isEmpty()) return 0
        
        var streak = 0
        // History is newest first
        for (log in history) {
            if (log.entries.isNotEmpty()) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private suspend fun calculateWeeklyExerciseMinutes(): Int {
        // Using Heart Rate or specific Exercise logs if they exist
        // For now, check how many HR entries exist this week as a proxy
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        
        val hrEntries = historyRepo.getEntriesByType(CalculatorType.HEART_RATE).firstOrNull() ?: emptyList()
        return hrEntries.filter { it.timestamp >= startOfWeek }.size * 30 // Proxy: 30 mins per entry
    }

    private fun calculateWeightTrackingWeeks(): Int {
        val historicalLogs = foodRepo.getHistoricalLogs()
        if (historicalLogs.isEmpty()) return 0
        val firstLog = historicalLogs.last().date
        // Parse date and calculate weeks... simplified:
        return (historicalLogs.size / 7).coerceAtLeast(1)
    }

    private fun calculateMaxHeartRate(dobMillis: Long?): Int {
        if (dobMillis == null) return 190
        val age = ((System.currentTimeMillis() - dobMillis) / (1000L * 60 * 60 * 24 * 365)).toInt()
        return 220 - age
    }

    private fun calculateHealthScore(water: Int, loggedFood: Boolean, profileComplete: Boolean): Int {
        var score = 50
        if (water > 2000) score += 15
        if (loggedFood) score += 15
        if (profileComplete) score += 10
        return score.coerceIn(0, 100)
    }

    private fun getDayStart(daysAgo: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
