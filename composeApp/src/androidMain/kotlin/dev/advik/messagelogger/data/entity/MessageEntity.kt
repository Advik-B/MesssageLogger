package dev.advik.messagelogger.data.entity

/**
 * Enhanced message entity based on app.md specification
 * Represents recovered messages with all metadata
 */
data class MessageEntity(
    val id: Long = 0,
    val notificationId: Int,
    val packageName: String,
    val appName: String,
    val sender: String,
    val messageContent: String,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    val messageType: MessageType,
    val chatIdentifier: String?, // Group chat or individual chat identifier
    val filePath: String? = null, // Path to media file if applicable
    val key: String // Notification key
)

enum class MessageType {
    TEXT,
    IMAGE, 
    VIDEO,
    AUDIO,
    VOICE_MESSAGE,
    DOCUMENT,
    STICKER,
    GIF,
    LOCATION,
    CONTACT_CARD,
    UNKNOWN
}