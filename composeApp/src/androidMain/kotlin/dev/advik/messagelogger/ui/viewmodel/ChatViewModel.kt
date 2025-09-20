package dev.advik.messagelogger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.ui.screen.ChatGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MessageLoggerRepository.getInstance(application)
    
    private val _selectedChatGroup = MutableStateFlow<ChatGroup?>(null)
    val selectedChatGroup: StateFlow<ChatGroup?> = _selectedChatGroup.asStateFlow()
    
    // Group messages by sender for chat-like view
    val chatGroups: StateFlow<List<ChatGroup>> = repository.getAllMessages()
        .map { messages ->
            messages
                .groupBy { "${it.sender}|${it.packageName}" } // Group by sender and app
                .map { (key, messageList) ->
                    val (sender, packageName) = key.split("|")
                    val appName = messageList.firstOrNull()?.appName ?: "Unknown"
                    ChatGroup(
                        displayName = sender,
                        appName = appName,
                        packageName = packageName,
                        messages = messageList.sortedBy { it.timestamp }
                    )
                }
                .sortedBy { it.displayName.lowercase() } // Sort alphabetically by sender name
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun selectChatGroup(chatGroup: ChatGroup) {
        _selectedChatGroup.value = chatGroup
    }
    
    // Auto-select first chat group when available
    init {
        viewModelScope.launch {
            chatGroups.collect { groups ->
                if (groups.isNotEmpty() && _selectedChatGroup.value == null) {
                    _selectedChatGroup.value = groups.first()
                }
            }
        }
    }
}