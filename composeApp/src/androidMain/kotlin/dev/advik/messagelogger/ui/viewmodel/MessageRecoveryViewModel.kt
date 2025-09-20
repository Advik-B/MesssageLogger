package dev.advik.messagelogger.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.data.entity.MessageEntity
import dev.advik.messagelogger.data.entity.ExportFormat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    
    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()
    
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
    
    // Statistics for dashboard
    val messageStats: StateFlow<MessageStats> = messages.map { messageList ->
        MessageStats(
            totalRecovered = messageList.size,
            deletedMessages = messageList.count { it.isDeleted },
            appBreakdown = messageList.groupBy { it.appName }.mapValues { it.value.size },
            todayCount = messageList.count { message ->
                val today = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                message.timestamp > today
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MessageStats()
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
    
    fun setSelectedApp(app: String?) {
        _selectedApp.value = app
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    // Export functionality - now with proper user feedback
    fun exportMessages(format: ExportFormat) {
        viewModelScope.launch {
            try {
                _exportStatus.value = "Exporting ${format.name.lowercase()} file..."
                
                val filename = when (format) {
                    ExportFormat.JSON -> exportAsJson()
                    ExportFormat.CSV -> exportAsCsv()
                    ExportFormat.TXT -> exportAsTxt()
                    ExportFormat.HTML -> exportAsHtml()
                    ExportFormat.PDF -> exportAsPdf()
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), 
                        "Export completed: $filename", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                _exportStatus.value = "Export completed: $filename"
                
                // Auto-clear status after 3 seconds
                kotlinx.coroutines.delay(3000)
                _exportStatus.value = null
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), 
                        "Export failed: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                _exportStatus.value = "Export failed: ${e.message}"
            }
        }
    }
    
    // Export individual message with user feedback
    fun exportMessage(message: MessageEntity) {
        viewModelScope.launch {
            try {
                val exportData = buildString {
                    appendLine("=== Recovered Message ===")
                    appendLine("App: ${message.appName}")
                    appendLine("From: ${message.sender}")
                    appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))}")
                    appendLine("Type: ${message.messageType}")
                    if (message.isDeleted) {
                        appendLine("Status: DELETED")
                        message.deletedTimestamp?.let {
                            appendLine("Deleted: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it))}")
                        }
                    }
                    appendLine("Content: ${message.messageContent}")
                    if (message.chatIdentifier != null) {
                        appendLine("Chat: ${message.chatIdentifier}")
                    }
                }
                
                val filename = "message_${System.currentTimeMillis()}.txt"
                saveToFile(filename, exportData)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), 
                        "Message exported: $filename", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), 
                        "Export failed: ${e.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    // Working share functionality
    fun shareMessage(message: MessageEntity) {
        viewModelScope.launch {
            try {
                val shareText = buildString {
                    appendLine("Recovered message from ${message.appName}:")
                    appendLine("From: ${message.sender}")
                    appendLine("Time: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(message.timestamp))}")
                    appendLine("")
                    appendLine("\"${message.messageContent}\"")
                    if (message.isDeleted) {
                        appendLine("")
                        appendLine("(This message was deleted)")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "Recovered Message from ${message.appName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    
                    getApplication<Application>().startActivity(
                        Intent.createChooser(shareIntent, "Share Message").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(), 
                        "Share failed: ${e.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private suspend fun exportAsJson(): String {
        val messages = repository.getAllMessages().first()
        val json = buildString {
            append("{\n")
            append("  \"exportInfo\": {\n")
            append("    \"generatedAt\": \"${formatTimestamp(System.currentTimeMillis())}\",\n")
            append("    \"totalMessages\": ${messages.size},\n")
            append("    \"appName\": \"Message Logger\"\n")
            append("  },\n")
            append("  \"messages\": [\n")
            messages.forEachIndexed { index, message ->
                append("    {\n")
                append("      \"id\": ${message.id},\n")
                append("      \"timestamp\": \"${formatTimestamp(message.timestamp)}\",\n")
                append("      \"sender\": \"${escapeJson(message.sender)}\",\n")
                append("      \"content\": \"${escapeJson(message.messageContent)}\",\n")
                append("      \"app\": \"${escapeJson(message.appName)}\",\n")
                append("      \"packageName\": \"${escapeJson(message.packageName)}\",\n")
                append("      \"type\": \"${message.messageType}\",\n")
                append("      \"deleted\": ${message.isDeleted}")
                if (message.isDeleted && message.deletedTimestamp != null) {
                    append(",\n      \"deletedAt\": \"${formatTimestamp(message.deletedTimestamp!!)}\"\n")
                } else {
                    append("\n")
                }
                append("    }")
                if (index < messages.size - 1) append(",")
                append("\n")
            }
            append("  ]\n")
            append("}")
        }
        val filename = "messages_export_${System.currentTimeMillis()}.json"
        saveToFile(filename, json)
        return filename
    }
    
    private suspend fun exportAsCsv(): String {
        val messages = repository.getAllMessages().first()
        val csv = buildString {
            append("ID,Timestamp,Sender,Content,App,PackageName,Type,Deleted,DeletedAt\n")
            messages.forEach { message ->
                append("\"${message.id}\",")
                append("\"${formatTimestamp(message.timestamp)}\",")
                append("\"${escapeCsv(message.sender)}\",")
                append("\"${escapeCsv(message.messageContent)}\",")
                append("\"${escapeCsv(message.appName)}\",")
                append("\"${escapeCsv(message.packageName)}\",")
                append("\"${message.messageType}\",")
                append("\"${message.isDeleted}\",")
                append("\"${if (message.deletedTimestamp != null) formatTimestamp(message.deletedTimestamp!!) else ""}\"\n")
            }
        }
        val filename = "messages_export_${System.currentTimeMillis()}.csv"
        saveToFile(filename, csv)
        return filename
    }
    
    private suspend fun exportAsTxt(): String {
        val messages = repository.getAllMessages().first()
        val txt = buildString {
            appendLine("MESSAGE LOGGER - EXPORT REPORT")
            appendLine("===============================")
            appendLine("Generated: ${formatTimestamp(System.currentTimeMillis())}")
            appendLine("Total Messages: ${messages.size}")
            appendLine("Deleted Messages: ${messages.count { it.isDeleted }}")
            appendLine("Apps: ${messages.map { it.appName }.distinct().joinToString(", ")}")
            appendLine()
            appendLine("=".repeat(50))
            appendLine()
            
            messages.groupBy { it.appName }.forEach { (appName, appMessages) ->
                appendLine("📱 $appName (${appMessages.size} messages)")
                appendLine("-".repeat(40))
                
                appMessages.forEach { message ->
                    appendLine()
                    appendLine("🕐 ${formatTimestamp(message.timestamp)}")
                    appendLine("👤 From: ${message.sender}")
                    appendLine("📧 ${message.messageContent}")
                    if (message.isDeleted) {
                        appendLine("🗑️ *** DELETED MESSAGE ***")
                        message.deletedTimestamp?.let {
                            appendLine("   Deleted at: ${formatTimestamp(it)}")
                        }
                    }
                    appendLine("📋 Type: ${message.messageType}")
                }
                appendLine()
                appendLine("=".repeat(50))
                appendLine()
            }
        }
        val filename = "messages_export_${System.currentTimeMillis()}.txt"
        saveToFile(filename, txt)
        return filename
    }
    
    private suspend fun exportAsHtml(): String {
        val messages = repository.getAllMessages().first()
        val html = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Message Logger - Export Report</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Arial, sans-serif; 
                            margin: 20px; 
                            background-color: #f5f5f5; 
                        }
                        .header { 
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                            color: white; 
                            padding: 20px; 
                            border-radius: 10px; 
                            margin-bottom: 20px; 
                        }
                        .stats { 
                            display: flex; 
                            gap: 20px; 
                            margin-bottom: 20px; 
                        }
                        .stat-card { 
                            background: white; 
                            padding: 15px; 
                            border-radius: 8px; 
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
                            flex: 1; 
                        }
                        .message { 
                            background: white; 
                            border-left: 4px solid #667eea; 
                            margin: 10px 0; 
                            padding: 15px; 
                            border-radius: 0 8px 8px 0; 
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
                        }
                        .deleted { 
                            border-left-color: #e74c3c; 
                            background-color: #fdf2f2; 
                        }
                        .timestamp { 
                            color: #666; 
                            font-size: 0.9em; 
                            margin-bottom: 5px; 
                        }
                        .sender { 
                            font-weight: bold; 
                            color: #333; 
                            margin-bottom: 5px; 
                        }
                        .app { 
                            color: #007bff; 
                            font-size: 0.85em; 
                            background: #e3f2fd; 
                            padding: 2px 8px; 
                            border-radius: 12px; 
                            display: inline-block; 
                            margin-bottom: 10px; 
                        }
                        .content { 
                            margin: 10px 0; 
                            line-height: 1.5; 
                        }
                        .deleted-badge { 
                            color: #e74c3c; 
                            font-weight: bold; 
                            font-size: 0.85em; 
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>📱 Message Logger - Export Report</h1>
                        <p>Generated: ${formatTimestamp(System.currentTimeMillis())}</p>
                    </div>
                    
                    <div class="stats">
                        <div class="stat-card">
                            <h3>📊 Total Messages</h3>
                            <h2>${messages.size}</h2>
                        </div>
                        <div class="stat-card">
                            <h3>🗑️ Deleted Messages</h3>
                            <h2>${messages.count { it.isDeleted }}</h2>
                        </div>
                        <div class="stat-card">
                            <h3>📱 Apps Monitored</h3>
                            <h2>${messages.map { it.appName }.distinct().size}</h2>
                        </div>
                    </div>
            """.trimIndent())
            
            // Group messages by app for better organization
            messages.groupBy { it.appName }.forEach { (appName, appMessages) ->
                append("""
                    <h2>📱 $appName (${appMessages.size} messages)</h2>
                """.trimIndent())
                
                appMessages.forEach { message ->
                    append("""
                        <div class="message${if (message.isDeleted) " deleted" else ""}">
                            <div class="timestamp">🕐 ${formatTimestamp(message.timestamp)}</div>
                            <div class="app">${escapeHtml(message.appName)}</div>
                            <div class="sender">👤 ${escapeHtml(message.sender)}</div>
                            <div class="content">💬 ${escapeHtml(message.messageContent)}</div>
                            ${if (message.isDeleted) {
                                val deletedAt = if (message.deletedTimestamp != null) 
                                    " at ${formatTimestamp(message.deletedTimestamp!!)}" else ""
                                "<div class=\"deleted-badge\">🗑️ DELETED MESSAGE$deletedAt</div>"
                            } else ""}
                        </div>
                    """.trimIndent())
                }
            }
            
            append("""
                </body>
                </html>
            """.trimIndent())
        }
        val filename = "messages_export_${System.currentTimeMillis()}.html"
        saveToFile(filename, html)
        return filename
    }
    
    private suspend fun exportAsPdf(): String {
        // For now, create a formatted text-based PDF representation
        // In a real implementation, you'd use a PDF library like iText
        return exportAsTxt() // Fallback to text for now
    }
    
    private fun saveToFile(filename: String, content: String) {
        try {
            // Use app-specific external directory for better compatibility
            val app = getApplication<Application>()
            val externalDir = app.getExternalFilesDir(null) ?: app.filesDir
            val exportsDir = File(externalDir, "exports")
            
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            
            val file = File(exportsDir, filename)
            file.writeText(content, Charsets.UTF_8)
            
            println("File saved successfully: ${file.absolutePath}")
            
        } catch (e: Exception) {
            println("Failed to save file: ${e.message}")
            throw e
        }
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

data class MessageStats(
    val totalRecovered: Int = 0,
    val deletedMessages: Int = 0,
    val appBreakdown: Map<String, Int> = emptyMap(),
    val todayCount: Int = 0
)