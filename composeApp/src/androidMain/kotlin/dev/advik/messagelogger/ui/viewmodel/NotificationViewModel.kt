package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.NotificationEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val dataStore: SimpleDataStore = SimpleDataStore.getInstance()
) : ViewModel() {

    private val _selectedPackage = MutableStateFlow<String?>(null)
    val selectedPackage: StateFlow<String?> = _selectedPackage.asStateFlow()

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        dataStore.notifications,
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

    val apps = dataStore.distinctApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notificationCount: StateFlow<Int> = dataStore.notifications
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
        viewModelScope.launch {
            dataStore.clearAllNotifications()
        }
    }
}