package dev.advik.messagelogger.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import dev.advik.messagelogger.data.SupportedPlatforms
import dev.advik.messagelogger.ui.viewmodel.SettingsViewModel

/**
 * Settings screen implementing app.md specifications
 * ALL premium features are FREE - no restrictions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val availableApps = SupportedPlatforms.PRIMARY_PLATFORMS
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Monitored Apps Section (FREE - was premium multi-app feature)
        item {
            MonitoredAppsSection(
                monitoredApps = settings.monitoredApps,
                availableApps = availableApps,
                onAppToggled = viewModel::toggleAppMonitoring
            )
        }
        
        // Storage Management (FREE - unlimited storage)
        item {
            StorageSection(
                autoDownloadMedia = settings.autoDownloadMedia,
                onAutoDownloadToggled = viewModel::toggleAutoDownload,
                retentionDays = settings.retentionDays,
                onRetentionChanged = viewModel::updateRetentionDays
            )
        }
        
        // Privacy & Security (FREE features)
        item {
            PrivacySecuritySection(
                enableAppLock = settings.enableAppLock,
                onAppLockToggled = viewModel::toggleAppLock,
                biometricAuth = settings.biometricAuth,
                onBiometricToggled = viewModel::toggleBiometricAuth,
                enableEncryption = settings.enableEncryption,
                onEncryptionToggled = viewModel::toggleEncryption
            )
        }
        
        // Export Settings (FREE - was premium feature)
        item {
            ExportSection(
                exportFormats = settings.exportFormats,
                onFormatToggled = viewModel::toggleExportFormat
            )
        }
        
        // Keyword Filters (FREE - was premium feature)
        item {
            KeywordFiltersSection(
                keywordFilters = settings.keywordFilters,
                onAddKeyword = viewModel::addKeywordFilter,
                onRemoveKeyword = viewModel::removeKeywordFilter
            )
        }
    }
}

@Composable
private fun MonitoredAppsSection(
    monitoredApps: Set<String>,
    availableApps: List<SupportedPlatforms.PlatformConfig>,
    onAppToggled: (String) -> Unit
) {
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
                    text = "Monitored Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Monitor all supported messaging apps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            availableApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = app.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = monitoredApps.contains(app.packageName),
                        onCheckedChange = { onAppToggled(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSection(
    autoDownloadMedia: Boolean,
    onAutoDownloadToggled: () -> Unit,
    retentionDays: Int,
    onRetentionChanged: (Int) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Storage Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto-download media (FREE feature)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Auto-download Media")
                    Text(
                        "Automatically download images, videos, and files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoDownloadMedia,
                    onCheckedChange = { onAutoDownloadToggled() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Retention settings (FREE - unlimited retention available)
            Column {
                Text(
                    text = "Message Retention",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (retentionDays == -1) "Unlimited retention" 
                          else "Keep messages for $retentionDays days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = retentionDays == -1,
                        onClick = { onRetentionChanged(-1) },
                        label = { Text("Unlimited") }
                    )
                    FilterChip(
                        selected = retentionDays == 30,
                        onClick = { onRetentionChanged(30) },
                        label = { Text("30 days") }
                    )
                    FilterChip(
                        selected = retentionDays == 7,
                        onClick = { onRetentionChanged(7) },
                        label = { Text("7 days") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacySecuritySection(
    enableAppLock: Boolean,
    onAppLockToggled: () -> Unit,
    biometricAuth: Boolean,
    onBiometricToggled: () -> Unit,
    enableEncryption: Boolean,
    onEncryptionToggled: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Lock (FREE feature)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("App Lock")
                    Text(
                        "Protect app with PIN/biometric authentication",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableAppLock,
                    onCheckedChange = { onAppLockToggled() }
                )
            }
            
            if (enableAppLock) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Biometric Authentication")
                        Text(
                            "Use fingerprint/face unlock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricAuth,
                        onCheckedChange = { onBiometricToggled() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Encryption (FREE feature)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Database Encryption")
                    Text(
                        "Encrypt stored messages and media",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableEncryption,
                    onCheckedChange = { onEncryptionToggled() }
                )
            }
        }
    }
}

@Composable
private fun ExportSection(
    exportFormats: Set<dev.advik.messagelogger.data.entity.ExportFormat>,
    onFormatToggled: (dev.advik.messagelogger.data.entity.ExportFormat) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Export in multiple formats",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Available Export Formats:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            dev.advik.messagelogger.data.entity.ExportFormat.values().forEach { format ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(format.name)
                    Switch(
                        checked = exportFormats.contains(format),
                        onCheckedChange = { onFormatToggled(format) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeywordFiltersSection(
    keywordFilters: Set<String>,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    var newKeyword by remember { mutableStateOf("") }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keyword Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Filter messages by keywords",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add new keyword
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text("Add keyword filter") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newKeyword.isNotBlank()) {
                            onAddKeyword(newKeyword)
                            newKeyword = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }
            
            // Existing keywords
            if (keywordFilters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                keywordFilters.forEach { keyword ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(keyword)
                        IconButton(onClick = { onRemoveKeyword(keyword) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}