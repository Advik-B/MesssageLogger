package dev.advik.messagelogger.data

import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.MediaFileEntity
import dev.advik.messagelogger.data.entity.AppSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

/**
 * Enhanced data store implementing app.md specifications
 * All premium features are FREE - no restrictions
 */
class SimpleDataStore {
    
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    private val _whatsAppImages = MutableStateFlow<List<WhatsAppImageEntity>>(emptyList())
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    private val _mediaFiles = MutableStateFlow<List<MediaFileEntity>>(emptyList())
    private val _settings = MutableStateFlow(getDefaultSettings())
    
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()
    val whatsAppImages: StateFlow<List<WhatsAppImageEntity>> = _whatsAppImages.asStateFlow()
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()
    val mediaFiles: StateFlow<List<MediaFileEntity>> = _mediaFiles.asStateFlow()
    val settings: StateFlow<AppSettingsEntity> = _settings.asStateFlow()
    
    // FREE: Unlimited message recovery (no 50/day limit)
    val recoveredMessages = messages.map { list ->
        list.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // FREE: Unlimited retention (no 7-day limit)
    val deletedMessages = messages.map { list ->
        list.filter { it.isDeleted }.sortedByDescending { it.deletedTimestamp ?: 0 }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // FREE: Multi-app monitoring (was premium feature)
    val messagesByApp = messages.map { list ->
        list.groupBy { it.packageName }
            .mapValues { (_, msgs) -> msgs.sortedByDescending { it.timestamp } }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // App info data class for simple implementation
    data class SimpleAppInfo(
        val packageName: String,
        val appName: String
    )
    
    val distinctApps = notifications.map { notificationList ->
        notificationList
            .distinctBy { it.packageName }
            .map { SimpleAppInfo(it.packageName, it.appName) }
            .sortedBy { it.appName }
    }
    
    // === Enhanced Message Management (FREE features) ===
    
    fun addMessage(message: MessageEntity) {
        _messages.value = _messages.value + message
    }
    
    fun markMessageAsDeleted(messageId: Long, deletedTimestamp: Long) {
        _messages.value = _messages.value.map { message ->
            if (message.id == messageId) {
                message.copy(isDeleted = true, deletedTimestamp = deletedTimestamp)
            } else {
                message
            }
        }
    }
    
    // FREE: Advanced search with filters (was premium)
    fun searchMessages(
        query: String,
        packageName: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): StateFlow<List<MessageEntity>> {
        return messages.map { list ->
            list.filter { message ->
                val matchesQuery = query.isEmpty() || 
                    message.messageContent.contains(query, ignoreCase = true) ||
                    message.sender.contains(query, ignoreCase = true)
                
                val matchesPackage = packageName == null || message.packageName == packageName
                
                val matchesDateRange = (startDate == null || message.timestamp >= startDate) &&
                    (endDate == null || message.timestamp <= endDate)
                
                matchesQuery && matchesPackage && matchesDateRange
            }.sortedByDescending { it.timestamp }
        } as StateFlow<List<MessageEntity>>
    }
    
    // === Media File Management ===
    
    fun addMediaFile(mediaFile: MediaFileEntity) {
        _mediaFiles.value = _mediaFiles.value + mediaFile
    }
    
    fun updateMediaDownloadStatus(mediaId: Long, status: dev.advik.messagelogger.data.entity.DownloadStatus) {
        _mediaFiles.value = _mediaFiles.value.map { media ->
            if (media.id == mediaId) {
                media.copy(downloadStatus = status, downloadTimestamp = System.currentTimeMillis())
            } else {
                media
            }
        }
    }
    
    // === Settings Management ===
    
    fun updateSettings(settings: AppSettingsEntity) {
        _settings.value = settings
    }
    
    fun addMonitoredApp(packageName: String) {
        val currentSettings = _settings.value
        _settings.value = currentSettings.copy(
            monitoredApps = currentSettings.monitoredApps + packageName
        )
    }
    
    fun removeMonitoredApp(packageName: String) {
        val currentSettings = _settings.value
        _settings.value = currentSettings.copy(
            monitoredApps = currentSettings.monitoredApps - packageName
        )
    }
    
    private fun getDefaultSettings(): AppSettingsEntity {
        return AppSettingsEntity(
            monitoredApps = SupportedPlatforms.ALL_PACKAGE_NAMES, // Monitor all supported apps by default
            autoDownloadMedia = true,
            storageLimit = -1, // Unlimited storage (FREE)
            retentionDays = -1, // Unlimited retention (FREE)
            enableAppLock = false,
            biometricAuth = false,
            enableEncryption = true
        )
    }
    
    fun addNotification(notification: NotificationEntity) {
        _notifications.value = _notifications.value + notification
    }
    
    fun removeNotificationByKey(key: String) {
        _notifications.value = _notifications.value.filter { it.key != key }
    }
    
    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
    
    fun getNotificationsByPackage(packageName: String): StateFlow<List<NotificationEntity>> {
        return notifications.map { list ->
            list.filter { it.packageName == packageName }
        } as StateFlow<List<NotificationEntity>>
    }
    
    fun addWhatsAppImage(image: WhatsAppImageEntity) {
        _whatsAppImages.value = _whatsAppImages.value + image
    }
    
    fun markImageAsDeleted(fileName: String, timestamp: Long) {
        _whatsAppImages.value = _whatsAppImages.value.map { image ->
            if (image.originalFileName == fileName && !image.isDeleted) {
                image.copy(isDeleted = true, deletedTimestamp = timestamp)
            } else {
                image
            }
        }
    }
    
    val activeImages = whatsAppImages.map { list ->
        list.filter { !it.isDeleted }
    }
    
    val deletedImages = whatsAppImages.map { list ->
        list.filter { it.isDeleted }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleDataStore? = null
        
        fun getInstance(): SimpleDataStore {
            return INSTANCE ?: synchronized(this) {
                val instance = SimpleDataStore()
                INSTANCE = instance
                instance
            }
        }
    }
}