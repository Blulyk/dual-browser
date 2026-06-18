package com.blulyk.dualbrowser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserState

@Composable
fun BrowserToolbar(
    state: BrowserState,
    onCommand: (BrowserCommand) -> Unit,
    onEngineAction: (EngineAction) -> Unit,
    onBookmark: () -> Unit,
    onLibrary: () -> Unit,
    onDiagnostics: () -> Unit,
) {
    var address by rememberSaveable(state.focusedTabId, state.focusedTab.url) {
        mutableStateOf(state.focusedTab.url.takeUnless { it == "about:blank" }.orEmpty())
    }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val navigate = {
        if (address.isNotBlank()) onCommand(BrowserCommand.Navigate(state.focusedTabId, address))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("browser-toolbar"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ToolbarIcon("Back", "back", Icons.AutoMirrored.Filled.ArrowBack) {
                onEngineAction(EngineAction.Back)
            }
            ToolbarIcon("Forward", "forward", Icons.AutoMirrored.Filled.ArrowForward) {
                onEngineAction(EngineAction.Forward)
            }
            ToolbarIcon("Reload", "reload", Icons.Default.Refresh) {
                onEngineAction(EngineAction.Reload)
            }
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.weight(1f).widthIn(min = 150.dp).testTag("address"),
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                placeholder = { Text("Search or enter address") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { navigate() }),
            )
            FilledIconButton(onClick = navigate, modifier = Modifier.testTag("go")) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Go")
            }
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("overflow")) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            ToolbarIcon("New tab", "new-tab", Icons.Default.Add) {
                onCommand(BrowserCommand.NewTab(isPrivate = false))
            }
            ToolbarIcon("Private tab", "private-tab", Icons.Default.DarkMode) {
                onCommand(BrowserCommand.NewTab(isPrivate = true))
            }
            ToolbarIcon(
                if (state.lowerTabId == null) "Dual view" else "Controls",
                "dual-view",
                Icons.AutoMirrored.Filled.OpenInNew,
            ) {
                onCommand(
                    if (state.lowerTabId == null) {
                        BrowserCommand.PromoteToLower(state.focusedTabId)
                    } else {
                        BrowserCommand.ClearLower
                    },
                )
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Bookmark this page") },
                onClick = { menuExpanded = false; onBookmark() },
                modifier = Modifier.testTag("bookmark"),
            )
            DropdownMenuItem(
                text = { Text("Library") },
                onClick = { menuExpanded = false; onLibrary() },
                modifier = Modifier.testTag("library"),
            )
            DropdownMenuItem(
                text = { Text("Diagnostics") },
                onClick = { menuExpanded = false; onDiagnostics() },
                modifier = Modifier.testTag("diagnostics"),
            )
        }
    }
}

@Composable
private fun ToolbarIcon(
    label: String,
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
    }
}
