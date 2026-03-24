package com.health.calculator.bmi.tracker.util

import com.health.calculator.bmi.tracker.ui.components.FitnessLevel

data class WHOGuideline(
    val ageGroup: String,
    val ageRange: String,
    val moderateMinPerWeek: IntRange,
    val vigorousMinPerWeek: IntRange,
    val additionalRecommendations: List<String>,
    val keyMessage: String
)

data class WeeklyPlanDay(
    val dayName: String,
    val isRestDay: Boolean,
    val zoneFocus: String = "",
    val zoneNumbers: List<Int> = emptyList(),
    val durationMinutes: Int = 0,
    val activitySuggestion: String = "",
    val intensityLabel: String = "",
    val emoji: String = ""
)

data class WeeklyPlan(
    val planName: String,
    val goalEmoji: String,
    val days: List<WeeklyPlanDay>,
    val totalModerateMin: Int,
    val totalVigorousMin: Int,
    val totalActiveMin: Int,
    val meetsWHOGuideline: Boolean,
    val whoComplianceNote: String
)

data class ExerciseLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val durationMinutes: Int,
    val zoneNumber: Int,
    val zoneName: String,
    val intensity: ExerciseIntensity,
    val activityNote: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val weekStartDate: String = "" // ISO date of that week's Monday
)

enum class ExerciseIntensity(val label: String, val emoji: String) {
    MODERATE("Moderate", "🟢"),
    VIGOROUS("Vigorous", "🔴")
}

object WHOExerciseGuidelinesEngine {

    fun getGuidelineForAge(age: Int): WHOGuideline {
        return when {
            age in 5..17 -> WHOGuideline(
                ageGroup = "Children & Adolescents",
                ageRange = "5-17 years",
                moderateMinPerWeek = 420..420, // 60 min/day × 7
                vigorousMinPerWeek = 180..180, // 3+ days
                additionalRecommendations = listOf(
                    "At least 60 minutes of moderate-to-vigorous activity daily",
                    "Vigorous-intensity activities at least 3 days/week",
                    "Include muscle and bone strengthening activities 3+ days/week",
                    "Limit sedentary screen time",
                    "More activity provides greater health benefits"
                ),
                keyMessage = "Children should be active for at least 60 minutes every day"
            )

            age >= 65 -> WHOGuideline(
                ageGroup = "Older Adults",
                ageRange = "65+ years",
                moderateMinPerWeek = 150..300,
                vigorousMinPerWeek = 75..150,
                additionalRecommendations = listOf(
                    "150-300 minutes moderate-intensity per week, OR",
                    "75-150 minutes vigorous-intensity per week",
                    "Muscle-strengthening activities 2+ days/week",
                    "Balance and functional training 3+ days/week to prevent falls",
                    "Multicomponent physical activity emphasizing balance",
                    "Some activity is better than none — start wherever you can"
                ),
                keyMessage = "Stay active with balance training to maintain independence"
            )

            else -> WHOGuideline(
                ageGroup = "Adults",
                ageRange = "18-64 years",
                moderateMinPerWeek = 150..300,
                vigorousMinPerWeek = 75..150,
                additionalRecommendations = listOf(
                    "150-300 minutes moderate-intensity per week, OR",
                    "75-150 minutes vigorous-intensity per week, OR",
                    "An equivalent combination of moderate and vigorous",
                    "Muscle-strengthening activities 2+ days/week",
                    "Reduce sedentary time throughout the day",
                    "Additional benefits above 300 minutes/week"
                ),
                keyMessage = "At least 150 minutes of moderate activity per week"
            )
        }
    }

    /**
     * Map heart rate zones to WHO intensity categories
     */
    fun getZoneIntensity(zoneNumber: Int): ExerciseIntensity {
        return when (zoneNumber) {
            1, 2, 3 -> ExerciseIntensity.MODERATE
            4, 5 -> ExerciseIntensity.VIGOROUS
            else -> ExerciseIntensity.MODERATE
        }
    }

    /**
     * Convert vigorous minutes to moderate equivalent
     * WHO: 1 minute vigorous = 2 minutes moderate
     */
    fun vigorousToModerateEquivalent(vigorousMin: Int): Int = vigorousMin * 2

    /**
     * Generate a weekly exercise plan
     */
    fun generateWeeklyPlan(
        goalName: String,
        fitnessLevel: FitnessLevel,
        age: Int
    ): WeeklyPlan {
        val guideline = getGuidelineForAge(age)

        return when {
            goalName.contains("Beginner", ignoreCase = true) ||
                    fitnessLevel == FitnessLevel.BEGINNER -> generateBeginnerPlan(guideline)

            goalName.contains("Weight Loss", ignoreCase = true) -> generateWeightLossPlan(
                fitnessLevel, guideline
            )

            goalName.contains("Cardiovascular", ignoreCase = true) -> generateCardioPlan(
                fitnessLevel, guideline
            )

            goalName.contains("Endurance", ignoreCase = true) -> generateEndurancePlan(
                fitnessLevel, guideline
            )

            goalName.contains("Performance", ignoreCase = true) ||
                    goalName.contains("Speed", ignoreCase = true) -> generatePerformancePlan(
                fitnessLevel, guideline
            )

            else -> generateCardioPlan(fitnessLevel, guideline)
        }
    }

    private fun generateBeginnerPlan(guideline: WHOGuideline): WeeklyPlan {
        val days = listOf(
            WeeklyPlanDay(
                dayName = "Monday",
                isRestDay = false,
                zoneFocus = "Zone 1-2",
                zoneNumbers = listOf(1, 2),
                durationMinutes = 25,
                activitySuggestion = "Brisk walk in the park",
                intensityLabel = "Easy",
                emoji = "🚶"
            ),
            WeeklyPlanDay("Tuesday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Wednesday",
                isRestDay = false,
                zoneFocus = "Zone 1-2",
                zoneNumbers = listOf(1, 2),
                durationMinutes = 25,
                activitySuggestion = "Light cycling or swimming",
                intensityLabel = "Easy",
                emoji = "🚴"
            ),
            WeeklyPlanDay("Thursday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Friday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 30,
                activitySuggestion = "Brisk walk with light intervals",
                intensityLabel = "Easy-Moderate",
                emoji = "🏃"
            ),
            WeeklyPlanDay("Saturday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Sunday",
                isRestDay = false,
                zoneFocus = "Zone 1-2",
                zoneNumbers = listOf(1, 2),
                durationMinutes = 30,
                activitySuggestion = "Relaxed walk or gentle yoga",
                intensityLabel = "Very Easy",
                emoji = "🧘"
            )
        )

        val totalModerate = days.filter { !it.isRestDay }.sumOf { it.durationMinutes }
        return WeeklyPlan(
            planName = "Beginner Safe Start",
            goalEmoji = "🌱",
            days = days,
            totalModerateMin = totalModerate,
            totalVigorousMin = 0,
            totalActiveMin = totalModerate,
            meetsWHOGuideline = totalModerate >= guideline.moderateMinPerWeek.first,
            whoComplianceNote = if (totalModerate >= guideline.moderateMinPerWeek.first)
                "✅ Meets WHO minimum of ${guideline.moderateMinPerWeek.first} min/week"
            else
                "📈 Build up to ${guideline.moderateMinPerWeek.first} min/week over the coming weeks"
        )
    }

    private fun generateWeightLossPlan(
        fitnessLevel: FitnessLevel,
        guideline: WHOGuideline
    ): WeeklyPlan {
        val isIntermediate = fitnessLevel == FitnessLevel.INTERMEDIATE
        val days = listOf(
            WeeklyPlanDay(
                dayName = "Monday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = if (isIntermediate) 40 else 50,
                activitySuggestion = "Jogging with walking intervals",
                intensityLabel = "Moderate",
                emoji = "🏃"
            ),
            WeeklyPlanDay(
                dayName = "Tuesday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 30,
                activitySuggestion = "Brisk walking or light cycling",
                intensityLabel = "Easy",
                emoji = "🚶"
            ),
            WeeklyPlanDay(
                dayName = "Wednesday",
                isRestDay = false,
                zoneFocus = if (isIntermediate) "Zone 3-4" else "Zone 4-5",
                zoneNumbers = if (isIntermediate) listOf(3, 4) else listOf(4, 5),
                durationMinutes = if (isIntermediate) 25 else 30,
                activitySuggestion = "HIIT or interval training",
                intensityLabel = "Hard",
                emoji = "⚡"
            ),
            WeeklyPlanDay("Thursday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Friday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = if (isIntermediate) 40 else 45,
                activitySuggestion = "Steady-state cardio (swim, cycle, run)",
                intensityLabel = "Moderate",
                emoji = "🏊"
            ),
            WeeklyPlanDay(
                dayName = "Saturday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = if (isIntermediate) 45 else 60,
                activitySuggestion = "Long easy session (hike, walk, bike)",
                intensityLabel = "Easy-Moderate",
                emoji = "🥾"
            ),
            WeeklyPlanDay("Sunday", isRestDay = true, emoji = "😴")
        )

        val moderateMin = days.filter { !it.isRestDay && it.zoneNumbers.all { z -> z <= 3 } }
            .sumOf { it.durationMinutes }
        val vigorousMin = days.filter { !it.isRestDay && it.zoneNumbers.any { z -> z >= 4 } }
            .sumOf { it.durationMinutes }
        val totalEquivalent = moderateMin + vigorousToModerateEquivalent(vigorousMin)

        return WeeklyPlan(
            planName = "Weight Loss Plan",
            goalEmoji = "⚖️",
            days = days,
            totalModerateMin = moderateMin,
            totalVigorousMin = vigorousMin,
            totalActiveMin = moderateMin + vigorousMin,
            meetsWHOGuideline = totalEquivalent >= guideline.moderateMinPerWeek.first,
            whoComplianceNote = "✅ ${totalEquivalent} moderate-equivalent min/week " +
                    "(WHO recommends ${guideline.moderateMinPerWeek.first}-${guideline.moderateMinPerWeek.last})"
        )
    }

    private fun generateCardioPlan(
        fitnessLevel: FitnessLevel,
        guideline: WHOGuideline
    ): WeeklyPlan {
        val days = listOf(
            WeeklyPlanDay(
                dayName = "Monday",
                isRestDay = false,
                zoneFocus = "Zone 3",
                zoneNumbers = listOf(3),
                durationMinutes = 35,
                activitySuggestion = "Aerobic run at comfortable pace",
                intensityLabel = "Moderate",
                emoji = "🏃"
            ),
            WeeklyPlanDay(
                dayName = "Tuesday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 30,
                activitySuggestion = "Easy recovery walk or swim",
                intensityLabel = "Easy",
                emoji = "🏊"
            ),
            WeeklyPlanDay(
                dayName = "Wednesday",
                isRestDay = false,
                zoneFocus = "Zone 3-4",
                zoneNumbers = listOf(3, 4),
                durationMinutes = 30,
                activitySuggestion = "Tempo run with Zone 4 pushes",
                intensityLabel = "Moderate-Hard",
                emoji = "💪"
            ),
            WeeklyPlanDay("Thursday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Friday",
                isRestDay = false,
                zoneFocus = "Zone 4-5",
                zoneNumbers = listOf(4, 5),
                durationMinutes = 25,
                activitySuggestion = "Interval training",
                intensityLabel = "Hard",
                emoji = "⚡"
            ),
            WeeklyPlanDay(
                dayName = "Saturday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = 45,
                activitySuggestion = "Long easy run or cycle",
                intensityLabel = "Easy-Moderate",
                emoji = "🚴"
            ),
            WeeklyPlanDay("Sunday", isRestDay = true, emoji = "😴")
        )

        val moderateMin = days.filter { !it.isRestDay && it.zoneNumbers.all { z -> z <= 3 } }
            .sumOf { it.durationMinutes }
        val vigorousMin = days.filter { !it.isRestDay && it.zoneNumbers.any { z -> z >= 4 } }
            .sumOf { it.durationMinutes }
        val totalEquivalent = moderateMin + vigorousToModerateEquivalent(vigorousMin)

        return WeeklyPlan(
            planName = "Cardiovascular Fitness",
            goalEmoji = "❤️",
            days = days,
            totalModerateMin = moderateMin,
            totalVigorousMin = vigorousMin,
            totalActiveMin = moderateMin + vigorousMin,
            meetsWHOGuideline = totalEquivalent >= guideline.moderateMinPerWeek.first,
            whoComplianceNote = "✅ ${totalEquivalent} moderate-equivalent min/week " +
                    "(WHO: ${guideline.moderateMinPerWeek.first}-${guideline.moderateMinPerWeek.last})"
        )
    }

    private fun generateEndurancePlan(
        fitnessLevel: FitnessLevel,
        guideline: WHOGuideline
    ): WeeklyPlan {
        val days = listOf(
            WeeklyPlanDay(
                dayName = "Monday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = 45,
                activitySuggestion = "Easy-moderate run",
                intensityLabel = "Moderate",
                emoji = "🏃"
            ),
            WeeklyPlanDay(
                dayName = "Tuesday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 30,
                activitySuggestion = "Active recovery walk",
                intensityLabel = "Easy",
                emoji = "🚶"
            ),
            WeeklyPlanDay(
                dayName = "Wednesday",
                isRestDay = false,
                zoneFocus = "Zone 3-4",
                zoneNumbers = listOf(3, 4),
                durationMinutes = 40,
                activitySuggestion = "Tempo session with intervals",
                intensityLabel = "Moderate-Hard",
                emoji = "💪"
            ),
            WeeklyPlanDay("Thursday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Friday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 35,
                activitySuggestion = "Easy cross-training (swim, bike)",
                intensityLabel = "Easy",
                emoji = "🚴"
            ),
            WeeklyPlanDay(
                dayName = "Saturday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = 75,
                activitySuggestion = "Long slow distance (LSD) session",
                intensityLabel = "Easy-Moderate",
                emoji = "🏔️"
            ),
            WeeklyPlanDay("Sunday", isRestDay = true, emoji = "😴")
        )

        val moderateMin = days.filter { !it.isRestDay && it.zoneNumbers.all { z -> z <= 3 } }
            .sumOf { it.durationMinutes }
        val vigorousMin = days.filter { !it.isRestDay && it.zoneNumbers.any { z -> z >= 4 } }
            .sumOf { it.durationMinutes }
        val totalEquivalent = moderateMin + vigorousToModerateEquivalent(vigorousMin)

        return WeeklyPlan(
            planName = "Endurance Training",
            goalEmoji = "🏔️",
            days = days,
            totalModerateMin = moderateMin,
            totalVigorousMin = vigorousMin,
            totalActiveMin = moderateMin + vigorousMin,
            meetsWHOGuideline = totalEquivalent >= guideline.moderateMinPerWeek.first,
            whoComplianceNote = "✅ ${totalEquivalent} moderate-equivalent min/week — exceeds WHO targets!"
        )
    }

    private fun generatePerformancePlan(
        fitnessLevel: FitnessLevel,
        guideline: WHOGuideline
    ): WeeklyPlan {
        val days = listOf(
            WeeklyPlanDay(
                dayName = "Monday",
                isRestDay = false,
                zoneFocus = "Zone 4-5",
                zoneNumbers = listOf(4, 5),
                durationMinutes = 25,
                activitySuggestion = "HIIT: 30s sprint / 90s recovery × 8",
                intensityLabel = "Very Hard",
                emoji = "🚀"
            ),
            WeeklyPlanDay(
                dayName = "Tuesday",
                isRestDay = false,
                zoneFocus = "Zone 2",
                zoneNumbers = listOf(2),
                durationMinutes = 35,
                activitySuggestion = "Easy recovery jog or walk",
                intensityLabel = "Easy",
                emoji = "🚶"
            ),
            WeeklyPlanDay(
                dayName = "Wednesday",
                isRestDay = false,
                zoneFocus = "Zone 3-4",
                zoneNumbers = listOf(3, 4),
                durationMinutes = 35,
                activitySuggestion = "Tempo run at threshold pace",
                intensityLabel = "Hard",
                emoji = "💪"
            ),
            WeeklyPlanDay("Thursday", isRestDay = true, emoji = "😴"),
            WeeklyPlanDay(
                dayName = "Friday",
                isRestDay = false,
                zoneFocus = "Zone 4-5",
                zoneNumbers = listOf(4, 5),
                durationMinutes = 20,
                activitySuggestion = "Speed intervals: 1 min on / 2 min off × 6",
                intensityLabel = "Very Hard",
                emoji = "⚡"
            ),
            WeeklyPlanDay(
                dayName = "Saturday",
                isRestDay = false,
                zoneFocus = "Zone 2-3",
                zoneNumbers = listOf(2, 3),
                durationMinutes = 50,
                activitySuggestion = "Long aerobic base session",
                intensityLabel = "Moderate",
                emoji = "🏃"
            ),
            WeeklyPlanDay("Sunday", isRestDay = true, emoji = "😴")
        )

        val moderateMin = days.filter { !it.isRestDay && it.zoneNumbers.all { z -> z <= 3 } }
            .sumOf { it.durationMinutes }
        val vigorousMin = days.filter { !it.isRestDay && it.zoneNumbers.any { z -> z >= 4 } }
            .sumOf { it.durationMinutes }
        val totalEquivalent = moderateMin + vigorousToModerateEquivalent(vigorousMin)

        return WeeklyPlan(
            planName = "Performance & Speed",
            goalEmoji = "🏆",
            days = days,
            totalModerateMin = moderateMin,
            totalVigorousMin = vigorousMin,
            totalActiveMin = moderateMin + vigorousMin,
            meetsWHOGuideline = totalEquivalent >= guideline.moderateMinPerWeek.first,
            whoComplianceNote = "✅ ${totalEquivalent} moderate-equivalent min/week — well above WHO targets!"
        )
    }
}
