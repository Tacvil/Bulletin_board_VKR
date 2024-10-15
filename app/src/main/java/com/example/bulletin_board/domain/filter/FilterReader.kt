package com.example.bulletin_board.domain.filter

interface FilterReader {
    fun getFilterValue(key: String): String?
}
