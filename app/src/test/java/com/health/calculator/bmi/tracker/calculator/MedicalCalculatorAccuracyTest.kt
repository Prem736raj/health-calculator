package com.health.calculator.bmi.tracker.calculator

import com.health.calculator.bmi.tracker.data.calculator.BMRCalculator
import com.health.calculator.bmi.tracker.data.calculator.BmiCalculator
import com.health.calculator.bmi.tracker.data.calculator.BmiReferenceCategory
import com.health.calculator.bmi.tracker.data.calculator.IdealWeightCalculator
import com.health.calculator.bmi.tracker.data.model.TEFData
import com.health.calculator.bmi.tracker.data.model.BMRFormula
import com.health.calculator.bmi.tracker.data.model.BloodPressureCalculator
import com.health.calculator.bmi.tracker.data.model.BpCategory
import com.health.calculator.bmi.tracker.data.model.WaterActivityLevel
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculator
import com.health.calculator.bmi.tracker.data.model.ClimateType
import com.health.calculator.bmi.tracker.data.model.HealthStatus
import com.health.calculator.bmi.tracker.data.calculator.ReproductiveHealthPolicy
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.WhrCalculator
import com.health.calculator.bmi.tracker.domain.usecase.CalorieCalculatorUseCase
import com.health.calculator.bmi.tracker.domain.usecase.MacroCalculatorUseCase
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel
import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import com.health.calculator.bmi.tracker.util.HeartRateZoneCalculator
import com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot
import com.health.calculator.bmi.tracker.util.HealthScoreCalculator
import com.health.calculator.bmi.tracker.util.VO2MaxCalculator
import com.health.calculator.bmi.tracker.calculator.BSACalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicalCalculatorAccuracyTest {
    @Test
    fun bmiUsesAdultFormulaAndBoundaries() {
        val result = BmiCalculator.calculate(75.0, 180.0)
        assertNotNull(result)
        assertEquals(23.148, result!!.bmi, 0.001)
        assertEquals(BmiReferenceCategory.REFERENCE_RANGE, result.category)
        assertEquals(18.5 * 1.8 * 1.8, result.healthyWeightMinKg, 0.001)
        assertEquals(24.9 * 1.8 * 1.8, result.healthyWeightMaxKg, 0.001)
        assertEquals(BmiReferenceCategory.REFERENCE_RANGE, BmiCalculator.classify(24.9))
        assertEquals(BmiReferenceCategory.OVERWEIGHT, BmiCalculator.classify(25.0))
        assertNull(BmiCalculator.calculate(0.0, 180.0))
        assertNull(BmiCalculator.calculate(Double.NaN, 180.0))
    }

    @Test
    fun bmrRejectsPediatricInputs() {
        assertNull(BMRCalculator.calculate(70f, 175f, 17, true, formula = BMRFormula.MIFFLIN_ST_JEOR))
        assertNotNull(BMRCalculator.calculate(70f, 175f, 18, true, formula = BMRFormula.MIFFLIN_ST_JEOR))
    }

    @Test
    fun idealWeightUses24_9UpperReference() {
        val range = IdealWeightCalculator.calculateHealthyRange(180f)
        assertEquals(18.5f * 1.8f * 1.8f, range.first, 0.001f)
        assertEquals(24.9f * 1.8f * 1.8f, range.second, 0.001f)
        assertTrue(IdealWeightCalculator.validateAge(17) != null)
    }

    @Test
    fun bloodPressureUsesAhaCategoriesAndEitherNumberForSevere() {
        assertEquals(BpCategory.OPTIMAL, BloodPressureCalculator.categorize(119, 79))
        assertEquals(BpCategory.NORMAL, BloodPressureCalculator.categorize(120, 79))
        assertEquals(BpCategory.HIGH_NORMAL, BloodPressureCalculator.categorize(130, 79))
        assertEquals(BpCategory.ISOLATED_SYSTOLIC, BloodPressureCalculator.categorize(140, 89))
        assertEquals(BpCategory.ISOLATED_SYSTOLIC, BloodPressureCalculator.categorize(150, 55))
        assertEquals(BpCategory.GRADE_1_HYPERTENSION, BloodPressureCalculator.categorize(135, 95))
        assertEquals(BpCategory.HYPOTENSION, BloodPressureCalculator.categorize(89, 59))
        assertEquals(BpCategory.GRADE_1_HYPERTENSION, BloodPressureCalculator.categorize(140, 90))
        assertEquals(BpCategory.GRADE_1_HYPERTENSION, BloodPressureCalculator.categorize(159, 99))
        assertEquals(BpCategory.HYPERTENSIVE_CRISIS, BloodPressureCalculator.categorize(181, 79))
        assertTrue(BloodPressureCalculator.isEmergencyReading(179, 120))
        assertEquals("Systolic must be higher than diastolic", BloodPressureCalculator.validateSystolicOverDiastolic("120", "120"))
        assertEquals("The difference between systolic and diastolic seems too small. Please verify.", BloodPressureCalculator.validateSystolicOverDiastolic("120", "115"))
    }

    @Test
    fun waistReferencesUseActionPointsAndSouthAsianCutoff() {
        val result = WhrCalculator.calculate(90f, 100f, Gender.MALE, 30, 180f, Ethnicity.SOUTH_ASIAN)
        assertEquals(0.9f, result.whr, 0.0001f)
        assertEquals(90f, result.waistThresholdIncreased, 0.001f)
        assertEquals(0.5f, result.whtr!!, 0.0001f)
        assertTrue(result.whtrAtRisk == true)
        assertTrue(result.healthRisks.none { it.title == "Hypertension" })
    }

    @Test
    fun waterTargetIsTransparentAdultBeverageStartingPoint() {
        assertEquals(3000, WaterIntakeCalculator.beverageTargetMl("Male", WaterActivityLevel.SEDENTARY, ClimateType.TEMPERATE, HealthStatus.NORMAL))
        assertEquals(2200, WaterIntakeCalculator.beverageTargetMl("Female", WaterActivityLevel.SEDENTARY, ClimateType.TEMPERATE, HealthStatus.NORMAL))
        assertEquals(0, HealthStatus.ILLNESS.additionalMl)
    }

    @Test
    fun reproductiveContextsDoNotCreateFalsePrecisionTargets() {
        assertEquals(0, HealthStatus.PREGNANT.additionalMl)
        assertEquals(0, HealthStatus.BREASTFEEDING.additionalMl)
        assertTrue(ReproductiveHealthPolicy.disclaimerFor(HealthStatus.PREGNANT)!!.contains("does not personalize"))
        assertTrue(ReproductiveHealthPolicy.disclaimerFor(HealthStatus.BREASTFEEDING)!!.contains("care team"))
        assertNull(ReproductiveHealthPolicy.disclaimerFor(HealthStatus.NORMAL))
    }

    @Test
    fun tdeeDoesNotAddTefTwice() {
        val result = CalorieCalculatorUseCase().calculate(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 30,
            gender = "Male",
            bodyFatPercent = null,
            activityMultiplier = 1.2,
            activityLevelName = "Light",
            goalAdjustment = 0,
            goalName = "Maintain",
            weeklyChangeKg = 0.0
        )
        assertEquals(result.usedBmr * 1.2, result.tdee, 0.0001)
        assertEquals(result.tdee - result.usedBmr, result.activityCalories, 0.0001)
        assertTrue(result.tefIncludedInTdee)
    }

    @Test
    fun calorieEstimateUsesAdultAndBodyFatBoundaries() {
        val calculator = CalorieCalculatorUseCase()
        calculator.calculate(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 18,
            gender = "Female",
            bodyFatPercent = 75.0,
            activityMultiplier = 1.2,
            activityLevelName = "Light",
            goalAdjustment = 0,
            goalName = "Maintain",
            weeklyChangeKg = 0.0
        )
        listOf(
            { calculator.calculate(70.0, 175.0, 17, "Female", null, 1.2, "Light", 0, "Maintain", 0.0) },
            { calculator.calculate(70.0, 175.0, 30, "Female", 76.0, 1.2, "Light", 0, "Maintain", 0.0) }
        ).forEach { invalidCalculation ->
            try {
                invalidCalculation()
                throw AssertionError("unsupported calorie inputs should be rejected")
            } catch (_: IllegalArgumentException) {
                // expected
            }
        }
    }

    @Test
    fun wellnessScoreExplainsItsNonClinicalNature() {
        val result = HealthScoreCalculator.calculateHealthScore(HealthMetricsSnapshot(bmi = 23f, restingHR = 70))
        assertFalse(result.isClinicallyValidated)
        assertEquals("Wellness Score", result.scoreName)
        assertTrue(result.methodology.contains("not a diagnosis"))
    }

    @Test
    fun metabolicScreenUsesSouthAsianMaleWaistAtBoundary() {
        val result = MetabolicSyndromeCalculator.evaluate(
            waistCm = 90f,
            isMale = true,
            systolic = 130f,
            diastolic = 85f,
            fastingGlucoseMgDl = 100f,
            triglyceridesMgDl = 150f,
            hdlMgDl = 39f,
            onBpMedication = false,
            onGlucoseMedication = false,
            onTriglyceridesMedication = false,
            onHdlMedication = false,
            ethnicity = Ethnicity.SOUTH_ASIAN
        )
        assertEquals(5, result.criteriaMet)
        assertTrue(result.isSyndromePresent)
        assertFalse(result.isClinicallyValidated)
    }

    @Test
    fun heartRateZonesRequireAdultInputsAndKeepKarvonenMathConsistent() {
        assertEquals(190, HeartRateZoneCalculator.calculateMaxHR(30, HeartRateFormula.STANDARD))
        val result = HeartRateZoneCalculator.calculateZones(
            age = 30,
            formula = HeartRateFormula.KARVONEN,
            restingHR = 60,
            fitnessLevel = FitnessLevel.BEGINNER
        )
        assertEquals(190, result.maxHeartRate)
        assertEquals(130, result.heartRateReserve)
        assertEquals(138, result.zones[1].bpmLow)
        assertEquals(151, result.zones[1].bpmHigh)
        try {
            HeartRateZoneCalculator.calculateZones(17, HeartRateFormula.STANDARD)
            throw AssertionError("pediatric heart-rate zones should be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun vo2EstimateIsExplicitlyNonClinicalAndDoesNotPromiseProjection() {
        val result = VO2MaxCalculator.analyze(maxHR = 190, restingHR = 60, age = 30, gender = "Male")
        assertFalse(result.isClinicalMeasurement)
        assertEquals(result.vo2Max, result.projectedVO2After6Months, 0.0001f)
        assertEquals(0f, result.improvementPotential, 0.0001f)
        assertTrue(result.methodology.contains("not a laboratory"))
    }

    @Test
    fun macroCalculatorKeepsPercentagesBalancedAndCapsSaturatedFatGuide() {
        val result = MacroCalculatorUseCase().calculateFromPercentages(
            totalCalories = 2_000.0,
            carbPercent = 40,
            proteinPercent = 30,
            fatPercent = 30,
            weightKg = 70.0,
            presetName = "Test"
        )
        assertTrue(result.isBalanced)
        assertEquals(2_000.0 * 0.10 / 9.0, result.saturatedFatGrams, 0.0001)
        assertEquals(result.fatGrams - result.saturatedFatGrams, result.unsaturatedFatGrams, 0.0001)
        try {
            MacroCalculatorUseCase().calculateFromPercentages(2_000.0, 40, 40, 30, 70.0, "Invalid")
            throw AssertionError("macro percentages that do not total 100 should be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun bsaMostellerMatchesPublishedEquationAndRejectsInvalidInput() {
        val result = BSACalculator.calculate(70f, 175f, "mosteller")
        assertEquals(kotlin.math.sqrt(70.0 * 175.0 / 3600.0).toFloat(), result.primaryBSA, 0.0001f)
        assertFalse(result.isClinicallyValidated)
        try {
            BSACalculator.calculate(0f, 175f, "mosteller")
            throw AssertionError("zero weight should be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun tefBreakdownDoesNotDoubleCountActivity() {
        val data = TEFData.calculate(
            bmr = 1_600f,
            activityCalories = 600f,
            tdee = 2_200f,
            proteinCalories = 600f,
            carbsCalories = 800f,
            fatCalories = 600f,
            proteinPct = 30f,
            carbsPct = 40f,
            fatPct = 30f
        )
        assertEquals(2_200f, data.adjustedTDEE, 0.0001f)
        assertEquals(340f, data.activityCaloriesForBreakdown, 0.0001f)
        assertTrue(data.bmrPercentOfTotal + data.activityPercentOfTotal + data.tefPercentOfTotal <= 100.01f)
    }
}
