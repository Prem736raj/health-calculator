package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpEducationalScreen(
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sections = remember { BpEducationalContent.getAllSections() }
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BP Education", fontWeight = FontWeight.Bold)
                        Text(
                            "Learn about blood pressure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Intro card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Blood Pressure Education",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Tap any section below to expand and learn. Knowledge is the first step to better health.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Sections
            itemsIndexed(sections) { index, section ->
                val isExpanded = expandedSections[section.id] ?: false

                val enterAnim = remember {
                    MutableTransitionState(false).apply { targetState = true }
                }

                AnimatedVisibility(
                    visibleState = enterAnim,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(400, delayMillis = index * 80)
                    ) + fadeIn(animationSpec = tween(400, delayMillis = index * 80))
                ) {
                    EducationalSectionCard(
                        section = section,
                        isExpanded = isExpanded,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expandedSections[section.id] = !isExpanded
                        }
                    )
                }
            }

            // Medical Disclaimer
            item {
                EducationalDisclaimer()
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ─── Section Card ──────────────────────────────────────────────────────────────

@Composable
private fun EducationalSectionCard(
    section: BpEducationalSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    section.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        section.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "section_chevron_${section.id}"
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(350)) + fadeIn(animationSpec = tween(350)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    section.content.forEachIndexed { idx, item ->
                        val itemAnim = remember {
                            MutableTransitionState(false).apply { targetState = true }
                        }
                        AnimatedVisibility(
                            visibleState = itemAnim,
                            enter = slideInVertically(
                                initialOffsetY = { 20 },
                                animationSpec = tween(300, delayMillis = idx * 40)
                            ) + fadeIn(animationSpec = tween(300, delayMillis = idx * 40))
                        ) {
                            EducationalItemRenderer(item = item)
                        }
                    }
                }
            }
        }
    }
}

// ─── Item Renderers ────────────────────────────────────────────────────────────

@Composable
private fun EducationalItemRenderer(item: BpEducationalItem) {
    when (item) {
        is BpEducationalItem.Paragraph -> ParagraphRenderer(item)
        is BpEducationalItem.Heading -> HeadingRenderer(item)
        is BpEducationalItem.BulletPoint -> BulletPointRenderer(item)
        is BpEducationalItem.NumberedStep -> NumberedStepRenderer(item)
        is BpEducationalItem.HighlightBox -> HighlightBoxRenderer(item)
        is BpEducationalItem.ComparisonRow -> ComparisonRowRenderer(item)
        is BpEducationalItem.MythBuster -> MythBusterRenderer(item)
        is BpEducationalItem.Analogy -> AnalogyRenderer(item)
        is BpEducationalItem.DividerItem -> DividerRenderer(item)
    }
}

@Composable
private fun ParagraphRenderer(item: BpEducationalItem.Paragraph) {
    Text(
        item.text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    )
}

@Composable
private fun HeadingRenderer(item: BpEducationalItem.Heading) {
    Text(
        item.text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun BulletPointRenderer(item: BpEducationalItem.BulletPoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            item.icon,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 1.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun NumberedStepRenderer(item: BpEducationalItem.NumberedStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${item.number}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.icon, style = MaterialTheme.typography.titleSmall)
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun HighlightBoxRenderer(item: BpEducationalItem.HighlightBox) {
    val (bgColor, borderColor, iconTint) = when (item.type) {
        HighlightType.INFO -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.06f),
            Color(0xFF2196F3).copy(alpha = 0.2f),
            Color(0xFF1565C0)
        )
        HighlightType.WARNING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.06f),
            Color(0xFFFF9800).copy(alpha = 0.2f),
            Color(0xFFE65100)
        )
        HighlightType.SUCCESS -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.06f),
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF2E7D32)
        )
        HighlightType.TIP -> Triple(
            Color(0xFF9C27B0).copy(alpha = 0.06f),
            Color(0xFF9C27B0).copy(alpha = 0.2f),
            Color(0xFF6A1B9A)
        )
        HighlightType.DANGER -> Triple(
            Color(0xFFF44336).copy(alpha = 0.06f),
            Color(0xFFF44336).copy(alpha = 0.2f),
            Color(0xFFC62828)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.emoji, style = MaterialTheme.typography.titleSmall)
                Text(
                    item.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
            }
            Text(
                item.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ComparisonRowRenderer(item: BpEducationalItem.ComparisonRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            item.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.25f)
        )
        Card(
            modifier = Modifier.weight(0.375f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "✅ ${item.include}",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32)
            )
        }
        Card(
            modifier = Modifier.weight(0.375f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.08f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "❌ ${item.avoid}",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun MythBusterRenderer(item: BpEducationalItem.MythBuster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Myth
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("❌", style = MaterialTheme.typography.titleSmall)
                    Column {
                        Text(
                            "MYTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            item.myth,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFD32F2F).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Fact
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✅", style = MaterialTheme.typography.titleSmall)
                    Column {
                        Text(
                            "FACT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            item.fact,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalogyRenderer(item: BpEducationalItem.Analogy) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(item.emoji, style = MaterialTheme.typography.headlineSmall)
            Column {
                Text(
                    "Think of it this way...",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8F00)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    item.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun DividerRenderer(item: BpEducationalItem.DividerItem) {
    if (item.label.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Text(
                "  ${item.label}  ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    } else {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    }
}

// ─── Medical Disclaimer ────────────────────────────────────────────────────────

@Composable
private fun EducationalDisclaimer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Column {
                Text(
                    "Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "This educational content is provided for informational purposes only and should not be considered a substitute for professional medical advice, diagnosis, or treatment. Always seek the advice of your physician or other qualified health provider with any questions you may have regarding a medical condition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "The information presented is based on general medical guidelines and may not apply to your specific health situation. Individual health needs vary, and treatment decisions should be made in consultation with qualified healthcare professionals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
