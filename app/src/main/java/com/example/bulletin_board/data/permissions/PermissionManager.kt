package com.example.bulletin_board.data.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

object PermissionManager {
    private const val PERMISSION_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkAndRequestNotificationPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE,
            )
        } else {
            Timber.tag("POST_NOTIFICATIONS_PER_TRUE").d("POST_NOTIFICATIONS_PER_TRUE")
        }
    }

    fun handleRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        activity: Activity,
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Timber.tag("POST_NOTIFICATIONS_PER_TRUE").d("POST_NOTIFICATIONS_PER_TRUE")
                } else {
                    Timber.tag("POST_NOTIFICATIONS_PER_FALSE").d("POST_NOTIFICATIONS_PER_FALSE")
                    Toast
                        .makeText(
                            activity,
                            "Permission denied, notifications cannot be sent",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
                return
            }
        }
    }
}
