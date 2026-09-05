package com.health.calculator.bmi.tracker.core.util

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Runs asynchronous receiver work while keeping the BroadcastReceiver process
 * eligible to finish only after the work completes.
 */
fun BroadcastReceiver.launchAsync(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

