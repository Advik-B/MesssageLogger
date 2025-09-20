package dev.advik.messagelogger.util

import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.MessageType

/**
 * Enhanced demo data showcasing all FREE features from app.md
 */
object DemoData {
    
    fun addSampleData() {
        val dataStore = SimpleDataStore.getInstance()
        
        // Add sample notifications (legacy system)
        addSampleNotifications(dataStore)
        
        // Add sample WhatsApp images (legacy system)  
        addSampleImages(dataStore)
        
        // Add sample recovered messages (NEW: FREE premium features)
        addSampleRecoveredMessages(dataStore)
    }
    
    private fun addSampleNotifications(dataStore: SimpleDataStore) {
        val currentTime = System.currentTimeMillis()
        
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
    }
    
    private fun addSampleImages(dataStore: SimpleDataStore) {
        val currentTime = System.currentTimeMillis()
        
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
    
    private fun addSampleRecoveredMessages(dataStore: SimpleDataStore) {
        val currentTime = System.currentTimeMillis()
        var messageId = 1L
        
        // FREE: Multi-app monitoring - messages from all supported platforms
        val sampleMessages = listOf(
            // WhatsApp messages
            MessageEntity(
                id = messageId++,
                notificationId = 1001,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "John Doe",
                messageContent = "Hey! How's your day going? 😊",
                timestamp = currentTime - 300000,
                messageType = MessageType.TEXT,
                chatIdentifier = "individual:John Doe",
                key = "whatsapp_msg_1"
            ),
            MessageEntity(
                id = messageId++,
                notificationId = 1002,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Sarah",
                messageContent = "📷 Image",
                timestamp = currentTime - 600000,
                messageType = MessageType.IMAGE,
                chatIdentifier = "group:Family Group",
                key = "whatsapp_msg_2"
            ),
            MessageEntity(
                id = messageId++,
                notificationId = 1003,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Mom",
                messageContent = "This message was deleted",
                timestamp = currentTime - 900000,
                isDeleted = true,
                deletedTimestamp = currentTime - 600000,
                messageType = MessageType.TEXT,
                chatIdentifier = "individual:Mom",
                key = "whatsapp_msg_3"
            ),
            
            // Telegram messages (FREE: Multi-platform support)
            MessageEntity(
                id = messageId++,
                notificationId = 2001,
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                sender = "Alex",
                messageContent = "Check out this new Kotlin framework: https://example.com/kotlin-framework",
                timestamp = currentTime - 1200000,
                messageType = MessageType.TEXT,
                chatIdentifier = "group:Tech Discussion",
                key = "telegram_msg_1"
            ),
            MessageEntity(
                id = messageId++,
                notificationId = 2002,
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                sender = "DevBot",
                messageContent = "🎵 Audio Message",
                timestamp = currentTime - 1500000,
                messageType = MessageType.AUDIO,
                chatIdentifier = "individual:DevBot",
                key = "telegram_msg_2"
            ),
            
            // Instagram messages (FREE: Multi-platform support)
            MessageEntity(
                id = messageId++,
                notificationId = 3001,
                packageName = "com.instagram.android",
                appName = "Instagram",
                sender = "mike_photo",
                messageContent = "Loved your recent post! 📸",
                timestamp = currentTime - 1800000,
                messageType = MessageType.TEXT,
                chatIdentifier = "individual:mike_photo",
                key = "instagram_msg_1"
            ),
            
            // Signal messages (FREE: Multi-platform support)
            MessageEntity(
                id = messageId++,
                notificationId = 4001,
                packageName = "org.thoughtcrime.securesms",
                appName = "Signal",
                sender = "SecureContact",
                messageContent = "Meeting at 3pm tomorrow?",
                timestamp = currentTime - 2100000,
                messageType = MessageType.TEXT,
                chatIdentifier = "individual:SecureContact",
                key = "signal_msg_1"
            ),
            
            // More diverse message types
            MessageEntity(
                id = messageId++,
                notificationId = 1004,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Travel Group",
                messageContent = "📍 Location: Central Park, New York",
                timestamp = currentTime - 2400000,
                messageType = MessageType.LOCATION,
                chatIdentifier = "group:Travel Group",
                key = "whatsapp_location_1"
            ),
            MessageEntity(
                id = messageId++,
                notificationId = 1005,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Work Chat",
                messageContent = "📄 Document: Q4_Report.pdf",
                timestamp = currentTime - 2700000,
                messageType = MessageType.DOCUMENT,
                chatIdentifier = "group:Work Chat",
                key = "whatsapp_doc_1"
            ),
            MessageEntity(
                id = messageId++,
                notificationId = 1006,
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Gaming Squad",
                messageContent = "This deleted message contained game spoilers 🎮",
                timestamp = currentTime - 3000000,
                isDeleted = true,
                deletedTimestamp = currentTime - 2400000,
                messageType = MessageType.TEXT,
                chatIdentifier = "group:Gaming Squad",
                key = "whatsapp_deleted_2"
            )
        )
        
        // Add all sample messages to data store
        sampleMessages.forEach { message ->
            dataStore.addMessage(message)
        }
    }
}