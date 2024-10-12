package com.example.bulletin_board.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {
    fun showToast(
        context: Context,
        message: String,
        duration: Int,
    ) {
        Toast.makeText(context, message, duration).show()
    }
}
