package com.blulyk.dualbrowser.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewFactory {
    fun create(context: Context, callbacks: WebViewCallbacks): WebViewBrowserEngine {
        val webView = WebView(context)
        configureSettings(webView.settings)
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        webView.webViewClient = BrowserWebViewClient(callbacks)
        webView.webChromeClient = BrowserWebChromeClient(callbacks)
        return WebViewBrowserEngine(webView)
    }

    private fun configureSettings(settings: WebSettings) = with(settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        safeBrowsingEnabled = true
        allowFileAccess = false
        allowContentAccess = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        mediaPlaybackRequiresUserGesture = false
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        setSupportMultipleWindows(true)
        javaScriptCanOpenWindowsAutomatically = false
    }

    private class BrowserWebViewClient(
        private val callbacks: WebViewCallbacks,
    ) : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            callbacks.onPageStarted(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            callbacks.onPageFinished(url)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.cancel()
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            callbacks.onRendererGone()
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
    }

    private class BrowserWebChromeClient(
        private val callbacks: WebViewCallbacks,
    ) : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            callbacks.onTitleChanged(title)
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            callbacks.onProgressChanged(newProgress)
        }
    }
}
