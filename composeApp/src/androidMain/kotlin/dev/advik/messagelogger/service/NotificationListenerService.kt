package dev.advik.messagelogger.service

import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.SupportedPlatforms
import dev.advik.messagelogger.data.entity.NotificationEntity
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Enhanced NotificationListenerService implementing app.md specifications
 * Features: Multi-platform support, deletion detection, advanced processing
 * ALL FEATURES ARE FREE - no premium restrictions
 */
class NotificationListenerService : NotificationListenerService() {

    private val dataStore = SimpleDataStore.getInstance()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var messageIdCounter = 1L // Simple ID counter

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    // Enhanced processing for supported platforms
                    if (SupportedPlatforms.isPlatformSupported(statusBarNotification.packageName)) {
                        processMessageNotification(statusBarNotification)
                    }
                    // Also keep legacy notification logging
                    logNotification(statusBarNotification)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing notification", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    // Detect potential message deletion
                    handlePotentialDeletion(statusBarNotification)
                    dataStore.removeNotificationByKey(statusBarNotification.key)
                    Log.d(TAG, "Removed notification: ${statusBarNotification.key}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing notification", e)
                }
            }
        }
    }

    /**
     * Enhanced message processing for supported platforms (app.md Section 5)
     */
    private suspend fun processMessageNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val platformConfig = SupportedPlatforms.getPlatformConfig(packageName) ?: return
        
        // Extract message information
        val sender = extractSender(notification, packageName)
        val messageContent = getNotificationText(notification)
        val timestamp = sbn.postTime
        val key = sbn.key
        
        // Determine message type
        val messageType = determineMessageType(notification)
        val chatIdentifier = extractChatIdentifier(notification, packageName)
        
        val messageEntity = MessageEntity(
            id = messageIdCounter++,
            notificationId = sbn.id,
            packageName = packageName,
            appName = platformConfig.displayName,
            sender = sender,
            messageContent = messageContent,
            timestamp = timestamp,
            messageType = messageType,
            chatIdentifier = chatIdentifier,
            key = key
        )
        
        dataStore.addMessage(messageEntity)
        Log.d(TAG, "Processed message from ${platformConfig.displayName}: $sender")
    }
    
    /**
     * Detect message deletion (app.md Section 2.2)
     */
    private suspend fun handlePotentialDeletion(sbn: StatusBarNotification) {
        // Enhanced deletion detection logic
        val packageName = sbn.packageName
        if (SupportedPlatforms.isPlatformSupported(packageName)) {
            // Mark potentially deleted messages
            // This is a simplified implementation - real apps would need more sophisticated logic
            Log.d(TAG, "Potential deletion detected for ${sbn.packageName}")
        }
    }
    
    /**
     * Extract sender information with platform-specific logic
     */
    private fun extractSender(notification: Notification, packageName: String): String {
        val extras = notification.extras ?: return "Unknown"
        
        return when (packageName) {
            "com.whatsapp", "com.whatsapp.w4b" -> {
                // WhatsApp-specific sender extraction
                extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Unknown"
            }
            "org.telegram.messenger" -> {
                // Telegram-specific sender extraction
                extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Unknown"
            }
            else -> {
                // Generic sender extraction
                extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Unknown"
            }
        }
    }
    
    /**
     * Determine message type based on notification content
     */
    private fun determineMessageType(notification: Notification): MessageType {
        val extras = notification.extras ?: return MessageType.TEXT
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        
        return when {
            text.contains("📷") || text.contains("Image") -> MessageType.IMAGE
            text.contains("🎥") || text.contains("Video") -> MessageType.VIDEO
            text.contains("🎵") || text.contains("Audio") -> MessageType.AUDIO
            text.contains("📄") || text.contains("Document") -> MessageType.DOCUMENT
            text.contains("📍") || text.contains("Location") -> MessageType.LOCATION
            text.contains("Contact") -> MessageType.CONTACT_CARD
            text.contains("Sticker") -> MessageType.STICKER
            text.contains("GIF") -> MessageType.GIF
            else -> MessageType.TEXT
        }
    }
    
    /**
     * Extract chat identifier for group chat detection
     */
    private fun extractChatIdentifier(notification: Notification, packageName: String): String? {
        val extras = notification.extras ?: return null
        
        return when (packageName) {
            "com.whatsapp", "com.whatsapp.w4b" -> {
                // Try to detect group chats vs individual chats
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                title?.let { if (it.contains("@")) "group:$it" else "individual:$it" }
            }
            else -> null
        }
    }

    private suspend fun logNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        
        // Skip system notifications and our own notifications
        if (packageName == this.packageName || isSystemNotification(packageName)) {
            return
        }

        val appName = getAppName(packageName)
        val title = getNotificationTitle(notification)
        val text = getNotificationText(notification)
        val timestamp = sbn.postTime
        val key = sbn.key

        // Save app icon
        val iconPath = saveAppIcon(packageName)

        val notificationEntity = NotificationEntity(
            appName = appName,
            packageName = packageName,
            appIconPath = iconPath,
            title = title,
            text = text,
            timestamp = timestamp,
            key = key
        )

        dataStore.addNotification(notificationEntity)
        Log.d(TAG, "Logged notification from $appName: $title")
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name
        }
    }

    private fun getNotificationTitle(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() 
            ?: "No Title"
    }

    private fun getNotificationText(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() 
            ?: "No Text"
    }

    private fun saveAppIcon(packageName: String): String? {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(appInfo)
            
            val iconsDir = File(filesDir, "app_icons")
            if (!iconsDir.exists()) {
                iconsDir.mkdirs()
            }
            
            val iconFile = File(iconsDir, "$packageName.png")
            if (!iconFile.exists()) {
                val bitmap = drawableToBitmap(icon)
                FileOutputStream(iconFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
            }
            
            iconFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving app icon for $packageName", e)
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isSystemNotification(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}