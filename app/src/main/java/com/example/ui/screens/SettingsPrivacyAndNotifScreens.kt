package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.LocalStrings
import com.example.tdlib.PrivacyOption
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.getAccountTtlDays
import com.example.tdlib.getBlockedUsersTotalCount
import com.example.tdlib.getPrivacyOption
import com.example.tdlib.getScopeNotification
import com.example.tdlib.setAccountTtlDays
import com.example.tdlib.setPrivacyOption
import com.example.tdlib.updateScopeNotification
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

private data class PrivacyRowSpec(
    val key: String,
    val label: String,
    val setting: TdApi.UserPrivacySetting,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    clientManager: TelegramClientManager,
    onSetPasscodePin: (String) -> Unit,
    onOpenDevices: () -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val appearance = LocalAppearanceSettings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val specs = remember(strings) {
        listOf(
            PrivacyRowSpec("phone", strings.phonePrivacy, TdApi.UserPrivacySettingShowPhoneNumber()),
            PrivacyRowSpec("last_seen", strings.lastSeenPrivacy, TdApi.UserPrivacySettingShowStatus()),
            PrivacyRowSpec("photos", strings.photoPrivacy, TdApi.UserPrivacySettingShowProfilePhoto()),
            PrivacyRowSpec("forwards", strings.forwardPrivacy, TdApi.UserPrivacySettingShowLinkInForwardedMessages()),
            PrivacyRowSpec("calls", strings.callsPrivacy, TdApi.UserPrivacySettingAllowCalls()),
            PrivacyRowSpec("bio", strings.bioPrivacy, TdApi.UserPrivacySettingShowBio()),
        )
    }

    var blockedCount by remember { mutableIntStateOf(0) }
    var ttlDays by remember { mutableIntStateOf(180) }
    var privacyValues by remember { mutableStateOf<Map<String, PrivacyOption>>(emptyMap()) }
    var activeSpec by remember { mutableStateOf<PrivacyRowSpec?>(null) }
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showTtlDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        clientManager.getBlockedUsersTotalCount().onSuccess { blockedCount = it }
        clientManager.getAccountTtlDays().onSuccess { ttlDays = it }
        val loaded = mutableMapOf<String, PrivacyOption>()
        specs.forEach { spec ->
            clientManager.getPrivacyOption(spec.setting).onSuccess { loaded[spec.key] = it }
        }
        privacyValues = loaded
    }

    fun optionLabel(opt: PrivacyOption?): String = when (opt) {
        PrivacyOption.EVERYBODY -> strings.everybody
        PrivacyOption.CONTACTS -> strings.myContacts
        PrivacyOption.NOBODY -> strings.nobody
        null -> "…"
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
                title = { Text(strings.privacyTitle, fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.securitySection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    PrivacyValueRow(
                        icon = Icons.Rounded.Lock,
                        label = strings.passcodeLock,
                        value = if (appearance.passcodePin.isNotEmpty()) strings.passcodeEnabled else strings.passcodeDisabled,
                        onClick = { showPasscodeDialog = true },
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    PrivacyValueRow(
                        icon = Icons.Rounded.Block,
                        label = strings.blockedUsers,
                        value = blockedCount.toString(),
                        onClick = {},
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    PrivacyValueRow(
                        icon = Icons.Rounded.Devices,
                        label = strings.devicesTitle,
                        value = "→",
                        onClick = onOpenDevices,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.privacySection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    specs.forEachIndexed { idx, spec ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { activeSpec = spec }.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(spec.label, style = MaterialTheme.typography.bodyLarge)
                            Text(optionLabel(privacyValues[spec.key]), style = MaterialTheme.typography.bodyMedium, color = tgColors.accent)
                        }
                        if (idx < specs.lastIndex) HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showTtlDialog = true }.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(strings.deleteAccountIfAway, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(String.format(strings.monthsFormat, (ttlDays / 30).coerceAtLeast(1)), color = tgColors.accent, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    activeSpec?.let { spec ->
        val current = privacyValues[spec.key] ?: PrivacyOption.EVERYBODY
        AlertDialog(
            onDismissRequest = { activeSpec = null },
            title = { Text(spec.label) },
            text = {
                Column {
                    PrivacyOption.entries.forEach { opt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                activeSpec = null
                                scope.launch {
                                    clientManager.setPrivacyOption(spec.setting, opt).onSuccess {
                                        privacyValues = privacyValues + (spec.key to opt)
                                        snackbarHostState.showSnackbar(strings.savedToast)
                                    }
                                }
                            }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = current == opt, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(optionLabel(opt))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { activeSpec = null }) { Text(strings.cancel) } },
        )
    }

    if (showPasscodeDialog) {
        var pinInput by remember { mutableStateOf(appearance.passcodePin) }
        AlertDialog(
            onDismissRequest = { showPasscodeDialog = false },
            title = { Text(strings.passcodeLock) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text(strings.setPasscodeHint) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetPasscodePin(pinInput)
                        showPasscodeDialog = false
                        scope.launch { snackbarHostState.showSnackbar(strings.savedToast) }
                    },
                ) { Text(strings.saveAction) }
            },
            dismissButton = { TextButton(onClick = { showPasscodeDialog = false }) { Text(strings.cancel) } },
        )
    }

    if (showTtlDialog) {
        val options = listOf(30, 90, 180, 365, 548)
        AlertDialog(
            onDismissRequest = { showTtlDialog = false },
            title = { Text(strings.deleteAccountIfAway) },
            text = {
                Column {
                    options.forEach { days ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                showTtlDialog = false
                                scope.launch {
                                    clientManager.setAccountTtlDays(days).onSuccess {
                                        ttlDays = days
                                        snackbarHostState.showSnackbar(strings.savedToast)
                                    }
                                }
                            }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = ttlDays == days, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(String.format(strings.monthsFormat, (days / 30).coerceAtLeast(1)))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTtlDialog = false }) { Text(strings.cancel) } },
        )
    }
}

@Composable
private fun PrivacyValueRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = tgColors.accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    clientManager: TelegramClientManager,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    var privateScope by remember { mutableStateOf<TdApi.ScopeNotificationSettings?>(null) }
    var groupScope by remember { mutableStateOf<TdApi.ScopeNotificationSettings?>(null) }
    var channelScope by remember { mutableStateOf<TdApi.ScopeNotificationSettings?>(null) }

    LaunchedEffect(Unit) {
        clientManager.getScopeNotification(TdApi.NotificationSettingsScopePrivateChats()).onSuccess { privateScope = it }
        clientManager.getScopeNotification(TdApi.NotificationSettingsScopeGroupChats()).onSuccess { groupScope = it }
        clientManager.getScopeNotification(TdApi.NotificationSettingsScopeChannelChats()).onSuccess { channelScope = it }
    }

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
                title = { Text(strings.notificationsTitle, fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    NotificationScopeCardRow(
                        icon = Icons.Rounded.Person,
                        title = strings.privateChats,
                        settings = privateScope,
                        previewLabel = strings.messagePreview,
                        onUpdate = { mute, preview ->
                            val cur = privateScope ?: return@NotificationScopeCardRow
                            scope.launch {
                                clientManager.updateScopeNotification(TdApi.NotificationSettingsScopePrivateChats(), cur, mute, preview).onSuccess {
                                    clientManager.getScopeNotification(TdApi.NotificationSettingsScopePrivateChats()).onSuccess { privateScope = it }
                                }
                            }
                        },
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationScopeCardRow(
                        icon = Icons.Rounded.Group,
                        title = strings.groupsLabel,
                        settings = groupScope,
                        previewLabel = strings.messagePreview,
                        onUpdate = { mute, preview ->
                            val cur = groupScope ?: return@NotificationScopeCardRow
                            scope.launch {
                                clientManager.updateScopeNotification(TdApi.NotificationSettingsScopeGroupChats(), cur, mute, preview).onSuccess {
                                    clientManager.getScopeNotification(TdApi.NotificationSettingsScopeGroupChats()).onSuccess { groupScope = it }
                                }
                            }
                        },
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationScopeCardRow(
                        icon = Icons.Rounded.Campaign,
                        title = strings.channelsLabel,
                        settings = channelScope,
                        previewLabel = strings.messagePreview,
                        onUpdate = { mute, preview ->
                            val cur = channelScope ?: return@NotificationScopeCardRow
                            scope.launch {
                                clientManager.updateScopeNotification(TdApi.NotificationSettingsScopeChannelChats(), cur, mute, preview).onSuccess {
                                    clientManager.getScopeNotification(TdApi.NotificationSettingsScopeChannelChats()).onSuccess { channelScope = it }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationScopeCardRow(
    icon: ImageVector,
    title: String,
    settings: TdApi.ScopeNotificationSettings?,
    previewLabel: String,
    onUpdate: (mute: Boolean, showPreview: Boolean) -> Unit,
) {
    val enabled = settings?.let { it.muteFor == 0 } ?: true
    val preview = settings?.showPreview ?: true
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { onUpdate(!it, preview) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 38.dp)) {
            Text(previewLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Switch(checked = preview, onCheckedChange = { onUpdate(!enabled, it) })
        }
    }
}
