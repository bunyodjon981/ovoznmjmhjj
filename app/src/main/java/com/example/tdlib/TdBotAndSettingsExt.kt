package com.example.tdlib

import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.TelegramExtendedColors
import org.drinkless.tdlib.TdApi

enum class PrivacyOption {
    EVERYBODY,
    CONTACTS,
    NOBODY,
}

suspend fun TelegramClientManager.sendBotStart(
    botUserId: Long,
    chatId: Long,
    parameter: String = "",
): Result<TdApi.Message> = send(TdApi.SendBotStartMessage(botUserId, chatId, parameter))

suspend fun TelegramClientManager.pressCallbackButton(
    chatId: Long,
    messageId: Long,
    data: ByteArray,
): Result<TdApi.CallbackQueryAnswer> =
    send(TdApi.GetCallbackQueryAnswer(chatId, messageId, TdApi.CallbackQueryPayloadData(data)))

suspend fun TelegramClientManager.openTelegramWebApp(
    chatId: Long,
    botUserId: Long,
    url: String,
    tgColors: TelegramExtendedColors,
): Result<TdApi.WebAppInfo> {
    val params = buildWebAppParams(tgColors)
    return send(TdApi.OpenWebApp(chatId, botUserId, url, null, null, params))
}

suspend fun TelegramClientManager.fetchWebAppUrl(
    botUserId: Long,
    url: String,
    tgColors: TelegramExtendedColors,
): Result<String> {
    val params = buildWebAppParams(tgColors)
    return send(TdApi.GetWebAppUrl(botUserId, url, params)).map { it.url }
}

private fun buildWebAppParams(tgColors: TelegramExtendedColors): TdApi.WebAppOpenParameters {
    val bg = tgColors.chatBackground.toArgb()
    val secBg = tgColors.cardBackground.toArgb()
    val headerBg = tgColors.headerBackground.toArgb()
    val text = tgColors.headerContent.toArgb()
    val accent = tgColors.accent.toArgb()
    val hint = tgColors.incomingTime.toArgb()
    val divider = tgColors.divider.toArgb()
    val theme = TdApi.ThemeParameters(
        bg,
        secBg,
        headerBg,
        secBg,
        secBg,
        divider,
        text,
        accent,
        accent,
        hint,
        0xFFEF5350.toInt(),
        hint,
        accent,
        accent,
        0xFFFFFFFF.toInt(),
    )
    return TdApi.WebAppOpenParameters(theme, "Ghostly Messenger", TdApi.WebAppOpenModeFullSize())
}

// Chat Folders
suspend fun TelegramClientManager.getRecommendedFolders(): Result<List<TdApi.RecommendedChatFolder>> =
    send(TdApi.GetRecommendedChatFolders()).map { it.chatFolders.toList() }

suspend fun TelegramClientManager.createFolder(folder: TdApi.ChatFolder): Result<TdApi.ChatFolderInfo> =
    send(TdApi.CreateChatFolder(folder))

suspend fun TelegramClientManager.createSimpleFolder(
    name: String,
    iconName: String = "Custom",
    includeContacts: Boolean = true,
    includeNonContacts: Boolean = true,
    includeGroups: Boolean = false,
    includeChannels: Boolean = false,
    includeBots: Boolean = false,
    excludeRead: Boolean = false,
): Result<TdApi.ChatFolderInfo> {
    val folder = TdApi.ChatFolder(
        TdApi.ChatFolderName(TdApi.FormattedText(name, emptyArray()), true),
        TdApi.ChatFolderIcon(iconName),
        -1,
        false,
        longArrayOf(),
        longArrayOf(),
        longArrayOf(),
        false,
        excludeRead,
        true,
        includeContacts,
        includeNonContacts,
        includeBots,
        includeGroups,
        includeChannels,
    )
    return createFolder(folder)
}

suspend fun TelegramClientManager.renameFolder(
    folderId: Int,
    newName: String,
): Result<TdApi.ChatFolderInfo> {
    val folder = TdApi.ChatFolder(
        TdApi.ChatFolderName(TdApi.FormattedText(newName, emptyArray()), true),
        TdApi.ChatFolderIcon("Custom"),
        -1,
        false,
        longArrayOf(),
        longArrayOf(),
        longArrayOf(),
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
    )
    return send(TdApi.EditChatFolder(folderId, folder))
}

suspend fun TelegramClientManager.deleteFolder(folderId: Int): Result<TdApi.Ok> =
    send(TdApi.DeleteChatFolder(folderId, longArrayOf()))

// Account & Profile
suspend fun TelegramClientManager.updateUsername(username: String): Result<TdApi.Ok> =
    send(TdApi.SetUsername(username.trim().removePrefix("@")))

suspend fun TelegramClientManager.updateBirthdate(day: Int, month: Int, year: Int): Result<TdApi.Ok> =
    send(TdApi.SetBirthdate(TdApi.Birthdate(day, month, year)))

suspend fun TelegramClientManager.uploadProfilePhoto(localPath: String): Result<TdApi.Ok> =
    send(TdApi.SetProfilePhoto(TdApi.InputChatPhotoStatic(TdApi.InputFileLocal(localPath)), true))

// Devices & Security & Privacy & Notifications
suspend fun TelegramClientManager.terminateAllOtherSessions(): Result<TdApi.Ok> =
    send(TdApi.TerminateAllOtherSessions())

suspend fun TelegramClientManager.getBlockedUsersTotalCount(): Result<Int> =
    send(TdApi.GetBlockedMessageSenders(TdApi.BlockListMain(), 0, 1)).map { it.totalCount }

suspend fun TelegramClientManager.getAccountTtlDays(): Result<Int> =
    send(TdApi.GetAccountTtl()).map { it.days }

suspend fun TelegramClientManager.setAccountTtlDays(days: Int): Result<TdApi.Ok> =
    send(TdApi.SetAccountTtl(TdApi.AccountTtl(days)))

suspend fun TelegramClientManager.getPrivacyOption(setting: TdApi.UserPrivacySetting): Result<PrivacyOption> =
    send(TdApi.GetUserPrivacySettingRules(setting)).map { rules ->
        val first = rules.rules.firstOrNull()
        when (first) {
            is TdApi.UserPrivacySettingRuleAllowAll -> PrivacyOption.EVERYBODY
            is TdApi.UserPrivacySettingRuleAllowContacts -> PrivacyOption.CONTACTS
            else -> PrivacyOption.NOBODY
        }
    }

suspend fun TelegramClientManager.setPrivacyOption(
    setting: TdApi.UserPrivacySetting,
    option: PrivacyOption,
): Result<TdApi.Ok> {
    val rule: TdApi.UserPrivacySettingRule = when (option) {
        PrivacyOption.EVERYBODY -> TdApi.UserPrivacySettingRuleAllowAll()
        PrivacyOption.CONTACTS -> TdApi.UserPrivacySettingRuleAllowContacts()
        PrivacyOption.NOBODY -> TdApi.UserPrivacySettingRuleRestrictAll()
    }
    return send(TdApi.SetUserPrivacySettingRules(setting, TdApi.UserPrivacySettingRules(arrayOf(rule))))
}

suspend fun TelegramClientManager.getScopeNotification(
    scope: TdApi.NotificationSettingsScope,
): Result<TdApi.ScopeNotificationSettings> =
    send(TdApi.GetScopeNotificationSettings(scope))

suspend fun TelegramClientManager.updateScopeNotification(
    scope: TdApi.NotificationSettingsScope,
    current: TdApi.ScopeNotificationSettings,
    mute: Boolean,
    showPreview: Boolean,
): Result<TdApi.Ok> {
    val updated = TdApi.ScopeNotificationSettings(
        if (mute) Int.MAX_VALUE else 0,
        current.soundId,
        showPreview,
        current.useDefaultMuteStories,
        current.muteStories,
        current.storySoundId,
        current.showStoryPoster,
        current.disablePinnedMessageNotifications,
        current.disableMentionNotifications,
    )
    return send(TdApi.SetScopeNotificationSettings(scope, updated))
}

// Media Albums, Video Notes, Audio, Polls
suspend fun TelegramClientManager.sendPhotoAlbum(
    chatId: Long,
    localPaths: List<String>,
    caption: String = "",
    replyToMessageId: Long? = null,
): Result<TdApi.Messages> {
    val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
    val contents = localPaths.take(10).mapIndexed { index, path ->
        TdApi.InputMessagePhoto(
            TdApi.InputFileLocal(path),
            null,
            intArrayOf(),
            0,
            0,
            TdApi.FormattedText(if (index == 0) caption else "", emptyArray()),
            false,
            null,
            false,
        )
    }.toTypedArray<TdApi.InputMessageContent>()
    return send(TdApi.SendMessageAlbum(chatId, null, replyTo, null, contents))
}

suspend fun TelegramClientManager.sendVideoNoteMessage(
    chatId: Long,
    localPath: String,
    durationSeconds: Int,
    length: Int = 360,
    replyToMessageId: Long? = null,
): Result<TdApi.Message> {
    val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
    val content = TdApi.InputMessageVideoNote(
        TdApi.InputFileLocal(localPath),
        null,
        durationSeconds.coerceAtLeast(1),
        length,
        null,
    )
    return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
}

suspend fun TelegramClientManager.sendAudioMessage(
    chatId: Long,
    localPath: String,
    caption: String = "",
    replyToMessageId: Long? = null,
): Result<TdApi.Message> {
    val replyTo = replyToMessageId?.let { TdApi.InputMessageReplyToMessage(it, null, 0) }
    val content = TdApi.InputMessageAudio(
        TdApi.InputFileLocal(localPath),
        null,
        0,
        "",
        "",
        TdApi.FormattedText(caption, emptyArray()),
    )
    return send(TdApi.SendMessage(chatId, null, replyTo, null, null, content))
}

suspend fun TelegramClientManager.sendPollMessage(
    chatId: Long,
    question: String,
    options: List<String>,
    isAnonymous: Boolean = true,
    allowMultipleAnswers: Boolean = false,
): Result<TdApi.Message> {
    val formattedOptions = options.filter { it.isNotBlank() }.take(10)
        .map { TdApi.FormattedText(it.trim(), emptyArray()) }.toTypedArray()
    val content = TdApi.InputMessagePoll(
        TdApi.FormattedText(question.trim(), emptyArray()),
        formattedOptions,
        isAnonymous,
        TdApi.PollTypeRegular(allowMultipleAnswers),
        0,
        0,
        false,
    )
    return send(TdApi.SendMessage(chatId, null, null, null, null, content))
}

suspend fun TelegramClientManager.votePoll(
    chatId: Long,
    messageId: Long,
    optionIds: IntArray,
): Result<TdApi.Ok> = send(TdApi.SetPollAnswer(chatId, messageId, optionIds))

// Channel & Group Management (Tahrirlash)
suspend fun TelegramClientManager.updateChatTitle(chatId: Long, title: String): Result<TdApi.Ok> =
    send(TdApi.SetChatTitle(chatId, title.trim()))

suspend fun TelegramClientManager.updateChatDescription(chatId: Long, description: String): Result<TdApi.Ok> =
    send(TdApi.SetChatDescription(chatId, description.trim()))

suspend fun TelegramClientManager.updateChatPhoto(chatId: Long, localPath: String): Result<TdApi.Ok> =
    send(TdApi.SetChatPhoto(chatId, TdApi.InputChatPhotoStatic(TdApi.InputFileLocal(localPath))))

suspend fun TelegramClientManager.updateSupergroupUsername(supergroupId: Long, username: String): Result<TdApi.Ok> =
    send(TdApi.SetSupergroupUsername(supergroupId, username.trim().removePrefix("@")))

suspend fun TelegramClientManager.updateChatPermissions(
    chatId: Long,
    permissions: TdApi.ChatPermissions,
): Result<TdApi.Ok> = send(TdApi.SetChatPermissions(chatId, permissions))

suspend fun TelegramClientManager.updateChatReactionsAll(chatId: Long, maxCount: Int = 11): Result<TdApi.Ok> =
    send(TdApi.SetChatAvailableReactions(chatId, TdApi.ChatAvailableReactionsAll(maxCount)))

suspend fun TelegramClientManager.updateChatReactionsNone(chatId: Long): Result<TdApi.Ok> =
    send(TdApi.SetChatAvailableReactions(chatId, TdApi.ChatAvailableReactionsSome(emptyArray(), 0)))

suspend fun TelegramClientManager.deleteChat(chatId: Long): Result<TdApi.Ok> =
    send(TdApi.DeleteChat(chatId))

suspend fun TelegramClientManager.getSupergroupAdmins(supergroupId: Long): Result<List<TdApi.ChatMember>> =
    send(TdApi.GetSupergroupMembers(supergroupId, TdApi.SupergroupMembersFilterAdministrators(), 0, 100))
        .map { it.members.toList() }

suspend fun TelegramClientManager.getSupergroupBanned(supergroupId: Long): Result<List<TdApi.ChatMember>> =
    send(TdApi.GetSupergroupMembers(supergroupId, TdApi.SupergroupMembersFilterBanned(""), 0, 100))
        .map { it.members.toList() }

// Storage, Network Statistics & Proxy
suspend fun TelegramClientManager.getStorageStatistics(chatLimit: Int = 25): Result<TdApi.StorageStatistics> =
    send(TdApi.GetStorageStatistics(chatLimit))

suspend fun TelegramClientManager.optimizeStorageCache(
    fileTypes: Array<TdApi.FileType>? = null,
): Result<TdApi.StorageStatistics> = send(
    TdApi.OptimizeStorage(
        0L,
        0,
        0,
        0,
        fileTypes ?: emptyArray(),
        longArrayOf(),
        longArrayOf(),
        false,
        25,
    )
)

suspend fun TelegramClientManager.getNetworkStatistics(onlyCurrent: Boolean = false): Result<TdApi.NetworkStatistics> =
    send(TdApi.GetNetworkStatistics(onlyCurrent))

suspend fun TelegramClientManager.resetNetworkStatistics(): Result<TdApi.Ok> =
    send(TdApi.ResetNetworkStatistics())

suspend fun TelegramClientManager.clearAllDrafts(): Result<TdApi.Ok> =
    send(TdApi.ClearAllDraftMessages(false))

suspend fun TelegramClientManager.getProxies(): Result<List<TdApi.Proxy>> =
    send(TdApi.GetProxies()).map { it.proxies.toList() }

suspend fun TelegramClientManager.addSocks5Proxy(
    server: String,
    port: Int,
    username: String = "",
    password: String = "",
): Result<TdApi.Proxy> = send(
    TdApi.AddProxy(
        server.trim(),
        port,
        true,
        TdApi.ProxyTypeSocks5(username.trim(), password),
    )
)

suspend fun TelegramClientManager.enableProxy(proxyId: Int): Result<TdApi.Ok> =
    send(TdApi.EnableProxy(proxyId))

suspend fun TelegramClientManager.disableProxy(): Result<TdApi.Ok> =
    send(TdApi.DisableProxy())

suspend fun TelegramClientManager.removeProxy(proxyId: Int): Result<TdApi.Ok> =
    send(TdApi.RemoveProxy(proxyId))

