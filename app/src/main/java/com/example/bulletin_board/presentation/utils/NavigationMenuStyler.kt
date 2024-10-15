package com.example.bulletin_board.presentation.utils

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import androidx.core.content.ContextCompat

object NavigationMenuStyler {
    fun setMenuItemStyle(
        menu: Menu,
        itemId: Int,
        color: Int,
        context: Context,
    ) {
        val menuItem = menu.findItem(itemId)
        val spannableString = SpannableString(menuItem.title)
        menuItem.title?.let {
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, color)),
                0,
                it.length,
                0,
            )
        }
        menuItem.title = spannableString
    }
}
