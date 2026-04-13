package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.HeightUnit
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import com.health.calculator.bmi.tracker.data.model.ProfileData
import com.health.calculator.bmi.tracker.data.model.WeightUnit
import com.health.calculator.bmi.tracker.data.model.calculateAge
import com.health.calculator.bmi.tracker.data.model.cmToFeetInches
import com.health.calculator.bmi.tracker.data.model.feetInchesToCm
import com.health.calculator.bmi.tracker.data.model.kgToLbs
import com.health.calculator.bmi.tracker.data.model.lbsToKg
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.health.calculator.bmi.tracker.data.model.BMIGoalData
import com.health.calculator.bmi.tracker.data.model.BMIHealthRiskProvider
import com.health.calculator.bmi.tracker.data.preferences.BMIGoalPreferences
import com.health.calculator.bmi.tracker.data.preferences.BMIInputMemoryPreferences
import com.health.calculator.bmi.tracker.data.preferences.BMILastUsedInput
import com.health.calculator.bmi.tracker.data.model.BMIValidationState
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.BMIEdgeCaseHandler
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.EdgeCaseMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow
import kotlin.math.roundToInt

// ─── Validation Constants ─────────────────────────────────────────────────────

private const val MIN_WEIGHT_KG = 2.0
private const val MAX_WEIGHT_KG = 500.0
private const val MIN_HEIGHT_CM = 30.0
private const val MAX_HEIGHT_CM = 300.0
private const val MIN_AGE = 2
private const val MAX_AGE = 120
private const val MAX_FEET = 9
private const val MAX_INCHES = 11.9

// ─── BMI WHO Categories ───────────────────────────────────────────────────────

/**
 * WHO BMI classification categories with their value ranges,
 * display colors, and health risk descriptions.
 */
enum class BmiCategory(
    val label: String,
    val shortLabel: String,
    val minBmi: Double,
    val maxBmi: Double,
    val colorHex: Long,
    val riskLevel: String,
    val riskDescription: String,
    val emoji: String
) {
    SEVERE_THINNESS(
        label = "Severe Thinness",
        shortLabel = "Severe Thin",
        minBmi = 0.0,
        maxBmi = 16.0,
        colorHex = 0xFFB71C1C,
        riskLevel = "Very High",
        riskDescription = "Very high risk of malnutrition and related health complications",
        emoji = "🔴"
    ),
    MODERATE_THINNESS(
        label = "Moderate Thinness",
        shortLabel = "Mod. Thin",
        minBmi = 16.0,
        maxBmi = 17.0,
        colorHex = 0xFFE53935,
        riskLevel = "High",
        riskDescription = "Increased risk of nutritional deficiency and weakened immunity",
        emoji = "🟠"
    ),
    MILD_THINNESS(
        label = "Mild Thinness",
        shortLabel = "Mild Thin",
        minBmi = 17.0,
        maxBmi = 18.5,
        colorHex = 0xFFFF9800,
        riskLevel = "Moderate",
        riskDescription = "Slightly increased risk; may need nutritional attention",
        emoji = "🟡"
    ),
    NORMAL(
        label = "Normal Weight",
        shortLabel = "Normal",
        minBmi = 18.5,
        maxBmi = 25.0,
        colorHex = 0xFF43A047,
        riskLevel = "Low",
        riskDescription = "Healthy BMI range associated with lowest health risk",
        emoji = "🟢"
    ),
    OVERWEIGHT(
        label = "Overweight",
        shortLabel = "Overweight",
        minBmi = 25.0,
        maxBmi = 30.0,
        colorHex = 0xFFFFC107,
        riskLevel = "Increased",
        riskDescription = "Increased risk of cardiovascular disease and type 2 diabetes",
        emoji = "🟡"
    ),
    OBESE_CLASS_I(
        label = "Obese Class I",
        shortLabel = "Obese I",
        minBmi = 30.0,
        maxBmi = 35.0,
        colorHex = 0xFFFF9800,
        riskLevel = "High",
        riskDescription = "High risk of metabolic syndrome, heart disease, and diabetes",
        emoji = "🟠"
    ),
    OBESE_CLASS_II(
        label = "Obese Class II",
        shortLabel = "Obese II",
        minBmi = 35.0,
        maxBmi = 40.0,
        colorHex = 0xFFE53935,
        riskLevel = "Very High",
        riskDescription = "Very high risk of serious health conditions",
        emoji = "🔴"
    ),
    OBESE_CLASS_III(
        label = "Obese Class III",
        shortLabel = "Obese III",
        minBmi = 40.0,
        maxBmi = 100.0,
        colorHex = 0xFFB71C1C,
        riskLevel = "Extremely High",
        riskDescription = "Extremely high risk of life-threatening health conditions",
        emoji = "🔴"
    );

    companion object {
        fun fromBmi(bmi: Double): BmiCategory {
            return entries.find { bmi >= it.minBmi && bmi < it.maxBmi }
                ?: if (bmi >= 40.0) OBESE_CLASS_III else SEVERE_THINNESS
        }
    }
}

// ─── BMI Result Data ──────────────────────────────────────────────────────────

/**
 * Holds all calculated BMI result values for display.
 */
data class BmiResult(
    /** The calculated BMI value */
    val bmiValue: Double = 0.0,

    /** WHO classification category */
    val category: BmiCategory = BmiCategory.NORMAL,

    /** Lower bound of healthy weight for this height (kg) */
    val healthyWeightMinKg: Double = 0.0,

    /** Upper bound of healthy weight for this height (kg) */
    val healthyWeightMaxKg: Double = 0.0,

    /** Weight difference needed to reach normal BMI (negative = lose, positive = gain) */
    val weightDifferenceKg: Double = 0.0,

    /** Target weight to reach nearest normal BMI boundary */
    val targetWeightKg: Double = 0.0,

    /** Whether user needs to gain, lose, or maintain weight */
    val weightAdvice: WeightAdvice = WeightAdvice.MAINTAIN,

    /** Input values used for display on result screen */
    val inputWeightKg: Double = 0.0,
    val inputHeightCm: Double = 0.0,
    val inputAge: Int = 0,
    val inputGender: Gender = Gender.NOT_SET,
    val displayWeightUnit: WeightUnit = WeightUnit.KG,
    /** Additional metrics: BMI Prime, Ponderal Index, Asian BMI */
    val additionalMetrics: AdditionalBmiMetrics = AdditionalBmiMetrics(),

    /** ID of the saved history entry, null if not yet saved */
    val historyEntryId: Long? = null,

    /** Age/gender comparison data */
    val comparisonData: BmiComparisonData = BmiComparisonData()
)

enum class WeightAdvice {
    GAIN, LOSE, MAINTAIN
}

// ─── BMI Prime ────────────────────────────────────────────────────────────────

/**
 * BMI Prime is the ratio of actual BMI to the upper limit of normal BMI (25.0).
 * A value of 1.0 means exactly at the upper boundary of normal weight.
 *
 * Interpretation:
 * - < 1.0 = Below upper normal limit
 * - = 1.0 = At upper normal limit
 * - > 1.0 = Above upper normal limit
 */
data class BmiPrimeResult(
    /** BMI Prime value (BMI / 25.0) */
    val value: Double = 0.0,

    /** Status interpretation */
    val status: BmiPrimeStatus = BmiPrimeStatus.UNDER,

    /** Percentage deviation from 1.0 */
    val deviationPercent: Double = 0.0
)

enum class BmiPrimeStatus(
    val label: String,
    val description: String,
    val colorHex: Long
) {
    UNDER(
        label = "Below Upper Normal",
        description = "Your BMI is below the upper limit of the normal range",
        colorHex = 0xFF43A047
    ),
    AT_LIMIT(
        label = "At Upper Normal Limit",
        description = "Your BMI is right at the upper boundary of normal weight",
        colorHex = 0xFFFFC107
    ),
    OVER(
        label = "Above Upper Normal",
        description = "Your BMI exceeds the upper limit of the normal range",
        colorHex = 0xFFE53935
    )
}

// ─── Ponderal Index ───────────────────────────────────────────────────────────

/**
 * The Ponderal Index (PI), also known as Corpulence Index, is calculated as:
 * PI = mass(kg) / height(m)³
 *
 * Unlike BMI, PI is less affected by height, making it more useful
 * for comparing body composition across different heights.
 *
 * Normal range: approximately 11–15 kg/m³
 */
data class PonderalIndexResult(
    /** Ponderal Index value */
    val value: Double = 0.0,

    /** Status interpretation */
    val status: PonderalStatus = PonderalStatus.NORMAL
)

enum class PonderalStatus(
    val label: String,
    val description: String,
    val colorHex: Long
) {
    LOW(
        label = "Below Normal",
        description = "May indicate low body mass relative to height",
        colorHex = 0xFFFF9800
    ),
    NORMAL(
        label = "Normal Range",
        description = "Healthy body mass to height proportion",
        colorHex = 0xFF43A047
    ),
    HIGH(
        label = "Above Normal",
        description = "May indicate high body mass relative to height",
        colorHex = 0xFFE53935
    )
}

// ─── Asian BMI Categories ─────────────────────────────────────────────────────

/**
 * WHO adjusted BMI categories for Asian populations.
 * Research has shown that Asian populations have higher health risks
 * at lower BMI values compared to European populations.
 *
 * These adjusted cutoffs are recommended by WHO for populations
 * of Asian descent.
 */
enum class AsianBmiCategory(
    val label: String,
    val minBmi: Double,
    val maxBmi: Double,
    val colorHex: Long,
    val riskLevel: String
) {
    UNDERWEIGHT(
        label = "Underweight",
        minBmi = 0.0,
        maxBmi = 18.5,
        colorHex = 0xFFFF9800,
        riskLevel = "Moderate"
    ),
    NORMAL(
        label = "Normal",
        minBmi = 18.5,
        maxBmi = 23.0,
        colorHex = 0xFF43A047,
        riskLevel = "Low"
    ),
    OVERWEIGHT(
        label = "Overweight",
        minBmi = 23.0,
        maxBmi = 25.0,
        colorHex = 0xFFFFC107,
        riskLevel = "Increased"
    ),
    OBESE_CLASS_I(
        label = "Obese Class I",
        minBmi = 25.0,
        maxBmi = 30.0,
        colorHex = 0xFFFF9800,
        riskLevel = "High"
    ),
    OBESE_CLASS_II(
        label = "Obese Class II",
        minBmi = 30.0,
        maxBmi = 100.0,
        colorHex = 0xFFE53935,
        riskLevel = "Very High"
    );

    companion object {
        fun fromBmi(bmi: Double): AsianBmiCategory {
            return entries.find { bmi >= it.minBmi && bmi < it.maxBmi }
                ?: if (bmi >= 30.0) OBESE_CLASS_II else UNDERWEIGHT
        }
    }
}

/**
 * Holds all additional BMI-related metrics calculated alongside the primary BMI.
 */
data class AdditionalBmiMetrics(
    val bmiPrime: BmiPrimeResult = BmiPrimeResult(),
    val ponderalIndex: PonderalIndexResult = PonderalIndexResult(),
    val asianCategory: AsianBmiCategory = AsianBmiCategory.NORMAL
)

// ─── BMI Trend Data ───────────────────────────────────────────────────────────

/**
 * A single data point in the BMI trend graph.
 */
data class BmiTrendPoint(
    val id: Long,
    val bmiValue: Double,
    val category: BmiCategory,
    val timestamp: Long,
    val inputSummary: String
)

/**
 * Statistics computed from all BMI history entries.
 */
data class BmiTrendStats(
    val currentBmi: Double = 0.0,
    val currentCategory: BmiCategory = BmiCategory.NORMAL,
    val lowestBmi: Double = 0.0,
    val highestBmi: Double = 0.0,
    val averageBmi: Double = 0.0,
    val totalReadings: Int = 0,
    val changeFromPrevious: Double? = null,
    val previousBmi: Double? = null,
    val previousTimestamp: Long? = null
)

/**
 * Complete trend data for the BMI trend section.
 */
data class BmiTrendData(
    val points: List<BmiTrendPoint> = emptyList(),
    val stats: BmiTrendStats = BmiTrendStats(),
    val isLoading: Boolean = true,
    val hasEnoughData: Boolean = false
)

// ─── BMI Age/Gender Comparison ────────────────────────────────────────────────

/**
 * Age group classification for BMI interpretation.
 */
enum class AgeGroupType {
    CHILD,    // 2-9 years
    TEEN,     // 10-19 years
    YOUNG_ADULT,  // 20-39 years
    MIDDLE_ADULT, // 40-59 years
    SENIOR    // 60+ years
}

/**
 * WHO BMI-for-age percentile category for children/teens (ages 2-19).
 */
enum class PediatricBmiCategory(
    val label: String,
    val description: String,
    val colorHex: Long,
    val emoji: String
) {
    UNDERWEIGHT(
        label = "Underweight",
        description = "Below 5th percentile",
        colorHex = 0xFFFF9800,
        emoji = "🟡"
    ),
    NORMAL(
        label = "Healthy Weight",
        description = "5th to 85th percentile",
        colorHex = 0xFF43A047,
        emoji = "🟢"
    ),
    OVERWEIGHT(
        label = "Overweight",
        description = "85th to 95th percentile",
        colorHex = 0xFFFFC107,
        emoji = "🟡"
    ),
    OBESE(
        label = "Obese",
        description = "Above 95th percentile",
        colorHex = 0xFFE53935,
        emoji = "🔴"
    )
}

/**
 * Population average BMI data by age group and gender.
 * Based on WHO and CDC population survey data.
 */
data class PopulationAverage(
    val ageGroupLabel: String,
    val maleAvgBmi: Double,
    val femaleAvgBmi: Double,
    val maleNormalRange: Pair<Double, Double>,
    val femaleNormalRange: Pair<Double, Double>
)

/**
 * Complete comparison data for the BMI comparison section.
 */
data class BmiComparisonData(
    /** Whether the user is in the pediatric age range (2-19) */
    val isPediatric: Boolean = false,

    /** Age group classification */
    val ageGroup: AgeGroupType = AgeGroupType.YOUNG_ADULT,

    /** Age group display label */
    val ageGroupLabel: String = "",

    /** Estimated percentile for pediatric users */
    val estimatedPercentile: Double = 50.0,

    /** Pediatric BMI category */
    val pediatricCategory: PediatricBmiCategory = PediatricBmiCategory.NORMAL,

    /** Population average BMI for the user's age/gender group */
    val populationAvgBmi: Double = 0.0,

    /** How the user's BMI compares to population average */
    val differenceFromAvg: Double = 0.0,

    /** Normal BMI range for this age/gender group */
    val recommendedRange: Pair<Double, Double> = Pair(18.5, 24.9),

    /** Whether user's BMI is within the recommended range */
    val isWithinRange: Boolean = false,

    /** Age-specific interpretation note */
    val ageContextNote: String = "",

    /** Gender used for comparison */
    val gender: Gender = Gender.NOT_SET
)

/**
 * Lookup table for population average BMI by age group.
 * Sources: WHO Global Health Observatory, CDC NHANES data
 */
private val populationAverages = listOf(
    PopulationAverage("2-5 years", 15.8, 15.6, Pair(13.5, 17.0), Pair(13.4, 17.0)),
    PopulationAverage("6-9 years", 16.5, 16.3, Pair(13.8, 18.5), Pair(13.6, 18.5)),
    PopulationAverage("10-13 years", 18.8, 19.0, Pair(15.5, 21.5), Pair(15.8, 22.0)),
    PopulationAverage("14-17 years", 21.2, 21.5, Pair(17.5, 24.0), Pair(17.8, 24.5)),
    PopulationAverage("18-19 years", 22.8, 22.2, Pair(18.5, 25.0), Pair(18.5, 25.0)),
    PopulationAverage("20-29 years", 24.2, 23.8, Pair(18.5, 25.0), Pair(18.5, 25.0)),
    PopulationAverage("30-39 years", 25.5, 25.1, Pair(18.5, 25.0), Pair(18.5, 25.0)),
    PopulationAverage("40-49 years", 26.3, 26.0, Pair(18.5, 25.0), Pair(18.5, 25.0)),
    PopulationAverage("50-59 years", 26.8, 27.0, Pair(18.5, 25.0), Pair(18.5, 25.0)),
    PopulationAverage("60-69 years", 26.5, 27.2, Pair(18.5, 27.0), Pair(18.5, 27.0)),
    PopulationAverage("70-79 years", 25.8, 26.5, Pair(20.0, 27.0), Pair(20.0, 27.0)),
    PopulationAverage("80+ years", 24.8, 25.5, Pair(20.0, 27.0), Pair(20.0, 27.0))
)

/**
 * Approximate BMI-for-age percentile lookup for pediatric BMI (ages 2-19).
 * Simplified from WHO growth reference data.
 * Returns estimated percentile based on age, gender, and BMI.
 */
private fun estimatePediatricPercentile(age: Int, gender: Gender, bmi: Double): Double {
    // Approximate median (50th percentile) BMI by age/gender
    data class RefPoint(val age: Int, val male50th: Double, val female50th: Double, val sd: Double)
    
    val refs = listOf(
        RefPoint(2, 16.4, 16.0, 1.4),
        RefPoint(3, 15.8, 15.5, 1.3),
        RefPoint(4, 15.5, 15.3, 1.3),
        RefPoint(5, 15.3, 15.2, 1.4),
        RefPoint(6, 15.4, 15.3, 1.6),
        RefPoint(7, 15.6, 15.5, 1.8),
        RefPoint(8, 15.9, 16.0, 2.0),
        RefPoint(9, 16.3, 16.4, 2.2),
        RefPoint(10, 16.8, 17.0, 2.4),
        RefPoint(11, 17.4, 17.7, 2.6),
        RefPoint(12, 18.0, 18.4, 2.8),
        RefPoint(13, 18.7, 19.1, 2.9),
        RefPoint(14, 19.4, 19.7, 3.0),
        RefPoint(15, 20.0, 20.2, 3.1),
        RefPoint(16, 20.6, 20.6, 3.1),
        RefPoint(17, 21.2, 21.0, 3.1),
        RefPoint(18, 21.8, 21.3, 3.1),
        RefPoint(19, 22.3, 21.6, 3.1)
    )

    val ref = refs.find { it.age == age } ?: refs.last()
    val median = if (gender == Gender.FEMALE) ref.female50th else ref.male50th
    
    // Z-score approximation
    val zScore = (bmi - median) / ref.sd
    
    // Convert z-score to approximate percentile using sigmoid
    val percentile = (100.0 / (1.0 + Math.exp(-1.7 * zScore)))
    return percentile.coerceIn(0.1, 99.9)
}

/**
 * Determines pediatric BMI category from percentile.
 */
private fun percentileToPediatricCategory(percentile: Double): PediatricBmiCategory {
    return when {
        percentile < 5.0 -> PediatricBmiCategory.UNDERWEIGHT
        percentile < 85.0 -> PediatricBmiCategory.NORMAL
        percentile < 95.0 -> PediatricBmiCategory.OVERWEIGHT
        else -> PediatricBmiCategory.OBESE
    }
}

/**
 * Calculates comparison data for the given BMI result.
 */
fun calculateBmiComparison(bmi: Double, age: Int, gender: Gender): BmiComparisonData {
    val isPediatric = age in 2..19
    
    val ageGroup = when (age) {
        in 2..9 -> AgeGroupType.CHILD
        in 10..19 -> AgeGroupType.TEEN
        in 20..39 -> AgeGroupType.YOUNG_ADULT
        in 40..59 -> AgeGroupType.MIDDLE_ADULT
        else -> AgeGroupType.SENIOR
    }

    // Find population average for this age group
    val popAvg = when (age) {
        in 2..5 -> populationAverages[0]
        in 6..9 -> populationAverages[1]
        in 10..13 -> populationAverages[2]
        in 14..17 -> populationAverages[3]
        in 18..19 -> populationAverages[4]
        in 20..29 -> populationAverages[5]
        in 30..39 -> populationAverages[6]
        in 40..49 -> populationAverages[7]
        in 50..59 -> populationAverages[8]
        in 60..69 -> populationAverages[9]
        in 70..79 -> populationAverages[10]
        else -> populationAverages[11]
    }

    val avgBmi = if (gender == Gender.FEMALE) popAvg.femaleAvgBmi else popAvg.maleAvgBmi
    val normalRange = if (gender == Gender.FEMALE) popAvg.femaleNormalRange else popAvg.maleNormalRange
    val differenceFromAvg = ((bmi - avgBmi) * 10).roundToInt() / 10.0

    // Pediatric-specific calculations
    val percentile = if (isPediatric) estimatePediatricPercentile(age, gender, bmi) else 50.0
    val pediatricCategory = if (isPediatric) percentileToPediatricCategory(percentile) else PediatricBmiCategory.NORMAL

    val isWithinRange = bmi >= normalRange.first && bmi <= normalRange.second

    val ageContextNote = when {
        isPediatric && age < 10 -> "For children aged \${age}, BMI is evaluated using age-and-sex-specific growth charts rather than fixed adult cutoffs. A child's BMI naturally changes as they grow."
        isPediatric -> "For teens aged \${age}, BMI is compared to others of the same age and sex using percentile charts. Normal BMI values change significantly during adolescence."
        age in 20..39 -> "Standard WHO adult BMI categories apply. This age group typically has the most stable body composition for BMI assessment."
        age in 40..59 -> "BMI may slightly underestimate body fat in this age group as muscle mass tends to decrease while fat mass increases. Consider waist circumference as an additional measure."
        age in 60..69 -> "For older adults, a slightly higher BMI (up to 27) may actually be associated with better health outcomes. Being underweight carries higher risks in this age group."
        else -> "For adults over 70, maintaining adequate weight is especially important. A BMI below 20 may indicate nutritional concerns. Consult your doctor about your ideal weight range."
    }

    val ageGroupLabel = when (ageGroup) {
        AgeGroupType.CHILD -> "Child (\${age} years)"
        AgeGroupType.TEEN -> "Teenager (\${age} years)"
        AgeGroupType.YOUNG_ADULT -> "Young Adult (\${age} years)"
        AgeGroupType.MIDDLE_ADULT -> "Middle-aged Adult (\${age} years)"
        AgeGroupType.SENIOR -> "Senior Adult (\${age} years)"
    }

    return BmiComparisonData(
        isPediatric = isPediatric,
        ageGroup = ageGroup,
        ageGroupLabel = ageGroupLabel,
        estimatedPercentile = (percentile * 10).roundToInt() / 10.0,
        pediatricCategory = pediatricCategory,
        populationAvgBmi = avgBmi,
        differenceFromAvg = differenceFromAvg,
        recommendedRange = normalRange,
        isWithinRange = isWithinRange,
        ageContextNote = ageContextNote,
        gender = gender
    )
}

// ─── Input Field State ────────────────────────────────────────────────────────

data class InputFieldState(
    val text: String = "",
    val isError: Boolean = false,
    val errorMessage: String = "",
    val isFromProfile: Boolean = false,
    val wasOverridden: Boolean = false
)

// ─── UI State ─────────────────────────────────────────────────────────────────

data class BmiInputUiState(
    // ── Weight Fields ─────────────────────────────────────────────────
    val weightField: InputFieldState = InputFieldState(),
    val weightUnit: WeightUnit = WeightUnit.KG,

    // ── Height Fields ─────────────────────────────────────────────────
    val heightCmField: InputFieldState = InputFieldState(),
    val heightFeetField: InputFieldState = InputFieldState(),
    val heightInchesField: InputFieldState = InputFieldState(),
    val heightUnit: HeightUnit = HeightUnit.CM,

    // ── Age Field ─────────────────────────────────────────────────────
    val ageField: InputFieldState = InputFieldState(),

    // ── Gender Selection ──────────────────────────────────────────────
    val selectedGender: Gender = Gender.NOT_SET,
    val genderFromProfile: Boolean = false,

    // ── Internal Metric Values ────────────────────────────────────────
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0,
    val age: Int = 0,

    // ── UI State ──────────────────────────────────────────────────────
    val isLoading: Boolean = true,
    val isProfileLoaded: Boolean = false,
    val hasAnyProfileData: Boolean = false,
    val isCalculateEnabled: Boolean = false,
    val isReadyToCalculate: Boolean = false,

    // ── Result State ──────────────────────────────────────────────────
    val showResult: Boolean = false,
    val bmiResult: BmiResult? = null,
    val isCalculating: Boolean = false,

    // ── Save & Share State ────────────────────────────────────────────
    val isSavedToHistory: Boolean = false,
    val showSaveConfirmation: Boolean = false,
    val shareText: String = "",

    // ── Trend Data ────────────────────────────────────────────────────
    val trendData: BmiTrendData = BmiTrendData(),

    // ── Edge Case & Validation ─────────────────────────────────────────
    val edgeCaseMessage: EdgeCaseMessage? = null,
    val validationState: BMIValidationState = BMIValidationState()
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class BmiViewModel(
    application: Application,
    private val milestoneEvaluationUseCase: com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase
) : AndroidViewModel(application) {

    private val profileDataStore = ProfileDataStore(application.applicationContext)
    private val database = com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(application.applicationContext)
    private val historyRepository = HistoryRepository(database.historyDao())
    private val bmiGoalPreferences = BMIGoalPreferences(application.applicationContext)
    private val inputMemoryPreferences = BMIInputMemoryPreferences(application.applicationContext)

    class Factory(
        private val application: Application,
        private val milestoneEvaluationUseCase: com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BmiViewModel(application, milestoneEvaluationUseCase) as T
        }
    }

    private val _uiState = MutableStateFlow(BmiInputUiState())
    val uiState: StateFlow<BmiInputUiState> = _uiState.asStateFlow()

    val lastUsedInput: StateFlow<BMILastUsedInput> = inputMemoryPreferences.lastUsedInputFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BMILastUsedInput()
        )

    private val _useSliderInput = MutableStateFlow(false)
    val useSliderInput: StateFlow<Boolean> = _useSliderInput.asStateFlow()

    private val _sliderWeightKg = MutableStateFlow(70f)
    val sliderWeightKg: StateFlow<Float> = _sliderWeightKg.asStateFlow()

    private val _sliderHeightCm = MutableStateFlow(170f)
    val sliderHeightCm: StateFlow<Float> = _sliderHeightCm.asStateFlow()

    val bmiGoalState: StateFlow<BMIGoalData> = bmiGoalPreferences.bmiGoalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BMIGoalData()
        )

    private val _goalSaveSuccess = MutableStateFlow(false)
    val goalSaveSuccess: StateFlow<Boolean> = _goalSaveSuccess.asStateFlow()

    init {
        loadProfileData()
        loadBmiHistory()
    }

    fun setGoal(targetBMI: Float, targetWeight: Float) {
        viewModelScope.launch {
            val currentState = _uiState.value.bmiResult ?: return@launch
            val goalData = BMIGoalData(
                targetBMI = targetBMI,
                targetWeight = targetWeight,
                currentBMI = currentState.bmiValue.toFloat(),
                currentWeight = currentState.inputWeightKg.toFloat(),
                heightCm = currentState.inputHeightCm.toFloat(),
                isGoalSet = true,
                goalSetDateMillis = System.currentTimeMillis(),
                startingBMI = currentState.bmiValue.toFloat(),
                startingWeight = currentState.inputWeightKg.toFloat()
            )
            bmiGoalPreferences.saveGoal(goalData)
            _goalSaveSuccess.value = true
        }
    }

    fun clearGoal() {
        viewModelScope.launch {
            bmiGoalPreferences.clearGoal()
        }
    }

    fun resetGoalSaveSuccess() {
        _goalSaveSuccess.value = false
    }

    // ─── Smart Input Methods ─────────────────────────────────────────────

    fun setUseSliderInput(useSliders: Boolean) {
        _useSliderInput.value = useSliders
        if (useSliders) {
            syncTextFieldsToSliders()
        } else {
            syncSlidersToTextFields()
        }
    }

    fun updateSliderWeight(weightKg: Float) {
        _sliderWeightKg.value = weightKg
    }

    fun updateSliderHeight(heightCm: Float) {
        _sliderHeightCm.value = heightCm
    }

    private fun syncTextFieldsToSliders() {
        val currentState = _uiState.value
        if (currentState.weightKg > 0) _sliderWeightKg.value = currentState.weightKg.toFloat()
        if (currentState.heightCm > 0) _sliderHeightCm.value = currentState.heightCm.toFloat()
    }

    fun syncSlidersToTextFields() {
        val kg = _sliderWeightKg.value.toDouble()
        val cm = _sliderHeightCm.value.toDouble()
        
        // Use existing methods to properly update text fields and converted states
        updateWeight(if (_uiState.value.weightUnit == WeightUnit.KG) kg.toCleanString() else kgToLbs(kg).toCleanString())
        
        if (_uiState.value.heightUnit == HeightUnit.CM) {
            updateHeightCm(cm.toCleanString())
        } else {
            val (feet, inches) = cmToFeetInches(cm)
            _uiState.update { current ->
                current.copy(
                    heightFeetField = current.heightFeetField.copy(text = if (feet > 0) feet.toString() else ""),
                    heightInchesField = current.heightInchesField.copy(text = if (inches > 0) inches.toCleanString() else ""),
                    heightCm = cm
                )
            }
        }
    }

    fun performQuickCheck() {
        val currentState = _uiState.value
        val hasWeight = currentState.weightKg > 0 && !currentState.weightField.isError
        val hasHeight = currentState.heightCm > 0
        val hasAge = currentState.age in MIN_AGE..MAX_AGE && !currentState.ageField.isError
        val hasGender = currentState.selectedGender != Gender.NOT_SET
        val isEnabled = hasWeight && hasHeight && hasAge && hasGender

        val updatedState = currentState.copy(
            isCalculateEnabled = isEnabled, 
            isReadyToCalculate = isEnabled,
            validationState = currentState.validationState.copy(
                hasAttemptedCalculation = true,
                isCalculating = isEnabled,
                showResults = false
            )
        )
        _uiState.value = updatedState
        if (isEnabled) {
            calculateBmi()
        }
    }

    fun applyLastUsedInput(lastUsed: BMILastUsedInput) {
        _sliderWeightKg.value = lastUsed.weightKg
        _sliderHeightCm.value = lastUsed.heightCm
        
        _uiState.update { current ->
            current.copy(
                weightUnit = if (lastUsed.wasUnitKg) WeightUnit.KG else WeightUnit.LBS,
                heightUnit = if (lastUsed.wasUnitCm) HeightUnit.CM else HeightUnit.FEET_INCHES,
                selectedGender = if (lastUsed.isMale) Gender.MALE else Gender.FEMALE,
                age = lastUsed.age,
                ageField = InputFieldState(text = lastUsed.age.toString())
            )
        }
        
        syncSlidersToTextFields()
    }

    fun saveInputMemory(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean, wasUnitKg: Boolean, wasUnitCm: Boolean) {
        viewModelScope.launch {
            inputMemoryPreferences.saveLastUsedInput(
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                isMale = isMale,
                wasUnitKg = wasUnitKg,
                wasUnitCm = wasUnitCm
            )
        }
    }

    // ─── History / Trend Loading ──────────────────────────────────────────

    private fun loadBmiHistory() {
        viewModelScope.launch {
            historyRepository.getEntriesByType(CalculatorType.BMI).collect { entries ->
                if (entries.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            trendData = BmiTrendData(
                                isLoading = false,
                                hasEnoughData = false
                            )
                        )
                    }
                    return@collect
                }

                // Convert to trend points (sorted newest first from DB)
                val points = entries.map { entry ->
                    val displayEntry = entry.toDisplayEntry()
                    val bmiValue = displayEntry.primaryValue.toDoubleOrNull() ?: 0.0
                    BmiTrendPoint(
                        id = entry.id,
                        bmiValue = bmiValue,
                        category = BmiCategory.fromBmi(bmiValue),
                        timestamp = entry.timestamp,
                        inputSummary = displayEntry.details.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                    )
                }

                // Chronological order for graph (oldest first)
                val chronological = points.sortedBy { it.timestamp }

                // Calculate statistics
                val values = points.map { it.bmiValue }
                val currentBmi = points.first().bmiValue // newest
                val currentCategory = BmiCategory.fromBmi(currentBmi)
                val previousBmi = if (points.size >= 2) points[1].bmiValue else null
                val previousTimestamp = if (points.size >= 2) points[1].timestamp else null
                val changeFromPrevious = if (previousBmi != null) {
                    val change = currentBmi - previousBmi
                    (change * 10).roundToInt() / 10.0
                } else null

                val stats = BmiTrendStats(
                    currentBmi = currentBmi,
                    currentCategory = currentCategory,
                    lowestBmi = (values.min() * 10).roundToInt() / 10.0,
                    highestBmi = (values.max() * 10).roundToInt() / 10.0,
                    averageBmi = (values.average() * 10).roundToInt() / 10.0,
                    totalReadings = points.size,
                    changeFromPrevious = changeFromPrevious,
                    previousBmi = previousBmi,
                    previousTimestamp = previousTimestamp
                )

                _uiState.update {
                    it.copy(
                        trendData = BmiTrendData(
                            points = chronological,
                            stats = stats,
                            isLoading = false,
                            hasEnoughData = points.size >= 2
                        )
                    )
                }
            }
        }
    }

    // ─── Profile Loading ──────────────────────────────────────────────────

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                val profile: ProfileData = profileDataStore.profileFlow.first()
                val hasData = profile.heightCm > 0 || profile.weightKg > 0 ||
                        profile.dateOfBirthMillis != null || profile.gender != Gender.NOT_SET

                if (hasData) {
                    applyProfileData(profile)
                } else {
                    _uiState.update { it.copy(isLoading = false, hasAnyProfileData = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun applyProfileData(profile: ProfileData) {
        _uiState.update { current ->
            var updated = current.copy(
                isLoading = false,
                isProfileLoaded = true,
                hasAnyProfileData = true,
                weightUnit = profile.weightUnit,
                heightUnit = profile.heightUnit,
                showResult = false,
                bmiResult = null
            )

            if (profile.weightKg > 0) {
                val displayValue = when (profile.weightUnit) {
                    WeightUnit.KG -> profile.weightKg
                    WeightUnit.LBS -> kgToLbs(profile.weightKg)
                }
                updated = updated.copy(
                    weightField = InputFieldState(text = displayValue.toCleanString(), isFromProfile = true),
                    weightKg = profile.weightKg
                )
            }

            if (profile.heightCm > 0) {
                val (feet, inches) = cmToFeetInches(profile.heightCm)
                updated = updated.copy(
                    heightCmField = InputFieldState(text = profile.heightCm.toCleanString(), isFromProfile = true),
                    heightFeetField = InputFieldState(text = if (feet > 0) feet.toString() else "", isFromProfile = true),
                    heightInchesField = InputFieldState(text = if (inches > 0) inches.toCleanString() else "", isFromProfile = true),
                    heightCm = profile.heightCm
                )
            }

            val calculatedAge = calculateAge(profile.dateOfBirthMillis)
            if (calculatedAge != null && calculatedAge in MIN_AGE..MAX_AGE) {
                updated = updated.copy(
                    ageField = InputFieldState(text = calculatedAge.toString(), isFromProfile = true),
                    age = calculatedAge
                )
            }

            if (profile.gender != Gender.NOT_SET) {
                updated = updated.copy(
                    selectedGender = profile.gender,
                    genderFromProfile = true
                )
            }

            validateAndUpdateEnabled(updated)
        }
    }

    // ─── BMI Calculation ──────────────────────────────────────────────────

    /**
     * Performs the BMI calculation using WHO standard formula:
     * BMI = weight(kg) / height(m)²
     *
     * Then determines the WHO category and calculates derived values
     * like healthy weight range and weight difference to normal.
     */
    fun calculateBmi() {
        val state = _uiState.value
        if (!state.isCalculateEnabled) return

        _uiState.update { it.copy(isCalculating = true) }

        viewModelScope.launch {
            val weightKg = state.weightKg
            val heightM = state.heightCm / 100.0

            // ── Core BMI: weight(kg) / height(m)² ────────────────────────
            val bmi = weightKg / heightM.pow(2)
            val roundedBmi = (bmi * 10).roundToInt() / 10.0

            // ── WHO Category ──────────────────────────────────────────────
            val category = BmiCategory.fromBmi(roundedBmi)

            // ── Healthy Weight Range (BMI 18.5–24.9) ─────────────────────
            val healthyWeightMin = 18.5 * heightM.pow(2)
            val healthyWeightMax = 24.9 * heightM.pow(2)
            val roundedMin = (healthyWeightMin * 10).roundToInt() / 10.0
            val roundedMax = (healthyWeightMax * 10).roundToInt() / 10.0

            // ── Weight Difference to Normal ───────────────────────────────
            val (weightDiff, targetWeight, advice) = when {
                roundedBmi < 18.5 -> {
                    val target = roundedMin
                    Triple(target - weightKg, target, WeightAdvice.GAIN)
                }
                roundedBmi >= 25.0 -> {
                    val target = roundedMax
                    Triple(weightKg - target, target, WeightAdvice.LOSE)
                }
                else -> Triple(0.0, weightKg, WeightAdvice.MAINTAIN)
            }

            // ── BMI Prime: BMI / 25.0 ────────────────────────────────────
            val bmiPrimeValue = (roundedBmi / 25.0 * 100).roundToInt() / 100.0
            val bmiPrimeStatus = when {
                bmiPrimeValue < 0.99 -> BmiPrimeStatus.UNDER
                bmiPrimeValue <= 1.01 -> BmiPrimeStatus.AT_LIMIT
                else -> BmiPrimeStatus.OVER
            }
            val deviationPercent = ((bmiPrimeValue - 1.0) * 100 * 10).roundToInt() / 10.0

            val bmiPrime = BmiPrimeResult(
                value = bmiPrimeValue,
                status = bmiPrimeStatus,
                deviationPercent = deviationPercent
            )

            // ── Ponderal Index: weight(kg) / height(m)³ ──────────────────
            val pi = weightKg / heightM.pow(3)
            val roundedPi = (pi * 10).roundToInt() / 10.0
            val piStatus = when {
                roundedPi < 11.0 -> PonderalStatus.LOW
                roundedPi <= 15.0 -> PonderalStatus.NORMAL
                else -> PonderalStatus.HIGH
            }

            val ponderalIndex = PonderalIndexResult(
                value = roundedPi,
                status = piStatus
            )

            // ── Asian BMI Category ────────────────────────────────────────
            val asianCategory = AsianBmiCategory.fromBmi(roundedBmi)

            // ── Combine All Additional Metrics ────────────────────────────
            val additionalMetrics = AdditionalBmiMetrics(
                bmiPrime = bmiPrime,
                ponderalIndex = ponderalIndex,
                asianCategory = asianCategory
            )

            // ── Age/Gender Comparison ─────────────────────────────────────
            val comparisonData = calculateBmiComparison(roundedBmi, state.age, state.selectedGender)

            // ── Build Result ──────────────────────────────────────────────
            val result = BmiResult(
                bmiValue = roundedBmi,
                category = category,
                healthyWeightMinKg = roundedMin,
                healthyWeightMaxKg = roundedMax,
                weightDifferenceKg = (weightDiff * 10).roundToInt() / 10.0,
                targetWeightKg = (targetWeight * 10).roundToInt() / 10.0,
                weightAdvice = advice,
                inputWeightKg = weightKg,
                inputHeightCm = state.heightCm,
                inputAge = state.age,
                inputGender = state.selectedGender,
                displayWeightUnit = state.weightUnit,
                additionalMetrics = additionalMetrics,
                comparisonData = comparisonData
            )

            // ── Auto-save to History ──────────────────────────────────────
            val entryId = saveToHistory(result)

            // ── Milestone Evaluation ──────────────────────────────────────
            milestoneEvaluationUseCase.onBmiCalculated(roundedBmi, category.label)

            // ── Generate Share Text ───────────────────────────────────────
            val shareText = generateShareText(result)

            // ── Update Result with Entry ID ───────────────────────────────
            val savedResult = result.copy(historyEntryId = entryId)
            
            // ── Edge case check ───────────────────────────────────────────
            val edgeCaseMessage = BMIEdgeCaseHandler.getEdgeCaseMessage(roundedBmi.toFloat())

            _uiState.update {
                it.copy(
                    isCalculating = false,
                    showResult = true,
                    bmiResult = savedResult,
                    isSavedToHistory = true,
                    showSaveConfirmation = true,
                    shareText = shareText,
                    edgeCaseMessage = edgeCaseMessage,
                    validationState = it.validationState.copy(
                        isCalculating = false,
                        showResults = true
                    )
                )
            }

            saveInputMemory(
                weightKg = weightKg.toFloat(),
                heightCm = state.heightCm.toFloat(),
                age = state.age,
                isMale = state.selectedGender == Gender.MALE,
                wasUnitKg = state.weightUnit == WeightUnit.KG,
                wasUnitCm = state.heightUnit == HeightUnit.CM
            )
        }
    }

    /**
     * Saves the calculation result to the Room database history.
     */
    private suspend fun saveToHistory(result: BmiResult): Long {
        val entry = HistoryEntry(
            calculatorKey = CalculatorType.BMI.key,
            resultValue = result.bmiValue.toString(),
            resultLabel = "kg/m²",
            category = result.category.label,
            detailsJson = """{"weightKg":${result.inputWeightKg},"heightCm":${result.inputHeightCm},"age":${result.inputAge},"gender":"${result.inputGender.name}"}""",
            timestamp = System.currentTimeMillis()
        )

        return historyRepository.addEntry(entry)
    }

    /**
     * Hides the result section and returns to input view.
     */
    fun hideResult() {
        _uiState.update { 
            it.copy(
                showResult = false,
                validationState = it.validationState.copy(showResults = false)
            ) 
        }
    }

    /**
     * Returns to the input form with current values preserved for editing.
     */
    fun recalculate() {
        _uiState.update {
            it.copy(
                showResult = false,
                bmiResult = null,
                isSavedToHistory = false,
                showSaveConfirmation = false,
                shareText = "",
                edgeCaseMessage = null,
                validationState = it.validationState.copy(
                    showResults = false,
                    isCalculating = false
                )
            )
        }
    }

    /**
     * Generates formatted share text for the BMI result.
     */
    private fun generateShareText(result: BmiResult): String {
        val weightDisplay = when (result.displayWeightUnit) {
            WeightUnit.KG -> "${result.inputWeightKg.toCleanString()} kg"
            WeightUnit.LBS -> "${kgToLbs(result.inputWeightKg).toCleanString()} lbs"
        }

        val heightDisplay = "${result.inputHeightCm.toCleanString()} cm"

        val adviceText = when (result.weightAdvice) {
            WeightAdvice.MAINTAIN -> "✅ Healthy BMI range!"
            WeightAdvice.GAIN -> {
                val diff = when (result.displayWeightUnit) {
                    WeightUnit.KG -> "${result.weightDifferenceKg.toCleanString()} kg"
                    WeightUnit.LBS -> "${kgToLbs(result.weightDifferenceKg).toCleanString()} lbs"
                }
                "📊 Need to gain ~$diff to reach healthy range"
            }
            WeightAdvice.LOSE -> {
                val diff = when (result.displayWeightUnit) {
                    WeightUnit.KG -> "${result.weightDifferenceKg.toCleanString()} kg"
                    WeightUnit.LBS -> "${kgToLbs(result.weightDifferenceKg).toCleanString()} lbs"
                }
                "📊 Need to lose ~$diff to reach healthy range"
            }
        }

        val healthRiskSummary = getHealthRiskSummaryForSharing(
            bmi = result.bmiValue.toFloat(),
            age = result.inputAge,
            isMale = result.inputGender == Gender.MALE
        )

        return buildString {
            appendLine("📊 My BMI Result")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("BMI: ${result.bmiValue} kg/m²")
            appendLine("Category: ${result.category.emoji} ${result.category.label}")
            appendLine("Risk Level: ${result.category.riskLevel}")
            appendLine()
            appendLine("📏 Measurements:")
            appendLine("  Weight: $weightDisplay")
            appendLine("  Height: $heightDisplay")
            appendLine("  Age: ${result.inputAge} years")
            appendLine("  Gender: ${result.inputGender.displayName}")
            appendLine()
            appendLine(adviceText)
            appendLine()
            appendLine("BMI Prime: ${String.format("%.2f", result.additionalMetrics.bmiPrime.value)}")
            appendLine("Ponderal Index: ${String.format("%.1f", result.additionalMetrics.ponderalIndex.value)} kg/m³")
            appendLine()
            appendLine(healthRiskSummary)
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("Calculated using Health Calculator app")
            appendLine("WHO Standard Classification")
            appendLine("⚕️ For educational purposes only")
        }
    }
    
    private fun getHealthRiskSummaryForSharing(bmi: Float, age: Int, isMale: Boolean): String {
        val info = BMIHealthRiskProvider.getHealthRiskInfo(bmi, age, isMale)
        val risksText = if (info.healthRisks.isNotEmpty()) {
            "\n\nKey Health Risks:\n" + info.healthRisks.joinToString("\n") { "• ${it.title}" }
        } else ""
    
        val recsText = "\n\nRecommendations:\n" +
                info.recommendations.take(3).joinToString("\n") { "• ${it.title}" }
    
        return """
Health Risk Assessment (${info.category})
Risk Level: ${info.riskLevel.emoji} ${info.riskLevel.label}
$risksText
$recsText
        """.trimIndent()
    }

    /**
     * Dismisses the save confirmation overlay.
     */
    fun dismissSaveConfirmation() {
        _uiState.update { it.copy(showSaveConfirmation = false) }
    }

    /**
     * Returns the share text for the current result.
     */
    fun getShareText(): String {
        return _uiState.value.shareText
    }

    // ─── Weight Input ─────────────────────────────────────────────────────

    fun updateWeight(text: String) {
        if (text.isNotEmpty() && !text.matches(Regex("^\\d*\\.?\\d*$"))) return
        _uiState.update { current ->
            val value = text.toDoubleOrNull()
            val kg = when (current.weightUnit) {
                WeightUnit.KG -> value ?: 0.0
                WeightUnit.LBS -> if (value != null) lbsToKg(value) else 0.0
            }
            val validation = validateWeight(value, current.weightUnit)
            val updated = current.copy(
                weightField = current.weightField.copy(
                    text = text,
                    isError = validation.first && text.isNotEmpty(),
                    errorMessage = validation.second,
                    wasOverridden = current.weightField.isFromProfile
                ),
                weightKg = kg,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    fun toggleWeightUnit() {
        _uiState.update { current ->
            val newUnit = when (current.weightUnit) {
                WeightUnit.KG -> WeightUnit.LBS
                WeightUnit.LBS -> WeightUnit.KG
            }
            val currentValue = current.weightField.text.toDoubleOrNull()
            val convertedText = if (currentValue != null && currentValue > 0) {
                when (newUnit) {
                    WeightUnit.KG -> lbsToKg(currentValue).toCleanString()
                    WeightUnit.LBS -> kgToLbs(currentValue).toCleanString()
                }
            } else current.weightField.text

            val convertedValue = convertedText.toDoubleOrNull()
            val validation = validateWeight(convertedValue, newUnit)

            current.copy(
                weightUnit = newUnit,
                weightField = current.weightField.copy(
                    text = convertedText,
                    isError = validation.first && convertedText.isNotEmpty(),
                    errorMessage = validation.second
                )
            )
        }
    }

    // ─── Height Input (CM) ────────────────────────────────────────────────

    fun updateHeightCm(text: String) {
        if (text.isNotEmpty() && !text.matches(Regex("^\\d*\\.?\\d*$"))) return
        _uiState.update { current ->
            val cm = text.toDoubleOrNull() ?: 0.0
            val validation = validateHeightCm(text.toDoubleOrNull())
            val (feet, inches) = if (cm > 0) cmToFeetInches(cm) else Pair(0, 0.0)
            val updated = current.copy(
                heightCmField = current.heightCmField.copy(
                    text = text,
                    isError = validation.first && text.isNotEmpty(),
                    errorMessage = validation.second,
                    wasOverridden = current.heightCmField.isFromProfile
                ),
                heightFeetField = current.heightFeetField.copy(text = if (feet > 0) feet.toString() else ""),
                heightInchesField = current.heightInchesField.copy(text = if (inches > 0) inches.toCleanString() else ""),
                heightCm = cm,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    fun updateHeightFeet(text: String) {
        if (text.isNotEmpty() && !text.matches(Regex("^\\d*$"))) return
        _uiState.update { current ->
            val feet = text.toIntOrNull() ?: 0
            val inches = current.heightInchesField.text.toDoubleOrNull() ?: 0.0
            val cm = if (feet > 0 || inches > 0) feetInchesToCm(feet, inches) else 0.0
            val validation = validateHeightFeet(text.toIntOrNull(), inches)
            val updated = current.copy(
                heightFeetField = current.heightFeetField.copy(
                    text = text,
                    isError = validation.first && text.isNotEmpty(),
                    errorMessage = validation.second,
                    wasOverridden = current.heightFeetField.isFromProfile
                ),
                heightCmField = current.heightCmField.copy(text = if (cm > 0) cm.toCleanString() else ""),
                heightCm = cm,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    fun updateHeightInches(text: String) {
        if (text.isNotEmpty() && !text.matches(Regex("^\\d*\\.?\\d*$"))) return
        _uiState.update { current ->
            val feet = current.heightFeetField.text.toIntOrNull() ?: 0
            val inches = text.toDoubleOrNull() ?: 0.0
            val cm = if (feet > 0 || inches > 0) feetInchesToCm(feet, inches) else 0.0
            val validation = validateHeightInches(text.toDoubleOrNull())
            val updated = current.copy(
                heightInchesField = current.heightInchesField.copy(
                    text = text,
                    isError = validation.first && text.isNotEmpty(),
                    errorMessage = validation.second,
                    wasOverridden = current.heightInchesField.isFromProfile
                ),
                heightCmField = current.heightCmField.copy(text = if (cm > 0) cm.toCleanString() else ""),
                heightCm = cm,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    fun toggleHeightUnit() {
        _uiState.update { current ->
            val newUnit = when (current.heightUnit) {
                HeightUnit.CM -> HeightUnit.FEET_INCHES
                HeightUnit.FEET_INCHES -> HeightUnit.CM
            }
            current.copy(heightUnit = newUnit)
        }
    }

    // ─── Age Input ────────────────────────────────────────────────────────

    fun updateAge(text: String) {
        if (text.isNotEmpty() && !text.matches(Regex("^\\d*$"))) return
        _uiState.update { current ->
            val ageValue = text.toIntOrNull() ?: 0
            val validation = validateAge(text.toIntOrNull())
            val updated = current.copy(
                ageField = current.ageField.copy(
                    text = text,
                    isError = validation.first && text.isNotEmpty(),
                    errorMessage = validation.second,
                    wasOverridden = current.ageField.isFromProfile
                ),
                age = ageValue,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    // ─── Gender Selection ─────────────────────────────────────────────────

    fun selectGender(gender: Gender) {
        _uiState.update { current ->
            val updated = current.copy(
                selectedGender = gender,
                genderFromProfile = false,
                showResult = false,
                bmiResult = null
            )
            validateAndUpdateEnabled(updated)
        }
    }

    // ─── Clear All ────────────────────────────────────────────────────────

    fun clearAll() {
        _uiState.update {
            BmiInputUiState(
                isLoading = false,
                isProfileLoaded = it.isProfileLoaded,
                hasAnyProfileData = it.hasAnyProfileData,
                weightUnit = it.weightUnit,
                heightUnit = it.heightUnit,
                validationState = BMIValidationState(),
                edgeCaseMessage = null
            )
        }
    }

    fun restoreProfileData() {
        _uiState.update { it.copy(isLoading = true, showResult = false, bmiResult = null) }
        loadProfileData()
    }

    // ─── Validation ───────────────────────────────────────────────────────

    private fun validateWeight(value: Double?, unit: WeightUnit): Pair<Boolean, String> {
        if (value == null) return Pair(false, "")
        val kg = when (unit) {
            WeightUnit.KG -> value
            WeightUnit.LBS -> lbsToKg(value)
        }
        return when {
            value <= 0 -> Pair(true, "Weight must be greater than 0")
            kg < MIN_WEIGHT_KG -> Pair(true, "Weight seems too low")
            kg > MAX_WEIGHT_KG -> Pair(true, "Weight seems too high (max ${MAX_WEIGHT_KG.toInt()} kg)")
            else -> Pair(false, "")
        }
    }

    private fun validateHeightCm(value: Double?): Pair<Boolean, String> {
        if (value == null) return Pair(false, "")
        return when {
            value <= 0 -> Pair(true, "Height must be greater than 0")
            value < MIN_HEIGHT_CM -> Pair(true, "Height seems too low (min ${MIN_HEIGHT_CM.toInt()} cm)")
            value > MAX_HEIGHT_CM -> Pair(true, "Height seems too high (max ${MAX_HEIGHT_CM.toInt()} cm)")
            else -> Pair(false, "")
        }
    }

    private fun validateHeightFeet(feet: Int?, @Suppress("UNUSED_PARAMETER") inches: Double?): Pair<Boolean, String> {
        if (feet == null) return Pair(false, "")
        return when {
            feet < 0 -> Pair(true, "Invalid value")
            feet > MAX_FEET -> Pair(true, "Maximum $MAX_FEET feet")
            else -> Pair(false, "")
        }
    }

    private fun validateHeightInches(inches: Double?): Pair<Boolean, String> {
        if (inches == null) return Pair(false, "")
        return when {
            inches < 0 -> Pair(true, "Invalid value")
            inches > MAX_INCHES -> Pair(true, "Inches must be 0–11.9")
            else -> Pair(false, "")
        }
    }

    private fun validateAge(age: Int?): Pair<Boolean, String> {
        if (age == null) return Pair(false, "")
        return when {
            age < MIN_AGE -> Pair(true, "Age must be at least $MIN_AGE years")
            age > MAX_AGE -> Pair(true, "Age must be under $MAX_AGE years")
            else -> Pair(false, "")
        }
    }

    private fun validateAndUpdateEnabled(state: BmiInputUiState): BmiInputUiState {
        val weightError = validateWeight(state.weightKg, state.weightUnit).second.takeIf { it.isNotEmpty() }
        val heightError = validateHeightCm(state.heightCm).second.takeIf { it.isNotEmpty() }
        val ageError = validateAge(state.age).second.takeIf { it.isNotEmpty() }

        val hasWeight = state.weightKg > 0 && !state.weightField.isError
        val hasHeight = state.heightCm > 0 &&
                !state.heightCmField.isError &&
                !state.heightFeetField.isError &&
                !state.heightInchesField.isError
        val hasAge = state.age in MIN_AGE..MAX_AGE && !state.ageField.isError
        val hasGender = state.selectedGender != Gender.NOT_SET
        val isEnabled = hasWeight && hasHeight && hasAge && hasGender
        
        val newValidationState = state.validationState.copy(
            weightError = weightError,
            heightError = if (state.heightUnit == HeightUnit.CM) heightError else null,
            ageError = ageError,
            hasAttemptedCalculation = if (weightError == null && heightError == null && ageError == null) false else state.validationState.hasAttemptedCalculation
        )

        return state.copy(
            isCalculateEnabled = isEnabled, 
            isReadyToCalculate = isEnabled,
            validationState = newValidationState
        )
    }
}

private fun Double.toCleanString(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}
