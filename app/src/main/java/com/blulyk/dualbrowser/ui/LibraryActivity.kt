package com.blulyk.dualbrowser.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.domain.BrowserCommand
import kotlinx.coroutines.launch

class LibraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as DualBrowserApplication
        setContent {
            val bookmarks by application.repository.bookmarks.collectAsState(emptyList())
            val history by application.repository.history.collectAsState(emptyList())
            val scope = rememberCoroutineScope()
            MaterialTheme {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    item { Text("Bookmarks", style = MaterialTheme.typography.headlineSmall) }
                    items(bookmarks, key = { "bookmark-${it.url}" }) { bookmark ->
                        LibraryEntry(bookmark.title, bookmark.url) { open(application, bookmark.url) }
                    }
                    item {
                        HorizontalDivider(Modifier.padding(vertical = 16.dp))
                        Text("History", style = MaterialTheme.typography.headlineSmall)
                        Button(onClick = { scope.launch { application.repository.clearHistory() } }) {
                            Text("Clear history")
                        }
                    }
                    items(history, key = { "history-${it.id}" }) { entry ->
                        LibraryEntry(entry.title, entry.url) { open(application, entry.url) }
                    }
                }
            }
        }
    }

    private fun open(application: DualBrowserApplication, url: String) {
        application.sessionManager.dispatch(BrowserCommand.OpenTab(url, isPrivate = false))
        finish()
    }
}

@androidx.compose.runtime.Composable
private fun LibraryEntry(title: String, url: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(title.ifBlank { url })
            Text(url, style = MaterialTheme.typography.bodySmall)
        }
    }
}

