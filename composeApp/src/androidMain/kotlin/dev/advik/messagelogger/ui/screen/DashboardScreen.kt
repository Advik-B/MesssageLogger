package dev.advik.messagelogger.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.advik.messagelogger.ui.viewmodel.MessageRecoveryViewModel

/**
 * Dashboard screen showing FREE premium features and statistics
 * All features available without restrictions per app.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MessageRecoveryViewModel,
    modifier: Modifier = Modifier
) {
    val messageStats by viewModel.messageStats.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Message Recovery Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Statistics Cards
        item {
            StatisticsSection(messageStats)
        }
        
        // App Breakdown
        if (messageStats.appBreakdown.isNotEmpty()) {
            item {
                AppBreakdownSection(messageStats.appBreakdown)
            }
        }
        
        // Recent Activity Summary
        item {
            RecentActivitySection(messageStats)
        }
        
        // Quick Actions
        item {
            QuickActionsSection(
                onExportAll = { viewModel.exportMessage(/* dummy message for demo */ 
                    dev.advik.messagelogger.data.entity.MessageEntity(
                        id = 0,
                        notificationId = 0,
                        packageName = "",
                        appName = "Export All",
                        sender = "System",
                        messageContent = "Export all data",
                        timestamp = System.currentTimeMillis(),
                        messageType = dev.advik.messagelogger.data.entity.MessageType.TEXT,
                        chatIdentifier = null,
                        key = "export_all"
                    )
                )},
                onClearOld = { /* TODO: Implement clear old messages */ }
            )
        }
    }
}

@Composable
private fun PremiumStatusBanner() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FREE Premium Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "All premium features are FREE! No limits, no subscriptions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatisticsSection(stats: dev.advik.messagelogger.ui.viewmodel.MessageStats) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recovery Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatCard(
                        icon = Icons.Default.Message,
                        title = "Total Recovered",
                        value = stats.totalRecovered.toString(),
                        subtitle = "Messages recovered"
                    )
                }
                item {
                    StatCard(
                        icon = Icons.Default.Delete,
                        title = "Deleted Messages",
                        value = stats.deletedMessages.toString(),
                        subtitle = "Successfully preserved"
                    )
                }
                item {
                    StatCard(
                        icon = Icons.Default.Today,
                        title = "Today",
                        value = stats.todayCount.toString(),
                        subtitle = "Messages recovered"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppBreakdownSection(appBreakdown: Map<String, Int>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Apps, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            
            appBreakdown.forEach { (appName, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$count messages",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivitySection(stats: dev.advik.messagelogger.ui.viewmodel.MessageStats) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Timeline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (stats.todayCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recovered ${stats.todayCount} messages today",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No messages recovered today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onExportAll: () -> Unit,
    onClearOld: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Data")
                }
                
                OutlinedButton(
                    onClick = onClearOld,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Storage Cleanup")
                }
            }
        }
    }
}