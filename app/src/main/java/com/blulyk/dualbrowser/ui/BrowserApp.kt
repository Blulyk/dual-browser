package com.blulyk.dualbrowser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserState
import com.blulyk.dualbrowser.domain.BrowserTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun BrowserApp(
    state: BrowserState,
    onCommand: (BrowserCommand) -> Unit,
    engineActions: Flow<EngineAction> = emptyFlow(),
    onEngineAction: (EngineAction) -> Unit = {},
    webContent: @Composable (BrowserTab) -> Unit = {
        WebSurface(
            tab = it,
            engineActions = engineActions,
            onRendererGone = { onCommand(BrowserCommand.RendererGone(it.id)) },
            onPageFinished = { url, title ->
                onCommand(BrowserCommand.UpdatePage(it.id, url, title))
            },
            onPopupRequested = { url ->
                onCommand(BrowserCommand.OpenTab(url, isPrivate = it.isPrivate))
            },
        )
    },
) {
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (state.focusedTab.needsRecovery) {
                RecoveryView(state.focusedTab, onCommand)
            } else {
                webContent(state.focusedTab)
            }
        }
        ControlCenter(
            state = state,
            onCommand = onCommand,
            onEngineAction = onEngineAction,
        )
    }
}
