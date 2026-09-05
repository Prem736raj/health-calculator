package com.health.calculator.bmi.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.health.calculator.bmi.tracker.core.navigation.AppEntryRouteResolver
import com.health.calculator.bmi.tracker.core.navigation.NavGraph
import com.health.calculator.bmi.tracker.core.navigation.Screen
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalytics
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import com.health.calculator.bmi.tracker.notifications.AppUsageTracker
import com.health.calculator.bmi.tracker.notifications.InactivityCheckScheduler
import com.health.calculator.bmi.tracker.notifications.NotificationChannelsManager
import com.health.calculator.bmi.tracker.notifications.StreakProtectionScheduler
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme
import com.health.calculator.bmi.tracker.widget.WaterWidgetDataProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var productAnalytics: ProductAnalytics

    private val pendingNavigation = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingNavigation.value = resolveEntryIntent(intent)

        // Collection is opt-in; this call is a no-op until the user enables it.
        productAnalytics.track(ProductAnalyticsEvent.APP_OPENED)

        try {
            val app = application as HealthCalculatorApp

            runCatching { NotificationChannelsManager.createAllChannels(this) }
            runCatching { AppUsageTracker(this).startTracking() }

            val inactivityRepo = runCatching { InactivityRepository(this) }.getOrNull()

            // Restore only explicitly enabled re-engagement schedules. New
            // installs remain quiet until a user enables a setting.
            lifecycleScope.launch {
                runCatching { inactivityRepo?.getInactivityState()?.first() }
                    .getOrNull()
                    ?.let { state ->
                        if (state.inactivityNotificationsEnabled) {
                            InactivityCheckScheduler(this@MainActivity).scheduleDaily()
                        } else {
                            InactivityCheckScheduler(this@MainActivity).cancel()
                        }
                        if (state.streakProtectionEnabled) {
                            StreakProtectionScheduler(this@MainActivity).scheduleEvening()
                        } else {
                            StreakProtectionScheduler(this@MainActivity).cancel()
                        }
                    }
            }

            setContent {
                val themeMode by app.themeModeFlow.collectAsStateWithLifecycle(
                    initialValue = ThemeMode.SYSTEM
                )

                HealthCalculatorTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val context = LocalContext.current

                        LaunchedEffect(Unit) {
                            try {
                                if (inactivityRepo != null) {
                                    // Read the previous timestamp before writing the new one.
                                    // The old ordering overwrote this value first, making the
                                    // natural welcome-back flow effectively impossible.
                                    val previousOpen = inactivityRepo.getLastAppOpenTime()
                                    val now = System.currentTimeMillis()
                                    val daysInactive = ((now - previousOpen) / DAY_MILLIS).toInt()
                                    val fromInactivity = intent.getBooleanExtra("from_inactivity", false)

                                    inactivityRepo.recordAppOpened()
                                    inactivityRepo.saveLastAppOpenTimeQuick()

                                    if (daysInactive >= 2 || fromInactivity) {
                                        inactivityRepo.markNeedsWelcomeBack()
                                        if (pendingNavigation.value == null) {
                                            pendingNavigation.value = WELCOME_BACK_ROUTE
                                        }
                                    }
                                }

                                runCatching {
                                    com.health.calculator.bmi.tracker.data.util.WaterDataIntegrity(context)
                                        .performIntegrityCheck()
                                }
                                runCatching { WaterWidgetDataProvider(context).refreshData() }
                            } catch (_: Exception) {
                                // Optional startup refreshes must never block
                                // navigation or write health values to logs.
                            }
                        }

                        // External entry points never bypass onboarding. Once the normal
                        // graph reaches an app destination, consume the allow-listed route.
                        LaunchedEffect(navController, app) {
                            pendingNavigation.filterNotNull().collectLatest { route ->
                                app.onboardingCompletedFlow.first { it }
                                navController.currentBackStackEntryFlow.first { entry ->
                                    entry.destination.route != Screen.Splash.route &&
                                        entry.destination.route != Screen.Onboarding.route
                                }

                                if (navController.currentDestination?.route != route) {
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                    }
                                }
                                pendingNavigation.compareAndSet(route, null)
                            }
                        }

                        NavGraph(navController = navController)
                    }
                }
            }
        } catch (t: Throwable) {
            writeCrashLog(t)
            throw t
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveEntryIntent(intent)?.let { pendingNavigation.value = it }
    }

    private fun resolveEntryIntent(intent: Intent): String? {
        return AppEntryRouteResolver.resolve(
            dataUri = intent.dataString,
            navigateTo = intent.getStringExtra("navigate_to"),
            navigateRoute = intent.getStringExtra("navigate_route"),
            showWelcomeBack = intent.getBooleanExtra("show_welcome_back", false)
        )
    }

    private fun writeCrashLog(throwable: Throwable) {
        runCatching {
            // Diagnostics stay in an app-private directory and are never
            // included in FileProvider share paths.
            val file = File(File(filesDir, "diagnostics").apply { mkdirs() }, "crash_log.txt")
            FileOutputStream(file, true).use { output ->
                PrintWriter(output).use { writer ->
                    writer.println("Crash at ${java.util.Date()}")
                    throwable.printStackTrace(writer)
                    writer.println()
                }
            }
        }
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        const val WELCOME_BACK_ROUTE = "welcome_back"
    }
}

