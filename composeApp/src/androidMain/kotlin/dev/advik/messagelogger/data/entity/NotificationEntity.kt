package dev.advik.messagelogger.data.entity

data class NotificationEntity(
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val appIconPath: String?, // Path to stored app icon
    val title: String,
    val text: String,
    val timestamp: Long, // Unix timestamp
    val key: String // Notification key for removing
)