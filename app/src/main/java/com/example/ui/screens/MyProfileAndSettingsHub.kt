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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.copyUriToCacheFile
import com.example.data.adminPanelTitle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.tdlib.displayName
import com.example.tdlib.getChatArchivedStories
import com.example.tdlib.getChatPostedStories
import com.example.tdlib.getReceivedGifts
import com.example.tdlib.postPhotoStory
import com.example.tdlib.primaryUsername
import com.example.tdlib.uploadProfilePhoto
import com.example.ui.components.ProfileGiftsSection
import com.example.ui.components.ProfilePhotoLightbox
import com.example.ui.components.ProfileStoriesGridSection
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class SettingsDestination {
    data object ACCOUNT : SettingsDestination()
    data object CHAT_SETTINGS : SettingsDestination()
    data object PRIVACY : SettingsDestination()
    data object NOTIFICATIONS : SettingsDestination()
    data object DATA_STORAGE : SettingsDestination()
    data object FOLDERS : SettingsDestination()
    data object DEVICES : SettingsDestination()
    data object POWER_SAVING : SettingsDestination()
    data object LANGUAGE : SettingsDestination()
    data object PREMIUM : SettingsDestination()
    data object STARS : SettingsDestination()
    data object SUPPORT_CHAT : SettingsDestination()
    data object SUPPORT_PROFILE : SettingsDestination()
    data object ADMIN_PANEL : SettingsDestination()
}

@Composable
fun MyProfileScreen(
    me: TdApi.User?,
    usersMap: Map<Long, TdApi.User>,
    filesMap: Map<Int, TdApi.File>,
    activeStories: Map<Long, TdApi.ChatActiveStories>,
    userFullInfoMap: Map<Long, TdApi.UserFullInfo>,
    clientManager: TelegramClientManager,
    onOpenAccountEdit: () -> Unit,
    onOpenSettingsTab: () -> Unit,
    onOpenSavedMessages: () -> Unit,
    onOpenAdminPanel: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showPhotoLightbox by remember { mutableStateOf(false) }
    var activeStoryViewer by remember { mutableStateOf<Pair<List<TdApi.Story>, Int>?>(null) }

    var postedStories by remember(me?.id) { mutableStateOf<List<TdApi.Story>>(emptyList()) }
    var archivedStories by remember(me?.id) { mutableStateOf<List<TdApi.Story>>(emptyList()) }
    var receivedGifts by remember(me?.id) { mutableStateOf<List<TdApi.ReceivedGift>>(emptyList()) }
    var loadingPosts by remember { mutableStateOf(false) }
    var loadingArchived by remember { mutableStateOf(false) }
    var loadingGifts by remember { mutableStateOf(false) }
    var refreshNonce by remember { mutableIntStateOf(0) }

    val myId = me?.id ?: 0L
    val myActiveStoryEntry = activeStories[myId]

    LaunchedEffect(myId, refreshNonce) {
        if (myId != 0L) {
            clientManager.getUserFullInfo(myId)
            loadingPosts = true
            clientManager.getChatPostedStories(myId).onSuccess { postedStories = it }
            loadingPosts = false
            loadingArchived = true
            clientManager.getChatArchivedStories(myId).onSuccess { archivedStories = it }
            loadingArchived = false
            loadingGifts = true
            clientManager.getReceivedGifts(TdApi.MessageSenderUser(myId)).onSuccess { receivedGifts = it }
            loadingGifts = false
        }
    }
    val fullInfo = me?.id?.let { userFullInfoMap[it] }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val file = copyUriToCacheFile(context, uri, "avatar.jpg")
                if (file != null) {
                    clientManager.uploadProfilePhoto(file.absolutePath)
                        .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                        .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                }
            }
        }
    }

    val storyPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null && myId != 0L) {
            scope.launch {
                val file = copyUriToCacheFile(context, uri, "story_post.jpg")
                if (file != null) {
                    clientManager.postPhotoStory(myId, file.absolutePath, postToProfile = true)
                        .onSuccess {
                            refreshNonce++
                            snackbarHostState.showSnackbar(strings.savedToast)
                        }
                        .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top QR + Menu Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Rounded.QrCode2, contentDescription = strings.myQrCode, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.setPhoto) },
                                onClick = {
                                    showMenu = false
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings.editInfo) },
                                onClick = {
                                    showMenu = false
                                    onOpenAccountEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(strings.savedMessages) },
                                onClick = {
                                    showMenu = false
                                    onOpenSavedMessages()
                                },
                            )
                        }
                    }
                }
            }

            // Centered Avatar, Name & Online status
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    val hasActiveStories = myActiveStoryEntry?.stories?.isNotEmpty() == true
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(
                                if (hasActiveStories) Modifier
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF00D2FF), Color(0xFF8A2BE2))),
                                        CircleShape,
                                    )
                                    .padding(3.5.dp)
                                else Modifier
                            )
                            .clickable {
                                if (hasActiveStories && postedStories.isNotEmpty()) {
                                    activeStoryViewer = postedStories to 0
                                } else if (me?.profilePhoto != null) {
                                    showPhotoLightbox = true
                                }
                            },
                    ) {
                        TelegramAvatar(
                            id = me?.id ?: 0L,
                            title = me.displayName(),
                            photoFile = me?.profilePhoto?.big ?: me?.profilePhoto?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 96.dp,
                            isOnline = false,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = me.displayName(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.online,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tgColors.accent,
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Pill Action Buttons matching screenshot
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ProfilePillAction(
                            icon = Icons.Rounded.CameraAlt,
                            label = strings.setPhoto,
                            modifier = Modifier.weight(1f),
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        )
                        ProfilePillAction(
                            icon = Icons.Rounded.Edit,
                            label = strings.editInfo,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenAccountEdit,
                        )
                        ProfilePillAction(
                            icon = Icons.Rounded.Settings,
                            label = strings.tabSettings,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenSettingsTab,
                        )
                    }
                    val isOwnerAdmin = me?.phoneNumber?.filter { it.isDigit() }?.endsWith("998948802809") == true
                    if (isOwnerAdmin && onOpenAdminPanel != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(tgColors.accent.copy(alpha = 0.16f))
                                .clickable(onClick = onOpenAdminPanel)
                                .padding(horizontal = 16.dp, vertical = 13.dp)
                                .testTag("profile_admin_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = strings.adminPanelTitle,
                                tint = tgColors.accent,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.adminPanelTitle,
                                color = tgColors.accent,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Info Card ("Axborot")
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.infoSection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    val phone = me?.phoneNumber?.takeIf { it.isNotBlank() }?.let { "+$it" }
                    if (phone != null) {
                        ProfileInfoLine(
                            icon = Icons.Rounded.Call,
                            value = phone,
                            label = strings.mobileLabel,
                            onClick = {
                                clipboard.setText(AnnotatedString(phone))
                                scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                            },
                        )
                    }
                    val username = me?.primaryUsername()?.let { "@$it" }
                    if (username != null) {
                        ProfileInfoLine(
                            icon = Icons.Rounded.AlternateEmail,
                            value = username,
                            label = strings.usernameLabel,
                            onClick = {
                                clipboard.setText(AnnotatedString(username))
                                scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                            },
                        )
                    }
                    fullInfo?.birthdate?.let { bd ->
                        if (bd.day > 0 && bd.month > 0) {
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_MONTH, bd.day)
                                set(Calendar.MONTH, bd.month - 1)
                                if (bd.year > 0) set(Calendar.YEAR, bd.year)
                            }
                            val datePart = SimpleDateFormat("MMM d" + if (bd.year > 0) ", yyyy" else "", Locale.getDefault()).format(cal.time)
                            val ageSuffix = if (bd.year > 1900) {
                                val now = Calendar.getInstance()
                                var age = now.get(Calendar.YEAR) - bd.year
                                if (now.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) age--
                                if (age > 0) " " + String.format(strings.ageYearsFormat, age) else ""
                            } else ""
                            ProfileInfoLine(
                                icon = Icons.Rounded.Cake,
                                value = "$datePart$ageSuffix",
                                label = strings.birthdateLabel,
                                onClick = onOpenAccountEdit,
                            )
                        }
                    }
                    val bio = fullInfo?.bio?.text?.takeIf { it.isNotBlank() }
                    if (bio != null) {
                        ProfileInfoLine(
                            icon = Icons.Rounded.Info,
                            value = bio,
                            label = strings.bioLabel,
                            onClick = onOpenAccountEdit,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tabs row (Postlar / Arxivlangan postlar / Hadyalar 💝🧸) matching Screenshots 1-3
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
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            strings.postsTab,
                            strings.archivedPostsTab,
                            "${strings.giftsTab} 💝🧸",
                        ).forEachIndexed { idx, tabLabel ->
                            val selected = selectedTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) tgColors.accent.copy(alpha = 0.22f) else Color.Transparent)
                                    .clickable { selectedTab = idx }
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = tabLabel,
                                    color = if (selected) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    when (selectedTab) {
                        0 -> ProfileStoriesGridSection(
                            stories = postedStories,
                            isLoading = loadingPosts,
                            hintText = null,
                            showAddButton = true,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            onStoryClick = { idx -> activeStoryViewer = postedStories to idx },
                            onAddStoryClick = {
                                storyPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        )
                        1 -> ProfileStoriesGridSection(
                            stories = archivedStories,
                            isLoading = loadingArchived,
                            hintText = strings.archivedStoriesHint,
                            showAddButton = false,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            onStoryClick = { idx -> activeStoryViewer = archivedStories to idx },
                            onAddStoryClick = {},
                        )
                        else -> ProfileGiftsSection(
                            gifts = receivedGifts,
                            isLoading = loadingGifts,
                            isOwnProfile = true,
                            targetOwnerId = TdApi.MessageSenderUser(myId),
                            usersMap = usersMap,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            onGiftsUpdated = { refreshNonce++ },
                            onStatusMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                        )
                    }
                }
            }
        }
    }

    if (showPhotoLightbox) {
        ProfilePhotoLightbox(
            title = me.displayName(),
            photoFile = me?.profilePhoto?.big ?: me?.profilePhoto?.small,
            filesMap = filesMap,
            clientManager = clientManager,
            onDismiss = { showPhotoLightbox = false },
        )
    }

    activeStoryViewer?.let { (storiesList, startIdx) ->
        StoryViewerDialog(
            chatId = myId,
            posterTitle = me.displayName(),
            posterPhoto = me?.profilePhoto?.small,
            initialStories = storiesList,
            initialIndex = startIdx,
            filesMap = filesMap,
            clientManager = clientManager,
            onStoryChanged = { refreshNonce++ },
            onDismiss = { activeStoryViewer = null },
        )
    }

    if (showQrDialog) {
        val uname = me?.primaryUsername()
        val qrLink = if (uname != null) "https://t.me/$uname" else "tg://user?id=${me?.id ?: 0}"
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text(strings.myQrCode) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.QrCode2, null, tint = tgColors.accent, modifier = Modifier.size(110.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(qrLink, style = MaterialTheme.typography.bodyMedium, color = tgColors.accent)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(qrLink))
                        showQrDialog = false
                        scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                    },
                ) {
                    Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.copy)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) { Text(strings.webAppClose) }
            },
        )
    }
}

@Composable
private fun ProfilePillAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tgColors.accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = tgColors.accent,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileInfoLine(
    icon: ImageVector,
    value: String,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsHubScreen(
    me: TdApi.User?,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onNavigate: (SettingsDestination) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAskQuestionDialog by remember { mutableStateOf(false) }
    var webPreviewSession by remember { mutableStateOf<Pair<String, String>?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val file = copyUriToCacheFile(context, uri, "avatar.jpg")
                if (file != null) {
                    clientManager.uploadProfilePhoto(file.absolutePath)
                        .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.logOut, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showLogoutConfirm = true
                                },
                            )
                        }
                    }
                }
            }

            // Centered Avatar with camera badge + Name + phone · @username (matches IMG_20261125_213646_e8c4d040.jpg!)
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        TelegramAvatar(
                            id = me?.id ?: 0L,
                            title = me.displayName(),
                            photoFile = me?.profilePhoto?.big ?: me?.profilePhoto?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 86.dp,
                            isOnline = false,
                        )
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = 2.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(tgColors.accent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = strings.setPhoto, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = me.displayName(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val contactSub = listOfNotNull(
                        me?.phoneNumber?.takeIf { it.isNotBlank() }?.let { "+$it" },
                        me?.primaryUsername()?.let { "@$it" },
                    ).joinToString(" · ")
                    if (contactSub.isNotBlank()) {
                        Text(
                            text = contactSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Accounts Card ("Hisoblar")
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .clickable { onNavigate(SettingsDestination.ACCOUNT) }
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                ) {
                    Text(
                        text = strings.accountsSection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TelegramAvatar(
                            id = me?.id ?: 0L,
                            title = me.displayName(),
                            photoFile = me?.profilePhoto?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 34.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = me.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Main Settings Grouped Card matching screenshot icons & order 1-to-1
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    SettingsHubRow(Icons.Rounded.Person, Color(0xFF2AA1F2), strings.accountTitle, strings.accountSubtitle, "settings_row_account") {
                        onNavigate(SettingsDestination.ACCOUNT)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.ChatBubble, Color(0xFFF5A623), strings.chatSettingsTitle, strings.chatSettingsSubtitle, "settings_row_chat") {
                        onNavigate(SettingsDestination.CHAT_SETTINGS)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Key, Color(0xFF4CAF50), strings.privacyTitle, strings.privacySubtitle, "settings_row_privacy") {
                        onNavigate(SettingsDestination.PRIVACY)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Notifications, Color(0xFFEF4747), strings.notificationsTitle, strings.notificationsSubtitle, "settings_row_notifications") {
                        onNavigate(SettingsDestination.NOTIFICATIONS)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.PieChart, Color(0xFF4F6EF7), strings.dataStorageTitle, strings.dataStorageSubtitle, "settings_row_data") {
                        onNavigate(SettingsDestination.DATA_STORAGE)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Folder, Color(0xFF22A5F5), strings.chatFoldersTitle, strings.chatFoldersSubtitle, "settings_row_folders") {
                        onNavigate(SettingsDestination.FOLDERS)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Devices, Color(0xFF1CB4C8), strings.devicesTitle, strings.devicesSubtitle, "settings_row_devices") {
                        onNavigate(SettingsDestination.DEVICES)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.BatteryChargingFull, Color(0xFFFA8C28), strings.powerSavingTitle, strings.powerSavingSubtitle, "settings_row_power") {
                        onNavigate(SettingsDestination.POWER_SAVING)
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Language, Color(0xFF8E44AD), strings.languageTitle, appearance.language.nativeName, "settings_row_language") {
                        onNavigate(SettingsDestination.LANGUAGE)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Telegram Premium & Telegram Stars Card (matches Screenshot 4)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    SettingsHubRow(
                        icon = Icons.Rounded.AutoAwesome,
                        iconBg = Color(0xFF7B61FF),
                        title = strings.telegramPremiumTitle,
                        subtitle = if (me?.isPremium == true) "Premium" else "Obuna va imkoniyatlar",
                        tag = "settings_row_premium",
                    ) { onNavigate(SettingsDestination.PREMIUM) }
                    SettingsDivider()
                    SettingsHubRow(
                        icon = Icons.Rounded.Star,
                        iconBg = Color(0xFFF59E0B),
                        title = strings.telegramStarsTitle,
                        subtitle = strings.yourBalance,
                        tag = "settings_row_stars",
                    ) { onNavigate(SettingsDestination.STARS) }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Help ("Yordam") Card matching Screenshot 4
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    Text(
                        text = strings.helpSection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 6.dp),
                    )
                    SettingsHubRow(Icons.Rounded.ChatBubble, Color(0xFFF5A623), strings.askQuestion, strings.supportPhoneDisplay, "settings_row_ask") {
                        showAskQuestionDialog = true
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Help, Color(0xFF2AA1F2), strings.telegramFaq, "telegram.org/faq", "settings_row_faq") {
                        if (appearance.inAppBrowser) webPreviewSession = strings.telegramFaq to "https://telegram.org/faq"
                        else runCatching { uriHandler.openUri("https://telegram.org/faq") }
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Lightbulb, Color(0xFF9B59B6), strings.telegramFeatures, "telegram.org/blog", "settings_row_features") {
                        if (appearance.inAppBrowser) webPreviewSession = strings.telegramFeatures to "https://telegram.org/blog"
                        else runCatching { uriHandler.openUri("https://telegram.org/blog") }
                    }
                    SettingsDivider()
                    SettingsHubRow(Icons.Rounded.Security, Color(0xFF4CAF50), strings.privacyPolicy, "telegram.org/privacy", "settings_row_privacy_policy") {
                        if (appearance.inAppBrowser) webPreviewSession = strings.privacyPolicy to "https://telegram.org/privacy"
                        else runCatching { uriHandler.openUri("https://telegram.org/privacy") }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = strings.appFooterVersion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )
            }
        }
    }

    if (showAskQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showAskQuestionDialog = false },
            title = { Text(strings.askVolunteerTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.askVolunteerBody, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = strings.telegramFaq,
                        color = tgColors.accent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            showAskQuestionDialog = false
                            if (appearance.inAppBrowser) webPreviewSession = strings.telegramFaq to "https://telegram.org/faq"
                            else runCatching { uriHandler.openUri("https://telegram.org/faq") }
                        },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAskQuestionDialog = false
                        onNavigate(SettingsDestination.SUPPORT_CHAT)
                    },
                ) { Text(strings.askVolunteerBtn, color = tgColors.accent, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showAskQuestionDialog = false }) { Text(strings.cancel, color = tgColors.accent) }
            },
        )
    }

    webPreviewSession?.let { (title, url) ->
        TelegramWebAppDialog(
            title = title,
            subtitle = url.substringAfter("://").substringBefore("/"),
            url = url,
            onDismiss = { webPreviewSession = null },
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(strings.logOut) },
            text = { Text(strings.logOutConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        scope.launch { clientManager.logOut() }
                    },
                ) { Text(strings.logOut, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text(strings.cancel) } },
        )
    }
}

@Composable
private fun SettingsDivider() {
    val tgColors = LocalTelegramColors.current
    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 66.dp))
}

@Composable
private fun SettingsHubRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
