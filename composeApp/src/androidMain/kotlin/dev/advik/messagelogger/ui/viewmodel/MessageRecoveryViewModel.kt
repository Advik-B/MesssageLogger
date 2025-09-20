package dev.advik.messagelogger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.ExportFormat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for enhanced message recovery features
 * All features are available without restrictions
 */
class MessageRecoveryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MessageLoggerRepository.getInstance(application)
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedApp = MutableStateFlow<String?>(null)
    val selectedApp: StateFlow<String?> = _selectedApp.asStateFlow()
    
    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()
    
    // Advanced search and filtering
    val messages: StateFlow<List<MessageEntity>> = combine(
        repository.getAllMessages(),
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
    
    // Get available apps for filtering
    val availableApps: StateFlow<List<String>> = repository.getAllPackages()
        .stateIn(
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
    
    // Export functionality - actually functional now
    fun exportMessages(format: ExportFormat) {
        viewModelScope.launch {
            try {
                when (format) {
                    ExportFormat.JSON -> exportAsJson()
                    ExportFormat.CSV -> exportAsCsv()
                    ExportFormat.TXT -> exportAsTxt()
                    ExportFormat.HTML -> exportAsHtml()
                    ExportFormat.PDF -> exportAsPdf()
                }
            } catch (e: Exception) {
                // Handle export error - could emit to UI state
            }
        }
    }
    
    private suspend fun exportAsJson() {
        val messages = repository.getAllMessages().first()
        val json = buildString {
            append("[\n")
            messages.forEachIndexed { index, message ->
                append("  {\n")
                append("    \"timestamp\": \"${formatTimestamp(message.timestamp)}\",\n")
                append("    \"sender\": \"${escapeJson(message.sender)}\",\n")
                append("    \"content\": \"${escapeJson(message.messageContent)}\",\n")
                append("    \"app\": \"${escapeJson(message.appName)}\",\n")
                append("    \"type\": \"${message.messageType}\",\n")
                append("    \"deleted\": ${message.isDeleted}\n")
                append("  }")
                if (index < messages.size - 1) append(",")
                append("\n")
            }
            append("]")
        }
        saveToFile("messages_export.json", json)
    }
    
    private suspend fun exportAsCsv() {
        val messages = repository.getAllMessages().first()
        val csv = buildString {
            append("Timestamp,Sender,Content,App,Type,Deleted\n")
            messages.forEach { message ->
                append("\"${formatTimestamp(message.timestamp)}\",")
                append("\"${escapeCsv(message.sender)}\",")
                append("\"${escapeCsv(message.messageContent)}\",")
                append("\"${escapeCsv(message.appName)}\",")
                append("\"${message.messageType}\",")
                append("\"${message.isDeleted}\"\n")
            }
        }
        saveToFile("messages_export.csv", csv)
    }
    
    private suspend fun exportAsTxt() {
        val messages = repository.getAllMessages().first()
        val txt = buildString {
            append("Message Export Report\n")
            append("Generated: ${formatTimestamp(System.currentTimeMillis())}\n")
            append("Total Messages: ${messages.size}\n\n")
            append("=" * 50 + "\n\n")
            
            messages.forEach { message ->
                append("[${formatTimestamp(message.timestamp)}] ")
                append("${message.appName} - ${message.sender}\n")
                append("${message.messageContent}\n")
                if (message.isDeleted) append("*** DELETED MESSAGE ***\n")
                append("-" * 30 + "\n\n")
            }
        }
        saveToFile("messages_export.txt", txt)
    }
    
    private suspend fun exportAsHtml() {
        val messages = repository.getAllMessages().first()
        val html = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Message Export</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .message { border: 1px solid #ddd; margin: 10px 0; padding: 10px; border-radius: 5px; }
                        .deleted { background-color: #ffe6e6; }
                        .timestamp { color: #666; font-size: 0.9em; }
                        .sender { font-weight: bold; color: #333; }
                        .app { color: #007bff; font-size: 0.9em; }
                    </style>
                </head>
                <body>
                    <h1>Message Export Report</h1>
                    <p>Generated: ${formatTimestamp(System.currentTimeMillis())}</p>
                    <p>Total Messages: ${messages.size}</p>
                    <hr>
            """.trimIndent())
            
            messages.forEach { message ->
                append("""
                    <div class="message${if (message.isDeleted) " deleted" else ""}">
                        <div class="timestamp">${formatTimestamp(message.timestamp)}</div>
                        <div class="app">${message.appName}</div>
                        <div class="sender">${message.sender}</div>
                        <div class="content">${escapeHtml(message.messageContent)}</div>
                        ${if (message.isDeleted) "<div style='color: red;'>*** DELETED MESSAGE ***</div>" else ""}
                    </div>
                """.trimIndent())
            }
            
            append("""
                </body>
                </html>
            """.trimIndent())
        }
        saveToFile("messages_export.html", html)
    }
    
    private suspend fun exportAsPdf() {
        // For now, create a text-based PDF representation
        // In a real implementation, you'd use a PDF library
        exportAsTxt() // Fallback to text for now
    }
    
    private fun saveToFile(filename: String, content: String) {
        val downloadsDir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
        downloadsDir.mkdirs()
        val file = File(downloadsDir, filename)
        file.writeText(content)
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
    }
    
    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }
    
    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
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
    
    // Export functionality
    fun exportMessage(format: ExportFormat) {
        viewModelScope.launch {
            try {
                val currentMessages = messages.value
                val file = when (format) {
                    ExportFormat.JSON -> exportToJson(currentMessages)
                    ExportFormat.CSV -> exportToCsv(currentMessages)
                    ExportFormat.TXT -> exportToTxt(currentMessages)
                    ExportFormat.HTML -> exportToHtml(currentMessages)
                    ExportFormat.PDF -> exportToTxt(currentMessages) // Fallback to text
                }
                // Handle success (could show toast or notification)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun shareMessage(message: MessageEntity) {
        // Implementation for sharing individual messages
    }
    
    fun setSelectedApp(app: String?) {
        _selectedApp.value = app
    }
    
    fun toggleSearchExpansion() {
        _isSearchExpanded.value = !_isSearchExpanded.value
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private suspend fun exportToJson(messages: List<MessageEntity>): File {
        val exportDir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        val file = File(exportDir, "messages_${System.currentTimeMillis()}.json")
        file.writeText(messages.toString()) // Simple implementation
        return file
    }
    
    private suspend fun exportToCsv(messages: List<MessageEntity>): File {
        val exportDir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        val file = File(exportDir, "messages_${System.currentTimeMillis()}.csv")
        val csv = StringBuilder("Sender,Content,Timestamp,Deleted\n")
        messages.forEach { message ->
            csv.append("${message.sender},${message.messageContent},${message.timestamp},${message.isDeleted}\n")
        }
        file.writeText(csv.toString())
        return file
    }
    
    private suspend fun exportToTxt(messages: List<MessageEntity>): File {
        val exportDir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        val file = File(exportDir, "messages_${System.currentTimeMillis()}.txt")
        val txt = StringBuilder()
        messages.forEach { message ->
            txt.append("From: ${message.sender}\n")
            txt.append("Content: ${message.messageContent}\n")
            txt.append("Time: ${Date(message.timestamp)}\n")
            txt.append("Deleted: ${message.isDeleted}\n")
            txt.append("---\n")
        }
        file.writeText(txt.toString())
        return file
    }
    
    private suspend fun exportToHtml(messages: List<MessageEntity>): File {
        val exportDir = File(getApplication<Application>().getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        val file = File(exportDir, "messages_${System.currentTimeMillis()}.html")
        val html = buildString {
            append("<!DOCTYPE html><html><body>")
            append("<h1>Exported Messages</h1>")
            messages.forEach { message ->
                append("<div style='border:1px solid #ccc; margin:10px; padding:10px;'>")
                append("<strong>From:</strong> ${message.sender}<br>")
                append("<strong>Content:</strong> ${message.messageContent}<br>")
                append("<strong>Time:</strong> ${Date(message.timestamp)}<br>")
                append("<strong>Deleted:</strong> ${message.isDeleted}")
                append("</div>")
            }
            append("</body></html>")
        }
        file.writeText(html)
        return file
    }
}

data class MessageStats(
    val totalRecovered: Int = 0,
    val deletedMessages: Int = 0,
    val appBreakdown: Map<String, Int> = emptyMap(),
    val todayCount: Int = 0
)