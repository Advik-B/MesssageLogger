# Message Logger - Ad-Free Notification & WhatsApp Image Logger

A comprehensive Android app built with Kotlin Multiplatform and Jetpack Compose that logs all notifications from all apps and monitors WhatsApp images for backup and deletion tracking.

## 🚀 Features

### 📱 Notification Logger
- **Comprehensive Logging**: Captures notifications from ALL apps (WhatsApp, Instagram, etc.)
- **Rich Data Capture**: App name, package name, app icon, notification title, text, and timestamp
- **Smart Filtering**: Filter notifications by specific apps using dropdown menu
- **App Icons**: Automatically extracts and displays app icons for easy identification
- **Local Storage**: All data stored locally using Room (SQLite) database - NO CLOUD, NO ADS

### 🖼️ WhatsApp Image Monitor
- **Real-time Monitoring**: Uses FileObserver to watch WhatsApp Images folder
- **Automatic Backup**: Creates backup copies of all WhatsApp images in app's internal storage
- **Deletion Tracking**: Logs when images are deleted with timestamps
- **Image Preview**: Shows thumbnails of backed up images
- **Status Indicators**: Clear visual indicators for active vs deleted images

### 🔒 Privacy & Storage
- **100% Offline**: Everything stored locally, no cloud services
- **Ad-Free**: Completely ad-free experience
- **Secure Storage**: Uses Android's internal storage for backups
- **Minimal Permissions**: Only requests essential permissions needed for functionality

## 📋 Requirements

### Android Version Support
- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Optimized for**: Android 10-14

### Required Permissions
1. **Notification Listener Service**
   - Required to capture notifications from all apps
   - Must be manually enabled in Android Settings

2. **Storage Access**
   - `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (Android 10-)
   - `MANAGE_EXTERNAL_STORAGE` (Android 11+)
   - Required to monitor WhatsApp Images folder and create backups

## 🛠️ Setup Instructions

### 1. Install the App
1. Download the APK from releases
2. Enable "Install from Unknown Sources" if needed
3. Install the APK

### 2. Enable Notification Access
1. Open the app
2. Tap "Enable Permissions" button
3. Select "Message Logger" from the notification access list
4. Toggle ON the notification access permission
5. Return to the app

### 3. Enable Storage Access (Android 11+)
1. If prompted, tap "Enable Storage Access"
2. Select "Allow management of all files" or similar option
3. Return to the app

### 4. Start Using
- **Main Screen**: View all notifications with filtering options
- **WhatsApp Images Tab**: Monitor WhatsApp image backups and deletions
- **Filter**: Use the filter button to view notifications from specific apps

## 🏗️ Architecture

### Technology Stack
- **Kotlin Multiplatform**: Modern cross-platform development
- **Jetpack Compose**: Modern declarative UI framework
- **Room Database**: Local SQLite database with type-safe access
- **Material 3**: Latest Material Design components
- **Coroutines & Flow**: Reactive programming for smooth UI updates

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE's toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Project Structure
```
composeApp/src/androidMain/kotlin/dev/advik/messagelogger/
├── data/
│   ├── entity/          # Room database entities
│   ├── dao/             # Database access objects
│   └── database/        # Database configuration
├── repository/          # Data access layer
├── service/             # Background services
│   ├── NotificationListenerService.kt
│   └── WhatsAppImageObserverService.kt
├── ui/
│   ├── screen/          # Main UI screens
│   ├── component/       # Reusable UI components
│   └── viewmodel/       # UI state management
└── MainActivity.kt
```

## 🔒 Privacy

This app is designed with privacy as a core principle:
- **No Network Access**: The app does not access the internet
- **Local Storage Only**: All data is stored locally on your device
- **No Analytics**: No tracking or analytics services
- **No Ads**: Completely ad-free experience
- **Minimal Data Collection**: Only collects notification metadata and file information

## ⚠️ Important Notes

1. **WhatsApp Folder Access**: The app monitors `/storage/emulated/0/WhatsApp/Media/WhatsApp Images/`
2. **Background Services**: Runs minimal NotificationListenerService and FileObserver
3. **Storage Usage**: Creates backups of WhatsApp images (additional storage space required)

## 🆘 Troubleshooting

### Notifications Not Being Logged
1. Verify notification access is enabled in Settings > Apps > Special Access > Notification Access
2. Restart the app after enabling permissions

### WhatsApp Images Not Being Monitored
1. Ensure storage permissions are granted
2. Verify WhatsApp is saving images to the standard directory

---

This is a Kotlin Multiplatform project targeting Android focused on notification and WhatsApp image logging.