package com.blulyk.dualbrowser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.blulyk.dualbrowser.domain.BrowserTab

@Composable
fun ControlCenter(
    state: BrowserState,
    onCommand: (BrowserCommand) -> Unit,
    onEngineAction: (EngineAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var address by rememberSaveable(state.focusedTabId) {
        mutableStateOf(state.focusedTab.url.takeUnless { it == "about:blank" }.orEmpty())
    }
    val navigate = { onCommand(BrowserCommand.Navigate(state.focusedTabId, address)) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("control-center"),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Dual Browser", style = MaterialTheme.typography.titleMedium)
            TabStrip(state = state, onCommand = onCommand)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrowserControl("Back", "back") { onEngineAction(EngineAction.Back) }
                BrowserControl("Forward", "forward") { onEngineAction(EngineAction.Forward) }
                BrowserControl("Reload", "reload") { onEngineAction(EngineAction.Reload) }
            }
            Row {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("address"),
                    singleLine = true,
                    placeholder = { Text("Search or enter address") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { navigate() }),
                )
                Button(
                    onClick = { onCommand(BrowserCommand.NewTab(isPrivate = false)) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("new-tab"),
                ) {
                    Text("New tab")
                }
                Button(
                    onClick = { onCommand(BrowserCommand.NewTab(isPrivate = true)) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("private-tab"),
                ) {
                    Text("Private")
                }
            }
        }
    }
}

@Composable
private fun TabStrip(state: BrowserState, onCommand: (BrowserCommand) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.tabs, key = BrowserTab::id) { tab ->
            Row {
                Button(
                    onClick = { onCommand(BrowserCommand.Focus(tab.id)) },
                    modifier = Modifier.testTag("tab-${tab.id}"),
                    colors = if (tab.id == state.focusedTabId) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                ) {
                    Text(
                        text = when {
                            tab.isPrivate -> "Private"
                            tab.title == "New tab" -> "New tab"
                            else -> tab.title
                        },
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = { onCommand(BrowserCommand.Close(tab.id)) },
                    modifier = Modifier.testTag("close-${tab.id}"),
                ) {
                    Text("X")
                }
            }
        }
    }
}

@Composable
private fun BrowserControl(label: String, tag: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label)
    }
}

enum class EngineAction {
    Back,
    Forward,
    Reload,
    Stop,
}
