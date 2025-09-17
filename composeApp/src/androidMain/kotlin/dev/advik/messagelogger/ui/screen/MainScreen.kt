package dev.advik.messagelogger.ui.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.advik.messagelogger.ui.component.NotificationItem
import dev.advik.messagelogger.ui.component.AppFilterDropdown
import dev.advik.messagelogger.ui.component.PermissionHandler
import dev.advik.messagelogger.ui.viewmodel.NotificationViewModel
import dev.advik.messagelogger.data.database.MessageLoggerDatabase
import dev.advik.messagelogger.repository.MessageLoggerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { MessageLoggerDatabase.getDatabase(context) }
    val repository = remember { 
        MessageLoggerRepository(
            notificationDao = database.notificationLogDao(),
            whatsAppImageDao = database.whatsAppImageDao()
        )
    }
    val viewModel = remember { NotificationViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    
    var showFilterDropdown by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    
    // Check permissions
    PermissionHandler(
        onPermissionStatusChanged = { hasNotificationPermission = it }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message Logger") },
                actions = {
                    IconButton(onClick = { showFilterDropdown = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { navController.navigate("whatsapp_images") }) {
                        Icon(Icons.Default.Image, contentDescription = "WhatsApp Images")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!hasNotificationPermission) {
                ExtendedFloatingActionButton(
                    onClick = { openNotificationSettings(context) },
                    text = { Text("Enable Permissions") },
                    icon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter dropdown
            if (showFilterDropdown) {
                AppFilterDropdown(
                    apps = uiState.distinctApps,
                    selectedPackageName = uiState.selectedPackageName,
                    onAppSelected = { viewModel.selectApp(it) },
                    onDismiss = { showFilterDropdown = false }
                )
            }
            
            // Current filter display
            if (uiState.selectedPackageName != null) {
                val selectedApp = uiState.distinctApps.find { it.packageName == uiState.selectedPackageName }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filtered by: ${selectedApp?.appName ?: uiState.selectedPackageName}",
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.selectApp(null) }) {
                            Text("Clear")
                        }
                    }
                }
            }
            
            // Notifications list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications found")
                    }
                }
                else -> {
                    LazyColumn {
                        items(uiState.notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}