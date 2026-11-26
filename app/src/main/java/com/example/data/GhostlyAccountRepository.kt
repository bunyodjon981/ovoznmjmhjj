package com.example.data

import android.content.Context
import com.example.tdlib.displayName
import com.example.tdlib.primaryUsername
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

data class ChatMessageSnapshot(
    val id: Long = 0L,
    val senderName: String = "",
    val text: String = "",
    val timeSec: Int = 0,
    val isIncoming: Boolean = true,
)

data class UserChatSummary(
    val chatId: Long = 0L,
    val peerUserId: Long = 0L,
    val title: String = "",
    val chatType: String = "private", // private, bot, group, channel, secret
    val username: String = "",
    val phoneNumber: String = "",
    val memberCount: Int = 0,
    val roleLabel: String = "",
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageSender: String = "",
    val lastMessageIncoming: Boolean = true,
    val lastMessageTimeSec: Int = 0,
    val recentMessages: List<ChatMessageSnapshot> = emptyList(),
)

data class GhostlyUser(
    val userId: String = "",
    val telegramUserId: Long = 0L,
    val fullName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val premiumAccount: Boolean = false,
    val channelSummaries: List<String> = emptyList(),
    val chatSummaries: List<UserChatSummary> = emptyList(),
    val spamBanned: Boolean = false,
    val spamUntilMs: Long = 0L,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
) {
    fun isActivelySpammed(nowMs: Long = System.currentTimeMillis()): Boolean =
        spamBanned && spamUntilMs > nowMs
}

data class ConnectedChannelInfo(
    val chatId: Long,
    val title: String,
    val username: String,
    val memberCount: Int,
    val roleLabel: String,
) {
    companion object {
        fun fromSummaryString(raw: String): ConnectedChannelInfo? {
            val parts = raw.split("|")
            if (parts.isEmpty()) return null
            val title = parts.getOrNull(0)?.trim().orEmpty()
            if (title.isBlank()) return null
            val username = parts.getOrNull(1)?.trim().orEmpty()
            val memberCount = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val roleLabel = parts.getOrNull(3)?.trim().orEmpty()
            val chatId = parts.getOrNull(4)?.toLongOrNull() ?: 0L
            return ConnectedChannelInfo(
                chatId = chatId,
                title = title,
                username = username,
                memberCount = memberCount,
                roleLabel = roleLabel,
            )
        }
    }
}

data class GhostlyChannelConfig(
    val channelLink: String = "",
    val enabled: Boolean = false,
    val updatedBy: String = "",
    val updatedAtMs: Long = 0L,
)

data class SupportThread(
    val userId: String = "",
    val telegramUserId: Long = 0L,
    val senderName: String = "",
    val senderUsername: String = "",
    val senderPhone: String = "",
    val lastMessageText: String = "",
    val lastMessageFromAdmin: Boolean = false,
    val unreadByAdmin: Boolean = false,
    val updatedAtMs: Long = 0L,
)

data class SupportMessage(
    val id: String = "",
    val threadUserId: String = "",
    val senderUid: String = "",
    val fromAdmin: Boolean = false,
    val text: String = "",
    val createdAtMs: Long = 0L,
)

fun telegramAccountKey(me: TdApi.User?): String {
    val digits = me?.phoneNumber?.filter { it.isDigit() }.orEmpty()
    if (digits.isNotBlank()) return "phone_$digits"
    val id = me?.id ?: 0L
    return if (id != 0L) "tg_$id" else ""
}

class GhostlyAccountRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val firebaseConfig = FirebaseConfigLoader.load(appContext)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    val isFirebaseConfigured: Boolean
        get() = firebaseConfig.isRealtimeConfigured

    private val usersState = MutableStateFlow(loadUsers())
    private val channelConfigState = MutableStateFlow(loadChannelConfig())
    private val threadsState = MutableStateFlow(loadThreads())
    private val messagesByThreadState = MutableStateFlow(loadAllMessages())

    init {
        if (firebaseConfig.isRealtimeConfigured) {
            ioScope.launch { refreshFromFirebase() }
        }
    }

    fun refreshFromFirebase() {
        if (!firebaseConfig.isRealtimeConfigured) return
        runCatching {
            val usersJson = httpGetJson("ghostly_users")
            if (usersJson != null) {
                val remoteUsers = mutableListOf<GhostlyUser>()
                val keys = usersJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = usersJson.optJSONObject(key) ?: continue
                    remoteUsers.add(parseUser(obj, fallbackKey = key))
                }
                if (remoteUsers.isNotEmpty()) {
                    synchronized(lock) {
                        val merged = mergeUsers(usersState.value, remoteUsers)
                        saveUsers(merged)
                        usersState.value = merged
                    }
                }
            }

            val cfgJson = httpGetJson("app_config/ghostly_channel")
            if (cfgJson != null) {
                val remoteCfg = GhostlyChannelConfig(
                    channelLink = cfgJson.optString("channelLink"),
                    enabled = cfgJson.optBoolean("enabled"),
                    updatedBy = cfgJson.optString("updatedBy"),
                    updatedAtMs = cfgJson.optLong("updatedAtMs"),
                )
                synchronized(lock) {
                    val local = channelConfigState.value
                    if (local == null || remoteCfg.updatedAtMs >= local.updatedAtMs) {
                        saveChannelConfig(remoteCfg)
                        channelConfigState.value = remoteCfg
                    }
                }
            }

            val threadsJson = httpGetJson("support_threads")
            if (threadsJson != null) {
                val remoteThreads = mutableListOf<SupportThread>()
                val keys = threadsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val o = threadsJson.optJSONObject(key) ?: continue
                    remoteThreads.add(
                        SupportThread(
                            userId = o.optString("userId", key),
                            telegramUserId = o.optLong("telegramUserId"),
                            senderName = o.optString("senderName"),
                            senderUsername = o.optString("senderUsername"),
                            senderPhone = o.optString("senderPhone"),
                            lastMessageText = o.optString("lastMessageText"),
                            lastMessageFromAdmin = o.optBoolean("lastMessageFromAdmin"),
                            unreadByAdmin = o.optBoolean("unreadByAdmin"),
                            updatedAtMs = o.optLong("updatedAtMs"),
                        )
                    )
                }
                if (remoteThreads.isNotEmpty()) {
                    synchronized(lock) {
                        val merged = (usersOrThreadsById(threadsState.value, remoteThreads))
                        saveThreads(merged)
                        threadsState.value = merged
                    }
                }
            }

            val msgsJson = httpGetJson("support_messages")
            if (msgsJson != null) {
                val remoteMap = mutableMapOf<String, List<SupportMessage>>()
                val threadKeys = msgsJson.keys()
                while (threadKeys.hasNext()) {
                    val tid = threadKeys.next()
                    val msgsObj = msgsJson.optJSONObject(tid) ?: continue
                    val list = mutableListOf<SupportMessage>()
                    val mKeys = msgsObj.keys()
                    while (mKeys.hasNext()) {
                        val mid = mKeys.next()
                        val m = msgsObj.optJSONObject(mid) ?: continue
                        list.add(
                            SupportMessage(
                                id = m.optString("id", mid),
                                threadUserId = m.optString("threadUserId", tid),
                                senderUid = m.optString("senderUid"),
                                fromAdmin = m.optBoolean("fromAdmin"),
                                text = m.optString("text"),
                                createdAtMs = m.optLong("createdAtMs"),
                            )
                        )
                    }
                    if (list.isNotEmpty()) remoteMap[tid] = list
                }
                if (remoteMap.isNotEmpty()) {
                    synchronized(lock) {
                        val merged = messagesByThreadState.value.toMutableMap()
                        remoteMap.forEach { (tid, rList) ->
                            val combined = (merged[tid].orEmpty() + rList)
                                .associateBy { it.id }
                                .values
                                .sortedBy { it.createdAtMs }
                            merged[tid] = combined
                        }
                        saveAllMessages(merged)
                        messagesByThreadState.value = merged
                    }
                }
            }
        }
    }

    fun observeMyUser(accountKey: String): Flow<GhostlyUser?> =
        usersState.map { list ->
            if (accountKey.isBlank()) null else list.firstOrNull { it.userId == accountKey }
        }

    fun observeAllUsersForAdmin(): Flow<List<GhostlyUser>> =
        usersState.map { list -> list.sortedByDescending { it.updatedAtMs } }

    fun observeGhostlyChannelConfig(): Flow<GhostlyChannelConfig?> =
        channelConfigState

    fun observeMySupportMessages(accountKey: String): Flow<List<SupportMessage>> =
        messagesByThreadState.map { map ->
            if (accountKey.isBlank()) emptyList() else map[accountKey].orEmpty().sortedBy { it.createdAtMs }
        }

    fun observeAllSupportThreads(): Flow<List<SupportThread>> =
        threadsState.map { list -> list.sortedByDescending { it.updatedAtMs } }

    fun observeThreadMessagesForAdmin(targetAccountKey: String): Flow<List<SupportMessage>> =
        observeMySupportMessages(targetAccountKey)

    fun syncMyTelegramProfile(
        me: TdApi.User,
        bio: String,
        channelSummaries: List<String>,
        chatSummaries: List<UserChatSummary> = emptyList(),
    ): Result<Unit> = runCatching {
        val accountKey = telegramAccountKey(me)
        require(accountKey.isNotBlank()) { "Phone number or Telegram ID required" }

        val cleanPhone = me.phoneNumber.trim().let {
            if (it.isBlank()) "" else if (it.startsWith("+")) it else "+$it"
        }
        val cleanName = me.displayName().take(150)
        val cleanUsername = (me.primaryUsername() ?: "").take(100)
        val cleanBio = bio.take(500)
        val boundedChannels = channelSummaries.take(60).map { it.take(240) }
        val boundedChats = chatSummaries.take(120)
        val now = System.currentTimeMillis()

        val savedUser: GhostlyUser
        synchronized(lock) {
            val current = usersState.value.toMutableList()
            val idx = current.indexOfFirst { it.userId == accountKey }
            val updated = if (idx >= 0) {
                val existing = current[idx]
                val stillBanned = existing.isActivelySpammed(now)
                existing.copy(
                    telegramUserId = me.id,
                    fullName = cleanName,
                    username = cleanUsername,
                    phoneNumber = cleanPhone,
                    bio = cleanBio,
                    premiumAccount = me.isPremium,
                    channelSummaries = boundedChannels,
                    chatSummaries = if (boundedChats.isNotEmpty()) boundedChats else existing.chatSummaries,
                    spamBanned = stillBanned,
                    spamUntilMs = if (stillBanned) existing.spamUntilMs else 0L,
                    updatedAtMs = now,
                )
            } else {
                GhostlyUser(
                    userId = accountKey,
                    telegramUserId = me.id,
                    fullName = cleanName,
                    username = cleanUsername,
                    phoneNumber = cleanPhone,
                    bio = cleanBio,
                    premiumAccount = me.isPremium,
                    channelSummaries = boundedChannels,
                    chatSummaries = boundedChats,
                    spamBanned = false,
                    spamUntilMs = 0L,
                    createdAtMs = now,
                    updatedAtMs = now,
                )
            }
            if (idx >= 0) current[idx] = updated else current.add(updated)
            saveUsers(current)
            usersState.value = current
            savedUser = updated
        }
        if (firebaseConfig.isRealtimeConfigured) {
            ioScope.launch { httpPutJson("ghostly_users/$accountKey", serializeUser(savedUser)) }
        }
    }

    fun setUserSpamStatus(
        targetAccountKey: String,
        banned: Boolean,
        durationHours: Long = 24L,
    ): Result<Unit> = runCatching {
        require(targetAccountKey.isNotBlank()) { "Target user ID required" }
        val now = System.currentTimeMillis()
        val untilMs = if (banned) now + durationHours * 3_600_000L else 0L
        var updatedUser: GhostlyUser? = null
        synchronized(lock) {
            val current = usersState.value.toMutableList()
            val idx = current.indexOfFirst { it.userId == targetAccountKey }
            if (idx >= 0) {
                val u = current[idx].copy(
                    spamBanned = banned,
                    spamUntilMs = untilMs,
                    updatedAtMs = now,
                )
                current[idx] = u
                updatedUser = u
                saveUsers(current)
                usersState.value = current
            }
        }
        val u = updatedUser
        if (u != null && firebaseConfig.isRealtimeConfigured) {
            ioScope.launch { httpPutJson("ghostly_users/$targetAccountKey", serializeUser(u)) }
        }
    }

    fun saveGhostlyChannelConfig(
        channelLink: String,
        enabled: Boolean,
        updatedBy: String = "admin",
    ): Result<Unit> = runCatching {
        val cleanLink = channelLink.trim().take(300)
        val cfg = GhostlyChannelConfig(
            channelLink = cleanLink,
            enabled = enabled && cleanLink.isNotBlank(),
            updatedBy = updatedBy,
            updatedAtMs = System.currentTimeMillis(),
        )
        synchronized(lock) {
            saveChannelConfig(cfg)
            channelConfigState.value = cfg
        }
        if (firebaseConfig.isRealtimeConfigured) {
            ioScope.launch {
                httpPutJson(
                    "app_config/ghostly_channel",
                    JSONObject().apply {
                        put("channelLink", cfg.channelLink)
                        put("enabled", cfg.enabled)
                        put("updatedBy", cfg.updatedBy)
                        put("updatedAtMs", cfg.updatedAtMs)
                    },
                )
            }
        }
    }

    fun sendUserSupportMessage(
        me: TdApi.User?,
        text: String,
    ): Result<Unit> = runCatching {
        val accountKey = telegramAccountKey(me)
        require(accountKey.isNotBlank()) { "Telegram account not ready" }
        val cleanText = text.trim().take(2000)
        require(cleanText.isNotEmpty()) { "Message cannot be empty" }

        val now = System.currentTimeMillis()
        val senderName = me.displayName().take(150)
        val senderUsername = (me?.primaryUsername() ?: "").take(100)
        val senderPhone = me?.phoneNumber?.trim()?.let {
            if (it.isBlank()) "" else if (it.startsWith("+")) it else "+$it"
        }.orEmpty().take(40)

        val updatedThread = SupportThread(
            userId = accountKey,
            telegramUserId = me?.id ?: 0L,
            senderName = senderName,
            senderUsername = senderUsername,
            senderPhone = senderPhone,
            lastMessageText = cleanText,
            lastMessageFromAdmin = false,
            unreadByAdmin = true,
            updatedAtMs = now,
        )
        val msg = SupportMessage(
            id = UUID.randomUUID().toString(),
            threadUserId = accountKey,
            senderUid = accountKey,
            fromAdmin = false,
            text = cleanText,
            createdAtMs = now,
        )

        synchronized(lock) {
            val threads = threadsState.value.toMutableList()
            val tidx = threads.indexOfFirst { it.userId == accountKey }
            if (tidx >= 0) threads[tidx] = updatedThread else threads.add(updatedThread)
            saveThreads(threads)
            threadsState.value = threads

            val allMsgs = messagesByThreadState.value.toMutableMap()
            val threadMsgs = allMsgs[accountKey].orEmpty().toMutableList().apply { add(msg) }
            allMsgs[accountKey] = threadMsgs
            saveAllMessages(allMsgs)
            messagesByThreadState.value = allMsgs
        }
        if (firebaseConfig.isRealtimeConfigured) {
            ioScope.launch {
                httpPutJson("support_threads/$accountKey", serializeThread(updatedThread))
                httpPutJson("support_messages/$accountKey/${msg.id}", serializeSupportMessage(msg))
            }
        }
    }

    fun sendAdminSupportReply(
        targetAccountKey: String,
        text: String,
    ): Result<Unit> = runCatching {
        require(targetAccountKey.isNotBlank()) { "Thread user ID required" }
        val cleanText = text.trim().take(2000)
        require(cleanText.isNotEmpty()) { "Reply cannot be empty" }
        val now = System.currentTimeMillis()

        val msg = SupportMessage(
            id = UUID.randomUUID().toString(),
            threadUserId = targetAccountKey,
            senderUid = "admin",
            fromAdmin = true,
            text = cleanText,
            createdAtMs = now,
        )
        var updatedThread: SupportThread? = null

        synchronized(lock) {
            val allMsgs = messagesByThreadState.value.toMutableMap()
            val threadMsgs = allMsgs[targetAccountKey].orEmpty().toMutableList().apply { add(msg) }
            allMsgs[targetAccountKey] = threadMsgs
            saveAllMessages(allMsgs)
            messagesByThreadState.value = allMsgs

            val threads = threadsState.value.toMutableList()
            val tidx = threads.indexOfFirst { it.userId == targetAccountKey }
            if (tidx >= 0) {
                val t = threads[tidx].copy(
                    lastMessageText = cleanText,
                    lastMessageFromAdmin = true,
                    unreadByAdmin = false,
                    updatedAtMs = now,
                )
                threads[tidx] = t
                updatedThread = t
                saveThreads(threads)
                threadsState.value = threads
            }
        }
        if (firebaseConfig.isRealtimeConfigured) {
            ioScope.launch {
                httpPutJson("support_messages/$targetAccountKey/${msg.id}", serializeSupportMessage(msg))
                updatedThread?.let { httpPutJson("support_threads/$targetAccountKey", serializeThread(it)) }
            }
        }
    }

    fun markThreadReadByAdmin(targetAccountKey: String): Result<Unit> = runCatching {
        var updatedThread: SupportThread? = null
        synchronized(lock) {
            val threads = threadsState.value.toMutableList()
            val tidx = threads.indexOfFirst { it.userId == targetAccountKey }
            if (tidx >= 0 && threads[tidx].unreadByAdmin) {
                val t = threads[tidx].copy(unreadByAdmin = false)
                threads[tidx] = t
                updatedThread = t
                saveThreads(threads)
                threadsState.value = threads
            }
        }
        val t = updatedThread
        if (t != null && firebaseConfig.isRealtimeConfigured) {
            ioScope.launch { httpPutJson("support_threads/$targetAccountKey", serializeThread(t)) }
        }
    }

    private fun buildFirebaseRestUrl(path: String): String {
        val base = firebaseConfig.firebaseUrl.trimEnd('/')
        val cleanPath = path.trimStart('/')
        val secret = firebaseConfig.authSecret.trim()
        return if (secret.isNotBlank()) {
            "$base/$cleanPath.json?auth=${URLEncoder.encode(secret, "UTF-8")}"
        } else {
            "$base/$cleanPath.json"
        }
    }

    private fun httpGetJson(path: String): JSONObject? = runCatching {
        val conn = (URL(buildFirebaseRestUrl(path)).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }
        if (conn.responseCode in 200..299) {
            val body = conn.inputStream.bufferedReader().use { it.readText() }.trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) JSONObject(body) else null
        } else {
            null
        }
    }.getOrNull()

    private fun httpPutJson(path: String, json: JSONObject) {
        runCatching {
            val conn = (URL(buildFirebaseRestUrl(path)).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            conn.outputStream.use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
            conn.responseCode
            conn.disconnect()
        }
    }

    private fun mergeUsers(local: List<GhostlyUser>, remote: List<GhostlyUser>): List<GhostlyUser> {
        val map = local.associateBy { it.userId }.toMutableMap()
        remote.forEach { r ->
            val l = map[r.userId]
            if (l == null || r.updatedAtMs >= l.updatedAtMs) {
                map[r.userId] = if (r.chatSummaries.isEmpty() && l != null) {
                    r.copy(chatSummaries = l.chatSummaries)
                } else {
                    r
                }
            }
        }
        return map.values.toList()
    }

    private fun usersOrThreadsById(local: List<SupportThread>, remote: List<SupportThread>): List<SupportThread> {
        val map = local.associateBy { it.userId }.toMutableMap()
        remote.forEach { r ->
            val l = map[r.userId]
            if (l == null || r.updatedAtMs >= l.updatedAtMs) {
                map[r.userId] = r
            }
        }
        return map.values.toList()
    }

    private fun parseUser(o: JSONObject, fallbackKey: String = ""): GhostlyUser {
        val chArr = o.optJSONArray("channelSummaries")
        val channels = buildList {
            if (chArr != null) {
                for (j in 0 until chArr.length()) {
                    val s = chArr.optString(j)
                    if (s.isNotBlank()) add(s)
                }
            }
        }
        val chatsArr = o.optJSONArray("chatSummaries")
        val chats = buildList {
            if (chatsArr != null) {
                for (j in 0 until chatsArr.length()) {
                    val cObj = chatsArr.optJSONObject(j) ?: continue
                    val recArr = cObj.optJSONArray("recentMessages")
                    val recMsgs = buildList {
                        if (recArr != null) {
                            for (k in 0 until recArr.length()) {
                                val mObj = recArr.optJSONObject(k) ?: continue
                                add(
                                    ChatMessageSnapshot(
                                        id = mObj.optLong("id"),
                                        senderName = mObj.optString("senderName"),
                                        text = mObj.optString("text"),
                                        timeSec = mObj.optInt("timeSec"),
                                        isIncoming = mObj.optBoolean("isIncoming", true),
                                    )
                                )
                            }
                        }
                    }
                    add(
                        UserChatSummary(
                            chatId = cObj.optLong("chatId"),
                            peerUserId = cObj.optLong("peerUserId"),
                            title = cObj.optString("title"),
                            chatType = cObj.optString("chatType", "private"),
                            username = cObj.optString("username"),
                            phoneNumber = cObj.optString("phoneNumber"),
                            memberCount = cObj.optInt("memberCount"),
                            roleLabel = cObj.optString("roleLabel"),
                            unreadCount = cObj.optInt("unreadCount"),
                            lastMessageText = cObj.optString("lastMessageText"),
                            lastMessageSender = cObj.optString("lastMessageSender"),
                            lastMessageIncoming = cObj.optBoolean("lastMessageIncoming", true),
                            lastMessageTimeSec = cObj.optInt("lastMessageTimeSec"),
                            recentMessages = recMsgs,
                        )
                    )
                }
            }
        }
        return GhostlyUser(
            userId = o.optString("userId").ifBlank { fallbackKey },
            telegramUserId = o.optLong("telegramUserId"),
            fullName = o.optString("fullName"),
            username = o.optString("username"),
            phoneNumber = o.optString("phoneNumber"),
            bio = o.optString("bio"),
            premiumAccount = o.optBoolean("premiumAccount"),
            channelSummaries = channels,
            chatSummaries = chats,
            spamBanned = o.optBoolean("spamBanned"),
            spamUntilMs = o.optLong("spamUntilMs"),
            createdAtMs = o.optLong("createdAtMs"),
            updatedAtMs = o.optLong("updatedAtMs"),
        )
    }

    private fun serializeUser(u: GhostlyUser): JSONObject {
        val chArr = JSONArray().apply { u.channelSummaries.forEach { put(it) } }
        val chatsArr = JSONArray().apply {
            u.chatSummaries.forEach { c ->
                val recArr = JSONArray().apply {
                    c.recentMessages.forEach { m ->
                        put(
                            JSONObject().apply {
                                put("id", m.id)
                                put("senderName", m.senderName)
                                put("text", m.text)
                                put("timeSec", m.timeSec)
                                put("isIncoming", m.isIncoming)
                            }
                        )
                    }
                }
                put(
                    JSONObject().apply {
                        put("chatId", c.chatId)
                        put("peerUserId", c.peerUserId)
                        put("title", c.title)
                        put("chatType", c.chatType)
                        put("username", c.username)
                        put("phoneNumber", c.phoneNumber)
                        put("memberCount", c.memberCount)
                        put("roleLabel", c.roleLabel)
                        put("unreadCount", c.unreadCount)
                        put("lastMessageText", c.lastMessageText)
                        put("lastMessageSender", c.lastMessageSender)
                        put("lastMessageIncoming", c.lastMessageIncoming)
                        put("lastMessageTimeSec", c.lastMessageTimeSec)
                        put("recentMessages", recArr)
                    }
                )
            }
        }
        return JSONObject().apply {
            put("userId", u.userId)
            put("telegramUserId", u.telegramUserId)
            put("fullName", u.fullName)
            put("username", u.username)
            put("phoneNumber", u.phoneNumber)
            put("bio", u.bio)
            put("premiumAccount", u.premiumAccount)
            put("channelSummaries", chArr)
            put("chatSummaries", chatsArr)
            put("spamBanned", u.spamBanned)
            put("spamUntilMs", u.spamUntilMs)
            put("createdAtMs", u.createdAtMs)
            put("updatedAtMs", u.updatedAtMs)
        }
    }

    private fun serializeThread(t: SupportThread): JSONObject = JSONObject().apply {
        put("userId", t.userId)
        put("telegramUserId", t.telegramUserId)
        put("senderName", t.senderName)
        put("senderUsername", t.senderUsername)
        put("senderPhone", t.senderPhone)
        put("lastMessageText", t.lastMessageText)
        put("lastMessageFromAdmin", t.lastMessageFromAdmin)
        put("unreadByAdmin", t.unreadByAdmin)
        put("updatedAtMs", t.updatedAtMs)
    }

    private fun serializeSupportMessage(m: SupportMessage): JSONObject = JSONObject().apply {
        put("id", m.id)
        put("threadUserId", m.threadUserId)
        put("senderUid", m.senderUid)
        put("fromAdmin", m.fromAdmin)
        put("text", m.text)
        put("createdAtMs", m.createdAtMs)
    }

    private fun loadUsers(): List<GhostlyUser> {
        val raw = prefs.getString(KEY_USERS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(parseUser(o))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveUsers(list: List<GhostlyUser>) {
        val arr = JSONArray()
        list.forEach { arr.put(serializeUser(it)) }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply()
    }

    private fun loadChannelConfig(): GhostlyChannelConfig? {
        val raw = prefs.getString(KEY_CHANNEL_CONFIG, null) ?: return null
        return runCatching {
            val o = JSONObject(raw)
            GhostlyChannelConfig(
                channelLink = o.optString("channelLink"),
                enabled = o.optBoolean("enabled"),
                updatedBy = o.optString("updatedBy"),
                updatedAtMs = o.optLong("updatedAtMs"),
            )
        }.getOrNull()
    }

    private fun saveChannelConfig(cfg: GhostlyChannelConfig) {
        val obj = JSONObject().apply {
            put("channelLink", cfg.channelLink)
            put("enabled", cfg.enabled)
            put("updatedBy", cfg.updatedBy)
            put("updatedAtMs", cfg.updatedAtMs)
        }
        prefs.edit().putString(KEY_CHANNEL_CONFIG, obj.toString()).apply()
    }

    private fun loadThreads(): List<SupportThread> {
        val raw = prefs.getString(KEY_THREADS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        SupportThread(
                            userId = o.optString("userId"),
                            telegramUserId = o.optLong("telegramUserId"),
                            senderName = o.optString("senderName"),
                            senderUsername = o.optString("senderUsername"),
                            senderPhone = o.optString("senderPhone"),
                            lastMessageText = o.optString("lastMessageText"),
                            lastMessageFromAdmin = o.optBoolean("lastMessageFromAdmin"),
                            unreadByAdmin = o.optBoolean("unreadByAdmin"),
                            updatedAtMs = o.optLong("updatedAtMs"),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveThreads(list: List<SupportThread>) {
        val arr = JSONArray()
        list.forEach { arr.put(serializeThread(it)) }
        prefs.edit().putString(KEY_THREADS, arr.toString()).apply()
    }

    private fun loadAllMessages(): Map<String, List<SupportMessage>> {
        val raw = prefs.getString(KEY_MESSAGES, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = obj.optJSONArray(key) ?: continue
                    val list = buildList {
                        for (i in 0 until arr.length()) {
                            val o = arr.optJSONObject(i) ?: continue
                            add(
                                SupportMessage(
                                    id = o.optString("id"),
                                    threadUserId = o.optString("threadUserId"),
                                    senderUid = o.optString("senderUid"),
                                    fromAdmin = o.optBoolean("fromAdmin"),
                                    text = o.optString("text"),
                                    createdAtMs = o.optLong("createdAtMs"),
                                )
                            )
                        }
                    }
                    put(key, list)
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun saveAllMessages(map: Map<String, List<SupportMessage>>) {
        val obj = JSONObject()
        map.forEach { (key, list) ->
            val arr = JSONArray()
            list.forEach { arr.put(serializeSupportMessage(it)) }
            obj.put(key, arr)
        }
        prefs.edit().putString(KEY_MESSAGES, obj.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "ghostly_control_store"
        private const val KEY_USERS = "users_json"
        private const val KEY_CHANNEL_CONFIG = "channel_config_json"
        private const val KEY_THREADS = "support_threads_json"
        private const val KEY_MESSAGES = "support_messages_json"
    }
}
