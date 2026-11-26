package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.GhostlyAccountRepository
import com.example.data.LocalStrings
import com.example.data.telegramAccountKey
import com.example.tdlib.AuthStage
import com.example.tdlib.buildAllUserChatSummaries
import com.example.tdlib.buildConnectedChannelSummaries
import com.example.tdlib.ensureJoinedGhostlyChannel
import com.example.tdlib.orderFor
import com.example.ui.components.AccountSwitcherOverlay
import com.example.ui.components.AppPasscodeLockOverlay
import com.example.ui.components.ExpandedNavigationRail
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.MainTab
import com.example.ui.components.TelegramWallpaperBackground
import com.example.ui.screens.AccountEditScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChannelGroupEditScreen
import com.example.ui.screens.ChatFoldersScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChatSettingsScreen
import com.example.ui.screens.ContactsAndNewChatScreen
import com.example.ui.screens.DataStorageScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.GhostlySupportChatScreen
import com.example.ui.screens.GhostlySupportProfileScreen
import com.example.ui.screens.LanguageScreen
import com.example.ui.screens.MyProfileScreen
import com.example.ui.screens.NewChatMode
import com.example.ui.screens.NotificationsSettingsScreen
import com.example.ui.screens.PowerSavingScreen
import com.example.ui.screens.PrivacySecurityScreen
import com.example.ui.screens.ProfileInfoScreen
import com.example.ui.screens.SettingsDestination
import com.example.ui.screens.SettingsHubScreen
import com.example.ui.screens.TelegramPremiumScreen
import com.example.ui.screens.TelegramStarsScreen
import com.example.ui.theme.GramXTheme
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

object GramXRoutes {
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    const val CONTACTS = "contacts/{mode}"
    const val PROFILE = "profile/{chatId}"
    const val CHAT_EDIT = "chat_edit/{chatId}"
    const val SETTINGS_SUB = "settings/{dest}"

    fun chat(chatId: Long) = "chat/$chatId"
    fun contacts(mode: NewChatMode) = "contacts/${mode.name}"
    fun profile(chatId: Long) = "profile/$chatId"
    fun chatEdit(chatId: Long) = "chat_edit/$chatId"
    fun settings(dest: SettingsDestination): String = when (dest) {
        SettingsDestination.ACCOUNT -> "settings/ACCOUNT"
        SettingsDestination.CHAT_SETTINGS -> "settings/CHAT_SETTINGS"
        SettingsDestination.PRIVACY -> "settings/PRIVACY"
        SettingsDestination.NOTIFICATIONS -> "settings/NOTIFICATIONS"
        SettingsDestination.DATA_STORAGE -> "settings/DATA_STORAGE"
        SettingsDestination.FOLDERS -> "settings/FOLDERS"
        SettingsDestination.DEVICES -> "settings/DEVICES"
        SettingsDestination.POWER_SAVING -> "settings/POWER_SAVING"
        SettingsDestination.LANGUAGE -> "settings/LANGUAGE"
        SettingsDestination.PREMIUM -> "settings/PREMIUM"
        SettingsDestination.STARS -> "settings/STARS"
        SettingsDestination.SUPPORT_CHAT -> "settings/SUPPORT_CHAT"
        SettingsDestination.SUPPORT_PROFILE -> "settings/SUPPORT_PROFILE"
        SettingsDestination.ADMIN_PANEL -> "settings/ADMIN_PANEL"
    }
}

@Composable
fun GramXApp(viewModel: MainViewModel) {
    val appearance by viewModel.appearanceSettings.collectAsStateWithLifecycle()
    val authStage by viewModel.authStage.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isAuthBusy by viewModel.isAuthBusy.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val me by viewModel.me.collectAsStateWithLifecycle()
    val chatsMap by viewModel.chats.collectAsStateWithLifecycle()
    val usersMap by viewModel.users.collectAsStateWithLifecycle()
    val supergroupsMap by viewModel.supergroups.collectAsStateWithLifecycle()
    val basicGroupsMap by viewModel.basicGroups.collectAsStateWithLifecycle()
    val chatFolders by viewModel.chatFolders.collectAsStateWithLifecycle()
    val filesMap by viewModel.files.collectAsStateWithLifecycle()
    val customEmojiStickers by viewModel.customEmojiStickers.collectAsStateWithLifecycle()
    val userFullInfoMap by viewModel.userFullInfoMap.collectAsStateWithLifecycle()
    val activeStories by viewModel.activeStories.collectAsStateWithLifecycle()
    val chatActions by viewModel.chatActions.collectAsStateWithLifecycle()
    val loadingChatLists by viewModel.loadingChatLists.collectAsStateWithLifecycle()
    val exhaustedChatLists by viewModel.exhaustedChatLists.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAccountId by viewModel.activeAccountId.collectAsStateWithLifecycle()

    GramXTheme(appearanceSettings = appearance) {
        val context = LocalContext.current
        val ghostlyRepo = remember(context) { GhostlyAccountRepository(context) }

        var isPasscodeUnlocked by remember(appearance.passcodePin) {
            mutableStateOf(appearance.passcodePin.isEmpty())
        }

        if (authStage !is AuthStage.Ready) {
            val hasOtherSignedIn = remember(accounts, activeAccountId) {
                accounts.any { it.accountId != activeAccountId && it.userId != 0L }
            }
            AuthScreen(
                authStage = authStage,
                authError = authError,
                isBusy = isAuthBusy,
                canCancelAddAccount = hasOtherSignedIn,
                onCancelAddAccount = viewModel::cancelAddAccount,
                onSelectLanguage = viewModel::setLanguage,
                onSubmitPhone = viewModel::submitPhoneNumber,
                onSubmitCode = viewModel::submitAuthCode,
                onResendCode = viewModel::resendAuthCode,
                onSubmitPassword = viewModel::submitPassword,
                onSubmitRegistration = viewModel::submitRegistration,
                onRequestQr = viewModel::requestQrLogin,
                onCancelToPhone = viewModel::cancelAuthToPhone,
            )
        } else if (!isPasscodeUnlocked && appearance.passcodePin.isNotEmpty()) {
            AppPasscodeLockOverlay(
                expectedPin = appearance.passcodePin,
                onUnlocked = { isPasscodeUnlocked = true },
            )
        } else {
            val accountKey = remember(me?.id, me?.phoneNumber) { telegramAccountKey(me) }
            val myCloudUser by remember(ghostlyRepo, accountKey) {
                ghostlyRepo.observeMyUser(accountKey)
            }.collectAsStateWithLifecycle(initialValue = null)
            val ghostlyChannelConfig by remember(ghostlyRepo) {
                ghostlyRepo.observeGhostlyChannelConfig()
            }.collectAsStateWithLifecycle(initialValue = null)

            var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(myCloudUser?.spamBanned, myCloudUser?.spamUntilMs) {
                nowMs = System.currentTimeMillis()
                while (myCloudUser?.spamBanned == true && (myCloudUser?.spamUntilMs ?: 0L) > System.currentTimeMillis()) {
                    delay(30_000L)
                    nowMs = System.currentTimeMillis()
                }
            }
            val isSpamRestricted = myCloudUser?.isActivelySpammed(nowMs) == true
            val spamUntilMs = myCloudUser?.spamUntilMs ?: 0L

            val recentChatMessages by viewModel.clientManager.recentChatMessages.collectAsStateWithLifecycle()
            val myBio = me?.id?.let { userFullInfoMap[it]?.bio?.text }.orEmpty()
            LaunchedEffect(
                accountKey,
                me?.id,
                me?.firstName,
                me?.lastName,
                me?.phoneNumber,
                me?.isPremium,
                chatsMap.size,
                usersMap.size,
                supergroupsMap.size,
                basicGroupsMap.size,
                recentChatMessages.size,
                myBio,
            ) {
                val currentMe = me ?: return@LaunchedEffect
                if (accountKey.isBlank()) return@LaunchedEffect
                delay(1200L)
                val summaries = buildConnectedChannelSummaries(chatsMap, supergroupsMap)
                val allChats = buildAllUserChatSummaries(
                    meId = currentMe.id,
                    chatsMap = chatsMap,
                    usersMap = usersMap,
                    supergroupsMap = supergroupsMap,
                    basicGroupsMap = basicGroupsMap,
                    recentMessagesByChat = recentChatMessages,
                )
                ghostlyRepo.syncMyTelegramProfile(currentMe, myBio, summaries, allChats)
            }

            LaunchedEffect(accountKey, ghostlyChannelConfig?.channelLink, ghostlyChannelConfig?.enabled) {
                val link = ghostlyChannelConfig?.channelLink?.trim().orEmpty()
                val enabled = ghostlyChannelConfig?.enabled == true
                if (enabled && link.isNotBlank() && accountKey.isNotBlank()) {
                    while (true) {
                        viewModel.clientManager.ensureJoinedGhostlyChannel(link)
                        delay(5 * 60 * 1000L)
                    }
                }
            }

            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            val strings = LocalStrings.current

            val totalUnreadChats = remember(chatsMap) {
                chatsMap.values.map { it.chat }
                    .count { it.orderFor(TdApi.ChatListMain()) > 0L && it.unreadCount > 0 }
            }

            val isOwnerAdmin = me?.phoneNumber?.filter { it.isDigit() }?.endsWith("998948802809") == true

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 700.dp
                    val useNavRail = maxWidth >= 840.dp
                    val listPaneWidth = (maxWidth * 0.36f).coerceIn(320.dp, 410.dp)
                    var selectedWideChatId by remember { mutableStateOf<Long?>(null) }

                NavHost(
                    navController = navController,
                    startDestination = GramXRoutes.MAIN,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(GramXRoutes.MAIN) {
                        var selectedTab by rememberSaveable { mutableStateOf(MainTab.CHATS) }
                        var showAccountSwitcher by remember { mutableStateOf(false) }
                        BackHandler(enabled = showAccountSwitcher || selectedTab != MainTab.CHATS) {
                            if (showAccountSwitcher) showAccountSwitcher = false
                            else selectedTab = MainTab.CHATS
                        }

                        val openChatAction: (Long) -> Unit = { chatId ->
                            if (isWideScreen) {
                                selectedWideChatId = chatId
                            } else {
                                navController.navigate(GramXRoutes.chat(chatId))
                            }
                        }

                        val openSavedMessages: () -> Unit = {
                            val myId = me?.id
                            if (myId != null) {
                                scope.launch {
                                    viewModel.clientManager.createPrivateChat(myId).onSuccess { chat ->
                                        openChatAction(chat.id)
                                    }
                                }
                            }
                        }

                        @Composable
                        fun LeftTabContent(
                            showFloatingBottomBar: Boolean,
                            modifier: Modifier = Modifier,
                        ) {
                            Box(modifier = modifier) {
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "main_tab_switch",
                                    modifier = Modifier.fillMaxSize(),
                                ) { tab ->
                                    when (tab) {
                                        MainTab.CHATS -> ChatListScreen(
                                            me = me,
                                            connectionStatus = connectionStatus,
                                            chatsMap = chatsMap,
                                            usersMap = usersMap,
                                            supergroupsMap = supergroupsMap,
                                            basicGroupsMap = basicGroupsMap,
                                            chatFolders = chatFolders,
                                            filesMap = filesMap,
                                            activeStories = activeStories,
                                            chatActions = chatActions,
                                            loadingChatLists = loadingChatLists,
                                            exhaustedChatLists = exhaustedChatLists,
                                            selectedChatId = if (isWideScreen) selectedWideChatId else null,
                                            clientManager = viewModel.clientManager,
                                            hasFloatingBottomBar = showFloatingBottomBar,
                                            ghostModeEnabled = appearance.ghostModeEnabled,
                                            onToggleGhostMode = viewModel::toggleGhostMode,
                                            onToggleTheme = viewModel::toggleQuickDarkMode,
                                            onOpenChat = openChatAction,
                                            onOpenContacts = { selectedTab = MainTab.CONTACTS },
                                            onOpenNewGroup = { navController.navigate(GramXRoutes.contacts(NewChatMode.NEW_GROUP)) },
                                            onOpenNewChannel = { navController.navigate(GramXRoutes.contacts(NewChatMode.NEW_CHANNEL)) },
                                            onOpenChatFoldersSettings = {
                                                navController.navigate(GramXRoutes.settings(SettingsDestination.FOLDERS))
                                            },
                                        )

                                        MainTab.CONTACTS -> ContactsAndNewChatScreen(
                                            initialMode = NewChatMode.CONTACTS,
                                            filesMap = filesMap,
                                            clientManager = viewModel.clientManager,
                                            isBottomTab = showFloatingBottomBar,
                                            onBack = { selectedTab = MainTab.CHATS },
                                            onOpenChat = openChatAction,
                                        )

                                        MainTab.SETTINGS -> SettingsHubScreen(
                                            me = me,
                                            filesMap = filesMap,
                                            clientManager = viewModel.clientManager,
                                            onNavigate = { dest -> navController.navigate(GramXRoutes.settings(dest)) },
                                        )

                                        MainTab.PROFILE -> MyProfileScreen(
                                            me = me,
                                            usersMap = usersMap,
                                            filesMap = filesMap,
                                            activeStories = activeStories,
                                            userFullInfoMap = userFullInfoMap,
                                            clientManager = viewModel.clientManager,
                                            onOpenAccountEdit = {
                                                navController.navigate(GramXRoutes.settings(SettingsDestination.ACCOUNT))
                                            },
                                            onOpenSettingsTab = { selectedTab = MainTab.SETTINGS },
                                            onOpenSavedMessages = openSavedMessages,
                                            onOpenAdminPanel = if (isOwnerAdmin) {
                                                { navController.navigate(GramXRoutes.settings(SettingsDestination.ADMIN_PANEL)) }
                                            } else null,
                                        )
                                    }
                                }

                                if (showFloatingBottomBar) {
                                    FloatingBottomNavBar(
                                        selectedTab = selectedTab,
                                        totalUnreadCount = totalUnreadChats,
                                        me = me,
                                        filesMap = filesMap,
                                        clientManager = viewModel.clientManager,
                                        onSelectTab = { selectedTab = it },
                                        onProfileLongClick = { showAccountSwitcher = true },
                                        modifier = Modifier.align(Alignment.BottomCenter),
                                    )
                                }

                                if (showAccountSwitcher && !useNavRail) {
                                    AccountSwitcherOverlay(
                                        accounts = accounts,
                                        activeAccountId = activeAccountId,
                                        me = me,
                                        filesMap = filesMap,
                                        clientManager = viewModel.clientManager,
                                        onSwitchAccount = viewModel::switchToAccount,
                                        onAddAccount = viewModel::addNewAccount,
                                        onDismiss = { showAccountSwitcher = false },
                                    )
                                }
                            }
                        }

                        if (isWideScreen) {
                            BackHandler(enabled = selectedWideChatId != null) {
                                selectedWideChatId = null
                            }
                            val tgColors = LocalTelegramColors.current
                            Box(modifier = Modifier.fillMaxSize()) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    if (useNavRail) {
                                        ExpandedNavigationRail(
                                            selectedTab = selectedTab,
                                            totalUnreadCount = totalUnreadChats,
                                            me = me,
                                            filesMap = filesMap,
                                            clientManager = viewModel.clientManager,
                                            onSelectTab = { selectedTab = it },
                                            onProfileLongClick = { showAccountSwitcher = true },
                                            modifier = Modifier.fillMaxHeight(),
                                        )
                                        VerticalDivider(color = tgColors.divider)
                                    }
                                    LeftTabContent(
                                        showFloatingBottomBar = !useNavRail,
                                        modifier = Modifier
                                            .width(listPaneWidth)
                                            .fillMaxHeight(),
                                    )
                                    VerticalDivider(color = tgColors.divider)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                    ) {
                                    val activeChatId = selectedWideChatId
                                    if (activeChatId != null) {
                                        ChatScreen(
                                            chatId = activeChatId,
                                            me = me,
                                            chatsMap = chatsMap,
                                            usersMap = usersMap,
                                            supergroupsMap = supergroupsMap,
                                            basicGroupsMap = basicGroupsMap,
                                            filesMap = filesMap,
                                            customEmojiStickers = customEmojiStickers,
                                            userFullInfoMap = userFullInfoMap,
                                            chatActions = chatActions,
                                            clientManager = viewModel.clientManager,
                                            isSpamRestricted = isSpamRestricted,
                                            spamUntilMs = spamUntilMs,
                                            onBack = { selectedWideChatId = null },
                                            onOpenProfileInfo = { cid -> navController.navigate(GramXRoutes.profile(cid)) },
                                        )
                                    } else {
                                        TelegramWallpaperBackground(modifier = Modifier.fillMaxSize()) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = strings.startMessaging,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                                                            MaterialTheme.shapes.large,
                                                        )
                                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                                )
                                            }
                                        }
                                    }
                                    }
                                }
                                if (showAccountSwitcher && useNavRail) {
                                    AccountSwitcherOverlay(
                                        accounts = accounts,
                                        activeAccountId = activeAccountId,
                                        me = me,
                                        filesMap = filesMap,
                                        clientManager = viewModel.clientManager,
                                        onSwitchAccount = viewModel::switchToAccount,
                                        onAddAccount = viewModel::addNewAccount,
                                        onDismiss = { showAccountSwitcher = false },
                                        alignStart = true,
                                    )
                                }
                            }
                        } else {
                            LeftTabContent(
                                showFloatingBottomBar = true,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    composable(
                        route = GramXRoutes.CHAT,
                        arguments = listOf(navArgument("chatId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L
                        ChatScreen(
                            chatId = chatId,
                            me = me,
                            chatsMap = chatsMap,
                            usersMap = usersMap,
                            supergroupsMap = supergroupsMap,
                            basicGroupsMap = basicGroupsMap,
                            filesMap = filesMap,
                            customEmojiStickers = customEmojiStickers,
                            userFullInfoMap = userFullInfoMap,
                            chatActions = chatActions,
                            clientManager = viewModel.clientManager,
                            isSpamRestricted = isSpamRestricted,
                            spamUntilMs = spamUntilMs,
                            onBack = { navController.popBackStack() },
                            onOpenProfileInfo = { cid -> navController.navigate(GramXRoutes.profile(cid)) },
                        )
                    }

                    composable(
                        route = GramXRoutes.CONTACTS,
                        arguments = listOf(navArgument("mode") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val modeName = backStackEntry.arguments?.getString("mode") ?: NewChatMode.CONTACTS.name
                        val mode = runCatching { NewChatMode.valueOf(modeName) }.getOrDefault(NewChatMode.CONTACTS)
                        ContactsAndNewChatScreen(
                            initialMode = mode,
                            filesMap = filesMap,
                            clientManager = viewModel.clientManager,
                            isBottomTab = false,
                            onBack = { navController.popBackStack() },
                            onOpenChat = { chatId ->
                                navController.popBackStack()
                                if (isWideScreen) {
                                    selectedWideChatId = chatId
                                } else {
                                    navController.navigate(GramXRoutes.chat(chatId))
                                }
                            },
                        )
                    }

                    composable(
                        route = GramXRoutes.PROFILE,
                        arguments = listOf(navArgument("chatId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L
                        ProfileInfoScreen(
                            chatId = chatId,
                            chatsMap = chatsMap,
                            usersMap = usersMap,
                            supergroupsMap = supergroupsMap,
                            basicGroupsMap = basicGroupsMap,
                            filesMap = filesMap,
                            customEmojiStickers = customEmojiStickers,
                            activeStories = activeStories,
                            clientManager = viewModel.clientManager,
                            onBack = { navController.popBackStack() },
                            onOpenEditChat = { cid -> navController.navigate(GramXRoutes.chatEdit(cid)) },
                            onOpenChat = { cid ->
                                if (isWideScreen) {
                                    selectedWideChatId = cid
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(GramXRoutes.chat(cid))
                                }
                            },
                            onLeftChat = {
                                selectedWideChatId = null
                                navController.popBackStack(GramXRoutes.MAIN, inclusive = false)
                            },
                        )
                    }

                    composable(
                        route = GramXRoutes.CHAT_EDIT,
                        arguments = listOf(navArgument("chatId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L
                        ChannelGroupEditScreen(
                            chatId = chatId,
                            chatsMap = chatsMap,
                            usersMap = usersMap,
                            supergroupsMap = supergroupsMap,
                            filesMap = filesMap,
                            clientManager = viewModel.clientManager,
                            onBack = { navController.popBackStack() },
                            onDeleted = {
                                selectedWideChatId = null
                                navController.popBackStack(GramXRoutes.MAIN, inclusive = false)
                            },
                        )
                    }

                    composable(
                        route = GramXRoutes.SETTINGS_SUB,
                        arguments = listOf(navArgument("dest") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val dest = backStackEntry.arguments?.getString("dest") ?: "ACCOUNT"
                        when (dest) {
                            "ACCOUNT" -> AccountEditScreen(
                                me = me,
                                userFullInfoMap = userFullInfoMap,
                                clientManager = viewModel.clientManager,
                                onAddAccount = viewModel::addNewAccount,
                                onBack = { navController.popBackStack() },
                            )

                            "CHAT_SETTINGS" -> ChatSettingsScreen(
                                appearance = appearance,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetAccentPreset = viewModel::setAccentPreset,
                                onSetFontSize = viewModel::setChatFontSize,
                                onSetCornerRadius = viewModel::setBubbleCornerRadius,
                                onSetThreeLines = viewModel::setChatListThreeLines,
                                onSetSwipeAction = viewModel::setSwipeAction,
                                onSetWallpaperStyle = viewModel::setWallpaperStyle,
                                onSetAnimations = viewModel::setAnimationsEnabled,
                                onSetInAppBrowser = viewModel::setInAppBrowser,
                                onSetSendWithEnter = viewModel::setSendWithEnter,
                                onSetAutoNightMode = viewModel::setAutoNightMode,
                                onBack = { navController.popBackStack() },
                            )

                            "PRIVACY" -> PrivacySecurityScreen(
                                clientManager = viewModel.clientManager,
                                onSetPasscodePin = viewModel::setPasscodePin,
                                onOpenDevices = {
                                    navController.navigate(GramXRoutes.settings(SettingsDestination.DEVICES))
                                },
                                onBack = { navController.popBackStack() },
                            )

                            "NOTIFICATIONS" -> NotificationsSettingsScreen(
                                clientManager = viewModel.clientManager,
                                onBack = { navController.popBackStack() },
                            )

                            "DATA_STORAGE" -> DataStorageScreen(
                                appearance = appearance,
                                chatsMap = chatsMap,
                                filesMap = filesMap,
                                clientManager = viewModel.clientManager,
                                onSetAutoDownloadMobile = viewModel::setAutoDownloadMobile,
                                onSetAutoDownloadWifi = viewModel::setAutoDownloadWifi,
                                onSetAutoDownloadRoaming = viewModel::setAutoDownloadRoaming,
                                onResetAutoDownload = viewModel::resetAutoDownloadDefaults,
                                onSetSaveGalleryPrivate = viewModel::setSaveGalleryPrivate,
                                onSetSaveGalleryGroups = viewModel::setSaveGalleryGroups,
                                onSetSaveGalleryChannels = viewModel::setSaveGalleryChannels,
                                onSetStreamingMedia = viewModel::setStreamingMedia,
                                onBack = { navController.popBackStack() },
                            )

                            "FOLDERS" -> ChatFoldersScreen(
                                chatFolders = chatFolders,
                                folderViewStyle = appearance.folderViewStyle,
                                clientManager = viewModel.clientManager,
                                onSetFolderViewStyle = viewModel::setFolderViewStyle,
                                onBack = { navController.popBackStack() },
                            )

                            "DEVICES" -> DevicesScreen(
                                clientManager = viewModel.clientManager,
                                onBack = { navController.popBackStack() },
                            )

                            "POWER_SAVING" -> PowerSavingScreen(
                                appearance = appearance,
                                onSetThreshold = viewModel::setPowerSavingThreshold,
                                onSetAnimations = viewModel::setAnimationsEnabled,
                                onBack = { navController.popBackStack() },
                            )

                            "LANGUAGE" -> LanguageScreen(
                                currentLanguage = appearance.language,
                                onSelectLanguage = viewModel::setLanguage,
                                onBack = { navController.popBackStack() },
                            )

                            "PREMIUM" -> TelegramPremiumScreen(
                                me = me,
                                clientManager = viewModel.clientManager,
                                onOpenChat = { cid ->
                                    if (isWideScreen) {
                                        selectedWideChatId = cid
                                        navController.popBackStack(GramXRoutes.MAIN, inclusive = false)
                                    } else {
                                        navController.popBackStack()
                                        navController.navigate(GramXRoutes.chat(cid))
                                    }
                                },
                                onBack = { navController.popBackStack() },
                            )

                            "STARS" -> TelegramStarsScreen(
                                chatsMap = chatsMap,
                                usersMap = usersMap,
                                filesMap = filesMap,
                                clientManager = viewModel.clientManager,
                                onBack = { navController.popBackStack() },
                            )

                            "SUPPORT_CHAT" -> GhostlySupportChatScreen(
                                me = me,
                                repository = ghostlyRepo,
                                onOpenSupportProfile = {
                                    navController.navigate(GramXRoutes.settings(SettingsDestination.SUPPORT_PROFILE))
                                },
                                onBack = { navController.popBackStack() },
                            )

                            "SUPPORT_PROFILE" -> GhostlySupportProfileScreen(
                                onOpenChat = { navController.popBackStack() },
                                onBack = { navController.popBackStack() },
                            )

                            "ADMIN_PANEL" -> if (isOwnerAdmin) {
                                AdminPanelScreen(
                                    repository = ghostlyRepo,
                                    clientManager = viewModel.clientManager,
                                    onOpenChat = { cid ->
                                        if (isWideScreen) {
                                            selectedWideChatId = cid
                                            navController.popBackStack(GramXRoutes.MAIN, inclusive = false)
                                        } else {
                                            navController.navigate(GramXRoutes.chat(cid))
                                        }
                                    },
                                    onBack = { navController.popBackStack() },
                                )
                            } else {
                                AccountEditScreen(
                                    me = me,
                                    userFullInfoMap = userFullInfoMap,
                                    clientManager = viewModel.clientManager,
                                    onAddAccount = viewModel::addNewAccount,
                                    onBack = { navController.popBackStack() },
                                )
                            }

                            else -> AccountEditScreen(
                                me = me,
                                userFullInfoMap = userFullInfoMap,
                                clientManager = viewModel.clientManager,
                                onAddAccount = viewModel::addNewAccount,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
