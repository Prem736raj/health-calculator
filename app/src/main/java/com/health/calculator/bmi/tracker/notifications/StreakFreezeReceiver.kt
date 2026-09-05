package com.health.calculator.bmi.tracker.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext

class StreakFreezeReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        val streakType = intent.getStringExtra("streak_type") ?: "water"
        val prefs = context.getSharedPreferences("streak_protection_prefs", Context.MODE_PRIVATE)
        val freezeCount = prefs.getInt("streak_freeze_count", 1)

        if (freezeCount > 0) {
            prefs.edit()
                .putInt("streak_freeze_count", freezeCount - 1)
                .putBoolean("freeze_used_${streakType}_${getTodayKey()}", true)
                .apply()

            Toast.makeText(
                context,
                "Day buffer used. Your streak will not be advanced or reset today.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "No day buffers are available. You can still skip today; your history stays saved.",
                Toast.LENGTH_LONG
            ).show()
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(StreakProtectionReceiver.NOTIFICATION_ID)
    }

    private fun getTodayKey(): String {
        val calendar = java.util.Calendar.getInstance()
        return "${calendar.get(java.util.Calendar.YEAR)}_${calendar.get(java.util.Calendar.DAY_OF_YEAR)}"
    }
}

