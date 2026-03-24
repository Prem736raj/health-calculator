package com.health.calculator.bmi.tracker.data.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GoogleDriveBackupManager(
    private val context: Context
) {
    companion object {
        private const val APP_FOLDER = "HealthCalculatorBackups"
        private const val MIME_TYPE = "application/octet-stream"

        @Volatile
        private var INSTANCE: GoogleDriveBackupManager? = null

        fun getInstance(context: Context): GoogleDriveBackupManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleDriveBackupManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var driveService: Drive? = null

    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun initDriveService(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Health Calculator BMI Tracker")
            .build()

        return true
    }

    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    suspend fun uploadBackup(
        encryptedData: ByteArray,
        entryCount: Int,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val drive = driveService ?: return@withContext false

        onProgress(0.1f)

        try {
            // Create app folder if needed
            val folderId = getOrCreateAppFolder(drive)
            onProgress(0.3f)

            // Create file metadata
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "HealthCalc_Backup_$timestamp.hcb"

            val fileMetadata = DriveFile().apply {
                name = fileName
                parents = listOf(folderId)
                description = "Health Calculator backup - $entryCount entries"
                properties = mapOf(
                    "entry_count" to entryCount.toString(),
                    "backup_version" to BackupMetadata.BACKUP_VERSION.toString(),
                    "device" to android.os.Build.MODEL
                )
            }

            onProgress(0.5f)

            val content = ByteArrayContent(MIME_TYPE, encryptedData)

            drive.files().create(fileMetadata, content)
                .setFields("id, name, size, createdTime, properties")
                .execute()

            onProgress(1f)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun listBackups(): List<BackupMetadata> = withContext(Dispatchers.IO) {
        val drive = driveService ?: return@withContext emptyList()

        try {
            val folderId = getOrCreateAppFolder(drive)

            val result = drive.files().list()
                .setQ("'$folderId' in parents and trashed = false")
                .setFields("files(id, name, size, createdTime, properties)")
                .setOrderBy("createdTime desc")
                .execute()

            result.files?.map { file ->
                BackupMetadata(
                    id = file.id,
                    fileName = file.name,
                    timestamp = file.createdTime?.value ?: 0L,
                    sizeBytes = file.getSize()?.toLong() ?: 0L,
                    entryCount = file.properties?.get("entry_count")?.toIntOrNull() ?: 0,
                    version = file.properties?.get("backup_version")?.toIntOrNull()
                        ?: BackupMetadata.BACKUP_VERSION,
                    source = BackupSource.GOOGLE_DRIVE
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun downloadBackup(
        fileId: String,
        onProgress: (Float) -> Unit
    ): ByteArray? = withContext(Dispatchers.IO) {
        val drive = driveService ?: return@withContext null

        try {
            onProgress(0.1f)

            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream)

            onProgress(0.8f)

            val data = outputStream.toByteArray()
            onProgress(1f)
            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteBackup(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val drive = driveService ?: return@withContext false
        try {
            drive.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getOrCreateAppFolder(drive: Drive): String {
        // Check if folder exists
        val query = "name = '$APP_FOLDER' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val result = drive.files().list().setQ(query).setFields("files(id)").execute()

        result.files?.firstOrNull()?.let { return it.id }

        // Create folder
        val folderMetadata = DriveFile().apply {
            name = APP_FOLDER
            mimeType = "application/vnd.google-apps.folder"
        }

        val folder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()

        return folder.id
    }

    fun signOut() {
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        driveService = null
    }
}
