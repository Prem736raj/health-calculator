package com.health.calculator.bmi.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.local.dao.BloodPressureDao

/**
 * Room database for the Health Calculator app.
 * Currently contains one table for calculation history.
 *
 * Future tables may include:
 * - Blood pressure log entries
 * - Water intake daily records
 * - Achievement/gamification data
 *
 * Uses a singleton pattern to ensure only one database instance exists.
 */
@Database(
    entities = [
        HistoryEntry::class,
        BloodPressureEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {

    /** DAO for calculation history operations */
    abstract fun historyDao(): HistoryDao

    /** DAO for blood pressure logging */
    abstract fun bloodPressureDao(): BloodPressureDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        /**
         * Returns the singleton database instance, creating it if needed.
         * Thread-safe via double-checked locking.
         */
        fun getInstance(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_calculator_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
