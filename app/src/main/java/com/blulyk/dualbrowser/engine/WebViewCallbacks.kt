package com.blulyk.dualbrowser.engine

interface WebViewCallbacks {
    fun onPageStarted(url: String) = Unit
    fun onPageFinished(url: String) = Unit
    fun onTitleChanged(title: String) = Unit
    fun onProgressChanged(progress: Int) = Unit
    fun onRendererGone() = Unit

    data object None : WebViewCallbacks
}

