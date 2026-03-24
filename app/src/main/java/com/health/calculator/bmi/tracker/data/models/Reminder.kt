package com.health.calculator.bmi.tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String, // ReminderCategory.name
    val title: String,
    val message: String,
    val isEnabled: Boolean = true,
    val times: String = "", // comma-separated "HH:mm" times
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon through 7=Sun
    val soundUri: String? = null,
    val soundName: String = "Default",
    val vibrationEnabled: Boolean = true,
    val isHighPriority: Boolean = false,
    val navigateRoute: String? = null, // deep link on tap
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null
) {
    fun getTimesList(): List<String> =
        times.split(",").filter { it.isNotBlank() }.map { it.trim() }

    fun getDaysList(): List<Int> =
        daysOfWeek.split(",").filter { it.isNotBlank() }.map { it.trim().toInt() }

    fun getDaysDisplayText(): String {
        val days = getDaysList()
        if (days.size == 7) return "Every day"
        if (days == listOf(1, 2, 3, 4, 5)) return "Weekdays"
        if (days == listOf(6, 7)) return "Weekends"
        val names = mapOf(
            1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
            5 to "Fri", 6 to "Sat", 7 to "Sun"
        )
        return days.mapNotNull { names[it] }.joinToString(", ")
    }

    fun getTimesDisplayText(): String {
        val list = getTimesList()
        if (list.isEmpty()) return "No time set"
        if (list.size == 1) return list.first()
        return "${list.size} times/day"
    }
}

enum class ReminderCategory(
    val displayName: String,
    val icon: String,
    val defaultTitle: String,
    val defaultMessage: String,
    val defaultRoute: String?,
    val suggestedTimes: List<String>
) {
    WATER_INTAKE(
        displayName = "Water Intake",
        icon = "💧",
        defaultTitle = "Time to Hydrate!",
        defaultMessage = "Don't forget to drink water. Stay hydrated!",
        defaultRoute = "water_intake",
        suggestedTimes = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
    ),
    BLOOD_PRESSURE(
        displayName = "Blood Pressure",
        icon = "❤️",
        defaultTitle = "BP Check Time",
        defaultMessage = "Time to measure your blood pressure.",
        defaultRoute = "blood_pressure",
        suggestedTimes = listOf("08:00", "20:00")
    ),
    WEIGHT_CHECK(
        displayName = "Weight Check",
        icon = "⚖️",
        defaultTitle = "Weekly Weigh-in",
        defaultMessage = "Time for your weekly weigh-in! Morning, after bathroom, before eating.",
        defaultRoute = "weight_tracking",
        suggestedTimes = listOf("07:00")
    ),
    MEDICATION(
        displayName = "Medication",
        icon = "💊",
        defaultTitle = "Medication Reminder",
        defaultMessage = "Time to take your medication.",
        defaultRoute = null,
        suggestedTimes = listOf("08:00", "20:00")
    ),
    EXERCISE(
        displayName = "Exercise",
        icon = "🏃",
        defaultTitle = "Time to Move!",
        defaultMessage = "Get active! Even a short walk makes a difference.",
        defaultRoute = "heart_rate",
        suggestedTimes = listOf("07:00", "17:00")
    ),
    CALORIE_LOGGING(
        displayName = "Calorie Logging",
        icon = "🍽️",
        defaultTitle = "Log Your Meals",
        defaultMessage = "Don't forget to log what you ate!",
        defaultRoute = "calorie",
        suggestedTimes = listOf("09:00", "13:00", "19:00")
    ),
    CUSTOM(
        displayName = "Custom",
        icon = "🔔",
        defaultTitle = "Health Reminder",
        defaultMessage = "Your custom health reminder",
        defaultRoute = null,
        suggestedTimes = listOf("09:00")
    );

    companion object {
        fun fromName(name: String): ReminderCategory =
            entries.find { it.name == name } ?: CUSTOM
    }
}
