package dev.advik.messagelogger.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationDatabaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val appIconPath: String?,
    val title: String,
    val text: String,
    val timestamp: Long,
    val key: String
)