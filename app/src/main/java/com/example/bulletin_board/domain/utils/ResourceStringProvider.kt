package com.example.bulletin_board.domain.utils

interface ResourceStringProvider {
    fun getStringImpl(resId: Int): String
}
