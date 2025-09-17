package dev.advik.messagelogger.ui.component

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

@Composable
fun PermissionHandler(
    onPermissionStatusChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        hasNotificationPermission = isNotificationListenerEnabled(context)
        hasStoragePermission = isStoragePermissionGranted()
        onPermissionStatusChanged(hasNotificationPermission)
    }
    
    if (!hasNotificationPermission || !hasStoragePermission) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!hasNotificationPermission) {
                    Text(
                        text = "• Notification Access: Required to log notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Button(
                        onClick = { openNotificationSettings(context) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Enable Notification Access")
                    }
                }
                
                if (!hasStoragePermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Storage Access: Required to monitor WhatsApp images",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Button(
                        onClick = { openStorageSettings(context) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Enable Storage Access")
                    }
                }
            }
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledNotificationListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    val packageName = context.packageName
    return enabledNotificationListeners?.contains(packageName) == true
}

private fun isStoragePermissionGranted(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        // For Android 10 and below, we'll assume permission is granted
        // In a real app, you'd check WRITE_EXTERNAL_STORAGE permission here
        true
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    context.startActivity(intent)
}

private fun openStorageSettings(context: Context) {
    val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
    }
    context.startActivity(intent)
}