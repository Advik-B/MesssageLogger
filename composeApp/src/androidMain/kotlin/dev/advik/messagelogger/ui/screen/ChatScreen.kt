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
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat-like view showing messages grouped by sender/contact
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val chatGroups by viewModel.chatGroups.collectAsStateWithLifecycle()
    val selectedChatGroup by viewModel.selectedChatGroup.collectAsStateWithLifecycle()
    
    Row(modifier = modifier.fillMaxSize()) {
        // Chat list sidebar
        ChatListPanel(
            chatGroups = chatGroups,
            selectedChatGroup = selectedChatGroup,
            onChatGroupSelected = viewModel::selectChatGroup,
            modifier = Modifier.weight(1f)
        )
        
        // Chat messages panel
        if (selectedChatGroup != null) {
            ChatMessagesPanel(
                chatGroup = selectedChatGroup!!,
                modifier = Modifier.weight(2f)
            )
        } else {
            EmptySelectionPanel(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun ChatListPanel(
    chatGroups: List<ChatGroup>,
    selectedChatGroup: ChatGroup?,
    onChatGroupSelected: (ChatGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        tonalElevation = 1.dp
    ) {
        Column {
            // Header
            TopAppBar(
                title = { Text("Conversations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            
            // Chat list
            LazyColumn {
                items(chatGroups) { chatGroup ->
                    ChatGroupItem(
                        chatGroup = chatGroup,
                        isSelected = chatGroup == selectedChatGroup,
                        onClick = { onChatGroupSelected(chatGroup) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatGroupItem(
    chatGroup: ChatGroup,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatGroup.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${chatGroup.messageCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (chatGroup.appName) {
                        "WhatsApp" -> Icons.Default.Message
                        "Telegram" -> Icons.Default.Chat
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = chatGroup.appName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (chatGroup.lastMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chatGroup.lastMessage!!.messageContent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun ChatMessagesPanel(
    chatGroup: ChatGroup,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        // Header with chat info
        TopAppBar(
            title = { 
                Column {
                    Text(chatGroup.displayName)
                    Text(
                        text = "${chatGroup.messageCount} messages • ${chatGroup.appName}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true, // Latest messages at bottom like a real chat
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatGroup.messages.reversed()) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isDeleted) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Message header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Message content
            Text(
                text = message.messageContent,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Message type and deletion indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (message.messageType.name) {
                            "IMAGE" -> Icons.Default.Image
                            "VIDEO" -> Icons.Default.VideoFile
                            "AUDIO" -> Icons.Default.AudioFile
                            "DOCUMENT" -> Icons.Default.Description
                            else -> Icons.Default.Message
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = message.messageType.name.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (message.isDeleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "deleted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySelectionPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select a conversation to view messages",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes for chat grouping
data class ChatGroup(
    val displayName: String, // sender or group name
    val appName: String,
    val packageName: String,
    val messages: List<MessageEntity>,
    val messageCount: Int = messages.size,
    val lastMessage: MessageEntity? = messages.maxByOrNull { it.timestamp }
)