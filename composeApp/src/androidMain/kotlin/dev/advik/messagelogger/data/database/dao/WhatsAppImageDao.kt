package dev.advik.messagelogger.data.database.dao

import androidx.room.*
import dev.advik.messagelogger.data.database.entity.WhatsAppImageDatabaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppImageDao {
    
    @Query("SELECT * FROM whatsapp_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<WhatsAppImageDatabaseEntity>>
    
    @Query("SELECT * FROM whatsapp_images WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getActiveImages(): Flow<List<WhatsAppImageDatabaseEntity>>
    
    @Query("SELECT * FROM whatsapp_images WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getDeletedImages(): Flow<List<WhatsAppImageDatabaseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: WhatsAppImageDatabaseEntity): Long
    
    @Update
    suspend fun updateImage(image: WhatsAppImageDatabaseEntity)
    
    @Query("UPDATE whatsapp_images SET isDeleted = 1, deletedTimestamp = :timestamp WHERE originalFileName = :fileName AND isDeleted = 0")
    suspend fun markImageAsDeleted(fileName: String, timestamp: Long)
}