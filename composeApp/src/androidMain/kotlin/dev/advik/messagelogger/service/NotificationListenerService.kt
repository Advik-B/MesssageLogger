package dev.advik.messagelogger.service

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import dev.advik.messagelogger.data.database.MessageLoggerDatabase
import dev.advik.messagelogger.data.entity.NotificationLog

class NotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "NotificationListener"
    }
    
    private lateinit var database: MessageLoggerDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        database = MessageLoggerDatabase.getDatabase(this)
        Log.d(TAG, "NotificationListenerService created")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        serviceScope.launch {
            try {
                val notification = sbn.notification
                val packageName = sbn.packageName
                val appName = getAppName(packageName)
                
                // Extract notification details
                val title = notification.extras.getCharSequence("android.title")?.toString()
                val text = notification.extras.getCharSequence("android.text")?.toString()
                val timestamp = sbn.postTime
                
                // Save app icon
                val iconPath = saveAppIcon(packageName)
                
                // Create notification log entry
                val notificationLog = NotificationLog(
                    appName = appName,
                    packageName = packageName,
                    appIconPath = iconPath,
                    title = title,
                    text = text,
                    timestamp = timestamp,
                    category = notification.category,
                    priority = notification.priority
                )
                
                // Insert into database
                database.notificationLogDao().insertNotification(notificationLog)
                Log.d(TAG, "Notification logged: $appName - $title")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Optional: Handle notification removal
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // fallback to package name
        }
    }
    
    private fun saveAppIcon(packageName: String): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val drawable = packageManager.getApplicationIcon(appInfo)
            
            val iconsDir = File(filesDir, "app_icons")
            if (!iconsDir.exists()) {
                iconsDir.mkdirs()
            }
            
            val iconFile = File(iconsDir, "$packageName.png")
            if (!iconFile.exists()) {
                val bitmap = drawableToBitmap(drawable)
                FileOutputStream(iconFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            
            iconFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving app icon for $packageName", e)
            null
        }
    }
    
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}