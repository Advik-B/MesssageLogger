package dev.advik.messagelogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dev.advik.messagelogger.data.entity.WhatsAppImage
import dev.advik.messagelogger.repository.MessageLoggerRepository

data class WhatsAppImageUiState(
    val allImages: List<WhatsAppImage> = emptyList(),
    val activeImages: List<WhatsAppImage> = emptyList(),
    val deletedImages: List<WhatsAppImage> = emptyList(),
    val showDeletedOnly: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WhatsAppImageViewModel(
    private val repository: MessageLoggerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WhatsAppImageUiState())
    val uiState: StateFlow<WhatsAppImageUiState> = _uiState.asStateFlow()
    
    init {
        loadImages()
    }
    
    fun toggleShowDeleted() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(showDeletedOnly = !currentState.showDeletedOnly)
    }
    
    private fun loadImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load all images
                repository.getAllWhatsAppImages().collect { allImages ->
                    val activeImages = allImages.filter { !it.isDeleted }
                    val deletedImages = allImages.filter { it.isDeleted }
                    
                    _uiState.value = _uiState.value.copy(
                        allImages = allImages,
                        activeImages = activeImages,
                        deletedImages = deletedImages,
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
        loadImages()
    }
    
    fun getDisplayImages(): List<WhatsAppImage> {
        val currentState = _uiState.value
        return if (currentState.showDeletedOnly) {
            currentState.deletedImages
        } else {
            currentState.activeImages
        }
    }
}