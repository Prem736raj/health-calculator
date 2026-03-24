package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.FamilyProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for family profiles.
 */
@Dao
interface FamilyProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FamilyProfile)

    @Update
    suspend fun updateProfile(profile: FamilyProfile)

    @Delete
    suspend fun deleteProfile(profile: FamilyProfile)

    @Query("SELECT * FROM family_profiles ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllProfiles(): Flow<List<FamilyProfile>>

    @Query("SELECT * FROM family_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<FamilyProfile?>

    @Query("SELECT * FROM family_profiles WHERE profileId = :id")
    suspend fun getProfileById(id: String): FamilyProfile?

    @Query("SELECT COUNT(*) FROM family_profiles")
    fun getProfileCount(): Flow<Int>

    @Query("UPDATE family_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE family_profiles SET isActive = 1 WHERE profileId = :id")
    suspend fun activateProfile(id: String)

    @Query("SELECT profileColor FROM family_profiles")
    suspend fun getUsedColors(): List<Int>

    @Query("DELETE FROM family_profiles WHERE profileId = :id")
    suspend fun deleteById(id: String)
}
