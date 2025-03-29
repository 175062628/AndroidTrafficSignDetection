package com.example.androidtrafficsigndetection.Classes

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {
    private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    private const val READ_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private const val WRITE_STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_REQUEST_CODE = 1001

    private var application: Application? = null

    fun init(application: Application) {
        this.application = application
    }

    fun requestPermissions(activity: Activity) {
        val app = application ?: return
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(app, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(CAMERA_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(app, READ_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(READ_STORAGE_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(app, WRITE_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(WRITE_STORAGE_PERMISSION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    fun getPermissionStatus(): Map<String, Boolean> {
        val app = application ?: return emptyMap()
        return mapOf(
            CAMERA_PERMISSION to (ContextCompat.checkSelfPermission(app, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED),
            READ_STORAGE_PERMISSION to (ContextCompat.checkSelfPermission(app, READ_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED),
            WRITE_STORAGE_PERMISSION to (ContextCompat.checkSelfPermission(app, WRITE_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED)
        )
    }
}