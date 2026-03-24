package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.model.SearchResult
import com.health.calculator.bmi.tracker.data.model.SearchResultType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

class SearchRepository {

    private val searchIndex: List<SearchResult> by lazy { buildSearchIndex() }

    fun search(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.trim().lowercase()

        return searchIndex
            .filter { item ->
                item.title.lowercase().contains(lowerQuery) ||
                item.subtitle.lowercase().contains(lowerQuery) ||
                item.id.lowercase().contains(lowerQuery) ||
                getKeywords(item.id).any { it.contains(lowerQuery) }
            }
            .sortedByDescending { item ->
                when {
                    item.title.lowercase().startsWith(lowerQuery) -> 100
                    item.title.lowercase().contains(lowerQuery) -> 80
                    item.subtitle.lowercase().contains(lowerQuery) -> 60
                    else -> 40
                }
            }
            .take(8)
            .map { it.copy(matchedKeyword = lowerQuery) }
    }

    private fun buildSearchIndex(): List<SearchResult> {
        return listOf(
            SearchResult("bmi", "BMI Calculator", "Body Mass Index", SearchResultType.CALCULATOR, "bmi_calculator", Icons.Default.Scale),
            SearchResult("bmr", "BMR Calculator", "Basal Metabolic Rate", SearchResultType.CALCULATOR, "bmr_calculator", Icons.Default.LocalFireDepartment),
            SearchResult("bp", "Blood Pressure", "Check BP levels", SearchResultType.CALCULATOR, "blood_pressure_calculator", Icons.Default.Favorite),
            SearchResult("whr", "Waist-Hip Ratio", "Body shape risk", SearchResultType.CALCULATOR, "whr_calculator", Icons.Default.Straighten),
            SearchResult("water", "Water Intake", "Daily hydration", SearchResultType.CALCULATOR, "water_calculator", Icons.Default.WaterDrop),
            SearchResult("met", "Metabolic Syndrome", "Risk assessment", SearchResultType.CALCULATOR, "metabolic_syndrome", Icons.Default.HealthAndSafety),
            SearchResult("bsa", "BSA Calculator", "Body Surface Area", SearchResultType.CALCULATOR, "bsa_calculator", Icons.Default.Person),
            SearchResult("ibw", "Ideal Body Weight", "Weight goals", SearchResultType.CALCULATOR, "ibw_calculator", Icons.Default.FitnessCenter),
            SearchResult("calories", "Calorie Requirement", "Daily energy needs", SearchResultType.CALCULATOR, "calorie_calculator", Icons.Default.Restaurant),
            SearchResult("hr", "Heart Rate Zones", "Training guidance", SearchResultType.CALCULATOR, "heart_rate_calculator", Icons.Default.MonitorHeart),
            
            // Features
            SearchResult("log_water", "Log Water", "Quick entry", SearchResultType.FEATURE, "water_calculator", Icons.Default.Add),
            SearchResult("history", "History", "Past calculations", SearchResultType.FEATURE, "history", Icons.Default.History),
            SearchResult("profile", "Profile", "Personal data", SearchResultType.FEATURE, "profile", Icons.Default.AccountCircle),
            
            // Terms
            SearchResult("hypertension", "Hypertension", "Understanding high BP", SearchResultType.HEALTH_TERM, "blood_pressure_calculator", Icons.Default.Info),
            SearchResult("metabolism", "Metabolism", "Energy expenditure", SearchResultType.HEALTH_TERM, "bmr_calculator", Icons.Default.Info),
            SearchResult("dehydration", "Dehydration", "Risks & signs", SearchResultType.HEALTH_TERM, "water_calculator", Icons.Default.Info)
        )
    }

    private fun getKeywords(id: String): List<String> {
        return when (id) {
            "bmi" -> listOf("weight", "height", "obesity", "fat")
            "bp" -> listOf("hypertension", "systolic", "diastolic", "heart")
            "water" -> listOf("hydration", "drink", "fluid")
            "calories" -> listOf("food", "diet", "nutrition", "macros")
            "hr" -> listOf("pulse", "bpm", "cardio", "vo2")
            else -> emptyList()
        }
    }
}
