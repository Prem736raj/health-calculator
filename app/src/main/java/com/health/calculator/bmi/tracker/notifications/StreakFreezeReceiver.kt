// app/src/main/java/com/health/calculator/bmi/tracker/notifications/StreakFreezeReceiver.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreakFreezeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val streakType = intent.getStringExtra("streak_type") ?: "water"

        CoroutineScope(Dispatchers.Main).launch {
            val prefs = context.getSharedPreferences("streak_protection_prefs", Context.MODE_PRIVATE)
            val freezeCount = prefs.getInt("streak_freeze_count", 1)

            if (freezeCount > 0) {
                prefs.edit()
                    .putInt("streak_freeze_count", freezeCount - 1)
                    .putBoolean("freeze_used_${streakType}_${getTodayKey()}", true)
                    .apply()

                Toast.makeText(
                    context,
                    "🛡️ Streak freeze activated! Your streak is safe for today.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "No streak freezes available. Log something to save your streak!",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Dismiss notification
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(StreakProtectionReceiver.NOTIFICATION_ID)
        }
    }

    private fun getTodayKey(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(java.util.Calendar.YEAR)}_${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
    }
}
