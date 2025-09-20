package dev.advik.messagelogger.data.repository

import android.content.Context
import dev.advik.messagelogger.data.database.MessageLoggerDatabase
import dev.advik.messagelogger.data.database.entity.MessageDatabaseEntity
import dev.advik.messagelogger.data.database.entity.NotificationDatabaseEntity
import dev.advik.messagelogger.data.database.entity.WhatsAppImageDatabaseEntity
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.MessageType
import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageLoggerRepository(context: Context) {
    
    private val database = MessageLoggerDatabase.getDatabase(context)
    private val messageDao = database.messageDao()
    private val notificationDao = database.notificationDao()
    private val whatsAppImageDao = database.whatsAppImageDao()
    
    // Message operations
    fun getAllMessages(): Flow<List<MessageEntity>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { entity -> entity.toMessageEntity() }
        }
    }
    
    fun getMessagesByPackage(packageName: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByPackage(packageName).map { entities ->
            entities.map { entity -> entity.toMessageEntity() }
        }
    }
    
    fun getMessagesBySender(sender: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesBySender(sender).map { entities ->
            entities.map { entity -> entity.toMessageEntity() }
        }
    }
    
    fun getSendersByPackage(packageName: String): Flow<List<String>> {
        return messageDao.getSendersByPackage(packageName)
    }
    
    fun getAllPackages(): Flow<List<String>> {
        return messageDao.getAllPackages()
    }
    
    fun searchMessages(query: String): Flow<List<MessageEntity>> {
        return messageDao.searchMessages(query).map { entities ->
            entities.map { entity -> entity.toMessageEntity() }
        }
    }
    
    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message.toDatabaseEntity())
    }
    
    // Notification operations
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { entity -> entity.toNotificationEntity() }
        }
    }
    
    suspend fun insertNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification.toDatabaseEntity())
    }
    
    suspend fun deleteNotificationByKey(key: String) {
        notificationDao.deleteNotificationByKey(key)
    }
    
    // WhatsApp Image operations
    fun getAllImages(): Flow<List<WhatsAppImageEntity>> {
        return whatsAppImageDao.getAllImages().map { entities ->
            entities.map { entity -> entity.toWhatsAppImageEntity() }
        }
    }
    
    fun getActiveImages(): Flow<List<WhatsAppImageEntity>> {
        return whatsAppImageDao.getActiveImages().map { entities ->
            entities.map { entity -> entity.toWhatsAppImageEntity() }
        }
    }
    
    fun getDeletedImages(): Flow<List<WhatsAppImageEntity>> {
        return whatsAppImageDao.getDeletedImages().map { entities ->
            entities.map { entity -> entity.toWhatsAppImageEntity() }
        }
    }
    
    suspend fun insertImage(image: WhatsAppImageEntity) {
        whatsAppImageDao.insertImage(image.toDatabaseEntity())
    }
    
    suspend fun markImageAsDeleted(fileName: String, timestamp: Long) {
        whatsAppImageDao.markImageAsDeleted(fileName, timestamp)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: MessageLoggerRepository? = null
        
        fun getInstance(context: Context): MessageLoggerRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = MessageLoggerRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}

// Extension functions for entity conversion
private fun MessageDatabaseEntity.toMessageEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        notificationId = notificationId,
        packageName = packageName,
        appName = appName,
        sender = sender,
        messageContent = messageContent,
        timestamp = timestamp,
        isDeleted = isDeleted,
        deletedTimestamp = deletedTimestamp,
        messageType = MessageType.valueOf(messageType),
        chatIdentifier = chatIdentifier,
        key = key
    )
}

private fun MessageEntity.toDatabaseEntity(): MessageDatabaseEntity {
    return MessageDatabaseEntity(
        id = id,
        notificationId = notificationId,
        packageName = packageName,
        appName = appName,
        sender = sender,
        messageContent = messageContent,
        timestamp = timestamp,
        isDeleted = isDeleted,
        deletedTimestamp = deletedTimestamp,
        messageType = messageType.name,
        chatIdentifier = chatIdentifier,
        key = key
    )
}

private fun NotificationDatabaseEntity.toNotificationEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        appName = appName,
        packageName = packageName,
        appIconPath = appIconPath,
        title = title,
        text = text,
        timestamp = timestamp,
        key = key
    )
}

private fun NotificationEntity.toDatabaseEntity(): NotificationDatabaseEntity {
    return NotificationDatabaseEntity(
        id = id,
        appName = appName,
        packageName = packageName,
        appIconPath = appIconPath,
        title = title,
        text = text,
        timestamp = timestamp,
        key = key
    )
}

private fun WhatsAppImageDatabaseEntity.toWhatsAppImageEntity(): WhatsAppImageEntity {
    return WhatsAppImageEntity(
        id = id,
        originalFileName = originalFileName,
        backupFilePath = backupFilePath,
        originalFilePath = originalFilePath,
        timestamp = timestamp,
        deletedTimestamp = deletedTimestamp,
        isDeleted = isDeleted,
        fileSize = fileSize
    )
}

private fun WhatsAppImageEntity.toDatabaseEntity(): WhatsAppImageDatabaseEntity {
    return WhatsAppImageDatabaseEntity(
        id = id,
        originalFileName = originalFileName,
        backupFilePath = backupFilePath,
        originalFilePath = originalFilePath,
        timestamp = timestamp,
        deletedTimestamp = deletedTimestamp,
        isDeleted = isDeleted,
        fileSize = fileSize
    )
}