package dev.advik.messagelogger.data.entity

/**
 * App settings entity as specified in app.md
 * All features are FREE - no premium restrictions
 */
data class AppSettingsEntity(
    val id: Long = 1,
    val monitoredApps: Set<String>, // Package names to monitor
    val autoDownloadMedia: Boolean = true, // FREE: Auto-download media
    val storageLimit: Long = -1, // -1 means unlimited (FREE)
    val retentionDays: Int = -1, // -1 means unlimited retention (FREE)
    val enableAppLock: Boolean = false,
    val biometricAuth: Boolean = false,
    val keywordFilters: Set<String> = emptySet(),
    val exportFormats: Set<ExportFormat> = setOf(ExportFormat.JSON, ExportFormat.TXT, ExportFormat.CSV),
    val enableEncryption: Boolean = true,
    val batteryOptimizationGuidanceShown: Boolean = false
)

enum class ExportFormat {
    JSON,
    TXT,
    CSV,
    PDF,
    HTML
}