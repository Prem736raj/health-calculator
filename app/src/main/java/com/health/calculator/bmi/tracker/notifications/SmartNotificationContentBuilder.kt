// notifications/SmartNotificationContentBuilder.kt
package com.health.calculator.bmi.tracker.notifications

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import java.util.Calendar

data class SmartNotificationContent(
    val title: String,
    val message: String,
    val bigText: String,
    val subText: String? = null,
    val priority: Int = 0, // 0 = default, 1 = high, -1 = low
    val category: String
)

class SmartNotificationContentBuilder(@ApplicationContext private val context: Context) {

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
        val title = customTitle ?: if (isEvening()) {
            "💧 Evening hydration check-in"
        } else {
            "💧 Time for a water break"
        }
        val message = customMessage
            ?: "If a drink fits your day, you can log it whenever it is convenient."

        return SmartNotificationContent(
            title = title,
            message = message,
            bigText = message,
            subText = null,
            priority = 0,
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

        val bigText = "$baseMessage\n\nIf you choose to measure, follow your device instructions and rest quietly first."

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            subText = null,
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

        val baseMessage = customMessage
            ?: "If a weekly weigh-in is part of your routine, you can record it when convenient."
        val bigText = baseMessage

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
        val message = customMessage
            ?: "Medication reminder. Follow the instructions you were given."

        val bigText = "$message\n\nIf you are unsure about your plan, contact a pharmacist or clinician."

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

        val baseMessage = customMessage
            ?: "If movement is part of your plan, consider a short activity that feels comfortable."
        val bigText = baseMessage

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

        val baseMessage = customMessage
            ?: "If you track meals, you can log what you ate when convenient."
        val bigText = baseMessage

        return SmartNotificationContent(
            title = title,
            message = baseMessage,
            bigText = bigText,
            subText = null,
            priority = 0,
            category = "calories"
        )
    }

    private fun buildCustomContent(
        customTitle: String?,
        customMessage: String?,
        data: NotificationContextData
    ): SmartNotificationContent {
        val title = customTitle ?: "🔔 Wellness reminder"
        val message = customMessage ?: "An optional wellness check-in is available."
        val bigText = message

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
