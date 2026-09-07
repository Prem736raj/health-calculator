package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R


import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.health.calculator.bmi.tracker.ui.components.*
import java.io.File
import java.io.FileOutputStream

private const val PROFILE_IMAGE_QUALITY = 95
private const val PROFILE_SCREEN_TAG = "ProfileScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    multiProfileViewModel: com.health.calculator.bmi.tracker.presentation.profile.MultiProfileViewModel,
    onNavigateToConnections: () -> Unit,
    onNavigateToMetric: (String) -> Unit,
    onViewWeightTrends: () -> Unit,
    onNavigateToMilestones: () -> Unit,
    onNavigateToReminders: () -> Unit,
    milestonesViewModel: com.health.calculator.bmi.tracker.ui.screens.profile.milestones.MilestonesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val multiProfileState by multiProfileViewModel.uiState.collectAsStateWithLifecycle()
    val milestonesState by milestonesViewModel.uiState.collectAsStateWithLifecycle()
    val weightStatistics by viewModel.weightStatistics.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showActionsMenu by remember { mutableStateOf(false) }
    var editingField by rememberSaveable { mutableStateOf<String?>(null) }
    var editingValue by rememberSaveable { mutableStateOf("") }
    var editingError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun beginEditing(field: String) {
        editingField = field
        editingError = null
        editingValue = when (field) {
            PROFILE_FIELD_NAME -> uiState.profile.name
            PROFILE_FIELD_HEIGHT -> uiState.profile.heightCm?.let {
                if (uiState.profile.useMetricSystem) "%.1f".format(it)
                else "%.1f".format(it / 2.54f)
            }.orEmpty()
            PROFILE_FIELD_WEIGHT, PROFILE_FIELD_GOAL_WEIGHT -> {
                val value = if (field == PROFILE_FIELD_WEIGHT) uiState.profile.weightKg else uiState.profile.goalWeightKg
                value?.let { if (uiState.profile.useMetricSystem) "%.1f".format(it) else "%.1f".format(it * 2.20462f) }.orEmpty()
            }
            else -> ""
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri != null) {
            viewModel.updateProfilePicture(selectedUri)
        } else {
            viewModel.dismissImagePickerDialog()
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val imageUri = bitmap?.let { saveBitmapToPrivateStorage(context, it) }
        if (imageUri != null) {
            viewModel.updateProfilePicture(imageUri)
        } else {
            viewModel.dismissImagePickerDialog()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.txt_profile), fontWeight = FontWeight.Bold) },
                actions = {
                    // Profile is a primary bottom-navigation destination, so a
                    // back arrow is not useful here. Keep the app bar calm and
                    // put secondary profile actions behind one predictable menu.
                    Box {
                        IconButton(onClick = { showActionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More profile actions")
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.txt_health_connections)) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showActionsMenu = false
                                    onNavigateToConnections()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.txt_reminders)) },
                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                onClick = {
                                    showActionsMenu = false
                                    onNavigateToReminders()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.txt_share_profile)) },
                                leadingIcon = { Icon(Icons.Default.Send, contentDescription = null) },
                                onClick = {
                                    showActionsMenu = false
                                    multiProfileViewModel.showShareDialog()
                                }
                            )
                        }
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
                                Text(stringResource(R.string.txt_save), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        )
                    )
                )
        ) {
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                compact = true
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
                                useMetricSystem = uiState.profile.useMetricSystem,
                                onNameChange = viewModel::updateName,
                                onNameClick = { beginEditing(PROFILE_FIELD_NAME) },
                                onProfilePictureClick = viewModel::showImagePickerDialog,
                                onDateOfBirthClick = viewModel::showDatePicker,
                                onGenderClick = viewModel::showGenderPicker,
                                onHeightClick = { beginEditing(PROFILE_FIELD_HEIGHT) },
                                onWeightClick = { beginEditing(PROFILE_FIELD_WEIGHT) },
                                onGoalWeightClick = { beginEditing(PROFILE_FIELD_GOAL_WEIGHT) },
                                onActivityLevelClick = viewModel::showActivityLevelPicker,
                                onHealthGoalsClick = viewModel::showHealthGoalsPicker,
                                onFrameSizeClick = viewModel::showFrameSizePicker,
                                onEthnicityClick = viewModel::showEthnicityPicker
                            )
                        }
                        ProfileTab.HEALTH_OVERVIEW -> {
                            HealthOverviewSection(
                                overview = uiState.healthOverview,
                                weightStatistics = weightStatistics,
                                latestWeight = weightStatistics?.currentWeight
                                    ?: uiState.profile.weightKg?.toDouble()
                                    ?: 0.0,
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
    }

    // Dialogs & Pickers
    if (uiState.showImagePickerDialog) {
        ProfileImagePickerDialog(
            onDismiss = viewModel::dismissImagePickerDialog,
            onCameraClick = {
                cameraLauncher.launch(null)
            },
            onGalleryClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
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
                    }) { Text(stringResource(R.string.txt_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showWeightDatePicker = false }) { Text(stringResource(R.string.txt_cancel)) }
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

    editingField?.let { field ->
        val isName = field == PROFILE_FIELD_NAME
        val unitLabel = when (field) {
            PROFILE_FIELD_HEIGHT -> if (uiState.profile.useMetricSystem) "cm" else "in"
            PROFILE_FIELD_WEIGHT, PROFILE_FIELD_GOAL_WEIGHT -> if (uiState.profile.useMetricSystem) "kg" else "lb"
            else -> null
        }
        AlertDialog(
            onDismissRequest = { editingField = null; editingError = null },
            title = {
                Text(
                    when (field) {
                        PROFILE_FIELD_NAME -> "Edit name"
                        PROFILE_FIELD_HEIGHT -> "Edit height"
                        PROFILE_FIELD_WEIGHT -> "Edit current weight"
                        PROFILE_FIELD_GOAL_WEIGHT -> "Edit goal weight"
                        else -> "Edit profile"
                    }
                )
            },
            text = {
                OutlinedTextField(
                    value = editingValue,
                    onValueChange = {
                        editingValue = if (isName) it.take(80) else it.take(12)
                        editingError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(if (isName) "Name" else "Value") },
                    suffix = unitLabel?.let { { Text(it) } },
                    isError = editingError != null,
                    supportingText = editingError?.let { error -> { Text(error) } }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = editingValue.trim()
                        when (field) {
                            PROFILE_FIELD_NAME -> if (value.isBlank()) {
                                editingError = "Enter a name"
                            } else {
                                viewModel.updateName(value)
                                editingField = null
                            }
                            PROFILE_FIELD_HEIGHT -> {
                                val numeric = value.toDoubleOrNull()
                                val cm = numeric?.let { if (uiState.profile.useMetricSystem) it else it * 2.54 }
                                if (cm == null || cm !in 80.0..250.0) {
                                    editingError = "Enter a height from 80–250 cm"
                                } else {
                                    viewModel.updateHeightCm(cm)
                                    editingField = null
                                }
                            }
                            PROFILE_FIELD_WEIGHT, PROFILE_FIELD_GOAL_WEIGHT -> {
                                val numeric = value.toDoubleOrNull()
                                val kg = numeric?.let { if (uiState.profile.useMetricSystem) it else it / 2.20462 }
                                if (kg == null || kg !in 20.0..300.0) {
                                    editingError = "Enter a weight from 20–300 kg"
                                } else {
                                    if (field == PROFILE_FIELD_WEIGHT) viewModel.updateWeightKg(kg)
                                    else viewModel.updateGoalWeightKg(kg)
                                    editingField = null
                                }
                            }
                        }
                    }
                ) { Text(stringResource(R.string.txt_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null; editingError = null }) {
                    Text(stringResource(R.string.txt_cancel))
                }
            }
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
            title = { Text(stringResource(R.string.txt_delete_profile)) },
            text = { Text("Are you sure you want to delete ${multiProfileState.profileToDelete?.displayName}'s profile? All their data will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = multiProfileViewModel::deleteProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.txt_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = multiProfileViewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.txt_cancel))
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

private fun saveBitmapToPrivateStorage(@ApplicationContext context: Context, bitmap: Bitmap): Uri? {
    return try {
        val directory = File(context.filesDir, "profile_images").apply { mkdirs() }
        val file = File(directory, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, PROFILE_IMAGE_QUALITY, out)) {
                throw IllegalStateException("Bitmap compression failed")
            }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        Log.e(PROFILE_SCREEN_TAG, "Failed to save profile image", e)
        null
    }
}

private const val PROFILE_FIELD_NAME = "name"
private const val PROFILE_FIELD_HEIGHT = "height"
private const val PROFILE_FIELD_WEIGHT = "weight"
private const val PROFILE_FIELD_GOAL_WEIGHT = "goal_weight"

