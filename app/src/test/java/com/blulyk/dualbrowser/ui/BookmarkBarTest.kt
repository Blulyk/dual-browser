package com.blulyk.dualbrowser.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.blulyk.dualbrowser.data.BookmarkEntity
import com.blulyk.dualbrowser.domain.BrowserCommand
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BookmarkBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bookmarkNavigatesFocusedTab() {
        val bookmark = BookmarkEntity("https://example.com", "Example", 1L)
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent {
            DualBrowserTheme {
                BookmarkBar(listOf(bookmark), "tab-1", commands::add)
            }
        }

        composeRule.onNodeWithTag("bookmark-https://example.com").performClick()

        assertEquals(BrowserCommand.Navigate("tab-1", bookmark.url), commands.single())
    }

    @Test
    fun emptyBookmarksShowHint() {
        composeRule.setContent {
            DualBrowserTheme { BookmarkBar(emptyList(), "tab-1", {}) }
        }

        composeRule.onNodeWithTag("bookmark-empty").assertIsDisplayed()
    }
}
