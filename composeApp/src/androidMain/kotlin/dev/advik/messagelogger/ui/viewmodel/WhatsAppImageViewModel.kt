package dev.advik.messagelogger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.advik.messagelogger.data.repository.MessageLoggerRepository
import dev.advik.messagelogger.data.entity.WhatsAppImageEntity
import kotlinx.coroutines.flow.*

class WhatsAppImageViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MessageLoggerRepository.getInstance(application)

    private val _showDeletedOnly = MutableStateFlow(false)
    val showDeletedOnly: StateFlow<Boolean> = _showDeletedOnly.asStateFlow()

    val images: StateFlow<List<WhatsAppImageEntity>> = combine(
        repository.getAllImages(),
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

    val allImages: StateFlow<List<WhatsAppImageEntity>> = repository.getAllImages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeImageCount: StateFlow<Int> = repository.getActiveImages()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val deletedImageCount: StateFlow<Int> = repository.getDeletedImages()
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