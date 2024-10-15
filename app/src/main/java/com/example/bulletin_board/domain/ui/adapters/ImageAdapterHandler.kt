package com.example.bulletin_board.domain.ui.adapters

import android.graphics.Bitmap

interface ImageAdapterHandler {
    fun getSingleImages(editImagePos: Int)

    fun getTitle(position: Int): String

    fun chooseScaleType(bitmap: Bitmap): Boolean
}
