package com.blulyk.dualbrowser.engine

import android.content.Context
import android.webkit.WebSettings
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WebViewFactoryTest {
    private lateinit var engine: WebViewBrowserEngine

    @Before
    fun createEngine() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        engine = WebViewFactory().create(context, WebViewCallbacks.None)
    }

    @After
    fun destroyEngine() {
        engine.destroy()
    }

    @Test
    fun createUsesCompatibleSecureSettings() {
        val settings = engine.view.settings

        assertTrue(settings.javaScriptEnabled)
        assertTrue(settings.domStorageEnabled)
        assertFalse(settings.allowFileAccess)
        assertFalse(settings.allowContentAccess)
        assertEquals(WebSettings.MIXED_CONTENT_NEVER_ALLOW, settings.mixedContentMode)
    }

    @Test
    fun engineDelegatesBasicNavigation() {
        engine.load("https://example.com")
        engine.reload()
        engine.stop()

        assertEquals("https://example.com", engine.view.url)
    }

    @Test
    fun visibleCommitIsForwardedToCallbacks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var committedUrl: String? = null
        val committedEngine = WebViewFactory().create(
            context,
            object : WebViewCallbacks {
                override fun onPageCommitVisible(url: String) {
                    committedUrl = url
                }
            },
        )

        committedEngine.view.webViewClient.onPageCommitVisible(
            committedEngine.view,
            "https://example.com",
        )

        assertEquals("https://example.com", committedUrl)
        committedEngine.destroy()
    }
}

