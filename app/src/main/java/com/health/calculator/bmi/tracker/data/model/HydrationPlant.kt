// data/model/HydrationPlant.kt
package com.health.calculator.bmi.tracker.data.model

enum class PlantStage(
    val displayName: String,
    val description: String,
    val daysRequired: Int
) {
    SPROUT("Tiny Sprout", "Just getting started!", 0),
    SMALL_PLANT("Small Plant", "Growing nicely!", 7),
    GROWING_PLANT("Growing Plant", "Look at those leaves!", 30),
    FLOWERING("Flowering Plant", "Beautiful blooms!", 90),
    FULL_BLOOM("Full Bloom", "A masterpiece of hydration!", 180)
}

enum class PlantMood {
    THRIVING,    // Goal met today
    HAPPY,       // >75% of goal
    NEUTRAL,     // >50% of goal
    THIRSTY,     // >25% of goal
    WILTING      // <25% or no water today
}

data class PlantState(
    val stage: PlantStage = PlantStage.SPROUT,
    val mood: PlantMood = PlantMood.NEUTRAL,
    val totalDaysTracked: Int = 0,
    val currentStreak: Int = 0,
    val todayPercentage: Float = 0f,
    val justWatered: Boolean = false,
    val goalReachedToday: Boolean = false
)
