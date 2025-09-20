package dev.advik.messagelogger.data.entity

data class WhatsAppImageEntity(
    val id: Long = 0,
    val originalFileName: String,
    val backupFilePath: String, // Path in app's internal storage
    val originalFilePath: String, // Original WhatsApp file path
    val timestamp: Long, // When the file was first detected
    val deletedTimestamp: Long? = null, // When the file was deleted (null if still exists)
    val isDeleted: Boolean = false,
    val fileSize: Long // File size in bytes
)