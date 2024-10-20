package com.example.bulletin_board.domain.navigation

import android.graphics.Bitmap

interface OnFragmentClosedListener {
    fun onFragClose(list: ArrayList<Bitmap>)
}
