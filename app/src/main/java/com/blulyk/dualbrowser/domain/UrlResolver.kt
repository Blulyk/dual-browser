package com.blulyk.dualbrowser.domain

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrlResolver(
    private val searchTemplate: String = "https://www.google.com/search?q=%s",
) {
    fun resolve(input: String): String {
        val value = input.trim()
        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
        ) {
            return value
        }
        if (value.equals("localhost", ignoreCase = true) ||
            value.startsWith("localhost:", ignoreCase = true)
        ) {
            return "http://$value"
        }
        if (value.isNotEmpty() && !value.contains(Regex("\\s")) && value.contains('.')) {
            return "https://$value"
        }
        val encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        return searchTemplate.format(encoded)
    }
}

