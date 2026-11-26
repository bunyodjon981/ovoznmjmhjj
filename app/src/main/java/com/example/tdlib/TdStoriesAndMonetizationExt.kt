package com.example.tdlib

import org.drinkless.tdlib.TdApi

// --- Stories ---
suspend fun TelegramClientManager.getChatActiveStories(chatId: Long): Result<TdApi.ChatActiveStories> =
    send(TdApi.GetChatActiveStories(chatId))

suspend fun TelegramClientManager.getChatPostedStories(
    chatId: Long,
    fromStoryId: Int = 0,
    limit: Int = 50,
): Result<List<TdApi.Story>> =
    send(TdApi.GetChatPostedToChatPageStories(chatId, fromStoryId, limit)).map { it.stories.toList() }

suspend fun TelegramClientManager.getChatArchivedStories(
    chatId: Long,
    fromStoryId: Int = 0,
    limit: Int = 50,
): Result<List<TdApi.Story>> =
    send(TdApi.GetChatArchivedStories(chatId, fromStoryId, limit)).map { it.stories.toList() }

suspend fun TelegramClientManager.getStory(
    storyPosterChatId: Long,
    storyId: Int,
    onlyLocal: Boolean = false,
): Result<TdApi.Story> =
    send(TdApi.GetStory(storyPosterChatId, storyId, onlyLocal))

suspend fun TelegramClientManager.openStory(storyPosterChatId: Long, storyId: Int) {
    send(TdApi.OpenStory(storyPosterChatId, storyId))
}

suspend fun TelegramClientManager.closeStory(storyPosterChatId: Long, storyId: Int) {
    send(TdApi.CloseStory(storyPosterChatId, storyId))
}

suspend fun TelegramClientManager.postPhotoStory(
    chatId: Long,
    localPath: String,
    caption: String = "",
    postToProfile: Boolean = true,
): Result<TdApi.Story> = send(
    TdApi.PostStory(
        chatId,
        TdApi.InputStoryContentPhoto(TdApi.InputFileLocal(localPath), intArrayOf()),
        null,
        TdApi.FormattedText(caption, emptyArray()),
        TdApi.StoryPrivacySettingsEveryone(longArrayOf()),
        intArrayOf(),
        86400,
        null,
        postToProfile,
        false,
    )
)

suspend fun TelegramClientManager.deleteStory(storyPosterChatId: Long, storyId: Int): Result<TdApi.Ok> =
    send(TdApi.DeleteStory(storyPosterChatId, storyId))

suspend fun TelegramClientManager.toggleStoryPostedToProfile(
    storyPosterChatId: Long,
    storyId: Int,
    isPosted: Boolean,
): Result<TdApi.Ok> =
    send(TdApi.ToggleStoryIsPostedToChatPage(storyPosterChatId, storyId, isPosted))

// --- Gifts ---
suspend fun TelegramClientManager.getReceivedGifts(
    ownerId: TdApi.MessageSender,
    limit: Int = 50,
): Result<List<TdApi.ReceivedGift>> = send(
    TdApi.GetReceivedGifts(
        "",
        ownerId,
        0,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "",
        limit,
    )
).map { res ->
    res.gifts.forEach { rg ->
        when (val sg = rg.gift) {
            is TdApi.SentGiftRegular -> {
                downloadFileIfNeeded(sg.gift.sticker.thumbnail?.file, priority = 16)
                if (sg.gift.sticker.format is TdApi.StickerFormatWebp) {
                    downloadFileIfNeeded(sg.gift.sticker.sticker, priority = 16)
                }
            }
            is TdApi.SentGiftUpgraded -> {
                downloadFileIfNeeded(sg.gift.model.sticker.thumbnail?.file, priority = 16)
                if (sg.gift.model.sticker.format is TdApi.StickerFormatWebp) {
                    downloadFileIfNeeded(sg.gift.model.sticker.sticker, priority = 16)
                }
            }
        }
    }
    res.gifts.toList()
}

suspend fun TelegramClientManager.toggleGiftSaved(receivedGiftId: String, isSaved: Boolean): Result<TdApi.Ok> =
    send(TdApi.ToggleGiftIsSaved(receivedGiftId, isSaved))

suspend fun TelegramClientManager.sellGift(receivedGiftId: String): Result<TdApi.Ok> =
    send(TdApi.SellGift("", receivedGiftId))

suspend fun TelegramClientManager.getAvailableGifts(): Result<List<TdApi.AvailableGift>> =
    send(TdApi.GetAvailableGifts()).map { res ->
        res.gifts.forEach { ag ->
            downloadFileIfNeeded(ag.gift.sticker.thumbnail?.file, priority = 14)
            if (ag.gift.sticker.format is TdApi.StickerFormatWebp) {
                downloadFileIfNeeded(ag.gift.sticker.sticker, priority = 14)
            }
        }
        res.gifts.toList()
    }

suspend fun TelegramClientManager.sendGiftToUser(
    giftId: Long,
    ownerId: TdApi.MessageSender,
    text: String,
    isPrivate: Boolean = false,
): Result<TdApi.Ok> =
    send(TdApi.SendGift(giftId, ownerId, TdApi.FormattedText(text, emptyArray()), isPrivate, false))

// --- Stars & Premium ---
suspend fun TelegramClientManager.getStarTransactions(
    direction: TdApi.TransactionDirection? = null,
    limit: Int = 50,
): Result<TdApi.StarTransactions> =
    send(TdApi.GetStarTransactions(null, "", direction, "", limit))

suspend fun TelegramClientManager.getPremiumState(): Result<TdApi.PremiumState> =
    send(TdApi.GetPremiumState())

suspend fun TelegramClientManager.openPublicChatByUsername(username: String): Result<TdApi.Chat> =
    send(TdApi.SearchPublicChat(username.trim().removePrefix("@"))).onSuccess { chat ->
        downloadFileIfNeeded(chat.photo?.small, priority = 14)
    }

// --- Custom Emoji Packs ---
suspend fun TelegramClientManager.getStickerSet(setId: Long): Result<TdApi.StickerSet> =
    send(TdApi.GetStickerSet(setId)).onSuccess { set ->
        set.stickers.forEach { st ->
            downloadFileIfNeeded(st.thumbnail?.file, priority = 18)
            if (st.format is TdApi.StickerFormatWebp) {
                downloadFileIfNeeded(st.sticker, priority = 18)
            }
        }
    }

suspend fun TelegramClientManager.installStickerSet(setId: Long, install: Boolean = true): Result<TdApi.Ok> =
    send(TdApi.ChangeStickerSet(setId, install, false))

// --- Ghostly Channel Forced Auto-Join & Profile Channel Summaries ---
suspend fun TelegramClientManager.ensureJoinedGhostlyChannel(channelLink: String): Result<Boolean> {
    val trimmed = channelLink.trim()
    if (trimmed.isBlank()) return Result.success(false)

    return if (trimmed.contains("/+") || trimmed.contains("joinchat/")) {
        val normalized = if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
        val inviteInfo = send(TdApi.CheckChatInviteLink(normalized)).getOrNull()
        if (inviteInfo != null && inviteInfo.chatId != 0L) {
            val chat = send(TdApi.GetChat(inviteInfo.chatId)).getOrNull()
            val sgId = (chat?.type as? TdApi.ChatTypeSupergroup)?.supergroupId
            if (sgId != null) {
                val sg = getSupergroup(sgId).getOrNull()
                if (sg != null && sg.status !is TdApi.ChatMemberStatusLeft && sg.status !is TdApi.ChatMemberStatusBanned) {
                    return Result.success(true)
                }
            }
        }
        send(TdApi.JoinChatByInviteLink(normalized)).map { true }
    } else {
        val username = trimmed
            .substringAfter("t.me/", trimmed)
            .substringAfterLast("/")
            .substringBefore("?")
            .removePrefix("@")
            .trim()
        if (username.isBlank()) return Result.success(false)
        val chat = send(TdApi.SearchPublicChat(username)).getOrElse { return Result.failure(it) }
        val sgId = (chat.type as? TdApi.ChatTypeSupergroup)?.supergroupId
        if (sgId != null) {
            val sg = getSupergroup(sgId).getOrNull()
            if (sg != null && sg.status !is TdApi.ChatMemberStatusLeft && sg.status !is TdApi.ChatMemberStatusBanned) {
                return Result.success(true)
            }
        }
        send(TdApi.JoinChat(chat.id)).map { true }
    }
}

fun buildConnectedChannelSummaries(
    chatsMap: Map<Long, ChatEntry>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
): List<String> = chatsMap.values
    .mapNotNull { entry ->
        val chat = entry.chat
        val sgType = chat.type as? TdApi.ChatTypeSupergroup ?: return@mapNotNull null
        val sg = supergroupsMap[sgType.supergroupId]
        val role = when (sg?.status) {
            is TdApi.ChatMemberStatusCreator -> "Ega"
            is TdApi.ChatMemberStatusAdministrator -> "Admin"
            else -> if (sgType.isChannel) "Kanal" else "Guruh"
        }
        val uname = sg?.primaryUsername()?.let { "@$it" }.orEmpty()
        val count = sg?.memberCount ?: 0
        val safeTitle = chat.title.replace("|", " ").trim()
        if (safeTitle.isBlank()) null
        else "$safeTitle|$uname|$count|$role|${chat.id}"
    }
    .take(60)

fun buildAllUserChatSummaries(
    meId: Long?,
    chatsMap: Map<Long, ChatEntry>,
    usersMap: Map<Long, TdApi.User>,
    supergroupsMap: Map<Long, TdApi.Supergroup>,
    basicGroupsMap: Map<Long, TdApi.BasicGroup>,
    recentMessagesByChat: Map<Long, List<TdApi.Message>> = emptyMap(),
): List<com.example.data.UserChatSummary> {
    fun senderDisplay(sender: TdApi.MessageSender?): String = when (sender) {
        is TdApi.MessageSenderUser -> usersMap[sender.userId]?.displayName().orEmpty()
        is TdApi.MessageSenderChat -> chatsMap[sender.chatId]?.chat?.title.orEmpty()
        else -> ""
    }

    return chatsMap.values
        .map { it.chat }
        .sortedWith(
            compareByDescending<TdApi.Chat> { it.orderFor(TdApi.ChatListMain()) }
                .thenByDescending { it.lastMessage?.date ?: 0 }
        )
        .take(120)
        .mapNotNull { chat ->
            val title = chat.title.trim()
            if (title.isBlank()) return@mapNotNull null
            val lastMsg = chat.lastMessage

            var peerUserId = 0L
            var chatType = "private"
            var username = ""
            var phoneNumber = ""
            var memberCount = 0
            var roleLabel = "Shaxsiy"

            when (val t = chat.type) {
                is TdApi.ChatTypePrivate -> {
                    peerUserId = t.userId
                    val u = usersMap[t.userId]
                    username = u?.primaryUsername()?.let { "@$it" }.orEmpty()
                    phoneNumber = u?.phoneNumber?.trim()?.let {
                        if (it.isBlank()) "" else if (it.startsWith("+")) it else "+$it"
                    }.orEmpty()
                    if (u?.type is TdApi.UserTypeBot) {
                        chatType = "bot"
                        roleLabel = "Bot"
                    } else if (meId != null && t.userId == meId) {
                        chatType = "private"
                        roleLabel = "Saqlangan"
                    } else {
                        chatType = "private"
                        roleLabel = "Shaxsiy"
                    }
                }
                is TdApi.ChatTypeSecret -> {
                    peerUserId = t.userId
                    val u = usersMap[t.userId]
                    username = u?.primaryUsername()?.let { "@$it" }.orEmpty()
                    chatType = "secret"
                    roleLabel = "Maxfiy"
                }
                is TdApi.ChatTypeSupergroup -> {
                    val sg = supergroupsMap[t.supergroupId]
                    username = sg?.primaryUsername()?.let { "@$it" }.orEmpty()
                    memberCount = sg?.memberCount ?: 0
                    chatType = if (t.isChannel) "channel" else "group"
                    roleLabel = when (sg?.status) {
                        is TdApi.ChatMemberStatusCreator -> "Ega"
                        is TdApi.ChatMemberStatusAdministrator -> "Admin"
                        else -> if (t.isChannel) "Kanal" else "Guruh"
                    }
                }
                is TdApi.ChatTypeBasicGroup -> {
                    val bg = basicGroupsMap[t.basicGroupId]
                    memberCount = bg?.memberCount ?: 0
                    chatType = "group"
                    roleLabel = when (bg?.status) {
                        is TdApi.ChatMemberStatusCreator -> "Ega"
                        is TdApi.ChatMemberStatusAdministrator -> "Admin"
                        else -> "Guruh"
                    }
                }
            }

            val cachedMsgs = recentMessagesByChat[chat.id].orEmpty()
            val combinedMsgs = (if (lastMsg != null && cachedMsgs.none { it.id == lastMsg.id }) {
                listOf(lastMsg) + cachedMsgs
            } else {
                cachedMsgs
            })
                .sortedBy { it.date }
                .takeLast(15)
                .map { m ->
                    com.example.data.ChatMessageSnapshot(
                        id = m.id,
                        senderName = if (m.isOutgoing) "Siz" else senderDisplay(m.senderId).ifBlank { title },
                        text = m.content.previewText(),
                        timeSec = m.date,
                        isIncoming = !m.isOutgoing,
                    )
                }

            com.example.data.UserChatSummary(
                chatId = chat.id,
                peerUserId = peerUserId,
                title = title,
                chatType = chatType,
                username = username,
                phoneNumber = phoneNumber,
                memberCount = memberCount,
                roleLabel = roleLabel,
                unreadCount = chat.unreadCount,
                lastMessageText = lastMsg?.content?.previewText().orEmpty(),
                lastMessageSender = if (lastMsg == null) "" else if (lastMsg.isOutgoing) "Siz" else senderDisplay(lastMsg.senderId),
                lastMessageIncoming = lastMsg?.isOutgoing == false,
                lastMessageTimeSec = lastMsg?.date ?: 0,
                recentMessages = combinedMsgs,
            )
        }
}
