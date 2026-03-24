// File: com/health/calculator/bmi/tracker/data/model/BMRResultData.kt
package com.health.calculator.bmi.tracker.data.model

data class BMRResultData(
    val primaryBMR: Float = 0f,
    val selectedFormula: BMRFormula = BMRFormula.MIFFLIN_ST_JEOR,
    val allFormulaResults: Map<BMRFormula, Float> = emptyMap(),
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val age: Int = 0,
    val isMale: Boolean = true,
    val bodyFatPercentage: Float = 0f,
    val isUnitKg: Boolean = true,
    val isUnitCm: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) {
    val bmrPerHour: Float get() = primaryBMR / 24f
    val bmrInKJ: Float get() = primaryBMR * 4.184f
    val bmrPerHourKJ: Float get() = bmrInKJ / 24f

    val lowestBMR: Float get() = allFormulaResults.values.minOrNull() ?: primaryBMR
    val highestBMR: Float get() = allFormulaResults.values.maxOrNull() ?: primaryBMR
    val averageBMR: Float get() = if (allFormulaResults.isNotEmpty())
        allFormulaResults.values.average().toFloat() else primaryBMR
    val bmrRange: Float get() = highestBMR - lowestBMR

    fun getInterpretation(): String {
        return "Your body burns approximately ${primaryBMR.toInt()} calories per day " +
                "just to maintain basic life functions like breathing, circulation, " +
                "and cell production. This is your Basal Metabolic Rate using the " +
                "${selectedFormula.displayName} equation."
    }

    fun getBMRLevel(): BMRLevel {
        // General reference ranges (vary by age/gender but give rough guidance)
        return if (isMale) {
            when {
                primaryBMR < 1200 -> BMRLevel.LOW
                primaryBMR < 1600 -> BMRLevel.BELOW_AVERAGE
                primaryBMR < 2000 -> BMRLevel.AVERAGE
                primaryBMR < 2400 -> BMRLevel.ABOVE_AVERAGE
                else -> BMRLevel.HIGH
            }
        } else {
            when {
                primaryBMR < 1000 -> BMRLevel.LOW
                primaryBMR < 1300 -> BMRLevel.BELOW_AVERAGE
                primaryBMR < 1600 -> BMRLevel.AVERAGE
                primaryBMR < 1900 -> BMRLevel.ABOVE_AVERAGE
                else -> BMRLevel.HIGH
            }
        }
    }

    fun toShareText(): String {
        return buildString {
            append("🔥 My Basal Metabolic Rate (BMR)\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("BMR: ${primaryBMR.toInt()} kcal/day")
            append(" (${bmrInKJ.toInt()} kJ/day)\n")
            append("Formula: ${selectedFormula.displayName}\n")
            append("BMR per hour: ${String.format("%.1f", bmrPerHour)} kcal\n\n")
            append("📊 All Formula Comparison:\n")
            allFormulaResults.forEach { (formula, value) ->
                val marker = if (formula == selectedFormula) " ⬅️" else ""
                append("  • ${formula.displayName}: ${value.toInt()} kcal$marker\n")
            }
            append("\n⚠️ For informational purposes only.\n")
            append("Calculated using Health Calculator app.")
        }
    }

    fun toHistoryJson(): String {
        return buildString {
            append("{")
            append("\"bmr\":${primaryBMR},")
            append("\"formula\":\"${selectedFormula.name}\",")
            append("\"weightKg\":${weightKg},")
            append("\"heightCm\":${heightCm},")
            append("\"age\":${age},")
            append("\"gender\":\"${if (isMale) "Male" else "Female"}\",")
            append("\"bodyFat\":${bodyFatPercentage},")
            append("\"bmrKJ\":${bmrInKJ},")
            append("\"bmrPerHour\":${bmrPerHour}")
            append("}")
        }
    }
}

enum class BMRLevel(
    val label: String,
    val emoji: String,
    val description: String
) {
    LOW(
        "Below Typical Range",
        "🔵",
        "Your BMR is lower than typical. This could be due to lower body mass, age, or metabolism."
    ),
    BELOW_AVERAGE(
        "Slightly Below Average",
        "🟢",
        "Your BMR is slightly below average for your demographics."
    ),
    AVERAGE(
        "Typical Range",
        "🟢",
        "Your BMR falls within the typical range for your age and gender."
    ),
    ABOVE_AVERAGE(
        "Above Average",
        "🟡",
        "Your BMR is above average, likely due to higher body mass or muscle content."
    ),
    HIGH(
        "Well Above Average",
        "🟠",
        "Your BMR is notably high. This is common with larger body frames or high muscle mass."
    )
}
