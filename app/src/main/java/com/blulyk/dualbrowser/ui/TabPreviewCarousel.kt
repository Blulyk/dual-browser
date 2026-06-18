package com.blulyk.dualbrowser.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserState
import com.blulyk.dualbrowser.domain.BrowserTab

@Composable
fun TabPreviewCarousel(
    state: BrowserState,
    previews: Map<String, Bitmap>,
    onCommand: (BrowserCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().testTag("tab-preview-carousel")) {
        Text(
            "Tabs",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("tab-preview-list"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.tabs, key = BrowserTab::id) { tab ->
                PreviewCard(
                    tab = tab,
                    preview = previews[tab.id],
                    selected = tab.id == state.focusedTabId,
                    onSelect = { onCommand(BrowserCommand.Focus(tab.id)) },
                    onClose = { onCommand(BrowserCommand.Close(tab.id)) },
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(
    tab: BrowserTab,
    preview: Bitmap?,
    selected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.width(300.dp).height(190.dp).testTag("tab-preview-${tab.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DualBrowserColors.surfaceElevated),
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) DualBrowserColors.primaryBright else DualBrowserColors.outline,
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Preview of ${tab.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(DualBrowserColors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(domain(tab), style = MaterialTheme.typography.titleLarge)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    .background(ColorOverlay).padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (tab.isPrivate) "Private" else tab.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(domain(tab), maxLines = 1, color = DualBrowserColors.textMuted)
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close-preview-${tab.id}")) {
                    Icon(Icons.Default.Close, contentDescription = "Close ${tab.title}")
                }
            }
        }
    }
}

private val ColorOverlay = androidx.compose.ui.graphics.Color(0xE6111B27)

private fun domain(tab: BrowserTab): String = Uri.parse(tab.url).host
    ?.removePrefix("www.")
    ?.takeIf(String::isNotBlank)
    ?: if (tab.isPrivate) "Private tab" else "New tab"
