package com.health.calculator.bmi.tracker.presentation.navigation

/**
 * User-facing quality contract for every calculator. Keeping this metadata in
 * one place prevents the hub and result screens from inventing different
 * claims about the same estimate.
 */
data class CalculatorQualityInfo(
    val id: CalculatorDestination,
    val title: String,
    val description: String,
    val inputs: String,
    val method: String,
    val interpretation: String,
    val limitations: String,
    val sources: List<String>,
    val related: List<CalculatorDestination>
)

object CalculatorQualityCatalog {
    val all: List<CalculatorQualityInfo> = listOf(
        CalculatorQualityInfo(
            id = CalculatorDestination.BMI,
            title = "BMI",
            description = "A height-and-weight reference calculation for adults.",
            inputs = "Weight and height; metric or imperial units.",
            method = "BMI = weight in kilograms ÷ height in metres². Adult categories follow the WHO reference ranges shown in the result.",
            interpretation = "Use the value as a population-level screening reference, alongside other information.",
            limitations = "BMI does not distinguish muscle from fat and is not intended for pregnancy, children, or a diagnosis.",
            sources = listOf("WHO: Obesity and overweight (who.int)", "CDC: About adult BMI (cdc.gov)"),
            related = listOf(CalculatorDestination.IDEAL_WEIGHT, CalculatorDestination.WAIST_HIP)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.BMR,
            title = "Resting energy (BMR)",
            description = "An estimate of energy used at rest in a typical day.",
            inputs = "Weight, height, age, sex used by the selected equation, and optional body fat.",
            method = "Mifflin–St Jeor is the default comparison equation; additional validated adult equations are shown when available.",
            interpretation = "This is a starting estimate, not a prescription for eating or weight change.",
            limitations = "Real needs vary with health, movement, body composition, environment and measurement error.",
            sources = listOf("Mifflin et al., 1990 (PubMed)", "National Academies dietary reference resources"),
            related = listOf(CalculatorDestination.CALORIES, CalculatorDestination.BMI)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.BLOOD_PRESSURE,
            title = "Blood pressure",
            description = "Record a single home reading and view its reference category.",
            inputs = "Systolic and diastolic pressure in mmHg; an optional pulse.",
            method = "Categories use the AHA adult blood-pressure reference table; one reading is not a diagnosis.",
            interpretation = "Sit quietly, repeat unusual readings, and discuss a persistent pattern with a qualified professional.",
            limitations = "Cuff fit, posture, timing, stress and medicines can change a reading. Urgent symptoms need local medical help.",
            sources = listOf("American Heart Association: Understanding blood pressure readings (heart.org)"),
            related = listOf(CalculatorDestination.HEART_RATE, CalculatorDestination.METABOLIC)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.WATER,
            title = "Water starting point",
            description = "A transparent adult beverage-intake estimate for planning.",
            inputs = "Weight, activity, climate and unit preferences where available.",
            method = "The app combines an adult starting estimate with the goal you choose; it is not a fixed medical requirement.",
            interpretation = "Use thirst, meals, activity and clinician guidance to adjust intake; water from food also contributes.",
            limitations = "Needs differ with illness, pregnancy, kidney/heart conditions, heat and exercise. Avoid forcing excess fluids.",
            sources = listOf("National Academies: Dietary Reference Intakes for Water"),
            related = listOf(CalculatorDestination.CALORIES, CalculatorDestination.BMI)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.CALORIES,
            title = "Daily energy",
            description = "Estimate daily energy needs for general planning and optional logging.",
            inputs = "Weight, height, age, sex used by the equation, activity and goal preference.",
            method = "Resting energy is estimated first, then an activity factor is applied; the result is rounded for readability.",
            interpretation = "Treat the number as a range or starting point and review how you feel and perform over time.",
            limitations = "It cannot account for every medical, metabolic or lifestyle factor and is not a treatment plan.",
            sources = listOf("Mifflin et al., 1990 (PubMed)", "Dietary Guidelines for Americans"),
            related = listOf(CalculatorDestination.BMR, CalculatorDestination.BMI)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.WAIST_HIP,
            title = "Waist and hip",
            description = "Body-proportion reference measures from waist and hip circumference.",
            inputs = "Waist and hip circumference; optional height and sex for additional context.",
            method = "WHR = waist circumference ÷ hip circumference. Waist-to-height context is shown separately when height is supplied.",
            interpretation = "Thresholds vary by sex, population and guideline; measurement technique matters.",
            limitations = "These measures do not diagnose body-fat percentage or disease and are not designed for pregnancy interpretation.",
            sources = listOf("WHO: Waist circumference and waist–hip ratio (who.int)", "NICE: Waist-to-height ratio guidance (nice.org.uk)"),
            related = listOf(CalculatorDestination.BMI, CalculatorDestination.METABOLIC)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.HEART_RATE,
            title = "Heart-rate zones",
            description = "Training-intensity ranges with a talk-test reminder.",
            inputs = "Age, selected formula, optional resting heart rate and fitness context.",
            method = "Age-based maximum-heart-rate equations and the Karvonen reserve method are estimates, not measured limits.",
            interpretation = "Use perceived effort and the talk test; stop if exercise feels unsafe and seek appropriate advice.",
            limitations = "Medications, conditions, heat and fitness change heart-rate response. Zone estimates are not clearance to exercise.",
            sources = listOf("American Heart Association: Target heart rates (heart.org)"),
            related = listOf(CalculatorDestination.BLOOD_PRESSURE, CalculatorDestination.CALORIES)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.IDEAL_WEIGHT,
            title = "Height-based weight range",
            description = "An adult BMI-reference range, not an ideal or required target.",
            inputs = "Height and optional sex, frame and current weight for context.",
            method = "The reference range applies adult BMI 18.5–24.9 to the supplied height; optional formulas are labelled separately.",
            interpretation = "A healthy target is personal. Consider strength, function, history and professional guidance—not a single number.",
            limitations = "BMI-based ranges are broad population references and do not account for muscle, pregnancy, age or medical needs.",
            sources = listOf("WHO: BMI and adult weight classification (who.int)", "CDC: Adult BMI (cdc.gov)"),
            related = listOf(CalculatorDestination.BMI, CalculatorDestination.WAIST_HIP)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.BSA,
            title = "Body surface area",
            description = "Compare common body-surface-area equations for informational use.",
            inputs = "Weight and height; metric or imperial units.",
            method = "Mosteller is the primary display equation; comparison equations are labelled with their names.",
            interpretation = "The result is a body-size estimate and should not be used to dose medicine independently.",
            limitations = "BSA varies with formula and measurement error; clinical dosing decisions require a qualified professional.",
            sources = listOf("Mosteller, 1987 (PubMed)", "Clinical pharmacology references for BSA limitations"),
            related = listOf(CalculatorDestination.BMI, CalculatorDestination.BMR)
        ),
        CalculatorQualityInfo(
            id = CalculatorDestination.METABOLIC,
            title = "Metabolic markers",
            description = "Count selected screening markers using a labelled guideline set.",
            inputs = "Waist, blood pressure, glucose, triglycerides, HDL and treatment status where available.",
            method = "The selected IDF/ATP-style criteria are shown individually; the app reports a screening count, not a diagnosis.",
            interpretation = "Missing laboratory data should remain unknown. Discuss a concerning pattern with a qualified professional.",
            limitations = "Cut-offs vary by guideline and population, and this tool cannot assess the full clinical picture.",
            sources = listOf("International Diabetes Federation consensus definition", "American Heart Association metabolic syndrome overview"),
            related = listOf(CalculatorDestination.BLOOD_PRESSURE, CalculatorDestination.WAIST_HIP, CalculatorDestination.BMI)
        )
    )

    private val byId = all.associateBy { it.id }

    fun get(id: CalculatorDestination): CalculatorQualityInfo = requireNotNull(byId[id])
}
