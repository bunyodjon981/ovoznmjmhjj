package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GhostlyAccountRepository
import com.example.data.LocalStrings
import com.example.data.SupportMessage
import com.example.data.telegramAccountKey
import com.example.tdlib.ChatEntry
import com.example.tdlib.TelegramClientManager
import com.example.tdlib.displayName
import com.example.tdlib.getPremiumState
import com.example.tdlib.getStarTransactions
import com.example.tdlib.openPublicChatByUsername
import com.example.ui.components.TelegramAvatar
import com.example.ui.components.TelegramWallpaperBackground
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GoldStarStart = Color(0xFFFFD54F)
private val GoldStarEnd = Color(0xFFF59E0B)
private val PremiumGradStart = Color(0xFF6C63FF)
private val PremiumGradEnd = Color(0xFF2AA1F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramPremiumScreen(
    me: TdApi.User?,
    clientManager: TelegramClientManager,
    onOpenChat: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var premiumState by remember { mutableStateOf<TdApi.PremiumState?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        clientManager.getPremiumState().onSuccess { premiumState = it }
        isLoading = false
    }

    val pulse = rememberInfiniteTransition(label = "premium_star")
    val starScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale",
    )

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = { Text(strings.telegramPremiumTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(tgColors.cardBackground)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .graphicsLayer { scaleX = starScale; scaleY = starScale }
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PremiumGradStart, PremiumGradEnd))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.telegramPremiumTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = premiumState?.state?.text?.takeIf { it.isNotBlank() }
                            ?: if (me?.isPremium == true) "Telegram Premium faol" else "Telegram Premium imkoniyatlari: katta fayllar, tezkor yuklash, premium emojilar va stikerlar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (isLoading) {
                item { CircularProgressIndicator(color = tgColors.accent, modifier = Modifier.padding(16.dp)) }
            } else {
                val options = premiumState?.paymentOptions?.toList().orEmpty()
                items(options) { opt ->
                    val po = opt.paymentOption
                    val formattedAmount = String.format(Locale.US, "%.2f %s", po.amount / 100.0, po.currency)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(tgColors.cardBackground)
                            .border(
                                width = if (opt.isCurrent) 1.5.dp else 0.dp,
                                color = if (opt.isCurrent) tgColors.accent else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                scope.launch {
                                    clientManager.openPublicChatByUsername("PremiumBot")
                                        .onSuccess { onOpenChat(it.id) }
                                        .onFailure { snackbarHostState.showSnackbar(it.message ?: "@PremiumBot") }
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${po.monthCount} oy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (po.discountPercentage > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "-${po.discountPercentage}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(tgColors.accent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            if (opt.isCurrent) {
                                Text("Faol obuna", color = tgColors.accent, fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = formattedAmount,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = tgColors.accent,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        scope.launch {
                            clientManager.openPublicChatByUsername("PremiumBot")
                                .onSuccess { onOpenChat(it.id) }
                                .onFailure { snackbarHostState.showSnackbar(it.message ?: "@PremiumBot") }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(PremiumGradStart, PremiumGradEnd))),
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (me?.isPremium == true) "@PremiumBot" else "${strings.telegramPremiumTitle} (@PremiumBot)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramStarsScreen(
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0=All, 1=Incoming, 2=Outgoing
    var starBalance by remember { mutableLongStateOf(0L) }
    var transactions by remember { mutableStateOf<List<TdApi.StarTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedTab) {
        isLoading = true
        val dir: TdApi.TransactionDirection? = when (selectedTab) {
            1 -> TdApi.TransactionDirectionIncoming()
            2 -> TdApi.TransactionDirectionOutgoing()
            else -> null
        }
        clientManager.getStarTransactions(direction = dir, limit = 50).onSuccess { res ->
            starBalance = res.starAmount.starCount
            transactions = res.transactions.toList()
        }
        isLoading = false
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = { Text(strings.telegramStarsTitle, fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Balance Hero Card (matches Screenshots 6–8)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GoldStarStart, GoldStarEnd))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Star, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = starBalance.toString(),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.yourBalance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // "Yulduzlar ishlash NEW" Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground)
                        .clickable {
                            scope.launch { snackbarHostState.showSnackbar(strings.earnStarsDesc) }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.earnStars, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tgColors.accent)
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.earnStarsDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Tabs & Transactions Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(tgColors.cardBackground),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        listOf(
                            strings.allTransactionsTab,
                            strings.incomingTab,
                            strings.outgoingTab,
                        ).forEachIndexed { idx, label ->
                            val selected = selectedTab == idx
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = idx }
                                    .padding(top = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (selected) tgColors.accent else Color.Transparent),
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = tgColors.divider)

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = tgColors.accent)
                        }
                    } else if (transactions.isEmpty()) {
                        Text(
                            text = "Tranzaksiyalar yoʻq",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        transactions.forEachIndexed { idx, tx ->
                            StarTransactionRow(
                                tx = tx,
                                chatsMap = chatsMap,
                                usersMap = usersMap,
                                filesMap = filesMap,
                                clientManager = clientManager,
                            )
                            if (idx < transactions.lastIndex) {
                                HorizontalDivider(color = tgColors.divider, modifier = Modifier.padding(start = 72.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarTransactionRow(
    tx: TdApi.StarTransaction,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    filesMap: Map<Int, TdApi.File>,
    clientManager: TelegramClientManager,
) {
    val strings = LocalStrings.current
    var peerId = 0L
    var peerTitle = "Telegram"
    var peerPhoto: TdApi.File? = null
    var subtitle = strings.telegramStarsTitle

    when (val t = tx.type) {
        is TdApi.StarTransactionTypeGiftSale -> {
            val u = usersMap[t.userId]
            peerId = t.userId
            peerTitle = u?.displayName() ?: "Telegram"
            peerPhoto = u?.profilePhoto?.small
            subtitle = "${t.gift.sticker.emoji.ifBlank { "🎁" }} ${strings.giftConverted.removePrefix("🧸 ")}"
        }
        is TdApi.StarTransactionTypeGiftPurchase -> {
            val uid = (t.ownerId as? TdApi.MessageSenderUser)?.userId
            val cid = (t.ownerId as? TdApi.MessageSenderChat)?.chatId
            val u = uid?.let { usersMap[it] }
            val c = cid?.let { chatsMap[it]?.chat }
            peerId = uid ?: cid ?: 0L
            peerTitle = u?.displayName() ?: c?.title ?: "Telegram"
            peerPhoto = u?.profilePhoto?.small ?: c?.photo?.small
            subtitle = t.gift.sticker.emoji.ifBlank { "🎁 Hadya" }
        }
        is TdApi.StarTransactionTypeChannelPaidReactionSend -> {
            val c = chatsMap[t.chatId]?.chat
            peerId = t.chatId
            peerTitle = c?.title ?: "Kanal"
            peerPhoto = c?.photo?.small
            subtitle = strings.starReaction
        }
        is TdApi.StarTransactionTypeBotInvoicePurchase -> {
            val u = usersMap[t.userId]
            peerId = t.userId
            peerTitle = u?.displayName() ?: "Bot"
            peerPhoto = u?.profilePhoto?.small
            subtitle = t.productInfo.title.ifBlank { strings.topUpBalance }
        }
        is TdApi.StarTransactionTypeUserDeposit -> {
            val u = usersMap[t.userId]
            peerId = t.userId
            peerTitle = u?.displayName() ?: "Telegram"
            peerPhoto = u?.profilePhoto?.small
            subtitle = strings.topUpBalance
        }
        is TdApi.StarTransactionTypePremiumPurchase -> {
            val u = usersMap[t.userId]
            peerId = t.userId
            peerTitle = u?.displayName() ?: "Telegram Premium"
            peerPhoto = u?.profilePhoto?.small
            subtitle = "${strings.telegramPremiumTitle} (${t.monthCount})"
        }
        else -> {
            subtitle = strings.telegramStarsTitle
        }
    }

    val starCount = tx.starAmount.starCount
    val isPositive = starCount >= 0
    val amountText = if (isPositive) "+$starCount" else "$starCount"
    val amountColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val dateStr = remember(tx.date) {
        SimpleDateFormat("d-MMM, HH:mm", Locale.getDefault()).format(Date(tx.date * 1000L))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TelegramAvatar(
            id = peerId,
            title = peerTitle,
            photoFile = peerPhoto,
            filesMap = filesMap,
            clientManager = clientManager,
            size = 44.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amountText,
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Rounded.Star, contentDescription = null, tint = GoldStarEnd, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SupportEmblemAvatar(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF2AA1F2), Color(0xFF1A73E8)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.SupportAgent,
            contentDescription = "Support",
            tint = Color.White,
            modifier = Modifier.size(size * 0.62f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostlySupportChatScreen(
    me: TdApi.User?,
    repository: GhostlyAccountRepository,
    onOpenSupportProfile: () -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val accountKey = remember(me?.id, me?.phoneNumber) { telegramAccountKey(me) }

    val messages by remember(repository, accountKey) {
        repository.observeMySupportMessages(accountKey)
    }.collectAsState(initial = emptyList<SupportMessage>())

    var input by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            // Floating Pill Top Header (+47777 Ghostly Messenger Support)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgColors.headerBackground)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel, tint = tgColors.headerContent)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(tgColors.pillBackground.copy(alpha = 0.72f))
                        .clickable(onClick = onOpenSupportProfile)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SupportEmblemAvatar(size = 34.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.supportCenterTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = tgColors.headerContent,
                        )
                        Text(
                            text = strings.supportRoleSubtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onOpenSupportProfile) {
                    Icon(Icons.Rounded.Call, contentDescription = strings.callLabel, tint = tgColors.headerContent)
                }
                IconButton(onClick = onOpenSupportProfile) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Info", tint = tgColors.headerContent)
                }
            }

            // Chat Body with Wallpaper + Center Prompt Pill
            TelegramWallpaperBackground(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.48f))
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = strings.supportWelcomePill,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = strings.supportWelcomePill.replace("\n", " "),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(horizontal = 12.dp, vertical = 5.dp),
                                    )
                                }
                            }
                            items(messages, key = { it.id }) { m ->
                                val isUserMsg = !m.fromAdmin
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (isUserMsg) Alignment.End else Alignment.Start,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 300.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isUserMsg) tgColors.outgoingBubble else tgColors.incomingBubble)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                    ) {
                                        Text(m.text, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            modifier = Modifier.align(Alignment.End),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            val ts = m.createdAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
                                            Text(
                                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isUserMsg) tgColors.outgoingTime else tgColors.incomingTime,
                                            )
                                            if (isUserMsg) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Rounded.DoneAll,
                                                    contentDescription = null,
                                                    tint = tgColors.outgoingTime,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Composer matching Screenshot 11
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tgColors.cardBackground)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 6.dp)) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                        cursorBrush = SolidColor(tgColors.accent),
                        decorationBox = { inner ->
                            if (input.isEmpty()) {
                                Text(strings.messageHint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                IconButton(
                    onClick = {
                        val trimmed = input.trim()
                        if (trimmed.isBlank()) return@IconButton
                        input = ""
                        scope.launch {
                            repository.sendUserSupportMessage(me, trimmed)
                                .onFailure { err ->
                                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Error")
                                }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank()) tgColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostlySupportProfileScreen(
    onOpenChat: () -> Unit,
    onBack: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(false) }
    var isContactAdded by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                },
                title = {},
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SupportEmblemAvatar(size = 88.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = strings.supportCenterTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.supportRoleSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(18.dp))

            // 3 Pill Actions matching Screenshot 10
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SupportActionPill(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    label = strings.previewMessage,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenChat,
                )
                SupportActionPill(
                    icon = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.Rounded.Notifications,
                    label = if (isMuted) strings.unmute else strings.mute,
                    modifier = Modifier.weight(1f),
                    onClick = { isMuted = !isMuted },
                )
                SupportActionPill(
                    icon = Icons.Rounded.Call,
                    label = strings.callLabel,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar(strings.supportBioDisplay) }
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card with +47777 and official bio matching Screenshot 10
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tgColors.cardBackground)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboard.setText(AnnotatedString(strings.supportPhoneDisplay))
                            scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                        }
                        .padding(vertical = 4.dp),
                ) {
                    Text(strings.supportPhoneDisplay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(strings.mobileLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(strings.supportBioDisplay, style = MaterialTheme.typography.bodyLarge)
                    Text(strings.bioLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add to contacts card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tgColors.cardBackground)
                    .clickable {
                        isContactAdded = true
                        scope.launch { snackbarHostState.showSnackbar(strings.savedToast) }
                    }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isContactAdded) strings.savedToast else strings.addToContacts,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SupportActionPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tgColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
