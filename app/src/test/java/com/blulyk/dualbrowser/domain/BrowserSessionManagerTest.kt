package com.blulyk.dualbrowser.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserSessionManagerTest {
    private val firstTab = BrowserTab(id = "tab-1", url = "about:blank")
    private val manager = BrowserSessionManager(
        initialState = BrowserState(tabs = listOf(firstTab), focusedTabId = firstTab.id),
        idGenerator = sequenceOf("tab-2", "tab-3").iterator()::next,
    )

    @Test
    fun navigateResolvesInputForFocusedTab() {
        manager.dispatch(BrowserCommand.Navigate(firstTab.id, "example.com"))

        assertEquals("https://example.com", manager.state.value.focusedTab.url)
    }

    @Test
    fun promotingTabAssignsLowerSurface() {
        manager.dispatch(BrowserCommand.PromoteToLower(firstTab.id))

        assertEquals(firstTab.id, manager.state.value.lowerTabId)
    }

    @Test
    fun closingLowerTabClearsLowerSurfaceAndKeepsOneTab() {
        manager.dispatch(BrowserCommand.PromoteToLower(firstTab.id))
        manager.dispatch(BrowserCommand.Close(firstTab.id))

        assertNull(manager.state.value.lowerTabId)
        assertEquals(1, manager.state.value.tabs.size)
        assertFalse(manager.state.value.focusedTab.isPrivate)
    }

    @Test
    fun privateTabsAreExcludedFromRestorableSession() {
        manager.dispatch(BrowserCommand.NewTab(isPrivate = true))

        assertTrue(manager.state.value.focusedTab.isPrivate)
        assertTrue(manager.restorableTabs().none(BrowserTab::isPrivate))
    }

    @Test
    fun rendererFailureMarksOnlyAffectedTab() {
        manager.dispatch(BrowserCommand.NewTab(isPrivate = false))
        val secondId = manager.state.value.focusedTabId

        manager.dispatch(BrowserCommand.RendererGone(firstTab.id))

        assertTrue(manager.state.value.tab(firstTab.id).needsRecovery)
        assertFalse(manager.state.value.tab(secondId).needsRecovery)
    }
}

