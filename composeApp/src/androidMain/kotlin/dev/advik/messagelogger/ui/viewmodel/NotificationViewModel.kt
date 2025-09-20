package dev.advik.messagelogger.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.data.entity.NotificationEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MessageLoggerRepository.getInstance(application)
    private val packageManager = application.packageManager
    
    private val _selectedPackage = MutableStateFlow<String?>(null)
    val selectedPackage: StateFlow<String?> = _selectedPackage.asStateFlow()

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        repository.getAllNotifications(),
        _selectedPackage
    ) { notificationList, selectedPkg ->
        if (selectedPkg != null) {
            notificationList.filter { it.packageName == selectedPkg }
        } else {
            notificationList
        }.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Get distinct apps with app info
    val apps: StateFlow<List<AppWithIcon>> = repository.getAllNotifications()
        .map { notifications ->
            notifications
                .distinctBy { it.packageName }
                .map { notification ->
                    AppWithIcon(
                        packageName = notification.packageName,
                        appName = notification.appName,
                        icon = getAppIcon(notification.packageName)
                    )
                }
                .sortedBy { it.appName }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notificationCount: StateFlow<Int> = repository.getAllNotifications()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun selectPackage(packageName: String?) {
        _selectedPackage.value = packageName
    }

    fun clearAllNotifications() {
        // Would need to add this method to repository
        // For now, do nothing
    }
    
    private fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}

data class AppWithIcon(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)