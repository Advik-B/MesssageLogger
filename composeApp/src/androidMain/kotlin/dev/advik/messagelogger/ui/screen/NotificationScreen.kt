package dev.advik.messagelogger.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.advik.messagelogger.ui.component.NotificationItem
import dev.advik.messagelogger.ui.viewmodel.NotificationViewModel
import dev.advik.messagelogger.ui.viewmodel.AppWithIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val selectedPackage by viewModel.selectedPackage.collectAsStateWithLifecycle()
    val notificationCount by viewModel.notificationCount.collectAsStateWithLifecycle()

    var showFilterDropdown by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with stats and actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedPackage != null) {
                                "${notifications.size} notifications from ${apps.find { it.packageName == selectedPackage }?.appName ?: selectedPackage}"
                            } else {
                                "$notificationCount total notifications"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        // Filter button
                        IconButton(
                            onClick = { showFilterDropdown = !showFilterDropdown }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }

                        // Clear all button
                        IconButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear All")
                        }
                    }
                }

                // Filter dropdown
                if (showFilterDropdown) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        item {
                            DropdownMenuItem(
                                text = { Text("All Apps") },
                                onClick = {
                                    viewModel.selectPackage(null)
                                    showFilterDropdown = false
                                },
                                leadingIcon = {
                                    if (selectedPackage == null) {
                                        Icon(Icons.Default.FilterList, contentDescription = null)
                                    }
                                }
                            )
                        }
                        items(apps) { app ->
                            DropdownMenuItem(
                                text = { Text(app.appName) },
                                onClick = {
                                    viewModel.selectPackage(app.packageName)
                                    showFilterDropdown = false
                                },
                                leadingIcon = {
                                    if (app.icon != null) {
                                        try {
                                            val bitmap = app.icon.toBitmap(48, 48)
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } catch (e: Exception) {
                                            Icon(Icons.Default.Apps, contentDescription = null)
                                        }
                                    } else {
                                        Icon(Icons.Default.Apps, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Notifications list
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedPackage != null) {
                            "No notifications from this app"
                        } else {
                            "No notifications yet\n\nMake sure notification access is enabled in Settings"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(notifications) { notification ->
                    NotificationItem(notification = notification)
                }
            }
        }
    }

    // Clear all dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Notifications") },
            text = { Text("Are you sure you want to delete all logged notifications? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllNotifications()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}