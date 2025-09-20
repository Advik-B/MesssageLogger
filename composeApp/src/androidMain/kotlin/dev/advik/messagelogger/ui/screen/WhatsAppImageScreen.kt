package dev.advik.messagelogger.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.advik.messagelogger.ui.component.WhatsAppImageItem
import dev.advik.messagelogger.ui.viewmodel.WhatsAppImageViewModel

@Composable
fun WhatsAppImageScreen(
    viewModel: WhatsAppImageViewModel,
    modifier: Modifier = Modifier
) {
    val images by viewModel.images.collectAsStateWithLifecycle()
    val showDeletedOnly by viewModel.showDeletedOnly.collectAsStateWithLifecycle()
    val activeImageCount by viewModel.activeImageCount.collectAsStateWithLifecycle()
    val deletedImageCount by viewModel.deletedImageCount.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with stats and toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "WhatsApp Images",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$activeImageCount Active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$deletedImageCount Deleted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row {
                        FilterChip(
                            onClick = { viewModel.showActiveImages() },
                            label = { Text("Active") },
                            selected = !showDeletedOnly,
                            leadingIcon = if (!showDeletedOnly) {
                                { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        FilterChip(
                            onClick = { viewModel.showDeletedImages() },
                            label = { Text("Deleted") },
                            selected = showDeletedOnly,
                            leadingIcon = if (showDeletedOnly) {
                                { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Images list
        if (images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (showDeletedOnly) Icons.Default.Delete else Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (showDeletedOnly) {
                            "No deleted images"
                        } else {
                            "No images detected yet\n\nMake sure storage permissions are granted and WhatsApp is saving images"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(images) { image ->
                    WhatsAppImageItem(image = image)
                }
            }
        }
    }
}