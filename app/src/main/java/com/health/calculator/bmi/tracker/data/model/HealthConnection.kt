package com.health.calculator.bmi.tracker.data.model

/**
 * Represents a connection between a calculator and its data sources (profile or other calculators).
 * Used to visualize the health data network and detect when results need updating.
 */
data class HealthConnection(
    val calculatorName: String,
    val calculatorRoute: String,
    val icon: String,
    val inputsFromProfile: List<String>,
    val inputsFromOtherCalculators: List<CalculatorDataLink>,
    val outputsUsedBy: List<String>,
    val lastCalculated: Long? = null,
    val needsRecalculation: Boolean = false
)

/**
 * Defines a link between two calculators where one's output is another's input.
 */
data class CalculatorDataLink(
    val sourceCalculator: String,
    val dataField: String,
    val description: String
)

/**
 * Aggregated view of all health connections in the app.
 */
data class HealthConnectionMap(
    val connections: List<HealthConnection>,
    val profileFieldUsageCount: Int,
    val totalInterconnections: Int,
    val calculatorsNeedingRecalculation: List<String>
)

/**
 * Registry defining how different health tools are interconnected.
 */
object HealthConnectionsRegistry {

    fun buildConnectionMap(
        lastCalculatedTimes: Map<String, Long?>,
        profileLastModified: Long
    ): HealthConnectionMap {
        val connections = listOf(
            HealthConnection(
                calculatorName = "BMI Calculator",
                calculatorRoute = "bmi",
                icon = "⚖️",
                inputsFromProfile = listOf("Weight", "Height", "Age", "Gender"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Health Score", "Metabolic Syndrome"),
                lastCalculated = lastCalculatedTimes["BMI"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["BMI"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "BMR Calculator",
                calculatorRoute = "bmr",
                icon = "🔥",
                inputsFromProfile = listOf("Weight", "Height", "Age", "Gender", "Activity Level"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Daily Calories", "Macro Calculator"),
                lastCalculated = lastCalculatedTimes["BMR"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["BMR"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Blood Pressure",
                calculatorRoute = "blood_pressure",
                icon = "❤️",
                inputsFromProfile = emptyList(),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Metabolic Syndrome", "Health Score", "Heart Rate Zones"),
                lastCalculated = lastCalculatedTimes["BP"],
                needsRecalculation = false // Profile changes don't affect manually entered BP
            ),
            HealthConnection(
                calculatorName = "Waist-to-Hip Ratio",
                calculatorRoute = "whr",
                icon = "📏",
                inputsFromProfile = listOf("Gender", "Height"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Metabolic Syndrome", "Health Score"),
                lastCalculated = lastCalculatedTimes["WHR"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["WHR"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Water Intake",
                calculatorRoute = "water_intake",
                icon = "💧",
                inputsFromProfile = listOf("Weight", "Age", "Gender", "Activity Level"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Health Score"),
                lastCalculated = lastCalculatedTimes["WATER"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["WATER"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Metabolic Syndrome",
                calculatorRoute = "metabolic_syndrome",
                icon = "🩺",
                inputsFromProfile = listOf("Gender"),
                inputsFromOtherCalculators = listOf(
                    CalculatorDataLink("Blood Pressure", "Systolic/Diastolic", "Uses latest BP reading"),
                    CalculatorDataLink("Waist-to-Hip Ratio", "Waist Circumference", "Uses latest waist measurement")
                ),
                outputsUsedBy = listOf("Health Score"),
                lastCalculated = lastCalculatedTimes["METABOLIC_SYNDROME"],
                needsRecalculation = false
            ),
            HealthConnection(
                calculatorName = "Body Surface Area",
                calculatorRoute = "bsa",
                icon = "📐",
                inputsFromProfile = listOf("Weight", "Height"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = emptyList(),
                lastCalculated = lastCalculatedTimes["BSA"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["BSA"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Ideal Body Weight",
                calculatorRoute = "ibw",
                icon = "🎯",
                inputsFromProfile = listOf("Height", "Gender", "Frame Size"),
                inputsFromOtherCalculators = emptyList(),
                outputsUsedBy = listOf("Weight Goal"),
                lastCalculated = lastCalculatedTimes["IBW"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["IBW"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Daily Calories",
                calculatorRoute = "calorie",
                icon = "🍽️",
                inputsFromProfile = listOf("Weight", "Height", "Age", "Gender", "Activity Level"),
                inputsFromOtherCalculators = listOf(
                    CalculatorDataLink("BMR Calculator", "BMR Value", "Uses BMR as base for TDEE")
                ),
                outputsUsedBy = listOf("Health Score"),
                lastCalculated = lastCalculatedTimes["CALORIE"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["CALORIE"], profileLastModified)
            ),
            HealthConnection(
                calculatorName = "Heart Rate Zones",
                calculatorRoute = "heart_rate",
                icon = "💓",
                inputsFromProfile = listOf("Age"),
                inputsFromOtherCalculators = listOf(
                    CalculatorDataLink("Blood Pressure", "Pulse/HR", "Can use resting HR from BP readings")
                ),
                outputsUsedBy = listOf("Health Score"),
                lastCalculated = lastCalculatedTimes["HEART_RATE"],
                needsRecalculation = shouldRecalculate(lastCalculatedTimes["HEART_RATE"], profileLastModified)
            )
        )

        val profileFields = connections.flatMap { it.inputsFromProfile }.distinct()
        val crossLinks = connections.sumOf { it.inputsFromOtherCalculators.size }
        val needsRecalc = connections.filter { it.needsRecalculation }.map { it.calculatorName }

        return HealthConnectionMap(
            connections = connections,
            profileFieldUsageCount = profileFields.size,
            totalInterconnections = crossLinks + connections.sumOf { it.outputsUsedBy.size },
            calculatorsNeedingRecalculation = needsRecalc
        )
    }

    private fun shouldRecalculate(lastCalcTime: Long?, profileModifiedTime: Long): Boolean {
        if (lastCalcTime == null) return false
        return profileModifiedTime > lastCalcTime
    }
}
