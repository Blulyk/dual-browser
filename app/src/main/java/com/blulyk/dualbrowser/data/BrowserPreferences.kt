package com.blulyk.dualbrowser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.browserPreferenceStore by preferencesDataStore(name = "browser-preferences")

enum class ThemeMode {
    System,
    Light,
    Dark,
}

class BrowserPreferences(
    private val store: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.browserPreferenceStore)

    private val data = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val theme: Flow<ThemeMode> = data.map { preferences ->
        preferences[THEME_KEY]
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.System
    }

    val searchTemplate: Flow<String> = data.map { preferences ->
        preferences[SEARCH_TEMPLATE_KEY] ?: DEFAULT_SEARCH_TEMPLATE
    }

    suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setSearchTemplate(template: String) {
        require(template.contains("%s")) { "Search template must contain %s" }
        store.edit { it[SEARCH_TEMPLATE_KEY] = template }
    }

    companion object {
        const val DEFAULT_SEARCH_TEMPLATE = "https://www.google.com/search?q=%s"
        private val THEME_KEY = stringPreferencesKey("theme")
        private val SEARCH_TEMPLATE_KEY = stringPreferencesKey("search_template")
    }
}

