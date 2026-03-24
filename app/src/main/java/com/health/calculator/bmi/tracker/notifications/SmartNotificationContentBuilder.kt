// notifications/SmartNotificationContentBuilder.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.Context
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import java.util.Calendar
import kotlin.random.Random

data class SmartNotificationContent(
    val title: String,
    val message: String,
    val bigText: String,
    val subText: String? = null,
    val priority: Int = 0, // 0 = default, 1 = high, -1 = low
    val category: String
)

class SmartNotificationContentBuilder(private val context: Context) {

    fun buildContent(
        category: ReminderCategory,
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        return when (category) {
            ReminderCategory.WATER_INTAKE -> buildWaterContent(customTitle, customMessage, data)
            ReminderCategory.BLOOD_PRESSURE -> buildBpContent(customTitle, customMessage, data)
            ReminderCategory.WEIGHT_CHECK -> buildWeightContent(customTitle, customMessage, data)
            ReminderCategory.MEDICATION -> buildMedicationContent(customTitle, customMessage, data)
            ReminderCategory.EXERCISE -> buildExerciseContent(customTitle, customMessage, data)
            ReminderCategory.CALORIE_LOGGING -> buildCalorieContent(customTitle, customMessage, data)
            ReminderCategory.CUSTOM -> buildCustomContent(customTitle, customMessage, data)
        }
    }

    private fun buildWaterContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val progressPercent = if (data.waterGoalMl > 0) {
            (data.waterIntakeMl.toFloat() / data.waterGoalMl * 100).toInt()
        } else 0

        val isBehind = isAfternoon() && progressPercent < 50
        val isEvening = isEvening()

        val title = customTitle ?: when {
            progressPercent >= 100 -> "🎉 Goal reached! Keep it up!"
            isBehind -> "💧 You're behind on hydration"
            isEvening -> "💧 Evening hydration check"
            else -> "💧 Time to hydrate!"
        }

        val progressText = "${data.waterIntakeMl}ml / ${data.waterGoalMl}ml (${progressPercent}%)"

        val message = when {
            progressPercent >= 100 -> "You've already met your goal! Great job staying hydrated."
            isBehind -> "You're at $progressPercent% of today's goal. Catch up with a big glass!"
            progressPercent >= 75 -> "Almost there! Just ${data.waterGoalMl - data.waterIntakeMl}ml more to reach your goal."
            progressPercent >= 50 -> "Halfway there! Keep drinking to hit your target."
            else -> customMessage ?: "Don't forget to drink water. You're at $progressPercent% of today's goal."
        }

        val bigText = buildString {
            append(message)
            if (data.waterStreak > 0) {
                append("\n\n🔥 Current streak: ${data.waterStreak} day${if (data.waterStreak > 1) "s" else ""}")
            }
            append("\n💧 Today: $progressText")
        }

        return SmartNotificationContent(
            title = title,
            message = message,
            bigText = bigText,
            subText = progressText,
            priority = if (isBehind) 1 else 0,
            category = "water"
        )
    }

    private fun buildBpContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: when {
            isMorning() -> "☀️ Morning BP Check"
            isEvening() -> "🌙 Evening BP Check"
            else -> "❤️ Time to check your BP"
        }

        val baseMessage = customMessage ?: "Take a moment to measure your blood pressure."

        val bigText = buildString {
            append(baseMessage)
            if (data.bpTrackingStreak > 0) {
                append("\n\n📅 Tracking streak: ${data.bpTrackingStreak} day${if (data.bpTrackingStreak > 1) "s" else ""}!")
            }
            data.lastBpReading?.let {
                append("\n❤️ Last reading: $it")
            }
            append("\n\n💡 Tip: Sit quietly for 5 minutes before measuring.")
        }

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            subText = if (data.bpTrackingStreak > 0) "Streak: ${data.bpTrackingStreak} days" else null,
            priority = 0,
            category = "bp"
        )
    }

    private fun buildWeightContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: "⚖️ Weekly Weigh-in"

        val baseMessage = customMessage ?: "Time for your weekly weigh-in!"

        val bigText = buildString {
            append(baseMessage)
            if (data.weightGoalKg != null && data.currentWeightKg != null) {
                val weightGoalKg = data.weightGoalKg!!
                val currentWeightKg = data.currentWeightKg!!
                val diff = kotlin.math.abs(weightGoalKg - currentWeightKg)
                val formatted = String.format("%.1f", diff)
                val direction = if (weightGoalKg < currentWeightKg) "lose" else "gain"
                append("\n\n🎯 ${formatted}kg to $direction to reach your goal!")
            }
            if (data.weightTrackingWeeks > 0) {
                append("\n📊 You've been tracking for ${data.weightTrackingWeeks} weeks")
            }
            append("\n\n💡 Tip: Weigh yourself in the morning, after bathroom, before eating.")
        }

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            priority = 0,
            category = "weight"
        )
    }

    private fun buildMedicationContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: "💊 Medication Reminder"
        val message = customMessage ?: "Time to take your medication."

        val bigText = buildString {
            append(message)
            append("\n\n⚠️ Don't skip doses. Consistency is key for effectiveness.")
        }

        return SmartNotificationContent(
            title = title,
            message = message,
            bigText = bigText,
            priority = 1, // High priority for medication
            category = "medication"
        )
    }

    private fun buildExerciseContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: when {
            isMorning() -> "🏃 Morning workout time!"
            isEvening() -> "🏃 Time for your evening workout"
            else -> "🏃 Ready to exercise?"
        }

        val motivationalMessages = listOf(
            "Movement is medicine!",
            "Every step counts.",
            "Your future self will thank you.",
            "Let's get those endorphins flowing!",
            "Just 30 minutes can make a huge difference."
        )

        val baseMessage = customMessage ?: motivationalMessages.random()

        val bigText = buildString {
            append(baseMessage)
            if (data.maxHeartRate > 0) {
                val zone2Low = (data.maxHeartRate * 0.6).toInt()
                val zone2High = (data.maxHeartRate * 0.7).toInt()
                append("\n\n💓 Your fat-burn zone: $zone2Low - $zone2High BPM")
            }
            if (data.exerciseMinutesThisWeek > 0) {
                append("\n🏅 This week: ${data.exerciseMinutesThisWeek} min (WHO recommends 150 min/week)")
            }
        }

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            priority = 0,
            category = "exercise"
        )
    }

    private fun buildCalorieContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val mealType = getMealType()

        val title = customTitle ?: "🍽️ Log your $mealType"

        val baseMessage = customMessage ?: "Don't forget to log what you ate!"

        val bigText = buildString {
            append(baseMessage)
            if (data.calorieGoal > 0) {
                val remaining = data.calorieGoal - data.caloriesConsumed
                if (remaining > 0) {
                    append("\n\n📊 Today: ${data.caloriesConsumed} / ${data.calorieGoal} cal")
                    append("\n🎯 ${remaining} calories remaining")
                } else {
                    append("\n\n⚠️ You've exceeded your calorie goal by ${-remaining} cal")
                }
            }
            if (data.calorieLoggingStreak > 0) {
                append("\n🔥 Logging streak: ${data.calorieLoggingStreak} days")
            }
        }

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            subText = if (data.calorieGoal > 0) "${data.caloriesConsumed}/${data.calorieGoal} cal" else null,
            priority = 0,
            category = "calories"
        )
    }

    private fun buildCustomContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: "🔔 Health Reminder"
        val message = customMessage ?: "Time for your health check-in!"

        val bigText = buildString {
            append(message)
            if (data.daysSinceLastAppUse > 2) {
                append("\n\n👋 We've missed you! It's been ${data.daysSinceLastAppUse} days since your last check-in.")
            }
            if (data.healthScore > 0) {
                append("\n🏆 Your health score: ${data.healthScore}/100")
            }
        }

        return SmartNotificationContent(
            title = title,
            message = message,
            bigText = bigText,
            priority = 0,
            category = "custom"
        )
    }

    // Time helpers
    private fun isMorning(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 5..11
    }

    private fun isAfternoon(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 12..17
    }

    private fun isEvening(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 18..22
    }

    private fun getMealType(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "breakfast"
            in 11..14 -> "lunch"
            in 15..17 -> "snack"
            in 18..21 -> "dinner"
            else -> "meal"
        }
    }
}

/**
 * Data class containing context for smart notification generation.
 * Fetched from repositories before showing notification.
 */
data class NotificationContextData(
    // Water
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val waterStreak: Int = 0,

    // Blood Pressure
    val bpTrackingStreak: Int = 0,
    val lastBpReading: String? = null,

    // Weight
    val currentWeightKg: Double? = null,
    val weightGoalKg: Double? = null,
    val weightTrackingWeeks: Int = 0,

    // Exercise
    val maxHeartRate: Int = 0,
    val exerciseMinutesThisWeek: Int = 0,

    // Calories
    val caloriesConsumed: Int = 0,
    val calorieGoal: Int = 2000,
    val calorieLoggingStreak: Int = 0,

    // General
    val healthScore: Int = 0,
    val daysSinceLastAppUse: Int = 0
)
