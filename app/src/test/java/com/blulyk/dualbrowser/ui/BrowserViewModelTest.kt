package com.blulyk.dualbrowser.ui

import android.graphics.Bitmap
import com.blulyk.dualbrowser.data.BookmarkEntity
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BrowserViewModelTest {
    @Test
    fun commandIsForwardedToSessionManager() {
        val manager = BrowserSessionManager()
        val viewModel = BrowserViewModel(manager)

        viewModel.dispatch(BrowserCommand.NewTab(isPrivate = false))

        assertEquals(2, viewModel.state.value.tabs.size)
    }

    @Test
    fun closingTabRemovesItsPreview() {
        val manager = BrowserSessionManager()
        val previews = TabPreviewStore()
        val viewModel = BrowserViewModel(manager, previewStore = previews)
        val tabId = manager.state.value.focusedTabId
        viewModel.updatePreview(tabId, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

        viewModel.dispatch(BrowserCommand.Close(tabId))

        assertEquals(emptySet<String>(), viewModel.previews.value.keys)
    }

    @Test
    fun exposesRepositoryBookmarks() = runTest {
        val bookmark = BookmarkEntity("https://example.com", "Example", 1L)
        val viewModel = BrowserViewModel(bookmarks = flowOf(listOf(bookmark)))

        assertEquals(listOf(bookmark), viewModel.bookmarks.first())
    }
}

