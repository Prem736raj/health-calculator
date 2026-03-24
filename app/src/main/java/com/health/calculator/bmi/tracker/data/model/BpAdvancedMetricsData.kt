// data/model/BpAdvancedMetricsData.kt
package com.health.calculator.bmi.tracker.data.model

data class PulsePressureAnalysis(
    val value: Int,
    val category: PpCategory,
    val normalizedPosition: Float, // 0.0–1.0 on scale
    val interpretation: String,
    val details: String,
    val isNormal: Boolean
)

enum class PpCategory(
    val displayName: String,
    val rangeLabel: String
) {
    VERY_NARROW("Very Narrow", "< 25 mmHg"),
    NARROW("Narrow", "25–39 mmHg"),
    NORMAL("Normal", "40–60 mmHg"),
    SLIGHTLY_WIDE("Slightly Wide", "61–80 mmHg"),
    WIDE("Wide", "81–100 mmHg"),
    VERY_WIDE("Very Wide", "> 100 mmHg")
}

data class MapAnalysis(
    val value: Double,
    val category: MapCategory,
    val normalizedPosition: Float,
    val interpretation: String,
    val details: String,
    val isNormal: Boolean
)

enum class MapCategory(
    val displayName: String,
    val rangeLabel: String
) {
    CRITICALLY_LOW("Critically Low", "< 60 mmHg"),
    LOW("Low", "60–69 mmHg"),
    NORMAL("Normal", "70–100 mmHg"),
    ELEVATED("Elevated", "101–110 mmHg"),
    HIGH("High", "111–130 mmHg"),
    VERY_HIGH("Very High", "> 130 mmHg")
}

data class HeartRateAnalysis(
    val bpm: Int,
    val category: HrCategory,
    val normalizedPosition: Float,
    val interpretation: String,
    val details: String,
    val isNormal: Boolean,
    val riskFactors: List<String>
)

enum class HrCategory(
    val displayName: String,
    val rangeLabel: String
) {
    SEVERELY_LOW("Severely Low", "< 40 BPM"),
    BRADYCARDIA("Bradycardia", "40–59 BPM"),
    ATHLETIC("Athletic Normal", "40–59 BPM"),
    NORMAL("Normal", "60–100 BPM"),
    ELEVATED("Elevated", "101–120 BPM"),
    TACHYCARDIA("Tachycardia", "121–150 BPM"),
    DANGEROUS("Dangerously High", "> 150 BPM")
}

object BpAdvancedMetrics {

    // ─── Pulse Pressure ────────────────────────────────────────────────────

    fun analyzePulsePressure(systolic: Int, diastolic: Int): PulsePressureAnalysis {
        val pp = systolic - diastolic

        val category = when {
            pp < 25 -> PpCategory.VERY_NARROW
            pp in 25..39 -> PpCategory.NARROW
            pp in 40..60 -> PpCategory.NORMAL
            pp in 61..80 -> PpCategory.SLIGHTLY_WIDE
            pp in 81..100 -> PpCategory.WIDE
            else -> PpCategory.VERY_WIDE
        }

        val interpretation = when (category) {
            PpCategory.VERY_NARROW -> "Very narrow pulse pressure detected"
            PpCategory.NARROW -> "Pulse pressure is below normal range"
            PpCategory.NORMAL -> "Pulse pressure is within the healthy range"
            PpCategory.SLIGHTLY_WIDE -> "Pulse pressure is slightly above normal"
            PpCategory.WIDE -> "Wide pulse pressure detected"
            PpCategory.VERY_WIDE -> "Very wide pulse pressure – consult a doctor"
        }

        val details = when (category) {
            PpCategory.VERY_NARROW -> "A very narrow pulse pressure (< 25 mmHg) may indicate significantly reduced cardiac output, severe heart failure, cardiac tamponade, or significant blood loss. This warrants medical evaluation."
            PpCategory.NARROW -> "A narrow pulse pressure (25–39 mmHg) may suggest reduced stroke volume or increased peripheral vascular resistance. It can occur with dehydration, heart failure, or aortic stenosis. Monitor and discuss with your healthcare provider."
            PpCategory.NORMAL -> "Your pulse pressure is in the ideal range (40–60 mmHg). This suggests good cardiovascular function with appropriate arterial compliance and cardiac output. Keep maintaining your healthy lifestyle!"
            PpCategory.SLIGHTLY_WIDE -> "A slightly widened pulse pressure (61–80 mmHg) may be normal during exercise or stress. If consistently elevated at rest, it could indicate early arterial stiffening. Worth monitoring over time."
            PpCategory.WIDE -> "A wide pulse pressure (81–100 mmHg) often indicates arterial stiffness, which is more common with aging. It can also occur with aortic regurgitation, hyperthyroidism, or anemia. Consider discussing with your doctor."
            PpCategory.VERY_WIDE -> "A very wide pulse pressure (> 100 mmHg) is a significant finding that strongly suggests arterial stiffness or another underlying condition. This is associated with increased cardiovascular risk. Medical consultation is recommended."
        }

        val normalizedPos = when {
            pp <= 0 -> 0.02f
            pp < 25 -> 0.02f + (pp / 25f) * 0.13f
            pp < 40 -> 0.15f + ((pp - 25f) / 15f) * 0.15f
            pp < 60 -> 0.30f + ((pp - 40f) / 20f) * 0.25f
            pp < 80 -> 0.55f + ((pp - 60f) / 20f) * 0.15f
            pp < 100 -> 0.70f + ((pp - 80f) / 20f) * 0.15f
            else -> 0.85f + ((pp - 100f).coerceAtMost(40f) / 40f) * 0.13f
        }.coerceIn(0.02f, 0.98f)

        return PulsePressureAnalysis(
            value = pp,
            category = category,
            normalizedPosition = normalizedPos,
            interpretation = interpretation,
            details = details,
            isNormal = category == PpCategory.NORMAL
        )
    }

    // ─── Mean Arterial Pressure ────────────────────────────────────────────

    fun analyzeMAP(systolic: Int, diastolic: Int): MapAnalysis {
        val map = diastolic + (systolic - diastolic) / 3.0
        val roundedMap = Math.round(map * 10.0) / 10.0

        val category = when {
            map < 60 -> MapCategory.CRITICALLY_LOW
            map < 70 -> MapCategory.LOW
            map <= 100 -> MapCategory.NORMAL
            map <= 110 -> MapCategory.ELEVATED
            map <= 130 -> MapCategory.HIGH
            else -> MapCategory.VERY_HIGH
        }

        val interpretation = when (category) {
            MapCategory.CRITICALLY_LOW -> "Critically low – organs may not receive adequate blood flow"
            MapCategory.LOW -> "Below normal – may indicate insufficient perfusion"
            MapCategory.NORMAL -> "Normal – adequate blood flow to organs"
            MapCategory.ELEVATED -> "Slightly elevated – increased vascular resistance"
            MapCategory.HIGH -> "High – significant risk of organ damage"
            MapCategory.VERY_HIGH -> "Very high – urgent risk of organ damage"
        }

        val details = when (category) {
            MapCategory.CRITICALLY_LOW -> "A MAP below 60 mmHg is considered a medical concern. At this level, vital organs (brain, kidneys, heart) may not receive enough blood flow to function properly. This can lead to organ damage if sustained. Possible causes include severe dehydration, blood loss, sepsis, or heart failure."
            MapCategory.LOW -> "A MAP of 60–69 mmHg is at the lower boundary of adequate organ perfusion. While some people function normally at this level, it may indicate mild hypotension. Watch for symptoms like dizziness, fatigue, or confusion."
            MapCategory.NORMAL -> "Your MAP is within the ideal range (70–100 mmHg). This means your heart is effectively pumping blood and your organs are receiving adequate perfusion. A MAP in this range is associated with good cardiovascular health."
            MapCategory.ELEVATED -> "A MAP of 101–110 mmHg suggests mildly elevated average arterial pressure. This can indicate increased vascular resistance or early hypertension. Lifestyle modifications may help bring this into the normal range."
            MapCategory.HIGH -> "A MAP above 110 mmHg indicates significantly elevated average arterial pressure. Sustained high MAP can damage blood vessel walls, kidneys, brain, and eyes. This often correlates with Stage 2+ hypertension."
            MapCategory.VERY_HIGH -> "A MAP above 130 mmHg is dangerously elevated. At this level, there is significant risk of immediate organ damage including stroke, kidney injury, and heart damage. Medical attention is strongly recommended."
        }

        val normalizedPos = when {
            map < 40 -> 0.02f
            map < 60 -> 0.02f + ((map - 40) / 20.0 * 0.15).toFloat()
            map < 70 -> 0.17f + ((map - 60) / 10.0 * 0.13).toFloat()
            map <= 100 -> 0.30f + ((map - 70) / 30.0 * 0.30).toFloat()
            map <= 110 -> 0.60f + ((map - 100) / 10.0 * 0.12).toFloat()
            map <= 130 -> 0.72f + ((map - 110) / 20.0 * 0.14).toFloat()
            else -> 0.86f + (((map - 130).coerceAtMost(30.0)) / 30.0 * 0.12).toFloat()
        }.coerceIn(0.02f, 0.98f)

        return MapAnalysis(
            value = roundedMap,
            category = category,
            normalizedPosition = normalizedPos,
            interpretation = interpretation,
            details = details,
            isNormal = category == MapCategory.NORMAL
        )
    }

    // ─── Heart Rate ────────────────────────────────────────────────────────

    fun analyzeHeartRate(bpm: Int, isAthlete: Boolean = false): HeartRateAnalysis {
        val category = when {
            bpm < 40 -> HrCategory.SEVERELY_LOW
            bpm in 40..59 && isAthlete -> HrCategory.ATHLETIC
            bpm in 40..59 -> HrCategory.BRADYCARDIA
            bpm in 60..100 -> HrCategory.NORMAL
            bpm in 101..120 -> HrCategory.ELEVATED
            bpm in 121..150 -> HrCategory.TACHYCARDIA
            else -> HrCategory.DANGEROUS
        }

        val interpretation = when (category) {
            HrCategory.SEVERELY_LOW -> "Severely low heart rate"
            HrCategory.BRADYCARDIA -> "Below normal resting heart rate"
            HrCategory.ATHLETIC -> "Normal for trained athletes"
            HrCategory.NORMAL -> "Normal resting heart rate"
            HrCategory.ELEVATED -> "Slightly elevated heart rate"
            HrCategory.TACHYCARDIA -> "Elevated heart rate (tachycardia)"
            HrCategory.DANGEROUS -> "Dangerously high heart rate"
        }

        val details = when (category) {
            HrCategory.SEVERELY_LOW -> "A heart rate below 40 BPM at rest is very low and may indicate a serious conduction problem or other cardiac issue. Seek medical evaluation, especially if accompanied by dizziness, fainting, or shortness of breath."
            HrCategory.BRADYCARDIA -> "A resting heart rate of 40–59 BPM is classified as bradycardia. While this can be normal for very fit individuals, in non-athletes it may indicate an underactive thyroid, medication effects, or a conduction disorder. If you experience symptoms, consult your doctor."
            HrCategory.ATHLETIC -> "For trained athletes, a resting heart rate of 40–59 BPM is completely normal and actually indicates excellent cardiovascular fitness. Your heart is so efficient that it can pump sufficient blood with fewer beats."
            HrCategory.NORMAL -> "Your resting heart rate is within the normal range (60–100 BPM). A lower resting heart rate generally indicates better cardiovascular fitness. Most healthy adults fall in the 60–80 BPM range."
            HrCategory.ELEVATED -> "A resting heart rate of 101–120 BPM is mildly elevated. This can be caused by stress, caffeine, dehydration, recent physical activity, fever, or anxiety. If consistently elevated at rest, discuss with your healthcare provider."
            HrCategory.TACHYCARDIA -> "A resting heart rate above 120 BPM is classified as tachycardia. Possible causes include anxiety, caffeine, fever, anemia, hyperthyroidism, or cardiac arrhythmia. If this is a resting rate, medical evaluation is recommended."
            HrCategory.DANGEROUS -> "A resting heart rate above 150 BPM is dangerously elevated. This may indicate a serious cardiac arrhythmia or other medical emergency. Seek immediate medical attention, especially if accompanied by chest pain, shortness of breath, or dizziness."
        }

        val riskFactors = when (category) {
            HrCategory.SEVERELY_LOW -> listOf(
                "Risk of fainting or syncope",
                "Potential organ hypoperfusion",
                "May need pacemaker evaluation"
            )
            HrCategory.BRADYCARDIA -> listOf(
                "May cause fatigue or dizziness",
                "Could indicate medication side effects",
                "Check thyroid function if persistent"
            )
            HrCategory.ATHLETIC -> listOf(
                "Sign of excellent cardiovascular fitness",
                "Normal if asymptomatic",
                "Continue regular exercise routine"
            )
            HrCategory.NORMAL -> listOf(
                "Healthy cardiovascular function",
                "Lower end of range indicates better fitness",
                "Regular exercise can lower resting HR"
            )
            HrCategory.ELEVATED -> listOf(
                "May indicate dehydration or stress",
                "Could be caffeine or stimulant related",
                "Higher resting HR linked to increased CV risk"
            )
            HrCategory.TACHYCARDIA -> listOf(
                "Increased cardiac workload",
                "Higher risk of cardiac events",
                "May indicate underlying condition"
            )
            HrCategory.DANGEROUS -> listOf(
                "Immediate cardiac risk",
                "Possible arrhythmia",
                "Seek emergency care if symptomatic"
            )
        }

        val normalizedPos = when {
            bpm < 30 -> 0.02f
            bpm < 40 -> 0.02f + ((bpm - 30f) / 10f) * 0.08f
            bpm < 60 -> 0.10f + ((bpm - 40f) / 20f) * 0.18f
            bpm <= 100 -> 0.28f + ((bpm - 60f) / 40f) * 0.35f
            bpm <= 120 -> 0.63f + ((bpm - 100f) / 20f) * 0.12f
            bpm <= 150 -> 0.75f + ((bpm - 120f) / 30f) * 0.13f
            else -> 0.88f + ((bpm - 150f).coerceAtMost(50f) / 50f) * 0.10f
        }.coerceIn(0.02f, 0.98f)

        return HeartRateAnalysis(
            bpm = bpm,
            category = category,
            normalizedPosition = normalizedPos,
            interpretation = interpretation,
            details = details,
            isNormal = category == HrCategory.NORMAL || category == HrCategory.ATHLETIC,
            riskFactors = riskFactors
        )
    }
}
