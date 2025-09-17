package dev.advik.messagelogger.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import dev.advik.messagelogger.data.entity.WhatsAppImage

@Composable
fun WhatsAppImageItem(
    image: WhatsAppImage,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (image.isDeleted) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Image thumbnail
            ImageThumbnail(
                backupPath = image.backupPath,
                fileName = image.fileName,
                isDeleted = image.isDeleted,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Image details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // File name and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = image.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (image.isDeleted) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "DELETED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Timestamps
                Text(
                    text = "Created: ${dateFormat.format(Date(image.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (image.isDeleted && image.deletedTimestamp != null) {
                    Text(
                        text = "Deleted: ${dateFormat.format(Date(image.deletedTimestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // File size and path info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatFileSize(image.fileSizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    if (image.backupPath != null) {
                        Text(
                            text = "BACKED UP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    backupPath: String?,
    fileName: String,
    isDeleted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = if (isDeleted) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outline)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        if (backupPath != null && File(backupPath).exists()) {
            val bitmap = remember(backupPath) {
                BitmapFactory.decodeFile(backupPath)
            }
            
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = fileName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                return@Card
            }
        }
        
        // Fallback placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDeleted) "❌" else "🖼️",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024
    val mb = kb / 1024
    
    return when {
        mb > 0 -> "${mb} MB"
        kb > 0 -> "${kb} KB"
        else -> "${bytes} B"
    }
}