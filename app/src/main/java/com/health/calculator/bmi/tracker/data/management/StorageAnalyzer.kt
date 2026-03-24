package com.health.calculator.bmi.tracker.data.management

import android.content.Context
import java.io.File

class StorageAnalyzer private constructor(
    private val context: Context
) {
    companion object {
        @Volatile
        private var instance: StorageAnalyzer? = null

        fun getInstance(context: Context): StorageAnalyzer {
            return instance ?: synchronized(this) {
                instance ?: StorageAnalyzer(context.applicationContext).also { instance = it }
            }
        }
    }

    fun analyzeStorage(): StorageInfo {
        // Use the correct database name from AppDatabase
        val dbFile = context.getDatabasePath("health_calculator_database")
        val historyBytes = if (dbFile?.exists() == true) dbFile.length() else 0L

        val cacheBytes = getDirSize(context.cacheDir)
        val exportsDir = File(context.cacheDir, "exports")
        val exportsBytes = getDirSize(exportsDir)
        
        val backupsDir = File(context.filesDir, "backups")
        val backupsBytes = getDirSize(backupsDir)

        val prefsDir = File(context.filesDir.parent ?: "", "shared_prefs")
        val settingsBytes = getDirSize(prefsDir)

        // Also check datastore files
        val datastoreDir = File(context.filesDir, "datastore")
        val datastoreBytes = getDirSize(datastoreDir)

        val totalSettings = settingsBytes + datastoreBytes

        val totalBytes = historyBytes + cacheBytes + totalSettings + exportsBytes + backupsBytes

        return StorageInfo(
            totalBytes = totalBytes,
            historyBytes = historyBytes,
            cacheBytes = (cacheBytes - exportsBytes).coerceAtLeast(0L), // Cache minus exports subfolder
            settingsBytes = totalSettings,
            exportsBytes = exportsBytes,
            backupsBytes = backupsBytes
        )
    }

    fun clearCache(): Long {
        val before = getDirSize(context.cacheDir)
        clearDir(context.cacheDir)
        val after = getDirSize(context.cacheDir)
        return before - after
    }

    fun clearExports(): Long {
        val dir = File(context.cacheDir, "exports")
        val size = getDirSize(dir)
        clearDir(dir)
        return size
    }

    fun clearBackups(): Long {
        val dir = File(context.filesDir, "backups")
        val size = getDirSize(dir)
        clearDir(dir)
        return size
    }

    private fun getDirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) size += file.length()
        }
        return size
    }

    private fun clearDir(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                clearDir(file)
                file.delete()
            } else {
                file.delete()
            }
        }
    }
}
