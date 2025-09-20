package dev.advik.messagelogger.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import dev.advik.messagelogger.data.database.dao.MessageDao
import dev.advik.messagelogger.data.database.dao.NotificationDao
import dev.advik.messagelogger.data.database.dao.WhatsAppImageDao
import dev.advik.messagelogger.data.database.entity.MessageDatabaseEntity
import dev.advik.messagelogger.data.database.entity.NotificationDatabaseEntity
import dev.advik.messagelogger.data.database.entity.WhatsAppImageDatabaseEntity

@Database(
    entities = [
        MessageDatabaseEntity::class,
        NotificationDatabaseEntity::class,
        WhatsAppImageDatabaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MessageLoggerDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
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
                )
                    .fallbackToDestructiveMigration() // For development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}