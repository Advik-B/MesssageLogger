package dev.advik.messagelogger.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.advik.messagelogger.ui.component.WhatsAppImageItem
import dev.advik.messagelogger.ui.viewmodel.WhatsAppImageViewModel
import dev.advik.messagelogger.data.database.MessageLoggerDatabase
import dev.advik.messagelogger.repository.MessageLoggerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppImagesScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { MessageLoggerDatabase.getDatabase(context) }
    val repository = remember { 
        MessageLoggerRepository(
            notificationDao = database.notificationLogDao(),
            whatsAppImageDao = database.whatsAppImageDao()
        )
    }
    val viewModel = remember { WhatsAppImageViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Images") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowDeleted() }) {
                        Icon(
                            if (uiState.showDeletedOnly) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.showDeletedOnly) "Show Active" else "Show Deleted"
                        )
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (uiState.showDeletedOnly) "Deleted Images" else "Active Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row {
                        Text("Active: ${uiState.activeImages.size}")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Deleted: ${uiState.deletedImages.size}")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Total: ${uiState.allImages.size}")
                    }
                }
            }
            
            // Images list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                viewModel.getDisplayImages().isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.showDeletedOnly) "No deleted images found" else "No images found"
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(viewModel.getDisplayImages()) { image ->
                            WhatsAppImageItem(
                                image = image,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}