package com.example.bulletin_board.domain.images

import android.graphics.Bitmap

interface ChooseScaleTypeHandler {
    fun chooseScaleType(bitmap: Bitmap): Boolean
}
