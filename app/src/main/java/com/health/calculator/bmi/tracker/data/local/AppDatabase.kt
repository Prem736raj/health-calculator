package com.health.calculator.bmi.tracker.data.local

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.local.dao.BloodPressureDao
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculation
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.dao.WaterIntakeDao
import com.health.calculator.bmi.tracker.data.model.EarnedBadge
import com.health.calculator.bmi.tracker.data.model.WaterStreakData
import com.health.calculator.bmi.tracker.data.dao.WaterGamificationDao
import com.health.calculator.bmi.tracker.data.model.UrineColorEntry
import com.health.calculator.bmi.tracker.data.dao.UrineColorDao


/**
 * Room database for the Health Calculator app.
 */
@Database(
    entities = [
        HistoryEntry::class,
        BloodPressureEntity::class,
        WaterIntakeCalculation::class,
        WaterIntakeLog::class,
        EarnedBadge::class,
        WaterStreakData::class,
        UrineColorEntry::class,
        com.health.calculator.bmi.tracker.data.local.entity.FavoriteQuoteEntity::class,
        com.health.calculator.bmi.tracker.data.model.WeightEntry::class,
        com.health.calculator.bmi.tracker.data.model.FamilyProfile::class,
        com.health.calculator.bmi.tracker.data.models.PersonalRecord::class,
        com.health.calculator.bmi.tracker.data.models.HealthMilestone::class,
        com.health.calculator.bmi.tracker.data.models.Reminder::class,
        com.health.calculator.bmi.tracker.data.models.WeeklyReport::class,
        com.health.calculator.bmi.tracker.data.local.entity.ChatMessageEntity::class
    ],
    version = 15,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for calculation history operations */
    abstract fun historyDao(): HistoryDao

    /** DAO for blood pressure logging */
    abstract fun bloodPressureDao(): BloodPressureDao

    /** DAO for tracking water intake */
    abstract fun waterIntakeDao(): WaterIntakeDao

    /** DAO for water gamification */
    abstract fun waterGamificationDao(): WaterGamificationDao

    /** DAO for urine color tracking */
    abstract fun urineColorDao(): UrineColorDao

    /** DAO for favoriting motivational quotes */
    abstract fun favoriteQuoteDao(): com.health.calculator.bmi.tracker.data.local.dao.FavoriteQuoteDao

    /** DAO for weight tracking */
    abstract fun weightDao(): com.health.calculator.bmi.tracker.data.local.dao.WeightDao

    /** DAO for family profiles */
    abstract fun familyProfileDao(): com.health.calculator.bmi.tracker.data.local.dao.FamilyProfileDao

    /** DAO for milestones and records */
    abstract fun milestonesDao(): com.health.calculator.bmi.tracker.data.local.dao.MilestonesDao

    /** DAO for reminders */
    abstract fun reminderDao(): com.health.calculator.bmi.tracker.data.local.dao.ReminderDao

    /** DAO for weekly reports */
    abstract fun weeklyReportDao(): com.health.calculator.bmi.tracker.data.local.dao.WeeklyReportDao

    /** DAO for AI chat history */
    abstract fun chatDao(): com.health.calculator.bmi.tracker.data.local.dao.ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty migration. Schema remains exactly the same as version 13.
                // This bumps the version safely while allowing removal of destructive migrations.
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `isUser` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        /**
         * Returns the singleton database instance, creating it if needed.
         */
        fun getDatabase(@ApplicationContext context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_calculator_database"
                )
                    .addMigrations(MIGRATION_13_14, MIGRATION_14_15)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Alias for getDatabase to maintain compatibility with code using HealthDatabase.
         */
        fun getInstance(@ApplicationContext context: Context): AppDatabase = getDatabase(context)
    }
}
