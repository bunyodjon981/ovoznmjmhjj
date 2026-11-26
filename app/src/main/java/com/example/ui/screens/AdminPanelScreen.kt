package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GhostlyAccountRepository
import com.example.data.GhostlyUser
import com.example.data.LocalStrings
import com.example.data.SupportMessage
import com.example.data.SupportThread
import com.example.data.adminChannelEnabledLabel
import com.example.data.adminChannelLinkLabel
import com.example.data.adminConfirmBtn
import com.example.data.adminGhostlyChannelDesc
import com.example.data.adminNoSupportThreads
import com.example.data.adminNoUsersFound
import com.example.data.adminPanelTitle
import com.example.data.adminReplyHint
import com.example.data.adminSearchUsersHint
import com.example.data.adminSpam24h
import com.example.data.adminSpamActiveBadge
import com.example.data.adminStatsMessages
import com.example.data.adminStatsSpammed
import com.example.data.adminStatsUsers
import com.example.data.adminSupportMessagesWithUser
import com.example.data.adminTabChannel
import com.example.data.adminTabMessages
import com.example.data.adminTabUsers
import com.example.data.adminUnspamNow
import com.example.data.adminViewProfile
import com.example.tdlib.TelegramClientManager
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    repository: GhostlyAccountRepository,
    clientManager: TelegramClientManager,
    onOpenChat: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(repository) {
        if (repository.isFirebaseConfigured) {
            withContext(Dispatchers.IO) { repository.refreshFromFirebase() }
        }
    }

    val users by remember(repository) {
        repository.observeAllUsersForAdmin()
    }.collectAsState(initial = emptyList())

    val supportThreads by remember(repository) {
        repository.observeAllSupportThreads()
    }.collectAsState(initial = emptyList())

    val channelConfig by remember(repository) {
        repository.observeGhostlyChannelConfig()
    }.collectAsState(initial = null)

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<GhostlyUser?>(null) }
    var inspectedUserKey by remember { mutableStateOf<String?>(null) }
    var activeThread by remember { mutableStateOf<SupportThread?>(null) }

    BackHandler(enabled = activeThread != null || inspectedUserKey != null) {
        when {
            activeThread != null -> activeThread = null
            inspectedUserKey != null -> inspectedUserKey = null
        }
    }

    val liveSelectedUser = remember(selectedUser, users) {
        selectedUser?.let { sel -> users.find { it.userId == sel.userId } ?: sel }
    }
    val liveInspectedUser = remember(inspectedUserKey, users) {
        inspectedUserKey?.let { key -> users.find { it.userId == key } }
    }

    val spammedCount = remember(users) { users.count { it.isActivelySpammed() } }
    val unreadThreadsCount = remember(supportThreads) { supportThreads.count { it.unreadByAdmin } }

    if (activeThread != null) {
        AdminSupportThreadChatView(
            thread = activeThread!!,
            repository = repository,
            onBack = { activeThread = null },
        )
        return
    }

    if (liveInspectedUser != null) {
        val userThread = remember(liveInspectedUser.userId, supportThreads) {
            supportThreads.firstOrNull { it.userId == liveInspectedUser.userId }
        }
        AdminUserFullProfileScreen(
            user = liveInspectedUser,
            supportThread = userThread,
            repository = repository,
            clientManager = clientManager,
            onOpenSupportThread = { th -> activeThread = th },
            onOpenChatInApp = onOpenChat,
            onBack = { inspectedUserKey = null },
        )
        return
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AdminPanelSettings,
                            contentDescription = null,
                            tint = tgColors.accent,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.adminPanelTitle, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (repository.isFirebaseConfigured) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = "Firebase",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(20.dp),
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
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AdminStatCard(
                        icon = Icons.Rounded.Group,
                        value = users.size.toString(),
                        label = strings.adminStatsUsers,
                        tint = tgColors.accent,
                        modifier = Modifier.weight(1f),
                    )
                    AdminStatCard(
                        icon = Icons.Rounded.Block,
                        value = spammedCount.toString(),
                        label = strings.adminStatsSpammed,
                        tint = Color(0xFFEF4747),
                        modifier = Modifier.weight(1f),
                    )
                    AdminStatCard(
                        icon = Icons.Rounded.SupportAgent,
                        value = if (unreadThreadsCount > 0) "${supportThreads.size} ($unreadThreadsCount)" else supportThreads.size.toString(),
                        label = strings.adminStatsMessages,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = tgColors.cardBackground,
                    contentColor = tgColors.accent,
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(strings.adminTabUsers, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            val label = if (unreadThreadsCount > 0) "${strings.adminTabMessages} ($unreadThreadsCount)"
                            else strings.adminTabMessages
                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(strings.adminTabChannel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }

                when (selectedTab) {
                    0 -> {
                        val filteredUsers = remember(users, searchQuery) {
                            val q = searchQuery.trim().lowercase()
                            if (q.isEmpty()) users
                            else {
                                val qClean = q.removePrefix("@")
                                val qDigits = q.filter { it.isDigit() }
                                users.filter { u ->
                                    u.fullName.lowercase().contains(q) ||
                                        u.username.lowercase().removePrefix("@").contains(qClean) ||
                                        u.telegramUserId.toString().contains(qClean) ||
                                        u.phoneNumber.lowercase().contains(q) ||
                                        (qDigits.isNotEmpty() && u.phoneNumber.filter { it.isDigit() }.contains(qDigits))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(strings.adminSearchUsersHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("admin_user_search"),
                        )

                        if (filteredUsers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(strings.adminNoUsersFound, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(filteredUsers, key = { it.userId }) { user ->
                                    AdminUserCard(
                                        user = user,
                                        onClick = { selectedUser = user },
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (supportThreads.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = strings.adminNoSupportThreads,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp),
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(supportThreads, key = { it.userId }) { thread ->
                                    AdminSupportThreadCard(
                                        thread = thread,
                                        onClick = { activeThread = thread },
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        var linkInput by remember(channelConfig?.channelLink) {
                            mutableStateOf(channelConfig?.channelLink.orEmpty())
                        }
                        var enabledToggle by remember(channelConfig?.enabled, channelConfig?.channelLink) {
                            mutableStateOf(channelConfig?.enabled ?: true)
                        }
                        var isSaving by remember { mutableStateOf(false) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(tgColors.cardBackground)
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(tgColors.accent.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Rounded.Campaign, null, tint = tgColors.accent)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = strings.adminTabChannel,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }

                                    Text(
                                        text = strings.adminGhostlyChannelDesc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    OutlinedTextField(
                                        value = linkInput,
                                        onValueChange = { linkInput = it },
                                        label = { Text(strings.adminChannelLinkLabel) },
                                        placeholder = { Text("https://t.me/my_channel") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_channel_link_input"),
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = strings.adminChannelEnabledLabel,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                        Switch(
                                            checked = enabledToggle,
                                            onCheckedChange = { enabledToggle = it },
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            isSaving = true
                                            scope.launch {
                                                val res = repository.saveGhostlyChannelConfig(
                                                    channelLink = linkInput,
                                                    enabled = enabledToggle && linkInput.isNotBlank(),
                                                )
                                                isSaving = false
                                                res.onSuccess {
                                                    snackbarHostState.showSnackbar(strings.savedToast)
                                                }.onFailure { err ->
                                                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Error")
                                                }
                                            }
                                        },
                                        enabled = !isSaving,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent, contentColor = Color.White),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("admin_channel_confirm_button"),
                                    ) {
                                        if (isSaving) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text(strings.adminConfirmBtn, fontWeight = FontWeight.SemiBold)
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

    liveSelectedUser?.let { user ->
        val isSpammed = user.isActivelySpammed()
        val userThread = remember(user.userId, supportThreads) {
            supportThreads.firstOrNull { it.userId == user.userId }
        }
        var isUpdatingSpam by remember { mutableStateOf(false) }

        ModalBottomSheet(onDismissRequest = { selectedUser = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatarBadge(name = user.fullName, size = 50.dp)
                    Spacer(modifier = Modifier.width(14.dp))
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
                        val sub = listOfNotNull(
                            user.username.takeIf { it.isNotBlank() }?.let { "@$it" },
                            user.phoneNumber.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        if (sub.isNotBlank()) {
                            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val totalChats = if (user.chatSummaries.isNotEmpty()) user.chatSummaries.size else user.channelSummaries.size
                        Text(
                            text = "ID: ${user.telegramUserId} · $totalChats chat",
                            style = MaterialTheme.typography.labelSmall,
                            color = tgColors.accent,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val key = user.userId
                            selectedUser = null
                            inspectedUserKey = key
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_view_user_profile_btn"),
                    ) {
                        Icon(Icons.Rounded.Person, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.adminViewProfile, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_toggle_spam_btn"),
                    ) {
                        if (isUpdatingSpam) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (isSpammed) Icons.Rounded.CheckCircle else Icons.Rounded.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpammed) strings.adminUnspamNow else strings.adminSpam24h,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (userThread != null) {
                    OutlinedButton(
                        onClick = {
                            selectedUser = null
                            activeThread = userThread
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(Icons.Rounded.SupportAgent, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.adminSupportMessagesWithUser, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSupportThreadChatView(
    thread: SupportThread,
    repository: GhostlyAccountRepository,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val messages by remember(thread.userId) {
        repository.observeThreadMessagesForAdmin(thread.userId)
    }.collectAsState(initial = emptyList<SupportMessage>())

    LaunchedEffect(thread.userId) {
        if (thread.unreadByAdmin) {
            repository.markThreadReadByAdmin(thread.userId)
        }
    }

    var replyInput by remember { mutableStateOf("") }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = thread.senderName.ifBlank { "User" },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val sub = listOfNotNull(
                            thread.senderUsername.takeIf { it.isNotBlank() }?.let { "@$it" },
                            thread.senderPhone.takeIf { it.isNotBlank() },
                            if (thread.telegramUserId > 0L) "ID: ${thread.telegramUserId}" else null,
                        ).joinToString(" · ")
                        if (sub.isNotBlank()) {
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .imePadding(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages, key = { it.id }) { m ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (m.fromAdmin) Alignment.End else Alignment.Start,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (m.fromAdmin) tgColors.outgoingBubble else tgColors.incomingBubble)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(m.text, style = MaterialTheme.typography.bodyMedium)
                            val ts = m.createdAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (m.fromAdmin) tgColors.outgoingTime else tgColors.incomingTime,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgColors.cardBackground)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    BasicTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                        cursorBrush = SolidColor(tgColors.accent),
                        decorationBox = { inner ->
                            if (replyInput.isEmpty()) {
                                Text(strings.adminReplyHint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                IconButton(
                    onClick = {
                        val trimmed = replyInput.trim()
                        if (trimmed.isBlank()) return@IconButton
                        replyInput = ""
                        scope.launch {
                            repository.sendAdminSupportReply(thread.userId, trimmed)
                                .onFailure { err ->
                                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Error")
                                }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Reply",
                        tint = if (replyInput.isNotBlank()) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdminUserCard(
    user: GhostlyUser,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val isSpammed = user.isActivelySpammed()
    val totalChats = if (user.chatSummaries.isNotEmpty()) user.chatSummaries.size else user.channelSummaries.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .border(
                width = if (isSpammed) 1.dp else 0.dp,
                color = if (isSpammed) Color(0xFFEF4747).copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialAvatarBadge(name = user.fullName, size = 44.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.fullName.ifBlank { "Telegram User" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (user.premiumAccount) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFF7B61FF), modifier = Modifier.size(15.dp))
                }
            }
            val meta = listOfNotNull(
                user.username.takeIf { it.isNotBlank() }?.let { "@$it" },
                user.phoneNumber.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "ID: ${user.telegramUserId} · $totalChats chat",
                style = MaterialTheme.typography.labelSmall,
                color = tgColors.accent,
            )
        }
        if (isSpammed) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.adminSpamActiveBadge,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4747))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun AdminSupportThreadCard(
    thread: SupportThread,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialAvatarBadge(name = thread.senderName, size = 44.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = thread.senderName.ifBlank { "User" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                val ts = thread.updatedAtMs
                if (ts > 0L) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (thread.unreadByAdmin) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val meta = listOfNotNull(
                thread.senderUsername.takeIf { it.isNotBlank() }?.let { "@$it" },
                thread.senderPhone.takeIf { it.isNotBlank() },
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = thread.lastMessageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (thread.unreadByAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(tgColors.accent),
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialAvatarBadge(
    name: String,
    size: androidx.compose.ui.unit.Dp,
) {
    val tgColors = LocalTelegramColors.current
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "G"
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(tgColors.accent, Color(0xFF00D2FF)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
        )
    }
}

fun formatRemainingDuration(untilMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val diffMs = (untilMs - nowMs).coerceAtLeast(0L)
    val totalMinutes = (diffMs / 60_000L).toInt()
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0) "${hours}s ${mins}d" else "${mins.coerceAtLeast(1)}d"
}
