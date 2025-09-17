package dev.advik.messagelogger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.advik.messagelogger.service.WhatsAppImageObserverService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Start WhatsApp Image Observer Service
        startService(Intent(this, WhatsAppImageObserverService::class.java))

        setContent {
            App()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Stop the service when the app is destroyed
        stopService(Intent(this, WhatsAppImageObserverService::class.java))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}