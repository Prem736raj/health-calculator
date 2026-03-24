package com.health.calculator.bmi.tracker.data.model

data class HealthTip(
    val id: Int,
    val category: DailyTipCategory,
    val shortTip: String,
    val detailedTip: String,
    val source: String? = null
)

enum class DailyTipCategory(val displayName: String, val emoji: String) {
    NUTRITION("Nutrition", "🥗"),
    EXERCISE("Exercise", "🏃"),
    HYDRATION("Hydration", "💧"),
    SLEEP("Sleep", "😴"),
    MENTAL_HEALTH("Mental Health", "🧠"),
    HEART_HEALTH("Heart Health", "❤️"),
    WEIGHT_MANAGEMENT("Weight Management", "⚖️")
}

data class MotivationalQuote(
    val id: Int,
    val quote: String,
    val author: String,
    val category: QuoteCategory = QuoteCategory.GENERAL
)

enum class QuoteCategory {
    GENERAL,
    FITNESS,
    NUTRITION,
    MINDFULNESS,
    PERSEVERANCE
}

data class DailyContent(
    val tip: HealthTip,
    val quote: MotivationalQuote,
    val date: Long
)
