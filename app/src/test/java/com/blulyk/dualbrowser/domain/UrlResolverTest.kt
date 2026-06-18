package com.blulyk.dualbrowser.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlResolverTest {
    private val resolver = UrlResolver()

    @Test
    fun hostBecomesHttpsUrl() {
        assertEquals("https://example.com", resolver.resolve("example.com"))
    }

    @Test
    fun absoluteHttpUrlIsPreserved() {
        assertEquals("http://example.com/path", resolver.resolve("http://example.com/path"))
    }

    @Test
    fun wordsBecomeEncodedSearch() {
        assertEquals(
            "https://www.google.com/search?q=dual+browser",
            resolver.resolve("dual browser"),
        )
    }

    @Test
    fun localhostUsesHttp() {
        assertEquals("http://localhost:8080", resolver.resolve("localhost:8080"))
    }
}

