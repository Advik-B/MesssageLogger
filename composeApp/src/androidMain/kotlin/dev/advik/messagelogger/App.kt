package dev.advik.messagelogger

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import dev.advik.messagelogger.service.WhatsAppImageObserverService
import dev.advik.messagelogger.ui.screen.NotificationScreen
import dev.advik.messagelogger.ui.screen.PermissionScreen
import dev.advik.messagelogger.ui.screen.WhatsAppImageScreen
import dev.advik.messagelogger.ui.screen.MessageRecoveryScreen
import dev.advik.messagelogger.ui.screen.ChatScreen
import dev.advik.messagelogger.ui.screen.DashboardScreen
import dev.advik.messagelogger.ui.screen.SettingsScreen
import dev.advik.messagelogger.ui.viewmodel.NotificationViewModel
import dev.advik.messagelogger.ui.viewmodel.WhatsAppImageViewModel
import dev.advik.messagelogger.ui.viewmodel.MessageRecoveryViewModel
import dev.advik.messagelogger.ui.viewmodel.ChatViewModel
import dev.advik.messagelogger.ui.viewmodel.SettingsViewModel
import dev.advik.messagelogger.util.DemoData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var showPermissionScreen by remember { mutableStateOf(true) }

    MaterialTheme {
        if (showPermissionScreen) {
            PermissionScreen(
                onPermissionsGranted = {
                    showPermissionScreen = false
                    // Add demo data for testing
                    DemoData.addSampleData()
                    // Start WhatsApp monitoring service
                    val intent = Intent(context, WhatsAppImageObserverService::class.java)
                    context.startService(intent)
                }
            )
        } else {
            MainNavigation(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigation(
    navController: androidx.navigation.NavHostController
) {
    var currentRoute by remember { mutableStateOf("dashboard") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            "dashboard" -> "Dashboard"
                            "chat" -> "Conversations"
                            "images" -> "WhatsApp Images"
                            "settings" -> "Settings"
                            else -> "Message Logger"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        currentRoute = "dashboard"
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text("Chat") },
                    selected = currentRoute == "chat",
                    onClick = {
                        currentRoute = "chat"
                        navController.navigate("chat") {
                            popUpTo("chat") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Image, contentDescription = null) },
                    label = { Text("Images") },
                    selected = currentRoute == "images",
                    onClick = {
                        currentRoute = "images"
                        navController.navigate("images") {
                            popUpTo("images") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        currentRoute = "settings"
                        navController.navigate("settings") {
                            popUpTo("settings") { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                val viewModel = viewModel<MessageRecoveryViewModel> {
                    MessageRecoveryViewModel()
                }
                DashboardScreen(viewModel = viewModel)
            }
            
            composable("chat") {
                val viewModel = viewModel<ChatViewModel> {
                    ChatViewModel(application = LocalContext.current.applicationContext as android.app.Application)
                }
                ChatScreen(viewModel = viewModel)
            }
            
            composable("images") {
                val viewModel = viewModel<WhatsAppImageViewModel> {
                    WhatsAppImageViewModel()
                }
                WhatsAppImageScreen(viewModel = viewModel)
            }
            
            composable("settings") {
                val viewModel = viewModel<SettingsViewModel> {
                    SettingsViewModel()
                }
                SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Update current route when navigation changes
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "notifications"
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}