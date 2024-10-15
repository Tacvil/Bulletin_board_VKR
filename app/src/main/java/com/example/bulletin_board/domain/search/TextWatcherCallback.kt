package com.example.bulletin_board.domain.search

interface TextWatcherCallback {
    fun onTextChanged(inputSearchQuery: String)

    fun clearSearchResults()
}
