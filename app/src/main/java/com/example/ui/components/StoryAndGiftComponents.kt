package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.LocalStrings
import com.example.tdlib.ChatEntry
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.closeStory
import com.example.tdlib.deleteStory
import com.example.tdlib.displayName
import com.example.tdlib.formatTime
import com.example.tdlib.getAvailableGifts
import com.example.tdlib.getStickerSet
import com.example.tdlib.getStory
import com.example.tdlib.installStickerSet
import com.example.tdlib.openStory
import com.example.tdlib.sellGift
import com.example.tdlib.sendGiftToUser
import com.example.tdlib.toggleGiftSaved
import com.example.tdlib.toggleStoryPostedToProfile
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.io.File

private val UnseenStoryBrush = Brush.linearGradient(
    listOf(Color(0xFF00D2FF), Color(0xFF3A7BD5), Color(0xFF8A2BE2)),
)

@Composable
fun StoriesStripRow(
    me: TdApi.User?,
    chatsMap: Map<Long, ChatEntry>,
    activeStories: Map<Long, TdApi.ChatActiveStories>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onAddMyStory: () -> Unit,
    onOpenStoriesForChat: (Long, List<TdApi.StoryInfo>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val ordered = remember(activeStories, chatsMap, me?.id) {
        activeStories.values
            .filter { it.stories.isNotEmpty() }
            .sortedByDescending { it.order }
    }
    val myStoryEntry = remember(ordered, me?.id) {
        me?.id?.let { uid -> ordered.firstOrNull { it.chatId == uid } }
    }
    val otherStories = remember(ordered, me?.id) {
        ordered.filter { it.chatId != me?.id }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // My Story bubble
        item(key = "my_story_item") {
            val hasMine = myStoryEntry != null && myStoryEntry.stories.isNotEmpty()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(66.dp)
                    .clickable {
                        if (hasMine && myStoryEntry != null) {
                            onOpenStoriesForChat(myStoryEntry.chatId, myStoryEntry.stories.toList())
                        } else {
                            onAddMyStory()
                        }
                    },
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .then(
                                if (hasMine) Modifier.border(2.5.dp, UnseenStoryBrush, CircleShape).padding(4.dp)
                                else Modifier.border(1.5.dp, tgColors.accent.copy(alpha = 0.45f), CircleShape).padding(3.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TelegramAvatar(
                            id = me?.id ?: 0L,
                            title = me.displayName(),
                            photoFile = me?.profilePhoto?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 50.dp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = 1.dp, y = 1.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(tgColors.accent)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { onAddMyStory() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.addPost, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.myStory,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(otherStories, key = { it.chatId }) { active ->
            val chat = chatsMap[active.chatId]?.chat
            val maxStoryId = active.stories.maxOfOrNull { it.storyId } ?: 0
            val hasUnread = maxStoryId > active.maxReadStoryId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(66.dp)
                    .clickable { onOpenStoriesForChat(active.chatId, active.stories.toList()) },
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .then(
                            if (hasUnread) Modifier.border(2.5.dp, UnseenStoryBrush, CircleShape).padding(4.dp)
                            else Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape).padding(4.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    TelegramAvatar(
                        id = active.chatId,
                        title = chat?.title ?: "Telegram",
                        photoFile = chat?.photo?.small,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        size = 50.dp,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat?.title ?: "Story",
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun ProfilePhotoLightbox(
    title: String,
    photoFile: TdApi.File?,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onDismiss: () -> Unit,
) {
    val resolved = photoFile?.let { filesMap[it.id] ?: it }
    LaunchedEffect(photoFile?.id) {
        if (photoFile != null) clientManager.downloadFileIfNeeded(photoFile, priority = 28)
    }
    val path = resolved?.local?.path?.takeIf {
        resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            if (path != null) {
                AsyncImage(
                    model = File(path),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun StoryViewerDialog(
    chatId: Long,
    posterTitle: String,
    posterPhoto: TdApi.File?,
    initialStories: List<TdApi.Story> = emptyList(),
    initialStoryIds: List<Int> = emptyList(),
    initialIndex: Int = 0,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onStoryChanged: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val scope = rememberCoroutineScope()
    var stories by remember(chatId, initialStories) { mutableStateOf(initialStories) }
    var index by remember(initialIndex) { mutableIntStateOf(initialIndex.coerceAtLeast(0)) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(chatId, initialStoryIds) {
        if (stories.isEmpty() && initialStoryIds.isNotEmpty()) {
            val loaded = initialStoryIds.mapNotNull { sid ->
                clientManager.getStory(chatId, sid).getOrNull()
            }
            stories = loaded
        }
    }

    val currentStory = stories.getOrNull(index)
    val currentStoryId = currentStory?.id ?: 0

    DisposableEffect(chatId, currentStoryId, appearance.ghostModeEnabled) {
        if (currentStoryId != 0 && !appearance.ghostModeEnabled) {
            scope.launch { clientManager.openStory(chatId, currentStoryId) }
        }
        onDispose {
            if (currentStoryId != 0 && !appearance.ghostModeEnabled) {
                scope.launch { clientManager.closeStory(chatId, currentStoryId) }
            }
        }
    }

    LaunchedEffect(currentStoryId, stories.size) {
        if (stories.isNotEmpty()) {
            delay(6500L)
            if (index + 1 < stories.size) index++ else onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(stories.size) {
                    detectTapGestures { offset ->
                        if (offset.x < size.width * 0.35f) {
                            if (index > 0) index--
                        } else {
                            if (index + 1 < stories.size) index++ else onDismiss()
                        }
                    }
                },
        ) {
            if (currentStory == null) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            } else {
                val mediaFile: TdApi.File? = when (val c = currentStory.content) {
                    is TdApi.StoryContentPhoto -> c.photo.sizes.lastOrNull()?.photo
                    is TdApi.StoryContentVideo -> c.video.thumbnail?.file
                    else -> null
                }
                val resolvedMedia = mediaFile?.let { filesMap[it.id] ?: it }
                LaunchedEffect(mediaFile?.id) {
                    if (mediaFile != null) clientManager.downloadFileIfNeeded(mediaFile, priority = 28)
                }
                val mediaPath = resolvedMedia?.local?.path?.takeIf {
                    resolvedMedia.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                }
                if (mediaPath != null) {
                    AsyncImage(
                        model = File(mediaPath),
                        contentDescription = posterTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }

            // Top progress bars + header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                if (stories.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        stories.forEachIndexed { idx, _ ->
                            LinearProgressIndicator(
                                progress = {
                                    when {
                                        idx < index -> 1f
                                        idx == index -> 0.7f
                                        else -> 0f
                                    }
                                },
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.5.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TelegramAvatar(
                        id = chatId,
                        title = posterTitle,
                        photoFile = posterPhoto,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        size = 36.dp,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(posterTitle, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        if (currentStory != null) {
                            Text(
                                text = formatTime(currentStory.date),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                            )
                        }
                    }
                    if (currentStory != null && (currentStory.canBeDeleted || currentStory.canToggleIsPostedToChatPage)) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = Color.White)
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                if (currentStory.canToggleIsPostedToChatPage) {
                                    val nextPosted = !currentStory.isPostedToChatPage
                                    DropdownMenuItem(
                                        text = { Text(if (nextPosted) strings.postToProfile else strings.archiveStory) },
                                        leadingIcon = {
                                            Icon(if (nextPosted) Icons.Rounded.Publish else Icons.Rounded.Archive, null)
                                        },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                clientManager.toggleStoryPostedToProfile(chatId, currentStory.id, nextPosted)
                                                onStoryChanged()
                                            }
                                        },
                                    )
                                }
                                if (currentStory.canBeDeleted) {
                                    DropdownMenuItem(
                                        text = { Text(strings.deleteStory, color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                clientManager.deleteStory(chatId, currentStory.id)
                                                stories = stories.filterNot { it.id == currentStory.id }
                                                onStoryChanged()
                                                if (stories.isEmpty()) onDismiss()
                                                else index = index.coerceAtMost(stories.lastIndex)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Bottom caption & view count
            if (currentStory != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    val captionText = currentStory.caption?.text.orEmpty()
                    if (captionText.isNotBlank()) {
                        Text(captionText, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    val views = currentStory.interactionInfo?.viewCount ?: 0
                    if (views > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Visibility, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$views", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStoriesGridSection(
    stories: List<TdApi.Story>,
    isLoading: Boolean,
    hintText: String?,
    showAddButton: Boolean,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onStoryClick: (Int) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!hintText.isNullOrBlank()) {
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp), color = tgColors.accent)
        } else if (stories.isEmpty()) {
            Text(
                text = strings.noStoriesYet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 28.dp),
            )
        } else {
            stories.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    for (col in 0 until 3) {
                        val st = rowItems.getOrNull(col)
                        if (st == null) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val idx = stories.indexOfFirst { it.id == st.id }
                            val file = when (val c = st.content) {
                                is TdApi.StoryContentPhoto -> c.photo.sizes.lastOrNull()?.photo
                                is TdApi.StoryContentVideo -> c.video.thumbnail?.file
                                else -> null
                            }
                            val resolved = file?.let { filesMap[it.id] ?: it }
                            LaunchedEffect(file?.id) {
                                if (file != null) clientManager.downloadFileIfNeeded(file, priority = 16)
                            }
                            val path = resolved?.local?.path?.takeIf {
                                resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.78f)
                                    .padding(bottom = 2.dp)
                                    .background(tgColors.pillBackground)
                                    .clickable { onStoryClick(idx.coerceAtLeast(0)) },
                            ) {
                                if (path != null) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = strings.storiesLabel,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                val views = st.interactionInfo?.viewCount ?: 0
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.52f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("$views", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddButton) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAddStoryClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.addPost, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileGiftsSection(
    gifts: List<TdApi.ReceivedGift>,
    isLoading: Boolean,
    isOwnProfile: Boolean,
    targetOwnerId: TdApi.MessageSender,
    usersMap: Map<Long, TdApi.User>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onGiftsUpdated: () -> Unit,
    onStatusMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var selectedGift by remember { mutableStateOf<TdApi.ReceivedGift?>(null) }
    var showSendGiftSheet by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "gift_float")
    val floatScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gift_scale",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isOwnProfile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(tgColors.pillBackground)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(strings.allGifts, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showSendGiftSheet = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(strings.addCollection, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp), color = tgColors.accent)
        } else if (gifts.isEmpty()) {
            Text(
                text = strings.noGiftsYet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                gifts.chunked(3).forEach { rowGifts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (col in 0 until 3) {
                            val rg = rowGifts.getOrNull(col)
                            if (rg == null) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val sticker = when (val sg = rg.gift) {
                                    is TdApi.SentGiftRegular -> sg.gift.sticker
                                    is TdApi.SentGiftUpgraded -> sg.gift.model.sticker
                                    else -> null
                                }
                                val stickerFile = when {
                                    sticker == null -> null
                                    sticker.format is TdApi.StickerFormatWebp -> sticker.sticker
                                    else -> sticker.thumbnail?.file
                                }
                                val resolved = stickerFile?.let { filesMap[it.id] ?: it }
                                val path = resolved?.local?.path?.takeIf {
                                    resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                                }
                                val senderUser = (rg.senderId as? TdApi.MessageSenderUser)?.let { usersMap[it.userId] }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.95f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(tgColors.pillBackground)
                                        .clickable { selectedGift = rg }
                                        .padding(8.dp),
                                ) {
                                    if (path != null) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = sticker?.emoji ?: "Gift",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                                .graphicsLayer { scaleX = floatScale; scaleY = floatScale },
                                        )
                                    } else {
                                        Text(
                                            text = sticker?.emoji?.ifBlank { "🎁" } ?: "🎁",
                                            fontSize = 38.sp,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .graphicsLayer { scaleX = floatScale; scaleY = floatScale },
                                        )
                                    }

                                    // Top-left sender / incognito badge matching screenshot 3
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (rg.isPrivate || senderUser == null) tgColors.accent else Color.Transparent),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (senderUser != null && !rg.isPrivate) {
                                            TelegramAvatar(
                                                id = senderUser.id,
                                                title = senderUser.displayName(),
                                                photoFile = senderUser.profilePhoto?.small,
                                                filesMap = filesMap,
                                                clientManager = clientManager,
                                                size = 22.dp,
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.VisibilityOff,
                                                contentDescription = strings.senderAnonymous,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp),
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

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = strings.giftsTapHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showSendGiftSheet = true },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Icon(Icons.Rounded.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.sendGiftsToFriends, fontWeight = FontWeight.SemiBold)
        }
    }

    selectedGift?.let { rg ->
        val senderUser = (rg.senderId as? TdApi.MessageSenderUser)?.let { usersMap[it.userId] }
        val sticker = when (val sg = rg.gift) {
            is TdApi.SentGiftRegular -> sg.gift.sticker
            is TdApi.SentGiftUpgraded -> sg.gift.model.sticker
            else -> null
        }
        val title = when (val sg = rg.gift) {
            is TdApi.SentGiftUpgraded -> "${sg.gift.title} #${sg.gift.number}"
            else -> "${sticker?.emoji ?: "🎁"} ${strings.giftsTab}"
        }
        ModalBottomSheet(onDismissRequest = { selectedGift = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (rg.isPrivate || senderUser == null) strings.senderAnonymous else senderUser.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (rg.text.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(rg.text.text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isOwnProfile) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    clientManager.toggleGiftSaved(rg.receivedGiftId, !rg.isSaved)
                                        .onSuccess {
                                            selectedGift = null
                                            onGiftsUpdated()
                                        }
                                        .onFailure { onStatusMessage(it.message ?: "Error") }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (rg.isSaved) strings.hideFromProfile else strings.showOnProfile, maxLines = 1)
                        }
                        if (rg.sellStarCount > 0) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        clientManager.sellGift(rg.receivedGiftId)
                                            .onSuccess {
                                                selectedGift = null
                                                onGiftsUpdated()
                                            }
                                            .onFailure { onStatusMessage(it.message ?: "Error") }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(String.format(strings.convertToStars, rg.sellStarCount), maxLines = 1)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showSendGiftSheet) {
        SendGiftModalSheet(
            targetOwnerId = targetOwnerId,
            filesMap = filesMap,
            clientManager = clientManager,
            onDismiss = { showSendGiftSheet = false },
            onSent = {
                showSendGiftSheet = false
                onGiftsUpdated()
                onStatusMessage(strings.savedToast)
            },
            onError = { onStatusMessage(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendGiftModalSheet(
    targetOwnerId: TdApi.MessageSender,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onDismiss: () -> Unit,
    onSent: () -> Unit,
    onError: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    val scope = rememberCoroutineScope()
    var available by remember { mutableStateOf<List<TdApi.AvailableGift>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var chosen by remember { mutableStateOf<TdApi.AvailableGift?>(null) }
    var giftMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        clientManager.getAvailableGifts().onSuccess { available = it }
        loading = false
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(strings.sendGiftTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            if (loading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tgColors.accent)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp),
                ) {
                    items(available, key = { it.gift.id }) { ag ->
                        val isSel = chosen?.gift?.id == ag.gift.id
                        val stFile = ag.gift.sticker.thumbnail?.file
                        val resolved = stFile?.let { filesMap[it.id] ?: it }
                        val path = resolved?.local?.path?.takeIf {
                            resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                        }
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) tgColors.accent.copy(alpha = 0.2f) else tgColors.pillBackground)
                                .border(
                                    width = if (isSel) 1.5.dp else 0.dp,
                                    color = if (isSel) tgColors.accent else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { chosen = ag }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (path != null) {
                                AsyncImage(model = File(path), contentDescription = null, modifier = Modifier.size(46.dp))
                            } else {
                                Text(ag.gift.sticker.emoji.ifBlank { "🎁" }, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⭐ ${ag.gift.starCount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (chosen != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = giftMessage,
                        onValueChange = { giftMessage = it },
                        label = { Text(strings.messageHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val g = chosen ?: return@Button
                            scope.launch {
                                clientManager.sendGiftToUser(g.gift.id, targetOwnerId, giftMessage)
                                    .onSuccess { onSent() }
                                    .onFailure { onError(it.message ?: "Error") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("${strings.sendGiftTitle} (⭐ ${chosen?.gift?.starCount})")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEmojiPackSheet(
    customEmojiId: Long,
    fallbackEmoji: String,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onDismiss: () -> Unit,
    onStatusMessage: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    val scope = rememberCoroutineScope()
    val sticker = customEmojiStickers[customEmojiId]
    var stickerSet by remember(customEmojiId, sticker?.setId) { mutableStateOf<TdApi.StickerSet?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(customEmojiId, sticker?.setId) {
        if (customEmojiId != 0L && sticker == null) {
            clientManager.loadCustomEmojis(listOf(customEmojiId))
        }
        val setId = sticker?.setId ?: 0L
        if (setId != 0L) {
            clientManager.getStickerSet(setId).onSuccess { stickerSet = it }
        }
        isLoading = false
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomEmojiIcon(
                    customEmojiId = customEmojiId,
                    fallbackEmoji = fallbackEmoji,
                    customEmojiStickers = customEmojiStickers,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    size = 34.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stickerSet?.title ?: strings.customEmojiPackTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stickerSet?.let { "${it.stickers.size} emoji · @${it.name}" }
                            ?: "Telegram Premium Emoji",
                        style = MaterialTheme.typography.bodySmall,
                        color = tgColors.accent,
                    )
                }
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF7B61FF))
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = tgColors.divider)
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tgColors.accent)
                }
            } else {
                val packStickers = stickerSet?.stickers?.toList().orEmpty()
                if (packStickers.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(190.dp),
                    ) {
                        items(packStickers, key = { it.id }) { st ->
                            val file = if (st.format is TdApi.StickerFormatWebp) st.sticker else st.thumbnail?.file
                            val resolved = file?.let { filesMap[it.id] ?: it }
                            val path = resolved?.local?.path?.takeIf {
                                resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tgColors.pillBackground),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (path != null && (path.endsWith(".webp", true) || path.endsWith(".png", true))) {
                                    AsyncImage(model = File(path), contentDescription = st.emoji, modifier = Modifier.size(32.dp))
                                } else {
                                    Text(st.emoji.ifBlank { "✨" }, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    val set = stickerSet
                    if (set != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    clientManager.installStickerSet(set.id, !set.isInstalled)
                                        .onSuccess {
                                            stickerSet = set.apply { isInstalled = !set.isInstalled }
                                            onStatusMessage(strings.emojiPackAdded)
                                        }
                                        .onFailure { onStatusMessage(it.message ?: "Error") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Rounded.Star, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (set.isInstalled) strings.emojiPackAdded else strings.addEmojiPack)
                        }
                    }
                } else {
                    Text(
                        text = "$fallbackEmoji · ID $customEmojiId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
