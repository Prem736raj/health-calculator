// receiver/WaterQuickLogReceiver.kt
package com.health.calculator.bmi.tracker.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.notification.WaterNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WaterQuickLogReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WaterNotificationHelper.QUICK_LOG_ACTION) {
            val amountMl = intent.getIntExtra("amount_ml", 250)

            scope.launch {
                try {
                    // Access database directly for quick log from notification
                    val db = com.health.calculator.bmi.tracker.data.local.AppDatabase
                        .getDatabase(context)
                    val dao = db.waterIntakeDao()
                    dao.insertWaterLog(
                        WaterIntakeLog(
                            amountMl = amountMl,
                            note = "Quick log from notification"
                        )
                    )

                    // Update last log time
                    val prefs = context.getSharedPreferences(
                        "water_reminder_prefs", Context.MODE_PRIVATE
                    )
                    prefs.edit()
                        .putLong("last_log_time", System.currentTimeMillis())
                        .apply()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Dismiss notification
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(WaterNotificationHelper.NOTIFICATION_ID)
        }
    }
}
