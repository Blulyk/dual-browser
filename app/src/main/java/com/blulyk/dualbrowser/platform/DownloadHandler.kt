package com.blulyk.dualbrowser.platform

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class DownloadSpec(
    val url: String,
    val userAgent: String?,
    val contentDisposition: String?,
    val mimeType: String?,
    val cookies: String?,
)

class DownloadHandler(
    private val context: Context,
) {
    fun enqueue(spec: DownloadSpec): Long {
        val fileName = guessFileName(spec.url, spec.contentDisposition, spec.mimeType)
        val request = DownloadManager.Request(spec.url.toUri())
            .setTitle(fileName)
            .setMimeType(spec.mimeType)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        spec.userAgent?.let { request.addRequestHeader("User-Agent", it) }
        spec.cookies?.let { request.addRequestHeader("Cookie", it) }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return manager.enqueue(request)
    }

    companion object {
        fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
            val dispositionName = contentDisposition
                ?.let { Regex("filename\\*?=(?:UTF-8''|\\\")?([^\\\";]+)", RegexOption.IGNORE_CASE).find(it) }
                ?.groupValues
                ?.get(1)
                ?.let { URLDecoder.decode(it.trim(), StandardCharsets.UTF_8.name()) }
            val pathName = runCatching { URI(url).path.substringAfterLast('/').ifBlank { null } }.getOrNull()
            var name = dispositionName ?: pathName ?: "download"
            if (!name.contains('.') && mimeType != null) {
                val extension = mapOf(
                    "application/pdf" to "pdf",
                    "image/jpeg" to "jpg",
                    "image/png" to "png",
                    "text/plain" to "txt",
                    "application/zip" to "zip",
                )[mimeType.substringBefore(';').lowercase()]
                if (extension != null) name += ".$extension"
            }
            return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(180)
        }
    }
}
