package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.ActivityLevel
import com.health.calculator.bmi.tracker.data.model.EthnicityRegion
import com.health.calculator.bmi.tracker.data.model.FrameSize
import com.health.calculator.bmi.tracker.data.model.HealthGoal
import com.health.calculator.bmi.tracker.domain.model.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    )
}

@Composable
fun GenderSelectionContent(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit
) {
    Column {
        Gender.values().filter { it != Gender.NOT_SPECIFIED }.forEach { gender ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGenderSelected(gender) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = gender.displayName)
                RadioButton(
                    selected = selectedGender == gender,
                    onClick = { onGenderSelected(gender) }
                )
            }
        }
    }
}

@Composable
fun ActivityLevelSelectionContent(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
        items(ActivityLevel.values().filter { it != ActivityLevel.NOT_SET }) { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLevelSelected(level) }
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${level.emoji} ${level.displayName}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RadioButton(
                    selected = selectedLevel == level,
                    onClick = { onLevelSelected(level) }
                )
            }
        }
    }
}

@Composable
fun HealthGoalsSelectionContent(
    selectedGoals: List<HealthGoal>,
    onGoalsChanged: (List<HealthGoal>) -> Unit
) {
    Column {
        HealthGoal.values().filter { it != HealthGoal.NOT_SET }.forEach { goal ->
            val isSelected = selectedGoals.contains(goal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newGoals = if (isSelected) {
                            selectedGoals - goal
                        } else {
                            selectedGoals + goal
                        }
                        onGoalsChanged(newGoals)
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${goal.emoji} ${goal.displayName}")
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            }
        }
    }
}

@Composable
fun FrameSizeSelectionContent(
    selectedSize: FrameSize,
    onSizeSelected: (FrameSize) -> Unit
) {
    Column {
        FrameSize.values().forEach { size ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSizeSelected(size) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = size.displayName)
                RadioButton(
                    selected = selectedSize == size,
                    onClick = { onSizeSelected(size) }
                )
            }
        }
    }
}

@Composable
fun EthnicitySelectionContent(
    selectedRegion: EthnicityRegion,
    onRegionSelected: (EthnicityRegion) -> Unit
) {
    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
        items(EthnicityRegion.values()) { region ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRegionSelected(region) }
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = region.displayName,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = region.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RadioButton(
                    selected = selectedRegion == region,
                    onClick = { onRegionSelected(region) }
                )
            }
        }
    }
}
