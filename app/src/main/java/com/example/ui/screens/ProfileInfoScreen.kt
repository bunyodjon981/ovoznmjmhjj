package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.tdlib.ChatEntry
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.tdlib.formatStatus
import com.example.tdlib.getChatPostedStories
import com.example.tdlib.getReceivedGifts
import com.example.tdlib.isMuted
import com.example.tdlib.primaryUsername
import com.example.ui.components.PremiumStatusBadge
import com.example.ui.components.ProfileGiftsSection
import com.example.ui.components.ProfilePhotoLightbox
import com.example.ui.components.ProfileStoriesGridSection
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    chatId: Long,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    basicGroupsMap: Map<Long, TdApi.BasicGroup>,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    activeStories: Map<Long, TdApi.ChatActiveStories> = emptyMap(),
    clientManager: TelegramClientManager,
    onBack: () -> Unit,
    onOpenEditChat: (Long) -> Unit,
    onOpenChat: (Long) -> Unit,
    onLeftChat: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val chat = chatsMap[chatId]?.chat
    val chatType = chat?.type
    val peerUser = (chatType as? TdApi.ChatTypePrivate)?.let { usersMap[it.userId] }
    val supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] }
    val basicGroup = (chatType as? TdApi.ChatTypeBasicGroup)?.let { basicGroupsMap[it.basicGroupId] }
    val isChannel = (chatType as? TdApi.ChatTypeSupergroup)?.isChannel == true
    val isGroup = chatType is TdApi.ChatTypeBasicGroup || (chatType is TdApi.ChatTypeSupergroup && !chatType.isChannel)

    val sgStatus = supergroup?.status
    val bgStatus = basicGroup?.status
    val isOwner = sgStatus is TdApi.ChatMemberStatusCreator || bgStatus is TdApi.ChatMemberStatusCreator
    val isAdmin = sgStatus is TdApi.ChatMemberStatusAdministrator || bgStatus is TdApi.ChatMemberStatusAdministrator
    val canEdit = (isChannel || isGroup) && (isOwner || isAdmin)
    val isMember = when {
        sgStatus != null -> sgStatus !is TdApi.ChatMemberStatusLeft && sgStatus !is TdApi.ChatMemberStatusBanned
        bgStatus != null -> bgStatus !is TdApi.ChatMemberStatusLeft && bgStatus !is TdApi.ChatMemberStatusBanned
        else -> true
    }

    var userFullInfo by remember(chatId) { mutableStateOf<TdApi.UserFullInfo?>(null) }
    var supergroupFullInfo by remember(chatId) { mutableStateOf<TdApi.SupergroupFullInfo?>(null) }
    var basicGroupFullInfo by remember(chatId) { mutableStateOf<TdApi.BasicGroupFullInfo?>(null) }
    var members by remember(chatId) { mutableStateOf<List<TdApi.ChatMember>>(emptyList()) }
    var postedStories by remember(chatId) { mutableStateOf<List<TdApi.Story>>(emptyList()) }
    var storiesLoading by remember(chatId) { mutableStateOf(true) }
    var receivedGifts by remember(chatId) { mutableStateOf<List<TdApi.ReceivedGift>>(emptyList()) }
    var giftsLoading by remember(chatId) { mutableStateOf(true) }
    var selectedProfileTab by remember { mutableIntStateOf(0) }
    var showProfileLightbox by remember { mutableStateOf(false) }
    var openStoryViewerIndex by remember { mutableStateOf<Int?>(null) }
    var showQrDialog by remember { mutableStateOf<String?>(null) }
    var showAddMemberSheet by remember { mutableStateOf(false) }

    val ownerSender = remember(chatId, peerUser?.id) {
        if (peerUser != null) TdApi.MessageSenderUser(peerUser.id) else TdApi.MessageSenderChat(chatId)
    }
    val chatActiveStories = activeStories[chatId]?.stories?.toList().orEmpty()

    LaunchedEffect(chatId) {
        when (chatType) {
            is TdApi.ChatTypePrivate -> {
                clientManager.getUserFullInfo(chatType.userId).onSuccess { userFullInfo = it }
            }
            is TdApi.ChatTypeSupergroup -> {
                clientManager.getSupergroup(chatType.supergroupId)
                clientManager.getSupergroupFullInfo(chatType.supergroupId).onSuccess { supergroupFullInfo = it }
                clientManager.getSupergroupMembers(chatType.supergroupId).onSuccess { members = it }
            }
            is TdApi.ChatTypeBasicGroup -> {
                clientManager.send(TdApi.GetBasicGroupFullInfo(chatType.basicGroupId)).onSuccess {
                    basicGroupFullInfo = it
                    members = it.members.toList()
                }
            }
        }
        clientManager.getChatPostedStories(chatId).onSuccess { postedStories = it }
        storiesLoading = false
        clientManager.getReceivedGifts(ownerSender).onSuccess { receivedGifts = it }
        giftsLoading = false
    }

    val usernameStr = peerUser?.primaryUsername() ?: supergroup?.primaryUsername()
    val inviteLink = supergroupFullInfo?.inviteLink?.inviteLink
        ?: basicGroupFullInfo?.inviteLink?.inviteLink
        ?: usernameStr?.let { "https://t.me/$it" }

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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = chat?.title ?: strings.infoSection,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (canEdit) {
                        IconButton(
                            onClick = { onOpenEditChat(chatId) },
                            modifier = Modifier.testTag("profile_edit_button"),
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                        }
                    }
                    if (!inviteLink.isNullOrBlank()) {
                        IconButton(onClick = { showQrDialog = inviteLink }) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = "QR")
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Centered Hero Banner + Action Pills matching Telegram screenshots
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tgColors.cardBackground)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val hasActiveStories = chatActiveStories.isNotEmpty()
                    val ringModifier = if (hasActiveStories) {
                        Modifier
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(listOf(Color(0xFF00D2FF), Color(0xFF3A7BD5), Color(0xFF8A2BE2))),
                                shape = CircleShape,
                            )
                            .padding(4.dp)
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(ringModifier)
                            .clickable {
                                when {
                                    hasActiveStories -> openStoryViewerIndex = -1
                                    chat?.photo != null -> showProfileLightbox = true
                                }
                            },
                    ) {
                        TelegramAvatar(
                            id = chatId,
                            title = chat?.title ?: strings.infoSection,
                            photoFile = chat?.photo?.big ?: chat?.photo?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 86.dp,
                            isOnline = peerUser?.status is TdApi.UserStatusOnline,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = chat?.title ?: "—",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        if (peerUser != null && (peerUser.isPremium || peerUser.emojiStatus != null)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            PremiumStatusBadge(peerUser, customEmojiStickers, filesMap, clientManager, size = 18.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val sub = when {
                        peerUser != null -> peerUser.status.formatStatus(strings)
                        isChannel -> {
                            val typeText = if (!usernameStr.isNullOrBlank()) strings.publicChannel else strings.privateChannel
                            val cnt = supergroup?.memberCount ?: 0
                            "$typeText · ${String.format(strings.subscribersCount, cnt)}"
                        }
                        isGroup -> {
                            val cnt = supergroup?.memberCount ?: basicGroup?.memberCount ?: members.size
                            val typeText = if (!usernameStr.isNullOrBlank()) strings.publicGroup else strings.privateGroup
                            "$typeText · ${String.format(strings.membersCount, cnt)}"
                        }
                        else -> ""
                    }
                    if (sub.isNotBlank()) {
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ProfileActionPill(
                            icon = if (isChannel) Icons.Rounded.Videocam else Icons.AutoMirrored.Rounded.Chat,
                            label = if (isChannel) strings.liveStream else strings.messagesLabel,
                            modifier = Modifier.weight(1f),
                            onClick = { onBack() },
                        )
                        if (chat != null) {
                            ProfileActionPill(
                                icon = if (chat.isMuted()) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                label = if (chat.isMuted()) strings.unmute else strings.mute,
                                modifier = Modifier.weight(1f),
                                onClick = { scope.launch { clientManager.toggleMuteChat(chat) } },
                            )
                        }
                        val linkedId = supergroupFullInfo?.linkedChatId ?: 0L
                        if (linkedId != 0L) {
                            ProfileActionPill(
                                icon = Icons.AutoMirrored.Rounded.Chat,
                                label = strings.discussion,
                                modifier = Modifier.weight(1f),
                                onClick = { onOpenChat(linkedId) },
                            )
                        } else if (!inviteLink.isNullOrBlank()) {
                            ProfileActionPill(
                                icon = Icons.Rounded.Share,
                                label = "Ulashish",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    clipboard.setText(AnnotatedString(inviteLink))
                                    scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                                },
                            )
                        }
                        if (isChannel || isGroup) {
                            ProfileActionPill(
                                icon = if (isMember) Icons.AutoMirrored.Rounded.ExitToApp else Icons.Rounded.PersonAdd,
                                label = if (isMember) "Tark etish" else "Qoʻshilish",
                                tint = if (isMember) MaterialTheme.colorScheme.error else tgColors.accent,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        if (isMember) {
                                            clientManager.leaveChat(chatId)
                                            onLeftChat()
                                        } else {
                                            clientManager.joinChat(chatId)
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }

            // Card 1: Description / Invite Link / Phone / Username / Notifications
            item {
                val phone = peerUser?.phoneNumber?.takeIf { it.isNotBlank() }?.let { "+$it" }
                val bio = userFullInfo?.bio?.text?.takeIf { it.isNotBlank() }
                    ?: supergroupFullInfo?.description?.takeIf { it.isNotBlank() }
                    ?: basicGroupFullInfo?.description?.takeIf { it.isNotBlank() }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    if (phone != null) {
                        InfoRow(
                            icon = Icons.Rounded.Call,
                            value = phone,
                            label = strings.mobileLabel,
                            onClick = {
                                clipboard.setText(AnnotatedString(phone))
                                scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                            },
                        )
                    }
                    if (!usernameStr.isNullOrBlank()) {
                        InfoRow(
                            icon = Icons.Rounded.AlternateEmail,
                            value = "@$usernameStr",
                            label = strings.usernameLabel,
                            onClick = {
                                clipboard.setText(AnnotatedString("@$usernameStr"))
                                scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                            },
                        )
                    }
                    if (bio != null) {
                        InfoRow(
                            icon = Icons.Rounded.Info,
                            value = bio,
                            label = if (isChannel || isGroup) strings.descriptionLabel else strings.bioLabel,
                            onClick = {
                                clipboard.setText(AnnotatedString(bio))
                                scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                            },
                        )
                    }
                    if (!inviteLink.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboard.setText(AnnotatedString(inviteLink))
                                    scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Link, contentDescription = strings.inviteLinkLabel, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = inviteLink, style = MaterialTheme.typography.bodyLarge, color = tgColors.accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = strings.inviteLinkLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { showQrDialog = inviteLink }) {
                                Icon(Icons.Rounded.QrCode2, contentDescription = "QR", tint = tgColors.accent)
                            }
                        }
                    }

                    if (chat != null) {
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { scope.launch { clientManager.toggleMuteChat(chat) } }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(18.dp))
                            Text(strings.notificationsTitle, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(checked = !chat.isMuted(), onCheckedChange = { scope.launch { clientManager.toggleMuteChat(chat) } })
                        }
                    }
                }
            }

            // Card 2: Owner / Admin Management Card (Obunachilar, Adminlar, Kanal/Guruh sozlamalari)
            if (canEdit && (supergroup != null || basicGroup != null)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground),
                    ) {
                        val subCount = supergroup?.memberCount ?: basicGroup?.memberCount ?: members.size
                        val adminCount = supergroupFullInfo?.administratorCount ?: members.count {
                            it.status is TdApi.ChatMemberStatusCreator || it.status is TdApi.ChatMemberStatusAdministrator
                        }
                        AdminStatRow(
                            icon = Icons.Rounded.Group,
                            label = if (isChannel) strings.subscribersLabel else strings.membersLabel,
                            count = subCount.toString(),
                            onClick = { onOpenEditChat(chatId) },
                        )
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        AdminStatRow(
                            icon = Icons.Rounded.AdminPanelSettings,
                            label = strings.adminsLabel,
                            count = adminCount.toString(),
                            onClick = { onOpenEditChat(chatId) },
                        )
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        AdminStatRow(
                            icon = Icons.Rounded.Tune,
                            label = if (isChannel) strings.channelSettings else strings.groupSettings,
                            count = "",
                            accent = true,
                            onClick = { onOpenEditChat(chatId) },
                        )
                    }
                }
            }

            // Group Members List + Ega / Admin badges + Add Members
            if (isGroup && (members.isNotEmpty() || canEdit)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground)
                            .padding(top = 8.dp),
                    ) {
                        if (canEdit) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAddMemberSheet = true }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = tgColors.accent)
                                Spacer(modifier = Modifier.width(18.dp))
                                Text(strings.addMembers, style = MaterialTheme.typography.bodyLarge, color = tgColors.accent, fontWeight = FontWeight.Medium)
                            }
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        }
                        Text(
                            text = String.format(strings.membersCount, members.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = tgColors.accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }

                items(members, key = { (it.memberId as? TdApi.MessageSenderUser)?.userId ?: it.hashCode().toLong() }) { member ->
                    val uid = (member.memberId as? TdApi.MessageSenderUser)?.userId
                    val user = uid?.let { usersMap[it] }
                    val roleBadge = when (member.status) {
                        is TdApi.ChatMemberStatusCreator -> strings.ownerBadge
                        is TdApi.ChatMemberStatusAdministrator -> strings.adminBadge
                        else -> null
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .background(tgColors.cardBackground)
                            .clickable(enabled = uid != null) {
                                if (uid != null) {
                                    scope.launch {
                                        clientManager.createPrivateChat(uid).onSuccess { c -> onOpenChat(c.id) }
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TelegramAvatar(
                            id = uid ?: 0L,
                            title = user.displayName(),
                            photoFile = user?.profilePhoto?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 44.dp,
                            isOnline = user?.status is TdApi.UserStatusOnline,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = user.displayName(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                if (user != null && (user.isPremium || user.emojiStatus != null)) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PremiumStatusBadge(user, customEmojiStickers, filesMap, clientManager, size = 14.dp)
                                }
                            }
                            Text(
                                text = user?.status.formatStatus(strings),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (roleBadge != null) {
                            Text(
                                text = roleBadge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB388FF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFB388FF).copy(alpha = 0.14f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            // Stories (Postlar) & Gifts (Hadyalar) Tabs matching Telegram Profile
            if (postedStories.isNotEmpty() || receivedGifts.isNotEmpty() || peerUser != null || isChannel) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground)
                            .padding(vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(strings.postsTab, strings.giftsTab).forEachIndexed { idx, label ->
                                val selected = selectedProfileTab == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) tgColors.accent.copy(alpha = 0.18f) else tgColors.pillBackground)
                                        .clickable { selectedProfileTab = idx }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (selectedProfileTab == 0) {
                            ProfileStoriesGridSection(
                                stories = postedStories,
                                isLoading = storiesLoading,
                                hintText = null,
                                showAddButton = false,
                                filesMap = filesMap,
                                clientManager = clientManager,
                                onStoryClick = { idx -> openStoryViewerIndex = idx },
                                onAddStoryClick = {},
                            )
                        } else {
                            ProfileGiftsSection(
                                gifts = receivedGifts,
                                isLoading = giftsLoading,
                                isOwnProfile = false,
                                targetOwnerId = ownerSender,
                                usersMap = usersMap,
                                filesMap = filesMap,
                                clientManager = clientManager,
                                onGiftsUpdated = {
                                    scope.launch {
                                        clientManager.getReceivedGifts(ownerSender).onSuccess { receivedGifts = it }
                                    }
                                },
                                onStatusMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProfileLightbox) {
        ProfilePhotoLightbox(
            title = chat?.title ?: strings.infoSection,
            photoFile = chat?.photo?.big ?: chat?.photo?.small,
            filesMap = filesMap,
            clientManager = clientManager,
            onDismiss = { showProfileLightbox = false },
        )
    }

    openStoryViewerIndex?.let { idx ->
        val useActive = idx < 0
        StoryViewerDialog(
            chatId = chatId,
            posterTitle = chat?.title ?: strings.infoSection,
            posterPhoto = chat?.photo?.small,
            initialStories = if (useActive) emptyList() else postedStories,
            initialStoryIds = if (useActive) chatActiveStories.map { it.storyId } else emptyList(),
            initialIndex = if (useActive) 0 else idx,
            filesMap = filesMap,
            clientManager = clientManager,
            onStoryChanged = {
                scope.launch {
                    clientManager.getChatPostedStories(chatId).onSuccess { postedStories = it }
                }
            },
            onDismiss = { openStoryViewerIndex = null },
        )
    }

    showQrDialog?.let { link ->
        AlertDialog(
            onDismissRequest = { showQrDialog = null },
            title = { Text(chat?.title ?: strings.inviteLinkLabel) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.QrCode2, contentDescription = "QR", tint = Color.Black, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(link, style = MaterialTheme.typography.bodySmall, color = tgColors.accent, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(link))
                        showQrDialog = null
                        scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                    },
                ) {
                    Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.copy)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = null }) { Text(strings.cancel) }
            },
        )
    }

    if (showAddMemberSheet) {
        val existingIds = members.mapNotNull { (it.memberId as? TdApi.MessageSenderUser)?.userId }.toSet()
        val candidates = usersMap.values.filter { it.id !in existingIds && it.type is TdApi.UserTypeRegular }.take(40)
        ModalBottomSheet(onDismissRequest = { showAddMemberSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text(
                    text = strings.addMembers,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
                HorizontalDivider(color = tgColors.divider)
                LazyColumn {
                    items(candidates, key = { it.id }) { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddMemberSheet = false
                                    scope.launch {
                                        clientManager.send(TdApi.AddChatMember(chatId, u.id, 50))
                                            .onSuccess {
                                                if (chatType is TdApi.ChatTypeSupergroup) {
                                                    clientManager.getSupergroupMembers(chatType.supergroupId).onSuccess { members = it }
                                                }
                                                snackbarHostState.showSnackbar(strings.savedToast)
                                            }
                                            .onFailure {
                                                snackbarHostState.showSnackbar(it.message ?: "Error")
                                            }
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TelegramAvatar(u.id, u.displayName(), u.profilePhoto?.small, filesMap, clientManager, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(u.displayName(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileActionPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tgColors.pillBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdminStatRow(
    icon: ImageVector,
    label: String,
    count: String,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (accent) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (accent) tgColors.accent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (count.isNotEmpty()) {
            Text(count, style = MaterialTheme.typography.bodyMedium, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    value: String,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(18.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
