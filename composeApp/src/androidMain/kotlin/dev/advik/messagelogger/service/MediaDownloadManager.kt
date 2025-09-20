package dev.advik.messagelogger.service

import android.content.Context
import android.util.Log
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.MediaFileEntity
import dev.advik.messagelogger.data.entity.DownloadStatus
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Media Download Manager implementing app.md Section 3.2
 * FREE: Automatic media downloading (was premium feature)
 */
class MediaDownloadManager(private val context: Context) {
    
    private val dataStore = SimpleDataStore.getInstance()
    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaIdCounter = 1L
    
    companion object {
        private const val TAG = "MediaDownloadManager"
        
        @Volatile
        private var INSTANCE: MediaDownloadManager? = null
        
        fun getInstance(context: Context): MediaDownloadManager {
            return INSTANCE ?: synchronized(this) {
                val instance = MediaDownloadManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Auto-download media files from notifications
     * FREE feature - no restrictions
     */
    fun scheduleDownload(
        messageId: Long,
        mediaUrl: String?,
        fileType: String,
        mimeType: String = "application/octet-stream"
    ) {
        if (mediaUrl == null) return
        
        val settings = dataStore.settings.value
        if (!settings.autoDownloadMedia) return
        
        downloadScope.launch {
            try {
                val mediaFile = MediaFileEntity(
                    id = mediaIdCounter++,
                    messageId = messageId,
                    fileType = fileType,
                    filePath = "", // Will be set after download
                    fileSize = 0L, // Will be set after download
                    mimeType = mimeType,
                    downloadStatus = DownloadStatus.PENDING,
                    originalUrl = mediaUrl
                )
                
                dataStore.addMediaFile(mediaFile)
                downloadMedia(mediaFile)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule download", e)
            }
        }
    }
    
    private suspend fun downloadMedia(mediaFile: MediaFileEntity) {
        try {
            // Update status to downloading
            dataStore.updateMediaDownloadStatus(mediaFile.id, DownloadStatus.DOWNLOADING)
            
            // Create directory structure following app.md specification
            val downloadsDir = createDownloadDirectory(mediaFile.fileType)
            val fileName = "media_${mediaFile.messageId}_${System.currentTimeMillis()}.${getFileExtension(mediaFile.fileType)}"
            val targetFile = File(downloadsDir, fileName)
            
            // Simulate media download (in real app would use actual HTTP client)
            delay(1000) // Simulate download time
            
            // Create placeholder file for demo
            FileOutputStream(targetFile).use { fos ->
                fos.write("Demo ${mediaFile.fileType} content for message ${mediaFile.messageId}".toByteArray())
            }
            
            // Update media file with download results
            val updatedMediaFile = mediaFile.copy(
                filePath = targetFile.absolutePath,
                fileSize = targetFile.length(),
                downloadStatus = DownloadStatus.COMPLETED,
                downloadTimestamp = System.currentTimeMillis()
            )
            
            dataStore.addMediaFile(updatedMediaFile)
            dataStore.updateMediaDownloadStatus(mediaFile.id, DownloadStatus.COMPLETED)
            
            Log.d(TAG, "Downloaded media: ${targetFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for media ${mediaFile.id}", e)
            dataStore.updateMediaDownloadStatus(mediaFile.id, DownloadStatus.FAILED)
        }
    }
    
    /**
     * Create directory structure as specified in app.md Section 3.2
     */
    private fun createDownloadDirectory(fileType: String): File {
        val baseDir = File(context.filesDir, "recovered_messages")
        val typeDir = when (fileType.lowercase()) {
            "image" -> File(baseDir, "whatsapp/images")
            "video" -> File(baseDir, "whatsapp/videos") 
            "audio" -> File(baseDir, "whatsapp/audio")
            "document" -> File(baseDir, "whatsapp/documents")
            else -> File(baseDir, "whatsapp/other")
        }
        
        if (!typeDir.exists()) {
            typeDir.mkdirs()
        }
        
        return typeDir
    }
    
    private fun getFileExtension(fileType: String): String {
        return when (fileType.lowercase()) {
            "image" -> "jpg"
            "video" -> "mp4"
            "audio" -> "mp3"
            "document" -> "pdf"
            else -> "bin"
        }
    }
    
    /**
     * FREE: Bulk download management (was premium feature)
     */
    fun downloadAllPendingMedia() {
        downloadScope.launch {
            val pendingMedia = dataStore.mediaFiles.value.filter { 
                it.downloadStatus == DownloadStatus.PENDING 
            }
            
            Log.d(TAG, "Starting bulk download of ${pendingMedia.size} media files")
            
            pendingMedia.forEach { mediaFile ->
                downloadMedia(mediaFile)
                delay(500) // Throttle downloads
            }
        }
    }
    
    /**
     * Storage management following app.md Section 6.2
     */
    fun cleanupOldMedia() {
        downloadScope.launch {
            val settings = dataStore.settings.value
            if (settings.retentionDays <= 0) return@launch // Unlimited retention
            
            val cutoffTime = System.currentTimeMillis() - (settings.retentionDays * 24 * 60 * 60 * 1000L)
            val oldMedia = dataStore.mediaFiles.value.filter { 
                (it.downloadTimestamp ?: 0) < cutoffTime 
            }
            
            oldMedia.forEach { mediaFile ->
                try {
                    val file = File(mediaFile.filePath)
                    if (file.exists()) {
                        file.delete()
                        Log.d(TAG, "Cleaned up old media: ${file.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to cleanup media: ${mediaFile.filePath}", e)
                }
            }
        }
    }
    
    fun shutdown() {
        downloadScope.cancel()
    }
}