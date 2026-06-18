package com.blulyk.dualbrowser.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blulyk.dualbrowser.data.BookmarkEntity
import com.blulyk.dualbrowser.domain.BrowserCommand

@Composable
fun BookmarkBar(
    bookmarks: List<BookmarkEntity>,
    focusedTabId: String,
    onCommand: (BrowserCommand) -> Unit,
) {
    Surface(
        color = DualBrowserColors.surface,
        modifier = Modifier.fillMaxWidth().height(72.dp).testTag("bookmark-bar"),
    ) {
        if (bookmarks.isEmpty()) {
            Text(
                "Bookmarks appear here for one-tap access",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp)
                    .testTag("bookmark-empty"),
                color = DualBrowserColors.textMuted,
            )
        } else {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(bookmarks, key = BookmarkEntity::url) { bookmark ->
                    AssistChip(
                        onClick = {
                            onCommand(BrowserCommand.Navigate(focusedTabId, bookmark.url))
                        },
                        label = {
                            Text(
                                bookmark.title.takeUnless { it == "New tab" }.orEmpty()
                                    .ifBlank { Uri.parse(bookmark.url).host ?: bookmark.url },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        modifier = Modifier.testTag("bookmark-${bookmark.url}"),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DualBrowserColors.surfaceElevated,
                        ),
                    )
                }
            }
        }
    }
}
