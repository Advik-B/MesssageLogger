package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for enhanced message recovery features
 * Implements app.md specifications - ALL features are FREE
 */
class MessageRecoveryViewModel : ViewModel() {
    
    private val dataStore = SimpleDataStore.getInstance()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedApp = MutableStateFlow<String?>(null)
    val selectedApp: StateFlow<String?> = _selectedApp.asStateFlow()
    
    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()
    
    // FREE: Advanced search and filtering (was premium feature)
    val messages: StateFlow<List<MessageEntity>> = combine(
        dataStore.recoveredMessages,
        searchQuery,
        selectedApp
    ) { messageList, query, app ->
        messageList.filter { message ->
            val matchesQuery = query.isEmpty() || 
                message.messageContent.contains(query, ignoreCase = true) ||
                message.sender.contains(query, ignoreCase = true)
            
            val matchesApp = app == null || message.packageName == app
            
            matchesQuery && matchesApp
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectApp(packageName: String?) {
        _selectedApp.value = packageName
    }
    
    fun toggleSearchExpanded() {
        _isSearchExpanded.value = !_isSearchExpanded.value
    }
    
    // FREE: Export functionality (was premium feature)
    fun exportMessage(message: MessageEntity) {
        viewModelScope.launch {
            try {
                // Simplified export - in real app would use proper export service
                // Export formats: JSON, TXT, CSV, PDF, HTML (all FREE)
                val exportData = buildString {
                    appendLine("=== Recovered Message ===")
                    appendLine("App: ${message.appName}")
                    appendLine("From: ${message.sender}")
                    appendLine("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))}")
                    appendLine("Type: ${message.messageType}")
                    if (message.isDeleted) {
                        appendLine("Status: DELETED")
                        message.deletedTimestamp?.let {
                            appendLine("Deleted: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(it))}")
                        }
                    }
                    appendLine("Content: ${message.messageContent}")
                    if (message.chatIdentifier != null) {
                        appendLine("Chat: ${message.chatIdentifier}")
                    }
                }
                
                // TODO: Implement actual file export
                println("Export data: $exportData")
            } catch (e: Exception) {
                println("Export failed: ${e.message}")
            }
        }
    }
    
    // FREE: Share functionality
    fun shareMessage(message: MessageEntity) {
        viewModelScope.launch {
            try {
                val shareText = buildString {
                    appendLine("Recovered message from ${message.appName}:")
                    appendLine("From: ${message.sender}")
                    appendLine("${message.messageContent}")
                    if (message.isDeleted) {
                        appendLine("(This message was deleted)")
                    }
                }
                
                // TODO: Implement actual share functionality
                println("Share text: $shareText")
            } catch (e: Exception) {
                println("Share failed: ${e.message}")
            }
        }
    }
    
    // Statistics for dashboard (FREE feature)
    val messageStats = dataStore.recoveredMessages.map { messages ->
        MessageStats(
            totalRecovered = messages.size,
            deletedMessages = messages.count { it.isDeleted },
            appBreakdown = messages.groupBy { it.appName }.mapValues { it.value.size },
            todayCount = messages.count { message ->
                val today = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                message.timestamp > today
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MessageStats()
    )
}

data class MessageStats(
    val totalRecovered: Int = 0,
    val deletedMessages: Int = 0,
    val appBreakdown: Map<String, Int> = emptyMap(),
    val todayCount: Int = 0
)