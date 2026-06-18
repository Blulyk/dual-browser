package com.blulyk.dualbrowser.engine

import android.webkit.WebSettings
import android.webkit.WebView

class WebViewBrowserEngine(
    val view: WebView,
) : BrowserEngine {
    private val mobileUserAgent = view.settings.userAgentString

    override fun load(url: String) = view.loadUrl(url)

    override fun back(): Boolean = view.canGoBack().also { canGoBack ->
        if (canGoBack) view.goBack()
    }

    override fun forward(): Boolean = view.canGoForward().also { canGoForward ->
        if (canGoForward) view.goForward()
    }

    override fun reload() = view.reload()

    override fun stop() = view.stopLoading()

    override fun find(query: String) {
        view.findAllAsync(query)
    }

    override fun setDesktopMode(enabled: Boolean) {
        view.settings.userAgentString = if (enabled) desktopUserAgent(mobileUserAgent) else mobileUserAgent
        view.settings.useWideViewPort = enabled
        view.settings.loadWithOverviewMode = enabled
        view.reload()
    }

    override fun destroy() {
        view.stopLoading()
        view.webChromeClient = null
        view.webViewClient = android.webkit.WebViewClient()
        view.destroy()
    }

    private fun desktopUserAgent(userAgent: String): String = userAgent
        .replace("; wv", "")
        .replace(" Mobile ", " ")
        .replace(" Version/4.0", "")
        .ifBlank { WebSettings.getDefaultUserAgent(view.context) }
}

