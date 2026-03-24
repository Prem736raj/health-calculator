package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    multiProfileViewModel: com.health.calculator.bmi.tracker.presentation.profile.MultiProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToConnections: () -> Unit,
    onNavigateToMetric: (String) -> Unit,
    onViewWeightTrends: () -> Unit,
    onNavigateToMilestones: () -> Unit,
    onNavigateToReminders: () -> Unit,
    milestonesViewModel: com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val multiProfileState by multiProfileViewModel.uiState.collectAsState()
    val milestonesState by milestonesViewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToConnections) {
                        Icon(Icons.Default.Share, contentDescription = "Health Connections")
                    }
                    IconButton(onClick = onNavigateToReminders) {
                        Icon(Icons.Default.Notifications, contentDescription = "Reminders")
                    }
                    IconButton(onClick = multiProfileViewModel::showShareDialog) {
                        Icon(Icons.Default.Send, contentDescription = "Share Profile")
                    }
                    if (uiState.selectedTab == ProfileTab.MY_INFO) {
                        TextButton(
                            onClick = { viewModel.saveProfile() },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile Switcher
            ProfileSwitcherBar(
                profiles = multiProfileState.profiles,
                activeProfileId = multiProfileState.activeProfile?.profileId,
                onProfileClick = multiProfileViewModel::switchProfile,
                onAddClick = multiProfileViewModel::showAddProfileDialog
            )

            // Profile Completion Progress
            // Prompt says: "A progress bar at the top of the profile screen indicating completion"
            ProfileCompletionBar(
                completion = uiState.completion,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Milestones Preview
            ProfileMilestonesPreview(
                journeySummary = milestonesState.journeySummary,
                recentRecords = milestonesState.personalRecords,
                recentMilestones = milestonesState.earnedMilestones,
                onViewAll = onNavigateToMilestones,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                ProfileTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        ProfileTab.MY_INFO -> {
                            MyInfoSection(
                                profile = uiState.profile,
                                onNameChange = viewModel::updateName,
                                onProfilePictureClick = viewModel::showImagePickerDialog,
                                onDateOfBirthClick = viewModel::showDatePicker,
                                onGenderClick = viewModel::showGenderPicker,
                                onHeightClick = { /* Scroll to height or show dialog if we had one */ },
                                onWeightClick = { /* Scroll to weight */ },
                                onGoalWeightClick = { /* Scroll to goal weight */ },
                                onActivityLevelClick = viewModel::showActivityLevelPicker,
                                onHealthGoalsClick = viewModel::showHealthGoalsPicker,
                                onFrameSizeClick = viewModel::showFrameSizePicker,
                                onEthnicityClick = viewModel::showEthnicityPicker
                            )
                        }
                        ProfileTab.HEALTH_OVERVIEW -> {
                            HealthOverviewSection(
                                overview = uiState.healthOverview,
                                weightStatistics = viewModel.weightStatistics.collectAsState().value,
                                latestWeight = uiState.profile.weightKg?.toDouble() ?: 0.0,
                                useMetric = uiState.profile.useMetricSystem,
                                onLogWeight = { viewModel.showWeightLogDialog() },
                                onViewTrends = onViewWeightTrends,
                                onMetricClick = { route, _ -> onNavigateToMetric(route) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Pickers
    if (uiState.showImagePickerDialog) {
        ProfileImagePickerDialog(
            onDismiss = viewModel::dismissImagePickerDialog,
            onCameraClick = { /* TODO: Launch Camera */ viewModel.dismissImagePickerDialog() },
            onGalleryClick = { /* TODO: Launch Gallery */ viewModel.dismissImagePickerDialog() },
            onImageSelected = viewModel::updateProfilePicture
        )
    }

    if (uiState.showGenderPicker) {
        GenericSelectionDialog(
            title = "Select Gender",
            onDismiss = viewModel::dismissGenderPicker
        ) {
            GenderSelectionContent(
                selectedGender = uiState.profile.gender,
                onGenderSelected = viewModel::updateGender
            )
        }
    }

    if (uiState.showActivityLevelPicker) {
        GenericSelectionDialog(
            title = "Physical Activity",
            onDismiss = viewModel::dismissActivityLevelPicker
        ) {
            ActivityLevelSelectionContent(
                selectedLevel = uiState.profile.activityLevel,
                onLevelSelected = viewModel::updateActivityLevel
            )
        }
    }

    if (uiState.showHealthGoalsPicker) {
        GenericSelectionDialog(
            title = "Health Goals",
            onDismiss = viewModel::dismissHealthGoalsPicker
        ) {
            HealthGoalsSelectionContent(
                selectedGoals = uiState.profile.healthGoals,
                onGoalsChanged = viewModel::updateHealthGoals
            )
        }
    }

    if (uiState.showFrameSizePicker) {
        GenericSelectionDialog(
            title = "Body Frame Size",
            onDismiss = viewModel::dismissFrameSizePicker
        ) {
            FrameSizeSelectionContent(
                selectedSize = uiState.profile.frameSize,
                onSizeSelected = viewModel::updateFrameSize
            )
        }
    }

    if (uiState.showEthnicityPicker) {
        GenericSelectionDialog(
            title = "Ethnicity / Region",
            onDismiss = viewModel::dismissEthnicityPicker
        ) {
            EthnicitySelectionContent(
                selectedRegion = uiState.profile.ethnicityRegion,
                onRegionSelected = viewModel::updateEthnicity
            )
        }
    }

    if (uiState.showWeightLogDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateMillis)
        var showWeightDatePicker by remember { mutableStateOf(false) }

        if (showWeightDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showWeightDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.updateWeightLogDate(it) }
                        showWeightDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showWeightDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        LogWeightDialog(
            weightInput = uiState.weightInput,
            noteInput = uiState.noteInput,
            dateMillis = uiState.dateMillis,
            useMetric = uiState.profile.useMetricSystem,
            isSaving = uiState.isWeightSaving,
            onWeightChange = viewModel::updateWeightLogInput,
            onNoteChange = viewModel::updateWeightLogNote,
            onDateClick = { showWeightDatePicker = true },
            onSave = viewModel::saveWeightLog,
            onDismiss = viewModel::dismissWeightLogDialog
        )
    }

    if (uiState.showSaveSuccess) {
        LaunchedEffect(Unit) {
            // Should show snackbar instead
            viewModel.dismissSaveSuccess()
        }
    }

    // Multi-Profile Dialogs
    if (multiProfileState.showAddProfileDialog) {
        AddProfileDialog(
            name = multiProfileState.newProfileName,
            selectedColor = multiProfileState.newProfileColor,
            onNameChange = multiProfileViewModel::updateNewProfileName,
            onColorSelect = multiProfileViewModel::updateNewProfileColor,
            onConfirm = multiProfileViewModel::createProfile,
            onDismiss = multiProfileViewModel::dismissAddProfileDialog
        )
    }

    if (multiProfileState.showShareDialog) {
        ProfileShareDialog(
            config = multiProfileState.shareConfig,
            onConfigChange = multiProfileViewModel::updateShareConfig,
            onShare = { multiProfileViewModel.shareProfile(context) },
            onDismiss = multiProfileViewModel::dismissShareDialog
        )
    }

    if (multiProfileState.showRecalculatePrompt) {
        RecalculatePromptDialog(
            calculators = multiProfileState.calculatorsToRecalculate,
            onRecalculateClick = {
                multiProfileViewModel.dismissRecalculatePrompt()
                onNavigateToConnections()
            },
            onDismiss = multiProfileViewModel::dismissRecalculatePrompt
        )
    }

    if (multiProfileState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = multiProfileViewModel::dismissDeleteConfirm,
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete ${multiProfileState.profileToDelete?.displayName}'s profile? All their data will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = multiProfileViewModel::deleteProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = multiProfileViewModel::dismissDeleteConfirm) {
                    Text("Cancel")
                }
            }
        )
    }

    // Achievement Celebrations
    if (milestonesState.showNewRecordCelebration && milestonesState.newRecordType != null) {
        NewRecordCelebrationDialog(
            recordType = milestonesState.newRecordType!!,
            newValue = milestonesState.newRecordValue,
            previousValue = milestonesState.previousRecordValue,
            onDismiss = milestonesViewModel::dismissRecordCelebration
        )
    }

    if (milestonesState.showNewMilestoneCelebration && milestonesState.newMilestones.isNotEmpty()) {
        val currentIdx = milestonesState.currentMilestoneCelebrationIndex
        if (currentIdx < milestonesState.newMilestones.size) {
            NewMilestoneCelebrationDialog(
                milestoneType = milestonesState.newMilestones[currentIdx],
                remainingCount = milestonesState.newMilestones.size - currentIdx,
                onNext = milestonesViewModel::dismissMilestoneCelebration,
                onDismissAll = milestonesViewModel::dismissAllCelebrations
            )
        }
    }
}
