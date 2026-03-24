package com.health.calculator.bmi.tracker.calculator

data class CriterionRecommendation(
    val criterionName: String,
    val isAbnormal: Boolean,
    val healthMeaning: String,
    val risks: List<String>,
    val recommendations: List<String>,
    val normalMessage: String,
    val icon: String,
    val urgencyLevel: String // "positive", "caution", "warning"
)

data class CardiovascularRiskSummary(
    val riskLevel: String,
    val riskDescription: String,
    val riskScore: Int, // 0-100
    val overallMessage: String,
    val actionItems: List<String>,
    val shouldSeekMedical: Boolean
)

object MetabolicSyndromeRecommendations {

    fun getRecommendationForCriterion(
        criterionName: String,
        isMet: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return when (criterionName) {
            "Central Obesity" -> getCentralObesityRecommendation(isMet, isOnMedication)
            "Elevated Triglycerides" -> getTriglyceridesRecommendation(isMet, isOnMedication)
            "Reduced HDL Cholesterol" -> getHdlRecommendation(isMet, isOnMedication)
            "Elevated Blood Pressure" -> getBloodPressureRecommendation(isMet, isOnMedication)
            "Elevated Fasting Glucose" -> getGlucoseRecommendation(isMet, isOnMedication)
            else -> getDefaultRecommendation(criterionName, isMet)
        }
    }

    private fun getCentralObesityRecommendation(
        isAbnormal: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = "Central Obesity",
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) {
                "Excess fat around your midsection (visceral fat) is strongly linked to insulin resistance and metabolic dysfunction. Unlike subcutaneous fat, visceral fat surrounds your organs and releases inflammatory compounds."
            } else {
                "Your waist circumference is within the healthy range, indicating lower levels of visceral fat."
            },
            risks = if (isAbnormal) listOf(
                "Insulin resistance and type 2 diabetes",
                "Cardiovascular disease and heart attack",
                "Chronic inflammation throughout the body",
                "Non-alcoholic fatty liver disease (NAFLD)",
                "Increased risk of certain cancers",
                "Sleep apnea and breathing difficulties"
            ) else emptyList(),
            recommendations = if (isAbnormal) listOf(
                "🏃 Engage in moderate cardio exercise for at least 30 minutes daily — brisk walking, cycling, or swimming are excellent choices",
                "🥗 Reduce refined carbohydrates and added sugars — replace with whole grains, vegetables, and lean proteins",
                "🥦 Increase dietary fiber to 25-30g daily — beans, oats, fruits, and vegetables help reduce visceral fat",
                "💪 Add strength training 2-3 times per week — muscle mass helps improve metabolism",
                "😴 Prioritize 7-9 hours of quality sleep — poor sleep increases cortisol and promotes belly fat storage",
                "🧘 Manage stress through meditation, deep breathing, or yoga — chronic stress raises cortisol which promotes central fat storage",
                "🚫 Limit alcohol consumption — excess alcohol is directly linked to increased abdominal fat"
            ) else listOf(
                "Continue maintaining a balanced diet rich in whole foods",
                "Stay active with regular physical activity",
                "Monitor your waist circumference periodically"
            ),
            normalMessage = "Your waist circumference is within the healthy range. Great job maintaining a healthy midsection! Continue your current healthy habits.",
            icon = "📏",
            urgencyLevel = if (isAbnormal) "warning" else "positive"
        )
    }

    private fun getTriglyceridesRecommendation(
        isAbnormal: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = "Elevated Triglycerides",
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) {
                "Triglycerides are a type of fat in your blood. When elevated, they contribute to the hardening and thickening of artery walls (atherosclerosis), significantly increasing your cardiovascular risk.${if (isOnMedication) " While you're on medication, lifestyle changes remain crucial for optimal management." else ""}"
            } else {
                "Your triglyceride levels are within the healthy range, suggesting good fat metabolism and lower cardiovascular risk from this factor."
            },
            risks = if (isAbnormal) listOf(
                "Increased cardiovascular disease risk",
                "Atherosclerosis (plaque buildup in arteries)",
                "Pancreatitis (especially if very high, >500 mg/dL)",
                "Fatty liver disease",
                "Metabolic dysfunction and insulin resistance",
                "Increased risk of stroke"
            ) else emptyList(),
            recommendations = if (isAbnormal) listOf(
                "🍬 Significantly reduce sugar and refined carbohydrate intake — these are the biggest dietary drivers of high triglycerides",
                "🐟 Increase omega-3 fatty acid intake — eat fatty fish (salmon, mackerel, sardines) 2-3 times per week or consider a fish oil supplement",
                "🏃 Exercise regularly — aim for 150 minutes of moderate aerobic activity per week, which can lower triglycerides by 20-30%",
                "🍺 Limit or eliminate alcohol — even moderate alcohol intake can significantly raise triglycerides",
                "⚖️ Lose excess weight — even a 5-10% weight loss can dramatically reduce triglyceride levels",
                "🥤 Eliminate sugary beverages — sodas, fruit juices, and sweetened drinks are major contributors",
                "🫒 Replace saturated fats with healthy fats — use olive oil, avocado, and nuts instead of butter and processed foods",
                "🍽️ Avoid overeating — large meals can spike triglyceride levels"
            ) else listOf(
                "Continue eating a balanced diet low in refined sugars",
                "Maintain your omega-3 intake through fish or supplements",
                "Stay active with regular exercise"
            ),
            normalMessage = "Your triglyceride levels are in the healthy range. Well done! This means your body is metabolizing fats effectively.",
            icon = "🩸",
            urgencyLevel = if (isAbnormal) "warning" else "positive"
        )
    }

    private fun getHdlRecommendation(
        isAbnormal: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = "Reduced HDL Cholesterol",
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) {
                "HDL (\"good\") cholesterol helps remove other forms of cholesterol from your bloodstream. Low HDL means less cholesterol is being cleared, leaving more plaque-building material in your arteries.${if (isOnMedication) " Medication can help, but lifestyle modifications are essential for sustainable improvement." else ""}"
            } else {
                "Your HDL cholesterol is at a healthy level, meaning your body is effectively clearing excess cholesterol from your bloodstream."
            },
            risks = if (isAbnormal) listOf(
                "Significantly increased heart disease risk",
                "Greater likelihood of atherosclerosis",
                "Higher risk of heart attack and stroke",
                "Reduced ability to clear arterial plaque",
                "Increased risk of peripheral artery disease",
                "Combined with high LDL, dramatically elevates cardiovascular risk"
            ) else emptyList(),
            recommendations = if (isAbnormal) listOf(
                "🏋️ Exercise regularly and vigorously — aerobic exercise is the most effective way to raise HDL, aim for 30-60 minutes most days",
                "🥑 Eat healthy fats — monounsaturated fats in olive oil, avocados, and nuts can boost HDL levels",
                "🚭 Quit smoking — smoking lowers HDL significantly, and quitting can raise HDL by up to 10%",
                "⚖️ Lose excess weight — every 6 pounds lost can increase HDL by approximately 1 mg/dL",
                "🍷 Moderate alcohol consumption (if you drink) — small amounts may raise HDL, but risks of alcohol often outweigh this benefit",
                "🫒 Add coconut oil in moderation — may help improve HDL levels",
                "🟣 Eat purple and red fruits — berries, grapes, and pomegranates contain antioxidants that support HDL function",
                "🚫 Avoid trans fats — these artificial fats actively lower HDL and are found in processed foods"
            ) else listOf(
                "Continue your active lifestyle to maintain healthy HDL levels",
                "Keep eating healthy fats from natural sources",
                "Avoid trans fats and excessive processed foods"
            ),
            normalMessage = "Your HDL cholesterol is at a protective level. This \"good\" cholesterol is helping keep your arteries clean. Keep up the great work!",
            icon = "💛",
            urgencyLevel = if (isAbnormal) "warning" else "positive"
        )
    }

    private fun getBloodPressureRecommendation(
        isAbnormal: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = "Elevated Blood Pressure",
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) {
                "Elevated blood pressure means your heart is working harder than it should to pump blood through your arteries. Over time, this extra force damages artery walls and contributes to serious health problems.${if (isOnMedication) " Your medication is helping manage this, but don't skip doses and continue lifestyle modifications." else ""}"
            } else {
                "Your blood pressure is within healthy limits, meaning your heart and blood vessels are functioning efficiently without excessive strain."
            },
            risks = if (isAbnormal) listOf(
                "Heart attack and coronary artery disease",
                "Stroke (both ischemic and hemorrhagic)",
                "Chronic kidney disease and kidney failure",
                "Heart failure from prolonged overwork",
                "Vision loss from retinal damage",
                "Cognitive decline and vascular dementia",
                "Peripheral artery disease",
                "Aortic aneurysm"
            ) else emptyList(),
            recommendations = if (isAbnormal) listOf(
                "🧂 Reduce sodium intake to less than 2,300mg/day (ideally 1,500mg) — read food labels carefully and cook at home more often",
                "🥗 Follow the DASH diet — rich in fruits, vegetables, whole grains, and low-fat dairy; clinically proven to lower BP by 8-14 mmHg",
                "🏃 Exercise for at least 150 minutes per week — regular physical activity can lower BP by 5-8 mmHg",
                "🧘 Manage stress — practice deep breathing, meditation, or yoga for at least 10 minutes daily",
                "🍺 Limit alcohol — no more than 1 drink/day for women, 2 for men",
                "🚭 Quit smoking — each cigarette temporarily raises BP, and smoking damages blood vessels",
                "⚖️ Maintain a healthy weight — losing 1 kg can reduce BP by about 1 mmHg",
                "☕ Moderate caffeine intake — limit to 2-3 cups of coffee per day",
                "📊 Monitor BP regularly at home — track readings to share with your doctor"
            ) else listOf(
                "Continue maintaining a low-sodium diet",
                "Stay active with regular physical activity",
                "Manage stress levels effectively",
                "Monitor your blood pressure periodically"
            ),
            normalMessage = "Your blood pressure is in the healthy range. Your heart and blood vessels are under normal stress levels. Excellent!",
            icon = "❤️",
            urgencyLevel = if (isAbnormal) "warning" else "positive"
        )
    }

    private fun getGlucoseRecommendation(
        isAbnormal: Boolean,
        isOnMedication: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = "Elevated Fasting Glucose",
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) {
                "Elevated fasting glucose suggests your body is having difficulty regulating blood sugar levels. This is often an early sign of insulin resistance and may indicate pre-diabetes if between 100-125 mg/dL, or diabetes if ≥126 mg/dL.${if (isOnMedication) " Continue your medication as prescribed and combine with lifestyle changes for best results." else ""}"
            } else {
                "Your fasting glucose is within normal limits, indicating your body is effectively regulating blood sugar levels."
            },
            risks = if (isAbnormal) listOf(
                "Progression to type 2 diabetes",
                "Nerve damage (diabetic neuropathy)",
                "Kidney damage (diabetic nephropathy)",
                "Vision problems (diabetic retinopathy)",
                "Increased cardiovascular disease risk",
                "Slow wound healing and increased infection risk",
                "Cognitive decline and increased dementia risk"
            ) else emptyList(),
            recommendations = if (isAbnormal) listOf(
                "🍬 Reduce sugar and refined carbohydrate intake — switch to complex carbs like quinoa, sweet potatoes, and brown rice",
                "🥦 Increase fiber intake to 25-30g daily — fiber slows glucose absorption and improves insulin sensitivity",
                "🏃 Exercise at least 150 minutes per week — both aerobic and resistance training improve insulin sensitivity significantly",
                "⚖️ Maintain a healthy weight — even 5-7% weight loss can reduce diabetes risk by 58%",
                "🍽️ Eat smaller, more frequent meals — this helps prevent blood sugar spikes",
                "🥜 Include protein and healthy fat with every meal — this slows carbohydrate absorption",
                "😴 Get 7-9 hours of quality sleep — poor sleep impairs insulin sensitivity",
                "💧 Stay well hydrated — dehydration can concentrate blood sugar",
                "📊 Consider monitoring blood sugar at home — track how different foods affect your levels",
                "🏥 Get HbA1c tested — this gives a 3-month average of blood sugar control"
            ) else listOf(
                "Continue eating a balanced diet with moderate carbohydrate intake",
                "Stay physically active to maintain good insulin sensitivity",
                "Get regular blood glucose check-ups annually"
            ),
            normalMessage = "Your fasting glucose is within the normal range. Your body is managing blood sugar effectively. Keep maintaining your healthy eating and exercise habits!",
            icon = "🍯",
            urgencyLevel = if (isAbnormal) "warning" else "positive"
        )
    }

    private fun getDefaultRecommendation(
        name: String,
        isAbnormal: Boolean
    ): CriterionRecommendation {
        return CriterionRecommendation(
            criterionName = name,
            isAbnormal = isAbnormal,
            healthMeaning = if (isAbnormal) "This criterion is outside the healthy range." else "This criterion is within the healthy range.",
            risks = emptyList(),
            recommendations = emptyList(),
            normalMessage = "Your $name is within the healthy range. Keep it up!",
            icon = "📋",
            urgencyLevel = if (isAbnormal) "caution" else "positive"
        )
    }

    fun getCardiovascularRiskSummary(criteriaMet: Int): CardiovascularRiskSummary {
        return when (criteriaMet) {
            0 -> CardiovascularRiskSummary(
                riskLevel = "Low",
                riskDescription = "Your cardiovascular risk from metabolic factors is low.",
                riskScore = 10,
                overallMessage = "Excellent! All your metabolic indicators are within healthy ranges. You have a low cardiovascular risk profile based on these criteria. Continue your healthy lifestyle to maintain these great results.",
                actionItems = listOf(
                    "Continue regular health check-ups annually",
                    "Maintain a balanced diet and active lifestyle",
                    "Monitor these values periodically to catch changes early",
                    "Share these results with your doctor during routine visits"
                ),
                shouldSeekMedical = false
            )
            1 -> CardiovascularRiskSummary(
                riskLevel = "Low-Moderate",
                riskDescription = "You have one metabolic risk factor that needs attention.",
                riskScore = 30,
                overallMessage = "You have 1 out of 5 metabolic risk factors present. While this doesn't indicate metabolic syndrome, it's a sign to pay attention. Addressing this factor now can prevent progression to metabolic syndrome.",
                actionItems = listOf(
                    "Focus on improving the abnormal criterion through lifestyle changes",
                    "Schedule a follow-up check in 3-6 months",
                    "Adopt a healthier diet and increase physical activity",
                    "Discuss findings with your doctor at your next visit"
                ),
                shouldSeekMedical = false
            )
            2 -> CardiovascularRiskSummary(
                riskLevel = "Moderate",
                riskDescription = "You have two metabolic risk factors — borderline for metabolic syndrome.",
                riskScore = 55,
                overallMessage = "With 2 out of 5 criteria met, you're at the borderline. This is a critical window where lifestyle changes can make a significant difference. Without intervention, there's a higher chance of developing metabolic syndrome.",
                actionItems = listOf(
                    "Make lifestyle modifications a priority — diet and exercise are crucial now",
                    "Consult your doctor for a comprehensive metabolic evaluation",
                    "Re-check values in 3 months to assess improvement",
                    "Consider working with a dietitian for a personalized plan",
                    "Start tracking your meals and physical activity"
                ),
                shouldSeekMedical = true
            )
            3 -> CardiovascularRiskSummary(
                riskLevel = "High",
                riskDescription = "Metabolic syndrome is present — elevated cardiovascular risk.",
                riskScore = 75,
                overallMessage = "With 3 of 5 criteria met, metabolic syndrome is clinically present. This condition significantly increases your risk for heart disease, stroke, and type 2 diabetes. However, metabolic syndrome is reversible with proper management.",
                actionItems = listOf(
                    "🏥 Consult your healthcare provider as soon as possible for comprehensive evaluation",
                    "Begin structured lifestyle modifications immediately",
                    "Discuss medication options with your doctor if not already on treatment",
                    "Get additional blood work: LDL cholesterol, HbA1c, liver function",
                    "Re-assess all criteria in 3 months to track progress",
                    "Consider cardiac risk assessment (Framingham Risk Score)"
                ),
                shouldSeekMedical = true
            )
            4 -> CardiovascularRiskSummary(
                riskLevel = "High",
                riskDescription = "Metabolic syndrome is present with 4 criteria — significantly elevated risk.",
                riskScore = 85,
                overallMessage = "With 4 of 5 criteria met, you have a strong metabolic syndrome profile with significantly elevated cardiovascular risk. Aggressive lifestyle changes combined with medical management are important. The good news is that all of these factors can be improved.",
                actionItems = listOf(
                    "🏥 Schedule a doctor's appointment this week for comprehensive evaluation",
                    "Discuss medication management for multiple risk factors",
                    "Begin a medically supervised diet and exercise program",
                    "Request comprehensive cardiovascular risk assessment",
                    "Get screened for type 2 diabetes with an HbA1c test",
                    "Check kidney function and liver health",
                    "Consider referral to an endocrinologist or cardiologist"
                ),
                shouldSeekMedical = true
            )
            5 -> CardiovascularRiskSummary(
                riskLevel = "Very High",
                riskDescription = "All 5 metabolic syndrome criteria are met — very high cardiovascular risk.",
                riskScore = 95,
                overallMessage = "With all 5 criteria met, your metabolic risk profile requires immediate medical attention. This combination dramatically increases the risk for cardiovascular events, diabetes, and other serious conditions. Please take this seriously — but know that improvement is absolutely possible with proper care.",
                actionItems = listOf(
                    "🚨 See your doctor as soon as possible — ideally within the next few days",
                    "Do not self-medicate — professional guidance is essential",
                    "Comprehensive blood work and cardiac evaluation are needed",
                    "Medication management for multiple conditions may be necessary",
                    "Begin lifestyle changes immediately while awaiting medical consultation",
                    "Consider multi-specialty evaluation (cardiology, endocrinology)",
                    "Establish regular monitoring schedule with your healthcare team"
                ),
                shouldSeekMedical = true
            )
            else -> CardiovascularRiskSummary(
                riskLevel = "Unknown",
                riskDescription = "Unable to determine risk level.",
                riskScore = 0,
                overallMessage = "Please ensure all values are entered correctly.",
                actionItems = emptyList(),
                shouldSeekMedical = false
            )
        }
    }
}
