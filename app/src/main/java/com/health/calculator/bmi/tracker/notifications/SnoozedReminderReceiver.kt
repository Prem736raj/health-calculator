// notifications/SnoozedReminderReceiver.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SnoozedReminderReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val snoozeMinutes = intent.getIntExtra("snooze_minutes", 15)

        // Dismiss current notification
        NotificationManagerCompat.from(context).cancel(reminderId.hashCode())

        val app = context.applicationContext as HealthCalculatorApp
        val repository = app.reminderRepository
        val scheduler = ReminderScheduler(context)

        scope.launch {
            val reminder = repository.getReminderById(reminderId)
            if (reminder != null) {
                // Schedule a one-time snooze alarm
                scheduler.scheduleSnooze(reminder, snoozeMinutes)
                
                // Track statistics
                NotificationStatistics(context).recordAction(reminder.category, "SNOOZE")
            }
        }
    }
}
