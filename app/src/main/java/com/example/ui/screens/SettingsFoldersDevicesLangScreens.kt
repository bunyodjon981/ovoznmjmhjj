package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.FolderViewStyle
import com.example.data.LocalStrings
import com.example.data.UserAppearanceSettings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.createFolder
import com.example.tdlib.createSimpleFolder
import com.example.tdlib.deleteFolder
import com.example.tdlib.formatChatDate
import com.example.tdlib.formatFileSize
import com.example.tdlib.getRecommendedFolders
import com.example.tdlib.terminateAllOtherSessions
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFoldersScreen(
    chatFolders: List<TdApi.ChatFolderInfo>,
    folderViewStyle: FolderViewStyle,
    clientManager: TelegramClientManager,
    onSetFolderViewStyle: (FolderViewStyle) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var recommended by remember { mutableStateOf<List<TdApi.RecommendedChatFolder>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        clientManager.getRecommendedFolders().onSuccess { recommended = it }
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                title = { Text(strings.chatFoldersTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFD9AE76)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Folder, null, tint = Color(0xFF5D3A1A), modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.foldersHeroDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }

            // Recommended Folders Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.recommendedFolders,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    if (recommended.isNotEmpty()) {
                        recommended.forEach { rec ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rec.folder.name.text.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(rec.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            clientManager.createFolder(rec.folder)
                                                .onSuccess {
                                                    recommended = recommended - rec
                                                    snackbarHostState.showSnackbar(strings.savedToast)
                                                }
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                ) { Text(strings.addFolderAction, color = Color.White) }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.unreadFolderName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(strings.unreadFolderDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        clientManager.createSimpleFolder(strings.unreadFolderName, includeGroups = true, includeChannels = true, excludeRead = true)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            ) { Text(strings.addFolderAction, color = Color.White) }
                        }
                    }
                }
            }

            // Active Chat Folders Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.chatFoldersTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.DragHandle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(strings.allChats, style = MaterialTheme.typography.bodyLarge)
                    }
                    chatFolders.forEach { folder ->
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 52.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.DragHandle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(folder.name.text.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { scope.launch { clientManager.deleteFolder(folder.id) } },
                                modifier = Modifier.size(34.dp),
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, strings.delete, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 52.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showCreateDialog = true }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.AddCircleOutline, null, tint = tgColors.accent)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(strings.createNewFolder, color = tgColors.accent, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Folder Icon vs Name Display Style Cards (matches screenshot!)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.folderDisplayStyle, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(FolderViewStyle.ICON to strings.showIconStyle, FolderViewStyle.NAME to strings.showNameStyle).forEach { (style, label) ->
                            val selected = folderViewStyle == style
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) tgColors.accent.copy(alpha = 0.16f) else tgColors.pillBackground)
                                    .clickable { onSetFolderViewStyle(style) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = { onSetFolderViewStyle(style) })
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(strings.createNewFolder) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.folderNameHint) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            showCreateDialog = false
                            scope.launch {
                                clientManager.createSimpleFolder(name.trim(), includeGroups = true, includeChannels = true, includeBots = true)
                                    .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                            }
                        }
                    },
                ) { Text(strings.createAction) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text(strings.cancel) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    clientManager: TelegramClientManager,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var sessions by remember { mutableStateOf<List<TdApi.Session>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        clientManager.getActiveSessions().onSuccess { sessions = it }
        isLoading = false
    }

    val currentSession = sessions.firstOrNull { it.isCurrent }
    val otherSessions = sessions.filterNot { it.isCurrent }

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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                title = { Text(strings.devicesTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(tgColors.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.LaptopMac, null, tint = tgColors.accent, modifier = Modifier.size(38.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(strings.devicesHeroDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }

            if (currentSession != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = strings.thisDeviceSection,
                            style = MaterialTheme.typography.titleSmall,
                            color = tgColors.accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        SessionRow(session = currentSession, onTerminate = null)
                        if (otherSessions.isNotEmpty()) {
                            HorizontalDivider(color = tgColors.divider)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            clientManager.terminateAllOtherSessions().onSuccess {
                                                sessions = listOf(currentSession)
                                                snackbarHostState.showSnackbar(strings.allOtherSessionsTerminated)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.PanTool, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(strings.terminateAllOtherSessions, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (otherSessions.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = strings.activeSessionsSection,
                            style = MaterialTheme.typography.titleSmall,
                            color = tgColors.accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        otherSessions.forEachIndexed { idx, s ->
                            SessionRow(
                                session = s,
                                onTerminate = {
                                    scope.launch {
                                        clientManager.terminateSession(s.id).onSuccess {
                                            sessions = sessions.filterNot { it.id == s.id }
                                            snackbarHostState.showSnackbar(strings.sessionTerminated)
                                        }
                                    }
                                },
                            )
                            if (idx < otherSessions.lastIndex) HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 66.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: TdApi.Session,
    onTerminate: (() -> Unit)?,
) {
    val tgColors = LocalTelegramColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (session.isCurrent) tgColors.accent else tgColors.pillBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.PhoneAndroid, null, tint = if (session.isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${session.applicationName} ${session.applicationVersion}", fontWeight = FontWeight.Medium)
            Text("${session.deviceModel} · ${session.platform}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${session.location} · ${formatChatDate(session.lastActiveDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onTerminate != null) {
            IconButton(onClick = onTerminate) {
                Icon(Icons.Rounded.Close, contentDescription = "Terminate", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerSavingScreen(
    appearance: UserAppearanceSettings,
    onSetThreshold: (Int) -> Unit,
    onSetAnimations: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.cardBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                title = { Text(strings.powerSavingTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.BatteryChargingFull, null, tint = Color(0xFFFA8C28), modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(strings.powerSavingHero, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.batteryThreshold, fontWeight = FontWeight.Medium)
                    Text("${appearance.powerSavingThreshold}%", color = tgColors.accent, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = appearance.powerSavingThreshold.toFloat(),
                    onValueChange = { onSetThreshold(it.toInt()) },
                    valueRange = 5f..80f,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tgColors.cardBackground)
                    .clickable { onSetAnimations(!appearance.animationsEnabled) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings.smoothAnimations, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = appearance.animationsEnabled, onCheckedChange = onSetAnimations)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.cardBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                title = { Text(strings.languageTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground),
            ) {
                AppLanguage.entries.forEachIndexed { idx, lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(lang) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("lang_option_${lang.code}"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = currentLanguage == lang, onClick = { onSelectLanguage(lang) })
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(lang.nativeName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(lang.englishName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (idx < AppLanguage.entries.lastIndex) {
                        HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 58.dp))
                    }
                }
            }
        }
    }
}
