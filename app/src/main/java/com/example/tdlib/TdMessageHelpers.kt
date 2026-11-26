package com.example.tdlib

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.AppStrings
import org.drinkless.tdlib.TdApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun TdApi.Chat.orderFor(chatList: TdApi.ChatList): Long {
    val targetCtor = chatList.constructor
    for (pos in positions) {
        if (pos.list.constructor == targetCtor) {
            if (chatList is TdApi.ChatListFolder && pos.list is TdApi.ChatListFolder) {
                if ((pos.list as TdApi.ChatListFolder).chatFolderId == chatList.chatFolderId) {
                    return pos.order
                }
            } else {
                return pos.order
            }
        }
    }
    return 0L
}

fun TdApi.Chat.isPinnedIn(chatList: TdApi.ChatList): Boolean {
    val targetCtor = chatList.constructor
    for (pos in positions) {
        if (pos.list.constructor == targetCtor) {
            if (chatList is TdApi.ChatListFolder && pos.list is TdApi.ChatListFolder) {
                if ((pos.list as TdApi.ChatListFolder).chatFolderId == chatList.chatFolderId) {
                    return pos.isPinned
                }
            } else {
                return pos.isPinned
            }
        }
    }
    return false
}

fun TdApi.Chat.isMuted(): Boolean {
    return notificationSettings.muteFor > 0
}

fun TdApi.Chat.isArchived(): Boolean {
    return orderFor(TdApi.ChatListArchive()) > 0L
}

fun TdApi.User?.displayName(): String {
    if (this == null) return "Telegram"
    return listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank {
        usernames?.editableUsername?.let { "@$it" } ?: "Deleted Account"
    }
}

fun TdApi.User?.primaryUsername(): String? {
    return this?.usernames?.activeUsernames?.firstOrNull() ?: this?.usernames?.editableUsername?.takeIf { it.isNotBlank() }
}

fun TdApi.Supergroup?.primaryUsername(): String? {
    return this?.usernames?.activeUsernames?.firstOrNull() ?: this?.usernames?.editableUsername?.takeIf { it.isNotBlank() }
}

fun TdApi.UserStatus?.formatStatus(
    strings: AppStrings? = null,
    nowSeconds: Long = System.currentTimeMillis() / 1000L,
): String {
    return when (this) {
        is TdApi.UserStatusOnline -> strings?.online ?: "online"
        is TdApi.UserStatusRecently -> strings?.lastSeenRecently ?: "last seen recently"
        is TdApi.UserStatusLastWeek -> strings?.lastSeenWithinWeek ?: "last seen within a week"
        is TdApi.UserStatusLastMonth -> strings?.lastSeenWithinMonth ?: "last seen within a month"
        is TdApi.UserStatusOffline -> {
            val diff = (nowSeconds - wasOnline).coerceAtLeast(0)
            when {
                diff < 60 -> strings?.lastSeenJustNow ?: "last seen just now"
                diff < 3600 -> {
                    val m = diff / 60
                    if (strings != null) String.format(strings.lastSeenAt, "${m}m")
                    else "last seen ${m}m ago"
                }
                diff < 86400 -> {
                    val t = formatTime(wasOnline)
                    if (strings != null) String.format(strings.lastSeenAt, t) else "last seen at $t"
                }
                else -> {
                    val d = formatChatDate(wasOnline)
                    if (strings != null) String.format(strings.lastSeenAt, d) else "last seen $d"
                }
            }
        }
        else -> strings?.offline ?: "offline"
    }
}

fun chatListKey(chatList: TdApi.ChatList): String {
    return when (chatList) {
        is TdApi.ChatListMain -> "main"
        is TdApi.ChatListArchive -> "archive"
        is TdApi.ChatListFolder -> "folder_${chatList.chatFolderId}"
        else -> "other_${chatList.constructor}"
    }
}

fun TdApi.MessageContent?.miniThumbnailBytes(): ByteArray? {
    return when (this) {
        is TdApi.MessagePhoto -> photo.minithumbnail?.data
        is TdApi.MessageVideo -> video.minithumbnail?.data
        else -> null
    }
}

fun TdApi.MessageContent?.previewText(strings: AppStrings? = null): String {
    return when (this) {
        is TdApi.MessageText -> text.text.replace('\n', ' ')
        is TdApi.MessagePhoto -> {
            val cap = caption.text.replace('\n', ' ').trim()
            val label = strings?.previewPhoto ?: "Rasm"
            if (cap.isNotEmpty()) "📷 $cap" else "📷 $label"
        }
        is TdApi.MessageVideo -> {
            val cap = caption.text.replace('\n', ' ').trim()
            val label = strings?.previewVideo ?: "Video"
            if (cap.isNotEmpty()) "🎬 $cap" else "🎬 $label"
        }
        is TdApi.MessageVoiceNote -> "🎤 ${strings?.previewVoice ?: "Ovozli xabar"} (${formatDuration(voiceNote.duration)})"
        is TdApi.MessageAudio -> "🎵 ${audio.performer.ifBlank { "Audio" }} - ${audio.title.ifBlank { audio.fileName }}"
        is TdApi.MessageDocument -> {
            val name = document.fileName.ifBlank { strings?.fileLabel ?: "Fayl" }
            val cap = caption.text.replace('\n', ' ').trim()
            if (cap.isNotEmpty()) "📄 $name · $cap" else "📄 $name"
        }
        is TdApi.MessageSticker -> "${sticker.emoji} ${strings?.previewSticker ?: "Stiker"}"
        is TdApi.MessageAnimation -> "GIF"
        is TdApi.MessageVideoNote -> "📹 ${strings?.previewVideoNote ?: "Videoxabar"}"
        is TdApi.MessageLocation -> "📍 ${strings?.previewLocation ?: "Joylashuv"}"
        is TdApi.MessageContact -> "👤 ${strings?.previewContact ?: "Kontakt"}: ${contact.firstName}"
        is TdApi.MessagePoll -> "📊 ${poll.question.text}"
        is TdApi.MessageCall -> if (isVideo) "📹 ${strings?.previewVideoCall ?: "Videoqoʻngʻiroq"}" else "📞 ${strings?.previewVoiceCall ?: "Ovozli qoʻngʻiroq"}"
        is TdApi.MessageChatAddMembers -> "guruhga qoʻshildi"
        is TdApi.MessageChatJoinByLink -> "havola orqali qoʻshildi"
        is TdApi.MessageChatDeleteMember -> "guruhdan chiqdi"
        is TdApi.MessagePinMessage -> "xabarni qadadi"
        is TdApi.MessageChatChangeTitle -> "sarlavhani oʻzgartirdi: \"$title\""
        is TdApi.MessageChatChangePhoto -> "chat rasmini yangiladi"
        is TdApi.MessageChatDeletePhoto -> "chat rasmini oʻchirdi"
        is TdApi.MessageBasicGroupChatCreate -> "\"$title\" guruhini yaratdi"
        is TdApi.MessageSupergroupChatCreate -> "\"$title\" yaratildi"
        is TdApi.MessageUnsupported -> "Qoʻllab-quvvatlanmaydigan xabar"
        null -> ""
        else -> strings?.previewMessage ?: "Xabar"
    }
}

fun TdApi.MessageContent.isServiceMessage(): Boolean {
    return this is TdApi.MessageChatAddMembers ||
        this is TdApi.MessageChatJoinByLink ||
        this is TdApi.MessageChatDeleteMember ||
        this is TdApi.MessagePinMessage ||
        this is TdApi.MessageChatChangeTitle ||
        this is TdApi.MessageChatChangePhoto ||
        this is TdApi.MessageChatDeletePhoto ||
        this is TdApi.MessageBasicGroupChatCreate ||
        this is TdApi.MessageSupergroupChatCreate
}

private val timeFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("HH:mm", Locale.getDefault()) }
private val weekdayFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("EEE", Locale.getDefault()) }
private val monthDayFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("MMM d", Locale.getDefault()) }
private val oldDateFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("dd.MM.yy", Locale.getDefault()) }
private val sepMonthDayFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("MMMM d", Locale.getDefault()) }
private val sepFullFormatLocal = ThreadLocal.withInitial { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

fun formatTime(epochSeconds: Int): String {
    if (epochSeconds <= 0) return ""
    return timeFormatLocal.get()?.format(Date(epochSeconds.toLong() * 1000L)).orEmpty()
}

fun formatChatDate(epochSeconds: Int): String {
    if (epochSeconds <= 0) return ""
    val msgCal = Calendar.getInstance().apply { timeInMillis = epochSeconds.toLong() * 1000L }
    val nowCal = Calendar.getInstance()
    return when {
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) -> {
            timeFormatLocal.get()?.format(msgCal.time).orEmpty()
        }
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) <= 6 -> {
            weekdayFormatLocal.get()?.format(msgCal.time).orEmpty()
        }
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) -> {
            monthDayFormatLocal.get()?.format(msgCal.time).orEmpty()
        }
        else -> oldDateFormatLocal.get()?.format(msgCal.time).orEmpty()
    }
}

fun formatDateSeparator(epochSeconds: Int, strings: AppStrings? = null): String {
    val msgCal = Calendar.getInstance().apply { timeInMillis = epochSeconds.toLong() * 1000L }
    val nowCal = Calendar.getInstance()
    return when {
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) -> strings?.today ?: "Bugun"
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1 -> strings?.yesterday ?: "Kecha"
        msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) -> {
            sepMonthDayFormatLocal.get()?.format(msgCal.time).orEmpty()
        }
        else -> sepFullFormatLocal.get()?.format(msgCal.time).orEmpty()
    }
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

fun copyUriToCacheFile(context: Context, uri: Uri, fallbackName: String): File? {
    return runCatching {
        var displayName = fallbackName
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    cursor.getString(idx)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                }
            }
        }
        val safeName = "${System.currentTimeMillis()}_${displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")}"
        val dest = File(context.cacheDir, safeName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        dest
    }.getOrNull()
}
