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
    
    // Add selectedApp filter state
    private val _selectedApp = MutableStateFlow<String?>(null)
    val selectedApp: StateFlow<String?> = _selectedApp.asStateFlow()
    
    // Get distinct apps for app selector dropdown
    val apps = repository.getAllMessages()
        .map { messages ->
            messages.map { it.appName to it.packageName }
                .distinct()
                .sortedBy { it.first }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Group messages by sender for chat-like view with app filtering
    val chatGroups: StateFlow<List<ChatGroup>> = combine(
        repository.getAllMessages(),
        _selectedApp
    ) { messages, selectedAppPackage ->
        val filteredMessages = if (selectedAppPackage != null) {
            messages.filter { it.packageName == selectedAppPackage }
        } else {
            messages
        }
        
        filteredMessages
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
    
    fun selectApp(packageName: String?) {
        _selectedChatGroup.value = null // Reset selected chat when app filter changes
        _selectedApp.value = packageName
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