// File: com/health/calculator/bmi/tracker/ui/screens/bmr/BMRShareFormatter.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr

import com.health.calculator.bmi.tracker.data.model.*

object BMRShareFormatter {

    fun formatCompleteResult(
        resultData: BMRResultData,
        activityLevel: ActivityLevel,
        macroBreakdown: MacroBreakdown?,
        tefData: TEFData?,
        mealCount: Int = 3
    ): String {
        val tdee = resultData.primaryBMR * activityLevel.multiplier
        val activityCals = tdee - resultData.primaryBMR

        return buildString {
            append("🔥 BMR & TDEE Results\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

            // BMR
            append("📊 Basal Metabolic Rate (BMR)\n")
            append("   ${resultData.primaryBMR.toInt()} kcal/day")
            append(" (${resultData.bmrInKJ.toInt()} kJ/day)\n")
            append("   Formula: ${resultData.selectedFormula.displayName}\n")
            append("   Per hour: ${String.format("%.1f", resultData.bmrPerHour)} kcal\n\n")

            // TDEE
            append("⚡ Total Daily Energy Expenditure (TDEE)\n")
            append("   ${tdee.toInt()} kcal/day\n")
            append("   Activity: ${activityLevel.displayName} (×${activityLevel.multiplier})\n")
            append("   BMR: ${resultData.primaryBMR.toInt()}")
            append(" + Activity: ${activityCals.toInt()}")
            append(" = ${tdee.toInt()} kcal\n\n")

            // TEF
            if (tefData != null) {
                append("🌡️ Thermic Effect of Food (TEF)\n")
                append("   ${tefData.totalTEF.toInt()} kcal/day")
                append(" (~${tefData.tefPercentOfIntake.toInt()}% of intake)\n")
                append("   Adjusted Total: ${tefData.adjustedTDEE.toInt()} kcal/day\n\n")
            }

            // Macros
            if (macroBreakdown != null) {
                append("🥗 Macronutrient Breakdown")
                append(" (${macroBreakdown.dietApproach.displayName})\n")
                append("   🔵 Protein: ${macroBreakdown.proteinGrams.toInt()}g")
                append(" (${macroBreakdown.proteinCalories.toInt()} kcal)")
                append(" — ${macroBreakdown.proteinPercentage.toInt()}%\n")
                append("   🟡 Carbs: ${macroBreakdown.carbsGrams.toInt()}g")
                append(" (${macroBreakdown.carbsCalories.toInt()} kcal)")
                append(" — ${macroBreakdown.carbsPercentage.toInt()}%\n")
                append("   🟠 Fat: ${macroBreakdown.fatGrams.toInt()}g")
                append(" (${macroBreakdown.fatCalories.toInt()} kcal)")
                append(" — ${macroBreakdown.fatPercentage.toInt()}%\n\n")

                // Per meal
                append("🍽️ Per Meal ($mealCount meals/day)\n")
                append("   ${(macroBreakdown.totalCalories / mealCount).toInt()} kcal |")
                append(" P: ${(macroBreakdown.proteinGrams / mealCount).toInt()}g |")
                append(" C: ${(macroBreakdown.carbsGrams / mealCount).toInt()}g |")
                append(" F: ${(macroBreakdown.fatGrams / mealCount).toInt()}g\n\n")
            }

            // Formula comparison
            if (resultData.allFormulaResults.size > 1) {
                append("📐 Formula Comparison:\n")
                resultData.allFormulaResults.entries
                    .sortedByDescending { it.value }
                    .forEach { (formula, value) ->
                        val marker = if (formula == resultData.selectedFormula) " ✓" else ""
                        append("   ${formula.displayName}: ${value.toInt()} kcal$marker\n")
                    }
                append("\n")
            }

            // Input data
            append("📋 Input Data:\n")
            append("   Weight: ${String.format("%.1f", resultData.weightKg)} kg\n")
            append("   Height: ${String.format("%.0f", resultData.heightCm)} cm\n")
            append("   Age: ${resultData.age} | Gender: ${if (resultData.isMale) "Male" else "Female"}\n")
            if (resultData.bodyFatPercentage > 0) {
                append("   Body Fat: ${String.format("%.1f", resultData.bodyFatPercentage)}%\n")
            }

            append("\n⚠️ For informational purposes only. Consult a healthcare professional.\n")
            append("📱 Calculated using Health Calculator: BMI Tracker")
        }
    }

    fun formatQuickResult(
        bmr: Float,
        tdee: Float,
        formulaName: String,
        activityLevel: String
    ): String {
        return buildString {
            append("🔥 My BMR: ${bmr.toInt()} kcal/day\n")
            append("⚡ My TDEE: ${tdee.toInt()} kcal/day\n")
            append("📐 Formula: $formulaName\n")
            append("🏃 Activity: $activityLevel\n")
            append("\n📱 Health Calculator: BMI Tracker")
        }
    }
}
