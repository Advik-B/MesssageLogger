package dev.advik.messagelogger.data.entity

/**
 * Media file entity as specified in app.md
 * Represents downloaded media files associated with messages
 */
data class MediaFileEntity(
    val id: Long = 0,
    val messageId: Long,
    val fileType: String, // image, video, audio, document
    val filePath: String,
    val thumbnailPath: String? = null,
    val fileSize: Long,
    val mimeType: String,
    val downloadStatus: DownloadStatus,
    val downloadTimestamp: Long? = null,
    val originalUrl: String? = null
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}