package dev.advik.messagelogger.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.advik.messagelogger.R
import dev.advik.messagelogger.MainActivity

/**
 * Service for sending notifications about detected deleted media
 */
object DeletedMediaNotificationService {
    
    private const val CHANNEL_ID = "deleted_media_channel"
    private const val CHANNEL_NAME = "Deleted Media Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications when deleted media is detected"
    private const val NOTIFICATION_ID = 1001
    
    fun initialize(context: Context) {
        createNotificationChannel(context)
    }
    
    fun sendDeletedMediaNotification(
        context: Context,
        fileName: String,
        mediaType: String = "Image",
        appName: String = "WhatsApp"
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Using system icon for now
            .setContentTitle("$mediaType Deleted - Backup Available")
            .setContentText("$appName $mediaType '$fileName' was deleted but we have a backup!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("The $mediaType '$fileName' was deleted from $appName, but don't worry - we automatically saved a backup copy for you. Tap to view your recovered files."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + fileName.hashCode(), notification)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}