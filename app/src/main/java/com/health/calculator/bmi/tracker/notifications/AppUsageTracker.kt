// notifications/AppUsageTracker.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

class AppUsageTracker(context: Context) {

    private val rateLimiter = NotificationRateLimiter(context)

    fun startTracking() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                        rateLimiter.recordAppUsed()
                    }
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                        rateLimiter.recordAppUsed() // Record when leaving too
                    }
                    else -> {}
                }
            }
        })
    }

    /**
     * Manual tracking for specific interactions.
     */
    fun recordInteraction() {
        rateLimiter.recordAppUsed()
    }
}
