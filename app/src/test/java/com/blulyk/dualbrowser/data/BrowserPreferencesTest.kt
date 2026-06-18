package com.blulyk.dualbrowser.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserPreferencesTest {
    @Test
    fun themeAndSearchProviderArePersisted() = runTest {
        val file = File.createTempFile("dual-browser-preferences", ".preferences_pb").apply { delete() }
        val store = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher(testScheduler)),
            produceFile = { file },
        )
        val preferences = BrowserPreferences(store)

        preferences.setTheme(ThemeMode.Dark)
        preferences.setSearchTemplate("https://duckduckgo.com/?q=%s")

        assertEquals(ThemeMode.Dark, preferences.theme.first())
        assertEquals("https://duckduckgo.com/?q=%s", preferences.searchTemplate.first())
    }
}
