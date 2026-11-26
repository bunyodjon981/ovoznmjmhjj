package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.tdlib.ChatEntry
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.copyUriToCacheFile
import com.example.tdlib.deleteChat
import com.example.tdlib.displayName
import com.example.tdlib.getSupergroupAdmins
import com.example.tdlib.getSupergroupBanned
import com.example.tdlib.primaryUsername
import com.example.tdlib.updateChatDescription
import com.example.tdlib.updateChatPermissions
import com.example.tdlib.updateChatPhoto
import com.example.tdlib.updateChatReactionsAll
import com.example.tdlib.updateChatReactionsNone
import com.example.tdlib.updateChatTitle
import com.example.tdlib.updateSupergroupUsername
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelGroupEditScreen(
    chatId: Long,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val chat = chatsMap[chatId]?.chat
    val chatType = chat?.type
    val supergroup = (chatType as? TdApi.ChatTypeSupergroup)?.let { supergroupsMap[it.supergroupId] }
    val isChannel = (chatType as? TdApi.ChatTypeSupergroup)?.isChannel == true

    var supergroupFullInfo by remember(chatId) { mutableStateOf<TdApi.SupergroupFullInfo?>(null) }
    var title by remember(chatId, chat?.title) { mutableStateOf(chat?.title.orEmpty()) }
    var description by remember(chatId) { mutableStateOf("") }
    var username by remember(chatId) { mutableStateOf(supergroup?.primaryUsername().orEmpty()) }
    var reactionsEnabled by remember(chatId) { mutableStateOf(true) }

    var admins by remember { mutableStateOf<List<TdApi.ChatMember>>(emptyList()) }
    var members by remember { mutableStateOf<List<TdApi.ChatMember>>(emptyList()) }
    var banned by remember { mutableStateOf<List<TdApi.ChatMember>>(emptyList()) }

    var showTypeDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showMembersSheetTitle by remember { mutableStateOf<String?>(null) }
    var sheetMembers by remember { mutableStateOf<List<TdApi.ChatMember>>(emptyList()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        if (chatType is TdApi.ChatTypeSupergroup) {
            clientManager.getSupergroupFullInfo(chatType.supergroupId).onSuccess { full ->
                supergroupFullInfo = full
                description = full.description
            }
            clientManager.getSupergroupAdmins(chatType.supergroupId).onSuccess { admins = it }
            clientManager.getSupergroupMembers(chatType.supergroupId).onSuccess { members = it }
            clientManager.getSupergroupBanned(chatType.supergroupId).onSuccess { banned = it }
        } else if (chatType is TdApi.ChatTypeBasicGroup) {
            clientManager.send(TdApi.GetBasicGroupFullInfo(chatType.basicGroupId)).onSuccess { full ->
                description = full.description
                members = full.members.toList()
            }
        }
        reactionsEnabled = chat?.availableReactions !is TdApi.ChatAvailableReactionsSome ||
            (chat?.availableReactions as? TdApi.ChatAvailableReactionsSome)?.reactions?.isNotEmpty() == true
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val cached = copyUriToCacheFile(context, uri, "chat_photo_${System.currentTimeMillis()}.jpg")
                if (cached != null) {
                    clientManager.updateChatPhoto(chatId, cached.absolutePath)
                        .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                        .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                }
            }
        }
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                title = { Text(strings.edit, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (title.isNotBlank() && title.trim() != chat?.title) {
                                    clientManager.updateChatTitle(chatId, title)
                                }
                                clientManager.updateChatDescription(chatId, description)
                                snackbarHostState.showSnackbar(strings.savedToast)
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("save_chat_edit_button"),
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = strings.saveAction)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Title, Avatar & Description Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .clickable {
                                    photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                        ) {
                            TelegramAvatar(
                                id = chatId,
                                title = title.ifBlank { "Chat" },
                                photoFile = chat?.photo?.small,
                                filesMap = filesMap,
                                clientManager = clientManager,
                                size = 60.dp,
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(if (isChannel) strings.channelNameHint else strings.groupNameHint) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.AddAPhoto, null, tint = tgColors.accent)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(strings.setNewPhoto, color = tgColors.accent, fontWeight = FontWeight.Medium)
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(strings.descriptionLabel) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Type, Reactions, Permissions Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    if (supergroup != null) {
                        EditOptionRow(
                            icon = if (username.isNotBlank()) Icons.Rounded.Public else Icons.Rounded.Lock,
                            label = if (isChannel) strings.channelTypeLabel else strings.groupTypeLabel,
                            value = if (username.isNotBlank()) strings.publicLabel else strings.privateLabel,
                            onClick = { showTypeDialog = true },
                        )
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = !reactionsEnabled
                                reactionsEnabled = next
                                scope.launch {
                                    if (next) clientManager.updateChatReactionsAll(chatId)
                                    else clientManager.updateChatReactionsNone(chatId)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.EmojiEmotions, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(strings.reactionsLabel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(
                            checked = reactionsEnabled,
                            onCheckedChange = { next ->
                                reactionsEnabled = next
                                scope.launch {
                                    if (next) clientManager.updateChatReactionsAll(chatId)
                                    else clientManager.updateChatReactionsNone(chatId)
                                }
                            },
                        )
                    }

                    if (!isChannel && chat != null) {
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        EditOptionRow(
                            icon = Icons.Rounded.Lock,
                            label = strings.permissionsLabel,
                            value = "",
                            onClick = { showPermissionsDialog = true },
                        )
                    }
                }
            }

            // Subscribers / Admins / Banned Management Card
            if (supergroup != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(tgColors.cardBackground),
                    ) {
                        EditOptionRow(
                            icon = Icons.Rounded.AdminPanelSettings,
                            label = strings.adminsLabel,
                            value = (supergroupFullInfo?.administratorCount ?: admins.size).toString(),
                            onClick = {
                                sheetMembers = admins
                                showMembersSheetTitle = strings.adminsLabel
                            },
                        )
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        EditOptionRow(
                            icon = Icons.Rounded.Group,
                            label = if (isChannel) strings.subscribersLabel else strings.membersLabel,
                            value = supergroup.memberCount.toString(),
                            onClick = {
                                sheetMembers = members
                                showMembersSheetTitle = if (isChannel) strings.subscribersLabel else strings.membersLabel
                            },
                        )
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                        EditOptionRow(
                            icon = Icons.Rounded.Block,
                            label = strings.bannedLabel,
                            value = (supergroupFullInfo?.bannedCount ?: banned.size).toString(),
                            onClick = {
                                sheetMembers = banned
                                showMembersSheetTitle = strings.bannedLabel
                            },
                        )
                    }
                }
            }

            // Delete Channel / Group Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .clickable { showDeleteConfirm = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (isChannel) strings.deleteChannel else strings.deleteAndLeaveGroup,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }

    if (showTypeDialog && supergroup != null) {
        var isPublic by remember { mutableStateOf(username.isNotBlank()) }
        var draftUser by remember { mutableStateOf(username) }
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text(if (isChannel) strings.channelTypeLabel else strings.groupTypeLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isPublic = true }) {
                        RadioButton(selected = isPublic, onClick = { isPublic = true })
                        Text(strings.publicLabel)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isPublic = false }) {
                        RadioButton(selected = !isPublic, onClick = { isPublic = false })
                        Text(strings.privateLabel)
                    }
                    if (isPublic) {
                        OutlinedTextField(
                            value = draftUser,
                            onValueChange = { draftUser = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                            prefix = { Text("t.me/") },
                            singleLine = true,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nextName = if (isPublic) draftUser.trim() else ""
                        showTypeDialog = false
                        scope.launch {
                            clientManager.updateSupergroupUsername(supergroup.id, nextName)
                                .onSuccess {
                                    username = nextName
                                    snackbarHostState.showSnackbar(strings.savedToast)
                                }
                                .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                        }
                    },
                ) { Text(strings.saveAction) }
            },
            dismissButton = { TextButton(onClick = { showTypeDialog = false }) { Text(strings.cancel) } },
        )
    }

    if (showPermissionsDialog && chat != null) {
        val currentPerm = chat.permissions
        var canSendBasic by remember { mutableStateOf(currentPerm.canSendBasicMessages) }
        var canSendPhotos by remember { mutableStateOf(currentPerm.canSendPhotos) }
        var canSendVideos by remember { mutableStateOf(currentPerm.canSendVideos) }
        var canSendPolls by remember { mutableStateOf(currentPerm.canSendPolls) }
        var canInvite by remember { mutableStateOf(currentPerm.canInviteUsers) }
        var canPin by remember { mutableStateOf(currentPerm.canPinMessages) }

        AlertDialog(
            onDismissRequest = { showPermissionsDialog = false },
            title = { Text(strings.permissionsLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PermissionToggleRow("Xabar yuborish", canSendBasic) { canSendBasic = it }
                    PermissionToggleRow("Rasmlar yuborish", canSendPhotos) { canSendPhotos = it }
                    PermissionToggleRow("Videolar yuborish", canSendVideos) { canSendVideos = it }
                    PermissionToggleRow("Soʻrovnoma yaratish", canSendPolls) { canSendPolls = it }
                    PermissionToggleRow("Foydalanuvchi qoʻshish", canInvite) { canInvite = it }
                    PermissionToggleRow("Xabar qadash", canPin) { canPin = it }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionsDialog = false
                        val updated = TdApi.ChatPermissions(
                            canSendBasic,
                            currentPerm.canSendAudios,
                            currentPerm.canSendDocuments,
                            canSendPhotos,
                            canSendVideos,
                            currentPerm.canSendVideoNotes,
                            currentPerm.canSendVoiceNotes,
                            canSendPolls,
                            currentPerm.canSendOtherMessages,
                            currentPerm.canAddLinkPreviews,
                            currentPerm.canChangeInfo,
                            canInvite,
                            canPin,
                            currentPerm.canCreateTopics,
                        )
                        scope.launch {
                            clientManager.updateChatPermissions(chatId, updated)
                                .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                        }
                    },
                ) { Text(strings.saveAction) }
            },
            dismissButton = { TextButton(onClick = { showPermissionsDialog = false }) { Text(strings.cancel) } },
        )
    }

    showMembersSheetTitle?.let { sheetTitle ->
        ModalBottomSheet(onDismissRequest = { showMembersSheetTitle = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text(
                    text = sheetTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
                HorizontalDivider(color = tgColors.divider)
                if (sheetMembers.isEmpty()) {
                    Text(
                        text = strings.noContactsFound,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                } else {
                    LazyColumn {
                        items(sheetMembers, key = { (it.memberId as? TdApi.MessageSenderUser)?.userId ?: it.hashCode().toLong() }) { member ->
                            val uid = (member.memberId as? TdApi.MessageSenderUser)?.userId
                            val user = uid?.let { usersMap[it] }
                            val badge = when (member.status) {
                                is TdApi.ChatMemberStatusCreator -> strings.ownerBadge
                                is TdApi.ChatMemberStatusAdministrator -> strings.adminBadge
                                else -> null
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TelegramAvatar(uid ?: 0L, user.displayName(), user?.profilePhoto?.small, filesMap, clientManager, size = 40.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(user.displayName(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                if (badge != null) {
                                    Text(
                                        text = badge,
                                        fontSize = 12.sp,
                                        color = Color(0xFFB388FF),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isChannel) strings.deleteChannel else strings.deleteAndLeaveGroup) },
            text = { Text(strings.deleteChatConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            clientManager.deleteChat(chatId)
                            onDeleted()
                        }
                    },
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.cancel) } },
        )
    }
}

@Composable
private fun EditOptionRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = tgColors.accent, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PermissionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
