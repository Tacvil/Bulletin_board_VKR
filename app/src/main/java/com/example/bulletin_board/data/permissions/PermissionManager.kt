package com.example.bulletin_board.data.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bulletin_board.R

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
        }
    }

    fun handleRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        activity: Activity,
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED
        ) {
            Toast
                .makeText(
                    activity,
                    activity.getString(R.string.permission_denied_notification),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }
}
