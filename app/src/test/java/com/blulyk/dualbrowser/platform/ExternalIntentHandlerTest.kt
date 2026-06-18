package com.blulyk.dualbrowser.platform

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExternalIntentHandlerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val handler = ExternalIntentHandler(context)

    @Test
    fun unsupportedSchemeReturnsReadableFailure() {
        assertEquals(
            ExternalResult.NoHandler("maps"),
            handler.open(Uri.parse("maps://place")),
        )
    }

    @Test
    fun webSchemesStayInsideBrowser() {
        assertEquals(
            ExternalResult.NotExternal,
            handler.open(Uri.parse("https://example.com")),
        )
    }

    @Test
    fun popupRequiresUserGesture() {
        assertEquals(PopupDecision.OpenNewTab, PopupPolicy.decide(userGesture = true))
        assertEquals(PopupDecision.Reject, PopupPolicy.decide(userGesture = false))
    }
}

