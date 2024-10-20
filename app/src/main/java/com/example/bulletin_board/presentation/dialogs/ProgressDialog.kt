package com.example.bulletin_board.presentation.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletin_board.databinding.ProgressDialogBinding

object ProgressDialog {
    fun createProgressDialog(activity: Activity): AlertDialog =
        AlertDialog
            .Builder(activity)
            .setView(ProgressDialogBinding.inflate(activity.layoutInflater).root)
            .create()
            .apply {
                setCancelable(false)
                show()
            }
}
