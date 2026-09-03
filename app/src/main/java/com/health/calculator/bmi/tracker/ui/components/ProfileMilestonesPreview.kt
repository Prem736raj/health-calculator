package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.HealthJourneySummary
import com.health.calculator.bmi.tracker.data.models.HealthMilestone
import com.health.calculator.bmi.tracker.data.models.MilestoneType
import com.health.calculator.bmi.tracker.data.models.PersonalRecord
import com.health.calculator.bmi.tracker.data.models.PersonalRecordType
import com.health.calculator.bmi.tracker.ui.theme.InsightCalloutShape

@Composable
fun ProfileMilestonesPreview(
    journeySummary: HealthJourneySummary,
    recentRecords: List<PersonalRecord>,
    recentMilestones: List<HealthMilestone>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewAll),
        shape = InsightCalloutShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.34f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.32f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.txt_milestones_records),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.txt_view_all),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(value = "${journeySummary.daysSinceFirstUse}", label = "Days", icon = Icons.Outlined.CalendarMonth)
                MiniStat(
                    value = "${journeySummary.totalCalculations}",
                    label = "Calcs",
                    icon = Icons.Outlined.Assessment
                )
                MiniStat(
                    value = "${journeySummary.milestonesEarned}/${journeySummary.totalMilestonesAvailable}",
                    label = "Milestones",
                    icon = Icons.Outlined.Flag
                )
                MiniStat(
                    value = "${journeySummary.personalRecordsSet}",
                    label = "Records",
                    icon = Icons.Outlined.EmojiEvents
                )
            }

            // Recent achievements scroll
            if (recentMilestones.isNotEmpty() || recentRecords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.txt_recent_achievements),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentMilestones.take(3)) { milestone ->
                        val type = try {
                            MilestoneType.valueOf(milestone.milestoneType)
                        } catch (e: Exception) {
                            null
                        } ?: return@items

                        AssistChip(
                            onClick = onViewAll,
                            leadingIcon = {
                                Icon(
                                    milestoneIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    type.displayName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    items(recentRecords.take(2)) { record ->
                        val type = try {
                            PersonalRecordType.valueOf(record.recordType)
                        } catch (e: Exception) {
                            null
                        } ?: return@items

                        AssistChip(
                            onClick = onViewAll,
                            leadingIcon = {
                                Icon(
                                    personalRecordIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    record.displayValue,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun milestoneIcon(type: MilestoneType): androidx.compose.ui.graphics.vector.ImageVector = when (type.category) {
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.GETTING_STARTED -> Icons.Outlined.Flag
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.CONSISTENCY -> Icons.Outlined.CalendarMonth
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.ACHIEVEMENTS -> Icons.Outlined.EmojiEvents
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.EXPLORATION -> Icons.Outlined.Explore
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.HEALTH_WINS -> Icons.Outlined.ShowChart
    com.health.calculator.bmi.tracker.data.models.MilestoneCategory.SOCIAL -> Icons.Outlined.Share
}

private fun personalRecordIcon(type: PersonalRecordType): androidx.compose.ui.graphics.vector.ImageVector = when {
    type.name.contains("WEIGHT") -> Icons.Outlined.MonitorWeight
    type.name.contains("WATER") -> Icons.Outlined.WaterDrop
    type.name.contains("BP") || type.name.contains("HR") -> Icons.Outlined.FavoriteBorder
    type.name.contains("BMI") || type.name.contains("WHR") -> Icons.Outlined.Assessment
    else -> Icons.Outlined.EmojiEvents
}
