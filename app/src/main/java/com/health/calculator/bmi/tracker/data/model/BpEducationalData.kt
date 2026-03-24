package com.health.calculator.bmi.tracker.data.model

data class BpEducationalSection(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val content: List<BpEducationalItem>
)

sealed class BpEducationalItem {
    data class Paragraph(val text: String) : BpEducationalItem()
    data class Heading(val text: String) : BpEducationalItem()
    data class BulletPoint(val icon: String, val title: String, val description: String) : BpEducationalItem()
    data class NumberedStep(val number: Int, val icon: String, val title: String, val description: String) : BpEducationalItem()
    data class HighlightBox(val emoji: String, val title: String, val text: String, val type: HighlightType) : BpEducationalItem()
    data class ComparisonRow(val label: String, val include: String, val avoid: String) : BpEducationalItem()
    data class MythBuster(val myth: String, val fact: String) : BpEducationalItem()
    data class Analogy(val emoji: String, val text: String) : BpEducationalItem()
    data class DividerItem(val label: String = "") : BpEducationalItem()
}

enum class HighlightType {
    INFO, WARNING, SUCCESS, TIP, DANGER
}

object BpEducationalContent {

    fun getAllSections(): List<BpEducationalSection> = listOf(
        getUnderstandingBP(),
        getHowToMeasure(),
        getRiskFactors(),
        getDashDiet(),
        getBpMyths()
    )

    // ─── Section 1: Understanding Blood Pressure ───────────────────────────

    private fun getUnderstandingBP() = BpEducationalSection(
        id = "understanding_bp",
        emoji = "🫀",
        title = "Understanding Blood Pressure",
        subtitle = "What the numbers mean and why they matter",
        content = listOf(
            BpEducationalItem.Analogy(
                "🏠",
                "Think of your circulatory system like a home plumbing system. Your heart is the pump, your arteries are the pipes, and blood pressure is the water pressure in those pipes. Too much pressure can damage the pipes over time — just like high blood pressure can damage your arteries."
            ),

            BpEducationalItem.Heading("What Do the Numbers Mean?"),

            BpEducationalItem.Paragraph(
                "Blood pressure is recorded as two numbers written as a fraction, like 120/80 mmHg (read as \"120 over 80\"). Each number tells you something different about your heart and blood vessels."
            ),

            BpEducationalItem.BulletPoint(
                "❤️",
                "Systolic (Top Number)",
                "This measures the pressure in your arteries when your heart BEATS and pushes blood out. It's the higher number because this is when the most force is being applied. Think of it as the 'push' pressure."
            ),

            BpEducationalItem.BulletPoint(
                "💙",
                "Diastolic (Bottom Number)",
                "This measures the pressure in your arteries when your heart RESTS between beats. It's the lower number because the heart is relaxing. Think of it as the 'resting' pressure. Even between beats, there should be some pressure to keep blood flowing."
            ),

            BpEducationalItem.BulletPoint(
                "📏",
                "mmHg (Millimeters of Mercury)",
                "This is the unit of measurement. It comes from early blood pressure devices that used a column of mercury. Even though modern devices are digital, we still use this unit."
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("The Heart Pump Analogy"),

            BpEducationalItem.Analogy(
                "💪",
                "Imagine squeezing a water balloon connected to a garden hose. When you SQUEEZE (systolic), water pressure in the hose goes up. When you RELEASE (diastolic), the pressure drops but doesn't go to zero because the balloon still has some tension. Your heart works the same way — each squeeze sends a wave of pressure through your arteries."
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("Why Both Numbers Matter"),

            BpEducationalItem.Paragraph(
                "Both systolic and diastolic pressures are important indicators of cardiovascular health:"
            ),

            BpEducationalItem.BulletPoint(
                "📈",
                "High Systolic",
                "Indicates your heart is working too hard to push blood, or your arteries are too stiff. This is especially concerning as we age and is the primary driver of stroke risk."
            ),

            BpEducationalItem.BulletPoint(
                "📊",
                "High Diastolic",
                "Suggests your blood vessels aren't relaxing properly between heartbeats. More common in younger adults and indicates increased resistance in smaller blood vessels."
            ),

            BpEducationalItem.HighlightBox(
                "💡",
                "Did You Know?",
                "Your blood pressure changes throughout the day. It's typically lowest during sleep and rises in the morning. Stress, physical activity, caffeine, and even the temperature can cause temporary changes. This is why consistent measurement conditions are important.",
                HighlightType.INFO
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("The Pulse Pressure Connection"),

            BpEducationalItem.Paragraph(
                "The difference between your systolic and diastolic numbers is called 'pulse pressure.' A healthy pulse pressure is typically 40-60 mmHg. A wide pulse pressure (>60) may indicate stiff arteries, while a narrow one (<40) might suggest reduced heart function."
            ),

            BpEducationalItem.HighlightBox(
                "🎯",
                "The Goal",
                "For most adults, the goal is to maintain blood pressure below 120/80 mmHg. However, your personal target may differ based on your age, health conditions, and other factors. Always discuss your target with your healthcare provider.",
                HighlightType.SUCCESS
            )
        )
    )

    // ─── Section 2: How to Measure BP Correctly ───────────────────────────

    private fun getHowToMeasure() = BpEducationalSection(
        id = "how_to_measure",
        emoji = "📋",
        title = "How to Measure BP Correctly",
        subtitle = "Step-by-step guide for accurate readings",
        content = listOf(
            BpEducationalItem.HighlightBox(
                "⚠️",
                "Why Accuracy Matters",
                "An incorrect measurement can lead to unnecessary worry or, worse, a missed diagnosis. Following these steps ensures your readings are reliable and useful for tracking your health.",
                HighlightType.WARNING
            ),

            BpEducationalItem.Heading("Before You Measure"),

            BpEducationalItem.NumberedStep(
                1, "🚫",
                "Avoid These 30 Minutes Before",
                "Don't consume caffeine, exercise, or smoke for at least 30 minutes before taking a reading. All of these can temporarily raise your blood pressure and give you an inaccurate result."
            ),

            BpEducationalItem.NumberedStep(
                2, "🚽",
                "Empty Your Bladder",
                "A full bladder can increase your blood pressure by up to 10-15 mmHg. Use the bathroom before measuring."
            ),

            BpEducationalItem.NumberedStep(
                3, "🪑",
                "Sit and Relax for 5 Minutes",
                "Sit in a comfortable chair with your back supported. Place both feet flat on the floor (don't cross your legs). Rest quietly for 5 minutes. This allows your body to reach a baseline state."
            ),

            BpEducationalItem.DividerItem("During Measurement"),

            BpEducationalItem.NumberedStep(
                4, "💪",
                "Position Your Arm Correctly",
                "Rest your arm on a flat surface (like a table) so that the cuff on your upper arm is at the same level as your heart. Roll up your sleeve – don't measure over clothing. Use the same arm each time (left arm is generally recommended)."
            ),

            BpEducationalItem.NumberedStep(
                5, "⌚",
                "Apply the Cuff Properly",
                "Place the cuff on your bare upper arm, about 1 inch (2.5 cm) above the elbow crease. The cuff should be snug but you should be able to slip two fingers underneath. Make sure the tubing runs down the center of your inner arm."
            ),

            BpEducationalItem.NumberedStep(
                6, "🤫",
                "Stay Still and Quiet",
                "Don't talk, text, or move during the measurement. Even small movements or talking can raise your reading by 5-10 mmHg. Breathe normally and try to relax."
            ),

            BpEducationalItem.NumberedStep(
                7, "📝",
                "Take Multiple Readings",
                "Take 2-3 readings, waiting at least 1 minute between each. Discard the first reading (it's often higher) and average the remaining readings. This is the medically recommended approach for the most accurate result."
            ),

            BpEducationalItem.DividerItem("Best Practices"),

            BpEducationalItem.NumberedStep(
                8, "⏰",
                "Measure at the Same Time Daily",
                "Blood pressure fluctuates throughout the day. For consistent tracking, measure at the same times each day. Morning (within 1 hour of waking, before medication) and evening (before bed) are ideal."
            ),

            BpEducationalItem.NumberedStep(
                9, "📊",
                "Keep a Log",
                "Record every reading with the date, time, and any relevant notes (like 'after exercise' or 'felt stressed'). This log is invaluable for your doctor. This app does this automatically for you!"
            ),

            BpEducationalItem.HighlightBox(
                "✅",
                "Pro Tip: The Rule of 3",
                "Medical professionals often recommend the 'Rule of 3': Measure at 3 different times of day, on 3 different days, taking 3 readings each time. The average of all these readings gives the most accurate picture of your true blood pressure.",
                HighlightType.TIP
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("Common Measurement Errors"),

            BpEducationalItem.BulletPoint(
                "❌",
                "Wrong Cuff Size",
                "A cuff that's too small gives falsely high readings; too large gives falsely low readings. Most home monitors come with standard cuffs — check if your arm circumference requires a different size."
            ),

            BpEducationalItem.BulletPoint(
                "❌",
                "Measuring Over Clothing",
                "Even a thin shirt sleeve can add 5-50 mmHg to your reading. Always measure on bare skin."
            ),

            BpEducationalItem.BulletPoint(
                "❌",
                "Unsupported Back or Feet",
                "Sitting without back support can raise systolic BP by 5-10 mmHg. Dangling feet can raise it by 5-6 mmHg. Crossed legs can add another 2-8 mmHg."
            ),

            BpEducationalItem.BulletPoint(
                "❌",
                "Arm Below Heart Level",
                "If your arm hangs at your side or rests on your lap, the reading can be 10+ mmHg higher than if properly positioned at heart level."
            )
        )
    )

    // ─── Section 3: Risk Factors ───────────────────────────────────────────

    private fun getRiskFactors() = BpEducationalSection(
        id = "risk_factors",
        emoji = "⚡",
        title = "Risk Factors for High BP",
        subtitle = "What you can and can't change",
        content = listOf(
            BpEducationalItem.Paragraph(
                "Understanding your risk factors helps you take control of what you can change and be vigilant about what you can't. Having risk factors doesn't mean you'll develop high blood pressure — but awareness is the first step to prevention."
            ),

            BpEducationalItem.Heading("🔧 Modifiable Risk Factors"),
            BpEducationalItem.Paragraph("These are factors YOU can change through lifestyle choices:"),

            BpEducationalItem.BulletPoint(
                "🧂",
                "High Sodium Diet",
                "Excess sodium causes your body to retain water, increasing blood volume and pressure. The average person consumes 3,400 mg/day — nearly 50% more than the recommended limit of 2,300 mg. Aim for 1,500 mg if you already have high BP."
            ),

            BpEducationalItem.BulletPoint(
                "⚖️",
                "Excess Weight",
                "Being overweight increases the workload on your heart. For every 2 lbs (1 kg) lost, blood pressure can drop by about 1 mmHg. Even losing 5-10% of body weight can significantly improve BP."
            ),

            BpEducationalItem.BulletPoint(
                "🛋️",
                "Physical Inactivity",
                "Sedentary people have a 20-50% higher risk of developing hypertension. Regular exercise strengthens the heart so it can pump more blood with less effort, reducing the force on arteries."
            ),

            BpEducationalItem.BulletPoint(
                "😰",
                "Chronic Stress",
                "Stress hormones constrict blood vessels and make the heart beat faster. While temporary stress spikes are normal, chronic stress can contribute to sustained high BP. It also often leads to unhealthy coping behaviors."
            ),

            BpEducationalItem.BulletPoint(
                "🍷",
                "Excessive Alcohol",
                "More than 2 drinks daily for men or 1 for women can raise blood pressure. Heavy drinking can also reduce the effectiveness of BP medications. Moderate consumption (or none) is recommended."
            ),

            BpEducationalItem.BulletPoint(
                "🚬",
                "Smoking",
                "Each cigarette temporarily raises BP for several minutes. Chemicals in tobacco damage artery walls, making them narrow and stiff. Secondhand smoke also poses risks."
            ),

            BpEducationalItem.BulletPoint(
                "🍔",
                "Poor Diet",
                "Low potassium, low fiber, high saturated fat, and high sugar diets all contribute to hypertension. The DASH diet (see our DASH Diet section) is specifically designed to combat this."
            ),

            BpEducationalItem.BulletPoint(
                "😴",
                "Poor Sleep",
                "Sleep apnea and chronic sleep deprivation (less than 6 hours per night) are linked to higher blood pressure. Quality sleep allows the cardiovascular system to recover."
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("🔒 Non-Modifiable Risk Factors"),
            BpEducationalItem.Paragraph("These factors are outside your control, but knowing them helps you stay vigilant:"),

            BpEducationalItem.BulletPoint(
                "📅",
                "Age",
                "Blood pressure tends to increase with age. The risk rises significantly after 55 for women and 45 for men. Arterial stiffness naturally increases over time, leading to higher systolic pressure."
            ),

            BpEducationalItem.BulletPoint(
                "🧬",
                "Genetics & Family History",
                "If your parents or siblings have hypertension, your risk is higher. Genetic factors can affect how your kidneys handle sodium, how your blood vessels respond to stress, and your hormonal regulation of blood pressure."
            ),

            BpEducationalItem.BulletPoint(
                "🌍",
                "Race & Ethnicity",
                "Hypertension is more prevalent and often more severe in people of African descent. It also tends to develop earlier and respond differently to certain medications. South Asian populations also face elevated risk."
            ),

            BpEducationalItem.BulletPoint(
                "👤",
                "Sex",
                "Men are more likely to develop high BP before age 55. After menopause, women's risk increases and may eventually exceed men's risk. Hormonal changes play a significant role."
            ),

            BpEducationalItem.BulletPoint(
                "🏥",
                "Chronic Conditions",
                "Kidney disease, diabetes, sleep apnea, and certain hormonal disorders can cause or worsen hypertension. These conditions may require specialized treatment approaches."
            ),

            BpEducationalItem.HighlightBox(
                "💡",
                "The Good News",
                "Even if you have non-modifiable risk factors, managing the modifiable ones can significantly reduce your overall risk. Many people with genetic predisposition successfully maintain normal blood pressure through healthy lifestyle choices.",
                HighlightType.SUCCESS
            )
        )
    )

    // ─── Section 4: DASH Diet ──────────────────────────────────────────────

    private fun getDashDiet() = BpEducationalSection(
        id = "dash_diet",
        emoji = "🥗",
        title = "DASH Diet Overview",
        subtitle = "Dietary Approaches to Stop Hypertension",
        content = listOf(
            BpEducationalItem.Paragraph(
                "The DASH diet (Dietary Approaches to Stop Hypertension) is a scientifically proven eating plan developed by the National Heart, Lung, and Blood Institute. Studies show it can lower blood pressure by 8-14 mmHg in just 2 weeks — comparable to some medications."
            ),

            BpEducationalItem.HighlightBox(
                "🏆",
                "Proven Results",
                "The DASH diet has been ranked the #1 Best Diet Overall by U.S. News & World Report multiple years running. It's not a fad — it's a sustainable, evidence-based eating pattern.",
                HighlightType.SUCCESS
            ),

            BpEducationalItem.Heading("Key Principles"),

            BpEducationalItem.BulletPoint(
                "🥬",
                "More Fruits & Vegetables",
                "Aim for 4-5 servings of each daily. They're rich in potassium, magnesium, and fiber — all of which help lower blood pressure. Fresh, frozen, and canned (no salt added) all count."
            ),

            BpEducationalItem.BulletPoint(
                "🌾",
                "Whole Grains",
                "6-8 servings daily. Choose whole wheat bread, brown rice, oatmeal, and whole grain pasta over refined grains. Whole grains provide fiber and nutrients that support cardiovascular health."
            ),

            BpEducationalItem.BulletPoint(
                "🥛",
                "Low-Fat Dairy",
                "2-3 servings daily. Low-fat milk, yogurt, and cheese provide calcium and vitamin D. Studies show dairy's calcium specifically helps regulate blood pressure."
            ),

            BpEducationalItem.BulletPoint(
                "🐟",
                "Lean Proteins",
                "Up to 6 servings daily. Choose fish (especially fatty fish like salmon), skinless poultry, and plant proteins like beans and lentils. Limit red meat to 1-2 times per week."
            ),

            BpEducationalItem.BulletPoint(
                "🥜",
                "Nuts, Seeds & Legumes",
                "4-5 servings per week. Almonds, walnuts, sunflower seeds, kidney beans, and lentils are excellent choices. They provide magnesium, potassium, and healthy fats."
            ),

            BpEducationalItem.BulletPoint(
                "🫒",
                "Healthy Fats",
                "2-3 servings daily. Use olive oil, avocado, and canola oil. Limit saturated fat to less than 6% of calories. Avoid trans fats entirely."
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("Foods to Limit or Avoid"),

            BpEducationalItem.BulletPoint(
                "🧂",
                "Sodium",
                "Standard DASH: Less than 2,300 mg/day. Low-sodium DASH: Less than 1,500 mg/day. The low-sodium version can lower BP by an additional 3-4 mmHg."
            ),

            BpEducationalItem.BulletPoint(
                "🍬",
                "Added Sugars",
                "Limit to 5 or fewer servings of sweets per week. Choose fruit for sweetness instead. Sugar-sweetened beverages are a major hidden source."
            ),

            BpEducationalItem.BulletPoint(
                "🥩",
                "Red & Processed Meats",
                "Limit red meat. Avoid processed meats like bacon, sausage, and deli meats which are very high in sodium and saturated fat."
            ),

            BpEducationalItem.BulletPoint(
                "🍟",
                "Processed & Fast Foods",
                "These are typically loaded with sodium, unhealthy fats, and empty calories. A single fast food meal can contain 2,000+ mg of sodium."
            ),

            BpEducationalItem.DividerItem(),

            BpEducationalItem.Heading("Sample DASH Day"),

            BpEducationalItem.HighlightBox(
                "🌅",
                "Breakfast",
                "Oatmeal with berries and walnuts, low-fat yogurt, orange juice (low sodium)",
                HighlightType.TIP
            ),

            BpEducationalItem.HighlightBox(
                "☀️",
                "Lunch",
                "Whole wheat turkey wrap with spinach and avocado, apple, low-fat milk",
                HighlightType.TIP
            ),

            BpEducationalItem.HighlightBox(
                "🌆",
                "Dinner",
                "Baked salmon, brown rice, steamed broccoli with olive oil, mixed green salad",
                HighlightType.TIP
            ),

            BpEducationalItem.HighlightBox(
                "🍎",
                "Snacks",
                "Handful of unsalted almonds, carrot sticks with hummus, banana",
                HighlightType.TIP
            ),

            BpEducationalItem.HighlightBox(
                "💡",
                "Getting Started",
                "You don't have to change everything at once. Start by adding one extra serving of fruits or vegetables daily, then gradually incorporate more DASH principles over several weeks. Small, consistent changes are more sustainable than dramatic overhauls.",
                HighlightType.INFO
            )
        )
    )

    // ─── Section 5: BP Myths ───────────────────────────────────────────────

    private fun getBpMyths() = BpEducationalSection(
        id = "bp_myths",
        emoji = "🔍",
        title = "Blood Pressure Myths",
        subtitle = "Separating fact from fiction",
        content = listOf(
            BpEducationalItem.Paragraph(
                "Misinformation about blood pressure is widespread and can be dangerous. Let's separate fact from fiction on the most common myths."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"I feel fine, so my blood pressure must be fine.\"",
                fact = "High blood pressure is called the 'silent killer' for a reason — it usually has NO symptoms until it causes serious damage. Most people with hypertension feel perfectly normal. The only way to know your blood pressure is to measure it. By the time symptoms appear (severe headaches, vision problems, chest pain), significant damage may have already occurred to your heart, kidneys, brain, or eyes."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"Only old people get high blood pressure.\"",
                fact = "While the risk increases with age, hypertension affects people of ALL ages. About 1 in 4 adults aged 20-44 has elevated blood pressure. Childhood obesity, sedentary lifestyles, high-sodium diets, and stress are causing hypertension to appear in increasingly younger populations. Even children can have high blood pressure."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"I can stop taking my BP medication once my numbers are normal.\"",
                fact = "NEVER stop or adjust blood pressure medication without consulting your doctor. Your BP is normal BECAUSE the medication is working. Stopping abruptly can cause a dangerous rebound spike in blood pressure. If your doctor agrees your BP is well-controlled, they may gradually reduce your dose — but this must be done under medical supervision."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"Salt is the only dietary factor that affects blood pressure.\"",
                fact = "While sodium is important, many other dietary factors influence blood pressure. Potassium deficiency, low calcium and magnesium intake, excessive alcohol, high sugar consumption, and not enough fiber all contribute. The DASH diet addresses all of these factors, not just sodium."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"If high blood pressure ran in my family, there's nothing I can do.\"",
                fact = "Genetics is just one risk factor. Even with a strong family history, lifestyle changes can significantly reduce your risk or delay onset. Regular exercise, a healthy diet, maintaining a healthy weight, limiting alcohol, and managing stress can lower blood pressure by 10-20+ mmHg — often more than a single medication."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"Home blood pressure monitors aren't accurate.\"",
                fact = "Modern validated home blood pressure monitors are very accurate and are actually recommended by medical professionals. Home readings can be MORE representative of your true blood pressure than clinic readings because they're taken in your natural environment, avoiding 'white coat syndrome.' The key is to use a validated, upper-arm monitor and follow proper measurement technique."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"Red wine is good for blood pressure.\"",
                fact = "While moderate red wine consumption has been associated with some cardiovascular benefits (likely from antioxidants), alcohol — including wine — actually RAISES blood pressure. The risks of alcohol on blood pressure outweigh any potential benefits. If you don't drink, there's no reason to start for heart health. Better alternatives include grape juice and berries."
            ),

            BpEducationalItem.MythBuster(
                myth = "\"Drinking lots of water will lower my blood pressure.\"",
                fact = "Staying hydrated is important for overall health, but drinking extra water won't directly lower blood pressure. However, dehydration CAN temporarily raise blood pressure, so adequate hydration helps maintain stable readings. The best approach is to drink water consistently throughout the day — about 8 glasses — rather than relying on water as a blood pressure treatment."
            ),

            BpEducationalItem.HighlightBox(
                "🎯",
                "The Bottom Line",
                "The most dangerous myth of all is that high blood pressure isn't serious. Uncontrolled hypertension is a leading cause of heart attack, stroke, kidney failure, and vision loss worldwide. Knowledge is power — now that you know the facts, you can take informed action to protect your health.",
                HighlightType.WARNING
            )
        )
    )
}
