package com.health.calculator.bmi.tracker.core.navigation

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import com.health.calculator.bmi.tracker.core.constants.AppConstants
import com.health.calculator.bmi.tracker.presentation.home.HomeScreen
import com.health.calculator.bmi.tracker.presentation.home.HomeViewModel
import com.health.calculator.bmi.tracker.presentation.profile.ProfileScreen
import com.health.calculator.bmi.tracker.presentation.settings.SettingsScreen
import com.health.calculator.bmi.tracker.ui.screens.history.HistoryScreen
import com.health.calculator.bmi.tracker.ui.screens.onboarding.OnboardingScreen
import com.health.calculator.bmi.tracker.ui.screens.splash.SplashScreen
import com.health.calculator.bmi.tracker.ui.screens.articles.HealthArticlesScreen
import com.health.calculator.bmi.tracker.ui.screens.export.ExportDataScreen
import com.health.calculator.bmi.tracker.ui.screens.common.PlaceholderScreen
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.BmiCalculatorScreen
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.BmiViewModel
import com.health.calculator.bmi.tracker.ui.screens.bmr.BMRCalculatorScreen
import com.health.calculator.bmi.tracker.presentation.profile.ProfileViewModel
import com.health.calculator.bmi.tracker.presentation.weight.WeightTrackingScreen
import com.health.calculator.bmi.tracker.presentation.weight.WeightTrackingViewModel
import com.health.calculator.bmi.tracker.presentation.profile.MultiProfileViewModel
import com.health.calculator.bmi.tracker.presentation.profile.HealthConnectionsScreen
import com.health.calculator.bmi.tracker.data.model.Gender
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome.MetabolicSyndromeScreen
import com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome.MetabolicSyndromeViewModel
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BloodPressureScreen
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpLogScreen
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpTrendScreen
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpReminderScreen
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpExportScreen
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpEducationalScreen
import com.health.calculator.bmi.tracker.ui.screens.bsa.BSAScreen
import com.health.calculator.bmi.tracker.ui.screens.ibw.IBWScreen
import com.health.calculator.bmi.tracker.ui.screens.calorie.CalorieScreen
import com.health.calculator.bmi.tracker.ui.screens.calorie.FoodLogScreen
import com.health.calculator.bmi.tracker.ui.screens.calorie.CalorieViewModel
import com.health.calculator.bmi.tracker.ui.screens.calorie.FoodLogViewModel
import com.health.calculator.bmi.tracker.ui.screens.heartrate.HeartRateZoneResultScreen
import com.health.calculator.bmi.tracker.ui.screens.heartrate.HeartRateZoneScreen
import com.health.calculator.bmi.tracker.util.HeartRateZoneResult
import com.health.calculator.bmi.tracker.ui.screens.history.HistoryViewModel
import com.health.calculator.bmi.tracker.data.model.toHistoryEntry
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BloodPressureViewModel
import com.health.calculator.bmi.tracker.ui.screens.settings.DataManagementScreen
import com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesScreen
import com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesViewModel
import com.health.calculator.bmi.tracker.ui.screens.reminders.RemindersScreen
import com.health.calculator.bmi.tracker.ui.screens.reminders.RemindersViewModel
import com.health.calculator.bmi.tracker.notifications.ReminderScheduler
import com.health.calculator.bmi.tracker.ui.screens.welcomeback.WelcomeBackViewModel
import com.health.calculator.bmi.tracker.ui.screens.welcomeback.WelcomeBackScreen
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import com.health.calculator.bmi.tracker.ui.screens.aicoach.AiCoachScreen
import com.health.calculator.bmi.tracker.presentation.components.BottomNavigationBar
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorDestination
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorsHubScreen
import com.health.calculator.bmi.tracker.presentation.navigation.InsightsHubScreen
import com.health.calculator.bmi.tracker.presentation.navigation.TrackHubScreen
import com.health.calculator.bmi.tracker.di.AnalyticsEntryPoint
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import dagger.hilt.android.EntryPointAccessors

private const val NAV_ANIMATION_DURATION = AppConstants.ANIMATION_DURATION_MEDIUM
const val WATER_REMINDER_SETTINGS_ROUTE = "water_reminder_settings"
const val WATER_HISTORY_ROUTE = "water_history"
const val WATER_GAMIFICATION_ROUTE = "water_gamification"
const val WATER_TOOLS_ROUTE = "water_hydration_tools"
const val WATER_EDUCATION_ROUTE = "water_education"
const val ELECTROLYTE_INFO_ROUTE = "electrolyte_info"

/**
 * True when the root graph must provide the status-bar inset. Home and Splash
 * are the only destinations without a child Scaffold that owns that inset.
 */
internal fun rootOwnsSystemBarInsets(route: String?): Boolean {
    return route == null || route == Screen.Home.route || route == Screen.Splash.route
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as HealthCalculatorApp
    val scope = rememberCoroutineScope()
    val productAnalytics = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AnalyticsEntryPoint::class.java
        ).productAnalytics()
    }

    // Track whether onboarding has been completed
    val onboardingCompleted by app.onboardingCompletedFlow.collectAsStateWithLifecycle(initialValue = null)

    // Wait until we know the onboarding state before rendering
    if (onboardingCompleted == null) return

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Each destination owns its own status-bar treatment. The root graph only
    // needs the inset for Home (a scrolling dashboard without a child
    // Scaffold) and Splash. Applying system-bar insets here as well as inside
    // a destination TopAppBar creates a visible blank strip above most screens
    // on edge-to-edge devices.
    val shouldApplyRootSystemBarInsets = rootOwnsSystemBarInsets(currentRoute)

    Scaffold(
        modifier = modifier,
        contentWindowInsets = if (shouldApplyRootSystemBarInsets) {
            ScaffoldDefaults.contentWindowInsets
        } else {
            WindowInsets(0, 0, 0, 0)
        },
        bottomBar = {
            if (Screen.isBottomNavRoute(currentRoute)) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if (currentRoute == item.route) return@BottomNavigationBar

                        val surface = when (item.route) {
                            Screen.Home.route -> "home"
                            Screen.Track.route -> "track"
                            Screen.Calculators.route -> "calculators"
                            Screen.Insights.route -> "insights"
                            Screen.Profile.route -> "profile"
                            else -> null
                        }
                        surface?.let {
                            productAnalytics.track(
                                ProductAnalyticsEvent.SURFACE_OPENED,
                                mapOf("surface" to it)
                            )
                        }
                        if (item.route == Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = false
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(innerPadding),
        enterTransition = {
            fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(
                            durationMillis = NAV_ANIMATION_DURATION,
                            easing = EaseInOut
                        ),
                        initialOffset = { it / 20 }
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(
                            durationMillis = NAV_ANIMATION_DURATION,
                            easing = EaseInOut
                        ),
                        targetOffset = { it / 20 }
                    )
        }
    ) {
        // ── Splash Screen ─────────────────────────────────────────────
        composable(
            route = Screen.Splash.route,
            exitTransition = {
                fadeOut(tween(200))
            }
        ) {
            SplashScreen(
                onSplashComplete = {
                    val destination = if (onboardingCompleted == true) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Onboarding Screen ─────────────────────────────────────────
        composable(
            route = Screen.Onboarding.route,
            enterTransition = {
                fadeIn(tween(500))
            },
            exitTransition = {
                fadeOut(tween(300))
            }
        ) {
            OnboardingScreen(
                onComplete = {
                    productAnalytics.track(ProductAnalyticsEvent.ONBOARDING_COMPLETED)
                    scope.launch {
                        app.settingsDataStore.setOnboardingCompleted()
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSetUpProfile = {
                    productAnalytics.track(ProductAnalyticsEvent.ONBOARDING_COMPLETED)
                    scope.launch {
                        app.settingsDataStore.setOnboardingCompleted()
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Bottom Navigation Destinations ───────────────────────────
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToBmi = { navController.navigate(Screen.BmiCalculator.route) { launchSingleTop = true } },
                onNavigateToBmr = { navController.navigate(Screen.BmrCalculator.route) { launchSingleTop = true } },
                onNavigateToBp = { navController.navigate(Screen.BloodPressureCalculator.route) { launchSingleTop = true } },
                onNavigateToWhr = { navController.navigate(Screen.WaistToHipCalculator.route) { launchSingleTop = true } },
                onNavigateToWater = { navController.navigate(Screen.WaterTracker.route) { launchSingleTop = true } },
                onNavigateToMetabolic = { navController.navigate(Screen.MetabolicSyndromeCalculator.route) { launchSingleTop = true } },
                onNavigateToBsa = { navController.navigate(Screen.BsaCalculator.route) { launchSingleTop = true } },
                onNavigateToIbw = { navController.navigate(Screen.IdealWeightCalculator.route) { launchSingleTop = true } },
                onNavigateToCalorie = { navController.navigate(Screen.DailyCalorieCalculator.route) { launchSingleTop = true } },
                onNavigateToHeartRate = { navController.navigate(Screen.HeartRateZoneCalculator.route) { launchSingleTop = true } },
                onNavigateToHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                onNavigateToAiCoach = { navController.navigate(Screen.AiCoach.route) { launchSingleTop = true } },
                onNavigateToWeight = { navController.navigate(Screen.WeightTracking.route) { launchSingleTop = true } },
                onNavigateToHealthConnections = { navController.navigate(Screen.HealthConnections.route) { launchSingleTop = true } },
                onNavigateToCalculators = { navController.navigate(Screen.Calculators.route) { launchSingleTop = true } },
                onNavigateToTrack = { navController.navigate(Screen.Track.route) { launchSingleTop = true } }
            )
        }

        composable(route = Screen.Track.route) {
            TrackHubScreen(
                onOpenWeight = { navController.navigate(Screen.WeightTracking.route) { launchSingleTop = true } },
                onOpenWater = { navController.navigate(Screen.WaterTracker.route) { launchSingleTop = true } },
                onOpenBloodPressure = { navController.navigate(Screen.BloodPressureLog.route) { launchSingleTop = true } },
                onOpenFood = { navController.navigate(Screen.FoodLog.route) { launchSingleTop = true } },
                onOpenHealthConnections = { navController.navigate(Screen.HealthConnections.route) { launchSingleTop = true } },
                onOpenHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                onOpenReminders = { navController.navigate(Screen.Reminders.route) { launchSingleTop = true } }
            )
        }

        composable(route = Screen.Calculators.route) {
            CalculatorsHubScreen(
                onOpen = { destination ->
                    val route = when (destination) {
                        CalculatorDestination.BMI -> Screen.BmiCalculator.route
                        CalculatorDestination.BMR -> Screen.BmrCalculator.route
                        CalculatorDestination.BLOOD_PRESSURE -> Screen.BloodPressureCalculator.route
                        CalculatorDestination.WATER -> Screen.WaterIntakeCalculator.route
                        CalculatorDestination.CALORIES -> Screen.DailyCalorieCalculator.route
                        CalculatorDestination.WAIST_HIP -> Screen.WaistToHipCalculator.route
                        CalculatorDestination.HEART_RATE -> Screen.HeartRateZoneCalculator.route
                        CalculatorDestination.IDEAL_WEIGHT -> Screen.IdealWeightCalculator.route
                        CalculatorDestination.BSA -> Screen.BsaCalculator.route
                        CalculatorDestination.METABOLIC -> Screen.MetabolicSyndromeCalculator.route
                    }
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }

        composable(route = Screen.Insights.route) {
            val insightsHomeViewModel: HomeViewModel = hiltViewModel()
            val insightsHomeState by insightsHomeViewModel.uiState.collectAsStateWithLifecycle()
            InsightsHubScreen(
                onOpenWeeklyReport = { navController.navigate(Screen.WeeklyReport.route) { launchSingleTop = true } },
                onOpenTrends = { navController.navigate(Screen.WeightTracking.route) { launchSingleTop = true } },
                onOpenAssistant = { navController.navigate(Screen.AiCoach.route) { launchSingleTop = true } },
                onOpenAchievements = { navController.navigate(Screen.Achievements.route) { launchSingleTop = true } },
                onOpenArticles = { navController.navigate(Screen.HealthArticles.route) { launchSingleTop = true } },
                onOpenHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                insights = insightsHomeState.deterministicInsights,
                onOpenInsight = { route ->
                    when (route) {
                        "weight_tracking" -> navController.navigate(Screen.WeightTracking.route) { launchSingleTop = true }
                        "water_tracker" -> navController.navigate(Screen.WaterTracker.route) { launchSingleTop = true }
                        "blood_pressure_checker" -> navController.navigate(Screen.BloodPressureLog.route) { launchSingleTop = true }
                        "health_connections" -> navController.navigate(Screen.HealthConnections.route) { launchSingleTop = true }
                        "track" -> navController.navigate(Screen.Track.route) { launchSingleTop = true }
                        else -> navController.navigate(Screen.History.route) { launchSingleTop = true }
                    }
                }
            )
        }

        composable(route = Screen.AiCoach.route) {
            AiCoachScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Profile.route) {
            val multiProfileViewModel: MultiProfileViewModel = hiltViewModel()
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val milestonesViewModel: MilestonesViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MilestonesViewModel(
                            app.milestonesRepository,
                            app.healthOverviewRepository
                        ) as T
                    }
                }
            )

            ProfileScreen(
                viewModel = profileViewModel,
                multiProfileViewModel = multiProfileViewModel,
                milestonesViewModel = milestonesViewModel,
                onNavigateToMetric = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onViewWeightTrends = {
                    navController.navigate(Screen.WeightTracking.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToConnections = {
                    navController.navigate(Screen.HealthConnections.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToMilestones = {
                    navController.navigate(Screen.Achievements.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToReminders = {
                    navController.navigate(Screen.Reminders.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Achievements.route) {
            val milestonesViewModel: MilestonesViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MilestonesViewModel(
                            app.milestonesRepository,
                            app.healthOverviewRepository
                        ) as T
                    }
                }
            )

            MilestonesScreen(
                viewModel = milestonesViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Reminders.route) {
            val remindersViewModel: RemindersViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RemindersViewModel(
                            reminderRepository = app.reminderRepository,
                            reminderScheduler = ReminderScheduler(context),
                            context = context
                        ) as T
                    }
                }
            )

            RemindersScreen(
                viewModel = remindersViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "welcome_back") {
            val welcomeBackViewModel: WelcomeBackViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return WelcomeBackViewModel(
                            inactivityRepository = InactivityRepository(context),
                            profileRepository = app.profileRepository,
                            historyRepository = app.historyRepository,
                            waterTrackingRepository = app.waterIntakeRepository,
                            healthOverviewRepository = app.healthOverviewRepository
                        ) as T
                    }
                }
            )

            WelcomeBackScreen(
                viewModel = welcomeBackViewModel,
                onNavigateToCalculator = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.WeeklyReport.route) {
            val weeklyReportViewModel: com.health.calculator.bmi.tracker.ui.screens.reports.WeeklyReportViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return com.health.calculator.bmi.tracker.ui.screens.reports.WeeklyReportViewModel(
                            com.health.calculator.bmi.tracker.domain.usecases.WeeklyReportGenerator(
                                app.weeklyReportDao,
                                app.historyRepository,
                                app.weightRepository,
                                app.waterIntakeRepository,
                                app.foodLogRepository,
                                app.milestonesRepository,
                                app.profileRepository
                            ),
                            app.weeklyReportDao,
                            com.health.calculator.bmi.tracker.notifications.WeeklyReportScheduler(context),
                            productAnalytics
                        ) as T
                    }
                }
            )

            com.health.calculator.bmi.tracker.ui.screens.reports.WeeklyReportScreen(
                viewModel = weeklyReportViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.HealthConnections.route) {
            val multiProfileViewModel: MultiProfileViewModel = hiltViewModel()
            val state by multiProfileViewModel.uiState.collectAsStateWithLifecycle()
            
            HealthConnectionsScreen(
                state = state,
                onBackClick = { navController.popBackStack() },
                onNavigateToCalculator = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.WeightTracking.route) {
            val weightViewModel: WeightTrackingViewModel = hiltViewModel()
            WeightTrackingScreen(
                viewModel = weightViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToDataManagement = {
                    navController.navigate(Screen.DataManagement.route)
                }
            )
        }

        composable(route = Screen.DataManagement.route) {
            DataManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResetToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Calculator Destinations (placeholders for now) ──────────
        // ── BMI Calculator (Full Implementation) ──────────────────────
        composable(route = Screen.BmiCalculator.route) {
            val bmiViewModel: BmiViewModel = hiltViewModel()
            BmiCalculatorScreen(
                viewModel = bmiViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(route = Screen.BmrCalculator.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileState = profileViewModel.uiState.collectAsStateWithLifecycle().value

            BMRCalculatorScreen(
                onNavigateBack = { navController.popBackStack() },
                profileWeightKg = profileState.profile.weightKg?.toFloat() ?: 0f,
                profileHeightCm = profileState.profile.heightCm?.toFloat() ?: 0f,
                profileAge = profileState.profile.age ?: 0,
                profileIsMale = profileState.profile.gender.name == Gender.MALE.name,
                profileUnitKg = profileState.weightUnit == com.health.calculator.bmi.tracker.presentation.profile.WeightUnit.KG,
                profileUnitCm = profileState.heightUnit == com.health.calculator.bmi.tracker.presentation.profile.HeightUnit.CM,
                profileActivityLevel = profileState.profile.activityLevel.name
            )
        }
        composable(
            route = Screen.BloodPressureCalculator.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            val bpViewModel: BloodPressureViewModel = hiltViewModel()
            BloodPressureScreen(
                viewModel = bpViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogs = {
                    navController.navigate(Screen.BloodPressureLog.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToTrends = {
                    navController.navigate(Screen.BloodPressureTrends.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToReminders = {
                    navController.navigate(Screen.BloodPressureReminders.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToExport = {
                    navController.navigate(Screen.BloodPressureExport.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEducation = {
                    navController.navigate(Screen.BloodPressureEducation.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.BloodPressureEducation.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            BpEducationalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CalculationDetail.route + "?waist={waist}&hip={hip}&gender={gender}&age={age}",
            arguments = listOf(
                navArgument("calculationId") { type = NavType.StringType },
                navArgument("waist") { 
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("hip") { 
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("gender") { 
                    type = NavType.StringType
                    defaultValue = "MALE"
                },
                navArgument("age") { 
                    type = NavType.IntType
                    defaultValue = 25
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(NAV_ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(NAV_ANIMATION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(NAV_ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(NAV_ANIMATION_DURATION))
            }
        ) { backStackEntry ->
            val calculationId = backStackEntry.arguments?.getString("calculationId") ?: return@composable
            
            if (calculationId == "whr_result") {
                val waist = backStackEntry.arguments?.getFloat("waist") ?: 0f
                val hip = backStackEntry.arguments?.getFloat("hip") ?: 0f
                val genderStr = backStackEntry.arguments?.getString("gender") ?: "MALE"
                val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                val age = backStackEntry.arguments?.getInt("age") ?: 25
                val profileStore = remember { com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore(context) }
                val whrRepository = remember { com.health.calculator.bmi.tracker.data.repository.WhrRepository(context) }
                val viewModel: com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultViewModel(profileStore, app) as T
                        }
                    }
                )

                androidx.compose.runtime.LaunchedEffect(waist, hip, genderStr, age) {
                    viewModel.calculateResult(waist, hip, gender, age)
                }
                
                val result by viewModel.result.collectAsStateWithLifecycle()
                
                if (result != null) {
                    com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultScreen(
                        result = result!!,
                        onNavigateBack = { navController.popBackStack() },
                        onRecalculate = { navController.popBackStack() },
                        onNavigateToEducation = { navController.navigate(Screen.WhrEducation.route) },
                        onSaveToHistory = {
                            whrRepository.addEntry(
                                com.health.calculator.bmi.tracker.data.model.WhrHistoryEntry(
                                    waistCm = result!!.waistCm,
                                    hipCm = result!!.hipCm,
                                    whr = result!!.whr,
                                    whtr = result!!.whtr,
                                    gender = result!!.gender,
                                    age = result!!.age,
                                    category = result!!.whrCategory,
                                    waistRiskLevel = result!!.waistRiskLevel,
                                    bodyShape = result!!.bodyShape
                                )
                            )
                        },
                        onViewProgress = { navController.navigate("whr_progress") },
                        showHeightInput = result!!.heightCm == null || result!!.heightCm == 0f,
                        onHeightSubmitted = { height -> viewModel.recalculateWithHeight(height) }
                    )
                } else {
                    PlaceholderScreen("Loading WHR Result...") { navController.popBackStack() }
                }
            } else {
                PlaceholderScreen("Result for ID: \$calculationId") { navController.popBackStack() }
            }
        }
        composable(
            route = "whr_progress",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeIn(tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeOut(tween(350)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeIn(tween(350)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeOut(tween(350)) }
        ) {
            val repository = remember { com.health.calculator.bmi.tracker.data.repository.WhrRepository(context) }
            val viewModel: com.health.calculator.bmi.tracker.ui.screens.whr.WhrProgressViewModel = hiltViewModel()

            com.health.calculator.bmi.tracker.ui.screens.whr.WhrProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.WaistToHipCalculator.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            com.health.calculator.bmi.tracker.ui.screens.whr.WhrInputScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEducation = { navController.navigate(Screen.WhrEducation.route) },
                onCalculate = { waistCm, hipCm, gender, age ->
                    val route = Screen.CalculationDetail.createWhrResultRoute(
                        waistCm = waistCm,
                        hipCm = hipCm,
                        gender = gender.name,
                        age = age
                    )
                    navController.navigate(route)
                }
            )
        }
        composable(
            route = Screen.WhrEducation.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            com.health.calculator.bmi.tracker.ui.screens.whr.WhrEducationalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.WaterIntakeCalculator.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            val application = context.applicationContext as android.app.Application
            val repository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val waterIntakeViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModel = hiltViewModel()

            // Auto-populate from profile if available
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileState = profileViewModel.uiState.collectAsStateWithLifecycle().value
            
            androidx.compose.runtime.LaunchedEffect(profileState) {
                // Determine if metric based on weight unit
                val isMetric = profileState.weightUnit == com.health.calculator.bmi.tracker.presentation.profile.WeightUnit.KG
                waterIntakeViewModel.loadFromProfile(
                    profileWeight = profileState.profile.weightKg?.toFloat()?.takeIf { it > 0f },
                    profileAge = profileState.profile.age?.takeIf { it > 0 },
                    profileGender = profileState.profile.gender.name,
                    profileActivityLevel = profileState.profile.activityLevel.name,
                    profileIsMetric = isMetric
                )
            }

            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeInputScreen(
                viewModel = waterIntakeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate("water_intake_result") },
                onNavigateToEducation = { navController.navigate(WATER_EDUCATION_ROUTE) }
            )
        }
        composable(
            route = "water_intake_result",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + fadeIn(tween(400))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + fadeOut(tween(400))
            }
        ) {
            val application = context.applicationContext as android.app.Application
            val repository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            // Use the previous back stack entry to share the ViewModel
            val previousEntry = remember { navController.previousBackStackEntry }
            val waterIntakeViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModel = hiltViewModel()

            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeResultScreen(
                viewModel = waterIntakeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRecalculate = {
                    waterIntakeViewModel.resetResult()
                    navController.popBackStack()
                },
                onStartTracking = {
                    navController.navigate(Screen.WaterTracker.route)
                }
            )
        }
        composable(route = Screen.MetabolicSyndromeCalculator.route) {
            val viewModel: MetabolicSyndromeViewModel = hiltViewModel()
            MetabolicSyndromeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.BsaCalculator.route) {
            BSAScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "ideal_body_weight",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + fadeIn(tween(400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + fadeOut(tween(400)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + fadeIn(tween(400)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + fadeOut(tween(400)) }
        ) {
            IBWScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBMI = { navController.navigate(Screen.BmiCalculator.route) },
                onNavigateToBMR = { navController.navigate(Screen.BmrCalculator.route) },
                onNavigateToWHR = { navController.navigate(Screen.WaistToHipCalculator.route) }
            )
        }
        composable(route = Screen.DailyCalorieCalculator.route) {
            CalorieScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.HeartRateZoneCalculator.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(350))
            }
        ) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

            val bpViewModel: BloodPressureViewModel = hiltViewModel()
            val lastPulseReading by bpViewModel.lastPulseReading.collectAsStateWithLifecycle()

            val historyViewModel: HistoryViewModel = hiltViewModel()

            var showResultScreen by rememberSaveable { mutableStateOf(false) }
            var calculationResult by remember { mutableStateOf<HeartRateZoneResult?>(null) }

            if (showResultScreen && calculationResult != null) {
                HeartRateZoneResultScreen(
                    result = calculationResult!!,
                    weightKg = profileState.profile.weightKg?.toFloat()?.takeIf { it > 0f } ?: 70f,
                    onNavigateBack = { showResultScreen = false },
                    onSaveToHistory = { result ->
                        historyViewModel.saveHistoryEntry(result.toHistoryEntry())
                    },
                    onRecalculate = { showResultScreen = false }
                )
            } else {
                HeartRateZoneScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBPChecker = {
                        navController.navigate(Screen.BloodPressureCalculator.route) {
                            launchSingleTop = true
                        }
                    },
                    profileAge = profileState.profile.age,
                    profileGender = if (profileState.profile.gender.name != Gender.NOT_SET.name) {
                        profileState.profile.gender.name.lowercase().replaceFirstChar { it.uppercase() }
                    } else null,
                    profileWeightKg = profileState.profile.weightKg?.toFloat()?.takeIf { it > 0f },
                    lastRestingHR = lastPulseReading,
                    onCalculate = { result ->
                        calculationResult = result
                        showResultScreen = true
                    }
                )
            }
        }
        composable(
            route = Screen.FoodLog.route + "?calories={calories}&protein={protein}&carbs={carbs}&fat={fat}",
            arguments = listOf(
                navArgument("calories") { type = NavType.FloatType; defaultValue = 2000f },
                navArgument("protein") { type = NavType.FloatType; defaultValue = 0f },
                navArgument("carbs") { type = NavType.FloatType; defaultValue = 0f },
                navArgument("fat") { type = NavType.FloatType; defaultValue = 0f }
            )
        ) { backStackEntry ->
            val calories = backStackEntry.arguments?.getFloat("calories")?.toDouble() ?: 2000.0
            val protein = backStackEntry.arguments?.getFloat("protein")?.toDouble() ?: 0.0
            val carbs = backStackEntry.arguments?.getFloat("carbs")?.toDouble() ?: 0.0
            val fat = backStackEntry.arguments?.getFloat("fat")?.toDouble() ?: 0.0

            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() },
                targetCalories = calories,
                targetProtein = protein,
                targetCarbs = carbs,
                targetFat = fat
            )
        }
        composable(route = Screen.CalorieHistory.route) {
            val foodLogViewModel: FoodLogViewModel = hiltViewModel()
            val calorieViewModel: CalorieViewModel = hiltViewModel()
            val calUiState by calorieViewModel.uiState.collectAsStateWithLifecycle()
            val foodLogUiState by foodLogViewModel.uiState.collectAsStateWithLifecycle()
            
            // Use current targets if available from calculator state, otherwise defaults
            val targetCal = calUiState.result?.safeGoalCalories ?: 2000.0
            val targetP = calUiState.macroResult?.proteinGrams ?: 0.0
            val targetC = calUiState.macroResult?.carbGrams ?: 0.0
            val targetF = calUiState.macroResult?.fatGrams ?: 0.0

            val stats = foodLogViewModel.getStats(targetCal, targetP, targetC, targetF)
            val weeklySummaries = foodLogViewModel.getWeeklySummaries()
            val logs = foodLogViewModel.getHistoricalLogs() + listOfNotNull(foodLogUiState.todayLog)

            com.health.calculator.bmi.tracker.ui.screens.calorie.CalorieHistoryScreen(
                logs = logs,
                stats = stats,
                weeklySummaries = weeklySummaries,
                onNavigateBack = { navController.popBackStack() },
                onDayTapped = { _ ->
                    // Optional: navigate to specific day food log if we implement historical viewing
                }
            )
        }

        // ── Feature Destinations (placeholders) ─────────────────────
        composable(
            route = Screen.WaterTracker.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            val application = context.applicationContext as android.app.Application
            val waterIntakeRepository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val waterTrackingViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterTrackingViewModel = hiltViewModel()

            val gamificationRepository = remember {
                com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterGamificationDao()
                )
            }
            val gamificationViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModel = hiltViewModel()

            // Check for yesterday's data on screen load
            androidx.compose.runtime.LaunchedEffect(Unit) {
                waterTrackingViewModel.checkAndSaveYesterdayData()
            }

            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterTrackingScreen(
                viewModel = waterTrackingViewModel,
                gamificationViewModel = gamificationViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReminders = { navController.navigate(WATER_REMINDER_SETTINGS_ROUTE) },
                onNavigateToHistory = { navController.navigate(WATER_HISTORY_ROUTE) },
                onNavigateToGamification = { navController.navigate(WATER_GAMIFICATION_ROUTE) },
                onNavigateToTools = { navController.navigate(WATER_TOOLS_ROUTE) },
                onNavigateToEducation = { navController.navigate(WATER_EDUCATION_ROUTE) }
            )
        }
        composable(
            route = WATER_REMINDER_SETTINGS_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterReminderSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = WATER_HISTORY_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            val waterHistoryViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterHistoryViewModel = hiltViewModel()

            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterHistoryScreen(
                viewModel = waterHistoryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = WATER_GAMIFICATION_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            val gamificationViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModel = hiltViewModel()

            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationScreen(
                viewModel = gamificationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = WATER_TOOLS_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            val toolsViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.HydrationToolsViewModel = hiltViewModel()

            com.health.calculator.bmi.tracker.ui.screens.waterintake.HydrationToolsScreen(
                viewModel = toolsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToElectrolytes = { navController.navigate(ELECTROLYTE_INFO_ROUTE) }
            )
        }
        composable(
            route = WATER_EDUCATION_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterEducationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = ELECTROLYTE_INFO_ROUTE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
            }
        ) {
            com.health.calculator.bmi.tracker.ui.screens.waterintake.ElectrolyteInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BloodPressureLog.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            BpLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BloodPressureTrends.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            BpTrendScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BloodPressureReminders.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            BpReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.BloodPressureExport.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            BpExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.HealthArticles.route) {
            HealthArticlesScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenBloodPressureEducation = { navController.navigate(Screen.BloodPressureEducation.route) },
                onOpenWaterEducation = { navController.navigate(WATER_EDUCATION_ROUTE) }
            )
        }
        composable(route = Screen.ExportData.route) {
            ExportDataScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenHistoryExport = { navController.navigate(Screen.History.route) },
                onOpenDataManagement = { navController.navigate(Screen.DataManagement.route) }
            )
        }
    }
    }
}
