package com.blulyk.dualbrowser.platform

import android.Manifest
import android.webkit.PermissionRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class WebPermissionPolicyTest {
    @Test
    fun cameraAndMicrophoneMapToAndroidPermissions() {
        assertEquals(
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            WebPermissionPolicy.androidPermissions(
                arrayOf(
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                ),
            ),
        )
    }

    @Test
    fun geolocationUsesFineLocationPermission() {
        assertEquals(Manifest.permission.ACCESS_FINE_LOCATION, WebPermissionPolicy.geolocationPermission)
    }
}
