// data/model/HydrationTools.kt
package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UrineColor(
    val level: Int,
    val colorHex: Long,
    val label: String,
    val status: String,
    val hydrationLevel: HydrationLevel
) {
    LEVEL_1(1, 0xFFFFF9C4, "Pale Straw", "Well hydrated", HydrationLevel.WELL_HYDRATED),
    LEVEL_2(2, 0xFFFFF176, "Light Yellow", "Well hydrated", HydrationLevel.WELL_HYDRATED),
    LEVEL_3(3, 0xFFFFEE58, "Pale Gold", "Adequately hydrated", HydrationLevel.ADEQUATE),
    LEVEL_4(4, 0xFFFFD54F, "Yellow", "Adequately hydrated", HydrationLevel.ADEQUATE),
    LEVEL_5(5, 0xFFFFCA28, "Dark Yellow", "Slightly dehydrated", HydrationLevel.SLIGHTLY_DEHYDRATED),
    LEVEL_6(6, 0xFFFFA726, "Amber", "Slightly dehydrated", HydrationLevel.SLIGHTLY_DEHYDRATED),
    LEVEL_7(7, 0xFFFF8F00, "Dark Amber", "Dehydrated", HydrationLevel.DEHYDRATED),
    LEVEL_8(8, 0xFFE65100, "Brown/Orange", "Severely dehydrated", HydrationLevel.DEHYDRATED)
}

enum class HydrationLevel(val displayName: String, val emoji: String) {
    WELL_HYDRATED("Well Hydrated", "✅"),
    ADEQUATE("Adequately Hydrated", "👍"),
    SLIGHTLY_DEHYDRATED("Slightly Dehydrated", "⚠️"),
    DEHYDRATED("Dehydrated", "🚨")
}

@Entity(tableName = "urine_color_log")
data class UrineColorEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val colorLevel: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class DehydrationSymptom(
    val name: String,
    val icon: String,
    val description: String,
    val severity: SymptomSeverity
)

enum class SymptomSeverity { MILD, MODERATE, SEVERE }

data class WaterRichFood(
    val name: String,
    val icon: String,
    val waterPercent: Int,
    val servingSize: String,
    val waterMl: Int
)

enum class DehydrationRisk(val label: String, val emoji: String, val colorHex: Long) {
    NONE("No Signs", "✅", 0xFF4CAF50),
    MILD("Mild Risk", "💡", 0xFFFFC107),
    MODERATE("Moderate Risk", "⚠️", 0xFFFF9800),
    HIGH("High Risk", "🚨", 0xFFF44336)
}
