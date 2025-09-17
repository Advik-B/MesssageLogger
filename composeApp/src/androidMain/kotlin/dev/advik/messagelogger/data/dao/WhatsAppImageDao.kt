package dev.advik.messagelogger.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import dev.advik.messagelogger.data.entity.WhatsAppImage

@Dao
interface WhatsAppImageDao {
    
    @Query("SELECT * FROM whatsapp_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<WhatsAppImage>>
    
    @Query("SELECT * FROM whatsapp_images WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getActiveImages(): Flow<List<WhatsAppImage>>
    
    @Query("SELECT * FROM whatsapp_images WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getDeletedImages(): Flow<List<WhatsAppImage>>
    
    @Query("SELECT * FROM whatsapp_images WHERE fileName = :fileName LIMIT 1")
    suspend fun getImageByFileName(fileName: String): WhatsAppImage?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: WhatsAppImage)
    
    @Update
    suspend fun updateImage(image: WhatsAppImage)
    
    @Delete
    suspend fun deleteImage(image: WhatsAppImage)
    
    @Query("UPDATE whatsapp_images SET isDeleted = 1, deletedTimestamp = :deletedTime WHERE fileName = :fileName")
    suspend fun markImageAsDeleted(fileName: String, deletedTime: Long)
}