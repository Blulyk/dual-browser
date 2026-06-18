package com.blulyk.dualbrowser.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.data.BookmarkEntity
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserState
import kotlinx.coroutines.launch

@Composable
fun ControlCenter(
    state: BrowserState,
    onCommand: (BrowserCommand) -> Unit,
    modifier: Modifier = Modifier,
    onEngineAction: (EngineAction) -> Unit = {},
    previews: Map<String, Bitmap> = emptyMap(),
    bookmarks: List<BookmarkEntity> = emptyList(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BrowserPanel(modifier.testTag("control-center")) {
        Column {
            BrowserToolbar(
                state = state,
                onCommand = onCommand,
                onEngineAction = onEngineAction,
                onBookmark = {
                    val application = context.applicationContext as DualBrowserApplication
                    scope.launch {
                        application.repository.addBookmark(state.focusedTab)
                        Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show()
                    }
                },
                onLibrary = { context.startActivity(Intent(context, LibraryActivity::class.java)) },
                onDiagnostics = { context.startActivity(Intent(context, DiagnosticsActivity::class.java)) },
            )
            TabPreviewCarousel(
                state = state,
                previews = previews,
                onCommand = onCommand,
                modifier = Modifier.weight(1f),
            )
            BookmarkBar(
                bookmarks = bookmarks,
                focusedTabId = state.focusedTabId,
                onCommand = onCommand,
            )
        }
    }
}

@Composable
fun BrowserPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = DualBrowserColors.background,
        contentColor = DualBrowserColors.text,
        content = content,
    )
}

enum class EngineAction {
    Back,
    Forward,
    Reload,
    Stop,
}
