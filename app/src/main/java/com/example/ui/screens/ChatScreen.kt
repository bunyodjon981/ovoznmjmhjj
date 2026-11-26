package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.WebAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import com.example.audio.VoiceAudioController
import com.example.data.LocalStrings
import com.example.data.ghostModeActiveBadge
import com.example.data.recordingTooShortHint
import com.example.data.spamRestrictedBanner
import com.example.data.spamRestrictedDialogBody
import com.example.data.spamRestrictedTitle
import com.example.tdlib.ChatEntry
import com.example.tdlib.ChatTypingState
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.copyUriToCacheFile
import com.example.tdlib.fetchWebAppUrl
import com.example.tdlib.formatDateSeparator
import com.example.tdlib.formatDuration
import com.example.tdlib.formatStatus
import com.example.tdlib.isMuted
import com.example.tdlib.openTelegramWebApp
import com.example.tdlib.orderFor
import com.example.tdlib.pressCallbackButton
import com.example.tdlib.previewText
import com.example.tdlib.sendAudioMessage
import com.example.tdlib.sendBotStart
import com.example.tdlib.sendPhotoAlbum
import com.example.tdlib.sendPollMessage
import com.example.tdlib.sendVideoNoteMessage
import com.example.tdlib.votePoll
import com.example.ui.components.CustomEmojiPackSheet
import com.example.ui.components.TelegramAvatar
import com.example.ui.components.TelegramWallpaperBackground
import com.example.ui.components.VideoNoteCameraOverlay
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

private val QuickEmojiSet = listOf(
    "😀", "😂", "🥹", "😍", "🤩", "😎", "🥳", "🤔",
    "👍", "👎", "❤️", "🔥", "🎉", "🙏", "👏", "💯",
    "🤝", "✨", "🚀", "👀", "😢", "😡", "🤯", "🫡",
)

private data class ActiveWebAppSession(
    val title: String,
    val subtitle: String,
    val url: String,
)

private sealed interface ChatTimelineItem {
    val primaryMessage: TdApi.Message
    val key: Long

    data class Single(val message: TdApi.Message) : ChatTimelineItem {
        override val primaryMessage: TdApi.Message get() = message
        override val key: Long get() = message.id
    }

    data class Album(val albumId: Long, val messages: List<TdApi.Message>) : ChatTimelineItem {
        override val primaryMessage: TdApi.Message get() = messages.first()
        override val key: Long get() = -albumId
    }
}

private fun groupMessagesIntoTimeline(messagesDesc: List<TdApi.Message>): List<ChatTimelineItem> {
    if (messagesDesc.isEmpty()) return emptyList()
    val result = ArrayList<ChatTimelineItem>(messagesDesc.size)
    var i = 0
    while (i < messagesDesc.size) {
        val current = messagesDesc[i]
        val albumId = current.mediaAlbumId
        if (albumId != 0L) {
            val group = mutableListOf(current)
            var j = i + 1
            while (j < messagesDesc.size && messagesDesc[j].mediaAlbumId == albumId) {
                group.add(messagesDesc[j])
                j++
            }
            if (group.size > 1) {
                result.add(ChatTimelineItem.Album(albumId, group.sortedBy { it.id }))
            } else {
                result.add(ChatTimelineItem.Single(current))
            }
            i = j
        } else {
            result.add(ChatTimelineItem.Single(current))
            i++
        }
    }
    return result
}

private fun extractCustomEmojiIdsFromMessages(messages: List<TdApi.Message>): List<Long> {
    val ids = LinkedHashSet<Long>()
    fun collect(ft: TdApi.FormattedText?) {
        ft?.entities?.forEach { entity ->
            val type = entity.type
            if (type is TdApi.TextEntityTypeCustomEmoji) {
                ids.add(type.customEmojiId)
            }
        }
    }
    messages.forEach { msg ->
        when (val c = msg.content) {
            is TdApi.MessageText -> collect(c.text)
            is TdApi.MessagePhoto -> collect(c.caption)
            is TdApi.MessageVideo -> collect(c.caption)
            is TdApi.MessageDocument -> collect(c.caption)
            is TdApi.MessageAnimation -> collect(c.caption)
            is TdApi.MessagePoll -> {
                collect(c.poll.question)
                c.poll.options.forEach { collect(it.text) }
            }
        }
    }
    return ids.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Long,
    me: TdApi.User?,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    basicGroupsMap: Map<Long, TdApi.BasicGroup>,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    userFullInfoMap: Map<Long, TdApi.UserFullInfo>,
    chatActions: Map<Long, ChatTypingState>,
    clientManager: TelegramClientManager,
    isSpamRestricted: Boolean = false,
    spamUntilMs: Long = 0L,
    onBack: () -> Unit,
    onOpenProfileInfo: (Long) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val voiceController = remember { VoiceAudioController(context) }
    val playbackState by voiceController.playbackState.collectAsState()
    val recordingState by voiceController.recordingState.collectAsState()

    DisposableEffect(chatId) {
        onDispose {
            voiceController.release()
        }
    }

    DisposableEffect(chatId, appearance.ghostModeEnabled) {
        val stealth = appearance.ghostModeEnabled
        if (!stealth) {
            scope.launch { clientManager.openChat(chatId) }
        }
        onDispose {
            if (!stealth) {
                scope.launch { clientManager.closeChat(chatId) }
            }
        }
    }

    val chat = chatsMap[chatId]?.chat
    val rawChatsMap = remember(chatsMap) { chatsMap.mapValues { it.value.chat } }
    val chatType = chat?.type
    val peerUser = (chatType as? TdApi.ChatTypePrivate)?.let { usersMap[it.userId] }
    val peerFullInfo = peerUser?.let { userFullInfoMap[it.id] }
    val botInfo = peerFullInfo?.botInfo
    val isBot = peerUser?.type is TdApi.UserTypeBot
    val supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] }
    val basicGroup = (chatType as? TdApi.ChatTypeBasicGroup)?.let { basicGroupsMap[it.basicGroupId] }
    val isSavedMessages = chatType is TdApi.ChatTypePrivate && me?.id == chatType.userId
    val blockForSpam = isSpamRestricted && !isSavedMessages
    val isChannel = (chatType as? TdApi.ChatTypeSupergroup)?.isChannel == true
    val isGroup = chatType is TdApi.ChatTypeBasicGroup || (chatType is TdApi.ChatTypeSupergroup && !chatType.isChannel)
    val canPost = !isChannel || (supergroup?.status is TdApi.ChatMemberStatusCreator || supergroup?.status is TdApi.ChatMemberStatusAdministrator)

    var messages by remember(chatId) { mutableStateOf<List<TdApi.Message>>(emptyList()) }
    var isLoadingHistory by remember(chatId) { mutableStateOf(true) }
    var pinnedMessage by remember(chatId) { mutableStateOf<TdApi.Message?>(null) }

    val initialDraft = (chat?.draftMessage?.inputMessageText as? TdApi.InputMessageText)?.text?.text.orEmpty()
    var messageInput by remember(chatId) { mutableStateOf(initialDraft) }
    var replyTarget by remember(chatId) { mutableStateOf<TdApi.Message?>(null) }
    var editingMessage by remember(chatId) { mutableStateOf<TdApi.Message?>(null) }
    var selectedMessage by remember { mutableStateOf<TdApi.Message?>(null) }
    var messageToDelete by remember { mutableStateOf<TdApi.Message?>(null) }
    var messageToForward by remember { mutableStateOf<TdApi.Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCreatePollDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showEmojiTray by remember { mutableStateOf(false) }
    var showReplyKeyboard by remember(chatId) { mutableStateOf(false) }
    var showBotCommandsSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var fullscreenImagePath by remember { mutableStateOf<String?>(null) }
    var inspectedCustomEmoji by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var activeWebApp by remember { mutableStateOf<ActiveWebAppSession?>(null) }
    var callbackAlertText by remember { mutableStateOf<String?>(null) }
    var pendingCallbackKey by remember { mutableStateOf<String?>(null) }
    var lastTypingSentMs by remember { mutableStateOf(0L) }
    var showSpamAlert by remember { mutableStateOf(false) }

    // Voice & Circular Video Note Mode State
    var isVideoNoteMode by remember { mutableStateOf(false) }
    var showModeTooltip by remember { mutableStateOf(false) }
    var isVideoNoteRecording by remember { mutableStateOf(false) }
    var isRecordingLocked by remember { mutableStateOf(false) }
    var videoFinalizeSignal by remember { mutableIntStateOf(0) }
    var videoCancelSignal by remember { mutableIntStateOf(0) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(showModeTooltip) {
        if (showModeTooltip) {
            delay(2600L)
            showModeTooltip = false
        }
    }

    val timelineItems = remember(messages) { groupMessagesIntoTimeline(messages) }

    LaunchedEffect(messages) {
        val ids = extractCustomEmojiIdsFromMessages(messages)
        if (ids.isNotEmpty()) {
            clientManager.loadCustomEmojis(ids)
        }
    }

    val activeReplyKeyboard = remember(messages) {
        messages.firstOrNull { it.replyMarkup is TdApi.ReplyMarkupShowKeyboard }?.replyMarkup as? TdApi.ReplyMarkupShowKeyboard
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10),
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (blockForSpam) {
                showSpamAlert = true
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                val cachedPaths = uris.mapIndexedNotNull { idx, uri ->
                    copyUriToCacheFile(context, uri, "photo_${System.currentTimeMillis()}_$idx.jpg")?.absolutePath
                }
                if (cachedPaths.size == 1) {
                    clientManager.sendPhotoMessage(chatId, cachedPaths.first(), replyToMessageId = replyTarget?.id)
                    replyTarget = null
                } else if (cachedPaths.size > 1) {
                    clientManager.sendPhotoAlbum(chatId, cachedPaths, replyToMessageId = replyTarget?.id)
                    replyTarget = null
                }
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            if (blockForSpam) {
                showSpamAlert = true
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                val cached = copyUriToCacheFile(context, uri, "attachment_${System.currentTimeMillis()}.bin")
                if (cached != null) {
                    clientManager.sendDocumentMessage(chatId, cached.absolutePath, replyToMessageId = replyTarget?.id)
                    replyTarget = null
                }
            }
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            if (blockForSpam) {
                showSpamAlert = true
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                val cached = copyUriToCacheFile(context, uri, "audio_${System.currentTimeMillis()}.mp3")
                if (cached != null) {
                    clientManager.sendAudioMessage(chatId, cached.absolutePath, replyToMessageId = replyTarget?.id)
                    replyTarget = null
                }
            }
        }
    }

    val recordPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val micGranted = results[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val camGranted = results[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (isVideoNoteMode && (!micGranted || !camGranted)) {
            scope.launch { snackbarHostState.showSnackbar(strings.cameraPermissionRequired) }
        } else if (!micGranted) {
            scope.launch { snackbarHostState.showSnackbar(strings.micPermissionRequired) }
        }
    }

    fun launchWebApp(botUserId: Long, rawUrl: String, title: String) {
        scope.launch {
            if (!appearance.inAppBrowser && rawUrl.startsWith("http")) {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl))) }
                return@launch
            }
            val sub = peerUser?.usernames?.activeUsernames?.firstOrNull()?.let { "@$it" } ?: "bot"
            clientManager.openTelegramWebApp(chatId, botUserId, rawUrl, tgColors)
                .onSuccess { info -> activeWebApp = ActiveWebAppSession(title, sub, info.url) }
                .onFailure {
                    clientManager.fetchWebAppUrl(botUserId, rawUrl, tgColors)
                        .onSuccess { resolved -> activeWebApp = ActiveWebAppSession(title, sub, resolved) }
                        .onFailure {
                            if (rawUrl.startsWith("http")) {
                                activeWebApp = ActiveWebAppSession(title, sub, rawUrl)
                            } else {
                                snackbarHostState.showSnackbar(it.message ?: "WebApp error")
                            }
                        }
                }
        }
    }

    fun sendCurrentText() {
        val text = messageInput.trim()
        if (text.isEmpty()) return
        if (blockForSpam) {
            showSpamAlert = true
            return
        }
        val editMsg = editingMessage
        val replyId = replyTarget?.id
        messageInput = ""
        editingMessage = null
        replyTarget = null
        scope.launch {
            if (editMsg != null) {
                clientManager.editMessageText(chatId, editMsg.id, text)
            } else {
                clientManager.sendTextMessage(chatId, text, replyId)
            }
        }
    }

    LaunchedEffect(chatId) {
        isLoadingHistory = true
        if (chat == null) clientManager.getChat(chatId)
        if (chatType is TdApi.ChatTypePrivate) {
            clientManager.getUserFullInfo(chatType.userId)
        } else if (chatType is TdApi.ChatTypeSupergroup && supergroup == null) {
            clientManager.getSupergroup(chatType.supergroupId)
        }
        clientManager.getChatHistory(chatId, 0L, 50).onSuccess { list ->
            var combined = list
            if (list.isNotEmpty() && list.size < 15) {
                clientManager.getChatHistory(chatId, list.last().id, 50).onSuccess { older ->
                    if (older.isNotEmpty()) {
                        combined = (list + older).distinctBy { it.id }.sortedByDescending { it.date }
                    }
                }
            }
            messages = combined
            if (combined.isNotEmpty() && !appearance.ghostModeEnabled) {
                clientManager.viewMessages(chatId, combined.take(20).map { it.id }.toLongArray())
            }
        }
        clientManager.getChatPinnedMessage(chatId).onSuccess { pinnedMessage = it }
        isLoadingHistory = false
    }

    LaunchedEffect(chatId, appearance.ghostModeEnabled, messages.size) {
        if (!appearance.ghostModeEnabled && messages.isNotEmpty()) {
            clientManager.viewMessages(chatId, messages.take(30).map { it.id }.toLongArray())
        }
    }

    LaunchedEffect(chatId) {
        clientManager.updates.collect { update ->
            when (update) {
                is TdApi.UpdateNewMessage -> {
                    if (update.message.chatId == chatId) {
                        messages = (listOf(update.message) + messages.filterNot { it.id == update.message.id })
                            .sortedByDescending { it.date }
                        if (!appearance.ghostModeEnabled) {
                            clientManager.viewMessages(chatId, longArrayOf(update.message.id))
                        }
                    }
                }
                is TdApi.UpdateMessageSendSucceeded -> {
                    if (update.message.chatId == chatId) {
                        messages = (listOf(update.message) + messages.filterNot {
                            it.id == update.oldMessageId || it.id == update.message.id
                        }).sortedByDescending { it.date }
                    }
                }
                is TdApi.UpdateMessageSendFailed -> {
                    if (update.message.chatId == chatId) {
                        messages = (listOf(update.message) + messages.filterNot {
                            it.id == update.oldMessageId || it.id == update.message.id
                        }).sortedByDescending { it.date }
                        snackbarHostState.showSnackbar("Failed: ${update.error.message}")
                    }
                }
                is TdApi.UpdateMessageContent -> {
                    if (update.chatId == chatId) {
                        messages = messages.map { m -> if (m.id == update.messageId) { m.content = update.newContent; m } else m }
                    }
                }
                is TdApi.UpdateMessageEdited -> {
                    if (update.chatId == chatId) {
                        messages = messages.map { m -> if (m.id == update.messageId) { m.editDate = update.editDate; m } else m }
                    }
                }
                is TdApi.UpdateMessageInteractionInfo -> {
                    if (update.chatId == chatId) {
                        messages = messages.map { m -> if (m.id == update.messageId) { m.interactionInfo = update.interactionInfo; m } else m }
                    }
                }
                is TdApi.UpdateDeleteMessages -> {
                    if (update.chatId == chatId && update.isPermanent) {
                        val deleted = update.messageIds.toSet()
                        messages = messages.filterNot { it.id in deleted }
                    }
                }
            }
        }
    }

    val activeTyping = chatActions[chatId]?.takeIf { it.expiresAtMs > System.currentTimeMillis() }?.label
    val subtitle = when {
        activeTyping != null -> activeTyping
        isSavedMessages -> strings.cloudStorage
        isBot -> "bot"
        peerUser != null -> peerUser.status.formatStatus(strings)
        supergroup != null -> {
            if (isChannel) String.format(strings.subscribersCount, supergroup.memberCount)
            else String.format(strings.membersCount, supergroup.memberCount)
        }
        basicGroup != null -> String.format(strings.membersCount, basicGroup.memberCount)
        else -> ""
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.cardBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                    actionIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (editingMessage == null) clientManager.saveDraft(chatId, messageInput)
                            onBack()
                        },
                        modifier = Modifier.testTag("chat_back_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProfileInfo(chatId) }
                            .testTag("chat_header_profile_button"),
                    ) {
                        TelegramAvatar(
                            id = chatId,
                            title = if (isSavedMessages) strings.savedMessages else (chat?.title ?: "Chat"),
                            photoFile = chat?.photo?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 40.dp,
                            isOnline = peerUser?.status is TdApi.UserStatusOnline,
                            isSavedMessages = isSavedMessages,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSavedMessages) strings.savedMessages else (chat?.title ?: "Telegram"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (activeTyping != null) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.infoAndMedia) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenProfileInfo(chatId)
                                },
                            )
                            if (chat != null) {
                                DropdownMenuItem(
                                    text = { Text(if (chat.isMuted()) strings.unmute else strings.mute) },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch { clientManager.toggleMuteChat(chat) }
                                    },
                                )
                            }
                            if (isBot && peerUser != null) {
                                DropdownMenuItem(
                                    text = { Text(strings.botStartButton) },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch { clientManager.sendBotStart(peerUser.id, chatId) }
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(strings.clearHistory) },
                                onClick = {
                                    showOverflowMenu = false
                                    scope.launch {
                                        clientManager.deleteChatHistory(chatId, removeFromList = false, revoke = false)
                                        messages = emptyList()
                                    }
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        TelegramWallpaperBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                pinnedMessage?.let { pinned ->
                    Surface(
                        color = tgColors.cardBackground.copy(alpha = 0.94f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val idx = messages.indexOfFirst { it.id == pinned.id }
                                if (idx >= 0) scope.launch { listState.animateScrollToItem(idx) }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.PushPin, null, tint = tgColors.accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.pinnedMessage, style = MaterialTheme.typography.labelSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                                Text(pinned.content.previewText(), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        clientManager.unpinMessage(chatId, pinned.id)
                                        pinnedMessage = null
                                    }
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Rounded.Close, "Unpin", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = appearance.ghostModeEnabled) {
                    Surface(
                        color = tgColors.accent.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = strings.ghostModeActiveBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = tgColors.accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (isLoadingHistory && messages.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (messages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                            ) {
                                Text(strings.noMessagesYet, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            reverseLayout = true,
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(items = timelineItems, key = { _, item -> item.key }) { index, item ->
                                val msg = item.primaryMessage
                                val nextOlder = timelineItems.getOrNull(index + 1)?.primaryMessage
                                val currentDay = formatDateSeparator(msg.date, strings)
                                val olderDay = nextOlder?.let { formatDateSeparator(it.date, strings) }

                                val handleInlineButton: (TdApi.Message, TdApi.InlineKeyboardButton, String) -> Unit = { targetMsg, btn, btnKey ->
                                    when (val t = btn.type) {
                                        is TdApi.InlineKeyboardButtonTypeCallback -> {
                                            pendingCallbackKey = btnKey
                                            scope.launch {
                                                clientManager.pressCallbackButton(chatId, targetMsg.id, t.data)
                                                    .onSuccess { ans ->
                                                        if (ans.url.isNotBlank()) {
                                                            val botId = peerUser?.id ?: 0L
                                                            if (botId != 0L) launchWebApp(botId, ans.url, btn.text)
                                                        } else if (ans.text.isNotBlank()) {
                                                            if (ans.showAlert) callbackAlertText = ans.text
                                                            else snackbarHostState.showSnackbar(ans.text)
                                                        }
                                                    }
                                                    .onFailure {
                                                        snackbarHostState.showSnackbar(it.message ?: "Error")
                                                    }
                                                pendingCallbackKey = null
                                            }
                                        }
                                        is TdApi.InlineKeyboardButtonTypeUrl -> {
                                            if (appearance.inAppBrowser && (t.url.startsWith("http://") || t.url.startsWith("https://"))) {
                                                activeWebApp = ActiveWebAppSession(btn.text, t.url.substringAfter("://").substringBefore("/"), t.url)
                                            } else {
                                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(t.url))) }
                                            }
                                        }
                                        is TdApi.InlineKeyboardButtonTypeWebApp -> {
                                            val botId = peerUser?.id ?: 0L
                                            if (botId != 0L) launchWebApp(botId, t.url, btn.text)
                                            else activeWebApp = ActiveWebAppSession(btn.text, "", t.url)
                                        }
                                        is TdApi.InlineKeyboardButtonTypeSwitchInline -> {
                                            messageInput = "@${peerUser?.usernames?.activeUsernames?.firstOrNull().orEmpty()} ${t.query}"
                                        }
                                    }
                                }

                                val openDoc: (String, String) -> Unit = { path, mime ->
                                    runCatching {
                                        val file = File(path)
                                        val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file,
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(contentUri, mime)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    }.onFailure {
                                        scope.launch { snackbarHostState.showSnackbar(File(path).name) }
                                    }
                                }

                                val handleUrlClick: (String) -> Unit = { url ->
                                    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                                    if (appearance.inAppBrowser) {
                                        activeWebApp = ActiveWebAppSession(url, normalized.substringAfter("://").substringBefore("/"), normalized)
                                    } else {
                                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized))) }
                                    }
                                }

                                val handleCommandClick: (String) -> Unit = { cmd ->
                                    if (blockForSpam) {
                                        showSpamAlert = true
                                    } else {
                                        scope.launch { clientManager.sendTextMessage(chatId, cmd) }
                                    }
                                }

                                when (item) {
                                    is ChatTimelineItem.Album -> AlbumMessageBubble(
                                        messages = item.messages,
                                        isGroupOrSupergroup = isGroup,
                                        isChannel = isChannel,
                                        lastReadOutboxId = chat?.lastReadOutboxMessageId ?: 0L,
                                        usersMap = usersMap,
                                        chatsMap = rawChatsMap,
                                        filesMap = filesMap,
                                        customEmojiStickers = customEmojiStickers,
                                        pendingCallbackButtonKey = pendingCallbackKey,
                                        clientManager = clientManager,
                                        onBubbleClick = { selectedMessage = it },
                                        onBubbleLongClick = { selectedMessage = it },
                                        onReplyClick = { replyId ->
                                            val targetIdx = timelineItems.indexOfFirst { ti ->
                                                when (ti) {
                                                    is ChatTimelineItem.Single -> ti.message.id == replyId
                                                    is ChatTimelineItem.Album -> ti.messages.any { it.id == replyId }
                                                }
                                            }
                                            if (targetIdx >= 0) scope.launch { listState.animateScrollToItem(targetIdx) }
                                        },
                                        onPhotoClick = { path -> fullscreenImagePath = path },
                                        onOpenDocument = openDoc,
                                        onToggleReaction = { mid, emoji, chosen ->
                                            scope.launch { clientManager.toggleReaction(chatId, mid, emoji, chosen) }
                                        },
                                        onInlineButtonClick = handleInlineButton,
                                        onUrlClick = handleUrlClick,
                                        onBotCommandClick = handleCommandClick,
                                        onCustomEmojiClick = { id, alt -> inspectedCustomEmoji = id to alt },
                                    )

                                    is ChatTimelineItem.Single -> ChatMessageBubble(
                                        message = msg,
                                        isGroupOrSupergroup = isGroup,
                                        isChannel = isChannel,
                                        lastReadOutboxId = chat?.lastReadOutboxMessageId ?: 0L,
                                        usersMap = usersMap,
                                        chatsMap = rawChatsMap,
                                        filesMap = filesMap,
                                        customEmojiStickers = customEmojiStickers,
                                        playbackState = playbackState,
                                        pendingCallbackButtonKey = pendingCallbackKey,
                                        clientManager = clientManager,
                                        onBubbleClick = { selectedMessage = msg },
                                        onBubbleLongClick = { selectedMessage = msg },
                                        onReplyClick = { replyId ->
                                            val targetIdx = timelineItems.indexOfFirst { ti ->
                                                when (ti) {
                                                    is ChatTimelineItem.Single -> ti.message.id == replyId
                                                    is ChatTimelineItem.Album -> ti.messages.any { it.id == replyId }
                                                }
                                            }
                                            if (targetIdx >= 0) scope.launch { listState.animateScrollToItem(targetIdx) }
                                        },
                                        onPhotoClick = { path -> fullscreenImagePath = path },
                                        onPlayAudio = { msgId, path -> voiceController.togglePlay(msgId, path) },
                                        onOpenDocument = openDoc,
                                        onToggleReaction = { emoji, chosen ->
                                            scope.launch { clientManager.toggleReaction(chatId, msg.id, emoji, chosen) }
                                        },
                                        onVotePoll = { msgId, optIdx ->
                                            scope.launch { clientManager.votePoll(chatId, msgId, intArrayOf(optIdx)) }
                                        },
                                        onInlineButtonClick = { btn, btnKey -> handleInlineButton(msg, btn, btnKey) },
                                        onUrlClick = handleUrlClick,
                                        onBotCommandClick = handleCommandClick,
                                        onCustomEmojiClick = { id, alt -> inspectedCustomEmoji = id to alt },
                                    )
                                }

                                if (currentDay != olderDay) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.Black.copy(alpha = if (tgColors.isDark) 0.42f else 0.25f))
                                                .padding(horizontal = 12.dp, vertical = 4.dp),
                                        ) {
                                            Text(currentDay, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isVideoNoteRecording) {
                        VideoNoteCameraOverlay(
                            isLocked = isRecordingLocked,
                            shouldFinalizeSignal = videoFinalizeSignal,
                            shouldCancelSignal = videoCancelSignal,
                            onVideoRecorded = { file, dur ->
                                isVideoNoteRecording = false
                                isRecordingLocked = false
                                if (blockForSpam) {
                                    showSpamAlert = true
                                } else {
                                    scope.launch {
                                        clientManager.sendVideoNoteMessage(chatId, file.absolutePath, dur, replyToMessageId = replyTarget?.id)
                                        replyTarget = null
                                    }
                                }
                            },
                            onCancel = {
                                isVideoNoteRecording = false
                                isRecordingLocked = false
                            },
                        )
                    }

                    // Mode Tooltip matching Telegram's dark floating toast above the mic/camera icon
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showModeTooltip && !recordingState.isRecording && !isVideoNoteRecording,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp),
                    ) {
                        Surface(
                            color = Color(0xFF1F2732).copy(alpha = 0.96f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 6.dp,
                        ) {
                            Text(
                                text = if (isVideoNoteMode) strings.holdForVideoTapForVoice else strings.holdForVoiceTapForVideo,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            )
                        }
                    }

                    // Floating Lock Pill while holding (slide up to lock)
                    if ((recordingState.isRecording || isVideoNoteRecording) && !isRecordingLocked) {
                        Surface(
                            color = Color(0xFF1F2732).copy(alpha = 0.94f),
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 14.dp, bottom = 16.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = "Lock", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Reply / Edit Banner
                AnimatedVisibility(visible = replyTarget != null || editingMessage != null) {
                    Surface(color = tgColors.cardBackground, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.width(3.dp).height(32.dp).background(tgColors.accent, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (editingMessage != null) strings.editMessageTitle
                                    else "${strings.replyToPrefix}${resolveSenderName(replyTarget?.senderId, usersMap, rawChatsMap)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tgColors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = (editingMessage ?: replyTarget)?.content?.previewText().orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = {
                                    replyTarget = null
                                    if (editingMessage != null) {
                                        editingMessage = null
                                        messageInput = ""
                                    }
                                },
                            ) {
                                Icon(Icons.Rounded.Close, "Cancel")
                            }
                        }
                    }
                }

                // Spam Restriction Banner
                AnimatedVisibility(visible = blockForSpam) {
                    Surface(
                        color = Color(0xFFEF4747).copy(alpha = 0.14f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSpamAlert = true }
                            .testTag("chat_spam_banner"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Block, contentDescription = null, tint = Color(0xFFEF4747), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(strings.spamRestrictedBanner, formatRemainingDuration(spamUntilMs)),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4747),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Bottom Composer OR Bot Start OR Channel Read-Only Bar
                if (!canPost) {
                    val isMember = supergroup?.status !is TdApi.ChatMemberStatusLeft
                    Surface(color = tgColors.cardBackground, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            if (!isMember) {
                                Button(
                                    onClick = { scope.launch { clientManager.joinChat(chatId) } },
                                    modifier = Modifier.testTag("join_channel_button"),
                                ) { Text(strings.joinChannel) }
                            } else if (chat != null) {
                                TextButton(
                                    onClick = { scope.launch { clientManager.toggleMuteChat(chat) } },
                                    modifier = Modifier.testTag("channel_mute_button"),
                                ) {
                                    Icon(
                                        imageVector = if (chat.isMuted()) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (chat.isMuted()) strings.unmute.uppercase() else strings.mute.uppercase(),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                } else if (isBot && messages.isEmpty() && !isLoadingHistory && peerUser != null) {
                    Surface(color = tgColors.cardBackground, modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { scope.launch { clientManager.sendBotStart(peerUser.id, chatId) } },
                                colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent),
                                modifier = Modifier.fillMaxWidth().testTag("bot_start_button"),
                            ) {
                                Text(strings.botStartButton, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Surface(
                        color = tgColors.cardBackground,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    ) {
                        Column {
                            val latestVideoNoteMode by rememberUpdatedState(isVideoNoteMode)
                            val latestBlockForSpam by rememberUpdatedState(blockForSpam)
                            val latestReplyTarget by rememberUpdatedState(replyTarget)
                            val latestGhostMode by rememberUpdatedState(appearance.ghostModeEnabled)
                            val isActivelyRecording = recordingState.isRecording || isVideoNoteRecording

                            if (isRecordingLocked && recordingState.isRecording) {
                                val mins = (recordingState.elapsedMillis / 60000L).toInt()
                                val secs = ((recordingState.elapsedMillis / 1000L) % 60L).toInt()
                                val deci = ((recordingState.elapsedMillis / 100L) % 10L).toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF5350)))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = String.format(Locale.US, "%d:%02d,%d", mins, secs, deci),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = strings.recordingVoice,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = strings.cancel,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                isRecordingLocked = false
                                                voiceController.cancelRecording()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("locked_voice_cancel_button"),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            isRecordingLocked = false
                                            val result = voiceController.stopAndReturnRecording()
                                            if (result != null) {
                                                if (latestBlockForSpam) {
                                                    showSpamAlert = true
                                                } else {
                                                    val replyId = latestReplyTarget?.id
                                                    scope.launch {
                                                        clientManager.sendVoiceNoteMessage(
                                                            chatId = chatId,
                                                            localPath = result.file.absolutePath,
                                                            durationSeconds = result.durationSeconds,
                                                            replyToMessageId = replyId,
                                                            waveform = result.waveform,
                                                        )
                                                        replyTarget = null
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(tgColors.accent)
                                            .testTag("locked_voice_send_button"),
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Send, "Send voice", tint = Color.White)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (isActivelyRecording) {
                                        // Recording bar left section — keeps the hold button mounted on the right!
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEF5350)),
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            if (recordingState.isRecording) {
                                                val mins = (recordingState.elapsedMillis / 60000L).toInt()
                                                val secs = ((recordingState.elapsedMillis / 1000L) % 60L).toInt()
                                                val deci = ((recordingState.elapsedMillis / 100L) % 10L).toInt()
                                                Text(
                                                    text = String.format(Locale.US, "%d:%02d,%d", mins, secs, deci),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                            val slideRatio = (1f + (dragOffsetX / 130f)).coerceIn(0.2f, 1f)
                                            Text(
                                                text = "‹  ${strings.slideToCancel}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (dragOffsetX < -60f) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .offset { IntOffset(dragOffsetX.coerceIn(-120f, 0f).roundToInt(), 0) }
                                                    .alpha(slideRatio),
                                            )
                                        }
                                    } else {
                                        // Bot WebApp Menu Button OR Commands button OR Emoji tray icon
                                        val menuBtn = botInfo?.menuButton
                                        if (isBot && menuBtn != null && menuBtn.url.isNotBlank() && peerUser != null) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(end = 6.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(tgColors.accent)
                                                    .clickable { launchWebApp(peerUser.id, menuBtn.url, menuBtn.text.ifBlank { peerUser.firstName }) }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    .testTag("bot_webapp_menu_button"),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(Icons.Rounded.WebAsset, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = menuBtn.text.ifBlank { strings.botMenuButtonDefault },
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                )
                                            }
                                        } else if (isBot && !botInfo?.commands.isNullOrEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(end = 6.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(tgColors.accent)
                                                    .clickable { showBotCommandsSheet = true }
                                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                                                    .testTag("bot_commands_button"),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(Icons.Rounded.Menu, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(strings.botMenuButtonDefault, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { showEmojiTray = !showEmojiTray },
                                                modifier = Modifier.size(38.dp).testTag("emoji_tray_button"),
                                            ) {
                                                Icon(Icons.Rounded.SentimentSatisfiedAlt, "Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        BasicTextField(
                                            value = messageInput,
                                            onValueChange = { text ->
                                                messageInput = text
                                                val now = System.currentTimeMillis()
                                                if (text.isNotBlank() && !latestGhostMode && now - lastTypingSentMs > 4000L) {
                                                    lastTypingSentMs = now
                                                    clientManager.sendTypingAction(chatId)
                                                }
                                            },
                                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                                            cursorBrush = SolidColor(tgColors.accent),
                                            maxLines = 5,
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = if (appearance.sendWithEnter) ImeAction.Send else ImeAction.Default,
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onSend = { sendCurrentText() },
                                            ),
                                            decorationBox = { inner ->
                                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                                                    if (messageInput.isEmpty()) {
                                                        Text(
                                                            text = activeReplyKeyboard?.inputFieldPlaceholder?.takeIf { it.isNotBlank() } ?: strings.messageHint,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 16.sp,
                                                        )
                                                    }
                                                    inner()
                                                }
                                            },
                                            modifier = Modifier.weight(1f).testTag("chat_message_input"),
                                        )

                                        if (activeReplyKeyboard != null) {
                                            IconButton(
                                                onClick = {
                                                    showReplyKeyboard = !showReplyKeyboard
                                                    showEmojiTray = false
                                                },
                                                modifier = Modifier.size(38.dp).testTag("bot_reply_keyboard_toggle"),
                                            ) {
                                                Icon(
                                                    imageVector = if (showReplyKeyboard) Icons.Rounded.Keyboard else Icons.Rounded.GridView,
                                                    contentDescription = "Bot Keyboard",
                                                    tint = if (showReplyKeyboard) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                if (latestBlockForSpam) showSpamAlert = true else showAttachmentSheet = true
                                            },
                                            modifier = Modifier.size(38.dp).testTag("chat_attach_button"),
                                        ) {
                                            Icon(Icons.Rounded.AttachFile, "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (messageInput.isNotBlank() && !isActivelyRecording) {
                                        IconButton(
                                            onClick = { sendCurrentText() },
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(tgColors.accent).testTag("chat_send_button"),
                                        ) {
                                            Icon(
                                                imageVector = if (editingMessage != null) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.Send,
                                                contentDescription = "Send",
                                                tint = Color.White,
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .scale(if (isActivelyRecording) 1.15f else 1f)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isActivelyRecording) tgColors.accent else Color.Transparent,
                                                )
                                                .pointerInput(chatId) {
                                                    awaitEachGesture {
                                                        val down = awaitFirstDown(requireUnconsumed = false)
                                                        if (latestBlockForSpam) {
                                                            waitForUpOrCancellation()
                                                            showSpamAlert = true
                                                            return@awaitEachGesture
                                                        }
                                                        val quickRelease = withTimeoutOrNull(210L) {
                                                            waitForUpOrCancellation()
                                                        }
                                                        if (quickRelease != null) {
                                                            // Single tap toggles Mic <-> Video Note Camera
                                                            isVideoNoteMode = !latestVideoNoteMode
                                                            showModeTooltip = true
                                                            return@awaitEachGesture
                                                        }

                                                        val videoMode = latestVideoNoteMode
                                                        showModeTooltip = false
                                                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                                        if (videoMode && (!hasMic || !hasCam)) {
                                                            recordPermissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
                                                            waitForUpOrCancellation()
                                                            return@awaitEachGesture
                                                        } else if (!videoMode && !hasMic) {
                                                            recordPermissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                                            waitForUpOrCancellation()
                                                            return@awaitEachGesture
                                                        }

                                                        dragOffsetX = 0f
                                                        dragOffsetY = 0f
                                                        isRecordingLocked = false
                                                        var startedVoice = false
                                                        var startedVideo = false
                                                        var outcome = "SEND"

                                                        if (videoMode) {
                                                            videoFinalizeSignal = 0
                                                            videoCancelSignal = 0
                                                            isVideoNoteRecording = true
                                                            startedVideo = true
                                                        } else {
                                                            val ok = voiceController.startRecording()
                                                            if (!ok) {
                                                                scope.launch { snackbarHostState.showSnackbar(strings.micPermissionRequired) }
                                                                waitForUpOrCancellation()
                                                                return@awaitEachGesture
                                                            }
                                                            startedVoice = true
                                                            if (!latestGhostMode) {
                                                                clientManager.sendTypingAction(chatId, isRecording = true)
                                                            }
                                                        }

                                                        try {
                                                            while (true) {
                                                                val event = awaitPointerEvent()
                                                                val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                                                                if (change == null || !change.pressed) {
                                                                    outcome = "SEND"
                                                                    break
                                                                }
                                                                val delta = change.positionChange()
                                                                dragOffsetX += delta.x
                                                                dragOffsetY += delta.y
                                                                change.consume()

                                                                if (dragOffsetX < -115f) {
                                                                    outcome = "CANCEL"
                                                                    if (startedVideo) {
                                                                        videoCancelSignal++
                                                                    } else if (startedVoice) {
                                                                        voiceController.cancelRecording()
                                                                    }
                                                                    break
                                                                } else if (dragOffsetY < -95f) {
                                                                    outcome = "LOCK"
                                                                    isRecordingLocked = true
                                                                    break
                                                                }
                                                            }

                                                            if (outcome == "SEND") {
                                                                if (startedVideo) {
                                                                    videoFinalizeSignal++
                                                                } else if (startedVoice) {
                                                                    val res = voiceController.stopAndReturnRecording()
                                                                    if (res != null) {
                                                                        val replyId = latestReplyTarget?.id
                                                                        scope.launch {
                                                                            clientManager.sendVoiceNoteMessage(
                                                                                chatId = chatId,
                                                                                localPath = res.file.absolutePath,
                                                                                durationSeconds = res.durationSeconds,
                                                                                replyToMessageId = replyId,
                                                                                waveform = res.waveform,
                                                                            )
                                                                            replyTarget = null
                                                                        }
                                                                    } else {
                                                                        scope.launch { snackbarHostState.showSnackbar(strings.recordingTooShortHint) }
                                                                    }
                                                                }
                                                            }
                                                        } catch (ce: CancellationException) {
                                                            if (startedVoice && !isRecordingLocked) {
                                                                voiceController.cancelRecording()
                                                            }
                                                            if (startedVideo && !isRecordingLocked) {
                                                                videoCancelSignal++
                                                            }
                                                            throw ce
                                                        }
                                                    }
                                                }
                                                .testTag("voice_record_button"),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (isVideoNoteMode) Icons.Rounded.Videocam else Icons.Rounded.Mic,
                                                contentDescription = if (isVideoNoteMode) "Video message" else "Voice message",
                                                tint = when {
                                                    isActivelyRecording -> Color.White
                                                    isVideoNoteMode -> tgColors.accent
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            // Bot Reply Keyboard Grid with colored accent buttons for WebApp/Action types
                            AnimatedVisibility(visible = showReplyKeyboard && activeReplyKeyboard != null) {
                                activeReplyKeyboard?.let { kb ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(tgColors.headerBackground)
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        kb.rows.forEach { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                row.forEach { kbtn ->
                                                    val isWebAppBtn = kbtn.type is TdApi.KeyboardButtonTypeWebApp
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isWebAppBtn) tgColors.accent.copy(alpha = 0.22f) else tgColors.cardBackground,
                                                        border = if (isWebAppBtn) BorderStroke(1.dp, tgColors.accent.copy(alpha = 0.55f)) else null,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .clickable {
                                                                when (val t = kbtn.type) {
                                                                    is TdApi.KeyboardButtonTypeWebApp -> {
                                                                        val botId = peerUser?.id ?: 0L
                                                                        if (botId != 0L) launchWebApp(botId, t.url, kbtn.text)
                                                                    }
                                                                    else -> {
                                                                        if (blockForSpam) {
                                                                            showSpamAlert = true
                                                                        } else {
                                                                            scope.launch {
                                                                                clientManager.sendTextMessage(chatId, kbtn.text)
                                                                                if (kb.oneTime) showReplyKeyboard = false
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(vertical = 13.dp, horizontal = 8.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Text(
                                                                text = kbtn.text,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = if (isWebAppBtn) tgColors.accent else MaterialTheme.colorScheme.onSurface,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Emoji Tray
                            AnimatedVisibility(visible = showEmojiTray) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(tgColors.headerBackground)
                                        .padding(10.dp),
                                ) {
                                    QuickEmojiSet.chunked(8).forEach { row ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            row.forEach { emoji ->
                                                Text(
                                                    text = emoji,
                                                    fontSize = 26.sp,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { messageInput += emoji }
                                                        .padding(6.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    activeWebApp?.let { app ->
        TelegramWebAppDialog(
            title = app.title,
            subtitle = app.subtitle,
            url = app.url,
            onDismiss = { activeWebApp = null },
        )
    }

    callbackAlertText?.let { msg ->
        AlertDialog(
            onDismissRequest = { callbackAlertText = null },
            title = { Text(chat?.title ?: "Bot") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { callbackAlertText = null }) { Text("OK") }
            },
        )
    }

    if (showBotCommandsSheet && !botInfo?.commands.isNullOrEmpty()) {
        ModalBottomSheet(onDismissRequest = { showBotCommandsSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = strings.botCommandsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
                HorizontalDivider(color = tgColors.divider)
                LazyColumn {
                    items(botInfo!!.commands.toList()) { cmd ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBotCommandsSheet = false
                                    if (blockForSpam) {
                                        showSpamAlert = true
                                    } else {
                                        scope.launch { clientManager.sendTextMessage(chatId, "/${cmd.command}") }
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Text("/${cmd.command}", color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                            if (cmd.description.isNotBlank()) {
                                Text(cmd.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMessage?.let { msg ->
        MessageActionsSheet(
            message = msg,
            canPin = canPost,
            onDismiss = { selectedMessage = null },
            onReact = { emoji ->
                val already = msg.interactionInfo?.reactions?.reactions?.any {
                    (it.type as? TdApi.ReactionTypeEmoji)?.emoji == emoji && it.isChosen
                } == true
                scope.launch { clientManager.toggleReaction(chatId, msg.id, emoji, already) }
                selectedMessage = null
            },
            onReply = {
                replyTarget = msg
                editingMessage = null
                selectedMessage = null
            },
            onCopy = {
                val txt = (msg.content as? TdApi.MessageText)?.text?.text.orEmpty()
                if (txt.isNotBlank()) {
                    clipboard.setText(AnnotatedString(txt))
                    scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                }
                selectedMessage = null
            },
            onEdit = {
                editingMessage = msg
                replyTarget = null
                messageInput = (msg.content as? TdApi.MessageText)?.text?.text.orEmpty()
                selectedMessage = null
            },
            onForward = {
                messageToForward = msg
                selectedMessage = null
            },
            onPinToggle = {
                scope.launch {
                    if (msg.isPinned) {
                        clientManager.unpinMessage(chatId, msg.id)
                        if (pinnedMessage?.id == msg.id) pinnedMessage = null
                    } else {
                        clientManager.pinMessage(chatId, msg.id)
                        pinnedMessage = msg
                    }
                }
                selectedMessage = null
            },
            onDelete = {
                messageToDelete = msg
                selectedMessage = null
            },
        )
    }

    if (showAttachmentSheet) {
        AttachmentPickerSheet(
            onDismiss = { showAttachmentSheet = false },
            onPickGallery = {
                showAttachmentSheet = false
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onPickFile = {
                showAttachmentSheet = false
                documentPickerLauncher.launch(arrayOf("*/*"))
            },
            onPickMusic = {
                showAttachmentSheet = false
                audioPickerLauncher.launch(arrayOf("audio/*"))
            },
            onCreatePoll = {
                showAttachmentSheet = false
                showCreatePollDialog = true
            },
            onPickLocation = {
                showAttachmentSheet = false
                showLocationDialog = true
            },
        )
    }

    if (showCreatePollDialog) {
        CreatePollDialog(
            onDismiss = { showCreatePollDialog = false },
            onCreate = { q, opts, anon, multi ->
                showCreatePollDialog = false
                if (blockForSpam) {
                    showSpamAlert = true
                } else {
                    scope.launch {
                        clientManager.sendPollMessage(chatId, q, opts, anon, multi)
                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                    }
                }
            },
        )
    }

    if (showLocationDialog) {
        var latText by remember { mutableStateOf("41.2995") }
        var lngText by remember { mutableStateOf("69.2401") }
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(strings.locationLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lat = latText.toDoubleOrNull() ?: 41.2995
                        val lng = lngText.toDoubleOrNull() ?: 69.2401
                        showLocationDialog = false
                        if (blockForSpam) {
                            showSpamAlert = true
                        } else {
                            scope.launch {
                                clientManager.send(
                                    TdApi.SendMessage(
                                        chatId,
                                        null,
                                        null,
                                        null,
                                        null,
                                        TdApi.InputMessageLocation(TdApi.Location(lat, lng, 10.0), 0, 0, 0),
                                    )
                                )
                            }
                        }
                    },
                ) { Text(strings.continueAction) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text(strings.cancel) }
            },
        )
    }

    messageToForward?.let { msg ->
        val mainChats = chatsMap.values.map { it.chat }
            .filter { it.orderFor(TdApi.ChatListMain()) > 0L }
            .sortedByDescending { it.orderFor(TdApi.ChatListMain()) }
        ForwardChatPickerSheet(
            chats = mainChats,
            filesMap = filesMap,
            clientManager = clientManager,
            onSelectChat = { targetId ->
                if (blockForSpam) {
                    showSpamAlert = true
                } else {
                    scope.launch {
                        clientManager.forwardMessages(targetId, chatId, longArrayOf(msg.id))
                        snackbarHostState.showSnackbar(strings.forward)
                    }
                }
                messageToForward = null
            },
            onDismiss = { messageToForward = null },
        )
    }

    messageToDelete?.let { msg ->
        DeleteMessageConfirmDialog(
            canDeleteForAll = msg.isOutgoing || !isChannel,
            onDismiss = { messageToDelete = null },
            onConfirm = { revoke ->
                scope.launch { clientManager.deleteMessages(chatId, longArrayOf(msg.id), revoke) }
                messageToDelete = null
            },
        )
    }

    fullscreenImagePath?.let { path ->
        FullscreenPhotoDialog(path = path, onDismiss = { fullscreenImagePath = null })
    }

    inspectedCustomEmoji?.let { (emojiId, fallback) ->
        CustomEmojiPackSheet(
            customEmojiId = emojiId,
            fallbackEmoji = fallback,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onDismiss = { inspectedCustomEmoji = null },
            onStatusMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
        )
    }

    if (showSpamAlert) {
        AlertDialog(
            onDismissRequest = { showSpamAlert = false },
            icon = { Icon(Icons.Rounded.Block, contentDescription = null, tint = Color(0xFFEF4747)) },
            title = { Text(strings.spamRestrictedTitle, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = String.format(strings.spamRestrictedDialogBody, formatRemainingDuration(spamUntilMs)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showSpamAlert = false }) {
                    Text("OK", color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }
}
