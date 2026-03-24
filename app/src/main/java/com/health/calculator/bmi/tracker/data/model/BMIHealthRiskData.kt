package com.health.calculator.bmi.tracker.data.model

data class HealthRiskInfo(
    val category: String,
    val categoryIcon: String,
    val toneMessage: String,
    val riskLevel: RiskLevel,
    val healthRisks: List<HealthRiskItem>,
    val recommendations: List<RecommendationItem>,
    val actionSteps: List<String>,
    val doctorNote: String
)

data class HealthRiskItem(
    val icon: String,
    val title: String,
    val description: String,
    val severity: RiskSeverity
)

data class RecommendationItem(
    val icon: String,
    val title: String,
    val description: String
)

enum class RiskLevel(val label: String, val emoji: String) {
    LOW("Low Risk", "🟢"),
    MODERATE("Moderate Risk", "🟡"),
    HIGH("High Risk", "🟠"),
    VERY_HIGH("Very High Risk", "🔴"),
    EXTREMELY_HIGH("Extremely High Risk", "🔴")
}

enum class RiskSeverity {
    MILD, MODERATE, HIGH, SEVERE
}

object BMIHealthRiskProvider {

    fun getHealthRiskInfo(bmi: Float, age: Int, isMale: Boolean): HealthRiskInfo {
        return when {
            bmi < 16f -> getSevereThinness(age, isMale)
            bmi < 17f -> getModerateThinness(age, isMale)
            bmi < 18.5f -> getMildThinness(age, isMale)
            bmi < 25f -> getNormal(bmi, age, isMale)
            bmi < 30f -> getOverweight(bmi, age, isMale)
            bmi < 35f -> getObeseClassI(age, isMale)
            bmi < 40f -> getObeseClassII(age, isMale)
            else -> getObeseClassIII(age, isMale)
        }
    }

    private fun getSevereThinness(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Severe Thinness",
            categoryIcon = "⚠️",
            toneMessage = "Your health matters to us. Being significantly underweight " +
                    "can affect your body's ability to function well. The good news is " +
                    "that with the right support, you can work toward a healthier weight. " +
                    "We'd strongly encourage you to reach out to a healthcare provider.",
            riskLevel = RiskLevel.VERY_HIGH,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🛡️",
                    title = "Weakened Immune System",
                    description = "Your body may have reduced ability to fight infections " +
                            "and recover from illness, making you more susceptible to disease.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🦴",
                    title = "Significant Bone Loss Risk",
                    description = "Severely low weight can lead to reduced bone density, " +
                            "increasing the risk of fractures and osteoporosis.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🫀",
                    title = "Heart & Organ Stress",
                    description = "Vital organs may not receive adequate nutrition, potentially " +
                            "leading to heart rhythm irregularities and organ function issues.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🧠",
                    title = "Cognitive & Energy Impact",
                    description = "Severe underweight can affect concentration, mood, and " +
                            "energy levels due to insufficient nutrient intake.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "Nutritional Deficiencies",
                    description = "Risk of anemia, vitamin deficiencies, and electrolyte " +
                            "imbalances that can affect multiple body systems.",
                    severity = RiskSeverity.SEVERE
                ),
                if (!isMale) HealthRiskItem(
                    icon = "👶",
                    title = "Fertility & Hormonal Impact",
                    description = "Significantly low weight can disrupt menstrual cycles " +
                            "and affect reproductive health and hormonal balance.",
                    severity = RiskSeverity.HIGH
                ) else HealthRiskItem(
                    icon = "💪",
                    title = "Muscle Wasting",
                    description = "The body may break down muscle tissue for energy, " +
                            "leading to weakness and reduced physical capacity.",
                    severity = RiskSeverity.HIGH
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "👨‍⚕️",
                    title = "Seek Medical Guidance",
                    description = "Please consult a healthcare provider or registered " +
                            "dietitian for a personalized nutrition and recovery plan."
                ),
                RecommendationItem(
                    icon = "🥑",
                    title = "Nutrient-Dense Foods",
                    description = "Focus on calorie-rich, nutritious foods like nuts, " +
                            "avocados, whole grains, lean proteins, and healthy oils."
                ),
                RecommendationItem(
                    icon = "🍽️",
                    title = "Gradual Increase",
                    description = "Increase calorie intake gradually — about 300-500 " +
                            "extra calories per day to gain weight safely."
                ),
                RecommendationItem(
                    icon = "🤝",
                    title = "Build a Support System",
                    description = "Consider working with a nutritionist and having " +
                            "supportive people around you during your journey."
                )
            ),
            actionSteps = listOf(
                "Schedule an appointment with your doctor this week",
                "Start a food journal to track your daily intake",
                "Add one extra nutrient-dense snack per day",
                "Consider a multivitamin (with medical advice)",
                "Set small, achievable weekly weight goals"
            ),
            doctorNote = "With a BMI in the severely underweight range, we strongly " +
                    "recommend consulting a healthcare provider as soon as possible. " +
                    "They can check for underlying conditions and create a safe plan " +
                    "to help you reach a healthier weight."
        )
    }

    private fun getModerateThinness(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Moderate Thinness",
            categoryIcon = "⚠️",
            toneMessage = "Being underweight can affect how your body functions day to day. " +
                    "Don't worry — with some mindful nutrition changes and guidance, " +
                    "you can work toward feeling stronger and healthier.",
            riskLevel = RiskLevel.HIGH,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🛡️",
                    title = "Weakened Immune Response",
                    description = "Your immune system may not be performing at its best, " +
                            "meaning you could catch colds and infections more easily.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🦴",
                    title = "Bone Density Concerns",
                    description = "Lower weight is associated with reduced bone mineral " +
                            "density, which may increase fracture risk over time.",
                    severity = RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "⚡",
                    title = "Low Energy & Fatigue",
                    description = "You might experience persistent tiredness and reduced " +
                            "stamina due to insufficient caloric intake.",
                    severity = RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "Nutritional Gaps",
                    description = "Important vitamins and minerals may be lacking, " +
                            "affecting your skin, hair, and overall wellbeing.",
                    severity = RiskSeverity.MODERATE
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "👨‍⚕️",
                    title = "Consult a Professional",
                    description = "A healthcare provider can help identify any underlying " +
                            "causes and create a safe weight gain strategy."
                ),
                RecommendationItem(
                    icon = "🥜",
                    title = "Calorie-Rich Healthy Foods",
                    description = "Include nuts, seeds, olive oil, whole-grain breads, " +
                            "lean meats, and protein-rich dairy in your meals."
                ),
                RecommendationItem(
                    icon = "🏋️",
                    title = "Strength Training",
                    description = "Light resistance exercises can help build muscle mass " +
                            "and stimulate healthy appetite."
                ),
                RecommendationItem(
                    icon = "📅",
                    title = "Regular Meal Schedule",
                    description = "Eat 5-6 smaller meals throughout the day rather than " +
                            "skipping meals or eating only 1-2 large ones."
                )
            ),
            actionSteps = listOf(
                "Visit your doctor for a health check-up",
                "Add a healthy snack between each main meal",
                "Include protein with every meal",
                "Try light strength training 2-3 times per week",
                "Track your progress weekly — celebrate small wins!"
            ),
            doctorNote = "A BMI in the moderately underweight range warrants a " +
                    "conversation with your healthcare provider to rule out any " +
                    "underlying health conditions and get personalized guidance."
        )
    }

    private fun getMildThinness(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Mild Thinness",
            categoryIcon = "💛",
            toneMessage = "You're slightly below the healthy weight range — but you're " +
                    "close! With a few small adjustments to your nutrition, you can " +
                    "reach a healthier weight. Think of it as fine-tuning, not overhauling.",
            riskLevel = RiskLevel.MODERATE,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🛡️",
                    title = "Slightly Reduced Immunity",
                    description = "Being slightly underweight may modestly affect your " +
                            "body's ability to fight off infections.",
                    severity = RiskSeverity.MILD
                ),
                HealthRiskItem(
                    icon = "⚡",
                    title = "Energy Fluctuations",
                    description = "You might notice occasional dips in energy or " +
                            "stamina, especially during physical activities.",
                    severity = RiskSeverity.MILD
                ),
                HealthRiskItem(
                    icon = "🦴",
                    title = "Long-term Bone Health",
                    description = "Over time, being underweight can contribute to " +
                            "lower bone density, especially important as you age.",
                    severity = RiskSeverity.MILD
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "🥗",
                    title = "Balanced Nutrition",
                    description = "Focus on eating balanced meals with good sources of " +
                            "protein, healthy fats, and complex carbohydrates."
                ),
                RecommendationItem(
                    icon = "🥤",
                    title = "Healthy Calorie Boost",
                    description = "Add smoothies, trail mix, or nut butter to boost " +
                            "your daily calorie intake in a tasty way."
                ),
                RecommendationItem(
                    icon = "🏃",
                    title = "Stay Active",
                    description = "Continue or start regular exercise — it helps build " +
                            "muscle and can improve appetite naturally."
                ),
                RecommendationItem(
                    icon = "😴",
                    title = "Prioritize Rest",
                    description = "Quality sleep supports healthy weight maintenance " +
                            "and overall wellbeing."
                )
            ),
            actionSteps = listOf(
                "Add 200-300 extra healthy calories per day",
                "Include a protein-rich snack in the afternoon",
                "Try adding healthy fats like olive oil to meals",
                "Stay physically active with enjoyable exercises",
                "Monitor your weight weekly to track progress"
            ),
            doctorNote = "While mildly underweight, you're close to the healthy range. " +
                    "If you've unintentionally lost weight or have difficulty gaining weight, " +
                    "a quick check-in with your doctor would be a good idea."
        )
    }

    private fun getNormal(bmi: Float, age: Int, isMale: Boolean): HealthRiskInfo {
        val isLowerNormal = bmi < 21.7f
        val positionNote = when {
            bmi < 20f -> "You're in the lower portion of the healthy range."
            bmi < 23f -> "You're right in the sweet spot of the healthy range!"
            else -> "You're in the upper portion of the healthy range."
        }

        return HealthRiskInfo(
            category = "Normal Weight",
            categoryIcon = "🌟",
            toneMessage = "Wonderful! You're at a healthy weight — and that's something " +
                    "to celebrate! 🎉 $positionNote Keep up the great habits that " +
                    "got you here. Your body thanks you!",
            riskLevel = RiskLevel.LOW,
            healthRisks = emptyList(), // No risks for normal!
            recommendations = listOf(
                RecommendationItem(
                    icon = "🥦",
                    title = "Keep Eating Well",
                    description = "Continue your balanced diet rich in fruits, vegetables, " +
                            "whole grains, lean proteins, and healthy fats."
                ),
                RecommendationItem(
                    icon = "🏃",
                    title = "Stay Active",
                    description = "Aim for at least 150 minutes of moderate exercise per " +
                            "week — walking, swimming, cycling, whatever you enjoy!"
                ),
                RecommendationItem(
                    icon = "😴",
                    title = "Quality Sleep",
                    description = "7-9 hours of quality sleep per night helps maintain " +
                            "a healthy weight and supports overall wellbeing."
                ),
                RecommendationItem(
                    icon = "💧",
                    title = "Stay Hydrated",
                    description = "Drink plenty of water throughout the day. It supports " +
                            "every system in your body."
                ),
                RecommendationItem(
                    icon = "🧘",
                    title = "Manage Stress",
                    description = "Chronic stress can affect weight. Practice mindfulness, " +
                            "deep breathing, or activities that bring you joy."
                ),
                RecommendationItem(
                    icon = "🩺",
                    title = "Routine Check-ups",
                    description = "Continue regular health screenings. A healthy BMI is " +
                            "great, but overall health involves many factors."
                )
            ),
            actionSteps = listOf(
                "Keep up your current healthy habits!",
                "Try a new healthy recipe this week",
                "Aim for 10,000 steps a day",
                "Schedule your annual health check-up",
                "Share your healthy lifestyle tips with friends and family"
            ),
            doctorNote = "Your BMI is in the healthy range — great work! Continue with " +
                    "regular check-ups and maintain your balanced lifestyle."
        )
    }

    private fun getOverweight(bmi: Float, age: Int, isMale: Boolean): HealthRiskInfo {
        val nearNormal = bmi < 27f

        return HealthRiskInfo(
            category = "Overweight",
            categoryIcon = "💛",
            toneMessage = if (nearNormal) {
                "You're just slightly above the healthy range — and that's very manageable! " +
                        "Small, consistent changes can make a meaningful difference. " +
                        "You've got this! 💪"
            } else {
                "Your weight is above the recommended range, but the important thing is " +
                        "that you're aware and taking steps to understand your health. " +
                        "With some adjustments, you can move toward a healthier weight. " +
                        "Every positive change counts!"
            },
            riskLevel = if (nearNormal) RiskLevel.MODERATE else RiskLevel.MODERATE,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🫀",
                    title = "Heart Health",
                    description = "Carrying extra weight can increase strain on your heart " +
                            "and raise the risk of cardiovascular issues over time.",
                    severity = if (nearNormal) RiskSeverity.MILD else RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "Blood Sugar Balance",
                    description = "Excess weight can affect how your body processes insulin, " +
                            "increasing the risk of type 2 diabetes.",
                    severity = if (nearNormal) RiskSeverity.MILD else RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "📊",
                    title = "Blood Pressure",
                    description = "Being overweight is associated with higher blood pressure, " +
                            "which can affect your heart and blood vessels.",
                    severity = if (nearNormal) RiskSeverity.MILD else RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "🦵",
                    title = "Joint Comfort",
                    description = "Extra weight puts additional pressure on weight-bearing " +
                            "joints like knees and hips.",
                    severity = RiskSeverity.MILD
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "🚶",
                    title = "Move More Daily",
                    description = "Aim for at least 150 minutes of moderate exercise per week. " +
                            "Start with brisk walking — it's effective and easy to begin!"
                ),
                RecommendationItem(
                    icon = "🥗",
                    title = "Mindful Eating",
                    description = "Focus on portion awareness, more vegetables, lean proteins, " +
                            "and whole grains. Small swaps add up!"
                ),
                RecommendationItem(
                    icon = "🚫",
                    title = "Reduce Processed Foods",
                    description = "Cut back on sugary drinks, fast food, and processed snacks. " +
                            "Replace with whole, natural alternatives."
                ),
                RecommendationItem(
                    icon = "📝",
                    title = "Track Your Progress",
                    description = "Keep a food diary or use this app to monitor your BMI over " +
                            "time. Seeing progress is motivating!"
                ),
                RecommendationItem(
                    icon = "🎯",
                    title = "Set Realistic Goals",
                    description = "Aim to lose 0.5-1 kg per week. Slow, steady progress is " +
                            "more sustainable than crash dieting."
                )
            ),
            actionSteps = listOf(
                "Start a 30-minute daily walk",
                "Replace one sugary drink with water each day",
                "Add an extra serving of vegetables to each meal",
                "Practice eating slowly and mindfully",
                "Set a realistic 3-month weight goal"
            ),
            doctorNote = "Your BMI indicates you're in the overweight range. Consider " +
                    "discussing a healthy weight management plan with your doctor, " +
                    "especially if you have other risk factors like family history of " +
                    "diabetes or heart disease."
        )
    }

    private fun getObeseClassI(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Obese Class I",
            categoryIcon = "🧡",
            toneMessage = "We know this might feel overwhelming, but please know that " +
                    "recognizing where you are is an incredibly brave and important first step. " +
                    "Many people have successfully improved their health from this starting point. " +
                    "You're not alone, and small changes can lead to big improvements.",
            riskLevel = RiskLevel.HIGH,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🫀",
                    title = "Cardiovascular Disease",
                    description = "Obesity significantly increases the risk of heart disease, " +
                            "heart attack, and stroke due to higher cholesterol and blood pressure.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "Type 2 Diabetes",
                    description = "Obesity is a major risk factor for developing type 2 diabetes " +
                            "as excess fat affects insulin resistance.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "📊",
                    title = "High Blood Pressure",
                    description = "The heart must work harder to pump blood through extra " +
                            "tissue, often leading to elevated blood pressure.",
                    severity = RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "😴",
                    title = "Sleep Apnea",
                    description = "Excess weight around the neck can cause breathing pauses " +
                            "during sleep, leading to poor rest and daytime fatigue.",
                    severity = RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "🦵",
                    title = "Joint Problems",
                    description = "Increased stress on joints, especially knees, hips, and " +
                            "lower back, can lead to pain and mobility issues.",
                    severity = RiskSeverity.MODERATE
                ),
                HealthRiskItem(
                    icon = "🧠",
                    title = "Mental Health Impact",
                    description = "Weight-related stress can affect mental wellbeing. " +
                            "Remember, your worth isn't defined by a number.",
                    severity = RiskSeverity.MILD
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "👨‍⚕️",
                    title = "Healthcare Partnership",
                    description = "Work with your doctor to create a structured, sustainable " +
                            "weight management plan tailored to your needs."
                ),
                RecommendationItem(
                    icon = "🚶",
                    title = "Start Moving Gently",
                    description = "Begin with low-impact activities like walking, swimming, " +
                            "or cycling. Even 10 minutes a day makes a difference!"
                ),
                RecommendationItem(
                    icon = "🥗",
                    title = "Sustainable Diet Changes",
                    description = "Focus on whole foods, portion control, and reducing " +
                            "processed foods. Avoid extreme diets — sustainability is key."
                ),
                RecommendationItem(
                    icon = "🤝",
                    title = "Build Support",
                    description = "Consider a support group, health coach, or nutritionist. " +
                            "Having someone in your corner makes the journey easier."
                ),
                RecommendationItem(
                    icon = "🩺",
                    title = "Health Screenings",
                    description = "Get checked for blood pressure, blood sugar, and " +
                            "cholesterol levels to understand your full health picture."
                )
            ),
            actionSteps = listOf(
                "Schedule an appointment with your doctor",
                "Start with a 10-minute walk after dinner each day",
                "Replace sugary beverages with water or herbal tea",
                "Increase vegetable intake at every meal",
                "Set a goal to lose 5% of body weight as a first milestone",
                "Consider joining a wellness or support community"
            ),
            doctorNote = "With a BMI in the obese range, consulting your healthcare provider " +
                    "is important. They can assess associated health risks, run relevant tests, " +
                    "and help create a safe, effective plan for improving your health."
        )
    }

    private fun getObeseClassII(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Obese Class II",
            categoryIcon = "🧡",
            toneMessage = "Your health is precious, and we're glad you're taking the time " +
                    "to check in on it. At this weight range, your body faces elevated health " +
                    "challenges — but meaningful improvement is absolutely possible. " +
                    "Many people have transformed their health with professional guidance " +
                    "and consistent small steps. You deserve to feel your best.",
            riskLevel = RiskLevel.VERY_HIGH,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🫀",
                    title = "Serious Heart Disease Risk",
                    description = "Significantly elevated risk of coronary artery disease, " +
                            "heart failure, and stroke. The heart is under considerable strain.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "High Diabetes Risk",
                    description = "Strong association with type 2 diabetes and metabolic " +
                            "syndrome. Blood sugar control may already be affected.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🫁",
                    title = "Respiratory Issues",
                    description = "Breathing difficulties, severe sleep apnea, and reduced " +
                            "lung capacity can significantly affect daily life.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🔬",
                    title = "Certain Cancer Risks",
                    description = "Obesity is linked to increased risk of several cancers " +
                            "including breast, colon, and kidney cancer.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🦵",
                    title = "Severe Joint Stress",
                    description = "High risk of osteoarthritis, particularly in knees and " +
                            "hips, potentially limiting mobility.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🧠",
                    title = "Mental Wellbeing",
                    description = "Higher rates of depression and anxiety are associated " +
                            "with this weight range. Your mental health matters just as much.",
                    severity = RiskSeverity.MODERATE
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "👨‍⚕️",
                    title = "Medical Supervision Required",
                    description = "Please work closely with healthcare professionals. " +
                            "Medical supervision ensures safe and effective weight management."
                ),
                RecommendationItem(
                    icon = "📋",
                    title = "Structured Program",
                    description = "A structured weight management program with medical, " +
                            "nutritional, and psychological support offers the best outcomes."
                ),
                RecommendationItem(
                    icon = "💊",
                    title = "Medical Options Discussion",
                    description = "Discuss all available options with your doctor, " +
                            "including medication or specialist referrals if appropriate."
                ),
                RecommendationItem(
                    icon = "🥗",
                    title = "Gradual Dietary Reform",
                    description = "Work with a registered dietitian for a personalized " +
                            "meal plan. Avoid extreme restriction — it's counterproductive."
                ),
                RecommendationItem(
                    icon = "🧘",
                    title = "Mind-Body Connection",
                    description = "Address emotional eating patterns. Consider counseling " +
                            "or mindful eating practices."
                )
            ),
            actionSteps = listOf(
                "See your doctor as soon as possible for a comprehensive assessment",
                "Request blood work: glucose, cholesterol, blood pressure check",
                "Ask for a referral to a registered dietitian",
                "Start with gentle, enjoyable movement — even 5 minutes counts",
                "Focus on one dietary change at a time",
                "Be kind to yourself — this is a journey, not a sprint"
            ),
            doctorNote = "A BMI in the severely obese range requires medical attention. " +
                    "Please schedule an appointment with your healthcare provider for a " +
                    "comprehensive health assessment and to discuss a structured weight " +
                    "management plan. Your doctor may recommend specialist support."
        )
    }

    private fun getObeseClassIII(age: Int, isMale: Boolean): HealthRiskInfo {
        return HealthRiskInfo(
            category = "Obese Class III",
            categoryIcon = "❤️",
            toneMessage = "We want you to know that checking your health takes courage, " +
                    "and we're here to support you with information, not judgment. At this " +
                    "weight range, your body faces serious health challenges — but improvement " +
                    "is possible at any starting point. Every step forward matters, " +
                    "no matter how small. You deserve compassionate care and support.",
            riskLevel = RiskLevel.EXTREMELY_HIGH,
            healthRisks = listOf(
                HealthRiskItem(
                    icon = "🫀",
                    title = "Critical Cardiovascular Risk",
                    description = "Extremely elevated risk of heart attack, stroke, and " +
                            "heart failure. The cardiovascular system is under severe strain.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🩸",
                    title = "Very High Diabetes Risk",
                    description = "Extremely strong association with type 2 diabetes and " +
                            "metabolic syndrome. Insulin resistance is likely significant.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🫁",
                    title = "Severe Breathing Difficulties",
                    description = "Obesity hypoventilation syndrome and severe sleep apnea " +
                            "can be life-threatening without treatment.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🔬",
                    title = "Elevated Cancer Risk",
                    description = "Significantly increased risk of multiple types of cancer. " +
                            "Regular screenings become even more important.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🦵",
                    title = "Mobility & Joint Damage",
                    description = "Severe joint stress may significantly limit mobility " +
                            "and daily activities, creating a challenging cycle.",
                    severity = RiskSeverity.SEVERE
                ),
                HealthRiskItem(
                    icon = "🩺",
                    title = "Organ System Impact",
                    description = "Liver disease (NAFLD), kidney issues, and other organ " +
                            "complications become more likely at this weight range.",
                    severity = RiskSeverity.HIGH
                ),
                HealthRiskItem(
                    icon = "🧠",
                    title = "Mental Health Support Needed",
                    description = "Depression, anxiety, and social isolation are common. " +
                            "Mental health care is as important as physical health care.",
                    severity = RiskSeverity.HIGH
                )
            ),
            recommendations = listOf(
                RecommendationItem(
                    icon = "🏥",
                    title = "Immediate Medical Care",
                    description = "Urgent consultation with healthcare providers is " +
                            "essential. A comprehensive medical assessment is the priority."
                ),
                RecommendationItem(
                    icon = "👨‍⚕️",
                    title = "Specialist Team",
                    description = "A multidisciplinary team — doctor, dietitian, " +
                            "psychologist, and exercise specialist — offers the best path forward."
                ),
                RecommendationItem(
                    icon = "💊",
                    title = "All Options on the Table",
                    description = "Discuss all treatment options with your doctor, " +
                            "including specialized medical interventions if appropriate."
                ),
                RecommendationItem(
                    icon = "❤️",
                    title = "Compassionate Self-Care",
                    description = "Be patient and kind with yourself. Focus on health " +
                            "improvements, not perfection. Every positive choice matters."
                ),
                RecommendationItem(
                    icon = "🤝",
                    title = "Community & Support",
                    description = "Connect with support groups and communities. " +
                            "Shared experiences and encouragement make a real difference."
                )
            ),
            actionSteps = listOf(
                "Contact your healthcare provider today for an appointment",
                "Be honest with your doctor about your health concerns",
                "Ask about comprehensive weight management programs",
                "Start with any movement you can do comfortably — every bit helps",
                "Focus on one small nutritional change this week",
                "Reach out to a mental health professional if you're struggling emotionally",
                "Remember: your value as a person is not defined by a number"
            ),
            doctorNote = "With a BMI of 40 or above, immediate medical consultation is " +
                    "strongly recommended. This weight range carries serious health risks " +
                    "that require professional medical management. Please reach out to " +
                    "your healthcare provider for a comprehensive evaluation and to " +
                    "discuss all available treatment options."
        )
    }
}
