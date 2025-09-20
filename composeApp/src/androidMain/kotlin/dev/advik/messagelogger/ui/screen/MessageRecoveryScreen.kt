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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.MessageType
import dev.advik.messagelogger.ui.viewmodel.MessageRecoveryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced message recovery screen implementing app.md features
 * ALL features are FREE - no premium restrictions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageRecoveryScreen(
    viewModel: MessageRecoveryViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedApp.collectAsStateWithLifecycle()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Export status feedback
        exportStatus?.let { status ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Search and filter section
        SearchAndFilterSection(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            selectedApp = selectedApp,
            onAppSelected = viewModel::setSelectedApp,
            isSearchExpanded = isSearchExpanded,
            onSearchExpandedChange = { viewModel.toggleSearchExpanded() }
        )
        
        // Messages list
        if (messages.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(messages) { message ->
                    MessageRecoveryItem(
                        message = message,
                        onExport = { viewModel.exportMessage(it) }, // FREE feature
                        onShare = { viewModel.shareMessage(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedApp: String?,
    onAppSelected: (String?) -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search messages...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { onSearchExpandedChange(!isSearchExpanded) }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand filters"
                        )
                    }
                }
            )
            
            // Advanced filters (FREE feature)
            if (isSearchExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // App filter
                    FilterChip(
                        selected = selectedApp != null,
                        onClick = { onAppSelected(if (selectedApp != null) null else "com.whatsapp") },
                        label = { Text(selectedApp ?: "All Apps") },
                        leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) }
                    )
                    
                    // Date filter (placeholder for now)
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Implement date filter */ },
                        label = { Text("Date Range") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageRecoveryItem(
    message: MessageEntity,
    onExport: (MessageEntity) -> Unit,
    onShare: (MessageEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with app and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MessageTypeIcon(messageType = message.messageType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.appName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sender
            Text(
                text = "From: ${message.sender}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Message content
            Text(
                text = message.messageContent,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Deletion indicator
            if (message.isDeleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Message was deleted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Action buttons (FREE features)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onShare(message) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                
                OutlinedButton(
                    onClick = { onExport(message) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun MessageTypeIcon(messageType: MessageType) {
    val icon = when (messageType) {
        MessageType.TEXT -> Icons.Default.Message
        MessageType.IMAGE -> Icons.Default.Image
        MessageType.VIDEO -> Icons.Default.VideoFile
        MessageType.AUDIO -> Icons.Default.AudioFile
        MessageType.VOICE_MESSAGE -> Icons.Default.Mic
        MessageType.DOCUMENT -> Icons.Default.Description
        MessageType.STICKER -> Icons.Default.EmojiEmotions
        MessageType.GIF -> Icons.Default.Gif
        MessageType.LOCATION -> Icons.Default.LocationOn
        MessageType.CONTACT_CARD -> Icons.Default.ContactPage
        MessageType.UNKNOWN -> Icons.Default.Help
    }
    
    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Message,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recovered messages yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Messages from supported apps will appear here when they're detected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}