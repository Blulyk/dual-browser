package com.blulyk.dualbrowser.engine

interface BrowserEngine {
    fun load(url: String)
    fun back(): Boolean
    fun forward(): Boolean
    fun reload()
    fun stop()
    fun find(query: String)
    fun setDesktopMode(enabled: Boolean)
    fun destroy()
}

