package dev.advik.messagelogger.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import dev.advik.messagelogger.data.entity.NotificationLog

@Dao
interface NotificationLogDao {
    
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationLog>>
    
    @Query("SELECT * FROM notification_logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationLog>>
    
    @Query("SELECT DISTINCT packageName, appName FROM notification_logs ORDER BY appName ASC")
    fun getDistinctApps(): Flow<List<NotificationLog>>
    
    @Query("SELECT COUNT(*) FROM notification_logs WHERE packageName = :packageName")
    suspend fun getNotificationCountByPackage(packageName: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationLog)
    
    @Query("DELETE FROM notification_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldNotifications(cutoffTime: Long)
}