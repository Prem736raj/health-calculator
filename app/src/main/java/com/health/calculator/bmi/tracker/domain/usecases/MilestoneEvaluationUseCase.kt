package com.health.calculator.bmi.tracker.domain.usecases

import com.health.calculator.bmi.tracker.data.models.MilestoneType
import com.health.calculator.bmi.tracker.data.models.PersonalRecordType
import com.health.calculator.bmi.tracker.data.repository.MilestonesRepository

/**
 * Called after each calculation to check for new records and milestones.
 * Integrate into each calculator's save-to-history flow.
 */
class MilestoneEvaluationUseCase(
    private val milestonesRepository: MilestonesRepository
) {

    /**
     * After BMI calculation
     */
    suspend fun onBmiCalculated(bmiValue: Double, category: String): List<Any> {
        val results = mutableListOf<Any>()

        val isRecord = milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.BEST_BMI,
            bmiValue,
            String.format("%.1f", bmiValue)
        )
        if (isRecord) results.add(PersonalRecordType.BEST_BMI)

        if (category == "Normal Weight") {
            val awarded = milestonesRepository.awardMilestone(MilestoneType.BMI_NORMAL)
            if (awarded) results.add(MilestoneType.BMI_NORMAL)
        }

        return results
    }

    /**
     * After BP reading
     */
    suspend fun onBpRecorded(systolic: Double, category: String): List<Any> {
        val results = mutableListOf<Any>()

        val isRecord = milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.BEST_BP_SYSTOLIC,
            systolic,
            "${systolic.toInt()} mmHg"
        )
        if (isRecord) results.add(PersonalRecordType.BEST_BP_SYSTOLIC)

        if (category == "Optimal") {
            val awarded = milestonesRepository.awardMilestone(MilestoneType.BP_OPTIMAL)
            if (awarded) results.add(MilestoneType.BP_OPTIMAL)
        }

        return results
    }

    /**
     * After WHR calculation
     */
    suspend fun onWhrCalculated(whrValue: Double): Boolean {
        return milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.BEST_WHR,
            whrValue,
            String.format("%.2f", whrValue)
        )
    }

    /**
     * After resting HR recorded
     */
    suspend fun onRestingHrRecorded(hr: Double): Boolean {
        return milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.LOWEST_RESTING_HR,
            hr,
            "${hr.toInt()} BPM"
        )
    }

    /**
     * When water streak updates
     */
    suspend fun onWaterStreakUpdated(streak: Int): Boolean {
        return milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.LONGEST_WATER_STREAK,
            streak.toDouble(),
            "$streak days"
        )
    }

    /**
     * When health score updates
     */
    suspend fun onHealthScoreUpdated(score: Int): Boolean {
        return milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.HIGHEST_HEALTH_SCORE,
            score.toDouble(),
            "$score/100"
        )
    }

    /**
     * When tracking streak updates
     */
    suspend fun onTrackingStreakUpdated(streak: Int): Boolean {
        return milestonesRepository.checkAndUpdateRecord(
            PersonalRecordType.LONGEST_TRACKING_STREAK,
            streak.toDouble(),
            "$streak days"
        )
    }

    /**
     * When a result is shared
     */
    suspend fun onResultShared(): Boolean {
        return milestonesRepository.awardMilestone(MilestoneType.SHARED_FIRST_RESULT)
    }

    /**
     * Full evaluation — call periodically or on app open
     */
    suspend fun evaluateAll(
        totalCalculations: Int,
        calculatorsUsed: Set<String>,
        daysSinceFirstUse: Int,
        currentTrackingStreak: Int,
        healthScore: Int,
        waterStreak: Int,
        weightGoalProgress: Float?,
        hasGoalSet: Boolean,
        hasGoalAchieved: Boolean,
        isProfileComplete: Boolean,
        hasBmiNormal: Boolean,
        hasBpOptimal: Boolean,
        hasSharedResult: Boolean
    ): List<MilestoneType> {
        return milestonesRepository.evaluateAllMilestones(
            totalCalculations = totalCalculations,
            calculatorsUsed = calculatorsUsed,
            daysSinceFirstUse = daysSinceFirstUse,
            currentTrackingStreak = currentTrackingStreak,
            healthScore = healthScore,
            waterStreak = waterStreak,
            weightGoalProgress = weightGoalProgress,
            hasGoalSet = hasGoalSet,
            hasGoalAchieved = hasGoalAchieved,
            isProfileComplete = isProfileComplete,
            hasBmiNormal = hasBmiNormal,
            hasBpOptimal = hasBpOptimal,
            hasSharedResult = hasSharedResult
        )
    }
}
