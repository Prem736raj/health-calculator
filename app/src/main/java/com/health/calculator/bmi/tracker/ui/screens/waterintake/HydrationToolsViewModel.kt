// ui/screens/waterintake/HydrationToolsViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.dao.UrineColorDao
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HydrationToolsViewModel(
    application: Application,
    private val urineColorDao: UrineColorDao
) : AndroidViewModel(application) {

    // Urine color
    val recentUrineColors: Flow<List<UrineColorEntry>> = urineColorDao.getRecent(10)

    private val _latestUrineColor = MutableStateFlow<UrineColorEntry?>(null)
    val latestUrineColor: StateFlow<UrineColorEntry?> = _latestUrineColor.asStateFlow()

    var showUrineLogConfirm by mutableStateOf(false)
        private set

    // Dehydration symptoms
    val checkedSymptoms = mutableStateListOf<Int>()

    val dehydrationRiskLevel: DehydrationRisk
        get() = when {
            checkedSymptoms.size >= 5 -> DehydrationRisk.HIGH
            checkedSymptoms.size >= 3 -> DehydrationRisk.MODERATE
            checkedSymptoms.size >= 1 -> DehydrationRisk.MILD
            else -> DehydrationRisk.NONE
        }

    // Water goal for food estimation
    val dailyGoalMl: Int
        get() {
            val prefs = getApplication<Application>()
                .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("daily_goal_ml", 2500)
        }

    init {
        loadLatestUrineColor()
    }

    private fun loadLatestUrineColor() {
        viewModelScope.launch {
            _latestUrineColor.value = urineColorDao.getLatest()
        }
    }

    fun logUrineColor(level: Int, note: String = "") {
        viewModelScope.launch {
            urineColorDao.insert(
                UrineColorEntry(colorLevel = level, note = note)
            )
            _latestUrineColor.value = urineColorDao.getLatest()
            showUrineLogConfirm = true
        }
    }

    fun dismissUrineConfirm() {
        showUrineLogConfirm = false
    }

    fun toggleSymptom(index: Int) {
        if (checkedSymptoms.contains(index)) {
            checkedSymptoms.remove(index)
        } else {
            checkedSymptoms.add(index)
        }
    }

    fun clearSymptoms() {
        checkedSymptoms.clear()
    }

    companion object {
        val DEHYDRATION_SYMPTOMS = listOf(
            DehydrationSymptom("Thirst", "🥵", "Feeling thirsty or having a dry sensation", SymptomSeverity.MILD),
            DehydrationSymptom("Dry Mouth", "👄", "Mouth feels dry or sticky", SymptomSeverity.MILD),
            DehydrationSymptom("Dark Urine", "🚽", "Urine is dark yellow or amber colored", SymptomSeverity.MODERATE),
            DehydrationSymptom("Fatigue", "😴", "Feeling unusually tired or lethargic", SymptomSeverity.MODERATE),
            DehydrationSymptom("Dizziness", "💫", "Feeling lightheaded or dizzy", SymptomSeverity.MODERATE),
            DehydrationSymptom("Headache", "🤕", "Persistent headache", SymptomSeverity.MODERATE),
            DehydrationSymptom("Dry Skin", "🖐️", "Skin feels dry or less elastic", SymptomSeverity.MODERATE),
            DehydrationSymptom("Decreased Urination", "⏬", "Urinating less frequently than normal", SymptomSeverity.SEVERE)
        )

        val WATER_RICH_FOODS = listOf(
            WaterRichFood("Cucumber", "🥒", 96, "1 cup sliced (119g)", 114),
            WaterRichFood("Lettuce", "🥬", 95, "1 cup shredded (47g)", 45),
            WaterRichFood("Celery", "🥬", 95, "1 stalk (40g)", 38),
            WaterRichFood("Tomato", "🍅", 94, "1 medium (123g)", 116),
            WaterRichFood("Watermelon", "🍉", 92, "1 cup diced (152g)", 140),
            WaterRichFood("Strawberries", "🍓", 91, "1 cup (152g)", 138),
            WaterRichFood("Bell Pepper", "🫑", 92, "1 cup chopped (149g)", 137),
            WaterRichFood("Spinach", "🥬", 91, "1 cup raw (30g)", 27),
            WaterRichFood("Cantaloupe", "🍈", 90, "1 cup diced (160g)", 144),
            WaterRichFood("Peach", "🍑", 89, "1 medium (150g)", 134),
            WaterRichFood("Orange", "🍊", 87, "1 medium (131g)", 114),
            WaterRichFood("Yogurt", "🥛", 85, "1 cup (245g)", 208),
            WaterRichFood("Apple", "🍎", 84, "1 medium (182g)", 153),
            WaterRichFood("Grapes", "🍇", 81, "1 cup (151g)", 122),
            WaterRichFood("Carrot", "🥕", 88, "1 medium (61g)", 54)
        )
    }
}
