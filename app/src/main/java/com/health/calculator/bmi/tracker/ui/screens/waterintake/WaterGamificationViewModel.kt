package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import com.health.calculator.bmi.tracker.domain.WaterGamificationEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class WaterGamificationViewModel(
    application: Application,
    private val gamificationRepo: WaterGamificationRepository,
    private val waterRepo: WaterIntakeRepository
) : AndroidViewModel(application) {

    private val engine = WaterGamificationEngine(gamificationRepo, waterRepo)

    val dailyGoalMl: Int
        get() {
            val prefs = getApplication<Application>()
                .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("daily_goal_ml", 2500)
        }

    val earnedBadges: Flow<List<EarnedBadge>> = gamificationRepo.getAllEarnedBadges()

    val streakData: Flow<WaterStreakData?> = gamificationRepo.observeStreakData()

    private val _todayScore = MutableStateFlow<HydrationScore?>(null)
    val todayScore: StateFlow<HydrationScore?> = _todayScore.asStateFlow()

    private val _newlyEarnedBadge = MutableStateFlow<BadgeType?>(null)
    val newlyEarnedBadge: StateFlow<BadgeType?> = _newlyEarnedBadge.asStateFlow()

    init {
        calculateTodayScore()
    }

    private fun calculateTodayScore() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            waterRepo.getWaterLogsForDay(startOfDay, endOfDay).collect { logs ->
                val totalMl = logs.sumOf { it.amountMl }
                val score = engine.calculateHydrationScore(totalMl, dailyGoalMl, logs)
                _todayScore.value = score

                // Update streak
                val updatedStreak = engine.updateStreak(totalMl, dailyGoalMl, score)

                // Check badges
                val firstLogHour = if (logs.isNotEmpty()) {
                    val logCal = Calendar.getInstance()
                    logCal.timeInMillis = logs.minOf { it.timestamp }
                    logCal.get(Calendar.HOUR_OF_DAY)
                } else null

                val newBadges = engine.checkAndAwardBadges(
                    currentMl = totalMl,
                    goalMl = dailyGoalMl,
                    todayLogCount = logs.size,
                    streakData = updatedStreak,
                    firstLogHour = firstLogHour
                )

                if (newBadges.isNotEmpty()) {
                    _newlyEarnedBadge.value = newBadges.first()
                }
            }
        }
    }

    fun dismissBadgeUnlock() {
        val badge = _newlyEarnedBadge.value
        _newlyEarnedBadge.value = null

        badge?.let {
            viewModelScope.launch {
                gamificationRepo.markBadgeSeen(it.name)
            }
        }
    }
}
