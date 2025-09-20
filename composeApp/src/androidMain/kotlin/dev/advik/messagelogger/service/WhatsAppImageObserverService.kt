package dev.advik.messagelogger.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Service to monitor WhatsApp images folder for changes
 */
class WhatsAppImageObserverService : Service() {

    private val dataStore = SimpleDataStore.getInstance()
    private var fileObserver: FileObserver? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val whatsAppImagesPath = File(
        Environment.getExternalStorageDirectory(),
        "WhatsApp/Media/WhatsApp Images"
    ).absolutePath

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WhatsAppImageObserverService created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    private fun startMonitoring() {
        val whatsAppDir = File(whatsAppImagesPath)
        if (!whatsAppDir.exists() || !whatsAppDir.canRead()) {
            Log.w(TAG, "WhatsApp Images directory not accessible: $whatsAppImagesPath")
            return
        }

        stopMonitoring() // Stop any existing observer

        fileObserver = object : FileObserver(whatsAppImagesPath, CREATE or DELETE) {
            override fun onEvent(event: Int, path: String?) {
                path?.let { fileName ->
                    when (event) {
                        CREATE -> {
                            serviceScope.launch {
                                handleFileCreated(fileName)
                            }
                        }
                        DELETE -> {
                            serviceScope.launch {
                                handleFileDeleted(fileName)
                            }
                        }
                        else -> {
                            // Do nothing for other events
                        }
                    }
                }
            }
        }

        fileObserver?.startWatching()
        Log.d(TAG, "Started monitoring WhatsApp Images directory")
    }

    private fun stopMonitoring() {
        fileObserver?.stopWatching()
        fileObserver = null
        Log.d(TAG, "Stopped monitoring WhatsApp Images directory")
    }

    private suspend fun handleFileCreated(fileName: String) {
        try {
            if (!isImageFile(fileName)) {
                return
            }

            val originalFile = File(whatsAppImagesPath, fileName)
            if (!originalFile.exists() || !originalFile.canRead()) {
                return
            }

            // Check if we already have this file (simple check by filename)
            val existingImages = dataStore.whatsAppImages.value
            if (existingImages.any { it.originalFileName == fileName }) {
                Log.d(TAG, "File already exists in database: $fileName")
                return
            }

            // Create backup directory if it doesn't exist
            val backupDir = File(filesDir, "WhatsAppBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Copy file to internal storage
            val backupFile = File(backupDir, fileName)
            copyFile(originalFile, backupFile)

            // Create database entry
            val imageEntity = WhatsAppImageEntity(
                originalFileName = fileName,
                backupFilePath = backupFile.absolutePath,
                originalFilePath = originalFile.absolutePath,
                timestamp = System.currentTimeMillis(),
                fileSize = originalFile.length()
            )

            dataStore.addWhatsAppImage(imageEntity)
            Log.d(TAG, "Backed up WhatsApp image: $fileName")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling file created: $fileName", e)
        }
    }

    private suspend fun handleFileDeleted(fileName: String) {
        try {
            if (!isImageFile(fileName)) {
                return
            }

            val existingImages = dataStore.whatsAppImages.value
            val existingImage = existingImages.find { it.originalFileName == fileName && !it.isDeleted }
            if (existingImage != null) {
                dataStore.markImageAsDeleted(fileName, System.currentTimeMillis())
                Log.d(TAG, "Marked WhatsApp image as deleted: $fileName")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling file deleted: $fileName", e)
        }
    }

    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
        return imageExtensions.any { fileName.lowercase().endsWith(it) }
    }

    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }

    companion object {
        private const val TAG = "WhatsAppImageObserver"
    }
}