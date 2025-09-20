package dev.advik.messagelogger.data

/**
 * Supported messaging platforms as specified in app.md Section 5
 * All platforms are supported for FREE users
 */
object SupportedPlatforms {
    
    data class PlatformConfig(
        val packageName: String,
        val displayName: String,
        val isEnabled: Boolean = true,
        val supportedFeatures: Set<PlatformFeature> = emptySet()
    )
    
    enum class PlatformFeature {
        MESSAGE_RECOVERY,
        MEDIA_RECOVERY,
        GROUP_CHAT_DETECTION,
        STATUS_RECOVERY,
        BUSINESS_CHAT_DETECTION,
        CHANNEL_MESSAGE_RECOVERY,
        BOT_INTERACTION_LOGGING,
        SECRET_CHAT_DETECTION
    }
    
    val PRIMARY_PLATFORMS = listOf(
        PlatformConfig(
            packageName = "com.whatsapp",
            displayName = "WhatsApp",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY,
                PlatformFeature.GROUP_CHAT_DETECTION,
                PlatformFeature.STATUS_RECOVERY
            )
        ),
        PlatformConfig(
            packageName = "com.whatsapp.w4b",
            displayName = "WhatsApp Business",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY,
                PlatformFeature.GROUP_CHAT_DETECTION,
                PlatformFeature.BUSINESS_CHAT_DETECTION
            )
        ),
        PlatformConfig(
            packageName = "org.telegram.messenger",
            displayName = "Telegram",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY,
                PlatformFeature.CHANNEL_MESSAGE_RECOVERY,
                PlatformFeature.BOT_INTERACTION_LOGGING,
                PlatformFeature.SECRET_CHAT_DETECTION
            )
        ),
        PlatformConfig(
            packageName = "com.facebook.orca",
            displayName = "Facebook Messenger",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY
            )
        ),
        PlatformConfig(
            packageName = "com.instagram.android",
            displayName = "Instagram",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY
            )
        ),
        PlatformConfig(
            packageName = "org.thoughtcrime.securesms",
            displayName = "Signal",
            supportedFeatures = setOf(
                PlatformFeature.MESSAGE_RECOVERY,
                PlatformFeature.MEDIA_RECOVERY
            )
        )
    )
    
    val ALL_PACKAGE_NAMES = PRIMARY_PLATFORMS.map { it.packageName }.toSet()
    
    fun getPlatformConfig(packageName: String): PlatformConfig? {
        return PRIMARY_PLATFORMS.find { it.packageName == packageName }
    }
    
    fun isPlatformSupported(packageName: String): Boolean {
        return ALL_PACKAGE_NAMES.contains(packageName)
    }
}