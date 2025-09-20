package dev.advik.messagelogger.util

import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity

/**
 * Utility to add sample data for demonstration
 */
object DemoData {
    
    fun addSampleData() {
        val dataStore = SimpleDataStore.getInstance()
        val currentTime = System.currentTimeMillis()
        
        // Add sample notifications
        val sampleNotifications = listOf(
            NotificationEntity(
                id = 1,
                appName = "WhatsApp",
                packageName = "com.whatsapp",
                appIconPath = null,
                title = "John Doe",
                text = "Hey! How are you?",
                timestamp = currentTime - 300000,
                key = "whatsapp_1"
            ),
            NotificationEntity(
                id = 2,
                appName = "Instagram",
                packageName = "com.instagram.android",
                appIconPath = null,
                title = "Instagram",
                text = "jane_doe liked your photo",
                timestamp = currentTime - 600000,
                key = "instagram_1"
            ),
            NotificationEntity(
                id = 3,
                appName = "Gmail",
                packageName = "com.google.android.gm",
                appIconPath = null,
                title = "New Email",
                text = "You have 2 new messages",
                timestamp = currentTime - 900000,
                key = "gmail_1"
            ),
            NotificationEntity(
                id = 4,
                appName = "WhatsApp",
                packageName = "com.whatsapp",
                appIconPath = null,
                title = "Alice Smith",
                text = "Let's meet tomorrow",
                timestamp = currentTime - 1200000,
                key = "whatsapp_2"
            ),
            NotificationEntity(
                id = 5,
                appName = "Telegram",
                packageName = "org.telegram.messenger",
                appIconPath = null,
                title = "Bob Wilson",
                text = "Check out this link",
                timestamp = currentTime - 1500000,
                key = "telegram_1"
            )
        )
        
        sampleNotifications.forEach { notification ->
            dataStore.addNotification(notification)
        }
        
        // Add sample WhatsApp images
        val sampleImages = listOf(
            WhatsAppImageEntity(
                id = 1,
                originalFileName = "IMG-20240101-WA0001.jpg",
                backupFilePath = "/data/data/dev.advik.messagelogger/files/WhatsAppBackup/IMG-20240101-WA0001.jpg",
                originalFilePath = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20240101-WA0001.jpg",
                timestamp = currentTime - 86400000, // 1 day ago
                fileSize = 2048576 // 2MB
            ),
            WhatsAppImageEntity(
                id = 2,
                originalFileName = "IMG-20240102-WA0002.jpg",
                backupFilePath = "/data/data/dev.advik.messagelogger/files/WhatsAppBackup/IMG-20240102-WA0002.jpg",
                originalFilePath = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20240102-WA0002.jpg",
                timestamp = currentTime - 172800000, // 2 days ago
                deletedTimestamp = currentTime - 86400000, // Deleted 1 day ago
                isDeleted = true,
                fileSize = 1536000 // 1.5MB
            ),
            WhatsAppImageEntity(
                id = 3,
                originalFileName = "IMG-20240103-WA0003.jpg",
                backupFilePath = "/data/data/dev.advik.messagelogger/files/WhatsAppBackup/IMG-20240103-WA0003.jpg",
                originalFilePath = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20240103-WA0003.jpg",
                timestamp = currentTime - 259200000, // 3 days ago
                fileSize = 3145728 // 3MB
            )
        )
        
        sampleImages.forEach { image ->
            dataStore.addWhatsAppImage(image)
        }
    }
}