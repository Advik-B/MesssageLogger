package dev.advik.messagelogger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_images")
data class WhatsAppImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalPath: String,
    val backupPath: String?,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    val fileSizeBytes: Long = 0
)