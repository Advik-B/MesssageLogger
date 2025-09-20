package dev.advik.messagelogger.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service to monitor WhatsApp images folder for changes
 * This is a minimal implementation to satisfy AndroidManifest.xml requirements
 */
class WhatsAppImageObserverService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        // This service is not intended to be bound
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Implement WhatsApp image monitoring functionality
        return START_STICKY
    }

    override fun onDestroy() {
        // TODO: Clean up resources
        super.onDestroy()
    }
}