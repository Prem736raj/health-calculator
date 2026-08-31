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
    VERY_NARROW("Very narrow", "< 25 mmHg"),
    NARROW("Narrow", "25–39 mmHg"),
    NORMAL("Common reference", "40–60 mmHg"),
    SLIGHTLY_WIDE("Somewhat wide", "61–80 mmHg"),
    WIDE("Wide", "81–100 mmHg"),
    VERY_WIDE("Very wide", "> 100 mmHg")
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
    CRITICALLY_LOW("Markedly low", "< 60 mmHg"),
    LOW("Low", "60–69 mmHg"),
    NORMAL("Normal", "70–100 mmHg"),
    ELEVATED("Elevated", "101–110 mmHg"),
    HIGH("High", "111–130 mmHg"),
    VERY_HIGH("Very high", "> 130 mmHg")
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
    SEVERELY_LOW("Very low", "< 40 BPM"),
    BRADYCARDIA("Below common range", "40–59 BPM"),
    ATHLETIC("Lower range", "40–59 BPM"),
    NORMAL("Common reference", "60–100 BPM"),
    ELEVATED("Elevated", "101–120 BPM"),
    TACHYCARDIA("Markedly elevated", "121–150 BPM"),
    DANGEROUS("Very high", "> 150 BPM")
}

object BpAdvancedMetrics {

    // ─── Pulse Pressure ────────────────────────────────────────────────────

    fun analyzePulsePressure(systolic: Int, diastolic: Int): PulsePressureAnalysis {
        require(systolic > diastolic && systolic in 60..300 && diastolic in 30..200) {
            "Enter a valid blood-pressure pair"
        }
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
            PpCategory.VERY_NARROW -> "Pulse pressure is in a very narrow reference band"
            PpCategory.NARROW -> "Pulse pressure is below the common reference band"
            PpCategory.NORMAL -> "Pulse pressure is within the common reference band"
            PpCategory.SLIGHTLY_WIDE -> "Pulse pressure is above the common reference band"
            PpCategory.WIDE -> "Pulse pressure is in a wide reference band"
            PpCategory.VERY_WIDE -> "Pulse pressure is in a very wide reference band; repeat and discuss persistent results"
        }

        val details = when (category) {
            PpCategory.VERY_NARROW -> "Pulse pressure is sensitive to measurement error and context. Repeat the pair carefully and seek professional advice if it persists or symptoms concern you."
            PpCategory.NARROW -> "Repeat the blood-pressure pair under consistent conditions; a single pulse-pressure value cannot identify a cause."
            PpCategory.NORMAL -> "This range is commonly used for reference. Pulse pressure alone does not describe overall cardiovascular health."
            PpCategory.SLIGHTLY_WIDE -> "Exercise, stress, age and technique can affect this value. Use repeated readings and personal context."
            PpCategory.WIDE -> "A persistent wide value deserves discussion with a healthcare professional, especially if other readings or symptoms are concerning."
            PpCategory.VERY_WIDE -> "Repeat the pair carefully and discuss persistent results with a healthcare professional; this metric cannot diagnose a condition."
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
        require(systolic > diastolic && systolic in 60..300 && diastolic in 30..200) {
            "Enter a valid blood-pressure pair"
        }
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
            MapCategory.CRITICALLY_LOW -> "Markedly low reference value"
            MapCategory.LOW -> "Below common reference band"
            MapCategory.NORMAL -> "Within common reference band"
            MapCategory.ELEVATED -> "Above common reference band"
            MapCategory.HIGH -> "High reference value"
            MapCategory.VERY_HIGH -> "Very high reference value"
        }

        val details = when (category) {
            MapCategory.CRITICALLY_LOW -> "MAP is an approximate derived value and is sensitive to the input pair. Repeat carefully and seek prompt professional advice if it remains very low or symptoms are present."
            MapCategory.LOW -> "Some people have lower values without a problem. Consider symptoms, repeat readings and personal context."
            MapCategory.NORMAL -> "This is a commonly used reference band; MAP alone cannot establish overall cardiovascular health."
            MapCategory.ELEVATED -> "Repeat the blood-pressure pair under consistent conditions and review the trend rather than inferring a cause."
            MapCategory.HIGH -> "A persistent high value merits professional review, particularly alongside repeated high blood-pressure readings."
            MapCategory.VERY_HIGH -> "Repeat the pair carefully and seek prompt professional advice if it remains very high or symptoms are present."
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
        require(bpm in 30..250) { "Resting heart rate must be between 30 and 250 BPM" }
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
            HrCategory.SEVERELY_LOW -> "Very low resting heart rate"
            HrCategory.BRADYCARDIA -> "Below common resting reference"
            HrCategory.ATHLETIC -> "Lower resting reference (athlete context)"
            HrCategory.NORMAL -> "Within common resting reference"
            HrCategory.ELEVATED -> "Elevated resting reference"
            HrCategory.TACHYCARDIA -> "Markedly elevated resting reference"
            HrCategory.DANGEROUS -> "Very high resting reference"
        }

        val details = when (category) {
            HrCategory.SEVERELY_LOW -> "A very low resting value can be affected by measurement conditions or personal context. Repeat it and seek professional advice if persistent or symptomatic."
            HrCategory.BRADYCARDIA -> "A lower resting value can be normal for some people and relevant for others. Consider symptoms, medicines, training and repeated readings."
            HrCategory.ATHLETIC -> "A lower value may occur in trained people, but this label is not a fitness diagnosis. Use symptoms and context."
            HrCategory.NORMAL -> "This is a common adult resting reference band. Resting heart rate varies with sleep, stress, illness, medicines and fitness."
            HrCategory.ELEVATED -> "Stress, caffeine, illness, dehydration or recent activity can affect a resting value. Repeat when rested and discuss persistent elevation."
            HrCategory.TACHYCARDIA -> "A markedly elevated resting value deserves repeat measurement and professional review, especially with symptoms."
            HrCategory.DANGEROUS -> "A very high resting value needs prompt confirmation and professional advice, particularly with chest discomfort, breathlessness, fainting or other concerning symptoms."
        }

        val riskFactors = when (category) {
            HrCategory.SEVERELY_LOW -> listOf(
                "Symptoms such as dizziness or fainting can matter",
                "Repeat measurement and context are important",
                "Discuss persistent or symptomatic readings with a professional"
            )
            HrCategory.BRADYCARDIA -> listOf(
                "Symptoms such as fatigue or dizziness can matter",
                "Medicines and training can affect readings",
                "Discuss persistent readings with a professional"
            )
            HrCategory.ATHLETIC -> listOf(
                "Can occur in trained people",
                "Use symptoms and personal context",
                "Track under similar conditions"
            )
            HrCategory.NORMAL -> listOf(
                "Common adult resting reference",
                "Values vary day to day",
                "Track under similar conditions"
            )
            HrCategory.ELEVATED -> listOf(
                "Stress, caffeine and illness can affect it",
                "Measurement timing matters",
                "Review a repeated pattern rather than one value"
            )
            HrCategory.TACHYCARDIA -> listOf(
                "Repeat when rested",
                "Symptoms and medicines provide context",
                "Discuss a repeated pattern with a professional"
            )
            HrCategory.DANGEROUS -> listOf(
                "Repeat and verify the measurement",
                "Symptoms change the appropriate next step",
                "Seek prompt professional advice if persistent or symptomatic"
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
