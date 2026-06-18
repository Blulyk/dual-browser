package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadHandlerTest {
    @Test
    fun contentDispositionProvidesDownloadName() {
        assertEquals(
            "report.pdf",
            DownloadHandler.guessFileName(
                url = "https://example.com/download?id=4",
                contentDisposition = "attachment; filename=report.pdf",
                mimeType = "application/pdf",
            ),
        )
    }
}

