package com.health.calculator.bmi.tracker.data.management

import android.app.ActivityManager
import android.content.Context

/**
 * Requests Android to clear this application's complete private data set.
 *
 * This is intentionally delegated to the operating system instead of manually
 * deleting selected Room/DataStore/SharedPreferences files. A successful call
 * clears all app-private data and normally terminates the process, matching the
 * user-facing promise of "Clear all data".
 */
object FullAppDataResetter {
    fun request(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        return activityManager.clearApplicationUserData()
    }
}

