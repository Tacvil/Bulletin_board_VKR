package com.example.bulletin_board.domain.ui.account

import android.widget.ImageView
import android.widget.TextView

interface AccountUiViewsProvider {
    fun getTextViewAccount(): TextView

    fun getImageViewAccount(): ImageView
}
