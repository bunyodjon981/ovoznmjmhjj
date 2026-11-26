package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatSwipeAction
import com.example.data.LocalStrings
import com.example.data.UserAppearanceSettings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.primaryUsername
import com.example.tdlib.updateBirthdate
import com.example.tdlib.updateUsername
import com.example.ui.components.TelegramWallpaperBackground
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    me: TdApi.User?,
    userFullInfoMap: Map<Long, TdApi.UserFullInfo>,
    clientManager: TelegramClientManager,
    onAddAccount: () -> Unit = {},
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(me?.id) {
        me?.id?.let { clientManager.getUserFullInfo(it) }
    }
    val fullInfo = me?.id?.let { userFullInfoMap[it] }

    var firstName by remember(me?.firstName) { mutableStateOf(me?.firstName.orEmpty()) }
    var lastName by remember(me?.lastName) { mutableStateOf(me?.lastName.orEmpty()) }
    var bio by remember(fullInfo?.bio?.text) { mutableStateOf(fullInfo?.bio?.text.orEmpty()) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showBirthdateDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                title = { Text(strings.accountTitle, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (firstName.isNotBlank()) clientManager.setMyName(firstName.trim(), lastName.trim())
                                clientManager.setMyBio(bio.trim())
                                snackbarHostState.showSnackbar(strings.savedToast)
                            }
                        },
                        modifier = Modifier.testTag("save_account_button"),
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = strings.saveAction, tint = tgColors.accent)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Ismingiz Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.yourNameSection, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(strings.firstName) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(strings.lastName) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Tarjimayi hol Card (70-char counter)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.bioHint, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                        Text("${70 - bio.length}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 70) bio = it },
                        placeholder = { Text(strings.bioHint) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(strings.bioHelper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Axborotlaringiz Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 8.dp),
                ) {
                    Text(
                        text = strings.yourInfoSection,
                        style = MaterialTheme.typography.titleSmall,
                        color = tgColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    AccountInfoRow(
                        icon = Icons.Rounded.Call,
                        iconBg = Color(0xFF4CAF50),
                        title = me?.phoneNumber?.takeIf { it.isNotBlank() }?.let { "+$it" } ?: "—",
                        subtitle = strings.mobileLabel,
                        onClick = {},
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 66.dp))
                    AccountInfoRow(
                        icon = Icons.Rounded.AlternateEmail,
                        iconBg = Color(0xFFF5A623),
                        title = me?.primaryUsername()?.let { "@$it" } ?: strings.tapToChangeUsername,
                        subtitle = strings.usernameLabel,
                        onClick = { showUsernameDialog = true },
                    )
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 66.dp))
                    val bd = fullInfo?.birthdate
                    val bdLabel = if (bd != null && bd.day > 0 && bd.month > 0) {
                        if (bd.year > 0) "${bd.day}.${bd.month}.${bd.year}" else "${bd.day}.${bd.month}"
                    } else strings.tapToSetBirthdate
                    AccountInfoRow(
                        icon = Icons.Rounded.Cake,
                        iconBg = Color(0xFF2AA1F2),
                        title = bdLabel,
                        subtitle = strings.birthdateLabel,
                        onClick = { showBirthdateDialog = true },
                    )
                }
            }

            // Hisob qo'shish Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .clickable { onAddAccount() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.PersonAdd, null, tint = tgColors.accent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(strings.addNewAccount, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                }
            }

            // Log out Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .clickable { showLogoutConfirm = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .testTag("logout_button"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(strings.logOut, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showUsernameDialog) {
        var usernameInput by remember { mutableStateOf(me?.primaryUsername().orEmpty()) }
        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
            title = { Text(strings.usernameLabel) },
            text = {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
                    prefix = { Text("@") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUsernameDialog = false
                        scope.launch {
                            clientManager.updateUsername(usernameInput)
                                .onSuccess { snackbarHostState.showSnackbar(strings.savedToast) }
                                .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                        }
                    },
                ) { Text(strings.saveAction) }
            },
            dismissButton = { TextButton(onClick = { showUsernameDialog = false }) { Text(strings.cancel) } },
        )
    }

    if (showBirthdateDialog) {
        val current = fullInfo?.birthdate
        var day by remember { mutableStateOf((current?.day?.takeIf { it > 0 } ?: 1).toString()) }
        var month by remember { mutableStateOf((current?.month?.takeIf { it > 0 } ?: 1).toString()) }
        var year by remember { mutableStateOf((current?.year?.takeIf { it > 0 } ?: 2000).toString()) }
        AlertDialog(
            onDismissRequest = { showBirthdateDialog = false },
            title = { Text(strings.birthdateLabel) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = day, onValueChange = { day = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("Kun") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = month, onValueChange = { month = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("Oy") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = year, onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) }, label = { Text("Yil") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.3f))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val d = (day.toIntOrNull() ?: 1).coerceIn(1, 31)
                        val m = (month.toIntOrNull() ?: 1).coerceIn(1, 12)
                        val y = (year.toIntOrNull() ?: 0).coerceIn(0, 2025)
                        showBirthdateDialog = false
                        scope.launch {
                            clientManager.updateBirthdate(d, m, y)
                                .onSuccess {
                                    me?.id?.let { clientManager.getUserFullInfo(it) }
                                    snackbarHostState.showSnackbar(strings.savedToast)
                                }
                                .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error") }
                        }
                    },
                ) { Text(strings.saveAction) }
            },
            dismissButton = { TextButton(onClick = { showBirthdateDialog = false }) { Text(strings.cancel) } },
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
private fun AccountInfoRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsScreen(
    appearance: UserAppearanceSettings,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetAccentPreset: (AccentPreset) -> Unit,
    onSetFontSize: (Int) -> Unit,
    onSetCornerRadius: (Int) -> Unit,
    onSetThreeLines: (Boolean) -> Unit,
    onSetSwipeAction: (ChatSwipeAction) -> Unit,
    onSetWallpaperStyle: (Int) -> Unit,
    onSetAnimations: (Boolean) -> Unit,
    onSetInAppBrowser: (Boolean) -> Unit,
    onSetSendWithEnter: (Boolean) -> Unit,
    onSetAutoNightMode: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    var showWallpaperPicker by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tgColors.cardBackground,
                    titleContentColor = tgColors.headerContent,
                    navigationIconContentColor = tgColors.headerContent,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                title = { Text(strings.chatSettingsTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Font size slider + Live animated Message Preview Card (matches screenshot!)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.messageTextSize, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = appearance.chatFontSizeSp.toFloat(),
                                onValueChange = { onSetFontSize(it.toInt()) },
                                valueRange = 12f..24f,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${appearance.chatFontSizeSp}", color = tgColors.accent, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Live Chat Wallpaper Preview
                    TelegramWallpaperBackground(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val corner = appearance.bubbleCornerRadiusDp.dp
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(corner))
                                    .background(tgColors.incomingBubble)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = "Xayrli tong! Soat nechchi boʻlganini bilasizmi?",
                                    fontSize = appearance.chatFontSizeSp.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = corner, bottomEnd = 4.dp))
                                        .background(tgColors.outgoingBubble)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Text(
                                        text = "Tokioda tong otdi 😎  20:53",
                                        fontSize = appearance.chatFontSizeSp.sp,
                                        color = if (tgColors.isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Rounded.DoneAll, null, tint = tgColors.outgoingTime, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showWallpaperPicker = true }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Wallpaper, null, tint = tgColors.accent)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(strings.changeChatWallpaper, color = tgColors.accent, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Color Theme + Day/Night Switch Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.colorThemeSection, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AccentPreset.entries.forEach { preset ->
                            val selected = appearance.accentPreset == preset
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(92.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tgColors.pillBackground)
                                    .border(if (selected) 2.dp else 0.dp, if (selected) preset.primary else Color.Transparent, RoundedCornerShape(14.dp))
                                    .clickable { onSetAccentPreset(preset) }
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Box(modifier = Modifier.align(Alignment.End).width(42.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).background(preset.primary))
                                Box(modifier = Modifier.align(Alignment.Start).width(46.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).background(tgColors.incomingBubble))
                                Text(preset.label.substringBefore(" "), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val next = if (tgColors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                                onSetThemeMode(next)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(if (tgColors.isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null, tint = tgColors.accent)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(if (tgColors.isDark) strings.switchToDayMode else strings.switchToNightMode, color = tgColors.accent, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Message Corners Slider
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.messageCorners, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = appearance.bubbleCornerRadiusDp.toFloat(),
                            onValueChange = { onSetCornerRadius(it.toInt()) },
                            valueRange = 4f..20f,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${appearance.bubbleCornerRadiusDp}", color = tgColors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat List View (2-line vs 3-line)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.chatListView, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(false to strings.twoLineView, true to strings.threeLineView).forEach { (isThree, label) ->
                            val selected = appearance.chatListThreeLines == isThree
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) tgColors.accent.copy(alpha = 0.16f) else tgColors.pillBackground)
                                    .clickable { onSetThreeLines(isThree) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected, onClick = { onSetThreeLines(isThree) })
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Swipe Gesture Picker
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(16.dp),
                ) {
                    Text(strings.swipeGestureTitle, style = MaterialTheme.typography.titleSmall, color = tgColors.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ChatSwipeAction.ARCHIVE to strings.swipeArchive,
                            ChatSwipeAction.PIN to strings.swipePin,
                            ChatSwipeAction.UNREAD to strings.swipeFolder,
                            ChatSwipeAction.DELETE to strings.swipeDelete,
                        ).forEach { (action, label) ->
                            FilterChip(
                                selected = appearance.swipeAction == action,
                                onClick = { onSetSwipeAction(action) },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }

            // Toggles Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(tgColors.cardBackground).padding(vertical = 6.dp),
                ) {
                    SettingsSwitchRow(Icons.Rounded.DarkMode, strings.autoNightMode, null, appearance.autoNightMode, onSetAutoNightMode)
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    SettingsSwitchRow(Icons.Rounded.Public, strings.inAppBrowser, strings.inAppBrowserDesc, appearance.inAppBrowser, onSetInAppBrowser)
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    SettingsSwitchRow(Icons.Rounded.AutoAwesome, strings.animationsLabel, null, appearance.animationsEnabled, onSetAnimations)
                    HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 56.dp))
                    SettingsSwitchRow(Icons.Rounded.Palette, strings.sendWithEnter, null, appearance.sendWithEnter, onSetSendWithEnter)
                }
            }
        }
    }

    if (showWallpaperPicker) {
        val names = listOf("Telegram Classic", "Midnight Violet", "Emerald Forest", "Sunset Dusk", "Minimal Slate")
        AlertDialog(
            onDismissRequest = { showWallpaperPicker = false },
            title = { Text(strings.changeChatWallpaper) },
            text = {
                Column {
                    names.forEachIndexed { index, title ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onSetWallpaperStyle(index)
                                showWallpaperPicker = false
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = appearance.wallpaperStyle == index, onClick = {
                                onSetWallpaperStyle(index)
                                showWallpaperPicker = false
                            })
                            Text(title)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showWallpaperPicker = false }) { Text(strings.webAppClose) } },
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
