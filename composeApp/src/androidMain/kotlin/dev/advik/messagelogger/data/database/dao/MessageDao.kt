package dev.advik.messagelogger.data.database.dao

import androidx.room.*
import dev.advik.messagelogger.data.database.entity.MessageDatabaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageDatabaseEntity>>
    
    @Query("SELECT * FROM messages WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getMessagesByPackage(packageName: String): Flow<List<MessageDatabaseEntity>>
    
    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY timestamp DESC")
    fun getMessagesBySender(sender: String): Flow<List<MessageDatabaseEntity>>
    
    @Query("SELECT DISTINCT sender FROM messages WHERE packageName = :packageName ORDER BY sender")
    fun getSendersByPackage(packageName: String): Flow<List<String>>
    
    @Query("SELECT DISTINCT packageName FROM messages ORDER BY packageName")
    fun getAllPackages(): Flow<List<String>>
    
    @Query("""
        SELECT * FROM messages 
        WHERE messageContent LIKE '%' || :searchQuery || '%' 
        OR sender LIKE '%' || :searchQuery || '%'
        ORDER BY timestamp DESC
    """)
    fun searchMessages(searchQuery: String): Flow<List<MessageDatabaseEntity>>
    
    @Query("SELECT * FROM messages WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getDeletedMessages(): Flow<List<MessageDatabaseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageDatabaseEntity): Long
    
    @Update
    suspend fun updateMessage(message: MessageDatabaseEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageDatabaseEntity)
    
    @Query("DELETE FROM messages WHERE timestamp < :cutoffTime")
    suspend fun deleteMessagesOlderThan(cutoffTime: Long): Int
}