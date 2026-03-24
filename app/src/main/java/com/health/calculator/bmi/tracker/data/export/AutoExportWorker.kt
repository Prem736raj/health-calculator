package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.flow.first

class AutoExportWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        
        // Manual dependency retrieval
        val database = AppDatabase.getInstance(context)
        val historyRepository = HistoryRepository(database.historyDao())
        val exportManager = DataExportManager.getInstance(context)
        val scheduleManager = ExportScheduleManager.getInstance(context)

        return try {
            val formatStr = inputData.getString("format") ?: ExportFormat.PDF.name
            val format = try {
                ExportFormat.valueOf(formatStr)
            } catch (e: Exception) {
                ExportFormat.PDF
            }

            val entries = historyRepository.getAllEntries().first()
            
            if (entries.isNotEmpty()) {
                val config = ExportConfig(
                    format = format,
                    scope = ExportScope.ALL,
                    dateRangeLabel = "Automatic Backup"
                )

                // We don't have easy access to profile data here without more plumbing, 
                // but we can export the history at least.
                exportManager.exportData(
                    entries = entries.map { it.toDisplayEntry() },
                    config = config
                )

                scheduleManager.updateLastExportTime()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
