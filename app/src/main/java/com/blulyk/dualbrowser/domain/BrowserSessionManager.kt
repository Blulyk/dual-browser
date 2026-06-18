package com.blulyk.dualbrowser.domain

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserSessionManager(
    initialState: BrowserState = defaultState(),
    private val resolver: UrlResolver = UrlResolver(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<BrowserState> = mutableState.asStateFlow()

    fun dispatch(command: BrowserCommand) {
        mutableState.value = reduce(mutableState.value, command)
    }

    fun restorableTabs(): List<BrowserTab> = state.value.tabs.filterNot(BrowserTab::isPrivate)

    private fun reduce(current: BrowserState, command: BrowserCommand): BrowserState = when (command) {
        is BrowserCommand.Navigate -> current.updateTab(command.tabId) {
            it.copy(url = resolver.resolve(command.input), needsRecovery = false)
        }
        is BrowserCommand.NewTab -> {
            val tab = BrowserTab(id = idGenerator(), url = "about:blank", isPrivate = command.isPrivate)
            current.copy(tabs = current.tabs + tab, focusedTabId = tab.id)
        }
        is BrowserCommand.OpenTab -> {
            val tab = BrowserTab(
                id = idGenerator(),
                url = resolver.resolve(command.input),
                isPrivate = command.isPrivate,
            )
            current.copy(tabs = current.tabs + tab, focusedTabId = tab.id)
        }
        is BrowserCommand.Close -> closeTab(current, command.tabId)
        is BrowserCommand.Focus -> current.copy(focusedTabId = current.tab(command.tabId).id)
        is BrowserCommand.PromoteToLower -> current.copy(lowerTabId = current.tab(command.tabId).id)
        BrowserCommand.ClearLower -> current.copy(lowerTabId = null)
        is BrowserCommand.RendererGone -> current.updateTab(command.tabId) {
            it.copy(needsRecovery = true)
        }
        is BrowserCommand.UpdatePage -> current.updateTab(command.tabId) {
            it.copy(url = command.url, title = command.title.ifBlank { it.title })
        }
        is BrowserCommand.Restore -> restore(command)
    }

    private fun restore(command: BrowserCommand.Restore): BrowserState {
        require(command.tabs.isNotEmpty()) { "A restored session must contain a tab" }
        val ids = command.tabs.mapTo(mutableSetOf(), BrowserTab::id)
        require(command.focusedTabId in ids) { "Focused tab is not part of the restored session" }
        require(command.lowerTabId == null || command.lowerTabId in ids) {
            "Lower tab is not part of the restored session"
        }
        return BrowserState(command.tabs, command.focusedTabId, command.lowerTabId)
    }

    private fun closeTab(current: BrowserState, tabId: String): BrowserState {
        current.tab(tabId)
        val remaining = current.tabs.filterNot { it.id == tabId }.toMutableList()
        if (remaining.isEmpty()) {
            remaining += BrowserTab(id = idGenerator(), url = "about:blank")
        }
        val focusedId = if (current.focusedTabId == tabId) remaining.last().id else current.focusedTabId
        return current.copy(
            tabs = remaining,
            focusedTabId = focusedId,
            lowerTabId = current.lowerTabId?.takeUnless { it == tabId },
        )
    }

    private fun BrowserState.updateTab(id: String, transform: (BrowserTab) -> BrowserTab): BrowserState {
        tab(id)
        return copy(tabs = tabs.map { if (it.id == id) transform(it) else it })
    }

    companion object {
        private fun defaultState(): BrowserState {
            val tab = BrowserTab(id = UUID.randomUUID().toString(), url = "about:blank")
            return BrowserState(tabs = listOf(tab), focusedTabId = tab.id)
        }
    }
}
