package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color

data class ActivityReference(
    val name: String,
    val emoji: String,
    val category: ActivityCategory,
    val zoneLabel: String,
    val zoneLow: Int, // zone number low
    val zoneHigh: Int, // zone number high
    val metValue: Float, // MET value for calorie estimation
    val description: String
)

enum class ActivityCategory(val label: String, val emoji: String) {
    DAILY("Daily Activities", "🏠"),
    CARDIO("Cardio & Endurance", "🏃"),
    STRENGTH("Strength & Flexibility", "🏋️"),
    SPORTS("Sports & Recreation", "⚽")
}

data class PersonalizedActivity(
    val activity: ActivityReference,
    val bpmLow: Int,
    val bpmHigh: Int,
    val caloriesPer30Min: Int,
    val zoneColor: Color,
    val zoneColorEnd: Color
)

object ActivityHeartRateReferenceEngine {

    private val activities = listOf(
        // DAILY ACTIVITIES
        ActivityReference(
            name = "Sleeping",
            emoji = "😴",
            category = ActivityCategory.DAILY,
            zoneLabel = "Below Zone 1",
            zoneLow = 0,
            zoneHigh = 0,
            metValue = 0.95f,
            description = "Heart rate drops to its lowest during deep sleep"
        ),
        ActivityReference(
            name = "Sitting / Desk Work",
            emoji = "💻",
            category = ActivityCategory.DAILY,
            zoneLabel = "Below Zone 1",
            zoneLow = 0,
            zoneHigh = 0,
            metValue = 1.3f,
            description = "Minimal cardiovascular demand; sedentary baseline"
        ),
        ActivityReference(
            name = "Standing / Light Tasks",
            emoji = "🧍",
            category = ActivityCategory.DAILY,
            zoneLabel = "Below Zone 1",
            zoneLow = 0,
            zoneHigh = 0,
            metValue = 1.8f,
            description = "Slightly above resting; organizing, cooking prep"
        ),
        ActivityReference(
            name = "Casual Walking",
            emoji = "🚶",
            category = ActivityCategory.DAILY,
            zoneLabel = "Zone 1",
            zoneLow = 1,
            zoneHigh = 1,
            metValue = 2.5f,
            description = "Easy stroll, window shopping, walking the dog"
        ),
        ActivityReference(
            name = "Housework",
            emoji = "🧹",
            category = ActivityCategory.DAILY,
            zoneLabel = "Zone 1-2",
            zoneLow = 1,
            zoneHigh = 2,
            metValue = 3.3f,
            description = "Vacuuming, mopping, laundry, cleaning"
        ),
        ActivityReference(
            name = "Gardening",
            emoji = "🌱",
            category = ActivityCategory.DAILY,
            zoneLabel = "Zone 1-2",
            zoneLow = 1,
            zoneHigh = 2,
            metValue = 3.8f,
            description = "Digging, planting, raking, general yard work"
        ),
        ActivityReference(
            name = "Playing with Kids",
            emoji = "👨👧",
            category = ActivityCategory.DAILY,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 4.0f,
            description = "Active play, chasing, carrying children"
        ),
        ActivityReference(
            name = "Climbing Stairs",
            emoji = "🪜",
            category = ActivityCategory.DAILY,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 5.0f,
            description = "Moderate pace stairs; higher floors push into Zone 3"
        ),

        // CARDIO & ENDURANCE
        ActivityReference(
            name = "Brisk Walking",
            emoji = "🚶♂️",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 2",
            zoneLow = 2,
            zoneHigh = 2,
            metValue = 4.3f,
            description = "5-6 km/h pace; can talk but slightly breathless"
        ),
        ActivityReference(
            name = "Light Jogging",
            emoji = "🏃♀️",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 7.0f,
            description = "Easy conversational pace, 7-8 km/h"
        ),
        ActivityReference(
            name = "Running",
            emoji = "🏃",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 9.8f,
            description = "Moderate to hard effort, 9-12 km/h"
        ),
        ActivityReference(
            name = "Sprinting",
            emoji = "💨",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 5",
            zoneLow = 5,
            zoneHigh = 5,
            metValue = 15.0f,
            description = "All-out maximum effort, short bursts only"
        ),
        ActivityReference(
            name = "Cycling (Leisure)",
            emoji = "🚲",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 6.8f,
            description = "Comfortable pace, 15-20 km/h"
        ),
        ActivityReference(
            name = "Cycling (Intense)",
            emoji = "🚴",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 10.0f,
            description = "Racing pace or steep hills, 25+ km/h"
        ),
        ActivityReference(
            name = "Swimming (Moderate)",
            emoji = "🏊",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.0f,
            description = "Steady laps, freestyle or breaststroke"
        ),
        ActivityReference(
            name = "Rowing",
            emoji = "🚣",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.5f,
            description = "Machine or water rowing at moderate intensity"
        ),
        ActivityReference(
            name = "Jump Rope",
            emoji = "⏫",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 11.0f,
            description = "Moderate to fast pace; excellent cardio"
        ),
        ActivityReference(
            name = "HIIT Workout",
            emoji = "⚡",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 4-5",
            zoneLow = 4,
            zoneHigh = 5,
            metValue = 12.5f,
            description = "High-intensity intervals with rest periods"
        ),
        ActivityReference(
            name = "Elliptical Trainer",
            emoji = "🏋️♀️",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 5.5f,
            description = "Low impact, moderate resistance"
        ),
        ActivityReference(
            name = "Stair Climber",
            emoji = "🪜",
            category = ActivityCategory.CARDIO,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 9.0f,
            description = "Machine-based stair climbing at steady pace"
        ),

        // STRENGTH & FLEXIBILITY
        ActivityReference(
            name = "Yoga",
            emoji = "🧘",
            category = ActivityCategory.STRENGTH,
            zoneLabel = "Zone 1-2",
            zoneLow = 1,
            zoneHigh = 2,
            metValue = 3.0f,
            description = "Hatha or restorative; power yoga may be higher"
        ),
        ActivityReference(
            name = "Pilates",
            emoji = "🤸",
            category = ActivityCategory.STRENGTH,
            zoneLabel = "Zone 1-2",
            zoneLow = 1,
            zoneHigh = 2,
            metValue = 3.5f,
            description = "Core-focused, controlled movements"
        ),
        ActivityReference(
            name = "Weight Training",
            emoji = "🏋️",
            category = ActivityCategory.STRENGTH,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 5.0f,
            description = "Moderate sets with rest; HR varies between sets"
        ),
        ActivityReference(
            name = "Circuit Training",
            emoji = "🔄",
            category = ActivityCategory.STRENGTH,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.0f,
            description = "Back-to-back exercises with minimal rest"
        ),
        ActivityReference(
            name = "Stretching",
            emoji = "🙆",
            category = ActivityCategory.STRENGTH,
            zoneLabel = "Zone 1",
            zoneLow = 1,
            zoneHigh = 1,
            metValue = 2.3f,
            description = "Static or dynamic stretching, cool-down"
        ),

        // SPORTS & RECREATION
        ActivityReference(
            name = "Dancing",
            emoji = "💃",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 5.5f,
            description = "Social or aerobic dancing; intensity varies"
        ),
        ActivityReference(
            name = "Tennis",
            emoji = "🎾",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 7.3f,
            description = "Singles match; doubles is lower intensity"
        ),
        ActivityReference(
            name = "Basketball",
            emoji = "🏀",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.0f,
            description = "Full-court game with running and jumping"
        ),
        ActivityReference(
            name = "Soccer / Football",
            emoji = "⚽",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.5f,
            description = "Match play with sprints and jogging"
        ),
        ActivityReference(
            name = "Hiking",
            emoji = "🥾",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 2-3",
            zoneLow = 2,
            zoneHigh = 3,
            metValue = 6.0f,
            description = "Trail walking; steeper terrain pushes higher"
        ),
        ActivityReference(
            name = "Martial Arts",
            emoji = "🥋",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 8.0f,
            description = "Karate, judo, boxing, kickboxing"
        ),
        ActivityReference(
            name = "Rock Climbing",
            emoji = "🧗",
            category = ActivityCategory.SPORTS,
            zoneLabel = "Zone 3-4",
            zoneLow = 3,
            zoneHigh = 4,
            metValue = 7.5f,
            description = "Indoor or outdoor climbing; sustained effort"
        )
    )

    fun getAllActivities(): List<ActivityReference> = activities

    fun getActivitiesByCategory(): Map<ActivityCategory, List<ActivityReference>> {
        return activities.groupBy { it.category }
    }

    /**
     * Personalize activities with user's actual BPM ranges and calorie estimates
     */
    fun personalizeActivities(
        zones: List<HeartRateZone>,
        weightKg: Float,
        restingHR: Int? = null
    ): Map<ActivityCategory, List<PersonalizedActivity>> {
        val belowZone1Low = restingHR ?: zones.firstOrNull()?.let { it.bpmLow - 20 } ?: 60
        val belowZone1High = zones.firstOrNull()?.bpmLow ?: 90

        return activities.groupBy { it.category }.mapValues { (_, activityList) ->
            activityList.map { activity ->
                val bpmLow: Int
                val bpmHigh: Int
                val zoneColor: Color
                val zoneColorEnd: Color

                if (activity.zoneLow == 0) {
                    // Below Zone 1
                    bpmLow = belowZone1Low
                    bpmHigh = belowZone1High
                    zoneColor = Color(0xFF9E9E9E)
                    zoneColorEnd = Color(0xFF90CAF9)
                } else {
                    val lowZone = zones.getOrNull(activity.zoneLow - 1)
                    val highZone = zones.getOrNull(activity.zoneHigh - 1)
                    bpmLow = lowZone?.bpmLow ?: 0
                    bpmHigh = highZone?.bpmHigh ?: 0
                    zoneColor = lowZone?.color ?: Color(0xFF90CAF9)
                    zoneColorEnd = highZone?.color ?: zoneColor
                }

                // Calories per 30 min = (MET × weight × 3.5) / 200 × 30
                val calPerMin = (activity.metValue * weightKg * 3.5f) / 200f
                val calPer30 = (calPerMin * 30).toInt()

                PersonalizedActivity(
                    activity = activity,
                    bpmLow = bpmLow,
                    bpmHigh = bpmHigh,
                    caloriesPer30Min = calPer30,
                    zoneColor = zoneColor,
                    zoneColorEnd = zoneColorEnd
                )
            }
        }
    }
}
