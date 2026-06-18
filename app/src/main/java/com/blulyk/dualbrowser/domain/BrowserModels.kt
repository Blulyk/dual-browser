package com.blulyk.dualbrowser.domain

data class BrowserTab(
    val id: String,
    val url: String,
    val title: String = "New tab",
    val isPrivate: Boolean = false,
    val needsRecovery: Boolean = false,
)

data class BrowserState(
    val tabs: List<BrowserTab>,
    val focusedTabId: String,
    val lowerTabId: String? = null,
) {
    val focusedTab: BrowserTab
        get() = tab(focusedTabId)

    fun tab(id: String): BrowserTab = requireNotNull(tabs.find { it.id == id }) {
        "Unknown tab: $id"
    }
}

sealed interface BrowserCommand {
    data class Navigate(val tabId: String, val input: String) : BrowserCommand
    data class NewTab(val isPrivate: Boolean) : BrowserCommand
    data class Close(val tabId: String) : BrowserCommand
    data class Focus(val tabId: String) : BrowserCommand
    data class PromoteToLower(val tabId: String) : BrowserCommand
    data object ClearLower : BrowserCommand
    data class RendererGone(val tabId: String) : BrowserCommand
    data class Restore(
        val tabs: List<BrowserTab>,
        val focusedTabId: String,
        val lowerTabId: String?,
    ) : BrowserCommand
}
