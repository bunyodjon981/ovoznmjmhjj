package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WebAssetOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.LocalStrings
import com.example.ui.theme.LocalTelegramColors
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramWebAppDialog(
    title: String,
    subtitle: String,
    url: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current
    var loadProgress by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var rendererCrashed by remember { mutableStateOf(false) }
    var retryKey by remember { mutableIntStateOf(0) }

    DisposableEffect(retryKey) {
        onDispose {
            webViewRef?.let { wv ->
                runCatching {
                    wv.stopLoading()
                    wv.removeJavascriptInterface("TelegramWebviewProxy")
                    wv.loadUrl("about:blank")
                    wv.clearHistory()
                    (wv.parent as? ViewGroup)?.removeView(wv)
                    wv.removeAllViews()
                    wv.destroy()
                }
            }
            webViewRef = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = tgColors.headerBackground,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = tgColors.cardBackground,
                        titleContentColor = tgColors.headerContent,
                        navigationIconContentColor = tgColors.headerContent,
                        actionIconContentColor = tgColors.headerContent,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("webapp_close_button"),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.webAppClose)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(strings.reload) },
                                    onClick = {
                                        showMenu = false
                                        if (rendererCrashed) {
                                            rendererCrashed = false
                                            retryKey++
                                        } else {
                                            webViewRef?.reload()
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.openInBrowser) },
                                    onClick = {
                                        showMenu = false
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                )
                            }
                        }
                    },
                )

                if (loadProgress in 1..99 && !rendererCrashed) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        color = tgColors.accent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tgColors.chatBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    if (rendererCrashed) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(tgColors.cardBackground)
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WebAssetOff,
                                contentDescription = null,
                                tint = tgColors.accent,
                                modifier = Modifier.size(44.dp),
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        rendererCrashed = false
                                        retryKey++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = tgColors.accent),
                                ) {
                                    Icon(Icons.Rounded.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(" ${strings.reload}", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                ) {
                                    Text(strings.openInBrowser)
                                }
                            }
                        }
                    } else {
                        key(retryKey) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        webViewRef = this
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                                        }
                                        CookieManager.getInstance().setAcceptCookie(true)
                                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.databaseEnabled = true
                                        settings.useWideViewPort = true
                                        settings.loadWithOverviewMode = true
                                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                        settings.mediaPlaybackRequiresUserGesture = false

                                        addJavascriptInterface(
                                            object {
                                                @JavascriptInterface
                                                fun postEvent(eventType: String, eventData: String?) {
                                                    when (eventType) {
                                                        "web_app_close" -> post { onDismiss() }
                                                        "web_app_open_link" -> {
                                                            val targetUrl = runCatching {
                                                                JSONObject(eventData ?: "{}").optString("url")
                                                            }.getOrNull()
                                                            if (!targetUrl.isNullOrBlank()) {
                                                                post {
                                                                    runCatching {
                                                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            "TelegramWebviewProxy",
                                        )

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                loadProgress = newProgress
                                            }
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                            ): Boolean {
                                                val reqUrl = request?.url?.toString() ?: return false
                                                if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                                    return false
                                                }
                                                runCatching {
                                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl)))
                                                }
                                                return true
                                            }

                                            @RequiresApi(Build.VERSION_CODES.O)
                                            override fun onRenderProcessGone(
                                                view: WebView?,
                                                detail: RenderProcessGoneDetail?,
                                            ): Boolean {
                                                runCatching {
                                                    (view?.parent as? ViewGroup)?.removeView(view)
                                                    view?.destroy()
                                                }
                                                if (webViewRef === view) {
                                                    webViewRef = null
                                                }
                                                loadProgress = 0
                                                rendererCrashed = true
                                                return true
                                            }
                                        }

                                        loadUrl(url)
                                    }
                                },
                                update = { view ->
                                    if (!rendererCrashed && view.url != url && view.url.isNullOrBlank()) {
                                        view.loadUrl(url)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
