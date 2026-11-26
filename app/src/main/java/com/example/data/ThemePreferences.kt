package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gramx_settings")

enum class ChatSwipeAction {
    DELETE,
    ARCHIVE,
    PIN,
    UNREAD,
}

enum class FolderViewStyle {
    ICON,
    NAME,
}

data class UserAppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentPreset: AccentPreset = AccentPreset.CLASSIC_BLUE,
    val language: AppLanguage = AppLanguage.UZ,
    val chatFontSizeSp: Int = 16,
    val bubbleCornerRadiusDp: Int = 17,
    val chatListThreeLines: Boolean = false,
    val swipeAction: ChatSwipeAction = ChatSwipeAction.ARCHIVE,
    val folderViewStyle: FolderViewStyle = FolderViewStyle.ICON,
    val wallpaperStyle: Int = 0,
    val animationsEnabled: Boolean = true,
    val inAppBrowser: Boolean = true,
    val sendWithEnter: Boolean = false,
    val autoNightMode: Boolean = false,
    val powerSavingThreshold: Int = 20,
    val passcodePin: String = "",
    val autoDownloadMobile: Boolean = false,
    val autoDownloadWifi: Boolean = false,
    val autoDownloadRoaming: Boolean = false,
    val saveGalleryPrivate: Boolean = false,
    val saveGalleryGroups: Boolean = false,
    val saveGalleryChannels: Boolean = false,
    val streamingMedia: Boolean = true,
    val ghostModeEnabled: Boolean = false,
)

class ThemePreferences(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val accentPresetKey = stringPreferencesKey("accent_preset")
    private val languageKey = stringPreferencesKey("language_code")
    private val chatFontSizeKey = intPreferencesKey("chat_font_size_sp")
    private val bubbleRadiusKey = intPreferencesKey("bubble_corner_radius_dp")
    private val threeLinesKey = booleanPreferencesKey("chat_list_three_lines")
    private val swipeActionKey = stringPreferencesKey("swipe_action")
    private val folderStyleKey = stringPreferencesKey("folder_view_style")
    private val wallpaperStyleKey = intPreferencesKey("wallpaper_style")
    private val animationsKey = booleanPreferencesKey("animations_enabled")
    private val inAppBrowserKey = booleanPreferencesKey("in_app_browser")
    private val sendWithEnterKey = booleanPreferencesKey("send_with_enter")
    private val autoNightKey = booleanPreferencesKey("auto_night_mode")
    private val powerThresholdKey = intPreferencesKey("power_saving_threshold")
    private val passcodePinKey = stringPreferencesKey("passcode_pin")
    private val autoDownloadMobileKey = booleanPreferencesKey("auto_download_mobile")
    private val autoDownloadWifiKey = booleanPreferencesKey("auto_download_wifi")
    private val autoDownloadRoamingKey = booleanPreferencesKey("auto_download_roaming")
    private val saveGalleryPrivateKey = booleanPreferencesKey("save_gallery_private")
    private val saveGalleryGroupsKey = booleanPreferencesKey("save_gallery_groups")
    private val saveGalleryChannelsKey = booleanPreferencesKey("save_gallery_channels")
    private val streamingMediaKey = booleanPreferencesKey("streaming_media")
    private val ghostModeKey = booleanPreferencesKey("ghost_mode_enabled")

    val settingsFlow: Flow<UserAppearanceSettings> = context.dataStore.data.map { prefs ->
        UserAppearanceSettings(
            themeMode = prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            accentPreset = prefs[accentPresetKey]?.let { runCatching { AccentPreset.valueOf(it) }.getOrNull() }
                ?: AccentPreset.CLASSIC_BLUE,
            language = prefs[languageKey]?.let { code -> AppLanguage.entries.firstOrNull { it.code == code } }
                ?: AppLanguage.UZ,
            chatFontSizeSp = (prefs[chatFontSizeKey] ?: 16).coerceIn(12, 24),
            bubbleCornerRadiusDp = (prefs[bubbleRadiusKey] ?: 17).coerceIn(4, 20),
            chatListThreeLines = prefs[threeLinesKey] ?: false,
            swipeAction = prefs[swipeActionKey]?.let { runCatching { ChatSwipeAction.valueOf(it) }.getOrNull() }
                ?: ChatSwipeAction.ARCHIVE,
            folderViewStyle = prefs[folderStyleKey]?.let { runCatching { FolderViewStyle.valueOf(it) }.getOrNull() }
                ?: FolderViewStyle.ICON,
            wallpaperStyle = (prefs[wallpaperStyleKey] ?: 0).coerceIn(0, 4),
            animationsEnabled = prefs[animationsKey] ?: true,
            inAppBrowser = prefs[inAppBrowserKey] ?: true,
            sendWithEnter = prefs[sendWithEnterKey] ?: false,
            autoNightMode = prefs[autoNightKey] ?: false,
            powerSavingThreshold = (prefs[powerThresholdKey] ?: 20).coerceIn(5, 80),
            passcodePin = prefs[passcodePinKey] ?: "",
            autoDownloadMobile = prefs[autoDownloadMobileKey] ?: false,
            autoDownloadWifi = prefs[autoDownloadWifiKey] ?: false,
            autoDownloadRoaming = prefs[autoDownloadRoamingKey] ?: false,
            saveGalleryPrivate = prefs[saveGalleryPrivateKey] ?: false,
            saveGalleryGroups = prefs[saveGalleryGroupsKey] ?: false,
            saveGalleryChannels = prefs[saveGalleryChannelsKey] ?: false,
            streamingMedia = prefs[streamingMediaKey] ?: true,
            ghostModeEnabled = prefs[ghostModeKey] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    suspend fun setAccentPreset(preset: AccentPreset) {
        context.dataStore.edit { it[accentPresetKey] = preset.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[languageKey] = language.code }
    }

    suspend fun setChatFontSize(sizeSp: Int) {
        context.dataStore.edit { it[chatFontSizeKey] = sizeSp.coerceIn(12, 24) }
    }

    suspend fun setBubbleCornerRadius(radiusDp: Int) {
        context.dataStore.edit { it[bubbleRadiusKey] = radiusDp.coerceIn(4, 20) }
    }

    suspend fun setChatListThreeLines(threeLines: Boolean) {
        context.dataStore.edit { it[threeLinesKey] = threeLines }
    }

    suspend fun setSwipeAction(action: ChatSwipeAction) {
        context.dataStore.edit { it[swipeActionKey] = action.name }
    }

    suspend fun setFolderViewStyle(style: FolderViewStyle) {
        context.dataStore.edit { it[folderStyleKey] = style.name }
    }

    suspend fun setWallpaperStyle(style: Int) {
        context.dataStore.edit { it[wallpaperStyleKey] = style.coerceIn(0, 4) }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[animationsKey] = enabled }
    }

    suspend fun setInAppBrowser(enabled: Boolean) {
        context.dataStore.edit { it[inAppBrowserKey] = enabled }
    }

    suspend fun setSendWithEnter(enabled: Boolean) {
        context.dataStore.edit { it[sendWithEnterKey] = enabled }
    }

    suspend fun setAutoNightMode(enabled: Boolean) {
        context.dataStore.edit { it[autoNightKey] = enabled }
    }

    suspend fun setPowerSavingThreshold(percent: Int) {
        context.dataStore.edit { it[powerThresholdKey] = percent.coerceIn(5, 80) }
    }

    suspend fun setPasscodePin(pin: String) {
        context.dataStore.edit { it[passcodePinKey] = pin.filter { ch -> ch.isDigit() }.take(4) }
    }

    suspend fun setAutoDownloadMobile(enabled: Boolean) {
        context.dataStore.edit { it[autoDownloadMobileKey] = enabled }
    }

    suspend fun setAutoDownloadWifi(enabled: Boolean) {
        context.dataStore.edit { it[autoDownloadWifiKey] = enabled }
    }

    suspend fun setAutoDownloadRoaming(enabled: Boolean) {
        context.dataStore.edit { it[autoDownloadRoamingKey] = enabled }
    }

    suspend fun resetAutoDownloadDefaults() {
        context.dataStore.edit {
            it[autoDownloadMobileKey] = false
            it[autoDownloadWifiKey] = false
            it[autoDownloadRoamingKey] = false
        }
    }

    suspend fun setSaveGalleryPrivate(enabled: Boolean) {
        context.dataStore.edit { it[saveGalleryPrivateKey] = enabled }
    }

    suspend fun setSaveGalleryGroups(enabled: Boolean) {
        context.dataStore.edit { it[saveGalleryGroupsKey] = enabled }
    }

    suspend fun setSaveGalleryChannels(enabled: Boolean) {
        context.dataStore.edit { it[saveGalleryChannelsKey] = enabled }
    }

    suspend fun setStreamingMedia(enabled: Boolean) {
        context.dataStore.edit { it[streamingMediaKey] = enabled }
    }

    suspend fun setGhostModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ghostModeKey] = enabled }
    }
}
