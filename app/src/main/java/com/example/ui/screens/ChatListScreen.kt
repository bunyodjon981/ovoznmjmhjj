package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MarkChatRead
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChatSwipeAction
import com.example.data.FolderViewStyle
import com.example.data.LocalStrings
import com.example.data.ghostModeOffToast
import com.example.data.ghostModeOnToast
import com.example.data.ghostModeTitle
import com.example.tdlib.ChatEntry
import com.example.tdlib.ChatTypingState
import com.example.tdlib.ConnectionStatus
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.chatListKey
import com.example.tdlib.copyUriToCacheFile
import com.example.tdlib.displayName
import com.example.tdlib.formatChatDate
import com.example.tdlib.isArchived
import com.example.tdlib.isMuted
import com.example.tdlib.isPinnedIn
import com.example.tdlib.miniThumbnailBytes
import com.example.tdlib.orderFor
import com.example.tdlib.postPhotoStory
import com.example.tdlib.previewText
import com.example.ui.components.StoriesStripRow
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.TgDraftRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    me: TdApi.User?,
    connectionStatus: ConnectionStatus,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    basicGroupsMap: Map<Long, TdApi.BasicGroup> = emptyMap(),
    chatFolders: List<TdApi.ChatFolderInfo>,
    filesMap: Map<Int, TdApi.File>,
    activeStories: Map<Long, TdApi.ChatActiveStories> = emptyMap(),
    chatActions: Map<Long, ChatTypingState>,
    loadingChatLists: Set<String> = emptySet(),
    exhaustedChatLists: Set<String> = emptySet(),
    selectedChatId: Long? = null,
    clientManager: TelegramClientManager,
    hasFloatingBottomBar: Boolean,
    ghostModeEnabled: Boolean = false,
    onToggleGhostMode: () -> Unit = {},
    onToggleTheme: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNewGroup: () -> Unit,
    onOpenNewChannel: () -> Unit,
    onOpenChatFoldersSettings: () -> Unit,
) {
    val context = LocalContext.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val scope = rememberCoroutineScope()

    var selectedFolderIndex by remember { mutableIntStateOf(0) }
    var showArchivedOnly by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var localSearchResults by remember { mutableStateOf<List<TdApi.Chat>>(emptyList()) }
    var publicSearchResults by remember { mutableStateOf<List<TdApi.Chat>>(emptyList()) }
    var selectedChatForMenu by remember { mutableStateOf<TdApi.Chat?>(null) }
    var chatToDelete by remember { mutableStateOf<TdApi.Chat?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var activeStoryTarget by remember { mutableStateOf<Triple<Long, List<TdApi.StoryInfo>, String>?>(null) }
    var ghostBannerMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ghostBannerMessage) {
        if (ghostBannerMessage != null) {
            delay(2200L)
            ghostBannerMessage = null
        }
    }

    val storyPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val myId = me?.id ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                val file = copyUriToCacheFile(context, uri, "story_upload")
                if (file != null) {
                    clientManager.postPhotoStory(myId, file.absolutePath, postToProfile = true)
                }
            }
        }
    }

    BackHandler(enabled = isSearchActive || showArchivedOnly) {
        when {
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
            showArchivedOnly -> showArchivedOnly = false
        }
    }

    LaunchedEffect(searchQuery, isSearchActive) {
        if (!isSearchActive || searchQuery.isBlank()) {
            localSearchResults = emptyList()
            publicSearchResults = emptyList()
            return@LaunchedEffect
        }
        delay(280L)
        localSearchResults = clientManager.searchLocalChats(searchQuery.trim())
        if (searchQuery.trim().length >= 3) {
            publicSearchResults = clientManager.searchPublicChats(searchQuery.trim())
        } else {
            publicSearchResults = emptyList()
        }
    }

    val activeChatList: TdApi.ChatList = remember(selectedFolderIndex, showArchivedOnly, chatFolders) {
        when {
            showArchivedOnly -> TdApi.ChatListArchive()
            selectedFolderIndex == 0 -> TdApi.ChatListMain()
            selectedFolderIndex - 1 in chatFolders.indices ->
                TdApi.ChatListFolder(chatFolders[selectedFolderIndex - 1].id)
            else -> TdApi.ChatListMain()
        }
    }
    val activeListKey = remember(activeChatList) { chatListKey(activeChatList) }
    val isLoadingMore = loadingChatLists.contains(activeListKey)
    val isExhausted = exhaustedChatLists.contains(activeListKey)

    val sortedChats = remember(chatsMap, activeChatList) {
        chatsMap.values
            .map { it.chat }
            .filter { it.orderFor(activeChatList) > 0L }
            .sortedByDescending { it.orderFor(activeChatList) }
    }

    val archivedChats = remember(chatsMap) {
        chatsMap.values.map { it.chat }.filter { it.orderFor(TdApi.ChatListArchive()) > 0L }
            .sortedByDescending { it.orderFor(TdApi.ChatListArchive()) }
    }

    val totalMainUnread = remember(chatsMap) {
        chatsMap.values
            .map { it.chat }
            .count { it.orderFor(TdApi.ChatListMain()) > 0L && it.unreadCount > 0 }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(activeListKey) {
        lazyListState.scrollToItem(0)
    }

    LaunchedEffect(lazyListState, activeListKey, sortedChats.size, isLoadingMore, isExhausted) {
        if (isExhausted || isLoadingMore) return@LaunchedEffect
        if (sortedChats.isEmpty()) {
            clientManager.loadMoreChats(activeChatList, 30)
            return@LaunchedEffect
        }
        snapshotFlow {
            val info = lazyListState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible to info.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, totalItems) ->
                if (!isExhausted && !isLoadingMore && totalItems > 0 && lastVisible >= totalItems - 10) {
                    clientManager.loadMoreChats(activeChatList, 30)
                }
            }
    }

    val fabBottomPadding = if (hasFloatingBottomBar) 88.dp else 18.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        val isCompactWidth = maxWidth < 380.dp
        val isMediumStandalone = maxWidth >= 600.dp
        val horizontalPadding = if (isMediumStandalone) 20.dp else 14.dp
        val avatarSize = when {
            isCompactWidth -> 50.dp
            isMediumStandalone -> 56.dp
            else -> 54.dp
        }
        val dividerIndent = horizontalPadding + avatarSize + 12.dp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 760.dp)
                .fillMaxSize(),
        ) {
            // Top App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showArchivedOnly) {
                    IconButton(
                        onClick = { showArchivedOnly = false },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("archive_back_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.archivedChats,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    val myAvatarFile = me?.profilePhoto?.small?.let { filesMap[it.id] ?: it }
                    TelegramAvatar(
                        id = me?.id ?: 0L,
                        title = strings.appName,
                        photoFile = myAvatarFile,
                        clientManager = clientManager,
                        size = 38.dp,
                        isOnline = false,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.5.dp, tgColors.accent, CircleShape)
                            .clickable {
                                scope.launch {
                                    val myId = me?.id ?: return@launch
                                    clientManager.createPrivateChat(myId).onSuccess { onOpenChat(it.id) }
                                }
                            },
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    val titleText = when (connectionStatus) {
                        ConnectionStatus.CONNECTING -> strings.connecting
                        ConnectionStatus.UPDATING -> strings.updating
                        ConnectionStatus.WAITING_FOR_NETWORK -> strings.waitingForNetwork
                        ConnectionStatus.READY -> strings.appName
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (connectionStatus != ConnectionStatus.READY) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = tgColors.accent,
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("quick_theme_toggle"),
                    ) {
                        Icon(
                            imageVector = if (tgColors.isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = if (tgColors.isDark) strings.switchToDayMode else strings.switchToNightMode,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val ghostBgColor by animateColorAsState(
                        targetValue = if (ghostModeEnabled) tgColors.accent.copy(alpha = 0.22f) else Color.Transparent,
                        label = "ghost_bg",
                    )
                    val ghostTintColor by animateColorAsState(
                        targetValue = if (ghostModeEnabled) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "ghost_tint",
                    )
                    IconButton(
                        onClick = {
                            val nextEnabled = !ghostModeEnabled
                            onToggleGhostMode()
                            ghostBannerMessage = if (nextEnabled) strings.ghostModeOnToast else strings.ghostModeOffToast
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(ghostBgColor)
                            .then(
                                if (ghostModeEnabled) {
                                    Modifier.border(1.2.dp, tgColors.accent.copy(alpha = 0.65f), CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .semantics { contentDescription = strings.ghostModeTitle }
                            .testTag("ghost_mode_toggle"),
                    ) {
                        GhostHeaderIcon(
                            tint = ghostTintColor,
                            glowing = ghostModeEnabled,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("chats_overflow_menu"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = strings.botMenuButtonDefault,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.newGroup) },
                                leadingIcon = { Icon(Icons.Rounded.Group, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenNewGroup()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings.newChannel) },
                                leadingIcon = { Icon(Icons.Rounded.Campaign, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenNewChannel()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings.savedMessages) },
                                leadingIcon = { Icon(Icons.Rounded.Bookmark, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    scope.launch {
                                        val myId = me?.id ?: return@launch
                                        clientManager.createPrivateChat(myId).onSuccess { onOpenChat(it.id) }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings.chatFoldersTitle) },
                                leadingIcon = { Icon(Icons.Rounded.FolderOpen, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenChatFoldersSettings()
                                },
                            )
                        }
                    }
                }
            }

            // Rounded Pill Search Bar
            if (!showArchivedOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tgColors.pillBackground)
                        .clickable { isSearchActive = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isSearchActive) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.cancel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable {
                                    isSearchActive = false
                                    searchQuery = ""
                                }
                                .testTag("search_back_button"),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = strings.searchChatsHint,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                ),
                                cursorBrush = SolidColor(tgColors.accent),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = strings.searchChatsHint,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 15.sp,
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_search_input"),
                            )
                        } else {
                            Text(
                                text = strings.searchChatsHint,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                            )
                        }
                    }
                    if (isSearchActive && searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.cancel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { searchQuery = "" },
                        )
                    }
                }
            }

            // Telegram Stories Strip (Hikoyalar)
            if (!isSearchActive && !showArchivedOnly) {
                Spacer(modifier = Modifier.height(4.dp))
                StoriesStripRow(
                    me = me,
                    chatsMap = chatsMap,
                    activeStories = activeStories,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    onAddMyStory = {
                        storyPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onOpenStoriesForChat = { cid, infos ->
                        val title = if (cid == me?.id) me.displayName() else chatsMap[cid]?.chat?.title ?: strings.storiesLabel
                        activeStoryTarget = Triple(cid, infos, title)
                    },
                )
            }

            // Compact Folder Pill Bar
            if (!isSearchActive && !showArchivedOnly && chatFolders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val allSelected = selectedFolderIndex == 0
                    FolderPillTab(
                        selected = allSelected,
                        label = if (appearance.folderViewStyle == FolderViewStyle.NAME) strings.allChats else null,
                        icon = Icons.AutoMirrored.Rounded.Send,
                        badgeCount = totalMainUnread,
                        onClick = { selectedFolderIndex = 0 },
                    )
                    chatFolders.forEachIndexed { idx, folder ->
                        val tabIdx = idx + 1
                        val folderUnread = chatsMap.values.count {
                            it.chat.orderFor(TdApi.ChatListFolder(folder.id)) > 0L && it.chat.unreadCount > 0
                        }
                        FolderPillTab(
                            selected = selectedFolderIndex == tabIdx,
                            label = if (appearance.folderViewStyle == FolderViewStyle.NAME) folder.name.text.text else null,
                            icon = Icons.Outlined.Folder,
                            badgeCount = folderUnread,
                            onClick = {
                                selectedFolderIndex = tabIdx
                                scope.launch { clientManager.loadMoreChats(TdApi.ChatListFolder(folder.id), 30) }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Chat Content
            AnimatedContent(
                targetState = isSearchActive && searchQuery.isNotBlank(),
                label = "chats_vs_search",
                modifier = Modifier.weight(1f),
            ) { searching ->
                if (searching) {
                    SearchResultsList(
                        localResults = localSearchResults,
                        publicResults = publicSearchResults,
                        meId = me?.id,
                        chatsMap = chatsMap,
                        usersMap = usersMap,
                        supergroupsMap = supergroupsMap,
                        basicGroupsMap = basicGroupsMap,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        horizontalPadding = horizontalPadding,
                        avatarSize = avatarSize,
                        onOpenChat = { chatId ->
                            isSearchActive = false
                            searchQuery = ""
                            onOpenChat(chatId)
                        },
                    )
                } else if (sortedChats.isEmpty() && (!showArchivedOnly && archivedChats.isEmpty()) && !isLoadingMore) {
                    EmptyChatsState(onOpenContacts = onOpenContacts)
                } else if (sortedChats.isEmpty() && isLoadingMore) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = tgColors.accent)
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (hasFloatingBottomBar) 104.dp else 80.dp),
                    ) {
                        if (!showArchivedOnly && selectedFolderIndex == 0 && archivedChats.isNotEmpty()) {
                            item(key = "archived_chats_row", contentType = "archived_row") {
                                ArchivedChatsRow(
                                    archivedCount = archivedChats.size,
                                    previewTitles = archivedChats.take(3).joinToString(", ") { it.title },
                                    horizontalPadding = horizontalPadding,
                                    avatarSize = avatarSize,
                                    onClick = { showArchivedOnly = true },
                                )
                                HorizontalDivider(modifier = Modifier.padding(start = dividerIndent), color = tgColors.divider)
                            }
                        }

                        items(
                            items = sortedChats,
                            key = { it.id },
                            contentType = { "chat_row" },
                        ) { chat ->
                            val chatVersion = chatsMap[chat.id]?.version ?: 0L
                            val chatType = chat.type
                            val peerUser = (chatType as? TdApi.ChatTypePrivate)?.let { usersMap[it.userId] }
                            val supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] }
                            val basicGroup = (chatType as? TdApi.ChatTypeBasicGroup)?.let { basicGroupsMap[it.basicGroupId] }
                            val resolvedAvatarFile = chat.photo?.small?.let { filesMap[it.id] ?: it }
                            val isGroup = chatType is TdApi.ChatTypeBasicGroup ||
                                (chatType is TdApi.ChatTypeSupergroup && !chatType.isChannel)
                            val senderName = remember(chat.lastMessage?.id, chat.lastMessage?.senderId, usersMap, chatsMap, isGroup) {
                                val msg = chat.lastMessage
                                if (!isGroup || msg == null || msg.isOutgoing) {
                                    null
                                } else {
                                    when (val sender = msg.senderId) {
                                        is TdApi.MessageSenderUser ->
                                            usersMap[sender.userId]?.firstName?.takeIf { it.isNotBlank() }
                                        is TdApi.MessageSenderChat ->
                                            chatsMap[sender.chatId]?.chat?.title?.takeIf { it.isNotBlank() }
                                        else -> null
                                    }
                                }
                            }
                            val isSelected = selectedChatId != null && selectedChatId == chat.id

                            SwipeableChatRow(
                                chat = chat,
                                version = chatVersion,
                                activeChatList = activeChatList,
                                swipeAction = appearance.swipeAction,
                                threeLines = appearance.chatListThreeLines,
                                meId = me?.id,
                                peerUser = peerUser,
                                supergroup = supergroup,
                                basicGroup = basicGroup,
                                resolvedAvatarFile = resolvedAvatarFile,
                                senderName = senderName,
                                typingLabel = chatActions[chat.id]?.label,
                                isSelected = isSelected,
                                horizontalPadding = horizontalPadding,
                                avatarSize = avatarSize,
                                clientManager = clientManager,
                                modifier = if (appearance.animationsEnabled) Modifier.animateItem() else Modifier,
                                onClick = { onOpenChat(chat.id) },
                                onLongClick = { selectedChatForMenu = chat },
                                onSwipeTriggered = {
                                    when (appearance.swipeAction) {
                                        ChatSwipeAction.ARCHIVE -> {
                                            val archived = chat.isArchived()
                                            scope.launch { clientManager.setChatArchived(chat.id, !archived) }
                                        }
                                        ChatSwipeAction.PIN -> {
                                            val pinned = chat.isPinnedIn(activeChatList)
                                            scope.launch { clientManager.togglePinChat(chat.id, activeChatList, !pinned) }
                                        }
                                        ChatSwipeAction.UNREAD -> {
                                            val unread = chat.unreadCount > 0 || chat.isMarkedAsUnread
                                            scope.launch { clientManager.toggleMarkAsUnread(chat.id, !unread) }
                                        }
                                        ChatSwipeAction.DELETE -> chatToDelete = chat
                                    }
                                },
                            )
                            if (!isSelected) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = dividerIndent),
                                    color = tgColors.divider,
                                )
                            }
                        }

                        if (isLoadingMore) {
                            item(key = "chats_loading_footer", contentType = "loading_footer") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.2.dp,
                                        color = tgColors.accent,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Camera + "+" Action Buttons
        if (!isSearchActive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = fabBottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SmallFloatingActionButton(
                    onClick = onOpenNewChannel,
                    shape = CircleShape,
                    containerColor = tgColors.cardBackground,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = strings.newChannel, modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(
                    onClick = onOpenContacts,
                    shape = CircleShape,
                    containerColor = tgColors.accent,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("new_chat_fab"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.startMessaging, modifier = Modifier.size(26.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = ghostBannerMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = fabBottomPadding + 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF152233).copy(alpha = 0.94f))
                    .border(1.dp, tgColors.accent.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GhostHeaderIcon(
                        tint = tgColors.accent,
                        glowing = true,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ghostBannerMessage.orEmpty(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    selectedChatForMenu?.let { chat ->
        ChatContextMenuSheet(
            chat = chat,
            activeChatList = activeChatList,
            onDismiss = { selectedChatForMenu = null },
            onPinToggle = {
                val pinned = chat.isPinnedIn(activeChatList)
                scope.launch { clientManager.togglePinChat(chat.id, activeChatList, !pinned) }
                selectedChatForMenu = null
            },
            onMuteToggle = {
                scope.launch { clientManager.toggleMuteChat(chat) }
                selectedChatForMenu = null
            },
            onArchiveToggle = {
                val archived = chat.isArchived()
                scope.launch { clientManager.setChatArchived(chat.id, !archived) }
                selectedChatForMenu = null
            },
            onMarkUnreadToggle = {
                val isUnread = chat.unreadCount > 0 || chat.isMarkedAsUnread
                scope.launch { clientManager.toggleMarkAsUnread(chat.id, !isUnread) }
                selectedChatForMenu = null
            },
            onDelete = {
                chatToDelete = chat
                selectedChatForMenu = null
            },
        )
    }

    chatToDelete?.let { chat ->
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text(strings.deleteChat) },
            text = { Text(String.format(strings.deleteChatConfirm, chat.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { clientManager.deleteChatHistory(chat.id, removeFromList = true, revoke = false) }
                        chatToDelete = null
                    },
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) { Text(strings.cancel) }
            },
        )
    }

    activeStoryTarget?.let { (cid, infos, title) ->
        val photo = if (cid == me?.id) me?.profilePhoto?.small else chatsMap[cid]?.chat?.photo?.small
        StoryViewerDialog(
            chatId = cid,
            posterTitle = title,
            posterPhoto = photo,
            initialStoryIds = infos.map { it.storyId },
            filesMap = filesMap,
            clientManager = clientManager,
            onDismiss = { activeStoryTarget = null },
        )
    }
}

@Composable
private fun FolderPillTab(
    selected: Boolean,
    label: String?,
    icon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val bg by animateColorAsState(
        targetValue = if (selected) tgColors.accent.copy(alpha = 0.2f) else tgColors.pillBackground,
        label = "folder_pill_bg",
    )
    val contentTint by animateColorAsState(
        targetValue = if (selected) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "folder_pill_tint",
    )
    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = contentTint, modifier = Modifier.size(18.dp))
        if (!label.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentTint,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
        if (badgeCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) tgColors.accent else tgColors.mutedBadge)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableChatRow(
    chat: TdApi.Chat,
    version: Long,
    activeChatList: TdApi.ChatList,
    swipeAction: ChatSwipeAction,
    threeLines: Boolean,
    meId: Long?,
    peerUser: TdApi.User?,
    supergroup: TdApi.Supergroup?,
    basicGroup: TdApi.BasicGroup?,
    resolvedAvatarFile: TdApi.File?,
    senderName: String?,
    typingLabel: String?,
    isSelected: Boolean,
    horizontalPadding: Dp,
    avatarSize: Dp,
    clientManager: TelegramClientManager,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeTriggered: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeTriggered()
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val bgColor = when (swipeAction) {
                ChatSwipeAction.DELETE -> MaterialTheme.colorScheme.error
                ChatSwipeAction.ARCHIVE -> Color(0xFF4A6074)
                ChatSwipeAction.PIN -> tgColors.accent
                ChatSwipeAction.UNREAD -> Color(0xFF4CAF50)
            }
            val icon = when (swipeAction) {
                ChatSwipeAction.DELETE -> Icons.Rounded.DeleteOutline
                ChatSwipeAction.ARCHIVE -> Icons.Rounded.Archive
                ChatSwipeAction.PIN -> Icons.Rounded.PushPin
                ChatSwipeAction.UNREAD -> Icons.Rounded.MarkChatUnread
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ChatListRow(
                chat = chat,
                version = version,
                activeChatList = activeChatList,
                threeLines = threeLines,
                meId = meId,
                peerUser = peerUser,
                supergroup = supergroup,
                basicGroup = basicGroup,
                resolvedAvatarFile = resolvedAvatarFile,
                senderName = senderName,
                typingLabel = typingLabel,
                isSelected = isSelected,
                horizontalPadding = horizontalPadding,
                avatarSize = avatarSize,
                clientManager = clientManager,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }
    }
}

@Composable
private fun ArchivedChatsRow(
    archivedCount: Int,
    previewTitles: String,
    horizontalPadding: Dp,
    avatarSize: Dp,
    onClick: () -> Unit,
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 10.dp)
            .testTag("archived_chats_row"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Archive,
                contentDescription = strings.archivedChats,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = strings.archivedChats,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = archivedCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = previewTitles,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatListRow(
    chat: TdApi.Chat,
    @Suppress("UNUSED_PARAMETER") version: Long,
    activeChatList: TdApi.ChatList,
    threeLines: Boolean,
    meId: Long?,
    peerUser: TdApi.User?,
    supergroup: TdApi.Supergroup?,
    @Suppress("UNUSED_PARAMETER") basicGroup: TdApi.BasicGroup?,
    resolvedAvatarFile: TdApi.File?,
    senderName: String?,
    typingLabel: String?,
    isSelected: Boolean,
    horizontalPadding: Dp,
    avatarSize: Dp,
    clientManager: TelegramClientManager,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val chatType = chat.type
    val isSavedMessages = chatType is TdApi.ChatTypePrivate && meId != null && chatType.userId == meId
    val isOnline = peerUser?.status is TdApi.UserStatusOnline
    val isVerified = peerUser?.verificationStatus?.isVerified == true || supergroup?.verificationStatus?.isVerified == true
    val isBot = peerUser?.type is TdApi.UserTypeBot
    val isChannel = (chatType as? TdApi.ChatTypeSupergroup)?.isChannel == true
    val isForum = supergroup?.isForum == true
    val isGroup = chatType is TdApi.ChatTypeBasicGroup || (chatType is TdApi.ChatTypeSupergroup && !chatType.isChannel)
    val isSecret = chatType is TdApi.ChatTypeSecret
    val isPinned = chat.isPinnedIn(activeChatList)
    val isMuted = chat.isMuted()

    val lastMsg = chat.lastMessage
    val draftText = (chat.draftMessage?.inputMessageText as? TdApi.InputMessageText)?.text?.text
    val miniThumbBytes = remember(lastMsg?.id, lastMsg?.content) { lastMsg?.content?.miniThumbnailBytes() }
    val rowBg by animateColorAsState(
        targetValue = if (isSelected) tgColors.accent.copy(alpha = 0.14f) else Color.Transparent,
        label = "selected_chat_bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(
                if (isSelected) {
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(rowBg)
                        .padding(horizontal = (horizontalPadding - 8.dp).coerceAtLeast(6.dp), vertical = 8.dp)
                } else {
                    Modifier
                        .background(rowBg)
                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
                }
            )
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TelegramAvatar(
            id = chat.id,
            title = if (isSavedMessages) strings.savedMessages else chat.title,
            photoFile = resolvedAvatarFile,
            clientManager = clientManager,
            size = avatarSize,
            isOnline = isOnline,
            isSavedMessages = isSavedMessages,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    isSecret -> Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                    )
                    isChannel -> Icon(
                        imageVector = Icons.Rounded.Campaign,
                        contentDescription = strings.channelsLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp).padding(end = 4.dp),
                    )
                    isForum -> Icon(
                        imageVector = Icons.Rounded.Forum,
                        contentDescription = strings.groupsLabel,
                        tint = tgColors.accent,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                    )
                    isGroup -> Icon(
                        imageVector = Icons.Rounded.Group,
                        contentDescription = strings.groupsLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp).padding(end = 4.dp),
                    )
                    isBot -> Icon(
                        imageVector = Icons.Rounded.SmartToy,
                        contentDescription = null,
                        tint = tgColors.accent,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                    )
                }

                Text(
                    text = if (isSavedMessages) strings.savedMessages else chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSecret) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Rounded.Verified, contentDescription = "Verified", tint = tgColors.accent, modifier = Modifier.size(16.dp))
                }
                if (isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = strings.mute,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                if (lastMsg != null && lastMsg.isOutgoing && !isSavedMessages) {
                    val isPending = lastMsg.sendingState is TdApi.MessageSendingStatePending
                    val isRead = lastMsg.id <= chat.lastReadOutboxMessageId
                    Icon(
                        imageVector = when {
                            isPending -> Icons.Rounded.Schedule
                            isRead -> Icons.Rounded.DoneAll
                            else -> Icons.Rounded.Check
                        },
                        contentDescription = null,
                        tint = if (isRead) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp).size(16.dp),
                    )
                }

                if (lastMsg != null) {
                    Text(
                        text = formatChatDate(lastMsg.date),
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = if (chat.unreadCount > 0 || chat.isMarkedAsUnread) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        typingLabel != null -> Text(
                            text = typingLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tgColors.accent,
                            maxLines = 1,
                        )
                        !draftText.isNullOrBlank() -> Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = TgDraftRed, fontWeight = FontWeight.Medium)) {
                                    append(strings.draftPrefix)
                                }
                                append(draftText.replace('\n', ' '))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (threeLines) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        lastMsg != null -> {
                            val senderPrefix = when {
                                lastMsg.isOutgoing && !isChannel -> strings.youPrefix
                                isGroup && !senderName.isNullOrBlank() -> "$senderName: "
                                else -> ""
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (miniThumbBytes != null) {
                                    AsyncImage(
                                        model = miniThumbBytes,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                    )
                                }
                                Text(
                                    text = buildAnnotatedString {
                                        if (senderPrefix.isNotEmpty()) {
                                            withStyle(
                                                SpanStyle(
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            ) {
                                                append(senderPrefix)
                                            }
                                        }
                                        append(lastMsg.content.previewText(strings))
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = if (threeLines) 2 else 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (chat.unreadMentionCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(tgColors.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("@", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (chat.unreadCount > 0 || chat.isMarkedAsUnread) {
                    val badgeColor = if (isMuted) tgColors.mutedBadge else tgColors.unreadBadge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (chat.unreadCount > 0) chat.unreadCount.toString() else " ",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else if (isPinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = strings.pinChat,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    localResults: List<TdApi.Chat>,
    publicResults: List<TdApi.Chat>,
    meId: Long?,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    basicGroupsMap: Map<Long, TdApi.BasicGroup>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    horizontalPadding: Dp,
    avatarSize: Dp,
    onOpenChat: (Long) -> Unit,
) {
    val strings = LocalStrings.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
    ) {
        if (localResults.isNotEmpty()) {
            item(contentType = "search_header") {
                Text(
                    text = strings.localSearchHeader,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                )
            }
            items(localResults, key = { "local_${it.id}" }, contentType = { "search_row" }) { chat ->
                val chatType = chat.type
                ChatListRow(
                    chat = chat,
                    version = chatsMap[chat.id]?.version ?: 0L,
                    activeChatList = TdApi.ChatListMain(),
                    threeLines = false,
                    meId = meId,
                    peerUser = (chatType as? TdApi.ChatTypePrivate)?.let { usersMap[it.userId] },
                    supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] },
                    basicGroup = (chatType as? TdApi.ChatTypeBasicGroup)?.let { basicGroupsMap[it.basicGroupId] },
                    resolvedAvatarFile = chat.photo?.small?.let { filesMap[it.id] ?: it },
                    senderName = null,
                    typingLabel = null,
                    isSelected = false,
                    horizontalPadding = horizontalPadding,
                    avatarSize = avatarSize,
                    clientManager = clientManager,
                    onClick = { onOpenChat(chat.id) },
                    onLongClick = {},
                )
            }
        }
        if (publicResults.isNotEmpty()) {
            item(contentType = "search_header") {
                Text(
                    text = strings.globalSearchHeader,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                )
            }
            items(publicResults, key = { "public_${it.id}" }, contentType = { "search_row" }) { chat ->
                val chatType = chat.type
                ChatListRow(
                    chat = chat,
                    version = chatsMap[chat.id]?.version ?: 0L,
                    activeChatList = TdApi.ChatListMain(),
                    threeLines = false,
                    meId = meId,
                    peerUser = (chatType as? TdApi.ChatTypePrivate)?.let { usersMap[it.userId] },
                    supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] },
                    basicGroup = (chatType as? TdApi.ChatTypeBasicGroup)?.let { basicGroupsMap[it.basicGroupId] },
                    resolvedAvatarFile = chat.photo?.small?.let { filesMap[it.id] ?: it },
                    senderName = null,
                    typingLabel = null,
                    isSelected = false,
                    horizontalPadding = horizontalPadding,
                    avatarSize = avatarSize,
                    clientManager = clientManager,
                    onClick = { onOpenChat(chat.id) },
                    onLongClick = {},
                )
            }
        }
        if (localResults.isEmpty() && publicResults.isEmpty()) {
            item(contentType = "search_empty") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = strings.noSearchResults,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatsState(onOpenContacts: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = strings.noChatsYet, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.noChatsSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onOpenContacts) {
            Text(strings.startMessaging)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContextMenuSheet(
    chat: TdApi.Chat,
    activeChatList: TdApi.ChatList,
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit,
    onMuteToggle: () -> Unit,
    onArchiveToggle: () -> Unit,
    onMarkUnreadToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalStrings.current
    val isPinned = chat.isPinnedIn(activeChatList)
    val isMuted = chat.isMuted()
    val isArchived = chat.isArchived()
    val isUnread = chat.unreadCount > 0 || chat.isMarkedAsUnread

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = chat.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ChatMenuOption(Icons.Rounded.PushPin, if (isPinned) strings.unpinChat else strings.pinChat, onClick = onPinToggle)
            ChatMenuOption(
                if (isMuted) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                if (isMuted) strings.unmute else strings.mute,
                onClick = onMuteToggle,
            )
            ChatMenuOption(
                if (isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                if (isArchived) strings.unarchive else strings.archive,
                onClick = onArchiveToggle,
            )
            ChatMenuOption(
                if (isUnread) Icons.Rounded.MarkChatRead else Icons.Rounded.MarkChatUnread,
                if (isUnread) strings.markRead else strings.markUnread,
                onClick = onMarkUnreadToggle,
            )
            ChatMenuOption(
                Icons.Rounded.DeleteOutline,
                strings.deleteChat,
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ChatMenuOption(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

@Composable
private fun GhostHeaderIcon(
    tint: Color,
    glowing: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val ghostPath = Path().apply {
            moveTo(w * 0.18f, h * 0.88f)
            lineTo(w * 0.18f, h * 0.44f)
            cubicTo(
                w * 0.18f, h * 0.16f,
                w * 0.82f, h * 0.16f,
                w * 0.82f, h * 0.44f,
            )
            lineTo(w * 0.82f, h * 0.88f)
            quadraticTo(w * 0.72f, h * 0.77f, w * 0.62f, h * 0.88f)
            quadraticTo(w * 0.50f, h * 0.96f, w * 0.38f, h * 0.88f)
            quadraticTo(w * 0.28f, h * 0.77f, w * 0.18f, h * 0.88f)
            close()
        }
        if (glowing) {
            drawCircle(
                color = tint.copy(alpha = 0.22f),
                radius = w * 0.56f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            )
            drawPath(path = ghostPath, color = tint)
            drawCircle(
                color = Color(0xFF0E1A2B),
                radius = w * 0.085f,
                center = androidx.compose.ui.geometry.Offset(w * 0.39f, h * 0.46f),
            )
            drawCircle(
                color = Color(0xFF0E1A2B),
                radius = w * 0.085f,
                center = androidx.compose.ui.geometry.Offset(w * 0.61f, h * 0.46f),
            )
        } else {
            drawPath(
                path = ghostPath,
                color = tint,
                style = Stroke(width = (w * 0.095f).coerceAtLeast(1.8f)),
            )
            drawCircle(
                color = tint,
                radius = w * 0.075f,
                center = androidx.compose.ui.geometry.Offset(w * 0.39f, h * 0.46f),
            )
            drawCircle(
                color = tint,
                radius = w * 0.075f,
                center = androidx.compose.ui.geometry.Offset(w * 0.61f, h * 0.46f),
            )
        }
    }
}

