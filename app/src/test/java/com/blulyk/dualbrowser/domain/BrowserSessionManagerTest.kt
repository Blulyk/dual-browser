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
    fun blankNavigationKeepsCurrentUrl() {
        manager.dispatch(BrowserCommand.Navigate(firstTab.id, "   "))

        assertEquals(firstTab.url, manager.state.value.focusedTab.url)
    }

    @Test
    fun newNormalTabOpensGoogle() {
        manager.dispatch(BrowserCommand.NewTab(isPrivate = false))

        assertEquals(BrowserSessionManager.HOME_URL, manager.state.value.focusedTab.url)
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
        assertEquals(BrowserSessionManager.HOME_URL, manager.state.value.focusedTab.url)
    }

    @Test
    fun privateTabsAreExcludedFromRestorableSession() {
        manager.dispatch(BrowserCommand.NewTab(isPrivate = true))

        assertTrue(manager.state.value.focusedTab.isPrivate)
        assertEquals(BrowserSessionManager.HOME_URL, manager.state.value.focusedTab.url)
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

    @Test
    fun restoredSessionReplacesDefaultState() {
        val restored = listOf(
            BrowserTab("restored-1", "https://one.example"),
            BrowserTab("restored-2", "https://two.example"),
        )

        manager.dispatch(
            BrowserCommand.Restore(
                tabs = restored,
                focusedTabId = "restored-2",
                lowerTabId = "restored-1",
            ),
        )

        assertEquals(restored, manager.state.value.tabs)
        assertEquals("restored-2", manager.state.value.focusedTabId)
        assertEquals("restored-1", manager.state.value.lowerTabId)
    }

    @Test
    fun pageMetadataUpdatesUrlAndTitle() {
        manager.dispatch(
            BrowserCommand.UpdatePage(
                tabId = firstTab.id,
                url = "https://example.com/redirected",
                title = "Example",
            ),
        )

        assertEquals("https://example.com/redirected", manager.state.value.focusedTab.url)
        assertEquals("Example", manager.state.value.focusedTab.title)
    }

    @Test
    fun openTabCreatesFocusedTabAtResolvedUrl() {
        manager.dispatch(BrowserCommand.OpenTab("example.com", isPrivate = false))

        assertEquals(2, manager.state.value.tabs.size)
        assertEquals("https://example.com", manager.state.value.focusedTab.url)
    }
}
