package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel

data class ZoneDistribution(
    val zoneNumber: Int,
    val zoneName: String,
    val percentage: Int,
    val color: Color,
    val icon: String
)

data class WorkoutRecommendation(
    val goalName: String,
    val goalEmoji: String,
    val goalDescription: String,
    val primaryZone: String,
    val zoneDistribution: List<ZoneDistribution>,
    val durationRange: String,
    val frequencyPerWeek: String,
    val keyAdvice: String,
    val sampleWorkout: String,
    val tips: List<String>,
    val isRecommendedForLevel: Boolean = false
)

data class ZoneCalorieBurn(
    val zoneNumber: Int,
    val zoneName: String,
    val caloriesPer30Min: Int,
    val color: Color,
    val icon: String
)

object HeartRateZoneRecommendationEngine {

    /**
     * Estimate calories burned per 30 minutes in each zone
     * Based on weight and approximate MET values per zone
     */
    fun estimateCaloriesPerZone(
        weightKg: Float,
        zones: List<HeartRateZone>
    ): List<ZoneCalorieBurn> {
        // Approximate MET values per zone
        val metValues = listOf(3.5f, 5.5f, 8.0f, 11.0f, 14.0f)

        return zones.mapIndexed { index, zone ->
            val met = metValues.getOrElse(index) { 5f }
            // Calories per minute = (MET × weight_kg × 3.5) / 200
            val calPerMin = (met * weightKg * 3.5f) / 200f
            val calPer30 = (calPerMin * 30).toInt()

            ZoneCalorieBurn(
                zoneNumber = zone.zoneNumber,
                zoneName = zone.zoneName,
                caloriesPer30Min = calPer30,
                color = zone.color,
                icon = zone.icon
            )
        }
    }

    /**
     * Generate goal-specific workout recommendations
     */
    fun generateRecommendations(
        fitnessLevel: FitnessLevel,
        zones: List<HeartRateZone>
    ): List<WorkoutRecommendation> {
        val z1Color = zones.getOrNull(0)?.color ?: Color(0xFF90CAF9)
        val z2Color = zones.getOrNull(1)?.color ?: Color(0xFF42A5F5)
        val z3Color = zones.getOrNull(2)?.color ?: Color(0xFF66BB6A)
        val z4Color = zones.getOrNull(3)?.color ?: Color(0xFFFFA726)
        val z5Color = zones.getOrNull(4)?.color ?: Color(0xFFEF5350)

        val z2Range = zones.getOrNull(1)?.let { "${it.bpmLow}-${it.bpmHigh}" } ?: ""
        val z3Range = zones.getOrNull(2)?.let { "${it.bpmLow}-${it.bpmHigh}" } ?: ""
        val z4Range = zones.getOrNull(3)?.let { "${it.bpmLow}-${it.bpmHigh}" } ?: ""
        val z5Range = zones.getOrNull(4)?.let { "${it.bpmLow}-${it.bpmHigh}" } ?: ""

        return listOf(
            // WEIGHT LOSS
            WorkoutRecommendation(
                goalName = "Weight Loss",
                goalEmoji = "⚖️",
                goalDescription = "Maximize fat burning while preserving muscle mass",
                primaryZone = "Zone 2 (Fat Burn) — $z2Range BPM",
                zoneDistribution = listOf(
                    ZoneDistribution(1, "Recovery", 5, z1Color, "🚶"),
                    ZoneDistribution(2, "Fat Burn", 60, z2Color, "🔥"),
                    ZoneDistribution(3, "Aerobic", 30, z3Color, "💪"),
                    ZoneDistribution(4, "Anaerobic", 5, z4Color, "⚡")
                ),
                durationRange = "30-60 minutes",
                frequencyPerWeek = "4-5 times/week",
                keyAdvice = "Spend most of your workout in Zone 2 for optimal fat burning. " +
                        "Your body uses a higher percentage of fat as fuel in this zone. " +
                        "Add Zone 3 intervals to boost total calorie burn.",
                sampleWorkout = buildString {
                    appendLine("🏃 Fat Burn Session (45 min)")
                    appendLine("• 5 min warm-up in Zone 1")
                    appendLine("• 15 min steady Zone 2 ($z2Range BPM)")
                    appendLine("• 5 min push to Zone 3 ($z3Range BPM)")
                    appendLine("• 10 min back to Zone 2")
                    appendLine("• 5 min Zone 3 push")
                    appendLine("• 5 min cool-down in Zone 1")
                },
                tips = listOf(
                    "Morning fasted workouts in Zone 2 may enhance fat oxidation",
                    "Longer duration in Zone 2 burns more total fat than short Zone 5 bursts",
                    "Combine with a moderate calorie deficit for best results",
                    "Walking, light jogging, cycling are ideal Zone 2 activities",
                    "Monitor your breathing — you should be able to talk comfortably"
                ),
                isRecommendedForLevel = fitnessLevel == FitnessLevel.BEGINNER || 
                        fitnessLevel == FitnessLevel.INTERMEDIATE
            ),

            // CARDIOVASCULAR FITNESS
            WorkoutRecommendation(
                goalName = "Cardiovascular Fitness",
                goalEmoji = "❤️",
                goalDescription = "Strengthen your heart and improve aerobic capacity",
                primaryZone = "Zone 3 (Aerobic) — $z3Range BPM",
                zoneDistribution = listOf(
                    ZoneDistribution(1, "Recovery", 5, z1Color, "🚶"),
                    ZoneDistribution(2, "Fat Burn", 15, z2Color, "🔥"),
                    ZoneDistribution(3, "Aerobic", 55, z3Color, "💪"),
                    ZoneDistribution(4, "Anaerobic", 20, z4Color, "⚡"),
                    ZoneDistribution(5, "VO₂ Max", 5, z5Color, "🚀")
                ),
                durationRange = "30-45 minutes",
                frequencyPerWeek = "3-5 times/week",
                keyAdvice = "Focus on sustained effort in Zone 3 to build a strong cardiovascular base. " +
                        "This zone strengthens your heart muscle, improves blood flow, " +
                        "and increases your body's ability to transport and use oxygen.",
                sampleWorkout = buildString {
                    appendLine("❤️ Cardio Builder (40 min)")
                    appendLine("• 5 min warm-up in Zone 1-2")
                    appendLine("• 10 min steady Zone 3 ($z3Range BPM)")
                    appendLine("• 3 min Zone 4 push ($z4Range BPM)")
                    appendLine("• 5 min Zone 2 recovery")
                    appendLine("• 10 min Zone 3 steady")
                    appendLine("• 2 min Zone 4 push")
                    appendLine("• 5 min cool-down in Zone 1")
                },
                tips = listOf(
                    "Aim for at least 150 min/week of Zone 3 activity (WHO guideline)",
                    "Running, swimming, rowing are excellent Zone 3 activities",
                    "Consistency matters more than intensity for heart health",
                    "Track your resting HR over time — it should decrease as fitness improves",
                    "You should be able to speak in short sentences in this zone"
                ),
                isRecommendedForLevel = fitnessLevel == FitnessLevel.INTERMEDIATE
            ),

            // ENDURANCE
            WorkoutRecommendation(
                goalName = "Endurance Training",
                goalEmoji = "🏔️",
                goalDescription = "Build stamina for longer, sustained activities",
                primaryZone = "Zone 2-3 (Fat Burn to Aerobic) — $z2Range to $z3Range BPM",
                zoneDistribution = listOf(
                    ZoneDistribution(1, "Recovery", 5, z1Color, "🚶"),
                    ZoneDistribution(2, "Fat Burn", 40, z2Color, "🔥"),
                    ZoneDistribution(3, "Aerobic", 40, z3Color, "💪"),
                    ZoneDistribution(4, "Tempo", 12, z4Color, "⚡"),
                    ZoneDistribution(5, "Sprint", 3, z5Color, "🚀")
                ),
                durationRange = "45-90 minutes",
                frequencyPerWeek = "3-4 times/week",
                keyAdvice = "The 80/20 rule: spend 80% of your training time in Zone 2-3 " +
                        "and only 20% in Zone 4-5. This polarized approach builds the aerobic " +
                        "base you need for long-distance performance.",
                sampleWorkout = buildString {
                    appendLine("🏔️ Long Endurance Session (60 min)")
                    appendLine("• 10 min warm-up in Zone 1-2")
                    appendLine("• 20 min Zone 2 steady ($z2Range BPM)")
                    appendLine("• 15 min Zone 3 tempo ($z3Range BPM)")
                    appendLine("• 3 min Zone 4 push ($z4Range BPM)")
                    appendLine("• 5 min Zone 2 recovery")
                    appendLine("• 2 min Zone 4 push")
                    appendLine("• 5 min cool-down Zone 1")
                },
                tips = listOf(
                    "Build volume (time) before adding intensity",
                    "Increase weekly duration by no more than 10% per week",
                    "Fuel properly — endurance training requires adequate carbohydrates",
                    "Include one long slow session per week (all Zone 2)",
                    "Rest days are crucial for adaptation and improvement",
                    "Ideal for marathon training, long cycling, hiking preparation"
                ),
                isRecommendedForLevel = fitnessLevel == FitnessLevel.INTERMEDIATE || 
                        fitnessLevel == FitnessLevel.ADVANCED
            ),

            // PERFORMANCE / SPEED
            WorkoutRecommendation(
                goalName = "Performance & Speed",
                goalEmoji = "🏆",
                goalDescription = "Push limits with high-intensity interval training",
                primaryZone = "Zone 4-5 (Anaerobic/VO₂ Max) — $z4Range to $z5Range BPM",
                zoneDistribution = listOf(
                    ZoneDistribution(1, "Recovery", 10, z1Color, "🚶"),
                    ZoneDistribution(2, "Active Rec.", 30, z2Color, "🔥"),
                    ZoneDistribution(3, "Transition", 10, z3Color, "💪"),
                    ZoneDistribution(4, "Anaerobic", 30, z4Color, "⚡"),
                    ZoneDistribution(5, "VO₂ Max", 20, z5Color, "🚀")
                ),
                durationRange = "20-30 minutes",
                frequencyPerWeek = "2-3 times/week",
                keyAdvice = "High-intensity intervals boost speed, power, and VO₂ max. " +
                        "Alternate between Zone 4-5 effort and Zone 2 recovery. " +
                        "Quality over quantity — keep sessions short but intense.",
                sampleWorkout = buildString {
                    appendLine("🏆 HIIT Session (25 min)")
                    appendLine("• 5 min warm-up (Zone 1→2→3)")
                    appendLine("• 30 sec ALL-OUT Zone 5 ($z5Range BPM)")
                    appendLine("• 90 sec Zone 2 recovery ($z2Range BPM)")
                    appendLine("• Repeat 8 times")
                    appendLine("• 4 min cool-down Zone 1")
                    appendLine()
                    appendLine("💡 Alternative: Tempo Run")
                    appendLine("• 5 min warm-up Zone 2")
                    appendLine("• 15 min sustained Zone 4 ($z4Range BPM)")
                    appendLine("• 5 min cool-down Zone 1-2")
                },
                tips = listOf(
                    "⚠️ Only attempt if you have a solid fitness base (Zone 3 comfortable)",
                    "Always warm up thoroughly before Zone 5 efforts",
                    "Limit HIIT to 2-3 times per week to avoid overtraining",
                    "HIIT burns more total calories in less time (EPOC effect)",
                    "Include easy Zone 2 days between HIIT sessions",
                    "Sprinting, cycling intervals, rowing are excellent HIIT activities"
                ),
                isRecommendedForLevel = fitnessLevel == FitnessLevel.ADVANCED
            ),

            // BEGINNER SAFE START
            WorkoutRecommendation(
                goalName = "Beginner Safe Start",
                goalEmoji = "🌱",
                goalDescription = "Build a foundation safely and sustainably",
                primaryZone = "Zone 1-2 (Recovery to Fat Burn) — up to $z2Range BPM",
                zoneDistribution = listOf(
                    ZoneDistribution(1, "Recovery", 40, z1Color, "🚶"),
                    ZoneDistribution(2, "Fat Burn", 50, z2Color, "🔥"),
                    ZoneDistribution(3, "Aerobic", 10, z3Color, "💪")
                ),
                durationRange = "20-30 minutes",
                frequencyPerWeek = "3 times/week",
                keyAdvice = "Start slow and build gradually. Stay in Zone 1-2 for the first 2-4 weeks. " +
                        "Your body needs time to adapt. Walking is a perfect Zone 1-2 exercise. " +
                        "Don't push into Zone 4-5 until you have a solid base fitness level.",
                sampleWorkout = buildString {
                    appendLine("🌱 Beginner Walk-Jog (25 min)")
                    appendLine("• 5 min easy walk Zone 1")
                    appendLine("• 3 min brisk walk Zone 2 ($z2Range BPM)")
                    appendLine("• 1 min easy walk Zone 1")
                    appendLine("• 3 min brisk walk Zone 2")
                    appendLine("• 1 min easy walk")
                    appendLine("• 3 min brisk walk Zone 2")
                    appendLine("• 1 min easy walk")
                    appendLine("• 3 min brisk walk Zone 2")
                    appendLine("• 5 min cool-down easy walk")
                },
                tips = listOf(
                    "If you can't talk comfortably, slow down — you're going too hard",
                    "Consistency beats intensity: 3 easy sessions > 1 hard session",
                    "Increase duration by 5 minutes each week",
                    "After 4 weeks, gradually introduce Zone 3",
                    "Listen to your body — rest if you feel pain or extreme fatigue",
                    "Walking, light cycling, swimming are ideal starter activities",
                    "Celebrate every workout — you're building a healthy habit!"
                ),
                isRecommendedForLevel = fitnessLevel == FitnessLevel.BEGINNER
            )
        )
    }
}
