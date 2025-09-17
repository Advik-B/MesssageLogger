package dev.advik.messagelogger.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dev.advik.messagelogger.data.entity.NotificationLog
import dev.advik.messagelogger.data.entity.WhatsAppImage
import dev.advik.messagelogger.data.dao.NotificationLogDao
import dev.advik.messagelogger.data.dao.WhatsAppImageDao

@Database(
    entities = [NotificationLog::class, WhatsAppImage::class],
    version = 1,
    exportSchema = false
)
abstract class MessageLoggerDatabase : RoomDatabase() {
    
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun whatsAppImageDao(): WhatsAppImageDao
    
    companion object {
        @Volatile
        private var INSTANCE: MessageLoggerDatabase? = null
        
        fun getDatabase(context: Context): MessageLoggerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageLoggerDatabase::class.java,
                    "message_logger_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}