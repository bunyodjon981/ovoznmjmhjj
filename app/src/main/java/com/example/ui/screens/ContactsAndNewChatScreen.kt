package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.tdlib.formatStatus
import com.example.tdlib.primaryUsername
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

enum class NewChatMode {
    CONTACTS,
    NEW_GROUP,
    NEW_CHANNEL,
    CALLS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsAndNewChatScreen(
    initialMode: NewChatMode,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    isBottomTab: Boolean = false,
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mode by remember { mutableStateOf(initialMode) }
    var contacts by remember { mutableStateOf<List<TdApi.User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var sortByLastSeen by remember { mutableStateOf(true) }
    var isSecretPickMode by remember { mutableStateOf(false) }

    val selectedMemberIds = remember { mutableStateListOf<Long>() }
    var groupTitle by remember { mutableStateOf("") }
    var showGroupTitleDialog by remember { mutableStateOf(false) }

    var channelTitle by remember { mutableStateOf("") }
    var channelDescription by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        contacts = clientManager.getContacts()
        isLoading = false
    }

    val filteredContacts = remember(contacts, searchQuery, sortByLastSeen) {
        val base = if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.displayName().contains(searchQuery, ignoreCase = true) ||
                (it.primaryUsername()?.contains(searchQuery, ignoreCase = true) == true)
        }
        if (sortByLastSeen) {
            base.sortedByDescending {
                when (val s = it.status) {
                    is TdApi.UserStatusOnline -> Int.MAX_VALUE
                    is TdApi.UserStatusOffline -> s.wasOnline
                    is TdApi.UserStatusRecently -> Int.MAX_VALUE - 100
                    else -> 0
                }
            }
        } else {
            base.sortedBy { it.displayName().lowercase() }
        }
    }

    val showCreateChannel = mode == NewChatMode.NEW_CHANNEL

    Scaffold(
        contentWindowInsets = if (isBottomTab) WindowInsets(0) else WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isBottomTab && mode == NewChatMode.CONTACTS && !isSecretPickMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.tabContacts,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { sortByLastSeen = !sortByLastSeen }) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort", tint = tgColors.accent)
                    }
                }
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = tgColors.cardBackground,
                        titleContentColor = tgColors.headerContent,
                        navigationIconContentColor = tgColors.headerContent,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (mode != initialMode && initialMode == NewChatMode.CONTACTS) {
                                    mode = NewChatMode.CONTACTS
                                    isSecretPickMode = false
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier.testTag("contacts_back_button"),
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        val title = when {
                            isSecretPickMode -> strings.newSecretChat
                            mode == NewChatMode.NEW_GROUP -> strings.newGroup
                            mode == NewChatMode.NEW_CHANNEL -> strings.newChannel
                            else -> strings.tabContacts
                        }
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    },
                )
            }
        },
        floatingActionButton = {
            if (mode == NewChatMode.NEW_GROUP && selectedMemberIds.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showGroupTitleDialog = true },
                    shape = CircleShape,
                    containerColor = tgColors.accent,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("confirm_group_members_fab"),
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = strings.createAction)
                }
            } else if (isBottomTab && mode == NewChatMode.CONTACTS) {
                FloatingActionButton(
                    onClick = { mode = NewChatMode.NEW_GROUP },
                    shape = CircleShape,
                    containerColor = tgColors.accent,
                    contentColor = Color.White,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 86.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.newGroup)
                }
            }
        },
    ) { padding ->
        if (showCreateChannel) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text(
                    text = strings.channelCreateSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = channelTitle,
                    onValueChange = { channelTitle = it },
                    label = { Text(strings.channelNameHint) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("channel_name_input"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = channelDescription,
                    onValueChange = { channelDescription = it },
                    label = { Text(strings.channelDescHint) },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("channel_desc_input"),
                )
                Spacer(modifier = Modifier.height(24.dp))
                FloatingActionButton(
                    onClick = {
                        if (channelTitle.isNotBlank()) {
                            scope.launch {
                                clientManager.createChannel(channelTitle.trim(), channelDescription.trim())
                                    .onSuccess { chat -> onOpenChat(chat.id) }
                                    .onFailure { snackbarHostState.showSnackbar(it.message ?: "Failed") }
                            }
                        }
                    },
                    shape = CircleShape,
                    containerColor = tgColors.accent,
                    contentColor = Color.White,
                    modifier = Modifier.align(Alignment.End).testTag("create_channel_button"),
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = strings.createAction)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Pill search bar matching screenshot ("Kontaktlarni qidirish")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tgColors.pillBackground)
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                        cursorBrush = SolidColor(tgColors.accent),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(strings.searchContactsHint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                            }
                            inner()
                        },
                        modifier = Modifier.weight(1f).testTag("contacts_search_input"),
                    )
                }

                // Sort mode caption matching screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sortByLastSeen = !sortByLastSeen }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (sortByLastSeen) strings.sortByLastSeen else strings.sortByName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (isBottomTab) 104.dp else 24.dp),
                    ) {
                        if (mode == NewChatMode.CONTACTS && !isSecretPickMode && searchQuery.isBlank()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(tgColors.cardBackground),
                                ) {
                                    QuickActionRow(Icons.Rounded.GroupAdd, strings.newGroup) { mode = NewChatMode.NEW_GROUP }
                                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 58.dp))
                                    QuickActionRow(Icons.Rounded.Campaign, strings.newChannel) { mode = NewChatMode.NEW_CHANNEL }
                                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 58.dp))
                                    QuickActionRow(Icons.Rounded.Lock, strings.newSecretChat) { isSecretPickMode = true }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        if (filteredContacts.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(strings.noContactsFound, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filteredContacts, key = { it.id }) { user ->
                                val checked = user.id in selectedMemberIds
                                val isOnline = user.status is TdApi.UserStatusOnline
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            when {
                                                mode == NewChatMode.NEW_GROUP -> {
                                                    if (checked) selectedMemberIds.remove(user.id) else selectedMemberIds.add(user.id)
                                                }
                                                isSecretPickMode -> {
                                                    scope.launch {
                                                        clientManager.createSecretChat(user.id)
                                                            .onSuccess { onOpenChat(it.id) }
                                                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Failed") }
                                                    }
                                                }
                                                else -> {
                                                    scope.launch {
                                                        clientManager.createPrivateChat(user.id).onSuccess { onOpenChat(it.id) }
                                                    }
                                                }
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TelegramAvatar(
                                        id = user.id,
                                        title = user.displayName(),
                                        photoFile = user.profilePhoto?.small,
                                        filesMap = filesMap,
                                        clientManager = clientManager,
                                        size = 48.dp,
                                        isOnline = isOnline,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.displayName(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = user.status.formatStatus(strings),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isOnline) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (mode == NewChatMode.NEW_GROUP) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = {
                                                if (checked) selectedMemberIds.remove(user.id) else selectedMemberIds.add(user.id)
                                            },
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

    if (showGroupTitleDialog) {
        AlertDialog(
            onDismissRequest = { showGroupTitleDialog = false },
            title = { Text(strings.newGroup) },
            text = {
                OutlinedTextField(
                    value = groupTitle,
                    onValueChange = { groupTitle = it },
                    label = { Text(strings.groupNameHint) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (groupTitle.isNotBlank()) {
                            showGroupTitleDialog = false
                            scope.launch {
                                clientManager.createBasicGroup(groupTitle.trim(), selectedMemberIds.toLongArray())
                                    .onSuccess { onOpenChat(it.id) }
                                    .onFailure { snackbarHostState.showSnackbar(it.message ?: "Failed") }
                            }
                        }
                    },
                ) { Text(strings.createAction, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showGroupTitleDialog = false }) { Text(strings.cancel) }
            },
        )
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    label: String,
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
        Icon(icon, contentDescription = label, tint = tgColors.accent, modifier = Modifier.size(23.dp))
        Spacer(modifier = Modifier.width(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tgColors.accent, fontWeight = FontWeight.Medium)
    }
}
