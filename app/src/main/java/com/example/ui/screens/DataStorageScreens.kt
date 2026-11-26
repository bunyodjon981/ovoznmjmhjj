package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.data.UserAppearanceSettings
import com.example.tdlib.ChatEntry
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.addSocks5Proxy
import com.example.tdlib.clearAllDrafts
import com.example.tdlib.disableProxy
import com.example.tdlib.enableProxy
import com.example.tdlib.formatFileSize
import com.example.tdlib.getNetworkStatistics
import com.example.tdlib.getProxies
import com.example.tdlib.getStorageStatistics
import com.example.tdlib.optimizeStorageCache
import com.example.tdlib.removeProxy
import com.example.tdlib.resetNetworkStatistics
import com.example.ui.components.TelegramAvatar
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

private enum class DataStorageSubView {
    MAIN,
    STORAGE_USAGE,
    NETWORK_USAGE,
}

private data class DonutSlice(
    val label: String,
    val bytes: Long,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(
    appearance: UserAppearanceSettings,
    chatsMap: Map<Long, ChatEntry>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onSetAutoDownloadMobile: (Boolean) -> Unit,
    onSetAutoDownloadWifi: (Boolean) -> Unit,
    onSetAutoDownloadRoaming: (Boolean) -> Unit,
    onResetAutoDownload: () -> Unit,
    onSetSaveGalleryPrivate: (Boolean) -> Unit,
    onSetSaveGalleryGroups: (Boolean) -> Unit,
    onSetSaveGalleryChannels: (Boolean) -> Unit,
    onSetStreamingMedia: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var subView by remember { mutableStateOf(DataStorageSubView.MAIN) }
    var storageStats by remember { mutableStateOf<TdApi.StorageStatistics?>(null) }
    var networkStats by remember { mutableStateOf<TdApi.NetworkStatistics?>(null) }
    var cacheBytes by remember { mutableLongStateOf(clientManager.getCacheSizeBytes()) }
    var proxies by remember { mutableStateOf<List<TdApi.Proxy>>(emptyList()) }
    var showProxyDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = subView != DataStorageSubView.MAIN) {
        subView = DataStorageSubView.MAIN
    }

    LaunchedEffect(Unit) {
        clientManager.getStorageStatistics(25).onSuccess { storageStats = it }
        clientManager.getNetworkStatistics(false).onSuccess { networkStats = it }
        clientManager.getProxies().onSuccess { proxies = it }
    }

    val totalStorageBytes = storageStats?.size?.coerceAtLeast(cacheBytes) ?: cacheBytes
    val totalNetworkBytes = remember(networkStats) {
        networkStats?.entries?.sumOf { entry ->
            when (entry) {
                is TdApi.NetworkStatisticsEntryFile -> entry.sentBytes + entry.receivedBytes
                is TdApi.NetworkStatisticsEntryCall -> entry.sentBytes + entry.receivedBytes
                else -> 0L
            }
        } ?: 0L
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
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (subView != DataStorageSubView.MAIN) subView = DataStorageSubView.MAIN else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                title = {
                    Text(
                        text = when (subView) {
                            DataStorageSubView.MAIN -> strings.dataStorageTitle
                            DataStorageSubView.STORAGE_USAGE -> strings.storageUsageTitle
                            DataStorageSubView.NETWORK_USAGE -> strings.networkUsageTitle
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { padding ->
        when (subView) {
            DataStorageSubView.MAIN -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Storage and Network card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(vertical = 8.dp),
                        ) {
                            SectionHeader(strings.storageAndNetworkSection)
                            DataNavRow(
                                icon = Icons.Rounded.PieChart,
                                label = strings.storageUsageTitle,
                                value = formatFileSize(totalStorageBytes),
                                onClick = { subView = DataStorageSubView.STORAGE_USAGE },
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            DataNavRow(
                                icon = Icons.Rounded.DataUsage,
                                label = strings.networkUsageTitle,
                                value = formatFileSize(totalNetworkBytes),
                                onClick = { subView = DataStorageSubView.NETWORK_USAGE },
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            DataNavRow(
                                icon = Icons.Rounded.Folder,
                                label = strings.saveLocationTitle,
                                value = strings.internalStorage,
                                onClick = {},
                            )
                        }
                    }

                    // Auto media download card (all OFF by default!)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(vertical = 8.dp),
                        ) {
                            SectionHeader(strings.autoMediaDownloadSection)
                            AutoDownloadToggleRow(
                                icon = Icons.Rounded.SignalCellularAlt,
                                label = strings.whenUsingMobile,
                                subtitle = if (appearance.autoDownloadMobile) strings.enabledLabel else strings.notEnabled,
                                checked = appearance.autoDownloadMobile,
                                onCheckedChange = onSetAutoDownloadMobile,
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            AutoDownloadToggleRow(
                                icon = Icons.Rounded.Wifi,
                                label = strings.whenUsingWifi,
                                subtitle = if (appearance.autoDownloadWifi) strings.enabledLabel else strings.notEnabled,
                                checked = appearance.autoDownloadWifi,
                                onCheckedChange = onSetAutoDownloadWifi,
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            AutoDownloadToggleRow(
                                icon = Icons.Rounded.Public,
                                label = strings.whenUsingRoaming,
                                subtitle = if (appearance.autoDownloadRoaming) strings.enabledLabel else strings.notEnabled,
                                checked = appearance.autoDownloadRoaming,
                                onCheckedChange = onSetAutoDownloadRoaming,
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            Text(
                                text = strings.resetAutoDownload,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onResetAutoDownload()
                                        scope.launch { snackbarHostState.showSnackbar(strings.savedToast) }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            )
                        }
                    }

                    // Save to gallery card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(vertical = 8.dp),
                        ) {
                            SectionHeader(strings.saveToGallerySection)
                            GalleryToggleRow(strings.privateChats, appearance.saveGalleryPrivate, strings.offLabel, onSetSaveGalleryPrivate)
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 16.dp))
                            GalleryToggleRow(strings.groupsLabel, appearance.saveGalleryGroups, strings.offLabel, onSetSaveGalleryGroups)
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 16.dp))
                            GalleryToggleRow(strings.channelsLabel, appearance.saveGalleryChannels, strings.offLabel, onSetSaveGalleryChannels)
                        }
                    }

                    // Streaming
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(vertical = 8.dp),
                        ) {
                            SectionHeader(strings.streamingSection)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSetStreamingMedia(!appearance.streamingMedia) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(strings.streamVideoAudio, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Switch(checked = appearance.streamingMedia, onCheckedChange = onSetStreamingMedia)
                            }
                            Text(
                                text = strings.streamVideoAudioDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }

                    // Proxy & Drafts
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(vertical = 8.dp),
                        ) {
                            SectionHeader(strings.proxySection)
                            val activeProxy = proxies.firstOrNull { it.isEnabled }
                            DataNavRow(
                                icon = Icons.Rounded.Public,
                                label = strings.proxySettings,
                                value = activeProxy?.server ?: strings.offLabel,
                                onClick = { showProxyDialog = true },
                            )
                            HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 54.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            clientManager.clearAllDrafts()
                                            snackbarHostState.showSnackbar(strings.savedToast)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(strings.clearDrafts, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            DataStorageSubView.STORAGE_USAGE -> {
                StorageUsageDonutView(
                    stats = storageStats,
                    fallbackCacheBytes = cacheBytes,
                    chatsMap = chatsMap,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    onClearCache = {
                        scope.launch {
                            clientManager.optimizeStorageCache()
                            clientManager.clearLocalMediaCache()
                            cacheBytes = clientManager.getCacheSizeBytes()
                            clientManager.getStorageStatistics(25).onSuccess { storageStats = it }
                            snackbarHostState.showSnackbar(strings.cacheCleared)
                        }
                    },
                    modifier = Modifier.padding(padding),
                )
            }

            DataStorageSubView.NETWORK_USAGE -> {
                NetworkUsageDonutView(
                    stats = networkStats,
                    onResetStats = {
                        scope.launch {
                            clientManager.resetNetworkStatistics()
                            clientManager.getNetworkStatistics(false).onSuccess { networkStats = it }
                            snackbarHostState.showSnackbar(strings.savedToast)
                        }
                    },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showProxyDialog) {
        ProxySettingsDialog(
            proxies = proxies,
            clientManager = clientManager,
            onRefresh = { scope.launch { clientManager.getProxies().onSuccess { proxies = it } } },
            onDismiss = { showProxyDialog = false },
        )
    }
}

@Composable
private fun StorageUsageDonutView(
    stats: TdApi.StorageStatistics?,
    fallbackCacheBytes: Long,
    chatsMap: Map<Long, ChatEntry>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val slices = remember(stats, fallbackCacheBytes) {
        var stickers = 0L
        var videos = 0L
        var photos = 0L
        var docs = 0L
        var profile = 0L
        var other = 0L
        stats?.byChat?.forEach { chatStat ->
            chatStat.byFileType.forEach { ftStat ->
                when (ftStat.fileType) {
                    is TdApi.FileTypeSticker -> stickers += ftStat.size
                    is TdApi.FileTypeVideo, is TdApi.FileTypeVideoNote, is TdApi.FileTypeAnimation -> videos += ftStat.size
                    is TdApi.FileTypePhoto -> photos += ftStat.size
                    is TdApi.FileTypeDocument -> docs += ftStat.size
                    is TdApi.FileTypeProfilePhoto -> profile += ftStat.size
                    else -> other += ftStat.size
                }
            }
        }
        val totalFromTypes = stickers + videos + photos + docs + profile + other
        if (totalFromTypes == 0L && fallbackCacheBytes > 0L) {
            other = fallbackCacheBytes
        }
        listOf(
            DonutSlice(strings.stickersAndEmojis, stickers, Color(0xFF42A5F5)),
            DonutSlice(strings.videosLabel, videos, Color(0xFF7E57C2)),
            DonutSlice(strings.photosLabel, photos, Color(0xFF66BB6A)),
            DonutSlice(strings.documentsLabel, docs, Color(0xFFFFA726)),
            DonutSlice(strings.profilePhotosLabel, profile, Color(0xFF26C6DA)),
            DonutSlice(strings.otherLabel, other, Color(0xFFEC407A)),
        ).filter { it.bytes > 0L }.ifEmpty {
            listOf(DonutSlice(strings.otherLabel, 0L, tgColors.accent))
        }
    }

    val totalBytes = (stats?.size ?: 0L).coerceAtLeast(slices.sumOf { it.bytes })

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DonutChartRing(
                    slices = slices,
                    centerText = formatFileSize(totalBytes),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.storageCloudNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tgColors.cardBackground)
                    .padding(vertical = 8.dp),
            ) {
                slices.forEachIndexed { idx, s ->
                    val pct = if (totalBytes > 0L) (s.bytes * 100f / totalBytes) else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(s.color),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(s.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            text = if (pct >= 1f) "${pct.toInt()}% · ${formatFileSize(s.bytes)}" else formatFileSize(s.bytes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (idx < slices.lastIndex) HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 48.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClearCache,
                    colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(String.format(strings.clearCacheBtn, formatFileSize(totalBytes)), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            val tabs = listOf(strings.chatsTab, strings.mediaTab, strings.filesTab, strings.musicTab)
            TabRow(selectedTabIndex = selectedTab, containerColor = tgColors.cardBackground) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1, fontSize = 12.sp) },
                    )
                }
            }
        }

        val chatBreakdown = stats?.byChat?.sortedByDescending { it.size }?.take(15).orEmpty()
        items(chatBreakdown, key = { it.chatId }) { cStat ->
            val chat = chatsMap[cStat.chatId]?.chat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tgColors.cardBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TelegramAvatar(
                    id = cStat.chatId,
                    title = chat?.title ?: "Chat ${cStat.chatId}",
                    photoFile = chat?.photo?.small,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    size = 40.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(chat?.title ?: "Chat", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(formatFileSize(cStat.size), style = MaterialTheme.typography.bodyMedium, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NetworkUsageDonutView(
    stats: TdApi.NetworkStatistics?,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val filtered = remember(stats, selectedTab) {
        stats?.entries?.filter { entry ->
            val netType = when (entry) {
                is TdApi.NetworkStatisticsEntryFile -> entry.networkType
                is TdApi.NetworkStatisticsEntryCall -> entry.networkType
                else -> null
            }
            when (selectedTab) {
                1 -> netType is TdApi.NetworkTypeMobile
                2 -> netType is TdApi.NetworkTypeWiFi
                3 -> netType is TdApi.NetworkTypeMobileRoaming
                else -> true
            }
        }.orEmpty()
    }

    var videos = 0L
    var messages = 0L
    var docs = 0L
    var photos = 0L
    var music = 0L
    var voice = 0L
    var sentTotal = 0L
    var receivedTotal = 0L

    filtered.forEach { entry ->
        when (entry) {
            is TdApi.NetworkStatisticsEntryFile -> {
                val sum = entry.sentBytes + entry.receivedBytes
                sentTotal += entry.sentBytes
                receivedTotal += entry.receivedBytes
                when (entry.fileType) {
                    is TdApi.FileTypeVideo, is TdApi.FileTypeVideoNote, is TdApi.FileTypeAnimation -> videos += sum
                    is TdApi.FileTypeDocument -> docs += sum
                    is TdApi.FileTypePhoto, is TdApi.FileTypeProfilePhoto -> photos += sum
                    is TdApi.FileTypeAudio -> music += sum
                    is TdApi.FileTypeVoiceNote -> voice += sum
                    else -> messages += sum
                }
            }
            is TdApi.NetworkStatisticsEntryCall -> {
                sentTotal += entry.sentBytes
                receivedTotal += entry.receivedBytes
                voice += entry.sentBytes + entry.receivedBytes
            }
        }
    }

    val total = sentTotal + receivedTotal
    val slices = listOf(
        DonutSlice(strings.videosLabel, videos, Color(0xFF42A5F5)),
        DonutSlice(strings.messagesLabel, messages, Color(0xFF7E57C2)),
        DonutSlice(strings.documentsLabel, docs, Color(0xFFFFA726)),
        DonutSlice(strings.photosLabel, photos, Color(0xFF66BB6A)),
        DonutSlice(strings.musicTab, music, Color(0xFF26C6DA)),
        DonutSlice(strings.voiceMessagesLabel, voice, Color(0xFFEC407A)),
    ).filter { it.bytes > 0L }.ifEmpty {
        listOf(DonutSlice(strings.messagesLabel, 0L, tgColors.accent))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            val tabs = listOf(strings.allTab, strings.mobileTab, strings.wifiTab, strings.roamingTab)
            TabRow(selectedTabIndex = selectedTab, containerColor = tgColors.cardBackground) {
                tabs.forEachIndexed { idx, t ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(t, maxLines = 1, fontSize = 12.sp) })
                }
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChartRing(slices = slices, centerText = formatFileSize(total))
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
            ) {
                slices.forEachIndexed { idx, s ->
                    val pct = if (total > 0L) (s.bytes * 100f / total) else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(s.color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(s.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            text = if (pct >= 1f) "${pct.toInt()}% · ${formatFileSize(s.bytes)}" else formatFileSize(s.bytes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (idx < slices.lastIndex) HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 40.dp))
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(strings.totalNetworkUsage, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.sentData, style = MaterialTheme.typography.bodyMedium)
                    Text(formatFileSize(sentTotal), fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.receivedData, style = MaterialTheme.typography.bodyMedium)
                    Text(formatFileSize(receivedTotal), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Text(
                text = strings.resetStatistics,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(tgColors.cardBackground)
                    .clickable(onClick = onResetStats)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun DonutChartRing(
    slices: List<DonutSlice>,
    centerText: String,
) {
    val total = slices.sumOf { it.bytes }.coerceAtLeast(1L)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = Modifier.size(168.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
            val sumNonZero = slices.sumOf { it.bytes }
            if (sumNonZero <= 0L) {
                drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            } else {
                var startAngle = -90f
                slices.forEach { s ->
                    if (s.bytes > 0L) {
                        val sweep = (s.bytes.toFloat() / total.toFloat()) * 360f
                        drawArc(color = s.color, startAngle = startAngle, sweepAngle = sweep.coerceAtLeast(2f), useCenter = false, style = stroke)
                        startAngle += sweep
                    }
                }
            }
        }
        Text(
            text = centerText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProxySettingsDialog(
    proxies: List<TdApi.Proxy>,
    clientManager: TelegramClientManager,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("1080") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.proxySettings) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                proxies.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${p.server}:${p.port}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = p.isEnabled,
                            onCheckedChange = { enable ->
                                scope.launch {
                                    if (enable) clientManager.enableProxy(p.id) else clientManager.disableProxy()
                                    onRefresh()
                                }
                            },
                        )
                        IconButton(onClick = {
                            scope.launch {
                                clientManager.removeProxy(p.id)
                                onRefresh()
                            }
                        }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                OutlinedTextField(value = server, onValueChange = { server = it }, label = { Text("SOCKS5 Server") }, singleLine = true)
                OutlinedTextField(value = port, onValueChange = { port = it.filter { ch -> ch.isDigit() } }, label = { Text("Port") }, singleLine = true)
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username (ixtiyoriy)") }, singleLine = true)
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password (ixtiyoriy)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = port.toIntOrNull() ?: 1080
                    if (server.isNotBlank()) {
                        scope.launch {
                            clientManager.addSocks5Proxy(server, p, user, pass)
                            onRefresh()
                            server = ""
                        }
                    }
                },
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.addFolderAction)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}

@Composable
private fun SectionHeader(title: String) {
    val tgColors = LocalTelegramColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = tgColors.accent,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun DataNavRow(
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
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = tgColors.accent, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AutoDownloadToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GalleryToggleRow(
    label: String,
    checked: Boolean,
    offText: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (!checked) {
                Text(offText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
