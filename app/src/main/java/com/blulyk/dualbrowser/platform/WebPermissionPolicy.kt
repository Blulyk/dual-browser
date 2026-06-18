package com.blulyk.dualbrowser.platform

import android.Manifest
import android.webkit.PermissionRequest

object WebPermissionPolicy {
    val geolocationPermission: String = Manifest.permission.ACCESS_FINE_LOCATION

    fun androidPermissions(resources: Array<String>): List<String> = buildList {
        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in resources) add(Manifest.permission.CAMERA)
        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in resources) add(Manifest.permission.RECORD_AUDIO)
    }
}
