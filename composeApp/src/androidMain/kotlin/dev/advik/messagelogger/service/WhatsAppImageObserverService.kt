package dev.advik.messagelogger.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Service to monitor WhatsApp images folder for changes
 * Automatically backs up detected images and sends notifications on deletions
 */
class WhatsAppImageObserverService : Service() {

    private lateinit var repository: MessageLoggerRepository
    private var fileObserver: FileObserver? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val whatsAppImagesPath = File(
        Environment.getExternalStorageDirectory(),
        "WhatsApp/Media/WhatsApp Images"
    ).absolutePath
    
    private val backupDirectory by lazy {
        File(getExternalFilesDir(null), "whatsapp_backups").apply {
            mkdirs()
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = MessageLoggerRepository.getInstance(this)
        DeletedMediaNotificationService.initialize(this)
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

            // Check if we already have this file
            val existingImages = repository.getAllImages().first()
            if (existingImages.any { it.originalFileName == fileName && !it.isDeleted }) {
                Log.d(TAG, "File already exists in database: $fileName")
                return
            }

            // Immediately backup the file
            val backupFile = File(backupDirectory, fileName)
            if (copyFile(originalFile, backupFile)) {
                // Create database entry
                val imageEntity = WhatsAppImageEntity(
                    originalFileName = fileName,
                    backupFilePath = backupFile.absolutePath,
                    originalFilePath = originalFile.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    fileSize = originalFile.length()
                )

                repository.insertImage(imageEntity)
                Log.d(TAG, "Immediately backed up WhatsApp image: $fileName")
            } else {
                Log.e(TAG, "Failed to backup file: $fileName")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling file created: $fileName", e)
        }
    }

    private suspend fun handleFileDeleted(fileName: String) {
        try {
            if (!isImageFile(fileName)) {
                return
            }

            // Mark the file as deleted in database and send notification
            val currentTime = System.currentTimeMillis()
            repository.markImageAsDeleted(fileName, currentTime)
            
            // Send notification about deleted media with backup available
            DeletedMediaNotificationService.sendDeletedMediaNotification(
                context = this@WhatsAppImageObserverService,
                fileName = fileName,
                mediaType = "Image",
                appName = "WhatsApp"
            )
            
            Log.d(TAG, "Marked WhatsApp image as deleted and sent notification: $fileName")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling file deleted: $fileName", e)
        }
    }

    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
        return imageExtensions.any { fileName.lowercase().endsWith(it) }
    }

    private fun copyFile(source: File, destination: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy file: ${source.name}", e)
            false
        }
    }

    companion object {
        private const val TAG = "WhatsAppImageObserver"
    }
}