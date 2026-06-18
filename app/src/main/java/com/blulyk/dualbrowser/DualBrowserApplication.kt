package com.blulyk.dualbrowser

import android.app.Application
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import com.blulyk.dualbrowser.ui.EngineActionBus

class DualBrowserApplication : Application() {
    val sessionManager by lazy { BrowserSessionManager() }
    val engineActionBus by lazy { EngineActionBus() }
}

