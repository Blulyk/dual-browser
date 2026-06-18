package com.blulyk.dualbrowser.platform

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

sealed interface ExternalResult {
    data object NotExternal : ExternalResult
    data object Opened : ExternalResult
    data class NoHandler(val scheme: String) : ExternalResult
}

class ExternalIntentHandler(
    private val context: Context,
) {
    @SuppressLint("QueryPermissionsNeeded")
    fun open(uri: Uri): ExternalResult {
        val scheme = uri.scheme?.lowercase().orEmpty()
        if (scheme == "http" || scheme == "https") return ExternalResult.NotExternal
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) == null) {
            return ExternalResult.NoHandler(scheme)
        }
        return try {
            context.startActivity(intent)
            ExternalResult.Opened
        } catch (_: ActivityNotFoundException) {
            ExternalResult.NoHandler(scheme)
        }
    }
}

enum class PopupDecision {
    OpenNewTab,
    Reject,
}

object PopupPolicy {
    fun decide(userGesture: Boolean): PopupDecision =
        if (userGesture) PopupDecision.OpenNewTab else PopupDecision.Reject
}
