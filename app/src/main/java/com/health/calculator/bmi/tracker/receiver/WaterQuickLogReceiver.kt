package com.health.calculator.bmi.tracker.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.core.util.launchAsync
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.notification.WaterNotificationHelper
import com.health.calculator.bmi.tracker.widget.WidgetDataNotifier
import dagger.hilt.android.qualifiers.ApplicationContext

class WaterQuickLogReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        if (intent.action != WaterNotificationHelper.QUICK_LOG_ACTION) return

        val amountMl = intent.getIntExtra("amount_ml", DEFAULT_AMOUNT_ML)
            .coerceIn(MIN_AMOUNT_ML, MAX_AMOUNT_ML)

        launchAsync {
            try {
                AppDatabase.getDatabase(context).waterIntakeDao().insertWaterLog(
                    WaterIntakeLog(
                        amountMl = amountMl,
                        note = "Quick log from notification"
                    )
                )

                context.getSharedPreferences("water_reminder_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_log_time", System.currentTimeMillis())
                    .apply()

                WidgetDataNotifier.notifyWaterChanged(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(WaterNotificationHelper.NOTIFICATION_ID)
            }
        }
    }

    private companion object {
        const val DEFAULT_AMOUNT_ML = 250
        const val MIN_AMOUNT_ML = 50
        const val MAX_AMOUNT_ML = 2_000
    }
}

