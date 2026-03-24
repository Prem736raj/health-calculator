// receiver/WaterReminderReceiver.kt
package com.health.calculator.bmi.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.notification.WaterNotificationHelper
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class WaterReminderReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = WaterReminderPreferences(context)
        val settings = prefs.load()

        if (!settings.isEnabled) return

        val enableVibration = intent.getBooleanExtra("vibration", true)
        val enableSound = intent.getBooleanExtra("sound", true)
        val smartSkip = intent.getBooleanExtra("smart_skip", true)
        val behindNudge = intent.getBooleanExtra("behind_nudge", true)

        scope.launch {
            try {
                // Smart skip: check if user logged water recently
                if (smartSkip) {
                    val lastLogTime = prefs.getLastLogTime()
                    val timeSinceLastLog = System.currentTimeMillis() - lastLogTime
                    val skipThresholdMs = (settings.frequencyMinutes * 60 * 1000 * 0.5).toLong()

                    if (lastLogTime > 0 && timeSinceLastLog < skipThresholdMs) {
                        // User logged recently, skip this reminder
                        scheduleNext(context, settings)
                        return@launch
                    }
                }

                // Get today's water total
                val db = com.health.calculator.bmi.tracker.data.local.AppDatabase
                    .getDatabase(context)
                val dao = db.waterIntakeDao()

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

                // Collect total once
                dao.getTotalWaterForDay(todayStart, todayEnd).collect { total ->
                    val currentMl = total ?: 0
                    val goalMl = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
                        .getInt("daily_goal_ml", 2500)

                    // Check if behind schedule
                    val isBehind = if (behindNudge) {
                        isUserBehindSchedule(currentMl, goalMl, settings)
                    } else false

                    // Don't notify if goal already reached (unless behind)
                    if (currentMl < goalMl || isBehind) {
                        val helper = WaterNotificationHelper(context)
                        helper.showReminderNotification(
                            currentMl = currentMl,
                            goalMl = goalMl,
                            enableVibration = enableVibration,
                            enableSound = enableSound,
                            isBehindSchedule = isBehind
                        )
                    }

                    return@collect
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Schedule next alarm
            scheduleNext(context, settings)
        }
    }

    private fun isUserBehindSchedule(currentMl: Int, goalMl: Int, settings: WaterReminderSettings): Boolean {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = settings.startHour * 60 + settings.startMinute
        val endMinutes = settings.endHour * 60 + settings.endMinute

        if (currentMinutes < startMinutes || currentMinutes > endMinutes) return false

        val totalActiveMinutes = endMinutes - startMinutes
        val elapsedMinutes = currentMinutes - startMinutes
        val progressFraction = elapsedMinutes.toFloat() / totalActiveMinutes

        val expectedMl = (goalMl * progressFraction).toInt()
        val deficit = expectedMl - currentMl

        // Consider behind if more than 20% below expected
        return deficit > (goalMl * 0.2f)
    }

    private fun scheduleNext(context: Context, settings: WaterReminderSettings) {
        WaterReminderScheduler(context).schedule(settings)
    }
}
