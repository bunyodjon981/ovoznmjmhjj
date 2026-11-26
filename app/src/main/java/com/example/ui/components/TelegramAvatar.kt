package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tdlib.TelegramClientManager
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueDeep
import com.example.ui.theme.avatarGradientForId
import org.drinkless.tdlib.TdApi
import java.io.File

@Composable
fun TelegramAvatar(
    id: Long,
    title: String,
    photoFile: TdApi.File?,
    filesMap: Map<Int, TdApi.File> = emptyMap(),
    clientManager: TelegramClientManager,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    isOnline: Boolean = false,
    isSavedMessages: Boolean = false,
) {
    val tgColors = LocalTelegramColors.current
    val context = LocalContext.current
    val resolvedFile = photoFile?.let { filesMap[it.id] ?: it }
    val localPath = remember(
        resolvedFile?.id,
        resolvedFile?.local?.isDownloadingCompleted,
        resolvedFile?.local?.path,
    ) {
        resolvedFile?.local?.path?.takeIf {
            resolvedFile.local.isDownloadingCompleted && it.isNotBlank() && File(it).exists()
        }
    }

    LaunchedEffect(photoFile?.id) {
        if (photoFile != null && localPath == null) {
            clientManager.downloadFileIfNeeded(photoFile, priority = 14)
        }
    }

    val initials = remember(title) {
        title.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "T" }
    }

    val (topColor, bottomColor) = remember(id) { avatarGradientForId(id) }

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    brush = if (isSavedMessages) {
                        Brush.verticalGradient(listOf(TgBlue, TgBlueDeep))
                    } else {
                        Brush.verticalGradient(listOf(topColor, bottomColor))
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isSavedMessages -> {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.52f),
                    )
                }
                localPath != null -> {
                    val imageRequest = remember(localPath) {
                        ImageRequest.Builder(context)
                            .data(File(localPath))
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.36f).sp,
                    )
                }
            }
        }

        if (isOnline && !isSavedMessages) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .paddingDot(2.dp, tgColors.onlineDot),
            )
        }
    }
}

private fun Modifier.paddingDot(inset: Dp, color: Color): Modifier {
    return this.border(inset, Color.Transparent, CircleShape).background(color, CircleShape)
}
