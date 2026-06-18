package com.blulyk.dualbrowser.ui

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TabPreviewStore(private val maxEntries: Int = 12) {
    init {
        require(maxEntries > 0) { "maxEntries must be positive" }
    }

    private val cache = LinkedHashMap<String, Bitmap>(maxEntries, 0.75f, true)
    private val mutablePreviews = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val previews: StateFlow<Map<String, Bitmap>> = mutablePreviews.asStateFlow()

    @Synchronized
    fun put(tabId: String, bitmap: Bitmap) {
        cache[tabId] = bitmap
        while (cache.size > maxEntries) {
            cache.remove(cache.keys.first())
        }
        publish()
    }

    @Synchronized
    fun remove(tabId: String) {
        if (cache.remove(tabId) != null) publish()
    }

    @Synchronized
    fun retain(tabIds: Set<String>) {
        if (cache.keys.removeAll { it !in tabIds }) publish()
    }

    private fun publish() {
        mutablePreviews.value = cache.toMap()
    }
}
