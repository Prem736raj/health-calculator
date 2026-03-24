// File: com/health/calculator/bmi/tracker/data/model/BMREducationalContent.kt
package com.health.calculator.bmi.tracker.data.model

data class EducationalSection(
    val title: String,
    val emoji: String,
    val content: List<EducationalParagraph>
)

data class EducationalParagraph(
    val text: String,
    val isBullet: Boolean = false,
    val isHighlight: Boolean = false,
    val isTip: Boolean = false,
    val isWarning: Boolean = false
)

object BMREducationalContent {

    val sections = listOf(
        EducationalSection(
            title = "What is BMR?",
            emoji = "🔥",
            content = listOf(
                EducationalParagraph(
                    text = "Basal Metabolic Rate (BMR) is the number of calories your body needs to perform its most basic life-sustaining functions — like breathing, blood circulation, cell production, and nutrient processing."
                ),
                EducationalParagraph(
                    text = "Think of it as the energy cost of simply being alive, even if you stayed in bed all day and didn't move at all."
                ),
                EducationalParagraph(
                    text = "BMR typically accounts for 60-75% of your total daily calorie burn, making it the single largest component of your energy expenditure.",
                    isHighlight = true
                ),
                EducationalParagraph(
                    text = "Your BMR is measured under very strict conditions: complete rest, a thermally neutral environment, and after a 12-hour fast."
                )
            )
        ),
        EducationalSection(
            title = "BMR vs TDEE",
            emoji = "⚡",
            content = listOf(
                EducationalParagraph(
                    text = "BMR and TDEE are related but different:"
                ),
                EducationalParagraph(
                    text = "BMR (Basal Metabolic Rate) — Calories burned at complete rest. This is your body's minimum energy requirement.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "TDEE (Total Daily Energy Expenditure) — Your total calories burned in a day, including all activity. TDEE = BMR + Physical Activity + Thermic Effect of Food.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "For weight management, TDEE is more practical because it reflects your actual daily calorie needs. Eat below your TDEE to lose weight, at TDEE to maintain, and above TDEE to gain.",
                    isHighlight = true
                ),
                EducationalParagraph(
                    text = "Example: If your BMR is 1,500 kcal and you're moderately active, your TDEE might be around 2,325 kcal (BMR × 1.55)."
                )
            )
        ),
        EducationalSection(
            title = "Factors Affecting BMR",
            emoji = "🧬",
            content = listOf(
                EducationalParagraph(
                    text = "Several factors influence your metabolic rate:"
                ),
                EducationalParagraph(
                    text = "Body Size & Composition — Larger bodies and more muscle mass require more energy. Muscle burns more calories at rest than fat tissue.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Age — BMR decreases approximately 1-2% per decade after age 20, mainly due to loss of muscle mass.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Gender — Males typically have higher BMR due to greater muscle mass and lower body fat percentage.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Genetics — Your genetic makeup can influence metabolic rate by up to 5-10%.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Hormones — Thyroid hormones play a crucial role. Conditions like hypothyroidism can significantly lower BMR.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Body Temperature — A 1°C increase in body temperature can raise BMR by about 7%.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Climate — Living in very cold or very hot environments can slightly increase BMR as your body works to maintain temperature.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Pregnancy & Lactation — BMR increases during pregnancy and breastfeeding to support the growing baby.",
                    isBullet = true
                )
            )
        ),
        EducationalSection(
            title = "How to Boost Your Metabolism",
            emoji = "🚀",
            content = listOf(
                EducationalParagraph(
                    text = "While you can't dramatically change your BMR, these evidence-based strategies can help:"
                ),
                EducationalParagraph(
                    text = "Build Muscle — Strength training is the most effective way to boost BMR. Each kg of muscle burns about 13 kcal/day at rest, vs. 4.5 kcal/day for fat.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Stay Active Throughout the Day — NEAT (Non-Exercise Activity Thermogenesis) like walking, standing, and fidgeting can burn 200-350 extra calories daily.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Eat Enough Protein — Protein has the highest thermic effect (20-35%), meaning your body burns more calories digesting it. Aim for 1.6-2.2g per kg of bodyweight.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Get Quality Sleep — Poor sleep can reduce BMR by 5-20%. Aim for 7-9 hours of quality sleep each night.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Stay Hydrated — Drinking cold water can temporarily boost metabolism by 24-30% for about an hour. Aim for 2-3 liters daily.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Don't Skip Meals — Regular eating patterns help maintain stable metabolic function. Extreme fasting can lower BMR.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "Manage Stress — Chronic stress increases cortisol, which can promote fat storage and negatively impact metabolism.",
                    isTip = true
                )
            )
        ),
        EducationalSection(
            title = "Why Crash Diets Harm Your BMR",
            emoji = "⚠️",
            content = listOf(
                EducationalParagraph(
                    text = "Extreme calorie restriction (crash diets) can significantly damage your metabolism. Here's why:",
                    isWarning = true
                ),
                EducationalParagraph(
                    text = "Adaptive Thermogenesis — When you drastically cut calories, your body adapts by slowing down metabolism to conserve energy. This can reduce BMR by 15-25%.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Muscle Loss — Crash diets cause your body to break down muscle for energy. Less muscle means a permanently lower BMR.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Yo-Yo Effect — After the diet ends, your lowered BMR means you burn fewer calories. Eating normally again leads to rapid weight regain, often more than was lost.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Hormonal Disruption — Extreme restriction affects thyroid hormones, leptin, and ghrelin, making it harder to maintain weight loss.",
                    isBullet = true
                ),
                EducationalParagraph(
                    text = "Instead of crash dieting, aim for a moderate deficit of 300-500 calories below your TDEE. This preserves muscle mass and keeps your metabolism healthy.",
                    isHighlight = true
                ),
                EducationalParagraph(
                    text = "Never eat below your BMR for extended periods without medical supervision.",
                    isWarning = true
                )
            )
        ),
        EducationalSection(
            title = "BMR and Weight Management",
            emoji = "📊",
            content = listOf(
                EducationalParagraph(
                    text = "Understanding your BMR is the foundation of smart weight management:"
                ),
                EducationalParagraph(
                    text = "For Weight Loss — Create a moderate calorie deficit (250-500 kcal below TDEE). This results in 0.25-0.5 kg loss per week, which is sustainable and healthy.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "For Weight Gain — Eat 250-500 kcal above TDEE with adequate protein (1.6-2.2g/kg) and strength training to build lean muscle rather than just fat.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "For Maintenance — Eat at your TDEE. Track your weight weekly and adjust calories if you notice unintended changes.",
                    isTip = true
                ),
                EducationalParagraph(
                    text = "The Golden Rule: Never eat below your BMR regularly. Your body needs at least this many calories to function properly.",
                    isHighlight = true
                ),
                EducationalParagraph(
                    text = "Track regularly — Recalculate your BMR every few months or when your weight changes significantly (±5 kg). Your BMR changes as your body changes."
                ),
                EducationalParagraph(
                    text = "Remember: BMR is an estimate. Individual variation of 5-10% is normal. Use it as a starting point and adjust based on real-world results."
                )
            )
        )
    )
}
