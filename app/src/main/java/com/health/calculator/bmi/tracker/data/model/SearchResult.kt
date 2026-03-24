package com.health.calculator.bmi.tracker.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: SearchResultType,
    val navigationRoute: String,
    val icon: ImageVector,
    val matchedKeyword: String = ""
)

enum class SearchResultType(val label: String) {
    CALCULATOR("Calculator"),
    FEATURE("Feature"),
    HEALTH_TERM("Health Term"),
    EDUCATIONAL("Educational")
}

data class QuickAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val route: String,
    val usageCount: Int = 0,
    val emoji: String = ""
)

data class SearchEntry(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
