package dev.advik.messagelogger.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageDatabaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationId: Int,
    val packageName: String,
    val appName: String,
    val sender: String,
    val messageContent: String,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    val messageType: String, // Store as string for database compatibility
    val chatIdentifier: String?,
    val key: String
)