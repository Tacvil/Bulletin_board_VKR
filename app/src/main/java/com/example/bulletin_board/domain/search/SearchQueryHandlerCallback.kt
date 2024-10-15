package com.example.bulletin_board.domain.search

interface SearchQueryHandlerCallback {
    fun onSearchResultsUpdated(results: List<Pair<String, String>>)
}
