package dev.advik.messagelogger.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_images")
data class WhatsAppImageDatabaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalFileName: String,
    val backupFilePath: String,
    val originalFilePath: String,
    val timestamp: Long,
    val deletedTimestamp: Long? = null,
    val isDeleted: Boolean = false,
    val fileSize: Long
)