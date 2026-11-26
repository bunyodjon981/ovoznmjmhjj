package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AppLanguage
import com.example.data.LocalStrings
import com.example.data.UserAppearanceSettings
import com.example.data.stringsFor

data class TelegramExtendedColors(
    val isDark: Boolean,
    val headerBackground: Color,
    val headerContent: Color,
    val chatBackground: Color,
    val cardBackground: Color,
    val pillBackground: Color,
    val floatingNavBackground: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val outgoingTime: Color,
    val incomingTime: Color,
    val unreadBadge: Color,
    val mutedBadge: Color,
    val onlineDot: Color,
    val divider: Color,
    val drawerHeaderBg: Color,
    val accent: Color,
)

val LocalTelegramColors = staticCompositionLocalOf {
    TelegramExtendedColors(
        isDark = true,
        headerBackground = TgNightHeader,
        headerContent = TgNightTextPrimary,
        chatBackground = TgNightChatBg,
        cardBackground = TgNightCard,
        pillBackground = TgNightSurface,
        floatingNavBackground = TgNightFloatingNav,
        outgoingBubble = TgOutgoingBubbleDark,
        incomingBubble = TgIncomingBubbleDark,
        outgoingTime = Color(0xFF9AC2EE),
        incomingTime = TgNightTextSecondary,
        unreadBadge = Color(0xFF3FA3E8),
        mutedBadge = Color(0xFF3E5266),
        onlineDot = TgOnlineGreen,
        divider = TgNightDivider,
        drawerHeaderBg = TgNightHeader,
        accent = Color(0xFF3FA3E8),
    )
}

val LocalAppearanceSettings = staticCompositionLocalOf { UserAppearanceSettings() }

@Composable
fun GramXTheme(
    appearanceSettings: UserAppearanceSettings = UserAppearanceSettings(),
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appearanceSettings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val accent = if (isDark) appearanceSettings.accentPreset.darkPrimary else appearanceSettings.accentPreset.primary

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF234263),
            onPrimaryContainer = Color.White,
            secondary = accent,
            onSecondary = Color.White,
            background = TgNightBg,
            onBackground = TgNightTextPrimary,
            surface = TgNightBg,
            onSurface = TgNightTextPrimary,
            surfaceVariant = TgNightCard,
            onSurfaceVariant = TgNightTextSecondary,
            surfaceContainerLowest = TgNightChatBg,
            surfaceContainerLow = TgNightHeader,
            surfaceContainer = TgNightCard,
            surfaceContainerHigh = TgNightFloatingNav,
            outlineVariant = TgNightDivider,
            error = Color(0xFFEF5350),
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD6EFFF),
            onPrimaryContainer = Color(0xFF0A3954),
            secondary = accent,
            onSecondary = Color.White,
            background = Color(0xFFF1F4F8),
            onBackground = Color(0xFF1A1E24),
            surface = Color(0xFFF1F4F8),
            onSurface = Color(0xFF1A1E24),
            surfaceVariant = Color.White,
            onSurfaceVariant = Color(0xFF6B7682),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF7F9FA),
            surfaceContainer = Color.White,
            surfaceContainerHigh = Color(0xFFE7ECF2),
            outlineVariant = Color(0xFFE2E7ED),
            error = TgDraftRed,
        )
    }

    val extended = if (isDark) {
        TelegramExtendedColors(
            isDark = true,
            headerBackground = TgNightBg,
            headerContent = TgNightTextPrimary,
            chatBackground = TgNightChatBg,
            cardBackground = TgNightCard,
            pillBackground = Color(0xFF192430),
            floatingNavBackground = TgNightFloatingNav,
            outgoingBubble = TgOutgoingBubbleDark,
            incomingBubble = TgIncomingBubbleDark,
            outgoingTime = Color(0xFFA7CBF2),
            incomingTime = TgNightTextSecondary,
            unreadBadge = accent,
            mutedBadge = Color(0xFF3E4E5E),
            onlineDot = TgOnlineGreen,
            divider = TgNightDivider,
            drawerHeaderBg = TgNightHeader,
            accent = accent,
        )
    } else {
        val header = if (appearanceSettings.accentPreset == AccentPreset.CLASSIC_BLUE) TgHeaderLight else appearanceSettings.accentPreset.primary
        TelegramExtendedColors(
            isDark = false,
            headerBackground = Color(0xFFFFFFFF),
            headerContent = Color(0xFF1A1E24),
            chatBackground = TgLightChatBg,
            cardBackground = Color.White,
            pillBackground = Color(0xFFE9EEF4),
            floatingNavBackground = Color(0xFFFFFFFF),
            outgoingBubble = TgOutgoingBubbleLight,
            incomingBubble = TgIncomingBubbleLight,
            outgoingTime = TgBubbleTimeOutLight,
            incomingTime = TgBubbleTimeInLight,
            unreadBadge = accent,
            mutedBadge = TgMutedBadge,
            onlineDot = TgOnlineGreen,
            divider = Color(0xFFE8ECEF),
            drawerHeaderBg = header,
            accent = accent,
        )
    }

    val strings = stringsFor(appearanceSettings.language)

    CompositionLocalProvider(
        LocalTelegramColors provides extended,
        LocalStrings provides strings,
        LocalAppearanceSettings provides appearanceSettings,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
