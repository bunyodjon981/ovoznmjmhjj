package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forward
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.LocalStrings
import com.example.tdlib.TelegramClientManager
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.TgBlue
import org.drinkless.tdlib.TdApi
import java.io.File

private val QuickReactions = listOf("👍", "❤️", "🔥", "😂", "🎉", "😢", "🙏")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsSheet(
    message: TdApi.Message,
    canPin: Boolean,
    onDismiss: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalStrings.current
    val isText = message.content is TdApi.MessageText
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                QuickReactions.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onReact(emoji) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ActionRow(Icons.AutoMirrored.Rounded.Reply, strings.reply, onClick = onReply)
            if (isText) {
                ActionRow(Icons.Rounded.ContentCopy, strings.copy, onClick = onCopy)
            }
            if (message.isOutgoing && isText) {
                ActionRow(Icons.Rounded.Edit, strings.edit, onClick = onEdit)
            }
            ActionRow(Icons.Rounded.Forward, strings.forward, onClick = onForward)
            if (canPin) {
                ActionRow(
                    Icons.Rounded.PushPin,
                    if (message.isPinned) strings.unpin else strings.pin,
                    onClick = onPinToggle,
                )
            }
            ActionRow(
                icon = Icons.Rounded.DeleteOutline,
                label = strings.delete,
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onPickFile: () -> Unit,
    onPickMusic: () -> Unit,
    onCreatePoll: () -> Unit,
    onPickLocation: () -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 26.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AttachmentOption(Icons.Rounded.PhotoLibrary, strings.gallery, TgBlue, onPickGallery)
                AttachmentOption(Icons.Rounded.InsertDriveFile, strings.fileLabel, Color(0xFF5B67CA), onPickFile)
                AttachmentOption(Icons.Rounded.MusicNote, strings.musicLabel, Color(0xFFF5A623), onPickMusic)
                AttachmentOption(Icons.Rounded.Poll, strings.pollLabel, Color(0xFF22A5F5), onCreatePoll)
                AttachmentOption(Icons.Rounded.LocationOn, strings.locationLabel, Color(0xFF4CAF50), onPickLocation)
            }
            Text(
                text = "${strings.gallery}: bir nechta rasm tanlab albom sifatida yuborishingiz mumkin",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tgColors.pillBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreate: (question: String, options: List<String>, isAnonymous: Boolean, allowMultiple: Boolean) -> Unit,
) {
    val strings = LocalStrings.current
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var isAnonymous by remember { mutableStateOf(true) }
    var allowMultiple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.createPollTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(strings.pollQuestionHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                options.forEachIndexed { idx, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { options[idx] = it },
                        label = { Text(String.format(strings.pollOptionHint, idx + 1)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (options.size < 6) {
                    TextButton(onClick = { options.add("") }) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.addFolderAction)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { isAnonymous = !isAnonymous },
                ) {
                    Text(strings.anonymousPoll, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { allowMultiple = !allowMultiple },
                ) {
                    Text(strings.multipleAnswers, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = allowMultiple, onCheckedChange = { allowMultiple = it })
                }
            }
        },
        confirmButton = {
            val validOptions = options.filter { it.isNotBlank() }
            TextButton(
                enabled = question.isNotBlank() && validOptions.size >= 2,
                onClick = { onCreate(question, validOptions, isAnonymous, allowMultiple) },
            ) {
                Text(strings.createAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardChatPickerSheet(
    chats: List<TdApi.Chat>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onSelectChat: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = strings.forwardTo,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(chats, key = { it.id }) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectChat(chat.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TelegramAvatar(
                            id = chat.id,
                            title = chat.title,
                            photoFile = chat.photo?.small,
                            filesMap = filesMap,
                            clientManager = clientManager,
                            size = 44.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = chat.title,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteMessageConfirmDialog(
    canDeleteForAll: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (revokeForAll: Boolean) -> Unit,
) {
    val strings = LocalStrings.current
    var deleteForAll by remember { mutableStateOf(canDeleteForAll) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.deleteMessageTitle) },
        text = {
            Column {
                Text(strings.deleteMessageConfirm)
                if (canDeleteForAll) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { deleteForAll = !deleteForAll },
                    ) {
                        Checkbox(checked = deleteForAll, onCheckedChange = { deleteForAll = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.deleteForEveryone)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(deleteForAll) }) {
                Text(strings.delete, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        },
    )
}

@Composable
fun FullscreenPhotoDialog(
    path: String,
    onDismiss: () -> Unit,
) {
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
            AsyncImage(
                model = File(path),
                contentDescription = "Fullscreen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}
