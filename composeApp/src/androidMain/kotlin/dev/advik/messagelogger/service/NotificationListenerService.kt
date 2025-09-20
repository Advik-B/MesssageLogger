package dev.advik.messagelogger.service

import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Service to listen to all notifications from all apps and log them to database
 */
class NotificationListenerService : NotificationListenerService() {

    private val dataStore = SimpleDataStore.getInstance()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    logNotification(statusBarNotification)
                } catch (e: Exception) {
                    Log.e(TAG, "Error logging notification", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    dataStore.removeNotificationByKey(statusBarNotification.key)
                    Log.d(TAG, "Removed notification: ${statusBarNotification.key}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing notification", e)
                }
            }
        }
    }

    private suspend fun logNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        
        // Skip system notifications and our own notifications
        if (packageName == this.packageName || isSystemNotification(packageName)) {
            return
        }

        val appName = getAppName(packageName)
        val title = getNotificationTitle(notification)
        val text = getNotificationText(notification)
        val timestamp = sbn.postTime
        val key = sbn.key

        // Save app icon
        val iconPath = saveAppIcon(packageName)

        val notificationEntity = NotificationEntity(
            appName = appName,
            packageName = packageName,
            appIconPath = iconPath,
            title = title,
            text = text,
            timestamp = timestamp,
            key = key
        )

        dataStore.addNotification(notificationEntity)
        Log.d(TAG, "Logged notification from $appName: $title")
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name
        }
    }

    private fun getNotificationTitle(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() 
            ?: "No Title"
    }

    private fun getNotificationText(notification: Notification): String {
        return notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() 
            ?: "No Text"
    }

    private fun saveAppIcon(packageName: String): String? {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(appInfo)
            
            val iconsDir = File(filesDir, "app_icons")
            if (!iconsDir.exists()) {
                iconsDir.mkdirs()
            }
            
            val iconFile = File(iconsDir, "$packageName.png")
            if (!iconFile.exists()) {
                val bitmap = drawableToBitmap(icon)
                FileOutputStream(iconFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
            }
            
            iconFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving app icon for $packageName", e)
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isSystemNotification(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}