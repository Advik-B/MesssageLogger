package dev.advik.messagelogger.data.database.dao

import androidx.room.*
import dev.advik.messagelogger.data.database.entity.NotificationDatabaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationDatabaseEntity>>
    
    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationDatabaseEntity>>
    
    @Query("SELECT DISTINCT packageName, appName FROM notifications ORDER BY appName")
    fun getDistinctApps(): Flow<Map<String, String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationDatabaseEntity): Long
    
    @Query("DELETE FROM notifications WHERE key = :key")
    suspend fun deleteNotificationByKey(key: String)
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}