package com.health.calculator.bmi.tracker.data.backup

import android.content.Context
import android.net.Uri
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import com.health.calculator.bmi.tracker.data.model.ParsedHistoryEntry
import com.health.calculator.bmi.tracker.data.model.toParsedEntry
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class LocalBackupManager(
    private val context: Context
) {
    private val backupDir: File
        get() = File(context.filesDir, "backups").apply { mkdirs() }

    companion object {
        @Volatile
        private var INSTANCE: LocalBackupManager? = null

        fun getInstance(context: Context): LocalBackupManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalBackupManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun createLocalBackup(
        historyEntries: List<HistoryDisplayEntry>,
        profileData: Map<String, String>,
        settingsData: Map<String, String>,
        achievementsData: Map<String, Any>,
        onProgress: (Float) -> Unit
    ): File {
        onProgress(0.05f)

        val backupJson = JSONObject().apply {
            put("backup_version", BackupMetadata.BACKUP_VERSION)
            put("app_package", "com.health.calculator.bmi.tracker")
            put("created_at", System.currentTimeMillis())
            put("device_model", android.os.Build.MODEL)
            put("android_version", android.os.Build.VERSION.SDK_INT)

            // Profile
            onProgress(0.1f)
            val profileObj = JSONObject()
            profileData.forEach { (k, v) -> profileObj.put(k, v) }
            put("profile", profileObj)

            // Settings
            onProgress(0.2f)
            val settingsObj = JSONObject()
            settingsData.forEach { (k, v) -> settingsObj.put(k, v) }
            put("settings", settingsObj)

            // Achievements
            onProgress(0.3f)
            val achievementsObj = JSONObject()
            achievementsData.forEach { (k, v) ->
                when (v) {
                    is String -> achievementsObj.put(k, v)
                    is Int -> achievementsObj.put(k, v)
                    is Long -> achievementsObj.put(k, v)
                    is Boolean -> achievementsObj.put(k, v)
                    else -> achievementsObj.put(k, v.toString())
                }
            }
            put("achievements", achievementsObj)

            // History entries
            onProgress(0.4f)
            val entriesArray = JSONArray()
            historyEntries.forEachIndexed { index, entry ->
                val entryObj = JSONObject().apply {
                    put("id", entry.id)
                    put("calculator_type", entry.calculatorType.key)
                    put("primary_value", entry.primaryValue)
                    put("primary_label", entry.primaryLabel)
                    put("category", entry.category ?: JSONObject.NULL)
                    put("category_color", entry.categoryColor.name)
                    put("timestamp", entry.timestamp)
                    put("note", entry.note ?: JSONObject.NULL)

                    val detailsObj = JSONObject()
                    entry.details.forEach { (dk, dv) -> detailsObj.put(dk, dv) }
                    put("details", detailsObj)
                }
                entriesArray.put(entryObj)
                onProgress(0.4f + 0.4f * (index + 1) / historyEntries.size)
            }
            put("history_entries", entriesArray)
            put("entry_count", historyEntries.size)
        }

        onProgress(0.85f)

        // Encrypt
        val jsonString = backupJson.toString()
        val encryptedData = BackupEncryption.encrypt(jsonString.toByteArray(Charsets.UTF_8))

        // Save to file
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HealthCalc_Backup_$timestamp.hcb"
        val file = File(backupDir, fileName)

        FileOutputStream(file).use { it.write(encryptedData) }

        onProgress(1f)
        return file
    }

    fun restoreFromFile(
        uri: Uri,
        mode: RestoreMode,
        onProgress: (Float) -> Unit
    ): RestoreResult {
        onProgress(0.1f)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return RestoreResult(success = false, error = "Cannot open file")

        val encryptedData = inputStream.use { it.readBytes() }
        onProgress(0.2f)

        // Decrypt
        val decryptedBytes = try {
            BackupEncryption.decrypt(encryptedData)
        } catch (e: Exception) {
            return RestoreResult(success = false, error = "Invalid or corrupted backup file")
        }

        onProgress(0.3f)

        val jsonString = String(decryptedBytes, Charsets.UTF_8)
        val backupJson = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            return RestoreResult(success = false, error = "Invalid backup format")
        }

        // Validate version
        val version = backupJson.optInt("backup_version", 0)
        if (version > BackupMetadata.BACKUP_VERSION) {
            return RestoreResult(
                success = false,
                error = "Backup is from a newer version. Please update the app."
            )
        }

        onProgress(0.4f)

        // Parse data
        val profileData = mutableMapOf<String, String>()
        backupJson.optJSONObject("profile")?.let { profileObj ->
            profileObj.keys().forEach { key ->
                profileData[key] = profileObj.getString(key)
            }
        }

        onProgress(0.5f)

        val settingsData = mutableMapOf<String, String>()
        backupJson.optJSONObject("settings")?.let { settingsObj ->
            settingsObj.keys().forEach { key ->
                settingsData[key] = settingsObj.getString(key)
            }
        }

        onProgress(0.6f)

        val achievementsData = mutableMapOf<String, Any>()
        backupJson.optJSONObject("achievements")?.let { achObj ->
            achObj.keys().forEach { key ->
                achievementsData[key] = achObj.get(key)
            }
        }

        onProgress(0.7f)

        val entries = mutableListOf<ParsedHistoryEntry>()
        backupJson.optJSONArray("history_entries")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val details = buildMap {
                    obj.optJSONObject("details")?.let { d ->
                        d.keys().forEach { k -> put(k, d.getString(k)) }
                    }
                }
                entries.add(
                    ParsedHistoryEntry(
                        id = if (obj.has("id")) obj.getLong("id") else 0L,
                        calculatorKey = obj.getString("calculator_type"),
                        primaryValue = obj.getString("primary_value").toDoubleOrNull() ?: 0.0,
                        primaryLabel = obj.getString("primary_label"),
                        secondaryValue = details["secondary_value"]?.toDoubleOrNull(),
                        secondaryLabel = details["secondary_label"],
                        category = if (obj.isNull("category")) null else obj.getString("category"),
                        timestamp = obj.getLong("timestamp"),
                        note = if (obj.isNull("note")) null else obj.getString("note"),
                        details = details
                    )
                )
            }
        }

        onProgress(0.9f)

        return RestoreResult(
            success = true,
            profileData = profileData,
            settingsData = settingsData,
            achievementsData = achievementsData,
            historyEntries = entries,
            entryCount = entries.size,
            mode = mode
        )
    }

    fun getLocalBackups(): List<BackupMetadata> {
        return backupDir.listFiles()
            ?.filter { it.extension == "hcb" }
            ?.map { file ->
                BackupMetadata(
                    id = file.name,
                    fileName = file.name,
                    timestamp = file.lastModified(),
                    sizeBytes = file.length(),
                    source = BackupSource.LOCAL
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    fun deleteBackup(fileName: String) {
        File(backupDir, fileName).delete()
    }

    fun getBackupFileUri(fileName: String): Uri? {
        val file = File(backupDir, fileName)
        if (!file.exists()) return null
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}


data class RestoreResult(
    val success: Boolean,
    val error: String? = null,
    val profileData: Map<String, String> = emptyMap(),
    val settingsData: Map<String, String> = emptyMap(),
    val achievementsData: Map<String, Any> = emptyMap(),
    val historyEntries: List<ParsedHistoryEntry> = emptyList(),
    val entryCount: Int = 0,
    val mode: RestoreMode = RestoreMode.REPLACE
)
