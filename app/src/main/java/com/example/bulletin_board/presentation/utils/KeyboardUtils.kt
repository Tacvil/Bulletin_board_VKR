package com.example.bulletin_board.presentation.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager

object KeyboardUtils {
    fun hideKeyboard(
        context: Context,
        view: View,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = (context as? Activity)?.window?.insetsController
            windowInsetsController?.hide(WindowInsets.Type.ime())
        } else {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
