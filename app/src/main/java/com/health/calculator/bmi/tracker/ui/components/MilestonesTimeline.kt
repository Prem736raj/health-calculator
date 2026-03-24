package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.HealthMilestone
import com.health.calculator.bmi.tracker.data.models.MilestoneCategory
import com.health.calculator.bmi.tracker.data.models.MilestoneType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MilestonesSection(
    earnedMilestones: List<HealthMilestone>,
    unearnedMilestoneTypes: List<MilestoneType>,
    selectedCategory: MilestoneCategory?,
    onCategorySelected: (MilestoneCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🏅 Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${earnedMilestones.size}/${earnedMilestones.size + unearnedMilestoneTypes.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(MilestoneCategory.values()) { category ->
                val earnedInCategory = earnedMilestones.count { m ->
                    try { MilestoneType.valueOf(m.milestoneType).category == category } catch (e: Exception) { false }
                }
                val totalInCategory = MilestoneType.values().count { it.category == category }

                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            "${category.icon} ${category.displayName} ($earnedInCategory/$totalInCategory)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Earned milestones
        if (earnedMilestones.isNotEmpty()) {
            earnedMilestones.forEachIndexed { index, milestone ->
                val milestoneType = try {
                    MilestoneType.valueOf(milestone.milestoneType)
                } catch (e: Exception) {
                    null
                } ?: return@forEachIndexed

                EarnedMilestoneItem(
                    milestone = milestone,
                    milestoneType = milestoneType,
                    isLast = index == earnedMilestones.lastIndex && unearnedMilestoneTypes.isEmpty(),
                    animDelay = index * 60
                )
            }
        }

        // Unearned milestones
        if (unearnedMilestoneTypes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp, bottom = 4.dp)
            )

            unearnedMilestoneTypes.forEach { milestoneType ->
                LockedMilestoneItem(milestoneType = milestoneType)
            }
        }
    }
}

@Composable
private fun EarnedMilestoneItem(
    milestone: HealthMilestone,
    milestoneType: MilestoneType,
    isLast: Boolean,
    animDelay: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.8f)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Timeline connector
            Column(
                modifier = Modifier.width(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = milestoneType.icon,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (isLast) 0.dp else 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = milestoneType.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(milestone.achievedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = milestoneType.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    milestone.details?.let { details ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = details,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockedMilestoneItem(milestoneType: MilestoneType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.5f)
    ) {
        // Timeline connector
        Column(
            modifier = Modifier.width(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp)
        ) {
            Text(
                text = "${milestoneType.icon} ${milestoneType.displayName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = milestoneType.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
