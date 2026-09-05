package com.health.calculator.bmi.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.core.util.launchAsync
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.notification.WaterNotificationHelper
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import kotlinx.coroutines.flow.first

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        val prefs = WaterReminderPreferences(context)
        val settings = prefs.load()

        if (!settings.isEnabled) return

        val enableVibration = intent.getBooleanExtra("vibration", true)
        val enableSound = intent.getBooleanExtra("sound", true)
        val smartSkip = intent.getBooleanExtra("smart_skip", true)
        val behindNudge = intent.getBooleanExtra("behind_nudge", true)

        launchAsync {
            try {
                val frequencyMinutes = settings.frequencyMinutes.coerceAtLeast(1)

                if (smartSkip) {
                    val lastLogTime = prefs.getLastLogTime()
                    val timeSinceLastLog = System.currentTimeMillis() - lastLogTime
                    val skipThresholdMs = (frequencyMinutes * 60_000L * 0.5).toLong()

                    if (lastLogTime > 0 && timeSinceLastLog < skipThresholdMs) {
                        return@launchAsync
                    }
                }

                val dao = AppDatabase.getDatabase(context).waterIntakeDao()
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                // This is a one-shot alarm check. Using collect() here previously
                // kept the receiver suspended forever and prevented rescheduling.
                val currentMl = dao.getTotalWaterForDay(todayStart, todayEnd).first() ?: 0
                val goalMl = context
                    .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
                    .getInt("daily_goal_ml", 2500)
                    .coerceAtLeast(1)

                val isBehind = behindNudge && isUserBehindSchedule(currentMl, goalMl, settings)
                if (currentMl < goalMl || isBehind) {
                    WaterNotificationHelper(context).showReminderNotification(
                        currentMl = currentMl,
                        goalMl = goalMl,
                        enableVibration = enableVibration,
                        enableSound = enableSound,
                        isBehindSchedule = isBehind
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Every delivered reminder schedules the next occurrence, including
                // smart-skipped reminders and recoverable failures.
                WaterReminderScheduler(context).schedule(settings)
            }
        }
    }

    private fun isUserBehindSchedule(
        currentMl: Int,
        goalMl: Int,
        settings: WaterReminderSettings
    ): Boolean {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = settings.startHour * 60 + settings.startMinute
        val endMinutes = settings.endHour * 60 + settings.endMinute
        val totalActiveMinutes = endMinutes - startMinutes

        if (totalActiveMinutes <= 0) return false
        if (currentMinutes < startMinutes || currentMinutes > endMinutes) return false

        val elapsedMinutes = currentMinutes - startMinutes
        val progressFraction = (elapsedMinutes.toFloat() / totalActiveMinutes).coerceIn(0f, 1f)
        val expectedMl = (goalMl * progressFraction).toInt()
        val deficit = expectedMl - currentMl

        return deficit > (goalMl * 0.2f)
    }
}

