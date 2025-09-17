package dev.advik.messagelogger

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.advik.messagelogger.ui.screen.MainScreen
import dev.advik.messagelogger.ui.screen.WhatsAppImagesScreen

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("main") {
                MainScreen(navController = navController)
            }
            composable("whatsapp_images") {
                WhatsAppImagesScreen(navController = navController)
            }
        }
    }
}