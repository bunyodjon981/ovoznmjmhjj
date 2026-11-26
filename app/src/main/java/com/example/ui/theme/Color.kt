package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Telegram Classic Light Palette
val TgBlue = Color(0xFF2AABEE)
val TgBlueDark = Color(0xFF229ED9)
val TgBlueDeep = Color(0xFF1A84B8)
val TgHeaderLight = Color(0xFF517DA2)
val TgLightBg = Color(0xFFFFFFFF)
val TgLightSurface = Color(0xFFF3F6F9)
val TgLightCard = Color(0xFFFFFFFF)
val TgLightChatBg = Color(0xFFD5E4D0)
val TgOutgoingBubbleLight = Color(0xFFEEFFDE)
val TgIncomingBubbleLight = Color(0xFFFFFFFF)
val TgBubbleTimeOutLight = Color(0xFF4FAE4E)
val TgBubbleTimeInLight = Color(0xFF8C97A0)
val TgUnreadBadge = Color(0xFF4ECC5E)
val TgMutedBadge = Color(0xFF627280)
val TgOnlineGreen = Color(0xFF4CD964)
val TgDraftRed = Color(0xFFE53935)

// Telegram / aka Night Dark Palette (matched to user screenshots)
val TgNightBg = Color(0xFF0E161E)
val TgNightSurface = Color(0xFF17212B)
val TgNightCard = Color(0xFF1B2632)
val TgNightHeader = Color(0xFF121B24)
val TgNightChatBg = Color(0xFF0D151E)
val TgNightFloatingNav = Color(0xFF1A2430)
val TgOutgoingBubbleDark = Color(0xFF2B6BB0)
val TgIncomingBubbleDark = Color(0xFF1B2734)
val TgNightTextPrimary = Color(0xFFF4F7FA)
val TgNightTextSecondary = Color(0xFF7D90A3)
val TgNightDivider = Color(0xFF1D2936)

// Accent Theme Options
enum class AccentPreset(val label: String, val primary: Color, val darkPrimary: Color) {
    CLASSIC_BLUE("Classic Blue", Color(0xFF2AABEE), Color(0xFF3FA3E8)),
    OCEAN_TEAL("Ocean Teal", Color(0xFF00A884), Color(0xFF26C6A6)),
    ROYAL_INDIGO("Indigo", Color(0xFF5B67CA), Color(0xFF7D89E8)),
    EMERALD("Emerald", Color(0xFF2E9E5B), Color(0xFF45C476)),
    SUNSET("Sunset", Color(0xFFE86A50), Color(0xFFFF866E)),
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Day"),
    DARK("Night"),
}

// Telegram Avatar Gradients (matched to user/chat ID modulo 7)
val TelegramAvatarColors: List<Pair<Color, Color>> = listOf(
    Color(0xFFFF845E) to Color(0xFFD45246),
    Color(0xFFFEBB5B) to Color(0xFFF68136),
    Color(0xFFB694F9) to Color(0xFF6C61DF),
    Color(0xFF9AD164) to Color(0xFF46BA43),
    Color(0xFF53EDD6) to Color(0xFF28C9B7),
    Color(0xFF5CAFFA) to Color(0xFF408ACF),
    Color(0xFFFF8AAC) to Color(0xFFD95574),
)

fun avatarGradientForId(id: Long): Pair<Color, Color> {
    val index = ((id % TelegramAvatarColors.size) + TelegramAvatarColors.size).toInt() % TelegramAvatarColors.size
    return TelegramAvatarColors[index]
}
