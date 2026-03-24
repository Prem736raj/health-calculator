package com.health.calculator.bmi.tracker.data.model

data class WhrInput(
    val waistCm: Float = 0f,
    val hipCm: Float = 0f,
    val gender: Gender = Gender.MALE,
    val age: Int = 25,
    val useMetric: Boolean = true
)

data class WhrMeasurementGuide(
    val title: String,
    val description: String,
    val steps: List<String>
)

object WhrGuideData {
    val waistGuide = WhrMeasurementGuide(
        title = "How to Measure Waist",
        description = "Measure at the narrowest point, usually just above the belly button",
        steps = listOf(
            "Stand up straight and breathe normally",
            "Find the narrowest part of your torso (usually above the belly button)",
            "Wrap the tape measure around your waist at this point",
            "Keep the tape snug but not compressing the skin",
            "Make sure the tape is level all around",
            "Read the measurement after a normal exhale",
            "Don't hold your breath or suck in your stomach"
        )
    )

    val hipGuide = WhrMeasurementGuide(
        title = "How to Measure Hips",
        description = "Measure at the widest point of the buttocks",
        steps = listOf(
            "Stand with feet together",
            "Find the widest part of your buttocks/hips",
            "Wrap the tape measure around at this widest point",
            "Keep the tape level and parallel to the floor",
            "Make sure the tape is snug but not tight",
            "Read the measurement while standing naturally",
            "Take 2-3 measurements and use the average"
        )
    )
}
