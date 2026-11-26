package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageSnapshot
import com.example.data.ConnectedChannelInfo
import com.example.data.GhostlyAccountRepository
import com.example.data.GhostlyUser
import com.example.data.LocalStrings
import com.example.data.SupportThread
import com.example.data.UserChatSummary
import com.example.data.adminAllChatsTab
import com.example.data.adminAllUserChats
import com.example.data.adminBotsTab
import com.example.data.adminChannelsTab
import com.example.data.adminChatDetailsTitle
import com.example.data.adminChatOpenFailed
import com.example.data.adminChatTypeLabel
import com.example.data.adminEnterChatBtn
import com.example.data.adminGroupsTab
import com.example.data.adminIncomingMessagesTitle
import com.example.data.adminIncomingTag
import com.example.data.adminMembersCountLabel
import com.example.data.adminNoChatMessages
import com.example.data.adminNoUserChats
import com.example.data.adminOutgoingTag
import com.example.data.adminPrivateChatsTab
import com.example.data.adminRoleLabel
import com.example.data.adminSearchUserChatsHint
import com.example.data.adminSpam24h
import com.example.data.adminSpamActiveBadge
import com.example.data.adminSupportMessagesWithUser
import com.example.data.adminUnreadLabel
import com.example.data.adminUnspamNow
import com.example.data.userIdLabel
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.tdlib.openPublicChatByUsername
import com.example.tdlib.previewText
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserFullProfileScreen(
    user: GhostlyUser,
    supportThread: SupportThread?,
    repository: GhostlyAccountRepository,
    clientManager: TelegramClientManager,
    onOpenSupportThread: (SupportThread) -> Unit,
    onOpenChatInApp: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var inspectedChat by remember { mutableStateOf<UserChatSummary?>(null) }
    var chatSearchQuery by remember { mutableStateOf("") }
    var chatFilterIndex by remember { mutableIntStateOf(0) }
    var isUpdatingSpam by remember { mutableStateOf(false) }

    BackHandler {
        if (inspectedChat != null) inspectedChat = null else onBack()
    }

    inspectedChat?.let { targetChat ->
        AdminChatInspectorScreen(
            chatSummary = targetChat,
            clientManager = clientManager,
            onOpenChatInApp = onOpenChatInApp,
            onBack = { inspectedChat = null },
        )
        return
    }

    val allUserChats = remember(user.chatSummaries, user.channelSummaries) {
        if (user.chatSummaries.isNotEmpty()) {
            user.chatSummaries
        } else {
            user.channelSummaries.mapNotNull { ConnectedChannelInfo.fromSummaryString(it) }.map { ch ->
                UserChatSummary(
                    chatId = ch.chatId,
                    title = ch.title,
                    chatType = if (ch.roleLabel == "Guruh") "group" else "channel",
                    username = ch.username,
                    memberCount = ch.memberCount,
                    roleLabel = ch.roleLabel,
                )
            }
        }
    }

    val filteredChats = remember(allUserChats, chatFilterIndex, chatSearchQuery) {
        val byType = when (chatFilterIndex) {
            1 -> allUserChats.filter { it.chatType == "private" || it.chatType == "secret" }
            2 -> allUserChats.filter { it.chatType == "group" }
            3 -> allUserChats.filter { it.chatType == "channel" }
            4 -> allUserChats.filter { it.chatType == "bot" }
            else -> allUserChats
        }
        val q = chatSearchQuery.trim().lowercase()
        if (q.isEmpty()) byType
        else byType.filter { c ->
            c.title.lowercase().contains(q) ||
                c.username.lowercase().contains(q) ||
                c.lastMessageText.lowercase().contains(q) ||
                c.chatId.toString().contains(q)
        }
    }

    val isSpammed = user.isActivelySpammed()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.headerBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_user_profile_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = user.fullName.ifBlank { "Telegram User" },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "ID: ${user.telegramUserId} · ${allUserChats.size} chat",
                            style = MaterialTheme.typography.labelSmall,
                            color = tgColors.accent,
                            maxLines = 1,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Full Profile Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(tgColors.accent, Color(0xFF00D2FF)))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = user.fullName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.fullName.ifBlank { "Telegram User" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (user.premiumAccount) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Rounded.Star, null, tint = Color(0xFF7B61FF), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = listOfNotNull(
                                        user.username.takeIf { it.isNotBlank() }?.let { "@$it" },
                                        user.phoneNumber.takeIf { it.isNotBlank() },
                                    ).joinToString(" · ").ifBlank { "ID: ${user.telegramUserId}" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isSpammed) {
                                Text(
                                    text = "${strings.adminSpamActiveBadge} (${formatRemainingDuration(user.spamUntilMs)})",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEF4747))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }

                        HorizontalDivider(color = tgColors.divider)

                        if (user.phoneNumber.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.Call, strings.mobileLabel, user.phoneNumber)
                        }
                        if (user.username.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.AlternateEmail, strings.usernameLabel, "@${user.username}")
                        }
                        DetailInfoRow(Icons.Rounded.Tag, strings.userIdLabel, user.telegramUserId.toString())
                        if (user.bio.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.Info, strings.bioLabel, user.bio)
                        }

                        Button(
                            onClick = {
                                isUpdatingSpam = true
                                scope.launch {
                                    val res = repository.setUserSpamStatus(
                                        targetAccountKey = user.userId,
                                        banned = !isSpammed,
                                        durationHours = 24L,
                                    )
                                    isUpdatingSpam = false
                                    res.onSuccess {
                                        snackbarHostState.showSnackbar(
                                            if (!isSpammed) strings.adminSpam24h else strings.adminUnspamNow
                                        )
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar(err.localizedMessage ?: "Error")
                                    }
                                }
                            },
                            enabled = !isUpdatingSpam,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpammed) Color(0xFF4CAF50) else Color(0xFFEF4747),
                                contentColor = Color.White,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_detail_spam_toggle"),
                        ) {
                            if (isUpdatingSpam) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (isSpammed) Icons.Rounded.CheckCircle else Icons.Rounded.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSpammed) strings.adminUnspamNow else strings.adminSpam24h,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                // +47777 Support Thread Card (incoming support messages from this user)
                if (supportThread != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tgColors.accent.copy(alpha = 0.14f))
                                .clickable { onOpenSupportThread(supportThread) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(tgColors.accent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.SupportAgent, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.adminSupportMessagesWithUser,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tgColors.accent,
                                )
                                Text(
                                    text = supportThread.lastMessageText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (supportThread.unreadByAdmin) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(tgColors.accent),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = tgColors.accent)
                        }
                    }
                }

                // All User Chats Section Header + Search + Category Filter Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${strings.adminAllUserChats} (${allUserChats.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tgColors.accent,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        OutlinedTextField(
                            value = chatSearchQuery,
                            onValueChange = { chatSearchQuery = it },
                            placeholder = { Text(strings.adminSearchUserChatsHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                if (chatSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { chatSearchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_user_chats_search"),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val counts = remember(allUserChats) {
                                listOf(
                                    allUserChats.size,
                                    allUserChats.count { it.chatType == "private" || it.chatType == "secret" },
                                    allUserChats.count { it.chatType == "group" },
                                    allUserChats.count { it.chatType == "channel" },
                                    allUserChats.count { it.chatType == "bot" },
                                )
                            }
                            val labels = listOf(
                                strings.adminAllChatsTab,
                                strings.adminPrivateChatsTab,
                                strings.adminGroupsTab,
                                strings.adminChannelsTab,
                                strings.adminBotsTab,
                            )
                            labels.forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = chatFilterIndex == idx,
                                    onClick = { chatFilterIndex = idx },
                                    label = { Text("$label (${counts[idx]})") },
                                )
                            }
                        }
                    }
                }

                if (filteredChats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = strings.adminNoUserChats,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(filteredChats, key = { "${it.chatId}_${it.title}" }) { chat ->
                        AdminUserChatRow(
                            chat = chat,
                            onClick = { inspectedChat = chat },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUserChatRow(
    chat: UserChatSummary,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val (typeIcon, iconTint) = when (chat.chatType) {
        "channel" -> Icons.Rounded.Campaign to Color(0xFF4C9BFF)
        "group" -> Icons.Rounded.Group to Color(0xFF4CAF50)
        "bot" -> Icons.Rounded.SmartToy to Color(0xFFAB47BC)
        "secret" -> Icons.Rounded.Lock to Color(0xFF26A69A)
        else -> Icons.Rounded.Person to tgColors.accent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("admin_user_chat_row_${chat.chatId}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(typeIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (chat.lastMessageTimeSec > 0) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.lastMessageTimeSec * 1000L)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chat.unreadCount > 0) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val meta = listOfNotNull(
                chat.roleLabel.takeIf { it.isNotBlank() },
                chat.username.takeIf { it.isNotBlank() },
                if (chat.memberCount > 0) "${chat.memberCount} aʼzo" else null,
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = tgColors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (chat.lastMessageText.isNotBlank()) {
                val prefix = when {
                    !chat.lastMessageIncoming -> "Siz: "
                    chat.lastMessageSender.isNotBlank() -> "${chat.lastMessageSender}: "
                    else -> ""
                }
                Text(
                    text = "$prefix${chat.lastMessageText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (chat.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(tgColors.accent)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(
                    text = chat.unreadCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminChatInspectorScreen(
    chatSummary: UserChatSummary,
    clientManager: TelegramClientManager,
    onOpenChatInApp: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var liveMessages by remember(chatSummary.chatId) { mutableStateOf<List<ChatMessageSnapshot>>(emptyList()) }
    var isLoadingLive by remember(chatSummary.chatId) { mutableStateOf(true) }
    var resolvedChatId by remember(chatSummary.chatId) { mutableStateOf<Long?>(null) }

    LaunchedEffect(chatSummary.chatId, chatSummary.username, chatSummary.peerUserId) {
        isLoadingLive = true
        var foundChat: TdApi.Chat? = null
        if (chatSummary.chatId != 0L) {
            foundChat = clientManager.getChat(chatSummary.chatId).getOrNull()
        }
        if (foundChat == null && chatSummary.username.isNotBlank()) {
            foundChat = clientManager.openPublicChatByUsername(chatSummary.username).getOrNull()
        }
        if (foundChat == null && chatSummary.peerUserId != 0L) {
            foundChat = clientManager.createPrivateChat(chatSummary.peerUserId).getOrNull()
        }
        resolvedChatId = foundChat?.id

        if (foundChat != null) {
            val hist = clientManager.getChatHistory(foundChat.id, 0L, 40).getOrDefault(emptyList())
            val combined = if (hist.isNotEmpty() && hist.size < 15) {
                val older = clientManager.getChatHistory(foundChat.id, hist.last().id, 40).getOrDefault(emptyList())
                (hist + older).distinctBy { it.id }
            } else {
                hist
            }
            val usersMap = clientManager.users.value
            val chatsMap = clientManager.chats.value
            liveMessages = combined.sortedBy { it.date }.map { m ->
                val senderName = if (m.isOutgoing) {
                    "Siz"
                } else {
                    when (val s = m.senderId) {
                        is TdApi.MessageSenderUser -> usersMap[s.userId]?.displayName().orEmpty()
                        is TdApi.MessageSenderChat -> chatsMap[s.chatId]?.chat?.title.orEmpty()
                        else -> ""
                    }.ifBlank { foundChat.title }
                }
                ChatMessageSnapshot(
                    id = m.id,
                    senderName = senderName,
                    text = m.content.previewText(strings),
                    timeSec = m.date,
                    isIncoming = !m.isOutgoing,
                )
            }
        }
        isLoadingLive = false
    }

    val displayedMessages = remember(liveMessages, chatSummary) {
        if (liveMessages.isNotEmpty()) {
            liveMessages
        } else if (chatSummary.recentMessages.isNotEmpty()) {
            chatSummary.recentMessages.sortedBy { it.timeSec }
        } else if (chatSummary.lastMessageText.isNotBlank()) {
            listOf(
                ChatMessageSnapshot(
                    id = 1L,
                    senderName = chatSummary.lastMessageSender.ifBlank { chatSummary.title },
                    text = chatSummary.lastMessageText,
                    timeSec = chatSummary.lastMessageTimeSec,
                    isIncoming = chatSummary.lastMessageIncoming,
                )
            )
        } else {
            emptyList()
        }
    }

    fun attemptOpenRealChat() {
        val directId = resolvedChatId
        if (directId != null) {
            onOpenChatInApp(directId)
            return
        }
        scope.launch {
            val target = when {
                chatSummary.chatId != 0L && clientManager.getChat(chatSummary.chatId).isSuccess -> chatSummary.chatId
                chatSummary.username.isNotBlank() -> clientManager.openPublicChatByUsername(chatSummary.username).getOrNull()?.id
                chatSummary.peerUserId != 0L -> clientManager.createPrivateChat(chatSummary.peerUserId).getOrNull()?.id
                else -> null
            }
            if (target != null) {
                resolvedChatId = target
                onOpenChatInApp(target)
            } else {
                snackbarHostState.showSnackbar(strings.adminChatOpenFailed)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.headerBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_chat_inspector_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = chatSummary.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val sub = listOfNotNull(
                            chatSummary.roleLabel.takeIf { it.isNotBlank() },
                            chatSummary.username.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        if (sub.isNotBlank()) {
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = tgColors.accent,
                                maxLines = 1,
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { attemptOpenRealChat() },
                        modifier = Modifier.testTag("admin_topbar_enter_chat_btn"),
                    ) {
                        Text(strings.adminEnterChatBtn, color = tgColors.accent, fontWeight = FontWeight.Bold)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Chat Details Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = strings.adminChatDetailsTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = tgColors.accent,
                            fontWeight = FontWeight.Bold,
                        )
                        DetailInfoRow(Icons.Rounded.Info, strings.adminChatTypeLabel, chatSummary.chatType.uppercase())
                        if (chatSummary.roleLabel.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.Person, strings.adminRoleLabel, chatSummary.roleLabel)
                        }
                        if (chatSummary.username.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.AlternateEmail, strings.usernameLabel, chatSummary.username)
                        }
                        if (chatSummary.phoneNumber.isNotBlank()) {
                            DetailInfoRow(Icons.Rounded.Call, strings.mobileLabel, chatSummary.phoneNumber)
                        }
                        if (chatSummary.chatId != 0L) {
                            DetailInfoRow(Icons.Rounded.Tag, "Chat ID", chatSummary.chatId.toString())
                        }
                        if (chatSummary.memberCount > 0) {
                            DetailInfoRow(Icons.Rounded.Group, strings.adminMembersCountLabel, chatSummary.memberCount.toString())
                        }
                        if (chatSummary.unreadCount > 0) {
                            DetailInfoRow(Icons.AutoMirrored.Rounded.Chat, strings.adminUnreadLabel, chatSummary.unreadCount.toString())
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { attemptOpenRealChat() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_enter_chat_button"),
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.adminEnterChatBtn, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Incoming & Recent Messages Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${strings.adminIncomingMessagesTitle} (${displayedMessages.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = tgColors.accent,
                            fontWeight = FontWeight.Bold,
                        )
                        if (isLoadingLive) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = tgColors.accent)
                        }
                    }
                }

                if (displayedMessages.isEmpty() && !isLoadingLive) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tgColors.cardBackground)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(strings.adminNoChatMessages, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(displayedMessages, key = { "${it.id}_${it.timeSec}_${it.text.hashCode()}" }) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isIncoming) Alignment.Start else Alignment.End,
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 320.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (msg.isIncoming) tgColors.incomingBubble else tgColors.outgoingBubble)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = msg.senderName.ifBlank { chatSummary.title },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = tgColors.accent,
                                    )
                                    Text(
                                        text = if (msg.isIncoming) strings.adminIncomingTag else strings.adminOutgoingTag,
                                        fontSize = 10.sp,
                                        color = if (msg.isIncoming) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (msg.timeSec > 0) {
                                    Text(
                                        text = SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()).format(Date(msg.timeSec * 1000L)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.isIncoming) tgColors.incomingTime else tgColors.outgoingTime,
                                        modifier = Modifier.align(Alignment.End),
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

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(value, style = MaterialTheme.typography.bodyMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
