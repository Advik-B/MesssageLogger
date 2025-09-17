package dev.advik.messagelogger.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.advik.messagelogger.data.entity.NotificationLog

@Composable
fun AppFilterDropdown(
    apps: List<NotificationLog>,
    selectedPackageName: String?,
    onAppSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Filter by App",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    // "All Apps" option
                    item {
                        FilterItem(
                            appName = "All Apps",
                            packageName = null,
                            isSelected = selectedPackageName == null,
                            onClick = {
                                onAppSelected(null)
                                onDismiss()
                            }
                        )
                    }
                    
                    // Individual apps
                    items(apps) { app ->
                        FilterItem(
                            appName = app.appName,
                            packageName = app.packageName,
                            isSelected = selectedPackageName == app.packageName,
                            onClick = {
                                onAppSelected(app.packageName)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterItem(
    appName: String,
    packageName: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (packageName != null) {
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}