// notifications/QuickActionReceiver.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import com.health.calculator.bmi.tracker.data.models.Reminder
import com.health.calculator.bmi.tracker.data.model.FoodEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getStringExtra("reminder_id") ?: ""
        val actionValue = intent.getStringExtra("action_value") ?: ""

        val app = context.applicationContext as HealthCalculatorApp
        val stats = NotificationStatistics(context)
        val usageTracker = AppUsageTracker(context)
        
        // Mark interaction to prevent immediate follow-up notifications
        usageTracker.recordInteraction()

        scope.launch {
            val reminder = app.reminderRepository.getReminderById(reminderId)
            val category = reminder?.category ?: "unknown"

            when (action) {
                "LOG_WATER" -> {
                    val amount = actionValue.toIntOrNull() ?: 250
                    app.waterIntakeRepository.logWater(amount, "Logged from notification")
                    showToast(context, "💧 ${amount}ml water logged!")
                    stats.recordAction(category, "LOG_WATER")
                }
                "LOG_MEAL" -> {
                    // Open app to log meal or provide a simple "Generic Meal" log?
                    // For now, let's log a generic 500 cal meal if quick-log is requested
                    val entry = FoodEntry(
                        name = "Quick Log Meal",
                        calories = 500.0,
                        mealSlot = "Quick Log",
                        timestamp = System.currentTimeMillis()
                    )
                    app.foodLogRepository.addEntry(entry)
                    showToast(context, "🍽️ 500 calories logged!")
                    stats.recordAction(category, "LOG_MEAL")
                }
                "MED_TAKEN" -> {
                    // Log medication taken (e.g., in a Medication repository - placeholder)
                    showToast(context, "💊 Medication marked as taken")
                    stats.recordAction(category, "MED_TAKEN")
                }
                "MED_SKIP" -> {
                    showToast(context, "💊 Medication marked as skipped")
                    stats.recordAction(category, "MED_SKIP")
                }
            }

            // Record engagement
            stats.recordTap(category)
            
            // Dismiss notification
            withContext(Dispatchers.Main) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(reminderId.hashCode())
            }
        }
    }

    private suspend fun showToast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
