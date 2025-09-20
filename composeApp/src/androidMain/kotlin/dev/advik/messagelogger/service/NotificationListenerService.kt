package dev.advik.messagelogger.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Service to listen to all notifications from all apps
 * This is a minimal implementation to satisfy AndroidManifest.xml requirements
 */
class NotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // TODO: Implement notification logging functionality
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // TODO: Handle notification removal
        super.onNotificationRemoved(sbn)
    }
}