// data/model/BpRecommendationsData.kt
package com.health.calculator.bmi.tracker.data.model

data class BpRecommendation(
    val icon: String,
    val title: String,
    val description: String
)

data class BpCategoryGuidance(
    val headerEmoji: String,
    val headerMessage: String,
    val headerTone: BpGuidanceTone,
    val monitoringFrequency: String,
    val recommendations: List<BpRecommendation>,
    val dietTips: List<BpRecommendation>,
    val exerciseTips: List<BpRecommendation>,
    val healthRisks: List<String>,
    val doctorAdvice: String?,
    val urgencyNote: String?
)

enum class BpGuidanceTone {
    POSITIVE,
    GENTLE_AWARENESS,
    CAUTIOUS,
    CONCERNED,
    URGENT,
    EMERGENCY,
    INFORMATIONAL
}

object BpRecommendationsProvider {

    fun getGuidance(category: BpCategory): BpCategoryGuidance {
        return when (category) {
            BpCategory.OPTIMAL -> getOptimalGuidance()
            BpCategory.NORMAL -> getNormalGuidance()
            BpCategory.HIGH_NORMAL -> getHighNormalGuidance()
            BpCategory.ISOLATED_SYSTOLIC -> getIsolatedSystolicGuidance()
            BpCategory.GRADE_1_HYPERTENSION -> getGrade1Guidance()
            BpCategory.GRADE_2_HYPERTENSION -> getGrade2Guidance()
            BpCategory.GRADE_3_HYPERTENSION -> getGrade3Guidance()
            BpCategory.HYPERTENSIVE_CRISIS -> getCrisisGuidance()
            BpCategory.HYPOTENSION -> getHypotensionGuidance()
        }
    }

    private fun getOptimalGuidance() = BpCategoryGuidance(
        headerEmoji = "🎉",
        headerMessage = "Excellent! Your blood pressure is in the optimal range. You're doing a great job taking care of your cardiovascular health!",
        headerTone = BpGuidanceTone.POSITIVE,
        monitoringFrequency = "Every 6–12 months",
        recommendations = listOf(
            BpRecommendation("✅", "Keep Up the Good Work", "Your current lifestyle choices are supporting healthy blood pressure. Continue what you're doing!"),
            BpRecommendation("😴", "Quality Sleep", "Aim for 7–9 hours of quality sleep each night. Good sleep supports cardiovascular health."),
            BpRecommendation("🧘", "Stress Management", "Continue managing stress through mindfulness, deep breathing, or hobbies you enjoy."),
            BpRecommendation("📊", "Regular Monitoring", "Even with optimal readings, check your BP periodically to catch any changes early.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "Balanced Diet", "Continue eating plenty of fruits, vegetables, whole grains, and lean proteins."),
            BpRecommendation("💧", "Stay Hydrated", "Drink adequate water throughout the day – aim for 8 glasses."),
            BpRecommendation("🐟", "Omega-3 Rich Foods", "Fish, walnuts, and flaxseed support heart health.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🏃", "Stay Active", "Maintain at least 150 minutes of moderate exercise per week."),
            BpRecommendation("💪", "Strength Training", "Include resistance exercises 2–3 times per week for overall fitness.")
        ),
        healthRisks = emptyList(),
        doctorAdvice = null,
        urgencyNote = null
    )

    private fun getNormalGuidance() = BpCategoryGuidance(
        headerEmoji = "👍",
        headerMessage = "Your blood pressure is within the normal range. That's good news! With a few mindful habits, you can keep it that way or even bring it to optimal.",
        headerTone = BpGuidanceTone.GENTLE_AWARENESS,
        monitoringFrequency = "Every 3–6 months",
        recommendations = listOf(
            BpRecommendation("🧂", "Watch Sodium Intake", "Try to keep sodium below 2,300 mg/day. Read food labels and choose low-sodium options."),
            BpRecommendation("🍌", "Increase Potassium", "Bananas, sweet potatoes, spinach, and avocados are potassium-rich foods that help balance sodium."),
            BpRecommendation("⚖️", "Maintain Healthy Weight", "Even small weight changes can affect blood pressure. Stay within a healthy BMI range."),
            BpRecommendation("🚭", "Avoid Smoking", "If you smoke, consider quitting. Smoking damages blood vessels and raises BP temporarily.")
        ),
        dietTips = listOf(
            BpRecommendation("🥬", "More Vegetables", "Aim for 5+ servings of vegetables daily. Leafy greens are especially beneficial."),
            BpRecommendation("🫘", "Fiber-Rich Foods", "Beans, lentils, oats, and whole grains help maintain healthy blood pressure."),
            BpRecommendation("🚫", "Limit Processed Foods", "Processed and packaged foods often contain hidden sodium.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🚶", "Daily Walking", "A brisk 30-minute walk daily can help maintain normal blood pressure."),
            BpRecommendation("🧘", "Yoga or Stretching", "Flexibility exercises and yoga can help reduce stress-related BP increases.")
        ),
        healthRisks = emptyList(),
        doctorAdvice = "Routine checkup recommended annually.",
        urgencyNote = null
    )

    private fun getHighNormalGuidance() = BpCategoryGuidance(
        headerEmoji = "⚠️",
        headerMessage = "Your blood pressure is at the higher end of normal – sometimes called 'pre-hypertension.' This is a signal to take proactive steps before it progresses.",
        headerTone = BpGuidanceTone.CAUTIOUS,
        monitoringFrequency = "Monthly",
        recommendations = listOf(
            BpRecommendation("🎯", "Take Action Now", "Lifestyle changes at this stage are very effective at preventing hypertension. This is your window of opportunity."),
            BpRecommendation("🧂", "Reduce Sodium Significantly", "Aim for less than 1,500 mg sodium per day. This single change can lower BP by 5–6 mmHg."),
            BpRecommendation("🍷", "Limit Alcohol", "Men: max 2 drinks/day. Women: max 1 drink/day. Less is better for blood pressure."),
            BpRecommendation("🚭", "Quit Smoking", "Smoking and high-normal BP together significantly increase cardiovascular risk."),
            BpRecommendation("😌", "Manage Stress Actively", "Chronic stress contributes to elevated BP. Try meditation, deep breathing, or progressive relaxation.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "DASH Diet", "The DASH (Dietary Approaches to Stop Hypertension) diet can lower BP by 8–14 mmHg. Focus on fruits, vegetables, low-fat dairy, and whole grains."),
            BpRecommendation("🚫", "Cut Processed Meats", "Bacon, sausage, deli meats, and hot dogs are high in sodium and saturated fat."),
            BpRecommendation("🫒", "Healthy Fats", "Replace saturated fats with olive oil, nuts, and avocado."),
            BpRecommendation("☕", "Moderate Caffeine", "Limit caffeine to 2–3 cups of coffee per day. Monitor if caffeine raises your BP.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🏃", "150 Minutes/Week", "Aim for at least 150 minutes of moderate aerobic exercise per week (30 min, 5 days)."),
            BpRecommendation("🚴", "Variety of Activities", "Mix walking, cycling, swimming, or dancing to keep exercise enjoyable and sustainable."),
            BpRecommendation("🧘", "Relaxation Exercises", "Include yoga, tai chi, or meditation to address stress-related blood pressure elevation.")
        ),
        healthRisks = listOf(
            "Increased risk of developing hypertension if not managed",
            "Higher cardiovascular risk compared to optimal BP",
            "May progress to Grade 1 hypertension without intervention"
        ),
        doctorAdvice = "Consider scheduling a checkup to discuss your BP trend and preventive strategies.",
        urgencyNote = null
    )

    private fun getIsolatedSystolicGuidance() = BpCategoryGuidance(
        headerEmoji = "📋",
        headerMessage = "Your systolic (top number) is elevated while diastolic remains normal. This pattern, called Isolated Systolic Hypertension, is common and important to address.",
        headerTone = BpGuidanceTone.CAUTIOUS,
        monitoringFrequency = "Every 2–4 weeks",
        recommendations = listOf(
            BpRecommendation("🏥", "Doctor Consultation", "Isolated systolic hypertension should be evaluated by a healthcare provider, especially if persistent."),
            BpRecommendation("🧂", "Sodium Reduction", "Reducing sodium is particularly effective for lowering systolic pressure."),
            BpRecommendation("⚖️", "Weight Management", "Even 5–10 lbs of weight loss can significantly lower systolic BP."),
            BpRecommendation("🏃", "Aerobic Exercise", "Regular cardio exercise is especially effective at reducing systolic pressure.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "DASH Diet", "This eating pattern is proven to lower systolic BP significantly."),
            BpRecommendation("🍌", "Potassium-Rich Foods", "Potassium helps counteract sodium's effects on systolic pressure.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🚶", "Daily Walking", "30–45 minutes of brisk walking daily targets systolic pressure effectively."),
            BpRecommendation("🏊", "Swimming", "Low-impact but highly effective for blood pressure reduction.")
        ),
        healthRisks = listOf(
            "Increased risk of stroke",
            "Higher risk of heart disease",
            "More common in older adults due to arterial stiffness"
        ),
        doctorAdvice = "A healthcare provider should evaluate persistent isolated systolic hypertension. Treatment may include lifestyle changes and possibly medication.",
        urgencyNote = null
    )

    private fun getGrade1Guidance() = BpCategoryGuidance(
        headerEmoji = "🩺",
        headerMessage = "Your blood pressure is in the Stage 1 Hypertension range. This requires attention, but with the right changes, many people successfully lower their BP.",
        headerTone = BpGuidanceTone.CONCERNED,
        monitoringFrequency = "Weekly",
        recommendations = listOf(
            BpRecommendation("🏥", "See Your Doctor", "A healthcare provider should evaluate your blood pressure and discuss a management plan."),
            BpRecommendation("💊", "Medication May Help", "Depending on your overall cardiovascular risk, your doctor may recommend medication alongside lifestyle changes."),
            BpRecommendation("🧂", "Strict Sodium Limit", "Keep sodium under 1,500 mg/day. This is crucial at this stage."),
            BpRecommendation("🍷", "Minimize Alcohol", "Alcohol can raise BP and interfere with BP medications. Limit or avoid."),
            BpRecommendation("🚭", "Stop Smoking", "Smoking with hypertension dramatically increases heart attack and stroke risk."),
            BpRecommendation("📝", "Track Consistently", "Daily BP monitoring helps you and your doctor make better decisions.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "Adopt DASH Diet", "The DASH diet is strongly recommended for Stage 1 hypertension."),
            BpRecommendation("🚫", "Eliminate Added Salt", "Stop adding salt at the table. Use herbs, spices, and lemon instead."),
            BpRecommendation("🫘", "High-Fiber Foods", "Increase fiber from beans, lentils, whole grains, fruits, and vegetables."),
            BpRecommendation("🍫", "Dark Chocolate", "Small amounts (1 oz) of dark chocolate (70%+) may have modest BP-lowering effects.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🏃", "Regular Cardio", "Aim for 150+ minutes of moderate or 75 minutes of vigorous aerobic exercise per week."),
            BpRecommendation("💪", "Resistance Training", "Light to moderate strength training 2–3 times per week."),
            BpRecommendation("⚠️", "Avoid Heavy Lifting", "Very heavy weightlifting can spike BP. Use moderate weights with higher reps.")
        ),
        healthRisks = listOf(
            "2–3x increased risk of heart disease",
            "Increased risk of stroke",
            "Potential kidney damage over time",
            "Risk of heart failure if untreated",
            "May damage blood vessels gradually"
        ),
        doctorAdvice = "Doctor consultation is strongly recommended. They may suggest lifestyle changes for 3–6 months before considering medication, or start medication immediately based on your overall risk profile.",
        urgencyNote = "Monitor your BP weekly and keep a log to share with your doctor."
    )

    private fun getGrade2Guidance() = BpCategoryGuidance(
        headerEmoji = "🚨",
        headerMessage = "Your blood pressure is significantly elevated (Stage 2 Hypertension). This level requires medical attention and aggressive management to prevent serious complications.",
        headerTone = BpGuidanceTone.URGENT,
        monitoringFrequency = "Daily, or as directed by your doctor",
        recommendations = listOf(
            BpRecommendation("🏥", "See Your Doctor Soon", "Stage 2 hypertension typically requires both lifestyle changes AND medication. Do not delay scheduling an appointment."),
            BpRecommendation("💊", "Medication Likely Needed", "Most people with Stage 2 hypertension need one or more BP medications. Follow your doctor's prescription exactly."),
            BpRecommendation("🚫", "Do NOT Self-Medicate", "Never take someone else's BP medication or adjust doses without medical guidance."),
            BpRecommendation("📋", "Daily Monitoring", "Check your BP daily at consistent times. Share logs with your doctor."),
            BpRecommendation("🧂", "Extreme Sodium Caution", "Keep sodium well under 1,500 mg/day. Read every food label."),
            BpRecommendation("😌", "Stress Reduction Critical", "High stress combined with Stage 2 hypertension is dangerous. Prioritize stress management.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "DASH Diet Essential", "Following DASH strictly can lower BP by 8–14 mmHg."),
            BpRecommendation("🚫", "Avoid Fast Food", "Fast food is typically extremely high in sodium, fat, and calories."),
            BpRecommendation("🍎", "Anti-Inflammatory Foods", "Berries, leafy greens, fatty fish, and nuts reduce inflammation that worsens hypertension.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🚶", "Start Gentle", "If you haven't been exercising, start with gentle walks and gradually increase intensity."),
            BpRecommendation("⚠️", "Check with Doctor First", "Get medical clearance before starting a new exercise program with Stage 2 hypertension.")
        ),
        healthRisks = listOf(
            "Significantly elevated risk of heart attack",
            "High risk of stroke",
            "Potential kidney damage and chronic kidney disease",
            "Risk of heart failure",
            "Potential vision problems from damaged blood vessels in eyes",
            "Risk of peripheral artery disease",
            "Possible cognitive decline over time"
        ),
        doctorAdvice = "Medical attention is urgently recommended. Stage 2 hypertension typically requires a combination of medication and lifestyle changes. Your doctor may want to run additional tests to assess organ damage.",
        urgencyNote = "Please schedule a doctor's appointment within the next few days if you haven't already."
    )

    private fun getGrade3Guidance() = BpCategoryGuidance(
        headerEmoji = "🆘",
        headerMessage = "Your blood pressure is severely elevated (Stage 3 Hypertension). This is a serious condition requiring immediate medical evaluation.",
        headerTone = BpGuidanceTone.URGENT,
        monitoringFrequency = "Multiple times daily, under medical supervision",
        recommendations = listOf(
            BpRecommendation("🏥", "Seek Medical Care Today", "This level of blood pressure requires urgent medical evaluation. Contact your doctor or visit a clinic today."),
            BpRecommendation("💊", "Medication Essential", "Stage 3 hypertension almost always requires medication, often multiple drugs."),
            BpRecommendation("🚫", "Do NOT Self-Treat", "This is not a condition to manage alone. Professional medical care is essential."),
            BpRecommendation("📞", "Emergency Signs", "If you experience severe headache, chest pain, vision changes, or difficulty breathing – call emergency services immediately.")
        ),
        dietTips = listOf(
            BpRecommendation("🥗", "Follow Doctor's Dietary Advice", "Your doctor may prescribe a specific dietary plan for your situation."),
            BpRecommendation("🧂", "Near-Zero Added Sodium", "Minimize sodium as much as possible under medical guidance.")
        ),
        exerciseTips = listOf(
            BpRecommendation("⚠️", "Medical Clearance Required", "Do not begin intense exercise without your doctor's approval at this BP level."),
            BpRecommendation("🚶", "Gentle Movement Only", "Light walking may be appropriate, but check with your doctor first.")
        ),
        healthRisks = listOf(
            "Very high risk of stroke – potentially life-threatening",
            "Severe risk of heart attack",
            "Risk of kidney failure",
            "Potential for heart failure",
            "Risk of aortic dissection",
            "Possible brain damage from cerebral hemorrhage",
            "Vision loss from retinal damage"
        ),
        doctorAdvice = "Immediate medical evaluation is necessary. This level of hypertension can cause serious organ damage if untreated. Your doctor will likely start you on medication right away and may order tests for organ damage.",
        urgencyNote = "⚠️ If you experience severe headache, chest pain, shortness of breath, vision changes, numbness, or confusion – call 911/112 immediately."
    )

    private fun getCrisisGuidance() = BpCategoryGuidance(
        headerEmoji = "🚑",
        headerMessage = "HYPERTENSIVE CRISIS DETECTED. This reading indicates dangerously high blood pressure that may require emergency medical care.",
        headerTone = BpGuidanceTone.EMERGENCY,
        monitoringFrequency = "Under medical supervision",
        recommendations = listOf(
            BpRecommendation("📞", "Call Emergency Services", "If experiencing ANY symptoms (headache, chest pain, vision changes, numbness), call 911/112 immediately."),
            BpRecommendation("🔄", "Retake Measurement", "Wait 5 minutes, sit quietly, then retake your BP. If still this high, seek medical care."),
            BpRecommendation("🏥", "Go to Emergency Room", "Even without symptoms, a reading this high should be evaluated in an emergency department."),
            BpRecommendation("🚫", "Do Not Delay", "Do not try to lower your BP with home remedies. Medical professionals need to manage this safely.")
        ),
        dietTips = emptyList(),
        exerciseTips = emptyList(),
        healthRisks = listOf(
            "Immediate risk of stroke",
            "Risk of heart attack",
            "Possible organ damage occurring right now",
            "Risk of brain hemorrhage",
            "Potential kidney failure",
            "Risk of aortic dissection (life-threatening)"
        ),
        doctorAdvice = "THIS IS A MEDICAL EMERGENCY if you are experiencing symptoms. Even without symptoms, this reading requires same-day medical evaluation.",
        urgencyNote = "🚨 EMERGENCY: Call 911/112 if you have severe headache, chest pain, shortness of breath, vision changes, numbness, confusion, or difficulty speaking."
    )

    private fun getHypotensionGuidance() = BpCategoryGuidance(
        headerEmoji = "💙",
        headerMessage = "Your blood pressure is below the normal range. While some people naturally have low BP, it's important to watch for symptoms and know when to seek help.",
        headerTone = BpGuidanceTone.INFORMATIONAL,
        monitoringFrequency = "Regularly, especially if experiencing symptoms",
        recommendations = listOf(
            BpRecommendation("🚨", "Know the Warning Signs", "Watch for dizziness, lightheadedness, fainting, blurred vision, nausea, or unusual fatigue. These may indicate your BP is too low."),
            BpRecommendation("🧍", "Stand Up Slowly", "When getting up from sitting or lying down, do so gradually to avoid sudden drops in BP (orthostatic hypotension)."),
            BpRecommendation("💧", "Stay Well Hydrated", "Dehydration is a common cause of low BP. Drink plenty of water throughout the day."),
            BpRecommendation("🧂", "Moderate Salt Intake", "Unlike hypertension, slightly increasing salt may help if your doctor recommends it."),
            BpRecommendation("🩺", "Check Medications", "Some medications can cause low BP. Talk to your doctor if you're on any medication.")
        ),
        dietTips = listOf(
            BpRecommendation("🍽️", "Small Frequent Meals", "Large meals can cause BP to drop. Eat smaller, more frequent meals."),
            BpRecommendation("🚫", "Limit Alcohol", "Alcohol can lower blood pressure further. Be cautious with intake."),
            BpRecommendation("☕", "Moderate Caffeine", "A cup of coffee or tea can temporarily raise BP. Use strategically if symptomatic."),
            BpRecommendation("🥤", "Electrolyte Balance", "Ensure adequate sodium, potassium, and magnesium through diet or as advised by your doctor.")
        ),
        exerciseTips = listOf(
            BpRecommendation("🏃", "Exercise Carefully", "Exercise is still beneficial, but avoid sudden position changes during workouts."),
            BpRecommendation("🧦", "Compression Stockings", "If you experience frequent dizziness, compression stockings can help maintain BP."),
            BpRecommendation("🚿", "Avoid Hot Showers", "Very hot water can cause blood vessels to dilate, lowering BP further.")
        ),
        healthRisks = listOf(
            "Risk of falls from dizziness or fainting",
            "Potential organ damage if BP is chronically very low",
            "Could indicate an underlying medical condition"
        ),
        doctorAdvice = "If you frequently experience symptoms like dizziness, fainting, or extreme fatigue, consult your doctor. Low BP can sometimes indicate underlying conditions that need attention.",
        urgencyNote = "Seek immediate medical attention if you experience prolonged dizziness, confusion, rapid shallow breathing, cold clammy skin, or fainting."
    )

    fun getWhiteCoatSyndromeInfo(): List<BpRecommendation> {
        return listOf(
            BpRecommendation("🏥", "What is White Coat Syndrome?", "Some people experience higher blood pressure readings in medical settings due to anxiety or stress. This is called 'white coat hypertension' or 'white coat syndrome.'"),
            BpRecommendation("📊", "Why Home Monitoring Matters", "Home blood pressure readings are often more representative of your true BP. They capture your readings in a relaxed, familiar environment."),
            BpRecommendation("📋", "How to Get Accurate Readings", "Sit quietly for 5 minutes before measuring. Use a validated monitor. Take 2–3 readings and average them. Measure at the same times each day."),
            BpRecommendation("🩺", "Talk to Your Doctor", "If your home readings are consistently lower than clinic readings, share your home log with your doctor. They may adjust your treatment accordingly."),
            BpRecommendation("📈", "Prevalence", "White coat syndrome affects 15–30% of people with elevated clinic readings. It's more common in older adults and women.")
        )
    }
}
