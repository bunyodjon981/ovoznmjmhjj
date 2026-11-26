package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SavedAccount(
    val accountId: String,
    val userId: Long = 0L,
    val displayName: String = "",
    val phone: String = "",
    val avatarLocalPath: String? = null,
    val dbDirName: String,
    val filesDirName: String,
)

class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("gramx_accounts_prefs", Context.MODE_PRIVATE)

    private val _accounts = MutableStateFlow<List<SavedAccount>>(emptyList())
    val accounts: StateFlow<List<SavedAccount>> = _accounts.asStateFlow()

    private val _activeAccountId = MutableStateFlow(DEFAULT_ACCOUNT_ID)
    val activeAccountId: StateFlow<String> = _activeAccountId.asStateFlow()

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null)
        val loaded = mutableListOf<SavedAccount>()
        if (!raw.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    loaded.add(
                        SavedAccount(
                            accountId = obj.getString("accountId"),
                            userId = obj.optLong("userId", 0L),
                            displayName = obj.optString("displayName", ""),
                            phone = obj.optString("phone", ""),
                            avatarLocalPath = obj.optString("avatarLocalPath", "").takeIf { it.isNotBlank() },
                            dbDirName = obj.getString("dbDirName"),
                            filesDirName = obj.getString("filesDirName"),
                        )
                    )
                }
            }
        }
        if (loaded.isEmpty()) {
            loaded.add(
                SavedAccount(
                    accountId = DEFAULT_ACCOUNT_ID,
                    dbDirName = "tdlib_db",
                    filesDirName = "tdlib_files",
                )
            )
        }
        val active = prefs.getString(KEY_ACTIVE_ACCOUNT_ID, null)
            ?.takeIf { id -> loaded.any { it.accountId == id } }
            ?: loaded.first().accountId

        _accounts.value = loaded
        _activeAccountId.value = active
        saveToPrefs(loaded, active)
    }

    @Synchronized
    fun getActiveAccount(): SavedAccount {
        val currentId = _activeAccountId.value
        return _accounts.value.firstOrNull { it.accountId == currentId } ?: _accounts.value.first()
    }

    @Synchronized
    fun updateActiveUser(userId: Long, displayName: String, phone: String, avatarLocalPath: String?) {
        val activeId = _activeAccountId.value
        val updated = _accounts.value.map { acc ->
            if (acc.accountId == activeId) {
                acc.copy(
                    userId = userId,
                    displayName = displayName,
                    phone = phone,
                    avatarLocalPath = avatarLocalPath ?: acc.avatarLocalPath,
                )
            } else acc
        }
        _accounts.value = updated
        saveToPrefs(updated, activeId)
    }

    @Synchronized
    fun createAndActivateNewAccount(): SavedAccount {
        val suffix = System.currentTimeMillis().toString()
        val newAcc = SavedAccount(
            accountId = "acc_$suffix",
            dbDirName = "tdlib_db_$suffix",
            filesDirName = "tdlib_files_$suffix",
        )
        val updated = _accounts.value + newAcc
        _accounts.value = updated
        _activeAccountId.value = newAcc.accountId
        saveToPrefs(updated, newAcc.accountId)
        return newAcc
    }

    @Synchronized
    fun setActiveAccount(accountId: String): SavedAccount? {
        val target = _accounts.value.firstOrNull { it.accountId == accountId } ?: return null
        _activeAccountId.value = accountId
        saveToPrefs(_accounts.value, accountId)
        return target
    }

    @Synchronized
    fun removeAccount(accountId: String): SavedAccount? {
        if (_accounts.value.size <= 1) return null
        val updated = _accounts.value.filterNot { it.accountId == accountId }
        val nextActive = if (_activeAccountId.value == accountId) updated.first().accountId else _activeAccountId.value
        _accounts.value = updated
        _activeAccountId.value = nextActive
        saveToPrefs(updated, nextActive)
        return updated.firstOrNull { it.accountId == nextActive }
    }

    private fun saveToPrefs(list: List<SavedAccount>, activeId: String) {
        val arr = JSONArray()
        list.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("accountId", acc.accountId)
                    put("userId", acc.userId)
                    put("displayName", acc.displayName)
                    put("phone", acc.phone)
                    put("avatarLocalPath", acc.avatarLocalPath ?: "")
                    put("dbDirName", acc.dbDirName)
                    put("filesDirName", acc.filesDirName)
                }
            )
        }
        prefs.edit()
            .putString(KEY_ACCOUNTS_JSON, arr.toString())
            .putString(KEY_ACTIVE_ACCOUNT_ID, activeId)
            .apply()
    }

    companion object {
        const val DEFAULT_ACCOUNT_ID = "acc_default"
        private const val KEY_ACCOUNTS_JSON = "accounts_json"
        private const val KEY_ACTIVE_ACCOUNT_ID = "active_account_id"
    }
}
