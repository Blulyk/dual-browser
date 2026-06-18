package com.blulyk.dualbrowser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.blulyk.dualbrowser.domain.BrowserTab
import com.blulyk.dualbrowser.engine.WebViewBrowserEngine
import com.blulyk.dualbrowser.engine.WebViewCallbacks
import com.blulyk.dualbrowser.engine.WebViewFactory
import kotlinx.coroutines.flow.Flow

@Composable
fun WebSurface(
    tab: BrowserTab,
    engineActions: Flow<EngineAction>,
    modifier: Modifier = Modifier,
) {
    var engine by remember(tab.id) { mutableStateOf<WebViewBrowserEngine?>(null) }

    AndroidView(
        factory = { context ->
            WebViewFactory().create(context, WebViewCallbacks.None).also {
                engine = it
                it.load(tab.url)
            }.view
        },
        update = { webView ->
            if (webView.url != tab.url) webView.loadUrl(tab.url)
        },
        modifier = modifier.fillMaxSize(),
    )

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
        }
    }
}

