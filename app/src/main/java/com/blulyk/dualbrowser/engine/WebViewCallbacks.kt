package com.blulyk.dualbrowser.engine

import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import com.blulyk.dualbrowser.platform.DownloadSpec

interface WebViewCallbacks {
    fun onPageStarted(url: String) = Unit
    fun onPageFinished(url: String) = Unit
    fun onTitleChanged(title: String) = Unit
    fun onProgressChanged(progress: Int) = Unit
    fun onRendererGone() = Unit
    fun shouldOverrideUrl(uri: Uri): Boolean = false
    fun onDownloadRequested(spec: DownloadSpec) = Unit
    fun onShowFileChooser(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams,
    ): Boolean = false
    fun onPermissionRequest(request: PermissionRequest) = request.deny()
    fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) = callback.onCustomViewHidden()
    fun onHideCustomView() = Unit
    fun onPopupRequested(url: String) = Unit
    fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback,
    ) = callback.invoke(origin, false, false)

    data object None : WebViewCallbacks
}
