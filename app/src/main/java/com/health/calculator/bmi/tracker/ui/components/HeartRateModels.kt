package com.health.calculator.bmi.tracker.ui.components

enum class HeartRateFormula(
    val label: String,
    val formulaText: String,
    val badge: String? = null
) {
    STANDARD(
        label = "Standard",
        formulaText = "220 − age",
        badge = "Most Used"
    ),
    TANAKA(
        label = "Tanaka",
        formulaText = "208 − (0.7 × age)",
        badge = "Accurate 40+"
    ),
    GULATI(
        label = "Gulati",
        formulaText = "206 − (0.88 × age)",
        badge = "Women"
    ),
    KARVONEN(
        label = "Karvonen",
        formulaText = "Uses resting HR for personalized zones",
        badge = "Most Personalized"
    ),
    CUSTOM(
        label = "Custom",
        formulaText = "Enter your known max heart rate",
        badge = null
    );
}

enum class FitnessLevel(
    val label: String,
    val emoji: String,
    val description: String
) {
    BEGINNER(
        label = "Beginner",
        emoji = "🌱",
        description = "New to exercise or returning after a long break. Light walking or minimal physical activity."
    ),
    INTERMEDIATE(
        label = "Intermediate",
        emoji = "🏃",
        description = "Regular exercise 2-4 times per week. Comfortable with moderate-intensity workouts."
    ),
    ADVANCED(
        label = "Advanced",
        emoji = "🏋️",
        description = "Consistent training 5+ times per week. Experienced with high-intensity exercise."
    );
}
