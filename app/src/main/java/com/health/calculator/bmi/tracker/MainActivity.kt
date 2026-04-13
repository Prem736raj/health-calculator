package com.health.calculator.bmi.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.health.calculator.bmi.tracker.core.navigation.NavGraph
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            // Get the Application instance for theme flow access
            val app = application as HealthCalculatorApp

            // Initialize Notification Channels
            try {
                com.health.calculator.bmi.tracker.notifications.NotificationChannelsManager.createAllChannels(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Start App Usage Tracking for Notification Rate Limiting
            try {
                com.health.calculator.bmi.tracker.notifications.AppUsageTracker(this).startTracking()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Initialize Re-engagement Schedulers
            try {
                com.health.calculator.bmi.tracker.notifications.InactivityCheckScheduler(this).scheduleDaily()
                com.health.calculator.bmi.tracker.notifications.StreakProtectionScheduler(this).scheduleEvening()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Record App Open for Inactivity tracking
            var inactivityRepo: com.health.calculator.bmi.tracker.data.repository.InactivityRepository? = null
            try {
                inactivityRepo = com.health.calculator.bmi.tracker.data.repository.InactivityRepository(this)
                inactivityRepo.saveLastAppOpenTimeQuick()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            setContent {
                // Observe theme mode from DataStore — recomposes entire theme on change
                val themeMode by app.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

                HealthCalculatorTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        val context = androidx.compose.ui.platform.LocalContext.current
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            try {
                                // Mark for Welcome Back check
                                if (inactivityRepo != null) {
                                    val fromInactivity = intent.getBooleanExtra("from_inactivity", false)
                                    val lastOpen = inactivityRepo.getLastAppOpenTime()
                                    val daysInactive = ((System.currentTimeMillis() - lastOpen) / (24 * 60 * 60 * 1000)).toInt()

                                    if (daysInactive >= 2 || fromInactivity) {
                                        inactivityRepo.markNeedsWelcomeBack()
                                    }
                                }

                                // Perform water data integrity check on app start
                                try {
                                    val dataIntegrity = com.health.calculator.bmi.tracker.data.util.WaterDataIntegrity(context)
                                    dataIntegrity.performIntegrityCheck()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                // Refresh widget cache
                                try {
                                    val widgetProvider = com.health.calculator.bmi.tracker.widget.WaterWidgetDataProvider(context)
                                    widgetProvider.refreshData()
                                } catch (e: Exception) {
                                    // Widget provider not critical
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        NavGraph(navController = navController)
                    }
                }
            }
        } catch (t: Throwable) {
            // Write crash log to file
            try {
                val file = File(cacheDir, "crash_log.txt")
                FileOutputStream(file, true).use { fos ->
                    PrintWriter(fos).use { pw ->
                        pw.println("Crash at ${java.util.Date()}")
                        t.printStackTrace(pw)
                        pw.println()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
            throw t
        }
    }
}
