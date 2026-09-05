package com.health.calculator.bmi.tracker.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import com.health.calculator.bmi.tracker.core.util.launchAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuickActionReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_LOG_WATER) return

        val reminderId = intent.getStringExtra("reminder_id") ?: ""
        val amount = intent.getStringExtra("action_value")
            ?.toIntOrNull()
            ?.coerceIn(MIN_WATER_ML, MAX_WATER_ML)
            ?: DEFAULT_WATER_ML

        val app = context.applicationContext as HealthCalculatorApp
        val stats = NotificationStatistics(context)
        AppUsageTracker(context).recordInteraction()

        launchAsync {
            val reminder = app.reminderRepository.getReminderById(reminderId)
            val category = reminder?.category ?: "unknown"

            app.waterIntakeRepository.logWater(amount, "Logged from notification")
            showToast(context, "💧 ${amount}ml water logged")
            stats.recordAction(category, ACTION_LOG_WATER)
            stats.recordTap(category)

            withContext(Dispatchers.Main) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(reminderId.hashCode())
            }
        }
    }

    private suspend fun showToast(@ApplicationContext context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val ACTION_LOG_WATER = "LOG_WATER"
        const val DEFAULT_WATER_ML = 250
        const val MIN_WATER_ML = 50
        const val MAX_WATER_ML = 2_000
    }
}

