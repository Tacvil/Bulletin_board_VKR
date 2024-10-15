package com.example.bulletin_board.domain.search

interface SearchQueryHandler {
    fun handleSearchQuery(
        inputSearchQuery: String,
        callback: SearchQueryHandlerCallback,
    )
}
