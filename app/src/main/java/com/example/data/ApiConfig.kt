package com.example.data

import android.content.Context
import org.json.JSONObject

data class TelegramApiConfig(
    val apiId: Int,
    val apiHash: String,
)

data class FirebaseClientConfig(
    val firebaseUrl: String = "",
    val projectId: String = "",
    val apiKey: String = "",
    val appId: String = "",
    val messagingSenderId: String = "",
    val storageBucket: String = "",
    val authSecret: String = "",
) {
    val isRealtimeConfigured: Boolean
        get() = firebaseUrl.isNotBlank() &&
            !firebaseUrl.contains("YOUR_", ignoreCase = true) &&
            (firebaseUrl.startsWith("https://") || firebaseUrl.startsWith("http://"))
}

object ApiConfigLoader {
    fun load(context: Context): TelegramApiConfig {
        val jsonText = context.assets.open("api.json").bufferedReader().use { it.readText() }
        val json = JSONObject(jsonText)
        val apiId = json.getInt("api_id")
        val apiHash = json.getString("api_hash").trim()
        require(apiId > 0 && apiHash.isNotEmpty()) {
            "Invalid api.json credentials"
        }
        return TelegramApiConfig(apiId = apiId, apiHash = apiHash)
    }
}

object FirebaseConfigLoader {
    fun load(context: Context): FirebaseClientConfig {
        return runCatching {
            val jsonText = context.assets.open("firebase.json").bufferedReader().use { it.readText() }
            val json = JSONObject(jsonText)
            val projectInfo = json.optJSONObject("project_info")

            fun pick(vararg keys: String): String {
                for (k in keys) {
                    val direct = json.optString(k, "").trim()
                    if (direct.isNotEmpty()) return direct
                    val nested = projectInfo?.optString(k, "")?.trim().orEmpty()
                    if (nested.isNotEmpty()) return nested
                }
                return ""
            }

            val firstClient = json.optJSONArray("client")?.optJSONObject(0)
            val clientInfo = firstClient?.optJSONObject("client_info")
            val apiKeyArr = firstClient?.optJSONArray("api_key")
            val fallbackApiKey = apiKeyArr?.optJSONObject(0)?.optString("current_key", "")?.trim().orEmpty()
            val fallbackAppId = clientInfo?.optString("mobilesdk_app_id", "")?.trim().orEmpty()

            FirebaseClientConfig(
                firebaseUrl = pick("firebase_url", "databaseURL", "database_url").trimEnd('/'),
                projectId = pick("project_id", "projectId"),
                apiKey = pick("api_key", "apiKey").ifBlank { fallbackApiKey },
                appId = pick("app_id", "appId").ifBlank { fallbackAppId },
                messagingSenderId = pick("messaging_sender_id", "messagingSenderId"),
                storageBucket = pick("storage_bucket", "storageBucket"),
                authSecret = pick("auth_secret", "authSecret", "database_secret"),
            )
        }.getOrDefault(FirebaseClientConfig())
    }
}
