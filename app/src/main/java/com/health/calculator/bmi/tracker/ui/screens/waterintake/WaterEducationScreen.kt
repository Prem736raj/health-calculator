// ui/screens/waterintake/WaterEducationScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBlueSurface = Color(0xFFE3F2FD)
private val WaterBluePale = Color(0xFFBBDEFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterEducationScreen(
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📚", fontSize = 22.sp)
                        Text("Hydration Guide", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            // Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
                ) {
                    EducationHeaderCard()
                }
            }

            // Section 1: Why Hydration Matters
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
                ) {
                    WhyHydrationMattersSection()
                }
            }

            // Section 2: How Much Water
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
                ) {
                    HowMuchWaterSection()
                }
            }

            // Section 3: Overhydration Warning
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
                ) {
                    OverhydrationSection()
                }
            }

            // Section 4: Exercise & Hydration
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
                ) {
                    ExerciseHydrationSection()
                }
            }

            // Section 5: Special Needs
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 40 }
                ) {
                    SpecialNeedsSection()
                }
            }

            // Medical Disclaimer
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 600))
                ) {
                    MedicalDisclaimerCard()
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun EducationHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(WaterBlueMedium, WaterBlueDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💧📖", fontSize = 36.sp)
                Text(
                    "Everything You Need to\nKnow About Hydration",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Text(
                    "Evidence-based information to help you stay healthy and hydrated",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Section 1: Why Hydration Matters ────────────────────────────────────────

@Composable
private fun WhyHydrationMattersSection() {
    val benefits = listOf(
        BenefitItem("⚡", "Energy & Performance",
            "Water is essential for energy production in every cell. Even mild dehydration (1-2%) can impair physical performance and cause fatigue. Staying hydrated helps maintain energy levels throughout the day."
        ),
        BenefitItem("🧠", "Brain Function & Mood",
            "Your brain is about 75% water. Dehydration can impair concentration, memory, and mood. Studies show that even 1-2% dehydration can affect cognitive performance and increase anxiety."
        ),
        BenefitItem("✨", "Skin Health",
            "Proper hydration helps maintain skin elasticity and can reduce the appearance of wrinkles. While water alone won't cure skin conditions, chronic dehydration can make your skin look dull and dry."
        ),
        BenefitItem("🫀", "Heart & Circulation",
            "Water helps maintain blood volume and supports healthy circulation. When dehydrated, your heart has to work harder to pump blood, which can increase heart rate and lower blood pressure."
        ),
        BenefitItem("🫁", "Digestion & Metabolism",
            "Water is crucial for proper digestion. It helps break down food, absorb nutrients, and prevent constipation. Adequate hydration also supports your body's metabolic processes."
        ),
        BenefitItem("🦴", "Joint Lubrication",
            "The cartilage in joints and spinal discs is about 80% water. Proper hydration keeps joints lubricated, reducing friction and helping prevent joint pain and discomfort."
        ),
        BenefitItem("🌡️", "Temperature Regulation",
            "Your body uses water (through sweat) to regulate temperature. Without adequate hydration, your body can't cool itself effectively, increasing the risk of heat-related illnesses."
        ),
        BenefitItem("🫘", "Kidney Function",
            "Kidneys need water to filter waste from the blood and produce urine. Adequate hydration reduces the risk of kidney stones and urinary tract infections. Dark urine often signals that you need more water."
        ),
        BenefitItem("🛡️", "Immune Support",
            "Water helps carry oxygen to cells, including immune cells, and supports the lymphatic system. Proper hydration helps your body fight off illness more effectively."
        )
    )

    ExpandableEducationCard(
        title = "Why Hydration Matters",
        icon = "💪",
        gradientColors = listOf(Color(0xFF1976D2), Color(0xFF0D47A1)),
        badge = "9 Benefits"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Quick stat
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WaterBlueSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickStatItem("60%", "of your body\nis water")
                    QuickStatItem("75%", "of your brain\nis water")
                    QuickStatItem("83%", "of your lungs\nis water")
                }
            }

            Spacer(Modifier.height(8.dp))

            benefits.forEach { benefit ->
                BenefitRow(benefit)
            }
        }
    }
}

@Composable
private fun QuickStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = WaterBlueDark
        )
        Text(
            label,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 14.sp
        )
    }
}

data class BenefitItem(val icon: String, val title: String, val description: String)

@Composable
private fun BenefitRow(benefit: BenefitItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (expanded) WaterBlueSurface.copy(alpha = 0.5f)
                    else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WaterBlueSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(benefit.icon, fontSize = 20.sp)
            }
            Text(
                benefit.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
        ) {
            Text(
                text = benefit.description,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 52.dp, end = 12.dp, bottom = 8.dp, top = 4.dp)
            )
        }
    }
}

// ─── Section 2: How Much Water ───────────────────────────────────────────────

@Composable
private fun HowMuchWaterSection() {
    ExpandableEducationCard(
        title = "How Much Water Do You Really Need?",
        icon = "🤔",
        gradientColors = listOf(Color(0xFF00897B), Color(0xFF004D40)),
        badge = "Myth Busted"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Myth busting
            MythBustCard(
                myth = "\"Everyone needs exactly 8 glasses (2L) of water per day\"",
                reality = "This is one of the most common health myths! The \"8×8 rule\" (eight 8-ounce glasses) has no scientific basis. " +
                        "Your actual water needs depend on your body weight, activity level, climate, health status, and diet. " +
                        "The original recommendation may have included water from all sources, including food."
            )

            // Actual guidelines
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0F2F1)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📋 General Guidelines", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF004D40))
                    HorizontalDivider(color = Color(0xFF004D40).copy(alpha = 0.1f))

                    GuidelineRow("🏢", "WHO Recommendation", "Approximately 2-2.5L/day for adults in temperate climates")
                    GuidelineRow("⚖️", "Body Weight Method", "30-35ml per kg of body weight is a good baseline")
                    GuidelineRow("🚽", "Urine Test", "If your urine is pale yellow, you're likely well hydrated")
                    GuidelineRow("🥵", "Thirst Mechanism", "For most healthy adults, drinking when thirsty is reasonable")
                }
            }

            // Factors that increase needs
            Text("📈 Factors That Increase Your Needs:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            val factors = listOf(
                "🏃" to "Physical activity — you lose water through sweat",
                "☀️" to "Hot or humid weather — increases perspiration",
                "🏔️" to "High altitude — faster breathing and increased urination",
                "🤒" to "Illness — fever, vomiting, or diarrhea",
                "🤰" to "Pregnancy and breastfeeding",
                "☕" to "High caffeine or alcohol intake (mild diuretic effect)",
                "🧂" to "High sodium diet — your body needs more water to process salt",
                "✈️" to "Air travel — cabin air is very dry"
            )

            factors.forEach { (icon, text) ->
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(text, fontSize = 13.sp, lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }

            // Pro tip
            InfoTipCard(
                "💡 Pro Tip",
                "The best indicator of your hydration status is the color of your urine. " +
                        "Aim for pale straw color. If it's dark yellow or amber, drink more water. " +
                        "If it's completely clear, you might be overhydrating."
            )
        }
    }
}

@Composable
private fun MythBustCard(myth: String, reality: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("❌", fontSize = 18.sp)
                Text("MYTH", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFE65100))
            }
            Text(
                myth,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFBF360C),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            HorizontalDivider(color = Color(0xFFE65100).copy(alpha = 0.2f))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✅", fontSize = 18.sp)
                Column {
                    Text("REALITY", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        reality,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidelineRow(icon: String, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 16.sp)
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(detail, fontSize = 12.sp, lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

// ─── Section 3: Overhydration ────────────────────────────────────────────────

@Composable
private fun OverhydrationSection() {
    ExpandableEducationCard(
        title = "Signs of Overhydration",
        icon = "⚠️",
        gradientColors = listOf(Color(0xFFF57C00), Color(0xFFE65100)),
        badge = "Important"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Key message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Text(
                        "Yes, you can drink too much water. While rare, overhydration (water intoxication) can be dangerous. " +
                                "It's most common in endurance athletes and people who drink excessive amounts quickly.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFBF360C)
                    )
                }
            }

            // Hyponatremia
            Text("🔬 What is Hyponatremia?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                "Hyponatremia occurs when sodium levels in your blood become dangerously low, usually from drinking too much " +
                        "water too quickly. Sodium is essential for nerve and muscle function. When diluted, cells can swell, " +
                        "which is particularly dangerous for brain cells.",
                fontSize = 13.sp, lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Symptoms
            Text("🚩 Warning Signs:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            val symptoms = listOf(
                "🤢" to "Nausea and vomiting" to "Early warning sign of water intoxication",
                "🤕" to "Persistent headache" to "Caused by brain cell swelling",
                "😵💫" to "Confusion or disorientation" to "Serious sign — seek medical attention",
                "💪" to "Muscle cramps or weakness" to "Due to electrolyte imbalance",
                "🫧" to "Bloating and swelling" to "Especially in hands and feet",
                "😴" to "Extreme fatigue" to "Beyond normal tiredness",
                "🚨" to "Seizures (severe cases)" to "Medical emergency — call 911"
            )

            symptoms.forEach { (iconLabel, description) ->
                val (icon, name) = iconLabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFFF8E1).copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, fontSize = 18.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(description, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }

            // When to be careful
            Text("🛑 When to Be Careful:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            val cautions = listOf(
                "🏃" to "During marathon/endurance events — don't force excessive drinking",
                "⏰" to "Don't drink large amounts in short time (>1L per hour sustained)",
                "🧂" to "If you sweat a lot, replace electrolytes too, not just water",
                "💊" to "Certain medications can affect water balance — consult your doctor",
                "📏" to "General safe upper limit: ~3-4L per day for most adults"
            )

            cautions.forEach { (icon, text) ->
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(text, fontSize = 13.sp, lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }

            InfoTipCard(
                "💡 Balance Is Key",
                "Listen to your body. Drink when thirsty, sip throughout the day rather than gulping large amounts, " +
                        "and pay attention to your urine color. Pale yellow is the sweet spot."
            )
        }
    }
}

// ─── Section 4: Exercise & Hydration ─────────────────────────────────────────

@Composable
private fun ExerciseHydrationSection() {
    ExpandableEducationCard(
        title = "Hydration and Exercise",
        icon = "🏋️",
        gradientColors = listOf(Color(0xFF43A047), Color(0xFF1B5E20)),
        badge = "Active Guide"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Timeline visual
            Text("⏱️ Exercise Hydration Timeline", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            // Before
            ExercisePhaseCard(
                phase = "BEFORE",
                emoji = "🔜",
                timeframe = "2-3 hours before exercise",
                color = Color(0xFF1565C0),
                recommendations = listOf(
                    "💧 Drink 400-600ml (2-3 cups) of water",
                    "⏰ 15-20 minutes before: drink another 200-300ml",
                    "🚽 Allow time for bathroom visit before starting",
                    "⚡ Start your workout well-hydrated for best performance"
                )
            )

            // During
            ExercisePhaseCard(
                phase = "DURING",
                emoji = "🏃",
                timeframe = "During exercise",
                color = Color(0xFF2E7D32),
                recommendations = listOf(
                    "💧 Drink 150-250ml every 15-20 minutes",
                    "⏱️ For workouts over 60 min: consider sports drinks",
                    "🧂 For intense/long sessions: add electrolytes",
                    "📏 Don't wait until you're thirsty — drink on schedule",
                    "🌡️ In hot weather: increase intake by 50%"
                )
            )

            // After
            ExercisePhaseCard(
                phase = "AFTER",
                emoji = "🏁",
                timeframe = "Post-exercise recovery",
                color = Color(0xFFE65100),
                recommendations = listOf(
                    "⚖️ Drink 1.25-1.5L for every 1kg of body weight lost",
                    "⏰ Rehydrate gradually over 2-4 hours",
                    "🧂 Include sodium to help retention (salty snack or electrolyte drink)",
                    "🥛 Milk and chocolate milk are excellent post-workout beverages",
                    "🍌 Eat water-rich foods to support recovery"
                )
            )

            // Electrolytes section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚡ Electrolytes & Exercise", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF33691E))
                    HorizontalDivider(color = Color(0xFF33691E).copy(alpha = 0.1f))

                    Text(
                        "For workouts lasting over 60 minutes or in hot conditions, plain water may not be enough. " +
                                "You need to replace electrolytes lost through sweat:",
                        fontSize = 13.sp, lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    val electrolytes = listOf(
                        "🧂" to "Sodium" to "Most important — lost in highest quantities through sweat",
                        "🍌" to "Potassium" to "Supports muscle function and prevents cramps",
                        "💚" to "Magnesium" to "Important for muscle relaxation and energy",
                        "🦴" to "Calcium" to "Supports muscle contraction"
                    )

                    electrolytes.forEach { (iconLabel, desc) ->
                        val (icon, name) = iconLabel
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(icon, fontSize = 16.sp)
                            Column {
                                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(desc, fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Sweat rate
            InfoTipCard(
                "💡 Know Your Sweat Rate",
                "Weigh yourself before and after exercise (without clothes). Each kg lost ≈ 1L of fluid loss. " +
                        "This helps you personalize your hydration strategy. Average sweat rate is 0.5-1.5L/hour."
            )
        }
    }
}

@Composable
private fun ExercisePhaseCard(
    phase: String,
    emoji: String,
    timeframe: String,
    color: Color,
    recommendations: List<String>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(color, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$emoji $phase",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
                Text(
                    timeframe,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            recommendations.forEach { rec ->
                Text(
                    rec,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

// ─── Section 5: Special Needs ────────────────────────────────────────────────

@Composable
private fun SpecialNeedsSection() {
    ExpandableEducationCard(
        title = "Special Hydration Needs",
        icon = "🩺",
        gradientColors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)),
        badge = "5 Categories"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "Certain conditions and situations require adjusted water intake. " +
                        "Always consult your healthcare provider for personalized advice.",
                fontSize = 13.sp, lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Pregnancy
            SpecialNeedCard(
                emoji = "🤰",
                title = "Pregnancy",
                color = Color(0xFFE91E63),
                additionalMl = "+300ml/day",
                details = listOf(
                    "Recommended total: ~2.3L (10 cups) per day",
                    "Water helps form amniotic fluid",
                    "Supports increased blood volume (up to 50% more)",
                    "Helps prevent constipation and UTIs",
                    "Morning sickness? Try small, frequent sips",
                    "Dehydration can trigger contractions — stay ahead"
                )
            )

            // Breastfeeding
            SpecialNeedCard(
                emoji = "🤱",
                title = "Breastfeeding",
                color = Color(0xFF9C27B0),
                additionalMl = "+700ml/day",
                details = listOf(
                    "Recommended total: ~3.1L (13 cups) per day",
                    "Breast milk is about 87% water",
                    "Drink a glass of water each time you nurse",
                    "Watch for signs of dehydration in yourself",
                    "Caffeine should be limited — it passes to breast milk"
                )
            )

            // Illness/Fever
            SpecialNeedCard(
                emoji = "🤒",
                title = "Illness & Fever",
                color = Color(0xFFF44336),
                additionalMl = "+500-1000ml/day",
                details = listOf(
                    "Fever increases water loss through sweating",
                    "Vomiting and diarrhea rapidly deplete fluids",
                    "Use oral rehydration solutions for severe cases",
                    "Sip frequently rather than large amounts at once",
                    "Include electrolytes — broth and sports drinks can help",
                    "Seek medical attention if unable to keep fluids down"
                )
            )

            // High Altitude
            SpecialNeedCard(
                emoji = "🏔️",
                title = "High Altitude",
                color = Color(0xFF1565C0),
                additionalMl = "+500ml/day",
                details = listOf(
                    "Above 2,500m (8,200ft): increase water intake",
                    "Lower humidity and faster breathing increase water loss",
                    "Increased urination is common at altitude",
                    "Proper hydration helps prevent altitude sickness",
                    "Avoid alcohol which worsens dehydration at altitude"
                )
            )

            // Hot Climate
            SpecialNeedCard(
                emoji = "🌡️",
                title = "Hot & Humid Climate",
                color = Color(0xFFFF6F00),
                additionalMl = "+500-750ml/day",
                details = listOf(
                    "You can lose 1-2L per hour of sweat in extreme heat",
                    "Don't wait until thirsty — drink proactively",
                    "Take breaks in shade and drink regularly",
                    "Watch for heat exhaustion signs: heavy sweating, weakness, nausea",
                    "Cold water is absorbed faster than warm water",
                    "Electrolyte drinks are helpful for prolonged heat exposure"
                )
            )

            // Medical disclaimer
            InfoTipCard(
                "⚕️ Medical Note",
                "Some medical conditions (such as heart failure, kidney disease, or those on fluid restrictions) " +
                        "may require limited fluid intake. Always follow your healthcare provider's specific recommendations."
            )
        }
    }
}

@Composable
private fun SpecialNeedCard(
    emoji: String,
    title: String,
    color: Color,
    additionalMl: String,
    details: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        additionalMl,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "expand_$title"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = color.copy(alpha = 0.15f))
                    Spacer(Modifier.height(4.dp))
                    details.forEach { detail ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("•", color = color, fontWeight = FontWeight.Bold)
                            Text(
                                detail, fontSize = 13.sp, lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

@Composable
private fun ExpandableEducationCard(
    title: String,
    icon: String,
    gradientColors: List<Color>,
    badge: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (expanded) 0.dp else 16.dp,
                            bottomEnd = if (expanded) 0.dp else 16.dp
                        )
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        expanded = !expanded
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(icon, fontSize = 28.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (badge != null) {
                        Text(
                            badge,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "expand_arrow"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.rotate(rotation)
                )
            }

            // Content - expandable
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun InfoTipCard(title: String, text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WaterBlueSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WaterBlueDark)
                Spacer(Modifier.height(4.dp))
                Text(
                    text,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Medical Disclaimer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "This information is for educational purposes only and should not replace professional medical advice, " +
                            "diagnosis, or treatment. Always consult your healthcare provider with questions about hydration " +
                            "and your specific health needs. Individual water requirements can vary significantly based on " +
                            "health conditions and medications.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
