package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.ChatSwipeAction
import com.example.data.FolderViewStyle
import com.example.data.SavedAccount
import com.example.data.ThemePreferences
import com.example.data.UserAppearanceSettings
import com.example.tdlib.AuthStage
import com.example.tdlib.ChatEntry
import com.example.tdlib.ChatTypingState
import com.example.tdlib.ConnectionStatus
import com.example.tdlib.TelegramClientManager
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val clientManager: TelegramClientManager = TelegramClientManager.getInstance(application)
    private val themePreferences = ThemePreferences(application)

    val appearanceSettings: StateFlow<UserAppearanceSettings> = themePreferences.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserAppearanceSettings())

    val authStage: StateFlow<AuthStage> = clientManager.authStage
    val authError: StateFlow<String?> = clientManager.authError
    val connectionStatus: StateFlow<ConnectionStatus> = clientManager.connectionStatus
    val me: StateFlow<TdApi.User?> = clientManager.me
    val chats: StateFlow<Map<Long, ChatEntry>> = clientManager.chats
    val users: StateFlow<Map<Long, TdApi.User>> = clientManager.users
    val supergroups: StateFlow<Map<Long, TdApi.Supergroup>> = clientManager.supergroups
    val basicGroups: StateFlow<Map<Long, TdApi.BasicGroup>> = clientManager.basicGroups
    val chatFolders: StateFlow<List<TdApi.ChatFolderInfo>> = clientManager.chatFolders
    val files: StateFlow<Map<Int, TdApi.File>> = clientManager.files
    val customEmojiStickers: StateFlow<Map<Long, TdApi.Sticker>> = clientManager.customEmojiStickers
    val userFullInfoMap: StateFlow<Map<Long, TdApi.UserFullInfo>> = clientManager.userFullInfoMap
    val chatActions: StateFlow<Map<Long, ChatTypingState>> = clientManager.chatActions
    val activeStories: StateFlow<Map<Long, TdApi.ChatActiveStories>> = clientManager.activeStories
    val loadingChatLists: StateFlow<Set<String>> = clientManager.loadingChatLists
    val exhaustedChatLists: StateFlow<Set<String>> = clientManager.exhaustedChatLists
    val accounts: StateFlow<List<SavedAccount>> = clientManager.accounts
    val activeAccountId: StateFlow<String> = clientManager.activeAccountId

    private val _isAuthBusy = MutableStateFlow(false)
    val isAuthBusy: StateFlow<Boolean> = _isAuthBusy.asStateFlow()

    fun switchToAccount(accountId: String) {
        clientManager.switchToAccount(accountId)
    }

    fun addNewAccount() {
        clientManager.addNewAccount()
    }

    fun cancelAddAccount() {
        clientManager.cancelAddAccount()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreferences.setThemeMode(mode) }
    }

    fun toggleQuickDarkMode() {
        val current = appearanceSettings.value.themeMode
        val next = if (current == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
        setThemeMode(next)
    }

    fun setAccentPreset(preset: AccentPreset) {
        viewModelScope.launch { themePreferences.setAccentPreset(preset) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { themePreferences.setLanguage(language) }
    }

    fun setChatFontSize(sizeSp: Int) {
        viewModelScope.launch { themePreferences.setChatFontSize(sizeSp) }
    }

    fun setBubbleCornerRadius(radiusDp: Int) {
        viewModelScope.launch { themePreferences.setBubbleCornerRadius(radiusDp) }
    }

    fun setChatListThreeLines(threeLines: Boolean) {
        viewModelScope.launch { themePreferences.setChatListThreeLines(threeLines) }
    }

    fun setSwipeAction(action: ChatSwipeAction) {
        viewModelScope.launch { themePreferences.setSwipeAction(action) }
    }

    fun setFolderViewStyle(style: FolderViewStyle) {
        viewModelScope.launch { themePreferences.setFolderViewStyle(style) }
    }

    fun setWallpaperStyle(style: Int) {
        viewModelScope.launch { themePreferences.setWallpaperStyle(style) }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setAnimationsEnabled(enabled) }
    }

    fun setInAppBrowser(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setInAppBrowser(enabled) }
    }

    fun setSendWithEnter(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setSendWithEnter(enabled) }
    }

    fun setAutoNightMode(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setAutoNightMode(enabled) }
    }

    fun setPowerSavingThreshold(percent: Int) {
        viewModelScope.launch { themePreferences.setPowerSavingThreshold(percent) }
    }

    fun setPasscodePin(pin: String) {
        viewModelScope.launch { themePreferences.setPasscodePin(pin) }
    }

    fun setAutoDownloadMobile(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setAutoDownloadMobile(enabled) }
    }

    fun setAutoDownloadWifi(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setAutoDownloadWifi(enabled) }
    }

    fun setAutoDownloadRoaming(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setAutoDownloadRoaming(enabled) }
    }

    fun resetAutoDownloadDefaults() {
        viewModelScope.launch { themePreferences.resetAutoDownloadDefaults() }
    }

    fun setSaveGalleryPrivate(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setSaveGalleryPrivate(enabled) }
    }

    fun setSaveGalleryGroups(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setSaveGalleryGroups(enabled) }
    }

    fun setSaveGalleryChannels(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setSaveGalleryChannels(enabled) }
    }

    fun setStreamingMedia(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setStreamingMedia(enabled) }
    }

    fun setGhostModeEnabled(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setGhostModeEnabled(enabled) }
    }

    fun toggleGhostMode() {
        val current = appearanceSettings.value.ghostModeEnabled
        setGhostModeEnabled(!current)
    }

    fun submitPhoneNumber(fullPhone: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.clearAuthError()
            clientManager.sendPhoneNumber(fullPhone)
            _isAuthBusy.value = false
        }
    }

    fun submitAuthCode(code: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.clearAuthError()
            clientManager.checkAuthCode(code)
            _isAuthBusy.value = false
        }
    }

    fun resendAuthCode() {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.resendAuthCode()
            _isAuthBusy.value = false
        }
    }

    fun submitPassword(password: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.clearAuthError()
            clientManager.checkAuthPassword(password)
            _isAuthBusy.value = false
        }
    }

    fun submitRegistration(firstName: String, lastName: String) {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.clearAuthError()
            clientManager.registerUser(firstName, lastName)
            _isAuthBusy.value = false
        }
    }

    fun requestQrLogin() {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.clearAuthError()
            clientManager.requestQrAuth()
            _isAuthBusy.value = false
        }
    }

    fun cancelAuthToPhone() {
        viewModelScope.launch {
            _isAuthBusy.value = true
            clientManager.backToPhoneLogin()
            _isAuthBusy.value = false
        }
    }
}
