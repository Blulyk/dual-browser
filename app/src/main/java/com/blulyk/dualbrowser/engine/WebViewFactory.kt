package com.blulyk.dualbrowser.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import android.webkit.GeolocationPermissions
import android.os.Message
import com.blulyk.dualbrowser.platform.PopupDecision
import com.blulyk.dualbrowser.platform.PopupPolicy

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
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            callbacks.onDownloadRequested(
                com.blulyk.dualbrowser.platform.DownloadSpec(
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = contentDisposition,
                    mimeType = mimeType,
                    cookies = CookieManager.getInstance().getCookie(url),
                ),
            )
        }
        return WebViewBrowserEngine(webView)
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
            callbacks.shouldOverrideUrl(request.url)
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

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams,
        ): Boolean = callbacks.onShowFileChooser(filePathCallback, fileChooserParams)

        override fun onPermissionRequest(request: PermissionRequest) {
            callbacks.onPermissionRequest(request)
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            callbacks.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            callbacks.onHideCustomView()
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback,
        ) {
            callbacks.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message,
        ): Boolean {
            if (PopupPolicy.decide(isUserGesture) == PopupDecision.Reject) return false
            val popup = WebView(view.context)
            popup.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    popupView: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    callbacks.onPopupRequested(request.url.toString())
                    popupView.destroy()
                    return true
                }
            }
            (resultMsg.obj as WebView.WebViewTransport).webView = popup
            resultMsg.sendToTarget()
            return true
        }
    }
}
