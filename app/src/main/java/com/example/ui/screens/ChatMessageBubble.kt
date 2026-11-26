package com.example.ui.screens

import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Forward
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WebAsset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.audio.PlaybackState
import com.example.data.LocalStrings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.tdlib.formatDuration
import com.example.tdlib.formatFileSize
import com.example.tdlib.formatTime
import com.example.tdlib.isServiceMessage
import com.example.tdlib.previewText
import com.example.ui.components.FormattedTelegramText
import com.example.ui.components.PremiumStatusBadge
import com.example.ui.components.rememberMinithumbnailBitmap
import com.example.ui.components.shouldAutoDownloadMedia
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.avatarGradientForId
import org.drinkless.tdlib.TdApi
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ChatMessageBubble(
    message: TdApi.Message,
    isGroupOrSupergroup: Boolean,
    isChannel: Boolean,
    lastReadOutboxId: Long,
    usersMap: Map<Long, TdApi.User>,
    chatsMap: Map<Long, TdApi.Chat>,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    playbackState: PlaybackState,
    pendingCallbackButtonKey: String?,
    clientManager: TelegramClientManager,
    onBubbleClick: () -> Unit,
    onBubbleLongClick: () -> Unit,
    onReplyClick: (Long) -> Unit,
    onPhotoClick: (String) -> Unit,
    onPlayAudio: (Long, String) -> Unit,
    onOpenDocument: (String, String) -> Unit,
    onToggleReaction: (String, Boolean) -> Unit,
    onVotePoll: (Long, Int) -> Unit,
    onInlineButtonClick: (TdApi.InlineKeyboardButton, String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val allowAutoDownload = remember(appearance) { shouldAutoDownloadMedia(context, appearance) }
    val content = message.content

    if (content.isServiceMessage()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = if (tgColors.isDark) 0.42f else 0.25f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                val senderName = resolveSenderName(message.senderId, usersMap, chatsMap)
                Text(
                    text = if (senderName.isNotBlank()) "$senderName ${content.previewText()}" else content.previewText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }
        }
        return
    }

    val isOutgoing = message.isOutgoing && !isChannel
    val bubbleColor = if (isOutgoing) tgColors.outgoingBubble else tgColors.incomingBubble
    val timeColor = if (isOutgoing) tgColors.outgoingTime else tgColors.incomingTime
    val corner = appearance.bubbleCornerRadiusDp.dp
    val tailCorner = (appearance.bubbleCornerRadiusDp / 4).coerceAtLeast(3).dp

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = corner, bottomEnd = tailCorner)
    } else {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = tailCorner, bottomEnd = corner)
    }

    val senderUser = (message.senderId as? TdApi.MessageSenderUser)?.let { usersMap[it.userId] }
    val senderName = if (!isOutgoing && isGroupOrSupergroup && !isChannel) {
        resolveSenderName(message.senderId, usersMap, chatsMap)
    } else ""

    val senderColor = when (val sid = message.senderId) {
        is TdApi.MessageSenderUser -> avatarGradientForId(sid.userId).second
        is TdApi.MessageSenderChat -> avatarGradientForId(sid.chatId).second
        else -> tgColors.accent
    }

    val msgFontSize = appearance.chatFontSizeSp.sp
    val msgTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = msgFontSize,
        lineHeight = (appearance.chatFontSizeSp + 6).sp,
    )

    val inlineMarkup = message.replyMarkup as? TdApi.ReplyMarkupInlineKeyboard

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 96.dp, max = 316.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = onBubbleClick,
                    onLongClick = onBubbleLongClick,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("message_bubble_${message.id}"),
        ) {
            if (senderName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = senderColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (senderUser != null && (senderUser.isPremium || senderUser.emojiStatus != null)) {
                        Spacer(modifier = Modifier.width(4.dp))
                        PremiumStatusBadge(
                            user = senderUser,
                            customEmojiStickers = customEmojiStickers,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 14.dp,
                            onCustomEmojiClick = onCustomEmojiClick,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            ForwardAndReplyHeaders(
                message = message,
                usersMap = usersMap,
                chatsMap = chatsMap,
                onReplyClick = onReplyClick,
            )

            when (content) {
                is TdApi.MessageText -> {
                    FormattedTelegramText(
                        formattedText = content.text,
                        style = msgTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        customEmojiStickers = customEmojiStickers,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessagePhoto -> {
                    PhotoMessageBody(
                        photo = content.photo,
                        caption = content.caption,
                        textStyle = msgTextStyle,
                        filesMap = filesMap,
                        customEmojiStickers = customEmojiStickers,
                        allowAutoDownload = allowAutoDownload,
                        clientManager = clientManager,
                        onPhotoClick = onPhotoClick,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessageVideo -> {
                    VideoMessageBody(
                        video = content.video,
                        caption = content.caption,
                        textStyle = msgTextStyle,
                        filesMap = filesMap,
                        customEmojiStickers = customEmojiStickers,
                        allowAutoDownload = allowAutoDownload,
                        clientManager = clientManager,
                        onOpenVideo = onOpenDocument,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessageVideoNote -> {
                    VideoNoteMessageBody(
                        videoNote = content.videoNote,
                        filesMap = filesMap,
                        allowAutoDownload = allowAutoDownload,
                        clientManager = clientManager,
                    )
                }
                is TdApi.MessageAnimation -> {
                    AnimationMessageBody(
                        animation = content.animation,
                        caption = content.caption,
                        textStyle = msgTextStyle,
                        filesMap = filesMap,
                        customEmojiStickers = customEmojiStickers,
                        allowAutoDownload = allowAutoDownload,
                        clientManager = clientManager,
                        onOpenAnimation = onOpenDocument,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessageVoiceNote -> {
                    VoiceNoteMessageBody(
                        messageId = message.id,
                        voiceNote = content.voiceNote,
                        filesMap = filesMap,
                        allowAutoDownload = allowAutoDownload,
                        playbackState = playbackState,
                        clientManager = clientManager,
                        onPlayToggle = onPlayAudio,
                    )
                }
                is TdApi.MessageAudio -> {
                    AudioMessageBody(
                        messageId = message.id,
                        audio = content.audio,
                        filesMap = filesMap,
                        allowAutoDownload = allowAutoDownload,
                        playbackState = playbackState,
                        clientManager = clientManager,
                        onPlayToggle = onPlayAudio,
                    )
                }
                is TdApi.MessageDocument -> {
                    DocumentMessageBody(
                        document = content.document,
                        caption = content.caption,
                        textStyle = msgTextStyle,
                        filesMap = filesMap,
                        customEmojiStickers = customEmojiStickers,
                        allowAutoDownload = allowAutoDownload,
                        clientManager = clientManager,
                        onOpenDocument = onOpenDocument,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessageSticker -> {
                    val stickerFile = content.sticker.sticker
                    val resolved = filesMap[stickerFile.id] ?: stickerFile
                    val path = resolved.local.path.takeIf {
                        resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
                    }
                    LaunchedEffect(stickerFile.id, allowAutoDownload) {
                        if (allowAutoDownload) {
                            clientManager.downloadFileIfNeeded(stickerFile, priority = 14)
                        }
                    }
                    if (path != null && (path.endsWith(".webp") || path.endsWith(".png"))) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = content.sticker.emoji,
                            modifier = Modifier.size(140.dp),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(tgColors.accent.copy(alpha = 0.08f))
                                .clickable { clientManager.downloadFileIfNeeded(stickerFile, priority = 22) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = content.sticker.emoji.ifBlank { "✨" }, fontSize = 48.sp)
                            if (resolved.local.isDownloadingActive) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
                is TdApi.MessagePoll -> {
                    PollMessageBody(
                        messageId = message.id,
                        poll = content.poll,
                        customEmojiStickers = customEmojiStickers,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        onVoteOption = { optIdx -> onVotePoll(message.id, optIdx) },
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
                is TdApi.MessageCall -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (content.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Call,
                            contentDescription = null,
                            tint = tgColors.accent,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (content.isVideo) "Videoqoʻngʻiroq" else "Ovozli qoʻngʻiroq",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (content.duration > 0) {
                                Text(
                                    text = formatDuration(content.duration),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = content.previewText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ReactionsRow(message = message, onToggleReaction = onToggleReaction)

            MessageMetaFooter(
                message = message,
                isOutgoing = isOutgoing,
                lastReadOutboxId = lastReadOutboxId,
                timeColor = timeColor,
                modifier = Modifier.align(Alignment.End),
            )
        }

        if (inlineMarkup != null && inlineMarkup.rows.isNotEmpty()) {
            InlineKeyboardRows(
                messageId = message.id,
                inlineMarkup = inlineMarkup,
                pendingCallbackButtonKey = pendingCallbackButtonKey,
                customEmojiStickers = customEmojiStickers,
                filesMap = filesMap,
                clientManager = clientManager,
                onInlineButtonClick = onInlineButtonClick,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumMessageBubble(
    messages: List<TdApi.Message>,
    isGroupOrSupergroup: Boolean,
    isChannel: Boolean,
    lastReadOutboxId: Long,
    usersMap: Map<Long, TdApi.User>,
    chatsMap: Map<Long, TdApi.Chat>,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    pendingCallbackButtonKey: String?,
    clientManager: TelegramClientManager,
    onBubbleClick: (TdApi.Message) -> Unit,
    onBubbleLongClick: (TdApi.Message) -> Unit,
    onReplyClick: (Long) -> Unit,
    onPhotoClick: (String) -> Unit,
    onOpenDocument: (String, String) -> Unit,
    onToggleReaction: (Long, String, Boolean) -> Unit,
    onInlineButtonClick: (TdApi.Message, TdApi.InlineKeyboardButton, String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val primary = messages.firstOrNull() ?: return
    val tgColors = LocalTelegramColors.current
    val appearance = LocalAppearanceSettings.current
    val context = LocalContext.current
    val allowAutoDownload = remember(appearance) { shouldAutoDownloadMedia(context, appearance) }

    val isOutgoing = primary.isOutgoing && !isChannel
    val bubbleColor = if (isOutgoing) tgColors.outgoingBubble else tgColors.incomingBubble
    val timeColor = if (isOutgoing) tgColors.outgoingTime else tgColors.incomingTime
    val corner = appearance.bubbleCornerRadiusDp.dp
    val tailCorner = (appearance.bubbleCornerRadiusDp / 4).coerceAtLeast(3).dp
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = corner, bottomEnd = tailCorner)
    } else {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = tailCorner, bottomEnd = corner)
    }

    val senderUser = (primary.senderId as? TdApi.MessageSenderUser)?.let { usersMap[it.userId] }
    val senderName = if (!isOutgoing && isGroupOrSupergroup && !isChannel) {
        resolveSenderName(primary.senderId, usersMap, chatsMap)
    } else ""
    val senderColor = when (val sid = primary.senderId) {
        is TdApi.MessageSenderUser -> avatarGradientForId(sid.userId).second
        is TdApi.MessageSenderChat -> avatarGradientForId(sid.chatId).second
        else -> tgColors.accent
    }

    val captionText = remember(messages) {
        messages.firstNotNullOfOrNull { m ->
            when (val c = m.content) {
                is TdApi.MessagePhoto -> c.caption.takeIf { it.text.isNotBlank() }
                is TdApi.MessageVideo -> c.caption.takeIf { it.text.isNotBlank() }
                is TdApi.MessageDocument -> c.caption.takeIf { it.text.isNotBlank() }
                else -> null
            }
        }
    }

    val msgTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = appearance.chatFontSizeSp.sp,
        lineHeight = (appearance.chatFontSizeSp + 6).sp,
    )

    val markupMsg = messages.lastOrNull { (it.replyMarkup as? TdApi.ReplyMarkupInlineKeyboard)?.rows?.isNotEmpty() == true }
    val inlineMarkup = markupMsg?.replyMarkup as? TdApi.ReplyMarkupInlineKeyboard

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 240.dp, max = 316.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = { onBubbleClick(primary) },
                    onLongClick = { onBubbleLongClick(primary) },
                )
                .padding(8.dp)
                .testTag("album_bubble_${primary.mediaAlbumId}"),
        ) {
            if (senderName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = senderColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (senderUser != null && (senderUser.isPremium || senderUser.emojiStatus != null)) {
                        Spacer(modifier = Modifier.width(4.dp))
                        PremiumStatusBadge(senderUser, customEmojiStickers, filesMap, clientManager, size = 14.dp)
                    }
                }
            }

            ForwardAndReplyHeaders(
                message = primary,
                usersMap = usersMap,
                chatsMap = chatsMap,
                onReplyClick = onReplyClick,
            )

            // Collage Mosaic Grid (2 columns, or 1 hero top + pairs)
            val rows = messages.chunked(2)
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        rowItems.forEach { itemMsg ->
                            AlbumTile(
                                message = itemMsg,
                                filesMap = filesMap,
                                allowAutoDownload = allowAutoDownload,
                                clientManager = clientManager,
                                onPhotoClick = onPhotoClick,
                                onOpenVideo = onOpenDocument,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (rowItems.size == 1) 190.dp else 134.dp),
                            )
                        }
                    }
                }
            }

            if (captionText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    FormattedTelegramText(
                        formattedText = captionText,
                        style = msgTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        customEmojiStickers = customEmojiStickers,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        onUrlClick = onUrlClick,
                        onBotCommandClick = onBotCommandClick,
                        onCustomEmojiClick = onCustomEmojiClick,
                    )
                }
            }

            ReactionsRow(
                message = primary,
                onToggleReaction = { emoji, chosen -> onToggleReaction(primary.id, emoji, chosen) },
            )

            MessageMetaFooter(
                message = primary,
                isOutgoing = isOutgoing,
                lastReadOutboxId = lastReadOutboxId,
                timeColor = timeColor,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 4.dp),
            )
        }

        if (markupMsg != null && inlineMarkup != null && inlineMarkup.rows.isNotEmpty()) {
            InlineKeyboardRows(
                messageId = markupMsg.id,
                inlineMarkup = inlineMarkup,
                pendingCallbackButtonKey = pendingCallbackButtonKey,
                customEmojiStickers = customEmojiStickers,
                filesMap = filesMap,
                clientManager = clientManager,
                onInlineButtonClick = { btn, key -> onInlineButtonClick(markupMsg, btn, key) },
            )
        }
    }
}

@Composable
private fun AlbumTile(
    message: TdApi.Message,
    filesMap: Map<Int, TdApi.File>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
    onPhotoClick: (String) -> Unit,
    onOpenVideo: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val c = message.content) {
        is TdApi.MessagePhoto -> {
            val best = c.photo.sizes.lastOrNull()
            val file = best?.photo?.let { filesMap[it.id] ?: it }
            val path = file?.local?.path?.takeIf {
                file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
            }
            val thumb = rememberMinithumbnailBitmap(c.photo.minithumbnail)
            LaunchedEffect(best?.photo?.id, allowAutoDownload) {
                if (allowAutoDownload && best != null) {
                    clientManager.downloadFileIfNeeded(best.photo, priority = 18)
                }
            }
            Box(
                modifier = modifier
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable {
                        if (path != null) onPhotoClick(path)
                        else if (best != null) clientManager.downloadFileIfNeeded(best.photo, priority = 24)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (path != null) {
                    AsyncImage(model = File(path), contentDescription = "Album photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    if (thumb != null) {
                        Image(bitmap = thumb, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(10.dp))
                    }
                    DownloadBadgePill(
                        isDownloading = file?.local?.isDownloadingActive == true,
                        sizeBytes = file?.expectedSize?.coerceAtLeast(file.size) ?: 0L,
                    )
                }
            }
        }
        is TdApi.MessageVideo -> {
            val file = filesMap[c.video.video.id] ?: c.video.video
            val path = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }
            val thumb = rememberMinithumbnailBitmap(c.video.minithumbnail)
            LaunchedEffect(c.video.video.id, allowAutoDownload) {
                if (allowAutoDownload) {
                    clientManager.downloadFileIfNeeded(c.video.video, priority = 18)
                }
            }
            Box(
                modifier = modifier
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable {
                        if (path != null) onOpenVideo(path, c.video.mimeType.ifBlank { "video/mp4" })
                        else clientManager.downloadFileIfNeeded(c.video.video, priority = 24)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (thumb != null) {
                    Image(bitmap = thumb, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(if (path != null) 0.dp else 10.dp))
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        path != null -> Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                        else -> Icon(Icons.Rounded.Download, null, tint = Color.White)
                    }
                }
                Text(
                    text = formatDuration(c.video.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }
        else -> {
            Box(modifier = modifier.background(Color.Black.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Text(c.previewText(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun ForwardAndReplyHeaders(
    message: TdApi.Message,
    usersMap: Map<Long, TdApi.User>,
    chatsMap: Map<Long, TdApi.Chat>,
    onReplyClick: (Long) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    message.forwardInfo?.let { fwd ->
        val originName = when (val o = fwd.origin) {
            is TdApi.MessageOriginUser -> usersMap[o.senderUserId].displayName()
            is TdApi.MessageOriginChannel -> chatsMap[o.chatId]?.title ?: "Kanal"
            is TdApi.MessageOriginChat -> chatsMap[o.senderChatId]?.title ?: "Chat"
            is TdApi.MessageOriginHiddenUser -> o.senderName
            else -> ""
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 3.dp)) {
            Icon(Icons.Rounded.Forward, null, tint = tgColors.accent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${strings.forwardedFrom}$originName",
                style = MaterialTheme.typography.labelSmall,
                color = tgColors.accent,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    val replyMsg = message.replyTo as? TdApi.MessageReplyToMessage
    if (replyMsg != null && replyMsg.messageId != 0L) {
        val replyAuthor = when (val o = replyMsg.origin) {
            is TdApi.MessageOriginUser -> usersMap[o.senderUserId].displayName()
            is TdApi.MessageOriginChannel -> chatsMap[o.chatId]?.title ?: "Kanal"
            is TdApi.MessageOriginHiddenUser -> o.senderName
            else -> strings.reply
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tgColors.accent.copy(alpha = 0.14f))
                .clickable { onReplyClick(replyMsg.messageId) }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(tgColors.accent),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = replyAuthor,
                    style = MaterialTheme.typography.labelSmall,
                    color = tgColors.accent,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = replyMsg.content?.previewText() ?: "Xabar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReactionsRow(
    message: TdApi.Message,
    onToggleReaction: (String, Boolean) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val reactions = message.interactionInfo?.reactions?.reactions
    if (reactions.isNullOrEmpty()) return
    Spacer(modifier = Modifier.height(6.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        reactions.forEach { reaction ->
            val emoji = (reaction.type as? TdApi.ReactionTypeEmoji)?.emoji ?: "👍"
            val chosen = reaction.isChosen
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (chosen) tgColors.accent.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                    .border(
                        width = if (chosen) 1.dp else 0.dp,
                        color = if (chosen) tgColors.accent else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { onToggleReaction(emoji, chosen) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reaction.totalCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chosen) tgColors.accent else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MessageMetaFooter(
    message: TdApi.Message,
    isOutgoing: Boolean,
    lastReadOutboxId: Long,
    timeColor: Color,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Spacer(modifier = Modifier.height(3.dp))
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val views = message.interactionInfo?.viewCount ?: 0
        if (views > 0) {
            Icon(Icons.Rounded.Visibility, null, tint = timeColor, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = views.toString(), style = MaterialTheme.typography.labelSmall, color = timeColor)
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (message.editDate > 0) {
            Text(
                text = strings.editedLabel,
                style = MaterialTheme.typography.labelSmall,
                color = timeColor,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(end = 5.dp),
            )
        }
        Text(text = formatTime(message.date), style = MaterialTheme.typography.labelSmall, color = timeColor)
        if (isOutgoing) {
            Spacer(modifier = Modifier.width(4.dp))
            val isPending = message.sendingState is TdApi.MessageSendingStatePending
            val isRead = message.id <= lastReadOutboxId
            Icon(
                imageVector = when {
                    isPending -> Icons.Rounded.Schedule
                    isRead -> Icons.Rounded.DoneAll
                    else -> Icons.Rounded.Check
                },
                contentDescription = null,
                tint = timeColor,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun InlineKeyboardRows(
    messageId: Long,
    inlineMarkup: TdApi.ReplyMarkupInlineKeyboard,
    pendingCallbackButtonKey: String?,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onInlineButtonClick: (TdApi.InlineKeyboardButton, String) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Spacer(modifier = Modifier.height(4.dp))
    Column(
        modifier = Modifier.widthIn(min = 160.dp, max = 316.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        inlineMarkup.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEachIndexed { colIndex, btn ->
                    val btnKey = "${messageId}_${rowIndex}_$colIndex"
                    val isLoading = pendingCallbackButtonKey == btnKey
                    val isWebApp = btn.type is TdApi.InlineKeyboardButtonTypeWebApp
                    val isUrl = btn.type is TdApi.InlineKeyboardButtonTypeUrl
                    val isBuy = btn.type is TdApi.InlineKeyboardButtonTypeBuy

                    val bgBrush = when {
                        isWebApp -> Brush.horizontalGradient(listOf(Color(0xFF2AA1F2).copy(alpha = 0.85f), Color(0xFF5B67CA).copy(alpha = 0.85f)))
                        isBuy -> Brush.horizontalGradient(listOf(Color(0xFF2E7D32).copy(alpha = 0.85f), Color(0xFF4CAF50).copy(alpha = 0.85f)))
                        isUrl -> Brush.horizontalGradient(listOf(tgColors.accent.copy(alpha = 0.25f), tgColors.incomingBubble.copy(alpha = 0.92f)))
                        else -> Brush.horizontalGradient(listOf(tgColors.incomingBubble.copy(alpha = 0.9f), tgColors.incomingBubble.copy(alpha = 0.9f)))
                    }
                    val labelColor = when {
                        isWebApp || isBuy -> Color.White
                        tgColors.isDark -> Color.White
                        else -> tgColors.accent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgBrush)
                            .border(
                                width = 0.6.dp,
                                color = if (isWebApp || isBuy) Color.White.copy(alpha = 0.25f) else tgColors.accent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable(enabled = !isLoading) { onInlineButtonClick(btn, btnKey) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("inline_btn_${btnKey}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = labelColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp),
                            )
                        } else {
                            Text(
                                text = btn.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if ((isUrl || isWebApp) && !isLoading) {
                            Icon(
                                imageVector = if (isWebApp) Icons.Rounded.WebAsset else Icons.Rounded.NorthEast,
                                contentDescription = null,
                                tint = labelColor.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadBadgePill(
    isDownloading: Boolean,
    sizeBytes: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isDownloading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
        } else {
            Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(24.dp))
        }
        if (sizeBytes > 0L) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatFileSize(sizeBytes),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PhotoMessageBody(
    photo: TdApi.Photo,
    caption: TdApi.FormattedText,
    textStyle: androidx.compose.ui.text.TextStyle,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
    onPhotoClick: (String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val bestSize = photo.sizes.lastOrNull() ?: return
    val resolved = filesMap[bestSize.photo.id] ?: bestSize.photo
    val path = resolved.local.path.takeIf {
        resolved.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
    }
    val miniThumb = rememberMinithumbnailBitmap(photo.minithumbnail)

    LaunchedEffect(bestSize.photo.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(bestSize.photo, priority = 20)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.16f))
            .clickable {
                if (path != null) onPhotoClick(path)
                else clientManager.downloadFileIfNeeded(bestSize.photo, priority = 24)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (path != null) {
            AsyncImage(
                model = File(path),
                contentDescription = caption.text.ifBlank { "Photo" },
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            if (miniThumb != null) {
                Image(
                    bitmap = miniThumb,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(12.dp),
                )
            }
            DownloadBadgePill(
                isDownloading = resolved.local.isDownloadingActive,
                sizeBytes = resolved.expectedSize.coerceAtLeast(resolved.size),
            )
        }
    }
    if (caption.text.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        FormattedTelegramText(
            formattedText = caption,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onUrlClick = onUrlClick,
            onBotCommandClick = onBotCommandClick,
            onCustomEmojiClick = onCustomEmojiClick,
        )
    }
}

@Composable
private fun VideoMessageBody(
    video: TdApi.Video,
    caption: TdApi.FormattedText,
    textStyle: androidx.compose.ui.text.TextStyle,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
    onOpenVideo: (String, String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val file = filesMap[video.video.id] ?: video.video
    val thumbFile = video.thumbnail?.file?.let { filesMap[it.id] ?: it }
    val thumbPath = thumbFile?.local?.path?.takeIf {
        thumbFile.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
    }
    val miniThumb = rememberMinithumbnailBitmap(video.minithumbnail)
    val videoPath = file.local.path.takeIf {
        file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
    }

    LaunchedEffect(video.video.id, allowAutoDownload) {
        if (allowAutoDownload) {
            video.thumbnail?.file?.let { clientManager.downloadFileIfNeeded(it, priority = 16) }
            clientManager.downloadFileIfNeeded(video.video, priority = 16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable {
                if (videoPath != null) onOpenVideo(videoPath, video.mimeType.ifBlank { "video/mp4" })
                else clientManager.downloadFileIfNeeded(video.video, priority = 24)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (thumbPath != null) {
            AsyncImage(model = File(thumbPath), contentDescription = "Video", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        } else if (miniThumb != null) {
            Image(
                bitmap = miniThumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().blur(10.dp),
            )
        }
        Box(
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                videoPath != null -> Icon(Icons.Rounded.PlayArrow, "Play video", tint = Color.White, modifier = Modifier.size(28.dp))
                else -> Icon(Icons.Rounded.Download, "Download video", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = "${formatDuration(video.duration)} · ${formatFileSize(video.video.expectedSize)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
    if (caption.text.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        FormattedTelegramText(
            formattedText = caption,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onUrlClick = onUrlClick,
            onBotCommandClick = onBotCommandClick,
            onCustomEmojiClick = onCustomEmojiClick,
        )
    }
}

@Composable
private fun VideoNoteMessageBody(
    videoNote: TdApi.VideoNote,
    filesMap: Map<Int, TdApi.File>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
) {
    val file = filesMap[videoNote.video.id] ?: videoNote.video
    val localPath = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }
    val miniThumb = rememberMinithumbnailBitmap(videoNote.minithumbnail)
    var isPlayingVideo by remember { mutableStateOf(false) }

    LaunchedEffect(videoNote.video.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(videoNote.video, priority = 16)
        }
    }

    Box(
        modifier = Modifier
            .size(196.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable {
                if (localPath != null) {
                    isPlayingVideo = !isPlayingVideo
                } else {
                    clientManager.downloadFileIfNeeded(videoNote.video, priority = 24)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isPlayingVideo && localPath != null) {
            CircularVideoPlayer(
                filePath = localPath,
                onFinished = { isPlayingVideo = false },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            if (miniThumb != null) {
                Image(
                    bitmap = miniThumb,
                    contentDescription = "Video note",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(if (localPath != null) 2.dp else 10.dp),
                )
            }
            DownloadBadgePill(
                isDownloading = file.local.isDownloadingActive,
                sizeBytes = if (localPath == null) file.expectedSize.coerceAtLeast(file.size) else 0L,
            )
            if (localPath != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play video note", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = formatDuration(videoNote.duration),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun CircularVideoPlayer(
    filePath: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(filePath) {
        onDispose {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
            mediaPlayer = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, w: Int, h: Int) {
                        runCatching {
                            val mp = MediaPlayer().apply {
                                setDataSource(filePath)
                                setSurface(Surface(surface))
                                setOnCompletionListener { onFinished() }
                                prepare()
                                start()
                            }
                            mediaPlayer = mp
                        }
                    }
                    override fun onSurfaceTextureSizeChanged(s: android.graphics.SurfaceTexture, w: Int, h: Int) {}
                    override fun onSurfaceTextureDestroyed(s: android.graphics.SurfaceTexture): Boolean {
                        runCatching {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                        }
                        mediaPlayer = null
                        return true
                    }
                    override fun onSurfaceTextureUpdated(s: android.graphics.SurfaceTexture) {}
                }
            }
        },
    )
}

@Composable
private fun AnimationMessageBody(
    animation: TdApi.Animation,
    caption: TdApi.FormattedText,
    textStyle: androidx.compose.ui.text.TextStyle,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
    onOpenAnimation: (String, String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val tgColors = LocalTelegramColors.current
    val file = filesMap[animation.animation.id] ?: animation.animation
    val localPath = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }
    val miniThumb = rememberMinithumbnailBitmap(animation.minithumbnail)

    LaunchedEffect(animation.animation.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(animation.animation, priority = 12)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tgColors.pillBackground)
            .clickable {
                if (localPath != null) onOpenAnimation(localPath, animation.mimeType.ifBlank { "video/mp4" })
                else clientManager.downloadFileIfNeeded(animation.animation, priority = 22)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (miniThumb != null) {
            Image(
                bitmap = miniThumb,
                contentDescription = "GIF",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().blur(if (localPath != null) 0.dp else 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                localPath != null -> Icon(Icons.Rounded.GifBox, null, tint = Color.White, modifier = Modifier.size(18.dp))
                else -> Icon(Icons.Rounded.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "GIF · ${formatFileSize(file.expectedSize.coerceAtLeast(file.size))}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
    if (caption.text.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        FormattedTelegramText(
            formattedText = caption,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onUrlClick = onUrlClick,
            onBotCommandClick = onBotCommandClick,
            onCustomEmojiClick = onCustomEmojiClick,
        )
    }
}

@Composable
private fun PollMessageBody(
    messageId: Long,
    poll: TdApi.Poll,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onVoteOption: (Int) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val tgColors = LocalTelegramColors.current
    val hasVoted = poll.options.any { it.isChosen } || poll.isClosed

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Poll, null, tint = tgColors.accent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (poll.isAnonymous) "Anonim soʻrovnoma" else "Ochiq soʻrovnoma",
                style = MaterialTheme.typography.labelSmall,
                color = tgColors.accent,
                fontWeight = FontWeight.SemiBold,
            )
        }
        FormattedTelegramText(
            formattedText = poll.question,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onCustomEmojiClick = onCustomEmojiClick,
        )
        poll.options.forEachIndexed { idx, option ->
            val pct = option.votePercentage.coerceIn(0, 100)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .clickable(enabled = !poll.isClosed) { onVoteOption(idx) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (option.isChosen) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (option.isChosen) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FormattedTelegramText(
                        formattedText = option.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        customEmojiStickers = customEmojiStickers,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        onCustomEmojiClick = onCustomEmojiClick,
                        modifier = Modifier.weight(1f),
                    )
                    if (hasVoted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.labelMedium,
                            color = tgColors.accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (hasVoted) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct / 100f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(tgColors.accent),
                        )
                    }
                }
            }
        }
        Text(
            text = "${poll.totalVoterCount} ta ovoz",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VoiceNoteMessageBody(
    messageId: Long,
    voiceNote: TdApi.VoiceNote,
    filesMap: Map<Int, TdApi.File>,
    allowAutoDownload: Boolean,
    playbackState: PlaybackState,
    clientManager: TelegramClientManager,
    onPlayToggle: (Long, String) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val file = filesMap[voiceNote.voice.id] ?: voiceNote.voice
    val localPath = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }
    val isThisActive = playbackState.activeMessageId == messageId
    val isPlaying = isThisActive && playbackState.isPlaying

    LaunchedEffect(voiceNote.voice.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(voiceNote.voice, priority = 16)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.widthIn(min = 190.dp).padding(vertical = 2.dp)) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tgColors.accent)
                .clickable {
                    if (localPath != null) onPlayToggle(messageId, localPath)
                    else clientManager.downloadFileIfNeeded(voiceNote.voice, priority = 24)
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                localPath == null -> Icon(Icons.Rounded.Download, "Download voice", tint = Color.White)
                isPlaying -> Icon(Icons.Rounded.Pause, "Pause", tint = Color.White)
                else -> Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(22.dp)) {
                val progress = if (isThisActive && playbackState.durationMs > 0) {
                    playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()
                } else 0f
                val bars = 24
                for (i in 0 until bars) {
                    val fraction = ((i * 7 + 3) % 10) / 10f
                    val barHeight = (6 + fraction * 14).dp
                    val filled = (i.toFloat() / bars) <= progress
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (filled) tgColors.accent else tgColors.accent.copy(alpha = 0.35f)),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            val timeLabel = if (isThisActive) "${formatDuration(playbackState.positionMs / 1000)} / ${formatDuration(voiceNote.duration)}"
            else "${formatDuration(voiceNote.duration)} · ${formatFileSize(file.expectedSize.coerceAtLeast(file.size))}"
            Text(text = timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AudioMessageBody(
    messageId: Long,
    audio: TdApi.Audio,
    filesMap: Map<Int, TdApi.File>,
    allowAutoDownload: Boolean,
    playbackState: PlaybackState,
    clientManager: TelegramClientManager,
    onPlayToggle: (Long, String) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val file = filesMap[audio.audio.id] ?: audio.audio
    val localPath = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }
    val isPlaying = playbackState.activeMessageId == messageId && playbackState.isPlaying

    LaunchedEffect(audio.audio.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(audio.audio, priority = 14)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tgColors.accent)
                .clickable {
                    if (localPath != null) onPlayToggle(messageId, localPath)
                    else clientManager.downloadFileIfNeeded(audio.audio, priority = 24)
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                localPath == null -> Icon(Icons.Rounded.Download, "Download audio", tint = Color.White)
                isPlaying -> Icon(Icons.Rounded.Pause, "Pause", tint = Color.White)
                else -> Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = audio.title.ifBlank { audio.fileName.ifBlank { "Audio" } }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${audio.performer.ifBlank { "Audio" }} · ${formatDuration(audio.duration)} · ${formatFileSize(file.expectedSize.coerceAtLeast(file.size))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DocumentMessageBody(
    document: TdApi.Document,
    caption: TdApi.FormattedText,
    textStyle: androidx.compose.ui.text.TextStyle,
    filesMap: Map<Int, TdApi.File>,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    allowAutoDownload: Boolean,
    clientManager: TelegramClientManager,
    onOpenDocument: (String, String) -> Unit,
    onUrlClick: (String) -> Unit,
    onBotCommandClick: (String) -> Unit,
    onCustomEmojiClick: (Long, String) -> Unit = { _, _ -> },
) {
    val tgColors = LocalTelegramColors.current
    val file = filesMap[document.document.id] ?: document.document
    val localPath = file.local.path.takeIf { file.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists() }

    LaunchedEffect(document.document.id, allowAutoDownload) {
        if (allowAutoDownload) {
            clientManager.downloadFileIfNeeded(document.document, priority = 12)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (localPath != null) onOpenDocument(localPath, document.mimeType.ifBlank { "*/*" })
                else clientManager.downloadFileIfNeeded(document.document, priority = 24)
            }
            .padding(vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(tgColors.accent),
            contentAlignment = Alignment.Center,
        ) {
            when {
                file.local.isDownloadingActive -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                localPath != null -> Icon(Icons.Rounded.Description, "Open file", tint = Color.White)
                else -> Icon(Icons.Rounded.Download, "Download file", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(text = document.fileName.ifBlank { "Fayl" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = formatFileSize(file.expectedSize.coerceAtLeast(file.size)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (caption.text.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        FormattedTelegramText(
            formattedText = caption,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            onUrlClick = onUrlClick,
            onBotCommandClick = onBotCommandClick,
            onCustomEmojiClick = onCustomEmojiClick,
        )
    }
}

fun resolveSenderName(
    sender: TdApi.MessageSender?,
    usersMap: Map<Long, TdApi.User>,
    chatsMap: Map<Long, TdApi.Chat>,
): String {
    return when (sender) {
        is TdApi.MessageSenderUser -> usersMap[sender.userId].displayName()
        is TdApi.MessageSenderChat -> chatsMap[sender.chatId]?.title ?: "Kanal"
        else -> ""
    }
}
