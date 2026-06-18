package com.blulyk.dualbrowser.data

import com.blulyk.dualbrowser.domain.BrowserTab
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val dao: BrowserDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    val history: Flow<List<HistoryEntity>> = dao.observeHistory()
    val bookmarks: Flow<List<BookmarkEntity>> = dao.observeBookmarks()

    suspend fun recordVisit(tab: BrowserTab) {
        if (tab.isPrivate) return
        dao.insertHistory(
            HistoryEntity(
                url = tab.url,
                title = tab.title,
                visitedAt = clock(),
            ),
        )
    }

    suspend fun addBookmark(tab: BrowserTab) {
        if (tab.isPrivate) return
        dao.upsertBookmark(BookmarkEntity(tab.url, tab.title, clock()))
    }

    suspend fun removeBookmark(url: String) = dao.removeBookmark(url)

    suspend fun clearHistory() = dao.clearHistory()

    suspend fun saveSession(
        tabs: List<BrowserTab>,
        focusedTabId: String,
        lowerTabId: String?,
    ) {
        val normalTabs = tabs.filterNot(BrowserTab::isPrivate)
        val savedTabs = normalTabs.mapIndexed { index, tab ->
            SessionTabEntity(tab.id, tab.url, tab.title, index)
        }
        val savedIds = normalTabs.mapTo(mutableSetOf(), BrowserTab::id)
        val savedFocus = focusedTabId.takeIf(savedIds::contains) ?: normalTabs.firstOrNull()?.id
        val savedLower = lowerTabId?.takeIf(savedIds::contains)
        dao.replaceSession(savedTabs, SessionMetaEntity(focusedTabId = savedFocus, lowerTabId = savedLower))
    }

    suspend fun restoreSession(): RestoredSession {
        val meta = dao.readSessionMeta()
        return RestoredSession(
            tabs = dao.readSessionTabs(),
            focusedTabId = meta?.focusedTabId,
            lowerTabId = meta?.lowerTabId,
        )
    }
}

