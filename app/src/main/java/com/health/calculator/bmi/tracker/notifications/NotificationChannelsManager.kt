// notifications/NotificationChannelsManager.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

object NotificationChannelsManager {

    // Channel IDs
    const val CHANNEL_HEALTH_REMINDERS = "health_reminders_high"
    const val CHANNEL_WATER_REMINDERS = "water_reminders"
    const val CHANNEL_WEEKLY_REPORTS = "weekly_reports"
    const val CHANNEL_ACHIEVEMENTS = "achievements"
    const val CHANNEL_APP_UPDATES = "app_updates"

    data class ChannelConfig(
        val id: String,
        val name: String,
        val description: String,
        val importance: Int,
        val showBadge: Boolean = true,
        val enableVibration: Boolean = true,
        val enableLights: Boolean = true
    )

    val channels = listOf(
        ChannelConfig(
            id = CHANNEL_HEALTH_REMINDERS,
            name = "Health Reminders",
            description = "Important health measurement reminders like BP checks and medication",
            importance = NotificationManager.IMPORTANCE_HIGH,
            showBadge = true,
            enableVibration = true
        ),
        ChannelConfig(
            id = CHANNEL_WATER_REMINDERS,
            name = "Water Reminders",
            description = "Hydration reminders throughout the day",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            showBadge = false,
            enableVibration = true
        ),
        ChannelConfig(
            id = CHANNEL_WEEKLY_REPORTS,
            name = "Weekly Reports",
            description = "Weekly health summary reports",
            importance = NotificationManager.IMPORTANCE_LOW,
            showBadge = true,
            enableVibration = false
        ),
        ChannelConfig(
            id = CHANNEL_ACHIEVEMENTS,
            name = "Achievements",
            description = "Notifications when you earn badges and milestones",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            showBadge = true,
            enableVibration = true
        ),
        ChannelConfig(
            id = CHANNEL_APP_UPDATES,
            name = "App Updates",
            description = "App updates and new features",
            importance = NotificationManager.IMPORTANCE_LOW,
            showBadge = false,
            enableVibration = false
        )
    )

    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            channels.forEach { config ->
                val channel = NotificationChannel(
                    config.id,
                    config.name,
                    config.importance
                ).apply {
                    description = config.description
                    setShowBadge(config.showBadge)
                    enableVibration(config.enableVibration)
                    enableLights(config.enableLights)
                    if (config.enableVibration) {
                        vibrationPattern = longArrayOf(0, 250, 100, 250)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun getChannelIdForCategory(category: String): String {
        return when (category) {
            "WATER_INTAKE" -> CHANNEL_WATER_REMINDERS
            "BLOOD_PRESSURE", "MEDICATION" -> CHANNEL_HEALTH_REMINDERS
            "WEIGHT_CHECK", "EXERCISE", "CALORIE_LOGGING" -> CHANNEL_HEALTH_REMINDERS
            else -> CHANNEL_HEALTH_REMINDERS
        }
    }

    fun getPriorityForChannel(channelId: String): Int {
        return when (channelId) {
            CHANNEL_HEALTH_REMINDERS -> NotificationCompat.PRIORITY_HIGH
            CHANNEL_WATER_REMINDERS -> NotificationCompat.PRIORITY_DEFAULT
            CHANNEL_WEEKLY_REPORTS -> NotificationCompat.PRIORITY_LOW
            CHANNEL_ACHIEVEMENTS -> NotificationCompat.PRIORITY_DEFAULT
            CHANNEL_APP_UPDATES -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun getChannelSettings(context: Context): List<ChannelSettingInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return channels.map { config ->
                ChannelSettingInfo(
                    id = config.id,
                    name = config.name,
                    description = config.description,
                    isEnabled = true,
                    importance = config.importance
                )
            }
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return channels.map { config ->
            val channel = notificationManager.getNotificationChannel(config.id)
            ChannelSettingInfo(
                id = config.id,
                name = config.name,
                description = config.description,
                isEnabled = channel?.importance != NotificationManager.IMPORTANCE_NONE,
                importance = channel?.importance ?: config.importance
            )
        }
    }
}

data class ChannelSettingInfo(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val importance: Int
)
