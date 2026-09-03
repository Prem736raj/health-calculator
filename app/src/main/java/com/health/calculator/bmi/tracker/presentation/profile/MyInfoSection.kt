package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.domain.model.UserProfile
import com.health.calculator.bmi.tracker.ui.components.ProfilePictureSection
import com.health.calculator.bmi.tracker.ui.components.WellnessIconBadge
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyInfoSection(
    profile: UserProfile,
    onNameChange: (String) -> Unit,
    onProfilePictureClick: () -> Unit,
    onDateOfBirthClick: () -> Unit,
    onGenderClick: () -> Unit,
    onHeightClick: () -> Unit,
    onWeightClick: () -> Unit,
    onGoalWeightClick: () -> Unit,
    onActivityLevelClick: () -> Unit,
    onHealthGoalsClick: () -> Unit,
    onFrameSizeClick: () -> Unit,
    onEthnicityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ProfilePictureSection(
            profilePictureUri = profile.profilePictureUri,
            initials = if (profile.name.isNotEmpty()) profile.name.take(1) else "U",
            onEditClick = onProfilePictureClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.txt_personal_details),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        InfoItem(
            label = "Name",
            value = profile.name.ifBlank { "Not set" },
            icon = Icons.Default.Person,
            onClick = { /* Handle name edit inline or dialog */ } 
        )

        InfoItem(
            label = "Date of Birth",
            value = profile.dateOfBirthMillis?.let { 
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
            } ?: "Not set",
            icon = Icons.Default.Cake,
            onClick = onDateOfBirthClick
        )

        InfoItem(
            label = "Gender",
            value = profile.gender.displayName,
            icon = Icons.Default.Wc,
            onClick = onGenderClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.txt_physical_parameters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        InfoItem(
            label = "Height",
            value = if (profile.heightCm != null && profile.heightCm > 0) "${profile.heightCm} cm" else "Not set",
            icon = Icons.Default.Height,
            onClick = onHeightClick
        )

        InfoItem(
            label = "Weight",
            value = if (profile.weightKg != null && profile.weightKg > 0) "${profile.weightKg} kg" else "Not set",
            icon = Icons.Default.MonitorWeight,
            onClick = onWeightClick
        )

        InfoItem(
            label = "Goal Weight",
            value = if (profile.goalWeightKg != null && profile.goalWeightKg > 0) "${profile.goalWeightKg} kg" else "Not set",
            icon = Icons.Default.Flag,
            onClick = onGoalWeightClick
        )

        InfoItem(
            label = "Frame Size",
            value = profile.frameSize.displayName,
            icon = Icons.Default.Boy,
            onClick = onFrameSizeClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.txt_lifestyle_goals),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        InfoItem(
            label = "Activity Level",
            value = profile.activityLevel.displayName,
            icon = Icons.Default.DirectionsRun,
            onClick = onActivityLevelClick
        )

        val goalsText = if (profile.healthGoals.isEmpty()) "Not set" 
                        else profile.healthGoals.joinToString(", ") { it.displayName }
        InfoItem(
            label = "Health Goals",
            value = goalsText,
            icon = Icons.Default.Favorite,
            onClick = onHealthGoalsClick
        )

        InfoItem(
            label = "Ethnicity / Region",
            value = profile.ethnicityRegion.displayName,
            icon = Icons.Default.Public,
            onClick = onEthnicityClick
        )

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom button/fab
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val iconTint = when {
        label in setOf("Height", "Weight", "Goal Weight") -> HealthColors.Healthy
        label in setOf("Activity Level", "Health Goals", "Ethnicity / Region") -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WellnessIconBadge(
                icon = icon,
                tint = iconTint,
                container = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = if (label in setOf("Height", "Weight", "Goal Weight")) {
                        WellnessMetricTextStyle.copy(fontSize = 16.sp, lineHeight = 24.sp)
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
