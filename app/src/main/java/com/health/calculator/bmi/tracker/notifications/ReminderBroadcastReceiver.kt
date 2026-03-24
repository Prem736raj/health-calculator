// notifications/ReminderBroadcastReceiver.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import com.health.calculator.bmi.tracker.data.models.QuietHours
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderBroadcastReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        
        val app = context.applicationContext as HealthCalculatorApp
        val repository = app.reminderRepository
        val rateLimiter = NotificationRateLimiter(context)
        val stats = NotificationStatistics(context)
        val dataProvider = NotificationContextDataProvider(
            context,
            app.waterIntakeRepository,
            app.foodLogRepository,
            app.historyRepository,
            app.profileRepository
        )
        val contentBuilder = SmartNotificationContentBuilder(context)
        val notificationBuilder = EnhancedNotificationBuilder(context)

        scope.launch {
            val reminder = repository.getReminderById(reminderId) ?: return@launch
            if (!reminder.isEnabled) return@launch

            // 1. Check Quiet Hours
            val quietHours = repository.getQuietHours()
            val now = Calendar.getInstance()
            if (quietHours.isEnabled && quietHours.isInQuietPeriod(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))) {
                if (!(reminder.isHighPriority && quietHours.allowEmergencyOverride)) {
                    return@launch
                }
            }

            // 2. Check Rate Limiter
            val rateLimitResult = rateLimiter.shouldSendNotification(reminder.isHighPriority, reminder.category)
            if (!rateLimitResult.allowed) {
                // Log reason for debugging but don't show
                return@launch
            }

            // 3. Gather Context Data
            val contextData = dataProvider.provideData()

            // 4. Build Smart Content
            val category = try {
                ReminderCategory.valueOf(reminder.category)
            } catch (e: Exception) {
                ReminderCategory.CUSTOM
            }

            val content = contentBuilder.buildContent(
                category = category,
                customTitle = reminder.title,
                customMessage = reminder.message,
                data = contextData
            )

            // 5. Show Notification
            val notification = notificationBuilder.buildNotification(reminder, content)
            NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)

            // 6. Record Stats & Rate Limit
            rateLimiter.recordNotificationSent(reminder.category)
            stats.recordSent(reminder.category)
            repository.updateLastTriggered(reminder.id, System.currentTimeMillis())
        }
    }
}
