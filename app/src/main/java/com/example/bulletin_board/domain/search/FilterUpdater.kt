package com.example.bulletin_board.domain.search

interface FilterUpdater {
    fun addToFilter(
        key: String,
        value: String,
    )
}
