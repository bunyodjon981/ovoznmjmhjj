package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.UserAppearanceSettings
import com.example.tdlib.TelegramClientManager
import com.example.ui.theme.LocalTelegramColors
import org.drinkless.tdlib.TdApi
import java.io.File

private val PremiumPurple = Color(0xFF7B61FF)
private val PremiumCyan = Color(0xFF2AA1F2)

fun shouldAutoDownloadMedia(context: Context, appearance: UserAppearanceSettings): Boolean {
    if (!appearance.autoDownloadMobile && !appearance.autoDownloadWifi && !appearance.autoDownloadRoaming) {
        return false
    }
    return runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> appearance.autoDownloadWifi
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) -> appearance.autoDownloadRoaming
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> appearance.autoDownloadMobile
            else -> false
        }
    }.getOrDefault(false)
}

@Composable
fun rememberMinithumbnailBitmap(minithumbnail: TdApi.Minithumbnail?): ImageBitmap? {
    return remember(minithumbnail) {
        val data = minithumbnail?.data ?: return@remember null
        if (data.isEmpty()) return@remember null
        runCatching {
            BitmapFactory.decodeByteArray(data, 0, data.size)?.asImageBitmap()
        }.getOrNull()
    }
}

@Composable
fun CustomEmojiIcon(
    customEmojiId: Long,
    fallbackEmoji: String,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    LaunchedEffect(customEmojiId) {
        if (customEmojiId != 0L) {
            clientManager.loadCustomEmojis(listOf(customEmojiId))
        }
    }
    val sticker = customEmojiStickers[customEmojiId]
    val candidateFile = when {
        sticker == null -> null
        sticker.format is TdApi.StickerFormatWebp -> sticker.sticker
        else -> sticker.thumbnail?.file
    }
    val resolved = candidateFile?.let { filesMap[it.id] ?: it }
    val localPath = resolved?.local?.path?.takeIf {
        resolved.local.isDownloadingCompleted &&
            it.isNotBlank() &&
            File(it).exists() &&
            (it.endsWith(".webp", true) || it.endsWith(".png", true) || it.endsWith(".jpg", true))
    }

    val clickMod = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    if (localPath != null) {
        AsyncImage(
            model = File(localPath),
            contentDescription = fallbackEmoji.ifBlank { "Custom emoji" },
            contentScale = ContentScale.Fit,
            modifier = clickMod.size(size),
        )
    } else {
        Box(
            modifier = clickMod
                .size(size)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PremiumPurple.copy(alpha = 0.28f), PremiumCyan.copy(alpha = 0.28f)),
                    )
                )
                .border(
                    width = 0.7.dp,
                    brush = Brush.linearGradient(listOf(PremiumPurple, PremiumCyan)),
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallbackEmoji.ifBlank { "✦" },
                fontSize = (size.value * 0.68f).sp,
                lineHeight = (size.value * 0.8f).sp,
            )
        }
    }
}

@Composable
fun PremiumStatusBadge(
    user: TdApi.User?,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier,
    onCustomEmojiClick: ((Long, String) -> Unit)? = null,
) {
    if (user == null) return
    val statusCustomId = (user.emojiStatus?.type as? TdApi.EmojiStatusTypeCustomEmoji)?.customEmojiId ?: 0L
    if (statusCustomId != 0L) {
        CustomEmojiIcon(
            customEmojiId = statusCustomId,
            fallbackEmoji = "⭐",
            customEmojiStickers = customEmojiStickers,
            filesMap = filesMap,
            clientManager = clientManager,
            size = size,
            modifier = modifier,
            onClick = onCustomEmojiClick?.let { cb -> { cb(statusCustomId, "⭐") } },
        )
    } else if (user.isPremium) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PremiumPurple, PremiumCyan))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Premium",
                tint = Color.White,
                modifier = Modifier.size(size * 0.68f),
            )
        }
    }
}

@Composable
fun FormattedTelegramText(
    formattedText: TdApi.FormattedText?,
    style: TextStyle,
    color: Color,
    customEmojiStickers: Map<Long, TdApi.Sticker>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onUrlClick: ((String) -> Unit)? = null,
    onBotCommandClick: ((String) -> Unit)? = null,
    onMentionClick: ((String) -> Unit)? = null,
    onCustomEmojiClick: ((Long, String) -> Unit)? = null,
) {
    if (formattedText == null) return
    val rawText = formattedText.text
    if (rawText.isEmpty()) return

    val tgColors = LocalTelegramColors.current
    val entities = formattedText.entities ?: emptyArray()
    var spoilerRevealed by remember(rawText) { mutableStateOf(false) }

    val customEmojiIds = remember(entities) {
        entities.mapNotNull { (it.type as? TdApi.TextEntityTypeCustomEmoji)?.customEmojiId }.distinct()
    }
    LaunchedEffect(customEmojiIds) {
        if (customEmojiIds.isNotEmpty()) {
            clientManager.loadCustomEmojis(customEmojiIds)
        }
    }

    val customEmojiEntities = remember(entities, rawText) {
        entities.filter { it.type is TdApi.TextEntityTypeCustomEmoji }
            .sortedBy { it.offset }
    }

    val annotated = remember(rawText, entities, tgColors.accent, spoilerRevealed) {
        buildAnnotatedString {
            if (customEmojiEntities.isEmpty()) {
                append(rawText)
            } else {
                var cursor = 0
                for (entity in customEmojiEntities) {
                    val start = entity.offset.coerceIn(0, rawText.length)
                    val end = (entity.offset + entity.length).coerceIn(start, rawText.length)
                    if (start < cursor) continue
                    if (start > cursor) {
                        append(rawText.substring(cursor, start))
                    }
                    val ceType = entity.type as TdApi.TextEntityTypeCustomEmoji
                    val alt = rawText.substring(start, end).ifBlank { "✨" }
                    appendInlineContent("ce_${ceType.customEmojiId}", alt)
                    cursor = end
                }
                if (cursor < rawText.length) {
                    append(rawText.substring(cursor))
                }
            }

            // Note: indices align 1:1 only when inline replacement lengths match; compute delta per offset
            fun mapOffset(origOffset: Int): Int {
                if (customEmojiEntities.isEmpty()) return origOffset.coerceIn(0, length)
                var delta = 0
                for (ce in customEmojiEntities) {
                    val ceStart = ce.offset
                    val ceEnd = ce.offset + ce.length
                    if (origOffset >= ceEnd) {
                        val altLen = rawText.substring(
                            ceStart.coerceIn(0, rawText.length),
                            ceEnd.coerceIn(0, rawText.length),
                        ).ifBlank { "✨" }.length
                        delta += (altLen - ce.length)
                    }
                }
                return (origOffset + delta).coerceIn(0, length)
            }

            entities.forEach { entity ->
                if (entity.type is TdApi.TextEntityTypeCustomEmoji) return@forEach
                val start = mapOffset(entity.offset)
                val end = mapOffset(entity.offset + entity.length)
                if (start >= end) return@forEach

                when (val t = entity.type) {
                    is TdApi.TextEntityTypeBold -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    is TdApi.TextEntityTypeItalic -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    is TdApi.TextEntityTypeUnderline -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    is TdApi.TextEntityTypeStrikethrough -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                    is TdApi.TextEntityTypeCode,
                    is TdApi.TextEntityTypePre,
                    is TdApi.TextEntityTypePreCode -> addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = tgColors.accent.copy(alpha = 0.14f),
                        ),
                        start,
                        end,
                    )
                    is TdApi.TextEntityTypeSpoiler -> {
                        if (!spoilerRevealed) {
                            addStyle(
                                SpanStyle(
                                    color = Color.Transparent,
                                    background = color.copy(alpha = 0.25f),
                                ),
                                start,
                                end,
                            )
                        }
                    }
                    is TdApi.TextEntityTypeUrl -> {
                        val urlText = rawText.substring(
                            entity.offset.coerceIn(0, rawText.length),
                            (entity.offset + entity.length).coerceIn(0, rawText.length),
                        )
                        addLink(
                            LinkAnnotation.Clickable(
                                tag = "url",
                                styles = TextLinkStyles(
                                    SpanStyle(color = tgColors.accent, textDecoration = TextDecoration.Underline),
                                ),
                            ) { onUrlClick?.invoke(urlText) },
                            start,
                            end,
                        )
                    }
                    is TdApi.TextEntityTypeTextUrl -> {
                        addLink(
                            LinkAnnotation.Clickable(
                                tag = "text_url",
                                styles = TextLinkStyles(
                                    SpanStyle(color = tgColors.accent, textDecoration = TextDecoration.Underline),
                                ),
                            ) { onUrlClick?.invoke(t.url) },
                            start,
                            end,
                        )
                    }
                    is TdApi.TextEntityTypeBotCommand -> {
                        val cmd = rawText.substring(
                            entity.offset.coerceIn(0, rawText.length),
                            (entity.offset + entity.length).coerceIn(0, rawText.length),
                        )
                        addLink(
                            LinkAnnotation.Clickable(
                                tag = "cmd",
                                styles = TextLinkStyles(SpanStyle(color = tgColors.accent, fontWeight = FontWeight.Medium)),
                            ) { onBotCommandClick?.invoke(cmd) },
                            start,
                            end,
                        )
                    }
                    is TdApi.TextEntityTypeMention -> {
                        val mention = rawText.substring(
                            entity.offset.coerceIn(0, rawText.length),
                            (entity.offset + entity.length).coerceIn(0, rawText.length),
                        )
                        addLink(
                            LinkAnnotation.Clickable(
                                tag = "mention",
                                styles = TextLinkStyles(SpanStyle(color = tgColors.accent, fontWeight = FontWeight.Medium)),
                            ) { onMentionClick?.invoke(mention) },
                            start,
                            end,
                        )
                    }
                    is TdApi.TextEntityTypeHashtag,
                    is TdApi.TextEntityTypeEmailAddress,
                    is TdApi.TextEntityTypePhoneNumber -> {
                        addStyle(SpanStyle(color = tgColors.accent), start, end)
                    }
                }
            }
        }
    }

    val inlineContentMap = remember(customEmojiEntities, customEmojiStickers, filesMap, style.fontSize) {
        val iconSizeSp = if (style.fontSize.value > 0f) (style.fontSize.value * 1.28f).sp else 20.sp
        val iconSizeDp = (iconSizeSp.value).dp
        customEmojiEntities.associate { entity ->
            val ce = entity.type as TdApi.TextEntityTypeCustomEmoji
            val alt = rawText.substring(
                entity.offset.coerceIn(0, rawText.length),
                (entity.offset + entity.length).coerceIn(0, rawText.length),
            ).ifBlank { "✨" }
            "ce_${ce.customEmojiId}" to InlineTextContent(
                Placeholder(
                    width = iconSizeSp,
                    height = iconSizeSp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                )
            ) {
                CustomEmojiIcon(
                    customEmojiId = ce.customEmojiId,
                    fallbackEmoji = alt,
                    customEmojiStickers = customEmojiStickers,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    size = iconSizeDp,
                    onClick = onCustomEmojiClick?.let { cb -> { cb(ce.customEmojiId, alt) } },
                )
            }
        }
    }

    val hasSpoiler = remember(entities) { entities.any { it.type is TdApi.TextEntityTypeSpoiler } }

    Text(
        text = annotated,
        inlineContent = inlineContentMap,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = if (hasSpoiler) {
            modifier.clickable { spoilerRevealed = !spoilerRevealed }
        } else {
            modifier
        },
    )
}
