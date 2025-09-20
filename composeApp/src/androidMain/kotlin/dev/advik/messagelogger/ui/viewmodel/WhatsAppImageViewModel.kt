package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.SimpleDataStore
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.flow.*

class WhatsAppImageViewModel(
    private val dataStore: SimpleDataStore = SimpleDataStore.getInstance()
) : ViewModel() {

    private val _showDeletedOnly = MutableStateFlow(false)
    val showDeletedOnly: StateFlow<Boolean> = _showDeletedOnly.asStateFlow()

    val images: StateFlow<List<WhatsAppImageEntity>> = combine(
        dataStore.whatsAppImages,
        _showDeletedOnly
    ) { imageList, showDeleted ->
        if (showDeleted) {
            imageList.filter { it.isDeleted }.sortedByDescending { it.deletedTimestamp ?: 0 }
        } else {
            imageList.filter { !it.isDeleted }.sortedByDescending { it.timestamp }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allImages: StateFlow<List<WhatsAppImageEntity>> = dataStore.whatsAppImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeImageCount: StateFlow<Int> = dataStore.activeImages
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val deletedImageCount: StateFlow<Int> = dataStore.deletedImages
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun toggleShowDeleted() {
        _showDeletedOnly.value = !_showDeletedOnly.value
    }

    fun showActiveImages() {
        _showDeletedOnly.value = false
    }

    fun showDeletedImages() {
        _showDeletedOnly.value = true
    }
}