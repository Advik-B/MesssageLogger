package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.AppSettingsEntity
import dev.advik.messagelogger.data.entity.ExportFormat
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Settings ViewModel implementing app.md specifications
 * ALL premium features are FREE - no restrictions
 */
class SettingsViewModel : ViewModel() {
    
    private val dataStore = SimpleDataStore.getInstance()
    
    val settings: StateFlow<AppSettingsEntity> = dataStore.settings
    
    // FREE: Multi-app monitoring (was premium feature)
    fun toggleAppMonitoring(packageName: String) {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            if (currentSettings.monitoredApps.contains(packageName)) {
                dataStore.removeMonitoredApp(packageName)
            } else {
                dataStore.addMonitoredApp(packageName)
            }
        }
    }
    
    // FREE: Auto-download media
    fun toggleAutoDownload() {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(autoDownloadMedia = !currentSettings.autoDownloadMedia)
            )
        }
    }
    
    // FREE: Unlimited retention (no 7-day restriction)
    fun updateRetentionDays(days: Int) {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(retentionDays = days)
            )
        }
    }
    
    // FREE: App lock security feature
    fun toggleAppLock() {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(enableAppLock = !currentSettings.enableAppLock)
            )
        }
    }
    
    // FREE: Biometric authentication
    fun toggleBiometricAuth() {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(biometricAuth = !currentSettings.biometricAuth)
            )
        }
    }
    
    // FREE: Database encryption
    fun toggleEncryption() {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(enableEncryption = !currentSettings.enableEncryption)
            )
        }
    }
    
    // FREE: Export functionality in multiple formats (was premium)
    fun toggleExportFormat(format: ExportFormat) {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            val updatedFormats = if (currentSettings.exportFormats.contains(format)) {
                currentSettings.exportFormats - format
            } else {
                currentSettings.exportFormats + format
            }
            
            dataStore.updateSettings(
                currentSettings.copy(exportFormats = updatedFormats)
            )
        }
    }
    
    // FREE: Keyword filtering (was premium feature)
    fun addKeywordFilter(keyword: String) {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(
                    keywordFilters = currentSettings.keywordFilters + keyword.trim()
                )
            )
        }
    }
    
    fun removeKeywordFilter(keyword: String) {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            dataStore.updateSettings(
                currentSettings.copy(
                    keywordFilters = currentSettings.keywordFilters - keyword
                )
            )
        }
    }
    
    // Storage management helper
    fun clearOldMessages() {
        viewModelScope.launch {
            val currentSettings = dataStore.settings.value
            if (currentSettings.retentionDays > 0) {
                val cutoffTime = System.currentTimeMillis() - (currentSettings.retentionDays * 24 * 60 * 60 * 1000L)
                // TODO: Implement actual cleanup logic
                println("Would clean messages older than $cutoffTime")
            }
        }
    }
    
    // Export all data (FREE feature)
    fun exportAllData() {
        viewModelScope.launch {
            try {
                val allMessages = dataStore.recoveredMessages.value
                val currentSettings = dataStore.settings.value
                
                val exportData = buildString {
                    appendLine("=== Message Logger Export ===")
                    appendLine("Export Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                    appendLine("Total Messages: ${allMessages.size}")
                    appendLine("Monitored Apps: ${currentSettings.monitoredApps.joinToString(", ")}")
                    appendLine()
                    
                    for (message in allMessages) {
                        appendLine("---")
                        appendLine("App: ${message.appName}")
                        appendLine("From: ${message.sender}")
                        appendLine("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))}")
                        appendLine("Type: ${message.messageType}")
                        if (message.isDeleted) {
                            appendLine("Status: DELETED")
                        }
                        appendLine("Content: ${message.messageContent}")
                        appendLine()
                    }
                }
                
                // TODO: Implement actual file export
                println("Export data prepared: ${exportData.length} characters")
            } catch (e: Exception) {
                println("Export failed: ${e.message}")
            }
        }
    }
}