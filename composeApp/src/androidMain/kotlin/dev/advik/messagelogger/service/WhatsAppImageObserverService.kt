package dev.advik.messagelogger.service

import android.app.Service
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import dev.advik.messagelogger.data.database.MessageLoggerDatabase
import dev.advik.messagelogger.data.entity.WhatsAppImage

class WhatsAppImageObserverService : Service() {
    
    companion object {
        private const val TAG = "WhatsAppImageObserver"
        private const val WHATSAPP_IMAGES_PATH = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images"
    }
    
    private lateinit var database: MessageLoggerDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var fileObserver: FileObserver? = null
    
    override fun onCreate() {
        super.onCreate()
        database = MessageLoggerDatabase.getDatabase(this)
        setupFileObserver()
        Log.d(TAG, "WhatsApp Image Observer Service created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startFileObserver()
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopFileObserver()
        Log.d(TAG, "WhatsApp Image Observer Service destroyed")
    }
    
    private fun setupFileObserver() {
        val whatsappImagesDir = File(WHATSAPP_IMAGES_PATH)
        
        if (!whatsappImagesDir.exists()) {
            Log.w(TAG, "WhatsApp Images directory not found")
            return
        }
        
        fileObserver = object : FileObserver(whatsappImagesDir, CREATE or DELETE or MOVED_FROM or MOVED_TO) {
            override fun onEvent(event: Int, path: String?) {
                if (path == null) return
                
                serviceScope.launch {
                    try {
                        when (event and ALL_EVENTS) {
                            CREATE, MOVED_TO -> handleImageCreated(path)
                            DELETE, MOVED_FROM -> handleImageDeleted(path)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing file event", e)
                    }
                }
            }
        }
    }
    
    private fun startFileObserver() {
        fileObserver?.startWatching()
        Log.d(TAG, "File observer started")
    }
    
    private fun stopFileObserver() {
        fileObserver?.stopWatching()
        Log.d(TAG, "File observer stopped")
    }
    
    private suspend fun handleImageCreated(fileName: String) {
        if (!isImageFile(fileName)) return
        
        Log.d(TAG, "Image created: $fileName")
        
        val originalFile = File(WHATSAPP_IMAGES_PATH, fileName)
        if (!originalFile.exists()) return
        
        // Create backup copy
        val backupPath = copyImageToBackup(originalFile)
        
        // Check if image already exists in database
        val existingImage = database.whatsAppImageDao().getImageByFileName(fileName)
        
        if (existingImage == null) {
            // Create new entry
            val whatsAppImage = WhatsAppImage(
                fileName = fileName,
                originalPath = originalFile.absolutePath,
                backupPath = backupPath,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = originalFile.length()
            )
            
            database.whatsAppImageDao().insertImage(whatsAppImage)
        } else if (existingImage.isDeleted) {
            // Image was recreated, update existing entry
            val updatedImage = existingImage.copy(
                isDeleted = false,
                deletedTimestamp = null,
                backupPath = backupPath,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = originalFile.length()
            )
            
            database.whatsAppImageDao().updateImage(updatedImage)
        }
    }
    
    private suspend fun handleImageDeleted(fileName: String) {
        if (!isImageFile(fileName)) return
        
        Log.d(TAG, "Image deleted: $fileName")
        
        // Mark image as deleted in database
        database.whatsAppImageDao().markImageAsDeleted(fileName, System.currentTimeMillis())
    }
    
    private fun copyImageToBackup(originalFile: File): String? {
        return try {
            val backupDir = File(filesDir, "WhatsAppBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, originalFile.name)
            
            FileInputStream(originalFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "Image backed up: ${originalFile.name}")
            backupFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying image to backup", e)
            null
        }
    }
    
    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
        return imageExtensions.any { fileName.lowercase().endsWith(it) }
    }
}