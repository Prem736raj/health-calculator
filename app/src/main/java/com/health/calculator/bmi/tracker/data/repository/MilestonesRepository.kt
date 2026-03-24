package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.dao.MilestonesDao
import com.health.calculator.bmi.tracker.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MilestonesRepository(
    private val milestonesDao: MilestonesDao,
    private val historyRepository: HistoryRepository
) {

    fun getAllRecords(): Flow<List<PersonalRecord>> = milestonesDao.getAllRecords()

    fun getAllMilestones(): Flow<List<HealthMilestone>> = milestonesDao.getAllMilestones()

    fun getUncelebratedMilestones(): Flow<List<HealthMilestone>> =
        milestonesDao.getUncelebratedMilestones()

    fun getRecordCount(): Flow<Int> = milestonesDao.getRecordCount()

    fun getMilestoneCount(): Flow<Int> = milestonesDao.getMilestoneCount()

    /**
     * Checks and updates a personal record. Returns true if new record set.
     */
    suspend fun checkAndUpdateRecord(
        type: PersonalRecordType,
        currentValue: Double,
        displayValue: String
    ): Boolean {
        val existing = milestonesDao.getRecord(type.name)

        if (type.isNewRecord(currentValue, existing?.value)) {
            val record = PersonalRecord(
                recordType = type.name,
                value = currentValue,
                displayValue = displayValue,
                achievedAt = System.currentTimeMillis(),
                previousValue = existing?.value,
                previousDisplayValue = existing?.displayValue
            )
            milestonesDao.insertRecord(record)
            return true
        }
        return false
    }

    /**
     * Awards a milestone if not already achieved.
     * Returns true if newly awarded.
     */
    suspend fun awardMilestone(
        type: MilestoneType,
        details: String? = null
    ): Boolean {
        val existing = milestonesDao.getMilestone(type.name)
        if (existing != null) return false

        milestonesDao.insertMilestone(
            HealthMilestone(
                milestoneType = type.name,
                achievedAt = System.currentTimeMillis(),
                details = details,
                isCelebrated = false
            )
        )
        return true
    }

    suspend fun markMilestoneCelebrated(type: String) {
        milestonesDao.markCelebrated(type)
    }

    /**
     * Checks all milestone conditions and awards any that are newly met.
     */
    suspend fun evaluateAllMilestones(
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
        val newlyAwarded = mutableListOf<MilestoneType>()

        // Getting Started
        if (totalCalculations >= 1)
            if (awardMilestone(MilestoneType.FIRST_CALCULATION)) newlyAwarded.add(MilestoneType.FIRST_CALCULATION)

        if (isProfileComplete)
            if (awardMilestone(MilestoneType.PROFILE_COMPLETE)) newlyAwarded.add(MilestoneType.PROFILE_COMPLETE)

        if (hasGoalSet)
            if (awardMilestone(MilestoneType.FIRST_GOAL_SET)) newlyAwarded.add(MilestoneType.FIRST_GOAL_SET)

        // Consistency
        if (currentTrackingStreak >= 7)
            if (awardMilestone(MilestoneType.ONE_WEEK_TRACKING)) newlyAwarded.add(MilestoneType.ONE_WEEK_TRACKING)

        if (currentTrackingStreak >= 14)
            if (awardMilestone(MilestoneType.TWO_WEEKS_TRACKING)) newlyAwarded.add(MilestoneType.TWO_WEEKS_TRACKING)

        if (daysSinceFirstUse >= 30)
            if (awardMilestone(MilestoneType.ONE_MONTH_ACTIVE)) newlyAwarded.add(MilestoneType.ONE_MONTH_ACTIVE)

        if (daysSinceFirstUse >= 90)
            if (awardMilestone(MilestoneType.THREE_MONTHS_ACTIVE)) newlyAwarded.add(MilestoneType.THREE_MONTHS_ACTIVE)

        if (daysSinceFirstUse >= 180)
            if (awardMilestone(MilestoneType.SIX_MONTHS_ACTIVE)) newlyAwarded.add(MilestoneType.SIX_MONTHS_ACTIVE)

        if (daysSinceFirstUse >= 365)
            if (awardMilestone(MilestoneType.ONE_YEAR_ACTIVE)) newlyAwarded.add(MilestoneType.ONE_YEAR_ACTIVE)

        // Exploration
        if (calculatorsUsed.size >= 10)
            if (awardMilestone(MilestoneType.ALL_CALCULATORS_USED)) newlyAwarded.add(MilestoneType.ALL_CALCULATORS_USED)

        if (totalCalculations >= 50)
            if (awardMilestone(MilestoneType.FIFTY_CALCULATIONS)) newlyAwarded.add(MilestoneType.FIFTY_CALCULATIONS)

        if (totalCalculations >= 100)
            if (awardMilestone(MilestoneType.HUNDRED_CALCULATIONS)) newlyAwarded.add(MilestoneType.HUNDRED_CALCULATIONS)

        if (totalCalculations >= 500)
            if (awardMilestone(MilestoneType.FIVE_HUNDRED_CALCULATIONS)) newlyAwarded.add(MilestoneType.FIVE_HUNDRED_CALCULATIONS)

        // Achievements
        if (hasGoalAchieved)
            if (awardMilestone(MilestoneType.FIRST_GOAL_ACHIEVED)) newlyAwarded.add(MilestoneType.FIRST_GOAL_ACHIEVED)

        if (healthScore >= 60)
            if (awardMilestone(MilestoneType.HEALTH_SCORE_60)) newlyAwarded.add(MilestoneType.HEALTH_SCORE_60)

        if (healthScore >= 80)
            if (awardMilestone(MilestoneType.HEALTH_SCORE_80)) newlyAwarded.add(MilestoneType.HEALTH_SCORE_80)

        if (healthScore >= 95)
            if (awardMilestone(MilestoneType.HEALTH_SCORE_95)) newlyAwarded.add(MilestoneType.HEALTH_SCORE_95)

        // Health Wins
        if (hasBmiNormal)
            if (awardMilestone(MilestoneType.BMI_NORMAL)) newlyAwarded.add(MilestoneType.BMI_NORMAL)

        if (hasBpOptimal)
            if (awardMilestone(MilestoneType.BP_OPTIMAL)) newlyAwarded.add(MilestoneType.BP_OPTIMAL)

        if (waterStreak >= 7)
            if (awardMilestone(MilestoneType.WATER_7_DAY_STREAK)) newlyAwarded.add(MilestoneType.WATER_7_DAY_STREAK)

        if (waterStreak >= 30)
            if (awardMilestone(MilestoneType.WATER_30_DAY_STREAK)) newlyAwarded.add(MilestoneType.WATER_30_DAY_STREAK)

        weightGoalProgress?.let { progress ->
            if (progress >= 0.25f)
                if (awardMilestone(MilestoneType.WEIGHT_GOAL_25_PERCENT)) newlyAwarded.add(MilestoneType.WEIGHT_GOAL_25_PERCENT)
            if (progress >= 0.50f)
                if (awardMilestone(MilestoneType.WEIGHT_GOAL_50_PERCENT)) newlyAwarded.add(MilestoneType.WEIGHT_GOAL_50_PERCENT)
            if (progress >= 0.75f)
                if (awardMilestone(MilestoneType.WEIGHT_GOAL_75_PERCENT)) newlyAwarded.add(MilestoneType.WEIGHT_GOAL_75_PERCENT)
            if (progress >= 1.0f)
                if (awardMilestone(MilestoneType.WEIGHT_GOAL_REACHED)) newlyAwarded.add(MilestoneType.WEIGHT_GOAL_REACHED)
        }

        // Social
        if (hasSharedResult)
            if (awardMilestone(MilestoneType.SHARED_FIRST_RESULT)) newlyAwarded.add(MilestoneType.SHARED_FIRST_RESULT)

        return newlyAwarded
    }

    suspend fun getJourneySummary(
        healthScore: Int,
        firstHealthScore: Int?,
        trackingStreak: Int,
        longestStreak: Int,
        goalsSet: Int,
        goalsAchieved: Int,
        mostUsedCalculator: String?
    ): HealthJourneySummary {
        val milestoneCount = milestonesDao.getMilestoneCount().first()
        val recordCount = milestonesDao.getRecordCount().first()

        val totalCalcs = historyRepository.getTotalCalculationCount()
        val calcsUsed = historyRepository.getDistinctCalculatorTypes()
        val firstUse = historyRepository.getFirstEntryDate()

        val daysSinceFirst = firstUse?.let {
            ((System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)).toInt()
        } ?: 0

        val scoreChange = firstHealthScore?.let { healthScore - it } ?: 0

        return HealthJourneySummary(
            daysSinceFirstUse = daysSinceFirst,
            totalCalculations = totalCalcs,
            calculatorsUsed = calcsUsed.size,
            totalCalculatorsAvailable = 10,
            currentHealthScore = healthScore,
            healthScoreChange = scoreChange,
            currentTrackingStreak = trackingStreak,
            longestTrackingStreak = longestStreak,
            goalsSet = goalsSet,
            goalsAchieved = goalsAchieved,
            milestonesEarned = milestoneCount,
            totalMilestonesAvailable = MilestoneType.values().size,
            personalRecordsSet = recordCount,
            firstUseDate = firstUse,
            mostUsedCalculator = mostUsedCalculator
        )
    }
}
