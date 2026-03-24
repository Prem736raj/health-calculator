package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.IBWGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IBWGoalRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("ibw_goal_prefs", Context.MODE_PRIVATE)
    }

    private val _activeGoal = MutableStateFlow<IBWGoal?>(null)
    val activeGoal: Flow<IBWGoal?> = _activeGoal.asStateFlow()

    init {
        loadGoal()
    }

    fun saveGoal(goal: IBWGoal) {
        prefs.edit().apply {
            putFloat("target_weight_kg", goal.targetWeightKg.toFloat())
            putString("target_source", goal.targetSource)
            putFloat("start_weight_kg", goal.startWeightKg.toFloat())
            putLong("start_date", goal.startDate)
            putString("selected_pace", goal.selectedPace)
            putBoolean("is_active", goal.isActive)
            apply()
        }
        _activeGoal.value = goal
    }

    fun loadGoal(): IBWGoal? {
        if (!prefs.contains("target_weight_kg")) {
            _activeGoal.value = null
            return null
        }

        val goal = IBWGoal(
            targetWeightKg = prefs.getFloat("target_weight_kg", 0f).toDouble(),
            targetSource = prefs.getString("target_source", "Custom") ?: "Custom",
            startWeightKg = prefs.getFloat("start_weight_kg", 0f).toDouble(),
            startDate = prefs.getLong("start_date", System.currentTimeMillis()),
            selectedPace = prefs.getString("selected_pace", "Moderate") ?: "Moderate",
            isActive = prefs.getBoolean("is_active", true)
        )
        _activeGoal.value = goal
        return goal
    }

    fun clearGoal() {
        prefs.edit().clear().apply()
        _activeGoal.value = null
    }

    fun updatePace(pace: String) {
        _activeGoal.value?.let { goal ->
            saveGoal(goal.copy(selectedPace = pace))
        }
    }

    // Track milestone celebrations so we only celebrate each once
    private val celebratedMilestones = mutableSetOf<Int>()

    fun shouldCelebrateMilestone(milestone: Int): Boolean {
        return if (milestone !in celebratedMilestones) {
            celebratedMilestones.add(milestone)
            prefs.edit().putStringSet(
                "celebrated_milestones",
                celebratedMilestones.map { it.toString() }.toSet()
            ).apply()
            true
        } else {
            false
        }
    }

    fun loadCelebratedMilestones() {
        val saved = prefs.getStringSet("celebrated_milestones", emptySet()) ?: emptySet()
        celebratedMilestones.clear()
        celebratedMilestones.addAll(saved.mapNotNull { it.toIntOrNull() })
    }
}
