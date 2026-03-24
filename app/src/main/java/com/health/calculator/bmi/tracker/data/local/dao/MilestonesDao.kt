package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.models.HealthMilestone
import com.health.calculator.bmi.tracker.data.models.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestonesDao {

    // Personal Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecord)

    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC")
    fun getAllRecords(): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE recordType = :type")
    suspend fun getRecord(type: String): PersonalRecord?

    @Query("SELECT COUNT(*) FROM personal_records")
    fun getRecordCount(): Flow<Int>

    @Query("DELETE FROM personal_records")
    suspend fun deleteAllRecords()

    // Health Milestones
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMilestone(milestone: HealthMilestone)

    @Update
    suspend fun updateMilestone(milestone: HealthMilestone)

    @Query("SELECT * FROM health_milestones ORDER BY achievedAt DESC")
    fun getAllMilestones(): Flow<List<HealthMilestone>>

    @Query("SELECT * FROM health_milestones WHERE milestoneType = :type LIMIT 1")
    suspend fun getMilestone(type: String): HealthMilestone?

    @Query("SELECT COUNT(*) FROM health_milestones")
    fun getMilestoneCount(): Flow<Int>

    @Query("SELECT * FROM health_milestones WHERE isCelebrated = 0")
    fun getUncelebratedMilestones(): Flow<List<HealthMilestone>>

    @Query("UPDATE health_milestones SET isCelebrated = 1 WHERE milestoneType = :type")
    suspend fun markCelebrated(type: String)

    @Query("DELETE FROM health_milestones")
    suspend fun deleteAllMilestones()
}
