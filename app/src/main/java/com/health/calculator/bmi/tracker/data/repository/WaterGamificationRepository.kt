package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.dao.WaterGamificationDao
import com.health.calculator.bmi.tracker.data.model.EarnedBadge
import com.health.calculator.bmi.tracker.data.model.WaterStreakData
import kotlinx.coroutines.flow.Flow

class WaterGamificationRepository(private val dao: WaterGamificationDao) {

    suspend fun earnBadge(badgeType: String): Boolean {
        val existing = dao.getBadge(badgeType)
        if (existing == null) {
            dao.insertBadge(EarnedBadge(badgeType = badgeType))
            return true
        }
        return false
    }

    fun getAllEarnedBadges(): Flow<List<EarnedBadge>> = dao.getAllEarnedBadges()

    suspend fun isBadgeEarned(type: String): Boolean = dao.getBadge(type) != null

    suspend fun getUnseenBadges(): List<EarnedBadge> = dao.getUnseenBadges()

    suspend fun markBadgeSeen(type: String) = dao.markBadgeSeen(type)

    suspend fun getStreakData(): WaterStreakData =
        dao.getStreakData() ?: WaterStreakData()

    suspend fun updateStreakData(data: WaterStreakData) =
        dao.updateStreakData(data)

    fun observeStreakData(): Flow<WaterStreakData?> = dao.observeStreakData()
}
