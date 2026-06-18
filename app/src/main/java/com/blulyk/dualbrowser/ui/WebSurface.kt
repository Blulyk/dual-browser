package com.blulyk.dualbrowser.ui

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.blulyk.dualbrowser.domain.BrowserTab
import com.blulyk.dualbrowser.engine.WebViewBrowserEngine
import com.blulyk.dualbrowser.engine.WebViewCallbacks
import com.blulyk.dualbrowser.engine.WebViewFactory
import com.blulyk.dualbrowser.platform.DownloadHandler
import com.blulyk.dualbrowser.platform.DownloadSpec
import com.blulyk.dualbrowser.platform.ExternalIntentHandler
import com.blulyk.dualbrowser.platform.ExternalResult
import com.blulyk.dualbrowser.platform.WebPermissionPolicy
import kotlinx.coroutines.flow.Flow

@Composable
fun WebSurface(
    tab: BrowserTab,
    engineActions: Flow<EngineAction>,
    modifier: Modifier = Modifier,
    onRendererGone: () -> Unit = {},
    onPageFinished: (url: String, title: String) -> Unit = { _, _ -> },
    onPopupRequested: (url: String) -> Unit = {},
) {
    val context = LocalContext.current
    var engine by remember(tab.id) { mutableStateOf<WebViewBrowserEngine?>(null) }
    var fileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var permissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var geolocationRequest by remember {
        mutableStateOf<Pair<String, GeolocationPermissions.Callback>?>(null)
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        fileCallback?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data),
        )
        fileCallback = null
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        permissionRequest?.let { request ->
            if (grants.isNotEmpty() && grants.values.all { it }) {
                request.grant(request.resources)
            } else {
                request.deny()
            }
        }
        permissionRequest = null
    }
    val geolocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        geolocationRequest?.let { (origin, callback) -> callback.invoke(origin, granted, false) }
        geolocationRequest = null
    }
    val hideCustomView = {
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }
    val callbacks = remember(tab.id, context) {
        val externalIntents = ExternalIntentHandler(context)
        val downloads = DownloadHandler(context)
        var latestTitle = tab.title
        object : WebViewCallbacks {
            override fun onTitleChanged(title: String) {
                latestTitle = title
            }

            override fun onPageFinished(url: String) {
                onPageFinished(url, latestTitle)
            }

            override fun shouldOverrideUrl(uri: Uri): Boolean = when (val result = externalIntents.open(uri)) {
                ExternalResult.NotExternal -> false
                ExternalResult.Opened -> true
                is ExternalResult.NoHandler -> {
                    Toast.makeText(context, "No app can open ${result.scheme}", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            override fun onDownloadRequested(spec: DownloadSpec) {
                runCatching { downloads.enqueue(spec) }
                    .onSuccess { Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show() }
                    .onFailure { Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show() }
            }

            override fun onShowFileChooser(
                callback: ValueCallback<Array<Uri>>,
                params: WebChromeClient.FileChooserParams,
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return runCatching {
                    fileLauncher.launch(params.createIntent())
                    true
                }.getOrElse {
                    fileCallback = null
                    callback.onReceiveValue(null)
                    false
                }
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                val permissions = WebPermissionPolicy.androidPermissions(request.resources)
                if (permissions.isEmpty()) {
                    request.deny()
                } else {
                    permissionRequest?.deny()
                    permissionRequest = request
                    permissionLauncher.launch(permissions.toTypedArray())
                }
            }

            override fun onShowCustomView(
                view: View,
                callback: WebChromeClient.CustomViewCallback,
            ) {
                customViewCallback?.onCustomViewHidden()
                customView = view
                customViewCallback = callback
            }

            override fun onHideCustomView() = hideCustomView()

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback,
            ) {
                geolocationRequest?.let { (oldOrigin, oldCallback) ->
                    oldCallback.invoke(oldOrigin, false, false)
                }
                geolocationRequest = origin to callback
                geolocationLauncher.launch(WebPermissionPolicy.geolocationPermission)
            }

            override fun onRendererGone() = onRendererGone()

            override fun onPopupRequested(url: String) = onPopupRequested(url)
        }
    }

    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { webContext ->
                WebViewFactory().create(webContext, callbacks).also {
                    engine = it
                    it.load(tab.url)
                }.view
            },
            update = { webView ->
                if (webView.url != tab.url) webView.loadUrl(tab.url)
            },
            modifier = Modifier.fillMaxSize(),
        )
        customView?.let { fullscreenView ->
            AndroidView(
                factory = {
                    (fullscreenView.parent as? ViewGroup)?.removeView(fullscreenView)
                    fullscreenView
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    BackHandler(enabled = customView != null) { hideCustomView() }

    LaunchedEffect(engine, engineActions) {
        engineActions.collect { action ->
            when (action) {
                EngineAction.Back -> engine?.back()
                EngineAction.Forward -> engine?.forward()
                EngineAction.Reload -> engine?.reload()
                EngineAction.Stop -> engine?.stop()
            }
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            engine?.destroy()
            engine = null
            fileCallback?.onReceiveValue(null)
            permissionRequest?.deny()
            geolocationRequest?.let { (origin, callback) -> callback.invoke(origin, false, false) }
            hideCustomView()
        }
    }
}
