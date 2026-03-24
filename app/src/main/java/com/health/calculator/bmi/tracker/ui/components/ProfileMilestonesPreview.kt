package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🏅 Milestones & Records",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View All",
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
                MiniStat(
                    value = "${journeySummary.daysSinceFirstUse}",
                    label = "Days",
                    icon = "📅"
                )
                MiniStat(
                    value = "${journeySummary.totalCalculations}",
                    label = "Calcs",
                    icon = "📊"
                )
                MiniStat(
                    value = "${journeySummary.milestonesEarned}/${journeySummary.totalMilestonesAvailable}",
                    label = "Milestones",
                    icon = "🏅"
                )
                MiniStat(
                    value = "${journeySummary.personalRecordsSet}",
                    label = "Records",
                    icon = "🏆"
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
                    text = "Recent Achievements",
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
                            label = {
                                Text(
                                    "${type.icon} ${type.displayName}",
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
                            label = {
                                Text(
                                    "${type.icon} ${record.displayValue}",
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
private fun MiniStat(value: String, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon)
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
