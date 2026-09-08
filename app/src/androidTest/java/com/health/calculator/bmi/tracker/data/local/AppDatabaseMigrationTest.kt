package com.health.calculator.bmi.tracker.data.local

import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the latest checked-in migration with a real SQLite implementation.
 * Versions below 13 require release-history fixtures and are intentionally not
 * fabricated here; the release checklist must confirm the oldest distributed
 * database before those fixtures are added.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate15To16PreservesExistingRowsAndAddsStepHistory() {
        helper.createDatabase(TEST_DB, 15).apply {
            execSQL(
                "INSERT INTO history_entries " +
                    "(calculator_key, result_value, result_label, category, timestamp, details_json, note) " +
                    "VALUES ('bmi', '23.4', 'BMI', 'Reference range', 1, '{\"source\":\"test\"}', NULL)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            16,
            true,
            AppDatabase.MIGRATION_15_16
        )
        migrated.query("SELECT COUNT(*) FROM history_entries").use { cursor: Cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'step_history'"
        ).use { cursor: Cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("step_history", cursor.getString(0))
        }
        migrated.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
