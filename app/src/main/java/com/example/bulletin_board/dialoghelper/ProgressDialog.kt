package com.example.bulletin_board.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletin_board.databinding.ProgressDialogBinding

object ProgressDialog {

    fun createProgressDialog(activity: Activity) :AlertDialog {

        val builder = AlertDialog.Builder(activity)
        val binding = ProgressDialogBinding.inflate(activity.layoutInflater)
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

}