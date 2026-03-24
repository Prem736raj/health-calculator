// util/WaterShareHelper.kt
package com.health.calculator.bmi.tracker.util

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

object WaterShareHelper {

    /**
     * Share water achievement as formatted text
     */
    fun shareWaterAchievement(
        context: Context,
        currentMl: Int,
        goalMl: Int,
        streakDays: Int,
        percentage: Float
    ) {
        val currentL = currentMl / 1000f
        val goalL = goalMl / 1000f
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        val text = buildString {
            appendLine("💧 Water Intake Achievement 💧")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📅 ${dateFormat.format(Date())}")
            appendLine()
            appendLine("💦 Today's Intake: ${String.format("%.1f", currentL)}L / ${String.format("%.1f", goalL)}L")
            appendLine("📊 Progress: ${percentage.toInt()}%")

            if (percentage >= 100) {
                appendLine("🎉 Goal Achieved!")
            }

            if (streakDays > 0) {
                appendLine()
                appendLine("🔥 Current Streak: $streakDays day${if (streakDays > 1) "s" else ""}!")
            }

            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()

            // Motivational message based on progress
            val message = when {
                percentage >= 150 -> "Going above and beyond! 💪"
                percentage >= 100 -> "Crushed the goal today! 🏆"
                percentage >= 75 -> "Almost there, keep going! 👍"
                percentage >= 50 -> "Halfway done! ⚡"
                else -> "Every drop counts! 💧"
            }
            appendLine(message)

            appendLine()
            appendLine("📱 Tracked with Health Calculator: BMI Tracker")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My Water Intake Progress")
        }

        context.startActivity(
            Intent.createChooser(intent, "Share Water Achievement")
        )
    }

    /**
     * Share with simple message format
     */
    fun shareSimpleMessage(
        context: Context,
        currentL: Float,
        streakDays: Int
    ) {
        val message = if (streakDays > 0) {
            "I drank ${String.format("%.1f", currentL)} liters of water today! Day $streakDays streak! 💧🔥 - Health Calculator"
        } else {
            "I drank ${String.format("%.1f", currentL)} liters of water today! 💧 - Health Calculator"
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        context.startActivity(
            Intent.createChooser(intent, "Share")
        )
    }

    /**
     * Get shareable text for quick copy
     */
    fun getQuickShareText(currentMl: Int, goalMl: Int, streakDays: Int): String {
        val currentL = currentMl / 1000f
        return if (currentMl >= goalMl) {
            if (streakDays > 0) {
                "🎉 I hit my water goal today! ${String.format("%.1f", currentL)}L ✓ | Day $streakDays streak 🔥 #Hydration #HealthCalculator"
            } else {
                "🎉 I hit my water goal today! ${String.format("%.1f", currentL)}L ✓ #Hydration #HealthCalculator"
            }
        } else {
            val percentage = if (goalMl > 0) (currentMl.toFloat() / goalMl * 100).toInt() else 0
            "💧 Hydration progress: ${String.format("%.1f", currentL)}L ($percentage%) - staying healthy! #HealthCalculator"
        }
    }
}
