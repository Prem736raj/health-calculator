package com.health.calculator.bmi.tracker.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.health.calculator.bmi.tracker.HealthCalculatorApp
import com.health.calculator.bmi.tracker.core.constants.AppConstants
import com.health.calculator.bmi.tracker.presentation.home.HomeScreen
import com.health.calculator.bmi.tracker.presentation.profile.ProfileScreen
import com.health.calculator.bmi.tracker.presentation.settings.SettingsScreen
import com.health.calculator.bmi.tracker.ui.screens.history.HistoryScreen
import com.health.calculator.bmi.tracker.ui.screens.onboarding.OnboardingScreen
import com.health.calculator.bmi.tracker.ui.screens.splash.SplashScreen
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.BmiCalculatorScreen
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.BmiViewModel
import com.health.calculator.bmi.tracker.ui.screens.bmr.BMRCalculatorScreen
import com.health.calculator.bmi.tracker.presentation.profile.ProfileViewModel
import com.health.calculator.bmi.tracker.presentation.profile.ProfileViewModelFactory
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
import com.health.calculator.bmi.tracker.ui.screens.backup.BackupScreen
import com.health.calculator.bmi.tracker.ui.screens.backup.BackupViewModel
import com.health.calculator.bmi.tracker.ui.screens.settings.DataManagementScreen
import com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesScreen
import com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesViewModel
import com.health.calculator.bmi.tracker.ui.screens.reminders.RemindersScreen
import com.health.calculator.bmi.tracker.ui.screens.reminders.RemindersViewModel
import com.health.calculator.bmi.tracker.notifications.ReminderScheduler
import com.health.calculator.bmi.tracker.ui.screens.welcomeback.WelcomeBackViewModel
import com.health.calculator.bmi.tracker.ui.screens.welcomeback.WelcomeBackScreen
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository

private const val NAV_ANIMATION_DURATION = AppConstants.ANIMATION_DURATION_MEDIUM
const val WATER_REMINDER_SETTINGS_ROUTE = "water_reminder_settings"
const val WATER_HISTORY_ROUTE = "water_history"
const val WATER_GAMIFICATION_ROUTE = "water_gamification"
const val WATER_TOOLS_ROUTE = "water_hydration_tools"
const val WATER_EDUCATION_ROUTE = "water_education"
const val ELECTROLYTE_INFO_ROUTE = "electrolyte_info"

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as HealthCalculatorApp
    val scope = rememberCoroutineScope()

    // Track whether onboarding has been completed
    val onboardingCompleted by app.onboardingCompletedFlow.collectAsState(initial = null)

    // Wait until we know the onboarding state before rendering
    if (onboardingCompleted == null) return

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
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
                    scope.launch {
                        app.settingsDataStore.setOnboardingCompleted()
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSetUpProfile = {
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
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } }
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
            val multiProfileViewModel: MultiProfileViewModel = viewModel(
                factory = MultiProfileViewModel.Factory(
                    app.familyProfileRepository,
                    app.healthOverviewRepository,
                    app.historyRepository
                )
            )
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    app.familyProfileRepository,
                    app.profileRepository,
                    app.healthOverviewRepository,
                    app.weightRepository,
                    com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase()
                )
            )

            val milestonesViewModel: MilestonesViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                onNavigateBack = {
                    navController.popBackStack()
                },
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
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                            com.health.calculator.bmi.tracker.notifications.WeeklyReportScheduler(context)
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
            val multiProfileViewModel: MultiProfileViewModel = viewModel(
                factory = MultiProfileViewModel.Factory(
                    app.familyProfileRepository,
                    app.healthOverviewRepository,
                    app.historyRepository
                )
            )
            val state by multiProfileViewModel.uiState.collectAsState()
            
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
            val weightViewModel: WeightTrackingViewModel = viewModel(
                factory = WeightTrackingViewModel.Factory(
                    app.weightRepository,
                    app.profileRepository,
                    app.weightReminderManager
                )
            )
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
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
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

        composable(route = Screen.Backup.route) {
            val backupViewModel: BackupViewModel = viewModel()
            BackupScreen(
                viewModel = backupViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Calculator Destinations (placeholders for now) ──────────
        // ── BMI Calculator (Full Implementation) ──────────────────────
        composable(route = Screen.BmiCalculator.route) {
            val bmiViewModel: BmiViewModel = viewModel(
                factory = BmiViewModel.Factory(
                    application = app,
                    milestoneEvaluationUseCase = app.milestoneEvaluationUseCase
                )
            )
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
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    familyProfileRepository = app.familyProfileRepository,
                    profileRepository = app.profileRepository,
                    healthOverviewRepository = app.healthOverviewRepository,
                    weightRepository = app.weightRepository,
                    profileCompletionUseCase = com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase()
                )
            )
            val profileState = profileViewModel.uiState.collectAsState().value

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
            val bpViewModel: BloodPressureViewModel = viewModel(
                factory = BloodPressureViewModel.Factory(
                    application = app,
                    milestoneEvaluationUseCase = app.milestoneEvaluationUseCase
                )
            )
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
                val context = LocalContext.current
                val profileStore = remember { com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore(context) }
                val whrRepository = remember { com.health.calculator.bmi.tracker.data.repository.WhrRepository(context) }
                val viewModel: com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultViewModel(profileStore, app) as T
                        }
                    }
                )
                
                androidx.compose.runtime.LaunchedEffect(waist, hip, genderStr, age) {
                    viewModel.calculateResult(waist, hip, gender, age)
                }
                
                val result by viewModel.result.collectAsState()
                
                if (result != null) {
                    com.health.calculator.bmi.tracker.ui.screens.whr.WhrResultScreen(
                        result = result!!,
                        onNavigateBack = { navController.popBackStack() },
                        onRecalculate = { navController.popBackStack() },
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
                        onViewAdvancedMetrics = {
                            navController.navigate(
                                "whr_advanced/\${result!!.waistCm}/\${result!!.hipCm}/\${result!!.gender.name}/\${result!!.age}/\${result!!.heightCm ?: -1f}"
                            )
                        },
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
            val context = LocalContext.current
            val repository = remember { com.health.calculator.bmi.tracker.data.repository.WhrRepository(context) }
            val viewModel: com.health.calculator.bmi.tracker.ui.screens.whr.WhrProgressViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.whr.WhrProgressViewModelFactory(repository)
            )

            com.health.calculator.bmi.tracker.ui.screens.whr.WhrProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "whr_advanced/{waistCm}/{hipCm}/{gender}/{age}/{heightCm}",
            arguments = listOf(
                navArgument("waistCm") { type = NavType.FloatType },
                navArgument("hipCm") { type = NavType.FloatType },
                navArgument("gender") { type = NavType.StringType },
                navArgument("age") { type = NavType.IntType },
                navArgument("heightCm") { type = NavType.FloatType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeIn(tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeOut(tween(350)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeIn(tween(350)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeOut(tween(350)) }
        ) { backStackEntry ->
            val waistCm = backStackEntry.arguments?.getFloat("waistCm") ?: 0f
            val hipCm = backStackEntry.arguments?.getFloat("hipCm") ?: 0f
            val genderStr = backStackEntry.arguments?.getString("gender") ?: "MALE"
            val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
            val age = backStackEntry.arguments?.getInt("age") ?: 25
            val heightCmArg = backStackEntry.arguments?.getFloat("heightCm") ?: -1f
            val heightCm = if (heightCmArg < 0) null else heightCmArg

            val whrResult = remember(waistCm, hipCm, gender, age, heightCm) {
                com.health.calculator.bmi.tracker.data.model.WhrCalculator.calculate(waistCm, hipCm, gender, age, heightCm)
            }

            val visceralFat = remember(waistCm, age, gender) {
                com.health.calculator.bmi.tracker.data.model.VisceralFatCalculator.estimateVisceralFat(waistCm, age, gender)
            }

            val abdominalObesity = remember(waistCm, gender) {
                com.health.calculator.bmi.tracker.data.model.VisceralFatCalculator.classifyAbdominalObesity(waistCm, gender)
            }

            val combinedRisk = remember(whrResult, visceralFat) {
                com.health.calculator.bmi.tracker.data.model.VisceralFatCalculator.buildCombinedRiskSummary(
                    whrCategory = whrResult.whrCategory,
                    waistRiskLevel = whrResult.waistRiskLevel,
                    whtrAtRisk = whrResult.whtrAtRisk,
                    visceralFat = visceralFat
                )
            }

            val tips = remember(combinedRisk, waistCm, gender, whrResult) {
                com.health.calculator.bmi.tracker.data.model.VisceralFatCalculator.generateImprovementTips(
                    overallRisk = combinedRisk.overallRisk,
                    waistCm = waistCm,
                    gender = gender,
                    whrCategory = whrResult.whrCategory
                )
            }

            com.health.calculator.bmi.tracker.ui.screens.whr.WhrAdvancedMetricsScreen(
                whrResult = whrResult,
                visceralFat = visceralFat,
                abdominalObesity = abdominalObesity,
                combinedRisk = combinedRisk,
                improvementTips = tips,
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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val repository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val waterIntakeViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModelFactory(application, repository)
            )

            // Auto-populate from profile if available
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    familyProfileRepository = app.familyProfileRepository,
                    profileRepository = app.profileRepository,
                    healthOverviewRepository = app.healthOverviewRepository,
                    weightRepository = app.weightRepository,
                    profileCompletionUseCase = com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase()
                )
            )
            val profileState = profileViewModel.uiState.collectAsState().value
            
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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val repository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            // Use the previous back stack entry to share the ViewModel
            val previousEntry = remember { navController.previousBackStackEntry }
            val waterIntakeViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModel = if (previousEntry != null) {
                viewModel(
                    viewModelStoreOwner = previousEntry,
                    factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModelFactory(application, repository)
                )
            } else {
                viewModel(
                    factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterIntakeViewModelFactory(application, repository)
                )
            }

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
            val viewModel: MetabolicSyndromeViewModel = viewModel()
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
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    familyProfileRepository = app.familyProfileRepository,
                    profileRepository = app.profileRepository,
                    healthOverviewRepository = app.healthOverviewRepository,
                    weightRepository = app.weightRepository,
                    profileCompletionUseCase = com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase()
                )
            )
            val profileState by profileViewModel.uiState.collectAsState()
            
            val bpViewModel: BloodPressureViewModel = viewModel(
                factory = BloodPressureViewModel.Factory(
                    application = app,
                    milestoneEvaluationUseCase = app.milestoneEvaluationUseCase
                )
            )
            val lastPulseReading by bpViewModel.lastPulseReading.collectAsState()

            val historyViewModel: HistoryViewModel = viewModel()

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
            val foodLogViewModel: FoodLogViewModel = viewModel()
            val calorieViewModel: CalorieViewModel = viewModel()
            val calUiState by calorieViewModel.uiState.collectAsState()
            
            // Use current targets if available from calculator state, otherwise defaults
            val targetCal = calUiState.result?.safeGoalCalories ?: 2000.0
            val targetP = calUiState.macroResult?.proteinGrams ?: 0.0
            val targetC = calUiState.macroResult?.carbGrams ?: 0.0
            val targetF = calUiState.macroResult?.fatGrams ?: 0.0

            val stats = foodLogViewModel.getStats(targetCal, targetP, targetC, targetF)
            val weeklySummaries = foodLogViewModel.getWeeklySummaries()
            val logs = foodLogViewModel.getHistoricalLogs() + listOfNotNull(foodLogViewModel.uiState.value.todayLog)

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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val waterIntakeRepository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val waterTrackingViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterTrackingViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterTrackingViewModelFactory(
                    application = application,
                    repository = waterIntakeRepository
                )
            )

            val gamificationRepository = remember {
                com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterGamificationDao()
                )
            }
            val gamificationViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModelFactory(
                    application = application,
                    gamificationRepo = gamificationRepository,
                    waterRepo = waterIntakeRepository
                )
            )

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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val waterIntakeRepository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val waterHistoryViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterHistoryViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterHistoryViewModelFactory(
                    application = application,
                    repository = waterIntakeRepository
                )
            )

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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val waterIntakeRepository = remember { 
                com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterIntakeDao()
                ) 
            }
            val gamificationRepository = remember {
                com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository(
                    com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).waterGamificationDao()
                )
            }
            val gamificationViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.WaterGamificationViewModelFactory(
                    application = application,
                    gamificationRepo = gamificationRepository,
                    waterRepo = waterIntakeRepository
                )
            )

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
            val context = LocalContext.current
            val application = context.applicationContext as android.app.Application
            val urineColorDao = remember { 
                com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(context).urineColorDao() 
            }
            val toolsViewModel: com.health.calculator.bmi.tracker.ui.screens.waterintake.HydrationToolsViewModel = viewModel(
                factory = com.health.calculator.bmi.tracker.ui.screens.waterintake.HydrationToolsViewModelFactory(
                    application = application,
                    urineColorDao = urineColorDao
                )
            )

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
            PlaceholderScreen("Health Articles") { navController.popBackStack() }
        }
        composable(route = Screen.ExportData.route) {
            PlaceholderScreen("Export Data") { navController.popBackStack() }
        }
    }
}

/**
 * Temporary placeholder screen for destinations not yet implemented.
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    onGoBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🚧",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onGoBack) {
                Text("← Go Back")
            }
        }
    }
}
