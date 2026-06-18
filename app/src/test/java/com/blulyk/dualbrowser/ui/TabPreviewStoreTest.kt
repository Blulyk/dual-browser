package com.blulyk.dualbrowser.ui

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TabPreviewStoreTest {
    @Test
    fun evictsLeastRecentPreviewAndRemovesClosedTabs() {
        val store = TabPreviewStore(maxEntries = 2)
        val first = bitmap()
        val second = bitmap()
        val third = bitmap()

        store.put("one", first)
        store.put("two", second)
        store.put("three", third)

        assertEquals(setOf("two", "three"), store.previews.value.keys)

        store.remove("two")
        assertEquals(setOf("three"), store.previews.value.keys)
    }

    @Test
    fun retainDiscardsEveryStaleTab() {
        val store = TabPreviewStore(maxEntries = 3)
        store.put("one", bitmap())
        store.put("two", bitmap())
        store.put("three", bitmap())

        store.retain(setOf("three"))

        assertEquals(setOf("three"), store.previews.value.keys)
    }

    private fun bitmap(): Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}
