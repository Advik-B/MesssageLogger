package dev.advik.messagelogger.repository

import kotlinx.coroutines.flow.Flow
import dev.advik.messagelogger.data.dao.NotificationLogDao
import dev.advik.messagelogger.data.dao.WhatsAppImageDao
import dev.advik.messagelogger.data.entity.NotificationLog
import dev.advik.messagelogger.data.entity.WhatsAppImage

class MessageLoggerRepository(
    private val notificationDao: NotificationLogDao,
    private val whatsAppImageDao: WhatsAppImageDao
) {
    
    // Notification methods
    fun getAllNotifications(): Flow<List<NotificationLog>> = notificationDao.getAllNotifications()
    
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationLog>> = 
        notificationDao.getNotificationsByPackage(packageName)
    
    fun getDistinctApps(): Flow<List<NotificationLog>> = notificationDao.getDistinctApps()
    
    suspend fun getNotificationCountByPackage(packageName: String): Int = 
        notificationDao.getNotificationCountByPackage(packageName)
    
    suspend fun insertNotification(notification: NotificationLog) = 
        notificationDao.insertNotification(notification)
    
    suspend fun deleteOldNotifications(cutoffTime: Long) = 
        notificationDao.deleteOldNotifications(cutoffTime)
    
    // WhatsApp Image methods
    fun getAllWhatsAppImages(): Flow<List<WhatsAppImage>> = whatsAppImageDao.getAllImages()
    
    fun getActiveImages(): Flow<List<WhatsAppImage>> = whatsAppImageDao.getActiveImages()
    
    fun getDeletedImages(): Flow<List<WhatsAppImage>> = whatsAppImageDao.getDeletedImages()
    
    suspend fun insertImage(image: WhatsAppImage) = whatsAppImageDao.insertImage(image)
    
    suspend fun markImageAsDeleted(fileName: String, deletedTime: Long) = 
        whatsAppImageDao.markImageAsDeleted(fileName, deletedTime)
}