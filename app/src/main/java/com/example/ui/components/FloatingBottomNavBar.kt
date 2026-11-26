package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalStrings
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import org.drinkless.tdlib.TdApi

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import com.example.data.SavedAccount

enum class MainTab {
    CHATS,
    CONTACTS,
    SETTINGS,
    PROFILE,
}

@Composable
fun FloatingBottomNavBar(
    selectedTab: MainTab,
    totalUnreadCount: Int,
    me: TdApi.User?,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onSelectTab: (MainTab) -> Unit,
    onProfileLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val animationsOn = LocalAppearanceSettings.current.animationsEnabled

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
                .clip(RoundedCornerShape(32.dp))
                .background(tgColors.floatingNavBackground.copy(alpha = 0.96f))
                .border(
                    width = 0.8.dp,
                    color = if (tgColors.isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(32.dp),
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavPillItem(
                selected = selectedTab == MainTab.CHATS,
                label = strings.tabChats,
                badgeCount = totalUnreadCount,
                animationsOn = animationsOn,
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_tab_chats"),
                onClick = { onSelectTab(MainTab.CHATS) },
            ) { tint ->
                Icon(
                    imageVector = if (selectedTab == MainTab.CHATS) Icons.Rounded.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = strings.tabChats,
                    tint = tint,
                    modifier = Modifier.size(23.dp),
                )
            }

            NavPillItem(
                selected = selectedTab == MainTab.CONTACTS,
                label = strings.tabContacts,
                badgeCount = 0,
                animationsOn = animationsOn,
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_tab_contacts"),
                onClick = { onSelectTab(MainTab.CONTACTS) },
            ) { tint ->
                Icon(
                    imageVector = if (selectedTab == MainTab.CONTACTS) Icons.Rounded.Person else Icons.Outlined.PersonOutline,
                    contentDescription = strings.tabContacts,
                    tint = tint,
                    modifier = Modifier.size(23.dp),
                )
            }

            NavPillItem(
                selected = selectedTab == MainTab.SETTINGS,
                label = strings.tabSettings,
                badgeCount = 0,
                animationsOn = animationsOn,
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_tab_settings"),
                onClick = { onSelectTab(MainTab.SETTINGS) },
            ) { tint ->
                Icon(
                    imageVector = if (selectedTab == MainTab.SETTINGS) Icons.Rounded.Settings else Icons.Outlined.Settings,
                    contentDescription = strings.tabSettings,
                    tint = tint,
                    modifier = Modifier.size(23.dp),
                )
            }

            NavPillItem(
                selected = selectedTab == MainTab.PROFILE,
                label = strings.tabProfile,
                badgeCount = 0,
                animationsOn = animationsOn,
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_tab_profile"),
                onClick = { onSelectTab(MainTab.PROFILE) },
                onLongClick = onProfileLongClick,
            ) { tint ->
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selectedTab == MainTab.PROFILE) 1.6.dp else 0.dp,
                            color = if (selectedTab == MainTab.PROFILE) tint else Color.Transparent,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    TelegramAvatar(
                        id = me?.id ?: 0L,
                        title = me.displayName(),
                        photoFile = me?.profilePhoto?.small,
                        filesMap = filesMap,
                        clientManager = clientManager,
                        size = 24.dp,
                        isOnline = false,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandedNavigationRail(
    selectedTab: MainTab,
    totalUnreadCount: Int,
    me: TdApi.User?,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onSelectTab: (MainTab) -> Unit,
    onProfileLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    Column(
        modifier = modifier
            .width(80.dp)
            .background(tgColors.headerBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        NavPillItem(
            selected = selectedTab == MainTab.CHATS,
            label = strings.tabChats,
            badgeCount = totalUnreadCount,
            animationsOn = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rail_tab_chats"),
            onClick = { onSelectTab(MainTab.CHATS) },
        ) { tint ->
            Icon(
                imageVector = if (selectedTab == MainTab.CHATS) Icons.Rounded.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                contentDescription = strings.tabChats,
                tint = tint,
                modifier = Modifier.size(23.dp),
            )
        }

        NavPillItem(
            selected = selectedTab == MainTab.CONTACTS,
            label = strings.tabContacts,
            badgeCount = 0,
            animationsOn = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rail_tab_contacts"),
            onClick = { onSelectTab(MainTab.CONTACTS) },
        ) { tint ->
            Icon(
                imageVector = if (selectedTab == MainTab.CONTACTS) Icons.Rounded.Person else Icons.Outlined.PersonOutline,
                contentDescription = strings.tabContacts,
                tint = tint,
                modifier = Modifier.size(23.dp),
            )
        }

        NavPillItem(
            selected = selectedTab == MainTab.SETTINGS,
            label = strings.tabSettings,
            badgeCount = 0,
            animationsOn = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rail_tab_settings"),
            onClick = { onSelectTab(MainTab.SETTINGS) },
        ) { tint ->
            Icon(
                imageVector = if (selectedTab == MainTab.SETTINGS) Icons.Rounded.Settings else Icons.Outlined.Settings,
                contentDescription = strings.tabSettings,
                tint = tint,
                modifier = Modifier.size(23.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavPillItem(
            selected = selectedTab == MainTab.PROFILE,
            label = strings.tabProfile,
            badgeCount = 0,
            animationsOn = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rail_tab_profile"),
            onClick = { onSelectTab(MainTab.PROFILE) },
            onLongClick = onProfileLongClick,
        ) { tint ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (selectedTab == MainTab.PROFILE) 1.6.dp else 0.dp,
                        color = if (selectedTab == MainTab.PROFILE) tint else Color.Transparent,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                TelegramAvatar(
                    id = me?.id ?: 0L,
                    title = me.displayName(),
                    photoFile = me?.profilePhoto?.small,
                    filesMap = filesMap,
                    clientManager = clientManager,
                    size = 26.dp,
                    isOnline = false,
                )
            }
        }
    }
}

@Composable
fun AccountSwitcherOverlay(
    accounts: List<SavedAccount>,
    activeAccountId: String,
    me: TdApi.User?,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onDismiss: () -> Unit,
    alignStart: Boolean = false,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1621).copy(alpha = 0.82f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .navigationBarsPadding(),
    ) {
        Surface(
            color = Color(0xFF1D2733),
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 18.dp,
            modifier = Modifier
                .align(if (alignStart) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (alignStart) 16.dp else 0.dp,
                    end = if (alignStart) 0.dp else 16.dp,
                    bottom = 84.dp,
                )
                .width(250.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onAddAccount()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("add_new_account_button"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircleOutline,
                        contentDescription = strings.addNewAccount,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = strings.addNewAccount,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                accounts.forEach { acc ->
                    val isActive = acc.accountId == activeAccountId
                    val title = if (isActive && me != null) {
                        me.displayName()
                    } else {
                        acc.displayName.ifBlank { acc.phone.ifBlank { "Telegram" } }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                if (!isActive) onSwitchAccount(acc.accountId)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isActive) 2.dp else 0.dp,
                                    color = if (isActive) tgColors.accent else Color.Transparent,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            TelegramAvatar(
                                id = if (acc.userId != 0L) acc.userId else acc.accountId.hashCode().toLong(),
                                title = title,
                                photoFile = if (isActive) me?.profilePhoto?.small else null,
                                filesMap = filesMap,
                                clientManager = clientManager,
                                size = 34.dp,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Active",
                                tint = tgColors.accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavPillItem(
    selected: Boolean,
    label: String,
    badgeCount: Int,
    animationsOn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    iconContent: @Composable (Color) -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val pillBg by animateColorAsState(
        targetValue = if (selected) tgColors.accent.copy(alpha = 0.16f) else Color.Transparent,
        label = "nav_pill_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_content_color",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = if (animationsOn) spring(stiffness = Spring.StiffnessMediumLow) else spring(stiffness = 10000f),
        label = "nav_scale",
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .background(pillBg)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            iconContent(contentColor)
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 12.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tgColors.accent)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badgeCount > 999) "999+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AppPasscodeLockOverlay(
    expectedPin: String,
    onUnlocked: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    var entered by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(tgColors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = tgColors.accent, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = strings.enterPasscodeTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { idx ->
                    val filled = idx < entered.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> MaterialTheme.colorScheme.error
                                    filled -> tgColors.accent
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                1.dp,
                                if (isError) MaterialTheme.colorScheme.error else tgColors.divider,
                                CircleShape,
                            ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(34.dp))

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL"),
            )
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    row.forEach { key ->
                        when (key) {
                            "" -> Spacer(modifier = Modifier.size(72.dp))
                            "DEL" -> Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        isError = false
                                        if (entered.isNotEmpty()) entered = entered.dropLast(1)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                            }
                            else -> Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(tgColors.cardBackground)
                                    .clickable {
                                        isError = false
                                        if (entered.length < 4) {
                                            val next = entered + key
                                            entered = next
                                            if (next.length == 4) {
                                                if (next == expectedPin) {
                                                    onUnlocked()
                                                } else {
                                                    isError = true
                                                    entered = ""
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
