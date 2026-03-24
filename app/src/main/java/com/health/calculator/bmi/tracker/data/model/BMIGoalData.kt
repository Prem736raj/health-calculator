package com.health.calculator.bmi.tracker.data.model

data class BMIGoalData(
    val targetBMI: Float = 21.7f, // Middle of normal range
    val targetWeight: Float = 0f, // in kg
    val currentBMI: Float = 0f,
    val currentWeight: Float = 0f,
    val heightCm: Float = 0f,
    val isGoalSet: Boolean = false,
    val goalSetDateMillis: Long = System.currentTimeMillis(),
    val startingBMI: Float = 0f,
    val startingWeight: Float = 0f
) {
    val weightChangeNeeded: Float
        get() = targetWeight - currentWeight

    val isWeightLoss: Boolean
        get() = weightChangeNeeded < 0

    val isWeightGain: Boolean
        get() = weightChangeNeeded > 0

    val isGoalReached: Boolean
        get() = if (isWeightLoss) currentWeight <= targetWeight
        else if (isWeightGain) currentWeight >= targetWeight
        else true

    val absoluteWeightChange: Float
        get() = kotlin.math.abs(weightChangeNeeded)

    // Safe rate: loss 0.5-1 kg/week, gain 0.25-0.5 kg/week
    val estimatedWeeksMin: Int
        get() {
            val ratePerWeek = if (isWeightLoss) 1.0f else 0.5f
            return kotlin.math.ceil(absoluteWeightChange / ratePerWeek.toDouble()).toInt()
        }

    val estimatedWeeksMax: Int
        get() {
            val ratePerWeek = if (isWeightLoss) 0.5f else 0.25f
            return kotlin.math.ceil(absoluteWeightChange / ratePerWeek.toDouble()).toInt()
        }

    val estimatedMonthsMin: Float
        get() = estimatedWeeksMin / 4.33f

    val estimatedMonthsMax: Float
        get() = estimatedWeeksMax / 4.33f

    val progressPercentage: Float
        get() {
            if (!isGoalSet || startingWeight == targetWeight) return 0f
            val totalChange = kotlin.math.abs(startingWeight - targetWeight)
            val achieved = kotlin.math.abs(startingWeight - currentWeight)
            // Only count progress in the right direction
            val isCorrectDirection = if (isWeightLoss) currentWeight < startingWeight
            else currentWeight > startingWeight
            if (!isCorrectDirection && achieved > 0.1f) return 0f
            return ((achieved / totalChange) * 100f).coerceIn(0f, 100f)
        }

    val remainingWeight: Float
        get() = absoluteWeightChange

    companion object {
        const val NORMAL_BMI_LOW = 18.5f
        const val NORMAL_BMI_HIGH = 24.9f
        const val NORMAL_BMI_MID = 21.7f

        fun calculateTargetWeight(targetBMI: Float, heightCm: Float): Float {
            val heightM = heightCm / 100f
            return targetBMI * heightM * heightM
        }

        fun calculateBMIFromWeight(weightKg: Float, heightCm: Float): Float {
            val heightM = heightCm / 100f
            if (heightM <= 0) return 0f
            return weightKg / (heightM * heightM)
        }
    }
}
