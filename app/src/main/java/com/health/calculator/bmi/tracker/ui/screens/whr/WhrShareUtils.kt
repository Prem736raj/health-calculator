package com.health.calculator.bmi.tracker.ui.screens.whr

import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object WhrShareUtils {

    fun buildShareText(result: WhrResult): String {
        return buildString {
            appendLine("📐 Waist-to-Hip Ratio Result")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("WHR: ${String.format("%.2f", result.whr)} — ${result.whrCategory.label} (WHO)")
            appendLine("Waist: ${String.format("%.1f", result.waistCm)} cm")
            appendLine("Hip: ${String.format("%.1f", result.hipCm)} cm")
            appendLine("Body Shape: ${result.bodyShape.emoji} ${result.bodyShape.label}")
            appendLine("Waist Risk: ${result.waistRiskLevel.label}")
            result.whtr?.let {
                appendLine("WHtR: ${String.format("%.2f", it)} — ${if (result.whtrAtRisk == true) "At Risk" else "Normal"}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Calculated using Health Calculator: BMI Tracker")
        }
    }

    fun buildCompactShareText(result: WhrResult): String {
        return "WHR: ${String.format("%.2f", result.whr)} - ${result.whrCategory.label} (WHO) | Waist: ${String.format("%.0f", result.waistCm)}cm | Hip: ${String.format("%.0f", result.hipCm)}cm"
    }

    fun buildDetailedShareText(
        result: WhrResult,
        visceralFat: VisceralFatAssessment? = null
    ): String {
        return buildString {
            appendLine("📐 WHR Health Assessment Report")
            appendLine("══════════════════════════════")
            appendLine()
            appendLine("📊 Waist-to-Hip Ratio")
            appendLine("   Value: ${String.format("%.2f", result.whr)}")
            appendLine("   Category: ${result.whrCategory.label} (WHO Standard)")
            appendLine("   Gender: ${if (result.gender == Gender.FEMALE) "Female" else "Male"}")
            appendLine("   Age: ${result.age} years")
            appendLine()
            appendLine("📏 Measurements")
            appendLine("   Waist: ${String.format("%.1f", result.waistCm)} cm")
            appendLine("   Hip: ${String.format("%.1f", result.hipCm)} cm")
            appendLine()
            appendLine("🏥 Waist Circumference Risk")
            appendLine("   Status: ${result.waistRiskLevel.label}")
            appendLine("   Threshold: ${if (result.gender == Gender.FEMALE) "80 cm (increased) / 88 cm (high)" else "94 cm (increased) / 102 cm (high)"}")
            appendLine()
            result.whtr?.let {
                appendLine("📐 Waist-to-Height Ratio")
                appendLine("   Value: ${String.format("%.2f", it)}")
                appendLine("   Status: ${if (result.whtrAtRisk == true) "At Risk (>0.5)" else "Normal (<0.5)"}")
                appendLine()
            }
            appendLine("🧍 Body Shape: ${result.bodyShape.emoji} ${result.bodyShape.label}")
            appendLine()
            visceralFat?.let {
                appendLine("🔥 Visceral Fat Estimation")
                appendLine("   Level: ${it.estimatedLevel}/20")
                appendLine("   Risk: ${it.riskLevel.label}")
                appendLine()
            }
            appendLine("══════════════════════════════")
            appendLine("Calculated on ${SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())}")
            appendLine("Health Calculator: BMI Tracker")
        }
    }

    fun shareResult(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My WHR Result — Health Calculator")
        }
        context.startActivity(Intent.createChooser(intent, "Share WHR Result"))
    }
}
