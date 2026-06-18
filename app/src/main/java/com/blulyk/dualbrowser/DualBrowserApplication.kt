package com.blulyk.dualbrowser

import android.app.Application
import androidx.room.Room
import com.blulyk.dualbrowser.data.BrowserDatabase
import com.blulyk.dualbrowser.data.BrowserRepository
import com.blulyk.dualbrowser.data.BrowserPreferences
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserTab
import com.blulyk.dualbrowser.ui.EngineActionBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DualBrowserApplication : Application() {
    val sessionManager by lazy { BrowserSessionManager() }
    val engineActionBus by lazy { EngineActionBus() }
    lateinit var repository: BrowserRepository
        private set
    lateinit var preferences: BrowserPreferences
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(this, BrowserDatabase::class.java, "dual-browser.db").build()
        repository = BrowserRepository(database.browserDao())
        preferences = BrowserPreferences(this)
        applicationScope.launch {
            val restored = repository.restoreSession()
            if (restored.tabs.isNotEmpty()) {
                val tabs = restored.tabs.map { saved ->
                    BrowserTab(id = saved.id, url = saved.url, title = saved.title)
                }
                sessionManager.dispatch(
                    BrowserCommand.Restore(
                        tabs = tabs,
                        focusedTabId = restored.focusedTabId ?: tabs.first().id,
                        lowerTabId = restored.lowerTabId,
                    ),
                )
            }
            sessionManager.state.collectLatest { state ->
                repository.saveSession(
                    tabs = state.tabs,
                    focusedTabId = state.focusedTabId,
                    lowerTabId = state.lowerTabId,
                )
            }
        }
    }
}
