package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.advik.messagelogger.data.entity.NotificationLog
import dev.advik.messagelogger.repository.MessageLoggerRepository

data class NotificationUiState(
    val notifications: List<NotificationLog> = emptyList(),
    val distinctApps: List<NotificationLog> = emptyList(),
    val selectedPackageName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val repository: MessageLoggerRepository
) : ViewModel() {
    
    private val _selectedPackageName = MutableStateFlow<String?>(null)
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadDistinctApps()
        loadNotifications()
    }
    
    fun selectApp(packageName: String?) {
        _selectedPackageName.value = packageName
        loadNotifications()
    }
    
    private fun loadDistinctApps() {
        viewModelScope.launch {
            repository.getDistinctApps().collect { apps ->
                _uiState.value = _uiState.value.copy(distinctApps = apps)
            }
        }
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val notificationsFlow = _selectedPackageName.value?.let { packageName ->
                    repository.getNotificationsByPackage(packageName)
                } ?: repository.getAllNotifications()
                
                notificationsFlow.collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        selectedPackageName = _selectedPackageName.value,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun refreshData() {
        loadNotifications()
        loadDistinctApps()
    }
}