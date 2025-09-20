package dev.advik.messagelogger.data

import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Simple in-memory data store for demonstration
 * In a real app, this would be backed by Room database
 */
class SimpleDataStore {
    
    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    private val _whatsAppImages = MutableStateFlow<List<WhatsAppImageEntity>>(emptyList())
    
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()
    val whatsAppImages: StateFlow<List<WhatsAppImageEntity>> = _whatsAppImages.asStateFlow()
    
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