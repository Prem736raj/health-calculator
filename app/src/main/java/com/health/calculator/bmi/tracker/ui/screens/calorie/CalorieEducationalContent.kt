package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalorieEducationalContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Calorie Education",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Section 1: Understanding Calories
        EducationCard(
            icon = Icons.Default.Lightbulb,
            iconColor = Color(0xFFFFEB3B),
            title = "Understanding Calories",
            content = { UnderstandingCaloriesContent() }
        )

        // Section 2: Deficit vs Surplus
        EducationCard(
            icon = Icons.Default.Balance,
            iconColor = Color(0xFF2196F3),
            title = "Calorie Deficit vs Surplus",
            content = { DeficitVsSurplusContent() }
        )

        // Section 3: Danger of Too Few Calories
        EducationCard(
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFF44336),
            title = "The Danger of Too Few Calories",
            content = { TooFewCaloriesContent() }
        )

        // Section 4: Calories in Common Foods
        EducationCard(
            icon = Icons.Default.Restaurant,
            iconColor = Color(0xFF4CAF50),
            title = "Calories in Common Foods",
            content = { CommonFoodsReferenceContent() }
        )

        // Section 5: Accurate Tracking Tips
        EducationCard(
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF9C27B0),
            title = "Tips for Accurate Calorie Tracking",
            content = { AccurateTrackingTipsContent() }
        )

        // Mindful Note
        MindfulNoteCard()
        
        // Medical Disclaimer
        MedicalDisclaimerEducation()
    }
}

@Composable
private fun EducationCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = iconColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}

// ─── SECTION 1: Understanding Calories ───────────────────────────────────────

@Composable
private fun UnderstandingCaloriesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EduParagraph(
            "A calorie (kcal) is a unit of energy. Specifically, it's the amount of energy needed to raise the temperature of 1 kilogram of water by 1°C. Your body uses calories from food as its primary fuel source."
        )

        EduHighlight(
            emoji = "⚡",
            title = "Energy In, Energy Out",
            text = "Your body uses calories in three main ways:\n\n• BMR (Basal Metabolic Rate): ~60-75% — energy to keep you alive at rest (breathing, circulation, cell repair)\n• Physical Activity: ~15-30% — energy for movement and exercise\n• TEF (Thermic Effect of Food): ~10% — energy to digest and absorb food",
            color = Color(0xFFFFEB3B)
        )

        EduHighlight(
            emoji = "🥗",
            title = "Quality Matters as Much as Quantity",
            text = "While calorie balance drives weight change, food quality drives health outcomes:\n\n• 500 calories from broccoli and chicken provides vitamins, minerals, and protein\n• 500 calories from cookies provides mostly sugar and fat\n• Both affect weight the same, but they affect your health very differently\n\nA sustainable approach combines appropriate calorie intake with nutritious food choices.",
            color = Color(0xFF4CAF50)
        )

        EduHighlight(
            emoji = "🔬",
            title = "Not All Calories Are Processed Equally",
            text = "Your body processes different foods at different rates:\n• Protein has a high thermic effect (20-35%) — your body burns more calories digesting it\n• Fat is efficiently stored (0-5% thermic effect)\n• Whole foods take longer to digest than processed foods\n• Fiber slows glucose absorption, preventing energy spikes and crashes",
            color = Color(0xFF2196F3)
        )
    }
}

// ─── SECTION 2: Deficit vs Surplus ───────────────────────────────────────────

@Composable
private fun DeficitVsSurplusContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EduParagraph("Weight change is fundamentally governed by the balance between calories consumed and calories burned over time.")

        // Visual balance scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📉", fontSize = 28.sp)
                    Text(
                        "Deficit",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "Calories In\n< Calories Out",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "→ Weight Loss",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2196F3)
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📈", fontSize = 28.sp)
                    Text(
                        "Surplus",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        "Calories In\n> Calories Out",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "→ Weight Gain",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }

        EduHighlight(
            emoji = "📐",
            title = "The 500 Calorie Rule",
            text = "1 kg of body fat contains approximately 7,700 kcal of stored energy.\n\n• 500 cal/day deficit × 7 days = 3,500 kcal/week ≈ 0.45 kg/week\n• 1,000 cal/day deficit × 7 days = 7,000 kcal/week ≈ 0.9 kg/week\n\nThis is a reliable estimate, though real-world results vary based on water retention, muscle gain, hormones, and metabolic adaptation.",
            color = Color(0xFF2196F3)
        )

        EduHighlight(
            emoji = "⚠️",
            title = "Why Extreme Deficits Backfire",
            text = "Cutting too many calories at once is counterproductive:\n\n• Your body adapts by slowing metabolism (metabolic adaptation)\n• Muscle mass is broken down for energy — lowering your BMR further\n• Intense hunger leads to overeating and rebound weight gain\n• Energy and motivation plummet, making exercise harder\n• After stopping, you may gain weight back faster than before\n\nA moderate deficit (250-500 cal/day) is sustainable and effective.",
            color = Color(0xFFFF9800)
        )

        EduHighlight(
            emoji = "💪",
            title = "Lean Bulk vs Aggressive Surplus",
            text = "For weight gain/muscle building:\n\n• Lean bulk (250 cal surplus): slower but mostly muscle gain\n• Aggressive surplus: faster weight gain but more fat accumulation\n• Your body can only synthesize a limited amount of muscle per week regardless of calorie surplus\n• Excess calories beyond muscle synthesis needs are stored as fat",
            color = Color(0xFF9C27B0)
        )
    }
}

// ─── SECTION 3: Too Few Calories ─────────────────────────────────────────────

@Composable
private fun TooFewCaloriesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF44336).copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Warning, null,
                    tint = Color(0xFFF44336), modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Eating too few calories can be just as harmful as overeating. Here's what happens when you restrict calories too severely:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336).copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
        }

        // Minimum recommendations
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "⚡ Minimum Daily Calorie Recommendations",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MinCalCard("👩 Women", "1,200", "kcal/day", Color(0xFFE91E63))
                    MinCalCard("👨 Men", "1,500", "kcal/day", Color(0xFF2196F3))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "These are absolute minimums. Most people need significantly more for safe, sustainable results.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }

        // Consequences
        listOf(
            Triple("🧬", "Metabolic Adaptation",
                "When calories drop too low, your body enters 'survival mode.' Metabolism slows by 15-25% as your body burns fewer calories to preserve fat stores. This makes further weight loss increasingly difficult."),
            Triple("💪", "Muscle Loss",
                "Without enough calories, your body breaks down muscle tissue for energy. This lowers your BMR (fewer calories burned at rest), making it even harder to lose fat long-term."),
            Triple("🥦", "Nutrient Deficiencies",
                "Very low-calorie diets rarely provide adequate vitamins and minerals. This can cause fatigue, hair loss, weakened immune function, poor bone health, and anemia."),
            Triple("⚖️", "Hormonal Disruption",
                "Severe restriction disrupts hunger hormones (ghrelin increases, leptin decreases), sex hormones, thyroid function, and cortisol levels — creating a biochemical drive to overeat.")
        ).forEach { (emoji, title, text) ->
            EduHighlight(emoji = emoji, title = title, text = text, color = Color(0xFFF44336))
        }

        // Warning signs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🚨 Signs You May Be Eating Too Little",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val signs = listOf(
                    "Constantly thinking about food", "Low energy and fatigue",
                    "Difficulty concentrating", "Frequent illness",
                    "Irritability and mood changes", "Hair loss or thinning",
                    "Feeling cold all the time", "Weight loss plateau despite restriction",
                    "Dizziness when standing quickly", "Loss of menstrual cycle (women)"
                )
                signs.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { sign ->
                            Row(modifier = Modifier.weight(1f).padding(vertical = 2.dp)) {
                                Text("• ", color = Color(0xFFF44336), style = MaterialTheme.typography.bodySmall)
                                Text(sign, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MinCalCard(label: String, value: String, unit: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(unit, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
    }
}

// ─── SECTION 4: Common Foods Reference ───────────────────────────────────────

private data class FoodCategoryRef(
    val name: String,
    val emoji: String,
    val color: Color,
    val foods: List<Pair<String, String>> // name to "X kcal / serving"
)

@Composable
private fun CommonFoodsReferenceContent() {
    val categories = remember {
        listOf(
            FoodCategoryRef(
                "Proteins", "🥩", Color(0xFFF44336),
                listOf(
                    "Chicken breast (100g)" to "165 kcal",
                    "Salmon (100g)" to "208 kcal",
                    "Egg (1 large)" to "72 kcal",
                    "Tuna, canned (100g)" to "116 kcal",
                    "Greek yogurt (100g)" to "59 kcal",
                    "Beef, lean (100g)" to "215 kcal",
                    "Tofu (100g)" to "76 kcal",
                    "Whey protein (1 scoop)" to "120 kcal"
                )
            ),
            FoodCategoryRef(
                "Carbohydrates", "🍚", Color(0xFF2196F3),
                listOf(
                    "Rice, white (1 cup, cooked)" to "206 kcal",
                    "Oatmeal (1 cup, cooked)" to "166 kcal",
                    "Bread, white (1 slice)" to "79 kcal",
                    "Bread, whole wheat (1 slice)" to "69 kcal",
                    "Pasta (1 cup, cooked)" to "220 kcal",
                    "Sweet potato (1 medium)" to "103 kcal",
                    "Potato (1 medium, baked)" to "161 kcal",
                    "Quinoa (1 cup, cooked)" to "222 kcal"
                )
            ),
            FoodCategoryRef(
                "Fats", "🥑", Color(0xFF4CAF50),
                listOf(
                    "Avocado (1 medium)" to "234 kcal",
                    "Olive oil (1 tbsp)" to "119 kcal",
                    "Butter (1 tbsp)" to "102 kcal",
                    "Almonds (28g/1oz)" to "164 kcal",
                    "Peanut butter (1 tbsp)" to "95 kcal",
                    "Cheddar cheese (28g)" to "114 kcal",
                    "Whole milk (1 cup)" to "149 kcal",
                    "Coconut oil (1 tbsp)" to "121 kcal"
                )
            ),
            FoodCategoryRef(
                "Fruits", "🍎", Color(0xFFFF9800),
                listOf(
                    "Apple (1 medium)" to "95 kcal",
                    "Banana (1 medium)" to "105 kcal",
                    "Orange (1 medium)" to "62 kcal",
                    "Grapes (1 cup)" to "104 kcal",
                    "Strawberries (1 cup)" to "49 kcal",
                    "Blueberries (1 cup)" to "84 kcal",
                    "Mango (1 cup, diced)" to "99 kcal",
                    "Watermelon (1 cup, diced)" to "46 kcal"
                )
            ),
            FoodCategoryRef(
                "Vegetables", "🥦", Color(0xFF009688),
                listOf(
                    "Broccoli (1 cup)" to "55 kcal",
                    "Spinach (1 cup, raw)" to "7 kcal",
                    "Carrot (1 medium)" to "25 kcal",
                    "Tomato (1 medium)" to "22 kcal",
                    "Cucumber (1 cup)" to "16 kcal",
                    "Bell pepper (1 medium)" to "31 kcal",
                    "Lettuce (1 cup, raw)" to "5 kcal",
                    "Onion (1 medium)" to "44 kcal"
                )
            ),
            FoodCategoryRef(
                "Beverages", "☕", Color(0xFF795548),
                listOf(
                    "Coffee, black (1 cup)" to "2 kcal",
                    "Tea, unsweetened (1 cup)" to "2 kcal",
                    "Orange juice (1 cup)" to "112 kcal",
                    "Whole milk (1 cup)" to "149 kcal",
                    "Cola (1 can, 355ml)" to "140 kcal",
                    "Beer (355ml, regular)" to "153 kcal",
                    "Red wine (1 glass, 148ml)" to "125 kcal",
                    "Sports drink (500ml)" to "125 kcal"
                )
            )
        )
    }

    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EduParagraph("Quick reference guide for common foods. Use this while logging to estimate calorie content.")

        categories.forEach { category ->
            val isExpanded = expandedCategory == category.name

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedCategory = if (isExpanded) null else category.name
                    },
                shape = RoundedCornerShape(12.dp),
                color = category.color.copy(alpha = 0.06f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                category.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = category.color
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = category.color.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "${category.foods.size} foods",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = category.color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = category.color.copy(alpha = 0.6f)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            HorizontalDivider(color = category.color.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 8.dp))

                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Food Item",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Calories",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            category.foods.forEachIndexed { index, (name, cal) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = category.color.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            cal,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = category.color,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (index < category.foods.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Values are approximate and may vary by brand, cooking method, and exact size. Use a food scale for accuracy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp, lineHeight = 14.sp
                )
            }
        }
    }
}

// ─── SECTION 5: Accurate Tracking Tips ───────────────────────────────────────

@Composable
private fun AccurateTrackingTipsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EduParagraph(
            "Accurate tracking is the foundation of effective calorie management. Here's how to make your logs as reliable as possible:"
        )

        val tips = listOf(
            Triple("⚖️", "Use a Food Scale When Possible",
                "Volume measurements (cups, tablespoons) are imprecise. A food scale measuring in grams provides much more accurate calorie counts. Even a 20% variation in portion size can mean 100-200 extra calories per meal."),
            Triple("🫒", "Don't Forget Cooking Oils and Sauces",
                "A single tablespoon of olive oil adds ~120 calories. Butter, sauces, dressings, and condiments are among the most commonly forgotten items. They add up quickly and are often the reason people \"can't figure out why they're not losing weight.\""),
            Triple("🥤", "Track All Beverages",
                "Liquid calories are often completely overlooked:\n• Juice: ~110 kcal/cup\n• Coffee with cream & sugar: ~50-150 kcal\n• Smoothies: 200-600 kcal\n• Alcohol: 100-250 kcal/drink\n\nDrink water or unsweetened beverages to reduce hidden calories."),
            Triple("📊", "Be Honest With Yourself",
                "Research consistently shows people underreport food intake by 20-50%. Common mistakes:\n• Estimating smaller portions than actual\n• Forgetting snacks and tastes while cooking\n• Not counting 'just a few' chips or bites\n\nAccurate tracking (even if imperfect) is far more valuable than inaccurate tracking."),
            Triple("🔄", "Consistency Beats Perfection",
                "You don't need a perfect day every day. Consistent, honest tracking that's occasionally off is more useful than sporadic perfect tracking. Aim for 80-90% accuracy consistently rather than 100% occasionally."),
            Triple("🍽️", "Weigh Food Before Cooking",
                "Cooking changes the weight of food. Meat shrinks as it cooks (water loss). Pasta and rice absorb water and expand. Always log the raw weight or check the label for 'cooked' values explicitly."),
            Triple("📱", "Log in Real Time",
                "The longer you wait to log a meal, the less accurate your memory becomes. Log food as you eat it or immediately after. 'I'll remember later' often leads to forgotten snacks and inaccurate estimations."),
            Triple("🎯", "Focus on Trends, Not Single Days",
                "One day of overeating won't derail progress. One day of under-eating won't fix a week of excess. Focus on weekly averages rather than obsessing over individual days.")
        )

        tips.forEach { (emoji, title, text) ->
            EduHighlight(emoji = emoji, title = title, text = text, color = Color(0xFF9C27B0))
        }
    }
}

// ─── SHARED HELPERS ──────────────────────────────────────────────────────────

@Composable
private fun EduParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        lineHeight = 22.sp
    )
}

@Composable
private fun EduHighlight(emoji: String, title: String, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MindfulNoteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f)
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Text("🌱", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "A Mindful Approach to Tracking",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Calorie tracking is a tool, not a lifestyle. Use it to learn about food, build awareness, and develop intuition. The goal is to eventually eat well naturally — without needing to count every calorie. If tracking becomes stressful or obsessive, take a break. Your mental health matters as much as your physical health.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp, fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerEducation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.06f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.MedicalInformation, null,
                tint = Color(0xFFF44336).copy(alpha = 0.7f), modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF44336).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "This educational content is for informational purposes only and does not constitute medical advice. Individual calorie needs vary. Consult a registered dietitian or healthcare provider before significantly changing your diet, especially if you have any medical conditions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336).copy(alpha = 0.7f),
                    fontSize = 11.sp, lineHeight = 16.sp
                )
            }
        }
    }
}
