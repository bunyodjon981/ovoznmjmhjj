package com.example.tdlib

import android.content.Context
import android.os.Build
import com.example.data.AccountStore
import com.example.data.ApiConfigLoader
import com.example.data.SavedAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

sealed class AuthStage {
    data object Initializing : AuthStage()
    data object WaitPhoneNumber : AuthStage()
    data class WaitQrConfirmation(val link: String) : AuthStage()
    data class WaitCode(
        val phoneNumber: String,
        val deliveryHint: String,
        val codeLength: Int,
        val timeoutSeconds: Int,
    ) : AuthStage()
    data class WaitPassword(
        val passwordHint: String,
        val hasRecoveryEmail: Boolean,
        val recoveryEmailPattern: String,
    ) : AuthStage()
    data object WaitRegistration : AuthStage()
    data object Ready : AuthStage()
    data object Closed : AuthStage()
}

enum class ConnectionStatus {
    CONNECTING,
    UPDATING,
    READY,
    WAITING_FOR_NETWORK,
}

data class ChatEntry(
    val chat: TdApi.Chat,
    val version: Long,
)

data class ChatTypingState(
    val label: String,
    val expiresAtMs: Long,
)

class TelegramClientManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val versionCounter = AtomicLong(0L)
    private val requestedDownloads = ConcurrentHashMap<Int, Boolean>()
    private val requestedCustomEmojis = ConcurrentHashMap<Long, Boolean>()
    private val loadingListsGuard = ConcurrentHashMap<String, Boolean>()

    val accountStore = AccountStore(appContext)
    val accounts: StateFlow<List<SavedAccount>> = accountStore.accounts
    val activeAccountId: StateFlow<String> = accountStore.activeAccountId

    @Volatile
    private var client: Client? = null

    private val _authStage = MutableStateFlow<AuthStage>(AuthStage.Initializing)
    val authStage: StateFlow<AuthStage> = _authStage.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CONNECTING)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _me = MutableStateFlow<TdApi.User?>(null)
    val me: StateFlow<TdApi.User?> = _me.asStateFlow()

    private val _chats = MutableStateFlow<Map<Long, ChatEntry>>(emptyMap())
    val chats: StateFlow<Map<Long, ChatEntry>> = _chats.asStateFlow()

    private val _users = MutableStateFlow<Map<Long, TdApi.User>>(emptyMap())
    val users: StateFlow<Map<Long, TdApi.User>> = _users.asStateFlow()

    private val _supergroups = MutableStateFlow<Map<Long, TdApi.Supergroup>>(emptyMap())
    val supergroups: StateFlow<Map<Long, TdApi.Supergroup>> = _supergroups.asStateFlow()

    private val _basicGroups = MutableStateFlow<Map<Long, TdApi.BasicGroup>>(emptyMap())
    val basicGroups: StateFlow<Map<Long, TdApi.BasicGroup>> = _basicGroups.asStateFlow()

    private val _chatFolders = MutableStateFlow<List<TdApi.ChatFolderInfo>>(emptyList())
    val chatFolders: StateFlow<List<TdApi.ChatFolderInfo>> = _chatFolders.asStateFlow()

    private val _files = MutableStateFlow<Map<Int, TdApi.File>>(emptyMap())
    val files: StateFlow<Map<Int, TdApi.File>> = _files.asStateFlow()

    private val _customEmojiStickers = MutableStateFlow<Map<Long, TdApi.Sticker>>(emptyMap())
    val customEmojiStickers: StateFlow<Map<Long, TdApi.Sticker>> = _customEmojiStickers.asStateFlow()

    private val _userFullInfoMap = MutableStateFlow<Map<Long, TdApi.UserFullInfo>>(emptyMap())
    val userFullInfoMap: StateFlow<Map<Long, TdApi.UserFullInfo>> = _userFullInfoMap.asStateFlow()

    private val _chatActions = MutableStateFlow<Map<Long, ChatTypingState>>(emptyMap())
    val chatActions: StateFlow<Map<Long, ChatTypingState>> = _chatActions.asStateFlow()

    private val _activeStories = MutableStateFlow<Map<Long, TdApi.ChatActiveStories>>(emptyMap())
    val activeStories: StateFlow<Map<Long, TdApi.ChatActiveStories>> = _activeStories.asStateFlow()

    private val _loadingChatLists = MutableStateFlow<Set<String>>(emptySet())
    val loadingChatLists: StateFlow<Set<String>> = _loadingChatLists.asStateFlow()

    private val _exhaustedChatLists = MutableStateFlow<Set<String>>(emptySet())
    val exhaustedChatLists: StateFlow<Set<String>> = _exhaustedChatLists.asStateFlow()

    private val _recentChatMessages = MutableStateFlow<Map<Long, List<TdApi.Message>>>(emptyMap())
    val recentChatMessages: StateFlow<Map<Long, List<TdApi.Message>>> = _recentChatMessages.asStateFlow()

    private val _updates = MutableSharedFlow<TdApi.Update>(extraBufferCapacity = 256)
    val updates: SharedFlow<TdApi.Update> = _updates.asSharedFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        startClient()
    }

    private fun startClient() {
        runCatching { Client.execute(TdApi.SetLogVerbosityLevel(0)) }
        client = Client.create(
            { obj -> handleIncomingObject(obj) },
            { /* update exception */ },
            { /* default exception */ },
        )
        client?.send(TdApi.GetAuthorizationState()) { obj ->
            if (obj is TdApi.AuthorizationState) {
                handleAuthorizationState(obj)
            }
        }
    }

    private fun handleIncomingObject(obj: TdApi.Object) {
        when (obj) {
            is TdApi.UpdateAuthorizationState -> handleAuthorizationState(obj.authorizationState)
            is TdApi.UpdateConnectionState -> handleConnectionState(obj.state)
            is TdApi.UpdateNewChat -> {
                upsertChat(obj.chat)
            }
            is TdApi.UpdateChatTitle -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.title = obj.title
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatPhoto -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.photo = obj.photo
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
                downloadFileIfNeeded(obj.photo?.small, priority = 12)
            }
            is TdApi.UpdateChatLastMessage -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.lastMessage = obj.lastMessage
                    existing.chat.positions = obj.positions
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatPosition -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    val filtered = existing.chat.positions.filterNot {
                        it.list.constructor == obj.position.list.constructor &&
                            (it.list !is TdApi.ChatListFolder || obj.position.list !is TdApi.ChatListFolder ||
                                (it.list as TdApi.ChatListFolder).chatFolderId == (obj.position.list as TdApi.ChatListFolder).chatFolderId)
                    }
                    existing.chat.positions = (filtered + obj.position).toTypedArray()
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatReadInbox -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.lastReadInboxMessageId = obj.lastReadInboxMessageId
                    existing.chat.unreadCount = obj.unreadCount
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatReadOutbox -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.lastReadOutboxMessageId = obj.lastReadOutboxMessageId
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatUnreadMentionCount -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.unreadMentionCount = obj.unreadMentionCount
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatDraftMessage -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.draftMessage = obj.draftMessage
                    existing.chat.positions = obj.positions
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatNotificationSettings -> {
                _chats.update { map ->
                    val existing = map[obj.chatId] ?: return@update map
                    existing.chat.notificationSettings = obj.notificationSettings
                    map + (obj.chatId to existing.copy(version = versionCounter.incrementAndGet()))
                }
            }
            is TdApi.UpdateChatFolders -> {
                _chatFolders.value = obj.chatFolders.toList()
            }
            is TdApi.UpdateUser -> {
                val user = obj.user
                _users.update { it + (user.id to user) }
                if (_me.value?.id == user.id) {
                    _me.value = user
                    downloadFileIfNeeded(user.profilePhoto?.small, priority = 14)
                }
            }
            is TdApi.UpdateUserStatus -> {
                _users.update { map ->
                    val u = map[obj.userId] ?: return@update map
                    u.status = obj.status
                    map + (obj.userId to u)
                }
            }
            is TdApi.UpdateUserFullInfo -> {
                _userFullInfoMap.update { it + (obj.userId to obj.userFullInfo) }
            }
            is TdApi.UpdateSupergroup -> {
                _supergroups.update { it + (obj.supergroup.id to obj.supergroup) }
            }
            is TdApi.UpdateBasicGroup -> {
                _basicGroups.update { it + (obj.basicGroup.id to obj.basicGroup) }
            }
            is TdApi.UpdateFile -> {
                val file = obj.file
                _files.update { it + (file.id to file) }
                if (file.local.isDownloadingCompleted) {
                    requestedDownloads.remove(file.id)
                    val currentMe = _me.value
                    if (currentMe != null && currentMe.profilePhoto?.small?.id == file.id) {
                        accountStore.updateActiveUser(
                            userId = currentMe.id,
                            displayName = currentMe.displayName(),
                            phone = currentMe.phoneNumber,
                            avatarLocalPath = file.local.path,
                        )
                    }
                }
            }
            is TdApi.UpdateChatAction -> {
                val label = when (obj.action) {
                    is TdApi.ChatActionTyping -> "typing…"
                    is TdApi.ChatActionRecordingVoiceNote -> "recording voice…"
                    is TdApi.ChatActionUploadingVoiceNote -> "sending voice…"
                    is TdApi.ChatActionUploadingPhoto -> "sending photo…"
                    is TdApi.ChatActionUploadingVideo -> "sending video…"
                    is TdApi.ChatActionUploadingDocument -> "sending file…"
                    is TdApi.ChatActionChoosingSticker -> "choosing sticker…"
                    is TdApi.ChatActionCancel -> null
                    else -> null
                }
                if (label == null) {
                    _chatActions.update { it - obj.chatId }
                } else {
                    val expiry = System.currentTimeMillis() + 5500L
                    _chatActions.update { it + (obj.chatId to ChatTypingState(label, expiry)) }
                    scope.launch {
                        kotlinx.coroutines.delay(5600L)
                        _chatActions.update { current ->
                            val existing = current[obj.chatId]
                            if (existing != null && existing.expiresAtMs <= expiry) {
                                current - obj.chatId
                            } else {
                                current
                            }
                        }
                    }
                }
            }
            is TdApi.UpdateChatActiveStories -> {
                val active = obj.activeStories
                _activeStories.update { current ->
                    if (active.stories.isEmpty()) current - active.chatId
                    else current + (active.chatId to active)
                }
            }
            is TdApi.UpdateNewMessage -> {
                cacheRecentMessages(obj.message.chatId, listOf(obj.message))
            }
        }

        if (obj is TdApi.Update) {
            _updates.tryEmit(obj)
        }
    }

    private fun cacheRecentMessages(chatId: Long, incoming: List<TdApi.Message>) {
        if (incoming.isEmpty()) return
        _recentChatMessages.update { current ->
            val merged = (current[chatId].orEmpty() + incoming)
                .associateBy { it.id }
                .values
                .sortedByDescending { it.date }
                .take(20)
            current + (chatId to merged)
        }
    }

    private fun handleConnectionState(state: TdApi.ConnectionState) {
        _connectionStatus.value = when (state) {
            is TdApi.ConnectionStateConnecting,
            is TdApi.ConnectionStateConnectingToProxy -> ConnectionStatus.CONNECTING
            is TdApi.ConnectionStateUpdating -> ConnectionStatus.UPDATING
            is TdApi.ConnectionStateReady -> ConnectionStatus.READY
            is TdApi.ConnectionStateWaitingForNetwork -> ConnectionStatus.WAITING_FOR_NETWORK
            else -> ConnectionStatus.CONNECTING
        }
    }

    private fun handleAuthorizationState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                _authStage.value = AuthStage.Initializing
                sendTdlibParameters()
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authError.value = null
                _authStage.value = AuthStage.WaitPhoneNumber
            }
            is TdApi.AuthorizationStateWaitOtherDeviceConfirmation -> {
                _authError.value = null
                _authStage.value = AuthStage.WaitQrConfirmation(state.link)
            }
            is TdApi.AuthorizationStateWaitCode -> {
                _authError.value = null
                val info = state.codeInfo
                val (hint, len) = when (val t = info.type) {
                    is TdApi.AuthenticationCodeTypeTelegramMessage ->
                        "We've sent the code to your Telegram app on another device." to t.length
                    is TdApi.AuthenticationCodeTypeSms ->
                        "We've sent an SMS with an activation code to ${info.phoneNumber}." to t.length
                    is TdApi.AuthenticationCodeTypeCall ->
                        "We are calling ${info.phoneNumber} to dictate the code." to t.length
                    else -> "Enter the verification code sent to ${info.phoneNumber}." to 5
                }
                _authStage.value = AuthStage.WaitCode(
                    phoneNumber = info.phoneNumber,
                    deliveryHint = hint,
                    codeLength = len.coerceIn(4, 8),
                    timeoutSeconds = info.timeout,
                )
            }
            is TdApi.AuthorizationStateWaitPassword -> {
                _authError.value = null
                _authStage.value = AuthStage.WaitPassword(
                    passwordHint = state.passwordHint,
                    hasRecoveryEmail = state.hasRecoveryEmailAddress,
                    recoveryEmailPattern = state.recoveryEmailAddressPattern,
                )
            }
            is TdApi.AuthorizationStateWaitRegistration -> {
                _authError.value = null
                _authStage.value = AuthStage.WaitRegistration
            }
            is TdApi.AuthorizationStateReady -> {
                _authError.value = null
                _authStage.value = AuthStage.Ready
                onSessionReady()
            }
            is TdApi.AuthorizationStateClosing -> {
                _authStage.value = AuthStage.Initializing
            }
            is TdApi.AuthorizationStateClosed -> {
                _chats.value = emptyMap()
                _me.value = null
                _users.value = emptyMap()
                _supergroups.value = emptyMap()
                _basicGroups.value = emptyMap()
                _chatFolders.value = emptyList()
                _userFullInfoMap.value = emptyMap()
                _chatActions.value = emptyMap()
                _activeStories.value = emptyMap()
                _customEmojiStickers.value = emptyMap()
                _loadingChatLists.value = emptySet()
                _exhaustedChatLists.value = emptySet()
                loadingListsGuard.clear()
                requestedDownloads.clear()
                requestedCustomEmojis.clear()
                _authStage.value = AuthStage.Closed
                startClient()
            }
        }
    }

    private fun sendTdlibParameters() {
        val config = ApiConfigLoader.load(appContext)
        val activeAcc = accountStore.getActiveAccount()
        val dbDir = File(appContext.filesDir, activeAcc.dbDirName).apply { mkdirs() }.absolutePath
        val filesDir = File(appContext.filesDir, activeAcc.filesDirName).apply { mkdirs() }.absolutePath
        val params = TdApi.SetTdlibParameters(
            false,
            dbDir,
            filesDir,
            byteArrayOf(),
            true,
            true,
            true,
            true,
            config.apiId,
            config.apiHash,
            Locale.getDefault().language.ifBlank { "en" },
            Build.MODEL ?: "Android",
            Build.VERSION.RELEASE ?: "14",
            "1.0.0",
        )
        client?.send(params) { obj ->
            if (obj is TdApi.Error) {
                _authError.value = obj.message
            }
        }
    }

    private fun onSessionReady() {
        scope.launch {
            send(TdApi.GetMe()).onSuccess { user ->
                _me.value = user
                _users.update { it + (user.id to user) }
                downloadFileIfNeeded(user.profilePhoto?.small, priority = 16)
                accountStore.updateActiveUser(
                    userId = user.id,
                    displayName = user.displayName(),
                    phone = user.phoneNumber,
                    avatarLocalPath = resolveLocalFilePath(user.profilePhoto?.small),
                )
                val statusEmojiId = (user.emojiStatus?.type as? TdApi.EmojiStatusTypeCustomEmoji)?.customEmojiId
                if (statusEmojiId != null && statusEmojiId != 0L) {
                    loadCustomEmojis(listOf(statusEmojiId))
                }
            }
            loadMoreChats(TdApi.ChatListMain(), 30)
            loadMoreChats(TdApi.ChatListArchive(), 20)
            send(TdApi.LoadActiveStories(TdApi.StoryListMain()))
        }
    }

    fun switchToAccount(accountId: String) {
        if (accountId == accountStore.activeAccountId.value) return
        if (accountStore.setActiveAccount(accountId) != null) {
            _authStage.value = AuthStage.Initializing
            client?.send(TdApi.Close()) { }
        }
    }

    fun addNewAccount() {
        accountStore.createAndActivateNewAccount()
        _authStage.value = AuthStage.Initializing
        client?.send(TdApi.Close()) { }
    }

    fun cancelAddAccount() {
        val currentId = accountStore.activeAccountId.value
        val fallback = accountStore.accounts.value.firstOrNull { it.accountId != currentId && it.userId != 0L }
        if (fallback != null) {
            accountStore.removeAccount(currentId)
            _authStage.value = AuthStage.Initializing
            client?.send(TdApi.Close()) { }
        }
    }

    fun loadCustomEmojis(ids: List<Long>) {
        val missing = ids.distinct().filter { id ->
            id != 0L && !_customEmojiStickers.value.containsKey(id) &&
                requestedCustomEmojis.putIfAbsent(id, true) == null
        }
        if (missing.isEmpty()) return
        scope.launch {
            missing.chunked(100).forEach { chunk ->
                send(TdApi.GetCustomEmojiStickers(chunk.toLongArray())).onSuccess { stickers ->
                    val newMap = stickers.stickers.associateBy { it.id }
                    if (newMap.isNotEmpty()) {
                        _customEmojiStickers.update { it + newMap }
                        stickers.stickers.forEach { st ->
                            downloadFileIfNeeded(st.thumbnail?.file, priority = 18)
                            if (st.format is TdApi.StickerFormatWebp) {
                                downloadFileIfNeeded(st.sticker, priority = 18)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun upsertChat(chat: TdApi.Chat) {
        _chats.update { map ->
            map + (chat.id to ChatEntry(chat, versionCounter.incrementAndGet()))
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    suspend fun <T : TdApi.Object> send(function: TdApi.Function<T>): Result<T> =
        suspendCancellableCoroutine { cont ->
            val currentClient = client
            if (currentClient == null) {
                cont.resume(Result.failure(IllegalStateException("TDLib client not ready")))
                return@suspendCancellableCoroutine
            }
            currentClient.send(function) { obj ->
                if (!cont.isActive) return@send
                when (obj) {
                    is TdApi.Error -> cont.resume(Result.failure(IllegalStateException("${obj.code}: ${obj.message}")))
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        cont.resume(Result.success(obj as T))
                    }
                }
            }
        }

    fun downloadFileIfNeeded(file: TdApi.File?, priority: Int = 16) {
        if (file == null) return
        val latest = _files.value[file.id] ?: file
        if (latest.local.isDownloadingCompleted && latest.local.path.isNotBlank() && File(latest.local.path).exists()) {
            return
        }
        if (latest.local.isDownloadingActive || requestedDownloads.putIfAbsent(file.id, true) != null) {
            return
        }
        client?.send(TdApi.DownloadFile(file.id, priority, 0L, 0L, false)) { result ->
            if (result is TdApi.File) {
                _files.update { it + (result.id to result) }
            } else {
                requestedDownloads.remove(file.id)
            }
        }
    }

    fun resolveLocalFilePath(file: TdApi.File?): String? {
        if (file == null) return null
        val latest = _files.value[file.id] ?: file
        val path = latest.local.path
        return if (latest.local.isDownloadingCompleted && path.isNotBlank() && File(path).exists()) path else null
    }

    suspend fun sendPhoneNumber(phone: String): Result<TdApi.Ok> {
        val settings = TdApi.PhoneNumberAuthenticationSettings(
            false, false, false, false, false, null, emptyArray()
        )
        return send(TdApi.SetAuthenticationPhoneNumber(phone, settings)).onFailure {
            _authError.value = it.message?.substringAfter(": ") ?: "Failed to send phone number"
        }
    }

    suspend fun checkAuthCode(code: String): Result<TdApi.Ok> {
        return send(TdApi.CheckAuthenticationCode(code)).onFailure {
            _authError.value = it.message?.substringAfter(": ") ?: "Invalid verification code"
        }
    }

    suspend fun resendAuthCode(): Result<TdApi.Ok> {
        return send(TdApi.ResendAuthenticationCode(null)).onFailure {
            _authError.value = it.message?.substringAfter(": ")
        }
    }

    suspend fun checkAuthPassword(password: String): Result<TdApi.Ok> {
        return send(TdApi.CheckAuthenticationPassword(password)).onFailure {
            _authError.value = it.message?.substringAfter(": ") ?: "Incorrect password"
        }
    }

    suspend fun registerUser(firstName: String, lastName: String): Result<TdApi.Ok> {
        return send(TdApi.RegisterUser(firstName, lastName, false)).onFailure {
            _authError.value = it.message?.substringAfter(": ")
        }
    }

    suspend fun requestQrAuth(): Result<TdApi.Ok> {
        return send(TdApi.RequestQrCodeAuthentication(longArrayOf())).onFailure {
            _authError.value = it.message?.substringAfter(": ")
        }
    }

    suspend fun backToPhoneLogin() {
        send(TdApi.LogOut())
    }

    suspend fun loadMoreChats(chatList: TdApi.ChatList = TdApi.ChatListMain(), limit: Int = 30) {
        val key = chatListKey(chatList)
        if (_exhaustedChatLists.value.contains(key)) return
        if (loadingListsGuard.putIfAbsent(key, true) != null) return
        _loadingChatLists.update { it + key }
        try {
            val res = send(TdApi.LoadChats(chatList, limit))
            res.onFailure { err ->
                val msg = err.message.orEmpty()
                if (msg.startsWith("404:") || msg.contains("Not Found", ignoreCase = true)) {
                    _exhaustedChatLists.update { it + key }
                }
            }
        } finally {
            loadingListsGuard.remove(key)
            _loadingChatLists.update { it - key }
        }
    }

    suspend fun getChat(chatId: Long): Result<TdApi.Chat> {
        return send(TdApi.GetChat(chatId)).onSuccess { upsertChat(it) }
    }

    suspend fun openChat(chatId: Long) {
        send(TdApi.OpenChat(chatId))
    }

    suspend fun closeChat(chatId: Long) {
        send(TdApi.CloseChat(chatId))
    }

    suspend fun getChatHistory(chatId: Long, fromMessageId: Long = 0L, limit: Int = 50): Result<List<TdApi.Message>> {
        return send(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)).map {
            val list = it.messages.toList()
            cacheRecentMessages(chatId, list)
            list
        }
    }

    suspend fun getChatPinnedMessage(chatId: Long): Result<TdApi.Message> {
        return send(TdApi.GetChatPinnedMessage(chatId))
    }

    suspend fun viewMessages(chatId: Long, messageIds: LongArray) {
        if (messageIds.isEmpty()) return
        send(TdApi.ViewMessages(chatId, messageIds, null, true))
    }

    suspend fun sendTextMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Long? = null,
    ): Result<TdApi.Message> {
        val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
        val content = TdApi.InputMessageText(
            TdApi.FormattedText(text, emptyArray()),
            TdApi.LinkPreviewOptions(false, "", false, false, false),
            true,
        )
        return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
    }

    suspend fun sendPhotoMessage(
        chatId: Long,
        localPath: String,
        caption: String = "",
        replyToMessageId: Long? = null,
    ): Result<TdApi.Message> {
        val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
        val content = TdApi.InputMessagePhoto(
            TdApi.InputFileLocal(localPath),
            null,
            intArrayOf(),
            0,
            0,
            TdApi.FormattedText(caption, emptyArray()),
            false,
            null,
            false,
        )
        return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
    }

    suspend fun sendDocumentMessage(
        chatId: Long,
        localPath: String,
        caption: String = "",
        replyToMessageId: Long? = null,
    ): Result<TdApi.Message> {
        val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
        val content = TdApi.InputMessageDocument(
            TdApi.InputFileLocal(localPath),
            null,
            false,
            TdApi.FormattedText(caption, emptyArray()),
        )
        return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
    }

    suspend fun sendVoiceNoteMessage(
        chatId: Long,
        localPath: String,
        durationSeconds: Int,
        replyToMessageId: Long? = null,
        waveform: ByteArray = byteArrayOf(),
    ): Result<TdApi.Message> {
        val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
        val content = TdApi.InputMessageVoiceNote(
            TdApi.InputFileLocal(localPath),
            durationSeconds,
            waveform,
            TdApi.FormattedText("", emptyArray()),
            null,
        )
        return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
    }

    suspend fun editMessageText(chatId: Long, messageId: Long, newText: String): Result<TdApi.Message> {
        val content = TdApi.InputMessageText(
            TdApi.FormattedText(newText, emptyArray()),
            TdApi.LinkPreviewOptions(false, "", false, false, false),
            false,
        )
        return send(TdApi.EditMessageText(chatId, messageId, null, content))
    }

    suspend fun deleteMessages(chatId: Long, messageIds: LongArray, revokeForAll: Boolean): Result<TdApi.Ok> {
        return send(TdApi.DeleteMessages(chatId, messageIds, revokeForAll))
    }

    suspend fun forwardMessages(toChatId: Long, fromChatId: Long, messageIds: LongArray): Result<TdApi.Messages> {
        return send(TdApi.ForwardMessages(toChatId, null, fromChatId, messageIds, null, false, false))
    }

    suspend fun toggleReaction(chatId: Long, messageId: Long, emoji: String, alreadyChosen: Boolean) {
        val reaction = TdApi.ReactionTypeEmoji(emoji)
        if (alreadyChosen) {
            send(TdApi.RemoveMessageReaction(chatId, messageId, reaction))
        } else {
            send(TdApi.AddMessageReaction(chatId, messageId, reaction, false, true))
        }
    }

    suspend fun pinMessage(chatId: Long, messageId: Long): Result<TdApi.Ok> {
        return send(TdApi.PinChatMessage(chatId, messageId, false, false))
    }

    suspend fun unpinMessage(chatId: Long, messageId: Long): Result<TdApi.Ok> {
        return send(TdApi.UnpinChatMessage(chatId, messageId))
    }

    fun sendTypingAction(chatId: Long, isRecording: Boolean = false) {
        val action: TdApi.ChatAction = if (isRecording) TdApi.ChatActionRecordingVoiceNote() else TdApi.ChatActionTyping()
        client?.send(TdApi.SendChatAction(chatId, null, "", action)) { }
    }

    fun saveDraft(chatId: Long, text: String) {
        val draft = if (text.isBlank()) {
            null
        } else {
            TdApi.DraftMessage(
                null,
                (System.currentTimeMillis() / 1000L).toInt(),
                TdApi.InputMessageText(
                    TdApi.FormattedText(text, emptyArray()),
                    TdApi.LinkPreviewOptions(false, "", false, false, false),
                    false,
                ),
                0L,
                null,
            )
        }
        client?.send(TdApi.SetChatDraftMessage(chatId, null, draft)) { }
    }

    suspend fun togglePinChat(chatId: Long, chatList: TdApi.ChatList, pinned: Boolean): Result<TdApi.Ok> {
        return send(TdApi.ToggleChatIsPinned(chatList, chatId, pinned))
    }

    suspend fun setChatArchived(chatId: Long, archive: Boolean): Result<TdApi.Ok> {
        val list = if (archive) TdApi.ChatListArchive() else TdApi.ChatListMain()
        return send(TdApi.AddChatToList(chatId, list))
    }

    suspend fun toggleMuteChat(chat: TdApi.Chat): Result<TdApi.Ok> {
        val current = chat.notificationSettings
        val newMuteFor = if (current.muteFor > 0) 0 else Int.MAX_VALUE
        val updated = TdApi.ChatNotificationSettings(
            false,
            newMuteFor,
            current.useDefaultSound,
            current.soundId,
            current.useDefaultShowPreview,
            current.showPreview,
            current.useDefaultMuteStories,
            current.muteStories,
            current.useDefaultStorySound,
            current.storySoundId,
            current.useDefaultShowStoryPoster,
            current.showStoryPoster,
            current.useDefaultDisablePinnedMessageNotifications,
            current.disablePinnedMessageNotifications,
            current.useDefaultDisableMentionNotifications,
            current.disableMentionNotifications,
        )
        return send(TdApi.SetChatNotificationSettings(chat.id, updated))
    }

    suspend fun toggleMarkAsUnread(chatId: Long, unread: Boolean): Result<TdApi.Ok> {
        return send(TdApi.ToggleChatIsMarkedAsUnread(chatId, unread))
    }

    suspend fun deleteChatHistory(chatId: Long, removeFromList: Boolean, revoke: Boolean): Result<TdApi.Ok> {
        return send(TdApi.DeleteChatHistory(chatId, removeFromList, revoke))
    }

    suspend fun joinChat(chatId: Long): Result<TdApi.Ok> = send(TdApi.JoinChat(chatId))
    suspend fun leaveChat(chatId: Long): Result<TdApi.Ok> = send(TdApi.LeaveChat(chatId))

    suspend fun searchLocalChats(query: String): List<TdApi.Chat> {
        val ids = send(TdApi.SearchChats(query, 25)).getOrNull()?.chatIds ?: return emptyList()
        return ids.toList().mapNotNull { id -> _chats.value[id]?.chat ?: getChat(id).getOrNull() }
    }

    suspend fun searchPublicChats(query: String): List<TdApi.Chat> {
        val ids = send(TdApi.SearchPublicChats(query)).getOrNull()?.chatIds ?: return emptyList()
        return ids.toList().mapNotNull { id -> _chats.value[id]?.chat ?: getChat(id).getOrNull() }
    }

    suspend fun getContacts(): List<TdApi.User> {
        val userIds = send(TdApi.GetContacts()).getOrNull()?.userIds ?: return emptyList()
        return userIds.toList().mapNotNull { id ->
            _users.value[id] ?: send(TdApi.GetUser(id)).getOrNull()?.also { u ->
                _users.update { it + (u.id to u) }
            }
        }
    }

    suspend fun createPrivateChat(userId: Long): Result<TdApi.Chat> {
        return send(TdApi.CreatePrivateChat(userId, false)).onSuccess { upsertChat(it) }
    }

    suspend fun createSecretChat(userId: Long): Result<TdApi.Chat> {
        return send(TdApi.CreateNewSecretChat(userId)).onSuccess { upsertChat(it) }
    }

    suspend fun createBasicGroup(title: String, userIds: LongArray): Result<TdApi.Chat> {
        val created = send(TdApi.CreateNewBasicGroupChat(userIds, title, 0)).getOrElse {
            return Result.failure(it)
        }
        return getChat(created.chatId)
    }

    suspend fun createChannel(title: String, description: String): Result<TdApi.Chat> {
        return send(TdApi.CreateNewSupergroupChat(title, false, true, description, null, 0, false)).onSuccess {
            upsertChat(it)
        }
    }

    suspend fun getUserFullInfo(userId: Long): Result<TdApi.UserFullInfo> =
        send(TdApi.GetUserFullInfo(userId)).onSuccess { info ->
            _userFullInfoMap.update { it + (userId to info) }
        }

    suspend fun getSupergroup(supergroupId: Long): Result<TdApi.Supergroup> = send(TdApi.GetSupergroup(supergroupId))

    suspend fun getSupergroupFullInfo(supergroupId: Long): Result<TdApi.SupergroupFullInfo> =
        send(TdApi.GetSupergroupFullInfo(supergroupId))

    suspend fun getSupergroupMembers(supergroupId: Long): Result<List<TdApi.ChatMember>> {
        return send(TdApi.GetSupergroupMembers(supergroupId, null, 0, 50)).map { it.members.toList() }
    }

    suspend fun setMyName(firstName: String, lastName: String): Result<TdApi.Ok> =
        send(TdApi.SetName(firstName, lastName))

    suspend fun setMyBio(bio: String): Result<TdApi.Ok> =
        send(TdApi.SetBio(bio))

    suspend fun getActiveSessions(): Result<List<TdApi.Session>> {
        return send(TdApi.GetActiveSessions()).map { it.sessions.toList() }
    }

    suspend fun terminateSession(sessionId: Long): Result<TdApi.Ok> =
        send(TdApi.TerminateSession(sessionId))

    suspend fun logOut(): Result<TdApi.Ok> = send(TdApi.LogOut())

    fun getCacheSizeBytes(): Long {
        fun dirSize(f: File): Long = if (!f.exists()) 0L else if (f.isFile) f.length() else f.listFiles()?.sumOf { dirSize(it) } ?: 0L
        val activeAcc = accountStore.getActiveAccount()
        return dirSize(File(appContext.filesDir, activeAcc.filesDirName)) + dirSize(appContext.cacheDir)
    }

    fun clearLocalMediaCache() {
        appContext.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    companion object {
        @Volatile
        private var instance: TelegramClientManager? = null

        fun getInstance(context: Context): TelegramClientManager {
            return instance ?: synchronized(this) {
                instance ?: TelegramClientManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
