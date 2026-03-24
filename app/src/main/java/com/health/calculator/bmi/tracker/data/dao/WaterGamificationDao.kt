package com.health.calculator.bmi.tracker.data.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.EarnedBadge
import com.health.calculator.bmi.tracker.data.model.WaterStreakData
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterGamificationDao {

    // Badges
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: EarnedBadge)

    @Query("SELECT * FROM water_badges ORDER BY earnedTimestamp DESC")
    fun getAllEarnedBadges(): Flow<List<EarnedBadge>>

    @Query("SELECT * FROM water_badges WHERE badgeType = :type LIMIT 1")
    suspend fun getBadge(type: String): EarnedBadge?

    @Query("UPDATE water_badges SET seen = 1 WHERE badgeType = :type")
    suspend fun markBadgeSeen(type: String)

    @Query("SELECT * FROM water_badges WHERE seen = 0")
    suspend fun getUnseenBadges(): List<EarnedBadge>

    // Streak
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStreakData(data: WaterStreakData)

    @Query("SELECT * FROM water_streak_data WHERE id = 1")
    suspend fun getStreakData(): WaterStreakData?

    @Query("SELECT * FROM water_streak_data WHERE id = 1")
    fun observeStreakData(): Flow<WaterStreakData?>
}
