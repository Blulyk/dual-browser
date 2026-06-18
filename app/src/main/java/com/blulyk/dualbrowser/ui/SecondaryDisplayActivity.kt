package com.blulyk.dualbrowser.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blulyk.dualbrowser.DualBrowserApplication

class SecondaryDisplayActivity : ComponentActivity() {
    private val viewModel by viewModels<BrowserViewModel> {
        BrowserViewModel.Factory(application as DualBrowserApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            MaterialTheme {
                val lowerTabId = state.lowerTabId
                if (lowerTabId == null) {
                    ControlCenter(
                        state = state,
                        onCommand = viewModel::dispatch,
                        onEngineAction = viewModel::dispatchEngine,
                    )
                } else {
                    BrowserApp(
                        state = state.copy(focusedTabId = lowerTabId),
                        onCommand = viewModel::dispatch,
                        engineActions = viewModel.engineActions,
                        onEngineAction = viewModel::dispatchEngine,
                    )
                }
            }
        }
    }
}

