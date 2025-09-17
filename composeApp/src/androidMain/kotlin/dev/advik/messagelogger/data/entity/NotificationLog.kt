package dev.advik.messagelogger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val appIconPath: String?, // Path to saved app icon
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val category: String? = null,
    val priority: Int = 0
)